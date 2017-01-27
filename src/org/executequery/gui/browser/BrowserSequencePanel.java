package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.impl.DefaultDatabaseSequence;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;

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

    private DisabledField sequenceNameField;

    private JLabel objectNameLabel;

    private Map cache;

    /** the browser's control object */
    private BrowserController controller;

    public BrowserSequencePanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() throws Exception {

        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());

        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));

        panel.add(paramPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.add("Sequence", panel);

        objectNameLabel = new JLabel();
        sequenceNameField = new DisabledField();

        JPanel base = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(10,10,5,5);
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

        setHeaderText("Database Sequence");
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

        objectNameLabel.setText("Sequence Name:");
        setHeaderText("Database Sequence");
        setHeaderIcon(GUIUtilities.loadIcon("Sequence16.png", true));

        try {
            sequenceNameField.setText(sequence.getName());
        }
        catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabaseSequence sequence = (DefaultDatabaseSequence)cache.get(metaObject);
        setValues(metaObject, sequence);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabaseSequence sequence) {

        objectNameLabel.setText("Sequence Name:");
        setHeaderText("Database Sequence");
        setHeaderIcon("Sequence16.png");

        if (sequence != null) {
            sequenceNameField.setText(sequence.getName());

        } else {
            sequenceNameField.setText(metaObject.getName());
        }

    }

    private void setHeaderIcon(String icon) {

        setHeaderIcon(GUIUtilities.loadIcon(icon, true));
    }


}
