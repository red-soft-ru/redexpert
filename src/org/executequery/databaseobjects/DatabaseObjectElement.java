/*
 * DatabaseObjectElement.java
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

import java.util.Map;

/**
 * @author takisd
 */
public interface DatabaseObjectElement extends NamedObject {

    /**
     * Returns any remarks attached to this object.
     *
     * @return database object remarks
     */
    String getRemarks();

    /**
     * Returns the metadata as a map of this column.
     *
     * @return the meta data
     */
    Map<String, String> getMetaData();


}
