/*
 * TreeFindAction.java
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

import org.apache.commons.lang.StringUtils;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.browser.tree.SchemaTree;
import org.executequery.localization.Bundles;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Position;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modified from the original by Santhosh Kumar
 * from http://www.jroller.com/santhosh/category/Swing
 * <p>
 * Usage: new TreeFindAction().install(tree);
 *
 * @author Santhosh Kumar, Takis Diakoumis
 */
public class TreeFindAction extends FindAction<TreePath> {
    private boolean searchInCols;
    public TreeFindAction() {

        super();

        putValue(Action.SHORT_DESCRIPTION, Bundles.get("BrowserTreeRootPopupMenu.searchNodes"));
        searchInCols = SystemProperties.getBooleanProperty("user", "browser.search.in.columns");
    }

    protected boolean changed(JComponent comp, String searchString, Position.Bias bias) {

        if (StringUtils.isBlank(searchString)) {

            return false;
        }

        JTree tree = (JTree) comp;
        String prefix = searchString;

        if (ignoreCase()) {

            prefix = prefix.toUpperCase();
        }

        boolean wildcardStart = prefix.startsWith("*");
        if (wildcardStart) {

            prefix = prefix.substring(1);

        } else {

            prefix = "^" + prefix;
        }
        prefix = prefix.replaceAll("\\*", ".*");

        Matcher matcher = Pattern.compile(prefix).matcher("");
        List<TreePath> matchedPaths = new ArrayList<TreePath>();
        findOnTree(tree.getPathForRow(0), matchedPaths, matcher);

        foundValues(matchedPaths);

        return !(matchedPaths.isEmpty());
    }

    private boolean openDatabaseObject = false;
    public void findString(JComponent comp, String searchString, DatabaseObjectNode nodeHost) {
        if (StringUtils.isBlank(searchString)) {

            return;
        }

        SchemaTree tree = (SchemaTree) comp;
        String prefix = searchString;

        if (ignoreCase()) {

            prefix = prefix.toUpperCase();
        }

        boolean wildcardStart = prefix.startsWith("*");
        if (wildcardStart) {

            prefix = prefix.substring(1);

        } else {

            prefix = "^" + prefix + "$";
        }
        prefix = prefix.replaceAll("\\*", ".*");

        Matcher matcher = Pattern.compile(prefix).matcher("");
        List<TreePath> matchedPaths = new ArrayList<TreePath>();
        TreePath hostPath = new TreePath(nodeHost.getPath());
        openDatabaseObject = true;
        findOnTree(hostPath, matchedPaths, matcher);
        foundValues(matchedPaths);

    }

    private void findOnTree(TreePath path, List<TreePath> matchedPaths, Matcher matcher) {
        DatabaseObjectNode root = (DatabaseObjectNode) path.getLastPathComponent();
        root.populateChildren();
        Enumeration<TreeNode> nodes = root.children();
        while (nodes.hasMoreElements()) {
            DatabaseObjectNode node = (DatabaseObjectNode) nodes.nextElement();
            String text = node.getName().trim();
            if (ignoreCase()) {

                text = text.toUpperCase();
            }
            matcher.reset(text);
            if (matcher.find()) {

                matchedPaths.add(path.pathByAddingChild(node));
            }
            if (!searchInCols) {
                if (node.getType() != NamedObject.SYSTEM_TABLE && node.getType() != NamedObject.TABLE && node.getType() != NamedObject.VIEW)
                    findOnTree(path.pathByAddingChild(node), matchedPaths, matcher);
            } else findOnTree(path.pathByAddingChild(node), matchedPaths, matcher);

        }
    }

    private void changeSelection(JTree tree, TreePath path) {
        SchemaTree schemaTree = (SchemaTree) tree;
        ConnectionsTreePanel connectionsTreePanel = (ConnectionsTreePanel) schemaTree.getTreePanel();
        TreePath parent = path.getParentPath();
        boolean expand = true;
        if (parent != null)
            expand = tree.isExpanded(parent);
        connectionsTreePanel.setMoveScrollAfterExpansion(!expand);
        connectionsTreePanel.setMoveScroll(expand);
        tree.setSelectionPath(path);
        if (openDatabaseObject) {
            DatabaseObjectNode node = ((DatabaseObjectNode) path.getLastPathComponent());
            connectionsTreePanel.valueChanged(node);
        }
    }

    public TreePath getNextMatch(JTree tree, String prefix, int startingRow,
                                 Position.Bias bias) {

        int max = tree.getRowCount();
        if (prefix == null) {
            throw new IllegalArgumentException();
        }
        if (startingRow < 0 || startingRow >= max) {
            throw new IllegalArgumentException();
        }

        if (ignoreCase()) {
            prefix = prefix.toUpperCase();
        }

        // start search from the next/previous element froom the
        // selected element
        int increment = (bias == null || bias == Position.Bias.Forward) ? 1 : -1;

        int row = startingRow;
        do {

            TreePath path = tree.getPathForRow(row);
            String text = tree.convertValueToText(path.getLastPathComponent(),
                    tree.isRowSelected(row), tree.isExpanded(row), true, row,
                    false);

            if (ignoreCase()) {

                text = text.toUpperCase();
            }

            if (text.startsWith(prefix)) {

                return path;
            }

            row = (row + increment + max) % max;

        } while (row != startingRow);

        return null;
    }

    protected void listValueSelected(JComponent component, TreePath selection) {

        changeSelection((JTree) component, selection);
    }

    protected boolean ignoreCase() {

        return true;
    }

    protected ListCellRenderer getListCellRenderer() {

        return new TreePathListCellRenderer();
    }

    public JList getResultsList() {
        return this.resultsList;
    }

    private static final Border cellRendererBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

    class TreePathListCellRenderer extends JLabel implements ListCellRenderer {

        public TreePathListCellRenderer() {

            super();
            setBorder(cellRendererBorder);
        }

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            TreePath treePath = (TreePath) value;

            setText(treePath.getLastPathComponent().toString());

            if (isSelected) {

                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());

            } else {

                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);

            return this;
        }

    }

}







