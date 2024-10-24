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

import biz.redsoft.IFBDatabaseMetadata;
import org.executequery.Constants;
import org.executequery.actions.databasecommands.DatabaseStatisticCommand;
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.Types;
import org.executequery.databaseobjects.*;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.datasource.PooledDatabaseMetaData;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.sqlbuilder.Field;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.Named;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

import static org.executequery.actions.databasecommands.DatabaseStatisticCommand.getHeaderValue;

/**
 * Default database host object implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultDatabaseHost extends AbstractNamedObject
        implements DatabaseHost {

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

    private List<DatabaseMetaTag> metaTags;
    private DatabaseObject dependObject;
    private boolean pauseLoadingTreeForSearch = false;

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

            boolean connected = connectionMediator().connect(getDatabaseConnection());

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

    Map<Object, Object> databaseProperties;

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

    /**
     * Returns the database metadata for this host.
     *
     * @return the database meta data
     */
    @Override
    public DatabaseMetaData getDatabaseMetaData() throws DataSourceException {

        if (!isConnected()) {
            if (!getDatabaseConnection().isConnected()) {
                Log.debug("Connection lost");
                return null;
            } else
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
     * Returns copy of this object
     */
    @Override
    public NamedObject copy() {
        return new DefaultDatabaseHost(databaseConnection.copy(), typeTree);
    }

    /**
     * Returns the table names hosted by this host.
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
     * @param table the database object name
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

    public void releaseStatementForColumns() {
        if (querySender != null)
            querySender.releaseResources();
    }

    DefaultStatementExecutor querySender;

    /**
     * Returns the columns of the specified database object.
     *
     * @param table the database object name
     * @return the columns
     */
    @Override
    public synchronized List<DatabaseColumn> getColumns(String table) {
        for (NamedObject namedObject : getTables()) {
            if (namedObject.getName().contentEquals(table))
                return ((AbstractTableObject) namedObject).getColumns();
        }
        return null;
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

    final static int SQL_TEXT = 452;
    final static int SQL_VARYING = 448;
    final static int SQL_SHORT = 500;
    final static int SQL_LONG = 496;
    final static int SQL_FLOAT = 482;
    final static int SQL_DOUBLE = 480;
    final static int SQL_D_FLOAT = 530;
    final static int SQL_TIMESTAMP = 510;
    final static int SQL_BLOB = 520;
    final static int SQL_ARRAY = 540;
    final static int SQL_QUAD = 550;
    final static int SQL_TYPE_TIME = 560;
    final static int SQL_TYPE_DATE = 570;
    final static int SQL_INT64 = 580;
    final static int SQL_BOOLEAN = 32764;
    final static int SQL_NULL = 32766;

    final static int CS_NONE = 0; /* No Character Set */
    final static int CS_BINARY = 1; /* BINARY BYTES */
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


    /**
     * Returns the privileges of the specified object.
     *
     * @param table the database object name
     */
    @Override
    public List<TablePrivilege> getPrivileges(String table)
            throws DataSourceException {

        ResultSet rs = null;
        try {
            List<TablePrivilege> privileges = new ArrayList<>();

            DatabaseMetaData dmd = getDatabaseMetaData();
            rs = dmd.getTablePrivileges(null, null, table);
            while (rs.next()) {
                privileges.add(new TablePrivilege(rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7)));
            }
            return privileges;

        } catch (SQLException e) {
            throw new DataSourceException(e);

        } finally {
            releaseResources(rs, connection);
        }
    }

    /**
     * Returns the default prefix name value for objects from this host.
     *
     * @return the default database object prefix
     */
    public String getDefaultNamePrefix() {
        return null;
    }

    /**
     * Returns the database source with the specified name.
     *
     * @param name name
     * @return the database source object
     */
    public DatabaseSource getDatabaseSource(String name) {
        if (name == null)
            return getDatabaseSource(getDefaultNamePrefix());
        return null;
    }

    /**
     * Returns the meta type objects.
     *
     * @return the meta type objects
     */
    public List<DatabaseMetaTag> getMetaObjects() throws DataSourceException {
        if (metaTags == null)
            metaTags = loadMetaObjects();
        return metaTags;
    }

    public void reset() {
        super.reset();
        metaTags = null;
    }

    public List<DatabaseMetaTag> loadMetaObjects() throws DataSourceException {

        List<DatabaseMetaTag> metaObjects = new ArrayList<>();

        try {
            createDefaultMetaObjects(metaObjects);
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
                    DatabaseMetaTag metaTag = createDatabaseMetaTag(type);
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

    public List<ColumnData> getColumnDataListFromTableName(String tableName) {

        DefaultDatabaseTable table = (DefaultDatabaseTable) getDatabaseObjectFromTypeAndName(NamedObject.TABLE, tableName);
        if (table == null)
            table = (DefaultDatabaseTable) ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).getDatabaseObjectFromTypeAndName(NamedObject.GLOBAL_TEMPORARY, tableName);
        if (table != null)
            return table.getColumnDataList();
        else return null;
    }

    public ColumnData[] getColumnDataArrayFromTableName(String tableName) {
        List<ColumnData> list = getColumnDataListFromTableName(tableName);
        if (list != null)
            return list.toArray(new ColumnData[0]);
        else return new ColumnData[0];
    }

    private void createDefaultMetaObjects(List<DatabaseMetaTag> metaObjects) throws Exception {

        for (int i = 0; i < META_TYPES.length; i++) {

            DefaultDatabaseMetaTag metaTag = createDatabaseMetaTag(META_TYPES[i]);
            if (supportedObject(i))
                metaObjects.add(metaTag);

            if (SystemProperties.getBooleanProperty("user", "treeconnection.alphabet.sorting"))
                metaObjects.sort(Comparator.comparing(Named::getName));
        }
    }

    public boolean supportedObject(int type) throws Exception {
        if (typeTree == TreePanel.TABLESPACE) {
            return type == TABLE || type == INDEX;
        }
        if (typeTree == TreePanel.DEPENDED_ON || typeTree == TreePanel.DEPENDENT) {
            if (type >= SYSTEM_DOMAIN)
                return false;
        }
        Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
        DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
        Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());
        if (driver.getClass().getName().contains("FBDriver")) {
            // TODO check after the 5 version is released
            if (getDatabaseMajorVersion() == 2) {
                switch (type) {
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
            }
            switch (type) {
                case NamedObject.SYSTEM_VIEW:
                case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
                case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
                case NamedObject.SYSTEM_STRING_FUNCTIONS:
                case NamedObject.SYSTEM_FUNCTION:
                    return false;
            }
            if (type == NamedObject.TABLESPACE || type == NamedObject.JOB)
                return isRDB() && getDatabaseMajorVersion() >= 4;
            return type != NamedObject.TABLE_COLUMN && type != NamedObject.CONSTRAINT;
        }
        return true;
    }

    protected boolean isRDB() {
        return getDatabaseProductName().toLowerCase().contains("reddatabase");
    }

    private DefaultDatabaseMetaTag createDatabaseMetaTag(String type) {
        return typeTree != TreePanel.DEFAULT ?
                new DefaultDatabaseMetaTag(this, type, typeTree, dependObject) :
                new DefaultDatabaseMetaTag(this, type);
    }

    /**
     * Retrieves key/value type pairs using the <code>Reflection</code>
     * API to call and retrieve values from the connection's metadata
     * object's methods and variables.
     *
     * @return the database properties as key/value pairs
     */
    public Map<Object, Object> getMetaProperties() throws DataSourceException {

        PooledDatabaseMetaData dmd = (PooledDatabaseMetaData) getDatabaseMetaData();
        if (dmd == null)
            return new HashMap<>();

        IFBDatabaseMetadata db;
        try {
            db = (IFBDatabaseMetadata) DynamicLibraryLoader.loadingObjectFromClassLoader(databaseConnection.getDriverMajorVersion(), dmd.getInner(), "FBDatabaseMetadataImpl");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Object[] defaultArg = new Object[]{};
        Class<?> metaClass = dmd.getClass();
        Method[] metaMethods = metaClass.getMethods();

        Map<Object, Object> properties = new HashMap<Object, Object>();

        String STRING = "String";
        String GET = "get";


        try {
            properties.put("Sever version", dmd.getDatabaseProductName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            properties.put("ODS version", db.getOdsVersion(dmd.getInner()));
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    /**
     * Closes the connection associated with this host.
     */
    public void close() {
        databaseMetaData = null;
        connection = null;
        databaseProperties = null;
    }

    @Override
    public Map<Object, Object> getDatabaseProperties() {
        if (databaseProperties == null || databaseProperties.isEmpty()) {
            databaseProperties = getDatabaseProperties(getDatabaseConnection(), true);
            databaseProperties.put(bundleString("ServerVersion"), getDatabaseProductNameVersion());

            Map<Object, Object> metaProperties = getMetaProperties();
            databaseProperties.put(bundleString("Driver"), metaProperties.get("DriverName") + " " + metaProperties.get("DriverVersion"));
        }

        return databaseProperties;
    }

    public static Map<Object, Object> getDatabaseProperties(DatabaseConnection connection, boolean handleException) {

        Map<Object, Object> databaseProperties = new HashMap<>();
        databaseProperties.put(bundleString("Driver"), connection.getDriverName());

        String databaseHeader = DatabaseStatisticCommand.getDatabaseHeader(connection, handleException);
        databaseProperties.put(bundleString("GUID"), getHeaderValue(DatabaseStatisticCommand.GUID, databaseHeader));
        databaseProperties.put(bundleString("NEXT_ATTACHMENT"), getHeaderValue(DatabaseStatisticCommand.NEXT_ATACHMENT, databaseHeader));
        databaseProperties.put(bundleString("GENERATION"), getHeaderValue(DatabaseStatisticCommand.GENERATION, databaseHeader));
        databaseProperties.put(bundleString("AUTOSWEEP_GAP"), getHeaderValue(DatabaseStatisticCommand.AUTOSWEEP_GAP, databaseHeader));
        databaseProperties.put(bundleString("SEQUENCE_NUM"), getHeaderValue(DatabaseStatisticCommand.SEQUENCE_NUM, databaseHeader));
        databaseProperties.put(bundleString("IMPLEMENTATION"), getHeaderValue(DatabaseStatisticCommand.IMPLEMENTATION, databaseHeader));
        databaseProperties.put(bundleString("SHADOW_COUNT"), getHeaderValue(DatabaseStatisticCommand.SHADOW_COUNT, databaseHeader));
        databaseProperties.put(bundleString("SCN"), getHeaderValue(DatabaseStatisticCommand.SCN, databaseHeader));
        databaseProperties.put(bundleString("CREATION_DATE"), getHeaderValue(DatabaseStatisticCommand.CREATION_DATE, databaseHeader));
        databaseProperties.put(bundleString("SQL_DIALECT"), getHeaderValue(DatabaseStatisticCommand.DIALECT, databaseHeader));
        databaseProperties.put(bundleString("NEXT_TRANSACTION"), getHeaderValue(DatabaseStatisticCommand.NEXT_TRANSACTION, databaseHeader));
        databaseProperties.put(bundleString("PAGE_SIZE"), getHeaderValue(DatabaseStatisticCommand.PAGE_SIZE, databaseHeader));
        databaseProperties.put(bundleString("ODS_VERSION"), getHeaderValue(DatabaseStatisticCommand.ODS, databaseHeader));
        databaseProperties.put(bundleString("ServerVersion"), (getHeaderValue(DatabaseStatisticCommand.SERVER, databaseHeader) + " " + connection.getMajorServerVersion()).trim());
        databaseProperties.put(bundleString("OLDEST_TRANSACTION"), getHeaderValue(DatabaseStatisticCommand.OIT, databaseHeader));
        databaseProperties.put(bundleString("OLDEST_ACTIVE"), getHeaderValue(DatabaseStatisticCommand.OAT, databaseHeader));
        databaseProperties.put(bundleString("OLDEST_SNAPSHOT"), getHeaderValue(DatabaseStatisticCommand.OST, databaseHeader));

        Object value = getHeaderValue(DatabaseStatisticCommand.PAGE_BUFF, databaseHeader);
        if (value != null && !MiscUtils.isNull(value.toString())) {
            int intVal = Integer.parseInt(value.toString());
            value = Objects.equals(intVal, 0) ? Bundles.getCommon("default") : value;
        }
        databaseProperties.put(bundleString("PAGE_BUFFER_GSTAT"), value);

        if (connection.isConnected()) {
            try {
                boolean isFirebird3 = connection.getMajorServerVersion() >= 3;
                boolean isFirebird4 = connection.getMajorServerVersion() >= 4;
                boolean isRedDatabase5 = connection.getMajorServerVersion() >= 5
                        && !MiscUtils.isNull(new DefaultDatabaseHost(connection).getDatabaseProductName())
                        && new DefaultDatabaseHost(connection).getDatabaseProductName().toLowerCase().contains("reddatabase");

                Table databaseTable = Table.createTable("MON$DATABASE", "D");
                SelectBuilder sb = new SelectBuilder(connection);
                sb.appendTable(databaseTable);
                sb.appendField(Field.createField(databaseTable, "MON$DATABASE_NAME", "DATABASE_NAME"));
                sb.appendField(Field.createField(databaseTable, "MON$PAGE_SIZE", "PAGE_SIZE"));
                sb.appendField(Field.createField(databaseTable, "MON$PAGE_BUFFERS", "PAGE_BUFFERS"));
                sb.appendField(Field.createField(databaseTable, "MON$SHUTDOWN_MODE", "SHUTDOWN_MODE"));
                sb.appendField(Field.createField(databaseTable, "MON$SWEEP_INTERVAL", "SWEEP_INTERVAL"));
                sb.appendField(Field.createField(databaseTable, "MON$READ_ONLY", "READ_ONLY"));
                sb.appendField(Field.createField(databaseTable, "MON$FORCED_WRITES", "FORCED_WRITES"));
                sb.appendField(Field.createField(databaseTable, "MON$RESERVE_SPACE", "RESERVE_SPACE"));
                sb.appendField(Field.createField(databaseTable, "MON$PAGES", "PAGES"));
                sb.appendField(Field.createField(databaseTable, "MON$STAT_ID", "STAT_ID"));
                sb.appendField(Field.createField(databaseTable, "MON$BACKUP_STATE", "BACKUP_STATE"));
                if (isFirebird3) {
                    sb.appendField(Field.createField(databaseTable, "MON$CRYPT_PAGE", "CRYPT_PAGE"));
                    sb.appendField(Field.createField(databaseTable, "MON$OWNER", "OWNER"));
                    sb.appendField(Field.createField(databaseTable, "MON$SEC_DATABASE", "SEC_DATABASE"));
                }
                if (isFirebird4) {
                    sb.appendField(Field.createField(databaseTable, "MON$FILE_ID", "FILE_ID"));
                    sb.appendField(Field.createField(databaseTable, "MON$REPLICA_MODE", "REPLICA_MODE"));
                    sb.appendField(Field.createField(databaseTable, "MON$NEXT_STATEMENT", "NEXT_STATEMENT"));
                }
                if (isRedDatabase5) {
                    sb.appendField(Field.createField(databaseTable, "MON$CRYPT_STATE", "CRYPT_STATE"));
                }

                ResultSet rs = new DefaultStatementExecutor(connection).getResultSet(sb.getSQLQuery()).getResultSet();
                if (rs != null && rs.next()) {

                    // --- shutdown mode ---

                    value = rs.getInt("SHUTDOWN_MODE");
                    if (value.equals(0))
                        value = bundleString("onlineDatabase");
                    else if (value.equals(1))
                        value = bundleString("multiUserShutdown");
                    else if (value.equals(2))
                        value = bundleString("singleUserShutdown");
                    else
                        value = bundleString("fullShutdown");

                    databaseProperties.put(bundleString("SHUTDOWN_MODE"), value);

                    // --- read/write mode ---

                    value = rs.getInt("READ_ONLY");
                    value = value.equals(1) ? bundleString("readOnly") : bundleString("readWrite");
                    databaseProperties.put(bundleString("READ_ONLY"), value);

                    // --- writes mode ---

                    value = rs.getInt("FORCED_WRITES");
                    value = value.equals(1) ? bundleString("forcedWrites") : bundleString("asynchronousWrite");
                    databaseProperties.put(bundleString("FORCED_WRITES"), value);

                    // --- db space management mode ---

                    value = rs.getInt("RESERVE_SPACE");
                    value = value.equals(0) ? bundleString("fullSpace") : bundleString("reserveSpace");
                    databaseProperties.put(bundleString("RESERVE_SPACE"), value);

                    // --- db space management mode ---

                    value = rs.getInt("BACKUP_STATE");
                    if (value.equals(0))
                        value = bundleString("noBackup");
                    else if (value.equals(1))
                        value = bundleString("blockedBackup");
                    else
                        value = bundleString("activeBackup");

                    databaseProperties.put(bundleString("BACKUP_STATE"), value);

                    // --- db file size ---

                    long pageSize = rs.getLong("PAGE_SIZE");    // db pages size
                    long pagesCount = rs.getLong("PAGES");      // db pages count

                    databaseProperties.put(bundleString("PAGES"), pagesCount);
                    databaseProperties.put(bundleString("dbFileSize"), pageSize * pagesCount);

                    // --- db security mode ---

                    if (isFirebird3) {

                        value = rs.getString("SEC_DATABASE");
                        if (value.equals("Default"))
                            value = bundleString("DefaultSecurity");
                        else if (value.equals("Self"))
                            value = bundleString("SelfSecurity");
                        else
                            value = bundleString("OtherSecurity");

                        databaseProperties.put(bundleString("SEC_DATABASE"), value);
                    }

                    // --- db encrypt mode ---

                    if (isFirebird4) {

                        value = rs.getInt("REPLICA_MODE");
                        if (value.equals(0))
                            value = bundleString("notReplica");
                        else if (value.equals(1))
                            value = bundleString("read-onlyReplica");
                        else
                            value = bundleString("read-writeReplica");

                        databaseProperties.put(bundleString("REPLICA_MODE"), value);
                    }

                    // --- db encrypt mode ---

                    if (isRedDatabase5) {

                        value = rs.getInt("CRYPT_STATE");
                        if (value.equals(0))
                            value = bundleString("noEncrypt");
                        else if (value.equals(1))
                            value = bundleString("encrypted");
                        else
                            value = bundleString("activeEncrypt");

                        databaseProperties.put(bundleString("CRYPT_STATE"), value);
                    }

                    // --- others ---

                    databaseProperties.put(bundleString("DATABASE_NAME"), rs.getString("DATABASE_NAME"));           //  db file name
                    databaseProperties.put(bundleString("PAGE_BUFFERS"), rs.getInt("PAGE_BUFFERS"));                // cached pages count
                    databaseProperties.put(bundleString("SWEEP_INTERVAL"), rs.getInt("SWEEP_INTERVAL"));            // sweep interval
                    databaseProperties.put(bundleString("STAT_ID"), rs.getInt("STAT_ID"));                          // statistics index
                    if (isFirebird3) {
                        databaseProperties.put(bundleString("CRYPT_PAGE"), rs.getInt("CRYPT_PAGE"));                // now encrypted db pages
                        databaseProperties.put(bundleString("OWNER"), rs.getString("OWNER"));                       // db owner name
                    }
                    if (isFirebird4) {
                        databaseProperties.put(bundleString("FILE_ID"), rs.getString("FILE_ID"));                   // db file id
                        databaseProperties.put(bundleString("NEXT_STATEMENT"), rs.getString("NEXT_STATEMENT"));     // next statement ID counter
                    }
                }

            } catch (SQLException e) {
                Log.error("Error occurred loading database properties", e);
            }
        }

        databaseProperties.values().removeAll(Collections.singleton(Constants.EMPTY));
        databaseProperties.values().removeAll(Collections.singleton(null));
        return databaseProperties;
    }

    /**
     * Concatenates product name and product version
     */
    @Override
    public String getDatabaseProductNameVersion() {
        return getDatabaseProductName() + " " + getDatabaseProductVersion();
    }

    @Override
    public String getDatabaseProductName() {
        return isConnected() ?
                (String) getMetaProperties().get("DatabaseProductName") :
                getDatabaseConnection().getDatabaseType();
    }

    @Override
    public String getDatabaseProductVersion() {
        return isConnected() ?
                (String) getMetaProperties().get("DatabaseProductVersion") :
                getDatabaseConnection().getDatabaseType();
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

    public List<NamedObject> getDatabaseObjectsForMetaTag(String metadatakey) {
        for (DatabaseMetaTag metaTag : getMetaObjects()) {
            if (metadatakey.equals(metaTag.getMetaDataKey()))
                return metaTag.getObjects();
        }
        return null;
    }

    public void reloadMetaTag(int type) {
        for (DatabaseMetaTag metaTag : getMetaObjects()) {
            if (metaTag.getMetaDataKey().equals(NamedObject.META_TYPES[type])) {
                metaTag.reset();
                return;
            }
        }
    }

    public AbstractTableObject getTableFromName(String name) {
        NamedObject namedObject = getDatabaseObjectFromTypeAndName(TABLE, name);
        if (namedObject == null)
            namedObject = getDatabaseObjectFromTypeAndName(GLOBAL_TEMPORARY, name);
        if (namedObject == null)
            namedObject = getDatabaseObjectFromTypeAndName(VIEW, name);
        return (AbstractTableObject) namedObject;
    }

    public List<String> getDatabaseObjectNamesForMetaTag(String metadatakey) {
        List<String> list = new ArrayList<>();
        List<NamedObject> databaseObjects = getDatabaseObjectsForMetaTag(metadatakey);
        for (NamedObject namedObject : databaseObjects) {
            list.add(MiscUtils.trimEnd(namedObject.getName()));
        }
        return list;
    }

    public NamedObject getDatabaseObjectFromMetaTagAndName(String metadatakey, String name) {
        List<NamedObject> namedObjects = getDatabaseObjectsForMetaTag(metadatakey);
        if (namedObjects != null)
            for (NamedObject namedObject : namedObjects) {
                if (MiscUtils.trimEnd(namedObject.getName()).contentEquals(name))
                    return namedObject;
            }
        return null;
    }

    public NamedObject getDatabaseObjectFromTypeAndName(int type, String name) {
        return getDatabaseObjectFromMetaTagAndName(NamedObject.META_TYPES[type], name);
    }

    public boolean isPauseLoadingTreeForSearch() {
        return pauseLoadingTreeForSearch;
    }

    public void setPauseLoadingTreeForSearch(boolean pauseLoadingTreeForSearch) {
        this.pauseLoadingTreeForSearch = pauseLoadingTreeForSearch;
    }

    public static String bundleString(String key) {
        return Bundles.get(DefaultDatabaseHost.class, key);
    }

}
