/*
 * AbstractNamedObject.java
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

package org.executequery.databaseobjects.impl;

import org.apache.commons.lang.StringUtils;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract named database object implementation.
 *
 * @author Takis Diakoumis
 */
public abstract class AbstractNamedObject implements NamedObject,
        Cloneable {

    /**
     * indicates whether this object has been marked for a reload
     */
    private boolean markedForReload;

    /**
     * the name of this database object
     */
    private String name;

    /**
     * the parent object
     */
    private NamedObject parent;

    private boolean keepAlive;

    private boolean isSystem;

    public AbstractNamedObject() {
        keepAlive = true;
    }

    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object
     */
    public NamedObject getParent() {
        return parent;
    }

    /**
     * Sets the parent object to that specified.
     *
     * @param parent named object
     */
    public void setParent(NamedObject parent) {
        this.parent = parent;
    }

    /**
     * Returns whether this object has been marked for a reload on the
     * next call to its meta data specific methods.
     *
     * @return true | false
     */
    protected boolean isMarkedForReload() {
        return markedForReload;
    }

    /**
     * Sets the reload flag to that specified.
     *
     * @param markedForReload true | false
     */
    protected void setMarkedForReload(boolean markedForReload) {
        this.markedForReload = markedForReload;
    }

    /**
     * Marks this object as being 'reset', where for any loaded objectnode.
     * these are cleared and a fresh database call would be made where
     * appropriate.
     */
    public void reset() {
        markedForReload = true;
    }

    /**
     * Closes the specified sql result set object.
     *
     * @param rs the result set to be closed
     */
    protected void releaseResources(ResultSet rs) {
        try {
            if (rs == null) // On RDB 2.6 is null
                return;
            Statement st = rs.getStatement();
            if (rs != null) {
                if (!rs.isClosed())

                    rs.close();
            }
            if (st != null) {
                if (!st.isClosed())
                    st.close();
            }
        } catch (SQLException sqlExc) {
        }
    }

    /**
     * Closes the specified connection
     *
     * @param connection connection to be closed
     */
    protected void releaseResources(Connection connection) {
        try {
            if (connection != null) {
                if (!keepAlive)
                    connection.close();
            }
        } catch (SQLException e) {
        }
    }

    /**
     * Closes the specified sql statement and result set objects.
     *
     * @param stmnt statement to be closed
     * @param rs    the result set to be closed
     */
    protected void releaseResources(Statement stmnt, ResultSet rs) {
        releaseResources(rs);
        releaseResources(stmnt);
    }

    /**
     * Closes the specified sql statement object.
     *
     * @param stmnt statement to be closed
     */
    protected void releaseResources(Statement stmnt) {
        try {
            if (stmnt != null) {
                if (!stmnt.isClosed())
                    stmnt.close();
            }
        } catch (SQLException sqlExc) {
        }
    }

    /**
     * Retrieves child database objects of this named object.
     * Depending on the type of named object - this may return null.
     *
     * @return this meta tag's child database objects.
     */
    public List<NamedObject> getObjects() throws DataSourceException {
        return null;
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public abstract int getType();

    /**
     * Returns the name of this object.
     *
     * @return the object name
     */
    public String getName() {
        return name;
    }

    Map<String, String> resultSetRowToMap(ResultSet rs) throws SQLException {

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        String[] metaColumnNames = new String[columnCount];
        for (int i = 1; i < columnCount; i++) {
            metaColumnNames[i - 1] = rsmd.getColumnName(i);
        }

        Map<String, String> metaData = new HashMap<String, String>(columnCount);
        for (int i = 1; i < columnCount; i++) {
            metaData.put(metaColumnNames[i - 1].toUpperCase(), rs.getString(i));
        }

        return metaData;
    }

    public String getDescription() {

        if (getType() != META_TAG) {

            String metaDataKey = getMetaDataKey();
            if (StringUtils.isNotBlank(metaDataKey)) {

                return metaDataKey + ": " + getName();
            }
        }
        if (getObjects() != null)
            return getName() + " (" + getObjects().size() + ")";
        else return getName();
    }

    /**
     * Sets the name of this database object as specified.
     *
     * @param name the name of this database object
     */
    public void setName(String name) {
        if (name != null)
            this.name = name.trim();
        else this.name = name;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public abstract String getMetaDataKey();

    /**
     * Returns the display name of this object.
     *
     * @return the display name
     */
    public String getShortName() {
        return getName();
    }

    /**
     * Override to return a call from getName().
     */
    public String toString() {
        return getName();
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected final void logThrowable(Throwable e) {

        if (Log.isDebugEnabled()) {

            if (e instanceof SQLException) {

                logSQLException((SQLException) e);

            } else if (e.getCause() != null && e.getCause() instanceof SQLException) {

                logSQLException((SQLException) e.getCause());

            } else {

                e.printStackTrace();
            }

        }

    }

    protected final void logSQLException(SQLException e) {

        e.printStackTrace();
        SQLException nextException = e;

        while ((nextException = nextException.getNextException()) != null) {

            nextException.printStackTrace();
        }

    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystemFlag(boolean flag) {
        isSystem = flag;
    }

}


