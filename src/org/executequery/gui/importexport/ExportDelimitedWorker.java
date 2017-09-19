/*
 * ExportDelimitedWorker.java
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Vector;

import org.executequery.Constants;
import org.executequery.gui.browser.ColumnData;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.util.SwingWorker;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1784 $
 * @date     $Date: 2017-09-19 00:55:31 +1000 (Tue, 19 Sep 2017) $
 */
public class ExportDelimitedWorker extends AbstractImportExportWorker {
    
    /** The thread worker object for this process */
    private SwingWorker worker;
    
    /** 
     * Constructs a new instance with the specified parent object 
     * and progress output panel.
     *
     * @param parent - the parent for this process
     * @param progress - the progress panel
     */
    public ExportDelimitedWorker(ImportExportDataProcess parent,
                                 ImportExportProgressPanel progress) {
        super(parent, progress);
        transferData();
    }
    
    /** <p>Begins the transfer process setting up the
     *  <code>SwingWorker</code> and creating the progress
     *  dialog.
     */
    private void transferData() {
        reset();
        
        // create the worker
        worker = new SwingWorker() {
            public Object construct() {
                return doWork();
            }
            public void finished() {
                String result = (String)get();
                setResult(result);

                releaseResources(getParent().getDatabaseConnection());

                printResults();
                setProgressStatus(-1);
                getParent().setProcessComplete(result == SUCCESS);
            }
        };
        worker.start();
    }
    
    /** <p>Performs the actual processing for the worker. */
    private Object doWork() {
        
        // counter variables
        int tableCount = 0;
        int totalRecordCount = 0;
        int errorCount = 0;
        
        appendProgressText("Beginning export to delimited file process...");
        appendProgressText("Using connection: " + 
                getParent().getDatabaseConnection().getName());

        // record the start time
        start();

        // --------------------------------
        // --- begin the export process ---
        // --------------------------------

        ResultSet rset = null;
        PrintWriter writer = null;
        try {

            // define the delimiter
            String delim = getParent().getDelimiter();

            // whether to trim whitespace
            boolean trimWhitespace = getParent().trimWhitespace();

            // include the column names
            boolean includeColumnNames = getParent().includeColumnNames();

            boolean quoteCharacterValues = getParent().quoteCharacterValues();
            
            // row data output buffer
            StringBuilder rowData = new StringBuilder(5000);

            // retrieve the export to files
            Vector<DataTransferObject> files = getParent().getDataFileVector();
            int fileCount = files.size();

            // ---------------------------
            // --- initialise counters ---
            // ---------------------------

            int columnCount = -1;
            int recordCount = 0;
            int totalRecords = 0;
            
            DateFormat dateFormat = null;
            
            boolean parseDateValues = parseDateValues();
            if (parseDateValues) {
                
                dateFormat = createDateFormatter();
            }

            // ----------------------------------------
            // --- begin looping through the tables ---
            // ----------------------------------------
            
            for (int i = 0; i < fileCount; i++) {
                
                tableCount++;
                setProgressStatus(0);

                DataTransferObject dto = (DataTransferObject)files.elementAt(i);
                
                totalRecords = getTableRecordCount(dto.getTableName());
                setProgressBarMaximum(totalRecords);

                // initialise the file object
                File exportFile = new File(dto.getFileName());
                
                // append some output
                outputBuffer.append("---------------------------\nTable: ");
                outputBuffer.append(dto.getTableName());
                outputBuffer.append("\nRecords found: ");
                outputBuffer.append(totalRecords);
                outputBuffer.append("\nExport file: ");
                outputBuffer.append(exportFile.getName());
                appendProgressText(outputBuffer);

                // retrieve the columns to be exported (or all)
                Vector<ColumnData> columns = getColumns(dto.getTableName());
                columnCount = columns.size();

                // initialise the writer
                writer = new PrintWriter(new FileWriter(exportFile, false), true);
                
                // print the column names if specified to do so
                if (includeColumnNames) {
                    for (int k = 0, n = columnCount - 1; k < columnCount; k++) {
                        rowData.append(columns.get(k));
                        if (k != n) {
                            rowData.append(delim);
                        }
                    }
                    writer.println(rowData.toString());
                    rowData.setLength(0);
                }

                appendProgressText("Exporting data...");
                
                // retrieve the result set
                rset = getResultSet(dto.getTableName(), columns);
                
                // start the loop over results
                while (rset.next()) {
                    
                    if (Thread.interrupted()) {
                        rset.close();
                        rset = null;
                        writer.close();
                        setProgressStatus(totalRecords);
                        throw new InterruptedException();
                    }
                    
                    setProgressStatus(recordCount);
                    
                    for (int j = 1; j <= columnCount; j++) {

                        String value = rset.getString(j);

                        if (value == null || rset.wasNull()) {

                            value = Constants.EMPTY;

                        } else if (trimWhitespace) {

                            value = value.trim();
                        }

                        ColumnData column = (ColumnData) columns.get(j - 1);

                        if (column.isDateDataType() && 
                                (parseDateValues && dateFormat != null)) {

                            value = dateFormat.format(rset.getDate(j));
                        
                        } else {
                            
                            value = formatString(value);
                        }

                        boolean isCharType = column.isCharacterType();
                        if (isCharType && quoteCharacterValues) {
                            
                            rowData.append("\"");                            
                        }
                        
                        rowData.append(value);

                        if (isCharType && quoteCharacterValues) {
                            
                            rowData.append("\"");                            
                        }

                        if (j != columnCount) {

                            rowData.append(delim);
                        }

                    }
                    
                    writer.println(rowData.toString());
                    rowData.setLength(0);
                    totalRecordCount++;
                    recordCount++;
                }

                rset.close();
                stmnt.close();
                writer.close();
                
                setProgressStatus(totalRecords);
                
                recordCount = 0;
                outputBuffer.append("Export successful for table: ");
                outputBuffer.append(dto.getTableName());
                appendProgressText(outputBuffer);
                
                appendFileInfo(exportFile);
                
                /*
                if (tableCount != fileCount) {
                    setProgressStatus(0);
                }
                */
            }
            
            return SUCCESS;
        }
        
        catch (InterruptedException e) {
            cancelStatement();
            return CANCELLED;
        }
        catch (SQLException e) {
            logException(e);
            outputExceptionError("SQL error exporting table data to file", e);
            return FAILED;
        }
        catch (DataSourceException e) {
            logException(e);
            outputExceptionError("Error exporting table data to file", e);
            return FAILED;
        }
        catch (IOException e) {
            logException(e);
            outputExceptionError("I/O error exporting table data to file", e);
            return FAILED;
        }
        catch (OutOfMemoryError e) {
            outputExceptionError("Error exporting table data to file", e);
            return FAILED;
        }
        finally {

            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException e) {}
            }

            finish();
            setTableCount(tableCount);
            setRecordCount(totalRecordCount + errorCount);
            setErrorCount(errorCount);
            setRecordCountProcessed(totalRecordCount);
        }
    }
    
    private String formatString(String value) {

        return value.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r");
    }

    private void logException(Throwable e) {
        if (Log.isDebugEnabled()) {
            Log.debug("Error on delimited export.", e);
        }
    }

    /** 
     * Cancels an in progress SQL statement. 
     */
    private void cancelStatement() {
        if (stmnt == null) {
            return;
        }
        try {
            stmnt.cancel();
            stmnt.close();
            stmnt = null;
        } catch (SQLException e) {}
    }
    
    /**
     * Cancels the current in-process transfer. 
     */
    public void cancelTransfer() {
        worker.interrupt();
        getParent().cancelTransfer();
    }
    
    /** 
     * Indicates that the process has completed. 
     */
    public void finished() {}
    
    
}


















