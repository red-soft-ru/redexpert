/*
 * DefaultDatabaseView.java
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

package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.DatabaseView;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.util.List;

public class DefaultDatabaseView extends AbstractTableObject implements DatabaseView {

    public DefaultDatabaseView(DatabaseObject object) {

        this(object.getHost());

        setCatalogName(object.getCatalogName());
        setSchemaName(object.getSchemaName());
        setName(object.getName());
        setRemarks(object.getRemarks());
        setSource(object.getSource());
    }

    public DefaultDatabaseView(DatabaseHost host) {

        super(host, "VIEW");
    }

    public String getCreateSQLText() throws DataSourceException {

        String fields = null;

        try {

            List<DatabaseColumn> columns = getColumns();
            if (columns != null) {
                fields = "";

                for (int i = 0; i < columns.size(); i++) {
                    fields += " " + MiscUtils.getFormattedObject(columns.get(i).getName());
                    if (i != columns.size() - 1)
                        fields += ", ";
                }
            }

        } catch (Exception ignored) { }

        return SQLUtils.generateCreateView(getName(), fields, getSource(),
                getRemarks(), getDatabaseMajorVersion(), false);

    }

    public String getSelectSQLText() {

        String fields = "";

        try {

            List<DatabaseColumn> columns = getColumns();

            for (int i = 0, n = columns.size(); i < n; i++) {

                fields += columns.get(i).getName();
                if (i < n - 1)
                    fields += ", ";

            }

        } catch (DataSourceException e) {

            fields = "*";
            e.printStackTrace();
        }

        return SQLUtils.generateDefaultSelectStatement(getName(), fields);
    }

    public String getInsertSQLText() {

        String fields = "";
        String values = "";

        try {

            List<DatabaseColumn> columns = getColumns();

            for (int i = 0, n = columns.size(); i < n; i++) {

                fields += columns.get(i).getName();
                values += toCamelCase(columns.get(i).getName());

                if (i < n - 1) {

                    fields += ", ";
                    values += ", ";
                }

            }

        } catch (DataSourceException e) {

            fields = "_fields_";
            values = "_values_";
            e.printStackTrace();
        }

        return SQLUtils.generateDefaultInsertStatement(getName(), fields, values);

    }

    public String getUpdateSQLText() {

        String settings = "";

        try {

            List<DatabaseColumn> columns = getColumns();

            for (int i = 0, n = columns.size(); i < n; i++) {

                settings += columns.get(i).getName() + " = " +
                        toCamelCase(columns.get(i).getName());
                if (i < n - 1)
                    settings += ", ";

            }

        } catch (DataSourceException e) {

            settings = "_oldValue_ = _newValue_";
            e.printStackTrace();
        }

        return SQLUtils.generateDefaultUpdateStatement(getName(), settings);

    }

    @Override
    public boolean hasSQLDefinition() {

        return true;
    }

    public int getType() {
        if (isSystem()) {
            return SYSTEM_VIEW;
        } else {
            return VIEW;
        }
    }

    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    public boolean allowsChildren() {
        return true;
    }

}




