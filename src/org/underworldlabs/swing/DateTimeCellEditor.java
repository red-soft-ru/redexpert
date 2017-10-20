package org.underworldlabs.swing;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.github.lgooddatepicker.zinternaltools.InternalUtilities;
import com.privatejgoodies.forms.layout.ConstantSize;
import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.EventObject;

public class DateTimeCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    private boolean autoAdjustMinimumTableRowHeight;
    public int clickCountToEdit;
    private boolean matchTableBackgroundColor;
    private boolean matchTableSelectionBackgroundColor;
    private Border borderFocusedCell;
    private Border borderUnfocusedCell;
    private EQDateTimePicker dateTimePicker;
    private int minimumRowHeightInPixels;

    public DateTimeCellEditor() {
        this(true, true, true);
    }

    public DateTimeCellEditor(boolean autoAdjustMinimumTableRowHeight, boolean matchTableBackgroundColor, boolean matchTableSelectionBackgroundColor) {
        this.autoAdjustMinimumTableRowHeight = true;
        this.clickCountToEdit = 1;
        this.matchTableBackgroundColor = true;
        this.matchTableSelectionBackgroundColor = true;
        this.autoAdjustMinimumTableRowHeight = autoAdjustMinimumTableRowHeight;
        this.matchTableBackgroundColor = matchTableBackgroundColor;
        this.matchTableSelectionBackgroundColor = matchTableSelectionBackgroundColor;
        JLabel exampleDefaultRenderer = (JLabel) (new DefaultTableCellRenderer()).getTableCellRendererComponent(new JTable(), "", true, true, 0, 0);
        this.borderFocusedCell = exampleDefaultRenderer.getBorder();
        this.borderUnfocusedCell = new EmptyBorder(1, 1, 1, 1);
        this.dateTimePicker = new EQDateTimePicker();
        this.dateTimePicker.setBorder(this.borderUnfocusedCell);
        this.dateTimePicker.setBackground(Color.white);
        this.dateTimePicker.datePicker.setBackground(Color.white);
        this.dateTimePicker.timePicker.setBackground(Color.white);
        this.dateTimePicker.datePicker.getComponentDateTextField().setBorder((Border) null);
        DatePickerSettings dateSettings = this.dateTimePicker.datePicker.getSettings();
        dateSettings.setGapBeforeButtonPixels(Integer.valueOf(0));
        dateSettings.setSizeTextFieldMinimumWidthDefaultOverride(false);
        dateSettings.setSizeTextFieldMinimumWidth(Integer.valueOf(20));
        this.minimumRowHeightInPixels = this.dateTimePicker.getPreferredSize().height + 1;
    }

    public Object getCellEditorValue() {
        if (this.dateTimePicker.getStringValue().equals(""))
            return null;
        return Timestamp.valueOf(this.dateTimePicker.getStringValue());
    }

    public EQDateTimePicker getDateTimePicker() {
        return this.dateTimePicker;
    }

    public DatePickerSettings getDatePickerSettings() {
        return this.dateTimePicker.datePicker.getSettings();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.setCellEditorValue(value);
        this.zAdjustTableRowHeightIfNeeded(table);
        this.dateTimePicker.datePicker.getComponentDateTextField().setScrollOffset(0);
        this.dateTimePicker.timePicker.setMinimumSize(new Dimension(0, 0));
        return this.dateTimePicker;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setCellEditorValue(value);
        Color tableBackground;
        if (isSelected) {
            if (this.matchTableSelectionBackgroundColor) {
                tableBackground = table.getSelectionBackground();
                this.dateTimePicker.setBackground(tableBackground);
                this.dateTimePicker.datePicker.setBackground(tableBackground);
                this.dateTimePicker.timePicker.setBackground(tableBackground);
                this.dateTimePicker.datePicker.getComponentDateTextField().setBackground(tableBackground);
                this.dateTimePicker.timePicker.setBackground(tableBackground);
            } else {
                this.dateTimePicker.datePicker.zDrawTextFieldIndicators();
            }
        }

        if (!isSelected) {
            if (this.matchTableBackgroundColor) {
                tableBackground = table.getBackground();
                this.dateTimePicker.setBackground(tableBackground);
                this.dateTimePicker.datePicker.setBackground(tableBackground);
                this.dateTimePicker.timePicker.setBackground(tableBackground);
                this.dateTimePicker.datePicker.getComponentDateTextField().setBackground(tableBackground);
                this.dateTimePicker.timePicker.setBackground(tableBackground);
            } else {
                this.dateTimePicker.datePicker.zDrawTextFieldIndicators();
            }
        }

        if (hasFocus) {
            this.dateTimePicker.setBorder(this.borderFocusedCell);
        } else {
            this.dateTimePicker.setBorder(this.borderUnfocusedCell);
        }

        this.zAdjustTableRowHeightIfNeeded(table);
        this.dateTimePicker.datePicker.getComponentDateTextField().setScrollOffset(0);
        return this.dateTimePicker;
    }

    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= this.clickCountToEdit;
        } else {
            return true;
        }
    }

    public void setCellEditorValue(Object value) {
        this.dateTimePicker.clear();
        if (value != null) {
            if (value instanceof LocalDateTime) {
                LocalDateTime nativeValue = (LocalDateTime) value;
                this.dateTimePicker.setDateTimePermissive(nativeValue);
            } else {
                if (value instanceof RecordDataItem) {
                    RecordDataItem item = ((RecordDataItem) value);
                    Timestamp dtvalue = (Timestamp) item.getDisplayValue();
                    if (dtvalue != null)
                        dateTimePicker.setDateTimePermissive(dtvalue.toLocalDateTime());
                    else dateTimePicker.setDateTimePermissive(null);
                } else {
                    String text = value.toString();
                    String shorterText = InternalUtilities.safeSubstring(text, 0, 100);
                    this.dateTimePicker.datePicker.setText(shorterText);
                }

            }

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