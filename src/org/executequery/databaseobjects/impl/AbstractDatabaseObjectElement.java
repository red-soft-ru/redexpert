/*
 * AbstractDatabaseObjectElement.java
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

import org.executequery.databaseobjects.DatabaseObjectElement;

/**
 * @author takisd
 */
public abstract class AbstractDatabaseObjectElement extends AbstractNamedObject implements DatabaseObjectElement {

    /**
     * the object's remarks
     */
    private String remarks;

    /**
     * Returns any remarks attached to this object.
     *
     * @return database object remarks
     */
    @Override
    public String getRemarks() {
        return remarks;
    }

    /**
     * Sets the remarks to that specified.
     *
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    @Override
    public int getType() {
        return OTHER;
    }

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    @Override
    public String getMetaDataKey() {
        return "";
    }

}
