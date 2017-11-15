/*
 * NamedParameterQuery.java
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

package org.underworldlabs.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dynamic query parser and executor.
 *
 * @author Takis Diakoumis
 */
public class NamedParameterQuery {
    
    /** the regex matcher for parameter substitution */
    private static Matcher MATCHER;
    
    /** the parameters to be applied for the current query */
    private List parameters;
    
    /** the sql statement object to be executed */
    private PreparedStatement pstmnt;
    
    /** the JDBC connection */
    private Connection connection;

    /**
     * Creates a new instance with the specified database
     * connection.
     *
     * @param the JDBC connection
     */
    public NamedParameterQuery(Connection connection) {
        this(connection, null);
    }

    /**
     * Creates a new instance with the specified database
     * connection and the specified query.
     *
     * @param the JDBC connection
     */
    public NamedParameterQuery(Connection connection, String query) {
        this.connection = connection;
        if (query != null) {
            prepareStatement(query);
        }
    }
    
    /**
     * Prepares the statement using the specified query.
     *
     * @param the query to be executed
     */
    public void prepareStatement(String query) {      
        try {
            buildStatement(query);
        } catch (SQLException e) {
            throw new DataSourceRuntimeException(e);
        }
    }

    protected boolean isStatementPrepared() {
        return pstmnt != null;
    }

    protected void buildStatement(String query) throws SQLException {
        if (pstmnt != null) {
            pstmnt.close();
        }

        if (MATCHER == null) {
            MATCHER = Pattern.compile("\\${1}(\\w)*\\b").matcher(query);
        } else {
            MATCHER.reset(query);
        }

        int start = 0;
        int end = 0;
        parameters = new ArrayList();
        while (MATCHER.find()) {
            start = MATCHER.start();
            end = MATCHER.end();
            parameters.add(query.substring(start + 1, end));
        }
        
        query = MATCHER.replaceAll("?");
        
        //Log.debug("Query: " + query);

        pstmnt = connection.prepareStatement(query);
    }
    
    protected String getSetter(String fieldName) {
        char[] chars = fieldName.toCharArray();
        StringBuffer sb = new StringBuffer("get");
        
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                sb.append(Character.toUpperCase(chars[i]));
            } else {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }
    
    public void setValues(Object object) {
        try {
            Class[] params = new Class[0];
            Object[] args = new Object[0];
            Class clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                String name = fields[i].getName();
                Class fieldType = fields[i].getType();
                String methodName = getSetter(name);
                Method method = clazz.getDeclaredMethod(methodName, params);
                Object value = method.invoke(object, args);

                /*
                System.out.println("name: " + name + 
                        " type: " + fieldType.getName() + 
                        " value: " + value);
                */

                if (fieldType == String.class) {
                    setString(name, (String)value);
                } else if (fieldType == Integer.class) {
                    setInt(name, (Integer)value);
                } else if (fieldType == Double.class) {
                    setDouble(name, (Double)value);
                } else if (fieldType == Float.class) {
                    setFloat(name, (Float)value);
                } else if (fieldType == Long.class) {
                    setLong(name, (Long)value);
                } else if (fieldType == Boolean.class) {
                    setBoolean(name, (Boolean)value);
                } else if (fieldType == java.util.Date.class) {
                    if (value != null) {
                        long _value = ((java.util.Date)value).getTime();
                        setDate(name, new Date(_value));
                    } else {
                        setDate(name, null);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    protected int getParameterPosition(String name) {
        for (int i = 0, k = parameters.size(); i < k; i++) {
            if (name.equals(parameters.get(i))) {
                return i;
            }
        }
        return -1;
    }
    
    public ResultSet executeQuery() throws SQLException {
        return pstmnt.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        return pstmnt.executeUpdate();
    }
    
    public void setString(String name, String value) throws SQLException {
        for (int i = 0, k = parameters.size(); i < k; i++) {
            if (name.equals(parameters.get(i))) {                
                if (value == null) {
                    pstmnt.setNull(i + 1, Types.VARCHAR);
                } else {
                    pstmnt.setString(i + 1, value);
                }
            }
        }
    }

    public void setInt(String name, Integer value) throws SQLException {
        for (int i = 0, k = parameters.size(); i < k; i++) {
            if (name.equals(parameters.get(i))) {
                if (value == null) {
                    pstmnt.setNull(i + 1, Types.INTEGER);
                } else {
                    pstmnt.setInt(i + 1, value.intValue());
                }
            }
        }
    }

    public void setLong(String name, Long value) throws SQLException {
        for (int i = 0, k = parameters.size(); i < k; i++) {
            if (name.equals(parameters.get(i))) {
                if (value == null) {
                    pstmnt.setNull(i + 1, Types.NUMERIC);
                } else {
                    pstmnt.setLong(i + 1, value.longValue());
                }
            }
        }
    }

    public void setFloat(String name, Float value) throws SQLException {
        for (int i = 0, k = parameters.size(); i < k; i++) {
            if (name.equals(parameters.get(i))) {
                if (value == null) {
                    pstmnt.setNull(i + 1, Types.FLOAT);
                } else {
                    pstmnt.setFloat(i + 1, value.floatValue());
                }
            }
        }
    }

    public void setDouble(String name, Double value) throws SQLException {
        for (int i = 0, k = parameters.size(); i < k; i++) {
            if (name.equals(parameters.get(i))) {
                if (value == null) {
                    pstmnt.setNull(i + 1, Types.DOUBLE);
                } else {
                    pstmnt.setDouble(i + 1, value.doubleValue());
                }
            }
        }
    }

    public void setBoolean(String name, Boolean value) throws SQLException {
        for (int i = 0, k = parameters.size(); i < k; i++) {
            if (name.equals(parameters.get(i))) {
                if (value == null) {
                    pstmnt.setNull(i + 1, Types.BOOLEAN);
                } else {
                    pstmnt.setBoolean(i + 1, value.booleanValue());
                }
            }
        }
    }
    
    public void setDate(String name, java.util.Date value) throws SQLException {
        for (int i = 0, k = parameters.size(); i < k; i++) {
            if (name.equals(parameters.get(i))) {
                if (value == null) {
                    pstmnt.setNull(i + 1, Types.DATE);
                } else {
                    pstmnt.setTimestamp(i + 1, new Timestamp(value.getTime()));
                }
            }
        }
    }
    
    public void close() {
        try {
            if (pstmnt != null) {
                pstmnt.close();
                pstmnt = null;
            }
            if (MATCHER != null) {
                MATCHER.reset();
            }
        } catch (SQLException e) {}
    }

}











