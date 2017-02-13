package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseUDF;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SQLTextPane;
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
public class BrowserUDFPanel extends AbstractFormObjectViewPanel {

    public static final String NAME = "BrowserUDFPanel";

    private DisabledField udfNameField;

    private JLabel objectNameLabel;

    /**
     * the current database object in view
     */
    private DatabaseObject currentObjectView;

    /**
     * The tabbed description pane
     */
    private JTabbedPane tabPane;

    private JTextPane descriptionPane;

    private JTextPane sqlPane;

    private Map cache;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    public BrowserUDFPanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() throws Exception {
        JPanel descPanel = new JPanel(new GridBagLayout());

        descPanel.add(
                new JScrollPane(),
                new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                        GridBagConstraints.SOUTHEAST,
                        GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

        tabPane = new JTabbedPane(JTabbedPane.TOP);
        tabPane.add("UDF", descPanel);
        JPanel descriptionPanel = new JPanel(new BorderLayout());

        descriptionPanel.setBorder(BorderFactory.createEtchedBorder());

        descriptionPane = new StyledLogPane();

        descriptionPanel.add(descriptionPane, BorderLayout.CENTER);

        tabPane.add("Description", descriptionPanel);

        JPanel sqlPanel = new JPanel(new BorderLayout());

        sqlPanel.setBorder(BorderFactory.createEtchedBorder());

        sqlPane = new SQLTextPane();

        sqlPanel.add(sqlPane, BorderLayout.CENTER);

        tabPane.add("Sql", sqlPanel);

        objectNameLabel = new JLabel();
        udfNameField = new DisabledField();

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
        base.add(udfNameField, gbc);
        ++gbc.gridy;
        gbc.insets.top = 0;

        setHeaderText("Database UDF");
        setHeaderIcon(GUIUtilities.loadIcon("udf16.png", true));
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

    public void setValues(DefaultDatabaseUDF udf) {

        currentObjectView = udf;

        objectNameLabel.setText("UDF Name:");
        setHeaderText("Database UDF");
        setHeaderIcon(GUIUtilities.loadIcon("udf16.png", true));

        try {
            udfNameField.setText(udf.getName());
            descriptionPane.setText(udf.getRemarks());
            sqlPane.setText(udf.getCreateSQLText());
        } catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabaseUDF udf = (DefaultDatabaseUDF) cache.get(metaObject);
        setValues(metaObject, udf);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabaseUDF udf) {

        objectNameLabel.setText("UDF Name:");
        setHeaderText("Database UDF");
        setHeaderIcon("udf16.png");

        if (udf != null) {
            udfNameField.setText(udf.getName());
            descriptionPane.setText(udf.getRemarks());
            sqlPane.setText(udf.getCreateSQLText());

        } else {
            udfNameField.setText(metaObject.getName());
        }

    }

    private void setHeaderIcon(String icon) {

        setHeaderIcon(GUIUtilities.loadIcon(icon, true));
    }
}
