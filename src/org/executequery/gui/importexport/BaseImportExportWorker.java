/*
 * BaseImportExportWorker.java
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.executequery.repository.LogRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.StringBundle;
import org.executequery.util.SystemResources;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

public abstract class BaseImportExportWorker {

    private ImportExportWizard importExportWizard;

    private ImportExportResult importExportResult;
    
    private ImportExportMonitor monitor;
    
    private int recordCount;
    
    private int recordCountProcessed;
    
    private int errorCount;
    
    private int tableCount;
    
    private long startTime;

    private long finishTime;

    public BaseImportExportWorker(ImportExportWizard importExportWizard) {
        
        super();

        this.importExportWizard = importExportWizard;
        
        monitor = importExportWizard.getImportExportMonitor();
        
    }
    
    /**
     * Appends the specified text to the output pane as normal
     * fixed width text.
     *
     * @param text - the text to be appended
     */
    protected void appendProgressText(String text) {
        monitor.appendProgressText(text);
    }

    /**
     * Appends the specified text to the output pane as normal
     * fixed width text.
     *
     * @param text - the text to be appended
     */
    protected void appendProgressErrorText(String text) {
        monitor.appendProgressErrorText(text);
    }

    /**
     * Appends the specified buffer to the output pane as normal
     * fixed width text.
     *
     * @param text - the text to be appended
     */
    protected void appendProgressText(StringBuilder text) {
        monitor.appendProgressText(text.toString());
        text.setLength(0);
    }

    /**
     * Appends the specified buffer to the output pane as error
     * fixed width text.
     *
     * @param text - the text to be appended
     */
    protected void appendProgressErrorText(StringBuilder text) {
        monitor.appendProgressErrorText(text.toString());
        text.setLength(0);
    }

    /**
     * Appends the specified buffer to the output pane as warning
     * fixed width text.
     *
     * @param text - the text to be appended
     */
    protected void appendProgressWarningText(StringBuilder text) {
        monitor.appendProgressWarningText(text.toString());
        text.setLength(0);
    }

    /**
     * Sets the progress bar as indeterminate as specified
     */
    protected void setIndeterminateProgress(boolean indeterminate) {
        monitor.setIndeterminate(indeterminate);
    }
    
    /**
     * Sets the maximum value for the progess bar.
     *
     * @param value - the max value
     */
    protected void setProgressBarMaximum(int value) {
        monitor.setMaximum(value);
    }
    
    /** 
     * Sets the progress bar's position during the process.
     *
     * @param the new process status
     */
    public void setProgressStatus(int status) {
        monitor.setProgressStatus(status);
    }

    /**
     * Releases all held database resources.
     */
    /*
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
                conn.setAutoCommit(true);
                ConnectionManager.close(dc, conn);
                conn = null;
            }

        } 
        catch (DataSourceException e) {
            System.err.println(
                    "Exception releasing resources at: " + e.getMessage());            
        }
        catch (SQLException e) {
            System.err.println(
                    "Exception releasing resources at: " + e.getMessage());
        }
    }
*/
    protected void outputExceptionError(String message, Throwable e) {
        
        StringBuilder outputBuffer = new StringBuilder();
        
        if (message != null) {
            outputBuffer.append(message);
        }
        outputBuffer.append("\n[ ");
        outputBuffer.append(MiscUtils.getExceptionName(e));
        outputBuffer.append(" ] ");
        
        if (e instanceof DataSourceException) {
            outputBuffer.append(e.getMessage());
            outputBuffer.append(((DataSourceException)e).getExtendedMessage());
        }
        else if (e instanceof SQLException) {
            outputBuffer.append(e.getMessage());
            SQLException _e = (SQLException)e;
            outputBuffer.append(getBundle().getString(
                    "AbstractImportExportWorker.errorCode", 
                    String.valueOf(_e.getErrorCode())));

            String state = _e.getSQLState();
            if (state != null) {
                outputBuffer.append(getBundle().getString(
                        "AbstractImportExportWorker.stateCode", state));
            }

        }
        else {
            outputBuffer.append(e.getMessage());
        }

        appendProgressErrorText(outputBuffer);
    }
    
    protected final void cancelStatement(Statement statement) {

        if (statement != null) {
         
            try {
            
                statement.cancel();
                statement.close();

            } catch (SQLException e) {}
        }

    }

    protected final void closeResultSet(ResultSet resultSet) {

        if (resultSet != null) {

            try {
                
                resultSet.close();

            } catch (SQLException e) {}
        }
    }

    protected final void closeStatement(Statement statement) {

        if (statement != null) {
         
            try {
            
                statement.close();

            } catch (SQLException e) {}
        }

    }


    /**
     * Appends the final process results to the output pane.
     */
    protected void printResults() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------\n");

        if (ImportExportResult.isSuccess(importExportResult)) {

            sb.append(getBundle().getString("AbstractImportExportWorker.processCompletedSuccessfully"));

        } else if (ImportExportResult.isCancelled(importExportResult)) {
            
            sb.append(getBundle().getString("AbstractImportExportWorker.processCancelled"));

        } else if (ImportExportResult.isFailed(importExportResult)) {
          
            sb.append(getBundle().getString("AbstractImportExportWorker.processCompletedWithErrors"));
        }

        sb.append(getBundle().getString("AbstractImportExportWorker.totalDuration"));
        sb.append(getFormattedDuration());
        sb.append(getBundle().getString("AbstractImportExportWorker.totalTablesProcessed"));
        sb.append(tableCount);
        sb.append(getBundle().getString("AbstractImportExportWorker.totalRecordsProcessed"));
        sb.append(recordCount);
        sb.append(getBundle().getString("AbstractImportExportWorker.totalRecordsTransferred"));
        sb.append(recordCountProcessed);
        sb.append(getBundle().getString("AbstractImportExportWorker.errors"));
        sb.append(errorCount);

        appendProgressText(sb.toString());
        
        // log the output to file
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                logOutputToFile();
            }
        });
        
    }
    
    protected void printExportFileSize(ImportExportFile importExportFile) {
        
        StringBuilder sb = new StringBuilder();

        sb.append(getBundle().getString("AbstractImportExportWorker.outputFileName"));
        sb.append(importExportFile.getFile().getName());
        
        long fileSize = importExportFile.getFile().length();
        
        sb.append(getBundle().getString("AbstractImportExportWorker.outputFileSize"));
        sb.append(new DecimalFormat("0.00").format(MiscUtils.bytesToMegaBytes(fileSize)));
        sb.append("Mb");

        appendProgressText(sb.toString());        
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
        monitor.reset();
        startTime = System.currentTimeMillis();
    }
    
    /**
     * Logs the finish time of this process.
     */
    protected void finish() {
        finishTime = System.currentTimeMillis();
        monitor.setStopButtonEnabled(false);
    }
    
    /**
     * Logs the contents of the output pane to file.
     */
    protected void logOutputToFile() {
        PrintWriter writer = null;
        try {
            String logHeader = null;
            String path = logFileDirectory();
            
            if (isExport()) {
                logHeader = "[ Data Export Process - ";
                path += SystemProperties.getProperty("system", "eq.export.log");
            } else {
                logHeader = "[ Data Import Process - ";
                path += SystemProperties.getProperty("system", "eq.import.log");
            }

            // add a header for this process
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            StringBuilder sb = new StringBuilder();
            sb.append(logHeader);
            sb.append(df.format(new Date(startTime)));
            sb.append(" ]\n\n");
            sb.append(monitor.getText());
            sb.append("\n\n");

            writer = new PrintWriter(new FileWriter(path, true), true);
            writer.println(sb.toString());
            sb = null;
        } 
        catch (IOException e) {}
        finally {
            if (writer != null) {
                writer.close();
            }
            writer = null;
        }
    }
    
    private String logFileDirectory() {
        
        return ((LogRepository)RepositoryCache.load(
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

    public ImportExportResult getResult() {
        return importExportResult;
    }

    public void setResult(ImportExportResult result) {
        this.importExportResult = result;
    }

    private StringBundle bundle() {
        if (bundle == null) {            
            bundle = SystemResources.loadBundle(getClass());
        }
        return bundle;
    }

    protected final boolean isExport() {
        return importExportDataModel().isExport();
    }
    
    protected final ImportExportWizard importExportWizard() {
        return importExportWizard;
    }
    
    protected final ImportExportDataModel importExportDataModel() {
        return importExportWizard.getExportDataModel();
    }

    protected final String getString(String key) {
        return getBundle().getString(key);
    }

    protected final StringBundle getBundle() {
        return bundle();
    }

    private StringBundle bundle;
    
}






