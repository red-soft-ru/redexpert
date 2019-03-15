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


import org.underworldlabs.util.SystemProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The properties for the editor's results panel cell colours
 *
 * @author Takis Diakoumis
 */
public class PropertiesResultSetTableColours extends AbstractPropertiesColours {

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesResultSetTableColours() {

        super();
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
                bundledString("RowAndCellBackgroundColours"),
                null));

        String key = "results.table.cell.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("DefaultCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.null.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("NullValueCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.null.adding.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("NullValueCellBackgroundWhenThereIsAdding"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.null.deleting.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("NullValueCellBackgroundWhenThereIsDeleting"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.changed.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("ChangedValueCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.deleted.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("DeletedValueCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.new.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("NewValueCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.char.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("CharacterValueCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.numeric.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("NumericValueCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.date.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("DateTimeValueCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.boolean.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("BooleanValueCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.blob.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("BinaryValueCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.cell.other.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("OtherValueCellBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.table.focus.row.background.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("FocusRowBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "results.alternating.row.background";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("AlternatingRowBackground"),
                SystemProperties.getColourProperty("user", key)));

        UserPreference[] preferences = list.toArray(new UserPreference[list.size()]);
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


