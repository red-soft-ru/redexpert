/*
 * ProcedureParameter.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.databaseobjects;

import java.sql.DatabaseMetaData;

/**
 * @author Takis Diakoumis
 */
public class ProcedureParameter {

    private String name;
    private int type;
    private int dataType;
    private String sqlType;
    private int size;
    private int scale;
    private String encoding;
    private String value;
    private int subtype;
    private int nullable;
    private String domain;
    private String description;
    private boolean typeOf;
    private String relation_name;
    private String field_name;
    private int typeOfFrom;

    private static final String RESULT_STORE = "< Result Store >";
    private static final String RETURN_VALUE = "< Return Value >";
    private static final String UNKNOWN = "< Unknown >";

    public ProcedureParameter(String name, int type, int dataType,
                              String sqlType, int size, int nullable) {
        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.sqlType = sqlType;
        this.size = size;
        this.nullable = nullable;
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

    public int getSubtype() {
        return subtype;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

    public String toString() {
        return getName();
    }

    public void setNullable(int nullable) {
        this.nullable = nullable;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getNullable() {
        return nullable;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTypeOf() {
        return typeOf;
    }

    public void setTypeOf(boolean typeOf) {
        this.typeOf = typeOf;
    }

    public String getRelation_name() {
        return relation_name;
    }

    public void setRelation_name(String relation_name) {
        this.relation_name = relation_name;
    }

    public String getField_name() {
        return field_name;
    }

    public void setField_name(String field_name) {
        this.field_name = field_name;
    }

    public void setTypeOfFrom(int typeOfFrom) {
        this.typeOfFrom = typeOfFrom;
    }

    public int getTypeOfFrom() {
        return typeOfFrom;
    }
}


