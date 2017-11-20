package org.underworldlabs.swing;


import com.github.lgooddatepicker.tableeditors.DateTableEditor;
import com.github.lgooddatepicker.zinternaltools.InternalUtilities;
import org.executequery.gui.resultset.RecordDataItem;

import java.sql.Date;
import java.time.LocalDate;

public class DateCellEditor extends DateTableEditor {
    public DateCellEditor() {
        super();
        this.clickCountToEdit = 1;
    }

    public void setCellEditorValue(Object value) {
        this.getDatePicker().clear();
        if (value != null) {
            RecordDataItem item = (RecordDataItem) value;
            if (!item.isDisplayValueNull()) {
                if (item.getDisplayValue() instanceof Date) {
                    LocalDate nativeValue = ((Date) item.getDisplayValue()).toLocalDate();
                    this.getDatePicker().setDate(nativeValue);
                } else {
                    String text = item.getDisplayValue().toString();
                    String shorterText = InternalUtilities.safeSubstring(text, 0, 100);
                    this.getDatePicker().setText(shorterText);
                }
            }

        }
    }

    public Object getCellEditorValue() {
        if (getDatePicker().getDateStringOrEmptyString().equals(""))
            return null;
        else
            return Date.valueOf(getDatePicker().getDate());
    }
}