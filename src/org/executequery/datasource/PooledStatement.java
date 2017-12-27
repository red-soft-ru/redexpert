package org.executequery.datasource;

import org.executequery.log.Log;

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

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return new PooledResultSet(this, statement.executeQuery(sql));
    }

    @Override
    public int executeUpdate(String s) throws SQLException {
        return statement.executeUpdate(s);
    }

    @Override
    public void close() throws SQLException {
        if (!closed) {
            statement.close();
            connection.lock(false);
            closed = true;
        } else {
            Log.info("Trying to close connection a second time.");
        }
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return statement.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int i) throws SQLException {
        statement.setMaxFieldSize(i);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return statement.getMaxRows();
    }

    @Override
    public void setMaxRows(int i) throws SQLException {
        statement.setMaxRows(i);
    }

    @Override
    public void setEscapeProcessing(boolean b) throws SQLException {
        statement.setEscapeProcessing(b);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return statement.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int i) throws SQLException {
        statement.setQueryTimeout(i);
    }

    @Override
    public void cancel() throws SQLException {
        statement.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return statement.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        statement.clearWarnings();
    }

    @Override
    public void setCursorName(String s) throws SQLException {
        statement.setCursorName(s);
    }

    @Override
    public boolean execute(String s) throws SQLException {
        return statement.execute(s);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return new PooledResultSet(this, statement.getResultSet());
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return statement.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return statement.getMoreResults();
    }

    @Override
    public void setFetchDirection(int i) throws SQLException {
        statement.setFetchDirection(i);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return statement.getFetchDirection();
    }

    @Override
    public void setFetchSize(int i) throws SQLException {
        statement.setFetchSize(i);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return statement.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return statement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return statement.getResultSetType();
    }

    @Override
    public void addBatch(String s) throws SQLException {
        statement.addBatch(s);
    }

    @Override
    public void clearBatch() throws SQLException {
        statement.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return statement.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return statement.getConnection();
    }

    @Override
    public boolean getMoreResults(int i) throws SQLException {
        return statement.getMoreResults(i);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return new PooledResultSet(this, statement.getGeneratedKeys());
    }

    @Override
    public int executeUpdate(String s, int i) throws SQLException {
        return statement.executeUpdate(s);
    }

    @Override
    public int executeUpdate(String s, int[] ints) throws SQLException {
        return statement.executeUpdate(s, ints);
    }

    @Override
    public int executeUpdate(String s, String[] strings) throws SQLException {
        return statement.executeUpdate(s, strings);
    }

    @Override
    public boolean execute(String s, int i) throws SQLException {
        return statement.execute(s, i);
    }

    @Override
    public boolean execute(String s, int[] ints) throws SQLException {
        return statement.execute(s, ints);
    }

    @Override
    public boolean execute(String s, String[] strings) throws SQLException {
        return statement.execute(s, strings);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return statement.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return statement.isClosed();
    }

    @Override
    public void setPoolable(boolean b) throws SQLException {
        statement.setPoolable(b);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return statement.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        statement.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return statement.isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return statement.unwrap(aClass);
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return statement.isWrapperFor(aClass);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return new PooledResultSet(this, preparedStatement.executeQuery());
    }

    @Override
    public int executeUpdate() throws SQLException {
        return preparedStatement.executeUpdate();
    }

    @Override
    public void setNull(int i, int i1) throws SQLException {
        preparedStatement.setNull(i, i1);
    }

    @Override
    public void setBoolean(int i, boolean b) throws SQLException {
        preparedStatement.setBoolean(i, b);
    }

    @Override
    public void setByte(int i, byte b) throws SQLException {
        preparedStatement.setByte(i, b);
    }

    @Override
    public void setShort(int i, short i1) throws SQLException {
        preparedStatement.setShort(i, i1);
    }

    @Override
    public void setInt(int i, int i1) throws SQLException {
        preparedStatement.setInt(i, i1);
    }

    @Override
    public void setLong(int i, long l) throws SQLException {
        preparedStatement.setLong(i, l);
    }

    @Override
    public void setFloat(int i, float v) throws SQLException {
        preparedStatement.setFloat(i, v);
    }

    @Override
    public void setDouble(int i, double v) throws SQLException {
        preparedStatement.setDouble(i, v);
    }

    @Override
    public void setBigDecimal(int i, BigDecimal bigDecimal) throws SQLException {
        preparedStatement.setBigDecimal(i, bigDecimal);
    }

    @Override
    public void setString(int i, String s) throws SQLException {
        preparedStatement.setString(i, s);
    }

    @Override
    public void setBytes(int i, byte[] bytes) throws SQLException {
        preparedStatement.setBytes(i, bytes);
    }

    @Override
    public void setDate(int i, Date date) throws SQLException {
        preparedStatement.setDate(i, date);
    }

    @Override
    public void setTime(int i, Time time) throws SQLException {
        preparedStatement.setTime(i, time);
    }

    @Override
    public void setTimestamp(int i, Timestamp timestamp) throws SQLException {
        preparedStatement.setTimestamp(i, timestamp);
    }

    @Override
    public void setAsciiStream(int i, InputStream inputStream, int i1) throws SQLException {
        preparedStatement.setAsciiStream(i, inputStream, i1);
    }

    @Override
    public void setUnicodeStream(int i, InputStream inputStream, int i1) throws SQLException {
        preparedStatement.setUnicodeStream(i, inputStream, i1);
    }

    @Override
    public void setBinaryStream(int i, InputStream inputStream, int i1) throws SQLException {
        preparedStatement.setBinaryStream(i, inputStream, i1);
    }

    @Override
    public void clearParameters() throws SQLException {
        preparedStatement.clearParameters();
    }

    @Override
    public void setObject(int i, Object o, int i1) throws SQLException {
        preparedStatement.setObject(i, o, i1);
    }

    @Override
    public void setObject(int i, Object o) throws SQLException {
        preparedStatement.setObject(i, o);
    }

    @Override
    public boolean execute() throws SQLException {
        return preparedStatement.execute();
    }

    @Override
    public void addBatch() throws SQLException {
        preparedStatement.addBatch();
    }

    @Override
    public void setCharacterStream(int i, Reader reader, int i1) throws SQLException {
        preparedStatement.setCharacterStream(i, reader, i1);
    }

    @Override
    public void setRef(int i, Ref ref) throws SQLException {
        preparedStatement.setRef(i, ref);
    }

    @Override
    public void setBlob(int i, Blob blob) throws SQLException {
        preparedStatement.setBlob(i, blob);
    }

    @Override
    public void setClob(int i, Clob clob) throws SQLException {
        preparedStatement.setClob(i, clob);
    }

    @Override
    public void setArray(int i, Array array) throws SQLException {
        preparedStatement.setArray(i, array);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return preparedStatement.getMetaData();
    }

    @Override
    public void setDate(int i, Date date, Calendar calendar) throws SQLException {
        preparedStatement.setDate(i, date, calendar);
    }

    @Override
    public void setTime(int i, Time time, Calendar calendar) throws SQLException {
        preparedStatement.setTime(i, time, calendar);
    }

    @Override
    public void setTimestamp(int i, Timestamp timestamp, Calendar calendar) throws SQLException {
        preparedStatement.setTimestamp(i, timestamp, calendar);
    }

    @Override
    public void setNull(int i, int i1, String s) throws SQLException {
        preparedStatement.setNull(i, i1, s);
    }

    @Override
    public void setURL(int i, URL url) throws SQLException {
        preparedStatement.setURL(i, url);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return preparedStatement.getParameterMetaData();
    }

    @Override
    public void setRowId(int i, RowId rowId) throws SQLException {
        preparedStatement.setRowId(i, rowId);
    }

    @Override
    public void setNString(int i, String s) throws SQLException {
        preparedStatement.setNString(i, s);
    }

    @Override
    public void setNCharacterStream(int i, Reader reader, long l) throws SQLException {
        preparedStatement.setNCharacterStream(i, reader, l);
    }

    @Override
    public void setNClob(int i, NClob nClob) throws SQLException {
        preparedStatement.setNClob(i, nClob);
    }

    @Override
    public void setClob(int i, Reader reader, long l) throws SQLException {
        preparedStatement.setClob(i, reader, l);
    }

    @Override
    public void setBlob(int i, InputStream inputStream, long l) throws SQLException {
        preparedStatement.setBlob(i, inputStream, l);
    }

    @Override
    public void setNClob(int i, Reader reader, long l) throws SQLException {
        preparedStatement.setNClob(i, reader, l);
    }

    @Override
    public void setSQLXML(int i, SQLXML sqlxml) throws SQLException {
        preparedStatement.setSQLXML(i, sqlxml);
    }

    @Override
    public void setObject(int i, Object o, int i1, int i2) throws SQLException {
        preparedStatement.setObject(i, o, i1, i2);
    }

    @Override
    public void setAsciiStream(int i, InputStream inputStream, long l) throws SQLException {
        preparedStatement.setAsciiStream(i, inputStream, l);
    }

    @Override
    public void setBinaryStream(int i, InputStream inputStream, long l) throws SQLException {
        preparedStatement.setBinaryStream(i, inputStream, l);
    }

    @Override
    public void setCharacterStream(int i, Reader reader, long l) throws SQLException {
        preparedStatement.setCharacterStream(i, reader, l);
    }

    @Override
    public void setAsciiStream(int i, InputStream inputStream) throws SQLException {
        preparedStatement.setAsciiStream(i, inputStream);
    }

    @Override
    public void setBinaryStream(int i, InputStream inputStream) throws SQLException {
        preparedStatement.setBinaryStream(i, inputStream);
    }

    @Override
    public void setCharacterStream(int i, Reader reader) throws SQLException {
        preparedStatement.setCharacterStream(i, reader);
    }

    @Override
    public void setNCharacterStream(int i, Reader reader) throws SQLException {
        preparedStatement.setNCharacterStream(i, reader);
    }

    @Override
    public void setClob(int i, Reader reader) throws SQLException {
        preparedStatement.setClob(i, reader);
    }

    @Override
    public void setBlob(int i, InputStream inputStream) throws SQLException {
        preparedStatement.setBlob(i, inputStream);
    }

    @Override
    public void setNClob(int i, Reader reader) throws SQLException {
        preparedStatement.setNClob(i, reader);
    }

    @Override
    public void registerOutParameter(int i, int i1) throws SQLException {
        callableStatement.registerOutParameter(i, i1);
    }

    @Override
    public void registerOutParameter(int i, int i1, int i2) throws SQLException {
        callableStatement.registerOutParameter(i, i1, i2);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return callableStatement.wasNull();
    }

    @Override
    public String getString(int i) throws SQLException {
        return callableStatement.getString(i);
    }

    @Override
    public boolean getBoolean(int i) throws SQLException {
        return callableStatement.getBoolean(i);
    }

    @Override
    public byte getByte(int i) throws SQLException {
        return callableStatement.getByte(i);
    }

    @Override
    public short getShort(int i) throws SQLException {
        return callableStatement.getShort(i);
    }

    @Override
    public int getInt(int i) throws SQLException {
        return callableStatement.getInt(i);
    }

    @Override
    public long getLong(int i) throws SQLException {
        return callableStatement.getLong(i);
    }

    @Override
    public float getFloat(int i) throws SQLException {
        return callableStatement.getFloat(i);
    }

    @Override
    public double getDouble(int i) throws SQLException {
        return callableStatement.getDouble(i);
    }

    @Override
    public BigDecimal getBigDecimal(int i, int i1) throws SQLException {
        return callableStatement.getBigDecimal(i, i1);
    }

    @Override
    public byte[] getBytes(int i) throws SQLException {
        return callableStatement.getBytes(i);
    }

    @Override
    public Date getDate(int i) throws SQLException {
        return callableStatement.getDate(i);
    }

    @Override
    public Time getTime(int i) throws SQLException {
        return callableStatement.getTime(i);
    }

    @Override
    public Timestamp getTimestamp(int i) throws SQLException {
        return callableStatement.getTimestamp(i);
    }

    @Override
    public Object getObject(int i) throws SQLException {
        return callableStatement.getObject(i);
    }

    @Override
    public BigDecimal getBigDecimal(int i) throws SQLException {
        return callableStatement.getBigDecimal(i);
    }

    @Override
    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return callableStatement.getObject(i, map);
    }

    @Override
    public Ref getRef(int i) throws SQLException {
        return callableStatement.getRef(i);
    }

    @Override
    public Blob getBlob(int i) throws SQLException {
        return callableStatement.getBlob(i);
    }

    @Override
    public Clob getClob(int i) throws SQLException {
        return callableStatement.getClob(i);
    }

    @Override
    public Array getArray(int i) throws SQLException {
        return callableStatement.getArray(i);
    }

    @Override
    public Date getDate(int i, Calendar calendar) throws SQLException {
        return callableStatement.getDate(i, calendar);
    }

    @Override
    public Time getTime(int i, Calendar calendar) throws SQLException {
        return callableStatement.getTime(i, calendar);
    }

    @Override
    public Timestamp getTimestamp(int i, Calendar calendar) throws SQLException {
        return callableStatement.getTimestamp(i, calendar);
    }

    @Override
    public void registerOutParameter(int i, int i1, String s) throws SQLException {
        callableStatement.registerOutParameter(i, i1, s);
    }

    @Override
    public void registerOutParameter(String s, int i) throws SQLException {
        callableStatement.registerOutParameter(s, i);
    }

    @Override
    public void registerOutParameter(String s, int i, int i1) throws SQLException {
        callableStatement.registerOutParameter(s, i, i1);
    }

    @Override
    public void registerOutParameter(String s, int i, String s1) throws SQLException {
        callableStatement.registerOutParameter(s, i, s1);
    }

    @Override
    public URL getURL(int i) throws SQLException {
        return callableStatement.getURL(i);
    }

    @Override
    public void setURL(String s, URL url) throws SQLException {
        callableStatement.setURL(s, url);
    }

    @Override
    public void setNull(String s, int i) throws SQLException {
        callableStatement.setNull(s, i);
    }

    @Override
    public void setBoolean(String s, boolean b) throws SQLException {
        callableStatement.setBoolean(s, b);
    }

    @Override
    public void setByte(String s, byte b) throws SQLException {
        callableStatement.setByte(s, b);
    }

    @Override
    public void setShort(String s, short i) throws SQLException {
        callableStatement.setShort(s, i);
    }

    @Override
    public void setInt(String s, int i) throws SQLException {
        callableStatement.setInt(s, i);
    }

    @Override
    public void setLong(String s, long l) throws SQLException {
        callableStatement.setLong(s, l);
    }

    @Override
    public void setFloat(String s, float v) throws SQLException {
        callableStatement.setFloat(s, v);
    }

    @Override
    public void setDouble(String s, double v) throws SQLException {
        callableStatement.setDouble(s, v);
    }

    @Override
    public void setBigDecimal(String s, BigDecimal bigDecimal) throws SQLException {
        callableStatement.setBigDecimal(s, bigDecimal);
    }

    @Override
    public void setString(String s, String s1) throws SQLException {
        callableStatement.setString(s, s1);
    }

    @Override
    public void setBytes(String s, byte[] bytes) throws SQLException {
        callableStatement.setBytes(s, bytes);
    }

    @Override
    public void setDate(String s, Date date) throws SQLException {
        callableStatement.setDate(s, date);
    }

    @Override
    public void setTime(String s, Time time) throws SQLException {
        callableStatement.setTime(s, time);
    }

    @Override
    public void setTimestamp(String s, Timestamp timestamp) throws SQLException {
        callableStatement.setTimestamp(s, timestamp);
    }

    @Override
    public void setAsciiStream(String s, InputStream inputStream, int i) throws SQLException {
        callableStatement.setAsciiStream(s, inputStream, i);
    }

    @Override
    public void setBinaryStream(String s, InputStream inputStream, int i) throws SQLException {
        callableStatement.setBinaryStream(s, inputStream, i);
    }

    @Override
    public void setObject(String s, Object o, int i, int i1) throws SQLException {
        callableStatement.setObject(s, o, i, i1);
    }

    @Override
    public void setObject(String s, Object o, int i) throws SQLException {
        callableStatement.setObject(s, o, i);
    }

    @Override
    public void setObject(String s, Object o) throws SQLException {
        callableStatement.setObject(s, o);
    }

    @Override
    public void setCharacterStream(String s, Reader reader, int i) throws SQLException {
        callableStatement.setCharacterStream(s, reader, i);
    }

    @Override
    public void setDate(String s, Date date, Calendar calendar) throws SQLException {
        callableStatement.setDate(s, date, calendar);
    }

    @Override
    public void setTime(String s, Time time, Calendar calendar) throws SQLException {
        callableStatement.setTime(s, time, calendar);
    }

    @Override
    public void setTimestamp(String s, Timestamp timestamp, Calendar calendar) throws SQLException {
        callableStatement.setTimestamp(s, timestamp, calendar);
    }

    @Override
    public void setNull(String s, int i, String s1) throws SQLException {
        callableStatement.setNull(s, i, s1);
    }

    @Override
    public String getString(String s) throws SQLException {
        return callableStatement.getString(s);
    }

    @Override
    public boolean getBoolean(String s) throws SQLException {
        return callableStatement.getBoolean(s);
    }

    @Override
    public byte getByte(String s) throws SQLException {
        return callableStatement.getByte(s);
    }

    @Override
    public short getShort(String s) throws SQLException {
        return callableStatement.getShort(s);
    }

    @Override
    public int getInt(String s) throws SQLException {
        return callableStatement.getInt(s);
    }

    @Override
    public long getLong(String s) throws SQLException {
        return callableStatement.getLong(s);
    }

    @Override
    public float getFloat(String s) throws SQLException {
        return callableStatement.getFloat(s);
    }

    @Override
    public double getDouble(String s) throws SQLException {
        return callableStatement.getDouble(s);
    }

    @Override
    public byte[] getBytes(String s) throws SQLException {
        return callableStatement.getBytes(s);
    }

    @Override
    public Date getDate(String s) throws SQLException {
        return callableStatement.getDate(s);
    }

    @Override
    public Time getTime(String s) throws SQLException {
        return callableStatement.getTime(s);
    }

    @Override
    public Timestamp getTimestamp(String s) throws SQLException {
        return callableStatement.getTimestamp(s);
    }

    @Override
    public Object getObject(String s) throws SQLException {
        return callableStatement.getObject(s);
    }

    @Override
    public BigDecimal getBigDecimal(String s) throws SQLException {
        return callableStatement.getBigDecimal(s);
    }

    @Override
    public Object getObject(String s, Map<String, Class<?>> map) throws SQLException {
        return callableStatement.getObject(s, map);
    }

    @Override
    public Ref getRef(String s) throws SQLException {
        return callableStatement.getRef(s);
    }

    @Override
    public Blob getBlob(String s) throws SQLException {
        return callableStatement.getBlob(s);
    }

    @Override
    public Clob getClob(String s) throws SQLException {
        return callableStatement.getClob(s);
    }

    @Override
    public Array getArray(String s) throws SQLException {
        return callableStatement.getArray(s);
    }

    @Override
    public Date getDate(String s, Calendar calendar) throws SQLException {
        return callableStatement.getDate(s, calendar);
    }

    @Override
    public Time getTime(String s, Calendar calendar) throws SQLException {
        return callableStatement.getTime(s, calendar);
    }

    @Override
    public Timestamp getTimestamp(String s, Calendar calendar) throws SQLException {
        return callableStatement.getTimestamp(s, calendar);
    }

    @Override
    public URL getURL(String s) throws SQLException {
        return callableStatement.getURL(s);
    }

    @Override
    public RowId getRowId(int i) throws SQLException {
        return callableStatement.getRowId(i);
    }

    @Override
    public RowId getRowId(String s) throws SQLException {
        return callableStatement.getRowId(s);
    }

    @Override
    public void setRowId(String s, RowId rowId) throws SQLException {
        callableStatement.setRowId(s, rowId);
    }

    @Override
    public void setNString(String s, String s1) throws SQLException {
        callableStatement.setNString(s, s1);
    }

    @Override
    public void setNCharacterStream(String s, Reader reader, long l) throws SQLException {
        callableStatement.setNCharacterStream(s, reader, l);
    }

    @Override
    public void setNClob(String s, NClob nClob) throws SQLException {
        callableStatement.setNClob(s, nClob);
    }

    @Override
    public void setClob(String s, Reader reader, long l) throws SQLException {
        callableStatement.setClob(s, reader, l);
    }

    @Override
    public void setBlob(String s, InputStream inputStream, long l) throws SQLException {
        callableStatement.setBlob(s, inputStream, l);
    }

    @Override
    public void setNClob(String s, Reader reader, long l) throws SQLException {
        callableStatement.setNClob(s, reader, l);
    }

    @Override
    public NClob getNClob(int i) throws SQLException {
        return callableStatement.getNClob(i);
    }

    @Override
    public NClob getNClob(String s) throws SQLException {
        return callableStatement.getNClob(s);
    }

    @Override
    public void setSQLXML(String s, SQLXML sqlxml) throws SQLException {
        callableStatement.setSQLXML(s, sqlxml);
    }

    @Override
    public SQLXML getSQLXML(int i) throws SQLException {
        return callableStatement.getSQLXML(i);
    }

    @Override
    public SQLXML getSQLXML(String s) throws SQLException {
        return callableStatement.getSQLXML(s);
    }

    @Override
    public String getNString(int i) throws SQLException {
        return callableStatement.getNString(i);
    }

    @Override
    public String getNString(String s) throws SQLException {
        return callableStatement.getNString(s);
    }

    @Override
    public Reader getNCharacterStream(int i) throws SQLException {
        return callableStatement.getNCharacterStream(i);
    }

    @Override
    public Reader getNCharacterStream(String s) throws SQLException {
        return callableStatement.getNCharacterStream(s);
    }

    @Override
    public Reader getCharacterStream(int i) throws SQLException {
        return callableStatement.getCharacterStream(i);
    }

    @Override
    public Reader getCharacterStream(String s) throws SQLException {
        return callableStatement.getCharacterStream(s);
    }

    @Override
    public void setBlob(String s, Blob blob) throws SQLException {
        callableStatement.setBlob(s, blob);
    }

    @Override
    public void setClob(String s, Clob clob) throws SQLException {
        callableStatement.setClob(s, clob);
    }

    @Override
    public void setAsciiStream(String s, InputStream inputStream, long l) throws SQLException {
        callableStatement.setAsciiStream(s, inputStream, l);
    }

    @Override
    public void setBinaryStream(String s, InputStream inputStream, long l) throws SQLException {
        callableStatement.setBinaryStream(s, inputStream, l);
    }

    @Override
    public void setCharacterStream(String s, Reader reader, long l) throws SQLException {
        callableStatement.setCharacterStream(s, reader, l);
    }

    @Override
    public void setAsciiStream(String s, InputStream inputStream) throws SQLException {
        callableStatement.setAsciiStream(s, inputStream);
    }

    @Override
    public void setBinaryStream(String s, InputStream inputStream) throws SQLException {
        callableStatement.setBinaryStream(s, inputStream);
    }

    @Override
    public void setCharacterStream(String s, Reader reader) throws SQLException {
        callableStatement.setCharacterStream(s, reader);
    }

    @Override
    public void setNCharacterStream(String s, Reader reader) throws SQLException {
        callableStatement.setNCharacterStream(s, reader);
    }

    @Override
    public void setClob(String s, Reader reader) throws SQLException {
        callableStatement.setClob(s, reader);
    }

    @Override
    public void setBlob(String s, InputStream inputStream) throws SQLException {
        callableStatement.setBlob(s, inputStream);
    }

    @Override
    public void setNClob(String s, Reader reader) throws SQLException {
        callableStatement.setNClob(s, reader);
    }

    @Override
    public <T> T getObject(int i, Class<T> aClass) throws SQLException {
        return callableStatement.getObject(i, aClass);
    }

    @Override
    public <T> T getObject(String s, Class<T> aClass) throws SQLException {
        return callableStatement.getObject(s, aClass);
    }
}
