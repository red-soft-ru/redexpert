/*
 * BrowserTreeFolderPopupMenu.java
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

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.menu.MenuItemFactory;

import javax.swing.*;
import java.awt.event.ActionListener;

public class BrowserTreeFolderPopupMenu extends JPopupMenu {

    public BrowserTreeFolderPopupMenu(ConnectionsTreePanel treePanel) {

        add(createMenuItem(bundleString("NewFolder"), "newFolder", treePanel));
        add(createMenuItem(bundleString("NewConnection"), "newConnection", treePanel));
        addSeparator();

        add(createMenuItem(bundleString("ConnectAll"), "connectAll", treePanel));
        add(createMenuItem(bundleString("DisconnectAll"), "disconnectAll", treePanel));

        addSeparator();
        add(createMenuItem(bundleString("SearchNodes"), "searchNodes", treePanel));
        add(createMenuItem(bundleString("SortConnections"), "sortConnections", treePanel));

        addSeparator();
        add(createMenuItem(bundleString("DeleteFolder"), "deleteConnection", treePanel));
    }

    private JMenuItem createMenuItem(String text, String command, ActionListener listener) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(text);
        menuItem.addActionListener(listener);
        menuItem.setActionCommand(command);

        return menuItem;
    }

    private String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

}
