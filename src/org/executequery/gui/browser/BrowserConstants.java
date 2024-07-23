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

/**
 * Reusable constants for construction and reference
 * to the tree structure within the Database Browser Panel.<br>
 * This is purely a convenience class due to the large
 * use of the same String objects in many places.
 *
 * @author Takis Diakoumis
 */
public class BrowserConstants {
    public static final String APPLICATION_IMAGE = "icon_red_expert";

    // --- connections tree ---

    public static final String FOLDER_IMAGE = "icon_folder";
    public static final String CONNECTIONS_IMAGE = "icon_db_search";
    public static final String DATABASE_OBJECT_IMAGE = "icon_db_object";
    public static final String HOST_NOT_CONNECTED_IMAGE = "icon_connection";
    public static final String HOST_CONNECTED_IMAGE = "icon_connection_active";

    // --- table constraints ---

    public static final String PRIMARY_COLUMNS_IMAGE = "icon_db_table_column_primary";
    public static final String PRIMARY_KEY_IMAGE = "icon_key_primary";
    public static final String FOREIGN_KEY_IMAGE = "icon_key_foreign";

    // --- DB objects ---

    public static final String FUNCTIONS_IMAGE = "icon_db_function";
    public static final String INDEXES_IMAGE = "icon_db_index";
    public static final String PROCEDURES_IMAGE = "icon_db_procedure";
    public static final String SEQUENCES_IMAGE = "icon_db_generator";
    public static final String TABLES_IMAGE = "icon_db_table";
    public static final String GLOBAL_TABLES_IMAGE = "icon_db_table_global";
    public static final String COLUMNS_IMAGE = "icon_db_table_column";
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

    // --- system DB objects ---

    public static final String SYSTEM_DOMAIN_IMAGE = "icon_db_domain_system";
    public static final String SYSTEM_TABLES_IMAGE = "icon_db_table_system";
    public static final String SYSTEM_VIEWS_IMAGE = "icon_db_view_system";
    public static final String SYSTEM_INDEX_IMAGE = "icon_db_index_system";
    public static final String SYSTEM_TRIGGER_IMAGE = "icon_db_trigger_system";
    public static final String SYSTEM_FUNCTIONS_IMAGE = "icon_db_function_system";
    public static final String SYSTEM_SEQUENCES_IMAGE = "icon_db_generator_system";
    public static final String SYSTEM_PACKAGE_IMAGE = "icon_db_package_system";
    public static final String SYSTEM_ROLE_IMAGE = "icon_db_role_system";

    // --- grant manager ---

    public static final String GRANT_IMAGE = "icon_grant";
    public static final String REVOKE_IMAGE = "icon_revoke";
    public static final String ADMIN_OPTION_IMAGE = "icon_grant_admin";
    public static final String FIELD_GRANT_IMAGE = "icon_grant_disable";
}
