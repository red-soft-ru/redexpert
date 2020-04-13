package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.impl.DefaultDatabasePackage;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SQLTextPane;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.StyledLogPane;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vasiliy on 04.05.17.
 */
public class BrowserPackagePanel extends AbstractFormObjectViewPanel {
    public static final String NAME = "BrowserPackagePanel";

    private DependenciesPanel dependenciesPanel;

    private DisabledField packageNameField;

    private JLabel objectNameLabel;

    JTextPane descriptionPane;
    JTextPane sqlPane;

    private Map cache;

    JTextPane headerTextPane;
    JTextPane bodyTextPane;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    public BrowserPackagePanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() throws Exception {

        dependenciesPanel = new DependenciesPanel();

        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerTextPane = new SQLTextPane();
        headerTextPane.setEditable(false);
        headerPanel.add(new JScrollPane(headerTextPane), BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.add("Package Header", panel);

        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyTextPane = new SQLTextPane();
        bodyTextPane.setEditable(false);
        bodyPanel.add(new JScrollPane(bodyTextPane), BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.CENTER);

        tabs.add("Package Body", bodyPanel);

        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBorder(BorderFactory.createEtchedBorder());

        descriptionPane = new StyledLogPane();

        descriptionPanel.add(descriptionPane, BorderLayout.CENTER);

        tabs.add("Description", descriptionPanel);

        JPanel sqlPanel = new JPanel(new BorderLayout());
        sqlPanel.setBorder(BorderFactory.createEtchedBorder());

        sqlPane = new SQLTextPane();

        sqlPanel.add(sqlPane, BorderLayout.CENTER);

        tabs.add("Sql", sqlPanel);
        tabs.add(Bundles.getCommon("dependencies"), dependenciesPanel);

        objectNameLabel = new JLabel();
        packageNameField = new DisabledField();

        JPanel base = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(10, 10, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx++;
        gbc.insets = insets;
        gbc.gridy++;
        base.add(objectNameLabel, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.right = 5;
        gbc.insets.right = 10;
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.insets.bottom = 10;
        gbc.fill = GridBagConstraints.BOTH;
        base.add(tabs, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = 5;
        gbc.insets.top = 10;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.gridy = 0;
        gbc.gridx = 1;
        base.add(packageNameField, gbc);
        ++gbc.gridy;
        gbc.insets.top = 0;

        setHeaderText("Database Package");
        setHeaderIcon(GUIUtilities.loadIcon("package16.png", true));
        setContentPanel(base);
        cache = new HashMap();

    }

    public String getLayoutName() {
        return NAME;
    }

    public Printable getPrintable() {
        return null;
    }

    public void refresh() {
        cache.clear();
    }

    public void cleanup() {
    }

    public void setValues(DefaultDatabasePackage databasePackage) {

        dependenciesPanel.setDatabaseObject(databasePackage);
        objectNameLabel.setText("Package Name:");
        setHeaderText("Database Package");
        setHeaderIcon(GUIUtilities.loadIcon("package16.png", true));

        try {
            packageNameField.setText(databasePackage.getName());
            headerTextPane.setText(databasePackage.getHeaderSource());
            bodyTextPane.setText(databasePackage.getBodySource());

            descriptionPane.setText(databasePackage.getDescription());
            sqlPane.setText(databasePackage.getCreateSQLText());
        } catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabasePackage databasePackage = (DefaultDatabasePackage) cache.get(metaObject);
        setValues(metaObject, databasePackage);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabasePackage databasePackage) {

        objectNameLabel.setText("Package Name:");
        setHeaderText("Database Package");
        setHeaderIcon("package16.png");

        if (databasePackage != null) {
            packageNameField.setText(databasePackage.getName());
        } else {
            packageNameField.setText(metaObject.getName());
        }
    }

    private void setHeaderIcon(String icon) {

    }

}
