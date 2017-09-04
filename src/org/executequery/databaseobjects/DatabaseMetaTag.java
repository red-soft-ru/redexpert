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
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public interface DatabaseMetaTag extends NamedObject {

    /**
     * Returns the db object with the specified name or null if 
     * it does not exist.
     *
     * @param name  the name of the object
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
     * Returns the parent catalog object.
     *
     * @return the parent catalog object
     */
    DatabaseCatalog getCatalog();

    /**
     * Returns the parent schema object.
     *
     * @return the parent schema object
     */
    DatabaseSchema getSchema();

    /**
     * Returns the sub-type indicator of this meta tag - the type this
     * meta tag ultimately represents.
     *
     * @return the sub-type
     */
    int getSubType();
    
    /**
     * Indicates whether child objects exist for this meta tag.
     * 
     * @return true | false
     * @throws DataSourceException
     */
    boolean hasChildObjects() throws DataSourceException;

}




