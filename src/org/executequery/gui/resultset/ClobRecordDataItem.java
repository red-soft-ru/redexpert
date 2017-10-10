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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.log.Log;
import org.underworldlabs.util.SystemProperties;

public class ClobRecordDataItem extends AbstractLobRecordDataItem {

	private int displayLength;

	private static final String CLOB_DATA="<CLOB Data>";

	private String displayValue;

	DatabaseConnection dc;

	public ClobRecordDataItem(String tableName, String name, int dataType, String dataTypeName, DatabaseConnection dc,int row) {

		super(tableName,name, dataType, dataTypeName,row);

		displayLength = SystemProperties.getIntProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.clob.length");
		this.dc=dc;
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

		DefaultStatementExecutor executor=new DefaultStatementExecutor(dc,true);
		Object value = getValue();
        if (value instanceof String) {

            return ((String) value).getBytes();
        }
    	Clob clob = (Clob) value;
		InputStream as;
		try {
			String query="SELECT "+name+" FROM "+tableName;
			ResultSet rs=executor.execute(QueryTypes.SELECT,query).getResultSet();
			int i=0;
			while (rs.next()&&i<=row)
			{
				if(i==row)
					clob=rs.getClob(1);
				i++;
			}
			as = clob.getAsciiStream();
			byte[] b = new byte[1024];
			ByteArrayOutputStream result=new ByteArrayOutputStream();
			int length;
			int l=0;
			while ((length = as.read(b)) != -1&&l<displayLength) {
				result.write(b, 0, length);
				l+=length;
			}
			executor.releaseResources();
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


