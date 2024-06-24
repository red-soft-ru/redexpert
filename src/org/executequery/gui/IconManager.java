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

        Map<String, Icon> icons = new HashMap<>();
        icons.put(
                BrowserConstants.DATABASE_OBJECT_IMAGE,
                GUIUtilities.loadIcon(BrowserConstants.DATABASE_OBJECT_IMAGE, true)
        );

        for (int i = 0; i < BrowserConstants.NODE_ICONS.length; i++) {
            icons.put(
                    BrowserConstants.NODE_ICONS[i],
                    GUIUtilities.loadIcon(
                            BrowserConstants.NODE_ICONS[i],
                            true
                    )
            );
        }

        icons.put(BrowserConstants.SYSTEM_DOMAIN_IMAGE_LIGHT, icons.get(BrowserConstants.DOMAIN_IMAGE_LIGHT));
        icons.put(BrowserConstants.SYSTEM_TABLES_IMAGE_LIGHT, icons.get(BrowserConstants.TABLES_IMAGE_LIGHT));
        icons.put(BrowserConstants.SYSTEM_VIEWS_IMAGE_LIGHT, icons.get(BrowserConstants.VIEWS_IMAGE_LIGHT));
        icons.put(BrowserConstants.SYSTEM_INDEX_IMAGE_LIGHT, icons.get(BrowserConstants.INDEXES_IMAGE_LIGHT));
        icons.put(BrowserConstants.SYSTEM_TRIGGER_IMAGE_LIGHT, icons.get(BrowserConstants.TABLE_TRIGGER_IMAGE_LIGHT));
        icons.put(BrowserConstants.SYSTEM_FUNCTIONS_IMAGE_LIGHT, icons.get(BrowserConstants.FUNCTIONS_IMAGE_LIGHT));
        icons.put(BrowserConstants.SYSTEM_SEQUENCES_IMAGE_LIGHT, icons.get(BrowserConstants.SEQUENCES_IMAGE_LIGHT));
        icons.put(BrowserConstants.SYSTEM_PACKAGE_IMAGE_LIGHT, icons.get(BrowserConstants.PACKAGE_IMAGE_LIGHT));
        icons.put(BrowserConstants.SYSTEM_ROLE_IMAGE_LIGHT, icons.get(BrowserConstants.ROLE_IMAGE_LIGHT));

        return icons;
    }

    public Icon getIconFromType(int type) {
        return getIconFromType(type, false);
    }

    public Icon getIconFromType(int type, boolean light) {

        String iconName;
        boolean system = false;

        switch (type) {

            case NamedObject.ROOT:
                iconName = BrowserConstants.CONNECTIONS_IMAGE;
                break;

            case NamedObject.BRANCH_NODE:
                iconName = BrowserConstants.CONNECTIONS_FOLDER_IMAGE;
                break;

            case NamedObject.CATALOG:
                iconName = BrowserConstants.CATALOG_IMAGE;
                break;

            case NamedObject.SCHEMA:
                iconName = BrowserConstants.SCHEMA_IMAGE;
                break;

            case NamedObject.SYSTEM_FUNCTION:
            case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
            case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
            case NamedObject.SYSTEM_STRING_FUNCTIONS:
                iconName = BrowserConstants.SYSTEM_FUNCTIONS_IMAGE;
                system = true;
                break;

            case NamedObject.FUNCTION:
                iconName = BrowserConstants.FUNCTIONS_IMAGE;
                break;

            case NamedObject.INDEX:
            case NamedObject.TABLE_INDEX:
                iconName = BrowserConstants.INDEXES_IMAGE;
                break;

            case NamedObject.PROCEDURE:
                iconName = BrowserConstants.PROCEDURES_IMAGE;
                break;

            case NamedObject.SEQUENCE:
                iconName = BrowserConstants.SEQUENCES_IMAGE;
                break;

            case NamedObject.SYSTEM_SEQUENCE:
                iconName = BrowserConstants.SYSTEM_SEQUENCES_IMAGE;
                system = true;
                break;

            case NamedObject.SYNONYM:
                iconName = BrowserConstants.SYNONYMS_IMAGE;
                break;

            case NamedObject.VIEW:
                iconName = BrowserConstants.VIEWS_IMAGE;
                break;

            case NamedObject.SYSTEM_VIEW:
                iconName = BrowserConstants.SYSTEM_VIEWS_IMAGE;
                system = true;
                break;

            case NamedObject.SYSTEM_TABLE:
                iconName = BrowserConstants.SYSTEM_TABLES_IMAGE;
                system = true;
                break;

            case NamedObject.TRIGGER:
                iconName = BrowserConstants.TABLE_TRIGGER_IMAGE;
                break;

            case NamedObject.DDL_TRIGGER:
                iconName = BrowserConstants.DDL_TRIGGER_IMAGE;
                break;

            case NamedObject.PACKAGE:
                iconName = BrowserConstants.PACKAGE_IMAGE;
                break;

            case NamedObject.SYSTEM_PACKAGE:
                iconName = BrowserConstants.SYSTEM_PACKAGE_IMAGE;
                system = true;
                break;

            case NamedObject.DOMAIN:
                iconName = BrowserConstants.DOMAIN_IMAGE;
                break;

            case NamedObject.ROLE:
                iconName = BrowserConstants.ROLE_IMAGE;
                break;

            case NamedObject.SYSTEM_ROLE:
                iconName = BrowserConstants.SYSTEM_ROLE_IMAGE;
                system = true;
                break;

            case NamedObject.USER:
                iconName = BrowserConstants.USER_IMAGE;
                break;

            case NamedObject.TABLESPACE:
                iconName = BrowserConstants.TABLESPACE_IMAGE;
                break;

            case NamedObject.JOB:
                iconName = BrowserConstants.JOB_IMAGE;
                break;

            case NamedObject.EXCEPTION:
                iconName = BrowserConstants.EXCEPTION_IMAGE;
                break;

            case NamedObject.UDF:
                iconName = BrowserConstants.UDF_IMAGE;
                break;

            case NamedObject.TABLE:
                iconName = BrowserConstants.TABLES_IMAGE;
                break;

            case NamedObject.GLOBAL_TEMPORARY:
                iconName = BrowserConstants.GLOBAL_TABLES_IMAGE;
                break;

            case NamedObject.FOREIGN_KEYS_FOLDER_NODE:
                iconName = BrowserConstants.FOLDER_FOREIGN_KEYS_IMAGE;
                break;

            case NamedObject.PRIMARY_KEYS_FOLDER_NODE:
                iconName = BrowserConstants.FOLDER_PRIMARY_KEYS_IMAGE;
                break;

            case NamedObject.COLUMNS_FOLDER_NODE:
                iconName = BrowserConstants.FOLDER_COLUMNS_IMAGE;
                break;

            case NamedObject.INDEXES_FOLDER_NODE:
                iconName = BrowserConstants.FOLDER_INDEXES_IMAGE;
                break;

            case NamedObject.DATABASE_TRIGGER:
                iconName = BrowserConstants.DB_TRIGGER_IMAGE;
                break;

            case NamedObject.SYSTEM_TRIGGER:
                iconName = BrowserConstants.SYSTEM_TRIGGER_IMAGE;
                system = true;
                break;

            case NamedObject.SYSTEM_INDEX:
                iconName = BrowserConstants.SYSTEM_INDEX_IMAGE;
                system = true;
                break;

            case NamedObject.SYSTEM_DOMAIN:
                iconName = BrowserConstants.SYSTEM_DOMAIN_IMAGE;
                system = true;
                break;

            case NamedObject.PRIMARY_KEY:
                iconName = BrowserConstants.PRIMARY_COLUMNS_IMAGE;
                break;

            case NamedObject.FOREIGN_KEY:
                iconName = BrowserConstants.FOREIGN_COLUMNS_IMAGE;
                break;

            case NamedObject.UNIQUE_KEY:
                iconName = BrowserConstants.COLUMNS_IMAGE;
                break;

            default:
                iconName = BrowserConstants.DATABASE_OBJECT_IMAGE;
                break;
        }

        return getIcon(iconName, light, system);
    }

    public Icon getIconFromNode(DatabaseObjectNode node, boolean light) {

        int type = node.getType();
        switch (node.getType()) {

            case NamedObject.HOST:
                return getHostIcon((DatabaseHostNode) node, light);

            case NamedObject.TABLE_COLUMN:
                return getTableColumnIcon((DatabaseColumn) node.getDatabaseObject(), light);

            case NamedObject.META_TAG:
                return getIconFromMetaTag(node.getDatabaseObject().getMetaDataKey(), light);

            default:
                return getIconFromType(type, light);
        }
    }

    public Icon getHostIcon(DatabaseHostNode hostNode, boolean light) {

        String iconName = BrowserConstants.HOST_NOT_CONNECTED_IMAGE;
        if (hostNode.isConnected())
            iconName = BrowserConstants.HOST_CONNECTED_IMAGE;

        return getIcon(iconName, light, false);
    }

    public Icon getTableColumnIcon(DatabaseColumn databaseColumn) {
        return getTableColumnIcon(databaseColumn, false);
    }

    public Icon getTableColumnIcon(DatabaseColumn databaseColumn, boolean light) {

        String iconName;
        if (databaseColumn.isPrimaryKey()) {
            iconName = BrowserConstants.PRIMARY_COLUMNS_IMAGE;

        } else if (databaseColumn.isForeignKey()) {
            iconName = BrowserConstants.FOREIGN_COLUMNS_IMAGE;

        } else
            iconName = BrowserConstants.COLUMNS_IMAGE;

        return getIcon(iconName, light, false);
    }

    public Icon getIconFromMetaTag(String metadata, boolean light) {

        String iconName;
        boolean system = false;

        if (metadata.compareToIgnoreCase("index") == 0) {
            iconName = BrowserConstants.INDEXES_IMAGE;

        } else if (metadata.compareToIgnoreCase("procedure") == 0) {
            iconName = BrowserConstants.PROCEDURES_IMAGE;

        } else if (metadata.compareToIgnoreCase("system table") == 0) {
            iconName = BrowserConstants.SYSTEM_TABLES_IMAGE;
            system = true;

        } else if (metadata.compareToIgnoreCase("table") == 0) {
            iconName = BrowserConstants.TABLES_IMAGE;

        } else if (metadata.compareToIgnoreCase("view") == 0) {
            iconName = BrowserConstants.VIEWS_IMAGE;

        } else if (metadata.compareToIgnoreCase("trigger") == 0) {
            iconName = BrowserConstants.TABLE_TRIGGER_IMAGE;

        } else if (metadata.compareToIgnoreCase("ddl trigger") == 0) {
            iconName = BrowserConstants.DDL_TRIGGER_IMAGE;

        } else if (metadata.compareToIgnoreCase("global temporary") == 0) {
            iconName = BrowserConstants.GLOBAL_TABLES_IMAGE;

        } else if (metadata.compareToIgnoreCase("system functions") == 0) {
            iconName = BrowserConstants.SYSTEM_FUNCTIONS_IMAGE;
            system = true;

        } else if (metadata.compareToIgnoreCase("sequence") == 0) {
            iconName = BrowserConstants.SEQUENCES_IMAGE;

        } else if (metadata.compareToIgnoreCase("system sequence") == 0) {
            iconName = BrowserConstants.SYSTEM_SEQUENCES_IMAGE;
            system = true;

        } else if (metadata.compareToIgnoreCase("domain") == 0) {
            iconName = BrowserConstants.DOMAIN_IMAGE;

        } else if (metadata.compareToIgnoreCase("role") == 0) {
            iconName = BrowserConstants.ROLE_IMAGE;

        } else if (metadata.compareToIgnoreCase("system role") == 0) {
            iconName = BrowserConstants.SYSTEM_ROLE_IMAGE;
            system = true;

        } else if (metadata.compareToIgnoreCase("user") == 0) {
            iconName = BrowserConstants.USER_IMAGE;

        } else if (metadata.compareToIgnoreCase("tablespace") == 0) {
            iconName = BrowserConstants.TABLESPACE_IMAGE;

        } else if (metadata.compareToIgnoreCase("job") == 0) {
            iconName = BrowserConstants.JOB_IMAGE;

        } else if (metadata.compareToIgnoreCase("exception") == 0) {
            iconName = BrowserConstants.EXCEPTION_IMAGE;

        } else if (metadata.compareToIgnoreCase("external function") == 0) {
            iconName = BrowserConstants.UDF_IMAGE;

        } else if (metadata.compareToIgnoreCase("system domain") == 0) {
            iconName = BrowserConstants.SYSTEM_DOMAIN_IMAGE;
            system = true;

        } else if (metadata.compareToIgnoreCase("system index") == 0) {
            iconName = BrowserConstants.SYSTEM_INDEX_IMAGE;
            system = true;

        } else if (metadata.compareToIgnoreCase("system trigger") == 0) {
            iconName = BrowserConstants.SYSTEM_TRIGGER_IMAGE;
            system = true;

        } else if (metadata.compareToIgnoreCase("database trigger") == 0) {
            iconName = BrowserConstants.DB_TRIGGER_IMAGE;

        } else if (metadata.compareToIgnoreCase("package") == 0) {
            iconName = BrowserConstants.PACKAGE_IMAGE;

        } else if (metadata.compareToIgnoreCase("system package") == 0) {
            iconName = BrowserConstants.SYSTEM_PACKAGE_IMAGE;
            system = true;

        } else if (metadata.compareToIgnoreCase("function") == 0) {
            iconName = BrowserConstants.FUNCTIONS_IMAGE;

        } else if (metadata.compareToIgnoreCase("system view") == 0) {
            iconName = BrowserConstants.SYSTEM_VIEWS_IMAGE;
            system = true;

        } else
            iconName = BrowserConstants.DATABASE_OBJECT_IMAGE;

        return getIcon(iconName, light, system);
    }

    private Icon getIcon(String iconName, boolean isLight, boolean isSystem) {

        if (isLight || (GUIUtilities.getLookAndFeel().isDarkTheme() && !isSystem))
            if (icons.containsKey(iconName + BrowserConstants.LIGHT_SUFFIX))
                iconName += BrowserConstants.LIGHT_SUFFIX;

        return icons.get(iconName);
    }

}
