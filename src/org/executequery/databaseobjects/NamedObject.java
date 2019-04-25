/*
 * NamedObject.java
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

package org.executequery.databaseobjects;

import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.Named;

import java.util.List;

/**
 * Defines a database named object.
 *
 * @author Takis Diakoumis
 */
public interface NamedObject extends Named, java.io.Serializable {

    int INDEX = 0;
    int PROCEDURE = 1;
    int SEQUENCE = 2;
    int SYNONYM = 3;
    int TABLE = 4;
    int TRIGGER = 5;
    int SYSTEM_DATABASE_TRIGGER = 6;
    int VIEW = 7;
    int DOMAIN = 8;
    int EXCEPTION = 9;
    int UDF = 10;
    int FUNCTION = 11;
    int GLOBAL_TEMPORARY = 12;
    int PACKAGE = 13;
    int ROLE = 14;
    int SYSTEM_FUNCTION = 15;
    int SYSTEM_STRING_FUNCTIONS = 16;
    int SYSTEM_NUMERIC_FUNCTIONS = 17;
    int SYSTEM_DATE_TIME_FUNCTIONS = 18;
    int SYSTEM_VIEW = 19;
    int SYSTEM_TABLE = 20;
    int SYSTEM_DOMAIN = 21;
    int SYSTEM_INDEX = 22;
    int SYSTEM_TRIGGER = 23;

    int META_TAG = 93;
    int TABLE_COLUMN = 94;
    int OTHER = 95;
    int ROOT = 96;
    int SCHEMA = 97;
    int CATALOG = 98;
    int HOST = 99;

    int BRANCH_NODE = 100;
    int COLUMNS_FOLDER_NODE = 101;
    int FOREIGN_KEYS_FOLDER_NODE = 102;
    int PRIMARY_KEYS_FOLDER_NODE = 103;
    int INDEXES_FOLDER_NODE = 104;

    int PRIMARY_KEY = 999;
    int FOREIGN_KEY = 998;
    int UNIQUE_KEY = 997;
    int TABLE_INDEX = 996;
    int CHECK_KEY = 995;

    String[] META_TYPES = {
            "INDEX",
            "PROCEDURE",
            "SEQUENCE",
            "SYNONYM",
            "TABLE",
            "TRIGGER",
            "DATABASE TRIGGER",
            "VIEW",
            "DOMAIN",
            "EXCEPTION",
            "EXTERNAL FUNCTION",
            "FUNCTION",
            "GLOBAL TEMPORARY",
            "PACKAGE",
            "ROLE",
            "SYSTEM FUNCTIONS",
            "SYSTEM_STRING_FUNCTIONS",
            "SYSTEM_NUMERIC_FUNCTIONS",
            "SYSTEM_DATE_TIME_FUNCTIONS",
            "SYSTEM VIEW",
            "SYSTEM TABLE",
            "SYSTEM DOMAIN",
            "SYSTEM INDEX",
            "SYSTEM TRIGGER"
    };
    String[] META_TYPES_FOR_BUNDLE = {
            "INDEX",
            "PROCEDURE",
            "SEQUENCE",
            "SYNONYM",
            "TABLE",
            "TRIGGER",
            "DATABASE_TRIGGER",
            "VIEW",
            "DOMAIN",
            "EXCEPTION",
            "EXTERNAL_FUNCTION",
            "FUNCTION",
            "GLOBAL_TEMPORARY",
            "PACKAGE",
            "ROLE",
            "SYSTEM_FUNCTIONS",
            "SYSTEM_STRING_FUNCTIONS",
            "SYSTEM_NUMERIC_FUNCTIONS",
            "SYSTEM_DATE_TIME_FUNCTIONS",
            "SYSTEM_VIEW",
            "SYSTEM_TABLE",
            "SYSTEM_DOMAIN",
            "SYSTEM_INDEX",
            "SYSTEM_TRIGGER"
    };

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
     * @param parent parent named object
     */
    void setParent(NamedObject parent);

    /**
     * Drops this named object in the database.
     *
     * @return drop statement result
     */
    int drop() throws DataSourceException;


    String getDescription();

    boolean isSystem();

    void setSystemFlag(boolean flag);

    boolean allowsChildren();

}


