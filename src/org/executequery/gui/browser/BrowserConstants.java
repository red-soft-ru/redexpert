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

import org.executequery.GUIUtilities;

/**
 * Reusable constants for construction and reference
 * to the tree structure within the Database Browser Panel.<br>
 * This is purely a convenience class due to the large
 * use of the same String objects in many places.
 *
 * @author Takis Diakoumis
 */
public class BrowserConstants {

    public static final String LIGHT_SUFFIX = "_light";

    public static final String DATABASE_OBJECT_IMAGE = "icon_db_object";
    public static final String CONNECTIONS_IMAGE = "icon_db_connection";
    public static final String CONNECTIONS_FOLDER_IMAGE = "icon_folder";
    public static final String CATALOG_IMAGE = "icon_db_image";
    public static final String HOST_IMAGE = "icon_db";
    public static final String HOST_NOT_CONNECTED_IMAGE = "icon_disconnect";
    public static final String HOST_CONNECTED_IMAGE = "icon_connect";
    public static final String SCHEMA_IMAGE = "icon_db_user";
    public static final String FUNCTIONS_IMAGE = "icon_db_function";
    public static final String INDEXES_IMAGE = "icon_db_index";
    public static final String PROCEDURES_IMAGE = "icon_db_procedure";
    public static final String SEQUENCES_IMAGE = "icon_db_generator";
    public static final String SYNONYMS_IMAGE = "icon_synonym";
    public static final String TABLES_IMAGE = "icon_db_table";
    public static final String GLOBAL_TABLES_IMAGE = "icon_db_table_global";
    public static final String COLUMNS_IMAGE = "icon_db_table_column";
    public static final String PRIMARY_COLUMNS_IMAGE = "icon_db_table_column_primary";
    public static final String FOREIGN_COLUMNS_IMAGE = "icon_db_table_column_foreign";
    public static final String VIEWS_IMAGE = "icon_db_view";
    public static final String TABLE_TRIGGER_IMAGE = "icon_db_trigger_table";
    public static final String DB_TRIGGER_IMAGE = "icon_db_trigger_db";
    public static final String DDL_TRIGGER_IMAGE = "icon_db_trigger_ddl";
    public static final String DOMAIN_IMAGE = "icon_db_domain";
    public static final String EXCEPTION_IMAGE = "icon_db_exception";
    public static final String UDF_IMAGE = "icon_db_udf";
    public static final String PACKAGE_IMAGE = "icon_db_package";
    public static final String ROLE_IMAGE = "icon_db_role";
    public static final String USER_IMAGE = "icon_db_user";
    public static final String TABLESPACE_IMAGE = "icon_db_tablespace";
    public static final String JOB_IMAGE = "icon_db_job";

    public static final String CONNECTIONS_FOLDER_IMAGE_LIGHT = CONNECTIONS_FOLDER_IMAGE + LIGHT_SUFFIX;
    public static final String CATALOG_IMAGE_LIGHT = CATALOG_IMAGE + LIGHT_SUFFIX;
    public static final String HOST_NOT_CONNECTED_IMAGE_LIGHT = HOST_NOT_CONNECTED_IMAGE + LIGHT_SUFFIX;
    public static final String HOST_CONNECTED_IMAGE_LIGHT = HOST_CONNECTED_IMAGE + LIGHT_SUFFIX;
    public static final String FUNCTIONS_IMAGE_LIGHT = FUNCTIONS_IMAGE + LIGHT_SUFFIX;
    public static final String INDEXES_IMAGE_LIGHT = INDEXES_IMAGE + LIGHT_SUFFIX;
    public static final String PROCEDURES_IMAGE_LIGHT = PROCEDURES_IMAGE + LIGHT_SUFFIX;
    public static final String SEQUENCES_IMAGE_LIGHT = SEQUENCES_IMAGE + LIGHT_SUFFIX;
    public static final String TABLES_IMAGE_LIGHT = TABLES_IMAGE + LIGHT_SUFFIX;
    public static final String GLOBAL_TABLES_IMAGE_LIGHT = GLOBAL_TABLES_IMAGE + LIGHT_SUFFIX;
    //    public static final String COLUMNS_IMAGE_LIGHT = COLUMNS_IMAGE + LIGHT_SUFFIX;
//    public static final String PRIMARY_COLUMNS_IMAGE_LIGHT = PRIMARY_COLUMNS_IMAGE + LIGHT_SUFFIX;
//    public static final String FOREIGN_COLUMNS_IMAGE_LIGHT = FOREIGN_COLUMNS_IMAGE + LIGHT_SUFFIX;
    public static final String VIEWS_IMAGE_LIGHT = VIEWS_IMAGE + LIGHT_SUFFIX;
    public static final String TABLE_TRIGGER_IMAGE_LIGHT = TABLE_TRIGGER_IMAGE + LIGHT_SUFFIX;
    public static final String DB_TRIGGER_IMAGE_LIGHT = DB_TRIGGER_IMAGE + LIGHT_SUFFIX;
    public static final String DDL_TRIGGER_IMAGE_LIGHT = DDL_TRIGGER_IMAGE + LIGHT_SUFFIX;
    public static final String DOMAIN_IMAGE_LIGHT = DOMAIN_IMAGE + LIGHT_SUFFIX;
    public static final String EXCEPTION_IMAGE_LIGHT = EXCEPTION_IMAGE + LIGHT_SUFFIX;
    public static final String UDF_IMAGE_LIGHT = UDF_IMAGE + LIGHT_SUFFIX;
    public static final String PACKAGE_IMAGE_LIGHT = PACKAGE_IMAGE + LIGHT_SUFFIX;
    public static final String ROLE_IMAGE_LIGHT = ROLE_IMAGE + LIGHT_SUFFIX;
    public static final String USER_IMAGE_LIGHT = USER_IMAGE + LIGHT_SUFFIX;
    public static final String TABLESPACE_IMAGE_LIGHT = TABLESPACE_IMAGE + LIGHT_SUFFIX;
    public static final String JOB_IMAGE_LIGHT = JOB_IMAGE + LIGHT_SUFFIX;

    public static final String FOLDER_FOREIGN_KEYS_IMAGE = "icon_folder_foreign";
    public static final String FOLDER_PRIMARY_KEYS_IMAGE = "icon_folder_primary";
    public static final String FOLDER_COLUMNS_IMAGE = "icon_folder_column";
    public static final String FOLDER_INDEXES_IMAGE = "icon_folder_index";

    public static final String SYSTEM_DOMAIN_IMAGE = "icon_db_domain_system";
    public static final String SYSTEM_TABLES_IMAGE = "icon_db_table_system";
    public static final String SYSTEM_VIEWS_IMAGE = "icon_db_view_system";
    public static final String SYSTEM_INDEX_IMAGE = "icon_db_index_system";
    public static final String SYSTEM_TRIGGER_IMAGE = "icon_db_trigger_system";
    public static final String SYSTEM_FUNCTIONS_IMAGE = "icon_db_function_system";
    public static final String SYSTEM_SEQUENCES_IMAGE = "icon_db_generator_system";
    public static final String SYSTEM_PACKAGE_IMAGE = "icon_db_package_system";
    public static final String SYSTEM_ROLE_IMAGE = "icon_db_role_system";

    public static final String SYSTEM_DOMAIN_IMAGE_LIGHT = SYSTEM_DOMAIN_IMAGE + LIGHT_SUFFIX;
    public static final String SYSTEM_TABLES_IMAGE_LIGHT = SYSTEM_TABLES_IMAGE + LIGHT_SUFFIX;
    public static final String SYSTEM_VIEWS_IMAGE_LIGHT = SYSTEM_VIEWS_IMAGE + LIGHT_SUFFIX;
    public static final String SYSTEM_INDEX_IMAGE_LIGHT = SYSTEM_INDEX_IMAGE + LIGHT_SUFFIX;
    public static final String SYSTEM_TRIGGER_IMAGE_LIGHT = SYSTEM_TRIGGER_IMAGE + LIGHT_SUFFIX;
    public static final String SYSTEM_FUNCTIONS_IMAGE_LIGHT = SYSTEM_FUNCTIONS_IMAGE + LIGHT_SUFFIX;
    public static final String SYSTEM_SEQUENCES_IMAGE_LIGHT = SYSTEM_SEQUENCES_IMAGE + LIGHT_SUFFIX;
    public static final String SYSTEM_PACKAGE_IMAGE_LIGHT = SYSTEM_PACKAGE_IMAGE + LIGHT_SUFFIX;
    public static final String SYSTEM_ROLE_IMAGE_LIGHT = SYSTEM_ROLE_IMAGE + LIGHT_SUFFIX;

    public static final String GRANT_IMAGE = "icon_grant.svg";
    public static final String NO_GRANT_IMAGE = "icon_revoke.svg";
    public static final String ADMIN_OPTION_IMAGE = "icon_grant_admin.svg";
    public static final String FIELD_GRANT_IMAGE = "icon_grant_disable.svg";

    public static String[] getNodeIcons() {
        return GUIUtilities.getLookAndFeel().isClassicTheme() ? NODE_ICONS_CLASSIC : NODE_ICONS_DEFAULT;
    }

    private static final String[] NODE_ICONS_DEFAULT = {
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
            CONNECTIONS_FOLDER_IMAGE_LIGHT,
            CATALOG_IMAGE_LIGHT,
            HOST_NOT_CONNECTED_IMAGE_LIGHT,
            HOST_CONNECTED_IMAGE_LIGHT,
            FUNCTIONS_IMAGE_LIGHT,
            INDEXES_IMAGE_LIGHT,
            PROCEDURES_IMAGE_LIGHT,
            SEQUENCES_IMAGE_LIGHT,
            TABLES_IMAGE_LIGHT,
            GLOBAL_TABLES_IMAGE_LIGHT,
//            COLUMNS_IMAGE_LIGHT,
//            PRIMARY_COLUMNS_IMAGE_LIGHT,
//            FOREIGN_COLUMNS_IMAGE_LIGHT,
            VIEWS_IMAGE_LIGHT,
            TABLE_TRIGGER_IMAGE_LIGHT,
            DB_TRIGGER_IMAGE_LIGHT,
            DDL_TRIGGER_IMAGE_LIGHT,
            DOMAIN_IMAGE_LIGHT,
            EXCEPTION_IMAGE_LIGHT,
            UDF_IMAGE_LIGHT,
            PACKAGE_IMAGE_LIGHT,
            ROLE_IMAGE_LIGHT,
            USER_IMAGE_LIGHT,
            TABLESPACE_IMAGE_LIGHT,
            JOB_IMAGE_LIGHT
    };

    private static final String[] NODE_ICONS_CLASSIC = {
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
            SYSTEM_SEQUENCES_IMAGE
    };

}
