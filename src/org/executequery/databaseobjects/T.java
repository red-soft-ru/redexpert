package org.executequery.databaseobjects;

public interface T {
    String NUMERIC = "NUMERIC";
    String DECIMAL = "DECIMAL";
    String SMALLINT = "SMALLINT";
    String INTEGER = "INTEGER";
    String DOUBLE_PRECISION = "DOUBLE PRECISION";
    String FLOAT = "FLOAT";
    String CHAR = "CHAR";
    String VARCHAR = "VARCHAR";
    String TIME = "TIME";
    String TIME_WITH_TIMEZONE = "TIME WITH TIME ZONE";
    String TIMESTAMP = "TIMESTAMP";
    String TIMESTAMP_WITH_TIMEZONE = "TIMESTAMP WITH TIME ZONE";
    String DATE = "DATE";
    String BIGINT = "BIGINT";
    String BLOB = "BLOB";
    String BLOB_SUB_TYPE_V0 = "BLOB SUB_TYPE <0";
    String BLOB_SUB_TYPE_BINARY = "BLOB SUB_TYPE BINARY";
    String BLOB_SUB_TYPE_TEXT = "BLOB SUB_TYPE TEXT";
    String ARRAY = "ARRAY";
    String BOOLEAN = "BOOLEAN";
    String CSTRING = "CSTRING";
    String DECFLOAT = "DECFLOAT";
    String INT128 = "INT128";

    String[] DEFAULT_TYPES = {
            NUMERIC,
            DECIMAL,
            SMALLINT,
            INTEGER,
            DOUBLE_PRECISION,
            FLOAT,
            CHAR,
            VARCHAR,
            TIME,
            TIMESTAMP,
            DATE,
            BIGINT,
            BLOB
    };

}
