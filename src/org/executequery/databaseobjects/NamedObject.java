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
    int USER = UDF + 1;
    int ROLE = USER + 1;
    int INDEX = ROLE + 1;
    int TABLESPACE = INDEX + 1;
    int JOB = TABLESPACE + 1;
    int COLLATION = JOB + 1;
    int SYSTEM_DOMAIN = COLLATION + 1;
    int SYSTEM_TABLE = SYSTEM_DOMAIN + 1;
    int SYSTEM_VIEW = SYSTEM_TABLE + 1;
    int SYSTEM_FUNCTION = SYSTEM_VIEW + 1;
    int SYSTEM_STRING_FUNCTIONS = SYSTEM_FUNCTION + 1;
    int SYSTEM_NUMERIC_FUNCTIONS = SYSTEM_STRING_FUNCTIONS + 1;
    int SYSTEM_DATE_TIME_FUNCTIONS = SYSTEM_NUMERIC_FUNCTIONS + 1;
    int SYSTEM_TRIGGER = SYSTEM_DATE_TIME_FUNCTIONS + 1;
    int SYSTEM_SEQUENCE = SYSTEM_TRIGGER + 1;
    int SYSTEM_ROLE = SYSTEM_SEQUENCE + 1;
    int SYSTEM_INDEX = SYSTEM_ROLE + 1;
    int SYSTEM_PACKAGE = SYSTEM_INDEX + 1;
    int TABLE_COLUMN = SYSTEM_PACKAGE + 1;
    int CONSTRAINT = TABLE_COLUMN + 1;
    int SYNONYM = CONSTRAINT + 1;

    int META_TAG = 93;
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
    int CHECK_KEY = 996;
    int TABLE_INDEX = 995;

    String[] KEYS = {
            "PRIMARY KEY",
            "FOREIGN KEY",
            "UNIQUE KEY",
            "CHECK KEY"
    };

    String[] KEYS_BUNDLE = {
            "PRIMARY_KEY",
            "FOREIGN_KEY",
            "UNIQUE_KEY",
            "CHECK_KEY"
    };
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
            "USER",
            "ROLE",
            "INDEX",
            "TABLESPACE",
            "JOB",
            "COLLATION",
            "SYSTEM DOMAIN",
            "SYSTEM TABLE",
            "SYSTEM VIEW",
            "SYSTEM FUNCTIONS",
            "SYSTEM_STRING_FUNCTIONS",
            "SYSTEM_NUMERIC_FUNCTIONS",
            "SYSTEM_DATE_TIME_FUNCTIONS",
            "SYSTEM TRIGGER",
            "SYSTEM SEQUENCE",
            "SYSTEM ROLE",
            "SYSTEM INDEX",
            "SYSTEM PACKAGE",
            "TABLE COLUMN",
            "CONSTRAINT",
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
            "USER",
            "ROLE",
            "INDEX",
            "TABLESPACE",
            "JOB",
            "COLLATION",
            "SYSTEM_DOMAIN",
            "SYSTEM_TABLE",
            "SYSTEM_VIEW",
            "SYSTEM_FUNCTIONS",
            "SYSTEM_STRING_FUNCTIONS",
            "SYSTEM_NUMERIC_FUNCTIONS",
            "SYSTEM_DATE_TIME_FUNCTIONS",
            "SYSTEM_TRIGGER",
            "SYSTEM_SEQUENCE",
            "SYSTEM_ROLE",
            "SYSTEM_INDEX",
            "SYSTEM_PACKAGE",
            "TABLE_COLUMN",
            "CONSTRAINT",
            "SYNONYM"
    };

    Integer[] META_TYPES_FOR_COMPARE = {
            COLLATION,
            DOMAIN,
            TABLESPACE,
            TABLE,
            GLOBAL_TEMPORARY,
            VIEW,
            INDEX,
            SEQUENCE,
            EXCEPTION,
            ROLE,
            FUNCTION,
            PROCEDURE,
            JOB,
            UDF,
            TRIGGER,
            DDL_TRIGGER,
            DATABASE_TRIGGER,
            PACKAGE
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
     * Sets the name fof this object.
     */
    void setName(String name);

    /**
     * Returns the display name of this object.
     *
     * @return the display name
     */
    String getShortName();

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
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

    int getRDBType();

    static int getSystemTypeFromType(int type) {
        switch (type) {
            case DOMAIN:
                return SYSTEM_DOMAIN;
            case TABLE:
                return SYSTEM_TABLE;
            case VIEW:
                return SYSTEM_VIEW;
            case FUNCTION:
                return SYSTEM_FUNCTION;
            case TRIGGER:
                return SYSTEM_TRIGGER;
            case SEQUENCE:
                return SYSTEM_SEQUENCE;
            case ROLE:
                return SYSTEM_ROLE;
            case INDEX:
                return SYSTEM_INDEX;
            case PACKAGE:
                return SYSTEM_PACKAGE;
            default:
                return -1;
        }
    }

    /**
     * Returns copy of this named object<br>
     * (by default returns NULL and should be overridden)
     */
    default NamedObject copy() {
        return null;
    };
    
}
