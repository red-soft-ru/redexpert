package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.datatype.SelectTypePanel;
import org.executequery.gui.text.SQLTextPane;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CreateDomainPanel extends AbstractCreateObjectPanel implements KeyListener {
    public static final String CREATE_TITLE = "Create Domain";
    public static final String EDIT_TITLE = "Edit Domain";
    private ColumnData columnData;
    private String domain;
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
    private JPanel sqlPanel;
    private SelectTypePanel selectTypePanel;
    private JCheckBox notNullBox;

    public CreateDomainPanel(DatabaseConnection connection, ActionContainer parent, String domain) {
        super(connection, parent, domain);
    }

    public CreateDomainPanel(DatabaseConnection connection, ActionContainer parent) {
        this(connection, parent, null);
    }

    protected void init() {
        defaultValuePanel = new JPanel();
        checkPanel = new JPanel();
        descriptionPanel = new JPanel();
        sqlPanel = new JPanel();
        selectTypePanel = new SelectTypePanel(metaData.getDataTypesArray(), metaData.getIntDataTypesArray(), columnData, false);
        notNullBox = new JCheckBox("Not Null");
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

        notNullBox.addActionListener(actionEvent -> columnData.setNotNull(notNullBox.isSelected()));
        columnData.setNotNull(notNullBox.isSelected());
        tabbedPane.addChangeListener(changeEvent -> {
            selectTypePanel.refreshColumn();
            if (tabbedPane.getSelectedComponent() == sqlPanel) {
                generateSQL();
            }
        });
        defaultValueTextPane.addKeyListener(this);
        checkTextPane.addKeyListener(this);
        descriptionTextPane.addKeyListener(this);

        centralPanel.setLayout(new GridBagLayout());

        centralPanel.add(notNullBox, new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        defaultValuePanel.setLayout(new GridBagLayout());
        checkPanel.setLayout(new GridBagLayout());
        descriptionPanel.setLayout(new GridBagLayout());
        sqlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcFull = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        defaultValuePanel.add(scrollDefaultValue, gbcFull);
        checkPanel.add(scrollCheck, gbcFull);
        descriptionPanel.add(scrollDescription, gbcFull);
        sqlPanel.add(scrollSQL, gbcFull);
        tabbedPane.add(bundlesString("type"), selectTypePanel);
        tabbedPane.add(bundlesString("default-value"), defaultValuePanel);
        tabbedPane.add(bundlesString("check"), checkPanel);
        tabbedPane.add(bundlesString("description"), descriptionPanel);
        tabbedPane.add(bundlesString("SQL"), sqlPanel);
    }

    protected void initEdited() {
        columnData.setColumnName(domain);
        columnData.setDomain(domain);
        columnData.setDescription(columnData.getDomainDescription());
        columnData.setCheck(columnData.getDomainCheck());
        columnData.setNotNull(columnData.isDomainNotNull());
        columnData.setDefaultValue(columnData.getDomainDefault());
        descriptionTextPane.setText(columnData.getDescription());
        checkTextPane.setText(columnData.getCheck());
        defaultValueTextPane.setText(columnData.getDefaultValue());
        nameField.setText(columnData.getColumnName());
        notNullBox.setSelected(columnData.isRequired());
        if (getDatabaseVersion() < 3)
            notNullBox.setEnabled(false);
        selectTypePanel.refresh();
        columnData.makeCopy();
    }

    @Override
    public void createObject() {
        if (tabbedPane.getSelectedComponent() != sqlPanel)
            generateSQL();
        displayExecuteQueryDialog(sqlTextPane.getText(), ";");
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
        return NamedObject.META_TYPES[NamedObject.DOMAIN];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        this.domain = (String) databaseObject;
        columnData = new ColumnData(connection);
    }

    @Override
    public void setParameters(Object[] params) {

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
        } else if (keyEvent.getSource() == nameField) {
            columnData.setColumnName(nameField.getText());
        }

    }

    private void generateSQL() {
        StringBuilder sb = new StringBuilder();
        columnData.setColumnName(nameField.getText());
        sb.setLength(0);
        if (editing) {
            if (columnData.isChanged()) {
                sb.append("ALTER DOMAIN ").append(MiscUtils.getFormattedObject(domain)).append("\n");
                if (columnData.isNameChanged()) {
                    sb.append("TO ").append(columnData.getFormattedColumnName()).append("\n");
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
                    sb.append("\nCOMMENT ON DOMAIN ").append(columnData.getFormattedColumnName()).append(" IS ");
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
            sb.append("CREATE DOMAIN ").append(columnData.getFormattedColumnName()).append(" as ").append(columnData.getFormattedDataType()).append("\n");
            if (!MiscUtils.isNull(columnData.getDefaultValue())) {
                sb.append(" DEFAULT ").append(MiscUtils.formattedSQLValue(columnData.getDefaultValue(), columnData.getSQLType()));
            }
            sb.append(columnData.isRequired() ? " NOT NULL" : "");
            if (!MiscUtils.isNull(columnData.getCheck())) {
                sb.append(" CHECK ( ").append(columnData.getCheck()).append(")");
            }
            sb.append(";");
            if (!MiscUtils.isNull(columnData.getDescription())) {
                sb.append("\nCOMMENT ON DOMAIN ").append(columnData.getFormattedColumnName()).append(" IS '")
                        .append(columnData.getDescription()).append("';");
            }
            sqlTextPane.setText(sb.toString());
        }
    }
}
