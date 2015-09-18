/*
 * ImportXMLWorker.java
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

import java.io.CharArrayWriter;
import java.io.File;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.datasource.ConnectionDataSource;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.ColumnData;
import org.executequery.log.Log;
import org.underworldlabs.swing.util.SwingWorker;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** 
 * Performs the 'work' during the import XML process.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ImportXMLWorker extends AbstractImportExportWorker 
                             implements Constants {
    
    /** The worker object performing the process */
    private SwingWorker worker;

    /** The tables to be imported */
    private String[] tablesArray;
    
    /** The type of table transfer - single/multiple */
    private int tableTransferType;
    
    /** the current file in process */
    private String currentImportFileName;
    
    /** the process result */
    private String processResult;

    /** the specified rollback size */
    private int rollbackSize = 0;

    /** Whether we are halting on errors */
    private boolean haltOnError;

    /** the number of errors */
    private int errorCount = 0;

    // ---------------------------------------
    // table specific counters

    /** the record count per table */
    private int tableRowCount = 0;
    
    /** the table commit count */
    private int tableCommitCount = 0;

    /** the table statement result */
    private int tableInsertCount = 0;

    // ---------------------------------------
    // total import process counters

    /** the current commit block size */
    private int commitCount = 0;

    /** the total record count */
    private int totalRecordCount = 0;

    /** the total number of records inserted */
    private int totalInsertCount = 0;

    /** the file format - single or multiple */
    private int fileFormat;
    
    // ---------------------------------------

    /** SAX exception message not to be printed */
    private final String SAX_NO_PRINT_EXCEPTION = "SAX_NO_PRINT";
    
    public ImportXMLWorker(ImportExportProcess parent,
                           ImportExportProgressPanel progress) {
        super(parent, progress);
        tableTransferType = parent.getTableTransferType();
        transferData();
    }
    
    // initialise results Hashtable and start the worker
    private void transferData() {

        setIndeterminateProgress(true);
        reset();
        
        worker = new SwingWorker() {
            public Object construct() {
                return doWork();
            }
            public void finished() {
                String result = (String)get();
                setResult(result);
                printResults();
                setProgressStatus(-1);
                setIndeterminateProgress(false);
                getParent().setProcessComplete(result == SUCCESS);
                GUIUtilities.scheduleGC();
            }
        };
        worker.start();
    }
    
    // retrieve the file, create the parser and start parseing
    private Object doWork() {
        
        haltOnError = (getParent().getOnError() == ImportExportProcess.STOP_TRANSFER);

        // the XML handler
        ImportXMLHandler handler = null;
        
        // the parser factory for the process
        SAXParserFactory factory = null;
        
        // the parser for the process
        SAXParser parser = null;
        
        // the transfer objects
        Vector<DataTransferObject> transfers = getParent().getDataFileVector();
        
        // single or multiple file for mutliple transfer
        fileFormat = getParent().getMutlipleTableTransferType();

        // the rollback/commit block size
        rollbackSize = getParent().getRollbackSize();
        
        appendProgressText("Beginning import from XML process...");
        appendProgressText("Using connection: " + 
                getParent().getDatabaseConnection().getName());

        // record the start time
        start();

        try {           
            // the size of the transfer
            int transfers_size = transfers.size();

            // the current import file
            File importFile = null;
            
            factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            handler = new ImportXMLHandler();
            
            for (int i = 0; i < transfers_size; i++) {

                DataTransferObject obj = (DataTransferObject)transfers.get(i);

                if (fileFormat == ImportExportProcess.SINGLE_FILE) {
                    tablesArray = getParent().getSelectedTables();
                }
                else {
                    tablesArray = new String[]{obj.getTableName()};
                }

                importFile = new File(obj.getFileName());
                currentImportFileName = importFile.getName();

                if (i > 0) {
                    handler.reset();
                }

                parser = factory.newSAXParser();
                parser.parse(importFile, handler);
            }

            // commit the last remaining block or where 
            // set to commit at the end of all files
            if (rollbackSize != ImportExportProcess.COMMIT_END_OF_FILE) {
                setProgressStatus(-1);
                try {
                    if (conn != null) {
                        conn.commit();
                        totalInsertCount += commitCount;
                    }
                }
                catch (SQLException e) {
                    errorCount++;
                    processResult = FAILED;
                    outputExceptionError("Error committing last transaction block", e);
                }
            }

            if (processResult == null) {
                return SUCCESS;
            }
            return processResult;
            
        }
        catch (SAXException e) {
            if (e.getMessage() != SAX_NO_PRINT_EXCEPTION) {
                outputExceptionError("Error parsing XML data file", e);
            }
            if (processResult != CANCELLED) {
                return FAILED;
            }
            return processResult;
        }
        catch (Exception e) {
            logException(e);
            outputExceptionError("Error importing table data from XML", e);
            return FAILED;
        } 
        finally {
            finish();
            setRecordCount(totalRecordCount);
            setRecordCountProcessed(totalInsertCount);
            setErrorCount(errorCount);
        }
    }
   
    private void logException(Throwable e) {
        if (Log.isDebugEnabled()) {
            Log.debug("Error on XML import.", e);
        }
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
    
    private String lastTableResultsPrinted;
    
    /**
     * Prints the table specific execution results to the output buffer.
     */
    private void printTableResult(int tableRowCount, 
                                  int tableInsertCount, 
                                  String tableName) {
        
        if (tableName.equals(lastTableResultsPrinted)) {

            return;
        }
        
        // update the progress display
        outputBuffer.append("Records processed: ");
        outputBuffer.append(tableRowCount);
        outputBuffer.append("\nRecords inserted: ");
        outputBuffer.append(tableInsertCount);
        outputBuffer.append("\nImport complete for table: ");
        outputBuffer.append(tableName);
        appendProgressText(outputBuffer);
        lastTableResultsPrinted = tableName;
    }

    class ImportXMLHandler extends DefaultHandler {

        private CharArrayWriter contents = new CharArrayWriter();
        
        // ----------------------------------
        // --- reuseable string constants ---
        // ----------------------------------
        private final char COMMA = ',';
        
        // the date format
        private String dateFormatString;

        // the current table's column meta-data
        private Vector<ColumnData> columns;
        
        // the ongoing rollback count
        private int rollbackCount = 0;
        
        // the table tag name
        private String tableTag;
        
        // the table tag attribute
        private String tableAtt;
        
        // the table identifier as shown in the XML file
        private String tableIdentifier;
        
        // the row identifier as shown in the XML file
        private String rowIdentifier;
        
        // denotes the first tag pass
        private boolean firstPass;
        
        //  whether the row tag has been encountered
        private boolean rowTagFound;
        
        // denotes the second tag pass
        private int passes;
        
        // the date format for DATE type fields
        private SimpleDateFormat df;
        
        // whether the table name is an attribute
        private boolean hasTableAttribute;
        
        // whether this is to be run as a batch process
        private boolean isBatch;
        
        // whether we are parsing date formats
        private boolean parsingDates;
        
        private boolean importThisTable;

        // the table currently being processed
        private String tableName;

        // columns in the import file to be ignored
        private Map<String,String> ignoredColumns;
        
        // currently bound variables in the prepared statement
        private Map<String,String> boundVariables;

        public ImportXMLHandler() {
            dateFormatString = getParent().getDateFormat();
            isBatch = getParent().runAsBatchProcess();
            
            if (dateFormatString != null) {
                if (dateFormatString.length() == 0) {
                    dateFormatString = null;
                } else {
                    df = new SimpleDateFormat(dateFormatString);
                }
            }

            ImportExportXMLPanel _parent = (ImportExportXMLPanel)getParent();
            hasTableAttribute = _parent.hasTableNameAsAttribute();
            tableIdentifier = _parent.getTableIdentifier();
            rowIdentifier = _parent.getRowIdentifier();
            parsingDates = _parent.parseDateValues();
            
            if (hasTableAttribute) {
                tableTag = (tableIdentifier.substring(0,
                                tableIdentifier.indexOf(COMMA))).trim();
                tableAtt = (tableIdentifier.substring(
                                tableIdentifier.indexOf(COMMA)+1)).trim();
            } else {
                tableTag = tableIdentifier;
            }

            firstPass = true;
            rowTagFound = false;
            passes = 0;
        }

        protected void reset() {
            firstPass = true;
            rowTagFound = false;
            passes = 0;
            tableName = null;
            lastStartElement = null;
            columns = null;
        }
        
        private String lastStartElement;

        public void startElement(String nameSpaceURI, 
                                 String localName,
                                 String qName, 
                                 Attributes attrs) throws SAXException {

            contents.reset();
            passes++;

            lastStartElement = localName;
            
            if (tableTransferType == ImportExportProcess.MULTIPLE_TABLE 
                    && !hasTableAttribute) {

                if (fileFormat == ImportExportProcess.SINGLE_FILE) {
                    tableTag = tablesArray[tableCount];
                } else {
                    tableTag = tablesArray[0];
                }

            }

            // check if we have found the row tag (only if we haven't)
            if (!rowTagFound && localName.equalsIgnoreCase(rowIdentifier)) {
                rowTagFound = true;
            }

            if (localName.equalsIgnoreCase(tableTag)) {
                firstPass = false;
                rowTagFound = false;
                
                if (hasTableAttribute) {
                    if (attrs.getIndex(tableAtt) == -1) {
                        appendProgressErrorText(
                            "The attribute name entered was not found.\nProcess is exiting.");
                        processResult = FAILED;
                        getParent().cancelTransfer();
                        throw new SAXException(SAX_NO_PRINT_EXCEPTION);
                    } 
                    else {
                        tableName = attrs.getValue(tableAtt);
                    }
                }
                else {
                    tableName = tableTag;
                }

                tableName = findTableName(tableName);
                // table is in file but not selected for import so skip
                if (tableName == null) {
                    importThisTable = false;
                    return;
                }
                else {
                    importThisTable = true;
                }

                // reset table counters
                tableInsertCount = 0;
                tableCommitCount = 0;
                tableRowCount = 0;
                rollbackCount = 0;

                // increment the table count
                tableCount++;
                
                // retrieve the columns to be imported (or all)
                try {
                    columns = getColumns(tableName);
                } catch (SQLException e) {}
                int columnCount = columns.size();
                
                if (parsingDates && dateFormatString == null) {
                    // check for a date data type
                    boolean hasDateField = false;
                    for (int j = 0; j < columnCount; j++) {
                        if (dateFormatString == null) {
                            ColumnData cd = (ColumnData)columns.get(j);

                            int sqlType = cd.getSQLType();
                            if (sqlType == Types.DATE || sqlType == Types.TIME ||
                                    sqlType == Types.TIMESTAMP) {
                                hasDateField = true;
                                break;
                            }

                        }
                    }                

                    if (hasDateField && dateFormatString == null) {
                        dateFormatString = verifyDate();
                        df = new SimpleDateFormat(dateFormatString);
                    }
                }

                if (boundVariables == null) {
                    boundVariables = new HashMap<String,String>();
                } else {
                    boundVariables.clear();
                }

                outputBuffer.append("---------------------------\nTable: ");
                outputBuffer.append(tableName);
                outputBuffer.append("\nImport File: ");
                outputBuffer.append(currentImportFileName);
                appendProgressText(outputBuffer);

                // prepare the statement
                try {
                    prepareStatement(tableName, columns);
                } catch (Exception e) {
                    processResult = FAILED;
                    outputExceptionError("Error preparing import SQL statement", e);
                    throw new SAXException(SAX_NO_PRINT_EXCEPTION);
                }
            }
            // if we encounter a row tag but have not yet
            // encountered a table tag, display error
            else if (rowTagFound && firstPass) {
                appendProgressErrorText(
                    "The table tag name entered was not found.\nProcess is exiting.");
                processResult = FAILED;
                getParent().cancelTransfer();
                throw new SAXException(SAX_NO_PRINT_EXCEPTION);
            }
            // if we have not encountered a row tag and
            // this is after the second pass ie. schema and
            // table elements have been found, show error
            else if (passes > 2 && !rowTagFound) {
                appendProgressErrorText(
                    "The XML tag elements for a table row as entered were not found.\n" +
                        "Process is exiting.");
                processResult = FAILED;
                getParent().cancelTransfer();
                throw new SAXException(SAX_NO_PRINT_EXCEPTION);
            }
            
        }

        private boolean cancelled;
        
        public void endElement(String nameSpaceURI, String localName,
                               String qName) throws SAXException {
            
            if (!importThisTable) {
                return;
            }
            
            try {
            
                if (Thread.interrupted()) {
                    cancelled = true;
                    printTableResult(tableRowCount, 
                            tableCommitCount, tableName);
                    throw new InterruptedException();
                }

                // check if we have reached the end of a row tag
                if (localName.equalsIgnoreCase(rowIdentifier)) {
                    
                    // check all variables are bound - insert NULL otherwise
                    for (int i = 0, n = columns.size(); i < n; i++) {
                        ColumnData columnData = columns.get(i);
                        String columnName = columnData.getColumnName();
                        Object bind = boundVariables.get(columnName);
                        if (bind == VARIABLE_NOT_BOUND) {
                            prepStmnt.setNull(i + 1, columnData.getSQLType());
                        }
                    }

                    try {
                        tableRowCount++;
                        rollbackCount++;
                        totalRecordCount++;

                        if (isBatch) { // add to batch if in batch mode
                            prepStmnt.addBatch();
                        }
                        else { // just execute otherwise
                            int result = prepStmnt.executeUpdate();
                            tableInsertCount += result;
                            commitCount += result;
                        }

                        if (rollbackCount == rollbackSize) {
                            if (isBatch) {
                                int result = getBatchResult(prepStmnt.executeBatch())[0];
                                tableInsertCount += result;
                                commitCount += result;
                                prepStmnt.clearBatch();
                            }
                            conn.commit();
                            totalInsertCount += commitCount;
                            tableCommitCount = tableInsertCount;
                            rollbackCount = 0;
                            commitCount = 0;                            
                        }                        
                    }
                    catch (SQLException e) {
                        logException(e);
                        errorCount++;
                        processResult = FAILED;
                        outputExceptionError("Error importing table data from XML", e);

                        if (haltOnError) {
                            throw new SAXException(SAX_NO_PRINT_EXCEPTION);
                        }
                        
                    }
                    return;
                }
                // check if we have reached the end of a table tag
                else if (localName.equalsIgnoreCase(tableTag)) {
                    
                    if (isBatch) {
                        int[] batchResult = null;
                        
                        try {
                            batchResult = getBatchResult(prepStmnt.executeBatch());
                            int result = batchResult[0];
                            tableInsertCount += result;
                            commitCount += result;
                            tableCommitCount = tableInsertCount;
                        }
                        catch (BatchUpdateException e) {
                            logException(e);
                            batchResult = getBatchResult(e.getUpdateCounts());
                            errorCount += batchResult[1];

                            outputBuffer.append("An error occured during the batch process: ");
                            outputBuffer.append(e.getMessage());
                            
                            SQLException _e = e.getNextException();
                            while (_e != null) {
                                outputBuffer.append("\nNext Exception: ");
                                outputBuffer.append(_e.getMessage());
                                _e = _e.getNextException();
                            }

                            outputBuffer.append("\n\nRecords processed to ");
                            outputBuffer.append("the point where this error occurred: ");
                            outputBuffer.append(batchResult.length);
                            appendProgressErrorText(outputBuffer);
                            processResult = FAILED;
                            
                            if (haltOnError) {
                                throw new SAXException(SAX_NO_PRINT_EXCEPTION);
                            }

                        }
                        
                        if (tableRowCount != tableInsertCount) {
                            conn.rollback();
                            if (haltOnError) {
                                getParent().cancelTransfer();
                                processResult = FAILED;
                                throw new SAXException("Failed to import all rows");
                            }                            
                        }

                    }

                    if (cancelled) {
                        throw new InterruptedException();
                    }

                    // do the commit rollback size selected is end of file
                    if (rollbackSize == ImportExportProcess.COMMIT_END_OF_FILE) {
                        conn.commit();
                        totalInsertCount += commitCount;
                        tableCommitCount = tableInsertCount;
                        commitCount = 0;
                    }

                    // log some record progress figures
                    printTableResult(tableRowCount, 
                            tableInsertCount, tableName);

                    return;
                }

                // -----------------------------------------
                // must be a column value

                if (!isSelectedColumn(localName)) {
                    return;
                }
                
                int index = getColumnIndex(localName);
                if (index == -1) {

                    if (localName.equals(lastStartElement)) {
                        
                        // if it doesn't exist in the insert table
                        // check the column selections in cases of 
                        // single file import
                        if (isIgnoredColumn(lastStartElement)) {
                            return;
                        }

                        outputBuffer.append("The column ");
                        outputBuffer.append(localName);
                        outputBuffer.append(" specified within the ");
                        outputBuffer.append("import\nfile does not ");
                        outputBuffer.append("exist in the current table");
                        appendProgressErrorText(outputBuffer);
                        processResult = FAILED;
                        throw new SAXException(SAX_NO_PRINT_EXCEPTION);
                    }
                    else {
                        return;
                    }
                }

                String value = contents.toString();
                if (value != null && value.trim().length() == 0) {
                    value = null;
                }

                ColumnData cd = (ColumnData)columns.get(index);
                try {
                    setValue(value,
                            (index+1), 
                            cd.getSQLType(),
                            false,
                            df);
                    // this is now a bound variable
                    boundVariables.put(cd.getColumnName(), VARIABLE_BOUND);
                }
                catch (ParseException e) {
                    errorCount++;
                    processResult = FAILED;
                    outputBuffer.append("Error parsing date value for column ");
                    outputBuffer.append(cd.getColumnName());
                    outputExceptionError(null, e);
                    throw new SAXException(SAX_NO_PRINT_EXCEPTION);
                }
                catch (NumberFormatException e) {
                    errorCount++;
                    processResult = FAILED;
                    outputBuffer.append("Error parsing value for column ");
                    outputBuffer.append(cd.getColumnName());
                    outputExceptionError(null, e);
                    throw new SAXException(SAX_NO_PRINT_EXCEPTION);
                }

            }
            catch (InterruptedException e) {

                if (processResult != FAILED) {
                    processResult = CANCELLED;
                }

                try {
                    prepStmnt.cancel();
                    conn.rollback();
                } catch (SQLException e2) {
                    outputExceptionError("Error rolling back transaction", e);
                }

                throw new SAXException("Process interrupted");
            }
            // catch if all else dumps
            catch (SQLException e) {
                logException(e);
                errorCount++;
                processResult = FAILED;
                outputExceptionError("Error importing table data from XML", e);
                
                if (haltOnError) {
                    throw new SAXException(SAX_NO_PRINT_EXCEPTION);
                }

            }
            catch (Exception e) {
                logException(e);
                errorCount++;
                processResult = FAILED;
                outputExceptionError("Error importing table data to file", e);

                if (haltOnError) {
                    throw new SAXException(SAX_NO_PRINT_EXCEPTION);
                }

            }

        }

        private int[] getBatchResult(int[] updateCount) throws SQLException {
            int insert = 0;
            int success = 0;
            int errors = 0;
            
            // annoying as becoming a little db specific,
            // but Oracle returns -2 on a successful batch
            // execution using prepared statement (-3 on error)
            
            if (isOracle()) {
                
                success = -2;

            } else {
                
                success = 1;
            }

            for (int i = 0; i < updateCount.length; i++) {
                if (updateCount[i] == success) {
                    insert++;
                } else {
                    errors++;
                }
            }

            int[] result = {insert, errors};
            return result;
        }

        /**
         * Evaluates whether the specified column is a selected
         * import column in cases of a single file import where
         * column selection is allowed.
         *
         * @param columnName - the column to be evaluated
         */
        private boolean isSelectedColumn(String columnName) {
            // return true for a multi-table import
            if (parent.getTableTransferType() == 
                    ImportExportProcess.MULTIPLE_TABLE) {
                return true;
            }

            // check the cache first
            if (ignoredColumns != null) {
                if (ignoredColumns.containsKey(columnName)) {
                    return false;
                }
            }
         
            // check the selected columns
            Vector<ColumnData> columns = parent.getSelectedColumns();
            for (int i = 0, n = columns.size(); i < n; i++) {
                
                if (columns.get(i).getColumnName().equalsIgnoreCase(columnName)) {
                    return true;
                }
                
            }
            return false;
        }
        
        /**
         * Evaluates whether the specified column is not a selected
         * import column in cases of a single file import where
         * column selection is allowed.
         *
         * @param columnName - the column to be evaluated
         */
        private boolean isIgnoredColumn(String columnName) {
            // return false for a multi-table import
            if (parent.getTableTransferType() == 
                    ImportExportProcess.MULTIPLE_TABLE) {
                return false;
            }

            // check the cache first
            if (ignoredColumns != null) {
                if (ignoredColumns.containsKey(columnName)) {
                    return true;
                }
            }
            
            // check the selected columns
            Vector<ColumnData> columns = parent.getSelectedColumns();
            for (int i = 0, n = columns.size(); i < n; i++) {
                
                if (columns.get(i).getColumnName().equalsIgnoreCase(columnName)) {
                    return false;
                }
                
            }

            // otherwise add to the ignored list
            if (ignoredColumns == null) {
                ignoredColumns = new HashMap<String,String>();
            }
            ignoredColumns.put(columnName, columnName);
            return true;
        }
        
        private int getColumnIndex(String columnName) {
            for (int i = 0, n = columns.size(); i < n; i++) {
                ColumnData cd = columns.get(i);
                String _columnName = cd.getColumnName();
                if (_columnName.equalsIgnoreCase(columnName)) {
                    return i;
                }
            }
            return -1;
        }
        
        private String findTableName(String name) {
            for (int i = 0; i < tablesArray.length; i++) {
                if (name.equalsIgnoreCase(tablesArray[i])) {
                    return tablesArray[i];
                }
            }
            return null;
        }

        public void characters(char[] data, int start, int length) {
            contents.write(data, start, length);
        }
        
        public void ignorableWhitespace(char[] data, int start, int length) {
            characters(data, start, length);
        }
        
        public void error(SAXParseException spe) throws SAXException {
            throw new SAXException(spe.getMessage());
        }
        
    } // ImportXMLHandler
    
    
}














