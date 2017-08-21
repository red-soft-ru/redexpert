/*
 * DefaultDatabaseDriver.java
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

package org.executequery.databasemediators.spi;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.datasource.DatabaseDefinition;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1766 $
 * @date     $Date: 2017-08-14 23:34:37 +1000 (Mon, 14 Aug 2017) $
 */
public class DefaultDatabaseDriver implements DatabaseDriver {

    private long id;

    private String name;

    private int type;
    
    private String path;
    
    private String className;
    
    private String url;
    
    private String description;
    
    public DefaultDatabaseDriver() {}

    public DefaultDatabaseDriver(String name) {

        this(0, name);
    }

    public DefaultDatabaseDriver(long id, String name) {

        this.id = id;
        this.name = name;

        type = DatabaseDefinition.INVALID_DATABASE_ID;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description == null ? "Not Available" : description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getURL() {
        return url;
    }
    
    public void setURL(String url) {
        this.url = url;
    }
    
    public int getType() {
        return type;
    }
    
    public void setDatabaseType(int type) {
        this.type = type;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public boolean equals(Object obj) {
    
        if (this == obj) {
            
            return true;
        }
        
        if (!(obj instanceof DefaultDatabaseDriver)) {
        
            return false;
        }

        DatabaseDriver dd = (DatabaseDriver)obj;
        return dd.getName().equals(name);        
    }
    
    public String toString() {
        return name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isDefaultSunOdbc() {
        
        return (getId() == SUN_ODBC_ID);
    }

    public boolean isIdValid() {

        return (getId() != 0);
    }
    
    public boolean isDatabaseTypeValid() {
        
        return (getType() != DatabaseDefinition.INVALID_DATABASE_ID);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    static final long serialVersionUID = -3111300858223645671L;

}





