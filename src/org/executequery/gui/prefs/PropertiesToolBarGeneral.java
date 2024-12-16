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
import org.underworldlabs.swing.toolbar.ToolBarWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The toolbar general properties panel.
 *
 * @author Takis Diakoumis
 */
public class PropertiesToolBarGeneral extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesToolBarGeneral(PropertiesPanel parent) {
        super(parent);
        init();
    }

    private void init() {
        List<UserPreference> list = new ArrayList<>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledStaticString("Visibility"),
                null
        ));

        String key = ToolBarManager.DATABASE_TOOLS;
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("DatabaseToolBar"),
                getToolBar(key).isVisible()
        ));

        key = ToolBarManager.APPLICATION_TOOLS;
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ApplicationToolBar"),
                getToolBar(key).isVisible()
        ));

        key = ToolBarManager.SYSTEM_TOOLS;
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("SystemToolBar"),
                getToolBar(key).isVisible()
        ));

        UserPreference[] preferences = list.toArray(new UserPreference[0]);
        preferencesPanel = new SimplePreferencesPanel(preferences, getClass());
        addContent(preferencesPanel);
    }

    private static ToolBarWrapper getToolBar(String toolbarName) {
        return Objects.requireNonNull(ToolBarProperties.getToolBar(toolbarName));
    }

    // --- UserPreferenceFunction impl ---

    @Override
    public void save() {
        for (UserPreference preference : preferencesPanel.getPreferences()) {
            if (preference.getType() != UserPreference.CATEGORY_TYPE) {
                boolean value = Boolean.parseBoolean(preference.getValue().toString());
                getToolBar(preference.getKey()).setVisible(value);
            }
        }
    }

    @Override
    public void restoreDefaults() {
        Arrays.stream(preferencesPanel.getPreferences()).forEach(pref -> pref.setValue(true, PropertiesToolBarGeneral.class));
        preferencesPanel.repaint();
    }

}
