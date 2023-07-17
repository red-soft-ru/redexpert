/*
 * DynamicTree.java
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

package org.underworldlabs.swing.tree;

import javax.swing.*;
import javax.swing.text.Position;
import javax.swing.tree.*;

/**
 * Dynamic JTree allowing moving of nodes up/down
 * and provides convenience methods for removal/insertion of nodes.
 *
 * @author Takis Diakoumis
 */
public class DynamicTree extends JTree {

    public static final int MOVE_UP = 0;
    public static final int MOVE_DOWN = 1;
    private static final int DEFAULT_ROW_HEIGHT = 18;

    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;

    public DynamicTree(DefaultMutableTreeNode root) {
        this.root = root;
        init();
    }

    private void init() {

        treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        putClientProperty("JTree.lineStyle", "Angled");
        setRootVisible(true);

        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ToolTipManager.sharedInstance().registerComponent(this);    // register for tool tips

    }

    public void expandSelectedRow() {
        int row = getTreeSelectionRow();
        if (row != -1)
            expandRow(row);
    }

    private int getTreeSelectionRow() {

        int selectedRow = -1;
        int[] selectedRowsArray = getSelectionRows();
        if (selectedRowsArray != null)
            selectedRow = selectedRowsArray[0];

        return selectedRow;
    }

    /**
     * This sets the user object of the TreeNode identified by path and posts a node changed.
     * If you use custom user objects in the TreeModel you're going to need to subclass
     * this and set the user object of the changed node to something meaningful.
     *
     * @param path     to the node that the user has altered
     * @param newValue new value from the TreeCellEditor
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        treeModel.valueForPathChanged(path, newValue);
    }

    /**
     * Returns the tree node from the root node with the specified user object.
     * This will traverse the tree from the root node to the root's children only, not its children's children.
     *
     * @param userObject user object to search for
     * @return the tree node or null if not found
     */
    public DefaultMutableTreeNode getNodeFromRoot(Object userObject) {

        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            if (node.getUserObject() == userObject)
                return node;
        }

        return null;
    }

    /**
     * Adds the specified node to the root node of this tree.
     *
     * @param node the tree node to add
     */
    public void addToRoot(TreeNode node) {
        addToRoot(node, true);
    }

    /**
     * Adds the specified node to the root node of this tree.
     *
     * @param node       the tree node to add
     * @param selectNode 'true' to select added node
     */
    public void addToRoot(TreeNode node, boolean selectNode) {

        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) node;
        treeModel.insertNodeInto(mutableTreeNode, root, root.getChildCount());

        if (selectNode)
            selectNode(mutableTreeNode);

    }

    public void insertNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild) {
        treeModel.insertNodeInto(newChild, parent, parent.getChildCount() - 1);
    }

    public void nodesWereInserted(TreeNode parent, int[] childIndices) {
        treeModel.nodesWereInserted(parent, childIndices);
    }

    public void selectNode(DefaultMutableTreeNode node) {
        TreePath path = new TreePath(node.getPath());
        scrollPathToVisible(path);
        setSelectionPath(path);
    }

    public void selectNodes(DefaultMutableTreeNode[] nodes) {

        TreePath[] paths = new TreePath[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            TreePath path = new TreePath(nodes[i].getPath());
            paths[i] = path;
            scrollPathToVisible(path);
        }
        setSelectionPaths(paths);
    }

    public int getIndexWithinParent(TreeNode node) {

        MutableTreeNode parent = (MutableTreeNode) node.getParent();
        if (parent == null)
            parent = root;

        return parent.getIndex(node);
    }

    /**
     * Moves the specified node in the specified direction.
     */
    private void move(TreeNode node, int direction) {

        MutableTreeNode parent = (MutableTreeNode) node.getParent();
        if (parent == null)
            parent = root;

        int currentIndex = parent.getIndex(node);
        if (currentIndex <= 0 && direction == MOVE_UP)
            return;

        int newIndex;
        if (direction != MOVE_UP) {

            newIndex = currentIndex + 1;
            int childCount = parent.getChildCount();
            if (newIndex > (childCount - 1))
                return;

        } else
            newIndex = currentIndex - 1;

        parent.remove(currentIndex);                        // remove node from root
        parent.insert((MutableTreeNode) node, newIndex);    // insert into the new index
        treeModel.nodeStructureChanged(parent);             // fire event

        TreePath path = (node instanceof DefaultMutableTreeNode) ?
                new TreePath(((DefaultMutableTreeNode) node).getPath()) :
                getNextMatch(node.toString(), getTreeSelectionRow(), (direction == MOVE_UP) ? Position.Bias.Forward : Position.Bias.Backward);

        scrollPathToVisible(path);
        setSelectionPath(path);
    }

    /**
     * Selects the node that matches the specified prefix forward
     * from the currently selected node.
     *
     * @param prefix the prefix of the node to select
     */
    public void selectNextNode(String prefix) {

        int selectedRow = getTreeSelectionRow();
        if (selectedRow == -1)
            return;

        TreePath path = getNextMatch(prefix, selectedRow, Position.Bias.Forward);
        if (path != null) {
            scrollPathToVisible(path);
            setSelectionPath(path);
        }

    }

    /**
     * Removes the currently selected node and sets the
     * next selected node beginning with the specified prefix.
     *
     * @param nextSelectionPrefix prefix of the node to select after removal
     */
    public void removeSelection(String nextSelectionPrefix) {

        TreeNode node = (TreeNode) getLastPathComponent();
        TreePath path = null;

        if (nextSelectionPrefix != null) {
            int selectedRow = getTreeSelectionRow();
            path = getNextMatch(nextSelectionPrefix, selectedRow, Position.Bias.Backward);
        }

        // remove the node from the tree
        treeModel.removeNodeFromParent((MutableTreeNode) node);
        if (path != null) {
            scrollPathToVisible(path);
            setSelectionPath(path);
        }

    }

    /**
     * Removes the specified node and sets the
     * next selected node beginning with the specified prefix.
     *
     * @param node                node to be removed
     * @param nextSelectionPrefix prefix of the node to select after removal
     */
    public void removeNode(TreeNode node, String nextSelectionPrefix) {

        TreePath path = null;
        if (nextSelectionPrefix != null) {
            int selectedRow = getTreeSelectionRow();
            path = getNextMatch(nextSelectionPrefix, selectedRow, Position.Bias.Backward);
        }

        // remove the node from the tree
        treeModel.removeNodeFromParent((MutableTreeNode) node);
        if (path != null) {
            scrollPathToVisible(path);
            setSelectionPath(path);
        }

    }

    /**
     * Invoke this method if you've totally changed the children
     * of node and its children and their...<p>
     * This will post a treeStructureChanged event
     */
    public void nodeStructureChanged(TreeNode node) {
        treeModel.nodeStructureChanged(node);
    }

    /**
     * Invoke this method after you've changed representation of the node.
     */
    public void nodeChanged(TreeNode node) {
        treeModel.nodeChanged(node);
    }

    /**
     * Invoke this method if you've modified the
     * TreeNodes upon which this model depends.
     */
    public void reload() {
        treeModel.reload();
    }

    /**
     * Set up new tree model by the specified root node.
     *
     * @param root new root node
     */
    public void reset(DefaultMutableTreeNode root) {
        setModel(new DefaultTreeModel(root));
        this.root = root;
        repaint();
    }

    public void moveSelection(int direction) {
        TreeNode node = (TreeNode) getLastPathComponent();
        move(node, direction);
    }

    public void moveSelectionUp() {
        moveSelection(MOVE_UP);
    }

    public void moveSelectionDown() {
        moveSelection(MOVE_DOWN);
    }

    public void removeNode(MutableTreeNode node) {
        treeModel.removeNodeFromParent(node);
    }

    public Object getLastPathComponent() {
        TreePath path = getSelectionPath();
        return (path != null) ? path.getLastPathComponent() : null;
    }

    public DefaultMutableTreeNode getRootNode() {
        return root;
    }

    @Override
    public int getRowHeight() {

        /* The default swing implementation allows the renderer to determine the row height.
        In most cases this is ok, though I found that on some LAFs the renderer's value is too small
        making the rows too cramped (ie. gtk). as a result, this method return a value of 20 if the rowHeight <= 0.
        This isn't ideal and a bit of a hack, but it works ok. [Takis D.] */

        return Math.max(super.getRowHeight(), DEFAULT_ROW_HEIGHT);
    }

}


