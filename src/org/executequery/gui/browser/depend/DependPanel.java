package org.executequery.gui.browser.depend;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.TablespaceTreePopupMenu;
import org.executequery.gui.browser.TreeFindAction;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.browser.tree.ConnectionTree;
import org.executequery.gui.browser.tree.TreePanel;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DependPanel extends TreePanel {

    private ConnectionTree tree;
    private DatabaseObject databaseObject;
    private DatabaseConnection databaseConnection;
    private TablespaceTreePopupMenu tablespacePopupMenu;

    public DependPanel(int treeType) {
        setTreeType(treeType);
        setLayout(new GridBagLayout());
    }

    @Override
    public void pathExpanded(TreePath path) {

        Object object = path.getLastPathComponent();
        final DatabaseObjectNode node = (DatabaseObjectNode) object;

        SwingWorker worker = new SwingWorker("expandDependenciesFor " + node.getName()) {

            @Override
            public Object construct() {
                GUIUtilities.showWaitCursor();
                doNodeExpansion(node);
                return null;
            }

            @Override
            public void finished() {
                GUIUtilities.showNormalCursor();
            }

        };
        worker.start();
    }

    public void setDatabaseObject(DatabaseObject databaseObject) {

        this.databaseObject = databaseObject;
        setDatabaseConnection(databaseObject.getHost().getDatabaseConnection());
        DatabaseHostNode hostNode = new DatabaseHostNode(
                new DefaultDatabaseHost(databaseConnection, treeType, databaseObject),
                null,
                false
        );
        hostNode.populateChildren();

        tree = new ConnectionTree(hostNode, this);
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) {
                    maybeShowPopup(e);
                    return;
                }
                twoClicks(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() < 2)
                    maybeShowPopup(e);
            }

        });

        add(tree, new GridBagConstraints(
                0, 0, 1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

    }

    private void maybeShowPopup(MouseEvent e) {

        if (e.isPopupTrigger()) {

            Point point = new Point(e.getX(), e.getY());
            TreePath treePathForLocation = getTreePathForLocation(point.x, point.y);

            TablespaceTreePopupMenu popupMenu;
            if (treePathForLocation != null && treeType == TABLESPACE) {

                popupMenu = getTablespacePopupMenu();
                popupMenu.setCurrentPath(treePathForLocation);
                popupMenu.setCurrentSelection(getDatabaseConnection());
                popupMenu.show(e.getComponent(), point.x, point.y);
            }

        }
    }

    public void reloadPath(TreePath path) {
        try {

            GUIUtilities.showWaitCursor();

            Object object = path.getLastPathComponent();
            boolean expanded = tree.isExpanded(path);

            if (expanded)
                tree.collapsePath(path);

            DatabaseObjectNode node = (DatabaseObjectNode) object;
            node.reset();
            pathExpanded(path);

            if (expanded)
                tree.expandPath(path);

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    private void twoClicks(MouseEvent e) {

        TreePath treePath = tree.getSelectionPath();
        if (treePath == null)
            return;

        DatabaseObjectNode node = (DatabaseObjectNode) treePath.getLastPathComponent();
        if (node.getType() == NamedObject.META_TAG)
            return;

        String nodeName = node.getName();
        if (nodeName != null) {

            nodeName = nodeName.replace("$", "\\$");
            TreeFindAction action = new TreeFindAction();
            ConnectionTree tree = ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getTree();
            action.install(tree);
            action.findString(tree, nodeName, ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getHostNode(databaseConnection));
            JList jList = action.getResultsList();

            if (jList.getModel().getSize() == 1) {
                jList.setSelectedIndex(0);
                action.listValueSelected((TreePath) jList.getSelectedValue());

            } else {

                for (int i = 0; i < jList.getModel().getSize(); i++) {
                    if (((DatabaseObjectNode) ((TreePath) jList.getModel().getElementAt(i)).getLastPathComponent()).getType() == node.getType()) {
                        jList.setSelectedIndex(i);
                        break;
                    }
                }

                action.listValueSelected((TreePath) jList.getSelectedValue());

                /*
                jList.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (jList.getModel().getSize() == 0)
                            dialog.finished();
                    }
                });
                JScrollPane scrollPane = new JScrollPane(jList);
                panel.add(scrollPane);
                dialog.addDisplayComponent(panel);
                dialog.display();
                */

            }
        }
    }

    private synchronized void doNodeExpansion(DatabaseObjectNode node) {
        if (node.getChildCount() == 0) {
            node.populateChildren();
            tree.nodeStructureChanged(node);
        }
    }

    public TablespaceTreePopupMenu getTablespacePopupMenu() {
        if (tablespacePopupMenu == null)
            tablespacePopupMenu = new TablespaceTreePopupMenu(this);
        return tablespacePopupMenu;
    }

    public DatabaseObject getDatabaseObject() {
        return databaseObject;
    }

    public ConnectionTree getTree() {
        return tree;
    }

    public void setTree(ConnectionTree tree) {
        this.tree = tree;
    }

    protected TreePath getTreePathForLocation(int x, int y) {
        return tree.getPathForLocation(x, y);
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public String getPropertyKey() {
        return null;
    }

    @Override
    public String getMenuItemKey() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void pathChanged(TreePath oldPath, TreePath newPath) {
    }

    @Override
    public void valueChanged(DatabaseObjectNode node) {
    }

    @Override
    public void connectionNameChanged(String name) {
    }

    @Override
    public void rebuildConnectionsFromTree() {
    }

}
