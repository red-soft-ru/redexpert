/*
 * AbstractRecordDataItem.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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


/**
 *
 * @author Takis Diakoumis
 * @version $Revision: 1689 $
 * @date $Date: 2017-02-14 11:05:59 +1100 (Tue, 14 Feb 2017) $
 */
public abstract class AbstractRecordDataItem implements RecordDataItem {

	private Object value;

    private String name;

    private int dataType;

	private String dataTypeName;

	private boolean changed;

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
		return getValue();
	}

	@Override
    public Object getValue() {
		return value;
	}

	@Override
    public void setValue(Object value) {
		this.value = value;
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

			return;
		}

	    if (newValue != null && isStringLiteralNull(newValue)) {

	        setValue(null);

	    } else {

	        setValue(newValue);
	    }
	    changed = true;
	}

	private boolean valuesEqual(Object firstValue, Object secondValue) {

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

}


