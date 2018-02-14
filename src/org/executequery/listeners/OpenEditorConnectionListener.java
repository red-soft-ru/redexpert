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
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class OpenEditorConnectionListener implements ConnectionListener {

    public void connected(ConnectionEvent connectionEvent) {
        List<QueryEditorHistory.PathNumber> listEditors = QueryEditorHistory.getEditors(connectionEvent.getDatabaseConnection());
        if (listEditors == null || listEditors.isEmpty()) {
            if (openEditorOnConnect()) {

                QueryEditor queryEditor = null;
                DatabaseConnection databaseConnection = connectionEvent.getDatabaseConnection();

                if (isQueryEditorTheCentralPanel()) {

                    queryEditor = queryEditor();

                } else {

                    queryEditor = new QueryEditor();
                    GUIUtilities.addCentralPane(QueryEditor.TITLE,
                            QueryEditor.FRAME_ICON,
                            queryEditor,
                            null,
                            true);
                }

                queryEditor.setSelectedConnection(databaseConnection);
                queryEditor.focusGained();
            }
        } else {
            List<QueryEditorHistory.PathNumber> copy = new ArrayList<>();
            copy.addAll(listEditors);
            for (int i = 0; i < copy.size(); i++) {
                try {
                    QueryEditorHistory.removeEditor(connectionEvent.getDatabaseConnection().getName(), copy.get(i).path);
                    File file = new File(copy.get(i).path);
                    if (file.exists()) {
                        String contents = FileUtils.loadFile(file);
                        QueryEditor queryEditor = new QueryEditor(contents, copy.get(i).path);
                        GUIUtilities.addCentralPane(QueryEditor.TITLE,
                                QueryEditor.FRAME_ICON,
                                queryEditor,
                                null,
                                true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isQueryEditorTheCentralPanel() {

        JPanel panel = GUIUtilities.getSelectedCentralPane();
        return (panel instanceof QueryEditor);
    }

    private QueryEditor queryEditor() {

        return (QueryEditor) GUIUtilities.getSelectedCentralPane();
    }

    private boolean openEditorOnConnect() {

        return SystemProperties.getBooleanProperty("user", "editor.open.on-connect");
    }

    public void disconnected(ConnectionEvent connectionEvent) {
        List<ComponentPanel> panels = GUIUtilities.getOpenPanels();
        Vector<String> closeTabs = new Vector();
        for (int i = 0; i < panels.size(); i++) {
            if (panels.get(i).getComponent() instanceof QueryEditor) {
                QueryEditor queryEditor = (QueryEditor) panels.get(i).getComponent();
                if (queryEditor.getSelectedConnection() == connectionEvent.getDatabaseConnection())
                    closeTabs.add(panels.get(i).getName());
            }
        }
        List<QueryEditorHistory.PathNumber> copy = new ArrayList<>();
        copy.addAll(QueryEditorHistory.getEditors(connectionEvent.getDatabaseConnection()));
        for (String name : closeTabs)
            GUIUtilities.closeTab(name);
        for (QueryEditorHistory.PathNumber s : copy)
            QueryEditorHistory.addEditor(connectionEvent.getDatabaseConnection().getName(), s);
    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof ConnectionEvent);
    }

}





