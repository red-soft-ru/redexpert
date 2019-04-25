/*
 * BaseDatabaseObject.java
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

package org.executequery.gui.browser;

import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;

import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class BaseDatabaseObject implements NamedObject {

    /**
     * the node type
     */
    private int type;

    /**
     * whether this a system type node
     */
    private boolean systemObject;

    /**
     * the name of the associated schema
     */
    private String schemaName;

    // a 'parent' entity name - eg table name for a column
    private String parentName;

    /**
     * the name of the associated catalog
     */
    private String catalogName;

    /**
     * the meta data key identifier (@see BrowserConstants)
     */
    private String metaDataKey;

    /**
     * the name of this node
     */
    private String name;

    /**
     * any remarks associated with this object
     */
    private String remarks;

    /**
     * whether this is the default connected catalog
     */
    private boolean defaultCatalog;

    private boolean useInQuery;

    private boolean isSystem;

    public BaseDatabaseObject() {
        useInQuery = true;
    }

    public BaseDatabaseObject(int type, String name) {
        useInQuery = true;
        this.name = name;
        this.type = type;
    }

    public int drop() {
        return 0;
    }

    public NamedObject getParent() {
        return null;
    }

    public void setParent(NamedObject parent) {
    }

    public List<NamedObject> getObjects() throws DataSourceException {
        return null;
    }

    public void reset() {
    }

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

    public String getDescription() {
        return getName();
    }

    public String getShortName() {
        return getName();
    }

    public String toString() {
        return name;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isDefaultCatalog() {
        return defaultCatalog;
    }

    public void setDefaultCatalog(boolean defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public boolean isUseInQuery() {
        return useInQuery;
    }

    public void setUseInQuery(boolean useInQuery) {
        this.useInQuery = useInQuery;
    }

    public boolean isSystemObject() {
        return systemObject;
    }

    public void setSystemObject(boolean systemObject) {
        this.systemObject = systemObject;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this object
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystemFlag(boolean flag) {
        isSystem = flag;
    }

    @Override
    public boolean allowsChildren() {
        return true;
    }

}















