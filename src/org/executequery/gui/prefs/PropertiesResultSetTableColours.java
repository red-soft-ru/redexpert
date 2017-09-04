/*
 * PropertiesResultSetTableColours.java
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
import java.util.Properties;

import org.underworldlabs.util.SystemProperties;

/**
 * The properties for the editor's results panel cell colours
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class PropertiesResultSetTableColours extends AbstractPropertiesColours {

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesResultSetTableColours() {
        
        super();
        try {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {

        List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                "Row and Cell Background Colours",
                null));

        String key = "results.table.cell.background.colour";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Default cell background",
                    SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.null.background.colour";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Null value cell background",
                    SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.char.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                "Character value cell background",
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.numeric.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                "Numeric value cell background",
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.date.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                "Date/time value cell background",
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.boolean.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                "Boolean value cell background",
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.blob.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                "Binary value cell background",
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.other.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                "Other value cell background",
                SystemProperties.getColourProperty("user", key)));

        key = "results.alternating.row.background";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                "Alternating row background",
                SystemProperties.getColourProperty("user", key)));
        
        UserPreference[] preferences = (UserPreference[])list.toArray(new UserPreference[list.size()]);
        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);

    }

    public void restoreDefaults() {
        
        Properties defaults = defaultsForTheme();
        UserPreference[] preferences = preferencesPanel.getPreferences();
        for (UserPreference userPreference : preferences) {
            
            if (userPreference.getType() == UserPreference.COLOUR_TYPE) {
             
                userPreference.reset(asColour(defaults.getProperty(userPreference.getKey())));
            }
            
        }
        
        preferencesPanel.fireTableDataChanged();
    }

    public void save() {

        preferencesPanel.savePreferences();
    }

}


