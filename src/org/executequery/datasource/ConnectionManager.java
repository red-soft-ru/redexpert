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

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.SystemProperties;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import javax.swing.tree.TreeNode;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Manages all data source connections across multiple
 * sources and associated connection pools.
 *
 * @author Takis Diakoumis
 */
public final class ConnectionManager {

    private static Map<DatabaseConnection, ConnectionPool> connectionPools = Collections.synchronizedMap(new HashMap<DatabaseConnection, ConnectionPool>());
    /**
     * Creates a stored data source for the specified database
     * connection properties object.
     *
     * @param the database connection properties object
     */
    public static synchronized void createDataSource(DatabaseConnection databaseConnection) throws IllegalArgumentException {

        // check the connection has a driver
        if (databaseConnection.getJDBCDriver() == null) {

            long driverId = databaseConnection.getDriverId();
            DatabaseDriver driver = driverById(driverId);

            if (driver != null) {

                databaseConnection.setJDBCDriver(driver);

            } else {

                throw new DataSourceException("No JDBC driver specified");
            }

        }

        Log.info("Initialising data source for " + databaseConnection.getName());
        ConnectionPool pool = new ConnectionPoolImpl(databaseConnection);
        pool.setMinimumConnections(SystemProperties.getIntProperty("user", "connection.initialcount"));
        pool.setInitialConnections(SystemProperties.getIntProperty("user", "connection.initialcount"));
        connectionPools.put(databaseConnection, pool);
        databaseConnection.setConnected(true);
        DatabaseObjectNode hostNode = ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getHostNode(databaseConnection);
        loadTree(hostNode);
        DatabaseHost host = (DatabaseHost) hostNode.getDatabaseObject();
        try {
            while (host.countFinishedMetaTags() < hostNode.getChildCount()) {

                Thread.sleep(100);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.info("Data source " + databaseConnection.getName() + " initialized.");
    }


    public static void loadTree(DatabaseObjectNode root) {
        root.populateChildren();
        Enumeration<TreeNode> nodes = root.children();
        while (nodes.hasMoreElements()) {
            DatabaseObjectNode node = (DatabaseObjectNode) nodes.nextElement();
            if (node.isHostNode() || node.getType() == NamedObject.META_TAG) {
                SwingWorker sw = new SwingWorker() {
                    @Override
                    public Object construct() {
                        loadTree(node);
                        return null;
                    }

                    @Override
                    public void finished() {
                        if (node.getType() == NamedObject.META_TAG) {
                            ((DefaultDatabaseMetaTag) node.getDatabaseObject()).getHost().incCountFinishedMetaTags();
                        }
                    }
                };
                sw.start();
            }
        }

    }

    /**
     * Returns a connection from the pool of the specified type.
     *
     * @param the stored database connection properties object
     * @return the connection itself
     */
    public static Connection getConnection(DatabaseConnection databaseConnection) {

        if (databaseConnection == null) {

            return null;
        }

        synchronized (databaseConnection) {

            if (connectionPools == null || !connectionPools.containsKey(databaseConnection)) {

                createDataSource(databaseConnection);
            }

            ConnectionPool pool = connectionPools.get(databaseConnection);
            Connection connection = pool.getConnection();

            return connection;
        }

    }

    public static Connection getTemporaryConnection(DatabaseConnection databaseConnection) {

        if (databaseConnection == null) {

            return null;
        }

        synchronized (databaseConnection) {

            if (connectionPools == null || !connectionPools.containsKey(databaseConnection)) {

                createDataSource(databaseConnection);
            }

            ConnectionPool pool = connectionPools.get(databaseConnection);
            DataSource dataSource = getDataSource(databaseConnection);
            try {
                return new PooledConnection(dataSource.getConnection(), databaseConnection);
            } catch (SQLException e) {
                Log.error("Error get connection", e);
                return pool.getConnection();
            }
        }

    }

    public static String getURL(DatabaseConnection databaseConnection) {
        if (databaseConnection == null) {

            return null;
        }
        return SimpleDataSource.generateUrl(databaseConnection, SimpleDataSource.buildAdvancedProperties(databaseConnection));
    }

    public static Connection realConnection(DatabaseMetaData dmd) throws SQLException {
        if (dmd instanceof PooledDatabaseMetaData)
            return ((PooledDatabaseMetaData) dmd).getRealConnection();
        else return dmd.getConnection();
    }

    /**
     * Closes all connections and removes the pool of the specified type.
     *
     * @param the stored database connection properties object
     */
    public static synchronized void closeConnection(DatabaseConnection databaseConnection) {

        if (connectionPools.containsKey(databaseConnection)) {

            Log.info("Disconnecting from data source " + databaseConnection.getName());

            ConnectionPool pool = connectionPools.get(databaseConnection);
            SimpleDataSource dataSource = (SimpleDataSource) pool.getDataSource();
            try {
                dataSource.close();
            } catch (ResourceException e) {
                e.printStackTrace();
            }
            pool.close();

            connectionPools.remove(databaseConnection);
            databaseConnection.setConnected(false);
        }
    }

    /**
     * Closes all connections and removes the pool of the specified type.
     *
     * @param the stored database connection properties object
     */
    public static void close() {

        if (connectionPools == null || connectionPools.isEmpty()) {

            return;
        }

        // iterate and close all the pools
        for (Iterator<DatabaseConnection> i = connectionPools.keySet().iterator(); i.hasNext(); ) {

            ConnectionPool pool = connectionPools.get(i.next());
            pool.close();
        }
        connectionPools.clear();
    }

    /**
     * Retrieves the data source objetc of the specified connection.
     *
     * @return the data source object
     */
    public static DataSource getDataSource(DatabaseConnection databaseConnection) {
        if (connectionPools == null || !connectionPools.containsKey(databaseConnection)) {

            return null;
        }
        return connectionPools.get(databaseConnection).getDataSource();
    }

    /**
     * Sets the transaction isolation level to that specified
     * for <i>all</i> connections in the pool of the specified connection.
     *
     * @param the isolation level
     * @see java.sql.Connection for possible values
     */
    public static void setTransactionIsolationLevel(DatabaseConnection databaseConnection, int isolationLevel) {

        if (connectionPools == null || connectionPools.containsKey(databaseConnection)) {

            ConnectionPool pool = connectionPools.get(databaseConnection);
            pool.setTransactionIsolationLevel(isolationLevel);
        }

    }

    /**
     * Returns a collection of database connection property
     * objects that are active (connected).
     *
     * @return a collection of active connections
     */
    public static Vector<DatabaseConnection> getActiveConnections() {
        if (connectionPools == null || connectionPools.isEmpty()) {
            return new Vector<DatabaseConnection>(0);
        }
        Vector<DatabaseConnection> connections =
                new Vector<DatabaseConnection>(connectionPools.size());
        for (Iterator<DatabaseConnection> i =
             connectionPools.keySet().iterator(); i.hasNext(); ) {
            connections.add(i.next());
        }
        return connections;
    }

    /**
     * Returns the open connection count for the specified connection.
     *
     * @param dc - the connection to be polled
     */
    public static int getOpenConnectionCount(DatabaseConnection dc) {
        ConnectionPool pool = connectionPools.get(dc);
        if (pool != null) {
            return pool.getSize();
        }
        return 0;
    }

    public static boolean hasConnections() {

        return getActiveConnectionPoolCount() > 0;
    }

    /**
     * Returns the number of pools currently active.
     *
     * @return number of active pools
     */
    public static int getActiveConnectionPoolCount() {
        if (connectionPools == null) {
            return 0;
        }
        return connectionPools.size();
    }

    /**
     * Closes the connection completely. The specified connection
     * is not returned to the pool.
     *
     * @param the connection be closed
     */
    public static void close(DatabaseConnection databaseConnection, Connection connection) {

        if (connectionPools == null || connectionPools.isEmpty()) {

            return;
        }

        if (connectionPools.containsKey(databaseConnection)) {

            ConnectionPool pool = connectionPools.get(databaseConnection);
            pool.close(connection);
        }

    }

    /**
     * Returns whether the specified connection [driver] supports transactions.
     *
     * @param databaseConnection the connection to be polled
     * @return true | false
     */
    public static boolean isTransactionSupported(
            DatabaseConnection databaseConnection) {
        if (connectionPools.containsKey(databaseConnection)) {
            ConnectionPool pool = connectionPools.get(databaseConnection);
            return pool.isTransactionSupported();
        }
        return false;
    }

    private static final int MAX_CONNECTION_USE_COUNT = 50;

    /**
     * Retrieves the maximum use count for each open connection
     * before being closed.
     *
     * @return the max connection use count
     */
    public static int getMaxUseCount() {

        return MAX_CONNECTION_USE_COUNT;
    }

    private static DatabaseDriver driverById(long driverId) {

        return ((DatabaseDriverRepository) RepositoryCache.load(
                DatabaseDriverRepository.REPOSITORY_ID)).findById(driverId);
    }

    private ConnectionManager() {
    }
}







