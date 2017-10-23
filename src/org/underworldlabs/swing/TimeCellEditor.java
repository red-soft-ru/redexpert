package org.underworldlabs.swing;

import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.util.EventObject;

public class TimeCellEditor extends AbstractCellEditor implements TableCellEditor {
    EQTimePicker picker;
    public TimeCellEditor() {
        picker = new EQTimePicker();
        this.autoAdjustMinimumTableRowHeight = true;
        this.minimumRowHeightInPixels = picker.getPreferredSize().height + 1;
    }

    private boolean autoAdjustMinimumTableRowHeight;
    private int minimumRowHeightInPixels;

    @Override
    public Component getTableCellEditorComponent(JTable table, Object o, boolean b, int i, int i1) {
        setCellEditorValue((Time) ((RecordDataItem) o).getDisplayValue());
        zAdjustTableRowHeightIfNeeded(table);
        picker.setMinimumSize(new Dimension(0, 0));
        return picker;
    }

    public void setCellEditorValue(Time value) {
        if (value == null) {
            picker.setTime(null);
        } else picker.setTime(value.toLocalTime());
    }

    @Override
    public Object getCellEditorValue() {
        if (!picker.getStringValue().equals(""))
            return Time.valueOf(picker.getStringValue());
        else return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= 2;
        } else {
            return true;
        }
    }

    private void zAdjustTableRowHeightIfNeeded(JTable table) {
        if (this.autoAdjustMinimumTableRowHeight) {
            if (table.getRowHeight() < this.minimumRowHeightInPixels) {
                table.setRowHeight(this.minimumRowHeightInPixels);
            }

        }
    }
}
