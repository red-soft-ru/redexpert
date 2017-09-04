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


import java.util.ArrayList;
import java.util.List;

import org.underworldlabs.util.SystemProperties;

/**
 * The properties for the editor's results panel
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class PropertiesResultSetTableGeneral extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesResultSetTableGeneral() {
        try  {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {

        List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                    UserPreference.CATEGORY_TYPE,
                    null,
                    "ResultSet Table",
                    null));

        String key = "results.table.column.resize";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Columns resizeable",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.column.reorder";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Column reordering",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.row.select";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Row selection",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.row.numbers";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Row number header",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.column.width";
        list.add(new UserPreference(
                    UserPreference.INTEGER_TYPE,
                    3,
                    key,
                    "Column width",
                    SystemProperties.getProperty("user", key)));

        key = "results.table.column.height";
        list.add(new UserPreference(
                    UserPreference.INTEGER_TYPE,
                    3,
                    key,
                    "Column Height",
                    SystemProperties.getProperty("user", key)));

        key = "results.table.column.width.save";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Save column width state between queries",
                Boolean.valueOf(stringUserProperty(key))));

        key = "resuts.date.pattern";
        list.add(new UserPreference(
                    UserPreference.STRING_TYPE,
                    -1,
                    key,
                    "Date pattern format",
                    stringUserProperty(key)));

        key = "results.table.cell.null.text";
        list.add(new UserPreference(
                    UserPreference.STRING_TYPE,
                    key,
                    "Null value cell text",
                    SystemProperties.getStringProperty("user", key)));

        key = "results.table.clob.length";
        list.add(new UserPreference(
                    UserPreference.INTEGER_TYPE,
                    5,
                    key,
                    "Max CLOB character length shown",
                    SystemProperties.getProperty("user", key)));

        key = "results.table.double-click.record.dialog";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Cell double-click opens data item viewer",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.single.row.transpose";
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Transpose when single row result",
                    Boolean.valueOf(stringUserProperty(key))));

        key = "results.table.right.align.numeric";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                "Right-align numeric values",
                Boolean.valueOf(stringUserProperty(key))));

        UserPreference[] preferences =
                (UserPreference[])list.toArray(new UserPreference[list.size()]);
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







