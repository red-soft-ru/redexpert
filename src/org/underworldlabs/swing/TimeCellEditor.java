package org.underworldlabs.swing;

import org.executequery.gui.resultset.RecordDataItem;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.EventObject;

public class TimeCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final EQTimePicker picker;
    private JTable table;

    private final boolean autoAdjustMinimumTableRowHeight;
    private final int minimumRowHeightInPixels;
    private int oldRowHeightInPixels;

    public TimeCellEditor() {
        picker = new EQTimePicker();
        this.autoAdjustMinimumTableRowHeight = true;
        this.minimumRowHeightInPixels = picker.getPreferredSize().height + 1;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object o, boolean b, int i, int i1) {
        this.table = table;

        setCellEditorValue(((RecordDataItem) o).getDisplayValue());
        zAdjustTableRowHeightIfNeeded();
        picker.setMinimumSize(new Dimension(0, 0));

        return picker;
    }

    public void setCellEditorValue(Object value) {

        if (value instanceof OffsetTime) {
            OffsetTime nativeValue = (OffsetTime) value;
            this.picker.setTime(nativeValue);

        } else if (value instanceof LocalTime) {
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
                picker.setTime((OffsetTime) null);
        }
    }

    @Override
    public Object getCellEditorValue() {
        if (!picker.getStringValue().isEmpty())
            return picker.timezoneSpinner.isVisible() ? picker.getOffsetTime() : picker.getLocalTime();
        else
            return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent)
            return ((MouseEvent) anEvent).getClickCount() >= 1;
        else
            return true;
    }

    private void zAdjustTableRowHeightIfNeeded() {
        this.oldRowHeightInPixels = table.getRowHeight();

        if (this.autoAdjustMinimumTableRowHeight) {
            if (table.getRowHeight() < this.minimumRowHeightInPixels) {
                table.setRowHeight(this.minimumRowHeightInPixels);
            }
        }
    }

    private void zRestoreTableRowHeigh() {
        table.setRowHeight(this.oldRowHeightInPixels);
    }

    public EQTimePicker getPicker() {
        return picker;
    }

    public void restoreCellSize() {
        zRestoreTableRowHeigh();
    }

}
