/*
 * ExportExcelWorker.java
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.executequery.Constants;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.util.SwingWorker;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ExportExcelWorker extends AbstractImportExportWorker
                               implements Constants {
    
    /** The <code>SwingWorker</code> object for this process */
    private SwingWorker worker;
    
    /** The controlling object for this process */
    private ImportExportExcelPanel _parent;
    
    /**
     * Constructs a new instance with the specified parent
     * object - an instance of <code>ImportExportExcelPanel</code>.
     */
    public ExportExcelWorker(ImportExportExcelPanel parent,
                             ImportExportProgressPanel progressPanel) {
        super(parent, progressPanel);
        this._parent = parent;
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

                releaseResources(_parent.getDatabaseConnection());

                printResults();
                setProgressStatus(-1);
                _parent.setProcessComplete(result == SUCCESS);
            }
        };
        worker.start();
    }
    
    /** <p>Performs the actual processing for the worker. */
    private Object doWork() {

        appendProgressText("Beginning export to Excel spreadsheet process...");
        appendProgressText("Using connection: " + 
                getParent().getDatabaseConnection().getName());

        // record the start time
        start();
        
        ResultSet rset = null;
        
        int errorCount = 0;
        int totalRecordCount = 0;
        
        FileOutputStream outputStream = null;
        
        try {

            int fileFormat = _parent.getMutlipleTableTransferType();
            boolean isSingleTable = (_parent.getTableTransferType() == 
                                            ImportExportDataProcess.SINGLE_TABLE);

            Vector<?> files = _parent.getDataFileVector();
            String[] tablesArray = _parent.getSelectedTables();
            
            // ---------------------------
            // --- initialise counters ---
            // ---------------------------
            int columnCount = -1;
            int recordCount = 0;
            int totalRecords = 0;
            
            ExcelWorkbookBuilder builder = createExcelWorkbookBuilder();
            
            String[][] sheetNames = _parent.getSheetNameValues();
            
            // ----------------------------------------
            // --- begin looping through the tables ---
            // ----------------------------------------
            
            List<String> values = new ArrayList<String>();
            
            for (int i = 0; i < tablesArray.length; i++) {
                
                values.clear();
                builder.reset();
                
                recordCount = 0;
                
                tableCount++;
                setProgressStatus(0);

                String tableName = tablesArray[i];
                DataTransferObject dto = null;

                if (isSingleTable) {
                
                    dto = (DataTransferObject)files.elementAt(i);
                
                } else {
                  
                    if (fileFormat == ImportExportDataProcess.MULTIPLE_FILE) {
                        
                        dto = (DataTransferObject)files.elementAt(i);

                    } else {

                        dto = (DataTransferObject)files.elementAt(0);
                    }

                }
                
                totalRecords = getTableRecordCount(tableName);
                setProgressBarMaximum(totalRecords);

                // initialise the file object
                File exportFile = new File(dto.getFileName());
                
                // append some output
                outputBuffer.append("---------------------------\nTable: ");
                outputBuffer.append(tableName);
                outputBuffer.append("\nRecords found: ");
                outputBuffer.append(totalRecords);
                outputBuffer.append("\nExport file: ");
                outputBuffer.append(exportFile.getName());
                appendProgressText(outputBuffer);

                // retrieve the columns to be exported (or all)
                Vector<?> columns = getColumns(tableName);
                columnCount = columns.size();

                // initialise the output stream
                outputStream = createOutputStream(exportFile);

                String sheetName = tablesArray[i];
                if (!isSingleTable) {

                    sheetName = sheetNames[i][1];
                }

                builder.createSheet(sheetName);
                
                appendProgressText("Exporting data...");
                
                // retrieve the result set
                rset = getResultSet(tableName, columns);

                if (_parent.includeColumnNames()) {

                    for (int j= 0; j < columnCount; j++) {
                        
                        values.add(columns.get(j).toString());
                    }

                    builder.addRowHeader(values);
                }

                while (rset.next()) {

                    if (Thread.interrupted()) {
                        rset.close();
                        rset = null;
                        outputStream.close();
                        setProgressStatus(totalRecords);
                        throw new InterruptedException();
                    }

                    values.clear();
                    
                    for (int j= 0; j < columnCount; j++) {
                        
                        String value = rset.getString(j + 1);
                        values.add(!rset.wasNull() ? value : EMPTY);
                    }
                    
                    builder.addRow(values);

                    recordCount++;
                    totalRecordCount++;

                    setProgressStatus(recordCount);                    
                }

                rset.close();
                
                if (isSingleTable || fileFormat == ImportExportDataProcess.MULTIPLE_FILE) {
                    
                    builder.writeTo(outputStream);
                }
                
                setProgressStatus(totalRecords);
                
                outputBuffer.append("Export successful for table: ");
                outputBuffer.append(tableName);
                appendProgressText(outputBuffer);
                
                appendFileInfo(exportFile);
                
            }
            
            if (!isSingleTable && fileFormat == ImportExportDataProcess.SINGLE_FILE) {     
                
                builder.writeTo(outputStream);
                
                if (outputStream != null) {
            
                    outputStream.close();
                }                

            }
            
            outputStream = null;
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
            
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
            
            finish();
            setTableCount(tableCount);
            setRecordCount(totalRecordCount + errorCount);
            setErrorCount(errorCount);
            setRecordCountProcessed(totalRecordCount);
        }

    }

    private FileOutputStream createOutputStream(File path) throws FileNotFoundException {

        return new FileOutputStream(path, false);
    }
    
    private ExcelWorkbookBuilder createExcelWorkbookBuilder() {
        
        return new DefaultExcelWorkbookBuilder();
    }

    private void logException(Throwable e) {
        if (Log.isDebugEnabled()) {
            Log.debug("Error on Excel export.", e);
        }
    }
    
    /** 
     * Cancels an in progress SQL statement. 
     */
    private void cancelStatement() {
        try {
            if (stmnt != null) {
                stmnt.cancel();
                stmnt.close();
            }
        } catch (SQLException e) {}
    }
    
    /** 
     * Cancels the current in-process transfer. 
     */
    public void cancelTransfer() {
        worker.interrupt();
        _parent.cancelTransfer();
    }
    
    /** 
     * Indicates that the process has completed. 
     */
    public void finished() {}
    
}







