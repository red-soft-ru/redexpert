package org.underworldlabs.swing.celleditor;

import org.executequery.gui.resultset.RecordDataItem;
import org.underworldlabs.swing.celleditor.picker.DefaultTimePicker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.EventObject;

public class TimeCellEditor extends AbstractAdjustableCellEditor {

    private final DefaultTimePicker picker;

    public TimeCellEditor() {
        picker = new DefaultTimePicker();
    }

    public void setCellEditorValue(Object value) {

        if (value instanceof LocalTime) {
            LocalTime nativeValue = (LocalTime) value;
            this.picker.setTime(nativeValue);

        } else {

            if (value instanceof Time) {
                picker.setTime(((Time) value).toLocalTime());

            } else if (value instanceof String) {
                String origin = (String) value;
                OffsetTime offsetTime = MiscUtils.convertToOffsetTime(origin);
                picker.setTime(offsetTime.toLocalTime());
            }

            if (value == null)
                picker.setTime(null);
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        setCellEditorValue(((RecordDataItem) value).getDisplayValue());
        adjustCellSize(table, column, picker);

        return picker;
    }

    @Override
    public Object getCellEditorValue() {
        return picker.getStringValue().isEmpty() ? null : picker.getLocalTime();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent)
            return ((MouseEvent) anEvent).getClickCount() >= 1;
        else
            return true;
    }

}
