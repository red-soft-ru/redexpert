/*
 * BrowserConstants.java
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

import org.executequery.localization.Bundles;

/**
 * Reuseable constants for construction and reference
 * to the tree structure within the Database Browser Panel.<br>
 * This is purely a convenience class due to the large
 * use of the same String objects in many places.
 *
 * @author Takis Diakoumis
 */
public class BrowserConstants {

    // --------------------------------------------
    // parent labels and hashtable keys for images
    // --------------------------------------------

    public static final int ROOT_NODE = 96;
    public static final int CATALOG_NODE = 98;
    public static final int HOST_NODE = 99;
    public static final int SCHEMA_NODE = 97;

    public static final int FUNCTIONS_NODE = 0;
    public static final int INDEX_NODE = 1;
    public static final int PROCEDURE_NODE = 2;
    public static final int SEQUENCE_NODE = 3;
    public static final int SYNONYM_NODE = 4;
    public static final int SYSTEM_TABLE_NODE = 5;
    public static final int TABLE_NODE = 6;
    public static final int TRIGGER_NODE = 7;
    public static final int VIEW_NODE = 8;

    public static final int SYSTEM_FUNCTION_NODE = 9;

    public static final int SYSTEM_STRING_FUNCTIONS_NODE = 10;

    public static final int SYSTEM_NUMERIC_FUNCTIONS_NODE = 11;

    public static final int SYSTEM_DATE_TIME_FUNCTIONS_NODE = 12;

    public static final int OTHER_NODE = 95;

    public static final int COLUMN_NODE = 94;


    // same index as node values above
    public static final String[] META_TYPES = Bundles.get(BrowserConstants.class, new String[]{"FUNCTION",
            "INDEX",
            "PROCEDURE",
            "SEQUENCE",
            "SYNONYM",
            "SYSTEM-TABLE",
            "TABLE",
            "TRIGGER",
            "VIEW",
            "SYSTEM FUNCTIONS"});

    // ------------------------------------------
    // to add a new node - ALL icons must be in same order as META_TYPES
    // ------------------------------------------

    public static final String[] META_TYPE_ICONS = {"Function24.png",
            "TableIndex24.png",
            "Procedure24.png",
            "Sequence24.png",
            "Synonym24.png",
            "SystemTable24.png",
            "DatabaseTable24.png",
            "DatabaseTable24.png",
            "Trigger.png",
            "TableView24.png",
            "TableColumn24.png",
            "SystemFunction24.png"}; // system function

    /** The String 'All Types' */
    //  String ALL_TYPES = "All Types";
    /**
     * The String 'All Types Closed'
     */
    public static final String ALL_TYPES_CLOSED = bundleString("ALL_TYPES_CLOSED");

    /**
     * The String 'Functions'
     */
    public static final String SYSTEM_FUNCTIONS_STRING = bundleString("SYSTEM_FUNCTIONS_STRING");

    /**
     * The String 'Functions'
     */
    public static final String FUNCTIONS_STRING = bundleString("FUNCTIONS_STRING");
    /**
     * The String 'Indexes'
     */
    public static final String INDEXES_STRING = bundleString("INDEXES_STRING");
    /** The String 'Packages' */
    //String PACKAGES_STRING = "Packages";
    /**
     * The String 'Procedures'
     */
    public static final String PROCEDURES_STRING = bundleString("PROCEDURES_STRING");
    /**
     * The String 'Sequences'
     */
    public static final String SEQUENCES_STRING = bundleString("SEQUENCES_STRING");
    /**
     * The String 'Synonyms'
     */
    public static final String SYNONYMS_STRING = bundleString("SYNONYMS_STRING");
    /**
     * The String 'System Tables'
     */
    public static final String SYSTEM_TABLES_STRING = bundleString("SYSTEM_TABLES_STRING");
    /**
     * The String 'Tables'
     */
    public static final String TABLES_STRING = bundleString("TABLES_STRING");
    /**
     * The String 'Triggers'
     */
    public static final String TRIGGERS_STRING = bundleString("TRIGGERS_STRING");
    /**
     * The String 'Views'
     */
    public static final String VIEWS_STRING = bundleString("VIEWS_STRING");
    /**
     * The String 'Source'
     */
    public static final String SOURCE_STRING = bundleString("SOURCE_STRING");
    /**
     * The String 'Schema'
     */
    public static final String SCHEMA_STRING = bundleString("SCHEMA_STRING");

    // -----------------------------
    // image icons for tree nodes
    // -----------------------------

    public static final String DATABASE_OBJECT_IMAGE = "DatabaseObject16.png";

    /**
     * The image icon 'SavedConnection16.png'
     */
    public static final String CONNECTIONS_IMAGE = "DatabaseConnections16.png";

    public static final String CONNECTIONS_FOLDER_IMAGE = "ConnectionsFolder16.png";

    /**
     * The image icon 'Database16.png'
     */
    public static final String CATALOG_IMAGE = "DBImage16.png";

    /**
     * The image icon 'Database16.png'
     */
    public static final String HOST_IMAGE = "Database16.png";

    /**
     * The image icon 'DatabaseNotConnected16.png'
     */
    public static final String HOST_NOT_CONNECTED_IMAGE = "DatabaseNotConnected16.png";

    /**
     * The image icon 'DatabaseConnected16.png'
     */
    public static final String HOST_CONNECTED_IMAGE = "DatabaseConnected16.png";

    /**
     * The image icon 'User16.png'
     */
    public static final String SCHEMA_IMAGE = "User16.png";

    /**
     * The image icon 'SystemFunction16.png'
     */
    public static final String SYSTEM_FUNCTIONS_IMAGE = "SystemFunction16.png";

    /**
     * The image icon 'Function16.png'
     */
    public static final String FUNCTIONS_IMAGE = "Function16.png";

    /**
     * The image icon 'TableIndex16.png'
     */
    public static final String INDEXES_IMAGE = "TableIndex16.png";

    /**
     * The image icon 'Procedure16.png'
     */
    public static final String PROCEDURES_IMAGE = "Procedure16.png";

    /**
     * The image icon 'Sequence16.png'
     */
    public static final String SEQUENCES_IMAGE = "Sequence16.png";

    /**
     * The image icon 'Synonym16.png'
     */
    public static final String SYNONYMS_IMAGE = "Synonym16.png";

    /**
     * The image icon 'SystemTables16.png'
     */
    public static final String SYSTEM_TABLES_IMAGE = "SystemTable16.png";

    /**
     * The image icon 'PlainTable16.png'
     */
    public static final String TABLES_IMAGE = "PlainTable16.png";

    public static final String GLOBAL_TABLES_IMAGE = "GlobalTable16.png";

    /**
     * The image icon 'TableColumn16.png'
     */
    public static final String COLUMNS_IMAGE = "TableColumn16.png";

    public static final String PRIMARY_COLUMNS_IMAGE = "TableColumnPrimary16.png";

    public static final String FOREIGN_COLUMNS_IMAGE = "TableColumnForeign16.png";

    public static final String VIEWS_IMAGE = "TableView16.png";

    public static final String SYSTEM_VIEWS_IMAGE = "SystemTableView16.png";

    public static final String TABLE_TRIGGER_IMAGE = "Trigger.png";

    public static final String DB_TRIGGER_IMAGE = "TriggerDB.png";

    public static final String DDL_TRIGGER_IMAGE = "TriggerDDL.png";

    public static final String FOLDER_FOREIGN_KEYS_IMAGE = "FolderForeignKeys16.png";

    public static final String FOLDER_PRIMARY_KEYS_IMAGE = "FolderPrimaryKeys16.png";

    public static final String FOLDER_COLUMNS_IMAGE = "FolderColumns16.png";

    public static final String FOLDER_INDEXES_IMAGE = "FolderIndexes16.png";

    public static final String DOMAIN_IMAGE = "domain16.png";

    public static final String EXCEPTION_IMAGE = "exception16.png";

    public static final String UDF_IMAGE = "udf16.png";

    public static final String SYSTEM_DOMAIN_IMAGE = "SystemDomain16.png";

    public static final String SYSTEM_INDEX_IMAGE = "SystemIndex16.png";

    public static final String SYSTEM_TRIGGER_IMAGE = "SystemTrigger.png";

    public static final String PACKAGE_IMAGE = "package16.png";

    public static final String ROLE_IMAGE = "user_manager_16.png";

    public static final String SYSTEM_ROLE_IMAGE = "system_role_16.png";


    public static final String GRANT_IMAGE = "grant.png";
    public static final String NO_GRANT_IMAGE = "no_grant.png";
    public static final String ADMIN_OPTION_IMAGE = "admin_option.png";


    public static final String[] NODE_ICONS = {CONNECTIONS_IMAGE,
            CONNECTIONS_FOLDER_IMAGE,
            CATALOG_IMAGE,
            HOST_IMAGE,
            HOST_NOT_CONNECTED_IMAGE,
            HOST_CONNECTED_IMAGE,
            SCHEMA_IMAGE,
            FUNCTIONS_IMAGE,
            INDEXES_IMAGE,
            PROCEDURES_IMAGE,
            SEQUENCES_IMAGE,
            SYNONYMS_IMAGE,
            SYSTEM_TABLES_IMAGE,
            TABLES_IMAGE,
            VIEWS_IMAGE,
            SYSTEM_FUNCTIONS_IMAGE,
            COLUMNS_IMAGE,
            PRIMARY_COLUMNS_IMAGE,
            FOREIGN_COLUMNS_IMAGE,
            SYSTEM_VIEWS_IMAGE,
            TABLE_TRIGGER_IMAGE,
            GLOBAL_TABLES_IMAGE,
            FOLDER_COLUMNS_IMAGE,
            FOLDER_FOREIGN_KEYS_IMAGE,
            FOLDER_INDEXES_IMAGE,
            FOLDER_PRIMARY_KEYS_IMAGE,
            DOMAIN_IMAGE,
            EXCEPTION_IMAGE,
            UDF_IMAGE,
            SYSTEM_DOMAIN_IMAGE,
            SYSTEM_INDEX_IMAGE,
            SYSTEM_TRIGGER_IMAGE,
            PACKAGE_IMAGE,
            ROLE_IMAGE,
            SYSTEM_ROLE_IMAGE,
            DB_TRIGGER_IMAGE,
            DDL_TRIGGER_IMAGE
    };

    private static String bundleString(String key) {

        return Bundles.get(BrowserConstants.class, key);
    }
}






