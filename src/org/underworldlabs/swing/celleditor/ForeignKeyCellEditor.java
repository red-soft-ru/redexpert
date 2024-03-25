package org.underworldlabs.swing.celleditor;

import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.underworldlabs.swing.celleditor.picker.ForeignKeyPicker;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author Alexey Kozlov
 */
public class ForeignKeyCellEditor extends AbstractAdjustableCellEditor {

    private final ForeignKeyPicker picker;

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
    }

    public void setCellEditorValue(Object value) {
        picker.clear();
        if (value != null)
            this.picker.setText(value.toString());
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

        setCellEditorValue(((RecordDataItem) value).getDisplayValue());
        adjustCellSize(table, column, picker);

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
