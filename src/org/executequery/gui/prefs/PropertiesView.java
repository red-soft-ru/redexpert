/*
 * PropertiesView.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.executequery.gui.prefs;


import org.underworldlabs.util.SystemProperties;

// **************************************************
// **************************************************
// DO NOT USE 
// **************************************************
// **************************************************
// **************************************************
// **************************************************
// **************************************************

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/** <p>The view properties panel.
 *
 *  @author   Takis Diakoumis
 * @version  $Revision: 1512 $
 * @date     $Date: 2015-09-27 21:23:07 +1000 (Sun, 27 Sep 2015) $
 */
public class PropertiesView extends AbstractPropertiesBasePanel {
    
    private SimplePreferencesPanel preferencesPanel;
    
    /** <p>Constructs a new instance. */
    public PropertiesView() {
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** <p>Initializes the state of this instance. */
    private void jbInit() throws Exception {

        int count = 0;
        UserPreference[] preferences = new UserPreference[5];

        String key = "system.display.statusbar";
        preferences[count++] = new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Status bar",
                new Boolean(SystemProperties.getProperty("user", key)));

        key = "system.display.console";
        preferences[count++] = new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "System console",
                new Boolean(SystemProperties.getProperty("user", key)));

        key = "system.display.connections";
        preferences[count++] = new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Connections",
                new Boolean(SystemProperties.getProperty("user", key)));

        key = "system.display.drivers";
        preferences[count++] = new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Drivers",
                new Boolean(SystemProperties.getProperty("user", key)));

        key = "system.display.systemprops";
        preferences[count++] = new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "System properties palette",
                new Boolean(SystemProperties.getProperty("user", key)));

        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);

    }

    public void restoreDefaults() {
        preferencesPanel.savePreferences();
    }
    
    public void save() {
        preferencesPanel.savePreferences();
    }
    
}














