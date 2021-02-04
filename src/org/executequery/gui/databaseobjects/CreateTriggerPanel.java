package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CreateTriggerPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = "Create Trigger";

    //common components for table and database trigger
    public static final String EDIT_TITLE = "Edit Trigger";

    private static final String TRIGGER = Bundles.get(CreateTriggerPanel.class, "table-trigger");
    private static final String DB_TRIGGER = Bundles.get(CreateTriggerPanel.class, "database-trigger");
    private static final String DDL_TRIGGER = Bundles.get(CreateTriggerPanel.class, "DDL-trigger");

    static String[] meta_types = {"FUNCTION",
            "INDEX",
            "PROCEDURE",
            "SEQUENCE",
            "TABLE",
            "TRIGGER",
            "VIEW",
            "DOMAIN",
            "EXCEPTION",
            "PACKAGE",
            "USER",
            "COLLATION",
            "CHARACTER SET",
            "MAPPING",
            "ROLE",
            "PACKAGE BODY",
    };
    private int triggerType;
    private DefaultDatabaseTrigger trigger;
    private JComboBox typeTriggerCombo;
    private JSpinner positionField;

    //components for database trigger
    private JCheckBox activeBox;
    private JLabel positionLabel;
    private SimpleSqlTextPanel sqlBodyText;

    //components for table trigger
    private SimpleTextArea descriptionText;
    private JPanel databaseTriggerPanel;
    private JComboBox actionCombo;
    private JLabel actionLabel;
    private JLabel labelTable;
    private JPanel tableTriggerPanel;
    private JLabel beforeAfterlabel;

    //components for ddl trigger
    private JComboBox typeTableTriggerCombo;
    private JCheckBox insertBox;
    private JCheckBox deleteBox;
    private JCheckBox updateBox;
    private JPanel ddlTriggerPanel;
    private JPanel ddlTableTriggerPanel;
    private List<JCheckBox> ddlCheckBoxes;
    private JScrollPane scrolDDL;
    private JCheckBox anyDdlBox;
    /**
     * The table combo selection
     */
    private JComboBox tablesCombo;

    /**
     * The constructor
     */

    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent, int triggerType) {
        this(dc, parent, null, triggerType);
    }

    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent, DefaultDatabaseTrigger trigger,
                              int triggerType) {
        super(dc, parent, trigger, new Object[] {triggerType});
    }

    protected void init() {
        if (getDatabaseVersion() > 2) {
            if (this.triggerType == NamedObject.TRIGGER)
                typeTriggerCombo = new JComboBox(new String[]{TRIGGER});
            else if (this.triggerType == NamedObject.DATABASE_TRIGGER)
                typeTriggerCombo = new JComboBox(new String[]{DB_TRIGGER});
            else if (this.triggerType == NamedObject.DDL_TRIGGER)
                typeTriggerCombo = new JComboBox(new String[]{DDL_TRIGGER});
            else
                typeTriggerCombo = new JComboBox(new String[]{TRIGGER, DB_TRIGGER, DDL_TRIGGER});
        } else {
            if (this.triggerType == NamedObject.TRIGGER)
                typeTriggerCombo = new JComboBox(new String[]{TRIGGER});
            else if (this.triggerType == NamedObject.DATABASE_TRIGGER)
                typeTriggerCombo = new JComboBox(new String[]{DB_TRIGGER});
            else
                typeTriggerCombo = new JComboBox(new String[]{TRIGGER, DB_TRIGGER});
        }
        positionLabel = new JLabel(bundleString("position"));
        descriptionText = new SimpleTextArea();
        SpinnerModel model = new SpinnerNumberModel(0, 0, Short.MAX_VALUE, 1);
        positionField = new JSpinner(model);
        positionField.setValue(0);
        activeBox = new JCheckBox(bundlesString("active"));
        activeBox.setSelected(true);
        databaseTriggerPanel = new JPanel();
        actionCombo = new JComboBox(new String[]{"CONNECT", "DISCONNECT", "TRANSACTION START", "TRANSACTION COMMIT", "TRANSACTION ROLLBACK"});
        actionLabel = new JLabel(bundleString("event"));
        tableTriggerPanel = new JPanel();
        typeTableTriggerCombo = new JComboBox(new String[]{"BEFORE", "AFTER"});
        insertBox = new JCheckBox("INSERT");
        updateBox = new JCheckBox("UPDATE");
        deleteBox = new JCheckBox("DELETE");
        labelTable = new JLabel(bundlesString("table"));
        beforeAfterlabel = new JLabel(bundleString("before-after"));
        tablesCombo = new JComboBox(getTables());
        sqlBodyText = new SimpleSqlTextPanel();
        ddlTriggerPanel = new JPanel(new GridBagLayout());
        scrolDDL = new JScrollPane(ddlTriggerPanel);
        scrolDDL.setMinimumSize(new Dimension(100, 200));
        setPreferredSize(new Dimension(800, 800));
        ddlTableTriggerPanel = new JPanel(new GridBagLayout());
        ddlCheckBoxes = new ArrayList<>();
        anyDdlBox = new JCheckBox("ANY DDL STATEMENT");
        sqlBodyText.setSQLText("as\n" +
                "begin\n" +
                "  /* Trigger text */\n" +
                "end");


        typeTriggerCombo.addActionListener(actionEvent -> changeTypeTrigger());

        anyDdlBox.addActionListener(actionEvent -> changeAnyDdlBox());

        centralPanel.setLayout(new GridBagLayout());
        JPanel commonPanel = new JPanel(new GridBagLayout());
        commonPanel.add(typeTriggerCombo, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        commonPanel.add(positionLabel, new GridBagConstraints(1, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        commonPanel.add(positionField, new GridBagConstraints(2, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        commonPanel.add(activeBox, new GridBagConstraints(3, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(commonPanel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTop = new GridBagConstraints(0, 0,
                1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0);
        topPanel.add(databaseTriggerPanel, gbcTop);
        gbcTop.gridy++;
        gbcTop.fill = GridBagConstraints.BOTH;
        topPanel.add(ddlTableTriggerPanel, gbcTop);

        centralPanel.add(topPanel, new GridBagConstraints(0, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        tabbedPane.add(bundlesString("SQL"), sqlBodyText);
        tabbedPane.add(bundlesString("description"), descriptionText);

        GridBagConstraints gbc = new GridBagConstraints();
        databaseTriggerPanel.setLayout(new GridBagLayout());
        tableTriggerPanel.setLayout(new GridBagLayout());
        ddlTriggerPanel.setLayout(new GridBagLayout());
        ddlTableTriggerPanel.setLayout(new GridBagLayout());
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHEAST;
        databaseTriggerPanel.add(actionLabel, gbc);
        tableTriggerPanel.add(labelTable, gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        ddlTableTriggerPanel.add(beforeAfterlabel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        ddlTableTriggerPanel.add(typeTableTriggerCombo, new GridBagConstraints(1, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        gbc.gridx++;
        gbc.weightx = 1;
        databaseTriggerPanel.add(actionCombo, gbc);
        tableTriggerPanel.add(tablesCombo, gbc);
        gbc.gridx++;
        gbc.weightx = 0.1;
        tableTriggerPanel.add(insertBox, gbc);
        gbc.gridx++;
        tableTriggerPanel.add(updateBox, gbc);
        gbc.gridx++;
        tableTriggerPanel.add(deleteBox, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        ddlTableTriggerPanel.add(tableTriggerPanel, gbc);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        ddlTableTriggerPanel.add(scrolDDL, new GridBagConstraints(0, 1,
                2, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                0, 0));
//        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        ddlTriggerPanel.add(anyDdlBox, gbc);
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        gbc.gridy++;
        ddlTriggerPanel.add(separator, gbc);
        gbc.gridwidth = 1;
        int[] addingY = {2, 2, 2};
        for (int i = 0; i < meta_types.length; i++) {
            for (int g = 0; g < 3; g++) {
                String operator = "";
                switch (g) {
                    case 0:
                        operator = "CREATE";
                        break;
                    case 1:
                        operator = "ALTER";
                        break;
                    case 2:
                        operator = "DROP";
                        break;
                }
                JCheckBox checkBox = new JCheckBox(operator + " " + meta_types[i]);
                gbc.gridx = g;
                gbc.gridy = i + addingY[g];
                if (!checkBox.getText().toUpperCase().contains("ALTER COLLATION")
                        && !checkBox.getText().contains("CREATE CHARACTER SET")
                        && !checkBox.getText().contains("DROP CHARACTER SET")
                        && !checkBox.getText().contains("ALTER PACKAGE BODY")
                ) {
                    ddlTriggerPanel.add(checkBox, gbc);
                    ddlCheckBoxes.add(checkBox);
                } else {
                    addingY[g]--;
                }
            }
        }

        changeTypeTrigger();

    }

    private void changeAnyDdlBox() {
        boolean selected = anyDdlBox.isSelected();
        for (JCheckBox checkBox : ddlCheckBoxes) {
            checkBox.setSelected(selected);
            checkBox.setEnabled(!selected);
        }
    }

    protected void initEdited() {
        typeTriggerCombo.setEnabled(false);
        nameField.setText(trigger.getName());
        nameField.setEnabled(false);
        descriptionText.getTextAreaComponent().setText(trigger.getRemarks());
        activeBox.setSelected(trigger.isTriggerActive());
        positionField.setValue(trigger.getTriggerSequence());
        sqlBodyText.setSQLText(trigger.getTriggerSourceCode());
        if (trigger.getIntTriggerType() == DefaultDatabaseTrigger.DATABASE_TRIGGER) {
            int type = ((int) trigger.getLongTriggerType()) - 8192;
            actionCombo.setSelectedIndex(type);
            actionCombo.setEnabled(false);
        } else {
            if (trigger.getStringTriggerType().startsWith("BEFORE"))
                typeTableTriggerCombo.setSelectedIndex(0);
            else typeTableTriggerCombo.setSelectedIndex(1);
            //typeTableTriggerCombo
            if (trigger.getIntTriggerType() == DefaultDatabaseTrigger.TABLE_TRIGGER) {
                insertBox.setSelected(trigger.getStringTriggerType().contains("INSERT"));
                updateBox.setSelected(trigger.getStringTriggerType().contains("UPDATE"));
                deleteBox.setSelected(trigger.getStringTriggerType().contains("DELETE"));
                for (int i = 0; i < tablesCombo.getItemCount(); i++) {
                    if (((String) tablesCombo.getModel().getElementAt(i)).trim().toUpperCase().equals(trigger.getTriggerTableName().trim().toUpperCase())) {
                        tablesCombo.setSelectedIndex(i);
                        tablesCombo.setEnabled(false);
                        break;
                    }
                }
            } else {
                typeTableTriggerCombo.setEnabled(false);
                anyDdlBox.setEnabled(false);
                if (trigger.getStringTriggerType().trim().contains(anyDdlBox.getText())) {
                    anyDdlBox.setSelected(true);
                    changeAnyDdlBox();
                } else {
                    for (JCheckBox checkBox : ddlCheckBoxes) {
                        checkBox.setSelected(trigger.getStringTriggerType().contains(checkBox.getText()));
                        checkBox.setEnabled(false);
                    }
                }
            }
        }

    }

    @Override
    public void createObject() {
        generateScript();
    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return EDIT_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.TRIGGER];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        trigger = (DefaultDatabaseTrigger) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {
        this.triggerType = (int)params[0];
    }

    int getVersion() throws SQLException {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        return host.getDatabaseMetaData().getDatabaseMajorVersion();
    }

    private void changeTypeTrigger() {
        databaseTriggerPanel.setVisible(typeTriggerCombo.getSelectedItem() == DB_TRIGGER);
        ddlTableTriggerPanel.setVisible(typeTriggerCombo.getSelectedItem() == DDL_TRIGGER);
        tableTriggerPanel.setVisible(typeTriggerCombo.getSelectedItem() == TRIGGER);
        scrolDDL.setVisible(typeTriggerCombo.getSelectedItem() == DDL_TRIGGER);
    }

    Object[] getTables() {
        try {
            Vector<String> tables = new Vector<>();
            String query = "Select RDB$RELATION_NAME,RDB$SYSTEM_FLAG from RDB$RELATIONS" +
                    " WHERE RDB$RELATION_TYPE != 1 order by 1";
            ResultSet rs = sender.getResultSet(query).getResultSet();
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
            sender.releaseResources();
            return tables.toArray();
        } catch (SQLException e) {
            Log.error("Error load tables for creating trigger");
            return null;
        }
    }

    private void generateScript() {
        StringBuilder query = new StringBuilder("CREATE OR ALTER TRIGGER " + getFormattedName());
        String selectedItem = (String) typeTriggerCombo.getSelectedItem();
        if (selectedItem == TRIGGER)
            query.append(" FOR ").append(MiscUtils.getFormattedObject(((String) tablesCombo.getSelectedItem()).trim()));
        query.append("\n");
        if (activeBox.isSelected())
            query.append("ACTIVE ");
        else query.append("INACTIVE ");
        if (selectedItem == TRIGGER) {
            query.append(typeTableTriggerCombo.getSelectedItem()).append(" ");
            boolean first = true;
            if (insertBox.isSelected()) {
                first = false;
                query.append("INSERT ");
            }
            if (updateBox.isSelected()) {
                if (!first)
                    query.append("OR ");
                first = false;
                query.append("UPDATE ");
            }
            if (deleteBox.isSelected()) {
                if (!first)
                    query.append("OR ");
                query.append("DELETE ");
            }

        } else {
            if (selectedItem == DB_TRIGGER)
                query.append("ON ").append(actionCombo.getSelectedItem()).append(" ");
            else {
                query.append(typeTableTriggerCombo.getSelectedItem()).append(" ");
                boolean first = true;
                if (anyDdlBox.isSelected()) {
                    query.append(anyDdlBox.getText()).append(" ");
                } else {
                    for (JCheckBox checkBox : ddlCheckBoxes) {
                        if (checkBox.isSelected()) {
                            if (!first)
                                query.append("OR ");
                            query.append(checkBox.getText()).append(" ");
                            first = false;
                        }
                    }
                }
            }
        }
        query.append("POSITION ").append(positionField.getValue()).append("\n");
        query.append(sqlBodyText.getSQLText()).append("^");
        String comment = descriptionText.getTextAreaComponent().getText();
        if (!MiscUtils.isNull(comment) && !comment.trim().isEmpty()) {
            comment = comment.replace("'", "''");
            query.append("COMMENT ON TRIGGER ").append(getFormattedName()).append(" IS '").append(comment).append("'^");
        }
        displayExecuteQueryDialog(query.toString(), "^");
    }

}
