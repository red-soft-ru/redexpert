/*
 * DefaultDatabaseHost.java
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

package org.executequery.databaseobjects.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseCatalog;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseSchema;
import org.executequery.databaseobjects.DatabaseSource;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.TablePrivilege;
import org.executequery.datasource.ConnectionManager;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

/**
 * Default database host object implementation.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DefaultDatabaseHost extends AbstractNamedObject
                                 implements DatabaseHost {

    /** the database connection wrapper for this host */
    private transient DatabaseConnection databaseConnection;

    /** the SQL connection for this host */
    private transient Connection connection;

    /** the database meta data object for this host */
    private transient DatabaseMetaData databaseMetaData;

    /** the catalogs of this host */
    private List<DatabaseCatalog> catalogs;

    /** the schemas of this host */
    private List<DatabaseSchema> schemas;

    /**
     * Creates a new instance of DefaultDatabaseHost with the
     * specifiec database connection wrapper.
     *
     * @param databaseConnection the connection wrapper
     */
    DefaultDatabaseHost(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Attempts to establish a connection using this host.
     */
    public boolean connect() throws DataSourceException {

        if (!isConnected()) {

            return connectionMediator().connect(getDatabaseConnection());
        }

        return true;
    }

    /**
     * Disconnects this host entirely - pool closed!
     */
    public boolean disconnect() throws DataSourceException {

        try {

            connectionMediator().disconnect(getDatabaseConnection());
            return true;

        } finally {

            schemas = null;
            catalogs = null;
            databaseMetaData = null;
            connection = null;
        }

    }

    /**
     * Closes the connection associated with this host.
     */
    public void close() {
        if (connection != null) {
            databaseMetaData = null;
            ConnectionManager.close(getDatabaseConnection(), connection);
            connection = null;
        }
    }

    /**
     * Returns the database connection wrapper object for this host.
     *
     * @return the database connection wrapper
     */
    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    /**
     * Recycles the open database connection.
     */
    public void recycleConnection() throws DataSourceException {
        close();
    }

    /**
     * Returns the sql connection for this host.
     *
     * @return the sql connection
     */
    public Connection getConnection() throws DataSourceException {
        
        try {
            
            if ((connection == null || connection.isClosed())
                    && getDatabaseConnection().isConnected()) {

                databaseMetaData = null;
                connection = ConnectionManager.getConnection(getDatabaseConnection());
            }
        } catch (SQLException e) {

            throw new DataSourceException(e);
        }

        return connection;
    }

    public Connection getTemporaryConnection() {

        return ConnectionManager.getConnection(getDatabaseConnection());
    }
    
    /**
     * Returns the database meta data for this host.
     *
     * @return the database meta data
     */
    public DatabaseMetaData getDatabaseMetaData() throws DataSourceException {

        if (!isConnected()) {

            throw new DataSourceException("Connection closed.", true);
        }

        try {

            if (databaseMetaData == null) {
    
                databaseMetaData = getConnection().getMetaData();

            } else if (databaseMetaData.getConnection().isClosed()) {
                
                databaseMetaData = null;
                return getDatabaseMetaData();                
            }


        } catch (SQLException e) {
            
            throw new DataSourceException(e);
        }
        
        return databaseMetaData;
    }

    /**
     * Returns the catalogs hosted by this host.
     *
     * @return the hosted catalogs
     */
    public List<DatabaseCatalog> getCatalogs() throws DataSourceException {

        if (!isMarkedForReload() && catalogs != null) {

            return catalogs;
        }

        ResultSet rs = null;
        try {

            catalogs = new ArrayList<DatabaseCatalog>();
            rs = getDatabaseMetaData().getCatalogs();
            while (rs.next()) {

                catalogs.add(new DefaultDatabaseCatalog(this, rs.getString(1)));
            }

            return catalogs;
        }
        catch (SQLException e) {

            throw new DataSourceException(e);
        }
        finally {

            releaseResources(rs);
            setMarkedForReload(false);
        }
    }

    /**
     * Returns the schemas hosted by this host.
     *
     * @return the hosted schemas
     */
    public List<DatabaseSchema> getSchemas() throws DataSourceException {

        if (!isMarkedForReload() && schemas != null) {

            return schemas;
        }

        ResultSet rs = null;
        try {

            schemas = new ArrayList<DatabaseSchema>();

            rs = getDatabaseMetaData().getSchemas();

            if (rs != null) {

                while (rs.next()) {

                    schemas.add(new DefaultDatabaseSchema(this, rs.getString(1)));
                }

            }

            return schemas;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            releaseResources(rs);
            setMarkedForReload(false);
        }
    }

    @SuppressWarnings("resource")
    public boolean hasTablesForType(String catalog, String schema, String type) {
        
        ResultSet rs = null;
        try {
            String _catalog = getCatalogNameForQueries(catalog);
            String _schema = getSchemaNameForQueries(schema);
            DatabaseMetaData dmd = getDatabaseMetaData();

            rs = dmd.getTables(_catalog, _schema, null, new String[]{type});
            while(rs.next()) {
                
                if (StringUtils.equalsIgnoreCase(type, rs.getString(4))) {
                    
                    return true;
                }
                
            }
            
            return false;
            
        } catch (SQLException e) {

            if (Log.isDebugEnabled()) {

                Log.error("Tables not available for type "
                        + type + " - driver returned: " + e.getMessage());
            }

            return false;

        } finally {

            releaseResources(rs);
        }
        
    }
    
    /**
     * Returns the tables hosted by this host of the specified type and
     * belonging to the specified catalog and schema.
     *
     * @param catalog the table catalog name
     * @param schema the table schema name
     * @param type the table type
     * @return the hosted tables
     */
    public List<NamedObject> getTables(String catalog, String schema, String type)
        throws DataSourceException {

        ResultSet rs = null;
        try {
            String _catalog = getCatalogNameForQueries(catalog);
            String _schema = getSchemaNameForQueries(schema);
            DatabaseMetaData dmd = getDatabaseMetaData();

            String tableName = null;
            String typeName = null;

            List<NamedObject> tables = new ArrayList<NamedObject>();

            String[] types = null;
            if (type != null) {

                types = new String[]{type};
            }

            rs = dmd.getTables(_catalog, _schema, null, types);

            // make sure type isn't null for compare
            if (type == null) {
                type = "";
            }

            while (rs.next()) {

                tableName = rs.getString(3);
                typeName = rs.getString(4);

                // only include if the returned reported type matches
                if (type.equalsIgnoreCase(typeName)) {

                    DefaultDatabaseObject object = new DefaultDatabaseObject(this, type);
                    object.setCatalogName(catalog);
                    object.setSchemaName(schema);
                    object.setName(tableName);
                    object.setRemarks(rs.getString(5));
                    tables.add(object);
                }

            }

            return tables;

        } catch (SQLException e) {

            if (Log.isDebugEnabled()) {

                Log.error("Tables not available for type "
                        + type + " - driver returned: " + e.getMessage());
            }

            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }

    }

    private DatabaseSchema getSchema(String name) throws DataSourceException {

        if (name != null) {

            name = name.toUpperCase();

            List<DatabaseSchema> _schemas = getSchemas();
            for (DatabaseSchema schema : _schemas) {

                if (name.equals(schema.getName().toUpperCase())) {

                    return schema;
                }

            }

        } else if (getSchemas().size() == 1) {

            return getSchemas().get(0);
        }

        return null;
    }

    /**
     * Returns the exported keys columns of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema the table schema name
     * @param table the database object name
     * @return the exported keys
     */
    public List<DatabaseColumn> getExportedKeys(String catalog, String schema, String table)
        throws DataSourceException {

        ResultSet rs = null;
        try {

            String _catalog = getCatalogNameForQueries(catalog);
            String _schema = getSchemaNameForQueries(schema);
            DatabaseMetaData dmd = getDatabaseMetaData();

            List<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();

            String tableTagName = "TABLE";

            // retrieve the base column info
            rs = dmd.getExportedKeys(_catalog, _schema, table);
            while (rs.next()) {

                String fkSchema = rs.getString(6);
                DatabaseSchema databaseSchema = getSchema(fkSchema);

                if (databaseSchema != null) {

                    String fkTable = rs.getString(7);
                    String fkColumn = rs.getString(8);

                    DatabaseMetaTag metaTag = databaseSchema.getDatabaseMetaTag(tableTagName);

                    DatabaseTable databaseTable = (DatabaseTable)metaTag.getNamedObject(fkTable);
                    columns.add(databaseTable.getColumn(fkColumn));
                }

            }

            return columns;
        }
        catch (SQLException e) {

            throw new DataSourceException(e);
        }
        finally {

            releaseResources(rs);
        }

    }

    public String getCatalogNameForQueries(String catalogName) {
        
        DatabaseMetaData dmd = getDatabaseMetaData();
        try {
            if (!dmd.supportsCatalogsInTableDefinitions()) {

                return null;
            }
        } catch (SQLException e) {

            throw new DataSourceException(e);
        }

        return catalogName;
    }
    
    public String getSchemaNameForQueries(String schemaName) {
        
        DatabaseMetaData dmd = getDatabaseMetaData();
        try {
            if (!dmd.supportsSchemasInTableDefinitions()) {
                
                return null;
            }
        } catch (SQLException e) {
            
            throw new DataSourceException(e);
        }
        
        return schemaName;
    }
    
    /**
     * Returns the table names hosted by this host of the specified type and
     * belonging to the specified catalog and schema.
     *
     * @param catalog the table catalog name
     * @param schema the table schema name
     * @param type the table type
     * @return the hosted tables
     */
    public List<String> getTableNames(String catalog, String schema, String type)
        throws DataSourceException {

        ResultSet rs = null;
        try {
            String _catalog = getCatalogNameForQueries(catalog);
            String _schema = getSchemaNameForQueries(schema);
            DatabaseMetaData dmd = getDatabaseMetaData();

            String typeName = null;
            List<String> tables = new ArrayList<String>();

            String[] types = null;
            if (type != null) {

                types = new String[]{type};
            }

            rs = dmd.getTables(_catalog, _schema, null, types);
            while (rs.next()) {

                typeName = rs.getString(4);

                // only include if the returned reported type matches
                if (type != null && type.equalsIgnoreCase(typeName)) {

                    tables.add(rs.getString(3));
                }

            }

            return tables;

        } catch (SQLException e) {

            if (Log.isDebugEnabled()) {

                Log.error("Tables not available for type "
                        + type + " - driver returned: " + e.getMessage());
            }

            return new ArrayList<String>(0);

        } finally {

            releaseResources(rs);
        }

    }

    /**
     * Returns the column names of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema the table schema name
     * @param table the database object name
     * @return the column names
     */
    public List<String> getColumnNames(String catalog, String schema, String table)
        throws DataSourceException {

        ResultSet rs = null;
        List<String> columns = new ArrayList<String>();

        try {
            String _catalog = getCatalogNameForQueries(catalog);
            String _schema = getSchemaNameForQueries(schema);
            DatabaseMetaData dmd = getDatabaseMetaData();

            // retrieve the base column info
            rs = dmd.getColumns(_catalog, _schema, table, null);
            while (rs.next()) {

                columns.add(rs.getString(4));
            }

            return columns;

        } catch (SQLException e) {

            if (Log.isDebugEnabled()) {

                Log.error("Error retrieving column data for table " + table 
                        + " using connection " + getDatabaseConnection(), e);
            }

            return columns;

        } finally {

            releaseResources(rs);
        }

    }

    private ColumnInformationFactory columnInformationFactory = new ColumnInformationFactory();
    
    /**
     * Returns the column names of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema the table schema name
     * @param table the database object name
     * @return the column names
     */
    public List<ColumnInformation> getColumnInformation(String catalog, String schema, String table)
        throws DataSourceException {

        ResultSet rs = null;
        List<ColumnInformation> columns = new ArrayList<ColumnInformation>();

        try {
            String _catalog = getCatalogNameForQueries(catalog);
            String _schema = getSchemaNameForQueries(schema);
            DatabaseMetaData dmd = getDatabaseMetaData();

            if (!isConnected()) {

                return new ArrayList<ColumnInformation>(0);
            }

            // retrieve the base column info
            rs = dmd.getColumns(_catalog, _schema, table, null);
            while (rs.next()) {

                String name = rs.getString(4);
                columns.add(columnInformationFactory.build(
                        table, 
                        name, 
                        rs.getString(6),
                        rs.getInt(5),
                        rs.getInt(7),
                        rs.getInt(9),
                        rs.getInt(11) == DatabaseMetaData.columnNoNulls)); 
            }

            return columns;

        } catch (SQLException e) {

            if (Log.isDebugEnabled()) {

                Log.error("Error retrieving column data for table " + table 
                        + " using connection " + getDatabaseConnection(), e);
            }

            return columns;

        } finally {

            releaseResources(rs);
        }

    }

    /**
     * Returns the columns of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema the table schema name
     * @param table the database object name
     * @return the columns
     */
    public List<DatabaseColumn> getColumns(String catalog, String schema, String table)
        throws DataSourceException {

        ResultSet rs = null;

        List<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();

        try {
            String _catalog = getCatalogNameForQueries(catalog);
            String _schema = getSchemaNameForQueries(schema);
            DatabaseMetaData dmd = getDatabaseMetaData();

            // retrieve the base column info
            rs = dmd.getColumns(_catalog, _schema, table, null);
            
            /*
            if (Log.isDebugEnabled()) {

                Log.debug("Meta data on columns for table - " + table);
                
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    
                    Log.debug("Column: [ " + (i + 1) + " ] " + metaData.getColumnName(i + 1));                    
                }
            }
            */

            while (rs.next()) {

                DefaultDatabaseColumn column = new DefaultDatabaseColumn();
                column.setCatalogName(catalog);
                column.setSchemaName(schema);
                column.setName(rs.getString(4));
                column.setTypeInt(rs.getInt(5));
                column.setTypeName(rs.getString(6));
                column.setColumnSize(rs.getInt(7));
                column.setColumnScale(rs.getInt(9));
                column.setRequired(rs.getInt(11) == DatabaseMetaData.columnNoNulls);
                column.setRemarks(rs.getString(12));
                column.setDefaultValue(rs.getString(13));
                columns.add(column);
            }
            releaseResources(rs);

            int columnCount = columns.size();
            if (columnCount > 0) {

                // check for primary keys
                rs = dmd.getPrimaryKeys(_catalog, _schema, table);
                while (rs.next()) {

                    String pkColumn = rs.getString(4);

                    // find the pk column in the previous list
                    for (int i = 0; i < columnCount; i++) {

                        DatabaseColumn column = columns.get(i);
                        String columnName = column.getName();

                        if (columnName.equalsIgnoreCase(pkColumn)) {
                            ((DefaultDatabaseColumn)column).setPrimaryKey(true);
                            break;
                        }

                    }

                }
                releaseResources(rs);

                // check for foreign keys
                rs = dmd.getImportedKeys(_catalog, _schema, table);
                while (rs.next()) {
                    String fkColumn = rs.getString(8);

                    // find the fk column in the previous list
                    for (int i = 0; i < columnCount; i++) {
                        DatabaseColumn column = columns.get(i);
                        String columnName = column.getName();
                        if (columnName.equalsIgnoreCase(fkColumn)) {
                            ((DefaultDatabaseColumn)column).setForeignKey(true);
                            break;
                        }
                    }

                }

            }

            return columns;

        } catch (SQLException e) {

            if (Log.isDebugEnabled()) {

                Log.error("Error retrieving column data for table " + table 
                        + " using connection " + getDatabaseConnection(), e);
            }

            return columns;

//            throw new DataSourceException(e);
        
        } finally {
          
            releaseResources(rs);
        }

    }

    public boolean supportCatalogOrSchemaInFunctionOrProcedureCalls() throws DataSourceException {

        try {
            
            DatabaseMetaData dmd = getDatabaseMetaData();
            return dmd.supportsCatalogsInProcedureCalls() || dmd.supportsSchemasInProcedureCalls();

        }
        catch (SQLException e) {

            throw new DataSourceException(e);
        }
    }
    
    /**
     * Returns the priviliges of the specified object.
     *
     * @param catalog the table catalog name
     * @param schema the table schema name
     * @param table the database object name
     */
    public List<TablePrivilege> getPrivileges(String catalog, String schema, String table)
        throws DataSourceException {

        ResultSet rs = null;
        try {
            String _catalog = getCatalogNameForQueries(catalog);
            String _schema = getSchemaNameForQueries(schema);
            DatabaseMetaData dmd = getDatabaseMetaData();

            List<TablePrivilege> privs = new ArrayList<TablePrivilege>();
            rs = dmd.getTablePrivileges(_catalog, _schema, table);
            while (rs.next()) {
                privs.add(new TablePrivilege(rs.getString(4),
                                             rs.getString(5),
                                             rs.getString(6),
                                             rs.getString(7)));
            }
            return privs;
        }
        catch (SQLException e) {
            throw new DataSourceException(e);
        }
        finally {
            releaseResources(rs);
        }

    }

    /**
     * Returns the default prefix name value for objects from this host.
     * ie. default catalog or schema name - with schema taking precedence.
     *
     *  @return the default database object prefix
     */
    public String getDefaultNamePrefix() {

        DatabaseSource source = getDefaultDatabaseSource();
        if (source != null) {

            return source.getName();
        }

        return null;
    }

    /**
     * Returns the database source with the name specified scanning schema
     * sources first, then catalogs.
     *
     * @param the name
     * @return the database source object
     */
    public DatabaseSource getDatabaseSource(String name) {

        if (name == null) {

            return getDatabaseSource(getDefaultNamePrefix());
        }

        DatabaseSource source = findByName(getSchemas(), name);
        if (source == null) {

            source = findByName(getCatalogs(), name);
        }

        return source;
    }

    private DatabaseSource findByName(List<?> sources, String name) {

        if (sources != null) {

            String _name = name.toUpperCase();
            for (int i = 0, n = sources.size(); i < n; i++) {

                DatabaseSource source = (DatabaseSource) sources.get(i);
                if (source.getName().toUpperCase().equals(_name)) {

                    return source;
                }

            }

        }

        return null;
    }

    /**
     * Returns the default database source object - schema or catalog with
     * schema taking precedence.
     *
     *  @return the default database object prefix
     */
    public DatabaseSource getDefaultDatabaseSource() {

        DatabaseSource source = getDefaultSchema();
        if (source == null) {

            source = getDefaultCatalog();
        }

        return source;
    }

    /**
     * Returns the default connected to catalog or null if there isn't one
     * or it can not be determined.
     *
     * @return the default catalog
     */
    public DatabaseSource getDefaultCatalog() {

        for (DatabaseCatalog databaseCatalog : getCatalogs()) {

            if (databaseCatalog.isDefault()) {

                return databaseCatalog;
            }

        }

        return null;
    }

    /**
     * Returns the default connected to schema or null if there isn't one
     * or it can not be determined.
     *
     * @return the default schema
     */
    public DatabaseSource getDefaultSchema() {

        for (DatabaseSchema databaseSchema : getSchemas()) {

            if (databaseSchema.isDefault()) {

                return databaseSchema;
            }

        }

        return null;
    }

    /**
     * Returns the meta type objects from the specified schema and catalog.
     *
     * @return the meta type objects
     */
    public List<DatabaseMetaTag> getMetaObjects() throws DataSourceException {

        return getMetaObjects(null, null);
    }

    /**
     * Returns the meta type objects from the specified schema and catalog.
     *
     * @return the meta type objects
     */
    public List<DatabaseMetaTag> getMetaObjects(DatabaseCatalog catalog,
            DatabaseSchema schema) throws DataSourceException {

        List<DatabaseMetaTag> metaObjects = new ArrayList<DatabaseMetaTag>();
        createDefaultMetaObjects(catalog, schema, metaObjects);

        // load other types available not included in the defaults
        ResultSet rs = null;
        try {

            rs = getDatabaseMetaData().getTableTypes();
            while (rs.next()) {

                String type = rs.getString(1);
                if (!MiscUtils.containsValue(META_TYPES, type)) {

                    DatabaseMetaTag metaTag =
                        createDatabaseMetaTag(catalog, schema, type);

                    if (metaTag.hasChildObjects()) {

                        metaObjects.add(metaTag);
                    }

                }

            }

            return metaObjects;
        }
        catch (SQLException e) {
            throw new DataSourceException(e);
        }
        finally {
            releaseResources(rs);
            setMarkedForReload(false);
        }

    }

    private void createDefaultMetaObjects(DatabaseCatalog catalog,
            DatabaseSchema schema, List<DatabaseMetaTag> metaObjects)
            throws DataSourceException {

        for (int i = 0; i < META_TYPES.length; i++) {

            DefaultDatabaseMetaTag metaTag =
                createDatabaseMetaTag(catalog, schema, META_TYPES[i]);

            metaTag.setCatalog(catalog);
            metaTag.setSchema(schema);

            if (metaTag.hasChildObjects()) {

                metaObjects.add(metaTag);
            }

        }
    }

    private DefaultDatabaseMetaTag createDatabaseMetaTag(
            DatabaseCatalog catalog, DatabaseSchema schema, String type) {

        return new DefaultDatabaseMetaTag(this, catalog, schema, type);
    }

    /**
     * Retrieves key/value type pairs using the <code>Reflection</code>
     * API to call and retrieve values from the connection's meta data
     * object's methods and variables.
     *
     *  @return the database properties as key/value pairs
     */
    public Map<Object, Object> getDatabaseProperties() throws DataSourceException {

        DatabaseMetaData dmd = getDatabaseMetaData();

        Object[] defaultArg = new Object[]{};

        Map<Object, Object> properties = new HashMap<Object, Object>();

        String STRING = "String";
        String GET = "get";

        Class<?> metaClass = dmd.getClass();
        Method[] metaMethods = metaClass.getMethods();

        for (int i = 0; i < metaMethods.length; i++) {

            Class<?> clazz = metaMethods[i].getReturnType();

            String methodName = metaMethods[i].getName();
            if (methodName == null || clazz == null) {

                continue;
            }

            if (clazz.isPrimitive() || clazz.getName().endsWith(STRING)) {

                if (methodName.startsWith(GET)) {

                    methodName = methodName.substring(3);
                }

                try {

                    Object res = metaMethods[i].invoke(dmd, defaultArg);
                    if (res != null) {

                        properties.put(methodName, res.toString());
                    }

                } catch (AbstractMethodError e) {

                    continue;

                } catch (IllegalArgumentException e) {

                    continue;

                } catch (IllegalAccessException e) {

                    continue;

                } catch (InvocationTargetException e) {

                    continue;
                }

            }

        }

        return properties;
    }

    public boolean supportsCatalogsInTableDefinitions() {

        try {

            return getDatabaseMetaData().supportsCatalogsInTableDefinitions();

        } catch (SQLException e) {

            return false;
        }
    }

    public boolean supportsSchemasInTableDefinitions() {

        try {

            return getDatabaseMetaData().supportsSchemasInTableDefinitions();

        } catch (SQLException e) {

            return false;
        }
    }

    /**
     * Concatenates product name and product verision.
     */
    public String getDatabaseProductNameVersion() {

        return getDatabaseProductName() + " " + getDatabaseProductVersion();
    }

    /**
     * Get database product name.
     */
    public String getDatabaseProductName() {

        if (isConnected()) {

            return (String) getDatabaseProperties().get("DatabaseProductName");
        }

        return getDatabaseConnection().getDatabaseType();
    }

    /**
     * Get database product version.
     */
    public String getDatabaseProductVersion() {

        if (isConnected()) {

            return (String) getDatabaseProperties().get("DatabaseProductVersion");
        }

        return getDatabaseConnection().getDatabaseType();
    }

    public boolean isConnected() {

        try {

            Connection _connection = getConnection();
            return (_connection != null && !_connection.isClosed());

        } catch (SQLException e) {

            return false;
        }
    }

    /**
     * Retrieves the database keywords associated with this host.
     */
    public String[] getDatabaseKeywords() throws DataSourceException {
        try {
            return MiscUtils.splitSeparatedValues(
                    getDatabaseMetaData().getSQLKeywords(), ",");
        }
        catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Retrieves the data types associated with this host.
     */
    public ResultSet getDataTypeInfo() throws DataSourceException {
        try {
            return getDatabaseMetaData().getTypeInfo();
        }
        catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    /** Does nothing. */
    public int drop() throws DataSourceException {
        return 0;
    }

    /** Returns NULL. */
    public NamedObject getParent() {
        return null;
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return HOST;
    }

    /**
     * Returns the name of this object.
     *
     * @return the object name
     */
    public String getName() {
        return getDatabaseConnection().getName();
    }

    /**
     * Override to do nothing. Name retrieved from underlying
     * connection wrapper object.
     */
    public void setName(String name) {}

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return null;
    }

    private ConnectionMediator connectionMediator() {
        return ConnectionMediator.getInstance();
    }

    private static final long serialVersionUID = 1L;

    public boolean storesLowerCaseQuotedIdentifiers() {

        try {

            return getDatabaseMetaData().storesLowerCaseQuotedIdentifiers();

        } catch (DataSourceException e) {

            throw e;

        } catch (SQLException e) {

            throw new DataSourceException(e);
        }
    }

    public boolean storesUpperCaseQuotedIdentifiers() {

        try {

            return getDatabaseMetaData().storesUpperCaseQuotedIdentifiers();

        } catch (DataSourceException e) {

            throw e;

        } catch (SQLException e) {

            throw new DataSourceException(e);
        }
    }

    public boolean storesMixedCaseQuotedIdentifiers() {

        try {

            return getDatabaseMetaData().storesMixedCaseQuotedIdentifiers();

        } catch (DataSourceException e) {

            throw e;

        } catch (SQLException e) {

            throw new DataSourceException(e);
        }
    }

    public boolean supportsMixedCaseQuotedIdentifiers() {
        try {

            return getDatabaseMetaData().supportsMixedCaseQuotedIdentifiers();

        } catch (DataSourceException e) {

            throw e;

        } catch (SQLException e) {

            throw new DataSourceException(e);
        }
    }

}





