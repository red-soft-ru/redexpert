/*
 * ExportAsSQLWizard.java
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

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.executequery.ActiveComponent;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.gui.ActionContainer;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.wizard.DefaultWizardProcessModel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ExportAsSQLWizard extends ImportExportWizardProcessPanel
                               implements ActiveComponent, ImportExportWizard {

    public static final String TITLE = "Export as SQL";

    private static final Dimension panelSize = new Dimension(580, 420);

    private ExportAsSQLDataModel exportDataModel;
    
    private TableSelectionCombosGroup tableSelectionCombosGroup;

    private ExportAsSQLPanelOne firstPanel;

    private ImportExportPanelTwo secondPanel;
    
    private ImportExportPanelThree thirdPanel;

    private ExportAsSQLPanelFour fourthPanel;

    private ExportAsSQLWorker exportAsSQLWorker;
    
    private NewImportExportProgressPanel fifthPanel;
    
    private final ActionContainer parent;

    public ExportAsSQLWizard(ActionContainer parent) {
    
        this(parent, null);
    }
    
    public ExportAsSQLWizard(ActionContainer parent, 
                             DatabaseTable databaseTable) {

        this.parent = parent;
        init();
        
        if (databaseTable != null) {
            
            setInitialSelectionForTable(databaseTable);
        }
        
    }

    private void init() {

        setModel(new ExportAsSQLWizardModel());

        tableSelectionCombosGroup = new TableSelectionCombosGroup();
        exportDataModel = createExportDataModel(tableSelectionCombosGroup.getSelectedHost());
     
        firstPanel = new ExportAsSQLPanelOne(this);
        initAndAddPanel(firstPanel);

        prepare();
    }

    private void setInitialSelectionForTable(DatabaseTable databaseTable) {

        next();
        tableSelectionCombosGroup.setSelectedDatabaseTable(databaseTable);
        firstPanelToModel();
        secondPanel.selectAll();
    }

    private void initAndAddPanel(JPanel panel) {
        panel.setPreferredSize(panelSize);
        getModel().addPanel(panel);
    }
    
    private ExportAsSQLDataModel createExportDataModel(DatabaseHost databaseHost) {
        return new ExportAsSQLDataModel(databaseHost);
    }
    
    public String getFileSuffix() {

        return "sql";
    }
    
    public void stopTransfer() {
        setButtonsEnabled(true);
        exportAsSQLWorker.cancelTransfer();
        setBackButtonEnabled(true);
    }

    public ExportAsSQLDataModel getExportDataModel() {
        return exportDataModel;
    }

    public final JComboBox getSchemasCombo() {
        return tableSelectionCombosGroup.getSchemasCombo();
    }

    public final JComboBox getTablesCombo() {
        return tableSelectionCombosGroup.getTablesCombo();
    }

    public final JComboBox getConnectionsCombo() {
        return tableSelectionCombosGroup.getConnectionsCombo();
    }

    public void cancel() {
        setButtonsEnabled(true);
        parent.finished();
    }

    public void cleanup() {
        tableSelectionCombosGroup.close();
        exportDataModel.getDatabaseHost().close();
    }
    
    public void enableButtons(boolean enable) {
        setButtonsEnabled(enable);
        setNextButtonEnabled(enable);
        setBackButtonEnabled(enable);
        setCancelButtonEnabled(enable);
    }
    
    private boolean doNext() {
        
        int index = getModel().getSelectedIndex();

        switch (index) {

            case 0:
                
                firstPanelToModel();
    
                if (secondPanel == null) {
                    secondPanel = new ImportExportPanelTwo(this);
                    initAndAddPanel(secondPanel);
                }
    
                secondPanel.panelSelected();
                break;

            case 1:
                
                if (!validateSecondPanelSelections()) {
                    return false;
                }
                
                secondPanelToModel();
    
                if (thirdPanel == null) {
                    thirdPanel = new ImportExportPanelThree(this);
                    initAndAddPanel(thirdPanel);
                }
    
                thirdPanel.panelSelected();
                break;

            case 2:

                thirdPanelToModel();
                
                if (!validateThirdPanelSelections()) {
                    return false;
                }

                if (fourthPanel == null) {
                    fourthPanel = new ExportAsSQLPanelFour(this);
                    initAndAddPanel(fourthPanel);
                }
    
                fourthPanel.panelSelected();
                break;

            case 3:
                
                fourthPanelToModel();

                if (fifthPanel == null) {
                    fifthPanel = new NewImportExportProgressPanel(this);
                    initAndAddPanel(fifthPanel);
                }

                startExport();
                break;

        }
        
        return true;
    }

    public void processComplete(ImportExportResult importExportResult) {

        setButtonsEnabled(true);
        setNextButtonEnabled(false);
        setBackButtonEnabled(true);
        setCancelButtonEnabled(true);

        if (ImportExportResult.isSuccess(importExportResult)) {

            setCancelButtonText("Finish");
        }

    }
    
    private void startExport() {

        if (exportAsSQLWorker == null) {
            
            exportAsSQLWorker = new ExportAsSQLWorker(this);
        }

        Log.info("Beginning data export process");

        setNextButtonEnabled(false);
        setCancelButtonEnabled(false);
        setBackButtonEnabled(false);

        exportAsSQLWorker.export();
    }

    private void fourthPanelToModel() {

        exportDataModel.setOnErrorOption(fourthPanel.getOnErrorOption());
        exportDataModel.setIncludeCreateTableStatements(fourthPanel.getIncludeCreateTableStatement());
        exportDataModel.setIncludePrimaryKeyConstraints(fourthPanel.getIncludePrimaryKeyConstraints());
        exportDataModel.setIncludeForeignKeyConstraints(fourthPanel.getIncludeForeignKeyConstraints());
    }

    private void thirdPanelToModel() {

        if (exportDataModel.isSingleFileMultiTableExport()) {

            exportDataModel.setSingleFileExport(thirdPanel.getSingleFileExportName());
        }

    }

    private boolean validateThirdPanelSelections() {

        if (!thirdPanel.hasSelections()) {

            displayErrorDialog(bundledString("ImportExportPanelThree.invalidDataFileSelection"));
            return false;
        }
        
        StringBuilder sb = new StringBuilder();
        List<ImportExportFile> exportFiles = exportDataModel.getImportExportFiles();
        for (ImportExportFile exportFile : exportFiles) {
            
            if (exportFile.fileExists()) {
                
                sb.append("\t");
                sb.append(exportFile.getFile().getName());
                sb.append("\n");
            }

        }

        if (sb.length() > 0) {
            
            int result = displayConfirmCancelDialog(bundledString("ImportExportPanelThree.filesExist", sb));
            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION) {

                return false;
            }
            
        }
        
        return true;
    }

    private boolean validateSecondPanelSelections() {

        if (!secondPanel.hasSelections()) {
            
            String errorMessage = null;
            if (exportDataModel.isMultipleTableImportExport()) {

                errorMessage = "You must select at least one table";
                
            } else {

                errorMessage = "You must select at least one column";
            }
            displayErrorDialog(errorMessage);
            
            return false;
        }

        return true;
    }

    private Component parentForDialog() {
        
        if (parent.isDialog()) {
            
            return (Component) parent;
        }
        
        return null;
    }
    
    private void displayErrorDialog(String errorMessage) {

        GUIUtils.displayErrorMessage(parentForDialog(), errorMessage);        
    }

    private int displayConfirmCancelDialog(String message) {

        return GUIUtils.displayConfirmCancelDialog(parentForDialog(), message);        
    }

    @SuppressWarnings("unchecked")
    private void secondPanelToModel() {

        exportDataModel.setDatabaseSource(tableSelectionCombosGroup.getSelectedSource());

        List<DatabaseTable> tables = null;
        if (exportDataModel.isMultipleTableImportExport()) {
        
            tables = (Vector<DatabaseTable>) secondPanel.getSelectedItems();

        } else {
            
            tables = new ArrayList<DatabaseTable>(1);
            tables.add(tableSelectionCombosGroup.getSelectedTable());
            
            exportDataModel.setDatabaseTableColumns(
                    (List<DatabaseColumn>) secondPanel.getSelectedItems());
        }

        exportDataModel.setDatabaseTables(tables);
    }

    private void firstPanelToModel() {

        exportDataModel.setDatabaseHost(
                tableSelectionCombosGroup.getSelectedHost());
        exportDataModel.setImportExportType(firstPanel.getExportType());
        exportDataModel.setImportExportFileType(firstPanel.getExportFileType());

        tableSelectionCombosGroup.setSchemaSelectionUpdatesEnabled(
                !(exportDataModel.isMultipleTableImportExport()));

    }
    
    private boolean doPrevious() {
        
        
        setCancelButtonText(Bundles.get("common.cancel.button"));
        setCancelButtonEnabled(true);

        return true;
    }
    
    private class ExportAsSQLWizardModel extends DefaultWizardProcessModel {
        
        public ExportAsSQLWizardModel() {

            String[] titles = {"Database Connection and Export Type",
                               "Table Selection",
                               "Data File Selection",
                               "Options",
                               "Exporting Data..."};
            setTitles(titles);

            String[] steps = {"Select database connection and transfer type",
                              "Select the tables/columns",
                              "Select the data file(s) to export to",
                              "Set any SQL statement output options",
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

    public ImportExportMonitor getImportExportMonitor() {

        return fifthPanel;
    }

}







