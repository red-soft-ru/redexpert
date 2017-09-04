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
 *
 * @author takisd
 */
public abstract class AbstractDatabaseObjectElement extends AbstractNamedObject implements DatabaseObjectElement {
    
    /** the catalog name */
    private String catalogName;
    
    /** the schema name */
    private String schemaName;

    /** the object's remarks */
    private String remarks;
    
    /**
     * Returns the catalog name parent to this database object.
     *
     * @return the catalog name
     */
    public String getCatalogName() {
        return catalogName;
    }
    
    /**
     * Sets the parent catalog name to that specified.
     *
     * @param catalog the catalog name
     */
    public void setCatalogName(String catalog) {
        this.catalogName = catalog;
    }
    
    /**
     * Returns the schema name parent to this database object.
     *
     * @return the schema name
     */
    public String getSchemaName() {
        return schemaName;
    }
    
    /**
     * Sets the parent schema name to that specified.
     *
     * @param schema the schema name
     */
    public void setSchemaName(String schema) {
        this.schemaName = schema;
    }

    /**
     * Returns any remarks attached to this object.
     *
     * @return database object remarks
     */
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
    public int getType() {
        return OTHER;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return "";
    }

}


