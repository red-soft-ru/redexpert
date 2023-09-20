/*
 * BrowsingCellEditor.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.components.table;

import org.executequery.Constants;
import org.underworldlabs.swing.table.TableCellEditorValue;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.EventObject;

/**
 * Table cell editor with a button to the right for option
 * selection or similar.
 *
 * @author Takis Diakoumis
 */
public abstract class BrowsingCellEditor extends DefaultCellEditor
        implements TableCellEditorValue,
        TableCellRenderer,
        TableCellEditor,
        ActionListener,
        FocusListener {

    private final int BUTTON_WIDTH = 16;

    protected transient ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();
    protected EditorDelegate delegate;

    /**
     * the selection button
     */
    protected BrowseButton browseButton;

    /**
     * the editor component
     */
    protected JTextField textField;

    /**
     * the editor component insets
     */
    protected static Insets textFieldInsets;

    /**
     * the editor component focus border
     */
    protected static Border textFieldFocusBorder;

    /**
     * the editor component focus border colour
     */
    protected static Color focusBorderColor;

    /**
     * the selection button background colour
     */
    protected static Color buttonBackground;

    /**
     * the selection button icon colour
     */
    protected static Color iconColor;

    /**
     * the base panel the components are rendered onto
     */
    private final RendererBasePanel base;

    /**
     * Indicates this has focus
     */
    private static boolean hasFocusOnLabel;

    static {
        Border focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
        if (focusBorder instanceof LineBorder) {
            focusBorderColor = ((LineBorder) focusBorder).getLineColor();
            textFieldFocusBorder = BorderFactory.createMatteBorder(1, 1, 1, 0, focusBorderColor);
        }
        textFieldInsets = new Insets(0, 2, 0, 0);
        iconColor = Color.DARK_GRAY.darker();
    }

    public BrowsingCellEditor() {

        super(new CellTextField());

        base = new RendererBasePanel();
        delegate = new EditorDelegate();

        textField = (CellTextField) editorComponent;
        textField.setBorder(null);
        textField.setMargin(textFieldInsets);
        textField.setDisabledTextColor(UIManager.getColor("Table.foreground"));
        textField.addActionListener(delegate);
        textField.addFocusListener(this);

        browseButton = new BrowseButton();
        browseButton.addActionListener(this);

        base.add(textField, BorderLayout.CENTER);
        base.add(browseButton, BorderLayout.EAST);
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {

        hasFocusOnLabel = false;
        textField.setFont(table.getFont());
        delegate.setValue(value);
        textField.setEnabled(true);

        return base;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean cellHasFocus, int row, int col) {

        hasFocusOnLabel = cellHasFocus;
        textField.setEnabled(false);
        buttonBackground = table.getBackground();
        textField.setBackground(buttonBackground);
        base.setBackground(buttonBackground);

        delegate.setValue(value);

        return base;
    }

    public void setDelegateValue(Object value) {
        delegate.setValue(value);
    }

    public void setFont(Font font) {
        textField.setFont(font);
    }

    /**
     * Returns the current editor value from the component
     * defining this object.
     *
     * @return the editor's value
     */
    @Override
    public String getEditorValue() {
        return getCellEditorValue().toString();
    }

    public void addKeyListener(KeyListener listener) {
        if (textField != null)
            textField.addKeyListener(listener);
    }

    @SuppressWarnings("unused")
    public void removeKeyListener(KeyListener listener) {
        if (textField != null)
            textField.removeKeyListener(listener);
    }

    /**
     * Defines the action to be taken upon activation of the
     * selection button.
     *
     * @param e the event
     */
    @Override
    public abstract void actionPerformed(ActionEvent e);

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        fireEditingStopped();
    }

    /**
     * Returns a reference to the editor component.
     *
     * @return the editor <code>Component</code>
     */
    @Override
    public Component getComponent() {
        return textField;
    }

    // -------------------------------------------
    // borrowed from javax.swing.DefaultCellEditor
    // -------------------------------------------

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }

    @Override
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
    }

    @Override
    protected void fireEditingStopped() {

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {

                // Lazily create the event:
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);

                ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
            }
        }

        super.fireEditingStopped();
    }

    @Override
    protected void fireEditingCanceled() {

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {

                // Lazily create the event:
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);

                ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
            }
        }

        super.fireEditingCanceled();
    }

    @Override
    public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return delegate.isCellEditable(anEvent);
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return delegate.shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        return delegate.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        delegate.cancelCellEditing();
    }

    // ----------------------------------

    protected static class RendererBasePanel extends JPanel {

        public RendererBasePanel() {
            super(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            // check for a selection border
            if (hasFocusOnLabel && focusBorderColor != null) {
                g.setColor(focusBorderColor);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        }

    } // RendererBasePanel class

    protected static class CellTextField extends JTextField {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            // check for a selection border
            if (hasFocusOnLabel && focusBorderColor != null) {
                int width = getWidth() - 1;
                int height = getHeight() - 1;
                g.setColor(focusBorderColor);
                g.drawLine(0, 0, width, 0); // top
                g.drawLine(0, height, width, height); // bottom
            }
        }

    } // CellTextField class

    protected class BrowseButton extends JButton
            implements MouseListener {

        public BrowseButton() {

            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(true);

            try {
                setUI(new javax.swing.plaf.basic.BasicButtonUI());
            } catch (NullPointerException e) {
                e.printStackTrace(System.out);
            }
        }

        @Override
        public void paintComponent(Graphics g) {

            int width = getWidth();
            int height = getHeight();

            g.setColor(buttonBackground);
            g.fillRect(1, 1, width - 2, height - 1);
            g.setColor(iconColor);
            g.drawRect(2, 3, width - 5, height - 5);

            int y = height - 5;
            int x = ((width - (width - 4)) / 2) + 4;

            g.setColor(Color.BLACK);
            g.drawLine(x, y, x, y);
            g.drawLine(x + 3, y, x + 3, y);
            g.drawLine(x + 6, y, x + 6, y);
        }

        @Override
        public boolean isFocusTraversable() {
            return false;
        }

        @Override
        public int getHeight() {
            return super.getHeight() - 2;
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(BUTTON_WIDTH + 2, getHeight());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            actionPerformed(new ActionEvent(this, -1, Constants.EMPTY));
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void requestFocus() {
        }

    } // BrowseButton class


    // ----------------------------------------------------------
    // borrowed from javax.swing.DefaultCellEditor.EditorDelegate
    // ----------------------------------------------------------

    private class EditorDelegate
            implements ActionListener,
            ItemListener,
            Serializable {

        /**
         * The value of this cell.
         */
        protected Object value;

        /**
         * Returns the value of this cell.
         *
         * @return the value of this cell
         */
        public Object getCellEditorValue() {
            return textField.getText();
        }

        /**
         * Sets the value of this cell.
         *
         * @param value the new value of this cell
         */
        public void setValue(Object value) {
            this.value = value;
            textField.setText((value != null) ? value.toString() : Constants.EMPTY);
        }

        /**
         * Returns true if <code>anEvent</code> is <b>not</b> a
         * <code>MouseEvent</code>.  Otherwise, it returns true
         * if the necessary number of clicks have occurred, and
         * returns false otherwise.
         *
         * @param anEvent the event
         * @return true  if cell is ready for editing, false otherwise
         * @see #setClickCountToStart
         * @see #shouldSelectCell
         */
        public boolean isCellEditable(EventObject anEvent) {

            if (anEvent instanceof MouseEvent) {
                MouseEvent mEvent = (MouseEvent) anEvent;

                Point point = mEvent.getPoint();
                JTable table = (JTable) mEvent.getSource();
                Rectangle cellRect = table.getCellRect(
                        table.rowAtPoint(point), table.columnAtPoint(point),true);

                if (mEvent.getX() >= (cellRect.x + cellRect.width - BUTTON_WIDTH))
                    return true;

                return mEvent.getClickCount() >= 2;
            }

            return true;
        }

        /**
         * Returns true to indicate that the editing cell may
         * be selected.
         *
         * @param anEvent the event
         * @return true
         * @see #isCellEditable
         */
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        /**
         * Returns true to indicate that editing has begun.
         *
         * @param anEvent the event
         */
        @SuppressWarnings("unused")
        public boolean startCellEditing(EventObject anEvent) {
            return true;
        }

        /**
         * Stops editing and
         * returns true to indicate that editing has stopped.
         * This method calls <code>fireEditingStopped</code>.
         *
         * @return true
         */
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        /**
         * Cancels editing.  This method calls <code>fireEditingCanceled</code>.
         */
        public void cancelCellEditing() {
            fireEditingCanceled();
        }

        /**
         * When an action is performed, editing is ended.
         *
         * @param e the action event
         * @see #stopCellEditing
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            BrowsingCellEditor.this.stopCellEditing();
        }

        /**
         * When an item's state changes, editing is ended.
         *
         * @param e the action event
         * @see #stopCellEditing
         */
        @Override
        public void itemStateChanged(ItemEvent e) {
            BrowsingCellEditor.this.stopCellEditing();
        }
    }

} // EditorDelegate class
