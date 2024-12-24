package org.underworldlabs.util.validation;

import org.executequery.localization.Bundles;

/// @author Aleksey Kozlov
abstract class AbstractValidator implements Validator {

    protected static final int ZONED_TIMESTAMP = 0;
    protected static final int ZONED_TIME = ZONED_TIMESTAMP + 1;
    protected static final int TIMESTAMP = ZONED_TIME + 1;
    protected static final int TIME = TIMESTAMP + 1;
    protected static final int DATE = TIME + 1;

    protected final int valueType;

    protected AbstractValidator(int valueType) {
        this.valueType = valueType;
    }

    // --- Validator impl ---

    public static Validator of(String key) {
        int type = typeForKey(key);

        switch (type) {
            case ZONED_TIMESTAMP:
            case ZONED_TIME:
            case TIMESTAMP:
            case TIME:
            case DATE:
                return new DatePatternValidator(type);

            default:
                throw new IllegalArgumentException("Unknown value type: " + type);
        }
    }

    private static int typeForKey(String key) {
        switch (key) {

            case "results.timestamp.timezone.pattern":
                return ZONED_TIMESTAMP;
            case "results.time.timezone.pattern":
                return ZONED_TIME;
            case "results.timestamp.pattern":
                return TIMESTAMP;
            case "results.time.pattern":
                return TIME;
            case "results.date.pattern":
                return DATE;

            default:
                throw new IllegalArgumentException("Unknown key: " + key);
        }
    }

    // --- helper methods ---

    protected final String getBundleKey() {
        switch (valueType) {

            case ZONED_TIMESTAMP:
                return "invalid-format.zoned-timestamp";
            case ZONED_TIME:
                return "invalid-format.zoned-time";
            case TIMESTAMP:
                return "invalid-format.timestamp";
            case TIME:
                return "invalid-format.time";
            case DATE:
                return "invalid-format.date";

            default:
                throw new IllegalArgumentException("Unknown value type: " + valueType);
        }
    }

    protected Object bundleString(String key, Object... args) {
        return Bundles.get(Validator.class, key, args);
    }

}
