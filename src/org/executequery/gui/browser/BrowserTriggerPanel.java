package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SQLTextPane;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DefaultComboBox;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.StyledLogPane;

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

    private DependenciesPanel dependenciesPanel;

    private DisabledField triggerNameField;
    private DisabledField triggerBeforeAfterField;
    private DisabledField triggerPositionField;

    private JLabel objectNameLabel;

    private JCheckBox activeCheckbox;

    private JLabel triggerInfoLabel;
    private JLabel beforeAfterLabel;
    private JLabel triggerPositionLabel;
    private JComboBox tableNameCombo;
    JTextPane descriptionPane;
    JTextPane sqlPane;

    private Map cache;

    JTextPane textPane;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    public BrowserTriggerPanel(BrowserController controller) {
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
        paramPanel.setBorder(BorderFactory.createTitledBorder(Bundles.getCommon("Parameters")));

        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.setBorder(BorderFactory.createTitledBorder(bundleString("Source")));
        textPane = new SQLTextPane();
        textPane.setEditable(false);
        sourcePanel.add(new JScrollPane(textPane), BorderLayout.CENTER);

        activeCheckbox = new JCheckBox(bundleString("Active"), false);
        paramPanel.add(activeCheckbox, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));

        triggerInfoLabel = new JLabel(bundleString("ForTable"));
        paramPanel.add(triggerInfoLabel, new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));

        tableNameCombo = new DefaultComboBox();
        //paramPanel.add(tableNameCombo);

        beforeAfterLabel = new JLabel(bundleString("BeforeAfter"));
        //paramPanel.add(beforeAfterLabel);

        triggerBeforeAfterField = new DisabledField();
        //paramPanel.add(triggerBeforeAfterField);

        triggerPositionLabel = new JLabel(bundleString("Position"));
        //paramPanel.add(triggerPositionLabel);

        triggerPositionField = new DisabledField();

        //paramPanel.add(triggerPositionField);

        panel.add(paramPanel, BorderLayout.NORTH);
        panel.add(sourcePanel, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.add(bundleString("Trigger"), panel);

        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBorder(BorderFactory.createEtchedBorder());

        descriptionPane = new StyledLogPane();

        descriptionPanel.add(descriptionPane, BorderLayout.CENTER);

        tabs.add(Bundles.getCommon("description"), descriptionPanel);

        JPanel sqlPanel = new JPanel(new BorderLayout());
        sqlPanel.setBorder(BorderFactory.createEtchedBorder());

        sqlPane = new SQLTextPane();

        sqlPanel.add(sqlPane, BorderLayout.CENTER);

        tabs.add("Sql", sqlPanel);
        tabs.add(Bundles.getCommon("dependencies"), dependenciesPanel);

        objectNameLabel = new JLabel();
        triggerNameField = new DisabledField();

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

    public void cleanup() {
    }

    public JTable getTable() {
//        return table;
        return null;
    }

    public void removeObject(Object object) {
        cache.remove(object);
    }

    public boolean hasObject(Object object) {
        return cache.containsKey(object);
    }

    public void setValues(DefaultDatabaseTrigger trigger) {
        dependenciesPanel.setDatabaseObject(trigger);
        objectNameLabel.setText(bundleString("TriggerName"));
        setHeaderText(bundleString("DatabaseTrigger"));
        setHeaderIcon(GUIUtilities.loadIcon("TableTrigger16.png", true));

        try {
            triggerNameField.setText(trigger.getName());
            textPane.setText(trigger.getTriggerSourceCode());
            activeCheckbox.setSelected(trigger.isTriggerActive());
            activeCheckbox.setEnabled(false);
            if (!trigger.getStringTriggerType().toLowerCase().contains("before") &&
                    !trigger.getStringTriggerType().toLowerCase().contains("after"))
                beforeAfterLabel.setText(bundleString("Event"));
            else if (trigger.getStringTriggerType().toLowerCase().contains("before"))
                beforeAfterLabel.setText(bundleString("Before"));
            else if (trigger.getStringTriggerType().toLowerCase().contains("after"))
                beforeAfterLabel.setText(bundleString("After"));
            else
                beforeAfterLabel.setText(bundleString("BeforeAfter"));
            triggerBeforeAfterField.setText(trigger.getStringTriggerType());
            tableNameCombo.removeAllItems();
            if (trigger.getTriggerTableName() != null && !trigger.getTriggerTableName().isEmpty()) {
                triggerInfoLabel.setText(bundleString("ForTable") + trigger.getTriggerTableName().trim());
                //tableNameCombo.setVisible(true);
                //tableNameCombo.addItem(trigger.getTriggerTableName());
            } else {
                triggerInfoLabel.setText("");
                //tableNameCombo.setVisible(false);
            }
            triggerPositionField.setText(String.valueOf(trigger.getTriggerSequence()));
            descriptionPane.setText(trigger.getTriggerDescription());
            sqlPane.setText(trigger.getCreateSQLText());
            triggerInfoLabel.setText(triggerInfoLabel.getText() + "       " + triggerBeforeAfterField.getText() + "       " + triggerPositionLabel.getText() + triggerPositionField.getText());
        } catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabaseTrigger trigger = (DefaultDatabaseTrigger) cache.get(metaObject);
        setValues(metaObject, trigger);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabaseTrigger trigger) {

        objectNameLabel.setText(bundleString("TriggerName"));
        setHeaderText(bundleString("Database Trigger"));
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
