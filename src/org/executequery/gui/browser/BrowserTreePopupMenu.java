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

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
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
    private final BrowserTreePopupMenuActionListener listener;

    private final JMenuItem copyName;
    private final JMenuItem editObject;
    private final JMenuItem createObject;
    private final JMenuItem deleteObject;
    private final JMenuItem reloadObject;

    private final JMenuItem recompileAll;
    private final JMenuItem recompileInvalid;

    private final JMenuItem selectChildren;
    private final JMenuItem selectNeighbors;

    private final JMenuItem validateTable;
    private final JMenuItem refreshIndexStatistic;
    private final JMenuItem refreshAllIndexStatistic;

    private final JMenu activeMenu;
    private final JMenu sqlTableMenu;

    BrowserTreePopupMenu(BrowserTreePopupMenuActionListener listener) {
        this.listener = listener;

        copyName = createMenuItem(bundleString("copyName"), "copyName", listener);
        editObject = createMenuItem(bundleString("edit"), "editObject", listener);
        reloadObject = createMenuItem(bundleString("reload"), "reloadPath", listener);
        deleteObject = createMenuItem(bundleString("delete"), "deleteObject", listener);
        createObject = createMenuItem(bundleString("create"), "createObject", listener);
        recompileAll = createMenuItem(bundleString("recompileAll"), "recompileAll", listener);
        validateTable = createMenuItem(bundleString("validateTable"), "validateTable", listener);
        selectChildren = createMenuItem(bundleString("selectAll"), "selectAllChildren", listener);
        selectNeighbors = createMenuItem(bundleString("selectAll"), "selectAllNeighbors", listener);
        recompileInvalid = createMenuItem(bundleString("recompileInvalid"), "recompileInvalid", listener);
        refreshIndexStatistic = createMenuItem(bundleString("refreshIndexStatistic"), "refreshIndexStatistic", listener);
        refreshAllIndexStatistic = createMenuItem(bundleString("refreshAllIndexStatistic"), "refreshAllIndexStatistic", listener);

        activeMenu = MenuItemFactory.createMenu(bundleString("switch"));
        activeMenu.add(createMenuItem(bundleString("active"), "setActive", listener));
        activeMenu.add(createMenuItem(bundleString("inactive"), "setInactive", listener));

        sqlTableMenu = MenuItemFactory.createMenu(bundleString("SQL"));
        sqlTableMenu.add(createMenuItem(bundleString("selectStatement"), "generateSelectStatement", listener));
        sqlTableMenu.add(createMenuItem(bundleString("insertStatement"), "generateInsertStatement", listener));
        sqlTableMenu.add(createMenuItem(bundleString("updateStatement"), "generateUpdateStatement", listener));
        sqlTableMenu.add(createMenuItem(bundleString("createStatement"), "generateCreateStatement", listener));
    }

    private JMenuItem createMenuItem(String label, String command, ActionListener listener) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(label);
        menuItem.addActionListener(listener);
        menuItem.setActionCommand(command);

        return menuItem;
    }

    @Override
    public void show(Component invoker, int x, int y) {

        if (!listener.hasCurrentPath())
            return;

        removeAll();
        buildPopup();
        super.show(invoker, x, y);
    }

    private void buildPopup() {

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) listener.getCurrentPathComponent();
        if (!(treeNode instanceof DatabaseObjectNode))
            return;

        DatabaseObjectNode objectNode = (DatabaseObjectNode) treeNode;
        int nodeType = objectNode.getType();

        String labelMultiple = Bundles.get(NamedObject.class, getMetaTag(objectNode)).toLowerCase();
        String labelSingle = Bundles.get(BrowserTreePopupMenu.class, getMetaTag(objectNode)).toLowerCase();

        // --- system objects popup ---

        if (objectNode.isSystem()) {
            reloadObject.setText(bundleString("reload", nodeType == NamedObject.META_TAG ? labelMultiple : labelSingle));

            add(reloadObject);
            add(copyName);
            return;
        }

        // --- user objects catalog ---

        if (nodeType == NamedObject.META_TAG || NamedObject.isTableFolder(nodeType)) {
            int subType = getSubType(objectNode);

            boolean isTrigger = isTrigger(subType);
            boolean isView = subType == NamedObject.VIEW;
            boolean isIndex = subType == NamedObject.INDEX;
            boolean isExecutable = subType == NamedObject.PROCEDURE
                    || subType == NamedObject.FUNCTION
                    || subType == NamedObject.PACKAGE;

            // ---

            createObject.setText(bundleString("create", labelSingle));
            add(createObject);

            reloadObject.setText(bundleString("reload", labelMultiple));
            add(reloadObject);

            addSeparator();

            if (isTrigger || isIndex) {
                selectChildren.setText(bundleString("selectAll", labelMultiple));
                add(selectChildren);
            }

            if (isView || isExecutable || isTrigger) {
                recompileAll.setText(bundleString("recompileAll", labelMultiple));
                add(recompileAll);
            }

            if (isExecutable || isTrigger) {
                recompileInvalid.setText(bundleString("recompileInvalid", labelMultiple));
                add(recompileInvalid);
            }

            if (isIndex)
                add(refreshAllIndexStatistic);

            if (isView || isTrigger || isIndex || isExecutable)
                addSeparator();
            add(copyName);

            return;
        }

        // --- single user object ---

        boolean isTrigger = isTrigger(nodeType);
        boolean isView = nodeType == NamedObject.VIEW;
        boolean isTable = nodeType == NamedObject.TABLE;
        boolean isIndex = nodeType == NamedObject.INDEX;
        boolean isSeveralSelected = listener.isSelectedSeveralPaths();
        boolean isViewColumn = nodeType == NamedObject.TABLE_COLUMN
                && ((DatabaseObjectNode) objectNode.getParent()).getType() == NamedObject.VIEW;

        // ---

        if (!isViewColumn) {

            if (!isSeveralSelected) {
                createObject.setText(bundleString("create", labelSingle));
                add(createObject);
                addSeparator();
            }

            reloadObject.setText(bundleString("reload", isSeveralSelected ? labelMultiple : labelSingle));
            add(reloadObject);

            if (!isSeveralSelected) {
                editObject.setText(bundleString("edit", labelSingle));
                add(editObject);
            }

            if (!isSeveralSelected) {
                deleteObject.setText(bundleString("delete", labelSingle));
                add(deleteObject);
                addSeparator();
            }
        }

        if (isIndex)
            add(refreshIndexStatistic);

        if (isTable)
            add(validateTable);

        if (isTable || isIndex)
            addSeparator();

        if (!isSeveralSelected)
            add(copyName);

        if (isTrigger || isIndex) {
            selectNeighbors.setText(bundleString("selectAll", labelMultiple));
            add(selectNeighbors);
        }

        if ((isTable || isView) && !isSeveralSelected)
            add(sqlTableMenu);
        if (isTrigger || isIndex)
            add(activeMenu);
    }

    private boolean isTrigger(int type) {
        return type == NamedObject.TRIGGER
                || type == NamedObject.DATABASE_TRIGGER
                || type == NamedObject.DDL_TRIGGER;
    }

    private String getMetaTag(DatabaseObjectNode objectNode) {

        int metaType = objectNode.getType() == NamedObject.META_TAG ?
                getSubType(objectNode) :
                objectNode.getType();

        return NamedObject.getTypeForBundle(metaType);
    }

    private int getSubType(DatabaseObjectNode objectNode) {

        NamedObject namedObject = objectNode.getDatabaseObject();
        if (namedObject instanceof DefaultDatabaseMetaTag)
            return ((DefaultDatabaseMetaTag) namedObject).getSubType();

        return -1;
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

    public BrowserTreePopupMenuActionListener getListener() {
        return listener;
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(BrowserTreePopupMenu.class, key, args);
    }

}
