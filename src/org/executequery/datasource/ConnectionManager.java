/*
 * ConnectionManager.java
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

package org.executequery.datasource;

import biz.redsoft.ITPB;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.ConnectionBuilder;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SystemProperties;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages all data source connections across multiple
 * sources and associated connection pools.
 *
 * @author Takis Diakoumis
 */
public final class ConnectionManager {

    private static final int MAX_CONNECTION_USE_COUNT = 50;
    private static final Map<String, ConnectionData> connections = Collections.synchronizedMap(new HashMap<>());

    /// Private constructor to prevent installation.
    private ConnectionManager() {
    }

    // --- establish connection ---

    public static synchronized void createDataSource(DatabaseConnection dc, ConnectionBuilder connectionBuilder, boolean isAutoConnect)
            throws IllegalArgumentException {

        dc.setAutoConnect(isAutoConnect);
        checkConnectionDriver(dc);

        Log.info("Initialising data source for " + dc.getName());
        long startTime = System.currentTimeMillis();

        initConnectionPool(dc);
        loadConnectionTree(dc, connectionBuilder);
        loadCharset(dc);

        if (connectionBuilder != null && connectionBuilder.isCancelled())
            dc.setConnected(false);

        if (dc.isConnected()) {
            Log.info("Connection time = " + (System.currentTimeMillis() - startTime) + "ms");
            Log.info("Data source " + dc.getName() + " initialized.");
        }
    }

    private static void checkConnectionDriver(DatabaseConnection dc) throws DataSourceException {

        if (dc.getJDBCDriver() != null)
            return;

        Repository repo = RepositoryCache.load(DatabaseDriverRepository.REPOSITORY_ID);
        if (repo instanceof DatabaseDriverRepository) {
            DatabaseDriver driver = ((DatabaseDriverRepository) repo).findById(dc.getDriverId());
            if (driver != null) {
                dc.setJDBCDriver(driver);
                return;
            }
        }

        throw new DataSourceException("No JDBC driver specified");
    }

    private static void initConnectionPool(DatabaseConnection dc) {

        ConnectionPool pool = new ConnectionPoolImpl(dc);
        pool.setMinimumConnections(SystemProperties.getIntProperty("user", "connection.initialcount"));
        pool.setInitialConnections(SystemProperties.getIntProperty("user", "connection.initialcount"));

        connections.put(dc.getId(), new ConnectionData(dc, pool));
        dc.setConnected(true);
    }

    private static void loadConnectionTree(DatabaseConnection dc, ConnectionBuilder connectionBuilder) {

        JPanel tabComponent = GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        if (tabComponent instanceof ConnectionsTreePanel) {
            ConnectionsTreePanel treePanel = (ConnectionsTreePanel) tabComponent;

            DatabaseObjectNode hostNode = treePanel.getHostNode(dc);
            loadTree(hostNode, connectionBuilder);
            treePanel.getTree().nodeChanged(hostNode);
        }
    }

    private static void loadTree(DatabaseObjectNode root, ConnectionBuilder connectionBuilder) {
        try {
            root.populateChildren();

            Enumeration<TreeNode> nodes = root.children();
            while (nodes.hasMoreElements()) {
                DatabaseObjectNode node = (DatabaseObjectNode) nodes.nextElement();
                if (node.isHostNode() || node.getType() == NamedObject.META_TAG)
                    loadTree(node, connectionBuilder);
            }

        } catch (Exception e) {
            if (!connectionBuilder.isCancelled())
                Log.error(e.getMessage(), e);
        }
    }

    private static void loadCharset(DatabaseConnection dc) {

        DefaultStatementExecutor executor = new DefaultStatementExecutor(dc);
        String query = "SELECT RDB$CHARACTER_SET_NAME FROM RDB$DATABASE";

        try {
            ResultSet rs = executor.getResultSet(query).getResultSet();
            if (rs != null && rs.next()) {
                String charset = rs.getString(1);
                if (charset != null && !charset.trim().isEmpty())
                    dc.setDBCharset(charset.trim());
            }

        } catch (Exception e) {
            Log.error(e.getMessage(), e);

        } finally {
            executor.releaseResources();
        }
    }

    // --- close connection ---

    /// Closes all connections and removes the pool of the specified type.
    public static synchronized void closeConnection(DatabaseConnection dc) {

        if (dc == null || !connections.containsKey(dc.getId()))
            return;

        Log.info("Disconnecting from data source " + dc.getName());

        String dcId = dc.getId();
        ConnectionPool pool = connections.get(dcId).getConnectionPool();
        try {
            DataSource dataSource = pool.getDataSource();
            if (dataSource instanceof SimpleDataSource) {
                SimpleDataSource simpleDataSource = (SimpleDataSource) pool.getDataSource();
                simpleDataSource.close();
            }

        } catch (ResourceException e) {
            Log.error(e.getMessage(), e);
        }
        pool.close();

        connections.remove(dcId);
        dc.setConnected(false);

        ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(dc).close();
    }

    /// Closes all connections and removes the pool of the specified type.
    public static void close() {
        if (!connections.isEmpty()) {
            connections.values().forEach(ConnectionData::close);
            connections.clear();
        }
    }

    /**
     * Closes the connection completely.<br>
     * The specified connection is not returned to the pool.
     */
    public static void close(DatabaseConnection dc, Connection connection) {
        if (dc != null && connection != null) {
            String dcId = dc.getId();
            if (connections.containsKey(dcId))
                connections.get(dcId).close(connection);
        }
    }

    // --- get connection ---

    /**
     * Returns a collection of database connection property
     * objects that are active (connected).
     *
     * @return a collection of active connections
     */
    public static List<DatabaseConnection> getActiveConnections() {
        return connections.values().stream().map(ConnectionData::getDatabaseConnection).collect(Collectors.toList());
    }

    /**
     * Returns a collection of database connection property
     * objects that are active (connected).
     *
     * @return a collection of active connections
     */
    public static List<DatabaseConnection> getAllConnections() {

        ArrayList<DatabaseConnection> connections = new ArrayList<>();
        Repository repository = RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
        if (repository instanceof DatabaseConnectionRepository)
            connections.addAll(((DatabaseConnectionRepository) repository).findAll());

        return connections;
    }

    /**
     * Returns a connection from the pool of the specified type.
     *
     * @param dc stored database connection properties object
     * @return the connection itself
     */
    @SuppressWarnings({"java:S2445", "SynchronizationOnLocalVariableOrMethodParameter"})
    public static Connection getConnection(DatabaseConnection dc) {

        if (dc == null)
            return null;

        String dcId = dc.getId();
        synchronized (dc) {
            // ignoring SynchronizationOnLocalVariableOrMethodParameter warning
            // to make available parallel method invocation for different DatabaseConnection objects

            if (!connections.containsKey(dcId))
                createDataSource(dc, null, false);

            return connections.get(dcId).getConnection();
        }
    }

    public static Connection getTemporaryConnection(DatabaseConnection dc) {
        return getTemporaryConnection(dc, null);
    }

    @SuppressWarnings({"java:S2445", "SynchronizationOnLocalVariableOrMethodParameter"})
    public static Connection getTemporaryConnection(DatabaseConnection dc, ITPB tpb) {

        if (dc == null)
            return null;

        String dcId = dc.getId();
        synchronized (dc) {
            // ignoring SynchronizationOnLocalVariableOrMethodParameter warning
            // to make available parallel method invocation for different DatabaseConnection objects

            if (!connections.containsKey(dcId))
                createDataSource(dc, null, false);

            try {
                Connection connection = null;
                DataSource dataSource = getDataSource(dc);

                if (dataSource instanceof SimpleDataSource)
                    connection = ((SimpleDataSource) dataSource).getConnection(tpb);
                else if (dataSource != null)
                    connection = dataSource.getConnection();

                return connection != null ?
                        new PooledConnection(connection, dc) :
                        connections.get(dcId).getConnection();

            } catch (SQLException e) {
                Log.error("Error get connection", e);
                return connections.get(dcId).getConnection();
            }
        }
    }

    public static Connection realConnection(DatabaseMetaData dmd) throws SQLException {
        if (dmd instanceof PooledDatabaseMetaData)
            return ((PooledDatabaseMetaData) dmd).getRealConnection();
        return dmd.getConnection();
    }

    // ---

    public static void setTPBtoConnection(DatabaseConnection dc, Connection connection, ITPB tpb) {

        if (dc == null || connection == null)
            return;

        DataSource dataSource = getDataSource(dc);
        if (dataSource instanceof SimpleDataSource) {
            try {
                SimpleDataSource simpleDataSource = (SimpleDataSource) dataSource;
                simpleDataSource.setTPBtoConnection(connection, tpb);

            } catch (SQLException e) {
                Log.error(e.getMessage(), e);
            }
        }
    }

    public static long getIDTransaction(DatabaseConnection dc, Connection connection) {

        if (dc == null || connection == null)
            return -1;

        DataSource dataSource = getDataSource(dc);
        if (dataSource instanceof SimpleDataSource) {
            try {
                SimpleDataSource simpleDataSource = (SimpleDataSource) dataSource;
                return simpleDataSource.getIDTransaction(connection);

            } catch (Exception e) {
                Log.error(e.getMessage(), e);
            }
        }

        return -1;
    }

    public static long getCurrentSnapshotTransaction(DatabaseConnection dc, Connection connection) {

        if (dc == null || connection == null || dc.getMajorServerVersion() < 4)
            return -1;

        DataSource dataSource = getDataSource(dc);
        if (dataSource instanceof SimpleDataSource) {
            try {
                SimpleDataSource simpleDataSource = (SimpleDataSource) dataSource;
                return simpleDataSource.getSnapshotTransaction(connection);

            } catch (Exception e) {
                Log.error(e.getMessage(), e);
            }
        }

        return -1;
    }

    public static ClassLoader getClassLoaderForDatabaseConnection(DatabaseConnection dc) {

        if (dc == null)
            return null;

        DataSource dataSource = getDataSource(dc);
        if (dataSource instanceof SimpleDataSource) {
            SimpleDataSource simpleDataSource = (SimpleDataSource) dataSource;
            return simpleDataSource.getClassLoaderFromPlugin();
        }

        return null;
    }

    /**
     * Retrieves the data source object of the specified connection.
     *
     * @return the data source object
     */
    public static DataSource getDataSource(DatabaseConnection dc) {

        if (dc == null)
            return null;

        String dcId = dc.getId();
        return connections.containsKey(dcId) ? connections.get(dcId).getDataSource() : null;
    }

    /**
     * Sets the transaction isolation level to that specified
     * for all connections in the pool of the specified connection.
     *
     * @see java.sql.Connection for possible values
     */
    public static void setTransactionIsolationLevel(DatabaseConnection dc, int isolationLevel) {
        if (dc != null) {
            String dcId = dc.getId();
            if (connections.containsKey(dcId))
                connections.get(dcId).setTransactionIsolationLevel(isolationLevel);
        }
    }

    /**
     * Returns whether the specified connection [driver] supports transactions.
     *
     * @param dc the connection to be polled
     * @return true | false
     */
    public static boolean isTransactionSupported(DatabaseConnection dc) {

        if (dc == null)
            return false;

        String dcId = dc.getId();
        return connections.containsKey(dcId) && connections.get(dcId).isTransactionSupported();
    }

    /**
     * Returns the number of pools currently active.
     *
     * @return number of active pools
     */
    public static int getActiveConnectionPoolCount() {
        return connections.size();
    }

    public static boolean noActiveConnections() {
        return getActiveConnectionPoolCount() < 1;
    }

    /**
     * Retrieves the maximum use count for each open connection
     * before being closed.
     *
     * @return the max connection use count
     */
    public static int getMaxUseCount() {
        return MAX_CONNECTION_USE_COUNT;
    }

    // ---

    private static class ConnectionData {
        private final DatabaseConnection databaseConnection;
        private final ConnectionPool connectionPool;

        public ConnectionData(DatabaseConnection databaseConnection, ConnectionPool connectionPool) {
            this.databaseConnection = databaseConnection;
            this.connectionPool = connectionPool;
        }

        public void setTransactionIsolationLevel(int isolationLevel) {
            connectionPool.setTransactionIsolationLevel(isolationLevel);
        }

        public void close() {
            connectionPool.close();
        }

        public void close(Connection connection) {
            connectionPool.close(connection);
        }

        public DataSource getDataSource() {
            return connectionPool.getDataSource();
        }

        public DatabaseConnection getDatabaseConnection() {
            return databaseConnection;
        }

        public ConnectionPool getConnectionPool() {
            return connectionPool;
        }

        public Connection getConnection() {
            return connectionPool.getConnection();
        }

        public boolean isTransactionSupported() {
            return connectionPool.isTransactionSupported();
        }

    } // ConnectionData class

}
