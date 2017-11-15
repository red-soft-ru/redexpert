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

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Dynamic JTree allowing moving of nodes up/down 
 * and provides convenience methods for removal/insertion of nodes.
 *
 * @author   Takis Diakoumis
 */
public class DynamicTree extends JTree {
//                         implements PropertyChangeListener,
//                                    TreeSelectionListener {

    /** directional constant for movements up the tree */
    public static final int MOVE_UP = 0;
    
    /** directional constant for movements down the tree */
    public static final int MOVE_DOWN = 1;

    /** the tree's root node */
    private DefaultMutableTreeNode root;

    /** the tree model for the display */
    private DefaultTreeModel treeModel;
    
    /** the previously selected path */
    //private TreePath previousSelectionPath;
    
    /** Creates a new instance of DynamicTree */
    public DynamicTree(DefaultMutableTreeNode root) {
        this.root = root;
        init();
    }

    private static final int DEFAULT_ROW_HEIGHT = 18;
    
    /**
     * Returns the height of each row.  The default swing implementation 
     * allows the renderer to determine the row height. In most cases this
     * is ok, though i found that on some LAFs the renderer's value is too 
     * small making the rows too cramped (ie. gtk). as a result, this method
     * return a value of 20 if the rowHeight <= 0.
     *
     * This isn't ideal and a bit of a hack, but it works ok.
     */
    public int getRowHeight() {
        int h = super.getRowHeight();
        if (h < DEFAULT_ROW_HEIGHT) {
            return DEFAULT_ROW_HEIGHT;
        }
        return h;
    }
    
    private void init() {
        treeModel = new DefaultTreeModel(root);
        setModel(treeModel);

        // lines on the branches
        putClientProperty("JTree.lineStyle", "Angled");
        setRootVisible(true);
        
        // single selection only
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // add the property change listener
        //addPropertyChangeListener(this);
        
        // add the tree selection listener
        //addTreeSelectionListener(this);
        
        // register for tool tips
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    
    // --------------------------------------------------
    // ------- TreeSelectionListener implementation
    // --------------------------------------------------

    /**
     * Called whenever the value of the selection changes.
     * This will store the current path selection.
     *
     * @param the event that characterizes the change
     *
    public void valueChanged(TreeSelectionEvent e) {
        previousSelectionPath = e.getOldLeadSelectionPath();
        System.out.println("old path: " + previousSelectionPath);
    }
    */
    
    // --------------------------------------------------
    // ------- PropertyChangeListener implementation
    // --------------------------------------------------
/*    
    public void propertyChange(PropertyChangeEvent e) {
        /*
        System.out.println("property change: " + e.getPropertyName() +
        " old value: " + e.getOldValue() + " new value: " + e.getNewValue());

        if ("leadSelectionPath".equals(e.getPropertyName())) {
            Object oldValue = e.getOldValue();
            if (oldValue != null && oldValue instanceof TreePath) {
                //previousSelectionPath = (TreePath)oldValue;
            }
        }
    }
*/

    /** 
     * Expands the currently selected row.
     */
    public void expandSelectedRow() {
        int row = getTreeSelectionRow();
        if (row != -1) {
            expandRow(row);
        }
    }
    
    private int getTreeSelectionRow() {
        int selectedRow = -1;
        int[] selectedRows = getSelectionRows();
        if (selectedRows != null) {
            selectedRow = selectedRows[0];
        }
        return selectedRow;
    }

    /**
     * Invoke this method if you've totally changed the children 
     * of node and its childrens children... 
     * This will post a treeStructureChanged event.
     */
    public void nodeStructureChanged(TreeNode node) {
        
        treeModel.nodeStructureChanged(node);
    }

    public void nodeStructureChanged() {

        treeModel.nodeStructureChanged(getRootNode());
    }

    /**
     * This sets the user object of the TreeNode identified by 
     * path and posts a node changed. If you use custom user 
     * objects in the TreeModel you're going to need to subclass 
     * this and set the user object of the changed node 
     * to something meaningful.
     *
     * @param path to the node that the user has altered
     * @param the new value from the TreeCellEditor
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        treeModel.valueForPathChanged(path, newValue);
    }
    
    /** 
     * Returns the tree node from the root node with the 
     * specified user object. This will traverse the tree from
     * the root node to the root's children only, not its children's
     * children.
     *
     * @param the user object to search for
     * @return the tree node or null if not found
     */
    public DefaultMutableTreeNode getNodeFromRoot(Object userObject) {

        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
        
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            if (node.getUserObject() == userObject) {
            
                return node;
            }

        }
        return null;
    } 
    
    /**
     * Returns the root node of this tree.
     *
     * @return the tree's root node
     */
    public DefaultMutableTreeNode getRootNode() {
        return root;
    }
    
    public void reset(DefaultMutableTreeNode root) {
        
        setModel(new DefaultTreeModel(root));
        repaint();
    }
    
    /**
     * Invoke this method after you've changed how node is to be 
     * represented in the tree.
     */
    public void nodeChanged(TreeNode node) {
        treeModel.nodeChanged(node);
    }
    
    /**
     * Returns the path component of the selected path.
     *
     * @return the component
     */
    public Object getLastPathComponent() {
        TreePath path = getSelectionPath();
        if (path == null) {
            return null;
        }
        return path.getLastPathComponent();
    }
    
    /**
     * Invoke this method if you've modified the TreeNodes 
     * upon which this model depends.
     */
    public void reload(TreeNode node) {
        treeModel.reload();
    }
    
    /**
     * Returns the tree model.
     * 
     * @return the tree model - an instance of DefaultTreeModel
     */
    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }
    
    /**
     * Adds the specified node to the root node of this tree.
     *
     * @param node - the tree node to add
     */
    public void addToRoot(TreeNode node) {
        addToRoot(node, true);
    }

    /**
     * Adds the specified node to the root node of this tree.
     *
     * @param node - the tree node to add
     */
    public void addToRoot(TreeNode node, boolean selectNode) {

        DefaultMutableTreeNode _node = (DefaultMutableTreeNode) node;
        treeModel.insertNodeInto(_node, root, root.getChildCount());
        
        if (selectNode) {

            selectNode(_node);
        }
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
    
    public int getIndexWithinParent(TreeNode node) {
        
        MutableTreeNode parent = (MutableTreeNode) node.getParent();
        if (parent == null) {
            
            parent = root;
        }
        
        return parent.getIndex(node);
    }
    
    /**
     * Moves the specified node in the specified direction.
     */
    private void move(TreeNode node, int direction) {

        MutableTreeNode parent = (MutableTreeNode) node.getParent();
        if (parent == null) {
            
            parent = root;
        }
        
        int currentIndex = parent.getIndex(node);
        if (currentIndex <= 0 && direction == MOVE_UP) {

            return;
        }

        int newIndex = -1;
        if (direction == MOVE_UP) {

            newIndex = currentIndex - 1;

        } else {

            newIndex = currentIndex + 1;

            int childCount = parent.getChildCount();
            if (newIndex > (childCount - 1)) {

                return;
            }

        }

        int selectedRow = getTreeSelectionRow();

        // remove node from root
        parent.remove(currentIndex);
        MutableTreeNode _node = (MutableTreeNode)node;
        
        // insert into the new index
        parent.insert(_node, newIndex);

        // fire event
        treeModel.nodeStructureChanged(parent);
        
        TreePath path = null;
        if (node instanceof DefaultMutableTreeNode) {

            path = new TreePath(((DefaultMutableTreeNode)node).getPath());

        } else {

            String prefix = node.toString();

            // reselect that node
            if (direction == MOVE_UP) {

                path = getNextMatch(prefix, selectedRow, Position.Bias.Forward);

            } else {

                path = getNextMatch(prefix, selectedRow, Position.Bias.Backward);
            }

        }

        scrollPathToVisible(path);
        setSelectionPath(path);
    }
    
    /**
     * Selects the node that matches the specified prefix forward 
     * from the currently selected node.
     *
     * @param prefix - the prefix of the node to select
     */
    public void selectNextNode(String prefix) {

        int selectedRow = getTreeSelectionRow();

        if (selectedRow == -1) {
        
            return;
        }
        
        TreePath path = getNextMatch(prefix, selectedRow, Position.Bias.Forward);
        if (path != null) {

            scrollPathToVisible(path);
            setSelectionPath(path);
        }

    }

    /**
     * Removes the currently selected node and sets the
     * next selected node beginning with the specified
     * prefix.
     *
     * @param the prefix of the node to select after removal
     */
    public void removeSelection(String nextSelectionPrefix) {
        TreeNode node = (TreeNode)getLastPathComponent();

        TreePath path = null;
        if (nextSelectionPrefix != null) {
            // get the row for the current path
            int selectedRow = getTreeSelectionRow();
            path = getNextMatch(nextSelectionPrefix, 
                                selectedRow, 
                                Position.Bias.Backward);
        }

        // remove the node from the tree
        treeModel.removeNodeFromParent((MutableTreeNode)node);
        if (path != null) {
            scrollPathToVisible(path);
            setSelectionPath(path);
        }
    }

    /**
     * Removes the specified node and sets the
     * next selected node beginning with the specified
     * prefix.
     *
     * @param the node to be removed
     * @param the prefix of the node to select after removal
     */
    public void removeNode(TreeNode node, String nextSelectionPrefix) {
        TreePath path = null;
        if (nextSelectionPrefix != null) {
            // get the row for the current path
            int selectedRow = getTreeSelectionRow();
            path = getNextMatch(nextSelectionPrefix, 
                                selectedRow, 
                                Position.Bias.Backward);
        }

        // remove the node from the tree
        treeModel.removeNodeFromParent((MutableTreeNode)node);
        if (path != null) {
            scrollPathToVisible(path);
            setSelectionPath(path);
        }
    }

    public void moveSelection(int direction) {
        TreeNode node = (TreeNode)getLastPathComponent();
        move(node, direction);
    }
    
    /**
     * Moves the selected node up in the tree.
     */
    public void moveSelectionUp() {
        moveSelection(MOVE_UP);
    }

    /**
     * Moves the selected node down in the tree.
     */
    public void moveSelectionDown() {
        moveSelection(MOVE_DOWN);
    }

    /**
     * Removes the specified node from the parent node.
     *
     * @param node - the node to be removed
     */
    public void removeNode(MutableTreeNode node) {
        treeModel.removeNodeFromParent(node);
    }

}


