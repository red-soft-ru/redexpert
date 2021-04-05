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
import org.executequery.databaseobjects.*;
import org.executequery.databaseobjects.impl.*;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.browser.managment.WindowAddRole;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.databaseobjects.*;
import org.executequery.gui.forms.FormObjectView;
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
            else if (node.getType() == NamedObject.DATABASE_TRIGGER)
                type = NamedObject.META_TYPES[NamedObject.TRIGGER];
            else
                type = NamedObject.META_TYPES[node.getType()];
            String query = "DROP " + type + " " + MiscUtils.getFormattedObject(node.getName());
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
                case NamedObject.DATABASE_TRIGGER:
                case NamedObject.DDL_TRIGGER:
                    if (GUIUtilities.isDialogOpen(CreateTriggerPanel.CREATE_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateTriggerPanel.CREATE_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();
                            BaseDialog dialog =
                                    new BaseDialog(CreateTriggerPanel.CREATE_TITLE, false);
                            CreateTriggerPanel panel = new CreateTriggerPanel(currentSelection, dialog,
                                    type);
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
                case NamedObject.DATABASE_TRIGGER:
                case NamedObject.DDL_TRIGGER:
                    if (GUIUtilities.isDialogOpen(CreateTriggerPanel.EDIT_TITLE)) {

                        GUIUtilities.setSelectedDialog(CreateTriggerPanel.EDIT_TITLE);

                    } else {
                        try {
                            GUIUtilities.showWaitCursor();

                            BaseDialog dialog = new BaseDialog(CreateTriggerPanel.EDIT_TITLE, false);
                            createObjectPanel = new CreateTriggerPanel(currentSelection, dialog,
                                    (DefaultDatabaseTrigger) node.getDatabaseObject(), type);
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
            String name;
            if (currentPath.getLastPathComponent() instanceof DatabaseObjectNode) {
                DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
                if (node.getDatabaseObject() instanceof DefaultDatabaseColumn)
                    name = node.getDatabaseObject().getParent().getName() + "." + node.getName();
                else name = node.getName();
            } else name = currentPath.getLastPathComponent().toString();
            GUIUtilities.copyToClipBoard(name);
        }
    }

    public void disconnect(ActionEvent e) {
        treePanel.disconnect(currentSelection);
    }



    /* dz .................................. */

    private BrowserController controller;
   // private BrowserViewPanel browserViewPanel;
    private BrowserViewPanel viewPanel;

    public void dataBaseInformation (ActionEvent e) {
       valueChanged_();
    }
    public void valueChanged_ ()
    {
        controller = new BrowserController(treePanel);
        DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
        DatabaseConnection connection = currentSelection;
        valueChanged_(node, connection);
    }

    public void valueChanged_ (DatabaseObjectNode node, DatabaseConnection connection)
    {
        treePanel.setInProcess(true);

        try {

            FormObjectView panel = buildPanelView(node);
            panel.setDatabaseObjectNode(node);
            String type = "";
            if (node.getType() < NamedObject.META_TYPES.length)
                type = NamedObject.META_TYPES[node.getType()];
            if (connection == null)
                connection = getDatabaseConnection();
            if (node.isHostNode() || node.getType() == NamedObject.CATALOG)
                panel.setObjectName(null);
            else panel.setObjectName(node.getShortName().trim() + ":" + type + ":" + connection.getName());
            panel.setDatabaseConnection(connection);
            if (panel != null) {
                viewPanel.setView(panel);
                checkBrowserPanel();
            }

        } finally {

            treePanel.setInProcess(false);
        }

    }

    protected DatabaseConnection getDatabaseConnection() {

        return treePanel.getSelectedDatabaseConnection();
    }

    private FormObjectView buildPanelView(DatabaseObjectNode node) {
        try {

            NamedObject databaseObject = node.getDatabaseObject();
            if (databaseObject == null) {

                return null;
            }

//            System.out.println("selected object type: " + databaseObject.getClass().getName());
            viewPanel = new BrowserViewPanel(controller);
            int type = node.getType();
            switch (type) {
                case NamedObject.HOST:
                    viewPanel = (BrowserViewPanel) GUIUtilities.getCentralPane(BrowserViewPanel.TITLE);
                    if (viewPanel == null)
                        viewPanel = new BrowserViewPanel(controller);
                    HostPanel hostPanel = hostPanel();
                    hostPanel.setValues((DatabaseHost) databaseObject);

                    return hostPanel;

                // catalog node:
                // this will display the schema table list
                case NamedObject.CATALOG:
                    viewPanel = (BrowserViewPanel) GUIUtilities.getCentralPane(BrowserViewPanel.TITLE);
                    CatalogPanel catalogPanel = null;
                    if (!viewPanel.containsPanel(CatalogPanel.NAME)) {
                        catalogPanel = new CatalogPanel(controller);
                        viewPanel.addToLayout(catalogPanel);
                    } else {
                        catalogPanel = (CatalogPanel) viewPanel.
                                getFormObjectView(CatalogPanel.NAME);
                    }

                    catalogPanel.setValues((DatabaseCatalog) databaseObject);
                    return catalogPanel;

                case NamedObject.SCHEMA:
                    viewPanel = (BrowserViewPanel) GUIUtilities.getCentralPane(BrowserViewPanel.TITLE);
                    SchemaPanel schemaPanel = null;
                    if (!viewPanel.containsPanel(SchemaPanel.NAME)) {
                        schemaPanel = new SchemaPanel(controller);
                        viewPanel.addToLayout(schemaPanel);
                    } else {
                        schemaPanel = (SchemaPanel) viewPanel.
                                getFormObjectView(SchemaPanel.NAME);
                    }

                    schemaPanel.setValues((DatabaseSchema) databaseObject);
                    return schemaPanel;

                case NamedObject.META_TAG:
                case NamedObject.SYSTEM_STRING_FUNCTIONS:
                case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
                case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
                    MetaKeyPanel metaKeyPanel = null;
                    if (!viewPanel.containsPanel(MetaKeyPanel.NAME)) {
                        metaKeyPanel = new MetaKeyPanel(controller);
                        viewPanel.addToLayout(metaKeyPanel);
                    } else {
                        metaKeyPanel = (MetaKeyPanel) viewPanel.
                                getFormObjectView(MetaKeyPanel.NAME);
                    }

                    metaKeyPanel.setValues(databaseObject);
                    return metaKeyPanel;

                case NamedObject.FUNCTION: // Internal function of Red Database 3+
                    BrowserFunctionPanel functionPanel = null;
                    if (!viewPanel.containsPanel(BrowserFunctionPanel.NAME)) {
                        functionPanel = new BrowserFunctionPanel(controller);
                        viewPanel.addToLayout(functionPanel);
                    } else {
                        functionPanel = (BrowserFunctionPanel) viewPanel.
                                getFormObjectView(BrowserFunctionPanel.NAME);
                    }

                    functionPanel.setValues((DefaultDatabaseFunction) databaseObject);
                    return functionPanel;
                case NamedObject.PROCEDURE:
                case NamedObject.SYSTEM_FUNCTION:
                    BrowserProcedurePanel procsPanel = null;
                    if (!viewPanel.containsPanel(BrowserProcedurePanel.NAME)) {
                        procsPanel = new BrowserProcedurePanel(controller);
                        viewPanel.addToLayout(procsPanel);
                    } else {
                        procsPanel = (BrowserProcedurePanel) viewPanel.
                                getFormObjectView(BrowserProcedurePanel.NAME);
                    }

                    procsPanel.setValues((DatabaseExecutable) databaseObject);
                    return procsPanel;

                case NamedObject.TRIGGER:
                case NamedObject.SYSTEM_TRIGGER:
                case NamedObject.DATABASE_TRIGGER:
                    BrowserTriggerPanel triggerPanel = null;
                    if (!viewPanel.containsPanel(BrowserTriggerPanel.NAME)) {
                        triggerPanel = new BrowserTriggerPanel(controller);
                        viewPanel.addToLayout(triggerPanel);
                    } else {
                        triggerPanel = (BrowserTriggerPanel) viewPanel.
                                getFormObjectView(BrowserTriggerPanel.NAME);
                    }

                    triggerPanel.setValues((DefaultDatabaseTrigger) databaseObject);
                    return triggerPanel;

                case NamedObject.PACKAGE:
                    BrowserPackagePanel packagePanel = null;
                    if (!viewPanel.containsPanel(BrowserPackagePanel.NAME)) {
                        packagePanel = new BrowserPackagePanel(controller);
                        viewPanel.addToLayout(packagePanel);
                    } else {
                        packagePanel = (BrowserPackagePanel) viewPanel.
                                getFormObjectView(BrowserPackagePanel.NAME);
                    }

                    packagePanel.setValues((DefaultDatabasePackage) databaseObject);
                    return packagePanel;

                case NamedObject.SEQUENCE:
                    BrowserSequencePanel sequencePanel = null;
                    if (!viewPanel.containsPanel(BrowserSequencePanel.NAME)) {
                        sequencePanel = new BrowserSequencePanel(controller);
                        viewPanel.addToLayout(sequencePanel);
                    } else {
                        sequencePanel = (BrowserSequencePanel) viewPanel.
                                getFormObjectView(BrowserSequencePanel.NAME);
                    }

                    sequencePanel.setValues((DefaultDatabaseSequence) databaseObject);
                    return sequencePanel;

                case NamedObject.DOMAIN:
                case NamedObject.SYSTEM_DOMAIN:
                    BrowserDomainPanel domainPanel = null;
                    if (!viewPanel.containsPanel(BrowserDomainPanel.NAME)) {
                        domainPanel = new BrowserDomainPanel(controller);
                        viewPanel.addToLayout(domainPanel);
                    } else {
                        domainPanel = (BrowserDomainPanel) viewPanel.
                                getFormObjectView(BrowserDomainPanel.NAME);
                    }

                    domainPanel.setValues((DefaultDatabaseDomain) databaseObject);
                    return domainPanel;
                case NamedObject.ROLE:
                case NamedObject.SYSTEM_ROLE:
                    BrowserRolePanel rolePanel = null;
                    if (!viewPanel.containsPanel(BrowserRolePanel.NAME)) {
                        rolePanel = new BrowserRolePanel(controller);
                        viewPanel.addToLayout(rolePanel);
                    } else {
                        rolePanel = (BrowserRolePanel) viewPanel.
                                getFormObjectView(BrowserRolePanel.NAME);
                    }
                    rolePanel.setValues((DefaultDatabaseRole) databaseObject, controller);
                    return rolePanel;
                case NamedObject.EXCEPTION:
                    BrowserExceptionPanel exceptionPanel = null;
                    if (!viewPanel.containsPanel(BrowserExceptionPanel.NAME)) {
                        exceptionPanel = new BrowserExceptionPanel(controller);
                        viewPanel.addToLayout(exceptionPanel);
                    } else {
                        exceptionPanel = (BrowserExceptionPanel) viewPanel.
                                getFormObjectView(BrowserExceptionPanel.NAME);
                    }

                    exceptionPanel.setValues((DefaultDatabaseException) databaseObject);
                    return exceptionPanel;

                case NamedObject.UDF:
                    BrowserUDFPanel browserUDFPanel = null;
                    if (!viewPanel.containsPanel(BrowserUDFPanel.NAME)) {
                        browserUDFPanel = new BrowserUDFPanel(controller);
                        viewPanel.addToLayout(browserUDFPanel);
                    } else {
                        browserUDFPanel = (BrowserUDFPanel) viewPanel.
                                getFormObjectView(BrowserUDFPanel.NAME);
                    }

                    browserUDFPanel.setValues((DefaultDatabaseUDF) databaseObject);
                    return browserUDFPanel;

                case NamedObject.INDEX:
                case NamedObject.SYSTEM_INDEX:
                    BrowserIndexPanel browserIndexPanel = null;
                    if (!viewPanel.containsPanel(BrowserIndexPanel.NAME)) {
                        browserIndexPanel = new BrowserIndexPanel(controller);
                        viewPanel.addToLayout(browserIndexPanel);
                    } else {
                        browserIndexPanel = (BrowserIndexPanel) viewPanel.
                                getFormObjectView(BrowserIndexPanel.NAME);
                    }

                    browserIndexPanel.setValues((DefaultDatabaseIndex) databaseObject);
                    return browserIndexPanel;

                case NamedObject.TABLE:
                case NamedObject.GLOBAL_TEMPORARY:
                    BrowserTableEditingPanel editingPanel = viewPanel.getEditingPanel();
                    editingPanel.setValues((DatabaseTable) databaseObject);
                    return editingPanel;
                case NamedObject.TABLE_COLUMN:
                    TableColumnPanel columnPanel = null;
                    if (!viewPanel.containsPanel(TableColumnPanel.NAME)) {
                        columnPanel = new TableColumnPanel(controller);
                        viewPanel.addToLayout(columnPanel);
                    } else {
                        columnPanel =
                                (TableColumnPanel) viewPanel.getFormObjectView(TableColumnPanel.NAME);
                    }
                    columnPanel.setValues((DatabaseColumn) databaseObject);
                    return columnPanel;

                case NamedObject.TABLE_INDEX:
                case NamedObject.PRIMARY_KEY:
                case NamedObject.FOREIGN_KEY:
                case NamedObject.UNIQUE_KEY:
                    SimpleMetaDataPanel panel = null;
                    if (!viewPanel.containsPanel(SimpleMetaDataPanel.NAME)) {
                        panel = new SimpleMetaDataPanel(controller);
                        viewPanel.addToLayout(panel);
                    } else {
                        panel = (SimpleMetaDataPanel) viewPanel.getFormObjectView(SimpleMetaDataPanel.NAME);
                    }
                    panel.setValues(databaseObject);
                    return panel;

                default:
                    ObjectDefinitionPanel objectDefnPanel = null;
                    if (!viewPanel.containsPanel(ObjectDefinitionPanel.NAME)) {
                        objectDefnPanel = new ObjectDefinitionPanel(controller);
                        viewPanel.addToLayout(objectDefnPanel);
                    } else {
                        objectDefnPanel = (ObjectDefinitionPanel) viewPanel.
                                getFormObjectView(ObjectDefinitionPanel.NAME);
                    }
                    objectDefnPanel.setValues(
                            (org.executequery.databaseobjects.DatabaseObject) databaseObject);
                    return objectDefnPanel;

            }

        } catch (Exception e) {
            handleException(e);
            return null;
        }

    }

    private HostPanel hostPanel() {
        HostPanel hostPanel = null;
        if (!viewPanel.containsPanel(HostPanel.NAME)) {

            hostPanel = new HostPanel(controller);
            viewPanel.addToLayout(hostPanel);

        } else {

            hostPanel = (HostPanel) viewPanel.getFormObjectView(HostPanel.NAME);
        }
        return hostPanel;
    }
    protected void checkBrowserPanel() {

        // check we have the browser view panel
//        if (viewPanel == null) {
//
//            viewPanel = new BrowserViewPanel(this);
//        }

        // check the panel is in the pane
        if (viewPanel == null)
            viewPanel = new BrowserViewPanel(controller);
        String title = viewPanel.getNameObject();
        if (title == null)
            title = BrowserViewPanel.TITLE;
        JPanel _viewPanel = GUIUtilities.getCentralPane(title);

        if (_viewPanel == null) {

            GUIUtilities.addCentralPane(title,
                    BrowserViewPanel.FRAME_ICON,
                    viewPanel,
                    title,
                    true);
            ConnectionHistory.add(viewPanel.getCurrentView());

        } else {

            GUIUtilities.setSelectedCentralPane(title);
        }
    }

        //..................................///////////////////////////////////////////////

    //end of code



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
