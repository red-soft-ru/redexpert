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
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.localization.Bundles;
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
        SwingWorker worker = new SwingWorker("loadingTreeForSearch") {
            @Override
            public Object construct() {
                ConnectionsTreePanel panel = (ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
                DatabaseObjectNode hostNode = panel.getHostNode(connectionEvent.getDatabaseConnection());
                try {
                    populate(hostNode);
                } catch (DataSourceException e) {
                    if (e.wasConnectionClosed())
                        Log.info("Connection was closed");
                    else e.printStackTrace();
                }
                finally {
                    ((DefaultDatabaseHost)hostNode.getDatabaseObject()).releaseStatementForColumns();
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
                bundledString("activeConns", ConnectionManager.getActiveConnectionPoolCount())
        );
    }

    void populate(DatabaseObjectNode root) {
        if(root.getDatabaseObject() instanceof AbstractDatabaseObject)
            if(!((AbstractDatabaseObject)root.getDatabaseObject()).getHost().isConnected())
                return;
            root.populateChildren();
            Enumeration<TreeNode> nodes = root.children();
            while (nodes.hasMoreElements()) {
                DatabaseObjectNode node = (DatabaseObjectNode) nodes.nextElement();
                populate(node);
            }

    }

    private StatusBarPanel statusBar() {

        return GUIUtilities.getStatusBar();
    }

    private String bundledString(String key, Object... args) {
        return Bundles.get(this.getClass(), key, args);
    }

}





