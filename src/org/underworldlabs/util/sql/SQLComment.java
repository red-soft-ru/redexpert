package org.underworldlabs.util.sql;

import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.util.MiscUtils;

import java.util.Objects;

import static org.executequery.databaseobjects.NamedObject.*;
import static org.underworldlabs.util.sql.Keywords.*;

public final class SQLComment {

    /// Private constructor to prevent installation
    private SQLComment() {
    }

    // --- generation ---

    public static String generate(String name, String metaTag, String comment, String delimiter, boolean nullable) {
        return buildQuery(
                name,
                getFormattedMetaTag(metaTag),
                null,
                getFormattedComment(comment, nullable),
                delimiter
        );
    }

    public static String generate(String name, String metaTag, String plugin, String comment, String delimiter, boolean nullable) {
        return buildQuery(
                name,
                getFormattedMetaTag(metaTag),
                plugin,
                getFormattedComment(comment, nullable),
                delimiter
        );
    }

    // --- main building method ---

    private static String buildQuery(String name, String metaTag, String plugin, String comment, String delimiter) {

        if (MiscUtils.isNull(comment))
            return EMPTY_STRING;

        StringBuilder sb = new StringBuilder();
        sb.append("COMMENT ON ").append(metaTag).append(SPACE).append(name);

        if (!MiscUtils.isNull(plugin))
            sb.append(" USING PLUGIN ").append(plugin);

        sb.append(" IS ").append(comment);
        sb.append(delimiter).append(END_LINE);

        return sb.toString();
    }

    // --- helper methods ---

    private static String getFormattedComment(String value, boolean nullable) {

        if (MiscUtils.isNull(value) || Objects.equals(value, NULL))
            return nullable ? NULL : null;

        // trim edge apostrophes
        if (surroundedWithApostrophes(value))
            value = value.substring(1, value.length() - 1);

        // replace inner apostrophes
        value = value.replace(APOSTROPHE, "''");

        // return formatted comments
        return APOSTROPHE + value + APOSTROPHE;
    }

    private static String getFormattedMetaTag(String metaTag) {

        if (metaTag == null)
            return null;

        if (metaTag.contentEquals(NamedObject.META_TYPES[GLOBAL_TEMPORARY]))
            return NamedObject.META_TYPES[TABLE];

        if (metaTag.contentEquals(NamedObject.META_TYPES[DATABASE_TRIGGER]) || metaTag.contentEquals(NamedObject.META_TYPES[DDL_TRIGGER]))
            return NamedObject.META_TYPES[TRIGGER];

        return metaTag;
    }

    private static boolean surroundedWithApostrophes(String value) {
        return !MiscUtils.isNull(value) && value.startsWith(APOSTROPHE) && value.endsWith(APOSTROPHE);
    }

}
