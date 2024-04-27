/*
 * ConnectionsTreeToolBar.java
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
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.toolbar.PanelToolBar;

import javax.swing.*;

/**
 * @author Takis Diakoumis
 */
class ConnectionsTreeToolBar extends PanelToolBar {

    private final ConnectionsTreePanel treePanel;

    private JButton upButton;
    private JButton downButton;
    private JButton reloadButton;
    private JButton connectButton;
    private JButton deleteConnectionButton;

    private ImageIcon connectedIcon;
    private ImageIcon disconnectedIcon;

    public ConnectionsTreeToolBar(ConnectionsTreePanel treePanel) {
        this.treePanel = treePanel;
        init();
    }

    private void init() {
        connectedIcon = GUIUtilities.loadIcon("Connected.png");
        disconnectedIcon = GUIUtilities.loadIcon("Disconnected.png");

        connectButton = addButton(
                treePanel,
                "connectDisconnect",
                GUIUtilities.getAbsoluteIconPath("Connected.png"),
                Bundles.get("action.connect-to-database-command")
        );

        addButton(
                treePanel,
                "connectAll",
                GUIUtilities.getAbsoluteIconPath("ConnectedAll.png"),
                bundleString("connectAll")
        );

        addButton(
                treePanel,
                "disconnectAll",
                GUIUtilities.getAbsoluteIconPath("DisconnectedAll.png"),
                bundleString("disconnectAll")
        );

        addSeparator();

        addButton(
                treePanel,
                "newFolder",
                GUIUtilities.getAbsoluteIconPath("NewFolder16.png"),
                bundleString("newFolder")
        );

        addButton(
                treePanel,
                "newConnection",
                GUIUtilities.getAbsoluteIconPath("NewConnection16.png"),
                Bundles.getCommon("newConnection.button")
        );

        addButton(
                treePanel,
                "newDatabase",
                GUIUtilities.getAbsoluteIconPath("create_database16.png"),
                Bundles.getCommon("newDatabase.button")
        );

        deleteConnectionButton = addButton(
                treePanel,
                "deleteConnection",
                GUIUtilities.getAbsoluteIconPath("Delete16.png"),
                Bundles.getCommon("delete.button")
        );

        addSeparator();

        reloadButton = addButton(
                treePanel,
                "reloadSelection",
                GUIUtilities.getAbsoluteIconPath("Refresh16.png"),
                bundleString("reloadSelection")
        );

        upButton = addButton(
                treePanel,
                "moveConnectionUp",
                GUIUtilities.getAbsoluteIconPath("Up16.png"),
                bundleString("moveConnectionUp")
        );

        downButton = addButton(
                treePanel,
                "moveConnectionDown",
                GUIUtilities.getAbsoluteIconPath("Down16.png"),
                bundleString("moveConnectionDown")
        );

        addButton(treePanel.getTreeFindAction());
    }

    protected void enableButtons(
            boolean enableUpButton, boolean enableDownButton, boolean enableReloadButton,
            boolean enableDeleteButton, boolean enableConnected, boolean databaseConnected) {

        upButton.setEnabled(enableUpButton);
        downButton.setEnabled(enableDownButton);
        connectButton.setEnabled(enableConnected);
        reloadButton.setEnabled(enableReloadButton);
        deleteConnectionButton.setEnabled(enableDeleteButton);

        if (enableConnected) {
            if (databaseConnected) {
                connectButton.setIcon(connectedIcon);
                connectButton.setToolTipText(Bundles.getCommon("disconnect.button"));
            } else {
                connectButton.setIcon(disconnectedIcon);
                connectButton.setToolTipText(Bundles.getCommon("connect.button"));
            }
        }
    }

    private static String bundleString(String key) {
        return Bundles.get(ConnectionsTreeToolBar.class, key);
    }

}
