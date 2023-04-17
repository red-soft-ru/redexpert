/*
 * DefaultSystemFunctionMetaTag.java
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

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.SystemFunctionMetaTag;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Default system function meta tag object implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultSystemFunctionMetaTag extends AbstractDatabaseObject
        implements SystemFunctionMetaTag {

    /**
     * the system function type identifier
     */
    private final int type;

    /**
     * Creates a new instance of DefaultSystemFunctionMetaTag
     */
    public DefaultSystemFunctionMetaTag(DatabaseMetaTag metaTagParent, int type, String name) {
        super(metaTagParent, name);
        this.type = type;
    }

    /**
     * Retrieves child objects classified as this tag type.
     *
     * @return this meta tag's child database objects.
     */
    @Override
    public List<NamedObject> getObjects() throws DataSourceException {

        String functions = null;
        int type = getType();
        DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

        try {
            switch (type) {
                case SYSTEM_STRING_FUNCTIONS:
                    functions = dmd.getStringFunctions();
                    break;
                case SYSTEM_DATE_TIME_FUNCTIONS:
                    functions = dmd.getTimeDateFunctions();
                    break;
                case SYSTEM_NUMERIC_FUNCTIONS:
                    functions = dmd.getNumericFunctions();
                    break;
            }

        } catch (SQLException e) {
            throw new DataSourceException(e);
        }

        List<NamedObject> objects = null;
        if (!MiscUtils.isNull(functions)) {
            String[] _functions = MiscUtils.splitSeparatedValues(functions, ",");
            DatabaseMetaTag parent = getMetaTagParent();
            objects = new ArrayList<>(_functions.length);
            for (String function : _functions)
                objects.add(new SystemDatabaseFunction(parent, function, type));
        }

        return objects;
    }

    @Override
    public boolean allowsChildren() {
        return true;
    }

    /**
     * Returns the parent meta tag object.
     *
     * @return the parent meta tag
     */
    @Override
    public DatabaseMetaTag getMetaTagParent() {
        return metaTagParent;
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[getType()];
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

    @Override
    protected String getFieldName() {
        return null;
    }

    @Override
    protected Table getMainTable() {
        return null;
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        return null;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
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
}


