/*
 * PropertiesConns.java
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


import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.SystemProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Takis Diakoumis
 */
public class PropertiesConns extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesConns() {
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

        String key = "startup.connection.connect";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("ConnectAtStartup"),
                Boolean.valueOf(SystemProperties.getProperty("user", key))));

        key = "startup.connection.name";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("StartupConnection"),
                SystemProperties.getProperty("user", key),
                connectionNames()));

        key = "connection.shutdown.timeout";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                key,
                bundledString("Connection.shutdown.timeout"),
                Integer.valueOf(SystemProperties.getProperty("user", key))));

        key = "connection.connect.timeout";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                key,
                bundledString("Connection.connect.timeout"),
                Integer.valueOf(SystemProperties.getProperty("user", key))));

        key = "startup.default.connection.username";
        list.add(new UserPreference(
            UserPreference.STRING_TYPE,
            key,
            bundledString("ConnectAtStartup.username"),
            SystemProperties.getProperty("user", key)
        ));
        key = "startup.default.connection.password";
        list.add(new UserPreference(
            UserPreference.STRING_TYPE,
            key,
            bundledString("ConnectAtStartup.password"),
            SystemProperties.getProperty("user", key)
        ));
        key = "startup.default.connection.charset";
        list.add(new UserPreference(
            UserPreference.STRING_TYPE,
            key,
            bundledString("ConnectAtStartup.charset"),
            SystemProperties.getProperty("user", key),
            availableCharsets()
        ));

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

    private String[] connectionNames() {

        List<DatabaseConnection> connections = connections();
        String[] connectionNames = new String[connections.size()];
        for (int i = 0; i < connectionNames.length; i++) {

            connectionNames[i] = connections.get(i).getName();
        }

        return connectionNames;
    }

    private List<DatabaseConnection> connections() {

        return ((DatabaseConnectionRepository) RepositoryCache.load(
                DatabaseConnectionRepository.REPOSITORY_ID)).findAll();
    }

    private String[] availableCharsets() {
        List<String> available = new ArrayList<>();
        try {
            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            String[] strings = resource.split("\n");
            for (String s : strings) {
                if (!s.startsWith("#") && !s.isEmpty())
                    available.add(s);
            }
            java.util.Collections.sort(available);
            available.add(0, "NONE");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return available.toArray(new String[0]);
    }

}






