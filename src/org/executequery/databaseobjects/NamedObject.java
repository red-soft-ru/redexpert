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

    int DOMAIN = 0;
    int TABLE = DOMAIN + 1;
    int GLOBAL_TEMPORARY = TABLE + 1;
    int VIEW = GLOBAL_TEMPORARY + 1;
    int PROCEDURE = VIEW + 1;
    int FUNCTION = PROCEDURE + 1;
    int PACKAGE = FUNCTION + 1;
    int TRIGGER = PACKAGE + 1;
    int DDL_TRIGGER = TRIGGER + 1;
    int DATABASE_TRIGGER = DDL_TRIGGER + 1;
    int SEQUENCE = DATABASE_TRIGGER + 1;
    int EXCEPTION = SEQUENCE + 1;
    int UDF = EXCEPTION + 1;
    int ROLE = UDF + 1;
    int INDEX = ROLE + 1;
    int SYSTEM_DOMAIN = INDEX + 1;
    int SYSTEM_TABLE = SYSTEM_DOMAIN + 1;
    int SYSTEM_VIEW = SYSTEM_TABLE + 1;
    int SYSTEM_FUNCTION = SYSTEM_VIEW + 1;
    int SYSTEM_STRING_FUNCTIONS = SYSTEM_FUNCTION + 1;
    int SYSTEM_NUMERIC_FUNCTIONS = SYSTEM_STRING_FUNCTIONS + 1;
    int SYSTEM_DATE_TIME_FUNCTIONS = SYSTEM_NUMERIC_FUNCTIONS + 1;
    int SYSTEM_TRIGGER = SYSTEM_DATE_TIME_FUNCTIONS + 1;
    int SYSTEM_ROLE = SYSTEM_TRIGGER + 1;
    int SYSTEM_INDEX = SYSTEM_ROLE + 1;
    int SYNONYM = SYSTEM_INDEX + 1;

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
            "DOMAIN",
            "TABLE",
            "GLOBAL TEMPORARY",
            "VIEW",
            "PROCEDURE",
            "FUNCTION",
            "PACKAGE",
            "TRIGGER",
            "DDL TRIGGER",
            "DATABASE TRIGGER",
            "SEQUENCE",
            "EXCEPTION",
            "EXTERNAL FUNCTION",
            "ROLE",
            "INDEX",
            "SYSTEM DOMAIN",
            "SYSTEM TABLE",
            "SYSTEM VIEW",
            "SYSTEM FUNCTIONS",
            "SYSTEM_STRING_FUNCTIONS",
            "SYSTEM_NUMERIC_FUNCTIONS",
            "SYSTEM_DATE_TIME_FUNCTIONS",
            "SYSTEM TRIGGER",
            "SYSTEM ROLE",
            "SYSTEM INDEX",
            "SYNONYM",
    };
    String[] META_TYPES_FOR_BUNDLE = {
            "DOMAIN",
            "TABLE",
            "GLOBAL_TEMPORARY",
            "VIEW",
            "PROCEDURE",
            "FUNCTION",
            "PACKAGE",
            "TRIGGER",
            "DDL_TRIGGER",
            "DATABASE_TRIGGER",
            "SEQUENCE",
            "EXCEPTION",
            "EXTERNAL_FUNCTION",
            "ROLE",
            "INDEX",
            "SYSTEM_DOMAIN",
            "SYSTEM_TABLE",
            "SYSTEM_VIEW",
            "SYSTEM_FUNCTIONS",
            "SYSTEM_STRING_FUNCTIONS",
            "SYSTEM_NUMERIC_FUNCTIONS",
            "SYSTEM_DATE_TIME_FUNCTIONS",
            "SYSTEM_TRIGGER",
            "SYSTEM_ROLE",
            "SYSTEM_INDEX",
            "SYNONYM",
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


