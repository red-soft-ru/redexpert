/*
 * ImportDelimitedWorker.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.gui.browser.ColumnData;
import org.executequery.log.Log;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ImportDelimitedWorker extends AbstractImportExportWorker {

    /** The <code>SwingWorker</code> object for this process */
    private SwingWorker worker;

    /** Whether we are halting on errors */
    private boolean haltOnError;

    /**
     * Constructs a new instance with the specified parent object - an
     * instance of <code>ImportExportDelimitedPanel</code>.
     *
     * @param the parent for this process
     */
    public ImportDelimitedWorker(ImportExportDataProcess parent,
                                 ImportExportProgressPanel importingDialog) {
        super(parent, importingDialog);
        transferData();
    }

    private void transferData() {
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
                getParent().setProcessComplete(result == SUCCESS);
                GUIUtilities.scheduleGC();
            }
        };
        worker.start();
    }

    private Object doWork() {

        // the process result
        String processResult = null;

        // are we halting on any error
        int onError = getParent().getOnError();
        haltOnError = (onError == ImportExportDataProcess.STOP_TRANSFER);

        boolean isBatch = getParent().runAsBatchProcess();

        appendProgressText("Beginning import from delimited file process...");
        appendProgressText("Using connection: " +
                getParent().getDatabaseConnection().getName());

        // ---------------------------------------
        // table specific counters

        // the table statement result
        int tableInsertCount = 0;

        // the records processed for this table
        int tableRowCount = 0;

        // the table commit count
        int tableCommitCount = 0;

        // ---------------------------------------
        // total import process counters

        // the current commit block size
        int commitCount = 0;

        // the total records inserted
        int totalInsertCount = 0;

        // the total records processed
        int totalRecordCount = 0;

        // the error count
        int errorCount = 0;

        // the current line number
        int lineNumber = 0;

        int rollbackSize = getParent().getRollbackSize();
        int rollbackCount = 0;

        FileReader fileReader = null;
        BufferedReader reader = null;
        DateFormat dateFormat = null;

        try {
            // retrieve the import files
            Vector files = getParent().getDataFileVector();
            int fileCount = files.size();

            // whether to trim whitespace
            boolean trimWhitespace = getParent().trimWhitespace();

            // whether this table has a date/time field
            boolean hasDate = false;

            // whether we are parsing date formats
            boolean parsingDates = parseDateValues();

            // column names are first row
            boolean hasColumnNames = getParent().includeColumnNames();

            // currently bound variables in the prepared statement
            Map<ColumnData,String> boundVariables = null;

            // ignored indexes of columns from the file
            List<Integer> ignoredIndexes = null;

            if (hasColumnNames) {
                boundVariables = new HashMap<ColumnData,String>();
                ignoredIndexes = new ArrayList<Integer>();
                appendProgressText(
                        "Using column names from input file's first row.");
            }

            // columns to be imported that are in the file
            Map<ColumnData,String> fileImportedColumns = new HashMap<ColumnData,String>();

            // whether the data format failed (switch structure)
            boolean failed = false;

            // define the delimiter
            String delim = getParent().getDelimiter();

            // ---------------------------
            // --- initialise counters ---
            // ---------------------------

            // the table's column count
            int columnCount = -1;

            // the length of each line in the file
            int rowLength = -1;

            // progress bar values
            int progressStatus = -1;

            // ongoing progress value
            int progressCheck = -1;

            // the import file size
            long fileSize = -1;

            // set the date format

            if (parseDateValues()) {

                try {

                    dateFormat = createDateFormatter();

                } catch (IllegalArgumentException e) {

                    errorCount++;
                    outputExceptionError("Error applying date mask", e);

                    return FAILED;
                }

            }

            // record the start time
            start();

            // setup the regex matcher for delims

            // ----------------------------------------------------------------
            // below was the original pattern from oreilly book.
            // discovered issues when parsing values with quotes
            // in them - not only around them.
            /*
            String regex =
                    "(?:^|\\" +
                    delim +
                    ") (?: \" ( (?> [^\"]*+ ) (?> \"\" [^\"]*+ )*+ ) \" | ( [^\"\\" +
                    delim + "]*+ ) )";
            Matcher matcher = Pattern.compile(regex, Pattern.COMMENTS).matcher("");
            Matcher qMatcher = Pattern.compile("\"\"", Pattern.COMMENTS).matcher("");
            */
            // ----------------------------------------------------------------

            // modified above to regex below
            // added the look-ahead after the close quote
            // and removed the quote from the last regex pattern

            String escapedDelim = escapeDelim(delim);

            String regex =
                    "(?:^|" +
                    escapedDelim +
                    ") (?: \" ( (?> [^\"]*+ ) (?> \"\" [^\"]*+ )*+ ) \"(?=" +
                    escapedDelim +
                    "?) | ( [^" +
                    escapedDelim + "]*+ ) )";

            // ----------------------------------------------------------------
            // changed above to the following - seems to work for now
            // regex pattern in full - where <delim> is the delimiter to use
            //      \"([^\"]+?)\"<delim>?|([^<delim>]+)<delim>?|<delim>
            //
            // fixed oreilly one - not running this one
            // ----------------------------------------------------------------

            Matcher matcher = Pattern.compile(regex, Pattern.COMMENTS).matcher("");
            Matcher qMatcher = Pattern.compile("\"\"", Pattern.COMMENTS).matcher("");

            // ----------------------------------------
            // --- begin looping through the tables ---
            // ----------------------------------------

            // ensure the connection has auto-commit to false
            conn = getConnection();
            conn.setAutoCommit(false);

            int currentRowLength = 0;
            boolean insertLine = false;

            // the number of columns actually available in the file
            int filesColumnCount = 0;

            for (int i = 0; i < fileCount; i++) {

                lineNumber = 0;
                tableInsertCount = 0;
                tableCommitCount = 0;
                rollbackCount = 0;
                tableRowCount = 0;
                rowLength = 0;

                if (Thread.interrupted()) {
                    setProgressStatus(100);
                    throw new InterruptedException();
                }

                tableCount++;

                DataTransferObject dto = (DataTransferObject)files.elementAt(i);

                // initialise the file object
                File inputFile = new File(dto.getFileName());

                outputBuffer.append("---------------------------\nTable: ");
                outputBuffer.append(dto.getTableName());
                outputBuffer.append("\nImport File: ");
                outputBuffer.append(inputFile.getName());
                appendProgressText(outputBuffer);

                // setup the reader objects
                fileReader = new FileReader(inputFile);
                reader = new BufferedReader(fileReader);

                // retrieve the columns to be imported (or all)
                Vector<ColumnData> columns = getColumns(dto.getTableName());
                columnCount = columns.size();
                filesColumnCount = columnCount;

                // the wntire row read
                String row = null;

                // the current delimited value
                String value = null;

                // the ignored column count
                int ignoredCount = 0;

                // clear the file columns cache
                fileImportedColumns.clear();

                // if the first row in the file has the column
                // names compare these with the columns selected
                if (hasColumnNames) {

                    // init the bound vars cache with the selected columns
                    boundVariables.clear();

                    for (int k = 0; k < columnCount; k++) {

                        boundVariables.put(columns.get(k), VARIABLE_NOT_BOUND);
                    }

                    row = reader.readLine();
                    lineNumber++;

                    String[] _columns = MiscUtils.splitSeparatedValues(row, delim);
                    if (_columns != null && _columns.length > 0) {

                        filesColumnCount = _columns.length;

                        // --------------------------------------
                        // first determine if we have any columns in the
                        // input file that were not selected for import

                        // reset the ignored columns
                        ignoredIndexes.clear();

                        // set up another list to re-add the columns in
                        // the order in which they appear in the file.
                        // all other columns will be added to the end
                        Vector<ColumnData> temp = new Vector<ColumnData>(columnCount);

                        ColumnData cd = null;
                        int ignoredIndex = -1;
                        for (int j = 0; j < _columns.length; j++) {
                            ignoredIndex = j;
                            String column = _columns[j];

                            for (int k = 0; k < columnCount; k++) {
                                cd = columns.get(k);
                                String _column = cd.getColumnName();

                                if (_column.equalsIgnoreCase(column)) {
                                    temp.add(cd);
                                    fileImportedColumns.put(cd, INCLUDED_COLUMN);
                                    ignoredIndex = -1;
                                    break;
                                }

                            }

                            if (ignoredIndex != -1) {

                                ignoredIndexes.add(Integer.valueOf(ignoredIndex));
                            }

                        }
                        ignoredCount = ignoredIndexes.size();

                        // if we didn't find any columns at all, show warning
                        if (temp.isEmpty()) {

                            String message = "No matching column names were " +
                                    "found within the specified file's first line.\n" +
                                    "The current file will be ignored.";

                            outputBuffer.append(message);
                            appendProgressWarningText(outputBuffer);

                            int yesNo = GUIUtilities.displayYesNoDialog(
                                    message + "\nDo you wish to continue?",
                                    "Warning");

                            if (yesNo == JOptionPane.YES_OPTION) {
                                continue;
                            } else {
                                throw new InterruptedException();
                            }

                        } else {

                            // add any other selected columns to the
                            // end of the temp list with the columns
                            // available in the file
                            boolean addColumn = false;
                            for (int k = 0; k < columnCount; k++) {
                                addColumn = false;
                                cd = columns.get(k);
                                for (int j = 0, n = temp.size(); j < n; j++) {
                                    addColumn = true;
                                    if (temp.get(j) == cd) {
                                        addColumn = false;
                                        break;
                                    }
                                }

                                if (addColumn) {
                                    temp.add(cd);
                                }

                            }
                            columns = temp; // note: size should not have changed
                        }

                    }
                }
                // otherwise just populate the columns in the file
                // with all the selected columns
                else {

                    for (int j = 0; j < columnCount; j++) {

                        fileImportedColumns.put(columns.get(j), INCLUDED_COLUMN);
                    }

                }

                /*
                Log.debug("ignored count: " + ignoredCount);
                for (int j = 0; j < columnCount; j++) {
                    Log.debug("column: " + columns.get(j));
                }
                */

                fileSize = inputFile.length();
                progressStatus = 10;
                progressCheck = (int)(fileSize / progressStatus);

                // prepare the statement
                prepareStatement(dto.getTableName(), columns);

                if (parsingDates && dateFormat == null) {

                    // check for a date data type
                    for (int j = 0; j < columnCount; j++) {

                        if (dateFormat == null && !hasDate) {

                            ColumnData cd = columns.get(j);

                            if (fileImportedColumns.containsKey(cd)) {

                                if (cd.isDateDataType()) {

                                    hasDate = true;
                                    break;
                                }

                            }

                        }
                    }

                    if (hasDate && dateFormat == null) {

                        String pattern = verifyDate();

                        if (StringUtils.isNotBlank(pattern)) {

                            fileReader.close();
                            setProgressStatus(100);
                            throw new InterruptedException();
                        }

                        dateFormat = createDateFormatter(pattern);
                    }

                }

                rowLength = 0;

                while ((row = reader.readLine()) != null) {

                    insertLine = true;
                    lineNumber++;
                    tableRowCount++;
                    totalRecordCount++;

                    if (Thread.interrupted()) {

                        fileReader.close();
                        printTableResult(tableRowCount,
                                tableCommitCount, dto.getTableName());

                        setProgressStatus(100);
                        throw new InterruptedException();
                    }

                    currentRowLength = row.length();

                    if (currentRowLength == 0) {

                        outputBuffer.append("Line ");
                        outputBuffer.append(lineNumber);
                        outputBuffer.append(" contains no delimited values");
                        appendProgressWarningText(outputBuffer);

                        int yesNo = GUIUtilities.displayYesNoDialog(
                                "No values provided from line " +
                                lineNumber + " - the row is blank.\n" +
                                "Do you wish to continue?",
                                "Warning");

                        if (yesNo == JOptionPane.YES_OPTION) {
                            continue;
                        } else {
                            throw new InterruptedException();
                        }
                    }

                    rowLength += currentRowLength;
                    if (progressCheck < rowLength) {

                        setProgressStatus(progressStatus);
                        progressStatus += 10;
                        rowLength = 0;
                    }

                    // reset matcher with current row
                    matcher.reset(row);

                    int index = 0;
                    int lastIndex = -1;
                    int loopIgnoredCount = 0;

                    //Log.debug(row);

                    for (int j = 0; j < filesColumnCount; j++) {

                        if (matcher.find(index)) {

                            String first = matcher.group(2);

                            if (first != null) {

                                value = first;

                            } else {

                                qMatcher.reset(matcher.group(1));
                                value = qMatcher.replaceAll("\"");
                            }

                            index = matcher.end();

                            // check if its an ignored column
                            if (ignoredCount > 0) {

                                if (isIndexIgnored(ignoredIndexes, j)) {

                                    loopIgnoredCount++;
                                    continue;
                                }

                            }

                        } else {

                            // not enough delims check
                            if (j < (filesColumnCount - 1)
                                    && index > (currentRowLength - 1)) {

                                outputBuffer.append("Insufficient number of column ");
                                outputBuffer.append("values provided at line ");
                                outputBuffer.append(lineNumber);
                                appendProgressErrorText(outputBuffer);

                                int yesNo = GUIUtilities.displayYesNoDialog(
                                        "Insufficient number of values provided from line " +
                                        lineNumber + ".\n" +
                                        "Do you wish to continue?",
                                        "Warning");

                                if (yesNo == JOptionPane.YES_OPTION) {

                                    insertLine = false;
                                    break;

                                } else {

                                    throw new InterruptedException();
                                }

                            } else {

                                // check if we're on a delim the matcher didn't pick up

                                int delimLength = delim.length();

                                if (row.substring(index, index + delimLength).equals(delim)) {

                                    // increment index
                                    index++;
                                    // assign as null value
                                    value = null;
                                }

                            }

                        }

                        // check if we landed on the same index - likely null value
                        if (index == lastIndex) {
                            index++;
                        }
                        lastIndex = index;

                        if (value != null && value.trim().length() == 0) {
                            value = null;
                        }

                        try {
                            ColumnData cd = columns.get(j - loopIgnoredCount);
                            setValue(value,
                                     getIndexOfColumn(columns, cd) + 1,
                                     cd.getSQLType(),
                                     trimWhitespace,
                                     dateFormat);

                            if (hasColumnNames) {
                                boundVariables.put(cd, VARIABLE_BOUND);
                            }

                        } catch (ParseException e) {

                            errorCount++;
                            failed = true;
                            outputBuffer.append("Error parsing date value - ");
                            outputBuffer.append(value);
                            outputBuffer.append(" - on line ");
                            outputBuffer.append(lineNumber);
                            outputBuffer.append(" at position ");
                            outputBuffer.append(j);
                            outputExceptionError(null, e);
                            break;

                        } catch (NumberFormatException e) {

                            errorCount++;
                            failed = true;
                            outputBuffer.append("Error parsing value - ");
                            outputBuffer.append(value);
                            outputBuffer.append(" - on line ");
                            outputBuffer.append(lineNumber);
                            outputBuffer.append(" at position ");
                            outputBuffer.append(j);
                            outputExceptionError(null, e);
                            break;
                        }

                    }

                    if (!insertLine) {

                        prepStmnt.clearParameters();
                        continue;
                    }

                    if (failed && haltOnError) {

                        processResult = FAILED;
                        break;
                    }

                    // execute the statement
                    try {

                        // check all variables are bound if we used
                        // the column names from the first row
                        if (hasColumnNames) {

                            index = 0;
                            // check all variables are bound - insert NULL otherwise

                            for (Map.Entry<ColumnData, String> entry : boundVariables.entrySet()) {

                                ColumnData cd = entry.getKey();

                                if (VARIABLE_NOT_BOUND.equals(entry.getValue())) {

                                    index = getIndexOfColumn(columns, cd);
                                    prepStmnt.setNull(index + 1, cd.getSQLType());
                                }

                            }

                        }

                        if (isBatch) {
                            prepStmnt.addBatch();
                        } else {
                            int result = prepStmnt.executeUpdate();
                            tableInsertCount += result;
                            commitCount += result;
                        }

                        rollbackCount++;
                        // check the rollback segment
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

                        // reset bound variables
                        if (hasColumnNames) {
                            for (int k = 0; k < columnCount; k++) {
                                boundVariables.put(columns.get(k), VARIABLE_NOT_BOUND);
                            }
                        }

                    }
                    catch (SQLException e) {
                        logException(e);
                        errorCount++;

                        if (!isBatch) {
                            outputBuffer.append("Error inserting data from line ");
                            outputBuffer.append(lineNumber);
                            outputExceptionError(null, e);
                        }
                        else {
                            outputBuffer.append("Error on last batch execution");
                            outputExceptionError(null, e);
                        }

                        if (haltOnError) {
                            processResult = FAILED;
                            conn.rollback();
                            getParent().cancelTransfer();
                            throw new InterruptedException();
                        }

                    }

                }

                // ----------------------------
                // file/table has ended here

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
                        int[] updateCounts = e.getUpdateCounts();
                        batchResult = getBatchResult(updateCounts);
                        errorCount += batchResult[1];
                        if (errorCount == 0) {
                            errorCount = 1;
                        }

                        outputBuffer.append("An error occured during the batch process: ");
                        outputBuffer.append(e.getMessage());

                        SQLException _e = e.getNextException();
                        while (_e != null) {
                            outputBuffer.append("\nNext Exception: ");
                            outputBuffer.append(_e.getMessage());
                            _e = _e.getNextException();
                        }

                        outputBuffer.append("\n\nRecords processed to the point ");
                        outputBuffer.append("where this error occurred: ");
                        outputBuffer.append(updateCounts.length);
                        appendProgressErrorText(outputBuffer);
                        processResult = FAILED;
                    }

                    //  Log.debug("commitCount: " + commitCount +
                    //                      " batch: " + batchResult[0]);

                    if (tableRowCount != tableInsertCount) {
                        conn.rollback();

                        if (onError == ImportExportDataProcess.STOP_TRANSFER) {
                            getParent().cancelTransfer();
                            processResult = FAILED;
                            throw new InterruptedException();
                        }

                    }

                }

                boolean doCommit = true;
                if (failed && !isBatch &&
                        rollbackSize != ImportExportDataProcess.COMMIT_END_OF_ALL_FILES) {

                    int yesNo = GUIUtilities.displayYesNoDialog(
                                    "The process completed with errors.\n" +
                                    "Do you wish to commit the last block?",
                                    "Confirm commit");

                    doCommit = (yesNo == JOptionPane.YES_OPTION);
                }

                // do the commit if ok from above
                // and if rollback size selected is end of file
                if (rollbackSize == ImportExportDataProcess.COMMIT_END_OF_FILE) {
                    if (doCommit) {
                        conn.commit();
                        totalInsertCount += commitCount;
                        tableCommitCount = tableInsertCount;
                        commitCount = 0;
                    } else {
                        conn.rollback();
                    }
                }

                // update the progress display
                printTableResult(tableRowCount,
                        tableInsertCount, dto.getTableName());
                setProgressStatus(100);

                // reset the checks
                hasDate = false;
                failed = false;

            }

            // commit the last remaining block or where
            // set to commit at the end of all files
            if (rollbackSize != ImportExportDataProcess.COMMIT_END_OF_FILE) {
                setProgressStatus(100);
                boolean doCommit = true;
                if (errorCount > 0 && errorCount != totalRecordCount) {
                    int yesNo = GUIUtilities.displayYesNoDialog(
                                    "The process completed with errors.\n" +
                                    "Do you wish to commit the changes?",
                                    "Confirm commit");
                    doCommit = (yesNo == JOptionPane.YES_OPTION);
                }

                if (doCommit) {
                    conn.commit();
                    totalInsertCount += commitCount;
                } else {
                    conn.rollback();
                }

            }

            processResult = SUCCESS;
        }
        catch (InterruptedException e) {

            if (processResult != FAILED) {
                processResult = CANCELLED;
            }

            try {
                if (prepStmnt != null) {
                    prepStmnt.cancel();
                }
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                outputExceptionError("Error rolling back transaction", e);
            }

        }
        catch (Exception e) {
            logException(e);
            outputBuffer.append("Error processing data from line ");
            outputBuffer.append(lineNumber);
            outputExceptionError("\nUnrecoverable error importing table data from file", e);

            int yesNo = GUIUtilities.displayYesNoDialog(
                            "The process encountered errors.\n" +
                            "Do you wish to commit the last transaction block?",
                            "Confirm commit");
            boolean doCommit = (yesNo == JOptionPane.YES_OPTION);

            try {
                if (doCommit) {
                    conn.commit();
                    totalInsertCount += commitCount;
                } else {
                    conn.rollback();
                }
            }
            catch (SQLException e2) {
                logException(e2);
                outputExceptionError("Error processing last transaction block", e2);
            }
            processResult = FAILED;
        }
        finally {
            finish();
            releaseResources(getParent().getDatabaseConnection());

            if (totalRecordCount == 0 || errorCount > 0) {
                processResult = FAILED;
            }

            setTableCount(tableCount);
            setRecordCount(totalRecordCount);
            setRecordCountProcessed(totalInsertCount);
            setErrorCount(errorCount);

            setProgressStatus(100);
            GUIUtilities.scheduleGC();

            if (reader != null) {           
                try {
                    reader.close();
                } catch (IOException e) {}
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {}
            }
            if (prepStmnt != null) {
                try {
                    prepStmnt.close();
                } catch (SQLException e) {}
            }

        }

        return processResult;
    }

    private String escapeDelim(String delim) {

        String escape = "\\";

        StringBuilder sb = new StringBuilder();
        for (char i : delim.toCharArray()) {

            sb.append(escape).append(i);
        }

        return sb.toString();
    }

    private void logException(Throwable e) {
        if (Log.isDebugEnabled()) {
            Log.debug("Error on delimited import.", e);
        }
    }

    private boolean isIndexIgnored(List<Integer> ignoredIndexes, int index) {
        for (int i = 0, n = ignoredIndexes.size(); i < n; i++) {
            if (index == ignoredIndexes.get(i).intValue()) {
                return true;
            }
        }
        return false;
    }

    private int getIndexOfColumn(Vector<ColumnData> columns, ColumnData cd) {
        for (int i = 0, n = columns.size(); i < n; i++) {
            if (columns.get(i) == cd) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Prints the table specific execution results to the output buffer.
     */
    private void printTableResult(int tableRowCount,
                                  int tableInsertCount,
                                  String tableName) {
        // update the progress display
        outputBuffer.append("Records processed: ");
        outputBuffer.append(tableRowCount);
        outputBuffer.append("\nRecords inserted: ");
        outputBuffer.append(tableInsertCount);
        outputBuffer.append("\nImport complete for table: ");
        outputBuffer.append(tableName);
        appendProgressText(outputBuffer);
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






