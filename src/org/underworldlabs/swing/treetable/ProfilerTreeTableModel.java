package org.underworldlabs.swing.treetable;

import java.util.HashSet;
import java.util.Set;
import javax.swing.tree.TreeNode;
public interface ProfilerTreeTableModel {

    public TreeNode getRoot();

    public int getColumnCount();

    public Class getColumnClass(int column);

    public String getColumnName(int column);

    public void setValueAt(Object aValue, TreeNode node, int column);

    public Object getValueAt(TreeNode node, int column);

    public boolean isCellEditable(TreeNode node, int column);

    public void addListener(Listener listener);

    public void removeListener(Listener listener);


    public static abstract class Abstract implements ProfilerTreeTableModel {

        private TreeNode root;

        private Set<Listener> listeners;

        public Abstract(TreeNode root) {
            if (root == null) throw new NullPointerException("Root cannot be null"); // NOI18N
            this.root = root;
        }

        public void dataChanged() {
            fireDataChanged();
        }

        public void structureChanged() {
            fireStructureChanged();
        }

        public void childrenChanged(TreeNode node) {
            fireChildrenChanged(node);
        }

        public void setRoot(TreeNode newRoot) {
            TreeNode oldRoot = root;
            root = newRoot;
            fireRootChanged(oldRoot, newRoot);
        }

        public TreeNode getRoot() {
            return root;
        }

        public void addListener(Listener listener) {
            if (listeners == null) listeners = new HashSet();
            listeners.add(listener);
        }

        public void removeListener(Listener listener) {
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) listeners = null;
            }
        }

        protected void fireDataChanged() {
            if (listeners != null)
                for (Listener listener : listeners)
                    listener.dataChanged();
        }

        protected void fireStructureChanged() {
            if (listeners != null)
                for (Listener listener : listeners)
                    listener.structureChanged();
        }

        protected void fireChildrenChanged(TreeNode node) {
            if (listeners != null)
                for (Listener listener : listeners)
                    listener.childrenChanged(node);
        }

        protected void fireRootChanged(TreeNode oldRoot, TreeNode newRoot) {
            if (listeners != null)
                for (Listener listener : listeners)
                    listener.rootChanged(oldRoot, newRoot);
        }

    }


    public static interface Listener {

        public void dataChanged();

        public void structureChanged();

        public void childrenChanged(TreeNode node);

        public void rootChanged(TreeNode oldRoot, TreeNode newRoot);

    }

    public static class Adapter implements Listener {

        public void dataChanged() {}

        public void structureChanged() {}

        public void childrenChanged(TreeNode node) {}

        public void rootChanged(TreeNode oldRoot, TreeNode newRoot) {}

    }

}
