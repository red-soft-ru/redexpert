package org.underworldlabs.swing.celleditor;


import com.github.lgooddatepicker.zinternaltools.InternalUtilities;
import org.executequery.gui.resultset.RecordDataItem;
import org.underworldlabs.swing.celleditor.picker.DefaultDatePicker;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;

public class DateCellEditor extends AbstractAdjustableCellEditor {

    private final DefaultDatePicker picker;

    public DateCellEditor() {
        picker = new DefaultDatePicker();
    }

    public void setCellEditorValue(Object value) {
        picker.clear();

        if (value == null)
            return;

        if (value instanceof LocalDate) {
            picker.setDate((LocalDate) value);

        } else if (value instanceof Date) {
            LocalDate nativeValue = ((Date) value).toLocalDate();
            this.picker.setDate(nativeValue);

        } else {
            String text = value.toString();
            String shorterText = InternalUtilities.safeSubstring(text, 0, 100);
            this.picker.setText(shorterText);
        }
    }

    @Override
    public Object getCellEditorValue() {
        return picker.getDateStringOrEmptyString().isEmpty() ? null : picker.getDate();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        setCellEditorValue(((RecordDataItem) value).getDisplayValue());
        adjustCellSize(table, column, picker);

        return picker;
    }

}
