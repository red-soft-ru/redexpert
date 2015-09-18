/*
 * SimpleDatabaseObject.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

/**
 * Simple database object definition.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class SimpleDatabaseObject implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	/** the node type */
    private int type;

    /** the name of the associated schema */
    private String schemaName;
    
    // a 'parent' entity name - eg table name for a column
    private String parentName;

    /** the name of the associated catalog */
    private String catalogName;
    
    /** the meta data key identifier (@see BrowserConstants)*/
    private String metaDataKey;
    
    /** the name of this node */
    private String name;
    
    /** object remarks */
    private String remarks;
    
    /** Creates a new instance of SimpleDatabaseObject */
    public SimpleDatabaseObject() {}

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getMetaDataKey() {
        return metaDataKey;
    }

    public void setMetaDataKey(String metaDataKey) {
        this.metaDataKey = metaDataKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
}











