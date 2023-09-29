package org.executequery.gui.browser;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseCatalog;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.browser.depend.DependPanel;
import org.executequery.gui.browser.nodes.DatabaseCatalogNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TablespaceTreePopupMenu extends JPopupMenu implements ActionListener {
    private final JMenuItem reload;
    private final JMenuItem delete;

    private final DependPanel treePanel;

    private StatementToEditorWriter statementWriter;

    private DatabaseConnection currentSelection;

    private TreePath currentPath;


    public TablespaceTreePopupMenu(DependPanel treePanel) {
        this.treePanel = treePanel;

        reload = createMenuItem(bundleString("reload"), "reload", this);
        add(reload);
        delete = createMenuItem(bundleString("delete"), "delete", this);
        add(delete);

    }

    public void show(Component invoker, int x, int y) {
        DatabaseObjectNode node = (DatabaseObjectNode) getCurrentPath().getLastPathComponent();
        if (node.isHostNode() || node.getType() == NamedObject.META_TAG)
            delete.setVisible(false);
        else {
            delete.setVisible(true);
            delete.setText(bundleString("delete", node.getName()));
        }
        reload.setText(bundleString("reload", node.getName()));
        super.show(invoker, x, y);
    }

    private JMenuItem createMenuItem(String text,
                                     String actionCommand,
                                     ActionListener listener) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(text);
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(listener);
        return menuItem;
    }

    private JCheckBoxMenuItem createCheckBoxMenuItem(String text,
                                                     String actionCommand,
                                                     ActionListener listener) {

        JCheckBoxMenuItem menuItem = MenuItemFactory.createCheckBoxMenuItem(text);
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(listener);
        return menuItem;
    }


    private DatabaseCatalog asDatabaseCatalog(DefaultMutableTreeNode currentPathComponent) {

        return (DatabaseCatalog) (asDatabaseObjectNode(currentPathComponent)).getDatabaseObject();
    }

    private DatabaseObjectNode asDatabaseObjectNode(DefaultMutableTreeNode currentPathComponent) {

        return (DatabaseObjectNode) currentPathComponent;
    }

    private boolean isCatalog(DefaultMutableTreeNode currentPathComponent) {

        return currentPathComponent instanceof DatabaseCatalogNode;
    }

    public DependPanel getTreePanel() {
        return treePanel;
    }

    public DatabaseConnection getCurrentSelection() {
        return currentSelection;
    }

    public void setCurrentSelection(DatabaseConnection currentSelection) {
        this.currentSelection = currentSelection;
    }

    public TreePath getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(TreePath currentPath) {
        this.currentPath = currentPath;
    }

    private String bundleString(String key, Object... args) {
        return Bundles.get(BrowserTreePopupMenu.class, key, args);
    }

    public void deleteObject(ActionEvent e) {
        if (currentPath != null && currentSelection != null) {
            DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
            DatabaseObject object = (DatabaseObject) node.getUserObject();
            StringBuilder sb = new StringBuilder();
            sb.append(node.getShortName());
            sb.append(":");
            sb.append(node.getMetaDataKey());
            sb.append(":");
            sb.append(object.getHost());
            String type;
            if (node.getType() == NamedObject.GLOBAL_TEMPORARY)
                type = NamedObject.META_TYPES[NamedObject.TABLE];
            else if (node.getType() == NamedObject.DATABASE_TRIGGER)
                type = NamedObject.META_TYPES[NamedObject.TRIGGER];
            else
                type = NamedObject.META_TYPES[node.getType()];
            String query = "ALTER " + type + " " + MiscUtils.getFormattedObject(node.getName(), object.getHost().getDatabaseConnection()) + " SET TABLESPACE PRIMARY";
            ExecuteQueryDialog eqd = new ExecuteQueryDialog("Dropping object", query, currentSelection, true);
            eqd.display();
            if (eqd.getCommit())
                treePanel.reloadPath(currentPath.getParentPath());
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().contains("delete"))
            deleteObject(e);
        else
            treePanel.reloadPath(currentPath);
    }
}
