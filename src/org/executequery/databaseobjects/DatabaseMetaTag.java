/*
 * DatabaseMetaTag.java
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

/**
 * Defines a database meta tag object.
 * This type of object is really only a database object type identifier
 * as in TABLE, FUNCTION, VIEW etc...
 *
 * @author Takis Diakoumis
 */
public interface DatabaseMetaTag extends NamedObject {

    /**
     * Returns the db object with the specified name or null if
     * it does not exist.
     *
     * @param name the name of the object
     * @return the NamedObject or null if not found
     */
    NamedObject getNamedObject(String name) throws DataSourceException;

    /**
     * Returns the parent host object.
     *
     * @return the parent object
     */
    DatabaseHost getHost();

    /**
     * Returns the subtype indicator of this meta tag - the type this
     * meta tag ultimately represents.
     *
     * @return the subtype
     */
    int getSubType();

    void loadFullInfoForObjects();

    void loadColumnsForAllTables();

}
