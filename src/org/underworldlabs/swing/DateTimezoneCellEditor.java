package org.underworldlabs.swing;

import com.github.lgooddatepicker.zinternaltools.InternalUtilities;
import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.EventObject;

public class DateTimezoneCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    private final EQDateTimezonePicker dateTimezonePicker;
    private JTable table;
    private int col;

    private final int minimumRowHeightInPixels;
    private final int minimumColWidthInPixels;
    private int oldRowHeightInPixels;
    private int oldColWidthInPixels;

    public DateTimezoneCellEditor() {

        dateTimezonePicker = new EQDateTimezonePicker();
        dateTimezonePicker.getDatePicker().getSettings().setGapBeforeButtonPixels(0);

        minimumRowHeightInPixels = dateTimezonePicker.getPreferredSize().height;
        minimumColWidthInPixels = dateTimezonePicker.getPreferredSize().width;
    }

    @Override
    public Object getCellEditorValue() {

        if (dateTimezonePicker.getStringValue().isEmpty())
            return null;

        return dateTimezonePicker.getOffsetDateTime();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        this.col = column;

        adjustTableColWidth();
        adjustTableRowHeight();
        setCellEditorValue(value);

        return dateTimezonePicker;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.table = table;
        this.col = column;

        Color color = isSelected ? table.getSelectionBackground() : table.getBackground();
        dateTimezonePicker.setBackground(color);
        dateTimezonePicker.getTimezonePicker().setBackground(color);
        dateTimezonePicker.getDatePicker().setBackground(color);
        dateTimezonePicker.getDatePicker().getComponentDateTextField().setBackground(color);

        setCellEditorValue(value);
        adjustTableRowHeight();
        adjustTableColWidth();

        return dateTimezonePicker;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        return !(e instanceof MouseEvent) || ((MouseEvent) e).getClickCount() >= 1;
    }

    public void setCellEditorValue(Object value) {

        if (value == null) {
            dateTimezonePicker.setDateTime((OffsetDateTime) null);
            return;
        }

        if (value instanceof LocalDateTime) {
            dateTimezonePicker.setDateTime((LocalDateTime) value);
            return;
        }

        if (value instanceof RecordDataItem) {
            RecordDataItem item = ((RecordDataItem) value);

            if (item.getDisplayValue() instanceof LocalDateTime) {
                dateTimezonePicker.setDateTime((LocalDateTime) item.getDisplayValue());

            } else if (item.getDisplayValue() instanceof OffsetDateTime) {
                dateTimezonePicker.setDateTime((OffsetDateTime) item.getDisplayValue());

            } else if (item.getDisplayValue() instanceof Timestamp) {
                dateTimezonePicker.setDateTime(((Timestamp) item.getDisplayValue()).toLocalDateTime());

            } else if (item.getDisplayValue() instanceof String) {

                String dateTime = (String) item.getDisplayValue();
                String date = dateTime.substring(0, dateTime.indexOf(' '));

                int indexTimezone = dateTime.indexOf('+');
                if (indexTimezone < 0)
                    indexTimezone = dateTime.indexOf('-');

                String time = dateTime.substring(dateTime.indexOf(' ') + 1, indexTimezone);
                LocalDateTime localDateTime = Timestamp.valueOf(date + " " + time).toLocalDateTime();

                dateTimezonePicker.setDateTime(localDateTime);

            } else
                dateTimezonePicker.setDateTime((OffsetDateTime) null);

        } else {
            String shorterText = InternalUtilities.safeSubstring(value.toString(), 0, 100);
            dateTimezonePicker.getDatePicker().setText(shorterText);
        }
    }

    private void adjustTableRowHeight() {
        oldRowHeightInPixels = table.getRowHeight();

        if (table.getRowHeight() < minimumRowHeightInPixels) {
            table.setRowHeight(minimumRowHeightInPixels);
        }
    }

    private void adjustTableColWidth() {
        oldColWidthInPixels = table.getColumnModel().getColumn(col).getWidth();

        if (table.getColumnModel().getColumn(col).getWidth() < minimumColWidthInPixels) {
            table.getColumnModel().getColumn(col).setWidth(minimumColWidthInPixels);
            table.getColumnModel().getColumn(col).setPreferredWidth(minimumColWidthInPixels);
        }
    }

    private void zRestoreTableRowHeigh() {
        table.setRowHeight(oldRowHeightInPixels);
    }

    private void zRestoreTableColWidth() {
        table.getColumnModel().getColumn(col).setWidth(oldColWidthInPixels);
        table.getColumnModel().getColumn(col).setPreferredWidth(oldColWidthInPixels);
    }

    public void restoreCellSize() {
        zRestoreTableRowHeigh();
        zRestoreTableColWidth();
    }

}
