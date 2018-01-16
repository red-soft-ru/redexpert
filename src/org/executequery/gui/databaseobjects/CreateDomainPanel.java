package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.SelectTypePanel;
import org.executequery.gui.text.SQLTextPane;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;

public class CreateDomainPanel extends JPanel implements KeyListener {
    private JLabel fieldLabel;
    private JTabbedPane tabPane;
    private JScrollPane scrollDefaultValue;
    private JScrollPane scrollCheck;
    private JScrollPane scrollDescription;
    private JScrollPane scrollSQL;
    private SQLTextPane defaultValueTextPane;
    private SQLTextPane checkTextPane;
    private SQLTextPane sqlTextPane;
    private JTextPane descriptionTextPane;
    private JPanel defaultValuePanel;
    private JPanel checkPanel;
    private JPanel descriptionPanel;
    private JPanel upPanel;
    private JPanel sqlPanel;
    private SelectTypePanel selectTypePanel;
    private JCheckBox notNullBox;
    private JButton okButton;
    private JButton cancelButton;
    private JTextField fieldNameField;

    public static final String CREATE_TITLE = "Create Domain";
    public static final String EDIT_TITLE = "Edit Domain";

    DatabaseConnection databaseConnection;
    ColumnData columnData;
    ActionContainer parent;
    MetaDataValues metaData;
    boolean editing;
    String domain;

    public CreateDomainPanel(DatabaseConnection connection, ActionContainer parent, String domain) {
        databaseConnection = connection;
        this.parent = parent;
        this.domain = domain;
        metaData = new MetaDataValues(databaseConnection, true);
        columnData = new ColumnData(databaseConnection);
        editing = domain != null;
        init();
        if (editing) {
            try {
                init_edited();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public CreateDomainPanel(DatabaseConnection connection, ActionContainer parent) {
        this(connection, parent, null);
    }

    void init() {
        upPanel = new JPanel(new GridBagLayout());
        defaultValuePanel = new JPanel(new GridBagLayout());
        checkPanel = new JPanel(new GridBagLayout());
        descriptionPanel = new JPanel(new GridBagLayout());
        sqlPanel = new JPanel(new GridBagLayout());
        selectTypePanel = new SelectTypePanel(metaData.getDataTypesArray(), metaData.getIntDataTypesArray(), columnData);
        tabPane = new JTabbedPane();
        fieldLabel = new JLabel("Name:");
        fieldNameField = new JTextField();
        notNullBox = new JCheckBox("Not Null");
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        scrollDefaultValue = new JScrollPane();
        scrollCheck = new JScrollPane();
        scrollDescription = new JScrollPane();
        scrollSQL = new JScrollPane();
        defaultValueTextPane = new SQLTextPane();
        checkTextPane = new SQLTextPane();
        sqlTextPane = new SQLTextPane();
        descriptionTextPane = new JTextPane();

        scrollDefaultValue.setViewportView(defaultValueTextPane);

        scrollCheck.setViewportView(checkTextPane);

        scrollDescription.setViewportView(descriptionTextPane);

        scrollSQL.setViewportView(sqlTextPane);

        fieldNameField.addKeyListener(this);

        notNullBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                columnData.setNotNull(notNullBox.isSelected());
            }
        });
        columnData.setNotNull(notNullBox.isSelected());
        tabPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                selectTypePanel.refreshColumn();
                if (tabPane.getSelectedComponent() == sqlPanel) {
                    generateSQL();
                }
            }
        });
        defaultValueTextPane.addKeyListener(this);
        checkTextPane.addKeyListener(this);
        descriptionTextPane.addKeyListener(this);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (tabPane.getSelectedComponent() != sqlPanel)
                    generateSQL();
                ExecuteQueryDialog eqd = new ExecuteQueryDialog("Add Domain", sqlTextPane.getText(), databaseConnection, true);
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


        this.setLayout(new GridBagLayout());
        upPanel.add(fieldLabel, new GridBagConstraints(0, 0, 1, 1,
                0, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        upPanel.add(fieldNameField, new GridBagConstraints(1, 0, 1, 1,
                1, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        upPanel.add(notNullBox, new GridBagConstraints(2, 0, 0, 1,
                0, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

        this.add(upPanel, new GridBagConstraints(0, 0, 3, 1,
                1, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

        defaultValuePanel.add(scrollDefaultValue, new GridBagConstraints(0, 0, 1, 1,
                1, 1, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        checkPanel.add(scrollCheck, new GridBagConstraints(0, 0, 1, 1,
                1, 1, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        descriptionPanel.add(scrollDescription, new GridBagConstraints(0, 0, 1, 1,
                1, 1, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        sqlPanel.add(scrollSQL, new GridBagConstraints(0, 0, 1, 1,
                1, 1, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        tabPane.add("Type", selectTypePanel);
        tabPane.add("Default Value", defaultValuePanel);
        tabPane.add("Check", checkPanel);
        tabPane.add("Description", descriptionPanel);
        tabPane.add("SQL", sqlPanel);

        this.add(tabPane, new GridBagConstraints(0, 1, 3, 1,
                1, 1, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        JPanel okCancelPanel = new JPanel(new GridBagLayout());
        okCancelPanel.add(okButton, new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        okCancelPanel.add(cancelButton, new GridBagConstraints(1, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        this.add(okCancelPanel, new GridBagConstraints(2, 3, 0, 1,
                0, 0, GridBagConstraints.EAST,
                GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    }

    void init_edited() throws SQLException {
        columnData.setColumnName(domain);
        columnData.setDomain(domain);
        columnData.setDescription(columnData.getDomainDescription());
        columnData.setCheck(columnData.getDomainCheck());
        columnData.setNotNull(columnData.isDomainNotNull());
        columnData.setDefaultValue(columnData.getDomainDefault());
        descriptionTextPane.setText(columnData.getDescription());
        checkTextPane.setText(columnData.getCheck());
        defaultValueTextPane.setText(columnData.getDefaultValue());
        fieldNameField.setText(columnData.getColumnName());
        notNullBox.setSelected(columnData.isRequired());
        if (getVersion() < 3)
            notNullBox.setEnabled(false);
        selectTypePanel.refresh();
        columnData.makeCopy();
    }

    int getVersion() throws SQLException {
        DatabaseHost host = new DefaultDatabaseHost(databaseConnection);
        return host.getDatabaseMetaData().getDatabaseMajorVersion();
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
        } else if (keyEvent.getSource() == checkTextPane) {
            columnData.setCheck(checkTextPane.getText());
        } else if (keyEvent.getSource() == descriptionTextPane) {
            columnData.setDescription(descriptionTextPane.getText());
        } else if (keyEvent.getSource() == fieldNameField) {
            columnData.setColumnName(fieldNameField.getText());
        }

    }

    void generateSQL() {
        StringBuffer sb = new StringBuffer();
        sb.setLength(0);
        if (editing) {
            if (columnData.isChanged()) {
                sb.append("ALTER DOMAIN ").append(domain).append("\n");
                if (columnData.isNameChanged()) {
                    sb.append("TO ").append(columnData.getColumnName()).append("\n");
                }
                if (columnData.isDefaultChanged()) {
                    if (MiscUtils.isNull(columnData.getDefaultValue()))
                        sb.append("DROP DEFAULT\n");
                    else {
                        sb.append("SET DEFAULT ");
                        if (columnData.getDefaultValue().toUpperCase().trim().equals("NULL")) {
                            sb.append("NULL");
                        } else {
                            sb.append(MiscUtils.formattedSQLValue(columnData.getDefaultValue(), columnData.getSQLType()));
                        }
                        sb.append("\n");

                    }
                }
                if (columnData.isRequiredChanged()) {
                    if (columnData.isRequired()) {
                        sb.append("SET ");
                    } else {
                        sb.append("DROP ");
                    }
                    sb.append("NOT NULL\n");

                }
                if (columnData.isCheckChanged()) {
                    sb.append("DROP CONSTRAINT\n");
                    if (!MiscUtils.isNull(columnData.getCheck())) {
                        sb.append("ADD CHECK (").append(columnData.getCheck()).append(")\n");
                    }
                }
                if (columnData.isTypeChanged()) {
                    sb.append("TYPE ").append(columnData.getFormattedDataType());
                }
                sb.append(";");
                if (columnData.isDescriptionChanged()) {
                    sb.append("\nCOMMENT ON DOMAIN ").append(columnData.getColumnName()).append(" IS ");
                    if (!MiscUtils.isNull(columnData.getDescription())) {

                        sb.append("'").append(columnData.getDescription()).append("'");
                    } else {
                        sb.append("NULL");
                    }
                    sb.append(";");
                }
                sqlTextPane.setText(sb.toString());
            }
        } else {
            sb.append("CREATE DOMAIN ").append(columnData.getColumnName()).append(" as ").append(columnData.getFormattedDataType()).append("\n");
            if (!MiscUtils.isNull(columnData.getDefaultValue())) {
                sb.append(" DEFAULT " + MiscUtils.formattedSQLValue(columnData.getDefaultValue(), columnData.getSQLType()));
            }
            sb.append(columnData.isRequired() ? " NOT NULL" : "");
            if (!MiscUtils.isNull(columnData.getCheck())) {
                sb.append(" CHECK ( " + columnData.getCheck() + ")");
            }
            sb.append(";");
            if (!MiscUtils.isNull(columnData.getDescription())) {
                sb.append("\nCOMMENT ON DOMAIN ").append(columnData.getColumnName()).append(" IS '")
                        .append(columnData.getDescription()).append("';");
            }
            sqlTextPane.setText(sb.toString());
        }
    }
}
