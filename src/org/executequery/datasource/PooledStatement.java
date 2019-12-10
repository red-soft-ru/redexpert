package org.executequery.datasource;

import biz.redsoft.IFBSQLException;
import org.executequery.log.Log;
import org.underworldlabs.util.DynamicLibraryLoader;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class PooledStatement implements CallableStatement {

    private PooledConnection connection;

    private Statement statement;

    private PreparedStatement preparedStatement;

    private CallableStatement callableStatement;

    private boolean closed;

    public PooledStatement(PooledConnection con, Statement statement) {
        this.connection = con;
        this.statement = statement;
        closed = false;
    }

    public PooledStatement(PooledConnection con, PreparedStatement statement) {
        this(con, (Statement) statement);
        this.preparedStatement = statement;
    }

    public PooledStatement(PooledConnection con, CallableStatement statement) {
        this(con, (PreparedStatement) statement);
        this.callableStatement = statement;
    }

    public Statement getStatement() {
        return statement;
    }

    protected void handleException(SQLException e) throws SQLException {
        connection.checkConnectionToServer();
        throw e;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            return new PooledResultSet(this, statement.executeQuery(sql));
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public int executeUpdate(String s) throws SQLException {
        try {
            return statement.executeUpdate(s);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    private boolean individual = false;

    @Override
    public int getMaxFieldSize() throws SQLException {
        try {
            return statement.getMaxFieldSize();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public void setMaxFieldSize(int i) throws SQLException {
        try {
            statement.setMaxFieldSize(i);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public int getMaxRows() throws SQLException {
        try {
            return statement.getMaxRows();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public void setMaxRows(int i) throws SQLException {
        try {
            statement.setMaxRows(i);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void setEscapeProcessing(boolean b) throws SQLException {
        try {
            statement.setEscapeProcessing(b);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        try {
            return statement.getQueryTimeout();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public void setQueryTimeout(int i) throws SQLException {
        try {
            statement.setQueryTimeout(i);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void cancel() throws SQLException {
        try {
            statement.cancel();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        try {
            return statement.getWarnings();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void clearWarnings() throws SQLException {
        try {
            statement.clearWarnings();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void setCursorName(String s) throws SQLException {
        try {
            statement.setCursorName(s);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public boolean execute(String s) throws SQLException {
        try {
            return statement.execute(s);
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        try {
            return new PooledResultSet(this, statement.getResultSet());
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public int getUpdateCount() throws SQLException {
        try {
            return statement.getUpdateCount();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        try {
            return statement.getMoreResults();
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public int getFetchDirection() throws SQLException {
        try {
            return statement.getFetchDirection();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public void setFetchDirection(int i) throws SQLException {
        try {
            statement.setFetchDirection(i);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public int getFetchSize() throws SQLException {
        try {
            return statement.getFetchSize();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public void setFetchSize(int i) throws SQLException {
        try {
            statement.setFetchSize(i);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        try {
            return statement.getResultSetConcurrency();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public int getResultSetType() throws SQLException {
        try {
            return statement.getResultSetType();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public void addBatch(String s) throws SQLException {
        try {
            statement.addBatch(s);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void clearBatch() throws SQLException {
        try {
            statement.clearBatch();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public int[] executeBatch() throws SQLException {
        try {
            return statement.executeBatch();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return statement.getConnection();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public boolean getMoreResults(int i) throws SQLException {
        try {
            return statement.getMoreResults(i);
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        try {
            return new PooledResultSet(this, statement.getGeneratedKeys());
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public int executeUpdate(String s, int i) throws SQLException {
        try {
            return statement.executeUpdate(s);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public int executeUpdate(String s, int[] ints) throws SQLException {
        try {
            return statement.executeUpdate(s, ints);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public int executeUpdate(String s, String[] strings) throws SQLException {
        try {
            return statement.executeUpdate(s, strings);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public boolean execute(String s, int i) throws SQLException {
        try {
            return statement.execute(s, i);
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public boolean execute(String s, int[] ints) throws SQLException {
        try {
            return statement.execute(s, ints);
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }


    @Override
    public boolean execute(String s, String[] strings) throws SQLException {
        try {
            return statement.execute(s, strings);
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        try {
            return statement.getResultSetHoldability();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            if (!closed) {
                if (statement != null)
                    if (!statement.isClosed())
                        statement.close();
                connection.lock(false);
                closed = true;
            } else {
                if (!individual)
                    Log.info("Trying to close connection a second time.");
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public boolean isPoolable() throws SQLException {
        try {
            return statement.isPoolable();
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public void setPoolable(boolean b) throws SQLException {
        try {
            statement.setPoolable(b);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        try {
            statement.closeOnCompletion();
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        try {
            return statement.isCloseOnCompletion();
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        try {
            return statement.unwrap(aClass);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        try {
            return statement.isWrapperFor(aClass);
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        try {
            return new PooledResultSet(this, preparedStatement.executeQuery());
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        try {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public void setNull(int i, int i1) throws SQLException {
        try {
            preparedStatement.setNull(i, i1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBoolean(int i, boolean b) throws SQLException {
        try {
            preparedStatement.setBoolean(i, b);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setByte(int i, byte b) throws SQLException {
        try {
            preparedStatement.setByte(i, b);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setShort(int i, short i1) throws SQLException {
        try {
            preparedStatement.setShort(i, i1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setInt(int i, int i1) throws SQLException {
        try {
            preparedStatement.setInt(i, i1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setLong(int i, long l) throws SQLException {
        try {
            preparedStatement.setLong(i, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setFloat(int i, float v) throws SQLException {
        try {
            preparedStatement.setFloat(i, v);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setDouble(int i, double v) throws SQLException {
        try {
            preparedStatement.setDouble(i, v);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBigDecimal(int i, BigDecimal bigDecimal) throws SQLException {
        try {
            preparedStatement.setBigDecimal(i, bigDecimal);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setString(int i, String s) throws SQLException {
        try {
            preparedStatement.setString(i, s);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBytes(int i, byte[] bytes) throws SQLException {
        try {
            preparedStatement.setBytes(i, bytes);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setDate(int i, Date date) throws SQLException {
        try {
            preparedStatement.setDate(i, date);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setTime(int i, Time time) throws SQLException {
        try {
            preparedStatement.setTime(i, time);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setTimestamp(int i, Timestamp timestamp) throws SQLException {
        try {
            preparedStatement.setTimestamp(i, timestamp);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setAsciiStream(int i, InputStream inputStream, int i1) throws SQLException {
        try {
            preparedStatement.setAsciiStream(i, inputStream, i1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setUnicodeStream(int i, InputStream inputStream, int i1) throws SQLException {
        try {
            preparedStatement.setUnicodeStream(i, inputStream, i1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBinaryStream(int i, InputStream inputStream, int i1) throws SQLException {
        try {
            preparedStatement.setBinaryStream(i, inputStream, i1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void clearParameters() throws SQLException {
        try {
            preparedStatement.clearParameters();
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setObject(int i, Object o, int i1) throws SQLException {
        try {
            preparedStatement.setObject(i, o, i1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setObject(int i, Object o) throws SQLException {
        try {
            preparedStatement.setObject(i, o);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public boolean execute() throws SQLException {
        try {
            return preparedStatement.execute();
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public void addBatch() throws SQLException {
        try {
            preparedStatement.addBatch();
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setCharacterStream(int i, Reader reader, int i1) throws SQLException {
        try {
            preparedStatement.setCharacterStream(i, reader, i1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setRef(int i, Ref ref) throws SQLException {
        try {
            preparedStatement.setRef(i, ref);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBlob(int i, Blob blob) throws SQLException {
        try {
            preparedStatement.setBlob(i, blob);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setClob(int i, Clob clob) throws SQLException {
        try {
            preparedStatement.setClob(i, clob);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setArray(int i, Array array) throws SQLException {
        try {
            preparedStatement.setArray(i, array);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        try {
            return preparedStatement.getMetaData();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void setDate(int i, Date date, Calendar calendar) throws SQLException {
        try {
            preparedStatement.setDate(i, date, calendar);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setTime(int i, Time time, Calendar calendar) throws SQLException {
        try {
            preparedStatement.setTime(i, time, calendar);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setTimestamp(int i, Timestamp timestamp, Calendar calendar) throws SQLException {
        try {
            preparedStatement.setTimestamp(i, timestamp, calendar);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNull(int i, int i1, String s) throws SQLException {
        try {
            preparedStatement.setNull(i, i1, s);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setURL(int i, URL url) throws SQLException {
        try {
            preparedStatement.setURL(i, url);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        try {
            return preparedStatement.getParameterMetaData();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void setRowId(int i, RowId rowId) throws SQLException {
        try {
            preparedStatement.setRowId(i, rowId);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNString(int i, String s) throws SQLException {
        try {
            preparedStatement.setNString(i, s);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNCharacterStream(int i, Reader reader, long l) throws SQLException {
        try {
            preparedStatement.setNCharacterStream(i, reader, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNClob(int i, NClob nClob) throws SQLException {
        try {
            preparedStatement.setNClob(i, nClob);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setClob(int i, Reader reader, long l) throws SQLException {
        try {
            preparedStatement.setClob(i, reader, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBlob(int i, InputStream inputStream, long l) throws SQLException {
        try {
            preparedStatement.setBlob(i, inputStream, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNClob(int i, Reader reader, long l) throws SQLException {
        try {
            preparedStatement.setNClob(i, reader, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setSQLXML(int i, SQLXML sqlxml) throws SQLException {
        try {
            preparedStatement.setSQLXML(i, sqlxml);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setObject(int i, Object o, int i1, int i2) throws SQLException {
        try {
            preparedStatement.setObject(i, o, i1, i2);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setAsciiStream(int i, InputStream inputStream, long l) throws SQLException {
        try {
            preparedStatement.setAsciiStream(i, inputStream, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBinaryStream(int i, InputStream inputStream, long l) throws SQLException {
        try {
            preparedStatement.setBinaryStream(i, inputStream, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setCharacterStream(int i, Reader reader, long l) throws SQLException {
        try {
            preparedStatement.setCharacterStream(i, reader, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setAsciiStream(int i, InputStream inputStream) throws SQLException {
        try {
            preparedStatement.setAsciiStream(i, inputStream);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBinaryStream(int i, InputStream inputStream) throws SQLException {
        try {
            preparedStatement.setBinaryStream(i, inputStream);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setCharacterStream(int i, Reader reader) throws SQLException {
        try {
            preparedStatement.setCharacterStream(i, reader);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNCharacterStream(int i, Reader reader) throws SQLException {
        try {
            preparedStatement.setNCharacterStream(i, reader);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setClob(int i, Reader reader) throws SQLException {
        try {
            preparedStatement.setClob(i, reader);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBlob(int i, InputStream inputStream) throws SQLException {
        try {
            preparedStatement.setBlob(i, inputStream);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNClob(int i, Reader reader) throws SQLException {
        try {
            preparedStatement.setNClob(i, reader);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void registerOutParameter(int i, int i1) throws SQLException {
        try {
            callableStatement.registerOutParameter(i, i1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void registerOutParameter(int i, int i1, int i2) throws SQLException {
        try {
            callableStatement.registerOutParameter(i, i1, i2);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        try {
            return callableStatement.wasNull();
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public String getString(int i) throws SQLException {
        try {
            return callableStatement.getString(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public boolean getBoolean(int i) throws SQLException {
        try {
            return callableStatement.getBoolean(i);
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public byte getByte(int i) throws SQLException {
        try {
            return callableStatement.getByte(i);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public short getShort(int i) throws SQLException {
        try {
            return callableStatement.getShort(i);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public int getInt(int i) throws SQLException {
        try {
            return callableStatement.getInt(i);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public long getLong(int i) throws SQLException {
        try {
            return callableStatement.getLong(i);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public float getFloat(int i) throws SQLException {
        try {
            return callableStatement.getFloat(i);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public double getDouble(int i) throws SQLException {
        try {
            return callableStatement.getDouble(i);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public BigDecimal getBigDecimal(int i, int i1) throws SQLException {
        try {
            return callableStatement.getBigDecimal(i, i1);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public byte[] getBytes(int i) throws SQLException {
        try {
            return callableStatement.getBytes(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Date getDate(int i) throws SQLException {
        try {
            return callableStatement.getDate(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Time getTime(int i) throws SQLException {
        try {
            return callableStatement.getTime(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Timestamp getTimestamp(int i) throws SQLException {
        try {
            return callableStatement.getTimestamp(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Object getObject(int i) throws SQLException {
        try {
            return callableStatement.getObject(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public BigDecimal getBigDecimal(int i) throws SQLException {
        try {
            return callableStatement.getBigDecimal(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        try {
            return callableStatement.getObject(i, map);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Ref getRef(int i) throws SQLException {
        try {
            return callableStatement.getRef(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Blob getBlob(int i) throws SQLException {
        try {
            return callableStatement.getBlob(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Clob getClob(int i) throws SQLException {
        try {
            return callableStatement.getClob(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Array getArray(int i) throws SQLException {
        try {
            return callableStatement.getArray(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Date getDate(int i, Calendar calendar) throws SQLException {
        try {
            return callableStatement.getDate(i, calendar);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Time getTime(int i, Calendar calendar) throws SQLException {
        try {
            return callableStatement.getTime(i, calendar);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Timestamp getTimestamp(int i, Calendar calendar) throws SQLException {
        try {
            return callableStatement.getTimestamp(i, calendar);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void registerOutParameter(int i, int i1, String s) throws SQLException {
        try {
            callableStatement.registerOutParameter(i, i1, s);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void registerOutParameter(String s, int i) throws SQLException {
        try {
            callableStatement.registerOutParameter(s, i);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void registerOutParameter(String s, int i, int i1) throws SQLException {
        try {
            callableStatement.registerOutParameter(s, i, i1);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public void registerOutParameter(String s, int i, String s1) throws SQLException {
        try {
            callableStatement.registerOutParameter(s, i, s1);
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    public URL getURL(int i) throws SQLException {
        try {
            return callableStatement.getURL(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void setURL(String s, URL url) throws SQLException {
        try {
            callableStatement.setURL(s, url);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNull(String s, int i) throws SQLException {
        try {
            callableStatement.setNull(s, i);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBoolean(String s, boolean b) throws SQLException {
        try {
            callableStatement.setBoolean(s, b);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setByte(String s, byte b) throws SQLException {
        try {
            callableStatement.setByte(s, b);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setShort(String s, short i) throws SQLException {
        try {
            callableStatement.setShort(s, i);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setInt(String s, int i) throws SQLException {
        try {
            callableStatement.setInt(s, i);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setLong(String s, long l) throws SQLException {
        try {
            callableStatement.setLong(s, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setFloat(String s, float v) throws SQLException {
        try {
            callableStatement.setFloat(s, v);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setDouble(String s, double v) throws SQLException {
        try {
            callableStatement.setDouble(s, v);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBigDecimal(String s, BigDecimal bigDecimal) throws SQLException {
        try {
            callableStatement.setBigDecimal(s, bigDecimal);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setString(String s, String s1) throws SQLException {
        try {
            callableStatement.setString(s, s1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBytes(String s, byte[] bytes) throws SQLException {
        try {
            callableStatement.setBytes(s, bytes);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setDate(String s, Date date) throws SQLException {
        try {
            callableStatement.setDate(s, date);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setTime(String s, Time time) throws SQLException {
        try {
            callableStatement.setTime(s, time);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setTimestamp(String s, Timestamp timestamp) throws SQLException {
        try {
            callableStatement.setTimestamp(s, timestamp);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setAsciiStream(String s, InputStream inputStream, int i) throws SQLException {
        try {
            callableStatement.setAsciiStream(s, inputStream, i);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBinaryStream(String s, InputStream inputStream, int i) throws SQLException {
        try {
            callableStatement.setBinaryStream(s, inputStream, i);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setObject(String s, Object o, int i, int i1) throws SQLException {
        try {
            callableStatement.setObject(s, o, i, i1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setObject(String s, Object o, int i) throws SQLException {
        try {
            callableStatement.setObject(s, o, i);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setObject(String s, Object o) throws SQLException {
        try {
            callableStatement.setObject(s, o);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setCharacterStream(String s, Reader reader, int i) throws SQLException {
        try {
            callableStatement.setCharacterStream(s, reader, i);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setDate(String s, Date date, Calendar calendar) throws SQLException {
        try {
            callableStatement.setDate(s, date, calendar);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setTime(String s, Time time, Calendar calendar) throws SQLException {
        try {
            callableStatement.setTime(s, time, calendar);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setTimestamp(String s, Timestamp timestamp, Calendar calendar) throws SQLException {
        try {
            callableStatement.setTimestamp(s, timestamp, calendar);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNull(String s, int i, String s1) throws SQLException {
        try {
            callableStatement.setNull(s, i, s1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public String getString(String s) throws SQLException {
        try {
            return callableStatement.getString(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public boolean getBoolean(String s) throws SQLException {
        try {
            return callableStatement.getBoolean(s);
        } catch (SQLException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public byte getByte(String s) throws SQLException {
        try {
            return callableStatement.getByte(s);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public short getShort(String s) throws SQLException {
        try {
            return callableStatement.getShort(s);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public int getInt(String s) throws SQLException {
        try {
            return callableStatement.getInt(s);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public long getLong(String s) throws SQLException {
        try {
            return callableStatement.getLong(s);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public float getFloat(String s) throws SQLException {
        try {
            return callableStatement.getFloat(s);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public double getDouble(String s) throws SQLException {
        try {
            return callableStatement.getDouble(s);
        } catch (SQLException e) {
            handleException(e);
            return 0;
        }
    }

    @Override
    public byte[] getBytes(String s) throws SQLException {
        try {
            return callableStatement.getBytes(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Date getDate(String s) throws SQLException {
        try {
            return callableStatement.getDate(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Time getTime(String s) throws SQLException {
        try {
            return callableStatement.getTime(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Timestamp getTimestamp(String s) throws SQLException {
        try {
            return callableStatement.getTimestamp(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Object getObject(String s) throws SQLException {
        try {
            return callableStatement.getObject(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public BigDecimal getBigDecimal(String s) throws SQLException {
        try {
            return callableStatement.getBigDecimal(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Object getObject(String s, Map<String, Class<?>> map) throws SQLException {
        try {
            return callableStatement.getObject(s, map);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Ref getRef(String s) throws SQLException {
        try {
            return callableStatement.getRef(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Blob getBlob(String s) throws SQLException {
        try {
            return callableStatement.getBlob(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Clob getClob(String s) throws SQLException {
        try {
            return callableStatement.getClob(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Array getArray(String s) throws SQLException {
        try {
            return callableStatement.getArray(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Date getDate(String s, Calendar calendar) throws SQLException {
        try {
            return callableStatement.getDate(s, calendar);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Time getTime(String s, Calendar calendar) throws SQLException {
        try {
            return callableStatement.getTime(s, calendar);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Timestamp getTimestamp(String s, Calendar calendar) throws SQLException {
        try {
            return callableStatement.getTimestamp(s, calendar);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public URL getURL(String s) throws SQLException {
        try {
            return callableStatement.getURL(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public RowId getRowId(int i) throws SQLException {
        try {
            return callableStatement.getRowId(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public RowId getRowId(String s) throws SQLException {
        try {
            return callableStatement.getRowId(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void setRowId(String s, RowId rowId) throws SQLException {
        try {
            callableStatement.setRowId(s, rowId);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNString(String s, String s1) throws SQLException {
        try {
            callableStatement.setNString(s, s1);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNCharacterStream(String s, Reader reader, long l) throws SQLException {
        try {
            callableStatement.setNCharacterStream(s, reader, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNClob(String s, NClob nClob) throws SQLException {
        try {
            callableStatement.setNClob(s, nClob);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setClob(String s, Reader reader, long l) throws SQLException {
        try {
            callableStatement.setClob(s, reader, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBlob(String s, InputStream inputStream, long l) throws SQLException {
        try {
            callableStatement.setBlob(s, inputStream, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNClob(String s, Reader reader, long l) throws SQLException {
        try {
            callableStatement.setNClob(s, reader, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public NClob getNClob(int i) throws SQLException {
        try {
            return callableStatement.getNClob(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public NClob getNClob(String s) throws SQLException {
        try {
            return callableStatement.getNClob(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void setSQLXML(String s, SQLXML sqlxml) throws SQLException {
        try {
            callableStatement.setSQLXML(s, sqlxml);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public SQLXML getSQLXML(int i) throws SQLException {
        try {
            return callableStatement.getSQLXML(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public SQLXML getSQLXML(String s) throws SQLException {
        try {
            return callableStatement.getSQLXML(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public String getNString(int i) throws SQLException {
        try {
            return callableStatement.getNString(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public String getNString(String s) throws SQLException {
        try {
            return callableStatement.getNString(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Reader getNCharacterStream(int i) throws SQLException {
        try {
            return callableStatement.getNCharacterStream(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Reader getNCharacterStream(String s) throws SQLException {
        try {
            return callableStatement.getNCharacterStream(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Reader getCharacterStream(int i) throws SQLException {
        try {
            return callableStatement.getCharacterStream(i);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Reader getCharacterStream(String s) throws SQLException {
        try {
            return callableStatement.getCharacterStream(s);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void setBlob(String s, Blob blob) throws SQLException {
        try {
            callableStatement.setBlob(s, blob);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setClob(String s, Clob clob) throws SQLException {
        try {
            callableStatement.setClob(s, clob);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setAsciiStream(String s, InputStream inputStream, long l) throws SQLException {
        try {
            callableStatement.setAsciiStream(s, inputStream, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBinaryStream(String s, InputStream inputStream, long l) throws SQLException {
        try {
            callableStatement.setBinaryStream(s, inputStream, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setCharacterStream(String s, Reader reader, long l) throws SQLException {
        try {
            callableStatement.setCharacterStream(s, reader, l);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setAsciiStream(String s, InputStream inputStream) throws SQLException {
        try {
            callableStatement.setAsciiStream(s, inputStream);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBinaryStream(String s, InputStream inputStream) throws SQLException {
        try {
            callableStatement.setBinaryStream(s, inputStream);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setCharacterStream(String s, Reader reader) throws SQLException {
        try {
            callableStatement.setCharacterStream(s, reader);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNCharacterStream(String s, Reader reader) throws SQLException {
        try {
            callableStatement.setNCharacterStream(s, reader);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setClob(String s, Reader reader) throws SQLException {
        try {
            callableStatement.setClob(s, reader);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setBlob(String s, InputStream inputStream) throws SQLException {
        try {
            callableStatement.setBlob(s, inputStream);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public void setNClob(String s, Reader reader) throws SQLException {
        try {
            callableStatement.setNClob(s, reader);
        } catch (SQLException e) {
            handleException(e);

        }
    }

    @Override
    public <T> T getObject(int i, Class<T> aClass) throws SQLException {
        try {
            return callableStatement.getObject(i, aClass);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public <T> T getObject(String s, Class<T> aClass) throws SQLException {
        try {
            return callableStatement.getObject(s, aClass);
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public void setIndividual(boolean individual) {
        this.individual = individual;
    }
}
