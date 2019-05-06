/*
 * PropertiesBrowserGeneral.java
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

import org.underworldlabs.util.SystemProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class PropertiesBrowserGeneral extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesBrowserGeneral() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {

        List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("General"),
                null));

        String key = "browser.catalog.schema.defaults.only";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("ShowOnlyDefaultCatalogOrSchema"),
                SystemProperties.getBooleanProperty("user", key)));

        key = "browser.double-click.to.connect";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("ConnectOnDouble-click"),
                SystemProperties.getBooleanProperty("user", key)));

        key = "browser.show.system.objects";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("ShowSystemObjects"),
                SystemProperties.getBooleanProperty("user", key)));

        key = "browser.search.in.columns";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("SearchInColumns"),
                SystemProperties.getBooleanProperty("user", key)));

        key = "browser.query.row.count";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("QueryForRowsCount"),
                SystemProperties.getBooleanProperty("user", key)));

        UserPreference[] preferences =
                list.toArray(new UserPreference[list.size()]);
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















