package org.underworldlabs.util.validation;

import org.executequery.GUIUtilities;
import org.underworldlabs.util.MiscUtils;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;

/// @author Aleksey Kozlov
class DatePatternValidator extends AbstractValidator {

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

    public DatePatternValidator(int type) {
        super(type);
    }

    // --- Validator impl ---

    /**
     * Validates the input string as a valid date format pattern.
     * This method checks if the input string contains only valid characters
     * and if it represents a valid date format pattern.
     *
     * @param value The string to be validated as a date format pattern.
     * @return true if the input is a valid date format pattern, false otherwise.
     */
    @Override
    public boolean isValid(Object value, boolean displayMessage) {
        String stringValue = (String) value;

        if (!MiscUtils.isNull(stringValue) && (containsInvalidChars(stringValue) || patternInvalid(stringValue))) {
            if (displayMessage)
                GUIUtilities.displayWarningMessage(bundleString(getBundleKey(), stringValue));
            return false;
        }

        return true;
    }

    // --- validation methods ---

    private boolean containsInvalidChars(String value) {
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

    // --- helper methods ---

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

}
