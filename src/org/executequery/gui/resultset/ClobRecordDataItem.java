/*
 * ClobRecordDataItem.java
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import org.executequery.Constants;
import org.executequery.log.Log;
import org.underworldlabs.util.SystemProperties;

public class ClobRecordDataItem extends AbstractLobRecordDataItem {

	private int displayLength;

	private String displayValue;
	
	public ClobRecordDataItem(String name, int dataType, String dataTypeName) {

		super(name, dataType, dataTypeName);
		
		displayLength = SystemProperties.getIntProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.clob.length");
	}

	@Override
	public Object getDisplayValue() {

		if (displayValue == null) {

		    displayValue = new String(getData());
		    if (displayValue.length() > displayLength) {
		    
		        displayValue = displayValue.substring(0, displayLength);
		    }
		}
		
		return displayValue;
	}
	
	public String getLobRecordItemName() {

		return getDataTypeName();
	}

    protected byte[] readLob() {
    	
        Object value = getValue();
        if (value instanceof String) {

            return ((String) value).getBytes();
        }

    	Clob clob = (Clob) value;

    	Reader reader;
    	Writer writer = new StringWriter();
    	
		try {

			reader = clob.getCharacterStream();

		} catch (SQLException e) {

			if (Log.isDebugEnabled()) {
			
				Log.debug("Error reading CLOB data", e);
			}

			return e.getMessage().getBytes();
		}

        try {
        
            int read;

            while ((read = reader.read()) > -1) {
	
	            writer.write(read);
	        }
	        
	        writer.flush();

		} catch (IOException e) {

			if (Log.isDebugEnabled()) {
				
				Log.debug("Error reading CLOB data", e);
			}

			return e.getMessage().getBytes();
		
		} finally {
		    
            try {
                
                if (writer != null) {
                    
                    writer.close();
                }
                
            } catch (IOException e) {}

		}

    	return writer.toString().trim().getBytes();
    }

}

