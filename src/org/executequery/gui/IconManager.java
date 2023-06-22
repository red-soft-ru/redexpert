package org.executequery.gui;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.BrowserConstants;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class IconManager {
    private static IconManager iconManager;

    private final Map<String, Icon> icons;

    private IconManager() {
        icons = loadIcons();
    }

    public static IconManager getInstance() {
        if (iconManager == null)
            iconManager = new IconManager();
        return iconManager;
    }

    private Map<String, Icon> loadIcons() {

        Map<String, Icon> icons = new HashMap<String, Icon>();
        for (int i = 0; i < BrowserConstants.NODE_ICONS.length; i++) {
            icons.put(BrowserConstants.NODE_ICONS[i],
                    GUIUtilities.loadIcon(BrowserConstants.NODE_ICONS[i], true));
        }

        icons.put(BrowserConstants.DATABASE_OBJECT_IMAGE,
                GUIUtilities.loadIcon(BrowserConstants.DATABASE_OBJECT_IMAGE, true));

        return icons;
    }

    public Icon getIconFromType(int type) {
        switch (type) {

            case NamedObject.ROOT:
                return (icons.get(
                        BrowserConstants.CONNECTIONS_IMAGE));


            case NamedObject.BRANCH_NODE:
                return (icons.get(
                        BrowserConstants.CONNECTIONS_FOLDER_IMAGE));


            case NamedObject.CATALOG:
                return (icons.get(BrowserConstants.CATALOG_IMAGE));


            case NamedObject.SCHEMA:
                return (icons.get(BrowserConstants.SCHEMA_IMAGE));


            case NamedObject.SYSTEM_FUNCTION:

            case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
            case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
            case NamedObject.SYSTEM_STRING_FUNCTIONS:
                return (icons.get(BrowserConstants.SYSTEM_FUNCTIONS_IMAGE));


            case NamedObject.FUNCTION:
                return (icons.get(BrowserConstants.FUNCTIONS_IMAGE));


            case NamedObject.INDEX:
            case NamedObject.TABLE_INDEX:
                return (icons.get(BrowserConstants.INDEXES_IMAGE));


            case NamedObject.PROCEDURE:
                return (icons.get(BrowserConstants.PROCEDURES_IMAGE));


            case NamedObject.SEQUENCE:
                return (icons.get(BrowserConstants.SEQUENCES_IMAGE));


            case NamedObject.SYSTEM_SEQUENCE:
                return (icons.get(BrowserConstants.SYSTEM_SEQUENCES_IMAGE));


            case NamedObject.SYNONYM:
                return (icons.get(BrowserConstants.SYNONYMS_IMAGE));


            case NamedObject.VIEW:
                return (icons.get(BrowserConstants.VIEWS_IMAGE));


            case NamedObject.SYSTEM_VIEW:
                return (icons.get(BrowserConstants.SYSTEM_VIEWS_IMAGE));


            case NamedObject.SYSTEM_TABLE:
                return (icons.get(BrowserConstants.SYSTEM_TABLES_IMAGE));


            case NamedObject.TRIGGER:
                return (icons.get(BrowserConstants.TABLE_TRIGGER_IMAGE));


            case NamedObject.DDL_TRIGGER:
                return (icons.get(BrowserConstants.DDL_TRIGGER_IMAGE));


            case NamedObject.PACKAGE:
                return (icons.get(BrowserConstants.PACKAGE_IMAGE));


            case NamedObject.SYSTEM_PACKAGE:
                return (icons.get(BrowserConstants.SYSTEM_PACKAGE_IMAGE));


            case NamedObject.DOMAIN:
                return (icons.get(BrowserConstants.DOMAIN_IMAGE));

            case NamedObject.ROLE:
                return (icons.get(BrowserConstants.ROLE_IMAGE));


            case NamedObject.SYSTEM_ROLE:
                return (icons.get(BrowserConstants.SYSTEM_ROLE_IMAGE));


            case NamedObject.USER:
                return (icons.get(BrowserConstants.USER_IMAGE));


            case NamedObject.TABLESPACE:
                return (icons.get(BrowserConstants.TABLESPACE_IMAGE));


            case NamedObject.JOB:
                return (icons.get(BrowserConstants.JOB_IMAGE));


            case NamedObject.EXCEPTION:
                return (icons.get(BrowserConstants.EXCEPTION_IMAGE));


            case NamedObject.UDF:
                return (icons.get(BrowserConstants.UDF_IMAGE));


            case NamedObject.TABLE:
                return (icons.get(BrowserConstants.TABLES_IMAGE));


            case NamedObject.GLOBAL_TEMPORARY:
                return (icons.get(BrowserConstants.GLOBAL_TABLES_IMAGE));


            case NamedObject.FOREIGN_KEYS_FOLDER_NODE:
                return (icons.get(BrowserConstants.FOLDER_FOREIGN_KEYS_IMAGE));


            case NamedObject.PRIMARY_KEYS_FOLDER_NODE:
                return (icons.get(BrowserConstants.FOLDER_PRIMARY_KEYS_IMAGE));


            case NamedObject.COLUMNS_FOLDER_NODE:
                return (icons.get(BrowserConstants.FOLDER_COLUMNS_IMAGE));


            case NamedObject.INDEXES_FOLDER_NODE:
                return (icons.get(BrowserConstants.FOLDER_INDEXES_IMAGE));


            case NamedObject.DATABASE_TRIGGER:
                return (icons.get(BrowserConstants.DB_TRIGGER_IMAGE));


            case NamedObject.SYSTEM_TRIGGER:
                return (icons.get(BrowserConstants.SYSTEM_TRIGGER_IMAGE));


            case NamedObject.SYSTEM_INDEX:
                return (icons.get(BrowserConstants.SYSTEM_INDEX_IMAGE));


            case NamedObject.SYSTEM_DOMAIN:
                return (icons.get(BrowserConstants.SYSTEM_DOMAIN_IMAGE));


            case NamedObject.PRIMARY_KEY:
                return (icons.get(BrowserConstants.PRIMARY_COLUMNS_IMAGE));


            case NamedObject.FOREIGN_KEY:
                return (icons.get(BrowserConstants.FOREIGN_COLUMNS_IMAGE));


            case NamedObject.UNIQUE_KEY:
                return (icons.get(BrowserConstants.COLUMNS_IMAGE));

            case NamedObject.COLLATION:
                return icons.get(BrowserConstants.COLLATION_IMAGE);


            default:
                return (icons.get(BrowserConstants.DATABASE_OBJECT_IMAGE));


        }
    }

    public Icon getIconFromNode(DatabaseObjectNode node) {
        int type = node.getType();
        NamedObject databaseObject = node.getDatabaseObject();
        switch (type) {
            case NamedObject.HOST:
                return getHostIcon((DatabaseHostNode) node);
            case NamedObject.TABLE_COLUMN:
                return getTableColumnIcon((DatabaseColumn) databaseObject);
            case NamedObject.META_TAG:
                return getIconFromMetaTag(databaseObject.getMetaDataKey());
            default:
                return getIconFromType(type);
        }
    }

    public Icon getHostIcon(DatabaseHostNode hostNode) {

        if (hostNode.isConnected()) {
            return (icons.get(
                    BrowserConstants.HOST_CONNECTED_IMAGE));
        } else {
            return (icons.get(
                    BrowserConstants.HOST_NOT_CONNECTED_IMAGE));
        }
    }

    public Icon getTableColumnIcon(DatabaseColumn databaseColumn) {

        if (databaseColumn.isPrimaryKey()) {

            return (icons.get(BrowserConstants.PRIMARY_COLUMNS_IMAGE));

        } else if (databaseColumn.isForeignKey()) {

            return (icons.get(BrowserConstants.FOREIGN_COLUMNS_IMAGE));

        } else {

            return (icons.get(BrowserConstants.COLUMNS_IMAGE));
        }
    }

    public Icon getIconFromMetaTag(String metatag) {
        if (metatag.compareToIgnoreCase("index") == 0) {
            return (icons.get(BrowserConstants.INDEXES_IMAGE));

        }
        if (metatag.compareToIgnoreCase("procedure") == 0) {
            return (icons.get(BrowserConstants.PROCEDURES_IMAGE));

        }
        if (metatag.compareToIgnoreCase("system table") == 0) {
            return (icons.get(BrowserConstants.SYSTEM_TABLES_IMAGE));

        }
        if (metatag.compareToIgnoreCase("table") == 0) {
            return (icons.get(BrowserConstants.TABLES_IMAGE));

        }
        if (metatag.compareToIgnoreCase("view") == 0) {
            return (icons.get(BrowserConstants.VIEWS_IMAGE));

        }
        if (metatag.compareToIgnoreCase("trigger") == 0) {
            return (icons.get(BrowserConstants.TABLE_TRIGGER_IMAGE));

        }
        if (metatag.compareToIgnoreCase("ddl trigger") == 0) {
            return (icons.get(BrowserConstants.DDL_TRIGGER_IMAGE));

        }
        if (metatag.compareToIgnoreCase("global temporary") == 0) {
            return (icons.get(BrowserConstants.GLOBAL_TABLES_IMAGE));

        }
        if (metatag.compareToIgnoreCase("system functions") == 0) {
            return (icons.get(BrowserConstants.SYSTEM_FUNCTIONS_IMAGE));

        }
        if (metatag.compareToIgnoreCase("sequence") == 0) {
            return (icons.get(BrowserConstants.SEQUENCES_IMAGE));

        }
        if (metatag.compareToIgnoreCase("system sequence") == 0) {
            return (icons.get(BrowserConstants.SYSTEM_SEQUENCES_IMAGE));

        }
        if (metatag.compareToIgnoreCase("domain") == 0) {
            return (icons.get(BrowserConstants.DOMAIN_IMAGE));

        }
        if (metatag.compareToIgnoreCase("role") == 0) {
            return (icons.get(BrowserConstants.ROLE_IMAGE));

        }
        if (metatag.compareToIgnoreCase("system role") == 0) {
            return (icons.get(BrowserConstants.SYSTEM_ROLE_IMAGE));

        }
        if (metatag.compareToIgnoreCase("user") == 0) {
            return (icons.get(BrowserConstants.USER_IMAGE));

        }
        if (metatag.compareToIgnoreCase("tablespace") == 0) {
            return (icons.get(BrowserConstants.TABLESPACE_IMAGE));

        }
        if (metatag.compareToIgnoreCase("job") == 0) {
            return (icons.get(BrowserConstants.JOB_IMAGE));

        }
        if (metatag.compareToIgnoreCase("exception") == 0) {
            return (icons.get(BrowserConstants.EXCEPTION_IMAGE));

        }
        if (metatag.compareToIgnoreCase("external function") == 0) {
            return (icons.get(BrowserConstants.UDF_IMAGE));

        }
        if (metatag.compareToIgnoreCase("system domain") == 0) {
            return (icons.get(BrowserConstants.SYSTEM_DOMAIN_IMAGE));

        }
        if (metatag.compareToIgnoreCase("system index") == 0) {
            return (icons.get(BrowserConstants.SYSTEM_INDEX_IMAGE));

        }
        if (metatag.compareToIgnoreCase("system trigger") == 0) {
            return (icons.get(BrowserConstants.SYSTEM_TRIGGER_IMAGE));

        }
        if (metatag.compareToIgnoreCase("database trigger") == 0) {
            return (icons.get(BrowserConstants.DB_TRIGGER_IMAGE));

        }
        if (metatag.compareToIgnoreCase("package") == 0) {
            return (icons.get(BrowserConstants.PACKAGE_IMAGE));

        }
        if (metatag.compareToIgnoreCase("system package") == 0) {
            return (icons.get(BrowserConstants.SYSTEM_PACKAGE_IMAGE));

        }
        if (metatag.compareToIgnoreCase("function") == 0) {
            return (icons.get(BrowserConstants.FUNCTIONS_IMAGE));

        }
        if (metatag.compareToIgnoreCase("system view") == 0) {
            return (icons.get(BrowserConstants.SYSTEM_VIEWS_IMAGE));

        }

        return (icons.get(BrowserConstants.DATABASE_OBJECT_IMAGE));
    }
}
