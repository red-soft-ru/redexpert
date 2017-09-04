/*
 * C3poConnectionPool.java
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

import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.c3p0.PoolBackedDataSource;
import org.executequery.databasemediators.DatabaseConnection;
import org.underworldlabs.jdbc.DataSourceException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class C3poConnectionPool extends AbstractConnectionPool {

    private static final String ACQUIRE_INCREMENT_KEY = "acquireIncrement";

    private static final String INITIAL_POOL_SIZE_KEY = "initialPoolSize";

    private static final String MAX_POOL_SIZE_KEY = "maxPoolSize";

    private static final String MIN_POOL_SIZE_KEY = "minPoolSize";

    private static final int ACQUIRE_INCREMENT = 1;

    private final List<Connection> activeConnections = new Vector<Connection>();
    
    private final DatabaseConnection databaseConnection;

    private final Properties c3poPoolProperties;

    private int defaultTxIsolation = -1;

    private DataSource dataSource;
    
    private PoolBackedDataSource pooledDataSource;
    
    public C3poConnectionPool(DatabaseConnection databaseConnection) {

        this.databaseConnection = databaseConnection;
        
        c3poPoolProperties = new Properties();
        setMinimumConnections(MIN_POOL_SIZE);
        setMaximumConnections(MAX_POOL_SIZE);
        setInitialConnections(INITIAL_POOL_SIZE);
        c3poPoolProperties.put(ACQUIRE_INCREMENT_KEY, asString(ACQUIRE_INCREMENT));
    }

    public DatabaseConnection getDatabaseConnection() {
     
        return databaseConnection;
    }
    
    public void close(Connection connection) {

        try {

            activeConnections.remove(connection);
            connection.close();
            
        } catch (SQLException e) {
            
            rethrowAsDataSourceException(e);
        }

    }

    public void close() {

        pooledDataSource.close();
    }

    public Connection getConnection() {

        Connection connection = null;
        
        try {

            if (pooledDataSource == null) {
                
                initialiseDataSource(new SimpleDataSource(databaseConnection));
            }

            connection = pooledDataSource.getConnection();

            if (defaultTxIsolation == -1) {
             
                defaultTxIsolation = connection.getTransactionIsolation();
            }
            
            int transactionIsolation = databaseConnection.getTransactionIsolation();
            if (transactionIsolation != -1) {
            
                connection.setTransactionIsolation(databaseConnection.getTransactionIsolation());
            }

            activeConnections.add(connection);
            
        } catch (SQLException e) {

            rethrowAsDataSourceException(e);
        }

        return connection;
    }

    private void initialiseDataSource(DataSource dataSource) throws SQLException {

        this.dataSource = dataSource;
        pooledDataSource = (PoolBackedDataSource) DataSources.pooledDataSource(
                dataSource, c3poPoolProperties);
    }

    public DataSource getDataSource() {

        return dataSource;
    }

    public int getMaximumConnections() {

        return ((Integer) c3poPoolProperties.get(MAX_POOL_SIZE_KEY)).intValue();
    }

    public int getMaximumUseCount() {

        return 0;
    }

    public int getMinimumConnections() {
        
        return ((Integer) c3poPoolProperties.get(MIN_POOL_SIZE_KEY)).intValue();
    }

    public int getPoolActiveSize() {

        return activeConnections.size();
    }

    public int getSize() {

        int size = 0;
        try {
        
            size = pooledDataSource.getNumConnectionsDefaultUser();

        } catch (SQLException e) {

            rethrowAsDataSourceException(e);
        }
        
        return size;
    }

    public boolean isTransactionSupported() {

        return false;
    }

    public void setDataSource(DataSource dataSource) {

        try {

            initialiseDataSource(dataSource);

        } catch (SQLException e) {

            rethrowAsDataSourceException(e);
        }
    }

    public int getInitialConnections() {
        
        return ((Integer) c3poPoolProperties.get(INITIAL_POOL_SIZE_KEY)).intValue();
    }
    
    public void setInitialConnections(int initialConnections) {

        if (initialConnections < 1) {
            
            throw new IllegalArgumentException("Initial connection count must be at least 1");
        }

        c3poPoolProperties.put(INITIAL_POOL_SIZE_KEY, asString(initialConnections));
    }
    
    public void setMaximumConnections(int maximumConnections) {

        if (maximumConnections < 1) {
            
            throw new IllegalArgumentException("Maximum connection count must be at least 1");
        }

        c3poPoolProperties.put(MAX_POOL_SIZE_KEY, asString(maximumConnections));
    }

    public void setMaximumUseCount(int maximumUseCount) {


    }

    public void setMinimumConnections(int minimumConnections) {

        if (minimumConnections < 1) {
            
            throw new IllegalArgumentException("Minimum connection count must be at least 1");
        }
        
        c3poPoolProperties.put(MIN_POOL_SIZE_KEY, asString(minimumConnections));
    }

    public void setTransactionIsolationLevel(int isolationLevel) {

        if (!isTransactionSupported()) {

            return;
        }
        
//        if (isolationLevel == -1) {
//         
//            isolationLevel = defaultTxIsolation;
//        }

        try {
        
            for (Connection connection : activeConnections) {

                if (!connection.isClosed()) {
                
                    connection.setTransactionIsolation(databaseConnection.getTransactionIsolation());
                }

            }

        } catch (SQLException e) {
            
            throw new DataSourceException(e);
        }
        
    }

    private Object asString(int value) {

        return String.valueOf(value);
    }

}






