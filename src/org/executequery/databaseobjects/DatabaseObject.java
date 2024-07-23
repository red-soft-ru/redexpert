/*
 * DatabaseObject.java
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

package org.executequery.databaseobjects;

import org.underworldlabs.jdbc.DataSourceException;

import java.sql.ResultSet;
import java.util.List;

/**
 * Defines a real database object - ie. a table, procedure,
 * function, index, etc.
 *
 * @author Takis Diakoumis
 */
public interface DatabaseObject extends NamedObject {

    void setHost(DatabaseHost host);

    /**
     * Returns the parent host object.
     *
     * @return the parent object
     */
    DatabaseHost getHost();

    /**
     * Returns the name prefix for this object
     */
    String getNamePrefix();

    /**
     * Returns the columns (if any) of this object.
     *
     * @return the columns
     */
    List<DatabaseColumn> getColumns() throws DataSourceException;

    /**
     * Returns the privileges (if any) of this object.
     *
     * @return the privileges
     */
    List<TablePrivilege> getPrivileges() throws DataSourceException;

    /**
     * Returns any remarks attached to this object.
     *
     * @return database object remarks
     */
    String getRemarks();

    /**
     * Setting remarks attached to this object.
     */
    void setRemarks(String description);

    /**
     * Sets the parent object to that specified.
     */
    void setParent(NamedObject parent);

    /**
     * Retrieves the data row count for this object (where applicable).
     *
     * @return the data row count for this object
     */
    int getDataRowCount() throws DataSourceException;

    /**
     * Retrieves the data for this object (where applicable).
     *
     * @return the data for this object
     */
    ResultSet getData() throws DataSourceException;

    /**
     * Cancels any open running statement against this object.
     */
    void cancelStatement();

    String getNameForQuery();

    ResultSet getData(boolean rollbackOnError) throws DataSourceException;

    void releaseResources();

    boolean hasSQLDefinition();

    ResultSet getMetaData() throws DataSourceException;

    String getCreateSQLText() throws DataSourceException;

    String getSource();

}
