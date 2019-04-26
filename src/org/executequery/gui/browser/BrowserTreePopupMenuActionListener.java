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

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.*;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.browser.managment.WindowAddRole;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.databaseobjects.*;
import org.executequery.gui.importexport.ImportExportDataProcess;
import org.executequery.gui.importexport.ImportExportDelimitedPanel;
import org.executequery.gui.importexport.ImportExportExcelPanel;
import org.executequery.gui.importexport.ImportExportXMLPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * @author Takis Diakoumis
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

    public void deleteObject(ActionEvent e) {
        if (currentPath != null && currentSelection != null) {
            DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
            DatabaseObject object = (DatabaseObject) node.getUserObject();
            StringBuilder sb = new StringBuilder();
            sb.append(node.getShortName());
            sb.append(":");
            sb.append(node.getMetaDataKey());
            sb.append(":");
            sb.append(object.getHost());
            if (GUIUtilities.getOpenFrame(sb.toString()) != null) {
                GUIUtilities.displayErrorMessage(bundledString("messageInUse", node.getShortName()));
                return;
            }
            String type;
            if (node.getType() == NamedObject.GLOBAL_TEMPORARY)
                type = NamedObject.META_TYPES[NamedObject.TABLE];
            else if (node.getType() == NamedObject.SYSTEM_DATABASE_TRIGGER)
                type = NamedObject.META_TYPES[NamedObject.TRIGGER];
            else
                type = NamedObject.META_TYPES[node.getType()];
            String query = "DROP " + type + " " + MiscUtils.wordInQuotes(node.getName());
            ExecuteQueryDialog eqd = new ExecuteQueryDialog("Dropping object", query, currentSelection, true);
            eqd.display();
            if (eqd.getCommit())
                treePanel.reloadPath(currentPath.getParentPath());
        }

    }

    public void createObject(ActionEvent e) {
        if (currentPath != null && currentSelection != null) {
            DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
            int type = node.getType();
            if (type == NamedObject.META_TAG)
                for (int i = 0; i < NamedObject.META_TYPES.length; i++)
                    if (NamedObject.META_TYPES[i].equals(node.getMetaDataKey())) {
                        type = i;
                        break;
                    }
            switch (type) {
                case NamedObject.TABLE:
                    if (GUIUtilities.isDialogOpen(CreateTablePanel.TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateTablePanel.TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateTablePanel.TITLE, false);
                            CreateTablePanel panel = new CreateTablePanel(currentSelection, dialog, false);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.ROLE:
                    try {
                        GUIUtilities.showWaitCursor();
                        BaseDialog dialog =
                                new BaseDialog(WindowAddRole.TITLE, true);
                        WindowAddRole panel = new WindowAddRole(dialog, currentSelection);
                        dialog.addDisplayComponentWithEmptyBorder(panel);
                        dialog.display();
                        treePanel.reloadPath(currentPath.getParentPath());
                    } finally {
                        GUIUtilities.showNormalCursor();
                    }
                    break;
                case NamedObject.SEQUENCE:
                    if (GUIUtilities.isDialogOpen(CreateGeneratorPanel.CREATE_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateGeneratorPanel.CREATE_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateGeneratorPanel.CREATE_TITLE, false);
                            CreateGeneratorPanel panel = new CreateGeneratorPanel(currentSelection, dialog);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.VIEW:
                    if (GUIUtilities.isDialogOpen(CreateViewPanel.TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateViewPanel.TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateViewPanel.TITLE, false);
                            CreateViewPanel panel = new CreateViewPanel(currentSelection, dialog);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.DOMAIN:
                    if (GUIUtilities.isDialogOpen(CreateDomainPanel.CREATE_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateDomainPanel.CREATE_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateDomainPanel.CREATE_TITLE, false);
                            CreateDomainPanel panel = new CreateDomainPanel(currentSelection, dialog);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.PROCEDURE:
                    if (GUIUtilities.isDialogOpen(CreateProcedurePanel.TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateProcedurePanel.TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateProcedurePanel.TITLE, false);
                            CreateProcedurePanel panel = new CreateProcedurePanel(currentSelection, dialog);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.TRIGGER:
                    if (GUIUtilities.isDialogOpen(CreateTriggerPanel.CREATE_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateTriggerPanel.CREATE_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateTriggerPanel.CREATE_TITLE, false);
                            CreateTriggerPanel panel = new CreateTriggerPanel(currentSelection, dialog,
                                    NamedObject.TRIGGER);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.SYSTEM_DATABASE_TRIGGER:
                    if (GUIUtilities.isDialogOpen(CreateTriggerPanel.CREATE_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateTriggerPanel.CREATE_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateTriggerPanel.CREATE_TITLE, false);
                            CreateTriggerPanel panel = new CreateTriggerPanel(currentSelection, dialog,
                                    NamedObject.SYSTEM_DATABASE_TRIGGER);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.EXCEPTION:
                    if (GUIUtilities.isDialogOpen(CreateExceptionPanel.CREATE_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateExceptionPanel.CREATE_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateExceptionPanel.CREATE_TITLE, false);
                            CreateExceptionPanel panel = new CreateExceptionPanel(currentSelection, dialog);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.INDEX:
                    if (GUIUtilities.isDialogOpen(CreateIndexPanel.CREATE_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateIndexPanel.CREATE_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateIndexPanel.CREATE_TITLE, false);
                            CreateIndexPanel panel = new CreateIndexPanel(currentSelection, dialog);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.FUNCTION:
                    if (GUIUtilities.isDialogOpen(CreateFunctionPanel.CREATE_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateFunctionPanel.CREATE_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateFunctionPanel.CREATE_TITLE, false);
                            CreateFunctionPanel panel = new CreateFunctionPanel(currentSelection, dialog);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.UDF:
                    if (GUIUtilities.isDialogOpen(CreateUDFPanel.CREATE_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateUDFPanel.CREATE_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateUDFPanel.CREATE_TITLE, false);
                            CreateUDFPanel panel = new CreateUDFPanel(currentSelection, dialog);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.PACKAGE:
                    if (GUIUtilities.isDialogOpen(CreatePackagePanel.CREATE_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreatePackagePanel.CREATE_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreatePackagePanel.CREATE_TITLE, false);
                            CreatePackagePanel panel = new CreatePackagePanel(currentSelection, dialog);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.GLOBAL_TEMPORARY:
                    if (GUIUtilities.isDialogOpen(CreateTablePanel.TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateTablePanel.TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateTablePanel.TITLE, false);
                            CreateTablePanel panel = new CreateTablePanel(currentSelection, dialog, true);
                            showDialogCreateObject(panel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                default:
                    GUIUtilities.displayErrorMessage(bundledString("temporaryInconvenience"));
                    break;


            }
        }

    }

    public void editObject(ActionEvent e) {
        if (currentPath != null && currentSelection != null) {
            DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
            AbstractCreateObjectPanel createObjectPanel = null;
            int type = node.getType();
            if (type == NamedObject.META_TAG)
                for (int i = 0; i < NamedObject.META_TYPES.length; i++)
                    if (Objects.equals(NamedObject.META_TYPES[i], node.getName())) {
                        type = i;
                        break;
                    }
            switch (type) {
                case NamedObject.TABLE:
                case NamedObject.GLOBAL_TEMPORARY:
                case NamedObject.ROLE:
                    treePanel.valueChanged(node, currentSelection);
                    break;
                case NamedObject.SEQUENCE:
                    if (GUIUtilities.isDialogOpen(CreateGeneratorPanel.ALTER_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateGeneratorPanel.ALTER_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateGeneratorPanel.ALTER_TITLE, false);
                            createObjectPanel = new CreateGeneratorPanel(currentSelection, dialog, (DefaultDatabaseSequence) node.getDatabaseObject());
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.VIEW:
                    if (GUIUtilities.isDialogOpen(CreateViewPanel.EDIT_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateViewPanel.EDIT_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateViewPanel.EDIT_TITLE, false);
                            createObjectPanel = new CreateViewPanel(currentSelection, dialog, (DefaultDatabaseView) node.getDatabaseObject());
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.PROCEDURE:
                    if (GUIUtilities.isDialogOpen(CreateProcedurePanel.EDIT_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateProcedurePanel.EDIT_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();

                            BaseDialog dialog = new BaseDialog(CreateProcedurePanel.EDIT_TITLE, false);
                            createObjectPanel = new CreateProcedurePanel(currentSelection, dialog, node.getName().trim());
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.DOMAIN:
                    if (GUIUtilities.isDialogOpen(CreateDomainPanel.EDIT_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateDomainPanel.EDIT_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();

                            BaseDialog dialog = new BaseDialog(CreateDomainPanel.EDIT_TITLE, false);
                            createObjectPanel = new CreateDomainPanel(currentSelection, dialog, node.getName().trim());
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.TRIGGER:
                    if (GUIUtilities.isDialogOpen(CreateTriggerPanel.EDIT_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateTriggerPanel.EDIT_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();

                            BaseDialog dialog = new BaseDialog(CreateTriggerPanel.EDIT_TITLE, false);
                            createObjectPanel = new CreateTriggerPanel(currentSelection, dialog,
                                    (DefaultDatabaseTrigger) node.getDatabaseObject(), NamedObject.TRIGGER);
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.SYSTEM_DATABASE_TRIGGER:
                    if (GUIUtilities.isDialogOpen(CreateTriggerPanel.EDIT_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateTriggerPanel.EDIT_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();

                            BaseDialog dialog = new BaseDialog(CreateTriggerPanel.EDIT_TITLE, false);
                            createObjectPanel = new CreateTriggerPanel(currentSelection, dialog,
                                    (DefaultDatabaseTrigger) node.getDatabaseObject(), NamedObject.SYSTEM_DATABASE_TRIGGER);
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.EXCEPTION:
                    if (GUIUtilities.isDialogOpen(CreateExceptionPanel.ALTER_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateExceptionPanel.ALTER_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateExceptionPanel.ALTER_TITLE, false);
                            createObjectPanel = new CreateExceptionPanel(currentSelection, dialog, (DefaultDatabaseException) node.getDatabaseObject());
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.INDEX:
                    if (GUIUtilities.isDialogOpen(CreateIndexPanel.ALTER_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateIndexPanel.ALTER_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateIndexPanel.ALTER_TITLE, false);
                            createObjectPanel = new CreateIndexPanel(currentSelection, dialog, (DefaultDatabaseIndex) node.getDatabaseObject());
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.FUNCTION:
                    if (GUIUtilities.isDialogOpen(CreateFunctionPanel.EDIT_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateFunctionPanel.EDIT_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();

                            BaseDialog dialog = new BaseDialog(CreateFunctionPanel.EDIT_TITLE, false);
                            createObjectPanel = new CreateFunctionPanel(currentSelection, dialog, node.getName().trim(), (DefaultDatabaseFunction) node.getDatabaseObject());
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.UDF:
                    if (GUIUtilities.isDialogOpen(CreateUDFPanel.EDIT_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateUDFPanel.EDIT_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();

                            BaseDialog dialog = new BaseDialog(CreateUDFPanel.EDIT_TITLE, false);
                            createObjectPanel = new CreateUDFPanel(currentSelection, dialog, node.getDatabaseObject());
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                case NamedObject.PACKAGE:
                    if (GUIUtilities.isDialogOpen(CreatePackagePanel.ALTER_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreatePackagePanel.ALTER_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();

                            BaseDialog dialog = new BaseDialog(CreatePackagePanel.ALTER_TITLE, false);
                            createObjectPanel = new CreatePackagePanel(currentSelection, dialog, (DefaultDatabasePackage) node.getDatabaseObject());
                            showDialogCreateObject(createObjectPanel, dialog);
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                    break;
                default:
                    GUIUtilities.displayErrorMessage(bundledString("temporaryInconvenience"));
                    break;
            }
        }

    }

    public void addNewConnection(ActionEvent e) {
        treePanel.newConnection();
    }

    public void switchDefaultCatalogAndSchemaDisplay(ActionEvent e) {

        JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();

        DatabaseHostNode node =
                (DatabaseHostNode) currentPath.getLastPathComponent();
        node.setDefaultCatalogsAndSchemasOnly(check.isSelected());

        treePanel.nodeStructureChanged(node);
    }

    public void delete(ActionEvent e) {
        if (currentPath != null) {
            DatabaseHostNode node = (DatabaseHostNode) currentPath.getLastPathComponent();
            treePanel.deleteConnection(node);
        }
    }

    public void recycle(ActionEvent e) {
        DatabaseHost host = treePanel.getSelectedMetaObject();
        try {
            host.recycleConnection();
        } catch (DataSourceException dse) {
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
                    currentSelection.getName() + " (" + Bundles.getCommon("copy")) + ")";
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

    public void exportDbunit(ActionEvent e) {

        NamedObject object = treePanel.getSelectedNamedObject();

        if (object != null && (object instanceof DatabaseTable)) {

            Action action = ActionBuilder.get("export-dbunit-command");
            action.actionPerformed(new ActionEvent(object, e.getID(), e.getActionCommand()));
        }

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

        DatabaseObject _object = (DatabaseObject) object;
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

            if (dialog != null) {
                dialog.addDisplayComponent(panel);
            }
            if (dialog != null) {
                dialog.display();
            }
        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    private DatabaseTable getSelectedTable() {
        return (DatabaseTable) treePanel.getSelectedNamedObject();
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

    protected void showDialogCreateObject(AbstractCreateObjectPanel panel, BaseDialog dialog) {
        panel.setTreePanel(treePanel);
        panel.setCurrentPath(currentPath);
        dialog.addDisplayComponentWithEmptyBorder(panel);
        dialog.display();
    }

    protected void showDialogCreateObject(CreateTablePanel panel, BaseDialog dialog) {
        panel.setTreePanel(treePanel);
        panel.setCurrentPath(currentPath);
        dialog.addDisplayComponentWithEmptyBorder(panel);
        dialog.display();
    }

}
