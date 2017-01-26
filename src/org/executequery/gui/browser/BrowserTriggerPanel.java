package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SQLTextPane;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vasiliy on 26.01.17.
 */
public class BrowserTriggerPanel extends AbstractFormObjectViewPanel {
    public static final String NAME = "BrowserTriggerPanel";

    private DisabledField triggerNameField;
    //private DisabledField schemaNameField;

    private JLabel objectNameLabel;

    private JCheckBox activeCheckbox;

    private Map cache;

    JTextPane textPane;

    /** the browser's control object */
    private BrowserController controller;

    public BrowserTriggerPanel(BrowserController controller) {
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

        JPanel paramPanel = new JPanel(new BorderLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));

        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.setBorder(BorderFactory.createTitledBorder("Source"));
        textPane = new SQLTextPane();
        textPane.setEditable(false);
        sourcePanel.add(new JScrollPane(textPane), BorderLayout.CENTER);

        activeCheckbox = new JCheckBox("Is Active", false);
        paramPanel.add(activeCheckbox);

        panel.add(paramPanel, BorderLayout.NORTH);
        panel.add(sourcePanel, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.add("Description", panel);

        objectNameLabel = new JLabel();
        triggerNameField = new DisabledField();

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
        base.add(triggerNameField, gbc);
        ++gbc.gridy;
        gbc.insets.top = 0;
        //base.add(schemaNameField, gbc);

        setHeaderText("Database Procedure");
        setHeaderIcon(GUIUtilities.loadIcon("Procedure24.png", true));
        setContentPanel(base);
        cache = new HashMap();

    }

    public String getLayoutName() {
        return NAME;
    }

    public Printable getPrintable() {
//        return new TablePrinter(table, procNameField.getText());\
        return null;
    }

    public void refresh() {
        cache.clear();
    }

    public void cleanup() {}

    public JTable getTable() {
//        return table;
        return null;
    }

    public void removeObject(Object object) {
        if (cache.containsKey(object)) {
            cache.remove(object);
        }
    }

    public boolean hasObject(Object object) {
        return cache.containsKey(object);
    }

    public void setValues(DefaultDatabaseTrigger trigger) {

        objectNameLabel.setText("Trigger Name:");
        setHeaderText("Database Trigger");
        setHeaderIcon(GUIUtilities.loadIcon("TableTrigger16.png", true));

        try {
            triggerNameField.setText(trigger.getName());
//            model.setValues(executeable.getParametersArray());
            textPane.setText(trigger.getTriggerSourceCode());
            activeCheckbox.setSelected(trigger.isTriggerActive());
            //schemaNameField.setText(executeable.getSchemaName());
        }
        catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabaseTrigger trigger = (DefaultDatabaseTrigger)cache.get(metaObject);
        setValues(metaObject, trigger);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabaseTrigger trigger) {

        objectNameLabel.setText("Trigger Name:");
        setHeaderText("Database Trigger");
        setHeaderIcon("Trigger16.png");

        if (trigger != null) {
            triggerNameField.setText(trigger.getName());
            //model.setValues(procedure.getParametersArray());
        } else {
            triggerNameField.setText(metaObject.getName());
        }

        //schemaNameField.setText(metaObject.getSchemaName());
    }

    private void setHeaderIcon(String icon) {

//        setHeaderIcon(GUIUtilities.loadIcon(icon, true));
    }

}
