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

    private JCheckBoxMenuItem showDefaultCatalogsAndSchemas;

    private JMenu sql;
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

        addSeparator();

        /*showDefaultCatalogsAndSchemas = createCheckBoxMenuItem(
                bundleString("switchDefaultCatalogAndSchemaDisplay"),
                "switchDefaultCatalogAndSchemaDisplay", listener);
        add(showDefaultCatalogsAndSchemas);*/

        addNewConnection = createMenuItem(bundleString("addNewConnection"), "addNewConnection", listener);
        add(addNewConnection);
        duplicate = createMenuItem(bundleString("duplicate"), "duplicate", listener);
        add(duplicate);
        duplicateWithSource = createMenuItem(bundleString("duplicateWithSource"), "duplicateWithSource", listener);
        add(duplicateWithSource);
        delete = createMenuItem(bundleString("delete"), "delete", listener);
        add(delete);

        copyName = createMenuItem(bundleString("copyName"), "copyName", listener);
        add(copyName);

        addSeparator();

        createSqlMenu(listener);
        createExportMenu(listener);
        createImportMenu(listener);
        moveToFolder = createMenuItem(bundleString("moveToFolder"), "moveToFolder", listener);
        add(moveToFolder);
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
                    sql.setVisible(false);
                    exportData.setVisible(false);
                    importData.setVisible(false);

                    recycleConnection.setVisible(!canConnect);
                    deleteObject.setVisible(false);
                    createObject.setVisible(false);
                    editObject.setVisible(false);
                } else {

                    label = node.toString();
                    disconnect.setVisible(false);
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
                        deleteObject.setText(bundleString("delete") + " " + node.toString());
                        editObject.setText(bundleString("edit") + " " + node.toString());
                    }
                    if (createObjectEnabled) {
                        String str = "";
                        if (type == NamedObject.META_TAG)
                            str = node.getMetaDataKey();
                        else
                            str = NamedObject.META_TYPES[node.getType()];
                        createObject.setText(bundleString("create") + " " + str);
                    }


                    boolean importExport = (node.getType() == NamedObject.TABLE);
                    sql.setVisible(importExport);
                }
            }
        }

        // re-label the menu items
        if (listener.hasCurrentSelection()) {

            String name = listener.getCurrentSelection().getName();
            connect.setText(bundleString("connectText", name));
            disconnect.setText(bundleString("disconnectText", name));
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
                reload.setText(bundleString("reload", label));
            } else {
                reload.setText(bundleString("reload", StringUtils.EMPTY));
            }

        }

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
        add(importData);
    }

    private void createExportMenu(ActionListener listener) {
        exportData = MenuItemFactory.createMenu(bundleString("ExportData"));
        exportData.add(createMenuItem(bundleString("exportSQL"), "exportSQL", listener));
        exportData.add(createMenuItem(bundleString("exportXml"), "exportXml", listener));
        exportData.add(createMenuItem(bundleString("exportDbunit"), "exportDbunit", listener));
        exportData.add(createMenuItem(bundleString("exportDelimited"), "exportDelimited", listener));
        exportData.add(createMenuItem(bundleString("exportExcel"), "exportExcel", listener));
        add(exportData);
    }

    private void createSqlMenu(ActionListener listener) {
        sql = MenuItemFactory.createMenu(bundleString("SQL"));
        sql.add(createMenuItem(bundleString("selectStatement"), "selectStatement", listener));
        sql.add(createMenuItem(bundleString("insertStatement"), "insertStatement", listener));
        sql.add(createMenuItem(bundleString("updateStatement"), "updateStatement", listener));
        sql.add(createMenuItem(bundleString("createTableStatement"), "createTableStatement", listener));
        add(sql);
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

    protected boolean hasCurrentSelection() {
        return listener.hasCurrentSelection();
    }

    protected TreePath getCurrentPath() {
        return listener.getCurrentPath();
    }

    private String bundleString(String key, Object... args) {
        return Bundles.get(getClass(), key, args);
    }

}
