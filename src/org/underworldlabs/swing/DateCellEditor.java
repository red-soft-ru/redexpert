package org.underworldlabs.swing;


import com.github.lgooddatepicker.zinternaltools.InternalUtilities;
import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;

public class DateCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final EQDatePicker picker;

    public DateCellEditor() {
        picker = new EQDatePicker();
        this.autoAdjustMinimumTableRowHeight = true;
        this.minimumRowHeightInPixels = picker.getPreferredSize().height + 1;
    }

    private final boolean autoAdjustMinimumTableRowHeight;
    private final int minimumRowHeightInPixels;

    public void setCellEditorValue(Object value) {
        picker.clear();
        if (value != null) {
            if (value instanceof LocalDate)
                picker.setDate((LocalDate) value);
            else if (value instanceof Date) {
                LocalDate nativeValue = ((Date) value).toLocalDate();
                this.picker.setDate(nativeValue);
            } else {
                String text = value.toString();
                String shorterText = InternalUtilities.safeSubstring(text, 0, 100);
                this.picker.setText(shorterText);
            }
        }
    }

    public Object getCellEditorValue() {
        if (picker.getDateStringOrEmptyString().equals(""))
            return null;
        else
            return picker.getDate();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        setCellEditorValue(((RecordDataItem) value).getDisplayValue());
        zAdjustTableRowHeightIfNeeded(table);
        picker.setMinimumSize(new Dimension(0, 0));
        return picker;
    }

    private void zAdjustTableRowHeightIfNeeded(JTable table) {
        if (this.autoAdjustMinimumTableRowHeight) {
            if (table.getRowHeight() < this.minimumRowHeightInPixels) {
                table.setRowHeight(this.minimumRowHeightInPixels);
            }

        }
    }
}