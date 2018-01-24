package org.executequery.databaseobjects;

import org.executequery.gui.browser.ColumnData;

import java.sql.DatabaseMetaData;

/**
 * @author vasiliy
 */
public class FunctionParameter {

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
        this.position = position;
        this.typeOf = (typeOf == 1);
        if (relationName != null)
            this.relationName = relationName.trim();
        if (fieldName != null)
            this.fieldName = fieldName.trim();
        if (this.relationName != null && this.fieldName != null)
            this.typeOfFrom = ColumnData.TYPE_OF_FROM_COLUMN;
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
