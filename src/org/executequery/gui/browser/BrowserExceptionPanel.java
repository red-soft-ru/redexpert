package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseException;
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
 * Created by vasiliy on 13.02.17.
 */
public class BrowserExceptionPanel extends AbstractFormObjectViewPanel {

    public static final String NAME = "BrowserExceptionPanel";

    private DisabledField exceptionNameField;

    private JLabel objectNameLabel;

    /**
     * the current database object in view
     */
    private DatabaseObject currentObjectView;

    /**
     * The tabbed description pane
     */
    private JTabbedPane tabPane;

    private JTextField idField;

    private JTextPane exceptionTextPane;

    private JTextPane descriptionPane;

    private JTextPane sqlPane;

    private DependenciesPanel dependenciesPanel;

    private Map cache;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    public BrowserExceptionPanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        JPanel descPanel = new JPanel(new GridBagLayout());
        dependenciesPanel = new DependenciesPanel();
        idField = new JTextField();

        exceptionTextPane = new JTextPane();

        JLabel idLabel = new JLabel(bundleString("ExceptionID"));

        JPanel topGroupPanel = new JPanel(new BorderLayout());

        topGroupPanel.add(idLabel, BorderLayout.LINE_START);
        topGroupPanel.add(idField, BorderLayout.CENTER);

        JPanel groupPanel = new JPanel(new BorderLayout());

        groupPanel.add(topGroupPanel, BorderLayout.NORTH);

        groupPanel.add(exceptionTextPane, BorderLayout.CENTER);

        descPanel.add(
                new JScrollPane(groupPanel),
                new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                        GridBagConstraints.SOUTHEAST,
                        GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

        tabPane = new JTabbedPane(JTabbedPane.TOP);
        tabPane.add(bundleString("Exception"), descPanel);
        JPanel descriptionPanel = new JPanel(new BorderLayout());

        descriptionPanel.setBorder(BorderFactory.createEtchedBorder());

        descriptionPane = new StyledLogPane();

        descriptionPanel.add(descriptionPane, BorderLayout.CENTER);

        tabPane.add(Bundles.getCommon("description"), descriptionPanel);

        JPanel sqlPanel = new JPanel(new BorderLayout());

        sqlPanel.setBorder(BorderFactory.createEtchedBorder());

        sqlPane = new SQLTextPane();

        sqlPanel.add(sqlPane, BorderLayout.CENTER);

        tabPane.add("Sql", sqlPanel);
        tabPane.add(Bundles.getCommon("dependencies"), dependenciesPanel);

        objectNameLabel = new JLabel();
        exceptionNameField = new DisabledField();

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
        base.add(tabPane, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = 5;
        gbc.insets.top = 10;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.gridy = 0;
        gbc.gridx = 1;
        base.add(exceptionNameField, gbc);
        ++gbc.gridy;
        gbc.insets.top = 0;

        setHeaderText("DatabaseException");
        setHeaderIcon(GUIUtilities.loadIcon("exception16.png", true));
        setContentPanel(base);
        cache = new HashMap();

    }

    public String getLayoutName() {
        return NAME;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Printable getPrintable() {
        return null;
    }

    public void setValues(DefaultDatabaseException exception) {

        currentObjectView = exception;
        dependenciesPanel.setDatabaseObject(currentObjectView);
        objectNameLabel.setText(bundleString("ExceptionName"));
        setHeaderText(bundleString("DatabaseException"));
        setHeaderIcon(GUIUtilities.loadIcon("exception16.png", true));

        try {
            exceptionNameField.setText(exception.getName());
            idField.setText(exception.getID());
            exceptionTextPane.setText(exception.getExceptionText());
            descriptionPane.setText(exception.getRemarks());
            sqlPane.setText(exception.getCreateSQLText());
        } catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabaseException exception = (DefaultDatabaseException) cache.get(metaObject);
        setValues(metaObject, exception);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabaseException exception) {

        objectNameLabel.setText("Exception Name:");
        setHeaderText(bundleString("DatabaseException"));
        setHeaderIcon("exception16.png");

        if (exception != null) {
            exceptionNameField.setText(exception.getName());
            idField.setText(exception.getID());
            exceptionTextPane.setText(exception.getExceptionText());
            descriptionPane.setText(exception.getRemarks());
            sqlPane.setText(exception.getCreateSQLText());

        } else {
            exceptionNameField.setText(metaObject.getName());
        }

    }

    private void setHeaderIcon(String icon) {

        setHeaderIcon(GUIUtilities.loadIcon(icon, true));
    }

}
