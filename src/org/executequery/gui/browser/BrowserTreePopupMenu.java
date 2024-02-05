/*
 * BrowserTreePopupMenu.java
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

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseCatalog;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.gui.browser.nodes.DatabaseCatalogNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.menu.MenuItemFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Takis Diakoumis
 */
public class BrowserTreePopupMenu extends JPopupMenu {

    private final JMenuItem addNewConnection;
    private final JMenuItem getMetadata;
    private final JMenuItem connect;
    private final JMenuItem disconnect;
    private final JMenuItem reload;
    private final JMenuItem createObject;
    private final JMenuItem editObject;
    private final JMenuItem deleteObject;
    private final JMenuItem duplicate;
    private final JMenuItem duplicateWithSource;
    private final JMenuItem delete;
    private final JMenuItem recycleConnection;
    private final JMenuItem copyName;
    private final JMenuItem moveToFolder;
    private final JMenuItem selectAll;
    private final JMenuItem selectAllChildren;

    private final JMenuItem recompileAll;
    private final JMenuItem recompileInvalid;
    private final JMenuItem reselectivityAllIndicies;
    private final JMenuItem reselectivityIndex;
    private final JMenuItem onlineTableValidation;

    private final JMenuItem dataBaseInformation;

    private JCheckBoxMenuItem showDefaultCatalogsAndSchemas;

    private JMenu active;
    private JMenu sqlTable;
    private JMenu sqlView;
    private JMenu exportData;
    private JMenu importData;

    private final BrowserTreePopupMenuActionListener listener;

    BrowserTreePopupMenu(BrowserTreePopupMenuActionListener listener) {

        this.listener = listener;

        connect = createMenuItem(bundleString("connect"), "connect", listener);
        add(connect);
        disconnect = createMenuItem(bundleString("disconnect"), "disconnect", listener);
        add(disconnect);
        reload = createMenuItem(bundleString("reload"), "reload", listener);
        add(reload);
        recycleConnection = createMenuItem(bundleString("recycle"), "recycle", listener);
        add(recycleConnection);
        createObject = createMenuItem(bundleString("create"), "createObject", listener);
        add(createObject);
        editObject = createMenuItem(bundleString("edit"), "editObject", listener);
        add(editObject);
        deleteObject = createMenuItem(bundleString("delete"), "deleteObject", listener);
        add(deleteObject);

        onlineTableValidation = createMenuItem(bundleString("onlineTableValidation"), "onlineTableValidation", listener);
        onlineTableValidation.setVisible(false);
        add(onlineTableValidation);

        addSeparator();

        /*showDefaultCatalogsAndSchemas = createCheckBoxMenuItem(
                bundleString("switchDefaultCatalogAndSchemaDisplay"),
                "switchDefaultCatalogAndSchemaDisplay", listener);
        add(showDefaultCatalogsAndSchemas);*/

        addNewConnection = createMenuItem(bundleString("addNewConnection"), "addNewConnection", listener);
        add(addNewConnection);
        getMetadata = createMenuItem(bundleString("getMetadata"), "getMetadata", listener);
        add(getMetadata);
        duplicate = createMenuItem(bundleString("duplicate"), "duplicate", listener);
        add(duplicate);
        duplicateWithSource = createMenuItem(bundleString("duplicateWithSource"), "duplicateWithSource", listener);
        add(duplicateWithSource);
        delete = createMenuItem(bundleString("delete"), "delete", listener);
        add(delete);

        copyName = createMenuItem(bundleString("copyName"), "copyName", listener);
        add(copyName);

        selectAll = createMenuItem(bundleString("selectAllTriggers"), "selectAll", listener);
        add(selectAll);

        selectAllChildren = createMenuItem(bundleString("selectAllChildren"), "selectAllChildren", listener);
        add(selectAllChildren);
        recompileAll = createMenuItem(bundleString("recompileAll"), "recompileAll", listener);
        recompileAll.setVisible(false);
        add(recompileAll);
        recompileInvalid = createMenuItem(bundleString("recompileInvalid"), "recompileInvalid", listener);
        recompileInvalid.setVisible(false);
        add(recompileInvalid);
        reselectivityAllIndicies = createMenuItem(bundleString("reselectivityAll"), "reselectivityAll", listener);
        reselectivityAllIndicies.setVisible(false);
        add(reselectivityAllIndicies);
        reselectivityIndex = createMenuItem(bundleString("reselectivity"), "reselectivity", listener);
        reselectivityIndex.setVisible(false);
        add(reselectivityIndex);
        //addSeparator();

        createActiveInactiveMenu(listener);
        createTableSqlMenu(listener);
        createViewSqlMenu(listener);
        createExportMenu(listener);
        createImportMenu(listener);
        moveToFolder = createMenuItem(bundleString("moveToFolder"), "moveToFolder", listener);
        add(moveToFolder);
        dataBaseInformation = createMenuItem("dataBaseInformation", "dataBaseInformation", listener);
        add(dataBaseInformation);
        //add(createMenuItem(bundleString("properties"), "properties", listener));
    }

    public void show(Component invoker, int x, int y) {
        setToConnect(!getCurrentSelection().isConnected());
        super.show(invoker, x, y);
    }

    private JMenuItem createMenuItem(String text,
                                     String actionCommand,
                                     ActionListener listener) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(text);
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(listener);
        return menuItem;
    }

    private JCheckBoxMenuItem createCheckBoxMenuItem(String text,
                                                     String actionCommand,
                                                     ActionListener listener) {

        JCheckBoxMenuItem menuItem = MenuItemFactory.createCheckBoxMenuItem(text);
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(listener);
        return menuItem;
    }

    private void setToConnect(boolean canConnect) {

        connect.setVisible(canConnect);
        disconnect.setVisible(!canConnect);
        getMetadata.setVisible(!canConnect);
        delete.setVisible(canConnect);
        reload.setVisible(!canConnect);

        String label = null;
        DefaultMutableTreeNode currentPathComponent = (DefaultMutableTreeNode) listener.getCurrentPathComponent();

        // check whether reload is available
        if (listener.hasCurrentPath()) {

            if (currentPathComponent instanceof DatabaseObjectNode) {

                DatabaseObjectNode node = asDatabaseObjectNode(currentPathComponent);

                //if (node.getUserObject() instanceof DatabaseHost) {

                if (node.isHostNode()) {

                    //reload.setEnabled(false);
                    selectAllChildren.setVisible(false);
                    selectAll.setVisible(false);
                    active.setVisible(false);
                    sqlTable.setVisible(false);
                    sqlView.setVisible(false);
                    exportData.setVisible(false);
                    importData.setVisible(false);

                    recycleConnection.setVisible(!canConnect);
                    deleteObject.setVisible(false);
                    createObject.setVisible(false);
                    editObject.setVisible(false);
                    moveToFolder.setVisible(true);
                    duplicate.setVisible(true);
                    onlineTableValidation.setVisible(false);
                    addNewConnection.setVisible(true);
                    dataBaseInformation.setVisible(true);
                } else {

                    label = node.getName();
                    if (node.getType() == NamedObject.META_TAG)
                        label = label.toLowerCase();
                    selectAllChildren.setVisible(false);
                    selectAll.setVisible(false);
                    disconnect.setVisible(false);
                    getMetadata.setVisible(false);
                    dataBaseInformation.setVisible(false);
                    reload.setVisible(true);
                    recycleConnection.setVisible(false);
                    moveToFolder.setVisible(false);
                    duplicate.setVisible(false);
                    addNewConnection.setVisible(false);
                    int type = node.getType();
                    boolean deleteObjectEnabled = type >= 0 && type < NamedObject.META_TYPES.length;
                    if (deleteObjectEnabled)
                        deleteObjectEnabled = !node.isSystem();
                    boolean createObjectEnabled = deleteObjectEnabled || type == NamedObject.META_TAG;
                    if (type == NamedObject.META_TAG)
                        createObjectEnabled = !NamedObject.META_TYPES[((DefaultDatabaseMetaTag) node.getDatabaseObject()).getSubType()].contains("SYSTEM");
                    deleteObject.setVisible(deleteObjectEnabled);
                    createObject.setVisible(createObjectEnabled);
                    editObject.setVisible(deleteObjectEnabled);
                    if (deleteObjectEnabled) {
                        deleteObject.setText(bundleString("delete", node.getName()));
                        editObject.setText(bundleString("edit", node.getName()));
                    }
                    if (createObjectEnabled) {
                        createObject.setText(bundleString("create", bundleString(getMetaTagFromNode(node))));
                    }
                    boolean recompileEnabled = false;
                    boolean reselectivityAll = false;
                    if (node.getType() == NamedObject.META_TAG) {
                        int nodeType = ((DefaultDatabaseMetaTag) node.getDatabaseObject()).getSubType();
                        boolean selectAllChildrenEnabled =
                                nodeType == NamedObject.TRIGGER ||
                                        nodeType == NamedObject.DDL_TRIGGER ||
                                        nodeType == NamedObject.DATABASE_TRIGGER ||
                                        nodeType == NamedObject.INDEX;
                        selectAllChildren.setVisible(selectAllChildrenEnabled);
                        selectAllChildren.setText(bundleString("selectAll", label));
                        recompileEnabled = nodeType == NamedObject.PROCEDURE
                                || nodeType == NamedObject.FUNCTION
                                || nodeType == NamedObject.PACKAGE
                                || nodeType == NamedObject.VIEW
                                || nodeType >= NamedObject.TRIGGER && nodeType <= NamedObject.DATABASE_TRIGGER;
                        reselectivityAll = nodeType == NamedObject.INDEX;
                    }


                    boolean isTable = (node.getType() == NamedObject.TABLE);
                    sqlTable.setVisible(isTable);
                    onlineTableValidation.setVisible(isTable);

                    boolean viewIsSelected = (node.getType() == NamedObject.VIEW);
                    sqlView.setVisible(viewIsSelected);

                    boolean triggerIndex = (node.getType() == NamedObject.TRIGGER ||
                            node.getType() == NamedObject.DATABASE_TRIGGER ||
                            node.getType() == NamedObject.DDL_TRIGGER ||
                            node.getType() == NamedObject.INDEX);
                    active.setVisible(triggerIndex);
                    selectAll.setVisible(triggerIndex);
                    if (triggerIndex) {
                        DatabaseObjectNode parent = (DatabaseObjectNode) node.getParent();
                        String parentName = parent.getName();
                        parentName = parentName.toLowerCase();
                        if (node.getType() == NamedObject.TRIGGER) {
                            selectAll.setText(bundleString("selectAll", parentName));
                        } else selectAll.setText(bundleString("selectAll", parentName));
                    }
                    reselectivityIndex.setVisible(node.getType() == NamedObject.INDEX);
                    reselectivityAll = node.getType() == NamedObject.INDEX || reselectivityAll;
                    reselectivityAllIndicies.setVisible(reselectivityAll);
                    recompileEnabled = node.getType() == NamedObject.PROCEDURE
                            || node.getType() == NamedObject.FUNCTION
                            || node.getType() == NamedObject.PACKAGE
                            || node.getType() == NamedObject.VIEW
                            || node.getType() >= NamedObject.TRIGGER && node.getType() <= NamedObject.DATABASE_TRIGGER
                            || recompileEnabled;
                    recompileAll.setVisible(recompileEnabled);
                    recompileInvalid.setVisible(recompileEnabled && (node.getType() != NamedObject.VIEW && node.getType() != NamedObject.META_TAG || (node.getType() == NamedObject.META_TAG && ((DefaultDatabaseMetaTag) node.getDatabaseObject()).getSubType() != NamedObject.VIEW)));
                    if (recompileEnabled) {
                        recompileAll.setText(bundleString("recompileAll", Bundles.get(NamedObject.class, getMetaTagFromNode(node))));
                        recompileInvalid.setText(bundleString("recompileInvalid", Bundles.get(NamedObject.class, getMetaTagFromNode(node))));
                    }
                }
            }
        }

        // re-label the menu items
        if (listener.hasCurrentSelection()) {

            String name = listener.getCurrentSelection().getName();
            connect.setText(bundleString("connectText", name));
            disconnect.setText(bundleString("disconnectText", name));
            dataBaseInformation.setText(bundleString("dataBaseInformation"));
            delete.setText(bundleString("deleteText", name));
            duplicate.setText(bundleString("duplicateText", name));

            // eeekkk...
            if (isCatalog(currentPathComponent) && asDatabaseCatalog(currentPathComponent).getHost().supportsCatalogsInTableDefinitions()) {

                duplicateWithSource.setVisible(true);
                duplicateWithSource.setText(bundleString("duplicateWithSourceText1", currentPathComponent.toString()));

            } else {

                duplicateWithSource.setText(bundleString("duplicateWithSourceText2"));
                duplicateWithSource.setVisible(false);
            }

            if (label != null) {
                reload.setText(bundleString("reload", label));              //Кнопка перезагрузки(Если нажать по узлу триггеров то покажет меню "перезагрузить триггеры")
            } else {
                reload.setText(bundleString("reload", StringUtils.EMPTY));
            }
        }
        if (listener.getSelectedSeveralPaths()) {

            addNewConnection.setVisible(false);
            connect.setVisible(false);
            disconnect.setVisible(false);
            getMetadata.setVisible(false);
            reload.setVisible(true);
            reload.setText(bundleString("reload", StringUtils.EMPTY));
            createObject.setVisible(false);
            editObject.setVisible(false);
            deleteObject.setVisible(false);
            duplicate.setVisible(false);
            recycleConnection.setVisible(false);
            copyName.setVisible(false);
            moveToFolder.setVisible(false);
            dataBaseInformation.setVisible(false);
            sqlTable.setVisible(false);
            sqlView.setVisible(false);
            exportData.setVisible(false);
            importData.setVisible(false);
        }
    }

    private String getMetaTagFromNode(DatabaseObjectNode node) {

        String str = "";
        if (node.getType() == NamedObject.META_TAG)
            str = NamedObject.META_TYPES_FOR_BUNDLE[((DefaultDatabaseMetaTag) node.getDatabaseObject()).getSubType()];
        else
            str = NamedObject.META_TYPES_FOR_BUNDLE[node.getType()];
        return str;
    }

    private DatabaseCatalog asDatabaseCatalog(DefaultMutableTreeNode currentPathComponent) {

        return (DatabaseCatalog) (asDatabaseObjectNode(currentPathComponent)).getDatabaseObject();
    }

    private DatabaseObjectNode asDatabaseObjectNode(DefaultMutableTreeNode currentPathComponent) {

        return (DatabaseObjectNode) currentPathComponent;
    }

    private boolean isCatalog(DefaultMutableTreeNode currentPathComponent) {

        return currentPathComponent instanceof DatabaseCatalogNode;
    }

    private void createImportMenu(ActionListener listener) {
        importData = MenuItemFactory.createMenu(bundleString("ImportData"));
        importData.add(createMenuItem(bundleString("importXml"), "importXml", listener));
        importData.add(createMenuItem(bundleString("importDelimited"), "importDelimited", listener));
        //add(importData);
    }

    private void createExportMenu(ActionListener listener) {
        exportData = MenuItemFactory.createMenu(bundleString("ExportData"));
        exportData.add(createMenuItem(bundleString("exportSQL"), "exportSQL", listener));
        exportData.add(createMenuItem(bundleString("exportXml"), "exportXml", listener));
        exportData.add(createMenuItem(bundleString("exportDbunit"), "exportDbunit", listener));
        exportData.add(createMenuItem(bundleString("exportDelimited"), "exportDelimited", listener));
        exportData.add(createMenuItem(bundleString("exportExcel"), "exportExcel", listener));
        //add(exportData);
    }

    private void createTableSqlMenu(ActionListener listener) {
        sqlTable = MenuItemFactory.createMenu(bundleString("SQL"));
        sqlTable.add(createMenuItem(bundleString("selectStatement"), "tableSelectStatement", listener));
        sqlTable.add(createMenuItem(bundleString("insertStatement"), "tableInsertStatement", listener));
        sqlTable.add(createMenuItem(bundleString("updateStatement"), "tableUpdateStatement", listener));
        sqlTable.add(createMenuItem(bundleString("createTableStatement"), "createTableStatement", listener));
        add(sqlTable);
    }

    private void createViewSqlMenu (ActionListener listener) {
        sqlView = MenuItemFactory.createMenu(bundleString("SQL"));
        sqlView.add(createMenuItem(bundleString("selectStatement"), "viewSelectStatement", listener));
        sqlView.add(createMenuItem(bundleString("insertStatement"), "viewInsertStatement", listener));
        sqlView.add(createMenuItem(bundleString("updateStatement"), "viewUpdateStatement", listener));
        sqlView.add(createMenuItem(bundleString("createViewStatement"), "createViewStatement", listener));
        add(sqlView);
    }

    private void createActiveInactiveMenu(ActionListener listener) {
        active = MenuItemFactory.createMenu(bundleString("switch"));
        active.add(createMenuItem(bundleString("active"), "active", listener));
        active.add(createMenuItem(bundleString("inactive"), "inactive", listener));
        add(active);
    }

    protected DatabaseConnection getCurrentSelection() {
        return listener.getCurrentSelection();
    }

    protected void setCurrentSelection(DatabaseConnection currentSelection) {
        listener.setCurrentSelection(currentSelection);
    }

    protected void setCurrentPath(TreePath currentPath) {
        listener.setCurrentPath(currentPath);
    }

    protected void setTreePaths(TreePath[] treePaths) {
        listener.setTreePaths(treePaths);
    }

    protected void setSelectedSeveralPaths(boolean selectedSeveralPaths) {
        listener.setSelectedSeveralPaths(selectedSeveralPaths);
    }

    protected boolean hasCurrentSelection() {
        return listener.hasCurrentSelection();
    }

    protected TreePath getCurrentPath() {
        return listener.getCurrentPath();
    }

    private String bundleString(String key, Object... args) {
        return Bundles.get(getClass(), key, args);
    }

    public BrowserTreePopupMenuActionListener getListener() {
        return listener;
    }
}
