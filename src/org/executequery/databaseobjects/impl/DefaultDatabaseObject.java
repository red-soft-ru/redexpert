/*
 * DefaultDatabaseObject.java
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

import org.executequery.databaseobjects.*;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class DefaultDatabaseObject extends AbstractDatabaseObject {

    private int typeTree;

    private DatabaseObject dependObject;

    /** the metadata key name for this object */
    private String metaDataKey;

    /** Creates a new instance of DefaultDatabaseObject */
    public DefaultDatabaseObject(DatabaseHost host) {
        super(host);
    }

    @Override
    public String getCreateSQLText() throws DataSourceException {
        return null;
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return null;
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return null;
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return null;
    }

    /**
     * Creates a new instance of DefaultDatabaseObject
     */
    public DefaultDatabaseObject(DatabaseMetaTag metaTagParent, String metaDataKey) {
        this(metaTagParent.getHost(), metaDataKey);
        this.metaTagParent = metaTagParent;
    }

    public DefaultDatabaseObject(DatabaseHost host, String metaDataKey) {
        super(host);
        typeTree = TreePanel.DEFAULT;
        dependObject = null;
        this.metaDataKey = metaDataKey;
        setSystemFlag(false);
    }

    public int getTypeTree() {
        return typeTree;
    }

    public void setTypeTree(int typeTree) {
        this.typeTree = typeTree;
    }

    public DatabaseObject getDependObject() {
        return dependObject;
    }

    public void setDependObject(DatabaseObject dependObject) {
        this.dependObject = dependObject;
    }

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    public String getMetaDataKey() {
        return metaDataKey;
    }

    /**
     * Propagates the call to getColumns() for TABLE and
     * SYSTEM_TABLE types only.
     * All others will return a null list.
     */
    public List<NamedObject> getObjects() throws DataSourceException {

        if (getType() == SYSTEM_TABLE || getType() == TABLE) {

            List<DatabaseColumn> _columns = getColumns();
            if (_columns == null)
                return null;

            List<NamedObject> objects = new ArrayList<>(_columns.size());
            objects.addAll(_columns);

            return objects;
        }

        return null;
    }

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
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        Table table = getMainTable();
        sb.appendFields(table, getFieldName(), DESCRIPTION);
        sb.appendTable(table);
        sb.setOrdering(getObjectField().getFieldTable());
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        setRemarks(getFromResultSet(rs, DESCRIPTION));
        return null;
    }

    @Override
    public void prepareLoadingInfo() {

    }

    @Override
    public void finishLoadingInfo() {

    }

    @Override
    public boolean isAnyRowsResultSet() {
        return false;
    }

    @Override
    public boolean allowsChildren() {
        return getType() == NamedObject.SYSTEM_TABLE;
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {

        String key = getMetaDataKey();
        for (int i = 0; i < META_TYPES.length; i++)
            if (META_TYPES[i].equals(key))
                return i;

        // check if this a 'derivative object' -
        // i.e. a SYSTEM INDEX is still an INDEX
        for (int i = 0; i < META_TYPES.length; i++)
            if (MiscUtils.containsWholeWord(key, META_TYPES[i]))
                return i;

        // ...and if all else fails
        return OTHER;
    }

    protected String toCamelCase(String value) {

        String underscore = "_";
        String _value = value.replaceAll(" ", underscore);

        if (!_value.contains(underscore))
            return _value.toLowerCase();

        StringBuilder sb = new StringBuilder();

        String[] parts = _value.split(underscore);
        for (int i = 0; i < parts.length; i++)
            sb.append((i > 0) ? MiscUtils.firstLetterToUpper(parts[i].toLowerCase()) : parts[i].toLowerCase());

        return sb.toString();
    }

    protected String databaseProductName() {
        return getHost().getDatabaseProductName();
    }

}




