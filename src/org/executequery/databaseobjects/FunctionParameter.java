package org.executequery.databaseobjects;

import org.executequery.gui.browser.ColumnData;

import java.sql.DatabaseMetaData;

/**
 * @author vasiliy
 */
public class FunctionParameter {

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

    private String name;
    private int type; // in or out
    private int position;
    private int dataType;
    private String sqlType;
    private int size;
    private int scale;
    private int subType;
    private int nullable;
    private String domain;
    private String encoding;
    private String description;
    private String value;
    private boolean typeOf;
    private String relationName;
    private String fieldName;
    private int typeOfFrom;

    private static final String RESULT_STORE = "< Result Store >";
    private static final String RETURN_VALUE = "< Return Value >";
    private static final String UNKNOWN = "< Unknown >";

    public FunctionParameter(String name, int dataType, int size, int precision, int scale,
                             int subType, int position, int typeOf, String relationName, String fieldName) {
        this.name = name;
        if (this.name == null)
            this.type = DatabaseMetaData.procedureColumnReturn;
        else {
            this.type = DatabaseMetaData.procedureColumnIn;
            this.name = name.trim();
        }
        this.dataType = dataType;
        this.scale = scale;
        this.subType = subType;
        this.size = precision == 0 ? size : precision;
        this.sqlType = getTypeWithSize(dataType, subType, this.size, scale);
        this.position = position;
        this.typeOf = (typeOf == 1);
        if (relationName != null)
            this.relationName = relationName.trim();
        if (fieldName != null)
            this.fieldName = fieldName.trim();
        if (this.relationName != null && this.fieldName != null)
            this.typeOfFrom = ColumnData.TYPE_OF_FROM_COLUMN;
    }

    private String getTypeWithSize(int sqlType, int sqlSubtype, int sqlSize, int sqlScale) {
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

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getDataType() {
        return dataType;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {

        if (name == null) {

            if (type == DatabaseMetaData.procedureColumnResult)
                return RESULT_STORE;

            else if (type == DatabaseMetaData.procedureColumnReturn)
                return RETURN_VALUE;

            else
                return UNKNOWN;

        }

        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getNullable() {
        return nullable;
    }

    public void setNullable(int nullable) {
        this.nullable = nullable;
    }

    public boolean isTypeOf() {
        return typeOf;
    }

    public void setTypeOf(boolean typeOf) {
        this.typeOf = typeOf;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public int getScale() {
        return scale;
    }

    public String toString() {
        return getName();
    }

    public void setTypeOfFrom(int typeOfFrom) {
        this.typeOfFrom = typeOfFrom;
    }

    public int getTypeOfFrom() {
        return typeOfFrom;
    }
}
