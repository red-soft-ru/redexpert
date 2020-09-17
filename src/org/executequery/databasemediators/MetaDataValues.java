/*
 * MetaDataValues.java
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

package org.executequery.databasemediators;

import org.executequery.EventMediator;
import org.executequery.databaseobjects.TablePrivilege;
import org.executequery.databaseobjects.impl.DefaultDatabaseProcedure;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.DatabaseDataSource;
import org.executequery.datasource.PooledConnection;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.browser.BaseDatabaseObject;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ColumnIndex;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

import static org.executequery.databaseobjects.NamedObject.FOREIGN_KEY;
import static org.executequery.databaseobjects.NamedObject.PRIMARY_KEY;

/**
 * This class provides access to the current connection's
 * database meta data. Each method performs specific requests
 * as may be required by the calling object to display the
 * relevant data usually within a table or similar widget.
 * <p>
 * Depending on the calling class and its requirements,
 * the connection to the database may be left open thereby
 * removing the overhead associated with connection retrieval -
 * as in the case of the Database Browser which makes frequent
 * database access requests. Other objects not requiring a
 * dedicated connection simply choose not to maintain one and
 * make their requests as required.
 *
 * @author Takis Diakoumis
 * @deprecated
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MetaDataValues implements ConnectionListener {

    /**
     * The open database connection.
     */
    private Connection connection;

    /**
     * Whether to keep the connection open.
     */
    private boolean keepAlive;

    /**
     * the database connection object associated with this instance
     */
    private DatabaseConnection databaseConnection;

    /**
     * the connection 'container'
     */
    private Map<DatabaseConnection, Connection> connections;

    /**
     * <p>Constructs a new instance where the conection
     * is returned following each request.
     */
    public MetaDataValues() {
        this(false);
    }

    /**
     * <p>Constructs a new instance where the conection
     * is returned following each request only if the
     * passed boolean value is 'false'. Otherwise the
     * connection is initialised and maintained following
     * the first request and reused for any subsequent requests.
     *
     * @param keepAlive to keep the connection open
     */
    public MetaDataValues(boolean keepAlive) {
        this(null, keepAlive);
    }

    public MetaDataValues(DatabaseConnection databaseConnection, boolean keepAlive) {
        this.databaseConnection = databaseConnection;
        this.keepAlive = keepAlive;
        connections = Collections.synchronizedMap(new HashMap());
        // register for connection events
        EventMediator.registerListener(this);
    }

    /**
     * Sets the database connection object to that specified.
     */
    public void setDatabaseConnection(DatabaseConnection dc) {

        /*
        if (!connections.containsKey(dc)) {
            connections.put(dc, null);
        }
        */

        if (this.databaseConnection != dc) {
            connection = null; // null out for pending change
            this.databaseConnection = dc;
        }
        
        /*
        if (this.databaseConnection == dc) {
            Log.debug("same conn");
            return;
        }
        Log.debug("new conn");
        closeConnection();
        this.databaseConnection = dc;
         */
    }

    private void ensureConnection() throws DataSourceException {
        try {

            if (connection == null || connection.isClosed()) {

                if (Log.isDebugEnabled()) {
                    if (connection != null) {
                        Log.debug("Connection is closed.");
                    } else {
                        Log.debug("Connection is null - checking cache");
                    }
                }

                // try the cache first

                if (connections.isEmpty()) {
                    openConnectionAndAddToCache();
                }

                connection = ConnectionManager.getConnection(databaseConnection);
                if (connection == null) {

                    if (Log.isDebugEnabled()) {
                        Log.debug("ensureConnection: Connection is null in cache.");
                        Log.debug("ensureConnection: Retrieving new connection.");
                    }

                    // retrieve and add to the cache
                    openConnectionAndAddToCache();
                }

                //connection = ConnectionManager.getConnection(databaseConnection);

                // if still null - something bad has happened, or maybe closed
                if (connection == null || connection.isClosed()) {

                    throw new DataSourceException("No connection available", true);
                }

            }

        } catch (SQLException e) {
            throw new DataSourceException(e);
        }

    }

    private void openConnectionAndAddToCache() {
        connection = ConnectionManager.getConnection(databaseConnection);
        connections.put(databaseConnection, connection);
    }

    /**
     * Retrieves the current connection's hosted
     * schema names. The names are stored within a
     * <code>Vector</code> object as single String objects.
     *
     * @return the schema names within a <code>Vector</code>
     */
    public Vector<String> getHostedCatalogsVector() throws DataSourceException {

        ResultSet rs = null;

        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getCatalogs();

            Vector<String> v = new Vector<String>();
            while (rs.next()) {
                v.add(rs.getString(1));
            }

            return v;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    public String[] getExportedKeyTables(String catalog,
                                         String schema,
                                         String table)
            throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getExportedKeys(catalog, schema, table);

            List list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString(7));
            }
            return (String[]) list.toArray(new String[list.size()]);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }
    }

    public String[] getImportedKeyTables(String catalog,
                                         String schema,
                                         String table) throws DataSourceException {

        ResultSet rs = null;
        try {

            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getImportedKeys(catalog, schema, table);

            List list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString(3));
            }
            return (String[]) list.toArray(new String[list.size()]);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    public List<String> getHostedCatalogSchemas() throws DataSourceException {
        ResultSet rs = null;
        try {

            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getSchemas();

            List<String> list = new ArrayList<String>();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            return list;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /**
     * <p>Retrieves the current connection's hosted
     * schema names. The names are stored within a
     * <code>Vector</code> object as single String objects.
     *
     * @return the schema names within a <code>Vector</code>
     */
    public Vector<String> getHostedSchemasVector() throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getSchemas();

            Vector v = new Vector<String>();
            while (rs.next()) {
                v.add(rs.getString(1));
            }

            int size = v.size();
            if (size == 1) {
                String value = (String) v.elementAt(0);
                if (MiscUtils.isNull(value)) {
                    return new Vector<String>(0);
                }
            }
            /*
            else if (size == 0) {
                return getHostedCatalogsVector();
            }
            */
            return v;

        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    public String[] getTableTypes() throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getTableTypes();

            List list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            return (String[]) list.toArray(new String[list.size()]);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }
    }

    public TablePrivilege[] getPrivileges(String catalog,
                                          String schema,
                                          String table) throws DataSourceException {

        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getTablePrivileges(catalog, schema, table);

            List list = new ArrayList();
            while (rs.next()) {
                list.add(new TablePrivilege(rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7)));
            }

            return (TablePrivilege[]) list.toArray(new TablePrivilege[list.size()]);

        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /**
     * <p>Retrieves the column names for the specified
     * database table and schema as an array.
     *
     * @param table  database table name
     * @param schema database schema name
     * @return the column name
     */
    public Map<String, String> getColumnProperties(String schema,
                                                   String table,
                                                   String column) throws DataSourceException {

        ResultSet rs = null;

        try {

            ensureConnection();
            if (schema == null) {
                schema = getSchemaName().toUpperCase();
            }

            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getColumns(null, schema, table, column);

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            String[] metaColumnNames = new String[columnCount];
            for (int i = 1; i < columnCount; i++) {
                metaColumnNames[i - 1] = rsmd.getColumnName(i);
            }

            Map<String, String> map = new HashMap<String, String>();

            if (rs.next()) {

                for (int i = 1; i < columnCount; i++) {
                    map.put(metaColumnNames[i - 1],
                            rs.getString(i));
                }

            }

            return map;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /**
     * Retrieves a <code>Vector</code> of <code>ColumnIndexData</code>
     * objects containing all relevant information on the table indexes
     * for the specified table.
     *
     * @param table table's name
     * @return a <code>Vector</code> of <code>ColumnIndexData</code> objects
     */
    public Vector<ColumnIndex> getTableIndexes(String catalog,
                                               String schema, String table) throws DataSourceException {

        ResultSet rs = null;

        try {

            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();

            rs = dmd.getIndexInfo(catalog, schema, table, false, true);

            Vector v = new Vector();
            while (rs.next()) {
                String name = rs.getString(6);

                if (MiscUtils.isNull(name)) {
                    continue;
                }

                ColumnIndex cid = new ColumnIndex();
                cid.setNonUnique(rs.getBoolean(4));
                cid.setIndexName(name);
                cid.setIndexedColumn(rs.getString(9));
                v.add(cid);
            }

            return v;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /**
     * Returns the table column meta data as a result set.
     *
     * @param catalog name
     * @param schema name
     * @param name table name
     */
    public ResultSet getTableMetaData(String catalog,
                                      String schema,
                                      String name) throws DataSourceException {
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            if (!dmd.supportsCatalogsInTableDefinitions()) {
                catalog = null;
            }

            if (!dmd.supportsSchemasInTableDefinitions()) {
                schema = null;
            }

            return dmd.getColumns(catalog, schema, name, null);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            // TODO: release ????
        }
    }

    /**
     * <p>Retrieves complete and detailed meta data for all columns
     * within the specified table and schema.
     * <p>The meta data will include data type, size and all
     * primary and foreign keys for the specified table. The results
     * of this method are specifically displayed within the Database
     * Browser feature for each selected table from the browser's
     * tree structure.
     *
     * @param tableName table name
     * @param schemaName schema name
     * @return the column meta data as a <code>ColumnData</code> array
     */
    public ColumnData[] getColumnMetaData(String tableName,
                                          String schemaName) throws DataSourceException {
        return getColumnMetaData(null, schemaName, tableName);
    }

    public ColumnData[] getColumnMetaData(String catalog,
                                          String schema,
                                          String name) throws DataSourceException {

        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();

            if (!dmd.supportsCatalogsInTableDefinitions()) {
                catalog = null;
            }

            if (!dmd.supportsSchemasInTableDefinitions()) {
                schema = null;
            }

            // -----------------------------------------
            // retrieve the primary keys for this table
            // -----------------------------------------

            ResultSet keys = dmd.getPrimaryKeys(catalog, schema, name);
            List _primaryKeys = new ArrayList();

            while (keys.next()) {
                ColumnConstraint cc = new ColumnConstraint();
                cc.setRefSchema(keys.getString(2));
                cc.setTable(keys.getString(3));
                cc.setColumn(keys.getString(4));
                cc.setName(keys.getString(6));
                cc.setType(PRIMARY_KEY);
                _primaryKeys.add(cc);
            }

            releaseResources(keys);

            int v_size = _primaryKeys.size();
            ColumnConstraint[] primaryKeys = new ColumnConstraint[v_size];

            for (int i = 0; i < v_size; i++) {
                primaryKeys[i] = (ColumnConstraint) _primaryKeys.get(i);
            }

            // -----------------------------------------
            // retrieve the foreign keys of this table
            // -----------------------------------------

            keys = dmd.getImportedKeys(catalog, schema, name);

            // put the foreign key details in a temporary collection
            List _foreignKeys = new ArrayList();
            while (keys.next()) {
                ColumnConstraint cc = new ColumnConstraint();
                cc.setTable(name);
                cc.setRefSchema(keys.getString(2));
                cc.setRefTable(keys.getString(3));
                cc.setRefColumn(keys.getString(4));
                cc.setColumn(keys.getString(8));
                cc.setName(keys.getString(12));
                cc.setType(FOREIGN_KEY);
                _foreignKeys.add(cc);
            }

            v_size = _foreignKeys.size();
            ColumnConstraint[] foreignKeys = new ColumnConstraint[v_size];

            for (int i = 0; i < v_size; i++) {
                foreignKeys[i] = (ColumnConstraint) _foreignKeys.get(i);
            }

           releaseResources(keys);

            // The primary key count
            int primaryKeyCount = 0;
            // The foreign key count
            int foreignKeyCount = 0;
            // The current column name
            String columnName = null;
            // The current key's column name
            String columnNameForKey = null;
            // to store the result set
            List _columns = new ArrayList();

            //Log.debug("catalog: " + catalog + " schema: " + schema);

            // retrieve the column data
            rs = dmd.getColumns(catalog, schema, name, null);

            while (rs.next()) {

                columnName = rs.getString(4);

                ColumnData cd = new ColumnData(databaseConnection);
                cd.setCatalog(catalog);
                cd.setSchema(schema);
                cd.setColumnName(columnName);
                cd.setSQLType(rs.getShort(5));
                cd.setColumnType(rs.getString(6));
                cd.setColumnSize(rs.getInt(7));
                cd.setColumnScale(rs.getInt(9));
                cd.setColumnRequired(rs.getInt(11));
                cd.setDefaultValue(rs.getString(13));
                cd.setTableName(name);

                // check if all primary keys have been identified
                if (primaryKeyCount < primaryKeys.length) {

                    // determine if the current column is a primary key
                    for (int j = 0; j < primaryKeys.length; j++) {
                        columnNameForKey = primaryKeys[j].getColumn();

                        if (columnNameForKey.compareTo(columnName) == 0) {
                            cd.addConstraint(primaryKeys[j]);
                            cd.setPrimaryKey(true);
                            primaryKeyCount++;
                            break;
                        }

                    }

                }

                // check if all foreign keys have been identified
                if (foreignKeyCount < foreignKeys.length) {

                    // determine if the current column is a foreign key
                    for (int j = 0; j < foreignKeys.length; j++) {
                        columnNameForKey = foreignKeys[j].getColumn();

                        if (columnNameForKey.compareTo(columnName) == 0) {
                            cd.addConstraint(foreignKeys[j]);
                            cd.setForeignKey(true);
                            foreignKeyCount++;
                            break;
                        }

                    }

                }

                columnName = null;
                columnNameForKey = null;
//                cd.setNamesToUpper();
                _columns.add(cd);

            }

            releaseResources(rs);

            if(((PooledConnection) connection).getRealConnection().unwrap(Connection.class).getClass().getName().contains("FBConnection")) {
                // need to add info about column subtype
                Statement statement;
                for (Object od :
                        _columns) {
                    ColumnData cd = (ColumnData)od;
                    statement = connection.createStatement();
                    ResultSet resultSet = null;
                    try {
                        resultSet = statement.executeQuery("select \n" +
                                "f.rdb$field_sub_type as field_sub_type \n" +
                                "from rdb$relation_fields rf, \n" +
                                "rdb$fields f \n" +
                                "where rf.rdb$relation_name = '" + cd.getTableName() + "'\n" +
                                "and rf.rdb$field_name = '" + cd.getColumnName() + "'\n" +
                                "and  rf.rdb$field_source = f.rdb$field_name");
                        resultSet.next();
                        cd.setColumnSubtype(resultSet.getShort(1));
                    } finally {
                        releaseResources(resultSet);
                    }
                }
            }

            v_size = _columns.size();
            ColumnData[] columnDataArray = new ColumnData[v_size];

            for (int i = 0; i < v_size; i++) {
                columnDataArray[i] = (ColumnData) _columns.get(i);
            }

            return columnDataArray;

        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /**
     * Retrieves the database product name from
     * the connection's meta data.
     *
     * @return the database product name
     */
    public String getDatabaseProductName() throws DataSourceException {
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            return dmd.getDatabaseProductName();
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources();
        }
    }

    /**
     * Retrieves the database product version from
     * the connection's meta data.
     *
     * @return the database product version
     */
    public String getDatabaseProductVersion() throws DataSourceException {
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            return dmd.getDatabaseProductVersion();
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources();
        }

    }

    /**
     * Retrieves the database product version from
     * the connection's meta data.
     *
     * @return the database product version
     */
    public String getDatabaseProductNameVersion() throws DataSourceException {
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            return dmd.getDatabaseProductName() + " " +
                    dmd.getDatabaseProductVersion();
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources();
        }

    }

    public DefaultDatabaseProcedure[] getProcedures(String schema,
                                                    String[] names) throws DataSourceException {
        return getProcedures(null, schema, names);
    }

    public DefaultDatabaseProcedure[] getProcedures(String catalog,
                                                    String schema,
                                                    String[] names) throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();

            List<DefaultDatabaseProcedure> list = new ArrayList<DefaultDatabaseProcedure>(names.length);
            for (int i = 0; i < names.length; i++) {
                rs = dmd.getProcedureColumns(catalog, schema, names[i], null);

                DefaultDatabaseProcedure proc = new DefaultDatabaseProcedure(schema, names[i]);
                while (rs.next()) {
                    proc.addParameter(rs.getString(4),
                            rs.getInt(5),
                            rs.getInt(6),
                            rs.getString(7),
                            rs.getInt(8),
                            rs.getInt(12));
                }
                list.add(proc);
                rs.close();
            }

            return (DefaultDatabaseProcedure[])
                    list.toArray(new DefaultDatabaseProcedure[names.length]);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /**
     * Retrieves the data in its entirety from the specified table
     * using <code>SELECT * FROM table_name</code>.
     *
     * @param schema - the schema name (may be null)
     * @param table  - the table name
     * @return the table data
     */
    public ResultSet getTableData(String schema, String table)
            throws DataSourceException {
        Statement stmnt = null;
        try {
            ensureConnection();
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT * FROM ");

            if (!MiscUtils.isNull(schema)) {
                sb.append(schema);
                sb.append(".");
            }
            sb.append(table);

            stmnt = connection.createStatement();
            return stmnt.executeQuery(sb.toString());
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    public boolean hasStoredObjects(String schema, String[] types)
            throws DataSourceException {
        return hasStoredObjects(null, schema, types);
    }

    public boolean hasStoredObjects(String catalog, String schema, String[] types)
            throws DataSourceException {

        if (schema == null) {
            schema = getSchemaName();
        }

        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getTables(catalog, schema, null, types);
            return rs.next();
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }
    }

    public DefaultDatabaseProcedure[] getStoredObjects(String schema, String[] types)
            throws DataSourceException {
        return getStoredObjects(null, schema, types);
    }

    public DefaultDatabaseProcedure[] getStoredObjects(
            String catalog, String schema, String[] types) throws DataSourceException {

        ResultSet rs = null;

        if (schema == null) {
            schema = getSchemaName();
        }

        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getTables(catalog, schema, null, types);

            ArrayList list = new ArrayList();

            while (rs.next()) {
                list.add(rs.getString(3));
            }

            releaseResources(rs);

            String[] procedures = (String[]) list.toArray(new String[list.size()]);
            list.clear();

            for (int i = 0; i < procedures.length; i++) {

                rs = dmd.getProcedures(null, schema, procedures[i]);

                while (rs.next()) {
                    String name = rs.getString(3);
                    DefaultDatabaseProcedure dbproc = new DefaultDatabaseProcedure(
                            rs.getString(2),
                            name);

                    ResultSet _rs = dmd.getProcedureColumns(null, schema, name, null);
                    while (_rs.next()) {
                        dbproc.addParameter(_rs.getString(4),
                                _rs.getInt(5),
                                _rs.getInt(6),
                                _rs.getString(7),
                                _rs.getInt(8),
                                rs.getInt(12));
                    }

                    _rs.close();
                    list.add(dbproc);
                }
                releaseResources(rs);

            }

            DefaultDatabaseProcedure[] procs =
                    (DefaultDatabaseProcedure[]) list.toArray(new DefaultDatabaseProcedure[list.size()]);
            return procs;

        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /**
     * Recycles the specified connection object.
     *
     * @param dc - the connection to be recycled
     */
    public void recycleConnection(DatabaseConnection dc)
            throws DataSourceException {
        if (connections.containsKey(dc)) {
            //Log.debug("Recycling connection");
            // close the connection held in the local cache
            // another will be retrieved on the next call to it
            Connection c = connections.get(dc);
            ConnectionManager.close(dc, c);
            connections.put(dc, null);
        }
    }

    /**
     * Closes the open connection and releases
     * all resources attached to it.
     */
    public void closeConnection() {
        try {
            for (Iterator i = connections.keySet().iterator(); i.hasNext(); ) {
                DatabaseConnection dc = (DatabaseConnection) i.next();
                connection = connections.get(dc);
                if (connection != null) {
                    connection.close();
                }
                connection = null;
            }
            connections.clear();
        } catch (SQLException sqlExc) {
            sqlExc.printStackTrace();
        }
    }

    /**
     * <p>Retrieves key/value type pairs using the
     * <code>Reflection</code> API to call and retrieve
     * values from the connection's meta data object's methods
     * and variables.
     * <p>The values are returned within a 2-dimensional
     * array of key/value pairs.
     *
     * @return the database properties as key/value pairs
     */
    public Hashtable getDatabaseProperties() throws DataSourceException {
        try {

            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();

            Class metaClass = dmd.getClass();
            Method[] metaMethods = metaClass.getMethods();

            Object[] p = new Object[]{};

            Hashtable h = new Hashtable();
            String STRING = "String";
            String GET = "get";

            for (int i = 0; i < metaMethods.length; i++) {
                try {
                    Class c = metaMethods[i].getReturnType();
                    String s = metaMethods[i].getName();

                    if (s == null || c == null) {
                        continue;
                    }

                    if (c.isPrimitive() || c.getName().endsWith(STRING)) {

                        if (s.startsWith(GET)) {
                            s = s.substring(3);
                        }

                        try {
                            Object res = metaMethods[i].invoke(dmd, p);
                            h.put(s, res.toString());
                        } catch (AbstractMethodError abe) {
                            continue;
                        }

                    }
                } catch (Exception e) {
                    continue;
                }
            }
/*            
            int count = 0;
            // prepare for key sort
            String[] keys = new String[h.size()];
            for (Enumeration i = h.keys(); i.hasMoreElements();) {
                keys[count++] = (String)i.nextElement();
            }
            
            Arrays.sort(keys);
            String[][] dbData = new String[keys.length][2];
            for (int i = 0; i < keys.length; i++) {
                dbData[i][0] = keys[i];
                dbData[i][1] = (String)h.get(keys[i]);
            }
*/
            return h;

        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources();
        }

    }

    /**
     * <p>Retrieves the connected databases SQL keyword
     * list via a call to the <code>DatabaseMetaData</code>
     * object's <code>getSQLKeywords()</code> method.
     * <p>The retrieved keywords are stored within a
     * 2-dimensional array for display with the relevant
     * header within a table.
     *
     * @return the schema names array
     */
    public String[] getDatabaseKeywords() throws DataSourceException {
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();

            String sql = dmd.getSQLKeywords();
            releaseResources();

            StringTokenizer st = new StringTokenizer(sql, ",");
            List<String> values = new ArrayList<String>();

            while (st.hasMoreTokens()) {
                values.add(st.nextToken());
            }

            int size = values.size();
            String[] words = new String[size];
            for (int i = 0; i < size; i++) {
                words[i] = values.get(i);
            }
            return words;

        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    @SuppressWarnings("unused")
    private void releaseResources(Statement stmnt) {
        try {
            if (stmnt != null) {
                if (!stmnt.isClosed())
                    stmnt.close();
            }
        } catch (SQLException sqlExc) {
        } finally {
            releaseResources();
        }
    }

    @SuppressWarnings("unused")
    private void releaseResources(Statement stmnt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmnt != null) {
                stmnt.close();
            }
        } catch (SQLException sqlExc) {
        } finally {
            releaseResources();
        }
    }

    private void releaseResources(ResultSet rs) {
        try {
            if (rs != null) {
                Statement st = rs.getStatement();
                if (rs != null) {
                    if (!rs.isClosed())
                        rs.close();
                }
                releaseResources(st);
            }
        } catch (SQLException sqlExc) {
        } finally {
            releaseResources();
        }
    }

    /**
     * <p>Releases this object's connection resources
     */
    private void releaseResources() {
        if (keepAlive) {
            return;
        }
        closeConnection();
    }

    /**
     * <p>Retrieves the database SQL data types as a
     * <code>ResultSet</code> object.
     * <p>This will be typically used to display the
     * complete data types meta data retrieved from the JDBC driver.
     *
     * @return the SQL data types
     */
    public ResultSet getDataTypesResultSet() throws DataSourceException {
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            return dmd.getTypeInfo();
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * <p>Retrieves the database SQL data type names only.
     *
     * @return the SQL data type names within an array
     */
    public String[] getDataTypesArray() throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getTypeInfo();

            String underscore = "_";
            List<String> _dataTypes = new ArrayList<String>();
            while (rs.next()) {
                String type = rs.getString(1);
                if (!type.startsWith(underscore)) {
                    _dataTypes.add(type);
                }
            }

            int size = _dataTypes.size();
            String[] dataTypes = new String[size];
            for (int i = 0; i < size; i++) {
                dataTypes[i] = _dataTypes.get(i);
            }

            //Arrays.sort(dataTypes);
            return dataTypes;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    public Object[][] getObjectDataTypesArray() throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getTypeInfo();

            String underscore = "_";
            List<String> _dataTypes = new ArrayList<String>();
            List<Integer> _dataIntTypes = new ArrayList<>();
            while (rs.next()) {
                String type = rs.getString(1);
                Integer typeInt = rs.getInt(2);
                if (!type.startsWith(underscore)) {
                    _dataTypes.add(type);
                    _dataIntTypes.add(typeInt);
                }
            }

            int size = _dataTypes.size();
            Object[][] dataTypes = new Object[size][];
            for (int i = 0; i < size; i++) {
                dataTypes[i] = new Object[2];
                dataTypes[i][0] = _dataTypes.get(i);
                dataTypes[i][1] = _dataIntTypes.get(i);
            }

            Arrays.sort(dataTypes);
            return dataTypes;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    public int[] getIntDataTypesArray() throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getTypeInfo();

            String underscore = "_";
            List<Integer> _dataTypes = new ArrayList<Integer>();
            while (rs.next()) {
                String stype = rs.getString(1);
                int type = rs.getInt(2);
                if (!stype.startsWith(underscore)) {
                    _dataTypes.add(type);
                }
            }

            int size = _dataTypes.size();
            int[] dataTypes = new int[size];
            for (int i = 0; i < size; i++) {
                dataTypes[i] = _dataTypes.get(i);
            }
            return dataTypes;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /**
     * <p>Retrieves the currently connected schema's
     * database table names within a <code>Vector</code>.
     *
     * @return the table names
     */
    public Vector getDatabaseTablesVector() throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            String[] type = {"TABLE"};
            rs = dmd.getTables(null,
                    getSchemaName(),
                    null,
                    type);

            Vector v = new Vector();
            while (rs.next()) {
                v.add(rs.getString(3));
            }

            return v;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }
    }

    /**
     * <p>Retrieves the column names for the specified
     * database table and schema as an array.
     *
     * @param table  database table name
     * @param schema database schema name
     * @return the column names array
     */
    public String[] getColumnNames(String table, String schema)
            throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            if (schema == null) {
                schema = getSchemaName().toUpperCase();
            }

            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getColumns(null, schema, table, null);

            Vector<String> v = new Vector<String>();
            while (rs.next()) {
                v.add(rs.getString(4));
            }

            int v_size = v.size();
            String[] columns = new String[v_size];
            for (int i = 0; i < v_size; i++) {
                columns[i] = v.get(i);
            }

            return columns;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /**
     * <p>Retrieves the column names for the specified
     * database table and schema as a <code>Vector</code>
     * object.
     *
     * @param table  database table name
     * @param schema database schema name
     * @return the column names <code>Vector</code>
     */
    public Vector<String> getColumnNamesVector(String table, String schema)
            throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            if (schema == null) {
                schema = getSchemaName().toUpperCase();
            }

            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getColumns(null, schema, table, null);

            Vector<String> v = new Vector<String>();
            while (rs.next()) {
//                v.add(rs.getString(4).toUpperCase());
                v.add(rs.getString(4));
            }
            return v;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }
    }

    /**
     * <p>Retrieves the specified schema's
     * database table names within a <code>Vector</code>.
     *
     * @return the table names
     */
    public Vector<String> getSchemaTables(String schema) throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            String _schema = null;
            boolean valueFound = false;

            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getSchemas();

            while (rs.next()) {
                _schema = rs.getString(1);

                if (_schema.equalsIgnoreCase(schema)) {
                    valueFound = true;
                    break;
                }

            }

            releaseResources(rs);

            if (!valueFound) {
                _schema = schema;
            }

            String[] type = {"TABLE"};
            rs = dmd.getTables(null, _schema, null, type);

            Vector<String> v = new Vector<String>();

            while (rs.next()) {
                v.add(rs.getString(3));
            }
            return v;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    public Vector<ColumnData> getColumnMetaDataVector(
            String name, String schema, String catalog)
            throws DataSourceException {

        ResultSet rs = null;

        try {

            ensureConnection();
            if (schema == null) {
                schema = getSchemaName().toUpperCase();
            }

            DatabaseMetaData dmd = connection.getMetaData();

            if (!dmd.supportsCatalogsInTableDefinitions()) {
                catalog = null;
            }

            if (!dmd.supportsSchemasInTableDefinitions()) {
                schema = null;
            }

            rs = dmd.getColumns(catalog, schema, name, null);

            Vector v = new Vector();
            while (rs.next()) {
                ColumnData cd = new ColumnData(databaseConnection);
                cd.setColumnName(rs.getString(4));
                cd.setSQLType(rs.getInt(5));
                cd.setColumnType(rs.getString(6));
                cd.setColumnSize(rs.getInt(7));
                cd.setColumnRequired(rs.getInt(11));
                cd.setTableName(name);
                v.add(cd);
            }

            return v;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /**
     * <p>Retrieves the complete column meta data
     * for the specified database table and schema.
     * <p>Each column and associated data is stored within
     * <code>ColumnData</code> objects and added to the
     * <code>Vector</code> object to be returned.
     *
     * @param name   database table name
     * @param schema database schema name
     * @return the table column meta data
     */
    public Vector<ColumnData> getColumnMetaDataVector(String name, String schema)
            throws DataSourceException {
        return getColumnMetaDataVector(name, schema, null);
    }

    public BaseDatabaseObject[] getTables(String catalog,
                                          String schema,
                                          String[] types)
            throws DataSourceException {

        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getTables(catalog, schema, null, types);

            if (rs == null) {
                return new BaseDatabaseObject[0];
            }

            ArrayList list = new ArrayList();
            while (rs.next()) {
                BaseDatabaseObject object = new BaseDatabaseObject();
                object.setCatalogName(rs.getString(1));
                object.setSchemaName(rs.getString(2));
                object.setName(rs.getString(3));
                object.setMetaDataKey(rs.getString(4));
                object.setRemarks(rs.getString(5));
                list.add(object);
            }

            return (BaseDatabaseObject[]) list.toArray(
                    new BaseDatabaseObject[list.size()]);

        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }
    }

    public static final int STRING_FUNCTIONS = 0;

    public static final int TIME_DATE_FUNCTIONS = 1;

    public static final int NUMERIC_FUNCTIONS = 2;

    public String[] getSystemFunctions(int type) throws DataSourceException {
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            String functions = null;

            switch (type) {

                case STRING_FUNCTIONS:
                    functions = dmd.getStringFunctions();
                    break;

                case TIME_DATE_FUNCTIONS:
                    functions = dmd.getTimeDateFunctions();
                    break;

                case NUMERIC_FUNCTIONS:
                    functions = dmd.getNumericFunctions();
                    break;

            }

            if (!MiscUtils.isNull(functions)) {
                StringTokenizer st = new StringTokenizer(functions, ",");

                List<String> list = new ArrayList<String>(st.countTokens());
                while (st.hasMoreTokens()) {
                    list.add(st.nextToken());
                }

                return (String[]) list.toArray(new String[list.size()]);
            }

            return new String[0];
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }

    }

    /**
     * Returns the procedure term used in the current connected
     * database.
     *
     * @return the procedure term
     */
    public String getProcedureTerm() throws DataSourceException {
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            return dmd.getProcedureTerm();
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    public String[] getProcedureNames(String catalog, String schema, String name)
            throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getProcedures(catalog, schema, name);

            List list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString(3));
            }

            return (String[]) list.toArray(new String[list.size()]);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }
    }

    public String[] getTables(String catalog, String schema, String metaType)
            throws DataSourceException {

        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();

            if (!dmd.supportsCatalogsInTableDefinitions()) {
                catalog = null;
            }

            if (!dmd.supportsSchemasInTableDefinitions()) {
                schema = null;
            }

            rs = dmd.getTables(catalog, schema, null, new String[]{metaType});

            if (rs != null) { // some odd null rs behaviour on some drivers
                ArrayList list = new ArrayList();
                while (rs.next()) {
                    list.add(rs.getString(3));
                }
                return (String[]) list.toArray(new String[list.size()]);
            } else {
                return new String[0];
            }

        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

    }

    /** <p>Executes the specified query (SELECT) and returns
     *  a <code>ResultSet</code> object from this query.
     *  <p>This is employed primarily by the Database Browser
     *  to populate the 'Data' tab.
     *
     *  @param  the SQL query to execute
     *  @return the query result
     */
    /*
    public ResultSet getResultSet(String query) throws Exception {
        Statement stmnt = null;
        
        try {
            ensureConnection();
            stmnt = connection.createStatement();
            return stmnt.executeQuery(query);            
        } 
        catch (SQLException e) {
            return null;
        } 
        catch (OutOfMemoryError e) {
            return null;
        } 
        
        finally {
            try {
                if (stmnt != null) {
                    stmnt.close();
                }
            }
            catch (SQLException sqlExc) {}
            releaseResources();
        } 
        
    }*/

    // ----------------------------------------------------------
    // convenience methods for simple values from the connection
    // ----------------------------------------------------------

    /**
     * <p>Retrieves the connected data source name.
     *
     * @return the data source name
     */
    public String getDataSourceName() {
        String name = databaseConnection.getSourceName();
        return name == null ? "Not Available" : name.toUpperCase();
    }

    /**
     * <p>Retrieves the connected port number.
     *
     * @return the port number
     */
    public int getPort() {
        return databaseConnection.getPortInt();
    }

    /**
     * <p>Retrieves the connected user.
     *
     * @return the user name
     */
    public String getUser() {
        return databaseConnection.getUserName();
    }

    /**
     * <p>Retrieves the connected JDBC URL.
     *
     * @return the JDBC URL
     */
    public String getURL() {
        return getDataSource().getJdbcUrl();
    }

    /**
     * <p>Retrieves the connected host name.
     *
     * @return the host name
     */
    public String getHost() {
        String host = databaseConnection.getHost();
        return host == null ? "Not Available" : host.toUpperCase();
    }

    /**
     * Retrieves the connected schema name.
     *
     * @return the schema name
     */
    public String getSchemaName() {
        String schema = databaseConnection.getUserName();
        return schema == null ? "Not Available" : schema.toUpperCase();
    }

    public String getCatalogName() {
        String catalog = null;
        try {
            ensureConnection();
            catalog = connection.getCatalog();
            if (MiscUtils.isNull(catalog)) {
                catalog = getSchemaName();
            }
        } catch (SQLException e) {
        } catch (DataSourceException e) {
        } finally {
            releaseResources();
        }
        return catalog == null ? "Not Available" : catalog.toUpperCase();
    }

    private DatabaseDataSource getDataSource() {
        return (DatabaseDataSource) ConnectionManager.getDataSource(databaseConnection);
    }

    /**
     * Indicates a connection has been established.
     *
     * @param connectionEvent encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {
    }

    /**
     * Indicates a connection has been closed.
     *
     * @param connectionEvent encapsulating event
     */
    public void disconnected(ConnectionEvent connectionEvent) {

        DatabaseConnection dc = connectionEvent.getDatabaseConnection();

        if (connections.containsKey(dc)) {

            connections.remove(dc);

            // null out the connection if its the one disconnected
            if (databaseConnection == dc) {

                connection = null;
            }

        }
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent);
    }

}











