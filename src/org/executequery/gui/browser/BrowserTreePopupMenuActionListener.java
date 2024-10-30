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
import org.executequery.actions.databasecommands.TableValidationCommand;
import org.executequery.actions.toolscommands.ComparerDBCommands;
import org.executequery.actions.toolscommands.DatabaseBackupRestoreCommands;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.*;
import org.executequery.databaseobjects.impl.*;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.gui.AnaliseRecompileDialog;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.browser.nodes.table.TableFolderNode;
import org.executequery.gui.databaseobjects.*;
import org.executequery.gui.table.CreateTablePanel;
import org.executequery.gui.table.EditConstraintPanel;
import org.executequery.gui.table.InsertColumnPanel;
import org.executequery.localization.Bundles;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Takis Diakoumis
 */
public class BrowserTreePopupMenuActionListener extends ReflectiveAction {
    private final ConnectionsTreePanel treePanel;

    private StatementExecutor querySender;
    private DatabaseConnection currentSelection;
    private StatementToEditorWriter statementWriter;

    private TreePath currentPath;
    private TreePath[] treePaths;

    private boolean selectedSeveralPaths = false;

    protected BrowserTreePopupMenuActionListener(ConnectionsTreePanel treePanel) {
        this.treePanel = treePanel;
    }

    // ---

    @SuppressWarnings("unused")
    public void reloadPath(ActionEvent e) {
        if (currentPath != null)
            treePanel.reloadPath(currentPath);
    }

    @SuppressWarnings("unused")
    public void copyName(ActionEvent e) {

        if (currentPath == null)
            return;

        String name;
        Object lastPathComponent = currentPath.getLastPathComponent();
        if (lastPathComponent instanceof DatabaseObjectNode) {

            DatabaseObjectNode node = (DatabaseObjectNode) lastPathComponent;
            name = node.getName();

            NamedObject object = node.getDatabaseObject();
            if (object instanceof DefaultDatabaseColumn)
                name = object.getParent().getName() + "." + name;

        } else
            name = lastPathComponent.toString();

        GUIUtilities.copyToClipBoard(name);
    }

    // --- BrowserTreeHostPopupMenu handlers ---

    @SuppressWarnings("unused")
    public void connect(ActionEvent e) {
        if (currentSelection != null)
            treePanel.connect(currentSelection);
    }

    @SuppressWarnings("unused")
    public void disconnect(ActionEvent e) {
        if (currentSelection != null)
            treePanel.disconnect(currentSelection);
    }

    @SuppressWarnings("unused")
    public void recycleConnection(ActionEvent e) {
        treePanel.getSelectedMetaObject().recycleConnection();
    }

    @SuppressWarnings("unused")
    public void newFolder(ActionEvent e) {
        treePanel.newFolder();
    }

    @SuppressWarnings("unused")
    public void newConnection(ActionEvent e) {
        treePanel.newConnection();
    }

    @SuppressWarnings("unused")
    public void extractMetadata(ActionEvent e) {
        if (currentPath != null) {
            DatabaseHostNode node = (DatabaseHostNode) currentPath.getLastPathComponent();
            new ComparerDBCommands().exportMetadata(node.getDatabaseConnection());
        }
    }

    @SuppressWarnings("unused")
    public void backupRestore(ActionEvent e) {
        if (currentSelection != null)
            new DatabaseBackupRestoreCommands().execute(null);
    }

    @SuppressWarnings("unused")
    public void moveToFolder(ActionEvent e) {
        if (currentSelection != null)
            treePanel.moveToFolder(currentSelection);
    }

    @SuppressWarnings("unused")
    public void duplicateConnection(ActionEvent e) {
        if (currentSelection != null)
            treePanel.newConnection(currentSelection.copy());
    }

    @SuppressWarnings("unused")
    public void deleteConnection(ActionEvent e) {
        if (currentPath != null) {
            DatabaseHostNode node = (DatabaseHostNode) currentPath.getLastPathComponent();
            treePanel.deleteConnection(node);
        }
    }

    @SuppressWarnings("unused")
    public void showConnectionInfo(ActionEvent e) {
        if (currentSelection != null) {
            DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
            treePanel.getController().valueChanged(node, currentSelection);
        }
    }

    // --- BrowserTreePopupMenu handlers ---

    @SuppressWarnings("unused")
    public void createObject(ActionEvent e) {
        if (currentPath != null && currentSelection != null) {
            DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
            showCreateObjectDialog(node, currentSelection, false);
        }
    }

    @SuppressWarnings("unused")
    public void editObject(ActionEvent e) {
        if (currentPath != null && currentSelection != null) {
            DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
            showCreateObjectDialog(node, currentSelection, true);
        }
    }

    @SuppressWarnings("unused")
    public void deleteObject(ActionEvent e) {

        if (currentPath == null || currentSelection == null)
            return;

        DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
        String tableName = getTableName(node.getDatabaseObject());

        String query = getDropQuery(node, node.getType());
        if (query == null)
            return;

        ExecuteQueryDialog executeDialog = new ExecuteQueryDialog(
                bundledString("DropObject"),
                query,
                currentSelection,
                true
        );

        executeDialog.display();
        if (executeDialog.getCommit())
            reloadNodes(tableName);
    }

    @SuppressWarnings("unused")
    public void validateTable(ActionEvent e) {
        new TableValidationCommand().validateTableAndShowResult(currentSelection, getSelectedTableObject().getName());
    }

    // --- trigger/index activity handlers ---

    @SuppressWarnings("unused")
    public void setActive(ActionEvent e) {
        setSelectedObjectActive(true);
    }

    @SuppressWarnings("unused")
    public void setInactive(ActionEvent e) {
        setSelectedObjectActive(false);
    }

    // --- selection handlers ---

    @SuppressWarnings("unused")
    public void selectAllNeighbors(ActionEvent e) {
        selectPaths(true);
    }

    @SuppressWarnings("unused")
    public void selectAllChildren(ActionEvent e) {
        selectPaths(false);
    }

    // --- recompile handlers ---

    @SuppressWarnings("unused")
    public void recompileAll(ActionEvent e) {
        recompileObjects("recompile-message", false);
    }

    @SuppressWarnings("unused")
    public void recompileInvalid(ActionEvent e) {
        recompileObjects("recompile-invalid-message", true);
    }

    // --- refresh index statistic handlers ---

    @SuppressWarnings("unused")
    public void refreshIndexStatistic(ActionEvent e) {
        refreshIndexStatistic(false);
    }

    @SuppressWarnings("unused")
    public void refreshAllIndexStatistic(ActionEvent e) {
        refreshIndexStatistic(true);
    }

    // --- query generation handlers ---

    @SuppressWarnings("unused")
    public void generateSelectStatement(ActionEvent e) {
        getStatementWriter().writeToOpenEditor(currentSelection, getSelectedTableObject().getSelectSQLText());
    }

    @SuppressWarnings("unused")
    public void generateInsertStatement(ActionEvent e) {
        getStatementWriter().writeToOpenEditor(currentSelection, getSelectedTableObject().getInsertSQLText());
    }

    @SuppressWarnings("unused")
    public void generateUpdateStatement(ActionEvent e) {
        getStatementWriter().writeToOpenEditor(currentSelection, getSelectedTableObject().getUpdateSQLText());
    }

    @SuppressWarnings("unused")
    public void generateCreateStatement(ActionEvent e) {
        getStatementWriter().writeToOpenEditor(currentSelection, getSelectedTableObject().getCreateSQLText());
    }

    // --- handlers helper methods ---

    private void reloadNodes(String tableName) {

        if (treePanel == null || currentPath == null)
            return;

        TreePath parentPath = currentPath.getParentPath();
        if (parentPath != null) {
            treePanel.reloadPath(parentPath);
            treePanel.reloadRelatedNodes(
                    (DatabaseObjectNode) parentPath.getLastPathComponent(),
                    tableName,
                    isGlobalTemporary(tableName)
            );
        }
    }

    private static String getTableName(NamedObject namedObject) {
        if (namedObject instanceof DefaultDatabaseIndex)
            return ((DefaultDatabaseIndex) namedObject).getTableName();
        if (namedObject instanceof DefaultDatabaseTrigger)
            return ((DefaultDatabaseTrigger) namedObject).getTriggerTableName();
        return null;
    }

    private boolean isGlobalTemporary(String tableName) {
        DefaultDatabaseHost host = treePanel.getDefaultDatabaseHostFromConnection(currentSelection);
        List<NamedObject> globalTables = host.getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY]);
        return globalTables != null && globalTables.stream().map(NamedObject::getName).anyMatch(name -> Objects.equals(name, tableName));
    }

    public void showCreateObjectDialog(DatabaseObjectNode node, DatabaseConnection connection, boolean editing) {
        try {
            GUIUtilities.showWaitCursor();

            if (editing) {
                treePanel.valueChanged(node, connection);
                return;
            }

            BaseDialog dialog = new BaseDialog("", false);
            AbstractCreateObjectPanel panel = getCreateObjectPanel(node, dialog, connection);
            if (panel == null)
                return;

            String title = panel.getCreateTitle();
            if (GUIUtilities.isDialogOpen(title)) {
                GUIUtilities.setSelectedDialog(title);
                return;
            }

            panel.setTreePanel(treePanel);
            panel.setCurrentPath(currentPath);

            dialog.setTitle(title);
            dialog.addDisplayComponentWithEmptyBorder(panel);
            dialog.display();

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    private AbstractCreateObjectPanel getCreateObjectPanel(DatabaseObjectNode node, BaseDialog
            dialog, DatabaseConnection connection) {

        int type = node.getType();
        if (type == NamedObject.META_TAG) {
            for (int i = 0; i < NamedObject.META_TYPES.length; i++) {
                if (NamedObject.META_TYPES[i].equals(node.getMetaDataKey())) {
                    type = i;
                    break;
                }
            }
        }

        ColumnConstraint constraint;
        AbstractCreateObjectPanel panel = null;
        switch (type) {

            case NamedObject.TABLE:
                panel = new CreateTablePanel(connection, dialog);
                break;

            case NamedObject.GLOBAL_TEMPORARY:
                panel = new CreateGlobalTemporaryTable(connection, dialog);
                break;

            case NamedObject.ROLE:
                panel = new CreateRolePanel(connection, dialog, null);
                break;

            case NamedObject.SEQUENCE:
                panel = new CreateGeneratorPanel(connection, dialog);
                break;

            case NamedObject.VIEW:
                panel = new CreateViewPanel(connection, dialog);
                break;

            case NamedObject.DOMAIN:
                panel = new CreateDomainPanel(connection, dialog);
                break;

            case NamedObject.PROCEDURE:
                panel = new CreateProcedurePanel(connection, dialog);
                break;

            case NamedObject.TRIGGER:
            case NamedObject.TRIGGERS_FOLDER_NODE:
                String triggerTableName = getTableName(node, type);
                panel = triggerTableName != null ?
                        new CreateTriggerPanel(connection, dialog, NamedObject.TRIGGER, triggerTableName) :
                        new CreateTriggerPanel(connection, dialog, NamedObject.TRIGGER);
                break;

            case NamedObject.DDL_TRIGGER:
            case NamedObject.DATABASE_TRIGGER:
                panel = new CreateTriggerPanel(connection, dialog, type);
                break;

            case NamedObject.EXCEPTION:
                panel = new CreateExceptionPanel(connection, dialog);
                break;

            case NamedObject.INDEX:
            case NamedObject.INDEXES_FOLDER_NODE:
                String indexTableName = getTableName(node, type);
                panel = indexTableName != null ?
                        new CreateIndexPanel(connection, dialog, indexTableName) :
                        new CreateIndexPanel(connection, dialog);
                break;

            case NamedObject.FUNCTION:
                panel = new CreateFunctionPanel(connection, dialog);
                break;

            case NamedObject.UDF:
                panel = new CreateUDFPanel(connection, dialog);
                break;

            case NamedObject.PACKAGE:
                panel = new CreatePackagePanel(connection, dialog);
                break;

            case NamedObject.USER:
                panel = new CreateDatabaseUserPanel(connection, dialog);
                break;

            case NamedObject.TABLESPACE:
                panel = new CreateTablespacePanel(connection, dialog);
                break;

            case NamedObject.JOB:
                panel = new CreateJobPanel(connection, dialog);
                break;

            case NamedObject.TABLE_COLUMN:
                panel = new InsertColumnPanel((DatabaseTableColumn) node.getDatabaseObject(), dialog, false);
                break;

            case NamedObject.COLUMNS_FOLDER_NODE:
                panel = new InsertColumnPanel((DatabaseTable) node.getDatabaseObject(), dialog);
                break;

            case NamedObject.PRIMARY_KEY:
                constraint = (ColumnConstraint) node.getDatabaseObject();
                panel = new EditConstraintPanel(constraint.getTable(), dialog, NamedObject.PRIMARY_KEY);
                break;

            case NamedObject.FOREIGN_KEY:
                constraint = (ColumnConstraint) node.getDatabaseObject();
                panel = new EditConstraintPanel(constraint.getTable(), dialog, NamedObject.FOREIGN_KEY);
                break;

            case NamedObject.PRIMARY_KEYS_FOLDER_NODE:
                panel = new EditConstraintPanel((DatabaseTable) node.getDatabaseObject(), dialog, NamedObject.PRIMARY_KEY);
                break;

            case NamedObject.FOREIGN_KEYS_FOLDER_NODE:
                panel = new EditConstraintPanel((DatabaseTable) node.getDatabaseObject(), dialog, NamedObject.FOREIGN_KEY);
                break;

            default:
                GUIUtilities.displayErrorMessage(bundledString("temporaryInconvenience"));
                break;
        }

        return panel;
    }

    private static String getTableName(DatabaseObjectNode node, int type) {
        String tableName = null;

        if (type == NamedObject.INDEX || type == NamedObject.TRIGGER) {
            TreeNode parentNode = node.getParent();
            if (parentNode instanceof TableFolderNode) {
                TableFolderNode tableFolderNode = (TableFolderNode) parentNode;
                tableName = tableFolderNode.getShortName();
            }

        } else if (type == NamedObject.INDEXES_FOLDER_NODE || type == NamedObject.TRIGGERS_FOLDER_NODE) {
            tableName = node.getShortName();
        }

        return tableName;
    }

    private String getDropQuery(DatabaseObjectNode node, int nodeType) {

        if (nodeType == NamedObject.TABLE_COLUMN) {
            DatabaseObjectNode parent = (DatabaseObjectNode) node.getParent();
            DatabaseTable table = (DatabaseTable) parent.getDatabaseObject();

            StringBuilder tabName = new StringBuilder()
                    .append(parent.getShortName()).append(".").append(node.getShortName())
                    .append(":").append(NamedObject.META_TYPES[nodeType])
                    .append(":").append(table.getHost());

            if (isOpen(node, tabName.toString()))
                return null;

            return SQLUtils.generateDefaultDropColumnQuery(
                    node.getName(),
                    table.getName(),
                    table.getHost().getDatabaseConnection(),
                    false
            );
        }

        if (nodeType >= NamedObject.PRIMARY_KEY && nodeType <= NamedObject.CHECK_KEY) {
            DatabaseObjectNode parent = (DatabaseObjectNode) node.getParent();
            DatabaseTable table = (DatabaseTable) parent.getDatabaseObject();

            return SQLUtils.generateDefaultDropColumnQuery(
                    node.getName(),
                    table.getName(),
                    table.getHost().getDatabaseConnection(),
                    true
            );
        }

        StringBuilder tabName = new StringBuilder()
                .append(node.getShortName())
                .append(":").append(node.getMetaDataKey())
                .append(":").append(((DatabaseObject) node.getUserObject()).getHost());

        if (isOpen(node, tabName.toString()))
            return null;

        String type;
        if (nodeType == NamedObject.GLOBAL_TEMPORARY)
            type = NamedObject.META_TYPES[NamedObject.TABLE];
        else if (nodeType == NamedObject.DATABASE_TRIGGER || nodeType == NamedObject.DDL_TRIGGER)
            type = NamedObject.META_TYPES[NamedObject.TRIGGER];
        else
            type = NamedObject.META_TYPES[nodeType];

        return SQLUtils.generateDefaultDropQuery(type, node.getName(), currentSelection);
    }

    private boolean isOpen(DatabaseObjectNode node, String tabName) {
        if (GUIUtilities.getOpenFrame(tabName) != null) {
            GUIUtilities.displayErrorMessage(bundledString("messageInUse", node.getShortName()));
            return true;
        }
        return false;
    }

    private void setSelectedObjectActive(boolean isActive) {

        List<TreePath> selectedObjects = isSelectedSeveralPaths() ?
                new ArrayList<>(Arrays.asList(treePaths)) :
                new ArrayList<>(Collections.singletonList(currentPath));

        try {
            boolean firstErrorExists = false;
            StringBuilder error = new StringBuilder();

            for (TreePath treePath : selectedObjects) {
                DatabaseObjectNode node = (DatabaseObjectNode) treePath.getLastPathComponent();
                String query = SQLUtils.generateAlterActive(NamedObject.META_TYPES[node.getType()], node.getName(), isActive);

                querySender = new DefaultStatementExecutor(currentSelection, false);
                SqlStatementResult result = querySender.execute(QueryTypes.ALTER_OBJECT, query);
                treePanel.reloadPath(treePath);

                if (result.isException() && !firstErrorExists) {
                    error.append(result.getErrorMessage());
                    firstErrorExists = true;
                }
            }

            querySender.execute(QueryTypes.COMMIT, "");
            if (error.length() > 0)
                GUIUtilities.displayErrorMessage(error.toString());

        } catch (SQLException ex) {
            GUIUtilities.displayErrorMessage(ex.getMessage());

        } finally {
            querySender.releaseResources();
        }
    }

    private void selectPaths(boolean fromParent) {

        TreePath[] selectionPaths = treePanel.getTree().getSelectionPaths();
        if (selectionPaths == null)
            return;

        TreeNode node = (DefaultMutableTreeNode) selectionPaths[0].getLastPathComponent();
        if (fromParent)
            node = node.getParent();

        DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[node.getChildCount()];
        for (int i = 0; i < node.getChildCount(); i++)
            if (node.getChildAt(i) instanceof DefaultMutableTreeNode)
                nodes[i] = (DefaultMutableTreeNode) node.getChildAt(i);

        treePanel.getTree().selectNodes(nodes);
    }

    private void recompileObjects(String key, boolean onlyInvalid) {

        DatabaseObjectNode objectNode = (DatabaseObjectNode) currentPath.getLastPathComponent();
        if (objectNode != null && objectNode.getType() != NamedObject.META_TAG)
            objectNode = (DatabaseObjectNode) objectNode.getParent();

        if (objectNode == null)
            return;

        int subType = ((DefaultDatabaseMetaTag) objectNode.getDatabaseObject()).getSubType();
        String confirmMessage = bundledString(key, Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[subType]));
        if (GUIUtilities.displayConfirmDialog(confirmMessage) != JOptionPane.YES_OPTION)
            return;

        AnaliseRecompileDialog recompileDialog = new AnaliseRecompileDialog(
                bundledString("Analise"),
                true,
                objectNode,
                onlyInvalid
        );
        recompileDialog.display();

        if (recompileDialog.success) {
            new ExecuteQueryDialog(
                    bundledString("Recompile"),
                    recompileDialog.sb,
                    currentSelection,
                    true,
                    "^",
                    true,
                    false
            ).display();
        }
    }

    private void refreshIndexStatistic(boolean allIndexes) {

        DatabaseObjectNode objectNode = (DatabaseObjectNode) currentPath.getLastPathComponent();
        if (objectNode != null && objectNode.getType() != NamedObject.META_TAG)
            objectNode = (DatabaseObjectNode) objectNode.getParent();

        if (objectNode == null)
            return;

        String confirmMessage = bundledString(allIndexes ? "recompute-all-message" : "recompute-message");
        if (GUIUtilities.displayConfirmDialog(confirmMessage) != JOptionPane.YES_OPTION)
            return;

        List<Object> selectedObjects = allIndexes ?
                new ArrayList<>(objectNode.getChildObjects()) :
                isSelectedSeveralPaths() ?
                        new ArrayList<>(Arrays.stream(treePaths).map(TreePath::getLastPathComponent).collect(Collectors.toList())) :
                        new ArrayList<>(Collections.singletonList(currentPath.getLastPathComponent()));

        StringBuilder sb = new StringBuilder();
        for (Object object : selectedObjects) {
            DatabaseObjectNode node = (DatabaseObjectNode) object;
            sb.append("SET STATISTICS INDEX ").append(MiscUtils.getFormattedObject(node.getName(), currentSelection)).append(";\n");
            node.getDatabaseObject().reset();
        }

        new ExecuteQueryDialog(
                bundledString("Recompute"),
                sb.toString(),
                currentSelection,
                true,
                ";",
                true,
                false
        ).display();
    }

    // ---

    private DatabaseTableObject getSelectedTableObject() {
        return (DatabaseTableObject) treePanel.getSelectedNamedObject();
    }

    private StatementToEditorWriter getStatementWriter() {
        if (statementWriter == null)
            statementWriter = new StatementToEditorWriter();
        return statementWriter;
    }

    protected void setTreePaths(TreePath[] treePaths) {
        this.treePaths = treePaths;
        if (treePaths.length > 0)
            this.currentPath = treePaths[0];
    }

    protected Object getCurrentPathComponent() {
        return hasCurrentPath() ? currentPath.getLastPathComponent() : null;
    }

    protected boolean hasCurrentPath() {
        return currentPath != null;
    }

    protected void setCurrentSelection(DatabaseConnection currentSelection) {
        this.currentSelection = currentSelection;
    }

    protected void setCurrentPath(TreePath currentPath) {
        this.currentPath = currentPath;
    }

    protected void setSelectedSeveralPaths(boolean selectedSeveralPaths) {
        this.selectedSeveralPaths = selectedSeveralPaths;
    }

    protected boolean isSelectedSeveralPaths() {
        return selectedSeveralPaths;
    }

    // ---

    @Override
    protected void postActionPerformed(ActionEvent e) {
        currentSelection = null;
        currentPath = null;
    }

}
