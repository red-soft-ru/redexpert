package org.underworldlabs.swing;

import com.github.lgooddatepicker.zinternaltools.CustomPopup;
import com.github.lgooddatepicker.zinternaltools.InternalUtilities;
import com.privatejgoodies.forms.factories.CC;
import com.privatejgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @author Alexey Kozlov
 */
public class ForeignKeyPicker extends JPanel
        implements DefaultDataPicker {

    private Object selectedValue;
    private DefaultTableModel defaultTableModel;
    private final ArrayList<DefaultTableModel> defaultTableModels;

    private CustomPopup popup;
    private JPanel editorPanel;
    private JTextField textField;
    private JButton toggleButton;

    public ForeignKeyPicker(ArrayList<DefaultTableModel> defaultTableModels, Object selectedValue, int columnIndex) {

        this.defaultTableModels = defaultTableModels;

        initCell();
        initPopup(columnIndex);
        setText(selectedValue.toString());
    }

    private void initCell() {

        textField = new JTextField();
        textField.setMargin(new Insets(1, 3, 2, 2));
        textField.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(122, 138, 153)),
                new EmptyBorder(1, 3, 2, 2)));

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

    private void initPopup(int columnIndex) {

        defaultTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (int j = 0; j < defaultTableModels.get(columnIndex).getColumnCount(); j++)
            defaultTableModel.addColumn("");

        for (int i = -1; i < defaultTableModels.get(columnIndex).getRowCount(); i++) {

            defaultTableModel.addRow(new Object[]{});
            for (int j = 0; j < defaultTableModels.get(columnIndex).getColumnCount(); j++)
                if (i != -1)
                    defaultTableModel.setValueAt(defaultTableModels.get(columnIndex).getValueAt(i, j), defaultTableModel.getRowCount() - 1, j);
                else
                    defaultTableModel.setValueAt(defaultTableModels.get(columnIndex).getColumnName(j), defaultTableModel.getRowCount() - 1, j);
        }

    }

    private void setPopupLocation(CustomPopup popup, int defaultX, int defaultY,
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

    private Component getCreateTable() {

        JTable table = new JTable(defaultTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int column = 0; column < table.getColumnCount(); column++) {

            int preferredWidth = table.getColumnModel().getColumn(column).getPreferredWidth();
            int maxWidth = table.getColumnModel().getColumn(column).getMaxWidth();

            for (int row = 0; row < table.getRowCount(); row++) {

                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component component = table.prepareRenderer(cellRenderer, row, column);

                preferredWidth = Math.max(preferredWidth, component.getPreferredSize().width);
                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }

            }

            table.getColumnModel().getColumn(column).setPreferredWidth(preferredWidth);
        }

        int rowIndex = -1;
        for (int row = 0; row < table.getRowCount(); row++) {
            String value = table.getValueAt(row, 0).toString();

            if (value.equals(selectedValue)) {
                rowIndex = row;
                break;
            }
        }

        if (rowIndex > -1)
            table.setRowSelectionInterval(rowIndex, rowIndex);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow > 0) {
                        setText(table.getValueAt(selectedRow, 0).toString());
                        closePopup();
                    }
                }
            }
        });

        return table;
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

            editorPanel = new JPanel(new GridBagLayout());
            editorPanel.add(new JScrollPane(getCreateTable(),
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

            popup = new CustomPopup(this.editorPanel, SwingUtilities.getWindowAncestor(this),
                    this, BorderFactory.createLineBorder(Color.BLACK));

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
        return selectedValue == null ? "" : selectedValue.toString();
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
