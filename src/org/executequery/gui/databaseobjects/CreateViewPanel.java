package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseView;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

public class CreateViewPanel extends AbstractCreateObjectPanel
        implements FocusListener, KeyListener {

    public static final String TITLE = getCreateTitle(NamedObject.VIEW);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.VIEW);

    private SimpleSqlTextPanel ddlTextPanel;
    private SimpleSqlTextPanel selectTextPanel;
    private DefaultDatabaseView view;

    private String notChangedText;
    private boolean released = true;

    public CreateViewPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateViewPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseView view) {
        super(dc, dialog, view);
    }

    @Override
    protected void initEdited() {
        nameField.setText(view.getName());
        simpleCommentPanel.setDatabaseObject(view);
    }

    @Override
    protected void init() {

        ddlTextPanel = new SimpleSqlTextPanel();
        ddlTextPanel.getTextPane().setDatabaseConnection(connection);

        selectTextPanel = new SimpleSqlTextPanel();
        selectTextPanel.getTextPane().setDatabaseConnection(connection);

        tabbedPane.add(Bundles.get("CreateViewPanel.selectTabLabel"), selectTextPanel);
        addCommentTab(null);
        tabbedPane.add("DDL", ddlTextPanel);

        tabbedPane.addChangeListener(e -> {

            if (tabbedPane.getSelectedComponent().equals(ddlTextPanel)) {
                ddlTextPanel.setSQLText(generateQuery());
                nameField.setEditable(false);
            } else
                nameField.setEditable(true);

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
                simpleCommentPanel.getComment(), getDatabaseVersion(), editing, getDatabaseConnection());
    }

    // --- KeyListener ---

    @Override
    public void keyPressed(KeyEvent e) {
        SQLTextArea textPane = (SQLTextArea) e.getSource();
        if (released) {
            notChangedText = textPane.getText();
            released = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        SQLTextArea textPane = (SQLTextArea) e.getSource();
        if (!textPane.getText().contains(" <view_name>\n") && !editing)
            textPane.setText(notChangedText);
        released = true;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // --- FocusListener ---

    @Override
    public void focusGained(FocusEvent focusEvent) {
        if (focusEvent.getSource() != ddlTextPanel)
            GUIUtils.requestFocusInWindow(ddlTextPanel);
    }

    @Override
    public void focusLost(FocusEvent focusEvent) {
    }

    // ---

    @Override
    public void setParameters(Object[] params) {
    }

    @Override
    protected void reset() {
    }

}
