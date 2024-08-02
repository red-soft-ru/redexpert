package org.executequery.gui.browser.generatortestdata.methodspanels;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.T;
import org.executequery.gui.browser.GeneratorTestDataPanel;
import org.executequery.localization.Bundles;

import javax.swing.*;

public abstract class AbstractMethodPanel extends JPanel {
    protected DatabaseColumn col;
    protected boolean first = true;

    public AbstractMethodPanel(DatabaseColumn col) {
        this.col = col;
    }

    public abstract Object getTestDataObject();

    protected static boolean isNumeric(String dataType) {
        return dataType.contentEquals(T.BIGINT)
                || dataType.contentEquals(T.INT128)
                || dataType.contentEquals(T.INTEGER)
                || dataType.contentEquals(T.SMALLINT)
                || dataType.contentEquals(T.DOUBLE_PRECISION)
                || dataType.contentEquals(T.FLOAT)
                || dataType.startsWith(T.DECIMAL)
                || dataType.startsWith(T.NUMERIC)
                || dataType.startsWith(T.DECFLOAT);
    }

    protected static boolean isDecimal(String dataType) {
        return dataType.contentEquals(T.DOUBLE_PRECISION)
                || dataType.contentEquals(T.FLOAT)
                || dataType.startsWith(T.DECIMAL)
                || dataType.startsWith(T.NUMERIC)
                || dataType.startsWith(T.DECFLOAT);
    }

    protected static boolean isSmallint(String dataType) {
        return dataType.contentEquals(T.SMALLINT);
    }

    protected static boolean isInteger(String dataType) {
        return dataType.contentEquals(T.INTEGER);
    }

    protected static boolean isBigint(String dataType) {
        return dataType.contentEquals(T.BIGINT) || dataType.contentEquals(T.INT128);
    }

    protected static boolean isChar(String dataType) {
        return dataType.contains(T.CHAR);
    }

    protected static boolean isDate(String dataType) {
        return dataType.contentEquals(T.DATE);
    }

    protected static boolean isTime(String dataType) {
        return dataType.contentEquals(T.TIME);
    }

    protected static boolean isTimestamp(String dataType) {
        return dataType.contentEquals(T.TIMESTAMP);
    }

    protected static boolean isZonedTime(String dataType) {
        return dataType.contentEquals(T.TIME_WITH_TIMEZONE);
    }

    protected static boolean isZonedTimestamp(String dataType) {
        return dataType.contentEquals(T.TIMESTAMP_WITH_TIMEZONE);
    }

    protected static boolean isBlob(String dataType) {
        return dataType.contains(T.BLOB);
    }

    protected static boolean isBoolean(String dataType) {
        return dataType.contains(T.BOOLEAN);
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    protected String bundleString(String key) {
        return Bundles.get(GeneratorTestDataPanel.class, key);
    }

}
