/*
 * ConnectionPoolImpl.java
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

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SystemProperties;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ConnectionPoolImpl extends AbstractConnectionPool implements PooledConnectionListener {

    private int maximumConnections = MAX_POOL_SIZE;

    private int minimumConnections = MIN_POOL_SIZE;

    private int initialConnections = INITIAL_POOL_SIZE;

    private final List<PooledConnection> openConnections = Collections.synchronizedList(new ArrayList<PooledConnection>());

    private final List<PooledConnection> activeConnections = Collections.synchronizedList(new ArrayList<PooledConnection>());

    private final DatabaseConnection databaseConnection;

    private int defaultTxIsolation = -1;

    private boolean supportsTransactions;

    private DataSource dataSource;

    private SshTunnel sshTunnel;

    public ConnectionPoolImpl(DatabaseConnection databaseConnection) {

        this.databaseConnection = databaseConnection;

        if (Log.isDebugEnabled()) {

            Log.debug("Creating new pool for connection " + databaseConnection.getName());
        }

    }

    public DatabaseConnection getDatabaseConnection() {

        return databaseConnection;
    }

    public void connectionClosed(PooledConnection pooledConnection) {

        if (Log.isDebugEnabled()) {

            Log.debug("Removing connection " + pooledConnection.getId()
                    + " from active connections list");
        }

        activeConnections.remove(pooledConnection);
        reduceCapacity(minimumConnections);
    }

    public void close(Connection connection) {

        if (connection != null) {

            activeConnections.remove(connection);

            PooledConnection pooledConnection = (PooledConnection) connection;
            pooledConnection.destroy();

            openConnections.remove(pooledConnection);
        }

        ensureCapacity(minimumConnections);
    }

    public synchronized void close() {

        if (Log.isDebugEnabled()) {

            Log.debug("Closing connection pool for connection " + databaseConnection.getName());
        }

        for (Connection connection : openConnections) {

            PooledConnection pooledConnection = (PooledConnection) connection;
            pooledConnection.destroy();
        }

        destroySshTunnel();
        activeConnections.clear();
        openConnections.clear();
    }

    public synchronized Connection getConnection() {

        int size = openConnections.size();
        if (Log.isTraceEnabled()) {

            Log.trace("Retrieving new connection from the pool with current size: [ "
                    + size + " ] from maximum pool capacity [ " + maximumConnections + " ]");
        }

        if (databaseConnection.isSshTunnel() && sshTunnel == null) {

            createSshTunnel();
        }

        if (size < minimumConnections) {

            ensureCapacity(minimumConnections);
        }

        PooledConnection connection = getNextOpenAvailable();

        if (connection != null) {

            try {
                if (connection.isClosed()) {

                    close(connection);
                    return getConnection();
                }
            } catch (SQLException e) {
            }

            connection.setInUse(true);
            activeConnections.add(connection);

        } else if (size < maximumConnections) {
            boolean usePool = SystemProperties.getBooleanProperty("user", "connection.usepool");
            if (!usePool) {
                if (activeConnections.size() > 0)
                    return activeConnections.get(0);
                else {
                    createConnection();
                    return getConnection();
                }
            } else {
                createConnection();
                return getConnection();
            }

        } else {

            throw new DataSourceException("Maximum open connection count exceeded");
        }

        if (Log.isDebugEnabled()) {

            Log.debug("Retrieving connection " + connection.getId());
        }

        return connection;
    }

    private void destroySshTunnel() {

        if (sshTunnel != null) {

            sshTunnel.disconnect(databaseConnection);
            sshTunnel = null;
        }
    }

    private void createSshTunnel() {

        sshTunnel = new JschSshTunnel();
        sshTunnel.connect(databaseConnection);
    }

    private void ensureCapacity(int capacity) {

        if (Log.isDebugEnabled()) {

            Log.debug("Ensuring pool capacity " + capacity);
        }

        while (openConnections.size() < capacity) {

            createConnection();
        }

    }

    private void reduceCapacity(int capacity) {

        if (Log.isDebugEnabled()) {

            Log.debug("Reducing pool capacity " + capacity);
        }

        while (openConnections.size() > capacity) {

            PooledConnection connection = getNextOpenAvailable();
            if (connection != null) {

                close(connection);

            } else {

                break;
            }

        }

    }

    private PooledConnection createConnection() {

        PooledConnection connection = null;

        try {

            if (dataSource == null) {

                DatabaseConnection _databaseConnection = databaseConnection;
                if (databaseConnection.isSshTunnel()) {

                    if (sshTunnel == null) {

                        createSshTunnel();
                    }

                    _databaseConnection = databaseConnection.copy();
                    _databaseConnection.setHost("localhost");
                    _databaseConnection.setPort(String.valueOf(sshTunnel.getTunnelPort()));
                }

                dataSource = new SimpleDataSource(_databaseConnection);
            }

            Connection realConnection = dataSource.getConnection();
            if (realConnection == null) {

                destroySshTunnel();
                throw new DataSourceException(
                        "A connection to the database could not be " +
                                "established.\nPlease ensure that the details " +
                                "are correct and the supplied host is available.");
            }

            if (defaultTxIsolation == -1) {

                configureTransactionIsolationLevel(realConnection);
            }

            int transactionIsolation = databaseConnection.getTransactionIsolation();
            if (transactionIsolation != -1) {

                try {

                    realConnection.setTransactionIsolation(databaseConnection.getTransactionIsolation());

                } catch (SQLException e) {

                    Log.warning("Error setting transaction isolation level: " + e.getMessage());
                }
            }

            connection = new PooledConnection(realConnection, databaseConnection);
            connection.addPooledConnectionListener(this);

            openConnections.add(connection);

            if (Log.isDebugEnabled()) {

                Log.debug("Added new connection to the pool - " + connection.getId());
            }

        } catch (SQLException e) {

            destroySshTunnel();
            rethrowAsDataSourceException(e);
        }

        return connection;
    }

    private void configureTransactionIsolationLevel(Connection connection) {

        try {

            defaultTxIsolation = connection.getTransactionIsolation();
            supportsTransactions = connection.getMetaData().supportsTransactions();

        } catch (SQLException e) {

            rethrowAsDataSourceException(e);
        }

    }

    private PooledConnection getNextOpenAvailable() {

        for (PooledConnection pooledConnection : openConnections) {

            if (pooledConnection.isAvailable()) {

                return pooledConnection;
            }

        }

        return null;
    }

    public DataSource getDataSource() {

        return dataSource;
    }

    public int getMaximumConnections() {

        return maximumConnections;
    }

    public int getMaximumUseCount() {

        return 0;
    }

    public int getMinimumConnections() {

        return minimumConnections;
    }

    public int getPoolActiveSize() {

        return activeConnections.size();
    }

    public int getSize() {

        return openConnections.size();
    }

    public boolean isTransactionSupported() {

        if (defaultTxIsolation == -1) {

            Connection connection = getConnection();
            try {

                configureTransactionIsolationLevel(connection);

            } finally {

                close(connection);
            }

        }

        return supportsTransactions;
    }

    public void setDataSource(DataSource dataSource) {

        this.dataSource = dataSource;
    }

    public int getInitialConnections() {

        return initialConnections;
    }

    public void setInitialConnections(int initialConnections) {

        if (initialConnections < 1) {

            throw new IllegalArgumentException("Initial connection count must be at least 1");
        }

        this.initialConnections = initialConnections;
    }

    public void setMaximumConnections(int maximumConnections) {

        if (maximumConnections < 1) {

            throw new IllegalArgumentException("Maximum connection count must be at least 1");
        }

        this.maximumConnections = maximumConnections;
    }

    public void setMaximumUseCount(int maximumUseCount) {


    }

    public void setMinimumConnections(int minimumConnections) {

        if (minimumConnections < 1) {

            throw new IllegalArgumentException("Minimum connection count must be at least 1");
        }

        this.minimumConnections = minimumConnections;
        ensureCapacity(minimumConnections);
    }

    public void setTransactionIsolationLevel(int isolationLevel) {

        if (!isTransactionSupported()) {

            return;
        }

        int isolationLevelToApply = isolationLevel;
        if (isolationLevelToApply == -1) {

            isolationLevelToApply = defaultTxIsolation;
        }

        Log.debug("Setting transaction isolation level to open connections as [ "
                + nameForTransactionIsolationLevel(isolationLevelToApply) + " ]");

        try {

            synchronized (openConnections) {

                for (Connection connection : openConnections) {

                    if (!connection.isClosed()) {

                        connection.setTransactionIsolation(isolationLevelToApply);
                    }

                }

            }

        } catch (SQLException e) {

            throw new DataSourceException(e);
        }

    }

    private String nameForTransactionIsolationLevel(int isolationLevel) {

        for (Field field : Connection.class.getFields()) {

            String name = field.getName();
            if (StringUtils.startsWith(name, "TRANSACTION_")) {

                try {
                    if (isolationLevel == field.getInt(null)) {

                        return name;
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                }

            }

        }

        return String.valueOf(isolationLevel);
    }

}






