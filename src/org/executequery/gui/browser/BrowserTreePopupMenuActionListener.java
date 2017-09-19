/*
 * BrowserTreePopupMenuActionListener.java
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

package org.executequery.gui.browser;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.importexport.ImportExportDelimitedPanel;
import org.executequery.gui.importexport.ImportExportExcelPanel;
import org.executequery.gui.importexport.ImportExportDataProcess;
import org.executequery.gui.importexport.ImportExportXMLPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.actions.ReflectiveAction;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1783 $
 * @date     $Date: 2017-09-19 00:04:44 +1000 (Tue, 19 Sep 2017) $
 */
public class BrowserTreePopupMenuActionListener extends ReflectiveAction {

    private ConnectionsTreePanel treePanel;

    private StatementToEditorWriter statementWriter;

    private DatabaseConnection currentSelection;

    private TreePath currentPath;

    BrowserTreePopupMenuActionListener(ConnectionsTreePanel treePanel) {
        this.treePanel = treePanel;
    }

    protected void postActionPerformed(ActionEvent e) {
        currentSelection = null;
        currentPath = null;
    }

    public void addNewConnection(ActionEvent e) {
        treePanel.newConnection();
    }

    public void switchDefaultCatalogAndSchemaDisplay(ActionEvent e) {

        JCheckBoxMenuItem check = (JCheckBoxMenuItem)e.getSource();

        DatabaseHostNode node =
            (DatabaseHostNode)currentPath.getLastPathComponent();
        node.setDefaultCatalogsAndSchemasOnly(check.isSelected());

        treePanel.nodeStructureChanged(node);
    }

    public void delete(ActionEvent e) {
        if (currentPath != null) {
            DatabaseHostNode node =
                    (DatabaseHostNode)currentPath.getLastPathComponent();
            treePanel.deleteConnection(node);
        }
    }

    public void recycle(ActionEvent e) {
        DatabaseHost host = treePanel.getSelectedMetaObject();
        try {
            host.recycleConnection();
        }
        catch (DataSourceException dse) {
            handleException(dse);
        }
    }

    public void reload(ActionEvent e) {
        if (currentPath != null) {
            treePanel.reloadPath(currentPath);
        }
    }

    public void copyName(ActionEvent e) {
        if (currentPath != null) {
            String name = currentPath.getLastPathComponent().toString();
            GUIUtilities.copyToClipBoard(name);
        }
    }
    
    public void disconnect(ActionEvent e) {
        treePanel.disconnect(currentSelection);
    }

    public void duplicate(ActionEvent e) {

        if (currentSelection != null) {

            String name = treePanel.buildConnectionName(
                            currentSelection.getName() + " ("+ Bundles.getCommon("Copy")) + ")";
            DatabaseConnection dc = currentSelection.copy().withName(name);
            treePanel.newConnection(dc);
        }

    }

    public void duplicateWithSource(ActionEvent e) {
        
        if (currentSelection != null) {

            String selectedSource = currentPath.getLastPathComponent().toString();
            String name = treePanel.buildConnectionName(selectedSource);
            DatabaseConnection dc = currentSelection.copy().withSource(selectedSource).withName(name);
            treePanel.newConnection(dc);
        }
        
    }
    
    public void exportExcel(ActionEvent e) {
        importExportDialog(ImportExportDataProcess.EXCEL);
    }

    public void importXml(ActionEvent e) {
        importExportDialog(ImportExportDataProcess.IMPORT_XML);
    }

    public void exportXml(ActionEvent e) {
        importExportDialog(ImportExportDataProcess.EXPORT_XML);
    }

    public void importDelimited(ActionEvent e) {
        importExportDialog(ImportExportDataProcess.IMPORT_DELIMITED);
    }

    public void exportDelimited(ActionEvent e) {
        importExportDialog(ImportExportDataProcess.EXPORT_DELIMITED);
    }

    public void exportSQL(ActionEvent e) {

        NamedObject object = treePanel.getSelectedNamedObject();

        if (object != null && (object instanceof DatabaseTable)) {

            Action action = ActionBuilder.get("export-sql-command");
            action.actionPerformed(new ActionEvent(object, e.getID(), e.getActionCommand()));
        }
    }

    public void moveToFolder(ActionEvent e) {
        treePanel.moveToFolder(currentSelection);
    }

    public void properties(ActionEvent e) {
        //reloadView = true;
        treePanel.setSelectedConnection(currentSelection);
    }
    
    public void connect(ActionEvent e) {
        treePanel.connect(currentSelection);
    }

    private void importExportDialog(int transferType) {

        NamedObject object = treePanel.getSelectedNamedObject();
        if (object == null || !(object instanceof DatabaseObject)) {
            return;
        }

        DatabaseConnection dc = treePanel.getSelectedDatabaseConnection();

        DatabaseObject _object = (DatabaseObject)object;
        String schemaName = _object.getNamePrefix(); // _object.getSchemaName();
        String tableName = _object.getName();

        BaseDialog dialog = null;
        JPanel panel = null;

        try {
            GUIUtilities.showWaitCursor();
            switch (transferType) {

                case ImportExportDataProcess.EXPORT_DELIMITED:
                    dialog = new BaseDialog(bundledString("ExportData"), false, false);
                    panel = new ImportExportDelimitedPanel(
                                    dialog, ImportExportDataProcess.EXPORT,
                                    dc, schemaName, tableName);
                    break;

                case ImportExportDataProcess.IMPORT_DELIMITED:
                    dialog = new BaseDialog(bundledString("ImportData"), false, false);
                    panel = new ImportExportDelimitedPanel(
                                    dialog, ImportExportDataProcess.IMPORT,
                                    dc, schemaName, tableName);
                    break;

                case ImportExportDataProcess.EXPORT_XML:
                    dialog = new BaseDialog(bundledString("exportXml"), false, false);
                    panel = new ImportExportXMLPanel(
                                    dialog, ImportExportDataProcess.EXPORT,
                                    dc, schemaName, tableName);
                    break;

                case ImportExportDataProcess.IMPORT_XML:
                    dialog = new BaseDialog(bundledString("importXml"), false, false);
                    panel = new ImportExportXMLPanel(
                                    dialog, ImportExportDataProcess.IMPORT,
                                    dc, schemaName, tableName);
                    break;

                case ImportExportDataProcess.EXCEL:
                    dialog = new BaseDialog(bundledString("exportExcel"), false, false);
                    panel = new ImportExportExcelPanel(
                                    dialog, ImportExportDataProcess.EXPORT,
                                    dc, schemaName, tableName);
                    break;

            }

            dialog.addDisplayComponent(panel);
            dialog.display();
        }
        finally {
            GUIUtilities.showNormalCursor();
        }
    }

    private DatabaseTable getSelectedTable() {
        return (DatabaseTable)treePanel.getSelectedNamedObject();
    }

    private StatementToEditorWriter getStatementWriter() {
        if (statementWriter == null) {
            statementWriter = new StatementToEditorWriter();
        }
        return statementWriter;
    }

    private void statementToEditor(DatabaseConnection databaseConnection, String statement) {
        getStatementWriter().writeToOpenEditor(databaseConnection, statement);
    }

    public void selectStatement(ActionEvent e) {
        statementToEditor(treePanel.getSelectedDatabaseConnection(), getSelectedTable().getSelectSQLText());
    }

    public void insertStatement(ActionEvent e) {
        statementToEditor(treePanel.getSelectedDatabaseConnection(), getSelectedTable().getInsertSQLText());
    }

    public void updateStatement(ActionEvent e) {
        statementToEditor(treePanel.getSelectedDatabaseConnection(), getSelectedTable().getUpdateSQLText());
    }

    public void createTableStatement(ActionEvent e) {
        try {
            statementToEditor(treePanel.getSelectedDatabaseConnection(), getSelectedTable().getCreateSQLText());
        } catch (DataSourceException dse) {
            handleException(dse);
        }
    }

    private void handleException(Throwable e) {
        treePanel.handleException(e);
    }

    protected Object getCurrentPathComponent() {
        if (hasCurrentPath()) {
            return currentPath.getLastPathComponent();
        }
        return null;
    }

    protected boolean hasCurrentPath() {
        return (currentPath != null);
    }

    protected boolean hasCurrentSelection() {
        return (currentSelection != null);
    }

    protected DatabaseConnection getCurrentSelection() {
        return currentSelection;
    }

    protected void setCurrentSelection(DatabaseConnection currentSelection) {
        this.currentSelection = currentSelection;
    }

    protected void setCurrentPath(TreePath currentPath) {
        this.currentPath = currentPath;
    }

    protected TreePath getCurrentPath() {
        return currentPath;
    }

}




