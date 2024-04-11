package org.underworldlabs.swing.celleditor;

import org.executequery.gui.resultset.RecordDataItem;
import org.underworldlabs.swing.celleditor.picker.TimePicker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;

public class TimeCellEditor extends AbstractAdjustableCellEditor
        implements BlockableCellEditor {

    private final TimePicker picker;

    public TimeCellEditor() {
        picker = new TimePicker();
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
        setBlock(true);

        return picker;
    }

    @Override
    public Object getCellEditorValue() {
        return picker.isNull() ? "" : picker.getLocalTime();
    }

    @Override
    public void setBlock(boolean block) {
        picker.setVisibleNullCheck(!block);
        picker.setEnabled(!block && !picker.isNull());
    }

}
