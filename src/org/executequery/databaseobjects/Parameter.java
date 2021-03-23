package org.executequery.databaseobjects;

import java.sql.DatabaseMetaData;

public class Parameter {

    private static final String RESULT_STORE = "< Result Store >";
    private static final String RETURN_VALUE = "< Return Value >";
    private static final String UNKNOWN = "< Unknown >";
    protected String name;
    protected int type; // in or out
    protected int position;
    protected int dataType;
    protected String sqlType;
    protected int size;
    protected int scale;
    protected int subType;
    protected int nullable;
    protected String domain;
    protected String encoding;
    protected String description;
    protected String value;
    protected boolean typeOf;
    protected String relationName;
    protected String fieldName;
    protected int typeOfFrom;
    protected String defaultValue;
    protected boolean descriptionAsSingleComment;

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
        if (dataType == 0) {
            dataType = DatabaseTypeConverter.getSQLDataTypeFromName(sqlType);
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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

    public void setName(String name) {
        this.name = name;
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

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String toString() {
        return getName();
    }

    public int getTypeOfFrom() {
        return typeOfFrom;
    }

    public void setTypeOfFrom(int typeOfFrom) {
        setTypeOf(true);
        this.typeOfFrom = typeOfFrom;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isDescriptionAsSingleComment() {
        return descriptionAsSingleComment;
    }

    public void setDescriptionAsSingleComment(boolean descriptionAsSingleComment) {
        this.descriptionAsSingleComment = descriptionAsSingleComment;
    }
}

