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

import javax.swing.*;

/**
 * @author Takis Diakoumis
 */
class ConnectionsTreeToolBar {

    private final ConnectionsTreePanel treePanel;

    private JButton reloadButton;
    private JButton connectButton;

    private ImageIcon connectedIcon;
    private ImageIcon disconnectedIcon;

    public ConnectionsTreeToolBar(ConnectionsTreePanel treePanel) {
        this.treePanel = treePanel;
        init();
    }

    private void init() {
        connectedIcon = GUIUtilities.loadIcon("Connected.png");
        disconnectedIcon = GUIUtilities.loadIcon("Disconnected.png");

        connectButton = GUIUtilities.getToolBar().getButton("connect-to-database-command");
        if (connectButton != null) {
            connectButton.addActionListener(e -> treePanel.connectDisconnect());
            connectButton.setToolTipText(Bundles.getCommon("disconnect.button"));
            connectButton.setIcon(disconnectedIcon);
        }

        reloadButton = GUIUtilities.getToolBar().getButton("reload-connection-tree-selection-command");
        if (reloadButton != null)
            reloadButton.addActionListener(e -> treePanel.reloadSelection());

        JButton searchButton = GUIUtilities.getToolBar().getButton("search-connection-tree-node-command");
        if (searchButton != null) {
            searchButton.setAction(treePanel.getTreeFindAction());
            searchButton.setText(null);
        }
    }

    protected void enableButtons(boolean enableReloadButton, boolean enableConnected, boolean databaseConnected) {

        if (connectButton != null)
            connectButton.setEnabled(enableConnected);

        if (reloadButton != null)
            reloadButton.setEnabled(enableReloadButton);

        if (connectButton != null && enableConnected) {
            if (databaseConnected) {
                connectButton.setIcon(connectedIcon);
                connectButton.setToolTipText(Bundles.getCommon("disconnect.button"));
            } else {
                connectButton.setIcon(disconnectedIcon);
                connectButton.setToolTipText(Bundles.getCommon("connect.button"));
            }
        }
    }

}
