/*
 * DefaultTableIndex.java
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
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.TableIndex;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author takisd
 */
public class DefaultTableIndex extends AbstractDatabaseObjectElement
        implements TableIndex {

    public int indexType;

    private List<DatabaseColumn> columns;

    private final DatabaseTable table;

    /**
     * Creates a new instance of DatabaseTableColumnIndex
     */
    public DefaultTableIndex(DatabaseTable table) {

        this.table = table;
    }

    public List<DatabaseColumn> getColumns() {

        return columns;
    }

    public void setColumns(List<DatabaseColumn> columns) {

        this.columns = columns;
    }

    public void clearColumns() {

        if (columns != null) {

            columns.clear();
        }
    }

    public void addColumn(DatabaseColumn column) {

        columns().add(column);
    }

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    public String getMetaDataKey() {

        return "INDEX";
    }

    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object
     */
    public NamedObject getParent() {

        return null;
    }

    /**
     * Does nothing.
     */
    public int drop() throws DataSourceException {

        return 0;
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    private List<DatabaseColumn> columns() {

        if (columns == null)
            columns = new ArrayList<>();

        return columns;
    }

    @Override
    public DatabaseTable getTable() {
        return table;
    }

    @Override
    public String getCreateSQLText() {
        return null;
    }

    private String formatName() {

        if (!MiscUtils.isNull(getName())) {

            return getName();
        }

        return "";
    }

    public int getIndexType() {

        return indexType;
    }

    public void setIndexType(int indexType) {

        this.indexType = indexType;
    }

    private String indexTypeSqlString() {

        switch (indexType) {
            case BITMAP_INDEX:
                return "BITMAP ";
            case UNIQUE_INDEX:
                return "UNIQUE ";
            default:
                return "";
        }
    }

    public Map<String, String> getMetaData() {
        return null;
    }

}


