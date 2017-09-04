/*
 * DefaultDatabaseFunction.java
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

import org.executequery.databaseobjects.DatabaseFunction;
import org.executequery.databaseobjects.DatabaseMetaTag;

/**
 * Default database function implementation.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class DefaultDatabaseFunction extends DefaultDatabaseExecutable 
                                     implements DatabaseFunction {
    
    /** Creates a new instance of DefaultDatabaseFunction */
    public DefaultDatabaseFunction(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return FUNCTION;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

}












