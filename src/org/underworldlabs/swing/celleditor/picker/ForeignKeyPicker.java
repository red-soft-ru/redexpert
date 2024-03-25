package org.underworldlabs.swing.celleditor.picker;

import com.github.lgooddatepicker.zinternaltools.CustomPopup;
import com.github.lgooddatepicker.zinternaltools.InternalUtilities;
import com.privatejgoodies.forms.factories.CC;
import com.privatejgoodies.forms.layout.FormLayout;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Vector;

/**
 * @author Alexey Kozlov
 */
public class ForeignKeyPicker extends JPanel
        implements DataPicker {

    private static final Color BACKGROUND_COLOR = SystemProperties.getColourProperty(
            "user", "editor.results.background.colour"
    );

    private final ResultSetTableModel foreignKeyTableModel;
    private final Vector<Vector<Object>> foreignKeysItems;
    private final Map<Integer, String> foreignKeysNames;
    private final Map<Integer, String> selectedValues;

    private ResultSetTable foreignTable;
    private Object selectedValue;
    private int selectedIndex;

    private CustomPopup popup;
    private JPanel editorPanel;
    private JTextField textField;
    private JButton toggleButton;

    public ForeignKeyPicker(
            ResultSetTableModel foreignKeysTableModel, Vector<Vector<Object>> foreignKeysItems,
            Map<Integer, String> foreignKeysNames, Object selectedValue, Map<Integer, String> selectedValues) {

        this.foreignKeyTableModel = foreignKeysTableModel;
        this.foreignKeysItems = foreignKeysItems;
        this.foreignKeysNames = foreignKeysNames;
        this.selectedValues = selectedValues;

        init();
        setText((selectedValue != null) ? selectedValue.toString() : "");
    }

    private void init() {

        textField = new JTextField();
        textField.setMargin(new Insets(1, 3, 2, 2));
        textField.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(122, 138, 153)),
                new EmptyBorder(1, 3, 2, 2))
        );

        toggleButton = new JButton();
        toggleButton.setText("...");
        toggleButton.setFocusPainted(false);
        toggleButton.setFocusable(false);
        toggleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                openPopup();
            }
        });

        this.setLayout(new FormLayout("pref:grow, [3px,pref], [26px,pref]", "fill:pref:grow"));
        this.add(this.textField, CC.xy(1, 1));
        this.add(toggleButton, CC.xy(3, 1));
    }

    private void setPopupLocation(
            CustomPopup popup, int defaultX, int defaultY,
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
                        setText(foreignKeysItems.get(0).get(selectedIndex).toString());
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

    @Override
    public void openPopup() {

        if (isPopupOpen()) {
            closePopup();
            return;
        }

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

            popup = new CustomPopup(
                    this.editorPanel,
                    SwingUtilities.getWindowAncestor(this),
                    this,
                    BorderFactory.createLineBorder(Color.BLACK)
            );

            if (selectedIndex > -1) {
                foreignTable.setRowSelectionInterval(selectedIndex, selectedIndex);
                foreignTable.scrollRectToVisible(new Rectangle(foreignTable.getCellRect(selectedIndex, 0, true)));
            }

            int defaultX = this.toggleButton.getLocationOnScreen().x + this.toggleButton.getBounds().width - this.popup.getBounds().width - 2;
            int defaultY = this.toggleButton.getLocationOnScreen().y + this.toggleButton.getBounds().height + 2;
            setPopupLocation(this.popup, defaultX, defaultY, this, this.textField);

            popup.show();
            editorPanel.requestFocus();
        }
    }

    @Override
    public boolean isPopupOpen() {
        return popup != null;
    }

    @Override
    public void closePopup() {
        if (popup != null)
            popup.hide();
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
    public void zEventCustomPopupWasClosed(CustomPopup customPopup) {
        popup = null;
        editorPanel = null;
    }

}
