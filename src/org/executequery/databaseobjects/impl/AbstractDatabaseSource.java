/*
 * AbstractDatabaseSource.java
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

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseCatalog;
import org.executequery.databaseobjects.DatabaseFunction;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.databaseobjects.DatabaseSchema;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;

/**
 * Database source object definition (ie. a catalog or schema)
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public abstract class AbstractDatabaseSource extends AbstractNamedObject {

    /** the host object for this catalog */
    private DatabaseHost host;

    /** the meta tag objects of this schema */
    private List<DatabaseMetaTag> metaObjects;

    /** Creates a new instance of AbstractDatabaseSource */
    public AbstractDatabaseSource(DatabaseHost host) {

        this.host = host;
    }

    /**
     * Returns the meta object with the specified name
     *
     * @param   name the meta tag name
     * @return the meta tag object
     */
    public DatabaseMetaTag getDatabaseMetaTag(String name) {

        name = name.toUpperCase();

        List<DatabaseMetaTag> _metaObjects = getMetaObjects();
        for (DatabaseMetaTag object : _metaObjects) {

            if (name.equals(object.getName().toUpperCase())) {

                return object;
            }

        }

        return null;
    }

    /**
     * Returns the procedure or function if procedure does not exist with
     * the specified name.
     *
     * @param name
     * @return the named procedure or function
     */
    public DatabaseProcedure getProcedure(String name) {

        DatabaseMetaTag metaTag = getDatabaseMetaTag(META_TYPES[PROCEDURE]);
        if (metaTag == null) {

            return null;
        }

        List<NamedObject> objects = metaTag.getObjects();
        for (NamedObject namedObject: objects) {

            if (name.equalsIgnoreCase(namedObject.getName())) {

                return (DatabaseProcedure) namedObject;
            }

        }

        return null;
    }

    /**
     * Returns the procedure or function if procedure does not exist with
     * the specified name.
     *
     * @param name
     * @return the named procedure or function
     */
    public DatabaseFunction getFunction(String name) {

        DatabaseMetaTag metaTag = getDatabaseMetaTag(META_TYPES[FUNCTION]);
        if (metaTag == null) {
            
            return null;
        }
        
        List<NamedObject> objects = metaTag.getObjects();
        for (NamedObject namedObject: objects) {
            
            if (name.equalsIgnoreCase(namedObject.getName())) {
                
                return (DatabaseFunction) namedObject;
            }
            
        }
        
        return null;
    }

    /**
     * Returns the meta type objects from this schema
     *
     * @return the meta type objects
     */
    public List<DatabaseMetaTag> getMetaObjects() throws DataSourceException {

        if (!isMarkedForReload() && metaObjects != null) {

            return metaObjects;
        }

        metaObjects = getHost().getMetaObjects(getCatalog(), getSchema());
        return metaObjects;
    }

    /**
     * Does nothing.
     */
    public int drop() throws DataSourceException {
        return 0;
    }

    /**
     * Override to determine if this is the default source connection.
     *
     * @return the display name
     */
    public String getShortName() {

        if (isDefault()) {

            return getName() + " (default)";
        }

        return getName();
    }

    /**
     * Returns whether this is the default source connection.
     */
    public boolean isDefault() {

        DatabaseHost _host = getHost();

        if (_host != null) {

            String value = null;
            String myName = getName();
            if (isCatalog()) {

                try {

                    value = _host.getConnection().getCatalog();

                } catch (SQLException e) {

                    logThrowable(e);

                } catch (DataSourceException e) {

                    logThrowable(e);
                }

                if (value != null) {

                    return value.equalsIgnoreCase(myName);
                }

            }

            DatabaseConnection dc = _host.getDatabaseConnection();

            // test if the login name matches
            value = dc.getUserName();
            if (StringUtils.equalsIgnoreCase(myName, value)) {

                return true;
            }

            // test the source name
            value = dc.getSourceName();
            if (StringUtils.equalsIgnoreCase(myName, value)) {

                return true;
            }

        }

        return false;
    }

    private boolean isCatalog() {

        return (getType() == CATALOG);
    }

    /**
     * Returns the parent host object.
     *
     * @return the parent object
     */
    public DatabaseHost getHost() {

        return host;
    }

    /**
     * Returns the parent catalog object.
     *
     * @return the parent catalog object
     */
    public DatabaseCatalog getCatalog() {

        return null;
    }

    /**
     * Returns the parent schema object.
     *
     * @return the parent schema object
     */
    public DatabaseSchema getSchema() {

        return null;
    }


}












