/*
 * BrowserTreeCellRenderer.java
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

package org.executequery.components.table;

import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.browser.BrowserConstants;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Map;

/**
 * Tree cell renderer or the database browser.
 *
 * @author Takis Diakoumis
 */
public class BrowserTreeCellRenderer extends AbstractTreeCellRenderer {

    /**
     * Icon collection for nodes
     */
    private Map<String, Icon> icons;

    private Color textForeground;
    private Color selectedTextForeground;

    private Color selectedBackground;
    private Font treeFont;

    /**
     * Constructs a new instance and initialises any variables
     */
    public BrowserTreeCellRenderer(Map<String, Icon> icons) {

        this.icons = icons;

        textForeground = UIManager.getColor("Tree.textForeground");
        selectedTextForeground = UIManager.getColor("Tree.selectionForeground");
        selectedBackground = UIManager.getColor("Tree.selectionBackground");
        reloadFont();

        setIconTextGap(10);
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        if (UIUtils.isGtkLookAndFeel()) {

            // has default black border on selection - ugly and wrong!
            setBorderSelectionColor(null);
        }

        sb = new StringBuilder();
    }

    /**
     * Sets the value of the current tree cell to value. If
     * selected is true, the cell will be drawn as if selected.
     * If expanded is true the node is currently expanded and if
     * leaf is true the node represets a leaf and if hasFocus
     * is true the node currently has focus. tree is the JTree
     * the receiver is being configured for. Returns the Component
     * that the renderer uses to draw the value.
     *
     * @return the Component that the renderer uses to draw the value
     */
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean isSelected,
                                                  boolean isExpanded,
                                                  boolean isLeaf,
                                                  int row,
                                                  boolean hasFocus) {

        this.hasFocus = hasFocus;
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) value;

        DatabaseObjectNode node = (DatabaseObjectNode) child;
        int type = node.getType();

        String label = node.getDisplayName();
        NamedObject databaseObject = node.getDatabaseObject();

        switch (type) {

            case NamedObject.ROOT:
                setIcon(icons.get(
                        BrowserConstants.CONNECTIONS_IMAGE));
                break;

            case NamedObject.BRANCH_NODE:
                setIcon(icons.get(
                        BrowserConstants.CONNECTIONS_FOLDER_IMAGE));
                break;

            case NamedObject.HOST:
                DatabaseHostNode _node = (DatabaseHostNode) node;

                if (_node.isConnected()) {
                    setIcon(icons.get(
                            BrowserConstants.HOST_CONNECTED_IMAGE));
                } else {
                    setIcon(icons.get(
                            BrowserConstants.HOST_NOT_CONNECTED_IMAGE));
                }

                break;

            case NamedObject.CATALOG:
                setIcon(icons.get(BrowserConstants.CATALOG_IMAGE));
                break;

            case NamedObject.SCHEMA:
                setIcon(icons.get(BrowserConstants.SCHEMA_IMAGE));
                break;

            case NamedObject.META_TAG:
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("index") == 0) {
                    setIcon(icons.get(BrowserConstants.INDEXES_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("procedure") == 0) {
                    setIcon(icons.get(BrowserConstants.PROCEDURES_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("system table") == 0) {
                    setIcon(icons.get(BrowserConstants.SYSTEM_TABLES_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("table") == 0) {
                    setIcon(icons.get(BrowserConstants.TABLES_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("view") == 0) {
                    setIcon(icons.get(BrowserConstants.VIEWS_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("trigger") == 0) {
                    setIcon(icons.get(BrowserConstants.TABLE_TRIGGER_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("global temporary") == 0) {
                    setIcon(icons.get(BrowserConstants.GLOBAL_TABLES_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("system functions") == 0) {
                    setIcon(icons.get(BrowserConstants.SYSTEM_FUNCTIONS_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("sequence") == 0) {
                    setIcon(icons.get(BrowserConstants.SEQUENCES_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("domain") == 0) {
                    setIcon(icons.get(BrowserConstants.DOMAIN_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("role") == 0) {
                    setIcon(icons.get(BrowserConstants.ROLE_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("exception") == 0) {
                    setIcon(icons.get(BrowserConstants.EXCEPTION_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("external function") == 0) {
                    setIcon(icons.get(BrowserConstants.UDF_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("system domain") == 0) {
                    setIcon(icons.get(BrowserConstants.SYSTEM_DOMAIN_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("system index") == 0) {
                    setIcon(icons.get(BrowserConstants.SYSTEM_INDEX_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("system trigger") == 0) {
                    setIcon(icons.get(BrowserConstants.SYSTEM_TRIGGER_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("database trigger") == 0) {
                    setIcon(icons.get(BrowserConstants.SYSTEM_DATABASE_TRIGGER_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("package") == 0) {
                    setIcon(icons.get(BrowserConstants.PACKAGE_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("function") == 0) {
                    setIcon(icons.get(BrowserConstants.FUNCTIONS_IMAGE));
                    break;
                }
                if (databaseObject.getMetaDataKey().compareToIgnoreCase("system view") == 0) {
                    setIcon(icons.get(BrowserConstants.SYSTEM_VIEWS_IMAGE));
                    break;
                }

                setIcon(icons.get(BrowserConstants.DATABASE_OBJECT_IMAGE));
                break;

            case NamedObject.SYSTEM_FUNCTION:
                setIcon(icons.get(BrowserConstants.SYSTEM_FUNCTIONS_IMAGE));
                break;

            case NamedObject.FUNCTION:
                setIcon(icons.get(BrowserConstants.FUNCTIONS_IMAGE));
                break;

            case NamedObject.INDEX:
            case NamedObject.TABLE_INDEX:
                DefaultDatabaseIndex index = (DefaultDatabaseIndex) databaseObject;
                if (index.isActive())
                    setIcon(icons.get(BrowserConstants.INDEXES_ACTIVE_IMAGE));
                else
                    setIcon(icons.get(BrowserConstants.INDEXES_IMAGE));
                break;

            case NamedObject.PROCEDURE:
                setIcon(icons.get(BrowserConstants.PROCEDURES_IMAGE));
                break;

            case NamedObject.SEQUENCE:
                setIcon(icons.get(BrowserConstants.SEQUENCES_IMAGE));
                break;

            case NamedObject.SYNONYM:
                setIcon(icons.get(BrowserConstants.SYNONYMS_IMAGE));
                break;

            case NamedObject.VIEW:
                setIcon(icons.get(BrowserConstants.VIEWS_IMAGE));
                break;

            case NamedObject.SYSTEM_VIEW:
                setIcon(icons.get(BrowserConstants.SYSTEM_VIEWS_IMAGE));
                break;

            case NamedObject.SYSTEM_TABLE:
                setIcon(icons.get(BrowserConstants.SYSTEM_TABLES_IMAGE));
                break;

            case NamedObject.TRIGGER:
                DefaultDatabaseTrigger trigger = (DefaultDatabaseTrigger) databaseObject;
                if (trigger.isTriggerActive())
                    setIcon(icons.get(BrowserConstants.TABLE_TRIGGER_ACTIVE_IMAGE));
                else
                    setIcon(icons.get(BrowserConstants.TABLE_TRIGGER_IMAGE));
                break;

            case NamedObject.PACKAGE:
                setIcon(icons.get(BrowserConstants.PACKAGE_IMAGE));
                break;

            case NamedObject.DOMAIN:
                setIcon(icons.get(BrowserConstants.DOMAIN_IMAGE));
                break;
            case NamedObject.ROLE:
                setIcon(icons.get(BrowserConstants.ROLE_IMAGE));
                break;

            case NamedObject.EXCEPTION:
                setIcon(icons.get(BrowserConstants.EXCEPTION_IMAGE));
                break;

            case NamedObject.UDF:
                setIcon(icons.get(BrowserConstants.UDF_IMAGE));
                break;

            case NamedObject.TABLE:
                setIcon(icons.get(BrowserConstants.TABLES_IMAGE));
                break;

            case NamedObject.GLOBAL_TEMPORARY:
                setIcon(icons.get(BrowserConstants.GLOBAL_TABLES_IMAGE));
                break;

            case NamedObject.FOREIGN_KEYS_FOLDER_NODE:
                setIcon(icons.get(BrowserConstants.FOLDER_FOREIGN_KEYS_IMAGE));
                break;

            case NamedObject.PRIMARY_KEYS_FOLDER_NODE:
                setIcon(icons.get(BrowserConstants.FOLDER_PRIMARY_KEYS_IMAGE));
                break;

            case NamedObject.COLUMNS_FOLDER_NODE:
                setIcon(icons.get(BrowserConstants.FOLDER_COLUMNS_IMAGE));
                break;

            case NamedObject.INDEXES_FOLDER_NODE:
                setIcon(icons.get(BrowserConstants.FOLDER_INDEXES_IMAGE));
                break;

            case NamedObject.SYSTEM_DATABASE_TRIGGER:
                trigger = (DefaultDatabaseTrigger) databaseObject;
                if (trigger.isTriggerActive())
                    setIcon(icons.get(BrowserConstants.SYSTEM_DATABASE_TRIGGER_ACTIVE_IMAGE));
                else
                    setIcon(icons.get(BrowserConstants.SYSTEM_DATABASE_TRIGGER_IMAGE));
                break;

            case NamedObject.SYSTEM_TRIGGER:
                setIcon(icons.get(BrowserConstants.SYSTEM_TRIGGER_IMAGE));
                break;

            case NamedObject.SYSTEM_INDEX:
                setIcon(icons.get(BrowserConstants.SYSTEM_INDEX_IMAGE));
                break;

            case NamedObject.SYSTEM_DOMAIN:
                setIcon(icons.get(BrowserConstants.SYSTEM_DOMAIN_IMAGE));
                break;



            case NamedObject.TABLE_COLUMN:

                DatabaseColumn databaseColumn = (DatabaseColumn) databaseObject;

                if (databaseColumn.isPrimaryKey()) {

                    setIcon(icons.get(BrowserConstants.PRIMARY_COLUMNS_IMAGE));

                } else if (databaseColumn.isForeignKey()) {

                    setIcon(icons.get(BrowserConstants.FOREIGN_COLUMNS_IMAGE));

                } else {

                    setIcon(icons.get(BrowserConstants.COLUMNS_IMAGE));
                }

                break;

            case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
            case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
            case NamedObject.SYSTEM_STRING_FUNCTIONS:
                setIcon(icons.get(BrowserConstants.SYSTEM_FUNCTIONS_IMAGE));
                break;

            case NamedObject.PRIMARY_KEY:
                setIcon(icons.get(BrowserConstants.PRIMARY_COLUMNS_IMAGE));
                break;

            case NamedObject.FOREIGN_KEY:
                setIcon(icons.get(BrowserConstants.FOREIGN_COLUMNS_IMAGE));
                break;

            case NamedObject.UNIQUE_KEY:
                setIcon(icons.get(BrowserConstants.COLUMNS_IMAGE));
                break;

            default:
                setIcon(icons.get(BrowserConstants.DATABASE_OBJECT_IMAGE));
                break;

        }

        setText(label);

        if (type == BrowserConstants.HOST_NODE) {

            DatabaseConnection connection =
                    ((DatabaseHost) databaseObject).getDatabaseConnection();
            setToolTipText(buildToolTip(connection));

        } else {

            if (databaseObject != null) {

                setToolTipText(databaseObject.getDescription());

            } else {

                setToolTipText(label);
            }
        }

        setBackgroundSelectionColor(selectedBackground);

        this.selected = isSelected;
        if (!selected) {

            setForeground(textForeground);

        } else {

            setForeground(selectedTextForeground);
        }

        setFont(treeFont);
        JTree.DropLocation dropLocation = tree.getDropLocation();
        if (dropLocation != null && type == NamedObject.BRANCH_NODE
                && dropLocation.getChildIndex() == -1
                && tree.getRowForPath(dropLocation.getPath()) == row) {

            setForeground(selectedTextForeground);
            Color background = UIManager.getColor("Tree.dropCellBackground");
            if (background == null) {
                background = UIUtils.getBrighter(getBackgroundSelectionColor(), 0.87);
            }
            setBackgroundSelectionColor(background);

            selected = true;
        }
        return this;
    }

    /**
     * tool tip string buffer
     */
    private StringBuilder sb;

    /**
     * Builds a HTML tool tip describing this tree connection.
     *
     * @param connection object
     */
    private String buildToolTip(DatabaseConnection connection) {
        // reset
        sb.setLength(0);

        // build the html display
        sb.append("<html>");
        sb.append(Constants.TABLE_TAG_START);
        sb.append("<tr><td><b>");
        sb.append(connection.getName());
        sb.append("</b></td></tr>");
        sb.append(Constants.TABLE_TAG_END);
        sb.append("<hr noshade>");
        sb.append(Constants.TABLE_TAG_START);
        sb.append("<tr><td>Host:</td><td width='30'></td><td>");
        sb.append(connection.getHost());
        sb.append("</td></tr><td>Data Source:</td><td></td><td>");
        sb.append(connection.getSourceName());
        sb.append("</td></tr><td>User:</td><td></td><td>");
        sb.append(connection.getUserName());
        sb.append("</td></tr><td>Driver:</td><td></td><td>");
        sb.append(connection.getDriverName());
        sb.append("</td></tr>");
        sb.append(Constants.TABLE_TAG_END);
        sb.append("</html>");

        return sb.toString();
    }

    @Override
    public Icon getClosedIcon() {

        return getIcon();
    }

    @Override
    public Icon getOpenIcon() {

        return getIcon();
    }

    @Override
    public Icon getLeafIcon() {

        return getIcon();
    }

    public void reloadFont() {
        String nameFont = SystemProperties.getProperty("user", "treeconnection.font.name");
        if (!MiscUtils.isNull(nameFont)) {
            treeFont = new Font(nameFont, Font.PLAIN, Integer.parseInt(SystemProperties.getProperty("user", "treeconnection.font.size")));
        } else {
            treeFont = UIManager.getDefaults().getFont("Tree.font");
            SystemProperties.setProperty("user", "treeconnection.font.name", treeFont.getFontName());
            reloadFont();
        }
    }

}
