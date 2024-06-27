package org.executequery.gui.browser;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.menu.MenuItemFactory;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

class BrowserTreeHostPopupMenu extends JPopupMenu {

    private final List<JMenuItem> connectItems;
    private final List<JMenuItem> disconnectItems;
    private final BrowserTreePopupMenuActionListener listener;

    protected BrowserTreeHostPopupMenu(BrowserTreePopupMenuActionListener listener) {

        this.listener = listener;
        this.connectItems = new ArrayList<>();
        this.disconnectItems = new ArrayList<>();

        add(createMenuItem(bundleString("Connect"), "connect", listener, disconnectItems));
        add(createMenuItem(bundleString("Disconnect"), "disconnect", listener, connectItems));
        add(createMenuItem(bundleString("Reload"), "reloadPath", listener, connectItems));
        add(createMenuItem(bundleString("Reconnect"), "recycleConnection", listener, connectItems));
        addSeparator();

        add(createMenuItem(bundleString("NewFolder"), "newFolder", listener));
        add(createMenuItem(bundleString("NewConnection"), "newConnection", listener));
        addSeparator();

        add(createMenuItem(bundleString("ExtractMetadata"), "extractMetadata", listener, connectItems));
        add(createMenuItem(bundleString("MoveToFolder"), "moveToFolder", listener));
        add(createMenuItem(bundleString("Duplicate"), "duplicateConnection", listener));
        add(createMenuItem(bundleString("Delete"), "deleteConnection", listener, disconnectItems));
        addSeparator();

        add(createMenuItem(bundleString("CopyName"), "copyName", listener));
        add(createMenuItem(bundleString("ShowInformation"), "showConnectionInfo", listener));
    }

    protected void setCurrentPath(TreePath treePath) {
        listener.setCurrentPath(treePath);
    }

    protected void setConnection(DatabaseConnection connection) {
        if (connection != null) {
            listener.setCurrentSelection(connection);
            setConnected(connection.isConnected());
        }
    }

    private void setConnected(boolean connected) {
        connectItems.forEach(menuItem -> menuItem.setVisible(connected));
        disconnectItems.forEach(menuItem -> menuItem.setVisible(!connected));
    }

    private JMenuItem createMenuItem(String label, String command, ActionListener listener) {
        return createMenuItem(label, command, listener, null);
    }

    private JMenuItem createMenuItem(String label, String command, ActionListener listener, List<JMenuItem> list) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(label);
        menuItem.addActionListener(listener);
        menuItem.setActionCommand(command);

        if (list != null)
            list.add(menuItem);

        return menuItem;
    }

    private static String bundleString(String key) {
        return Bundles.get(BrowserTreeHostPopupMenu.class, key);
    }

}
