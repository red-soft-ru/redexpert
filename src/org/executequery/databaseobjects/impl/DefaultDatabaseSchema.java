/*
 * DefaultDatabaseSchema.java
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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Default database schema object implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultDatabaseSchema extends AbstractDatabaseSource
        implements DatabaseSchema {

    /**
     * the catalog object for this schema
     */
    private DatabaseCatalog catalog;

    /**
     * Creates a new instance of DefaultDatabaseSchema
     */
    public DefaultDatabaseSchema(DatabaseHost host, String name) {
        this(host, null, name);
    }

    /**
     * Creates a new instance of DefaultDatabaseSchema
     */
    public DefaultDatabaseSchema(DatabaseHost host, DatabaseCatalog catalog, String name) {

        super(host);

        this.catalog = catalog;
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

            DatabaseCatalog _catalog = getCatalog();

            for (DatabaseMetaTag metaObject : metaObjects) {

                DefaultDatabaseMetaTag _metaTag = (DefaultDatabaseMetaTag) metaObject;

                _metaTag.setCatalog(_catalog);
                _metaTag.setSchema(this);
            }

            metaObjectsLoaded = true;
        }

        return metaObjects;

    }

    /**
     * Returns all available objects within this schema.
     *
     * @return the schema objects
     */
    public List<SimpleDatabaseObject> getSchemaObjects() throws DataSourceException {

        ResultSet rs = null;

        try {
            List<DatabaseMetaTag> metaTags = getMetaObjects();

            String[] _metaTags = new String[metaTags.size()];
            for (int i = 0; i < _metaTags.length; i++) {

                _metaTags[i] = metaTags.get(i).getName();
            }

            DatabaseHost databaseHost = getHost();
            DatabaseMetaData dmd = databaseHost.getDatabaseMetaData();
            String catalogName = databaseHost.getCatalogNameForQueries(getCatalogName());
            String schemaName = databaseHost.getSchemaNameForQueries(getName());

            rs = dmd.getTables(catalogName, schemaName, null, _metaTags);
            if (rs == null) {

                return null;
            }

            List<SimpleDatabaseObject> list = new ArrayList<SimpleDatabaseObject>();
            while (rs.next()) {

                SimpleDatabaseObject object = new SimpleDatabaseObject();
                object.setCatalogName(rs.getString(1));
                object.setSchemaName(rs.getString(2));
                object.setName(rs.getString(3));
                object.setMetaDataKey(rs.getString(4));
                object.setRemarks(rs.getString(5));
                list.add(object);
            }

            return list;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            releaseResources(rs);
        }

    }

    /**
     * Returns the tables belonging to this source.
     *
     * @return the hosted tables
     */
    public List<NamedObject> getTables() throws DataSourceException {

        return getHost().getTables(getCatalogName(), getName(), "TABLE");
    }

    /**
     * Returns the parent catalog object.
     *
     * @return the parent object
     */
    public DatabaseCatalog getCatalog() {

        return catalog;
    }

    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object or null if we are at the top of the hierarchy
     */
    public NamedObject getParent() {

        return getCatalog() == null ? getHost() : getCatalog();
    }

    @Override
    public void setParent(NamedObject parent) {

        if (parent instanceof DatabaseCatalog) {

            this.catalog = (DatabaseCatalog) parent;
        }

        super.setParent(parent);
    }

    /**
     * Override to return value from getName().
     */
    public String getCatalogName() {

        DatabaseCatalog catalog = getCatalog();

        if (catalog != null) {

            return catalog.getName();
        }

        return null;
    }

    /**
     * Does nothing in this case.
     */
    public void setCatalogName(String catalog) {
    }

    /**
     * Override to return value from getName().
     */
    public String getSchemaName() {

        return getName();
    }

    /**
     * Does nothing in this case.
     */
    public void setSchemaName(String schema) {
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {

        return SCHEMA;
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
     * Returns the parent schema object.
     *
     * @return the parent schema object
     */
    public DatabaseSchema getSchema() {

        return this;
    }

    @Override
    public String getDescription() {

        return "SCHEMA: " + getName();
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }


}





