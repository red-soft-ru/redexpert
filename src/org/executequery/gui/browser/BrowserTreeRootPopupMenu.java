/*
 * BrowserTreeRootPopupMenu.java
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

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.menu.MenuItemFactory;

public class BrowserTreeRootPopupMenu extends JPopupMenu {

    public BrowserTreeRootPopupMenu(ConnectionsTreePanel treePanel) {

        add(createMenuItem("New Folder", "newFolder", treePanel));
        add(createMenuItem("New Connection", "newConnection", treePanel));
        addSeparator();

        add(createMenuItem("Connect All", "connectAll", treePanel));
        add(createMenuItem("Disconnect All", "disconnectAll", treePanel));

        addSeparator();
        add(createMenuItem("Sort Connections", "sortConnections", treePanel));
        add(createMenuItem("Search Nodes...", "searchNodes", treePanel));

        addSeparator();
        add(createMenuItem("export-connections-command"));
        add(createMenuItem("import-connections-command"));
    }
    
    private JMenuItem createMenuItem(String actionId) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(ActionBuilder.get(actionId));
        menuItem.setIcon(null);

        return menuItem;
    }

    private JMenuItem createMenuItem(String text,
            String actionCommand,
            ActionListener listener) {
        
        JMenuItem menuItem = MenuItemFactory.createMenuItem(text);
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(listener);
        return menuItem;
    }
    
}

