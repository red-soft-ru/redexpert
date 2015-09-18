/*
 * SQLTypeObjectFactory.java
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

package org.executequery.databasemediators;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.underworldlabs.jdbc.DataSourceException;

public class SQLTypeObjectFactory {

    public Object create(int type, Object value) {
        
        Class<?> clazz = classForType(type);
        if (value.getClass().isAssignableFrom(clazz)) {
            
            return value;
        }
        
        Object valueAsType = value;
        String valueAsString = value.toString();
        switch (type) {
        
            case Types.TINYINT:
                valueAsType = Byte.valueOf(valueAsString);
                break;
        
            case Types.BIGINT:
                valueAsType = Long.valueOf(valueAsString);
                break;

            case Types.SMALLINT:
                valueAsType = Short.valueOf(valueAsString);
                break;
        
            case Types.LONGVARCHAR:
            case Types.CHAR:
            case Types.VARCHAR:
                valueAsType = valueAsString;
                break;
        
            case Types.BIT:
            case Types.BOOLEAN:
                valueAsType = Boolean.valueOf(valueAsString);
                break;
        
            case Types.NUMERIC:
            case Types.DECIMAL:
                valueAsType = new BigDecimal(valueAsString);
                break;
                
            case Types.INTEGER:
                valueAsType = Integer.valueOf(valueAsString);
                break;
                
            case Types.REAL:
                valueAsType = Float.valueOf(valueAsString);
                break;
        
            case Types.FLOAT:
            case Types.DOUBLE:
                valueAsType = Double.valueOf(valueAsString);
                break;

            case Types.DATE:
                valueAsType = stringAsDate(valueAsString);
                break;

            case Types.TIME:
                valueAsType = stringAsTime(valueAsString);
                break;
                
            case Types.TIMESTAMP:
                valueAsType = stringAsTimestamp(valueAsString);
                break;

            case Types.ARRAY:
                if (value instanceof Object[]) {
                    
                    valueAsType = Arrays.toString((Object[])value);
                }
                break;
                
        }

        return valueAsType;
    }
    
    private Date stringAsDate(String value) {
        
        try {
        
            return new java.sql.Date(parseDate(value).getTime());

        } catch (ParseException e) {
            
            throw new DataSourceException(e);
        }

    }
    
    private Timestamp stringAsTimestamp(String value) {
        
        try {
            
            return new Timestamp(parseDateTime(value).getTime());
            
        } catch (ParseException e) {
            
            throw new DataSourceException(e);
        }
        
    }

    private Time stringAsTime(String value) {
        
        try {
            
            DateFormat dateFormat;
            if (value.length() != TIME_FORMAT_SIMPLE_LENGTH) {
                
                dateFormat = new SimpleDateFormat(TIME_FORMAT_FULL);

            } else {
                
                dateFormat = new SimpleDateFormat(TIME_FORMAT_SIMPLE);
            }
            
            return new Time(dateFormat.parse(value).getTime());
            
        } catch (ParseException e) {
            
            throw new DataSourceException(e);
        }
        
    }
    
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT_FULL = "HH:mm:ss.S"; 
    private static final String TIME_FORMAT_SIMPLE = "HH:mm:ss"; 
    private static final int TIME_FORMAT_SIMPLE_LENGTH = TIME_FORMAT_SIMPLE.length();
    
    private Date parseDate(String value) throws ParseException {
        
        return new SimpleDateFormat(DATE_FORMAT).parse(value);
    }
    
    private Date parseDateTime(String value) throws ParseException {
        
        return new SimpleDateFormat(DATE_FORMAT + " " + TIME_FORMAT_FULL).parse(value);
    }
    
    private Class<?> classForType(int type) {
        
        switch (type) {
        
            case Types.TINYINT:
                return Byte.class;
        
            case Types.BIGINT:
                return Long.class;
    
            case Types.SMALLINT:
                return Short.class;
        
            case Types.LONGVARCHAR:
            case Types.CHAR:
            case Types.VARCHAR:
                return String.class;
        
            case Types.BIT:
            case Types.BOOLEAN:
                return Boolean.class;
        
            case Types.NUMERIC:
            case Types.DECIMAL:
                return BigDecimal.class;
                
            case Types.INTEGER:
                return Integer.class;
                
            case Types.REAL:
                return Float.class;
        
            case Types.FLOAT:
            case Types.DOUBLE:
                return Double.class;
    
            case Types.DATE:
                return Date.class;
    
            case Types.TIME:
                return Time.class;
                
            case Types.TIMESTAMP:
                return Timestamp.class;
    
        }
        
        return Object.class;
    }
    
}

