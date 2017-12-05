package org.executequery.datasource;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class PooledResultSet implements ResultSet {

    private PooledStatement _stmt;

    private ResultSet _res;

    public PooledResultSet(PooledStatement _stmt, ResultSet rs) {
        this._stmt = _stmt;
        this._res = rs;
    }

    public Statement getStatement() throws SQLException {
        return _stmt;
    }

    /**
     * Wrapper for close of ResultSet which removes this
     * result set from being traced then calls close on
     * the original ResultSet.
     */
    public void close() throws SQLException {

        _res.close();

    }


    public boolean next() throws SQLException {
        return _res.next();
    }

    public boolean wasNull() throws SQLException {
        return _res.wasNull();
    }

    public String getString(int columnIndex) throws SQLException {
        return _res.getString(columnIndex);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return _res.getBoolean(columnIndex);
    }

    public byte getByte(int columnIndex) throws SQLException {
        return _res.getByte(columnIndex);
    }

    public short getShort(int columnIndex) throws SQLException {
        return _res.getShort(columnIndex);
    }

    public int getInt(int columnIndex) throws SQLException {
        return _res.getInt(columnIndex);
    }

    public long getLong(int columnIndex) throws SQLException {
        return _res.getLong(columnIndex);
    }

    public float getFloat(int columnIndex) throws SQLException {
        return _res.getFloat(columnIndex);
    }

    public double getDouble(int columnIndex) throws SQLException {
        return _res.getDouble(columnIndex);
    }

    /**
     * @deprecated
     */
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return _res.getBigDecimal(columnIndex);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return _res.getBytes(columnIndex);
    }

    public Date getDate(int columnIndex) throws SQLException {
        return _res.getDate(columnIndex);
    }

    public Time getTime(int columnIndex) throws SQLException {
        return _res.getTime(columnIndex);
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return _res.getTimestamp(columnIndex);
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return _res.getAsciiStream(columnIndex);
    }

    /**
     * @deprecated
     */
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return _res.getUnicodeStream(columnIndex);
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return _res.getBinaryStream(columnIndex);
    }

    public String getString(String columnName) throws SQLException {
        return _res.getString(columnName);
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return _res.getBoolean(columnName);
    }

    public byte getByte(String columnName) throws SQLException {
        return _res.getByte(columnName);
    }

    public short getShort(String columnName) throws SQLException {
        return _res.getShort(columnName);
    }

    public int getInt(String columnName) throws SQLException {
        return _res.getInt(columnName);
    }

    public long getLong(String columnName) throws SQLException {
        return _res.getLong(columnName);
    }

    public float getFloat(String columnName) throws SQLException {
        return _res.getFloat(columnName);
    }

    public double getDouble(String columnName) throws SQLException {
        return _res.getDouble(columnName);
    }

    /**
     * @deprecated
     */
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return _res.getBigDecimal(columnName);
    }

    public byte[] getBytes(String columnName) throws SQLException {
        return _res.getBytes(columnName);
    }

    public Date getDate(String columnName) throws SQLException {
        return _res.getDate(columnName);
    }

    public Time getTime(String columnName) throws SQLException {
        return _res.getTime(columnName);
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        return _res.getTimestamp(columnName);
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        return _res.getAsciiStream(columnName);
    }

    /**
     * @deprecated
     */
    public InputStream getUnicodeStream(String columnName) throws SQLException {
        return _res.getUnicodeStream(columnName);
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        return _res.getBinaryStream(columnName);
    }

    public SQLWarning getWarnings() throws SQLException {
        return _res.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        _res.clearWarnings();
    }

    public String getCursorName() throws SQLException {
        return _res.getCursorName();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return _res.getMetaData();
    }

    public Object getObject(int columnIndex) throws SQLException {
        return _res.getObject(columnIndex);
    }

    public Object getObject(String columnName) throws SQLException {
        return _res.getObject(columnName);
    }

    public int findColumn(String columnName) throws SQLException {
        return _res.findColumn(columnName);
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return _res.getCharacterStream(columnIndex);
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        return _res.getCharacterStream(columnName);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return _res.getBigDecimal(columnIndex);
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return _res.getBigDecimal(columnName);
    }

    public boolean isBeforeFirst() throws SQLException {
        return _res.isBeforeFirst();
    }

    public boolean isAfterLast() throws SQLException {
        return _res.isAfterLast();
    }

    public boolean isFirst() throws SQLException {
        return _res.isFirst();
    }

    public boolean isLast() throws SQLException {
        return _res.isLast();
    }

    public void beforeFirst() throws SQLException {
        _res.beforeFirst();
    }

    public void afterLast() throws SQLException {
        _res.afterLast();
    }

    public boolean first() throws SQLException {
        return _res.first();
    }

    public boolean last() throws SQLException {
        return _res.last();
    }

    public int getRow() throws SQLException {
        return _res.getRow();
    }

    public boolean absolute(int row) throws SQLException {
        return _res.absolute(row);
    }

    public boolean relative(int rows) throws SQLException {
        return _res.relative(rows);
    }

    public boolean previous() throws SQLException {
        return _res.previous();
    }

    public void setFetchDirection(int direction) throws SQLException {
        _res.setFetchDirection(direction);
    }

    public int getFetchDirection() throws SQLException {
        return _res.getFetchDirection();
    }

    public void setFetchSize(int rows) throws SQLException {
        _res.setFetchSize(rows);
    }

    public int getFetchSize() throws SQLException {
        return _res.getFetchSize();
    }

    public int getType() throws SQLException {
        return _res.getType();
    }

    public int getConcurrency() throws SQLException {
        return _res.getConcurrency();
    }

    public boolean rowUpdated() throws SQLException {
        return _res.rowUpdated();
    }

    public boolean rowInserted() throws SQLException {
        return _res.rowInserted();
    }

    public boolean rowDeleted() throws SQLException {
        return _res.rowDeleted();
    }

    public void updateNull(int columnIndex) throws SQLException {
        _res.updateNull(columnIndex);
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        _res.updateBoolean(columnIndex, x);
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        _res.updateByte(columnIndex, x);
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        _res.updateShort(columnIndex, x);
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        _res.updateInt(columnIndex, x);
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        _res.updateLong(columnIndex, x);
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        _res.updateFloat(columnIndex, x);
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        _res.updateDouble(columnIndex, x);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        _res.updateBigDecimal(columnIndex, x);
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        _res.updateString(columnIndex, x);
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        _res.updateBytes(columnIndex, x);
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        _res.updateDate(columnIndex, x);
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        _res.updateTime(columnIndex, x);
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        _res.updateTimestamp(columnIndex, x);
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        _res.updateAsciiStream(columnIndex, x, length);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        _res.updateBinaryStream(columnIndex, x, length);
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        _res.updateCharacterStream(columnIndex, x, length);
    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        _res.updateObject(columnIndex, x);
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        _res.updateObject(columnIndex, x);
    }

    public void updateNull(String columnName) throws SQLException {
        _res.updateNull(columnName);
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
        _res.updateBoolean(columnName, x);
    }

    public void updateByte(String columnName, byte x) throws SQLException {
        _res.updateByte(columnName, x);
    }

    public void updateShort(String columnName, short x) throws SQLException {
        _res.updateShort(columnName, x);
    }

    public void updateInt(String columnName, int x) throws SQLException {
        _res.updateInt(columnName, x);
    }

    public void updateLong(String columnName, long x) throws SQLException {
        _res.updateLong(columnName, x);
    }

    public void updateFloat(String columnName, float x) throws SQLException {
        _res.updateFloat(columnName, x);
    }

    public void updateDouble(String columnName, double x) throws SQLException {
        _res.updateDouble(columnName, x);
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        _res.updateBigDecimal(columnName, x);
    }

    public void updateString(String columnName, String x) throws SQLException {
        _res.updateString(columnName, x);
    }

    public void updateBytes(String columnName, byte[] x) throws SQLException {
        _res.updateBytes(columnName, x);
    }

    public void updateDate(String columnName, Date x) throws SQLException {
        _res.updateDate(columnName, x);
    }

    public void updateTime(String columnName, Time x) throws SQLException {
        _res.updateTime(columnName, x);
    }

    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        _res.updateTimestamp(columnName, x);
    }

    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        _res.updateAsciiStream(columnName, x, length);
    }

    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        _res.updateBinaryStream(columnName, x, length);
    }

    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        _res.updateCharacterStream(columnName, reader, length);
    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        _res.updateObject(columnName, x);
    }

    public void updateObject(String columnName, Object x) throws SQLException {
        _res.updateObject(columnName, x);
    }

    public void insertRow() throws SQLException {
        _res.insertRow();
    }

    public void updateRow() throws SQLException {
        _res.updateRow();
    }

    public void deleteRow() throws SQLException {
        _res.deleteRow();
    }

    public void refreshRow() throws SQLException {
        _res.refreshRow();
    }

    public void cancelRowUpdates() throws SQLException {
        _res.cancelRowUpdates();
    }

    public void moveToInsertRow() throws SQLException {
        _res.moveToInsertRow();
    }

    public void moveToCurrentRow() throws SQLException {
        _res.moveToCurrentRow();
    }

    public Object getObject(int i, Map map) throws SQLException {
        return _res.getObject(i, map);
    }

    public Ref getRef(int i) throws SQLException {
        return _res.getRef(i);
    }

    public Blob getBlob(int i) throws SQLException {
        return _res.getBlob(i);
    }

    public Clob getClob(int i) throws SQLException {
        return _res.getClob(i);
    }

    public Array getArray(int i) throws SQLException {
        return _res.getArray(i);
    }

    public Object getObject(String colName, Map map) throws SQLException {
        return _res.getObject(colName, map);
    }

    public Ref getRef(String colName) throws SQLException {
        return _res.getRef(colName);
    }

    public Blob getBlob(String colName) throws SQLException {
        return _res.getBlob(colName);
    }

    public Clob getClob(String colName) throws SQLException {
        return _res.getClob(colName);
    }

    public Array getArray(String colName) throws SQLException {
        return _res.getArray(colName);
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return _res.getDate(columnIndex, cal);
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return _res.getDate(columnName, cal);
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return _res.getTime(columnIndex, cal);
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return _res.getTime(columnName, cal);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return _res.getTimestamp(columnIndex, cal);
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return _res.getTimestamp(columnName, cal);
    }


    public java.net.URL getURL(int columnIndex) throws SQLException {
        return _res.getURL(columnIndex);
    }

    public java.net.URL getURL(String columnName) throws SQLException {
        return _res.getURL(columnName);
    }

    public void updateRef(int columnIndex, java.sql.Ref x) throws SQLException {
        _res.updateRef(columnIndex, x);
    }

    public void updateRef(String columnName, java.sql.Ref x) throws SQLException {
        _res.updateRef(columnName, x);
    }

    public void updateBlob(int columnIndex, java.sql.Blob x) throws SQLException {
        _res.updateBlob(columnIndex, x);
    }

    public void updateBlob(String columnName, java.sql.Blob x) throws SQLException {
        _res.updateBlob(columnName, x);
    }

    public void updateClob(int columnIndex, java.sql.Clob x) throws SQLException {
        _res.updateClob(columnIndex, x);
    }

    public void updateClob(String columnName, java.sql.Clob x) throws SQLException {
        _res.updateClob(columnName, x);
    }

    public void updateArray(int columnIndex, java.sql.Array x) throws SQLException {
        _res.updateArray(columnIndex, x);
    }

    public void updateArray(String columnName, java.sql.Array x) throws SQLException {
        _res.updateArray(columnName, x);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(getClass()) || _res.isWrapperFor(iface);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        } else if (iface.isAssignableFrom(_res.getClass())) {
            return iface.cast(_res);
        } else {
            return _res.unwrap(iface);
        }
    }

    public RowId getRowId(int columnIndex) throws SQLException {

        return _res.getRowId(columnIndex);
    }


    public RowId getRowId(String columnLabel) throws SQLException {

        return _res.getRowId(columnLabel);
    }

    public void updateRowId(int columnIndex, RowId value) throws SQLException {

        _res.updateRowId(columnIndex, value);
    }

    public void updateRowId(String columnLabel, RowId value) throws SQLException {

        _res.updateRowId(columnLabel, value);
    }

    public int getHoldability() throws SQLException {

        return _res.getHoldability();
    }


    public boolean isClosed() throws SQLException {

        return _res.isClosed();
    }


    public void updateNString(int columnIndex, String value) throws SQLException {

        _res.updateNString(columnIndex, value);
    }


    public void updateNString(String columnLabel, String value) throws SQLException {

        _res.updateNString(columnLabel, value);
    }


    public void updateNClob(int columnIndex, NClob value) throws SQLException {

        _res.updateNClob(columnIndex, value);
    }


    public void updateNClob(String columnLabel, NClob value) throws SQLException {

        _res.updateNClob(columnLabel, value);
    }


    public NClob getNClob(int columnIndex) throws SQLException {

        return _res.getNClob(columnIndex);
    }


    public NClob getNClob(String columnLabel) throws SQLException {

        return _res.getNClob(columnLabel);
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {

        return _res.getSQLXML(columnIndex);
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {

        return _res.getSQLXML(columnLabel);
    }

    public void updateSQLXML(int columnIndex, SQLXML value) throws SQLException {

        _res.updateSQLXML(columnIndex, value);
    }

    public void updateSQLXML(String columnLabel, SQLXML value) throws SQLException {

        _res.updateSQLXML(columnLabel, value);
    }

    public String getNString(int columnIndex) throws SQLException {

        return _res.getNString(columnIndex);
    }

    public String getNString(String columnLabel) throws SQLException {

        return _res.getNString(columnLabel);
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {

        return _res.getNCharacterStream(columnIndex);
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {

        return _res.getNCharacterStream(columnLabel);
    }

    public void updateNCharacterStream(int columnIndex, Reader reader, long length) throws SQLException {

        _res.updateNCharacterStream(columnIndex, reader, length);
    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

        _res.updateNCharacterStream(columnLabel, reader, length);
    }

    public void updateAsciiStream(int columnIndex, InputStream inputStream, long length) throws SQLException {

        _res.updateAsciiStream(columnIndex, inputStream, length);
    }

    public void updateBinaryStream(int columnIndex, InputStream inputStream, long length) throws SQLException {

        _res.updateBinaryStream(columnIndex, inputStream, length);
    }


    public void updateCharacterStream(int columnIndex, Reader reader, long length) throws SQLException {

        _res.updateCharacterStream(columnIndex, reader, length);
    }


    public void updateAsciiStream(String columnLabel, InputStream inputStream, long length) throws SQLException {

        _res.updateAsciiStream(columnLabel, inputStream, length);
    }

    public void updateBinaryStream(String columnLabel, InputStream inputStream, long length) throws SQLException {

        _res.updateBinaryStream(columnLabel, inputStream, length);
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

        _res.updateCharacterStream(columnLabel, reader, length);
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

        _res.updateBlob(columnIndex, inputStream, length);
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

        _res.updateBlob(columnLabel, inputStream, length);
    }

    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

        _res.updateClob(columnIndex, reader, length);
    }

    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

        _res.updateClob(columnLabel, reader, length);
    }

    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

        _res.updateNClob(columnIndex, reader, length);
    }

    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

        _res.updateNClob(columnLabel, reader, length);
    }

    public void updateNCharacterStream(int columnIndex, Reader reader) throws SQLException {

        _res.updateNCharacterStream(columnIndex, reader);
    }

    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

        _res.updateNCharacterStream(columnLabel, reader);
    }

    public void updateAsciiStream(int columnIndex, InputStream inputStream) throws SQLException {

        _res.updateAsciiStream(columnIndex, inputStream);
    }

    public void updateBinaryStream(int columnIndex, InputStream inputStream) throws SQLException {

        _res.updateBinaryStream(columnIndex, inputStream);
    }

    public void updateCharacterStream(int columnIndex, Reader reader) throws SQLException {

        _res.updateCharacterStream(columnIndex, reader);
    }

    public void updateAsciiStream(String columnLabel, InputStream inputStream) throws SQLException {

        _res.updateAsciiStream(columnLabel, inputStream);
    }

    public void updateBinaryStream(String columnLabel, InputStream inputStream) throws SQLException {

        _res.updateBinaryStream(columnLabel, inputStream);
    }

    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

        _res.updateCharacterStream(columnLabel, reader);
    }

    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

        _res.updateBlob(columnIndex, inputStream);
    }

    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

        _res.updateBlob(columnLabel, inputStream);
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {

        _res.updateClob(columnIndex, reader);
    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException {

        _res.updateClob(columnLabel, reader);
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {

        _res.updateNClob(columnIndex, reader);
    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException {

        _res.updateNClob(columnLabel, reader);
    }

    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return _res.getObject(columnIndex, type);
    }

    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return _res.getObject(columnLabel, type);
    }

    public ResultSet getResultSet() {
        return _res;
    }
}
