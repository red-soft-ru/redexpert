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
    JScrollPane dependentScroll;
    JScrollPane dependedOnScroll;

    public DependenciesPanel() {
        init();
    }


    private void init() {

        this.dependentPanel = new DependPanel(TreePanel.DEPENDENT);
        this.dependedOnPanel = new DependPanel(TreePanel.DEPENDED_ON);
        dependentScroll = new JScrollPane(dependentPanel);
        dependedOnScroll = new JScrollPane(dependedOnPanel);
        this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dependentScroll, dependedOnScroll);
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
        dependentScroll.setBorder(new BorderUIResource.TitledBorderUIResource(bundledString("dependent", databaseObject.getName())));
        dependedOnScroll.setBorder(new BorderUIResource.TitledBorderUIResource(bundledString("dependedOn", databaseObject.getName())));
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    private String bundledString(String key, Object... args) {
        return Bundles.get(this.getClass(), key, args);
    }


}
