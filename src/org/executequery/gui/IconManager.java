package org.executequery.gui;

import org.executequery.ExecuteQuery;
import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.BrowserConstants;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.log.Log;
import org.underworldlabs.swing.plaf.SVGImage;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class IconManager {

    private static final Map<String, ImageIcon> iconsDark = new HashMap<>();
    private static final Map<String, ImageIcon> iconsLight = new HashMap<>();
    private static final boolean isDarkTheme = GUIUtilities.getLookAndFeel().isDarkTheme();
    private static final boolean isDefaultTheme = GUIUtilities.getLookAndFeel().isDefaultTheme();

    /**
     * Loads all needed for selected LAF icons
     * and put them to the maps
     */
    public static void loadIcons() {

        if (isDefaultTheme) {
            loadIcons("icons/default/dark/", iconsDark);
            loadIcons("icons/default/light/", iconsLight);
            return;
        }

        loadIcons("icons/classic/", iconsLight);
    }

    private static void loadIcons(String resourceName, Map<String, ImageIcon> iconMap) {

        URL iconsResource = ExecuteQuery.class.getResource(resourceName);
        if (iconsResource == null)
            return;

        final String basePath = "/org/executequery/" + resourceName;
        try {
            Path iconsPath = new File(iconsResource.toURI()).toPath();
            try (Stream<Path> iconsStream = Files.walk(iconsPath)) {
                iconsStream.map(Path::toFile)
                        .filter(File::isFile)
                        .map(File::getName)
                        .forEach(name -> iconMap.put(
                                name.replaceAll("[.]\\w+$", ""),
                                loadIcon(basePath + name, 18)
                        ));
            }
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    private static ImageIcon loadIcon(String iconPath, int iconSize) {

        URL url = ExecuteQuery.class.getResource(iconPath);
        if (url == null) {
            Log.info("icon with path [" + iconPath + "] not found");
            return null;
        }

        if (!url.getPath().endsWith(".svg"))
            return new ImageIcon(url);

        try {
            BufferedImage image = SVGImage.fromSvg(url, iconSize, iconSize);
            return new ImageIcon(image);

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Returns loaded icon by its name
     * without extension for the selected LAF
     *
     * @param iconName the name of the icon that should be returned
     */
    public static ImageIcon getIcon(String iconName) {
        if (isDefaultTheme && isDarkTheme)
            return iconsDark.get(iconName);
        return iconsLight.get(iconName);
    }

    private static Icon getIcon(String iconName, boolean fromDarkTheme) {
        if ((isDefaultTheme && fromDarkTheme) || isDarkTheme)
            return iconsDark.get(iconName);
        return iconsLight.get(iconName);
    }

    public static Icon getVectorIcon(String iconName) {
        return getVectorIcon(iconName, 18);
    }

    public static Icon getVectorIcon(String iconName, int iconSize) {
        iconName = "/org/executequery/icons/default/dark/" + iconName + ".svg";
        return loadIcon(iconName, iconSize);
    }

    public static Icon getIconFromNode(DatabaseObjectNode node, boolean isSelected) {

        int type = node.getType();
        switch (type) {

            case NamedObject.HOST:
                return getIconHostNode((DatabaseHostNode) node, isSelected);

            case NamedObject.TABLE_COLUMN:
                return getIconFromDatabaseColumn((DatabaseColumn) node.getDatabaseObject(), isSelected);

            case NamedObject.META_TAG:
                return getIconFromMetaTag(node.getDatabaseObject().getMetaDataKey(), isSelected);

            default:
                return getIconFromType(type, isSelected);
        }
    }

    public static Icon getIconFromType(int type) {
        return getIconFromType(type, false);
    }

    public static Icon getIconFromDatabaseColumn(DatabaseColumn databaseColumn) {
        return getIconFromDatabaseColumn(databaseColumn, false);
    }

    private static Icon getIconFromDatabaseColumn(DatabaseColumn databaseColumn, boolean isLight) {

        String iconName = databaseColumn.isPrimaryKey() ?
                BrowserConstants.PRIMARY_COLUMNS_IMAGE :
                BrowserConstants.COLUMNS_IMAGE;

        return getIcon(iconName, isLight);
    }

    private static Icon getIconHostNode(DatabaseHostNode hostNode, boolean isLight) {

        String iconName = BrowserConstants.HOST_NOT_CONNECTED_IMAGE;
        if (hostNode.isConnected())
            iconName = BrowserConstants.HOST_CONNECTED_IMAGE;

        return getIcon(iconName, isLight);
    }

    private static Icon getIconFromType(int type, boolean isSelected) {
        String iconName;

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

            case NamedObject.SYNONYM:
                iconName = BrowserConstants.SYNONYMS_IMAGE;
                break;

            case NamedObject.VIEW:
                iconName = BrowserConstants.VIEWS_IMAGE;
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

            case NamedObject.DOMAIN:
                iconName = BrowserConstants.DOMAIN_IMAGE;
                break;

            case NamedObject.ROLE:
                iconName = BrowserConstants.ROLE_IMAGE;
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

            case NamedObject.PRIMARY_KEY:
                iconName = BrowserConstants.PRIMARY_COLUMNS_IMAGE;
                break;

            case NamedObject.FOREIGN_KEY:
            case NamedObject.UNIQUE_KEY:
                iconName = BrowserConstants.COLUMNS_IMAGE;
                break;

            case NamedObject.SYSTEM_FUNCTION:
            case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
            case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
            case NamedObject.SYSTEM_STRING_FUNCTIONS:
                iconName = isSelected ?
                        BrowserConstants.FUNCTIONS_IMAGE :
                        BrowserConstants.SYSTEM_FUNCTIONS_IMAGE;
                break;

            case NamedObject.SYSTEM_TRIGGER:
                iconName = isSelected ?
                        BrowserConstants.TABLE_TRIGGER_IMAGE :
                        BrowserConstants.SYSTEM_TRIGGER_IMAGE;
                break;

            case NamedObject.SYSTEM_INDEX:
                iconName = isSelected ?
                        BrowserConstants.INDEXES_IMAGE :
                        BrowserConstants.SYSTEM_INDEX_IMAGE;
                break;

            case NamedObject.SYSTEM_DOMAIN:
                iconName = isSelected ?
                        BrowserConstants.DOMAIN_IMAGE :
                        BrowserConstants.SYSTEM_DOMAIN_IMAGE;
                break;

            case NamedObject.SYSTEM_SEQUENCE:
                iconName = isSelected ?
                        BrowserConstants.SEQUENCES_IMAGE :
                        BrowserConstants.SYSTEM_SEQUENCES_IMAGE;
                break;

            case NamedObject.SYSTEM_VIEW:
                iconName = isSelected ?
                        BrowserConstants.VIEWS_IMAGE :
                        BrowserConstants.SYSTEM_VIEWS_IMAGE;
                break;

            case NamedObject.SYSTEM_TABLE:
                iconName = isSelected ?
                        BrowserConstants.TABLES_IMAGE :
                        BrowserConstants.SYSTEM_TABLES_IMAGE;
                break;

            case NamedObject.SYSTEM_PACKAGE:
                iconName = isSelected ?
                        BrowserConstants.PACKAGE_IMAGE :
                        BrowserConstants.SYSTEM_PACKAGE_IMAGE;
                break;

            case NamedObject.SYSTEM_ROLE:
                iconName = isSelected ?
                        BrowserConstants.ROLE_IMAGE :
                        BrowserConstants.SYSTEM_ROLE_IMAGE;
                break;

            default:
                iconName = BrowserConstants.DATABASE_OBJECT_IMAGE;
                break;
        }

        return getIcon(iconName, isSelected);
    }

    private static Icon getIconFromMetaTag(String metadata, boolean isSelected) {
        String iconName;

        if (metadata.compareToIgnoreCase("index") == 0) {
            iconName = BrowserConstants.INDEXES_IMAGE;

        } else if (metadata.compareToIgnoreCase("procedure") == 0) {
            iconName = BrowserConstants.PROCEDURES_IMAGE;

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

        } else if (metadata.compareToIgnoreCase("sequence") == 0) {
            iconName = BrowserConstants.SEQUENCES_IMAGE;

        } else if (metadata.compareToIgnoreCase("domain") == 0) {
            iconName = BrowserConstants.DOMAIN_IMAGE;

        } else if (metadata.compareToIgnoreCase("role") == 0) {
            iconName = BrowserConstants.ROLE_IMAGE;

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

        } else if (metadata.compareToIgnoreCase("database trigger") == 0) {
            iconName = BrowserConstants.DB_TRIGGER_IMAGE;

        } else if (metadata.compareToIgnoreCase("package") == 0) {
            iconName = BrowserConstants.PACKAGE_IMAGE;

        } else if (metadata.compareToIgnoreCase("function") == 0) {
            iconName = BrowserConstants.FUNCTIONS_IMAGE;

        } else if (metadata.compareToIgnoreCase("system view") == 0) {
            iconName = isSelected ?
                    BrowserConstants.VIEWS_IMAGE :
                    BrowserConstants.SYSTEM_VIEWS_IMAGE;

        } else if (metadata.compareToIgnoreCase("system package") == 0) {
            iconName = isSelected ?
                    BrowserConstants.PACKAGE_IMAGE :
                    BrowserConstants.SYSTEM_PACKAGE_IMAGE;

        } else if (metadata.compareToIgnoreCase("system domain") == 0) {
            iconName = isSelected ?
                    BrowserConstants.DOMAIN_IMAGE :
                    BrowserConstants.SYSTEM_DOMAIN_IMAGE;

        } else if (metadata.compareToIgnoreCase("system index") == 0) {
            iconName = isSelected ?
                    BrowserConstants.INDEXES_IMAGE :
                    BrowserConstants.SYSTEM_INDEX_IMAGE;

        } else if (metadata.compareToIgnoreCase("system trigger") == 0) {
            iconName = isSelected ?
                    BrowserConstants.TABLE_TRIGGER_IMAGE :
                    BrowserConstants.SYSTEM_TRIGGER_IMAGE;

        } else if (metadata.compareToIgnoreCase("system table") == 0) {
            iconName = isSelected ?
                    BrowserConstants.TABLES_IMAGE :
                    BrowserConstants.SYSTEM_TABLES_IMAGE;

        } else if (metadata.compareToIgnoreCase("system functions") == 0) {
            iconName = isSelected ?
                    BrowserConstants.FUNCTIONS_IMAGE :
                    BrowserConstants.SYSTEM_FUNCTIONS_IMAGE;

        } else if (metadata.compareToIgnoreCase("system sequence") == 0) {
            iconName = isSelected ?
                    BrowserConstants.SEQUENCES_IMAGE :
                    BrowserConstants.SYSTEM_SEQUENCES_IMAGE;

        } else if (metadata.compareToIgnoreCase("system role") == 0) {
            iconName = isSelected ?
                    BrowserConstants.ROLE_IMAGE :
                    BrowserConstants.SYSTEM_ROLE_IMAGE;

        } else
            iconName = BrowserConstants.DATABASE_OBJECT_IMAGE;

        return getIcon(iconName, isSelected);
    }

}
