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

    private int type;
    private boolean isSystem;

    private String name;
    private String remarks;
    private String metaDataKey;

    public BaseDatabaseObject() {
    }

    public BaseDatabaseObject(int type, String name) {
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

    @Override
    public int getRDBType() {
        return -1;
    }

}
