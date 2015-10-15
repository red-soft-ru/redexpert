/*
 * PropertiesEditorGeneral.java
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
 * Query Editor general preferences panel.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1512 $
 * @date     $Date: 2015-09-27 21:23:07 +1000 (Sun, 27 Sep 2015) $
 */
public class PropertiesEditorGeneral extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesEditorGeneral() {
        try  {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** <p>Initializes the state of this instance. */
    private void init() throws Exception {

    	List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                    UserPreference.CATEGORY_TYPE,
                    null,
                    "General",
                    null));

        String key = "editor.tabs.tospaces";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Convert tabs to spaces",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "editor.tab.spaces";
        list.add(new UserPreference(
                    UserPreference.INTEGER_TYPE,
                    1,
                    key,
                    "Tab size",
                    stringUserProperty(key)));

        key = "editor.autocomplete.keywords.on";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Auto-complete keywords on",
                Boolean.valueOf(SystemProperties.getBooleanProperty("user", key))));

        key = "editor.autocomplete.schema.on";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Auto-complete database objects on",
                Boolean.valueOf(SystemProperties.getBooleanProperty("user", key))));

        key = "editor.undo.count";
        list.add(new UserPreference(
                    UserPreference.INTEGER_TYPE,
                    3,
                    key,
                    "Undo count",
                    stringUserProperty(key)));

        key = "editor.history.count";
        list.add(new UserPreference(
                    UserPreference.INTEGER_TYPE,
                    3,
                    key,
                    "History count",
                    stringUserProperty(key)));

        key = "editor.connection.commit";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Default editor auto-commit",
                    Boolean.valueOf(SystemProperties.getBooleanProperty("user", key))));

        key = "editor.results.metadata";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Retain result set meta data",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "editor.results.tabs.single";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Recycle result set tabs",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "editor.execute.remove.comments";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Remove comments for execution",
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.max.records";
        list.add(new UserPreference(
                    UserPreference.INTEGER_TYPE,
                    -1,
                    key,
                    "Default maximum rows returned",
                    stringUserProperty(key)));

        key = "editor.logging.verbose";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Print all SQL to output panel",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "editor.logging.enabled";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Log output to file",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "editor.logging.backups";
        list.add(new UserPreference(
                    UserPreference.INTEGER_TYPE,
                    1,
                    key,
                    "Maximum rolling log backups",
                    stringUserProperty(key)));

        key = "editor.logging.path";
        list.add(new UserPreference(
                    UserPreference.FILE_TYPE,
                    key,
                    "Output log file path",
                    stringUserProperty(key)));

        key = "editor.open.on-connect";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Open a new editor for new open connection",
                Boolean.valueOf(SystemProperties.getBooleanProperty("user", key))));

        
        list.add(new UserPreference(
                    UserPreference.CATEGORY_TYPE,
                    null,
                    "Display",
                    null));

        key = "editor.display.statusbar";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Status bar",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "editor.display.toolsPanel";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Tools panel",
                new Boolean(SystemProperties.getProperty("user", key))));

        key = "editor.display.linenums";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Line numbers",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "editor.display.results";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Results panel",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "editor.display.linehighlight";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Current line highlight",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "editor.display.margin";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Right margin",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "editor.margin.size";
        list.add(new UserPreference(
                    UserPreference.INTEGER_TYPE,
                    3,
                    key,
                    "Right margin column",
                    stringUserProperty(key)));

        key = "editor.margin.colour";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Right margin colour",
                    SystemProperties.getColourProperty("user", key)));

        UserPreference[] preferences = (UserPreference[])list.toArray(new UserPreference[list.size()]);
        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);

    }

    public void restoreDefaults() {
        preferencesPanel.restoreDefaults();
    }

    public String getName() {
        return getClass().getName();
    }

    public void save() {
        preferencesPanel.savePreferences();
    }

}




