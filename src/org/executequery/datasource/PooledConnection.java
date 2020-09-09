/*
 * PooledConnection.java
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

import biz.redsoft.IFBDatabasePerformance;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.log.Log;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.sql.*;
import java.util.Timer;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * Pooled connection wrapper.
 *
 * @author Takis Diakoumis
 */
public class PooledConnection implements Connection {

    private String id = UUID.randomUUID().toString();

    /**
     * mutex that regulates the execution of only one statement
     */
    Semaphore mutex;

    /**
     * this connections use count
     */
    private int useCount;

    /**
     * indicates whether this connection is in use
     */
    private boolean inUse;

    /**
     * indicates whether to close this connection
     */
    private boolean closeOnReturn;

    /**
     * the original auto-commit mode from the real connection
     */
    private boolean originalAutoCommit;

    /**
     * the real JDBC connection that this object wraps
     */
    private Connection realConnection;
    private DatabaseConnection databaseConnection;

    private List<PooledConnectionListener> listeners;

    private Timer timer;
    private Timer timerDelay;
    private TimerTask task;
    private int timeoutShutdown;
    private PooledStatement lastStatement;
    private boolean timerCheckConnection;


    /**
     * Creates a new PooledConnection object with the
     * specified connection as the source.
     *
     * @param realConnection real java.sql.Connection
     *
     */
    public PooledConnection(Connection realConnection, DatabaseConnection databaseConnection) {
        this(realConnection, databaseConnection, false,false);
    }
    public PooledConnection(Connection realConnection, DatabaseConnection databaseConnection,boolean timerCheckConnection) {
        this(realConnection, databaseConnection, false,timerCheckConnection);
    }

    /**
     * Creates a new PooledConnection object with the
     * specified connection as the source.
     *
     * @param realConnection real java.sql.Connection
     */
    public PooledConnection(Connection realConnection, DatabaseConnection databaseConnection, boolean closeOnReturn,boolean timerCheckConnection) {
        this.databaseConnection = databaseConnection;
        this.timerCheckConnection=timerCheckConnection;
        mutex = new Semaphore(1);
        useCount = 0;
        timeoutShutdown = SystemProperties.getIntProperty("user", "connection.shutdown.timeout");
        this.realConnection = realConnection;
        this.closeOnReturn = closeOnReturn;
        timer = new Timer();
        timerDelay = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                checkConnectionToServer();
            }
        };
        if(this.timerCheckConnection)
            timer.schedule(task, timeoutShutdown);
        try {

            originalAutoCommit = realConnection.getAutoCommit();

        } catch (SQLException e) {

            // default to true on dump
            originalAutoCommit = true;
        }

    }

    public String getId() {
        return id;
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public void addPooledConnectionListener(PooledConnectionListener pooledConnectionListener) {

        if (listeners == null) {

            listeners = new ArrayList<PooledConnectionListener>();
        }

        listeners.add(pooledConnectionListener);
    }

    /**
     * Determine if the connection is available
     *
     * @return true if the connection can be used
     */
    public boolean isAvailable() {
        try {

            if (realConnection != null) {

                return !inUse && !realConnection.isClosed();

            }

            return false;

        } catch (SQLException e) {

            return false;
        }
    }

    public void setInUse(boolean inUse) {

        if (inUse) {

            useCount++;
        }

        this.inUse = inUse;
    }

    protected void destroy() {

        /*if (Log.isDebugEnabled()) {*/

        //Log.info("Destroying connection - " + id);
        //}

        try {

            close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //realConnection = null;
    }

    /**
     * Closes the underlying connection, and close
     * any Statements that were not explicitly closed.
     */
    public void close() throws SQLException {

        inUse = false;
        if(timerCheckConnection)
            timer.cancel();

        if (realConnection != null) {

            //if (Log.isDebugEnabled()) {

            //Log.info("Closing connection - " + id);
            //}

            if (closeOnReturn) {

                realConnection.close();
                realConnection = null;

            } else {

                // reset the original auto-commit mode
                try {

                    realConnection.setAutoCommit(originalAutoCommit);

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

            fireConnectionClosed();

        }
    }

    private void fireConnectionClosed() {

        if (listeners != null)
            for (PooledConnectionListener listener : listeners) {

                listener.connectionClosed(this);
            }

    }

    protected void handleException(SQLException e) throws SQLException {
        checkConnectionToServer();
        throw e;
    }

    public void checkConnectionToServer()
    {
        try {
            IFBDatabasePerformance db = (IFBDatabasePerformance) DynamicLibraryLoader.loadingObjectFromClassLoader(realConnection, "FBDatabasePerformanceImpl");
            db.setConnection(realConnection);
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (databaseConnection.isConnected()) {
                        if (GUIUtilities.displayConfirmDialog("The server is not responding. do you want to close the connection?") == JOptionPane.OK_OPTION) {
                            closeDatabaseConnection();
                            timerDelay.cancel();
                        }
                    } else
                        timerDelay.cancel();
                }
            };
            timerDelay = new Timer();
            /*StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            Log.info("---------------------------------Start check----------------------------------\n\n\n");
            for (int i = 0; i < stack.length - 2; i++)
                Log.info(stack[stack.length - 1 - i]);*/
            timerDelay.schedule(task, timeoutShutdown);
            db.getPerformanceInfo();
            timerDelay.cancel();
            //Log.info("---------------------------------Finish check.----------------------------------\n\n\n");
        } catch (SQLException e)
        {
            if (databaseConnection.isConnected())
                closeDatabaseConnection();
            timerDelay.cancel();
        } catch (ClassNotFoundException e) {
            if (databaseConnection.isConnected()) {
                if (GUIUtilities.displayConfirmDialog("The server is not responding. do you want to close the connection?") == JOptionPane.OK_OPTION) {
                    closeDatabaseConnection();
                }
            }
            timerDelay.cancel();
        }
    }

    public Statement createStatement() throws SQLException {
        checkOpen();
        Statement statement = null;
        lock(true);
        try {
            statement = realConnection.createStatement();
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            if (statement == null)
                lock(false);
            handleException(e);
            return null;
        }

    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        checkOpen();
        Statement statement = null;
        lock(true);
        try {
            statement = realConnection.createStatement(resultSetType, resultSetConcurrency);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            if (statement == null)
                lock(false);
            handleException(e);
            return null;
        }
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkOpen();
        PreparedStatement statement = null;
        lock(true);
        try {
            statement = realConnection.prepareStatement(sql);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            if (statement == null)
                lock(false);
            handleException(e);
            return null;
        }
    }

    public PreparedStatement prepareStatement(String sql,
                                              int resultSetType,
                                              int resultSetConcurrency)
            throws SQLException {
        checkOpen();
        PreparedStatement statement = null;
        lock(true);
        try {
            statement = realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            if (statement == null)
                lock(false);
            handleException(e);
            return null;
        }
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        checkOpen();
        CallableStatement statement = null;
        lock(true);
        try {
            statement = realConnection.prepareCall(sql);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            if (statement == null)
                lock(false);
            handleException(e);
            return null;
        }
    }

    public CallableStatement prepareCall(String sql,
                                         int resultSetType,
                                         int resultSetConcurrency)
            throws SQLException {
        checkOpen();
        CallableStatement statement = null;
        lock(true);
        try {
            statement = realConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            if (statement == null)
                lock(false);
            handleException(e);
            return null;
        }
    }

    public void clearWarnings() throws SQLException {
        checkOpen();
        try {
            realConnection.clearWarnings();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public void commit() throws SQLException {
        checkOpen();
        try {
            realConnection.commit();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public boolean getAutoCommit() throws SQLException {
        checkOpen();
        try {
            return realConnection.getAutoCommit();
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    public String getCatalog() throws SQLException {
        checkOpen();
        try {
            return realConnection.getCatalog();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        checkOpen();
        try {
            return new PooledDatabaseMetaData(this, realConnection.getMetaData());
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public int getTransactionIsolation() throws SQLException {
        checkOpen();
        try {
            return realConnection.getTransactionIsolation();
        } catch (SQLException e) {
            handleException(e);
            return -1;
        }
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        checkOpen();
        try {
            return realConnection.getTypeMap();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        checkOpen();
        try {
            return realConnection.getWarnings();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public boolean isReadOnly() throws SQLException {
        checkOpen();
        try {
            return realConnection.isReadOnly();
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    public String nativeSQL(String sql) throws SQLException {
        checkOpen();
        try {
            return realConnection.nativeSQL(sql);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public void rollback() throws SQLException {
        checkOpen();
        try {
            realConnection.rollback();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkOpen();
        if (getAutoCommit() != autoCommit)
            try {
                lock(true);
                realConnection.setAutoCommit(autoCommit);
                lock(false);
            } catch (SQLException e) {
                handleException(e);
            }
    }

    public void setCatalog(String catalog) throws SQLException {
        checkOpen();
        try {
            realConnection.setCatalog(catalog);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        checkOpen();
        try {
            realConnection.setReadOnly(readOnly);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public void setTransactionIsolation(int level) throws SQLException {
        checkOpen();
        try {
            realConnection.setTransactionIsolation(level);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public boolean isClosed() throws SQLException {
        if (realConnection == null) {
            return true;
        }
        return realConnection.isClosed();
    }

    protected void checkOpen() throws SQLException {
        if (realConnection != null && realConnection.isClosed()) {
            throw new SQLException("Connection is closed.");
        }
        if (realConnection == null) {
            throw new SQLException("Connection is closed.");
        }
        /*try {
            realConnection.createStatement().executeQuery("SELECT * FROM RDB$DATABASE");
        } catch (SQLException e){
            GUIUtilities.displayErrorMessage("lost connection to server");
            ConnectionMediator.getInstance().disconnect(databaseConnection);
        }*/
    }

    public void closeDatabaseConnection() {
        GUIUtilities.displayErrorMessage("lost connection to server");
        ConnectionMediator.getInstance().disconnect(databaseConnection);
        timer.cancel();
    }

    public int getHoldability() throws SQLException {
        checkOpen();
        try {
            return realConnection.getHoldability();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    public void setHoldability(int holdability) throws SQLException {
        checkOpen();
        try {
            realConnection.setHoldability(holdability);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public java.sql.Savepoint setSavepoint() throws SQLException {
        checkOpen();
        try {
            return realConnection.setSavepoint();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public java.sql.Savepoint setSavepoint(String name) throws SQLException {
        checkOpen();
        try {
            return realConnection.setSavepoint(name);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public void rollback(java.sql.Savepoint savepoint) throws SQLException {
        checkOpen();
        try {
            realConnection.rollback(savepoint);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException {
        checkOpen();
        try {
            realConnection.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    public Statement createStatement(int resultSetType,
                                     int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException {
        checkOpen();
        Statement statement = null;
        try {
            lock(true);
            statement = realConnection.createStatement(
                    resultSetType, resultSetConcurrency, resultSetHoldability);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            lock(false);
            handleException(e);
            return null;
        }
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        checkOpen();
        Statement statement = null;
        try {
            lock(true);
            statement = realConnection.prepareStatement(
                    sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            lock(false);
            handleException(e);
            return null;
        }
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        checkOpen();
        Statement statement = null;
        try {
            lock(true);
            statement = realConnection.prepareCall(
                    sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            lock(false);
            handleException(e);
            return null;
        }
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        checkOpen();
        PreparedStatement statement = null;
        try {
            lock(true);
            statement = realConnection.prepareStatement(sql, autoGeneratedKeys);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            lock(false);
            handleException(e);
            return null;
        }
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        checkOpen();
        PreparedStatement statement = null;
        try {
            lock(true);
            statement = realConnection.prepareStatement(sql, columnIndexes);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            lock(false);
            handleException(e);
            return null;
        }
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        checkOpen();
        PreparedStatement statement = null;
        try {
            lock(true);
            statement = realConnection.prepareStatement(sql, columnNames);
            lastStatement = new PooledStatement(this, statement);
            return lastStatement;
        } catch (SQLException e) {
            lock(false);
            handleException(e);
            return null;
        }
    }

    public Connection getRealConnection() {
        return realConnection;
    }

    public void setRealConnection(Connection realConnection) {
        this.realConnection = realConnection;
    }

    public boolean isCloseOnReturn() {
        return closeOnReturn;
    }

    public void setCloseOnReturn(boolean closeOnReturn) {
        this.closeOnReturn = closeOnReturn;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        checkOpen();
        try {
            realConnection.setTypeMap(map);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    // ------------------------------------------------------------------
    // java v1.6+

    public Array createArrayOf(String typeName, Object[] elements)
            throws SQLException {
        checkOpen();
        try {
            return realConnection.createArrayOf(typeName, elements);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public Blob createBlob() throws SQLException {
        checkOpen();
        try {
            return realConnection.createBlob();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public Clob createClob() throws SQLException {
        checkOpen();
        try {
            return realConnection.createClob();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public NClob createNClob() throws SQLException {
        checkOpen();
        try {
            return realConnection.createNClob();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public SQLXML createSQLXML() throws SQLException {
        checkOpen();
        try {
            return realConnection.createSQLXML();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public Struct createStruct(String typeName, Object[] attributes)
            throws SQLException {
        checkOpen();
        try {
            return realConnection.createStruct(typeName, attributes);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public Properties getClientInfo() throws SQLException {
        checkOpen();
        try {
            return realConnection.getClientInfo();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public String getClientInfo(String name) throws SQLException {
        checkOpen();
        try {
            return realConnection.getClientInfo(name);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public boolean isValid(int timeout) throws SQLException {
        checkOpen();
        try {
            return realConnection.isValid(timeout);
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    public void setClientInfo(Properties properties)
            throws SQLClientInfoException {
        try {
            checkOpen();
            realConnection.setClientInfo(properties);
        } catch (SQLException e) {
            throw new SQLClientInfoException(e.getMessage(), null);
        }
    }

    public void setClientInfo(String name, String value)
            throws SQLClientInfoException {
        try {
            checkOpen();
            realConnection.setClientInfo(name, value);
        } catch (SQLException e) {
            throw new SQLClientInfoException(e.getMessage(), null);
        }
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        checkOpen();
        try {
            return realConnection.isWrapperFor(iface);
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        checkOpen();
        try {
            return realConnection.unwrap(iface);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public String getSchema() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setSchema(String schema) {
        // TODO Auto-generated method stub

    }

    public void abort(Executor executor) {
        // TODO Auto-generated method stub

    }

    public void setNetworkTimeout(Executor executor, int milliseconds) {
        // TODO Auto-generated method stub

    }

    public int getNetworkTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Sets the state of the mutex. Before creating a statement,
     * you need to capture it, and after execution - release.
     *
     * @param flag - mutex status
     */
    public void lock(boolean flag) throws SQLException {

        if (flag) {
            try {
                mutex.acquire();
                /*StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                Log.debug("---------------------------------Start a connection lock. Stack:----------------------------------\n\n\n");
                for (int i = 0; i < stack.length - 2; i++)
                    Log.debug(stack[stack.length - 1 - i]);
                Log.debug("---------------------------------Connection is locked.----------------------------------\n\n\n");*/
            } catch (InterruptedException e) {
                throw new SQLException(e);
            }
        }
        else {
            mutex.release();
            //Log.debug("---------------------------------Connection is released.----------------------------------\n\n\n");
        }

    }

    public PooledStatement createIndividualStatement() throws SQLException {
        checkOpen();
        Statement statement = null;
        try {
            setAutoCommit(false);
            statement = realConnection.createStatement();
            PooledStatement pooledStatement = new PooledStatement(this, statement);
            pooledStatement.setIndividual(true);
            return pooledStatement;
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    public PooledStatement getLastStatement() {
        return lastStatement;
    }
}





