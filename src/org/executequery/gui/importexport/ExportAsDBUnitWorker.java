/*
 * ExportAsSQLWorker.java
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;
import org.executequery.log.Log;
import org.executequery.util.Base64;
import org.executequery.util.ThreadWorker;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 *
 * @author   Takis Diakoumis
 */
public class ExportAsDBUnitWorker extends BaseImportExportWorker {

    private ThreadWorker worker;

    public ExportAsDBUnitWorker(ImportExportWizard importExportWizard) {

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
        
        appendProgressText("Beginning export to DBUnit dataset process...");
        appendProgressText("Using connection: " + 
                model.getDatabaseHost().getDatabaseConnection().getName());

        // record the start time
        start();

        int tableCount = 0;
        int recordCount = 0;
        int errorCount = 0;
        int totalRecordCount = 0;

        try {
        
            ResultSet rs = null;
            List<DatabaseTable> databaseTables = model.getDatabaseTables();
            
            StringBuilder sb = new StringBuilder();

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,"yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            
            Document document = null;
            Element rootElement = null;
            for (DatabaseTable table : databaseTables) {
                
                ImportExportFile importExportFile = model.getImportExportFileForTable(table);
                
                if (!model.isSingleFileMultiTableExport() || document == null) {

                    document = docBuilder.newDocument();
                    rootElement = document.createElement("dataset");
                    document.appendChild(rootElement);
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

                    if (dataRowCount > 0) {

                        List<DatabaseColumn> columns = columnSelections(importExportFile);
                        
                        appendProgressText("Exporting data...");
                        
                        rs = resultSetForExport(importExportFile, columns);
                        ResultSetMetaData rsmd = rs.getMetaData();
                        
                        while (rs.next()) {
                            
                            if (Thread.interrupted()) {
    
                                setProgressStatus(dataRowCount);
                                throw new InterruptedException();
                            }
                            
                            Element tableElement = document.createElement(table.getName());
                            rootElement.appendChild(tableElement);

                            for (int i = 1, n = rsmd.getColumnCount(); i <= n; i++) {

                                String value = formatNextValue(rs, i, rsmd.getColumnType(i));

                                Attr attr = document.createAttribute(rsmd.getColumnName(i));
                                attr.setValue(value);
                                tableElement.setAttributeNode(attr);
                                
                            }
                            
                            recordCount++;
                            totalRecordCount++;
                            
                            setProgressStatus(recordCount);
                        }
                        
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
                
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(importExportFile.getFile());

                transformer.transform(source, result);
                
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
            
        } catch (OutOfMemoryError e) {

            errorCount++;

            outputExceptionError("Error exporting table data to file", e);
            return ImportExportResult.FAILED;
            
        } catch (ParserConfigurationException e) {

            errorCount++;
            
            logException(e);
            outputExceptionError("I/O error exporting table data to file", e);
            return ImportExportResult.FAILED;


        } catch (TransformerConfigurationException e) {

            errorCount++;
            
            logException(e);
            outputExceptionError("I/O error exporting table data to file", e);
            return ImportExportResult.FAILED;

        } catch (TransformerException e) {
        
            errorCount++;
            
            logException(e);
            outputExceptionError("I/O error exporting table data to file", e);
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
                return formatString(value.toString());

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return value.toString();
            
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
                return formatString(value.toString());

        }

    }

    private static final String NEW_LINE_REPLACEMENT = "\\\\n";
    private static final String CARRIAGE_RETURN_REPLACEMENT = "\\\\r";
    private static final String QUOTE_REPLACEMENT = "''";

    private Matcher newLineMatcher = Pattern.compile("\n").matcher(StringUtils.EMPTY);
    private Matcher carriageReturnMatcher = Pattern.compile("\r").matcher(StringUtils.EMPTY);
    private Matcher quoteMatcher = Pattern.compile("'").matcher(StringUtils.EMPTY);

    private String formatString(String value) {

        if (value != null) {

            String formattedValue = newLineMatcher.reset(value).replaceAll(NEW_LINE_REPLACEMENT);
            formattedValue = carriageReturnMatcher.reset(formattedValue).replaceAll(CARRIAGE_RETURN_REPLACEMENT);
            formattedValue = quoteMatcher.reset(formattedValue).replaceAll(QUOTE_REPLACEMENT);

            return formattedValue;
        }

        return value;
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

    public void cancelTransfer() {

        worker.interrupt();
    }

    public void finished() {
        // nothing here
    }

    private void logException(Throwable e) {
     
        if (Log.isDebugEnabled()) {
     
            Log.debug("Error on DBUnit export.", e);
        }
    }
    
}
