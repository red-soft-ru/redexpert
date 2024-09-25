package org.executequery.gui.resultset;

import org.executequery.Constants;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * ResultSet values formatter class.<br>
 * Values formatting in according to the patterns from user properties.
 *
 * @author Aleksey Kozlov
 */
public final class ValueFormatter {

    private static final DateTimeFormatter dateFormatter;
    private static final DateTimeFormatter timeFormatter;
    private static final DateTimeFormatter timestampFormatter;
    private static final DateTimeFormatter zonedTimeFormatter;
    private static final DateTimeFormatter zonedTimestampFormatter;

    static {
        String datePattern = SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "results.date.pattern");
        String timePattern = SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "results.time.pattern");
        String timestampPattern = SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "results.timestamp.pattern");
        String timeTimezonePattern = SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "results.time.timezone.pattern");
        String timestampTimezonePattern = SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "results.timestamp.timezone.pattern");

        dateFormatter = !MiscUtils.isNull(datePattern) ? DateTimeFormatter.ofPattern(datePattern) : null;
        timeFormatter = !MiscUtils.isNull(timePattern) ? DateTimeFormatter.ofPattern(timePattern) : null;
        timestampFormatter = !MiscUtils.isNull(timestampPattern) ? DateTimeFormatter.ofPattern(timestampPattern) : null;
        zonedTimeFormatter = !MiscUtils.isNull(timeTimezonePattern) ? DateTimeFormatter.ofPattern(timeTimezonePattern) : null;
        zonedTimestampFormatter = !MiscUtils.isNull(timestampTimezonePattern) ? DateTimeFormatter.ofPattern(timestampTimezonePattern) : null;
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
