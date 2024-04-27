package org.underworldlabs.swing.celleditor.picker;

import com.github.lgooddatepicker.zinternaltools.InternalUtilities;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.Vector;

/**
 * @author Alexey Kozlov
 */
public class ForeignKeyPicker extends JPanel
        implements PopupDataPicker {

    private static final Color BACKGROUND_COLOR = UserProperties.getInstance()
            .getColourProperty("editor.results.background.colour");

    private final ResultSetTableModel foreignKeyTableModel;
    private final Vector<Vector<Object>> foreignKeysItems;
    private final Map<Integer, String> foreignKeysNames;
    private final Map<Integer, String> selectedValues;

    private ResultSetTable foreignTable;
    private Object selectedValue;
    private int selectedIndex = -1;
    private final int foreignIndex;
    protected boolean isPopupOpen;

    private PickerPopup popup;
    private JPanel editorPanel;
    private JTextField textField;
    private JButton toggleButton;

    public ForeignKeyPicker(
            ResultSetTableModel foreignKeysTableModel,
            Vector<Vector<Object>> foreignKeysItems,
            Map<Integer, String> foreignKeysNames,
            Object selectedValue, Map<Integer,
            String> selectedValues,
            int foreignIndex
    ) {

        this.foreignKeyTableModel = foreignKeysTableModel;
        this.foreignKeysItems = foreignKeysItems;
        this.foreignKeysNames = foreignKeysNames;
        this.selectedValues = selectedValues;
        this.isPopupOpen = false;
        this.foreignIndex = foreignIndex;

        init();
        arrange();
        setText((selectedValue != null) ? selectedValue.toString() : "");
    }

    private void init() {

        textField = WidgetFactory.createTextField("textField");
        textField.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(122, 138, 153)),
                new EmptyBorder(1, 3, 2, 2))
        );

        toggleButton = WidgetFactory.createButton("toggleButton", "...");
        toggleButton.addActionListener(e -> togglePopup());
    }

    private void arrange() {

        GridBagHelper gbh = new GridBagHelper()
                .anchorNorthWest()
                .fillBoth();

        setLayout(new GridBagLayout());
        add(textField, gbh.setMaxWeightX().get());
        add(toggleButton, gbh.nextCol().setMinWeightX().get());

        setPreferredSize(new Dimension(
                getWidth() + toggleButton.getMinimumSize().width,
                toggleButton.getPreferredSize().height
        ));
    }

    private void setPopupLocation(
            PickerPopup popup, int defaultX, int defaultY,
            JComponent picker, JComponent verticalFlipReference) {

        Window topWindowOrNull = SwingUtilities.getWindowAncestor(picker);
        Rectangle workingArea = InternalUtilities.getScreenWorkingArea(topWindowOrNull);

        int popupWidth = popup.getBounds().width;
        int popupHeight = popup.getBounds().height;

        Rectangle popupRectangle = new Rectangle(defaultX, defaultY, popupWidth, popupHeight);

        if (popupRectangle.getMaxY() > workingArea.getMaxY() + (double) 6)
            popupRectangle.y = verticalFlipReference.getLocationOnScreen().y - popupHeight - 2;

        if (popupRectangle.getMaxX() > workingArea.getMaxX())
            popupRectangle.x = (int) ((double) popupRectangle.x - (popupRectangle.getMaxX() - workingArea.getMaxX()));

        if (popupRectangle.getMaxY() > workingArea.getMaxY() + (double) 6)
            popupRectangle.y = (int) ((double) popupRectangle.y - (popupRectangle.getMaxY() - workingArea.getMaxY()));

        if (popupRectangle.x < workingArea.x)
            popupRectangle.x += workingArea.x - popupRectangle.x;

        if (popupRectangle.y < workingArea.y)
            popupRectangle.y += workingArea.y - popupRectangle.y;

        popup.setLocation(popupRectangle.x, popupRectangle.y);
    }

    private ResultSetTable getCreateTable() {

        selectedIndex = -1;
        foreignTable = new ResultSetTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // this is set for the bg of any remaining
        // header region outside the cells themselves
        foreignTable.getTableHeader().setBackground(BACKGROUND_COLOR);

        int rowCount = foreignKeyTableModel.getRowCount();
        if (rowCount > 0) {

            TableSorter sorter = new TableSorter(foreignKeyTableModel);
            foreignTable.setModel(sorter);
            sorter.setTableHeader(foreignTable.getTableHeader());
            foreignTable.applyUserPreferences();
            foreignTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            foreignTable.setTableColumnWidthFromContents();

        } else
            foreignTable.setTableColumnWidth(SystemProperties.getIntProperty("user", "results.table.column.width"));

        boolean notSelected = true;
        for (String s : selectedValues.values()) {
            if (s != null) {
                notSelected = false;
                break;
            }
        }

        if (!notSelected) {
            for (int row = 0; row < foreignTable.getRowCount(); row++) {

                int matchCounter = 0;
                for (int col : selectedValues.keySet()) {

                    RecordDataItem value = (RecordDataItem) foreignTable.getValueAt(row, foreignTable.getColumn(foreignKeysNames.get(col)).getModelIndex());
                    if (value.getValue() != null) {

                        if (value.getValue() instanceof Number && selectedValues.get(col) != null) {
                            if (value.getValue().toString().contentEquals(selectedValues.get(col)))
                                matchCounter++;
                            else
                                break;

                        } else {
                            if (value.getValue().equals(selectedValues.get(col)))
                                matchCounter++;
                            else
                                break;
                        }

                    } else
                        break;
                }

                if (matchCounter == selectedValues.size()) {
                    selectedIndex = row;
                    break;
                }
            }
        }

        foreignTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    selectedIndex = ((TableSorter) foreignTable.getModel()).modelIndex(foreignTable.getSelectedRow());
                    if (selectedIndex > -1) {
                        setText(foreignKeysItems.get(foreignIndex).get(selectedIndex).toString());
                        closePopup();
                    }
                }
            }
        });

        return foreignTable;
    }

    public int getSelectedIndex() {
        return selectedValue != null ? selectedIndex : -1;
    }

    public String getValueAt(int col) {
        return (selectedValue != null && selectedIndex > -1) ? foreignKeysItems.get(col).get(selectedIndex).toString() : textField.getText();
    }

    private void togglePopup() {
        if (isPopupOpen())
            closePopup();
        else
            openPopup();

        isPopupOpen = !isPopupOpen;
    }

    @Override
    public void openPopup() {

        if (isEnabled()) {

            if (!textField.hasFocus())
                textField.requestFocusInWindow();

            ResultSetTable table = getCreateTable();
            JScrollPane scroller = new JScrollPane(
                    table,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            );
            scroller.setBackground(BACKGROUND_COLOR);
            scroller.setBorder(null);
            scroller.getViewport().setBackground(BACKGROUND_COLOR);

            editorPanel = new JPanel(new GridBagLayout());
            editorPanel.add(scroller);

            popup = new PickerPopup(
                    this.editorPanel,
                    SwingUtilities.getWindowAncestor(this),
                    this
            );

            if (selectedIndex > -1) {

                int rowToView = selectedIndex;
                rowToView += foreignTable.getVisibleRect().height / foreignTable.getRowHeight() / 2;

                foreignTable.setRowSelectionInterval(selectedIndex, selectedIndex);
                foreignTable.scrollRectToVisible(foreignTable.getCellRect(rowToView, 0, true));
            }

            int defaultX = this.toggleButton.getLocationOnScreen().x + this.toggleButton.getBounds().width - this.popup.getBounds().width - 2;
            int defaultY = this.toggleButton.getLocationOnScreen().y + this.toggleButton.getBounds().height + 2;
            setPopupLocation(this.popup, defaultX, defaultY, this, this.textField);

            editorPanel.requestFocus();
            popup.show();
        }
    }

    @Override
    public boolean isPopupOpen() {
        return isPopupOpen;
    }

    @Override
    public void closePopup() {
        if (popup != null)
            popup.hide();
    }

    @Override
    public void disposePopup() {
        popup = null;
        editorPanel = null;
    }

    @Override
    public String getText() {
        return selectedValue != null ? selectedValue.toString() : "";
    }

    @Override
    public void setText(String text) {
        selectedValue = text;
        textField.setText(text);
        firePropertyChange("text", null, this.textField.getText());
        textField.requestFocusInWindow();
    }

    @Override
    public void clear() {
        setText(null);
    }

    @Override
    public void setEnabled(boolean enabled) {
        toggleButton.setEnabled(enabled);
        textField.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    private class PickerPopup extends Popup
            implements WindowFocusListener,
            ComponentListener {

        private Window topWindow;
        private JWindow displayWindow;
        private ForeignKeyPicker optionalPanel;
        private boolean enableHideWhenFocusIsLost = false;

        public PickerPopup(
                Component contentsComponent,
                Window topWindow,
                ForeignKeyPicker optionalPanel) {

            this.topWindow = topWindow;
            this.optionalPanel = optionalPanel;

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(contentsComponent, "Center");
            mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            displayWindow = new JWindow(topWindow);
            displayWindow.getContentPane().add(mainPanel);
            displayWindow.setFocusable(true);
            displayWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    enableHideWhenFocusIsLost = true;
                }
            });
            displayWindow.pack();

            String cancelName = "cancel";
            InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            inputMap.put(KeyStroke.getKeyStroke(27, 0), cancelName);
            mainPanel.getActionMap().put(cancelName, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    hide();
                }
            });

            this.topWindow.addComponentListener(this);
            this.displayWindow.addWindowFocusListener(this);
        }

        @Override
        public void show() {
            this.displayWindow.setVisible(true);
        }

        @Override
        public void hide() {

            if (this.displayWindow != null) {
                this.displayWindow.setVisible(false);
                this.displayWindow.removeWindowFocusListener(this);
                this.displayWindow = null;
            }

            if (this.topWindow != null) {
                this.topWindow.removeComponentListener(this);
                this.topWindow = null;
            }

            if (this.optionalPanel != null) {
                this.optionalPanel.disposePopup();
                this.optionalPanel = null;
            }
        }

        public Rectangle getBounds() {
            return displayWindow.getBounds();
        }

        public void setLocation(int xPos, int yPos) {
            displayWindow.setLocation(xPos, yPos);
        }

        // --- component listener impl ---

        @Override
        public void componentHidden(ComponentEvent e) {
            hide();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            hide();
        }

        @Override
        public void componentResized(ComponentEvent e) {
            hide();
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        // --- window focus listener impl ---

        @Override
        public void windowGainedFocus(WindowEvent e) {
        }

        @Override
        public void windowLostFocus(WindowEvent e) {

            if (!this.enableHideWhenFocusIsLost) {
                e.getWindow().requestFocus();
                return;
            }

            if (e.getOppositeWindow() == null)
                isPopupOpen = false;

            hide();
        }

    } // ForeignKeyPickerPopup class

}
