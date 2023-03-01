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
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.*;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.datasource.PooledStatement;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.log.Log;
import org.executequery.sql.sqlbuilder.*;
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

    private final int typeTree;

    /**
     * the database connection wrapper for this host
     */
    private final transient DatabaseConnection databaseConnection;

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
            if(connected)
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

            close();
        }

    }

    /**
     * Closes the connection associated with this host.
     */
    public void close() {
        schemas = null;
        catalogs = null;
        databaseMetaData = null;
        connection = null;
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

        if (!getDatabaseConnection().isConnected()) {
            connection = null;
            return connection;
        }
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

    public void resetCountFinishedMetaTags() {
        countFinishedMetaTags = 0;
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
            if (!getDatabaseConnection().isConnected()) {
                Log.info("Connection lost");
                return null;
            } else throw new DataSourceException("Connection closed.", true);
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
    public List<String> getTableNames()
            throws DataSourceException {

        List<String> tables = new ArrayList<>();
        for (NamedObject table : getTables()) {
            tables.add(table.getName());
        }
        return tables;

    }

    public List<NamedObject> getTables()
            throws DataSourceException {

        List<NamedObject> tables = new ArrayList<>();
        tables.addAll(getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.TABLE]));
        tables.addAll(getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY]));
        tables.addAll(getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.VIEW]));
        tables.addAll(getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.SYSTEM_TABLE]));
        return tables;
    }

    /**
     * Returns the column names of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param table   the database object name
     * @return the column names
     */
    public List<String> getColumnNames(String table)
            throws DataSourceException {

        List<String> columns = new ArrayList<>();

        for (NamedObject namedObject : getTables()) {
            if (namedObject.getName().contentEquals(table))
                for (DatabaseColumn column : ((AbstractTableObject) namedObject).getColumns()) {
                    columns.add(column.getName());
                }
        }
        return columns;
    }

    private final transient ColumnInformationFactory columnInformationFactory = new ColumnInformationFactory();

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


    private PooledStatement statementForColumns;

    public void releaseStatementForColumns()
    {
        if(querySender!=null)
            querySender.releaseResources();
        releaseResources(statementForColumns);
    }

    /**
     * Returns the columns of the specified database object.
     *
     * @param catalog the table catalog name
     * @param schema  the table schema name
     * @param table   the database object name
     * @return the columns
     */

    DefaultStatementExecutor querySender;

    private final static String RELATION_NAME = "RELATION_NAME";
    private final static String FIELD_NAME = "FIELD_NAME";
    private final static String FIELD_TYPE = "FIELD_TYPE";
    private final static String FIELD_SUB_TYPE = "FIELD_SUB_TYPE";
    private final static String SEGMENT_LENGTH = "SEGMENT_LENGTH";
    private final static String FIELD_PRECISION = "FIELD_PRECISION";
    private final static String FIELD_SCALE = "FIELD_SCALE";
    private final static String FIELD_LENGTH = "FIELD_LENGTH";
    private final static String CHARACTER_LENGTH = "CHAR_LEN";
    private final static String DESCRIPTION = "DESCRIPTION";
    private final static String DEFAULT_SOURCE = "DEFAULT_SOURCE";
    private final static String DOMAIN_DEFAULT_SOURCE = "DOMAIN_DEFAULT_SOURCE";
    private final static String FIELD_POSITION = "FIELD_POSITION";
    private final static String NULL_FLAG = "NULL_FLAG";
    private final static String DOMAIN_NULL_FLAG = "DOMAIN_NULL_FLAG";
    private final static String COMPUTED_BLR = "COMPUTED_BLR";
    private final static String IDENTITY_TYPE = "IDENTITY_TYPE";
    private final static String CHARACTER_SET_ID = "CHARACTER_SET_ID";
    private final static String CHARACTER_SET_NAME = "CHARACTER_SET_NAME";
    private final static String COLLATION_NAME = "COLLATION_NAME";
    private final static String FIELD_SOURCE = "FIELD_SOURCE";
    private final static String COMPUTED_SOURCE = "COMPUTED_SOURCE";
    private final static String KEY_SEQ = "KEY_SEQ";
    private final static String CONSTRAINT_NAME = "CONSTRAINT_NAME";
    private final static String CONSTRAINT_TYPE = "CONSTRAINT_TYPE";
    private final static String REF_TABLE = "REF_TABLE";
    private final static String REF_COLUMN = "REF_COLUMN";
    private final static String UPDATE_RULE = "UPDATE_RULE";
    private final static String DELETE_RULE = "DELETE_RULE";

    public synchronized List<DatabaseColumn> getColumns(String table) {
        for (NamedObject namedObject : getTables()) {
            if (namedObject.getName().contentEquals(table))
                return ((AbstractTableObject) namedObject).getColumns();
        }
        return null;
    }

    public synchronized List<DatabaseColumn> getColumns(String table, boolean keepAlive)
            throws DataSourceException {

        ResultSet rs = null;

        List<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();

        try {
            if (querySender == null)
                    querySender=new DefaultStatementExecutor();
                if(querySender.getDatabaseConnection()!=getDatabaseConnection())
                    querySender.setDatabaseConnection(getDatabaseConnection());


                if(statementForColumns==null||statementForColumns.isClosed()) {
                    String prefix = "RDB$";
                    SelectBuilder sb = new SelectBuilder();
                    sb.setDistinct(true);
                    Table relationFields = Table.createTable().setName("RDB$RELATION_FIELDS").setAlias("RF");
                    Table fields = Table.createTable().setName("RDB$FIELDS").setAlias("F");
                    Table charsets = Table.createTable().setName("RDB$CHARACTER_SETS").setAlias("CH");
                    Table collations = Table.createTable().setName("RDB$COLLATIONS").setAlias("CO");
                    Table constraints = Table.createTable().setName("RDB$RELATION_CONSTRAINTS").setAlias("RC");
                    Table constraints1 = Table.createTable().setName("RDB$RELATION_CONSTRAINTS").setAlias("RCO");
                    Table indexSegments = Table.createTable().setName("RDB$INDEX_SEGMENTS").setAlias("ISGMT");
                    Table refTable = Table.createTable().setName("RDB$RELATION_CONSTRAINTS").setAlias("RC_REF");
                    Table refColumn = Table.createTable().setName("RDB$INDEX_SEGMENTS").setAlias("ISGMT_REF");
                    Table refCons = Table.createTable().setName("RDB$REF_CONSTRAINTS").setAlias("REF_CONS");
                    Field relName = Field.createField().setTable(relationFields).setName(prefix + RELATION_NAME).setAlias(RELATION_NAME);
                    sb.appendField(relName);
                    Field fieldName = Field.createField().setTable(relationFields).setName(prefix + FIELD_NAME).setAlias(FIELD_NAME);
                    sb.appendField(fieldName);
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + FIELD_TYPE).setAlias(FIELD_TYPE));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + FIELD_SUB_TYPE).setAlias(FIELD_SUB_TYPE));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + SEGMENT_LENGTH).setAlias(SEGMENT_LENGTH));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + FIELD_PRECISION).setAlias(FIELD_PRECISION));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + FIELD_SCALE).setAlias(FIELD_SCALE));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + FIELD_LENGTH).setAlias(FIELD_LENGTH));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + "CHARACTER_LENGTH").setAlias(CHARACTER_LENGTH));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + DEFAULT_SOURCE).setAlias(DOMAIN_DEFAULT_SOURCE));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + NULL_FLAG).setAlias(DOMAIN_NULL_FLAG));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + COMPUTED_BLR).setAlias(COMPUTED_BLR));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + CHARACTER_SET_ID).setAlias(CHARACTER_SET_ID));
                    sb.appendField(Field.createField().setTable(fields).setName(prefix + COMPUTED_SOURCE).setAlias(COMPUTED_SOURCE));
                    sb.appendField(Field.createField().setTable(charsets).setName(prefix + CHARACTER_SET_NAME).setAlias(CHARACTER_SET_NAME));
                    sb.appendField(Field.createField().setTable(collations).setName(prefix + COLLATION_NAME).setAlias(COLLATION_NAME));
                    sb.appendField(Field.createField().setTable(relationFields).setName(prefix + DEFAULT_SOURCE).setAlias(DEFAULT_SOURCE));
                    sb.appendField(Field.createField().setTable(relationFields).setName(prefix + NULL_FLAG).setAlias(NULL_FLAG));
                    Field fieldSource = Field.createField().setTable(relationFields).setName(prefix + FIELD_SOURCE).setAlias(FIELD_SOURCE);
                    sb.appendField(fieldSource);
                    sb.appendField(Field.createField().setTable(relationFields).setName(prefix + DESCRIPTION).setAlias(DESCRIPTION));
                    sb.appendField(Field.createField().setTable(relationFields).setName(prefix + IDENTITY_TYPE).setAlias(IDENTITY_TYPE).setNull(getDatabaseMajorVersion() < 3));
                    Field fieldPosition = Field.createField().setTable(relationFields).setName(prefix + FIELD_POSITION).setAlias(FIELD_POSITION);
                    fieldPosition.setStatement(fieldPosition.getFieldTable() + " + 1");
                    sb.appendField(fieldPosition);
                    sb.appendField(Field.createField().setTable(constraints).setName(prefix + CONSTRAINT_NAME).setAlias(CONSTRAINT_NAME));
                    sb.appendField(Field.createField().setTable(constraints).setName(prefix + CONSTRAINT_TYPE).setAlias(CONSTRAINT_TYPE));
                    Field keyPosition = Field.createField().setTable(indexSegments).setName(prefix + FIELD_POSITION).setAlias(KEY_SEQ);
                    keyPosition.setStatement(keyPosition.getFieldTable() + " + 1");
                    sb.appendField(keyPosition);
                    sb.appendField(Field.createField().setTable(refTable).setName(relName.getName()).setAlias(REF_TABLE));
                    sb.appendField(Field.createField().setTable(refColumn).setName(fieldName.getName()).setAlias(REF_COLUMN));
                    sb.appendField(Field.createField().setTable(refCons).setName(prefix + UPDATE_RULE).setAlias(UPDATE_RULE));
                    sb.appendField(Field.createField().setTable(refCons).setName(prefix + DELETE_RULE).setAlias(DELETE_RULE));


                    sb.appendJoin(LeftJoin.createLeftJoin().appendFields(relName, Field.createField().setTable(constraints1).setName(relName.getName())));
                    sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField().setTable(constraints1).setName("RDB$INDEX_NAME"),
                            Field.createField().setTable(indexSegments).setName("RDB$INDEX_NAME")).appendFields(fieldName
                            , Field.createField().setTable(indexSegments).setName(fieldName.getName())));
                    sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField().setTable(indexSegments).setName("RDB$INDEX_NAME"),
                            Field.createField().setTable(constraints).setName("RDB$INDEX_NAME")));
                    sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField().setTable(constraints).setName(prefix + CONSTRAINT_NAME),
                            Field.createField().setTable(refCons).setName(prefix + CONSTRAINT_NAME)));
                    sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField().setTable(refCons).setName(prefix + "CONST_NAME_UQ"),
                            Field.createField().setTable(refTable).setName(prefix + CONSTRAINT_NAME)));
                    sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField().setTable(refTable).setName("RDB$INDEX_NAME"),
                            Field.createField().setTable(refColumn).setName("RDB$INDEX_NAME")));
                    sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField().setTable(fields).setName("RDB$CHARACTER_SET_ID"),
                            Field.createField().setTable(charsets).setName("RDB$CHARACTER_SET_ID")));
                    sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField().setTable(fields).setName("RDB$CHARACTER_SET_ID"),
                                    Field.createField().setTable(collations).setName("RDB$CHARACTER_SET_ID"))
                            .appendFields(Field.createField().setTable(fields).setName("RDB$COLLATION_ID"),
                                    Field.createField().setTable(collations).setName("RDB$COLLATION_ID")));

                    sb.appendCondition(Condition.createCondition().setLeftField(relName).setOperator("=").setRightStatement("?"));
                    sb.appendCondition(Condition.createCondition().setLeftField(fieldSource).setOperator("=").
                            setRightStatement(Field.createField().setTable(fields).setName("RDB$FIELD_NAME").getFieldTable()));
                    /*Field conType = Field.createField().setTable(constraints1).setName(prefix + CONSTRAINT_TYPE);
                    sb.appendCondition(Condition.createCondition()
                            .appendCondition(Condition.createCondition()
                                    .appendCondition(Condition.createCondition().setLeftField(conType).setOperator("<>").setRightStatement("'NOT NULL'"))
                                    .appendCondition(Condition.createCondition().setLeftField(conType).setOperator("<>").setRightStatement("'CHECK'")))
                            .appendCondition(Condition.createCondition().setLeftField(conType).setOperator("IS").setRightStatement("NULL")).setLogicOperator("OR"));
                    */
                    sb.setOrdering(fieldPosition.getFieldTable());

                    String firebirdSql = sb.getSQLQuery();
                    statementForColumns = (PooledStatement) querySender.getPreparedStatement(firebirdSql);
                }
                statementForColumns.setString(1,table);
                rs = querySender.getResultSet(-1,statementForColumns).getResultSet();
                columns = createColumns(rs, table);
            //releaseResources(rs);

            return columns;

        } catch (Exception e) {

            Log.error("Error retrieving column data for table " + table
                    + " using connection " + getDatabaseConnection(), e);
            e.printStackTrace();

            return columns;

        }
        finally {
            if(!keepAlive)
                releaseStatementForColumns();
        }

    }

    private List<DatabaseColumn> createColumns (ResultSet rs, String table) throws SQLException {
        List<DatabaseColumn> columns = new ArrayList<>();
        DefaultDatabaseColumn previousColumn = null;
        while (rs.next()) {
            String colName = rs.getString(FIELD_NAME).trim();
            if (previousColumn == null || !colName.equalsIgnoreCase(previousColumn.getName())) {
                DefaultDatabaseColumn column = new DefaultDatabaseColumn();
                previousColumn = column;
                final short fieldType = rs.getShort(FIELD_TYPE);
                final short fieldSubType = rs.getShort(FIELD_SUB_TYPE);
                final short fieldScale = rs.getShort(FIELD_SCALE);
                final int characterSetId = rs.getInt(CHARACTER_SET_ID);
                final int dataType = getDataType(fieldType, fieldSubType, fieldScale, characterSetId);

                column.setTypeInt(dataType);
                column.setColumnSubtype(fieldSubType);
                column.setColumnScale(fieldScale);
                column.setName(colName);
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
                        column.setColumnSize(rs.getShort(FIELD_LENGTH));
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
                column.setColumnSize(rs.getInt(FIELD_LENGTH));
                if (rs.getInt(FIELD_PRECISION) != 0)
                    column.setColumnSize(rs.getInt(FIELD_PRECISION));
                if (rs.getInt(CHARACTER_LENGTH) != 0)
                    column.setColumnSize(rs.getInt(CHARACTER_LENGTH));

                final short nullFlag = rs.getShort(NULL_FLAG);
                final short sourceNullFlag = rs.getShort(DOMAIN_NULL_FLAG);
                column.setRemarks(rs.getString(DESCRIPTION));
                column.setRequired(nullFlag == 1 || sourceNullFlag == 1);

                String column_def = rs.getString(DEFAULT_SOURCE);
                if (column_def == null) {
                    column_def = rs.getString(DOMAIN_DEFAULT_SOURCE);
                }
                if (column_def != null) {
                    // TODO This looks suspicious (what if it contains default)
                    int defaultPos = column_def.toUpperCase().trim().indexOf("DEFAULT");
                    if (defaultPos == 0)
                        column_def = column_def.substring(7).trim();
                    column.setDefaultValue(column_def);
                }

                column.setIdentity(rs.getInt(IDENTITY_TYPE) == 1);
                String charset = rs.getString(CHARACTER_SET_NAME);
                String collate = rs.getString(COLLATION_NAME);
                if (charset != null)
                    charset = charset.trim();
                if (collate != null)
                    collate = collate.trim();
                column.setCharset(charset);
                column.setCollate(collate);
                String domain = rs.getString(FIELD_SOURCE);
                if (domain != null && !domain.isEmpty()) {
                    column.setDomain(domain);
                }
                String computedSource = rs.getString(COMPUTED_SOURCE);
                if (computedSource != null && !computedSource.isEmpty()) {
                    column.setGenerated(true);
                    if (computedSource.startsWith("(") && computedSource.endsWith(")"))
                        computedSource = computedSource.substring(1, computedSource.length() - 1);
                    column.setComputedSource(computedSource);
                }
                if (column.getTypeInt() == Types.LONGVARBINARY ||
                        column.getTypeInt() == Types.LONGVARCHAR ||
                        column.getTypeInt() == Types.BLOB) {
                    column.setColumnSubtype(fieldSubType);
                    column.setColumnSize(rs.getInt(SEGMENT_LENGTH));
                }
                columns.add(column);
            }
            String conType = rs.getString(CONSTRAINT_TYPE);
            if (conType != null) {
                conType = conType.trim();
                switch (conType) {
                    case "PRIMARY KEY":
                        previousColumn.setPrimaryKey(true);
                        TableColumnConstraint constraint = new TableColumnConstraint(null, ColumnConstraint.PRIMARY_KEY);
                        constraint.setName(rs.getString(CONSTRAINT_NAME));
                        previousColumn.addConstraint(constraint);
                        break;
                    case "UNIQUE":
                        previousColumn.setUnique(true);
                        constraint = new TableColumnConstraint(null, ColumnConstraint.UNIQUE_KEY);
                        constraint.setName(rs.getString(CONSTRAINT_NAME));
                        previousColumn.addConstraint(constraint);
                        break;
                    case "FOREIGN KEY":
                        previousColumn.setForeignKey(true);
                        constraint = new TableColumnConstraint(null, ColumnConstraint.FOREIGN_KEY);
                        constraint.setName(rs.getString(CONSTRAINT_NAME));
                        constraint.setReferencedTable(rs.getString(REF_TABLE));
                        constraint.setReferencedColumn(rs.getString(REF_COLUMN));
                        String rule = rs.getString(UPDATE_RULE);
                        if (rule != null)
                            constraint.setUpdateRule(rule.trim());
                        rule = rs.getString(DELETE_RULE);
                        if (rule != null)
                            constraint.setDeleteRule(rule.trim());
                        previousColumn.addConstraint(constraint);
                        break;
                    default:
                        break;
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
        final int jdbcType = fromMetaDataToJdbcType(fieldType, fieldSubType);
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

    public static int fromMetaDataToJdbcType(int metaDataType, int subtype) {
        return DatabaseTypeConverter.getSqlTypeFromRDBType(metaDataType, subtype);
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
    List<DatabaseMetaTag> metaTags;

    public List<DatabaseMetaTag> getMetaObjects() throws DataSourceException {
        if (metaTags == null)
            metaTags = getMetaObjects(null, null);
        return metaTags;
    }

    public void reset() {
        super.reset();
        metaTags = null;
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
            if (supportedObject(i)) {
                metaObjects.add(metaTag);
                //!NamedObject.META_TYPES[type].contains("SYSTEM") || SystemProperties.getBooleanProperty("user", "browser.show.system.objects");
            }
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
        if (typeTree == TreePanel.TABLESPACE) {
            return type == TABLE || type == INDEX;
        }
        if (typeTree == TreePanel.DEPENDED_ON || typeTree == TreePanel.DEPENDENT) {
            if (type >= SYSTEM_DOMAIN)
                return false;
        }
        DefaultDriverLoader driverLoader = new DefaultDriverLoader();
        Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
        DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
        Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());
        if (driver.getClass().getName().contains("FBDriver")) {
            Connection conn = getConnection().unwrap(Connection.class);
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
                        case NamedObject.SYSTEM_PACKAGE:
                        case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
                        case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
                        case NamedObject.SYSTEM_STRING_FUNCTIONS:
                        case NamedObject.SYSTEM_FUNCTION:
                        case NamedObject.DDL_TRIGGER:
                        case NamedObject.USER:
                            return false;
                    }
                default: // TODO check after the 5 version is released
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
            if (type == NamedObject.TABLESPACE)
                return getDatabaseProductName().toUpperCase().contains("REDDATABASE") && db.getMajorVersion() >= 4;
            return type != NamedObject.TABLE_COLUMN && type != NamedObject.CONSTRAINT;
        }
        return true;
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

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return getDatabaseMetaData().getDatabaseMajorVersion();
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

    public List<NamedObject> getDatabaseObjectsForMetaTag(String metadatakey) {
        for (DatabaseMetaTag metaTag : getMetaObjects()) {
            if (metadatakey.equals(metaTag.getMetaDataKey()))
                return metaTag.getObjects();
        }
        return null;
    }

    public List<String> getDatabaseObjectNamesForMetaTag(String metadatakey) {
        List<String> list = new ArrayList<>();
        List<NamedObject> databaseObjects = getDatabaseObjectsForMetaTag(metadatakey);
        for (NamedObject namedObject : databaseObjects) {
            list.add(namedObject.getName().trim());
        }
        return list;
    }

    public NamedObject getDatabaseObjectFromMetaTagAndName(String metadatakey, String name) {
        List<NamedObject> namedObjects = getDatabaseObjectsForMetaTag(metadatakey);
        for (NamedObject namedObject : namedObjects) {
            if (namedObject.getName().trim().contentEquals(name))
                return namedObject;
        }
        return null;
    }

    public NamedObject getDatabaseObjectFromTypeAndName(int type, String name) {
        return getDatabaseObjectFromMetaTagAndName(NamedObject.META_TYPES[type], name);
    }
}
