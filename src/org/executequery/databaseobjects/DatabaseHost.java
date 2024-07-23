/*
 * DatabaseHost.java
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

import org.executequery.databasemediators.DatabaseConnection;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Defines a database host object.
 * This is the top-level object for a particular database connection.
 *
 * @author Takis Diakoumis
 */
public interface DatabaseHost extends NamedObject {

    /**
     * Closes the connection associated with this host.
     */
    void close();

    /**
     * Returns the database connection wrapper object for this host.
     *
     * @return the database connection wrapper
     */
    DatabaseConnection getDatabaseConnection();

    /**
     * Returns the sql connection for this host.
     *
     * @return the sql connection
     */
    Connection getConnection() throws DataSourceException;

    /**
     * Returns the database metadata for this host.
     *
     * @return the database meta data
     */
    DatabaseMetaData getDatabaseMetaData() throws DataSourceException;

    /**
     * Returns the meta type objects
     *
     * @return the meta type objects
     */
    List<DatabaseMetaTag> getMetaObjects() throws DataSourceException;

    /**
     * Returns the columns of the specified database object.
     *
     * @return the columns
     */
    List<DatabaseColumn> getColumns(String table);

    /**
     * Returns the privileges of the specified object.
     *
     * @param table the database object name
     */
    List<TablePrivilege> getPrivileges(String table)
            throws DataSourceException;

    /**
     * Retrieves key/value pair database properties.
     */
    Map<Object, Object> getMetaProperties() throws DataSourceException;

    Map<Object, Object> getDatabaseProperties() throws DataSourceException;

    /**
     * Get database product name.
     */
    String getDatabaseProductName();

    /**
     * Retrieves the database keywords associated with this host.
     */
    String[] getDatabaseKeywords() throws DataSourceException;

    /**
     * Retrieves the data types associated with this host.
     */
    ResultSet getDataTypeInfo() throws DataSourceException;

    /**
     * Recycles the open database connection.
     */
    void recycleConnection() throws DataSourceException;

    /**
     * Attempts to establish a connection using this host.
     */
    boolean connect() throws DataSourceException;

    /**
     * Disconnects this host.
     */
    boolean disconnect() throws DataSourceException;

    /**
     * Returns the column names of the specified database object.
     *
     * @param table the database object name
     * @return the column names
     */
    List<String> getColumnNames(String table)
            throws DataSourceException;

    /**
     * Returns the table names hosted by this host.
     *
     * @return the hosted tables
     */
    List<String> getTableNames() throws DataSourceException;

    /**
     * Returns whether a current and valid connection exists for this host.
     *
     * @return true | false
     */
    boolean isConnected();

    /**
     * Concatenates product name and product version.
     */
    String getDatabaseProductNameVersion();

    /**
     * Get database product version.
     */
    String getDatabaseProductVersion();

    int getDatabaseMajorVersion() throws SQLException;

    /**
     * Returns the default prefix name value for objects from this host.
     *
     * @return the default database object prefix
     */
    String getDefaultNamePrefix();

    /**
     * Returns the database source object with the specified name.
     *
     * @return the default database object prefix
     */
    DatabaseSource getDatabaseSource(String name);

    Connection getTemporaryConnection();

    boolean isPauseLoadingTreeForSearch();

    void setPauseLoadingTreeForSearch(boolean pauseLoadingTreeForSearch);

}
