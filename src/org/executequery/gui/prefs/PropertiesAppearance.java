/*
 * PropertiesAppearance.java
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

import org.executequery.plaf.LookAndFeelType;
import org.underworldlabs.util.LabelValuePair;
import org.underworldlabs.util.SystemProperties;

/**
 * System preferences appearance panel.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class PropertiesAppearance extends PropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;

    /** <p>Constructs a new instance. */
    public PropertiesAppearance() {
        try  {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Initializes the state of this instance. */
    private void init() throws Exception {

    	List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                    UserPreference.CATEGORY_TYPE,
                    null,
                    "General",
                    null));

        String key = "system.display.statusbar";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Status bar",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "system.display.console";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "System console",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "system.display.connections";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Connections",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "system.display.drivers";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Drivers",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "system.display.keywords";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "SQL Keywords",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "system.display.state-codes";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "SQL State Codes",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "system.display.systemprops";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "System properties palette",
                    Boolean.valueOf(stringUserProperty(key))));

        list.add(new UserPreference(
                    UserPreference.CATEGORY_TYPE,
                    null,
                    "Appearance",
                    null));

        key = "startup.display.lookandfeel";
        list.add(new UserPreference(
                    UserPreference.ENUM_TYPE,
                    key,
                    "Look and feel (requires restart)",
                    LookAndFeelType.valueOf(stringUserProperty(key)),
                    lookAndFeelValuePairs()));

        key = "display.aa.fonts";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Use anti-alias fonts (requires restart)",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "desktop.background.custom.colour";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Desktop background",
                    SystemProperties.getColourProperty("user", key)));

        key = "decorate.dialog.look";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Decorate dialogs",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "decorate.frame.look";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Decorate frame",
                    Boolean.valueOf(stringUserProperty(key))));

        UserPreference[] preferences =
                (UserPreference[])list.toArray(new UserPreference[list.size()]);
        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);

    }

    private Object[] lookAndFeelValuePairs() {

        LookAndFeelType[] lookAndFeelTypes = LookAndFeelType.values();
        LabelValuePair[] values = new LabelValuePair[lookAndFeelTypes.length];
        for (int i = 0; i < lookAndFeelTypes.length; i++) {
            
            LookAndFeelType lookAndFeelType = lookAndFeelTypes[i];
            values[i] = new LabelValuePair(lookAndFeelType, lookAndFeelType.getDescription());
        }
        
        return values;
    }

    public void restoreDefaults() {
        preferencesPanel.savePreferences();
    }

    public void save() {
        preferencesPanel.savePreferences();
    }

}







