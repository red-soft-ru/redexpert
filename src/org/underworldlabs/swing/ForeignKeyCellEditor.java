package org.underworldlabs.swing;

import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Vector;

/**
 * @author Alexey Kozlov
 */
public class ForeignKeyCellEditor extends AbstractCellEditor
        implements TableCellEditor {

    private final ForeignKeyPicker picker;
    private final int minimumRowHeight;
    private final int minimumColWidth;

    public ForeignKeyCellEditor(DefaultTableModel defaultTableModel, Vector<Object> items, Object selectedValue) {

        picker = new ForeignKeyPicker(defaultTableModel, items, selectedValue);
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
        return picker.getText();
    }

}
