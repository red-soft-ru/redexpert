package org.executequery.gui.browser;

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.menu.MenuItemFactory;

import javax.swing.*;
import java.awt.event.ActionListener;

public class BrowserTreeDefaultPopupMenu extends JPopupMenu {

    public BrowserTreeDefaultPopupMenu(ConnectionsTreePanel treePanel) {

        add(createMenuItem(bundleString("NewFolder"), "newFolder", treePanel));
        add(createMenuItem(bundleString("NewConnection"), "newConnection", treePanel));
        addSeparator();

        add(createMenuItem(bundleString("ConnectAll"), "connectAll", treePanel));
        add(createMenuItem(bundleString("DisconnectAll"), "disconnectAll", treePanel));

        addSeparator();
        add(createMenuItem(bundleString("SearchNodes"), "searchNodes", treePanel));
        add(createMenuItem(bundleString("SortConnections"), "sortConnections", treePanel));
    }

    protected JMenuItem createMenuItem(String text, String command, ActionListener listener) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(text);
        menuItem.addActionListener(listener);
        menuItem.setActionCommand(command);

        return menuItem;
    }

    protected static String bundleString(String key) {
        return Bundles.get(BrowserTreeDefaultPopupMenu.class, key);
    }

}
