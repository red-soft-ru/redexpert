/*
 * PropertiesEditorDisplay.java
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

package org.executequery.gui.prefs;


import java.util.ArrayList;
import java.util.List;

import org.underworldlabs.util.SystemProperties;

/** <p>The Query Editor properties panel.
 *
 *  @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class PropertiesEditorDisplay extends AbstractPropertiesBasePanel {
    
    private SimplePreferencesPanel preferencesPanel;
    
    public PropertiesEditorDisplay() {       
        try {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {
        
    	List<UserPreference> list = new ArrayList<UserPreference>();
    	
        String key = "editor.display.statusbar";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Status bar",
                Boolean.valueOf(SystemProperties.getProperty("user", key))));
        
        key = "editor.display.linenums";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Line numbers",
                Boolean.valueOf(SystemProperties.getProperty("user", key))));
        
        key = "editor.display.results";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Results panel",
                Boolean.valueOf(SystemProperties.getProperty("user", key))));

        key = "editor.display.linehighlight";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Current line highlight",
                Boolean.valueOf(SystemProperties.getProperty("user", key))));

        key = "editor.display.margin";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Right margin",
                Boolean.valueOf(SystemProperties.getProperty("user", key))));

        key = "editor.margin.size";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                "Right margin column",
                SystemProperties.getProperty("user", key)));

        key = "editor.margin.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                "Right margin colour",
                SystemProperties.getColourProperty("user", key)));

        UserPreference[] preferences = 
            (UserPreference[])list.toArray(new UserPreference[list.size()]);

        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);
        
    }

    public void restoreDefaults() {
        preferencesPanel.restoreDefaults();
    }
    
    public void save() {
        preferencesPanel.savePreferences();
    }
    
}











