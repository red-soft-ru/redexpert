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
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.gui.ErrorMessagePublisher;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.executequery.util.UserProperties;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.table.AbstractSortableTableModel;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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

    private List<ResultSetColumnHeader> columnHeaders;

    private List<ResultSetColumnHeader> visibleColumnHeaders;

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

    public ResultSetTableModel(boolean isTable) {

        this(null, -1, isTable);
    }

    public ResultSetTableModel(int maxRecords, boolean isTable) {

        this(null, maxRecords, isTable);
    }

    public ResultSetTableModel(ResultSet resultSet, int maxRecords, boolean isTable) {

        this(resultSet, maxRecords, null, isTable);
    }

    public ResultSetTableModel(ResultSet resultSet, int maxRecords, String query, boolean isTable) {

        this.maxRecords = maxRecords;
        this.query = query;
        this.isTable = isTable;
        columnHeaders = new ArrayList<ResultSetColumnHeader>();
        visibleColumnHeaders = new ArrayList<ResultSetColumnHeader>();

        tableData = new ArrayList<List<RecordDataItem>>();
        recordDataItemFactory = new RecordDataItemFactory();

        holdMetaData = UserProperties.getInstance().getBooleanProperty("editor.results.metadata");

        if (resultSet != null) {

            createTable(resultSet);
        }

    }

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

    public synchronized void createTable(ResultSet resultSet) {
        createTable(resultSet, null);
    }

    int zeroBaseIndex;
    int fetchSize;
    boolean rsClose;
    ResultSet rs;
    int count;
    private int recordCount;

    public synchronized void createTable(ResultSet resultSet, List<ColumnData> columnDataList) {

        if (!isOpenAndValid(resultSet)) {

            clearData();
            return;
        }

        try {
            resetMetaData();
            ResultSetMetaData rsmd = resultSet.getMetaData();

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
                                rsmd.getColumnTypeName(i)));
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
                System.err.println("SQL error populating table model at: " + e.getMessage());
                Log.debug("Table model error - " + e.getMessage(), e);
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
                    GUIUtilities.displayExceptionErrorDialog("Error loading data", e);
                fireTableDataChanged();
            }
    }

    private void fetchOneRecord(ResultSet resultSet, int count) throws SQLException, InterruptedException {
        if (resultSet.next())
            addingRecord(resultSet, count);
        else {
            resultSet.close();
            rsClose = true;
        }
    }

    private void fetchAllRecords(ResultSet resultSet, int count) throws SQLException, InterruptedException {
        while (resultSet.next())
            addingRecord(resultSet, count);
        fireTableDataChanged();
        resultSet.close();
        rsClose = true;
    }

    public synchronized void createTableFromMetaData(ResultSet resultSet, DatabaseConnection dc, List<ColumnData> columnDataList) {

        if (!isOpenAndValid(resultSet)) {

            clearData();
            return;
        }
        StatementExecutor executor = new DefaultStatementExecutor(dc, true);

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
                                resultSet.getString(6)
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
                        case Types.DATE:
                            value.setValue(resultSet.getDate(i));
                            break;
                        case Types.TIME:
                            value.setValue(resultSet.getTime(i));
                            break;
                        case Types.TIMESTAMP:
                            value.setValue(resultSet.getTimestamp(i));
                            break;
                        case Types.LONGVARCHAR:
                        case Types.CLOB:
                            Clob clob = resultSet.getClob(i);
                            if (clob != null && clob.getClass().getName().contains("org.firebirdsql.jdbc")) {
                                URL[] urls = new URL[0];
                                Class clazzdb = null;
                                Object odb = null;
                                try {
                                    urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
                                    ClassLoader cl = new URLClassLoader(urls, resultSet.getStatement().getConnection().getClass().getClassLoader());
                                    clazzdb = cl.loadClass("biz.redsoft.FBClobImpl");
                                    odb = clazzdb.newInstance();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }

                                IFBClob ifbClob = (IFBClob) odb;
                                ifbClob.detach(clob);
                                value.setValue(ifbClob);
                            } else {
                                value.setValue(clob);
                            }
                            if (columnDataList != null) {
                                ((ClobRecordDataItem) value).setCharset(columnDataList.get(i - 1).getCharset());
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
                                URL[] urls = new URL[0];
                                Class clazzdb = null;
                                Object odb = null;
                                try {
                                    urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
                                    ClassLoader cl = new URLClassLoader(urls, resultSet.getStatement().getConnection().getClass().getClassLoader());
                                    clazzdb = cl.loadClass("biz.redsoft.FBBlobImpl");
                                    odb = clazzdb.newInstance();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }

                                IFBBlob ifbBlob = (IFBBlob) odb;
                                ifbBlob.detach(blob);
                                value.setValue(ifbBlob);
                            } else {
                                value.setValue(blob);
                            }
                            break;
                        case Types.BIT:
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.INTEGER:
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

                if (resultSet.wasNull()) {

                    value.setNull();
                }

                rowData.add(value);
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

                    ErrorMessagePublisher.publish(
                            "Invalid value provided for type -\n" + e.getExtendedMessage(), cause);
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
        RecordDataItem recordDataItem = tableData.get(row).get(asVisibleColumnIndex(column));

        if (!visibleColumnHeaders.get(column).isEditable()) {
            return recordDataItem.isNew() && cellsEditable;
        }
        if (recordDataItem.isBlob()) {

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
            if (rdi instanceof ClobRecordDataItem)
                ((ClobRecordDataItem) rdi).setCharset(columnDataList.get(i).getCharset());
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

        return visibleColumnHeaders.get(column).getNameHint();
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

    public void closeResultSet() throws SQLException {
        if (rs != null && !rs.isClosed())
            rs.close();
    }
}


