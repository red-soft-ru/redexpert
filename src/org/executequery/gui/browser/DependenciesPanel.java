package org.executequery.gui.browser;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.gui.browser.depend.DependPanel;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.localization.Bundles;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;


public class DependenciesPanel extends JPanel {
    JSplitPane splitPane;
    DependPanel dependentPanel;
    DependPanel dependedOnPanel;
    DatabaseConnection databaseConnection;
    DefaultStatementExecutor executor;
    DatabaseObject databaseObject;

    public DependenciesPanel() {
        init();
    }


    private void init() {

        this.dependentPanel = new DependPanel(TreePanel.DEPENDENT);
        this.dependedOnPanel = new DependPanel(TreePanel.DEPENDED_ON);
        dependentPanel.setBorder(new BorderUIResource.TitledBorderUIResource(bundledString("dependent")));
        dependedOnPanel.setBorder(new BorderUIResource.TitledBorderUIResource(bundledString("dependedOn")));
        this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(dependentPanel), new JScrollPane(dependedOnPanel));
        setLayout(new GridBagLayout());

        add(splitPane, new GridBagConstraints(0, 0,
                1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                0, 0));
    }


    public DefaultStatementExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(DefaultStatementExecutor executor) {
        this.executor = executor;
    }

    public DatabaseObject getDatabaseObject() {
        return databaseObject;
    }

    public void setDatabaseObject(DatabaseObject databaseObject) {
        this.databaseObject = databaseObject;
        dependedOnPanel.setDatabaseObject(databaseObject);
        dependentPanel.setDatabaseObject(databaseObject);
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    private String bundledString(String key) {
        return Bundles.get(this.getClass(), key);
    }


}
