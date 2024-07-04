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
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.SystemProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Takis Diakoumis
 */
public class PropertiesConnections extends AbstractPropertiesBasePanel {
    private SimplePreferencesPanel preferencesPanel;

    public PropertiesConnections(PropertiesPanel parent) {
        super(parent);
        init();
    }

    private void init() {

        String key;
        List<UserPreference> list = new ArrayList<>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledStaticString("General"),
                null
        ));

        key = "startup.connection.connect";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ConnectAtStartup"),
                Boolean.valueOf(SystemProperties.getProperty("user", key))
        ));

        key = "startup.connection.name";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledStaticString("StartupConnection"),
                SystemProperties.getProperty("user", key),
                connectionNames()
        ));

        key = "connection.connect.timeout";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                key,
                bundledStaticString("Connection.connect.timeout"),
                Integer.valueOf(Objects.requireNonNull(SystemProperties.getProperty("user", key)))
        ));

        key = "connection.shutdown.timeout";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                key,
                bundledStaticString("Connection.shutdown.timeout"),
                Integer.valueOf(Objects.requireNonNull(SystemProperties.getProperty("user", key)))
        ));

        key = "startup.default.connection.username";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledStaticString("ConnectAtStartup.username"),
                SystemProperties.getProperty("user", key)
        ));

        key = "startup.default.connection.password";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledStaticString("ConnectAtStartup.password"),
                SystemProperties.getProperty("user", key)
        ));

        key = "startup.default.connection.charset";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledStaticString("ConnectAtStartup.charset"),
                SystemProperties.getProperty("user", key),
                availableCharsets()
        ));

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledStaticString("TreeConnections"),
                null
        ));

        key = "treeconnection.row.height";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledStaticString("NodeHeight"),
                stringUserProperty(key)
        ));

        key = "browser.double-click.to.connect";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ConnectOnDouble-click"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "treeconnection.alphabet.sorting";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("AlphabetSorting"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "browser.show.system.objects";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ShowSystemObjects"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "browser.show.connection.properties";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ShowConnectionProperties"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "browser.show.connection.properties.advanced";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ShowAdvancedConnectionProperties"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "browser.search.in.columns";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("SearchInColumns"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "browser.query.row.count";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("QueryForRowsCount"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        preferencesPanel = new SimplePreferencesPanel(list.toArray(new UserPreference[0]));
        addContent(preferencesPanel);
    }

    private String[] connectionNames() {

        Repository repo = RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
        if (repo == null)
            return null;

        List<DatabaseConnection> connections = ((DatabaseConnectionRepository) repo).findAll();
        String[] connectionNames = new String[connections.size()];

        for (int i = 0; i < connectionNames.length; i++)
            connectionNames[i] = connections.get(i).getName();

        return connectionNames;
    }


    private String[] availableCharsets() {
        List<String> available = new ArrayList<>();

        try {
            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            for (String line : resource.split("\n"))
                if (!line.startsWith("#") && !line.isEmpty())
                    available.add(line);

            Collections.sort(available);
            available.add(0, "NONE");

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }

        return available.toArray(new String[0]);
    }

    @Override
    public void restoreDefaults() {
        preferencesPanel.restoreDefaults();
    }

    @Override
    public void save() {
        preferencesPanel.savePreferences();
    }

}
