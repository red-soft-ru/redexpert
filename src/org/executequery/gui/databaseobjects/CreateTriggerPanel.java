package org.executequery.gui.databaseobjects;

import org.executequery.components.SplitPaneFactory;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.BrowserConstants;
import org.executequery.gui.browser.comparer.Trigger;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.log.Log;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CreateTriggerPanel extends JPanel {

    /**
     * The connection combo selection
     */
    private JComboBox connectionsCombo;

    /**
     * the schema combo box model
     */
    private DynamicComboBoxModel connectionsModel;

    /**
     * the type trigger combo selection
     */
    private JComboBox typeTriggerCombo;

    //common components for table and database trigger

    private JTextField nameField;

    private NumberTextField positionField;

    private JCheckBox activeBox;

    private JLabel connectionLabel;

    private JLabel nameLabel;

    private JLabel positionLabel;

    private SimpleSqlTextPanel sqlBodyText;

    private JScrollPane scrollSqlBody;

    private JButton okButton;

    private JButton cancelButton;

    private JSplitPane splitPane;

    //components for database trigger

    private JPanel databaseTriggerPanel;

    private JComboBox actionCombo;

    private JLabel actionLabel;

    //components for table trigger

    private JLabel labelTable;

    private JPanel tableTriggerPanel;

    private JLabel beforeAfterlabel;

    private JComboBox typeTableTriggerCombo;

    private JCheckBox insertBox;

    private JCheckBox deleteBox;

    private JCheckBox updateBox;

    //components for ddl trigger

    private JPanel ddlTriggerPanel;

    private JPanel ddlTableTriggerPanel;

    private List<JCheckBox> ddlCheckBoxes;

    private JScrollPane scrolDDL;

    private JCheckBox anyDdlBox;

    /**
     * The table combo selection
     */
    private JComboBox tablesCombo;

    private DatabaseConnection connection;

    DefaultStatementExecutor executor;

    ActionContainer parent;

    DefaultDatabaseTrigger trigger;

    boolean editing;

    String[] meta_types = {"FUNCTION",
            "INDEX",
            "PROCEDURE",
            "SEQUENCE",
            "TABLE",
            "TRIGGER",
            "VIEW",
            "DOMAIN",
            "EXCEPTION",
            "PACKAGE",
            "PACKAGE BODY",
            "USER",
            "COLLATION",
            "CHARACTER SET",
            "MAPPING",
            "ROLE"};

    public static final String CREATE_TITLE = "Create Trigger";

    public static final String EDIT_TITLE = "Edit Trigger";

    /**
     * The constructor
     */

    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent) {
        this(dc, parent, null);
    }

    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent, DefaultDatabaseTrigger trigger) {
        this.trigger = trigger;
        this.parent = parent;
        connection = dc;
        executor = new DefaultStatementExecutor(connection, true);
        try {
            init();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        editing = trigger != null;
        if (editing)
            init_edited();
    }

    void init() throws SQLException {
        if (getVersion() > 2)
            typeTriggerCombo = new JComboBox(new String[]{"Table trigger", "Database trigger", "DDL trigger"});
        else typeTriggerCombo = new JComboBox(new String[]{"Table trigger", "Database trigger"});
        nameField = new JTextField(15);
        connectionLabel = new JLabel("Connection");
        nameLabel = new JLabel("Name");
        positionLabel = new JLabel("Position");
        positionField = new NumberTextField();
        positionField.setValue(0);
        activeBox = new JCheckBox("Active");
        databaseTriggerPanel = new JPanel();
        actionCombo = new JComboBox(new String[]{"CONNECT", "DISCONNECT", "TRANSACTION START", "TRANSACTION COMMIT", "TRANSACTION ROLLBACK"});
        actionLabel = new JLabel("Event");
        tableTriggerPanel = new JPanel();
        typeTableTriggerCombo = new JComboBox(new String[]{"BEFORE", "AFTER"});
        insertBox = new JCheckBox("INSERT");
        updateBox = new JCheckBox("UPDATE");
        deleteBox = new JCheckBox("DELETE");
        labelTable = new JLabel("Table");
        beforeAfterlabel = new JLabel("Before/After");
        tablesCombo = new JComboBox(getTables());
        sqlBodyText = new SimpleSqlTextPanel();
        scrollSqlBody = new JScrollPane(sqlBodyText);
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        ddlTriggerPanel = new JPanel(new GridBagLayout());
        scrolDDL = new JScrollPane(ddlTriggerPanel);
        scrolDDL.setMinimumSize(new Dimension(100, 200));
        setPreferredSize(new Dimension(800, 800));
        ddlTableTriggerPanel = new JPanel(new GridBagLayout());
        ddlCheckBoxes = new ArrayList<>();
        anyDdlBox = new JCheckBox("ANY DDL STATEMENT");
        sqlBodyText.setSQLText("AS\n" +
                "begin\n" +
                "  /* Trigger text */\n" +
                "end");

        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);
        connectionsCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
                executor.setDatabaseConnection(connection);
            }
        });
        if (connection != null) {
            connectionsCombo.setSelectedItem(connection);
        } else {
            connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
        }

        typeTriggerCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                changeTypeTrigger();
            }
        });

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                generateScript();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.finished();
            }
        });

        anyDdlBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                changeAnyDdlBox();
            }
        });

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0);
        this.add(connectionLabel, gbc);
        gbc.gridx++;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(connectionsCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        this.add(nameLabel, gbc);
        gbc.gridx++;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(nameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        this.add(typeTriggerCombo, gbc);
        gbc.gridx += 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        this.add(positionLabel, gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        this.add(positionField, new GridBagConstraints(3, 2,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        gbc.gridx++;
        this.add(activeBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 5;

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTop = new GridBagConstraints(0, 0,
                1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0);
        topPanel.add(databaseTriggerPanel, gbcTop);
        gbcTop.gridy++;
        gbcTop.fill = GridBagConstraints.BOTH;
        topPanel.add(ddlTableTriggerPanel, gbcTop);

        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcBottom = new GridBagConstraints(0, 0,
                1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                0, 0);
        bottomPanel.add(scrollSqlBody, gbcBottom);

        splitPane = new SplitPaneFactory().create(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setDividerLocation(-1);
        splitPane.setDividerSize(5);

        this.add(splitPane, gbc);

        gbc.gridy++;
        gbc.gridx = 3;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;

        JPanel okCancelPanel = new JPanel(new GridBagLayout());
        okCancelPanel.add(okButton, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        okCancelPanel.add(cancelButton, new GridBagConstraints(1, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
//        gbc.gridx++;
        this.add(okCancelPanel, gbc);

        databaseTriggerPanel.setLayout(new GridBagLayout());
        tableTriggerPanel.setLayout(new GridBagLayout());
        ddlTriggerPanel.setLayout(new GridBagLayout());
        ddlTableTriggerPanel.setLayout(new GridBagLayout());
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
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
                gbc.gridy = i + 2;
                if (!checkBox.getText().toUpperCase().contains("ALTER COLLATION")
                        && !checkBox.getText().contains("CREATE CHARACTER SET")
                        && !checkBox.getText().contains("DROP CHARACTER SET")
                        && !checkBox.getText().contains("ALTER PACKAGE BODY")
                        ) {
                    ddlTriggerPanel.add(checkBox, gbc);
                    ddlCheckBoxes.add(checkBox);
                }
            }
        }

        changeTypeTrigger();

    }

    void changeAnyDdlBox() {
        boolean selected = anyDdlBox.isSelected();
        for (JCheckBox checkBox : ddlCheckBoxes) {
            checkBox.setSelected(selected);
            checkBox.setEnabled(!selected);
        }
    }

    void init_edited() {
        typeTriggerCombo.setSelectedIndex(trigger.getIntTriggerType());
        typeTriggerCombo.setEnabled(false);
        nameField.setText(trigger.getName());
        nameField.setEnabled(false);
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

    int getVersion() throws SQLException {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        return host.getDatabaseMetaData().getDatabaseMajorVersion();
    }

    void changeTypeTrigger() {
        boolean dbtrigger = typeTriggerCombo.getSelectedIndex() == 1;
        boolean tabletrigger = typeTriggerCombo.getSelectedIndex() == 0;
        databaseTriggerPanel.setVisible(dbtrigger);
        ddlTableTriggerPanel.setVisible(!dbtrigger);
        tableTriggerPanel.setVisible(tabletrigger);
        scrolDDL.setVisible(!tabletrigger && !dbtrigger);

        splitPane.setDividerLocation(-1);
    }

    Object[] getTables() {
        try {
            Vector<String> tables = new Vector<>();
            String query = "Select RDB$RELATION_NAME,RDB$SYSTEM_FLAG from RDB$RELATIONS" +
                    " WHERE RDB$RELATION_TYPE != 1 order by 1";
            ResultSet rs = executor.getResultSet(query).getResultSet();
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
            executor.releaseResources();
            return tables.toArray();
        } catch (SQLException e) {
            Log.error("Error load tables for creating trigger");
            return null;
        }
    }

    void generateScript() {
        String query = "CREATE OR ALTER TRIGGER " + nameField.getText();
        if (typeTriggerCombo.getSelectedIndex() == 0)
            query += " FOR " + tablesCombo.getSelectedItem();
        query += "\n";
        if (activeBox.isSelected())
            query += "ACTIVE ";
        else query += "INACTIVE ";
        if (typeTriggerCombo.getSelectedIndex() == 0) {
            query += typeTableTriggerCombo.getSelectedItem() + " ";
            boolean first = true;
            if (insertBox.isSelected()) {
                first = false;
                query += "INSERT ";
            }
            if (updateBox.isSelected()) {
                if (!first)
                    query += "OR ";
                first = false;
                query += "UPDATE ";
            }
            if (deleteBox.isSelected()) {
                if (!first)
                    query += "OR ";
                query += "DELETE ";
            }

        } else {
            if (typeTriggerCombo.getSelectedIndex() == 1)
                query += "ON " + actionCombo.getSelectedItem() + " ";
            else {
                query += typeTableTriggerCombo.getSelectedItem() + " ";
                boolean first = true;
                if (anyDdlBox.isSelected()) {
                    query += anyDdlBox.getText() + " ";
                } else {
                    for (JCheckBox checkBox : ddlCheckBoxes) {
                        if (checkBox.isSelected()) {
                            if (!first)
                                query += "OR ";
                            query += checkBox.getText() + " ";
                            first = false;
                        }
                    }
                }
            }
        }
        query += "POSITION " + positionField.getValue() + "\n";
        query += sqlBodyText.getSQLText();
        ExecuteQueryDialog eqd = new ExecuteQueryDialog("Creating trigger", query, connection, true, "^");
        eqd.display();
        if (eqd.getCommit())
            parent.finished();
    }

}
