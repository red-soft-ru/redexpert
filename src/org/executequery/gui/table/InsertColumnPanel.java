package org.executequery.gui.table;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.databaseobjects.CreateDomainPanel;
import org.executequery.gui.text.SQLTextPane;
import org.executequery.log.Log;
import org.executequery.sql.spi.LiquibaseStatementGenerator;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


public class InsertColumnPanel extends JPanel implements KeyListener {

    private JPanel upPanel;
    private JPanel domainPanel;
    private JPanel defaultValuePanel;
    private JPanel checkPanel;
    private JPanel computedPanel;
    private JPanel descriptionPanel;
    private JPanel sqlPanel;
    private SelectTypePanel selectTypePanel;
    private AutoIncrementPanel autoIncrementPanel;
    private JTabbedPane tabPane;
    private JLabel tableLabel;
    private JLabel fieldLabel;
    private JLabel domainLabel;
    private JTextField tableNameField;
    private JTextField fieldNameField;
    private JCheckBox notNullBox;
    private JCheckBox primaryBox;
    private JComboBox domainBox;
    private JButton okButton;
    private JButton cancelButton;
    private JButton editDomainButton;
    private JButton newDomainButton;
    private JScrollPane scrollDefaultValue;
    private JScrollPane scrollCheck;
    private JScrollPane scrollComputed;
    private JScrollPane scrollDescription;
    private JScrollPane scrollSQL;
    private SQLTextPane defaultValueTextPane;
    private SQLTextPane checkTextPane;
    private SQLTextPane computedTextPane;
    private SQLTextPane sqlTextPane;
    private JTextPane descriptionTextPane;

    DatabaseConnection databaseConnection;
    ColumnData columnData;
    DatabaseTable table;
    StringBuffer sb;
    MetaDataValues metaData;
    ActionContainer parent;
    DatabaseColumn columnEdited;
    DatabaseTableColumn column;
    boolean editing;
    LiquibaseStatementGenerator statementGenerator;

    public InsertColumnPanel(DatabaseTable table, ActionContainer dialog) {
        this(table, dialog, null);
    }

    public InsertColumnPanel(DatabaseTable table, ActionContainer dialog, DatabaseColumn column) {
        this.table = table;
        parent = dialog;
        databaseConnection = table.getHost().getDatabaseConnection();
        metaData = new MetaDataValues(true);
        metaData.setDatabaseConnection(databaseConnection);
        columnData = new ColumnData(databaseConnection);
        sb = new StringBuffer(200);
        columnEdited = column;
        editing = column != null;
        this.column = new DatabaseTableColumn(table, columnEdited);
        statementGenerator = new LiquibaseStatementGenerator();
        init();
        if (editing)
            init_edited_elements();
    }

    void init() {
        upPanel = new JPanel();
        domainPanel = new JPanel();
        defaultValuePanel = new JPanel();
        checkPanel = new JPanel();
        computedPanel = new JPanel();
        descriptionPanel = new JPanel();
        sqlPanel = new JPanel();
        selectTypePanel = new SelectTypePanel(metaData.getDataTypesArray(), metaData.getIntDataTypesArray(), columnData);
        autoIncrementPanel = new AutoIncrementPanel(databaseConnection, null, columnData.getAutoincrement(), table.getName(), getGenerators());
        tabPane = new JTabbedPane();
        tableLabel = new JLabel("Table:");
        fieldLabel = new JLabel("Name:");
        domainLabel = new JLabel("Domain:");
        tableNameField = new JTextField(table.getName());
        fieldNameField = new JTextField(15);
        notNullBox = new JCheckBox("Not Null");
        primaryBox = new JCheckBox("Primary Key");
        domainBox = new JComboBox();
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        editDomainButton = new JButton("Edit Domain");
        newDomainButton = new JButton("New Domain");
        scrollDefaultValue = new JScrollPane();
        scrollCheck = new JScrollPane();
        scrollComputed = new JScrollPane();
        scrollDescription = new JScrollPane();
        scrollSQL = new JScrollPane();
        defaultValueTextPane = new SQLTextPane();
        checkTextPane = new SQLTextPane();
        computedTextPane = new SQLTextPane();
        sqlTextPane = new SQLTextPane();
        descriptionTextPane = new JTextPane();

        scrollDefaultValue.setViewportView(defaultValueTextPane);

        scrollCheck.setViewportView(checkTextPane);

        scrollComputed.setViewportView(computedTextPane);

        scrollDescription.setViewportView(descriptionTextPane);

        scrollSQL.setViewportView(sqlTextPane);

        tableNameField.setEnabled(false);

        fieldNameField.addKeyListener(this);

        notNullBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                columnData.setNotNull(notNullBox.isSelected());
                if (editing) {
                    column.makeCopy();
                    column.setRequired(notNullBox.isSelected());
                }
            }
        });
        columnData.setNotNull(notNullBox.isSelected());
        primaryBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                columnData.setPrimaryKey(primaryBox.isSelected());
            }
        });

        domainBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                columnData.setDomain((String) domainBox.getSelectedItem());
                if (domainBox.getSelectedIndex() != 0)
                    selectTypePanel.refresh();
                if (editing) {
                    column.makeCopy();
                    column.setDomain((String) domainBox.getSelectedItem());
                } else {
                    editDomainButton.setEnabled(!columnData.getDomain().equals(""));
                }
            }
        });

        tabPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                if (tabPane.getSelectedComponent() == sqlPanel) {
                    generateSQL();
                }
            }
        });

        if (editing)
            domainBox.setModel(new DefaultComboBoxModel(getEditingDomains()));
        else domainBox.setModel(new DefaultComboBoxModel(getDomains()));

        defaultValueTextPane.addKeyListener(this);
        checkTextPane.addKeyListener(this);
        computedTextPane.addKeyListener(this);
        descriptionTextPane.addKeyListener(this);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (tabPane.getSelectedComponent() != sqlPanel)
                    generateSQL();
                ExecuteQueryDialog eqd = new ExecuteQueryDialog("Add Column", sqlTextPane.getText(), databaseConnection, true, "^");
                eqd.display();
                if (eqd.getCommit())
                    parent.finished();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.finished();
            }
        });

        newDomainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                BaseDialog dialog = new BaseDialog(CreateDomainPanel.CREATE_TITLE, true);
                CreateDomainPanel panel = new CreateDomainPanel(databaseConnection, dialog);
                dialog.addDisplayComponent(panel);
                dialog.display();
            }
        });
        editDomainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                BaseDialog dialog = new BaseDialog(CreateDomainPanel.EDIT_TITLE, true);
                CreateDomainPanel panel = new CreateDomainPanel(databaseConnection, dialog, (String) domainBox.getSelectedItem());
                dialog.addDisplayComponent(panel);
                dialog.display();
            }
        });

        this.setLayout(new GridBagLayout());
        upPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 2, 2), 0, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        upPanel.add(tableLabel, gbc);
        gbc.gridy++;
        upPanel.add(fieldLabel, gbc);
        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        upPanel.add(tableNameField, gbc);
        gbc.gridy++;
        upPanel.add(fieldNameField, gbc);
        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        upPanel.add(notNullBox, gbc);
        gbc.gridy++;
        upPanel.add(primaryBox, gbc);
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        domainPanel.setLayout(new GridBagLayout());
        domainPanel.add(domainLabel, gbc);
        gbc.gridx++;
        gbc.weightx = 0.5;
        domainPanel.add(domainBox, gbc);
        gbc.gridx++;
        gbc.weightx = 1;
        domainPanel.add(new Panel(), gbc);
        gbc.ipadx = 0;
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        domainPanel.add(editDomainButton, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        domainPanel.add(newDomainButton, gbc);
        defaultValuePanel.setLayout(new GridBagLayout());
        checkPanel.setLayout(new GridBagLayout());
        computedPanel.setLayout(new GridBagLayout());
        descriptionPanel.setLayout(new GridBagLayout());
        sqlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcFull = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        defaultValuePanel.add(scrollDefaultValue, gbcFull);
        checkPanel.add(scrollCheck, gbcFull);
        computedPanel.add(scrollComputed, gbcFull);
        descriptionPanel.add(scrollDescription, gbcFull);
        sqlPanel.add(scrollSQL, gbcFull);
        tabPane.add("Domain", domainPanel);
        if (!editing || columnEdited.getDomain().toUpperCase().startsWith("RDB$"))
            tabPane.add("Type", selectTypePanel);
        tabPane.add("Default Value", defaultValuePanel);
        if (!editing)
            tabPane.add("Check", checkPanel);
        if (!editing || !MiscUtils.isNull(columnEdited.getComputedSource()))
            tabPane.add("Computed by", computedPanel);
        tabPane.add("Autoincrement", autoIncrementPanel);
        tabPane.add("Description", descriptionPanel);
        tabPane.add("SQL", sqlPanel);
        tabPane.setPreferredSize(new Dimension(700, 400));
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        this.add(upPanel, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        this.add(tabPane, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JPanel(), gbc);
        gbc.weightx = 0.1;
        gbc.gridx++;
        this.add(okButton, gbc);
        gbc.gridx++;
        this.add(cancelButton, gbc);
        editDomainButton.setEnabled(false);
    }

    void init_edited_elements() {
        columnData.setSQLType(column.getTypeInt());
        columnData.setColumnType(column.getTypeName());
        columnData.setColumnSize(column.getColumnSize());
        columnData.setColumnScale(column.getColumnScale());
        columnData.setColumnSubtype(column.getColumnSubtype());
        selectTypePanel.refresh();
        fieldNameField.setText(columnEdited.getName());
        notNullBox.setSelected(columnEdited.isRequired());
        if (getVersion() < 3)
            notNullBox.setEnabled(false);
        defaultValueTextPane.setText(columnEdited.getDefaultValue() != null ? columnEdited.getDefaultValue() : "");
        computedTextPane.setText(columnEdited.getComputedSource() != null ? columnEdited.getComputedSource() : "");
        descriptionTextPane.setText(columnEdited.getRemarks() != null ? columnEdited.getRemarks() : "");
        /*if(!MiscUtils.isNull(columnEdited.getComputedSource()))
        {
            notNullBox.setEnabled(false);
        }*/
        primaryBox.setEnabled(false);
        editDomainButton.setEnabled(true);
    }


    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {

    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        if (keyEvent.getSource() == defaultValueTextPane) {
            columnData.setDefaultValue(defaultValueTextPane.getText());
            if (editing) {
                column.makeCopy();
                column.setDefaultValue(defaultValueTextPane.getText());
            }
        } else if (keyEvent.getSource() == checkTextPane) {
            columnData.setCheck(checkTextPane.getText());
        } else if (keyEvent.getSource() == computedTextPane) {
            columnData.setComputedBy(computedTextPane.getText());
            if (editing) {
                column.makeCopy();
                column.setComputedSource(computedTextPane.getText());
            }
        } else if (keyEvent.getSource() == descriptionTextPane) {
            columnData.setDescription(descriptionTextPane.getText());
            if (editing) {
                column.makeCopy();
                column.setColumnDescription(descriptionTextPane.getText());
            }
        } else if (keyEvent.getSource() == fieldNameField) {
            columnData.setColumnName(fieldNameField.getText());
            if (editing) {
                column.makeCopy();
                column.setName(fieldNameField.getText());
            }
        }

    }

    String[] getGenerators() {
        DefaultStatementExecutor executor = new DefaultStatementExecutor(databaseConnection, true);
        List<String> domains = new ArrayList<>();
        try {
            String query = "select " +
                    "RDB$GENERATOR_NAME FROM RDB$GENERATORS " +
                    "where RDB$SYSTEM_FLAG = 0 " +
                    "order by 1";
            ResultSet rs = executor.execute(QueryTypes.SELECT, query).getResultSet();
            while (rs.next()) {
                domains.add(rs.getString(1).trim());
            }
            executor.releaseResources();
            return domains.toArray(new String[domains.size()]);
        } catch (Exception e) {
            Log.error("Error loading generators:" + e.getMessage());
            return null;
        }
    }

    int getVersion() {
        DatabaseHost host = new DefaultDatabaseHost(databaseConnection);
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

    String[] getDomains() {
        DefaultStatementExecutor executor = new DefaultStatementExecutor(databaseConnection, true);
        List<String> domains = new ArrayList<>();
        domains.add("");
        try {
            String query = "select " +
                    "RDB$FIELD_NAME FROM RDB$FIELDS " +
                    "where RDB$FIELD_NAME not like 'RDB$%'\n" +
                    "and RDB$FIELD_NAME not like 'MON$%'\n" +
                    "order by RDB$FIELD_NAME";
            ResultSet rs = executor.execute(QueryTypes.SELECT, query).getResultSet();
            while (rs.next()) {
                domains.add(rs.getString(1).trim());
            }
            executor.releaseResources();
            return domains.toArray(new String[domains.size()]);
        } catch (Exception e) {
            Log.error("Error loading domains:" + e.getMessage());
            return null;
        }
    }

    String[] getEditingDomains() {
        DefaultStatementExecutor executor = new DefaultStatementExecutor(databaseConnection, true);
        List<String> domains = new ArrayList<>();
        domains.add(columnEdited.getDomain());
        try {
            String query = "select " +
                    "RDB$FIELD_NAME FROM RDB$FIELDS " +
                    "where RDB$FIELD_NAME not like 'RDB$%'\n" +
                    "and RDB$FIELD_NAME not like 'MON$%'\n" +
                    "order by RDB$FIELD_NAME";
            ResultSet rs = executor.execute(QueryTypes.SELECT, query).getResultSet();
            while (rs.next()) {
                domains.add(rs.getString(1).trim());
            }
            executor.releaseResources();
            return domains.toArray(new String[domains.size()]);
        } catch (Exception e) {
            Log.error("Error loading domains:" + e.getMessage());
            return null;
        }
    }

    void generateSQL() {
        sb.setLength(0);
        if (editing) {
            column.makeCopy();
            column.setTypeInt(columnData.getSQLType());
            column.setTypeName(columnData.getColumnType());
            column.setColumnSize(columnData.getColumnSize());
            column.setColumnScale(columnData.getColumnScale());
            sb.append(statementGenerator.alterColumn(column, table).replace(";", "^"));
            if (columnData.isAutoincrement()) {
                sb.append(columnData.getAutoincrement().getSqlAutoincrement());
            }
            sqlTextPane.setText(sb.toString());
        } else {
            sb.append("ALTER TABLE ").append(table.getName()).append("\nADD ").append(columnData.getColumnName()).append("\n");
            if (MiscUtils.isNull(columnData.getComputedBy())) {
                if (MiscUtils.isNull(columnData.getDomain())) {
                    if (columnData.getColumnType() != null) {
                        sb.append(columnData.getFormattedDataType());
                    }
                } else {
                    sb.append(columnData.getDomain());
                }
                if (!MiscUtils.isNull(columnData.getDefaultValue())) {
                    String value = "";
                    boolean str = false;
                    int sqlType = columnData.getSQLType();
                    switch (sqlType) {

                        case Types.LONGVARCHAR:
                        case Types.LONGNVARCHAR:
                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.VARCHAR:
                        case Types.NVARCHAR:
                        case Types.CLOB:
                        case Types.DATE:
                        case Types.TIME:
                        case Types.TIMESTAMP:
                            value = "'";
                            str = true;
                            break;
                        default:
                            break;
                    }
                    value += columnData.getDefaultValue();
                    if (str) {
                        value += "'";
                    }
                    sb.append(" DEFAULT " + value);
                }
                sb.append(columnData.isRequired() ? " NOT NULL" : "");
                if (!MiscUtils.isNull(columnData.getCheck())) {
                    sb.append(" CHECK ( " + columnData.getCheck() + ")");
                }
            } else {
                sb.append("COMPUTED BY ( " + columnData.getComputedBy() + ")");
            }
            if (columnData.isPrimaryKey()) {
                sb.append(" PRIMARY KEY");
            }
            sb.append("^");
            if (!MiscUtils.isNull(columnData.getDescription())) {
                sb.append("\nCOMMENT ON COLUMN ").append(table.getName()).append(".").append(columnData.getColumnName()).append(" IS '")
                        .append(columnData.getDescription()).append("'^");
            }
            autoIncrementPanel.generateAI();
            if (columnData.isAutoincrement()) {
                sb.append(columnData.getAutoincrement().getSqlAutoincrement());
            }
            sqlTextPane.setText(sb.toString());
        }
    }
}
