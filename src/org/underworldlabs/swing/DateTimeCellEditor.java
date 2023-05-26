package org.underworldlabs.swing;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.zinternaltools.InternalUtilities;
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
import java.time.OffsetDateTime;
import java.util.EventObject;

public class DateTimeCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    private boolean autoAdjustMinimumTableRowHeight;
    private final boolean autoAdjustMinimumTableColWidth;
    public int clickCountToEdit;
    private boolean matchTableBackgroundColor;
    private boolean matchTableSelectionBackgroundColor;
    private final Border borderFocusedCell;
    private final Border borderUnfocusedCell;
    private final EQDateTimePicker dateTimePicker;
    private final int minimumRowHeightInPixels;
    private final int minimumColWidthInPixels;


    public DateTimeCellEditor() {
        this(true, true, true);
    }

    public DateTimeCellEditor(boolean autoAdjustMinimumTableRowHeight, boolean matchTableBackgroundColor, boolean matchTableSelectionBackgroundColor) {
        this.autoAdjustMinimumTableRowHeight =true;
        this.autoAdjustMinimumTableColWidth = true;
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
        dateTimePicker.setVisibleNullBox(true);
        dateTimePicker.setVisibleTimeZone(true);
        this.dateTimePicker.setBorder(this.borderUnfocusedCell);
        this.dateTimePicker.setBackground(Color.white);
        this.dateTimePicker.datePicker.setBackground(Color.white);
        this.dateTimePicker.timePicker.setBackground(Color.white);
        this.dateTimePicker.datePicker.getComponentDateTextField().setBorder(null);
        DatePickerSettings dateSettings = this.dateTimePicker.datePicker.getSettings();
        dateSettings.setGapBeforeButtonPixels(Integer.valueOf(0));
        dateSettings.setSizeTextFieldMinimumWidthDefaultOverride(false);
        dateSettings.setSizeTextFieldMinimumWidth(Integer.valueOf(20));
        this.minimumRowHeightInPixels = this.dateTimePicker.getPreferredSize().height + 1;
        this.minimumColWidthInPixels = this.dateTimePicker.getPreferredSize().width + 1
        ;
    }

    public Object getCellEditorValue() {
        if (this.dateTimePicker.getStringValue().equals(""))
            return null;
        if (dateTimePicker.timePicker.timezoneSpinner.isVisible()) {
            return this.dateTimePicker.getOffsetDateTime();
        }
        try {
            return Timestamp.valueOf(this.dateTimePicker.getStringValue());
        } catch (IllegalArgumentException e) {
            return this.dateTimePicker.getStringValue();
        }
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
        this.zAdjustTableColWidthIfNeeded(table,column);
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
        this.zAdjustTableColWidthIfNeeded(table,column);
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
                    if (item.getDisplayValue() instanceof LocalDateTime) {
                        LocalDateTime nativeValue = (LocalDateTime) item.getDisplayValue();
                        this.dateTimePicker.setDateTimePermissive(nativeValue);
                    } else if (item.getDisplayValue() instanceof OffsetDateTime) {
                        OffsetDateTime nativeValue = (OffsetDateTime) item.getDisplayValue();
                        this.dateTimePicker.setDateTimePermissive(nativeValue);
                    } else if (item.getDisplayValue() instanceof Timestamp) {
                        Timestamp dtvalue = (Timestamp) item.getDisplayValue();
                        dateTimePicker.setDateTimePermissive(dtvalue.toLocalDateTime());
                    } else if (item.getDisplayValue() instanceof String) {
                        String date_time = (String) item.getDisplayValue();
                        String date = date_time.substring(0, date_time.indexOf(' '));
                        int index_timezone = date_time.indexOf('+');
                        if (index_timezone < 0)
                            index_timezone = date_time.indexOf('-');
                        String time = date_time.substring(date_time.indexOf(' ') + 1, index_timezone);
                        //time = time.substring(0,12);
                        String timezone = date_time.substring(index_timezone);
                        LocalDateTime localDateTime = Timestamp.valueOf(date + " " + time).toLocalDateTime();
                        dateTimePicker.setDateTimePermissive(localDateTime);
                    } else dateTimePicker.setDateTimePermissive((OffsetDateTime) null);
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

    private void zAdjustTableColWidthIfNeeded(JTable table,int col) {
        if (this.autoAdjustMinimumTableColWidth) {
            if (table.getColumnModel().getColumn(col).getWidth() < this.minimumColWidthInPixels) {
                table.getColumnModel().getColumn(col).setWidth(this.minimumColWidthInPixels);
                table.getColumnModel().getColumn(col).setPreferredWidth(this.minimumColWidthInPixels);
            }

        }
    }
}