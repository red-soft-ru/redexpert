/*
 * PropertiesResultSetTableGeneral.java
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


import org.executequery.Constants;
import org.executequery.localization.Bundles;
import org.underworldlabs.util.SystemProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * The properties for the editor's results panel
 *
 * @author Takis Diakoumis
 */
public class PropertiesResultSetTableGeneral extends AbstractPropertiesBasePanel {

    public static final String[] ALIIGNS = {
            Bundles.get("preferences.allign.right"),
            Bundles.get("preferences.allign.left"),
            Bundles.get("preferences.allign.center")
    };

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesResultSetTableGeneral() {
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
                bundledString("ResultSetTable"),
                null));

        String key = "results.table.column.resize";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("ColumnsResizeable"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.column.reorder";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("ColumnReordering"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.row.numbers";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("RowNumberHeader"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.column.width";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledString("ColumnWidth"),
                SystemProperties.getProperty("user", key)));

        key = "results.table.column.height";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledString("ColumnHeight"),
                SystemProperties.getProperty("user", key)));

        key = "results.table.column.width.save";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("SaveColumnWidthStateBetweenQueries"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "results.date.pattern";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                -1,
                key,
                bundledString("DatePatternFormat"),
                stringUserProperty(key)));

        key = "results.time.pattern";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                -1,
                key,
                bundledString("TimePatternFormat"),
                stringUserProperty(key)));

        key = "results.timestamp.pattern";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                -1,
                key,
                bundledString("TimestampPatternFormat"),
                stringUserProperty(key)));

        key = "results.table.cell.null.text";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("NullValueCellText"),
                SystemProperties.getStringProperty("user", key)));

        key = "results.table.double-click.record.dialog";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("CellDouble-clickOpensDataItemViewer"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.single.row.transpose";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("TransposeWhenSingleRowResult"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.align.numeric";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("alignNumericValues"),
                alignUserProperty(key),
                ALIIGNS));

        key = "results.table.align.text";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("alignTextValues"),
                alignUserProperty(key),
                ALIIGNS));

        key = "results.table.align.bool";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("alignBoolValues"),
                alignUserProperty(key),
                ALIIGNS));

        key = "results.table.align.null";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("alignNullValues"),
                alignUserProperty(key),
                ALIIGNS));

        key = "results.table.align.other";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("alignOtherValues"),
                alignUserProperty(key),
                ALIIGNS));

        key = "results.table.use.form.adding.deleting";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("UseFormForAddingDeletingRecords"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.use.other.color.null";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("UseOtherColorForNullWhenAddingDeletingRecords"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.fetch.size";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                key,
                bundledString("FetchSize"),
                Integer.valueOf(stringUserProperty(key))));

        UserPreference[] preferences =
                list.toArray(new UserPreference[list.size()]);
        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);

    }

    protected String alignUserProperty(String key) {

        String property = SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, key);
        if (property != null && property.contains("default-"))
            property = Bundles.get("preferences.allign." + property.replace("default-", ""));

        return property;
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
