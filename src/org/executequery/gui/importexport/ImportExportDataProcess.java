/*
 * ImportExportDataProcess.java
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

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JDialog;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.gui.browser.ColumnData;

/** 
 * Interface defining an import or export
 * process. This interface will be implemented
 * by those classes handling data transfer tasks
 * including both the delimited file import/export
 * and the XML file import/export.
 *
 * <p>Retrieval of common information within each
 * process is defined in addition to some minor
 * view information such as the size of child
 * components within the 'wizard' type functionality
 * that is each process.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1784 $
 * @date     $Date: 2017-09-19 00:55:31 +1000 (Tue, 19 Sep 2017) $
 */
public interface ImportExportDataProcess {
    
    // ------------------------------------------------
    // ---- Import/Export process types constants -----
    // ------------------------------------------------
    
    int XML = 12;
    int DELIMITED = 13;
    int EXCEL = 14;
    
    /** The import from XML process */
    int IMPORT_XML = 0;
    /** The export to XML process */
    int EXPORT_XML = 1;
    /** The import from delimited file process */
    int IMPORT_DELIMITED = 2;
    /** The export to delimited file process */
    int EXPORT_DELIMITED = 3;
    /** The export process */
    int IMPORT = 4;
    /** The import process */
    int EXPORT = 5;
    /** Denotes a single table export process */
    int SINGLE_TABLE = 6;
    /** Denotes a multiple table export process */
    int MULTIPLE_TABLE = 7;
    /** On error log and continue */
    int LOG_AND_CONTINUE = 8;
    /** On error stop transfer */
    int STOP_TRANSFER = 9;
    /** A single file export - multiple table */
    int SINGLE_FILE = 10;
    /** A multiple file export - multiple table */
    int MULTIPLE_FILE = 11;
    
    
    /** indicator for commit and end of file */
    int COMMIT_END_OF_FILE = -99;

    /** indicator for commit and end of all files */
    int COMMIT_END_OF_ALL_FILES = -98;

    /** XML format with schema element */
    int SCHEMA_ELEMENT = 0;

    /** XML format with table element only */
    int TABLE_ELEMENT = 1;

    /**
     * Returns the transfer format - XML, CSV etc.
     */
    int getTransferFormat();
    
    /** 
     * Stops the current process. 
     */
    void stopTransfer();
    
    /** 
     * Flags the current transfer process as cancelled. 
     */
    void cancelTransfer();
    
    /** 
     * Retrieves the selected rollback size for the transfer.
     *
     * @return the rollback size
     */
    int getRollbackSize();
    
    /** 
     * Retrieves the action on an error occuring during the import/export process.
     *
     * @return the action on error -<br>either:
     *          <code>ImportExportProcess.LOG_AND_CONTINUE</code> or
     *          <code>ImportExportProcess.STOP_TRANSFER</code>
     */
    int getOnError();
    
    /** 
     * Retrieves the date format for date fields contained within 
     * the data file/database table.
     *
     * @return the date format (ie. ddMMyyy)
     */
    String getDateFormat();
    
    /**
     * Returns whether to parse date values.
     *
     * @return true | false
     */
    boolean parseDateValues();

    /** 
     * Retrieves the column names for this process.
     *
     * @return the column names
     */
    Vector<ColumnData> getSelectedColumns();
    
    /** 
     * Retrieves the size of the child panel to be added to the main base panel.
     *
     *  @return the size of the child panel
     */
    Dimension getChildDimension();
    
    /** 
     * Retrieves the <code>MetaDataValues</code> object defined for this process.
     *
     * @return the <code>MetaDataValues</code> helper class
     */
    MetaDataValues getMetaDataUtility();
    
    /** 
     * Returns the type of transfer - single or multiple table.
     *
     * @return the type of transfer
     */
    int getTableTransferType();
    
    /** 
     * Begins an import process. 
     */
    void doImport();
    
    /** 
     * Begins an export process. 
     */
    void doExport();
    
    /** 
     * Returns the type of transfer - import or export.
     *
     * @return the transfer type - import/export
     */
    int getTransferType();
    
    
    /**
     * Returns whether this process defines an export.
     *  
     * @return
     */
    boolean isExport();
    
    /** 
     * Retrieves the selected tables for this process.
     *
     * @return the selected table names
     */
    String[] getSelectedTables();
    
    /** 
     * Retrieves the table name for this process in the case 
     * of a single table import/export.
     *
     * @return the table name
     */
    String getTableName();
    
    /** 
     * Returns the type of multiple table transfer - single or multiple file.
     *
     * @return the type of multiple table transfer
     */
    int getMutlipleTableTransferType();
    
    /** 
     * Returns whether the table transfer type is single or multiple file.
     *
     * @return true | false
     */
    boolean isSingleFileExport();
    
    /**
     * Returns the schema name where applicable.
     *
     * @return the schema name
     */
    String getSchemaName();
    
    /**
     * Returns the selected database connection properties object.
     *
     * @return the connection properties object
     */
    DatabaseConnection getDatabaseConnection();

    /**
     * Returns the XML format style for an XML import/export.
     *
     * @return the XML format
     */
    int getXMLFormat();

    /** 
     * Indicates whether the process (import only) should 
     * be run as a batch process.
     *
     * @return whether to run as a batch process
     */
    boolean runAsBatchProcess();

    /** 
     * Returns a <code>Vector</code> of <code>DataTransferObject</code> 
     * objects containing all relevant data for the process.
     *
     * @return a <code>Vector</code> of <code>DataTransferObject</code> objects
     */
    Vector<DataTransferObject> getDataFileVector();

    /**
     * Returns whether to include column names as the
     * first row of a delimited export process.
     *
     * @return true | false
     */
    boolean includeColumnNames();

    boolean quoteCharacterValues();
    
    /** 
     * Retrieves the selected type of delimiter within
     * the file to be used with this process.
     *
     * @return the selected delimiter
     */
    String getDelimiter();

    /**
     * Indicates the process has completed successfully or
     * otherwise as indicated.
     *
     * @param success - true | false
     */
    void setProcessComplete(boolean success);

    /**
     * Returns whether to trim whitespace on column data values.
     *
     * @return true | false
     */
    boolean trimWhitespace();
    
    /**
     * Returns the dialog container for this process.
     *
     * @return the dialog or null if there is no dialog
     */
    JDialog getDialog();
    
}









