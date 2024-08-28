/*
 * OpenEditorConnectionListener.java
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
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.ComponentPanel;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.editor.QueryEditorHistory;
import org.underworldlabs.util.SystemProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class OpenEditorConnectionListener implements ConnectionListener {

    @Override
    public void connected(ConnectionEvent connectionEvent) {
        DatabaseConnection connection = connectionEvent.getDatabaseConnection();

        List<QueryEditorHistory.PathNumber> listEditors = QueryEditorHistory.getEditors(connectionEvent.getDatabaseConnection());
        if (listEditors.isEmpty()) {
            if (openEditorOnConnect() && !connection.isAutoConnected()) {

                QueryEditor queryEditor;
                if (isQueryEditorTheCentralPanel() && queryEditor().getSelectedConnection() == null) {
                    queryEditor = queryEditor();

                } else {
                    queryEditor = new QueryEditor();

                    GUIUtilities.addCentralPane(
                            QueryEditor.TITLE,
                            QueryEditor.FRAME_ICON,
                            queryEditor,
                            null,
                            true
                    );
                }

                queryEditor.setSelectedConnection(connection);
                queryEditor.focusGained();
            }

        } else
            QueryEditorHistory.restoreTabs(connectionEvent.getDatabaseConnection());
    }

    @Override
    public void disconnected(ConnectionEvent connectionEvent) {

        Vector<String> closeTabs = new Vector<>();
        for (ComponentPanel panel : GUIUtilities.getOpenPanels()) {
            if (panel.getComponent() instanceof QueryEditor) {
                QueryEditor queryEditor = (QueryEditor) panel.getComponent();
                if (queryEditor.getSelectedConnection() == connectionEvent.getDatabaseConnection())
                    closeTabs.add(panel.getName());
            }
        }
        closeTabs.forEach(GUIUtilities::closeTab);

        List<QueryEditorHistory.PathNumber> copy = new ArrayList<>(QueryEditorHistory.getEditors(connectionEvent.getDatabaseConnection()));
        copy.forEach(number -> QueryEditorHistory.addEditor(connectionEvent.getDatabaseConnection().getId(), number));
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return event instanceof ConnectionEvent;
    }

    private boolean isQueryEditorTheCentralPanel() {
        return GUIUtilities.getSelectedCentralPane() instanceof QueryEditor;
    }

    private QueryEditor queryEditor() {
        return (QueryEditor) GUIUtilities.getSelectedCentralPane();
    }

    private boolean openEditorOnConnect() {
        return SystemProperties.getBooleanProperty("user", "editor.open.on-connect");
    }

}
