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

    public PropertiesEditorGeneral(PropertiesPanel parent) {
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

        key = "editor.autocomplete.only.hotkey";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("Auto-completeOnlyHotKey"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "editor.autocomplete.keywords.on";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("Auto-completeKeywordsOn"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "editor.autocomplete.objects.on";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("Auto-completeDatabaseObjectsOn"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "editor.connection.commit";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("DefaultEditorAuto-commit"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "editor.results.tabs.single";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("RecycleResultSetTabs"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.execute.remove.comments";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("RemoveCommentsForExecution"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.logging.verbose";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("PrintAllSQLToOutputPanel"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.plan.explained";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("PrintExplainedPlan"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.open.on-connect";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("OpenANewEditorForNewOpenConnection"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "editor.use.multiple.connections";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("UseMultipleConnections"),
                SystemProperties.getBooleanProperty("user", key)
        ));

        key = "editor.tabs.tospaces";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ConvertTabsToSpaces"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.tab.spaces";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                1,
                key,
                bundledStaticString("TabSize"),
                stringUserProperty(key)
        ));

        key = "editor.limit.records.count";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("LimitReturnedRowsCount"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.max.records.count";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                -1,
                key,
                bundledStaticString("DefaultMaximumRowsReturned"),
                stringUserProperty(key)
        ));

        key = "editor.undo.count";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledStaticString("UndoCount"),
                stringUserProperty(key)
        ));

        key = "editor.history.count";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledStaticString("HistoryCount"),
                stringUserProperty(key)
        ));

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledStaticString("Display"),
                null
        ));

        key = "editor.display.toolsPanel";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ToolsPanel"),
                Boolean.valueOf(SystemProperties.getProperty("user", key))
        ));

        key = "editor.display.transaction.params";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("TransactionParams"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.display.statusbar";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("StatusBar"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.display.linenums";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("LineNumbers"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.wrap.lines";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("WrapLines"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.display.linehighlight";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("CurrentLineHighlight"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        preferencesPanel = new SimplePreferencesPanel(list.toArray(new UserPreference[0]), getClass());
        addContent(preferencesPanel);
    }

    @Override
    public void save() {
        preferencesPanel.savePreferences();
    }

    @Override
    public void restoreDefaults() {
        preferencesPanel.restoreDefaults();
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

}
