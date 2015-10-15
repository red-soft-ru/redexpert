/*
 * PropertiesBrowserTableData.java
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

import java.util.ArrayList;
import java.util.List;

import org.underworldlabs.util.SystemProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision$
 * @date     $Date$
 */
public class PropertiesBrowserTableData extends AbstractPropertiesBasePanel {
    
    private SimplePreferencesPanel preferencesPanel;
    
    public PropertiesBrowserTableData() {
        try  {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() {

        List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                    UserPreference.CATEGORY_TYPE,
                    null,
                    "Database table data view tab",
                    null));

        String key = "browser.max.records";
        list.add(new UserPreference(
                    UserPreference.INTEGER_TYPE,
                    key,
                    "Maximum records returned",
                    SystemProperties.getProperty("user", key)));

        key = "browser.always.show.table.editable.label";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Indicate when table data may be edited in-place",
                    SystemProperties.getBooleanProperty("user", key)));

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

