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
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DefaultDatabaseView extends AbstractTableObject
        implements DatabaseView {

    public DefaultDatabaseView(DatabaseObject object) {

        this(object.getHost());
        metaTagParent = ((DefaultDatabaseObject) object).getMetaTagParent();
        setCatalogName(object.getCatalogName());
        setSchemaName(object.getSchemaName());
        setName(object.getName());
    }

    private List<String> fields;

    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String SOURCE = "VIEW_SOURCE";

    @Override
    protected String getFieldName() {
        return "RELATION_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$RELATIONS", "R");
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder();
        Table rels = getMainTable();
        Table rf = Table.createTable("RDB$RELATION_FIELDS", "RF");
        sb.appendFields(rels, getFieldName(), SOURCE, DESCRIPTION);
        sb.appendFields(rf, FIELD_NAME);
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(getObjectField(), Field.createField(rf, getFieldName())));
        sb.setOrdering(getObjectField().getFieldTable() + ", " + Field.createField(rf, FIELD_POSITION).getFieldTable());
        return sb;
    }

    @Override
    protected SelectBuilder builderForInfoAllObjects(SelectBuilder commonBuilder) {
        return super.builderForInfoAllObjects(commonBuilder).appendCondition(Condition.createCondition(Field.createField(getMainTable(), "VIEW_BLR"), "IS", "NOT NULL"));
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        if (first) {
            setRemarks(getFromResultSet(rs, DESCRIPTION));
            setSource(getFromResultSet(rs, SOURCE));
        }
        fields.add(rs.getString(FIELD_NAME).trim());
        return null;
    }

    @Override
    public void prepareLoadingInfo() {
        fields = new ArrayList<>();
    }

    @Override
    public void finishLoadingInfo() {

    }

    @Override
    public boolean isAnyRowsResultSet() {
        return true;
    }

    public DefaultDatabaseView(DatabaseHost host) {
        super(host, "VIEW");
    }

    @Override
    public String getCreateSQLText() throws DataSourceException {
        return SQLUtils.generateCreateView(getName(), getCreateFields(), getSource(),
                getRemarks(), getDatabaseMajorVersion(), false);
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("VIEW", getName());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        String comment = Comparer.isCommentsNeed() ? getRemarks() : null;
        return SQLUtils.generateCreateView(getName(), getCreateFields(), getSource(),
                comment, getDatabaseMajorVersion(), false);
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return (!this.getCompareCreateSQL().equals(databaseObject.getCompareCreateSQL())) ?
                databaseObject.getCompareCreateSQL() : "/* there are no changes */";
    }

    @Override
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

    @Override
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

    @Override
    public String getUpdateSQLText() {

        String settings = "";

        try {

            List<String> columns = getFields();
            for (int i = 0, n = columns.size(); i < n; i++) {

                settings += columns.get(i) + " = :" +
                        toCamelCase(columns.get(i));
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
    public boolean hasSQLDefinition() {
        return true;
    }

    public int getType() {
        return isSystem() ? SYSTEM_VIEW : VIEW;
    }

    private String getCreateFields() {

        String fields = null;

        try {

            List<String> columns = getFields();
            if (columns != null) {
                fields = "";

                for (int i = 0; i < columns.size(); i++) {
                    fields += MiscUtils.getFormattedObject(columns.get(i));
                    if (i != columns.size() - 1)
                        fields += ", ";
                }
            }

        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return fields;
    }

    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    public boolean allowsChildren() {
        return true;
    }

    public List<String> getFields() {
        checkOnReload(fields);
        return fields;
    }
}




