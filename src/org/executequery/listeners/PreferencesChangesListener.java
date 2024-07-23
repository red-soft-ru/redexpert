/*
 * PreferencesChangesListener.java
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
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;
import org.executequery.gui.SystemOutputPanel;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.repository.UserLayoutProperties;

import javax.swing.*;

public class PreferencesChangesListener extends AbstractUserPreferenceListener
        implements UserPreferenceListener {

    private final UserLayoutProperties layoutProperties;

    public PreferencesChangesListener(UserLayoutProperties layoutProperties) {
        super();
        this.layoutProperties = layoutProperties;
    }

    @Override
    public void preferencesChanged(UserPreferenceEvent event) {

        if (event.getEventType() != UserPreferenceEvent.ALL)
            return;

        for (String key : dockedPanelKeysArray()) {
            layoutProperties.setDockedPaneVisible(
                    key,
                    systemUserBooleanProperty(key),
                    false
            );
        }

        JPanel component = GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        if (component instanceof ConnectionsTreePanel) {
            ConnectionsTreePanel panel = (ConnectionsTreePanel) component;
            panel.setPropertiesPanelVisible(systemUserBooleanProperty(ConnectionsTreePanel.CONNECTION_PROPERTIES_KEY));
            panel.reloadOpenedConnections();
        }

        GUIUtilities.setDockedTabViews(true);

        layoutProperties.save();
    }

    private String[] dockedPanelKeysArray() {
        return new String[]{
                ConnectionsTreePanel.PROPERTY_KEY,
                SystemOutputPanel.PROPERTY_KEY
        };
    }

}
