package org.executequery.gui.browser;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.gui.browser.depend.DependPanel;
import org.executequery.gui.browser.tree.TreePanel;

import javax.swing.*;
import java.awt.*;


public class DependenciesPanel extends JPanel {
    JTabbedPane tabPanel;
    DependPanel dependentPanel;
    DependPanel dependedOnPanel;
    DatabaseConnection databaseConnection;
    DefaultStatementExecutor executor;
    DatabaseObject databaseObject;

    public DependenciesPanel() {
        init();
    }


    private void init() {

        this.tabPanel = new JTabbedPane();
        this.dependentPanel = new DependPanel(TreePanel.DEPENDENT);
        this.dependedOnPanel = new DependPanel(TreePanel.DEPENDED_ON);

        setLayout(new GridBagLayout());

        add(tabPanel, new GridBagConstraints(0, 0,
                1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                0, 0));
        tabPanel.add("Depended on", dependedOnPanel);
        tabPanel.add("Dependent", dependentPanel);


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


}
