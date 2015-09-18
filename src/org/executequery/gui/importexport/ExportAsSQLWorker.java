/*
 * ExportAsSQLWorker.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;
import org.executequery.log.Log;
import org.executequery.util.Base64;
import org.executequery.util.ThreadWorker;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

/** 
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ExportAsSQLWorker extends BaseImportExportWorker {

    private ThreadWorker worker;

    public ExportAsSQLWorker(ImportExportWizard importExportWizard) {

        super(importExportWizard);
    }

    protected void export() {
        
        worker = new ThreadWorker() {

            public Object construct() {
                
                return doWork();
            }
            
            public void finished() {

                ImportExportResult importExportResult = (ImportExportResult) get();
                setResult(importExportResult);
                
                printResults();
                if (importExportDataModel().isSingleFileMultiTableExport()) {

                    printExportFileSize(importExportDataModel().getImportExportFiles().get(0));
                }
                setProgressStatus(-1);

                importExportWizard().processComplete(importExportResult);

                GUIUtilities.scheduleGC();
            }

        };
        worker.start();
    }

    private Object doWork() {

        importExportWizard().enableButtons(false);
        
        ImportExportDataModel model = importExportDataModel();
        
        appendProgressText("Beginning export to SQL process...");
        appendProgressText("Using connection: " + 
                model.getDatabaseHost().getDatabaseConnection().getName());

        // record the start time
        start();

        int tableCount = 0;
        int recordCount = 0;
        int errorCount = 0;
        int totalRecordCount = 0;

        PrintWriter writer = null;

        try {
        
            ResultSet rs = null;
            List<DatabaseTable> databaseTables = model.getDatabaseTables();
            
            StringBuilder sb = new StringBuilder();

            StringBuilder primaryKeys = new StringBuilder();
            StringBuilder foreignKeys = new StringBuilder();
            StringBuilder uniqueKeys = new StringBuilder();
            
            for (DatabaseTable table : databaseTables) {
                
                ImportExportFile importExportFile = model.getImportExportFileForTable(table);
                
                if (!model.isSingleFileMultiTableExport() || writer == null) {

                    writer = new PrintWriter(
                            new FileWriter(importExportFile.getFile(), false), true);
                }

                try {

                    int dataRowCount = table.getDataRowCount();

                    setProgressStatus(0);
                    
                    if (dataRowCount > 0) {
                    
                        setProgressBarMaximum(dataRowCount);
                    
                    } else {
                        
                        setProgressBarMaximum(100);
                    }
                
                    sb.append("---------------------------\nTable: ");
                    sb.append(table.getName());
                    sb.append("\nRecords found: ");
                    sb.append(dataRowCount);
                    sb.append("\nExport file: ");
                    sb.append(importExportFile.getFile().getName());
                    appendProgressText(sb);
                    sb.setLength(0);

                    if (dataRowCount > 0 || isCreateTableStatementsIncluded()) {

                        writer.println(headerForTable(table));
                    }

                    if (isCreateTableStatementsIncluded()) {

                        writer.println(table.getCreateSQLText(DatabaseTable.STYLE_NO_CONSTRAINTS));
                        writer.println();
                    }

                    if (isPrimaryKeyStatementsIncluded()) {

                        primaryKeys.append(table.getAlterSQLTextForPrimaryKeys());
                    }
                    
                    if (isForeignKeyStatementsIncluded()) {

                        foreignKeys.append(table.getAlterSQLTextForForeignKeys());
                    }
                    
                    if (isUniqueKeyStatementsIncluded()) {

                        uniqueKeys.append(table.getAlterSQLTextForUniqueKeys());
                    }

                    if (dataRowCount > 0) {

                        List<DatabaseColumn> columns = columnSelections(importExportFile);
                        
                        appendProgressText("Exporting data...");
                        
                        rs = resultSetForExport(importExportFile, columns);
                        ResultSetMetaData rsmd = rs.getMetaData();
                        
                        String insertStatement = insertStatementForTable(importExportFile, columns);

                        while (rs.next()) {
                            
                            if (Thread.interrupted()) {
    
                                setProgressStatus(dataRowCount);
                                throw new InterruptedException();
                            }
    
                            sb.append(insertStatement);
    
                            for (int i = 1, n = rsmd.getColumnCount(); i <= n; i++) {
    
                                String value = formatNextValue(rs, i, rsmd.getColumnType(i));
                                sb.append(value);
                                
                                if (i < n) {
                                    
                                    sb.append(", ");
                                }
    
                            }
                            
                            sb.append(");\n");
                            writer.println(sb.toString());
                            sb.setLength(0);
    
                            recordCount++;
                            totalRecordCount++;
                            
                            setProgressStatus(recordCount);
                        }
                        
                    }
                    
                    if (!model.isSingleFileMultiTableExport()) {
                        
                        writeConstraints(writer, primaryKeys, foreignKeys, uniqueKeys);
                    }

                } catch (SQLException e) {

                    errorCount++;
                    logException(e);

                    if (OnErrorOption.isLogAndContinue(
                            importExportDataModel().getOnErrorOption())) {
                        
                        outputExceptionError("SQL error exporting table ", e);

                    } else {

                        throw new DataSourceException(e);
                    }

                } finally {
                    
                    if (!model.isSingleFileMultiTableExport()) {

                        flushAndClose(writer);                        
                        printExportFileSize(importExportFile);
                    }

                    closeResultSet(rs);
                    closeStatement(statement);
                }

                setProgressStatus(-1);

                recordCount = 0;
                sb.append("Export complete for table: ");
                sb.append(table.getName());
                appendProgressText(sb);
                sb.setLength(0);

                tableCount++;
            }
    
            if (model.isSingleFileMultiTableExport()) {
                
                writeConstraints(writer, primaryKeys, foreignKeys, uniqueKeys);
                
                flushAndClose(writer);
            }

            setTableCount(tableCount);
            
        } catch (InterruptedException e) {

            cancelStatement(statement);
            return ImportExportResult.CANCELLED;

        } catch (DataSourceException e) {

            errorCount++;

            logException(e);
            outputExceptionError("Data source error exporting table data to file", e);
            return ImportExportResult.FAILED;
            
        } catch (IOException e) {
            
            errorCount++;
            
            logException(e);
            outputExceptionError("I/O error exporting table data to file", e);
            return ImportExportResult.FAILED;
            
        } catch (OutOfMemoryError e) {

            errorCount++;

            outputExceptionError("Error exporting table data to file", e);
            return ImportExportResult.FAILED;
            
        } finally {
            
            finish();
            releaseConnection();
            setTableCount(tableCount);
            setRecordCount(totalRecordCount + errorCount);
            setErrorCount(errorCount);
            setRecordCountProcessed(totalRecordCount);
        }
        
        return ImportExportResult.SUCCESS;
    }

    private void writeConstraints(PrintWriter writer,
            StringBuilder primaryKeys, StringBuilder foreignKeys,
            StringBuilder uniqueKeys) {

        if (isPrimaryKeyStatementsIncluded()) {
            
            writer.println(headerForPrimaryKeyConstraints());
            writer.println(primaryKeys);
        }
        
        if (isForeignKeyStatementsIncluded()) {
        
            writer.println(headerForForeignKeyConstraints());
            writer.println(foreignKeys);
        }
        
        if (isUniqueKeyStatementsIncluded()) {

            writer.println(headerForUniqueKeyConstraints());
            writer.println(uniqueKeys);
        }

        primaryKeys.setLength(0);
        foreignKeys.setLength(0);
        uniqueKeys.setLength(0);
    }
    
    private void releaseConnection() {

        importExportDataModel().getDatabaseHost().close();
    }

    private static final String NULL_STRING = "NULL";
    
    private String formatNextValue(ResultSet rs, int index, int columnType) 
        throws SQLException {

        Object value = rs.getObject(index); 
        if (rs.wasNull() || value == null) {
            
            return NULL_STRING;
        }
        
        switch (columnType) {

            case Types.LONGVARCHAR:
            case Types.CHAR:
            case Types.VARCHAR:
                return "'" + formatString(value.toString()) + "'";

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return "'" + value.toString() + "'";
            
            // TODO: not really sure how well this will work with blobs et al

            case Types.LONGVARBINARY:
            case Types.BINARY:
            case Types.BLOB:
            case Types.CLOB:
                return Base64.encodeBytes(
                        MiscUtils.inputStreamToBytes(rs.getBinaryStream(index)));

            case Types.BOOLEAN:
                boolean boolValue = ((Boolean) value).booleanValue();

                return boolValue ? "true" : "false";

            case Types.BIT:
            case Types.TINYINT:
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
                    return value.toString();

            default:
                return "'" + formatString(value.toString()) + "'";

        }

    }

    private String formatString(String value) {

        if (value != null) {

            return value.replaceAll("\n", "\\\\n").
                replaceAll("\r", "\\\\r").
                replaceAll("'", "''");
        }

        return value;
    }

    private StringBuilder stringBuilder = new StringBuilder();
    
    private String insertStatementForTable(ImportExportFile importExportFile, List<DatabaseColumn> columns) throws SQLException {

        DatabaseTable table = importExportFile.getDatabaseTable();
        
        stringBuilder.setLength(0);
        stringBuilder.append("INSERT INTO ");
        stringBuilder.append(table.getName());
        stringBuilder.append(" (");

        for (int i = 0, n = columns.size(); i < n; i++) {

            stringBuilder.append(((DatabaseTableColumn) columns.get(i)).getNameEscaped());
            if (i < (n - 1)) {
                
                stringBuilder.append(", ");
            }

        }
        
        stringBuilder.append(") VALUES \n    (");
        return stringBuilder.toString();
    }

    private List<DatabaseColumn> columnSelections(ImportExportFile importExportFile) {

        if (importExportFile.hasColumnSelections()) {
            
            return importExportFile.getDatabaseTableColumns();
            
        } else {
            
            return importExportFile.getDatabaseTable().getColumns();
        }        
    }

    private Statement statement;
    
    private ResultSet resultSetForExport(ImportExportFile importExportFile, List<DatabaseColumn> columns) 
        throws SQLException {

        closeStatement(statement);
        
        DatabaseTable table = importExportFile.getDatabaseTable();

        Connection connection = table.getHost().getConnection();
        statement = connection.createStatement();

        return statement.executeQuery(selectStatementForExport(importExportFile, columns));
    }

    private String selectStatementForExport(ImportExportFile importExportFile, List<DatabaseColumn> columns) {
        
        DatabaseTable table = importExportFile.getDatabaseTable();

        StringBuilder sb = new StringBuilder("SELECT ");
        for (int i = 0, n = columns.size(); i < n; i++) {
            
            DatabaseTableColumn column = (DatabaseTableColumn) columns.get(i);
            sb.append(column.getNameEscaped());
            if (i < (n - 1)) {
                
                sb.append(',');
            }
            
        }

        sb.append(" FROM ");
        if (table.getParentNameForStatement() != null) {
        
            sb.append(table.getParentNameForStatement());
            sb.append(".");
        }
        sb.append(table.getNameForQuery());

        Log.info("Executing query for export: [ " + sb + " ]");

        return sb.toString();
    }
    
    private boolean isCreateTableStatementsIncluded() {

        return ((ExportAsSQLDataModel)importExportDataModel()).includeCreateTableStatements();
    }

    private boolean isPrimaryKeyStatementsIncluded() {

        return ((ExportAsSQLDataModel)importExportDataModel()).includePrimaryKeyConstraints();
    }

    private boolean isForeignKeyStatementsIncluded() {

        return ((ExportAsSQLDataModel)importExportDataModel()).includeForeignKeyConstraints();
    }

    private boolean isUniqueKeyStatementsIncluded() {

        return ((ExportAsSQLDataModel)importExportDataModel()).includeUniqueKeyConstraints();
    }

    private String headerForTable(DatabaseTable table) {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n---\n--- Table: ");
        sb.append(table.getName());
        sb.append("\n---\n");
        
        return sb.toString();
    }

    private String headerForPrimaryKeyConstraints() {
        
        return "\n---\n--- Primary Keys: \n---\n";
    }

    private String headerForForeignKeyConstraints() {
        
        return "\n---\n--- Foreign Keys: \n---\n";
    }

    private String headerForUniqueKeyConstraints() {
        
        return "\n---\n--- Unique Keys: \n---\n";
    }

    private void flushAndClose(PrintWriter writer) {

        if (writer != null) {

            writer.flush();
            writer.close();
        }

    }

    public void cancelTransfer() {

        worker.interrupt();
    }

    public void finished() {
        // nothing here
    }

    private void logException(Throwable e) {
     
        if (Log.isDebugEnabled()) {
     
            Log.debug("Error on SQL export.", e);
        }
    }
    
}






