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
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.TokenizingFormatter;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DefaultDatabaseView extends AbstractTableObject implements DatabaseView {

    public DefaultDatabaseView(DatabaseObject object) {

        this(object.getHost());

        setCatalogName(object.getCatalogName());
        setSchemaName(object.getSchemaName());
        setName(object.getName());
    }

    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String SOURCE = "SOURCE";

    protected String queryForInfo() {

        String query = "select r.rdb$description as "+DESCRIPTION+",\n" +
                "r.rdb$view_source as "+SOURCE+"\n"+
                "from rdb$relations r\n" +
                "where r.rdb$relation_name = ?";

        return query;
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) {
       try {
           if(rs.next())
           {
                setRemarks(getFromResultSet(rs,DESCRIPTION));
                setSource(getFromResultSet(rs,SOURCE));
           }
       } catch (Exception e)
       {
           e.printStackTrace();
       }

    }

    public DefaultDatabaseView(DatabaseHost host) {
        super(host, "VIEW");
    }

    public String getCreateSQLText() throws DataSourceException {
        return SQLUtils.generateCreateView(getName(), getCreateFields(), getSource(),
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

        return getFormatter().format(SQLUtils.generateDefaultSelectStatement(getName(), fields));
    }

    public String getInsertSQLText() {

        String fields = "";
        String values = "";

        try {

            List<DatabaseColumn> columns = getColumns();

            for (int i = 0, n = columns.size(); i < n; i++) {

                fields += columns.get(i).getName();
                values += ":" + toCamelCase(columns.get(i).getName());

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

        return getFormatter().format(SQLUtils.generateDefaultInsertStatement(getName(), fields, values));
    }

    public String getUpdateSQLText() {

        String settings = "";

        try {

            List<DatabaseColumn> columns = getColumns();

            for (int i = 0, n = columns.size(); i < n; i++) {

                settings += columns.get(i).getName() + " = :" +
                        toCamelCase(columns.get(i).getName());
                if (i < n - 1)
                    settings += ", ";

            }

        } catch (DataSourceException e) {
            settings = "_oldValue_ = _newValue_";
            e.printStackTrace();
        }

        return getFormatter().format(SQLUtils.generateDefaultUpdateStatement(getName(), settings));

    }

    TokenizingFormatter formatter;

    protected TokenizingFormatter getFormatter() {
        if (formatter == null)
            formatter = new TokenizingFormatter();
        return formatter;
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        String comment = Comparer.isCommentsNeed() ? getRemarks() : null;
        return SQLUtils.generateCreateView(getName(), getCreateFields(), getSource(),
                comment, getDatabaseMajorVersion(), false);
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropRequest("VIEW", getName());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return (!this.getCompareCreateSQL().equals(databaseObject.getCompareCreateSQL())) ?
                databaseObject.getCompareCreateSQL() : "/* there are no changes */";
    }

    @Override
    public boolean hasSQLDefinition() {
        return true;
    }

    public int getType() {
        return isSystem() ? SYSTEM_VIEW : VIEW;
    }

    private String getCreateFields() {

        String fields = null;

        try {

            List<DatabaseColumn> columns = getColumns();
            if (columns != null) {
                fields = "";

                for (int i = 0; i < columns.size(); i++) {
                    fields += MiscUtils.getFormattedObject(columns.get(i).getName());
                    if (i != columns.size() - 1)
                        fields += ", ";
                }
            }

        } catch (Exception ignored) {}

        return fields;
    }

    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    public boolean allowsChildren() {
        return true;
    }

}




