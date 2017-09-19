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

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.menu.MenuItemFactory;

public class BrowserTreeFolderPopupMenu extends JPopupMenu {

    public BrowserTreeFolderPopupMenu(ConnectionsTreePanel treePanel) {

        add(createMenuItem(bundleString("NewConnection"), "newConnection", treePanel));
        add(createMenuItem(bundleString("DeleteFolder"), "deleteConnection", treePanel));
        addSeparator();

        add(createMenuItem(bundleString("ConnectAll"), "connectAll", treePanel));
        add(createMenuItem(bundleString("DisconnectAll"), "disconnectAll", treePanel));

        addSeparator();
        add(createMenuItem(bundleString("SortConnections"), "sortConnections", treePanel));
    }

    private JMenuItem createMenuItem(String text,
                                     String actionCommand,
                                     ActionListener listener) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(text);
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(listener);
        return menuItem;
    }
    
    private String bundleString(String key) {
        
        return Bundles.get(getClass(),key);
    }

}

