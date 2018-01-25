package org.executequery.databaseobjects;

import java.sql.Types;

public class DatabaseTypeConverter {

    private static final int SUBTYPE_NUMERIC = 1;
    private static final int SUBTYPE_DECIMAL = 2;

    private static final int smallint_type = 7;
    private static final int integer_type = 8;
    private static final int quad_type = 9;
    private static final int float_type = 10;
    private static final int d_float_type = 11;
    private static final int date_type = 12;
    private static final int time_type = 13;
    private static final int char_type = 14;
    private static final int int64_type = 16;
    private static final int double_type = 27;
    private static final int timestamp_type = 35;
    private static final int varchar_type = 37;
    //  private static final int cstring_type = 40;
    private static final int blob_type = 261;
    private static final short boolean_type = 23;

    public static String getTypeWithSize(int sqlType, int sqlSubtype, int sqlSize, int sqlScale) {
        switch (sqlType) {
            case smallint_type:
                if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0))
                    return "NUMERIC(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else if (sqlSubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else
                    return "SMALLINT";
            case integer_type:
                if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0))
                    return "NUMERIC(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else if (sqlSubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else
                    return "INTEGER";
            case double_type:
            case d_float_type:
                if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0))
                    return "NUMERIC(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else if (sqlSubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else
                    return "DOUBLE PRECISION";
            case float_type:
                return "FLOAT";
            case char_type:
                return "CHAR(" + sqlSize + ")";
            case varchar_type:
                return "VARCHAR(" + sqlSize + ")";
            case timestamp_type:
                return "TIMESTAMP";
            case time_type:
                return "TIME";
            case date_type:
                return "DATE";
            case int64_type:
                if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0))
                    return "NUMERIC(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else if (sqlSubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else
                    return "BIGINT";
            case blob_type:
                if (sqlSubtype < 0)
                    return "BLOB SUB_TYPE <0";
                else if (sqlSubtype == 0)
                    return "BLOB SUB_TYPE 0";
                else if (sqlSubtype == 1)
                    return "BLOB SUB_TYPE 1";
                else
                    return "BLOB SUB_TYPE " + sqlSubtype;
            case quad_type:
                return "ARRAY";
            case boolean_type:
                return "BOOLEAN";
            default:
                return "NULL";
        }
    }

    public static String getDataTypeName(int sqltype, int sqlsubtype, int sqlscale) {
        switch (sqltype) {
            case smallint_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
                    return "SMALLINT";
            case integer_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
                    return "INTEGER";
            case double_type:
            case d_float_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
                    return "DOUBLE PRECISION";
            case float_type:
                return "FLOAT";
            case char_type:
                return "CHAR";
            case varchar_type:
                return "VARCHAR";
            case timestamp_type:
                return "TIMESTAMP";
            case time_type:
                return "TIME";
            case date_type:
                return "DATE";
            case int64_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
                    return "BIGINT";
            case blob_type:
                if (sqlsubtype < 0)
                    // TODO Include actual subtype?
                    return "BLOB SUB_TYPE <0";
                else if (sqlsubtype == 0)
                    return "BLOB SUB_TYPE BINARY";
                else if (sqlsubtype == 1)
                    return "BLOB SUB_TYPE TEXT";
                else
                    return "BLOB SUB_TYPE " + sqlsubtype;
            case quad_type:
                return "ARRAY";
            case boolean_type:
                return "BOOLEAN";
            default:
                return "NULL";
        }
    }

    public static int getSQLDataTypeFromName(String databaseType) {
        switch (databaseType.trim().toUpperCase()) {
            case "BIGINT":
                return Types.BIGINT;
            case "BLOB SUB_TYPE BINARY":
                return Types.LONGVARBINARY;
            case "VARCHAR":
                return Types.VARCHAR;
            case "CHAR":
                return Types.CHAR;
            case "BLOB SUB_TYPE TEXT":
                return Types.LONGVARCHAR;
            case "NUMERIC":
                return Types.NUMERIC;
            case "DECIMAL":
                return Types.DECIMAL;
            case "INTEGER":
                return Types.INTEGER;
            case "SMALLINT":
                return Types.SMALLINT;
            case "FLOAT":
                return Types.FLOAT;
            case "DOUBLE PRECISION":
                return Types.DOUBLE;
            case "BOOLEAN":
                return Types.BOOLEAN;
            case "DATE":
                return Types.DATE;
            case "TIME":
                return Types.TIME;
            case "TIMESTAMP":
                return Types.TIMESTAMP;
            case "ARRAY":
                return Types.OTHER;
            case "BLOB SUB_TYPE <0":
                return Types.BLOB;
            default:
                return 0;
        }
    }
}
