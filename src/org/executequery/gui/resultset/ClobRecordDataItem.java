/*
 * ClobRecordDataItem.java
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

import java.io.*;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.executequery.Constants;
import org.executequery.log.Log;
import org.underworldlabs.util.SystemProperties;

public class ClobRecordDataItem extends AbstractLobRecordDataItem {

	private int displayLength;

	private static final String CLOB_DATA="<CLOB Data>";

	private String displayValue;

	public ClobRecordDataItem(String name, int dataType, String dataTypeName) {

		super(name, dataType, dataTypeName);

		displayLength = SystemProperties.getIntProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.clob.length");
	}

	@Override
	public Object getDisplayValue() {


		return CLOB_DATA;
	}

	@Override
    public String getLobRecordItemName() {

		return getDataTypeName();
	}


    @Override
    protected byte[] readLob() {

        Object value = getValue();
        if (value instanceof String) {

            return ((String) value).getBytes();
        }

    	Clob clob = (Clob) value;
		InputStream as;
		try {
			as = clob.getAsciiStream();
			byte[] b = new byte[1024];
			ByteArrayOutputStream result=new ByteArrayOutputStream();
			int length;
			while ((length = as.read(b)) != -1) {
				result.write(b, 0, length);
			}
			return result.toByteArray();



			//reader = clob.getCharacterStream();

		} catch (SQLException e) {

			if (Log.isDebugEnabled()) {

				Log.debug("Error reading CLOB data", e);
			}

			return e.getMessage().getBytes();
		}
		catch (Exception e)
		{
			Log.error("Error reading CLOB data:"+e.getMessage());
			return "Error reading CLOB data:".getBytes();
		}
    }

    @Override
    public String toString() {

        if (isValueNull()) {

            return null;
        }
        return getDisplayValue().toString();
    }

	public boolean isBlob() {

		return true;
	}
}


