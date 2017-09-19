/*
 * ImportExportDelimitedPanel.java
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
import javax.swing.JPanel;

import org.executequery.ActiveComponent;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.wizard.DefaultWizardProcessModel;
import org.underworldlabs.swing.wizard.WizardProcessPanel;

/** 
 * Import export to delimited file parent object.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1784 $
 * @date     $Date: 2017-09-19 00:55:31 +1000 (Tue, 19 Sep 2017) $
 */
public class ImportExportDelimitedPanel extends WizardProcessPanel
                                        implements ImportExportDataProcess,
                                                   ActiveComponent {
    
    /** The type of transfer - import/export */
    private int transferType;
    
    /** The dimension of each child panel */
    private Dimension childDim;
    
    /** The object to retrieve table details */
    private MetaDataValues metaData;
    
    /** The worker that will run the process */
    private ImportExportWorker worker;
    
    /** The first panel displayed */
    private ImportExportDelimitedPanel_1 firstPanel;
    
    /** The second panel displayed */
    private ImportExportPanel_2 secondPanel;
    
    /** The third panel displayed */
    private ImportExportPanel_3 thirdPanel;
    
    /** The fourth panel displayed */
    private ImportExportDelimitedPanel_4 fourthPanel;
    
    /** The progress panel to track the process */
    private ImportExportProgressPanel progressPanel;
    
    /** Whether the process was a success */
    private boolean processing;

    /** the parent container */
    private ActionContainer parent;

    /** the selection model */
    private TransferDelimitedWizardModel model;
    
    /** 
     * Creates a new instance of the process with the specified parent
     * container and process type.
     *
     * @param the process type - import/export
     */
    public ImportExportDelimitedPanel(ActionContainer parent, int transferType) {
        this(parent, transferType, null, null, null);
    }

    /** 
     * Creates a new instance of the process with the specified parent
     * container and process type.
     *
     * @param the process type - import/export
     */
    public ImportExportDelimitedPanel(ActionContainer parent, 
                                      int transferType,
                                      DatabaseConnection databaseConnection,
                                      String schemaName,
                                      String tableName) {
        this.transferType = transferType;
        model = new TransferDelimitedWizardModel();
        setModel(model);

        this.parent = parent;

        try {
            init();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        if (databaseConnection != null) {
            firstPanel.setDatabaseConnection(databaseConnection);
            next();
            
            if (schemaName != null) {
                secondPanel.setSelectedSchema(schemaName);
                if (tableName != null) {
                    secondPanel.setSelectedTable(tableName);
                    secondPanel.selectAllAvailable();
                }

            }

        }
        
    }

    /** <p>Initialises the state of this instance and
     *  lays out components on the panel. */
    private void init() throws Exception {
        
        metaData = new MetaDataValues(true);
        childDim = new Dimension(580, 420);
    
        // set the help action
        setHelpAction(ActionBuilder.get("help-command"), "import-export");
        
        firstPanel = new ImportExportDelimitedPanel_1(this);        
        model.addPanel(firstPanel);
        prepare();
    }
    
    /**
     * Returns the transfer format - XML, CSV etc.
     */
    public int getTransferFormat() {
        return DELIMITED;
    }

    /**
     * Releases database resources before closing.
     */
    public void cleanup() {
        metaData.closeConnection();
    }

    public String getSchemaName() {
        return secondPanel.getSelectedSchema();
    }
    
    /** <p>Retrieves the selected tables for this process.
     *
     *  @return the selected table names
     */
    public String[] getSelectedTables() {
        return secondPanel.getSelectedTables();
    }
    
    /** <p>Retrieves the table name for this process in the
     *  case of a single table import/export.
     *
     *  @return the table name
     */
    public String getTableName() {
        return secondPanel.getSelectedTables()[0];
    }
    
    /** <p>Retrieves the column names for this process.
     *
     *  @return the column names
     */
    public Vector<ColumnData> getSelectedColumns() {
        return secondPanel.getSelectedColumns();
    }
    
    /** <p>Retrieves the selected rollback size for
     *  the transfer.
     *
     *  @return the rollback size
     */
    public int getRollbackSize() {
        return fourthPanel.getRollbackSize();
    }
    
    public boolean quoteCharacterValues() {
        return fourthPanel.quoteCharacterValues();
    }

    public boolean includeColumnNames() {
        return fourthPanel.includeColumnNames();
    }
    
    /** <p>Retrieves the action on an error occuring
     *  during the import/export process.
     *
     *  @return the action on error -<br>either:
     *          <code>ImportExportProcess.LOG_AND_CONTINUE</code> or
     *          <code>ImportExportProcess.STOP_TRANSFER</code>
     */
    public int getOnError() {
        return fourthPanel.getOnError();
    }
    
    /** 
     * Retrieves the selected type of delimiter within
     *  the file to be used with this process.
     *
     *  @return the selected delimiter
     */
    public String getDelimiter() {
        return fourthPanel.getDelimiter();
    }
    
    /** <p>Retrieves the date format for date fields
     *  contained within the data file/database table.
     *
     *  @return the date format (ie. ddMMyyy)
     */
    public String getDateFormat() {
        String format = fourthPanel.getDateFormat();
        if (format == null || format.length() == 0) {
            return null;
        } else {
            return format;
        }
    }
    
    /**
     * Returns whether to parse date values.
     *
     * @return true | false
     */
    public boolean parseDateValues() {
        return fourthPanel.parseDateValues();
    }

    /** 
     * Indicates whether the process (import only) should 
     * be run as a batch process.
     *
     * @return whether to run as a batch process
     */
    public boolean runAsBatchProcess() {
        return fourthPanel.runAsBatchProcess();
    }
    
    public boolean trimWhitespace() {
        return fourthPanel.trimWhitespace();
    }
    
    /** <p>Begins an import process. */
    public void doImport() {
        Log.info("Beginning data import process");
        setNextButtonEnabled(false);
        setBackButtonEnabled(false);
        setCancelButtonEnabled(false);
        worker = new ImportDelimitedWorker(this, progressPanel);
    }
    
    public void setProcessComplete(boolean success) {
        setButtonsFinished(success);
    }
    
    public void setButtonsFinished(boolean success) {
        setButtonsEnabled(true);
        setNextButtonEnabled(false);
        setBackButtonEnabled(true);
        setCancelButtonEnabled(true);

        if (success) {
            setCancelButtonText("Finish");
        }
        processing  = false;        
    }
    
    /** <p>Begins an export process. */
    public void doExport() {
        Log.info("Beginning data export process");
        setNextButtonEnabled(false);
        setBackButtonEnabled(false);
        setCancelButtonEnabled(false);
        worker = new ExportDelimitedWorker(this, progressPanel);
    }
    
    /** <p>Cancels the current in-process transfer */
    public void cancelTransfer() {
        Log.info("Process cancelled");
    }
    
    public boolean isExport() {

        int type = getTransferType();
        
        return type == EXPORT || 
            type == EXPORT_XML || 
            type == EXPORT_DELIMITED;
    }
    
    /** <p>Returns the type of transfer - import or export.
     *
     *  @return the transfer type - import/export
     */
    public int getTransferType() {
        return transferType;
    }
    
    /** <p>Returns a <code>Vector</code> of <code>
     *  DataTransferObject</code> objects containing
     *  all relevant data for the process.
     *
     *  @return a <code>Vector</code> of
     *          <code>DataTransferObject</code> objects
     */
    public Vector<DataTransferObject> getDataFileVector() {
        return thirdPanel.getDataFileVector();
    }
    
    /**
     * Returns the XML format style for an XML import/export.
     * Value of -1 is returned in this case.
     *
     * @return the XML format
     */
    public int getXMLFormat() {
        return -1;
    }

    /**
     * Defines the action for the BACK button.
     */
    private boolean doPrevious() {
        // make sure the cancel button says cancel
        setCancelButtonText(Bundles.get("common.cancel.button"));
        return true;
    }
    
    /** 
     * Defines the action for the NEXT button.
     */
    private boolean doNext() {
        JPanel nextPanel = null;
        int index = model.getSelectedIndex();
        
        switch (index) {
        
            case 0:
                DatabaseConnection dc = getDatabaseConnection();
                if (dc != null) {
                    metaData.setDatabaseConnection(dc);
                }

                if (secondPanel == null) {
                    secondPanel = new ImportExportPanel_2(this);
                }
                nextPanel = secondPanel;

                secondPanel.setListData(getTableTransferType());
                break;

            case 1:

                if (!secondPanel.hasSelections()) {
                    if (getTableTransferType() == ImportExportDataProcess.MULTIPLE_TABLE) {
                        GUIUtilities.displayErrorMessage(
                        "You must select at least one table");
                    }
                    else if (getTableTransferType() == ImportExportDataProcess.SINGLE_TABLE) {
                        GUIUtilities.displayErrorMessage(
                        "You must select at least one column");
                    }                
                    return false;
                }

                if (thirdPanel == null) {
                    thirdPanel = new ImportExportPanel_3(this);
                }
                else {
                    thirdPanel.buildTable();
                }
                nextPanel = thirdPanel;
                break;
                
            case 2:

                if (!thirdPanel.transferObjectsComplete()) {
                    return false;
                }

                if (fourthPanel == null) {
                    fourthPanel = new ImportExportDelimitedPanel_4(this);
                }
                nextPanel = fourthPanel;
                break;

            case 3:
            
                if (progressPanel == null) {
                    progressPanel = new ImportExportProgressPanel(this);
                }
                processing = true;
                model.addPanel(progressPanel);

                if (transferType == ImportExportDataProcess.EXPORT) {
                    doExport();
                }
                else if (transferType == ImportExportDataProcess.IMPORT) {
                    doImport();
                }
                setButtonsEnabled(false);
                return true;

        }

        model.addPanel(nextPanel);
        return true;
    }
    
    /** 
     * Stops the current process. 
     */
    public void stopTransfer() {
        setButtonsEnabled(true);
        if (processing) {
            worker.cancelTransfer();
            setBackButtonEnabled(true);
        }
    }

    /** 
     * Defines the action for the CANCEL button.
     */
    public void cancel() {
        setButtonsEnabled(true);
        if (processing) {
            worker.cancelTransfer();
            setBackButtonEnabled(true);
        }
        else {
            worker = null;
            parent.finished();
        }
    }
    
    public JDialog getDialog() {
        if (parent.isDialog()) {
            return (JDialog)parent;
        }
        return null;
    }
    
    /** 
     * Returns the type of transfer - single or multiple table.
     *
     * @return the type of transfer
     */
    public int getTableTransferType() {
        return firstPanel.getSelection();
    }

    /**
     * Returns the selected database connection properties object.
     *
     * @return the connection properties object
     */
    public DatabaseConnection getDatabaseConnection() {
        return firstPanel.getDatabaseConnection();
    }

    /** <p>Retrieves the size of the child panel
     *  to be added to the main base panel.
     *
     *  @return the size of the child panel
     */
    public Dimension getChildDimension() {
        return childDim;
    }
    
    /** <p>Retrieves the <code>MetaDataValues</code>
     *  object defined for this process.
     *
     *  @return the <code>MetaDataValues</code> helper class
     */
    public MetaDataValues getMetaDataUtility() {
        return metaData;
    }
    
    /** <p>Returns the type of multiple table
     *  transfer - single or multiple file.
     *
     *  @return the type of multiple table transfer
     */
    public int getMutlipleTableTransferType() {
        return MULTIPLE_FILE;
    }

    public boolean isSingleFileExport() {
        return false;
    }
    
    
    private class TransferDelimitedWizardModel extends DefaultWizardProcessModel {
        
        public TransferDelimitedWizardModel() {
            int type = getTransferType();
            String firstTitle = "Database Connection and Export Type";
            String fifthTitle = "Exporting Data...";
            if (type == ImportExportDataProcess.IMPORT) {
                firstTitle = "Database Connection and Import Type";
                fifthTitle = "Importing Data...";
            }

            String[] titles = {firstTitle,
                               "Table Selection",
                               "Data File Selection",
                               "Options",
                               fifthTitle};
            setTitles(titles);

            String[] steps = {"Select database connection and transfer type",
                              "Select the tables/columns",
                              type == ImportExportDataProcess.IMPORT ?
                                  "Select the data file(s) to import from" :
                                  "Select the data file(s) to export to",
                              "Set any further transfer options",
                              type == ImportExportDataProcess.IMPORT ?
                                  "Import the data" :
                                  "Export the data"};
            setSteps(steps);
        }

        public boolean previous() {
            if (doPrevious()) {
                return super.previous();
            }
            return false;
        }
        
        public boolean next() {
            if (doNext()) {
                return super.next();
            }
            return false;
        }

    }
    
}















