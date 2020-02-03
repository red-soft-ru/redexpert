package org.executequery.gui.browser.depend;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.TreeFindAction;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.browser.tree.SchemaTree;
import org.executequery.gui.browser.tree.TreePanel;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class DependPanel extends TreePanel {
    private SchemaTree tree;
    private DatabaseObject databaseObject;
    private DatabaseConnection databaseConnection;
    private int treeType;

    public DependPanel(int treeType) {
        this.treeType = treeType;
        setLayout(new GridBagLayout());
    }

    @Override
    public void pathChanged(TreePath oldPath, TreePath newPath) {

    }

    @Override
    public void pathExpanded(TreePath path) {
        Object object = path.getLastPathComponent();
        final DatabaseObjectNode node = (DatabaseObjectNode) object;
        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                GUIUtilities.showWaitCursor();
                doNodeExpansion(node);
                return null;
            }

            public void finished() {
                GUIUtilities.showNormalCursor();
            }

        };
        worker.start();
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

    private synchronized void doNodeExpansion(DatabaseObjectNode node) {

        if (node.getChildCount() == 0) {
            node.populateChildren();
            tree.nodeStructureChanged(node);
        }
    }

    public SchemaTree getTree() {
        return tree;
    }

    public void setTree(SchemaTree tree) {
        this.tree = tree;
    }

    public DatabaseObject getDatabaseObject() {
        return databaseObject;
    }

    public void setDatabaseObject(DatabaseObject databaseObject) {
        this.databaseObject = databaseObject;
        setDatabaseConnection(databaseObject.getHost().getDatabaseConnection());
        DatabaseHostNode hostNode = new DatabaseHostNode(new DefaultDatabaseHost(databaseConnection, treeType, databaseObject), null);
        hostNode.populateChildren();
        tree = new SchemaTree(hostNode, this);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) {

                    return;
                }
                twoClicks(e);
            }
        });
        add(tree, new GridBagConstraints(0, 0,
                1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                0, 0));
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    private void twoClicks(MouseEvent e) {
        String s = ((DatabaseObjectNode) tree.getSelectionPath().getLastPathComponent()).getName();
        if (s != null) {
            s = s.replace("$", "\\$");
            TreeFindAction action = new TreeFindAction();
            SchemaTree tree = ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getTree();
            action.install(tree);
            action.findString(tree, s, ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getHostNode(databaseConnection));
            BaseDialog dialog = new BaseDialog("find", false);
            JPanel panel = new JPanel();
            JList jList = action.getResultsList();
            if (jList.getModel().getSize() == 1) {
                jList.setSelectedIndex(0);
                action.listValueSelected((TreePath) jList.getSelectedValue());
            } else {
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
            }
        }
    }
}
