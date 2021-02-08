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
import org.executequery.databaseobjects.impl.ColumnInformation;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
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
     * Returns the database meta data for this host.
     *
     * @return the database meta data
     */
    DatabaseMetaData getDatabaseMetaData() throws DataSourceException;

    /**
     * Returns the meta type objects from this schema
     *
     * @return the meta type objects
     */
    List<DatabaseMetaTag> getMetaObjects() throws DataSourceException;

    /**
     * Returns the meta type objects from this schema
     *
     * @return the meta type objects
     */
    List<DatabaseMetaTag> getMetaObjects(DatabaseCatalog catalog,
                                         DatabaseSchema schema) throws DataSourceException;

    /**
     * Returns the catalogs hosted by this host.
     *
     * @return the hosted catalogs
     */
    List<DatabaseCatalog> getCatalogs() throws DataSourceException;

    /**
     * Returns the schemas hosted by this host.
     *
     * @return the hosted schemas
     */
    List<DatabaseSchema> getSchemas() throws DataSourceException;

    /**
     * Returns the tables hosted by this host of the specified type and
     * belonging to the specified catalog and schema.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param type    the table type
     * @return the hosted tables
     */
    List<NamedObject> getTables(String catalog, String schema, String type)
            throws DataSourceException;

    /**
     * Returns the columns of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param table   the database object name
     * @return the columns
     */
    List<DatabaseColumn> getColumns(String catalog, String schema, String table)
            throws DataSourceException;

    /**
     * Returns the exported keys columns of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param table   the database object name
     * @return the exported keys
     */
    List<DatabaseColumn> getExportedKeys(String catalog, String schema, String table)
            throws DataSourceException;

    /**
     * Returns the priviliges of the specified object.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param table   the database object name
     */
    List<TablePrivilege> getPrivileges(String catalog, String schema, String table)
            throws DataSourceException;

    /**
     * Retrieves key/value pair database properties.
     */
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
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param table   the database object name
     * @return the column names
     */
    List<String> getColumnNames(String catalog, String schema, String table)
            throws DataSourceException;

    /**
     * Returns the table names hosted by this host of the specified type and
     * belonging to the specified catalog and schema.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param type    the table type
     * @return the hosted tables
     */
    List<String> getTableNames(String catalog, String schema, String type)
            throws DataSourceException;

    /**
     * Returns the default connected to catalog or null if there isn't one
     * or it can not be determined.
     *
     * @return the default catalog
     */
    DatabaseSource getDefaultCatalog();

    /**
     * Returns the default connected to schema or null if there isn't one
     * or it can not be determined.
     *
     * @return the default schema
     */
    DatabaseSource getDefaultSchema();

    /**
     * Returns whethere a current and valiud connection exists for this host.
     *
     * @return true | false
     */
    boolean isConnected();

    /**
     * Concatenates product name and product verision.
     */
    String getDatabaseProductNameVersion();

    /**
     * Get database product version.
     */
    String getDatabaseProductVersion();

    /**
     * Returns the default prefix name value for objects from this host.
     * ie. default catalog or schema name - with schema taking precedence.
     *
     * @return the default database object prefix
     */
    String getDefaultNamePrefix();

    /**
     * Returns the default database source object - schema or catalog with
     * schema taking precedence.
     *
     * @return the default database object prefix
     */
    DatabaseSource getDefaultDatabaseSource();

    /**
     * Returns the database source object with the specified name - schema or
     * catalog with schema taking precedence.
     *
     * @param name
     * @return the default database object prefix
     */
    DatabaseSource getDatabaseSource(String name);

    boolean supportsSchemasInTableDefinitions();

    boolean supportsCatalogsInTableDefinitions();

    boolean storesMixedCaseQuotedIdentifiers();

    boolean storesUpperCaseQuotedIdentifiers();

    boolean storesLowerCaseQuotedIdentifiers();

    boolean supportsMixedCaseQuotedIdentifiers();

    List<ColumnInformation> getColumnInformation(String catalog, String schema, String table) throws DataSourceException;

    String getCatalogNameForQueries(String catalogName);

    String getSchemaNameForQueries(String schemaName);

    boolean hasTablesForType(String catalog, String schema, String type);

    boolean supportCatalogOrSchemaInFunctionOrProcedureCalls();

    Connection getTemporaryConnection();

    int countFinishedMetaTags();

    void incCountFinishedMetaTags();

}

