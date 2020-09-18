/*
 * DefaultDatabaseProcedure.java
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
import org.executequery.databaseobjects.DatabaseProcedure;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;

/**
 * Default database procedure implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultDatabaseProcedure extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    /**
     * Creates a new instance of DefaultDatabaseProcedure.
     */
    public DefaultDatabaseProcedure() {
    }


    /**
     * Creates a new instance of DefaultDatabaseProcedure
     */
    public DefaultDatabaseProcedure(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Creates a new instance of DefaultDatabaseProcedure with
     * the specified values.
     */
    public DefaultDatabaseProcedure(String schema, String name) {
        setName(name);
        setSchemaName(schema);
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return PROCEDURE;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name
     */


    public String getMetaDataKey() {
        return META_TYPES[PROCEDURE];
    }

    public String getCreateSQLText() {

        return SQLUtils.generateCreateProcedure(getName(), getParameters(), getProcedureSourceCode(), getRemarks(), getHost().getDatabaseConnection());
    }

    @Override
    protected String queryForInfo() {
        return "select rdb$description\n" +
                "from rdb$procedures \n" +
                "where rdb$procedure_name = '" + getName().trim() + "'";
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) {
        try {
            if (rs != null && rs.next()) {
                setRemarks(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}