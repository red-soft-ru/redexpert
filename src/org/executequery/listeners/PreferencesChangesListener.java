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

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.executequery.GUIUtilities;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;
import org.executequery.gui.SystemOutputPanel;
import org.executequery.gui.SystemPropertiesDockedTab;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.drivers.DriversTreePanel;
import org.executequery.gui.keywords.KeywordsDockedPanel;
import org.executequery.gui.sqlstates.SQLStateCodesDockedPanel;
import org.executequery.repository.UserLayoutProperties;

public class PreferencesChangesListener extends AbstractUserPreferenceListener
                                       implements UserPreferenceListener {

    private final UserLayoutProperties layoutProperties;
    
    public PreferencesChangesListener(UserLayoutProperties layoutProperties) {

        super();
        this.layoutProperties = layoutProperties;
    }

    public void preferencesChanged(UserPreferenceEvent event) {

        if (event.getEventType() == UserPreferenceEvent.ALL) {
            
            for (String key : dockedPanelKeysArray()) {

                layoutProperties.setDockedPaneVisible(
                       key, 
                       systemUserBooleanProperty(key), 
                       false);
            }

            applyComponentLookAndFeel();
            
            GUIUtilities.setDockedTabViews(true);
            
            layoutProperties.save();
            
        }
        
    }

    private String[] dockedPanelKeysArray() {

        return new String[] {
                ConnectionsTreePanel.PROPERTY_KEY,
                DriversTreePanel.PROPERTY_KEY,
                KeywordsDockedPanel.PROPERTY_KEY,
                SQLStateCodesDockedPanel.PROPERTY_KEY,
                SystemPropertiesDockedTab.PROPERTY_KEY,
                SystemOutputPanel.PROPERTY_KEY};
    }

    private void applyComponentLookAndFeel() {

        JDialog.setDefaultLookAndFeelDecorated(
                systemUserBooleanProperty("decorate.dialog.look"));

        JFrame.setDefaultLookAndFeelDecorated(
                systemUserBooleanProperty("decorate.frame.look"));
        
    }

}











