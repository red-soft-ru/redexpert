package org.executequery.databaseobjects;

public class
DatabaseTypeConverter {

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
    private static final int cstring_type = 40;
    private static final int blob_type = 261;
    private static final short boolean_type = 23;
    private static final int decfloat16_type = 24;
    private static final int decfloat34_type = 25;
    private static final int int128 = 26;
    private static final int time_with_timezone = 28;
    private static final int timestamp_with_timezone = 29;
    private static final int time_without_timezone = 30;
    private static final int timestamp_without_timezone = 31;


    public static String getTypeWithSize(int sqlType, int sqlSubtype, int sqlSize, int sqlScale) {
        switch (sqlType) {
            case smallint_type:
                if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0))
                    return T.NUMERIC+"(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else if (sqlSubtype == SUBTYPE_DECIMAL)
                    return T.DECIMAL+"(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else
                    return T.SMALLINT;
            case integer_type:
                if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0))
                    return T.NUMERIC+"(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else if (sqlSubtype == SUBTYPE_DECIMAL)
                    return T.DECIMAL+"(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else
                    return T.INTEGER;
            case double_type:
            case d_float_type:
                if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0))
                    return T.NUMERIC+"(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else if (sqlSubtype == SUBTYPE_DECIMAL)
                    return T.DECIMAL+"(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else
                    return T.DOUBLE_PRECISION;
            case float_type:
                return T.FLOAT;
            case char_type:
                return T.CHAR+"(" + sqlSize + ")";
            case varchar_type:
                return T.VARCHAR+"(" + sqlSize + ")";
            case timestamp_type:
            case timestamp_without_timezone:
                return T.TIMESTAMP;
            case timestamp_with_timezone:
                return T.TIMESTAMP_WITH_TIMEZONE;
            case time_type:
            case time_without_timezone:
                return T.TIME;
            case time_with_timezone:
                return T.TIME_WITH_TIMEZONE;
            case date_type:
                return T.DATE;
            case int64_type:
                if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0))
                    return T.NUMERIC+"(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else if (sqlSubtype == SUBTYPE_DECIMAL)
                    return T.DECIMAL+"(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else
                    return T.BIGINT;
            case blob_type:
                if (sqlSubtype < 0)
                    return T.BLOB_SUB_TYPE_V0;
                else if (sqlSubtype == 0)
                    return T.BLOB_SUB_TYPE_BINARY;
                else if (sqlSubtype == 1)
                    return T.BLOB_SUB_TYPE_TEXT;
                else
                    return T.BLOB + " SUB_TYPE " + sqlSubtype;
            case quad_type:
                return T.ARRAY;
            case boolean_type:
                return T.BOOLEAN;
            case cstring_type:
                return T.CSTRING+"(" + sqlSize + ")";
            case decfloat16_type:
                return T.DECFLOAT+"(16)";
            case decfloat34_type:
                return T.DECFLOAT+"(34)";
            case int128:
                if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0))
                    return T.NUMERIC+"(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else if (sqlSubtype == SUBTYPE_DECIMAL)
                    return T.DECIMAL+"(" + sqlSize + "," + Math.abs(sqlScale) + ")";
                else
                    return T.INT128;
            default:
                return "NULL";
        }
    }

    public static String getDataTypeName(int sqltype, int sqlsubtype, int sqlscale) {
        switch (sqltype) {
            case smallint_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return T.NUMERIC;
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return T.DECIMAL;
                else
                    return T.SMALLINT;
            case integer_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return T.NUMERIC;
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return T.DECIMAL;
                else
                    return T.INTEGER;
            case double_type:
            case d_float_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return T.NUMERIC;
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return T.DECIMAL;
                else
                    return T.DOUBLE_PRECISION;
            case float_type:
                return T.FLOAT;
            case char_type:
                return T.CHAR;
            case varchar_type:
                return T.VARCHAR;
            case timestamp_type:
                return T.TIMESTAMP;
            case timestamp_with_timezone:
                return T.TIMESTAMP_WITH_TIMEZONE;
            case timestamp_without_timezone:
                return "TIMESTAMP WITHOUT TIME ZONE";
            case time_type:
                return T.TIME;
            case time_with_timezone:
                return T.TIME_WITH_TIMEZONE;
            case time_without_timezone:
                return "TIME WITHOUT TIME ZONE";
            case date_type:
                return T.DATE;
            case int64_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return T.NUMERIC;
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return T.DECIMAL;
                else
                    return T.BIGINT;
            case blob_type:
                if (sqlsubtype < 0)
                    return T.BLOB_SUB_TYPE_V0;
                else if (sqlsubtype == 0)
                    return T.BLOB_SUB_TYPE_BINARY;
                else if (sqlsubtype == 1)
                    return T.BLOB_SUB_TYPE_TEXT;
                else
                    return "BLOB SUB_TYPE " + sqlsubtype;
            case quad_type:
                return T.ARRAY;
            case boolean_type:
                return T.BOOLEAN;
            case cstring_type:
                return T.CSTRING;
            case decfloat16_type:
                return T.DECFLOAT+"(16)";
            case decfloat34_type:
                return T.DECFLOAT+"(34)";
            case int128:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return T.NUMERIC;
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return T.DECIMAL;
                else
                    return T.INT128;
            default:
                return "NULL";
        }
    }

    public static int[] getSQLDataTypesFromNames(String[] databaseTypes) {
        int[] types = new int[databaseTypes.length];
        for (int i = 0; i < databaseTypes.length; i++) {
            types[i] = getSQLDataTypeFromName(databaseTypes[i]);
        }
        return types;
    }

    public static int getSQLDataTypeFromName(String databaseType) {
        databaseType = databaseType.replaceAll("[\n\t\r]", " ");
        databaseType = databaseType.replaceAll("[/][*].*[\\*][/]", " ");
        while (databaseType.contains("  "))
            databaseType = databaseType.replaceAll("  ", " ");
        switch (databaseType.trim().toUpperCase()) {
            case T.BIGINT:
                return Types.BIGINT;
            case T.BLOB_SUB_TYPE_BINARY:
                return Types.LONGVARBINARY;
            case T.VARCHAR:
            case "VARBINARY":
                return Types.VARCHAR;
            case T.CHAR:
            case "BINARY":
            case "CHARACTER":
                return Types.CHAR;
            case T.NCHAR:
            case "NATIONAL CHAR":
            case "NATIONAL CHARACTER":
                return Types.NCHAR;
            case "NATIONAL CHARACTER VARYING":
                return Types.NVARCHAR;
            case T.BLOB_SUB_TYPE_TEXT:
                return Types.LONGVARCHAR;
            case T.NUMERIC:
                return Types.NUMERIC;
            case T.DECIMAL:
            case "DEC":
                return Types.DECIMAL;
            case T.INT128:
                return Types.INT128;
            case T.INTEGER:
                return Types.INTEGER;
            case T.SMALLINT:
                return Types.SMALLINT;
            case T.FLOAT:
            case "REAL":
                return Types.FLOAT;
            case T.DOUBLE_PRECISION:
            case "LONG FLOAT":
                return Types.DOUBLE;
            case T.BOOLEAN:
                return Types.BOOLEAN;
            case T.DATE:
                return Types.DATE;
            case T.TIME:
            case "TIME WITHOUT TIME ZONE":
                return Types.TIME;
            case T.TIME_WITH_TIMEZONE:
                return Types.TIME_WITH_TIMEZONE;
            case T.TIMESTAMP:
            case "TIMESTAMP WITHOUT TIME ZONE":
                return Types.TIMESTAMP;
            case T.TIMESTAMP_WITH_TIMEZONE:
                return Types.TIMESTAMP_WITH_TIMEZONE;
            case T.ARRAY:
                return Types.OTHER;
            case T.BLOB_SUB_TYPE_V0:
                return Types.BLOB;
            case T.DECFLOAT+"(16)":
            case T.DECFLOAT+"(34)":
                return Types.DECFLOAT;
            default:
                return 0;
        }
    }

    public static int getSqlTypeFromRDBType(int type, int subtype) {
        switch (type) {
            case smallint_type:
                switch (subtype) {
                    case 1:
                        return Types.NUMERIC;
                    case 2:
                        return Types.DECIMAL;
                    default:
                        return Types.SMALLINT;
                }
            case integer_type:
                switch (subtype) {
                    case 1:
                        return Types.NUMERIC;
                    case 2:
                        return Types.DECIMAL;
                    default:
                        return Types.INTEGER;
                }
            case float_type:
                return Types.FLOAT;
            case date_type:
                return Types.DATE;
            case time_type:
            case time_without_timezone:
                return Types.TIME;
            case time_with_timezone:
                return Types.TIME_WITH_TIMEZONE;
            case char_type:
                if (subtype == 0) {
                    return Types.BINARY;
                }
                return Types.CHAR;
            case int64_type:
                switch (subtype) {
                    case 1:
                        return Types.NUMERIC;
                    case 2:
                        return Types.DECIMAL;
                    default:
                        return Types.BIGINT;
                }
            case boolean_type:
                return Types.BOOLEAN;
            case double_type:
                return Types.DOUBLE;
            case timestamp_type:
            case timestamp_without_timezone:
                return Types.TIMESTAMP;
            case timestamp_with_timezone:
                return Types.TIMESTAMP_WITH_TIMEZONE;
            case varchar_type:
                if (subtype == 0) {
                    return Types.VARBINARY;
                }
                return Types.VARCHAR;
            case blob_type:
                switch (subtype) {
                    case 1:
                        return Types.LONGVARCHAR;
                    case 2:
                        return Types.LONGVARBINARY;
                    default:
                        return Types.BLOB;
                }
            case decfloat16_type:
            case decfloat34_type:
                return Types.DECFLOAT;
            case int128:
                switch (subtype) {
                    case 1:
                        return Types.NUMERIC;
                    case 2:
                        return Types.DECIMAL;
                    default:
                        return Types.INT128;
                }
            default:
                return 0;
        }
    }
}
