/*
 * NamedObject.java
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

import java.util.List;

import org.underworldlabs.jdbc.DataSourceException;

/**
 * Defines a database named object.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public interface NamedObject extends java.io.Serializable {

    int BRANCH_NODE = 100;
    int COLUMNS_FOLDER_NODE = 101;
    int FOREIGN_KEYS_FOLDER_NODE = 102;
    int PRIMARY_KEYS_FOLDER_NODE = 103;
    int INDEXES_FOLDER_NODE = 104;
    
    int ROOT = 96;
    int CATALOG = 98;
    int HOST = 99;
    int SCHEMA = 97;

    int OTHER = 95;
    
    int TABLE_COLUMN = 94;

    int META_TAG = 93;

    
    int FUNCTION = 0;
    int INDEX = 1;
    int PROCEDURE = 2;
    int SEQUENCE = 3;
    int SYNONYM = 4;
    int SYSTEM_TABLE = 5;
    int TABLE = 6;
    int TRIGGER = 7;
    int VIEW = 8;

    int PRIMARY_KEY = 999;
    int FOREIGN_KEY = 998;
    int UNIQUE_KEY = 997;
    int TABLE_INDEX = 996;

    int SYSTEM_VIEW = 13;
    
    int SYSTEM_FUNCTION = 9;
    
    int SYSTEM_STRING_FUNCTIONS = 10;
    
    int SYSTEM_NUMERIC_FUNCTIONS = 11;
    
    int SYSTEM_DATE_TIME_FUNCTIONS = 12;
    
    
    String[] META_TYPES = {"FUNCTION",
                                               "INDEX",
                                               "PROCEDURE",
                                               "SEQUENCE",
                                               "SYNONYM",
                                               "SYSTEM TABLE",
                                               "TABLE",
                                               "TRIGGER",
                                               "VIEW",
                                               "SYSTEM FUNCTIONS",
                                               "SYSTEM_STRING_FUNCTIONS",
                                               "SYSTEM_NUMERIC_FUNCTIONS",
                                               "SYSTEM_DATE_TIME_FUNCTIONS",
                                               "SYSTEM VIEW"};

    /**
     * Marks this object as being 'reset', where for any loaded object
     * these are cleared and a fresh database call would be made where 
     * appropriate.
     */
    void reset();
    
    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    int getType();

    /**
     * Returns the name of this object.
     *
     * @return the object name
     */
    String getName();

    /**
     * Sets the name of this database object as specified.
     *
     * @param name the name of this database object
     */
    void setName(String name);
    
    /**
     * Returns the display name of this object.
     *
     * @return the display name
     */
    String getShortName();
    
    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    String getMetaDataKey();
    
    /**
     * Retrieves child database objects of this named object.
     * Depending on the type of named object - this may return null.
     *
     * @return this meta tag's child database objects.
     */
    List<NamedObject> getObjects() throws DataSourceException;
    
    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object or null if we are at the top of the hierarchy
     */
    NamedObject getParent();
    
    /**
     * Sets the parent object to that specified.
     *
     * @param the parent named object
     */
    void setParent(NamedObject parent);

    /**
     * Drops this named object in the database.
     *
     * @return drop statement result
     */
    int drop() throws DataSourceException;
    
    
    String getDescription();

}

