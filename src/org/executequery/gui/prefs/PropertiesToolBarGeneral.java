/*
 * PropertiesToolBarGeneral.java
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


import org.executequery.toolbars.ToolBarManager;
import org.underworldlabs.swing.toolbar.ToolBarProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the
 *           release of version 3.0.0beta1 has meant a
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * <p>The tool bar general properties panel.
 *
 * @author Takis Diakoumis
 */
public class PropertiesToolBarGeneral extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;

    /**
     * <p>Constructs a new instance.
     */
    public PropertiesToolBarGeneral(PropertiesPanel parent) {
        super(parent);
        init();
    }

    /**
     * <p>Initializes the state of this instance.
     */
    private void init() {

        List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledStaticString("Visibility"),
                null));

        String key = ToolBarManager.DATABASE_TOOLS;
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("DatabaseToolBar"),
                Boolean.valueOf(ToolBarProperties.getToolBar(key).isVisible())));

        key = ToolBarManager.APPLICATION_TOOLS;
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ApplicationToolBar"),
                Objects.requireNonNull(ToolBarProperties.getToolBar(key)).isVisible()));

        key = ToolBarManager.SYSTEM_TOOLS;
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("SystemToolBar"),
                Boolean.valueOf(ToolBarProperties.getToolBar(key).isVisible())));

        UserPreference[] preferences =
                list.toArray(new UserPreference[list.size()]);
        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);
    }

    public void restoreDefaults() {
        preferencesPanel.restoreDefaults();
    }

    public void save() {
        UserPreference[] preferences = preferencesPanel.getPreferences();
        for (int i = 0; i < preferences.length; i++) {
            if (preferences[i].getType() != UserPreference.CATEGORY_TYPE) {
                boolean value = Boolean.valueOf(
                        preferences[i].getValue().toString()).booleanValue();
                ToolBarProperties.getToolBar(
                        preferences[i].getKey()).setVisible(value);
            }
        }
    }

}















