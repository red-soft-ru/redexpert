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

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseCatalog;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.nodes.DatabaseCatalogNode;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.menu.MenuItemFactory;

/**
 *
 * @author   Takis Diakoumis
 */
class BrowserTreePopupMenu extends JPopupMenu {

    private JMenuItem addNewConnection;
    private JMenuItem connect;
    private JMenuItem disconnect;
    private JMenuItem reload;
    private JMenuItem createObject;
    private JMenuItem deleteObject;
    private JMenuItem duplicate;
    private JMenuItem duplicateWithSource;
    private JMenuItem delete;
    private JMenuItem recycleConnection;
    private JMenuItem copyName;
    
    private JCheckBoxMenuItem showDefaultCatalogsAndSchemas; 

    private JMenu sql;
    private JMenu exportData;
    private JMenu importData;

    private BrowserTreePopupMenuActionListener listener;
    
    BrowserTreePopupMenu(BrowserTreePopupMenuActionListener listener) {

        this.listener = listener;
        
        connect = createMenuItem(bundleString("connect"), "connect", listener);
        add(connect);
        disconnect = createMenuItem(bundleString("disconnect"), "disconnect", listener);
        add(disconnect);

        addSeparator();

        reload = createMenuItem(bundleString("reload"), "reload", listener);
        add(reload);
        recycleConnection = createMenuItem(bundleString("recycle"), "recycle", listener);
        add(recycleConnection);

        addSeparator();

        createObject=createMenuItem(bundleString("create"),"createObject",listener);
        add(createObject);
        deleteObject=createMenuItem(bundleString("delete"),"deleteObject",listener);
        add(deleteObject);

        addSeparator();

        showDefaultCatalogsAndSchemas = createCheckBoxMenuItem(
                bundleString("switchDefaultCatalogAndSchemaDisplay"),
                "switchDefaultCatalogAndSchemaDisplay", listener);
        add(showDefaultCatalogsAndSchemas);

        addSeparator();

        addNewConnection = createMenuItem(bundleString("addNewConnection"), "addNewConnection", listener);
        add(addNewConnection);
        duplicate = createMenuItem(bundleString("duplicate"), "duplicate", listener);
        add(duplicate);
        duplicateWithSource = createMenuItem(bundleString("duplicateWithSource"), "duplicateWithSource", listener);
        add(duplicateWithSource);
        delete = createMenuItem(bundleString("delete"), "delete", listener);
        add(delete);

        addSeparator();

        copyName = createMenuItem(bundleString("copyName"), "copyName", listener);
        add(copyName);
        
        createSqlMenu(listener);
        createExportMenu(listener);
        createImportMenu(listener);

        addSeparator();
        add(createMenuItem(bundleString("moveToFolder"), "moveToFolder", listener));
        add(createMenuItem(bundleString("properties"), "properties", listener));
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

        connect.setEnabled(canConnect);
        disconnect.setEnabled(!canConnect);
        delete.setEnabled(canConnect);

        String label = null;
        DefaultMutableTreeNode currentPathComponent = (DefaultMutableTreeNode) listener.getCurrentPathComponent();

        // check whether reload is available
        if (listener.hasCurrentPath()) {

            if (currentPathComponent instanceof DatabaseObjectNode) {
                
                DatabaseObjectNode node = asDatabaseObjectNode(currentPathComponent);
                
                //if (node.getUserObject() instanceof DatabaseHost) {
                
                if (node.isHostNode()) {

                    //reload.setEnabled(false);
                    sql.setEnabled(false);
                    exportData.setEnabled(false);
                    importData.setEnabled(false);
                    
                    showDefaultCatalogsAndSchemas.setEnabled(true);
                    showDefaultCatalogsAndSchemas.setSelected(
                            ((DatabaseHostNode)node).isDefaultCatalogsAndSchemasOnly());
                    
                    recycleConnection.setEnabled(!canConnect);
                    deleteObject.setEnabled(false);
                    createObject.setEnabled(false);
                } 
                else {

                    label = node.toString();

                    reload.setEnabled(true);
                    recycleConnection.setEnabled(false);
                    showDefaultCatalogsAndSchemas.setEnabled(false);
                    int type=node.getType();
                    boolean deleteObjectEnabled=type>=0&&type<NamedObject.META_TYPES.length;
                    if(deleteObjectEnabled)
                        deleteObjectEnabled=!NamedObject.META_TYPES[type].contains("SYSTEM");
                    boolean createObjectEnabled=deleteObjectEnabled||type==NamedObject.META_TAG;
                    if(type==NamedObject.META_TAG)
                        createObjectEnabled=!node.getName().contains("SYSTEM");
                    deleteObject.setEnabled(deleteObjectEnabled);
                    createObject.setEnabled(createObjectEnabled);
                    if(deleteObjectEnabled)
                    {
                        deleteObject.setText(bundleString("delete")+" "+node.toString());
                    }
                    if(createObjectEnabled)
                    {
                        String str="";
                        if(type==NamedObject.META_TAG)
                            str=node.getName();
                        else
                            str=NamedObject.META_TYPES[node.getType()];
                        createObject.setText(bundleString("create")+" "+str);
                    }


                    boolean importExport = (node.getType() == NamedObject.TABLE);
                    sql.setEnabled(importExport);
                    exportData.setEnabled(importExport);
                    importData.setEnabled(importExport);
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
                
                duplicateWithSource.setEnabled(true);
                duplicateWithSource.setText(bundleString("duplicateWithSourceText1", currentPathComponent.toString()));

            } else {

                duplicateWithSource.setText(bundleString("duplicateWithSourceText2"));
                duplicateWithSource.setEnabled(false);
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

    private String bundleString(String key, Object...args) {
        return Bundles.get(getClass(), key, args);
    }

}
