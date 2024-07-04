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
import org.executequery.databaseobjects.T;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.DatabaseDataSource;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.browser.ColumnData;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.*;
import java.util.*;

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
    private final boolean keepAlive;

    /**
     * the database connection object associated with this instance
     */
    private DatabaseConnection databaseConnection;

    /**
     * the connection 'container'
     */
    private final Map<DatabaseConnection, Connection> connections;

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
     * passed boolean value is 'false'. Otherwise, the
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
        if (this.databaseConnection != dc) {
            connection = null;
            this.databaseConnection = dc;
        }
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

                connection = ConnectionManager.getTemporaryConnection(databaseConnection);
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
        connection = ConnectionManager.getTemporaryConnection(databaseConnection);
        connections.put(databaseConnection, connection);
    }

    /**
     * Retrieves the database product name from
     * the connection's metadata.
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
     * Closes the open connection and releases
     * all resources attached to it.
     */
    public void closeConnection() {
        try {
            for (DatabaseConnection dc : connections.keySet()) {
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
                if (!type.startsWith(underscore) && !type.equalsIgnoreCase(T.ARRAY)) {
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
                if (!stype.startsWith(underscore) && !stype.equalsIgnoreCase(T.ARRAY)) {
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
     * <p>Retrieves the column names for the specified
     * database table as a <code>Vector</code> object.
     *
     * @param table database table name
     * @return the column names <code>Vector</code>
     */
    public Vector<String> getColumnNamesVector(String table)
            throws DataSourceException {
        ResultSet rs = null;
        try {
            ensureConnection();

            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getColumns(null, null, table, null);

            Vector<String> v = new Vector<String>();
            while (rs.next()) {
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
     * <p>Retrieves the specified database table names
     * within a <code>Vector</code> object.
     *
     * @return the table names
     */
    public Vector<String> getAllTables() throws DataSourceException {

        ResultSet rs = null;
        try {
            ensureConnection();

            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getTables(null, null, null, new String[]{"TABLE"});

            Vector<String> tablesVector = new Vector<>();
            while (rs.next())
                tablesVector.add(rs.getString(3));
            return tablesVector;

        } catch (SQLException e) {
            throw new DataSourceException(e);

        } finally {
            releaseResources(rs);
        }
    }

    /**
     * <p>Retrieves the complete column metadata
     * for the specified database table.
     * <p>Each column and associated data is stored within
     * <code>ColumnData</code> objects and added to the
     * <code>Vector</code> object to be returned.
     *
     * @param name database table name
     * @return the table column meta data
     */
    public Vector<ColumnData> getColumnMetaDataVector(String name) throws DataSourceException {

        ResultSet rs = null;
        try {
            ensureConnection();

            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getColumns(null, null, name, null);

            Vector v = new Vector();
            while (rs.next()) {
                ColumnData cd = new ColumnData(databaseConnection);
                cd.setColumnName(rs.getString(4));
                cd.setSQLType(rs.getInt(5));
                cd.setTypeName(rs.getString(6));
                cd.setSize(rs.getInt(7));
                cd.setNotNull(rs.getInt(11) == 0);
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

    public String[] getTables(String metaType)
            throws DataSourceException {

        ResultSet rs = null;
        try {
            ensureConnection();
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getTables(null, null, null, new String[]{metaType});

            if (rs != null) { // some odd null rs behaviour on some drivers
                ArrayList list = new ArrayList();
                while (rs.next()) {
                    list.add(rs.getString(3));
                }
                return (String[]) list.toArray(new String[0]);
            } else {
                return new String[0];
            }

        } catch (SQLException e) {
            throw new DataSourceException(e);
        } finally {
            releaseResources(rs);
        }

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
     * @return the username
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
