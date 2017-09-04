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

import javax.swing.JPanel;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.editor.QueryEditor;
import org.underworldlabs.util.SystemProperties;

public class OpenEditorConnectionListener implements ConnectionListener {

    public void connected(ConnectionEvent connectionEvent) {

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

        // not interested
    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof ConnectionEvent);
    }

}





