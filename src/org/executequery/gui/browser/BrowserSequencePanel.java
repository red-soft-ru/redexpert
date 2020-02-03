package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.impl.DefaultDatabaseSequence;
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
 * Created by vasiliy on 27.01.17.
 */
public class BrowserSequencePanel extends AbstractFormObjectViewPanel {

    public static final String NAME = "BrowserSequencePanel";

    private DependenciesPanel dependenciesPanel;
    private DisabledField sequenceNameField;

    private JLabel objectNameLabel;

    private JLabel valueLabel;
    private JTextField valueField;

    private JTextPane descriptionPane;
    private JTextPane sqlPane;

    private Map cache;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    public BrowserSequencePanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {

        dependenciesPanel = new DependenciesPanel();

        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());

        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder(Bundles.getCommon("parameters")));

        valueLabel = new JLabel();
        valueLabel.setText(Bundles.getCommon("value"));

        valueField = new DisabledField();

        paramPanel.add(valueLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.LINE_START, GridBagConstraints.LINE_START, new Insets(5, 0, 5, 5), 0, 0));
        paramPanel.add(valueField, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));

        panel.add(paramPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.add(bundleString("sequence"), panel);

        descriptionPane = new StyledLogPane();

        JPanel descPanel = new JPanel();

        descPanel.setLayout(new BorderLayout());
        descPanel.add(descriptionPane);

        tabs.add(Bundles.getCommon("description"), descPanel);

        JPanel sqlPanel = new JPanel(new BorderLayout());

        sqlPanel.setBorder(BorderFactory.createEtchedBorder());

        sqlPane = new SQLTextPane();

        sqlPanel.add(sqlPane, BorderLayout.CENTER);

        tabs.add("Sql", sqlPanel);
        tabs.add(Bundles.getCommon("dependencies"), dependenciesPanel);

        objectNameLabel = new JLabel();
        sequenceNameField = new DisabledField();

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
        //base.add(new JLabel("Schema:"), gbc);
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
        base.add(sequenceNameField, gbc);
        ++gbc.gridy;
        gbc.insets.top = 0;

        setHeaderText(bundleString("DatabaseSequence"));
        setHeaderIcon(GUIUtilities.loadIcon("Sequence24.png", true));
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

    public void setValues(DefaultDatabaseSequence sequence) {
        dependenciesPanel.setDatabaseObject(sequence);
        objectNameLabel.setText(bundleString("SequenceName"));
        setHeaderText(bundleString("DatabaseSequence"));
        setHeaderIcon(GUIUtilities.loadIcon("Sequence16.png", true));

        try {
            sequenceNameField.setText(sequence.getName());
            valueField.setText(String.valueOf(sequence.getSequenceValue()));
            descriptionPane.setText(sequence.getRemarks());
            sqlPane.setText(sequence.getCreateSQLText() +
                    "\n\n" +
                    sequence.getAlterSQLText());
        } catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabaseSequence sequence = (DefaultDatabaseSequence) cache.get(metaObject);
        setValues(metaObject, sequence);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabaseSequence sequence) {

        objectNameLabel.setText("Sequence Name:");
        setHeaderText("Database Sequence");
        setHeaderIcon("Sequence16.png");

        if (sequence != null) {
            sequenceNameField.setText(sequence.getName());
            valueField.setText(String.valueOf(sequence.getSequenceValue()));
            descriptionPane.setText(sequence.getDescription());
            sqlPane.setText(sequence.getCreateSQLText() +
                    "\n\n" +
                    sequence.getAlterSQLText());

        } else {
            sequenceNameField.setText(metaObject.getName());
        }

    }

    private void setHeaderIcon(String icon) {

        setHeaderIcon(GUIUtilities.loadIcon(icon, true));
    }


}
