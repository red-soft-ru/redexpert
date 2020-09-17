/*
 * AbstractImportExportWorker.java
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

package org.executequery.gui.importexport;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.LogRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.Base64;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Abstract import/export worker class.
 *
 * @author Takis Diakoumis Dragan Vasic
 */
public abstract class AbstractImportExportWorker implements ImportExportWorker {

    /**
     * The progress dialog for this process
     */
    protected ImportExportProgressPanel progress;

    /**
     * the parent process controller
     */
    protected ImportExportDataProcess parent;

    /**
     * the start time of this process
     */
    protected long startTime;

    /**
     * the finish time of this process
     */
    protected long finishTime;

    /**
     * The database connection for data retrieval
     */
    protected Connection conn;

    /**
     * The original commit mode
     */
    private boolean autoCommit;

    /**
     * The database statement
     */
    protected Statement stmnt;

    /**
     * The database prepared statement
     */
    protected PreparedStatement prepStmnt;

    /**
     * indicates a cancelled process
     */
    protected final String CANCELLED = "cancelled";

    /**
     * indicates a failed process
     */
    protected final String FAILED = "failed";

    /**
     * indicates a failed process
     */
    protected final String SUCCESS = "success";

    /**
     * the total records processed
     */
    private int recordCount;

    /**
     * the total records processed successfully
     */
    private int recordCountProcessed;

    /**
     * the number of errors
     */
    private int errorCount;

    /**
     * the table count
     */
    protected int tableCount;

    /**
     * the process result
     */
    private String result;

    /**
     * temp output logging buffer
     */
    protected StringBuilder outputBuffer;

    /**
     * Indicates a bound column value
     */
    protected final String VARIABLE_BOUND = "variableBound";

    /**
     * Indicates a not bound column value
     */
    protected final String VARIABLE_NOT_BOUND = "variableNotBound";

    /**
     * Indicates an ignored column
     */
    protected final String IGNORED_COLUMN = "ignoredColumn";

    /**
     * Indicates an included column
     */
    protected final String INCLUDED_COLUMN = "includedColumn";

    public AbstractImportExportWorker(ImportExportDataProcess parent,
                                      ImportExportProgressPanel progress) {
        this.parent = parent;
        this.progress = progress;
        outputBuffer = new StringBuilder();
    }

    /**
     * Resets the progress panel.
     */
    protected void reset() {
        progress.reset();
    }

    protected int getTableRecordCount(String tableName) throws DataSourceException, SQLException {

        ResultSet rs = null;
        try {

            StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ");

            String schema = parent.getSchemaName();
            if (!MiscUtils.isNull(schema)) {
                query.append(schema).append('.');
            }

            query.append(formatTableName(tableName));

            appendProgressText("Retrieving row count for table [ " + tableName + " ] ...");

            conn = getConnection();
            stmnt = conn.createStatement();
            rs = stmnt.executeQuery(query.toString());
            if (rs.next()) {

                return rs.getInt(1);
            }

            return 0;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmnt != null) {
                stmnt.close();
            }
        }
    }

    /**
     * Returns a data result set for the specified table.
     *
     * @param table - the database table
     */
    protected ResultSet getResultSet(String table) throws DataSourceException, SQLException {
        return getResultSet(table, null);
    }

    /**
     * Returns a data result set for the specified table and the
     * specified columns. If the columns collection is null,
     * this will assume a multi-table export and expor all the
     * database columns of the table.
     *
     * @param table   - the database table name
     * @param columns - the columns to select from the table
     */
    protected ResultSet getResultSet(String table, List<?> columns) throws DataSourceException, SQLException {

        // check the columns and retrieve if null
        if (columns == null) {
            columns = getColumns(table);
        }

        // build the SQL statement
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(columnNamesAsCommaSeparatedString(table, columns));
        query.append(" FROM ");

        String schema = parent.getSchemaName();
        if (!MiscUtils.isNull(schema)) {
            query.append(schema).append('.');
        }

        query.append(formatTableName(table));

        if (stmnt != null) {
            try {
                stmnt.close();
            } catch (SQLException e) {
            }
        }

        conn = getConnection();
        conn.setAutoCommit(false);

        stmnt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
        stmnt.setFetchSize(fetchSizeForDatabaseProduct(conn.getMetaData()));

        Log.info("Executing query for export: [ " + query + " ]");

        return stmnt.executeQuery(query.toString());
    }

    private String columnNamesAsCommaSeparatedString(String table, List<?> columns) throws DataSourceException, SQLException {

        StringBuilder sb = new StringBuilder();

        int columnCount = columns.size();
        for (int i = 0, n = columnCount - 1; i < columnCount; i++) {

            String columnName = columns.get(i).toString();
            columnName = MiscUtils.getFormattedObject(columnName);

            sb.append(columnName);
            if (i != n) {
                sb.append(',');
            }
        }

        return sb.toString();
    }

    private Object formatTableName(String table) {
        try {

            if (table.contains(" ")) {
                return "\"" + table + "\"";
            }

            String identifierQuoteString = getConnection().getMetaData().getIdentifierQuoteString();
            return identifierQuoteString + table + identifierQuoteString;

        } catch (SQLException e) {

            return "\"";
        }
    }

    /**
     * Prepares the statement for an import process.
     *
     * @param table   - the database table name
     * @param columns - the columns to select from the table
     */
    protected void prepareStatement(String table, Vector<?> columns) throws DataSourceException, SQLException {

        // check the columns and retrieve if null
        if (columns == null) {
            columns = getColumns(table);
        }

        String schema = parent.getSchemaName();
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ");

        if (!MiscUtils.isNull(schema)) {
            query.append(schema).append('.');
        }

        query.append(formatTableName(table));
        query.append(" (");
        query.append(columnNamesAsCommaSeparatedString(table, columns));
        query.append(") VALUES (");

        // add the value place holders
        int columnCount = columns.size();
        for (int i = 0, n = columnCount - 1; i < columnCount; i++) {
            query.append('?');
            if (i != n) {
                query.append(',');
            }
        }
        query.append(")");

        // make sure it was closed from a possible previous run
        if (prepStmnt != null) {
            try {
                prepStmnt.close();
            } catch (SQLException e) {
            }
        }

        conn = getConnection();
        conn.setAutoCommit(false);
        prepStmnt = conn.prepareStatement(query.toString(), java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
        prepStmnt.setFetchSize(fetchSizeForDatabaseProduct(conn.getMetaData()));
    }

    private int fetchSizeForDatabaseProduct(DatabaseMetaData metaData) throws SQLException {

        // we only care about mysql right now which needs Integer.MIN_VALUE
        // to provide row-by-row return on the result set cursor
        // otherwise default to 1000 row fetch size... 

        if (metaData.getDatabaseProductName().toUpperCase().contains("MYSQL")) {

            return Integer.MIN_VALUE;
        }

        return 10000;
    }

    /**
     * Returns the columns to be exported for the specified table.
     *
     * @param table - the table to be dumped
     */
    @SuppressWarnings("unchecked")
    protected Vector<ColumnData> getColumns(String table)
            throws SQLException {
        Vector<ColumnData> columns = parent.getSelectedColumns();
        if (columns == null) {
            String schema = parent.getSchemaName();
            MetaDataValues metaData = parent.getMetaDataUtility();
            try {
                columns = metaData.getColumnMetaDataVector(table, schema);
            } catch (DataSourceException e) {
                if (e.getCause() instanceof SQLException) {
                    throw (SQLException) (e.getCause());
                }
                throw new SQLException(e.getMessage());
            }
        } else {
            columns = (Vector<ColumnData>) columns.clone();
        }
        return columns;
    }

    /**
     * Returns the connection to be used with this process.
     *
     * @return the connection
     */
    protected Connection getConnection() throws DataSourceException {
        if (conn == null) {
            conn = ConnectionManager.getConnection(parent.getDatabaseConnection());
            try {
                autoCommit = conn.getAutoCommit();
            } catch (SQLException e) {
            }
        }
        return conn;
    }

    /**
     * Sets the specified value in the specified position for the
     * specified java.sql.Type within the prepared statement.
     *
     * @param value   - the value
     * @param index   - the position within the statement
     * @param sqlType - the SQL type
     * @param trim    - whether to trim the whitespace from the value
     * @param df      - the DataFormat object for date values
     */
    protected void setValue(String value, int index,
                            int sqlType, boolean trim, DateFormat df)
            throws Exception {

        if (value == null) {

            prepStmnt.setNull(index, sqlType);

        } else {

            switch (sqlType) {

                case Types.TINYINT:
                    byte _byte = Byte.valueOf(value).byteValue();
                    prepStmnt.setShort(index, _byte);
                    break;

                case Types.BIGINT:
                    long _long = Long.valueOf(value).longValue();
                    prepStmnt.setLong(index, _long);
                    break;

                case Types.SMALLINT:
                    short _short = Short.valueOf(value).shortValue();
                    prepStmnt.setShort(index, _short);
                    break;

                case Types.LONGVARCHAR:
                case Types.CHAR:
                case Types.VARCHAR:
                    if (trim) {
                        value = value.trim();
                    }
                    prepStmnt.setString(index, value);
                    break;

                case Types.BIT:
                case Types.BOOLEAN:

                    String booleanValue = value;
                    if ("t".equalsIgnoreCase(value)) {

                        booleanValue = "true";

                    } else if ("f".equalsIgnoreCase(value)) {

                        booleanValue = "false";
                    }

                    boolean _boolean = Boolean.valueOf(booleanValue).booleanValue();
                    prepStmnt.setBoolean(index, _boolean);
                    break;

                case Types.NUMERIC:
                case Types.DECIMAL:
                    prepStmnt.setBigDecimal(index, new BigDecimal(value));
                    break;

                case Types.REAL:
                    float _float = Float.valueOf(value).floatValue();
                    prepStmnt.setFloat(index, _float);
                    break;

                case Types.FLOAT:
                case Types.DOUBLE:
                    prepStmnt.setDouble(index, Double.parseDouble(value));
                    break;

                case Types.INTEGER:
                    prepStmnt.setInt(index, Integer.parseInt(value));
                    break;

                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                    // if the date format is null, insert as a char value
                    if (df != null) {
                        java.util.Date j_datetime = df.parse(value);
                        prepStmnt.setDate(index,
                                new java.sql.Date(j_datetime.getTime()));
                    } else {
                        try {
                            prepStmnt.setObject(index, value, sqlType);
                            /*
                            if (sqlType == Types.TIMESTAMP) {
                                prepStmnt.setTimestamp(index,
                                        java.sql.Timestamp.valueOf(value));
                            }
                            else if (sqlType == Types.TIME) {
                                prepStmnt.setTime(index,
                                        java.sql.Time.valueOf(value));
                            }
                            else {
                                prepStmnt.setDate(index,
                                        java.sql.Date.valueOf(value));
                            }
                             */
                        }
                        // want a more useful message here than what will likely
                        // be returned due to internal driver code on formatting
                        // a SQL date value from string
                        // (ie. could be parsing error, number format etc...)
                        catch (Exception e) {
                            throw new IllegalArgumentException(
                                    "[ " + MiscUtils.getExceptionName(e) + " ] " +
                                            Bundles.get("AbstractImportExportWorker.dateConversionError"));
                        }

                    }
                    break;

                case Types.LONGVARBINARY:
                case Types.BINARY:
                case Types.BLOB:
                case Types.CLOB:
                    prepStmnt.setBytes(index, Base64.decode(value));
                    break;

                default:
                    prepStmnt.setObject(index, value);
                    break;
            }
        }
    }

    /**
     * Displays an error dialog with the specified message.
     */
    protected void displayErrorDialog(String message) {
        GUIUtilities.displayErrorMessage(message);
    }

    /**
     * Displays an input dialog to enter the date format mask.
     *
     * @return the mask entered
     */
    protected String displayDateFormatDialog() {
        return GUIUtilities.displayInputMessage(
                Bundles.get(
                        "AbstractImportExportWorker.dateFormatDialogTitle"),
                Bundles.get(
                        "AbstractImportExportWorker.dateFormatDialog"));
    }

    /**
     * Verifies the date format where applicable.
     *
     * @return the date format
     */
    protected String verifyDate() {

        String format = displayDateFormatDialog();

        if (format == null || format.length() == 0) {

            int yesNo = GUIUtilities.displayConfirmDialog(
                    Bundles.get("AsbtractImportExportWorker.cancelProcessConfirm"));

            if (yesNo == JOptionPane.YES_OPTION) {
                cancelTransfer();
                return null;
            } else {
                format = displayDateFormatDialog();
            }

        }

        return format;
    }

    /**
     * Appends the specified text to the output pane as normal
     * fixed width text.
     *
     * @param text - the text to be appended
     */
    protected void appendProgressText(String text) {
        progress.appendProgressText(text);
    }

    /**
     * Appends the specified text to the output pane as normal
     * fixed width text.
     *
     * @param text - the text to be appended
     */
    protected void appendProgressErrorText(String text) {
        progress.appendProgressErrorText(text);
    }

    /**
     * Appends the specified buffer to the output pane as normal
     * fixed width text.
     *
     * @param text - the text to be appended
     */
    protected void appendProgressText(StringBuilder text) {
        progress.appendProgressText(text.toString());
        text.setLength(0);
    }

    /**
     * Appends the specified buffer to the output pane as error
     * fixed width text.
     *
     * @param text - the text to be appended
     */
    protected void appendProgressErrorText(StringBuilder text) {
        progress.appendProgressErrorText(text.toString());
        text.setLength(0);
    }

    /**
     * Appends the specified buffer to the output pane as warning
     * fixed width text.
     *
     * @param text - the text to be appended
     */
    protected void appendProgressWarningText(StringBuilder text) {
        progress.appendProgressWarningText(text.toString());
        text.setLength(0);
    }

    /**
     * Sets the progress bar as indeterminate as specified
     */
    protected void setIndeterminateProgress(boolean indeterminate) {
        progress.setIndeterminate(indeterminate);
    }

    /**
     * Sets the maximum value for the progess bar.
     *
     * @param value - the max value
     */
    protected void setProgressBarMaximum(int value) {
        progress.setMaximum(value);
    }

    /**
     * Sets the progress bar's position during the process.
     *
     * @param the new process status
     */
    public void setProgressStatus(int status) {
        progress.setProgressStatus(status);
    }

    /**
     * Releases all held database resources.
     */
    protected void releaseResources(DatabaseConnection dc) {
        try {

            if (stmnt != null) {
                stmnt.close();
                stmnt = null;
            }

            if (prepStmnt != null) {
                prepStmnt.close();
                prepStmnt = null;
            }

            if (conn != null) {
                conn.setAutoCommit(autoCommit);
                ConnectionManager.close(dc, conn);
                conn = null;
            }

        } catch (DataSourceException e) {
            System.err.println(
                    "Exception releasing resources at: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println(
                    "Exception releasing resources at: " + e.getMessage());
        }
    }

    protected void outputExceptionError(String message, Throwable e) {
        if (message != null) {
            outputBuffer.append(message);
        }
        outputBuffer.append("\n[ ");
        outputBuffer.append(MiscUtils.getExceptionName(e));
        outputBuffer.append(" ] ");

        if (e instanceof DataSourceException) {
            outputBuffer.append(e.getMessage());
            outputBuffer.append(((DataSourceException) e).getExtendedMessage());
        } else if (e instanceof SQLException) {
            outputBuffer.append(e.getMessage());
            SQLException _e = (SQLException) e;
            outputBuffer.append(Bundles.get(
                    "AbstractImportExportWorker.errorCode",
                    String.valueOf(_e.getErrorCode())));

            String state = _e.getSQLState();
            if (state != null) {
                outputBuffer.append(Bundles.get(
                        "AbstractImportExportWorker.stateCode", state));
            }

        } else {
            String exceptionMessage = e.getMessage();
            if (StringUtils.isNotBlank(exceptionMessage)) {
                outputBuffer.append(exceptionMessage);
            }
        }

        appendProgressErrorText(outputBuffer);
    }

    /**
     * Appends the final process results to the output pane.
     */
    protected void printResults() {

        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------\n");

        if (result == SUCCESS) {
            sb.append(Bundles.get("AbstractImportExportWorker.processCompletedSuccessfully"));
        } else if (result == CANCELLED) {
            sb.append(Bundles.get("AbstractImportExportWorker.processCancelled"));
        } else if (result == FAILED) {
            sb.append(Bundles.get("AbstractImportExportWorker.processCompletedWithErrors"));
        }

        sb.append(Bundles.get("AbstractImportExportWorker.totalDuration"));
        sb.append(getFormattedDuration());
        sb.append(Bundles.get("AbstractImportExportWorker.totalTablesProcessed"));
        sb.append(tableCount);
        sb.append(Bundles.get("AbstractImportExportWorker.totalRecordsProcessed"));
        sb.append(recordCount);
        sb.append(Bundles.get("AbstractImportExportWorker.totalRecordsTransferred"));
        sb.append(recordCountProcessed);
        sb.append(Bundles.get("AbstractImportExportWorker.errors"));
        sb.append(errorCount);

        appendProgressText(sb.toString());

        // log the output to file
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                logOutputToFile();
            }
        });

    }

    protected void appendFileInfo(File file) {

        StringBuilder sb = new StringBuilder();
        sb.append(Bundles.get("AbstractImportExportWorker.outputFileName"));
        sb.append(file.getName());
        sb.append(Bundles.get("AbstractImportExportWorker.outputFileSize"));
        sb.append(new DecimalFormat("###,###.###").format(MiscUtils.bytesToMegaBytes(file.length())));
        sb.append("Mb");

        appendProgressText(sb);
    }

    /**
     * Returns the controlling parent process object.
     */
    protected ImportExportDataProcess getParent() {
        return parent;
    }

    /**
     * Cancels the current data transfer process.
     */
    public abstract void cancelTransfer();

    /**
     * Indicates a data transfer process has completed
     * and clean-up can be performed.
     */
    public abstract void finished();

    /**
     * Logs the start time of this process.
     */
    protected void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Logs the finish time of this process.
     */
    protected void finish() {
        finishTime = System.currentTimeMillis();
        progress.setStopButtonEnabled(false);
    }

    /**
     * Logs the contents of the output pane to file.
     */
    protected void logOutputToFile() {
        PrintWriter writer = null;
        try {
            String logHeader = null;
            String path = logFileDirectory();

            int transferType = parent.getTransferType();
            if (transferType == ImportExportDataProcess.EXPORT) {
                logHeader = "[ Data Export Process - ";
                path += SystemProperties.getProperty("system", "eq.export.log");
            } else {
                logHeader = "[ Data Import Process - ";
                path += SystemProperties.getProperty("system", "eq.import.log");
            }

            // add a header for this process
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            StringBuffer sb = new StringBuffer();
            sb.append(logHeader);
            sb.append(df.format(new Date(startTime)));
            sb.append(" ]\n\n");
            sb.append(progress.getText());
            sb.append("\n\n");

            writer = new PrintWriter(new FileWriter(path, true), true);
            writer.println(sb.toString());
            sb = null;
        } catch (IOException io) {
        } finally {
            if (writer != null) {
                writer.close();
            }
            writer = null;
        }
    }

    private String logFileDirectory() {

        return ((LogRepository) RepositoryCache.load(
                LogRepository.REPOSITORY_ID)).getLogFileDirectory();
    }

    /**
     * Returns the start time of the import/export process.
     *
     * @return the process start time
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the start time of the import/export process.
     *
     * @return the process finish time
     */
    public long getFinishTime() {
        return finishTime;
    }

    /**
     * Returns a formatted string of the duration of the process.
     *
     * @return the process duration formatted as hh:mm:ss
     */
    public String getFormattedDuration() {
        return MiscUtils.formatDuration(finishTime - startTime);
    }

    /**
     * Sets the start time to that specified.
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Sets the finish time to that specified.
     */
    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    /**
     * Returns the total record count.
     */
    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getRecordCountProcessed() {
        return recordCountProcessed;
    }

    public void setRecordCountProcessed(int recordCountProcessed) {
        this.recordCountProcessed = recordCountProcessed;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getTableCount() {
        return tableCount;
    }

    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    protected final DateFormat createDateFormatter() {

        return createDateFormatter(getDateFormatPattern());
    }

    protected final DateFormat createDateFormatter(String pattern) {

        if (StringUtils.isNotBlank(pattern)) {

            return new SimpleDateFormat(getDateFormatPattern());
        }

        return new SimpleDateFormat();
    }

    protected final String getDateFormatPattern() {

        return getParent().getDateFormat();
    }

    protected final boolean parseDateValues() {

        return getParent().parseDateValues();
    }

    private String databaseProductName;
    private static final String ORACLE = "ORACLE";

    protected boolean isOracle() throws SQLException {

        if (databaseProductName == null) {

            DatabaseMetaData metaData = conn.getMetaData();
            databaseProductName = metaData.getDatabaseProductName().toUpperCase();

        } else {

            return databaseProductName.contains(ORACLE);
        }

        return false;
    }


}









