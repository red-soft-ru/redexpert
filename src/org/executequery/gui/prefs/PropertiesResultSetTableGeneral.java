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
    private SimplePreferencesPanel preferencesPanel;

    public static final String[] ALIIGNS = {
            Bundles.get("preferences.allign.right"),
            Bundles.get("preferences.allign.left"),
            Bundles.get("preferences.allign.center")
    };

    public PropertiesResultSetTableGeneral() {
        init();
    }

    private void init() {

        String key;
        List<UserPreference> list = new ArrayList<>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledStaticString("ResultSetTable"),
                null
        ));

        key = "results.table.column.resize";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ColumnsResizeable"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "results.table.column.reorder";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("ColumnReordering"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "results.table.row.numbers";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("RowNumberHeader"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "results.table.column.width";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledStaticString("ColumnWidth"),
                SystemProperties.getProperty("user", key)
        ));

        key = "results.table.column.height";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledStaticString("ColumnHeight"),
                SystemProperties.getProperty("user", key)
        ));

        key = "results.table.column.width.save";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("SaveColumnWidthStateBetweenQueries"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "results.date.pattern";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                -1,
                key,
                bundledStaticString("DatePatternFormat"),
                stringUserProperty(key)
        ));

        key = "results.time.pattern";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                -1,
                key,
                bundledStaticString("TimePatternFormat"),
                stringUserProperty(key)
        ));

        key = "results.timestamp.pattern";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                -1,
                key,
                bundledStaticString("TimestampPatternFormat"),
                stringUserProperty(key)
        ));

        key = "results.time.timezone.pattern";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                -1,
                key,
                bundledStaticString("TimeTimezonePatternFormat"),
                stringUserProperty(key)
        ));

        key = "results.timestamp.timezone.pattern";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                -1,
                key,
                bundledStaticString("TimestampTimezonePatternFormat"),
                stringUserProperty(key)
        ));

        key = "results.table.cell.null.text";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledStaticString("NullValueCellText"),
                SystemProperties.getStringProperty("user", key)
        ));

        key = "results.table.double-click.record.dialog";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("CellDouble-clickOpensDataItemViewer"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "results.table.single.row.transpose";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("TransposeWhenSingleRowResult"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "results.table.align.numeric";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledStaticString("alignNumericValues"),
                alignUserProperty(key),
                ALIIGNS
        ));

        key = "results.table.align.text";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledStaticString("alignTextValues"),
                alignUserProperty(key),
                ALIIGNS
        ));

        key = "results.table.align.bool";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledStaticString("alignBoolValues"),
                alignUserProperty(key),
                ALIIGNS
        ));

        key = "results.table.align.null";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledStaticString("alignNullValues"),
                alignUserProperty(key),
                ALIIGNS
        ));

        key = "results.table.align.other";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledStaticString("alignOtherValues"),
                alignUserProperty(key),
                ALIIGNS
        ));

        key = "results.table.use.form.adding.deleting";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("UseFormForAddingDeletingRecords"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "results.table.use.other.color.null";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("UseOtherColorForNullWhenAddingDeletingRecords"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "results.table.fetch.size";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                key,
                bundledStaticString("FetchSize"),
                Integer.valueOf(stringUserProperty(key))
        ));

        key = "browser.max.records";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                key,
                bundledStaticString("MaximumRecordsReturned"),
                SystemProperties.getProperty("user", key)
        ));

        preferencesPanel = new SimplePreferencesPanel(list.toArray(new UserPreference[0]));
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
