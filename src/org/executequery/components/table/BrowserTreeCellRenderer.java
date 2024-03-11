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
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.IconManager;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Enumeration;

/**
 * Tree cell renderer or the database browser.
 *
 * @author Takis Diakoumis
 */
public class BrowserTreeCellRenderer extends AbstractTreeCellRenderer {

    /**
     * Icon collection for nodes
     */


    private final Color textForeground;
    private final Color selectedTextForeground;
    private final Color disabledTextForeground;

    private final Color selectedBackground;
    private Font treeFont;

    /**
     * Constructs a new instance and initialises any variables
     */
    public BrowserTreeCellRenderer() {

        textForeground = UIManager.getColor("Tree.textForeground");
        selectedTextForeground = UIManager.getColor("Tree.selectionForeground");
        selectedBackground = UIManager.getColor("Tree.selectionBackground");
        disabledTextForeground = UIManager.getColor("Button.disabledText");
        reloadFont();

        setIconTextGap(10);
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        if (UIUtils.isGtkLookAndFeel()) {

            // has default black border on selection - ugly and wrong!
            setBorderSelectionColor(null);
        }

        sb = new StringBuilder();
    }

    public static void printUIManagerKeys() {
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<Object> keysEnumeration = defaults.keys();
        while (keysEnumeration.hasMoreElements()) {
            Object key = keysEnumeration.nextElement();
            System.out.println(key);
        }
    }

    /**
     * tool tip string buffer
     */
    private final StringBuilder sb;

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
    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean isSelected,
            boolean isExpanded,
            boolean isLeaf,
            int row,
            boolean hasFocus) {

        this.hasFocus = hasFocus;

        DefaultMutableTreeNode child = (DefaultMutableTreeNode) value;
        DatabaseObjectNode node = (DatabaseObjectNode) child;

        String label = node.getDisplayName();
        NamedObject databaseObject = node.getDatabaseObject();

        setIcon(IconManager.getInstance().getIconFromNode(node));

        int type = node.getType();
        if (type == NamedObject.HOST) {
            try {
                DatabaseConnection connection = ((DatabaseHost) databaseObject).getDatabaseConnection();
                setToolTipText(buildToolTip(connection));
            } catch (Exception e) {
                Log.error("Error genarating connection tooltip", e);
                setToolTipText(label);
            }

        } else if (databaseObject != null) {
            setToolTipText(databaseObject.getDescription());

        } else
            setToolTipText(label);

        setBackgroundSelectionColor(selectedBackground);

        this.selected = isSelected;
        if (!selected) {
            setForeground(textForeground);
            if (databaseObject != null)
                if (node.isSystem())
                    setForeground(Color.RED);
                else {
                    if (databaseObject instanceof DefaultDatabaseTrigger) {
                        DefaultDatabaseTrigger trigger = (DefaultDatabaseTrigger) databaseObject;
                        if (!trigger.isTriggerActive())
                            setForeground(disabledTextForeground);
                    }
                    if (databaseObject instanceof DefaultDatabaseIndex) {
                        DefaultDatabaseIndex index = (DefaultDatabaseIndex) databaseObject;
                        if (!index.isActive())
                            setForeground(disabledTextForeground);
                    }
                }

        } else {
            if (databaseObject != null) {
                if (node.isSystem())
                    setForeground(Color.RED);
                else
                    setForeground(selectedTextForeground);
            } else setForeground(selectedTextForeground);
        }
        if (type == NamedObject.META_TAG && node.getDatabaseObject().getObjects().size() > 0)
            setFont(treeFont.deriveFont(Font.BOLD));
        else setFont(treeFont);
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
        setText(label);
        return this;
    }

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
        sb.append("<tr><td>").append(bundleString("Host")).append("</td><td width='30'></td><td>");
        sb.append(connection.getHost());
        sb.append("</td></tr><td>").append(bundleString("DataSource")).append("</td><td></td><td>");
        sb.append(connection.getSourceName());
        sb.append("</td></tr><td>").append(bundleString("User")).append("</td><td></td><td>");
        sb.append(connection.getUserName());
        sb.append("</td></tr><td>").append(bundleString("Driver")).append("</td><td></td><td>");
        sb.append(connection.isConnected() ? ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection).getDatabaseProperties().get(Bundles.get(DefaultDatabaseHost.class, "Driver")) : connection.getDriverName());
        if (connection.isConnected()) {
            sb.append("</td></tr><td>").append(bundleString("ServerVersion")).append("</td><td></td><td>");
            sb.append(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection).getDatabaseProperties().get(Bundles.get(DefaultDatabaseHost.class, "ServerVersion")));
            sb.append("</td></tr><td>").append(bundleString("ODSVersion")).append("</td><td></td><td>");
            sb.append(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection).getDatabaseProperties().get(Bundles.get(DefaultDatabaseHost.class, "ODS_VERSION")));
        }
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

    private static String bundleString(String key) {
        return Bundles.get(BrowserTreeCellRenderer.class, key);
    }

}
