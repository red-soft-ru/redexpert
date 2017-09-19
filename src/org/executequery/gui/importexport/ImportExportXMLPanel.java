/*
 * ImportExportXMLPanel.java
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
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1784 $
 * @date     $Date: 2017-09-19 00:55:31 +1000 (Tue, 19 Sep 2017) $
 */
public class ImportExportXMLPanel extends WizardProcessPanel
                                  implements ImportExportDataProcess,
                                             ActiveComponent {
    
    /** The dimension of each child panel */
    private Dimension childDim;

    /** The object to retrieve table details */
    private MetaDataValues metaData;
    
    /** The worker that will run the process */
    private ImportExportWorker worker;
    
    /** The first panel displayed */
    private ImportExportXMLPanel_1 firstPanel;
    
    /** The second panel displayed */
    private ImportExportPanel_2 secondPanel;
    
    /** The third panel displayed */
    private ImportExportPanel_3 thirdPanel;
    
    /** The fourth panel displayed - export */
    private ExportXMLPanel_4 fourthPanel_ex;
    
    /** The fourth panel displayed - import */
    private ImportXMLPanel_4 fourthPanel_im;
    
    /** The fifth panel displayed */
    private ImportExportXMLPanel_5 fifthPanel;
    
    /** The sixth panel displayed */
    private ImportExportXMLPanel_6 sixthPanel;
    
    /** The progress panel */
    private ImportExportProgressPanel progressPanel;
    
    /** The type of transfer - import/export */
    private int transferType;

    /** Whether the process was cancelled */
    private boolean processCancelled;

    /** Whether a transfer is currently underway */
    private boolean processing;
    
    /** the parent container */
    private ActionContainer parent;

    /** the selection model */
    private TransferXMLWizardModel model;

    public ImportExportXMLPanel(ActionContainer parent, int transferType) {
        this(parent, transferType, null, null, null);
    }
    
    public ImportExportXMLPanel(ActionContainer parent, 
                                int transferType,
                                DatabaseConnection databaseConnection,
                                String schemaName,
                                String tableName) {
        this.transferType = transferType;
        
        model = new TransferXMLWizardModel();
        setModel(model);

        this.parent = parent;
        try {
            jbInit();
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

    private void jbInit() throws Exception {
        metaData = new MetaDataValues(true);
        childDim = new Dimension(580, 420);

        // set the help action
        setHelpAction(ActionBuilder.get("help-command"), "import-export");
        
        firstPanel = new ImportExportXMLPanel_1(this);
        model.addPanel(firstPanel);
        prepare();

    }
    
    /**
     * Returns the transfer format - XML, CSV etc.
     */
    public int getTransferFormat() {
        return XML;
    }

    /**
     * Returns whether to trim whitespace on column data values.
     *
     * @return true | false
     */
    public boolean trimWhitespace() {
        return false;
    }

    public boolean isExport() {

        int type = getTransferType();
        
        return type == EXPORT || 
            type == EXPORT_XML || 
            type == EXPORT_DELIMITED;
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
    
    /** <p>Cancels the current in-process transfer */
    public void cancelTransfer() {
        processCancelled = true;
    }
    
    /** <p>Return whether this process is an
     *  import or export process.
     *
     *  @return <code>ImportExportProcess.IMPORT |
     *          ImportExportProcess.EXPORT</code>
     */
    public int getTransferType() {
        return transferType;
    }
    
    /** <p>Retrieves the selected rollback size for
     *  the transfer.
     *
     *  @return the rollback size
     */
    public int getRollbackSize() {
        return fifthPanel.getRollbackSize();
    }
    
    /** <p>Retrieves the action on an error occuring
     *  during the import/export process.
     *
     *  @return the action on error -<br>either:
     *          <code>ImportExportProcess.LOG_AND_CONTINUE</code> or
     *          <code>ImportExportProcess.STOP_TRANSFER</code>
     */
    public int getOnError() {
        return fifthPanel.getOnError();
    }
    
    /** <p>Retrieves the date format for date fields
     *  contained within the data file/database table.
     *
     *  @return the date format (ie. ddMMyyy)
     */
    public String getDateFormat() {
        return fifthPanel.getDateFormat();
    }
    
    /**
     * Returns whether to parse date values.
     *
     * @return true | false
     */
    public boolean parseDateValues() {
        return fifthPanel.parseDateValues();
    }

    /** <p>Whether the table name is an attribute
     *  within the XML document.
     *
     *  @return whether the table name is an attribute
     */
    public boolean hasTableNameAsAttribute() {
        return fourthPanel_im.hasTableNameAsAttribute();
    }
    
    /**
     * Returns whether to include column names.
     *
     * @return true | false
     */
    public boolean includeColumnNames() {
        return true;
    }

    @Override
    public boolean quoteCharacterValues() {
        return false;
    }
    
    public JDialog getDialog() {
        if (parent.isDialog()) {
            return (JDialog)parent;
        }
        return null;
    }

    /** 
     * Retrieves the selected type of delimiter within
     * the file to be used with this process.
     *
     * @return the selected delimiter
     */
    public String getDelimiter() {
        return "";
    }

    public void setProcessComplete(boolean success) {
        setButtonsEnabled(true);
        setNextButtonEnabled(false);
        setBackButtonEnabled(true);
        setCancelButtonEnabled(true);

        if (success) {
            setCancelButtonText("Finish");
        }
        processing  = false;
    }
    
    public void doImport() {
        Log.info("Beginning data import process");
        processCancelled = false;
        setNextButtonEnabled(false);
        setCancelButtonEnabled(false);
        setBackButtonEnabled(false);
        worker = new ImportXMLWorker(this, progressPanel);
    }
    
    /** <p>Begins an export process. */
    public void doExport() {
        Log.info("Beginning data export process");
        processCancelled = false;
        setNextButtonEnabled(false);
        setCancelButtonEnabled(false);
        setBackButtonEnabled(false);
        worker = new ExportXMLWorker(this, progressPanel);
    }
    
    /** <p>Retrieves the selected tables for this process.
     *
     *  @return the selected table names
     */
    public String[] getSelectedTables() {
        return secondPanel.getSelectedTables();
    }
    
    /** <p>Retrieves the table name for this process in
     *  the case of a single table import/export.
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
    
    /** 
     * Returns a <code>Vector</code> of <code>DataTransferObject</code> 
     * objects containing all relevant data for the process.
     *
     * @return a <code>Vector</code> of <code>DataTransferObject</code> objects
     */
    public Vector getDataFileVector() {
        return thirdPanel.getDataFileVector();
    }
    
    public String getTableIdentifier() {
        return fourthPanel_im.getTableTagString();
    }
    
    public String getRowIdentifier() {
        return fourthPanel_im.getRowTagString();
    }
    
    // returns single or multiple table export
    public int getTableTransferType() {
        return firstPanel.getTableTransferType();
    }
    
    public String getPrimaryImportNodes() {
        return fourthPanel_im.getTagInfoString();
    }

    public String getXMLFormatString() {
        if (fourthPanel_ex.getSelection() == ImportExportDataProcess.SCHEMA_ELEMENT) {
            return "Schema and table elements";
        }
        else {
            return "Table element only";
        }
    }
    
    public int getXMLFormat() {
        return fourthPanel_ex.getSelection();
    }
    
    /** <p>Returns the type of multiple table
     *  transfer - single or multiple file.
     *
     *  @return the type of multiple table transfer
     */
    public int getMutlipleTableTransferType() {
        return firstPanel.getMutlipleTableTransferType();
    }
    
    public boolean isSingleFileExport() {
        return getMutlipleTableTransferType() == SINGLE_FILE;
    }

    /** <p>Indicates whether the process (import only)
     *  should be run as a batch process.
     *
     *  @return whether to run as a batch process
     */
    public boolean runAsBatchProcess() {
        return fifthPanel.runAsBatchProcess();
    }
    
    /**
     * Returns the selected database connection properties object.
     *
     * @return the connection properties object
     */
    public DatabaseConnection getDatabaseConnection() {
        return firstPanel.getDatabaseConnection();
    }

    private boolean doNext() {
        JPanel nextPanel = null;
        int currentPanel = model.getSelectedIndex();

        switch (currentPanel) {
            
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
                } else {
                    thirdPanel.buildTable();
                }
                nextPanel = thirdPanel;
                break;
                
            case 2:
                
                if (!thirdPanel.transferObjectsComplete()) {
                    return false;
                }

                if (transferType == EXPORT) {
                    
                    if (fourthPanel_ex == null) {
                        fourthPanel_ex = new ExportXMLPanel_4(this);
                    }
                    nextPanel = fourthPanel_ex;
                    fourthPanel_ex.setSelectedRadios();
                    
                } else if (transferType == IMPORT) {
                    
                    if (fourthPanel_im == null) {
                        fourthPanel_im = new ImportXMLPanel_4(this);
                    }
                    nextPanel = fourthPanel_im;
                }
                
                break;
                
            case 3:
                
                if (transferType == IMPORT &&
                    !fourthPanel_im.entriesComplete()) {
                    GUIUtilities.displayErrorMessage(
                            "Please ensure all required fields been entered correctly.");
                    return false;
                }
                
                if (fifthPanel == null) {
                    fifthPanel = new ImportExportXMLPanel_5(this);
                }
                nextPanel = fifthPanel;
                break;
                
            case 4:
                
                if (sixthPanel == null) {
                    sixthPanel = new ImportExportXMLPanel_6(this);
                }
                nextPanel = sixthPanel;
                sixthPanel.setValues();
                break;
                
            case 5:
                
                if (progressPanel == null) {
                    progressPanel = new ImportExportProgressPanel(this);
                }
                model.addPanel(progressPanel);
                processing = true;

                if (transferType == EXPORT) {
                    doExport();
                }
                else if (transferType == IMPORT) {
                    doImport();
                }
                setButtonsEnabled(false);
                return true;

        }

        model.addPanel(nextPanel);
        return true;
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
     * Stops the current process. 
     */
    public void stopTransfer() {
        setButtonsEnabled(true);
        if (processing) {
            worker.cancelTransfer();
            setBackButtonEnabled(true);
            processCancelled = true;
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
            processCancelled = true;
        }
        else {
            worker = null;
            parent.finished();
        }
        
    }
    
    public Dimension getChildDimension() {
        return childDim;
    }
    
    public MetaDataValues getMetaDataUtility() {
        return metaData;
    }
    
    
    private class TransferXMLWizardModel extends DefaultWizardProcessModel {
        
        public TransferXMLWizardModel() {
            int type = getTransferType();
            String firstTitle = "Database Connection and Export Type";
            String lastTitle = "Exporting Data...";
            if (type == ImportExportDataProcess.IMPORT) {
                firstTitle = "Database Connection and Import Type";
                lastTitle = "Importing Data...";
            }

            String[] titles = {firstTitle,
                               "Table Selection",
                               "Data File Selection",
                               "XML Element Style",
                               "Options",
                               "Summary",
                               lastTitle};

            setTitles(titles);

            String[] steps = {"Select database connection and transfer type",
                              "Select the tables/columns",
                              type == ImportExportDataProcess.IMPORT ?
                                  "Select the XML file(s) to import from" :
                                  "Select the XML file(s) to export to",
                              "Select the element style",
                              "Set any further transfer options",
                              "Summary",
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















