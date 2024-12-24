package org.executequery.gui.resultset;

import org.executequery.util.UserProperties;
import org.underworldlabs.util.validation.Validator;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * ResultSet values formatter class.<br>
 * Values formatting in according to the patterns from user properties.
 *
 * @author Aleksey Kozlov
 */
public final class ValueFormatter {

    private static final String ZONED_TIMESTAMP_KEY = "results.timestamp.timezone.pattern";
    private static final String ZONED_TIME_KEY = "results.time.timezone.pattern";
    private static final String TIMESTAMP_KEY = "results.timestamp.pattern";
    private static final String TIME_KEY = "results.time.pattern";
    private static final String DATE_KEY = "results.date.pattern";

    private static DateTimeFormatter dateFormatter;
    private static DateTimeFormatter timeFormatter;
    private static DateTimeFormatter timestampFormatter;
    private static DateTimeFormatter zonedTimeFormatter;
    private static DateTimeFormatter zonedTimestampFormatter;

    private static boolean markedForReload = true;

    /// Private constructor to prevent installation
    private ValueFormatter() {
    }

    private static void maybeInitialize() {
        if (markedForReload) {
            markLoaded();
            reset();
            init();
        }
    }

    private static void init() {

        String pattern = UserProperties.getInstance().getProperty(ZONED_TIMESTAMP_KEY);
        if (Validator.of(ZONED_TIMESTAMP_KEY).isValid(pattern, false))
            zonedTimestampFormatter = DateTimeFormatter.ofPattern(pattern);

        pattern = UserProperties.getInstance().getProperty(ZONED_TIME_KEY);
        if (Validator.of(ZONED_TIME_KEY).isValid(pattern, false))
            zonedTimeFormatter = DateTimeFormatter.ofPattern(pattern);

        pattern = UserProperties.getInstance().getProperty(TIMESTAMP_KEY);
        if (Validator.of(TIMESTAMP_KEY).isValid(pattern, false))
            timestampFormatter = DateTimeFormatter.ofPattern(pattern);

        pattern = UserProperties.getInstance().getProperty(TIME_KEY);
        if (Validator.of(TIME_KEY).isValid(pattern, false))
            timeFormatter = DateTimeFormatter.ofPattern(pattern);

        pattern = UserProperties.getInstance().getProperty(DATE_KEY);
        if (Validator.of(DATE_KEY).isValid(pattern, false))
            dateFormatter = DateTimeFormatter.ofPattern(pattern);
    }

    public static String formatted(Object value) {
        maybeInitialize();

        if (value == null)
            return null;

        if (value instanceof SimpleRecordDataItem)
            value = ((SimpleRecordDataItem) value).getValue();

        DateTimeFormatter formatter = formatterForValue(value);
        return formatter != null ? formatter.format((TemporalAccessor) value) : value.toString();
    }

    private static DateTimeFormatter formatterForValue(Object value) {

        if (value instanceof OffsetDateTime)
            return zonedTimestampFormatter;

        if (value instanceof OffsetTime)
            return zonedTimeFormatter;

        if (value instanceof LocalDateTime)
            return timestampFormatter;

        if (value instanceof LocalTime)
            return timeFormatter;

        if (value instanceof LocalDate)
            return dateFormatter;

        return null;
    }

    // ---

    private static void reset() {
        zonedTimestampFormatter = null;
        zonedTimeFormatter = null;
        timestampFormatter = null;
        timeFormatter = null;
        dateFormatter = null;
    }

    public static void markForReload() {
        ValueFormatter.markedForReload = true;
    }

    public static void markLoaded() {
        ValueFormatter.markedForReload = false;
    }

}
