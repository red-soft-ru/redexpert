package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseView;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.util.List;

public class CreateViewPanel extends AbstractCreateObjectPanel {

    public static final String TITLE = getCreateTitle(NamedObject.VIEW);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.VIEW);

    private SimpleSqlTextPanel ddlTextPanel;
    private SimpleSqlTextPanel selectTextPanel;
    private DefaultDatabaseView view;
    private int oldSelectedTabIndex = 0;

    public CreateViewPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateViewPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseView view) {
        super(dc, dialog, view);
    }

    @Override
    protected void initEdited() {
        nameField.setText(view.getName());
        nameField.setEditable(false);
        simpleCommentPanel.setDatabaseObject(view);
    }

    @Override
    protected void init() {

        ddlTextPanel = new SimpleSqlTextPanel();
        ddlTextPanel.getTextPane().setDatabaseConnection(connection);

        selectTextPanel = new SimpleSqlTextPanel();
        selectTextPanel.getTextPane().setDatabaseConnection(connection);

        tabbedPane.add(bundleString("selectTabLabel"), selectTextPanel);
        addCommentTab(null);
        tabbedPane.add("DDL", ddlTextPanel);

        tabbedPane.addChangeListener(e -> {

            if (tabbedPane.getSelectedComponent().equals(ddlTextPanel)) {
                ddlTextPanel.setSQLText(generateQuery());

            } else if (oldSelectedTabIndex == 2) {
                String ddlText = ddlTextPanel.getSQLText();

                int result = GUIUtilities.displayConfirmDialog(bundleString("confirmTabChange"));
                if (result != JOptionPane.YES_OPTION) {
                    tabbedPane.setSelectedComponent(ddlTextPanel);
                    ddlTextPanel.setSQLText(ddlText);
                }
            }

            oldSelectedTabIndex = tabbedPane.getSelectedIndex();
        });

        selectTextPanel.setSQLText((view != null) ? view.getSource() : "SELECT _fields_ FROM _table_ WHERE _conditions_");
        ddlTextPanel.setSQLText(generateQuery());
        centralPanel.setVisible(false);
    }

    @Override
    public String getCreateTitle() {
        return TITLE;
    }

    @Override
    public String getEditTitle() {
        return EDIT_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.VIEW];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        view = (DefaultDatabaseView) databaseObject;
    }

    @Override
    public void createObject() {

        String query = tabbedPane.getSelectedComponent().equals(ddlTextPanel) ?
                ddlTextPanel.getSQLText() : generateQuery();

        displayExecuteQueryDialog(query, ";");
    }

    @Override
    protected String generateQuery() {

        StringBuilder fields = new StringBuilder();
        if (view != null) {

            List<DatabaseColumn> columns = view.getColumns();
            if (columns != null) {
                for (DatabaseColumn column : columns)
                    fields.append(" ").append(MiscUtils.getFormattedObject(column.getName(), getDatabaseConnection())).append(", ");
                fields.deleteCharAt(fields.lastIndexOf(","));
            }
        }

        return SQLUtils.generateCreateView(nameField.getText(), fields.toString(), selectTextPanel.getSQLText(),
                simpleCommentPanel.getComment(), getDatabaseVersion(), editing, false, getDatabaseConnection());
    }

    @Override
    public void setParameters(Object[] params) {
    }

    @Override
    protected void reset() {
    }

}
