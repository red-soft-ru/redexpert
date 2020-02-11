/*
 * DefaultConnectionListener.java
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

package org.executequery.listeners;

import org.executequery.GUIUtilities;
import org.executequery.components.StatusBarPanel;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.SystemProperties;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class DefaultConnectionListener implements ConnectionListener {
    private boolean searchInCols;
    public void connected(ConnectionEvent connectionEvent) {

        updateStatusBarDataSourceCounter();
        SwingWorker worker = new SwingWorker() {
            @Override
            public Object construct() {
                searchInCols = SystemProperties.getBooleanProperty("user", "browser.search.in.columns");
                ConnectionsTreePanel panel = (ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
                DatabaseObjectNode hostNode = panel.getHostNode(connectionEvent.getDatabaseConnection());
                try {
                    populate(hostNode);
                } catch (DataSourceException e) {
                    if (e.wasConnectionClosed())
                        Log.info("Connection was closed");
                    else e.printStackTrace();
                }
                return null;
            }
        };
        worker.start();
    }

    public void disconnected(ConnectionEvent connectionEvent) {

        updateStatusBarDataSourceCounter();
    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof ConnectionEvent);
    }

    private void updateStatusBarDataSourceCounter() {

        statusBar().setFirstLabelText(
                " Active Data Sources: " +
                        ConnectionManager.getActiveConnectionPoolCount());
    }

    void populate(DatabaseObjectNode root) {
        root.populateChildren();
        Enumeration<TreeNode> nodes = root.children();
        while (nodes.hasMoreElements()) {
            DatabaseObjectNode node = (DatabaseObjectNode) nodes.nextElement();
            if (!searchInCols) {
                if (node.getType() != NamedObject.SYSTEM_TABLE && node.getType() != NamedObject.TABLE && node.getType() != NamedObject.VIEW)
                    populate(node);
            } else populate(node);
        }
    }

    private StatusBarPanel statusBar() {

        return GUIUtilities.getStatusBar();
    }

}





