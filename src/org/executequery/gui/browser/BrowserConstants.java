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





    // ------------------------------------------
    // to add a new node - ALL icons must be in same order as META_TYPES
    // ------------------------------------------

     // system function

    /** The String 'All Types' */
    //  String ALL_TYPES = "All Types";
    /**
     * The String 'All Types Closed'
     *

    // -----------------------------
    // image icons for tree nodes
    // -----------------------------
     */

    public static final String DATABASE_OBJECT_IMAGE = "DatabaseObject16.svg";

    /**
     * The image icon 'SavedConnection16.svg'
     */
    public static final String CONNECTIONS_IMAGE = "DatabaseConnections16.svg";

    public static final String CONNECTIONS_FOLDER_IMAGE = "ConnectionsFolder16.svg";

    /**
     * The image icon 'Database16.svg'
     */
    public static final String CATALOG_IMAGE = "DBImage24.svg";

    /**
     * The image icon 'Database16.svg'
     */
    public static final String HOST_IMAGE = "Database16.svg";

    /**
     * The image icon 'DatabaseNotConnected16.svg'
     */
    public static final String HOST_NOT_CONNECTED_IMAGE = "DatabaseNotConnected16.svg";

    /**
     * The image icon 'DatabaseConnected16.svg'
     */
    public static final String HOST_CONNECTED_IMAGE = "DatabaseConnected16.svg";

    /**
     * The image icon 'User16.svg'
     */
    public static final String SCHEMA_IMAGE = "User16.svg";

    /**
     * The image icon 'SystemFunction16.svg'
     */
    public static final String SYSTEM_FUNCTIONS_IMAGE = "SystemFunction16.svg";

    /**
     * The image icon 'Function16.svg'
     */
    public static final String FUNCTIONS_IMAGE = "Function16.svg";

    /**
     * The image icon 'TableIndex16.svg'
     */
    public static final String INDEXES_IMAGE = "TableIndex16.svg";

    /**
     * The image icon 'Procedure16.svg'
     */
    public static final String PROCEDURES_IMAGE = "Procedure16.svg";

    /**
     * The image icon 'Sequence16.svg'
     */
    public static final String SEQUENCES_IMAGE = "Sequence16.svg";
    public static final String SYSTEM_SEQUENCES_IMAGE = "SystemSequence16.svg";

    /**
     * The image icon 'Synonym16.svg'
     */
    public static final String SYNONYMS_IMAGE = "Synonym16.svg";

    /**
     * The image icon 'SystemTables16.svg'
     */
    public static final String SYSTEM_TABLES_IMAGE = "SystemTable16.svg";

    /**
     * The image icon 'PlainTable16.svg'
     */
    public static final String TABLES_IMAGE = "PlainTable16.svg";

    public static final String GLOBAL_TABLES_IMAGE = "GlobalTable16.svg";

    /**
     * The image icon 'TableColumn16.svg'
     */
    public static final String COLUMNS_IMAGE = "TableColumn16.svg";

    public static final String PRIMARY_COLUMNS_IMAGE = "TableColumnPrimary16.svg";

    public static final String FOREIGN_COLUMNS_IMAGE = "TableColumnForeign16.svg";

    public static final String VIEWS_IMAGE = "TableView16.svg";

    public static final String SYSTEM_VIEWS_IMAGE = "SystemTableView16.svg";

    public static final String TABLE_TRIGGER_IMAGE = "Trigger.svg";

    public static final String DB_TRIGGER_IMAGE = "TriggerDB.svg";

    public static final String DDL_TRIGGER_IMAGE = "TriggerDDL.svg";

    public static final String FOLDER_FOREIGN_KEYS_IMAGE = "FolderForeignKeys16.svg";

    public static final String FOLDER_PRIMARY_KEYS_IMAGE = "FolderPrimaryKeys16.svg";

    public static final String FOLDER_COLUMNS_IMAGE = "FolderColumns16.svg";

    public static final String FOLDER_INDEXES_IMAGE = "FolderIndexes16.svg";

    public static final String DOMAIN_IMAGE = "domain16.svg";

    public static final String EXCEPTION_IMAGE = "exception16.svg";

    public static final String UDF_IMAGE = "udf16.svg";

    public static final String SYSTEM_DOMAIN_IMAGE = "SystemDomain16.svg";

    public static final String SYSTEM_INDEX_IMAGE = "SystemIndex16.svg";

    public static final String SYSTEM_TRIGGER_IMAGE = "SystemTrigger.svg";

    public static final String PACKAGE_IMAGE = "package16.svg";

    public static final String SYSTEM_PACKAGE_IMAGE = "system_package16.svg";

    public static final String ROLE_IMAGE = "user_manager_16.svg";

    public static final String SYSTEM_ROLE_IMAGE = "system_role_16.svg";

    public static final String USER_IMAGE = "User16.svg";

    public static final String TABLESPACE_IMAGE = "tablespace16.svg";
    public static final String JOB_IMAGE = "job16.svg";
    public static final String COLLATION_IMAGE = "XmlFile16.svg";


    public static final String GRANT_IMAGE = "grant.svg";
    public static final String NO_GRANT_IMAGE = "no_grant.svg";
    public static final String ADMIN_OPTION_IMAGE = "admin_option.svg";
    public static final String FIELD_GRANT_IMAGE = "grantPart.svg";


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
            USER_IMAGE,
            TABLESPACE_IMAGE,
            JOB_IMAGE,
            DB_TRIGGER_IMAGE,
            DDL_TRIGGER_IMAGE,
            SYSTEM_PACKAGE_IMAGE,
            SYSTEM_SEQUENCES_IMAGE,
            COLLATION_IMAGE
    };

    private static String bundleString(String key) {

        return Bundles.get(BrowserConstants.class, key);
    }
}






