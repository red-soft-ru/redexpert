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
    private JTable table;
    private int col;

    private final boolean autoAdjustMinimumTableRowHeight;
    private final boolean autoAdjustMinimumTableColWidth;
    private final int minimumRowHeightInPixels;
    private final int minimumColWidthInPixels;
    private int oldRowHeightInPixels;
    private int oldColWidthInPixels;

    public DateCellEditor() {
        picker = new EQDatePicker();

        this.autoAdjustMinimumTableRowHeight = true;
        this.autoAdjustMinimumTableColWidth = true;
        this.minimumRowHeightInPixels = picker.getPreferredSize().height + 1;
        this.minimumColWidthInPixels = picker.getPreferredSize().width + 1;
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
        this.table = table;
        this.col = column;

        setCellEditorValue(((RecordDataItem) value).getDisplayValue());
        zAdjustTableRowHeightIfNeeded();
        zAdjustTableColWidthIfNeeded();
        picker.setMinimumSize(new Dimension(0, 0));

        return picker;
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

    public void restoreCellSize() {
        zRestoreTableRowHeigh();
        zRestoreTableColWidth();
    }

}
