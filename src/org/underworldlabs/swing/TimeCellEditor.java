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
    private int col;

    private final boolean autoAdjustMinimumTableRowHeight;
    private final boolean autoAdjustMinimumTableColWidth;
    private final int minimumRowHeightInPixels;
    private final int minimumColWidthInPixels;
    private int oldRowHeightInPixels;
    private int oldColWidthInPixels;

    public TimeCellEditor() {
        picker = new EQTimePicker();

        this.autoAdjustMinimumTableRowHeight = true;
        this.autoAdjustMinimumTableColWidth = true;
        this.minimumRowHeightInPixels = picker.getPreferredSize().height + 1;
        this.minimumColWidthInPixels = picker.getPreferredSize().width + 1;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        this.col = column;

        setCellEditorValue(((RecordDataItem) value).getDisplayValue());
        zAdjustTableRowHeightIfNeeded();
        zAdjustTableColWidthIfNeeded();
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

    private void zAdjustTableColWidthIfNeeded() {
        this.oldColWidthInPixels = table.getColumnModel().getColumn(col).getWidth();

        if (this.autoAdjustMinimumTableColWidth) {
            if (table.getColumnModel().getColumn(col).getWidth() < this.minimumColWidthInPixels) {
                table.getColumnModel().getColumn(col).setWidth(this.minimumColWidthInPixels);
                table.getColumnModel().getColumn(col).setPreferredWidth(this.minimumColWidthInPixels);
            }
        }
    }

    private void zRestoreTableRowHeigh() {
        table.setRowHeight(this.oldRowHeightInPixels);
    }

    private void zRestoreTableColWidth() {
        table.getColumnModel().getColumn(col).setWidth(this.oldColWidthInPixels);
        table.getColumnModel().getColumn(col).setPreferredWidth(this.oldColWidthInPixels);
    }

    public EQTimePicker getPicker() {
        return picker;
    }

    public void restoreCellSize() {
        zRestoreTableRowHeigh();
        zRestoreTableColWidth();
    }

}
