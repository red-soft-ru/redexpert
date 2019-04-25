/*
 * DefaultDatabaseCatalog.java
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

import org.executequery.databaseobjects.*;
import org.underworldlabs.jdbc.DataSourceException;

import java.util.List;

/**
 * Default database catalog object implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultDatabaseCatalog extends AbstractDatabaseSource
        implements DatabaseCatalog {

    /**
     * the schemas for this catalog
     */
    private List<DatabaseSchema> schemas;

    /**
     * Creates a new instance of DefaultDatabaseCatalog
     */
    public DefaultDatabaseCatalog(DatabaseHost host, String name) {
        super(host);
        setName(name);
    }

    /**
     * indicates whether the meta objects have been loaded
     */
    private boolean metaObjectsLoaded = false;

    /**
     * Returns the meta type objects from this schema
     *
     * @return the meta type objects
     */
    public List<DatabaseMetaTag> getMetaObjects() throws DataSourceException {

        if (isMarkedForReload()) {

            metaObjectsLoaded = false;
        }

        List<DatabaseMetaTag> metaObjects = super.getMetaObjects();

        if (!metaObjectsLoaded) {

            for (DatabaseMetaTag metaObject : metaObjects) {

                DefaultDatabaseMetaTag _metaTag = (DefaultDatabaseMetaTag) metaObject;
                _metaTag.setCatalog(this);
            }

            metaObjectsLoaded = true;
        }

        return metaObjects;
    }

    /**
     * Returns the schemas of this catalog.
     *
     * @return the catalog's schemas
     */
    public List<DatabaseSchema> getSchemas() throws DataSourceException {

        if (!isMarkedForReload() && schemas != null) {
            return schemas;
        }

        setMarkedForReload(isMarkedForReload());
        schemas = getHost().getSchemas();

        for (DatabaseSchema schema : schemas) {

            schema.setParent(this);
        }

        return schemas;
    }

    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object or null if we are at the top of the hierarchy
     */
    public NamedObject getParent() {
        return getHost();
    }

    /**
     * Returns the tables belonging to this source.
     *
     * @return the hosted tables
     */
    public List<NamedObject> getTables() throws DataSourceException {
        return getHost().getTables(getName(), null, "TABLE");
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return CATALOG;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return null;
    }

    /**
     * Returns the parent catalog object.
     *
     * @return the parent catalog object
     */
    public DatabaseCatalog getCatalog() {

        return this;
    }

    /**
     * Override to return value from getName().
     */
    public String getCatalogName() {
        return getName();
    }

    /**
     * Does nothing in this case.
     */
    public void setCatalogName(String catalog) {
    }

    /**
     * Override to return null.
     */
    public String getSchemaName() {
        return null;
    }

    /**
     * Does nothing in this case.
     */
    public void setSchemaName(String schema) {
    }

    @Override
    public String getDescription() {

        return "CATALOG: " + getName();
    }

    @Override
    public boolean allowsChildren() {
        return true;
    }

}





