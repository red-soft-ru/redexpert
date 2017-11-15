/*
 * AbstractRecordDataItem.java
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

package org.executequery.gui.resultset;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.SQLTypeObjectFactory;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;


/**
 * @author Takis Diakoumis
 */
public abstract class AbstractRecordDataItem implements RecordDataItem {

    private Object value;

    private Object newValue;

    private boolean newRecord = false;

    private boolean deleted = false;

    private String name;

    private int dataType;

    private String dataTypeName;

    protected boolean changed;

    private boolean generated = false;

    private static final SQLTypeObjectFactory TYPE_OBJECT_FACTORY = new SQLTypeObjectFactory();

    public AbstractRecordDataItem(String name, int dataType, String dataTypeName) {

        super();
        this.name = name;
        this.dataType = dataType;
        this.dataTypeName = dataTypeName;
    }

    @Override
    public int length() {

        if (!isValueNull()) {

            return toString().length();

        } else {

            return 0;
        }
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    @Override
    public int getDataType() {
        return dataType;
    }

    @Override
    public Object getDisplayValue() {
        return getNewValue();
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Object getNewValue() {
        return newValue;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
        this.newValue = value;
    }

    @Override
    public boolean valueContains(String pattern) {

        if (isLob() || isValueNull()) {

            return false;
        }
        return StringUtils.containsIgnoreCase(getValue().toString(), pattern);
    }

    @Override
    public void valueChanged(Object newValue) {

        if (valuesEqual(this.value, newValue)) {

            changed = false;
            return;
        }

        if (newValue != null && isStringLiteralNull(newValue)) {

            this.newValue = null;

        } else {

            this.newValue = newValue;
        }
        changed = true;
    }

    protected boolean valuesEqual(Object firstValue, Object secondValue) {

        if (ObjectUtils.equals(firstValue, secondValue)) {

            return true;
        }

        if (firstValue != null && secondValue != null) {

            return firstValue.toString().equals(secondValue.toString());
        }

        return false;
    }

    private boolean isStringLiteralNull(Object newValue) {

        return newValue.toString().equalsIgnoreCase("NULL");
    }

    @Override
    public boolean isValueNull() {
        return (value == null);
    }

    public boolean isDisplayValueNull() {
        return isNewValueNull();
    }

    @Override
    public String toString() {

        if (getValue() != null) {

            return getValue().toString();
        }

        return null;
    }

    @Override
    public void setNull() {
        value = null;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isSQLValueNull() {
        return isValueNull();// && StringUtils.isBlank(toString());
    }

    @Override
    public boolean isNewValueNull() {
        return newValue == null;
    }

    @Override
    public Object getValueAsType() {

        if (isValueNull()) {

            return null;
        }
        return valueAsType(this.value);
    }

    protected Object valueAsType(Object value) {

        try {

            return TYPE_OBJECT_FACTORY.create(dataType, value);

        } catch (DataSourceException e) {

            Log.info("Unable to retrieve value as type for column [ " + name + " ]");
            return e.getMessage();

        }
    }

    @Override
    public boolean isBlob() {

        return false;
    }

    @Override
    public boolean isLob() {

        return false;
    }

    @Override
    public boolean isGenerated() {
        return generated;
    }

    @Override
    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    @Override
    public boolean isNew() {
        return newRecord;
    }

    public void setNew(boolean newRecord) {
        this.newRecord = newRecord;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public int compareTo(Object o) {
        RecordDataItem compar_object = (RecordDataItem) o;
        if (isDisplayValueNull() && isDisplayValueNull())
            switch (getDataType()) {
                case Types.BIGINT: {
                    Long first = (Long) getDisplayValue();
                    Long second = (Long) compar_object.getDisplayValue();
                    return first.compareTo(second);
                }
                case Types.DOUBLE: {
                    Double first = (Double) getDisplayValue();
                    Double second = (Double) compar_object.getDisplayValue();
                    return first.compareTo(second);
                }
                case Types.INTEGER: {
                    Integer first = (Integer) getDisplayValue();
                    Integer second = (Integer) compar_object.getDisplayValue();
                    return first.compareTo(second);
                }
                case Types.DECIMAL: {
                    BigDecimal first = (BigDecimal) getDisplayValue();
                    BigDecimal second = (BigDecimal) compar_object.getDisplayValue();
                    return first.compareTo(second);
                }
                case Types.DATE: {
                    Date first = (Date) getDisplayValue();
                    Date second = (Date) compar_object.getDisplayValue();
                    return first.compareTo(second);
                }
                case Types.TIME: {
                    Time first = (Time) getDisplayValue();
                    Time second = (Time) compar_object.getDisplayValue();
                    return first.compareTo(second);
                }
                case Types.TIMESTAMP: {
                    Timestamp first = (Timestamp) getDisplayValue();
                    Timestamp second = (Timestamp) compar_object.getDisplayValue();
                    return first.compareTo(second);
                }
                case Types.NUMERIC: {
                    BigDecimal first = (BigDecimal) getDisplayValue();
                    BigDecimal second = (BigDecimal) compar_object.getDisplayValue();
                    return first.compareTo(second);
                }
                case Types.FLOAT: {
                    Double first = (Double) getDisplayValue();
                    Double second = (Double) compar_object.getDisplayValue();
                    return first.compareTo(second);
                }
                case Types.SMALLINT: {
                    Short first = (Short) getDisplayValue();
                    Short second = (Short) compar_object.getDisplayValue();
                    return first.compareTo(second);
                }
                default:
                    return String.valueOf(getDisplayValue()).compareTo(String.valueOf(compar_object.getDisplayValue()));
            }
        else if (compar_object.getDisplayValue() == null && getDisplayValue() == null)
            return 0;
        else if (compar_object.getDisplayValue() == null)
            return 1;
        else return -1;
    }
}



