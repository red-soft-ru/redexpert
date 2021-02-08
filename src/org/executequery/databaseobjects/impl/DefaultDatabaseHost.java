/*
 * DefaultDatabaseHost.java
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

import biz.redsoft.IFBDatabaseConnection;
import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.*;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.*;

/**
 * Default database host object implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultDatabaseHost extends AbstractNamedObject
        implements DatabaseHost {

    private int countFinishedMetaTags;

    private int typeTree;

    /**
     * the database connection wrapper for this host
     */
    private transient DatabaseConnection databaseConnection;

    /**
     * the SQL connection for this host
     */
    private transient Connection connection;

    /**
     * the database meta data object for this host
     */
    private transient DatabaseMetaData databaseMetaData;

    /**
     * the catalogs of this host
     */
    private List<DatabaseCatalog> catalogs;

    /**
     * the schemas of this host
     */
    private List<DatabaseSchema> schemas;
    private DatabaseObject dependObject;

    /**
     * Creates a new instance of DefaultDatabaseHost with the
     * specifiec database connection wrapper.
     *
     * @param databaseConnection the connection wrapper
     */

    public DefaultDatabaseHost(DatabaseConnection databaseConnection) {
        this(databaseConnection, TreePanel.DEFAULT);
    }

    public DefaultDatabaseHost(DatabaseConnection databaseConnection, int typeTree) {
        this.databaseConnection = databaseConnection;
        countFinishedMetaTags = 0;
        this.typeTree = typeTree;
    }

    public DefaultDatabaseHost(DatabaseConnection databaseConnection, int typeTree, DatabaseObject dependObject) {
        this.databaseConnection = databaseConnection;
        this.typeTree = typeTree;
        this.dependObject = dependObject;
    }

    /**
     * Attempts to establish a connection using this host.
     */
    public boolean connect() throws DataSourceException {

        if (!isConnected()) {
            countFinishedMetaTags = 0;

            boolean connected = connectionMediator().connect(getDatabaseConnection());
            try {
                getDatabaseConnection().setServerVersion(connection.getMetaData().getDatabaseMajorVersion());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return connected;
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
            //ConnectionManager.close(getDatabaseConnection(), connection);
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
        this.disconnect();
        this.connect();
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

        return ConnectionManager.getTemporaryConnection(getDatabaseConnection());
    }

    @Override
    public int countFinishedMetaTags() {
        return countFinishedMetaTags;
    }

    @Override
    public void incCountFinishedMetaTags() {
        countFinishedMetaTags++;
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
        } catch (SQLException e) {

            throw new DataSourceException(e);
        } finally {

            releaseResources(rs, null);
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

            releaseResources(rs, null);
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
            while (rs.next()) {

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

            releaseResources(rs, null);
        }

    }

    /**
     * Returns the tables hosted by this host of the specified type and
     * belonging to the specified catalog and schema.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param type    the table type
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

            releaseResources(rs, null);
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
     * @param schema  the table schema name
     * @param table   the database object name
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

                    DatabaseTable databaseTable = (DatabaseTable) metaTag.getNamedObject(fkTable);
                    columns.add(databaseTable.getColumn(fkColumn));
                }

            }

            return columns;
        } catch (SQLException e) {

            throw new DataSourceException(e);
        } finally {

            releaseResources(rs, null);
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
     * @param schema  the table schema name
     * @param type    the table type
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

            releaseResources(rs, null);
        }

    }

    /**
     * Returns the column names of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param table   the database object name
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

            releaseResources(rs, null);
        }

    }

    private transient ColumnInformationFactory columnInformationFactory = new ColumnInformationFactory();

    /**
     * Returns the column names of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param table   the database object name
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

            releaseResources(rs, null);
        }

    }

    /**
     * Returns the columns of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param table   the database object name
     * @return the columns
     */
    public synchronized List<DatabaseColumn> getColumns(String catalog, String schema, String table)
            throws DataSourceException {

        ResultSet rs = null;

        List<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();

        try {
            String _catalog = getCatalogNameForQueries(catalog);
            String _schema = getSchemaNameForQueries(schema);
            DatabaseMetaData dmd = getDatabaseMetaData();

            boolean isFirebirdConnection = false;
            Connection connection = dmd.getConnection();
            if (connection.unwrap(Connection.class).getClass().getName().contains("FBConnection"))
                isFirebirdConnection = true;

            // retrieve the base column info

            Statement statement = null;

            if (isFirebirdConnection) {
                String identity = null;
                if (getDatabaseMetaData().getDatabaseMajorVersion() >= 3) {
                    identity = "    RF.RDB$IDENTITY_TYPE AS IDENTITY\n";
                } else {
                    identity = "    CAST(NULL AS INTEGER) AS IDENTITY\n";
                }
                String firebirdSql = "SELECT\n" +
                        "    '' AS CATALOG,\n" +
                        "    '' AS SCHEME,\n" +
                        "    cast(RF.RDB$RELATION_NAME as varchar(63)) AS RELATION_NAME,\n" +
                        "    cast(RF.RDB$FIELD_NAME as varchar(63)) AS FIELD_NAME,\n" +
                        "    F.RDB$FIELD_TYPE AS FIELD_TYPE,\n" +
                        "    F.RDB$FIELD_SUB_TYPE AS FIELD_SUB_TYPE,\n" +
                        "    F.RDB$FIELD_PRECISION AS FIELD_PRECISION,\n" +
                        "    F.RDB$FIELD_SCALE AS FIELD_SCALE,\n" +
                        "    F.RDB$FIELD_LENGTH AS FIELD_LENGTH,\n" +
                        "    F.RDB$CHARACTER_LENGTH AS CHAR_LEN,\n" +
                        "    RF.RDB$DESCRIPTION AS REMARKS,\n" +
                        "    RF.RDB$DEFAULT_SOURCE AS DEFAULT_SOURCE,\n" +
                        "    F.RDB$DEFAULT_SOURCE AS DOMAIN_DEFAULT_SOURCE,\n" +
                        "    RF.RDB$FIELD_POSITION + 1 AS FIELD_POSITION,\n" +
                        "    RF.RDB$NULL_FLAG AS NULL_FLAG,\n" +
                        "    F.RDB$NULL_FLAG AS SOURCE_NULL_FLAG,\n" +
                        "    F.RDB$COMPUTED_BLR AS COMPUTED_BLR,\n" +
                        "    F.RDB$CHARACTER_SET_ID,\n" +
                        identity +
                        "FROM\n" +
                        "    RDB$RELATION_FIELDS RF,\n" +
                        "    RDB$FIELDS F\n" +
                        "WHERE\n" +
                        "    RF.RDB$RELATION_NAME = " +
                        "'" +
                        table +
                        "'" +
                        "and\n" +
                        "    RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME\n" +
                        "order by\n" +
                        "    RF.RDB$RELATION_NAME, RF.RDB$FIELD_POSITION";

                statement = connection.createStatement();
                rs = statement.executeQuery(firebirdSql);
            } else {
                rs = dmd.getColumns(_catalog, _schema, table, null);
            }

            if (isFirebirdConnection) {
                columns = createColumns(rs, table);
            } else {

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
            }
            releaseResources(rs, connection);

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
                            ((DefaultDatabaseColumn) column).setPrimaryKey(true);
                            break;
                        }

                    }

                }
                releaseResources(rs, connection);

                // check for foreign keys
                rs = dmd.getImportedKeys(_catalog, _schema, table);
                while (rs.next()) {
                    String fkColumn = rs.getString(8);

                    // find the fk column in the previous list
                    for (int i = 0; i < columnCount; i++) {
                        DatabaseColumn column = columns.get(i);
                        String columnName = column.getName();
                        if (columnName.equalsIgnoreCase(fkColumn)) {
                            ((DefaultDatabaseColumn) column).setForeignKey(true);
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

            releaseResources(rs, connection);
        }

    }

    private List<DatabaseColumn> createColumns (ResultSet rs, String table) throws SQLException {
        List<DatabaseColumn> columns = new ArrayList<>();

        while (rs.next()) {
            DefaultDatabaseColumn column = new DefaultDatabaseColumn();
            final short fieldType = rs.getShort("FIELD_TYPE");
            final short fieldSubType = rs.getShort("FIELD_SUB_TYPE");
            final short fieldScale = rs.getShort("FIELD_SCALE");
            final int characterSetId = rs.getInt("RDB$CHARACTER_SET_ID");
            final int dataType = getDataType(fieldType, fieldSubType, fieldScale, characterSetId);

            column.setTypeInt(dataType);
            column.setColumnSubtype(fieldSubType);
            column.setColumnScale(fieldScale);
            column.setName(rs.getString("FIELD_NAME").trim());
            column.setTypeName(DatabaseTypeConverter.getDataTypeName(fieldType, fieldSubType, fieldScale));

            switch (dataType) {
                case Types.DECIMAL:
                case Types.NUMERIC:
                    // TODO column precision
                    column.setColumnScale(fieldScale * (-1));
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.BINARY:
                case Types.VARBINARY:
                    //valueBuilder.at(15).set(createInt(rs.getShort("FIELD_LENGTH")));
                    column.setColumnSize(rs.getShort("FIELD_LENGTH"));
                    short charLen = rs.getShort("CHAR_LEN");
                    break;
                case Types.FLOAT:
                    // TODO column precision
//                    valueBuilder.at(6).set(FLOAT_PRECISION);
                    break;
                case Types.DOUBLE:
                    // TODO column precision
//                    valueBuilder.at(6).set(DOUBLE_PRECISION);
                    break;
                case Types.BIGINT:
                    // TODO column precision
//                    valueBuilder
//                            .at(6).set(BIGINT_PRECISION)
//                            .at(8).set(INT_ZERO);
                    break;
                case Types.INTEGER:
                    // TODO column precision
//                    valueBuilder
//                            .at(6).set(INTEGER_PRECISION)
//                            .at(8).set(INT_ZERO);
                    break;
                case Types.SMALLINT:
                    // TODO column precision
//                    valueBuilder
//                            .at(6).set(SMALLINT_PRECISION)
//                            .at(8).set(INT_ZERO);
                    break;
                case Types.DATE:
                    // TODO column precision
//                    valueBuilder.at(6).set(DATE_PRECISION);
                    break;
                case Types.TIME:
                    // TODO column precision
//                    valueBuilder.at(6).set(TIME_PRECISION);
                    break;
                case Types.TIMESTAMP:
                    // TODO column precision
//                    valueBuilder.at(6).set(TIMESTAMP_PRECISION);
                    break;
                case Types.BOOLEAN:
                    // TODO column precision
//                    valueBuilder
//                            .at(6).set(BOOLEAN_PRECISION)
//                            .at(9).set(RADIX_BINARY);
                    break;
            }

            final short nullFlag = rs.getShort("NULL_FLAG");
            final short sourceNullFlag = rs.getShort("SOURCE_NULL_FLAG");
            column.setRemarks(rs.getString("REMARKS"));
            column.setRequired(nullFlag == 1 || sourceNullFlag == 1);

            String column_def = rs.getString("DEFAULT_SOURCE");
            if (column_def == null) {
                column_def = rs.getString("DOMAIN_DEFAULT_SOURCE");
            }
            if (column_def != null) {
                // TODO This looks suspicious (what if it contains default)
                int defaultPos = column_def.toUpperCase().trim().indexOf("DEFAULT");
                if (defaultPos == 0)
                    column_def = column_def.substring(7).trim();
                column.setDefaultValue(column_def);
            }

            column.setIdentity(rs.getInt("IDENTITY") == 1);

            columns.add(column);
        }

        releaseResources(rs, connection);

        Statement statement = null;

        for (Iterator it = columns.iterator(); it.hasNext();) {
            DefaultDatabaseColumn column = (DefaultDatabaseColumn)it.next();
            String computedSource = null;

            statement = connection.createStatement();
            try {
                ResultSet sourceRS = statement.executeQuery("select " +
                        " RRF.RDB$FIELD_SOURCE" +
                        " from RDB$FIELDS RF, " +
                        "rdb$relation_fields RRF\n" +
                        "where\n" +
                        "    RRF.rdb$field_name = '" + column.getName() + "'\n" +
                        "    and\n" +
                        "    RRF.rdb$relation_name = '" + table + "'\n" +
                        "    and\n" +
                        "    RF.rdb$field_name = RRF.rdb$field_source");
                if (sourceRS.next()) {
                    computedSource = sourceRS.getString(1);
                }

                releaseResources(sourceRS, connection);

                if (computedSource != null && !computedSource.isEmpty()) {
                    column.setDomain(computedSource);
                }

                computedSource = null;

                // TODO check for RDB 3.0
//                if (isGen.compareToIgnoreCase("YES") == 0) {
//                    column.setGenerated(true);
//                        Statement statement = dmd.getConnection().createStatement();
                    /*ResultSet*/
                    statement = connection.createStatement();
                    sourceRS = statement.executeQuery("select RF.RDB$COMPUTED_SOURCE, " +
                            " RRF.RDB$FIELD_NAME" +
                            " from RDB$FIELDS RF, " +
                            "rdb$relation_fields RRF\n" +
                            "where\n" +
                            "    RRF.rdb$field_name = '" + column.getName() + "'\n" +
                            "    and\n" +
                            "    RRF.rdb$relation_name = '" + table + "'\n" +
                            "    and\n" +
                            "    RF.rdb$field_name = RRF.rdb$field_source");
                    if (sourceRS.next()) {
                        computedSource = sourceRS.getString(1);
                    }
                releaseResources(sourceRS, connection);
                    if (computedSource != null && !computedSource.isEmpty()) {
//                            column.setTypeName(computedSource);
                        column.setGenerated(true);
                        column.setComputedSource(computedSource);
                    }
//                }
            } finally {
                if (!statement.isClosed())
                    statement.close();
            }

            // if column is blob, get segment size
            if (column.getTypeInt() == Types.LONGVARBINARY ||
                    column.getTypeInt() == Types.LONGVARCHAR ||
                    column.getTypeInt() == Types.BLOB) {
                Statement st = connection.createStatement();
                try {
                    ResultSet sourceRS = st.executeQuery("select\n" +
                            "f.rdb$field_sub_type as field_subtype,\n" +
                            "f.rdb$segment_length as segment_length\n" +
                            "from rdb$relation_fields rf,\n" +
                            "rdb$fields f\n" +
                            "where rf.rdb$relation_name = '" + table + "'\n" +
                            "and rf.rdb$field_name = '" + column.getName() + "'\n" +
                            "and rf.rdb$field_source = f.rdb$field_name");
                    if (sourceRS.next()) {
                        column.setColumnSubtype(sourceRS.getInt(1));
                        column.setColumnSize(sourceRS.getInt(2));
                        releaseResources(sourceRS, connection);
                    }

                } finally {
                    releaseResources(st);
                }
            }

        }

        return columns;
    }

    private static final int smallint_type = 7;
    private static final int integer_type = 8;
    private static final int quad_type = 9;
    private static final int float_type = 10;
    private static final int d_float_type = 11;
    private static final int date_type = 12;
    private static final int time_type = 13;
    private static final int char_type = 14;
    private static final int int64_type = 16;
    private static final int double_type = 27;
    private static final int timestamp_type = 35;
    private static final int varchar_type = 37;
    //  private static final int cstring_type = 40;
    private static final int blob_type = 261;
    private static final short boolean_type = 23;

    static final int SUBTYPE_NUMERIC = 1;
    static final int SUBTYPE_DECIMAL = 2;

    final static int SQL_TEXT      = 452;
    final static int SQL_VARYING   = 448;
    final static int SQL_SHORT     = 500;
    final static int SQL_LONG      = 496;
    final static int SQL_FLOAT     = 482;
    final static int SQL_DOUBLE    = 480;
    final static int SQL_D_FLOAT   = 530;
    final static int SQL_TIMESTAMP = 510;
    final static int SQL_BLOB      = 520;
    final static int SQL_ARRAY     = 540;
    final static int SQL_QUAD      = 550;
    final static int SQL_TYPE_TIME = 560;
    final static int SQL_TYPE_DATE = 570;
    final static int SQL_INT64     = 580;
    final static int SQL_BOOLEAN   = 32764;
    final static int SQL_NULL      = 32766;

    final static int CS_NONE    = 0; /* No Character Set */
    final static int CS_BINARY  = 1; /* BINARY BYTES */
    final static int CS_dynamic = 127; // Pseudo number for runtime charset (see intl\charsets.h and references to it in Firebird)

    public static int getDataType(int fieldType, int fieldSubType, int fieldScale, int characterSetId) {

        // TODO Preserved for backwards compatibility, is this really necessary?
        if (fieldType == blob_type && fieldSubType > 1) {
            return Types.OTHER;
        }
        final int jdbcType = fromMetaDataToJdbcType(fieldType, fieldSubType, fieldScale);
        // Metadata from RDB$ tables does not contain character set in subtype, manual fixup
        if (characterSetId == CS_BINARY) {
            if (jdbcType == Types.CHAR) {
                return Types.BINARY;
            } else if (jdbcType == Types.VARCHAR) {
                return Types.VARBINARY;
            }
        }
        return jdbcType;
    }

    public static int fromMetaDataToJdbcType(int metaDataType, int subtype, int scale) {
        return fromFirebirdToJdbcType(fromMetaDataToFirebirdType(metaDataType), subtype, scale);
    }

    public static int fromFirebirdToJdbcType(int firebirdType, int subtype, int scale) {
        firebirdType = firebirdType & ~1;

        switch (firebirdType) {
            case SQL_SHORT:
                if (subtype == SUBTYPE_NUMERIC || (subtype == 0 && scale < 0))
                    return Types.NUMERIC;
                else if (subtype == SUBTYPE_DECIMAL)
                    return Types.DECIMAL;
                else
                    return Types.SMALLINT;
            case SQL_LONG:
                if (subtype == SUBTYPE_NUMERIC || (subtype == 0 && scale < 0))
                    return Types.NUMERIC;
                else if (subtype == SUBTYPE_DECIMAL)
                    return Types.DECIMAL;
                else
                    return Types.INTEGER;
            case SQL_INT64:
                if (subtype == SUBTYPE_NUMERIC || (subtype == 0 && scale < 0))
                    return Types.NUMERIC;
                else if (subtype == SUBTYPE_DECIMAL)
                    return Types.DECIMAL;
                else
                    return Types.BIGINT;
            case SQL_DOUBLE:
            case SQL_D_FLOAT:
                if (subtype == SUBTYPE_NUMERIC || (subtype == 0 && scale < 0))
                    return Types.NUMERIC;
                else if (subtype == SUBTYPE_DECIMAL)
                    return Types.DECIMAL;
                else
                    return Types.DOUBLE;
            case SQL_FLOAT:
                return Types.FLOAT;
            case SQL_TEXT:
                if (subtype == CS_BINARY){
                    return Types.BINARY;
                } else {
                    return Types.CHAR;
                }
            case SQL_VARYING:
                if (subtype == CS_BINARY){
                    return Types.VARBINARY;
                } else {
                    return Types.VARCHAR;
                }
            case SQL_TIMESTAMP:
                return Types.TIMESTAMP;
            case SQL_TYPE_TIME:
                return Types.TIME;
            case SQL_TYPE_DATE:
                return Types.DATE;
            case SQL_BLOB:
                if (subtype < 0)
                    return Types.BLOB;
                else if (subtype == 1)
                    return Types.LONGVARCHAR;
                else // if (subtype == 0 || subtype > 1)
                    return Types.LONGVARBINARY;
            case SQL_BOOLEAN:
                return Types.BOOLEAN;
            case SQL_NULL:
                return Types.NULL;
            case SQL_ARRAY:
                return Types.ARRAY;
            case SQL_QUAD:
            default:
                return Types.OTHER;
        }
    }

    public static int fromMetaDataToFirebirdType(int metaDataType) {
        switch (metaDataType) {
            case smallint_type:
                return SQL_SHORT;
            case integer_type:
                return SQL_LONG;
            case int64_type:
                return SQL_INT64;
            case quad_type:
                return SQL_QUAD;
            case float_type:
                return SQL_FLOAT;
            case double_type:
                return SQL_DOUBLE;
            case d_float_type:
                return SQL_D_FLOAT;
            case date_type:
                return SQL_TYPE_DATE;
            case time_type:
                return SQL_TYPE_TIME;
            case timestamp_type:
                return SQL_TIMESTAMP;
            case char_type:
                return SQL_TEXT;
            case varchar_type:
                return SQL_VARYING;
            case blob_type:
                return SQL_BLOB;
            case boolean_type:
                return SQL_BOOLEAN;
            default:
                // TODO Throw illegal arg / unsupported instead?
                return SQL_NULL;
        }
    }

    public boolean supportCatalogOrSchemaInFunctionOrProcedureCalls() throws DataSourceException {

        try {

            DatabaseMetaData dmd = getDatabaseMetaData();
            return dmd.supportsCatalogsInProcedureCalls() || dmd.supportsSchemasInProcedureCalls();

        } catch (SQLException e) {

            throw new DataSourceException(e);
        }
    }

    /**
     * Returns the priviliges of the specified object.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param table   the database object name
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
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs, connection);
        }

    }

    /**
     * Returns the default prefix name value for objects from this host.
     * ie. default catalog or schema name - with schema taking precedence.
     *
     * @return the default database object prefix
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
     * @param name name
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
     * @return the default database object prefix
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

        try {
            createDefaultMetaObjects(catalog, schema, metaObjects);
        } catch (Exception e) {
            throw new DataSourceException(e);
        }

        // load other types available not included in the defaults
        ResultSet rs = null;
        try {

            rs = getDatabaseMetaData().getTableTypes();
            while (rs.next()) {

                String type = rs.getString(1);
                if (!MiscUtils.containsValue(META_TYPES, type)) {

                    DatabaseMetaTag metaTag =
                            createDatabaseMetaTag(catalog, schema, type);
                    metaObjects.add(metaTag);


                }

            }

            return metaObjects;
        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs, connection);
            setMarkedForReload(false);
        }

    }

    private void createDefaultMetaObjects(DatabaseCatalog catalog,
                                          DatabaseSchema schema, List<DatabaseMetaTag> metaObjects)
            throws Exception {

        for (int i = 0; i < META_TYPES.length; i++) {

            DefaultDatabaseMetaTag metaTag =
                    createDatabaseMetaTag(catalog, schema, META_TYPES[i]);

            metaTag.setCatalog(catalog);
            metaTag.setSchema(schema);
            DatabaseConnection databaseConnection = metaTag.getHost().getDatabaseConnection();
            if (supportedObject(i))
                metaObjects.add(metaTag);
            if (SystemProperties.getBooleanProperty("user", "treeconnection.alphabet.sorting"))
                metaObjects.sort(new Comparator<DatabaseMetaTag>() {
                    @Override
                    public int compare(DatabaseMetaTag o1, DatabaseMetaTag o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

        }
    }

    boolean supportedObject(int type) throws Exception {
        DefaultDriverLoader driverLoader = new DefaultDriverLoader();
        Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
        DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
        Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());
        if (driver.getClass().getName().contains("FBDriver")) {
            Connection conn = connection.unwrap(Connection.class);
            URL[] urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
            ClassLoader cl = new URLClassLoader(urls, conn.getClass().getClassLoader());
            IFBDatabaseConnection db;
            Class clazzdb;
            Object odb;
            clazzdb = cl.loadClass("biz.redsoft.FBDatabaseConnectionImpl");
            odb = clazzdb.newInstance();
            db = (IFBDatabaseConnection) odb;
            db.setConnection(conn);
            switch (db.getMajorVersion()) {
                case 2:
                    switch (type) {
                        case NamedObject.SYNONYM:
                        case NamedObject.FUNCTION:
                        case NamedObject.SYSTEM_VIEW:
                        case NamedObject.PACKAGE:
                        case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
                        case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
                        case NamedObject.SYSTEM_STRING_FUNCTIONS:
                        case NamedObject.SYSTEM_FUNCTION:
                        case NamedObject.DDL_TRIGGER:
                            return false;
                    }
                case 3:
                case 4: // TODO check after the 4 version is released
                    switch (type) {
                        case NamedObject.SYNONYM:
                        case NamedObject.SYSTEM_VIEW:
                        case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
                        case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
                        case NamedObject.SYSTEM_STRING_FUNCTIONS:
                        case NamedObject.SYSTEM_FUNCTION:
                            return false;
                    }
            }
        }
        return !NamedObject.META_TYPES[type].contains("SYSTEM") || SystemProperties.getBooleanProperty("user", "browser.show.system.objects");
    }

    private DefaultDatabaseMetaTag createDatabaseMetaTag(
            DatabaseCatalog catalog, DatabaseSchema schema, String type) {
        DefaultDatabaseMetaTag tag = null;
        if (typeTree != TreePanel.DEFAULT) {
            tag = new DefaultDatabaseMetaTag(this, catalog, schema, type, typeTree, dependObject);
        } else tag = new DefaultDatabaseMetaTag(this, catalog, schema, type);
        return tag;
    }

    /**
     * Retrieves key/value type pairs using the <code>Reflection</code>
     * API to call and retrieve values from the connection's meta data
     * object's methods and variables.
     *
     * @return the database properties as key/value pairs
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
        } catch (SQLException e) {
            Log.error("Error attempting to retrieve database SQL keywords: " + e.getMessage());
            return new String[0];
        }
    }

    /**
     * Retrieves the data types associated with this host.
     */
    public ResultSet getDataTypeInfo() throws DataSourceException {
        try {
            return getDatabaseMetaData().getTypeInfo();
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Does nothing.
     */
    public int drop() throws DataSourceException {
        return 0;
    }

    @Override
    public boolean allowsChildren() {
        return true;
    }

    /**
     * Returns NULL.
     */
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
    public void setName(String name) {
    }

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
