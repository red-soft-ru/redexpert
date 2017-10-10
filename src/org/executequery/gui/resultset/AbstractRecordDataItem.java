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


/**
 *
 * @author Takis Diakoumis
 * @version $Revision: 1780 $
 * @date $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public abstract class AbstractRecordDataItem implements RecordDataItem {

	protected Object value;

	protected Object newValue;

    protected String name;

    protected int dataType;

	protected String dataTypeName;

	protected boolean changed;

	protected int row;

	private static final SQLTypeObjectFactory TYPE_OBJECT_FACTORY = new SQLTypeObjectFactory();

	public AbstractRecordDataItem(String name, int dataType, String dataTypeName,int row) {

		super();
        this.name = name;
        this.dataType = dataType;
		this.dataTypeName = dataTypeName;
		this.row=row;
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
		this.newValue=value;
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

			changed=false;
			return;
		}

	    if (newValue != null && isStringLiteralNull(newValue)) {

	        this.newValue=null;

	    } else {

	       this.newValue=newValue;
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
	public boolean isNewValueNull() {
		return newValue==null;
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



