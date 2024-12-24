package org.executequery.gui.resultset;

import org.executequery.util.UserProperties;
import org.underworldlabs.util.validation.Validator;

import java.time.*;
import java.time.format.DateTimeFormatter;

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

    private static final DateTimeFormatter dateFormatter;
    private static final DateTimeFormatter timeFormatter;
    private static final DateTimeFormatter timestampFormatter;
    private static final DateTimeFormatter zonedTimeFormatter;
    private static final DateTimeFormatter zonedTimestampFormatter;

    static {

        String zonedTimestamp = UserProperties.getInstance().getProperty(ZONED_TIMESTAMP_KEY);
        String zonedTime = UserProperties.getInstance().getProperty(ZONED_TIME_KEY);
        String timestamp = UserProperties.getInstance().getProperty(TIMESTAMP_KEY);
        String time = UserProperties.getInstance().getProperty(TIME_KEY);
        String date = UserProperties.getInstance().getProperty(DATE_KEY);

        dateFormatter = Validator.of(DATE_KEY).isValid(date, false) ? DateTimeFormatter.ofPattern(date) : null;
        timeFormatter = Validator.of(TIME_KEY).isValid(time, false) ? DateTimeFormatter.ofPattern(time) : null;
        timestampFormatter = Validator.of(TIMESTAMP_KEY).isValid(timestamp, false) ? DateTimeFormatter.ofPattern(timestamp) : null;
        zonedTimeFormatter = Validator.of(ZONED_TIME_KEY).isValid(zonedTime, false) ? DateTimeFormatter.ofPattern(zonedTime) : null;
        zonedTimestampFormatter = Validator.of(ZONED_TIMESTAMP_KEY).isValid(zonedTimestamp, false) ? DateTimeFormatter.ofPattern(zonedTimestamp) : null;
    }

    /// Private constructor to prevent installation
    private ValueFormatter() {
    }

    public static String formatted(Object value) {

        if (value instanceof SimpleRecordDataItem)
            value = ((SimpleRecordDataItem) value).getValue();

        if (value instanceof LocalDate)
            return formattedDate((LocalDate) value);
        else if (value instanceof LocalTime)
            return formattedTime((LocalTime) value);
        else if (value instanceof LocalDateTime)
            return formattedTimestamp((LocalDateTime) value);
        else if (value instanceof OffsetTime)
            return formattedZonedTime((OffsetTime) value);
        else if (value instanceof OffsetDateTime)
            return formattedZonedTimestamp((OffsetDateTime) value);
        else if (value != null)
            return value.toString();

        return null;
    }

    private static String formattedDate(LocalDate date) {
        return dateFormatter != null ? dateFormatter.format(date) : date.toString();
    }

    private static String formattedTime(LocalTime date) {
        return timeFormatter != null ? timeFormatter.format(date) : date.toString();
    }

    private static String formattedTimestamp(LocalDateTime date) {
        return timestampFormatter != null ? timestampFormatter.format(date) : date.toString();
    }

    private static String formattedZonedTime(OffsetTime date) {
        return zonedTimeFormatter != null ? zonedTimeFormatter.format(date) : date.toString();
    }

    private static String formattedZonedTimestamp(OffsetDateTime date) {
        return zonedTimestampFormatter != null ? zonedTimestampFormatter.format(date) : date.toString();
    }
}
