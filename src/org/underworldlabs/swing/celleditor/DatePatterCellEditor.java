package org.underworldlabs.swing.celleditor;

import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.celleditor.picker.StringPicker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;

/// @author Aleksey Kozlov
public class DatePatterCellEditor extends DefaultCellEditor
        implements CellEditorListener {

    private static final int ZONED_TIMESTAMP = 0;
    private static final int ZONED_TIME = ZONED_TIMESTAMP + 1;
    private static final int TIMESTAMP = ZONED_TIME + 1;
    private static final int TIME = TIMESTAMP + 1;
    private static final int DATE = TIME + 1;

    private static final List<Character> DICTIONARY = Arrays.asList(
            ':', '-', '/', '.', ' ', ',', '\'',  // Separators
            'G', // Era designator (e.g., AD, BC)
            'y', // Year (e.g., 2023)
            'Y', // Week-based year
            'M', // Month in year (1-12 or Jan-Dec)
            'L', // Stand-alone month (1-12 or Jan-Dec)
            'w', // Week of year (1-52)
            'W', // Week of month (1-5)
            'D', // Day of year (1-366)
            'd', // Day of month (1-31)
            'F', // Day of week in month (1-5)
            'E', // Day name in week (e.g., Tuesday)
            'u', // Day number of week (1-7, where 1 = Monday)
            'a', // AM/PM marker
            'H', // Hour in day (0-23)
            'k', // Hour in day (1-24)
            'K', // Hour in AM/PM (0-11)
            'h', // Hour in AM/PM (1-12)
            'm', // Minute in hour (0-59)
            's', // Second in minute (0-59)
            'S', // Millisecond (0-999)
            'z', // Time zone name (e.g., Moscow Standard Time)
            'Z', // Time zone offset/id (e.g., +0300 or Russia/Moscow)
            'X', // Time zone offset (ISO-8601)
            'V', // Time zone ID (e.g., Russia/Moscow)
            'O', // Localized zone-offset (e.g., GMT+3)
            'x'  // Time zone offset (ISO-8601 without 'Z' for UTC)
    );

    private String bundleKey = "invalid-format.";
    private transient Object oldValue;

    private final int valueType;
    private final JTable table;
    private final int rowIndex;

    public DatePatterCellEditor(String key, JTable table, int rowIndex) {
        super(new StringPicker());

        this.valueType = typeForKey(key);
        this.rowIndex = rowIndex;
        this.oldValue = null;
        this.table = table;

        addCellEditorListener(this);
    }

    /**
     * Validates the input string as a valid date format pattern.
     * This method checks if the input string contains only valid characters
     * and if it represents a valid date format pattern.
     *
     * @param value The string to be validated as a date format pattern.
     * @return true if the input is a valid date format pattern, false otherwise.
     */
    private boolean validate(String value) {

        if (!MiscUtils.isNull(value) && (containsInvalidChars(value) || patternInvalid(value))) {
            GUIUtilities.displayWarningMessage(bundleString(bundleKey, value));
            return false;
        }

        return true;
    }

    private static boolean containsInvalidChars(String value) {
        return !value.chars().mapToObj(charCode -> (char) charCode).allMatch(DICTIONARY::contains);
    }

    private boolean patternInvalid(String value) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(value);
            formatter.format(getCheckValue());
            return false;

        } catch (IllegalArgumentException | DateTimeException e) {
            return true;
        }
    }

    // --- helpers ---

    private TemporalAccessor getCheckValue() {
        switch (valueType) {

            case ZONED_TIMESTAMP:
                return ZonedDateTime.now().toOffsetDateTime();
            case TIMESTAMP:
                return ZonedDateTime.now().toLocalDateTime();
            case TIME:
                return ZonedDateTime.now().toLocalTime();
            case DATE:
                return ZonedDateTime.now().toLocalDate();
            case ZONED_TIME:
                return ZonedDateTime.now();

            default:
                throw new IllegalArgumentException("Unknown value type: " + valueType);
        }
    }

    private int typeForKey(String key) {
        switch (key) {

            case "results.timestamp.timezone.pattern":
                this.bundleKey += "zoned-timestamp";
                return ZONED_TIMESTAMP;

            case "results.time.timezone.pattern":
                this.bundleKey += "zoned-time";
                return ZONED_TIME;

            case "results.timestamp.pattern":
                this.bundleKey += "timestamp";
                return TIMESTAMP;

            case "results.time.pattern":
                this.bundleKey += "time";
                return TIME;

            case "results.date.pattern":
                this.bundleKey += "date";
                return DATE;

            default:
                throw new IllegalArgumentException("Unknown key: " + key);
        }
    }

    private Object bundleString(String key, Object... args) {
        return Bundles.get(DatePatterCellEditor.class, key, args);
    }

    // --- DefaultCellEditor impl ---

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        this.oldValue = value;
        return component;
    }

    // --- CellEditorListener impl ---

    @Override
    public void editingStopped(ChangeEvent e) {
        if (!validate(((StringPicker) getComponent()).getValue()))
            table.setValueAt(oldValue, rowIndex, 2);
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
        // do nothing
    }

}
