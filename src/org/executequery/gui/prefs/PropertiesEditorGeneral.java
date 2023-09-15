/*
 * PropertiesEditorGeneral.java
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
 * Query Editor general preferences panel.
 *
 * @author Takis Diakoumis
 */
public class PropertiesEditorGeneral extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesEditorGeneral() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Initializes the state of this instance.
     */
    private void init() throws Exception {

        List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("General"),
                null));

        String key = "editor.tabs.tospaces";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("ConvertTabsToSpaces"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.tab.spaces";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                1,
                key,
                bundledString("TabSize"),
                stringUserProperty(key)));

        key = "editor.autocomplete.only.hotkey";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("Auto-completeOnlyHotKey"),
                Boolean.valueOf(SystemProperties.getBooleanProperty("user", key))));

        key = "editor.autocomplete.keywords.on";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("Auto-completeKeywordsOn"),
                Boolean.valueOf(SystemProperties.getBooleanProperty("user", key))));

        key = "editor.autocomplete.schema.on";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("Auto-completeDatabaseObjectsOn"),
                Boolean.valueOf(SystemProperties.getBooleanProperty("user", key))));

        key = "editor.undo.count";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledString("UndoCount"),
                stringUserProperty(key)));

        key = "editor.history.count";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledString("HistoryCount"),
                stringUserProperty(key)));

        key = "editor.connection.commit";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("DefaultEditorAuto-commit"),
                Boolean.valueOf(SystemProperties.getBooleanProperty("user", key))));

        key = "editor.results.metadata";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("RetainResultSetMetaData"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.results.tabs.single";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("RecycleResultSetTabs"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.execute.remove.comments";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("RemoveCommentsForExecution"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.max.records";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                -1,
                key,
                bundledString("DefaultMaximumRowsReturned"),
                stringUserProperty(key)));

        key = "editor.logging.verbose";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("PrintAllSQLToOutputPanel"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.logging.enabled";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("LogOutputToFile"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.logging.path";
        list.add(new UserPreference(
                UserPreference.DIR_TYPE,
                key,
                bundledString("OutputLogFilePath"),
                stringUserProperty(key)));

        key = "editor.logging.backups";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                1,
                key,
                bundledString("MaximumRollingLogBackups"),
                stringUserProperty(key)));

        key = "editor.open.on-connect";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("OpenANewEditorForNewOpenConnection"),
                Boolean.valueOf(SystemProperties.getBooleanProperty("user", key))));


        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("Display"),
                null));

        key = "editor.display.statusbar";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("StatusBar"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.display.toolsPanel";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("ToolsPanel"),
                Boolean.valueOf(SystemProperties.getProperty("user", key))));

        key = "editor.display.linenums";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("LineNumbers"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.display.results";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("ResultsPanel"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.display.linehighlight";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("CurrentLineHighlight"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.display.margin";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("RightMargin"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "editor.margin.size";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledString("RightMarginColumn"),
                stringUserProperty(key)));

        key = "editor.margin.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("RightMarginColour"),
                SystemProperties.getColourProperty("user", key)));

        UserPreference[] preferences = list.toArray(new UserPreference[list.size()]);
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





