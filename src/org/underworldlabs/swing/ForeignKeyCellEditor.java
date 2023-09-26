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
    private final int minimumRowHeight;
    private final int minimumColWidth;

    private final TableModel resultSetTableModel;
    private final int[] childColumnIndices;
    private final int selectedRow;

    public ForeignKeyCellEditor(TableModel resultSetTableModel, ResultSetTableModel defaultTableModel,
                                Vector<Vector<Object>> foreignKeysItems, Vector<String> foreignKeysNames, Object selectedValue, int selectedRow,
                                int[] childColumnIndices) {

        this.resultSetTableModel = resultSetTableModel;
        this.selectedRow = selectedRow;
        this.childColumnIndices = childColumnIndices;

        picker = new ForeignKeyPicker(defaultTableModel, foreignKeysItems, getForeignNames(foreignKeysNames),
                selectedValue, getForeignSelectedValues());
        this.minimumRowHeight = picker.getPreferredSize().height + 1;
        this.minimumColWidth = picker.getPreferredSize().width + 1;
    }

    public void setCellEditorValue(Object value) {
        picker.clear();
        if (value != null)
            this.picker.setText(value.toString());
    }

    private void adjustTableRowHeight(JTable table) {
        if (table.getRowHeight() < this.minimumRowHeight) {
            table.setRowHeight(this.minimumRowHeight);
        }
    }

    private void adjustTableColWidth(JTable table, int col) {
        if (table.getColumnModel().getColumn(col).getWidth() < this.minimumColWidth) {
            table.getColumnModel().getColumn(col).setWidth(this.minimumColWidth);
            table.getColumnModel().getColumn(col).setPreferredWidth(this.minimumColWidth);
        }
    }

    private Map<Integer, String> getForeignSelectedValues() {

        Map <Integer, String> foreignSelectedValues = new HashMap<>();
        for (int index : childColumnIndices)
            foreignSelectedValues.put(index, resultSetTableModel.getValueAt(selectedRow, index).toString());

        return foreignSelectedValues;
    }

    private Map<Integer, String> getForeignNames(Vector<String> foreignKeysNames) {

        Map <Integer, String> foreignNames = new HashMap<>();
        for (int i = 0; i < childColumnIndices.length; i++)
            foreignNames.put(childColumnIndices[i], foreignKeysNames.get(i));

        return foreignNames;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        setCellEditorValue(((RecordDataItem) value).getDisplayValue());
        adjustTableRowHeight(table);
        adjustTableColWidth(table, column);
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

}
