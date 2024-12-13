/*
 * ResultSetTableModel.java
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

package org.executequery.gui.resultset;

import biz.redsoft.IFBBlob;
import biz.redsoft.IFBClob;
import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.Types;
import org.executequery.datasource.PooledConnection;
import org.executequery.datasource.PooledResultSet;
import org.executequery.datasource.PooledStatement;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.executequery.util.UserProperties;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.table.AbstractSortableTableModel;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.text.ParseException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The sql result set table model.
 *
 * @author Takis Diakoumis
 */
public class ResultSetTableModel extends AbstractSortableTableModel {

    /**
     * Whether the meta data should be generated
     */
    private boolean holdMetaData;

    /**
     * The maximum number of records displayed
     */
    private int maxRecords;

    /**
     * Indicates that the query executing has been interrupted
     */
    private boolean interrupted;

    private final List<ResultSetColumnHeader> columnHeaders;

    private final List<ResultSetColumnHeader> visibleColumnHeaders;

    /**
     * The table values
     */
    private List<List<RecordDataItem>> tableData;

    /**
     * result set meta data model
     */
    private ResultSetMetaDataTableModel metaDataTableModel;

    private RecordDataItemFactory recordDataItemFactory;

    private List<RecordDataItem> deletedRow;

    private String query;

    private List<ColumnData> columnDataList;

    boolean isTable;

    DefaultStatementExecutor executor;

    private ResultSetTable table;

    SQLException exception;

    public ResultSetTableModel(boolean isTable) throws SQLException {

        this(null, -1, isTable);
    }

    public ResultSetTableModel(int maxRecords, boolean isTable) throws SQLException {

        this(null, maxRecords, isTable);
    }

    public ResultSetTableModel(ResultSet resultSet, int maxRecords, boolean isTable) throws SQLException {

        this(resultSet, maxRecords, null, isTable);
    }

    private DatabaseConnection databaseConnection;

    public ResultSetTableModel(List<String> columnHeaders, List<List<RecordDataItem>> tableData) {

        this.tableData = tableData;
        this.columnHeaders = createHeaders(columnHeaders);
        visibleColumnHeaders = new ArrayList<ResultSetColumnHeader>();
        resetVisibleColumnHeaders();
    }

    public List<ResultSetColumnHeader> getColumnHeaders() {

        return columnHeaders;
    }

    private List<ResultSetColumnHeader> createHeaders(List<String> columnHeaders) {

        int index = 0;
        List<ResultSetColumnHeader> list = new ArrayList<ResultSetColumnHeader>();
        for (String columnHeader : columnHeaders) {

            list.add(new ResultSetColumnHeader(index++, columnHeader));
        }

        return list;
    }

    public String getQuery() {
        return query;
    }

    public synchronized void createTable(ResultSet resultSet) throws SQLException {
        createTable(resultSet, null);
    }

    int zeroBaseIndex;
    int fetchSize;
    boolean rsClose;
    ResultSet rs;
    int count;
    private int recordCount;
    public ResultSetTableModel(ResultSet resultSet, int maxRecords, String query, boolean isTable) throws SQLException {

        this.maxRecords = maxRecords;
        this.query = query;
        this.isTable = isTable;

        table = new ResultSetTable();
        tableData = new ArrayList<>();
        columnHeaders = new ArrayList<>();
        visibleColumnHeaders = new ArrayList<>();
        recordDataItemFactory = new RecordDataItemFactory();

        holdMetaData = UserProperties.getInstance().getBooleanProperty("editor.results.metadata");

        if (resultSet != null) {
            createTable(resultSet);
        }
    }

    public synchronized void createTable(ResultSet resultSet, List<ColumnData> columnDataList) throws SQLException {

        if (!isOpenAndValid(resultSet)) {

            clearData();
            return;
        }

        try {
            resetMetaData();
            ResultSetMetaData rsmd = resultSet.getMetaData();
            try {
                databaseConnection = ((PooledStatement) resultSet.getStatement()).getPooledConnection().getDatabaseConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }

            columnHeaders.clear();
            visibleColumnHeaders.clear();
            tableData.clear();

            int zeroBaseIndex = 0;
            int count = rsmd.getColumnCount();
            for (int i = 1; i <= count; i++) {

                zeroBaseIndex = i - 1;

                columnHeaders.add(
                        new ResultSetColumnHeader(zeroBaseIndex,
                                rsmd.getColumnLabel(i),
                                rsmd.getColumnName(i),
                                rsmd.getColumnType(i),
                                rsmd.getColumnTypeName(i),
                                rsmd));
            }
            interrupted = false;

            if (holdMetaData) {

                setMetaDataVectors(rsmd);
            }
            getDataForTable(resultSet, count, columnDataList);

        } catch (SQLException e) {

            // TODO make sure that all resources are released
            if (!e.getMessage().equalsIgnoreCase("Look at a column before testing null.")
                    && !e.getMessage().equalsIgnoreCase("Result set is already closed.")) {
                throw e;
                //System.err.println("SQL error populating table model at: " + e.getMessage());
                //Log.debug("Table model error - " + e.getMessage(), e);
            }

        } catch (Exception e) {

            if (e instanceof InterruptedException) {

                Log.debug("ResultSet generation interrupted.", e);

            } else {

                String message = e.getMessage();
                if (StringUtils.isBlank(message)) {

                    System.err.println("Exception populating table model.");

                } else {

                    System.err.println("Exception populating table model at: " + message);
                }

                Log.debug("Table model error - ", e);
            }

        } finally {
            if (!isTable) {
                if (resultSet != null) {

                    try {

                        Statement statement = resultSet.getStatement();
                        resultSet.close();

                        if (statement != null) {
                            if (!statement.isClosed())
                                statement.close();
                        }

                    } catch (SQLException e) {
                    }

                }
            }
        }

    }

    private boolean fetchAll = false;
    private boolean cancelled = false;


    public synchronized void getDataForTable(ResultSet resultSet, int count, List<ColumnData> columnDataList) throws SQLException, InterruptedException {
        recordCount = 0;
        this.columnDataList = columnDataList;
        long time = System.currentTimeMillis();
        fetchSize = SystemProperties.getIntProperty("user", "results.table.fetch.size");
        rsClose = false;
        rs = resultSet;
        this.count = count;
        if (isTable)
            for (int i = 0; i < fetchSize && !rsClose; i++) {
                fetchOneRecord(resultSet, count);
            }
        else if (maxRecords > 0) {
            for (int i = 0; i < maxRecords && !rsClose; i++) {
                fetchOneRecord(resultSet, count);
            }
        } else
            fetchAllRecords(resultSet, count);
        if (Log.isTraceEnabled()) {

            Log.trace("Finished populating table model - " + recordCount + " rows - [ "
                    + MiscUtils.formatDuration(System.currentTimeMillis() - time) + "]");
        }

        fireTableStructureChanged();

    }

    public void setFetchAll(boolean fetchAll) {
        this.fetchAll = fetchAll;
    }

    public boolean isResultSetClose() {
        return rsClose;
    }

    public void fetchMoreData() {
        if (!rsClose)
            try {
                if (fetchAll) {
                    fetchAllRecords(rs, count);
                } else {
                    for (int i = 0; i < fetchSize && !rsClose; i++) {
                        fetchOneRecord(rs, count);
                    }
                    fireTableDataChanged();
                }
            } catch (Exception e) {
                rsClose = true;
                if (cancelled) {
                    cancelled = false;
                    fetchAll = false;
                } else
                    GUIUtilities.displayExceptionErrorDialog("Error loading data", e, this.getClass());
                fireTableDataChanged();
            } finally {
                fetchAll = false;
            }
    }

    private void fetchOneRecord(ResultSet resultSet, int count) throws SQLException, InterruptedException {
        try {
            if (resultSet.next())
                addingRecord(resultSet, count);
            else {
                resultSet.close();
                rsClose = true;
            }
        } catch (SQLException e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
            exception = e;
            resultSet.close();
            rsClose = true;
        }
    }

    private void fetchAllRecords(ResultSet resultSet, int count) throws SQLException, InterruptedException {
        try {
            while (resultSet.next())
                addingRecord(resultSet, count);
        } catch (SQLException e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
            exception = e;
        } finally {
            fireTableDataChanged();
            resultSet.close();
            rsClose = true;
        }

    }

    public synchronized void createTableFromMetaData(ResultSet resultSet, DatabaseConnection dc, List<ColumnData> columnDataList) {

        if (!isOpenAndValid(resultSet)) {

            clearData();
            return;
        }
        if (executor != null)
            executor.releaseResources();
        executor = new DefaultStatementExecutor(dc, true);
        databaseConnection = dc;
        try {
            resetMetaData();

            columnHeaders.clear();
            visibleColumnHeaders.clear();
            tableData.clear();
            String tableName = "";
            int zeroBaseIndex = 0;
            int g = 1;
            while (resultSet.next()) {

                zeroBaseIndex = g - 1;

                columnHeaders.add(
                        new ResultSetColumnHeader(zeroBaseIndex,
                                resultSet.getString(4),
                                resultSet.getString(4),
                                resultSet.getInt(5),
                                resultSet.getString(6),
                                resultSet.getInt(7)
                        ));
                tableName = resultSet.getString(3);
                g++;
            }
            Statement st = resultSet.getStatement();
            if (st != null)
                if (!st.isClosed()) {
                    st.close();
                }
            int count = g - 1;

            int recordCount = 0;
            interrupted = false;

            /*if (holdMetaData) {

                setMetaDataVectors(rsmd);
            }*/
            //List<String> errorCols=new ArrayList<>();
            String sql = "SELECT ";
            for (int i = 0; i < count; i++) {
                try {
                    String query = "SELECT " + columnHeaders.get(i).getName() + " FROM " + tableName;
                    SqlStatementResult result = executor.execute(QueryTypes.SELECT, query);
                    ResultSet rs = result.getResultSet();
                    if (rs != null)
                        sql += columnHeaders.get(i).getName();
                    else {
                        sql += "'" + result.getErrorMessage() + "' as " + columnHeaders.get(i).getName();
                        columnHeaders.get(i).setEditable(false);
                    }

                } catch (Exception e) {
                    Log.error("Error get result set from metadata" + e.getMessage());

                }
                if (i < count - 1)
                    sql += ", ";
                executor.releaseResources();

            }
            sql += " FROM " + tableName;
            resultSet = executor.execute(QueryTypes.SELECT, sql).getResultSet();
            getDataForTable(resultSet, count, columnDataList);

        } catch (SQLException e) {

            System.err.println("SQL error populating table model at: " + e.getMessage());
            Log.debug("Table model error - " + e.getMessage(), e);

        } catch (Exception e) {

            if (e instanceof InterruptedException) {

                Log.error("ResultSet generation interrupted.", e);

            } else {

                String message = e.getMessage();
                if (StringUtils.isBlank(message)) {

                    System.err.println("Exception populating table model.");

                } else {

                    System.err.println("Exception populating table model at: " + message);
                }

                Log.debug("Table model error - ", e);
            }

        }

    }

    private void asStringOrObject(RecordDataItem value, ResultSet resultSet, int column) throws SQLException {

        // often getString returns a more useful representation
        // return using getString where object.toString is the default impl

        Object valueAsObject = resultSet.getObject(column);
        String valueAsString = resultSet.getString(column);

        if (valueAsObject != null) {

            String valueAsObjectToString = valueAsObject.toString();
            String toString = valueAsObject.getClass().getName() + "@" + Integer.toHexString(valueAsObject.hashCode());
            if (!StringUtils.equals(valueAsObjectToString, toString)) {

                valueAsString = valueAsObjectToString;
            }
        }

        value.setValue(valueAsString);
    }

    private boolean isOpenAndValid(ResultSet resultSet) {

        try {

            if (resultSet != null) {

                try {

                    return !resultSet.isClosed();

                } catch (IllegalAccessError e) {

                    // possible jt400 issue

                    return false;
                }

            }

            return false;

        } catch (SQLException e) {

            Log.debug("Error checking if result set is open and valid - " + e.getMessage());
            return false;
        }
    }

    private void resetMetaData() {
        if (metaDataTableModel != null) {

            metaDataTableModel.reset();
        }
    }

    private void clearData() {

        if (tableData != null) {

            tableData.clear();

        } else {

            tableData = new ArrayList<List<RecordDataItem>>(0);
        }

        fireTableStructureChanged();
    }

    private void addingRecord(ResultSet resultSet, int count) throws SQLException, InterruptedException {


        if (interrupted || Thread.interrupted()) {

            throw new InterruptedException();
        }

        recordCount++;
        List<RecordDataItem> rowData = new ArrayList<RecordDataItem>(count);

        for (int i = 1; i <= count; i++) {

            zeroBaseIndex = i - 1;

            ResultSetColumnHeader header = columnHeaders.get(zeroBaseIndex);
            RecordDataItem value = recordDataItemFactory.create(header);

            try {

                int dataType = header.getDataType();
                switch (dataType) {

                    // some drivers (informix for example)
                    // was noticed to return the hashcode from
                    // getObject for -1 data types (eg. longvarchar).
                    // force string for these - others stick with
                    // getObject() for default value formatting

                    case Types.CHAR:
                    case Types.VARCHAR:
                        value.setValue(resultSet.getString(i));
                        break;
                    case Types.TIME_WITH_TIMEZONE:
                        value.setValue(resultSet.getObject(i, OffsetTime.class));
                        break;
                    case Types.TIMESTAMP_WITH_TIMEZONE:
                        value.setValue(resultSet.getObject(i, OffsetDateTime.class));
                        break;
                    case Types.DATE:
                        value.setValue(resultSet.getObject(i, LocalDate.class));
                        break;
                    case Types.TIME:
                        value.setValue(resultSet.getObject(i, LocalTime.class));
                        break;
                    case Types.TIMESTAMP:
                        value.setValue(resultSet.getObject(i, LocalDateTime.class));
                        break;
                    case Types.LONGVARCHAR:
                    case Types.CLOB:
                        Clob clob = resultSet.getClob(i);
                        if (clob != null && clob.getClass().getName().contains("org.firebirdsql.jdbc")) {
                            try {
                                PooledResultSet pooledResultSet = (PooledResultSet) resultSet;
                                PooledConnection connection = (PooledConnection) pooledResultSet.getStatement().getConnection();
                                Connection unwrapCon = connection.unwrap(Connection.class);
                                IFBClob ifbClob = (IFBClob) DynamicLibraryLoader.loadingObjectFromClassLoader(connection.getDatabaseConnection().getDriverMajorVersion(), unwrapCon, "FBClobImpl");
                                ifbClob.detach(clob, ((PooledStatement) pooledResultSet.getStatement()).getStatement());
                                value.setValue(ifbClob);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            value.setValue(clob);
                        }
                        if (columnDataList != null) {
                            ((ClobRecordDataItem) value).setCharset(columnDataList.get(i - 1).getCharset());
                            if (MiscUtils.isNull(columnDataList.get(i - 1).getCharset()) || Objects.equals(columnDataList.get(i - 1).getCharset(), CreateTableSQLSyntax.NONE))
                                ((ClobRecordDataItem) value).setCharset(columnDataList.get(i - 1).getConnection().getCharset());
                        } else ((ClobRecordDataItem) value).setCharset(CreateTableSQLSyntax.NONE);
                        break;
                    case Types.LONGVARBINARY:
                    case Types.VARBINARY:
                    case Types.BINARY:
                        value.setValue(resultSet.getBytes(i));
                        break;
                    case Types.BLOB:
                        Blob blob = resultSet.getBlob(i);
                        if (blob != null && blob.getClass().getName().contains("org.firebirdsql.jdbc")) {
                            try {
                                PooledResultSet pooledResultSet = (PooledResultSet) resultSet;
                                PooledConnection connection = (PooledConnection) pooledResultSet.getStatement().getConnection();
                                Connection unwrapCon = connection.unwrap(Connection.class);
                                IFBBlob ifbBlob = (IFBBlob) DynamicLibraryLoader.loadingObjectFromClassLoader(connection.getDatabaseConnection().getDriverMajorVersion(), unwrapCon, "FBBlobImpl");
                                ifbBlob.detach(blob, ((PooledStatement) pooledResultSet.getStatement()).getStatement());
                                value.setValue(ifbBlob);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            value.setValue(blob);
                        }
                        break;
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.INT128:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                    case Types.NULL:
                    case Types.OTHER:
                    case Types.JAVA_OBJECT:
                    case Types.DISTINCT:
                    case Types.STRUCT:
                    case Types.ARRAY:
                    case Types.REF:
                    case Types.DATALINK:
                    case Types.BOOLEAN:
                    case Types.ROWID:
                    case Types.NCHAR:
                    case Types.NVARCHAR:
                    case Types.LONGNVARCHAR:
                    case Types.NCLOB:
                    case Types.SQLXML:

                        // use getObject for all other known types

                        value.setValue(resultSet.getObject(i));
                        break;

                    default:

                        // otherwise try as string

                        asStringOrObject(value, resultSet, i);
                        break;
                }

            } catch (Exception e) {
                try {
                    // ... and on dump, resort to string
                    value.setValue(resultSet.getString(i));

                } catch (SQLException sqlException) {
                    // catch-all SQLException - yes, this is hideous
                    // noticed with invalid date formatted values in mysql
                    value.setValue("<Error - " + sqlException.getMessage() + ">");
                }
            }

            if (resultSet.wasNull())
                value.setNull();

            rowData.add(value);
            if (value.getDisplayValue() != null) {

                int width = -1;
                FontMetrics metrics = table.getFontMetrics(table.getFont());

                int valueType = value.getDataType();
                if (valueType == Types.DATE) {
                    String stringValue = SystemProperties.getProperty("user", "results.date.pattern");
                    if (!MiscUtils.isNull(stringValue))
                        width = metrics.stringWidth(stringValue);

                } else if (valueType == Types.TIME) {
                    String stringValue = SystemProperties.getProperty("user", "results.time.pattern");
                    if (!MiscUtils.isNull(stringValue))
                        width = metrics.stringWidth(stringValue);

                } else if (valueType == Types.TIMESTAMP) {
                    String stringValue = SystemProperties.getProperty("user", "results.timestamp.pattern");
                    if (!MiscUtils.isNull(stringValue))
                        width = metrics.stringWidth(stringValue);

                } else if (valueType == Types.TIME_WITH_TIMEZONE) {
                    String stringValue = SystemProperties.getProperty("user", "results.time.timezone.pattern");
                    if (!MiscUtils.isNull(stringValue))
                        width = metrics.stringWidth(stringValue);

                } else if (valueType == Types.TIMESTAMP_WITH_TIMEZONE) {
                    String stringValue = SystemProperties.getProperty("user", "results.timestamp.timezone.pattern");
                    if (!MiscUtils.isNull(stringValue))
                        width = metrics.stringWidth(stringValue);
                }

                if (width < 0)
                    width = metrics.stringWidth(value.getDisplayValue().toString());

                if (width > header.getColWidth())
                    header.setColWidth(width + 5);
            }
        }

        tableData.add(rowData);
    }

    public void cancelFetch() {
        cancelled = true;
    }

    public void interrupt() {

        interrupted = true;
    }

    public void setHoldMetaData(boolean holdMetaData) {

        this.holdMetaData = holdMetaData;
    }

    private static final String STRING = "String";
    private static final String GET = "get";
    private static final String EXCLUDES = "getColumnCount";
    private static final String COLUMN_NAME = "ColumnName";

    private void setMetaDataVectors(ResultSetMetaData rsmd) {

        Class<?> metaClass = rsmd.getClass();
        Method[] metaMethods = metaClass.getMethods();

        List<String> columns = null;
        List<String> rowData = null;
        List<List<String>> metaData = null;

        try {

            int columnCount = rsmd.getColumnCount();
            columns = new ArrayList<String>(metaMethods.length - 1);
            metaData = new ArrayList<List<String>>(columnCount);

            Object[] obj = new Object[1];
            for (int j = 1; j <= columnCount; j++) {

                obj[0] = Integer.valueOf(j);
                rowData = new ArrayList<String>(metaMethods.length - 1);
                for (int i = 0; i < metaMethods.length; i++) {

                    String methodName = metaMethods[i].getName();
                    if (EXCLUDES.contains(methodName)) {

                        continue;
                    }

                    Class<?> c = metaMethods[i].getReturnType();

                    if (c.isPrimitive() || c.getName().endsWith(STRING)) {

                        if (methodName.startsWith(GET)) {

                            methodName = methodName.substring(3);
                        }

                        try {

                            Object res = metaMethods[i].invoke(rsmd, obj);

                            if (methodName.equals(COLUMN_NAME)) {

                                if (j == 1) {

                                    columns.add(0, methodName);
                                }

                                rowData.add(0, objectToString(res));

                            } else {

                                if (j == 1) {

                                    columns.add(methodName);
                                }

                                rowData.add(objectToString(res));

                            }

                        } catch (AbstractMethodError e) {
                        } catch (IllegalArgumentException e) {
                        } catch (IllegalAccessException e) {
                        } catch (InvocationTargetException e) {
                        }

                    }

                }

                metaData.add(rowData);

            }

        } catch (SQLException e) {

            Log.debug(e.getMessage(), e);
        }

        if (metaDataTableModel == null) {

            metaDataTableModel = new ResultSetMetaDataTableModel();
        }

        metaDataTableModel.setValues(columns, metaData);
    }

    private String objectToString(Object res) {

        String value = null;

        if (res != null) {

            value = res.toString();

        } else {

            value = "";
        }

        return value;
    }

    public void setMaxRecords(int maxRecords) {

        this.maxRecords = maxRecords;
    }

    public boolean hasResultSetMetaData() {

        return (metaDataTableModel != null && metaDataTableModel.getRowCount() > 0);
    }

    public ResultSetMetaDataTableModel getResultSetMetaData() {

        return metaDataTableModel;
    }

    // ----------------------------------------------------------

    @Override
    public void fireTableStructureChanged() {

        resetVisibleColumnHeaders();
        super.fireTableStructureChanged();
    }

    private void resetVisibleColumnHeaders() {

        visibleColumnHeaders.clear();
        for (ResultSetColumnHeader header : columnHeaders) {

            if (header.isVisible()) {

                visibleColumnHeaders.add(header);
            }

        }
    }

    @Override
    public int getColumnCount() {

        if (visibleColumnHeaders == null) {

            return 0;
        }
        return visibleColumnHeaders.size();
    }

    @Override
    public int getRowCount() {

        if (tableData == null) {

            return 0;
        }
        return tableData.size();
    }

    public List<String> getColumnNames() {

        List<String> list = new ArrayList<String>();
        for (ResultSetColumnHeader header : columnHeaders) {

            list.add(header.getLabel());
        }

        return list;
    }

    public List<RecordDataItem> getRowDataForRow(int row) {

        return tableData.get(row);
    }

    @Override
    public void setValueAt(Object value, int row, int column) {

        List<RecordDataItem> rowData = tableData.get(row);
        if (column < rowData.size()) {

            try {
                if (value != null)
                    if (value.getClass().equals(String.class))
                        if (value.equals("") || columnHeaders.get(column).getDataType() == Types.BOOLEAN && value == "null")
                            value = null;
                rowData.get(asVisibleColumnIndex(column)).valueChanged(value);
                fireTableCellUpdated(row, column);

            } catch (DataSourceException e) {

                Throwable cause = e.getCause();
                if (cause instanceof ParseException) {
                    GUIUtilities.displayExceptionErrorDialog(
                            "Invalid value provided for type -\n" + e.getExtendedMessage(),
                            cause, this.getClass()
                    );
                }
            }

        }
    }

    private int asVisibleColumnIndex(int column) {

        ResultSetColumnHeader columnHeader = visibleColumnHeaders.get(column);
        for (int i = 0, n = columnHeaders.size(); i < n; i++) {

            if (columnHeader.getId().equals(columnHeaders.get(i).getId())) {

                return i;
            }

        }

        return column;
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (row < tableData.size()) {

            List<RecordDataItem> rowData = tableData.get(row);
            if (column < rowData.size()) {

                return rowData.get(asVisibleColumnIndex(column));
            }
        }

        return null;
    }

    public Object getRowValueAt(int row) {

        return tableData.get(row);
    }

    private boolean cellsEditable;

    public void setCellsEditable(boolean cellsEditable) {

        this.cellsEditable = cellsEditable;
    }

    @Override
    public boolean isCellEditable(int row, int column) {

        if (!isTable)
            return false;

        RecordDataItem recordDataItem = tableData.get(row).get(asVisibleColumnIndex(column));
        if (!visibleColumnHeaders.get(column).isEditable()) {
            table.restoreOldCellSize(column);
            return recordDataItem.isNew() && cellsEditable;
        }

        if (recordDataItem.isBlob()) {
            table.restoreOldCellSize(column);
            return false;
        }

        return cellsEditable;
    }

    public void setNonEditableColumns(List<String> nonEditableColumns) {

        setCellsEditable(true);

        for (String nonEditableColumn : nonEditableColumns) {

            for (ResultSetColumnHeader header : columnHeaders) {

                if (header.getLabel().equals(nonEditableColumn)) {

                    header.setEditable(false);
                    break;
                }

            }

        }

    }

    public int getColumnIndex(String column) {
        int ind = -1;
        for (int i = 0; i < visibleColumnHeaders.size(); i++) {
            if (visibleColumnHeaders.get(i).getName().equals(column)) {
                ind = i;
                break;
            }
        }
        return ind;
    }

    public void AddRow(List<RecordDataItem> row) {
        tableData.add(row);
    }

    public void AddRow() {
        List<RecordDataItem> row = new ArrayList<>();
        for (int i = 0; i < getColumnCount(); i++) {
            ResultSetColumnHeader rsch = getColumnHeaders().get(i);
            RecordDataItem rdi = recordDataItemFactory.create(rsch);
            rdi.setValue(null);
            rdi.setNew(true);
            if (rdi instanceof ClobRecordDataItem) {
                ((ClobRecordDataItem) rdi).setCharset(columnDataList.get(i).getCharset());
                if (MiscUtils.isNull(columnDataList.get(i).getCharset()) || Objects.equals(columnDataList.get(i).getCharset(), CreateTableSQLSyntax.NONE))
                    ((ClobRecordDataItem) rdi).setCharset(columnDataList.get(i).getConnection().getCharset());
            }
            row.add(rdi);
        }
        AddRow(row);
        fireTableRowsInserted(tableData.size() - 1, tableData.size() - 1);
        //fireTableChanged(new TableModelEvent(this,tableData.size()-1));
    }

    public void deleteRow(int rowNumber) {
        if (rowNumber >= 0 && rowNumber < tableData.size()) {
            List<RecordDataItem> row = tableData.get(rowNumber);
            if (row.get(0).isNew()) {
                deletedRow = tableData.get(rowNumber);
                tableData.remove(rowNumber);
                fireTableRowsDeleted(rowNumber, rowNumber);
            } else {
                for (int i = 0; i < row.size(); i++) {
                    row.get(i).setDeleted(true);
                }
                fireTableRowsUpdated(rowNumber, rowNumber);
            }
        }
    }

    public List<RecordDataItem> getDeletedRow() {
        return deletedRow;
    }

    public String getColumnNameHint(int column) {

        return visibleColumnHeaders.get(column).getNameHint() + " " + visibleColumnHeaders.get(column).getDataTypeName() + (visibleColumnHeaders.get(column).getDisplaySize() != 0 ? " (" + visibleColumnHeaders.get(column).getDisplaySize() + ")" : "");
    }

    public String getColumnType(int column) {

        return visibleColumnHeaders.get(column).getDataTypeName() + (visibleColumnHeaders.get(column).getDisplaySize() != 0 ? " (" + visibleColumnHeaders.get(column).getDisplaySize() + ")" : "");
    }

    @Override
    public String getColumnName(int column) {

        return visibleColumnHeaders.get(column).getLabel();
    }

    @Override
    public Class<?> getColumnClass(int column) {

        if (tableData.isEmpty()) {

            return String.class;
        }

        RecordDataItem recordDataItem = tableData.get(0).get(column);
        if (recordDataItem.isDisplayValueNull()) {

            return String.class;
        }

        int columnType = recordDataItem.getDataType();
        switch (columnType) {

            case Types.TINYINT:
                return Byte.class;

            case Types.INT128:
                return BigInteger.class;

            case Types.BIGINT:
                return Long.class;

            case Types.SMALLINT:
                return Short.class;

            case Types.BIT:
            case Types.LONGVARCHAR:
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.BOOLEAN: // don't display the checkbox
                return String.class;

            case Types.NUMERIC:
            case Types.DECIMAL:
                return BigDecimal.class;

            case Types.INTEGER:
                return Integer.class;

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return java.util.Date.class;

            case Types.REAL:
                return Float.class;

            case Types.FLOAT:
            case Types.DOUBLE:
                return Double.class;

            default:
                return Object.class;

        }
    }

    public void setTable(ResultSetTable table) {
        this.table = table != null ? table : new ResultSetTable();
    }

    public void closeResultSet() throws SQLException {

        if (rs != null && !rs.isClosed())
            rs.close();

        if (executor != null)
            executor.releaseResources();
    }

    public void reset() {
        List<List<RecordDataItem>> newRows = new ArrayList<>();
        for (List<RecordDataItem> row : tableData) {
            if (row != null && row.size() > 0)
                if (row.get(0).isNew())
                    newRows.add(row);
                else for (RecordDataItem recordDataItem : row)
                    recordDataItem.reset();

        }
        for (List<RecordDataItem> row : newRows) {
            tableData.remove(row);
        }
        fireTableDataChanged();
    }

    public SQLException getException() {
        return exception;
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }
}
