package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.BrowserConstants;
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

    //components for database trigger

    private JPanel databaseTriggerPanel;

    private JComboBox actionCombo;

    private JLabel actionLabel;

    //components for table trigger

    private JLabel labelTable;

    private JPanel tableTriggerPanel;

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

    public static final String TITLE = "Create Trigger";

    /**
     * The constructor
     */
    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent) {
        this.parent = parent;
        connection = dc;
        executor = new DefaultStatementExecutor(connection, true);
        init();
    }

    void init() {
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
        tablesCombo = new JComboBox(getTables());
        sqlBodyText = new SimpleSqlTextPanel();
        scrollSqlBody = new JScrollPane(sqlBodyText);
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        ddlTriggerPanel = new JPanel();
        scrolDDL = new JScrollPane(ddlTriggerPanel);
        scrolDDL.setMinimumSize(new Dimension(100, 200));
        setPreferredSize(new Dimension(800, 800));
        ddlTableTriggerPanel = new JPanel();
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
                boolean selected = anyDdlBox.isSelected();
                for (JCheckBox checkBox : ddlCheckBoxes) {
                    checkBox.setSelected(selected);
                    checkBox.setEnabled(!selected);
                }
            }
        });

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1),
                0, 0);
        this.add(connectionLabel, gbc);
        gbc.gridx++;
        gbc.gridwidth = 3;
        this.add(connectionsCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        this.add(nameLabel, gbc);
        gbc.gridx++;
        gbc.gridwidth = 3;
        this.add(nameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        this.add(typeTriggerCombo, gbc);
        gbc.gridx++;
        this.add(positionLabel, gbc);
        gbc.gridx++;
        this.add(positionField, gbc);
        gbc.gridx++;
        this.add(activeBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 4;
        this.add(databaseTriggerPanel, gbc);
        gbc.gridy++;
        this.add(ddlTableTriggerPanel, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(scrollSqlBody, gbc);
        gbc.gridy++;
        gbc.gridx = 2;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        this.add(okButton, gbc);
        gbc.gridx++;
        this.add(cancelButton, gbc);

        databaseTriggerPanel.setLayout(new GridBagLayout());
        tableTriggerPanel.setLayout(new GridBagLayout());
        ddlTriggerPanel.setLayout(new GridBagLayout());
        ddlTableTriggerPanel.setLayout(new GridBagLayout());
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        databaseTriggerPanel.add(actionLabel, gbc);
        ddlTableTriggerPanel.add(typeTableTriggerCombo, gbc);
        tableTriggerPanel.add(labelTable, gbc);
        gbc.gridx++;
        databaseTriggerPanel.add(actionCombo, gbc);
        tableTriggerPanel.add(tablesCombo, gbc);
        gbc.gridx++;
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
        gbc.gridwidth = 1;
        ddlTableTriggerPanel.add(tableTriggerPanel, gbc);
        gbc.gridy++;
        ddlTableTriggerPanel.add(scrolDDL, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
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

    int getVersion() {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        String vers = host.getDatabaseProductVersion();
        int version = 2;
        if (vers != null) {
            int number = 0;
            for (int i = 0; i < vers.length(); i++) {
                if (Character.isDigit(vers.charAt(i))) {
                    number = Character.getNumericValue(vers.charAt(i));
                    break;
                }
            }
            if (number >= 3)
                version = 3;

        }
        return version;
    }

    void changeTypeTrigger() {
        boolean dbtrigger = typeTriggerCombo.getSelectedIndex() == 1;
        boolean tabletrigger = typeTriggerCombo.getSelectedIndex() == 0;
        databaseTriggerPanel.setVisible(dbtrigger);
        ddlTableTriggerPanel.setVisible(!dbtrigger);
        tableTriggerPanel.setVisible(tabletrigger);
        scrolDDL.setVisible(!tabletrigger && !dbtrigger);

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
        String query = "CREATE TRIGGER " + nameField.getText();
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
