package org.underworldlabs.swing;

import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTableModel;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author Alexey Kozlov
 */
public class ForeignKeyCellEditor extends AbstractCellEditor
        implements TableCellEditor {

    private final ForeignKeyPicker picker;
    private JTable table;
    private int col;

    private final int minimumRowHeight;
    private final int minimumColWidth;
    private int oldRowHeightInPixels;
    private int oldColWidthInPixels;

    private final TableModel resultSetTableModel;
    private final int[] childColumnIndices;
    private final int selectedRow;

    public ForeignKeyCellEditor(
            TableModel resultSetTableModel,
            ResultSetTableModel defaultTableModel,
            Vector<Vector<Object>> foreignKeysItems,
            Vector<String> foreignKeysNames,
            Object selectedValue,
            int selectedRow,
            int[] childColumnIndices) {

        this.resultSetTableModel = resultSetTableModel;
        this.childColumnIndices = childColumnIndices;
        this.selectedRow = selectedRow;

        this.picker = new ForeignKeyPicker(
                defaultTableModel,
                foreignKeysItems,
                getForeignNames(foreignKeysNames),
                selectedValue,
                getForeignSelectedValues()
        );

        this.minimumRowHeight = picker.getPreferredSize().height + 1;
        this.minimumColWidth = picker.getPreferredSize().width + 1;
    }

    public void setCellEditorValue(Object value) {
        picker.clear();
        if (value != null)
            this.picker.setText(value.toString());
    }

    private void adjustTableRowHeight() {
        this.oldRowHeightInPixels = table.getRowHeight();

        if (table.getRowHeight() < this.minimumRowHeight)
            table.setRowHeight(this.minimumRowHeight);
    }

    private void adjustTableColWidth() {
        this.oldColWidthInPixels = table.getColumnModel().getColumn(col).getWidth();

        if (table.getColumnModel().getColumn(col).getWidth() < this.minimumColWidth) {
            table.getColumnModel().getColumn(col).setWidth(this.minimumColWidth);
            table.getColumnModel().getColumn(col).setPreferredWidth(this.minimumColWidth);
        }
    }

    private Map<Integer, String> getForeignSelectedValues() {

        Map<Integer, String> foreignSelectedValues = new HashMap<>();
        for (int index : childColumnIndices)
            foreignSelectedValues.put(index, resultSetTableModel.getValueAt(selectedRow, index).toString());

        return foreignSelectedValues;
    }

    private Map<Integer, String> getForeignNames(Vector<String> foreignKeysNames) {

        Map<Integer, String> foreignNames = new HashMap<>();
        for (int i = 0; i < childColumnIndices.length; i++)
            foreignNames.put(childColumnIndices[i], foreignKeysNames.get(i));

        return foreignNames;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        this.col = column;

        setCellEditorValue(((RecordDataItem) value).getDisplayValue());
        adjustTableRowHeight();
        adjustTableColWidth();
        picker.setMinimumSize(new Dimension(0, 0));

        return picker;
    }

    @Override
    public Object getCellEditorValue() {

        if (childColumnIndices.length > 0) {
            for (int i = 0; i < childColumnIndices.length; i++) {

                String newValue = picker.getValueAt(i);
                if (newValue != null)
                    resultSetTableModel.setValueAt(newValue, selectedRow, childColumnIndices[i]);
            }
        }

        return picker.getText();
    }

    private void zRestoreTableRowHeigh() {
        table.setRowHeight(this.oldRowHeightInPixels);
    }

    private void zRestoreTableColWidth() {
        table.getColumnModel().getColumn(col).setWidth(this.oldColWidthInPixels);
        table.getColumnModel().getColumn(col).setPreferredWidth(this.oldColWidthInPixels);
    }

    public void restoreCellSize() {
        zRestoreTableRowHeigh();
        zRestoreTableColWidth();
    }

}
