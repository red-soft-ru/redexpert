package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.datatype.SelectTypePanel;
import org.executequery.gui.text.SQLTextArea;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class CreateDomainPanel extends AbstractCreateObjectPanel implements DocumentListener {
    public static final String CREATE_TITLE = getCreateTitle(NamedObject.DOMAIN);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.DOMAIN);
    private ColumnData columnData;
    private String domain;
    private JScrollPane scrollDefaultValue;
    private JScrollPane scrollCheck;
    private JScrollPane scrollSQL;
    private SQLTextArea defaultValueTextPane;
    private SQLTextArea checkTextPane;
    private SQLTextArea sqlTextPane;
    private JPanel defaultValuePanel;
    private JPanel checkPanel;
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
        sqlPanel = new JPanel();
        selectTypePanel = new SelectTypePanel(connection.getDataTypesArray(), connection.getIntDataTypesArray(), columnData, false);
        notNullBox = new JCheckBox("Not Null");
        scrollDefaultValue = new JScrollPane();
        scrollCheck = new JScrollPane();
        scrollSQL = new JScrollPane();
        defaultValueTextPane = new SQLTextArea();
        checkTextPane = new SQLTextArea();
        sqlTextPane = new SQLTextArea();

        scrollDefaultValue.setViewportView(defaultValueTextPane);

        scrollCheck.setViewportView(checkTextPane);

        scrollSQL.setViewportView(sqlTextPane);

        notNullBox.addActionListener(actionEvent -> columnData.setNotNull(notNullBox.isSelected()));
        columnData.setNotNull(notNullBox.isSelected());
        tabbedPane.addChangeListener(changeEvent -> {
            selectTypePanel.refreshColumn();
            if (tabbedPane.getSelectedComponent() == sqlPanel) {
                generateSQL();
            }
        });
        defaultValueTextPane.getDocument().addDocumentListener(this);
        checkTextPane.getDocument().addDocumentListener(this);
        topPanel.add(notNullBox, topGbh.nextRowFirstCol().setLabelDefault().get());
        defaultValuePanel.setLayout(new GridBagLayout());
        checkPanel.setLayout(new GridBagLayout());
        sqlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcFull = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        defaultValuePanel.add(scrollDefaultValue, gbcFull);
        checkPanel.add(scrollCheck, gbcFull);
        sqlPanel.add(scrollSQL, gbcFull);
        tabbedPane.add(bundleStaticString("type"), selectTypePanel);
        tabbedPane.add(bundleStaticString("default-value"), defaultValuePanel);
        tabbedPane.add(bundleStaticString("check"), checkPanel);
        addCommentTab(null);
        simpleCommentPanel.getCommentField().getTextAreaComponent().getDocument().addDocumentListener(this);
        tabbedPane.add(bundleStaticString("SQL"), sqlPanel);
        centralPanel.setVisible(false);
    }

    protected void initEdited() {
        reset();
        if (parent == null) {
            addDependenciesTab((DatabaseObject) ConnectionsTreePanel.getNamedObjectFromHost(connection, NamedObject.DOMAIN, domain));
            addCreateSqlTab((DatabaseObject) ConnectionsTreePanel.getNamedObjectFromHost(connection, NamedObject.DOMAIN, domain));
        }
    }

    protected void reset() {
        columnData.setColumnName(domain);
        columnData.setDomain(domain);
        columnData.setDescription(columnData.getDomainDescription());
        columnData.setCheck(columnData.getDomainCheck());
        columnData.setNotNull(columnData.isDomainNotNull());
        columnData.setDefaultValue(columnData.getDomainDefault());
        simpleCommentPanel.setDatabaseObject((DatabaseObject) ConnectionsTreePanel.getNamedObjectFromHost(connection, getTypeObject(), domain));
        checkTextPane.setText(columnData.getCheck());
        defaultValueTextPane.setText(columnData.getDefaultValue().getValue());
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

    private void generateSQL() {
        sqlTextPane.setText(generateQuery());
    }

    protected String generateQuery() {
        columnData.setColumnName(nameField.getText());
        if (editing)
            return SQLUtils.generateAlterDomain(columnData, domain);
        else
            return SQLUtils.generateCreateDomain(columnData, columnData.getFormattedColumnName(), false, true);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changed(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changed(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        changed(e);
    }

    private void changed(DocumentEvent e) {
        if (e.getDocument() == defaultValueTextPane.getDocument()) {
            columnData.setDefaultValue(defaultValueTextPane.getText());
        } else if (e.getDocument() == checkTextPane.getDocument()) {
            columnData.setCheck(checkTextPane.getText());
        } else if (e.getDocument() == simpleCommentPanel.getCommentField().getTextAreaComponent().getDocument()) {
            columnData.setDescription(simpleCommentPanel.getComment());
        } else if (e.getDocument() == nameField.getDocument()) {
            columnData.setColumnName(nameField.getText());
        }
    }
}
