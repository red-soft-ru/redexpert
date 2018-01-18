package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseView;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.editor.autocomplete.DefaultAutoCompletePopupProvider;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.swing.GUIUtils;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class CreateViewPanel extends AbstractCreateObjectPanel implements FocusListener {
    public static final String TITLE = "Create View";
    public static final String EDIT_TITLE = "Alter View";
    private static final String AUTO_COMPLETE_POPUP_ACTION_KEY = "autoCompletePopupActionKey";
    SimpleSqlTextPanel sqlTextPanel;
    SimpleTextArea descriptionTextArea;
    DefaultAutoCompletePopupProvider autoCompletePopup;
    DefaultDatabaseView view;

    public CreateViewPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateViewPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseView view) {
        super(dc, dialog, view);
    }

    protected void init() {
        sqlTextPanel = new SimpleSqlTextPanel();
        sqlTextPanel.addFocusListener(this);
        descriptionTextArea = new SimpleTextArea();
        this.autoCompletePopup = new DefaultAutoCompletePopupProvider(connection, sqlTextPanel.getTextPane());
        String sql;
        if (editing) {
            sql = view.getCreateSQLText();
        } else {
            sql = "create view <view_name> ( _fields_ )\n" +
                    "as\n" +
                    "select _fields_ from _table_name_\n" +
                    "where _conditions_";
        }
        sqlTextPanel.setSQLText(sql);

        connectionsCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                autoCompletePopup.connectionChanged((DatabaseConnection) connectionsCombo.getSelectedItem());
            }
        });

        //create location elements
        tabbedPane.add(bundlesString("SQL"), sqlTextPanel);
        tabbedPane.add(bundlesString("description"), descriptionTextArea);


        Action autoCompletePopupAction = autoCompletePopup.getPopupAction();

        sqlTextPanel.getTextPane().getActionMap().put(AUTO_COMPLETE_POPUP_ACTION_KEY, autoCompletePopupAction);
        sqlTextPanel.getTextPane().getInputMap().put((KeyStroke)
                        autoCompletePopupAction.getValue(Action.ACCELERATOR_KEY),
                AUTO_COMPLETE_POPUP_ACTION_KEY);
    }

    @Override
    protected void init_edited() {
        nameField.setText(view.getName());
        descriptionTextArea.getTextAreaComponent().setText(view.getRemarks());
    }

    @Override
    public void create_object() {
        String searchS = " view ";
        String query = sqlTextPanel.getSQLText().trim();
        int indexStart = query.indexOf(searchS) + searchS.length();
        int indexFinish = query.indexOf(" ", indexStart);
        query = query.substring(0, indexStart) + nameField.getText() + query.substring(indexFinish) + "^";
        query += "COMMENT ON VIEW " + nameField.getText() + " IS '" + descriptionTextArea.getTextAreaComponent().getText() + "'";
        displayExecuteQueryDialog(query, "^");
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
    public void focusGained(FocusEvent focusEvent) {
        if (focusEvent.getSource() != sqlTextPanel)
            GUIUtils.requestFocusInWindow(sqlTextPanel);
    }

    @Override
    public void focusLost(FocusEvent focusEvent) {

    }
}
