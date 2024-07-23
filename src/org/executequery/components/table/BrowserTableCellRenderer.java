package org.executequery.components.table;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.IconManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Mikhail Kalyashin on 22.04.2017.
 */
public class BrowserTableCellRenderer extends JLabel
        implements TableCellRenderer,
        Serializable {

    private final UIDefaults uiDefaults;
    private Color unselectedForeground;
    private Color unselectedBackground;

    public BrowserTableCellRenderer() {
        super();
        uiDefaults = new UIDefaults();

        setOpaque(true);
        setBorder(getNoFocusBorder());
        setName("Table.cellRenderer");
    }

    private Border getNoFocusBorder() {

        Border border = uiDefaults.getBorder("Table.cellNoFocusBorder");
        if (border != null)
            return border;

        return new EmptyBorder(1, 1, 1, 1);
    }

    private void setValue(Object value) {

        if (value instanceof Icon) {
            setText("");
            setIcon((Icon) value);
            setHorizontalAlignment(JLabel.CENTER);

        } else if (value instanceof DatabaseColumn) {
            setIcon(IconManager.getIconFromDatabaseColumn((DatabaseColumn) value));
            setText(((NamedObject) value).getName());
            setHorizontalAlignment(JLabel.LEFT);

        } else if (value instanceof NamedObject) {
            setIcon(IconManager.getIconFromType(((NamedObject) value).getType()));
            setText(((NamedObject) value).getName());
            setHorizontalAlignment(JLabel.LEFT);

        } else {
            setIcon(null);
            setText((String) value);
            setHorizontalAlignment(JLabel.LEFT);
        }
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to assign
     * the unselected-foreground color to the specified color.
     *
     * @param c set the foreground color to this value
     */
    @Override
    public void setForeground(Color c) {
        super.setForeground(c);
        unselectedForeground = c;
    }

    /**
     * Overrides <code>JComponent.setBackground</code> to assign
     * the unselected-background color to the specified color.
     *
     * @param c set the background color to this value
     */
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        unselectedBackground = c;
    }

    /**
     * Notification from the <code>UIManager</code> that the look and feel
     * [L&amp;F] has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>.
     *
     * @see JComponent#updateUI
     */
    @Override
    public void updateUI() {
        super.updateUI();
        setForeground(null);
        setBackground(null);
    }

    /**
     * Returns the default table cell renderer.
     * <p>
     * During a printing operation, this method will be called with
     * <code>isSelected</code> and <code>hasFocus</code> values of
     * <code>false</code> to prevent selection and focus from appearing
     * in the printed output. To do other customization based on whether
     * the table is being printed, check the return value from
     * {@link javax.swing.JComponent#isPaintingForPrint()}.
     *
     * @param table      the <code>JTable</code>
     * @param value      the value to assign to the cell at
     *                   <code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus   true if cell has focus
     * @param row        the row of the cell to render
     * @param column     the column of the cell to render
     * @return the default table cell renderer
     * @see javax.swing.JComponent#isPaintingForPrint()
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (table == null)
            return this;

        if (value == null)
            value = table.getModel().getValueAt(row, column);

        Color foregroundColor = null;
        if (value.getClass().equals(Object[].class)) {
            foregroundColor = (Color) ((Object[]) value)[1];
            value = ((Object[]) value)[0];
        }

        if (hasFocus) {
            Border border = null;

            if (isSelected) {
                border = uiDefaults.getBorder("Table.focusSelectedCellHighlightBorder");

                super.setBackground(table.getSelectionBackground());
                super.setForeground(foregroundColor == null ?
                        table.getSelectionForeground()
                        : foregroundColor
                );
            }

            if (border == null)
                border = uiDefaults.getBorder("Table.focusCellHighlightBorder");
            setBorder(border);

            if (!isSelected && table.isCellEditable(row, column)) {

                Color color = uiDefaults.getColor("Table.focusCellForeground");
                if (color != null)
                    super.setForeground(color);

                color = uiDefaults.getColor("Table.focusCellBackground");
                if (color != null)
                    super.setBackground(color);
            }

        } else {
            Color background = unselectedBackground != null ? unselectedBackground : table.getBackground();

            if (background == null || background instanceof javax.swing.plaf.UIResource) {
                Color alternateColor = uiDefaults.getColor("Table.alternateRowColor");
                if (alternateColor != null && row % 2 != 0)
                    background = alternateColor;
            }

            if (foregroundColor == null)
                foregroundColor = unselectedForeground != null ? unselectedForeground : table.getForeground();

            super.setForeground(foregroundColor);
            super.setBackground(background);
            setBorder(getNoFocusBorder());
        }

        setFont(table.getFont());
        setValue(value);

        return this;
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {

        if (Objects.equals(propertyName, "text")
                || Objects.equals(propertyName, "labelFor")
                || Objects.equals(propertyName, "displayedMnemonic")
                || ((Objects.equals(propertyName, "font")
                || Objects.equals(propertyName, "foreground"))
                && oldValue != newValue
                && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public boolean isOpaque() {

        Component parent = getParent();
        if (parent != null)
            parent = parent.getParent();

        Color background = getBackground();
        boolean colorMatch = background != null
                && parent != null
                && background.equals(parent.getBackground())
                && parent.isOpaque();

        return !colorMatch && super.isOpaque();
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void invalidate() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void validate() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void revalidate() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(Rectangle r) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void repaint() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

}
