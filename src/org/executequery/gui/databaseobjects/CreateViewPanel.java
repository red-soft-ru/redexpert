package org.executequery.gui.databaseobjects;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseView;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.editor.autocomplete.DefaultAutoCompletePopupProvider;
import org.executequery.gui.text.SQLTextPane;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.localization.Bundles;
import org.executequery.sql.SQLFormatter;
import org.underworldlabs.swing.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CreateViewPanel extends AbstractCreateObjectPanel implements FocusListener, KeyListener {
    public static final String TITLE = "Create View";
    public static final String EDIT_TITLE = "Alter View";
    private static final String AUTO_COMPLETE_POPUP_ACTION_KEY = "autoCompletePopupActionKey";
    private SimpleSqlTextPanel sqlTextPanel;
    private JButton formatSqlButton;
    private SimpleTextArea descriptionTextArea;
    private static final String replacing_name = "<view_name>";
    String notChangedText;
    private DefaultAutoCompletePopupProvider autoCompletePopup;
    private DefaultDatabaseView view;

    public CreateViewPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateViewPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseView view) {
        super(dc, dialog, view);
    }

    boolean released = true;

    @Override
    protected void initEdited() {
        nameField.setText(view.getName());
        descriptionTextArea.getTextAreaComponent().setText(view.getRemarks());
    }

    protected void init() {
        formatSqlButton = WidgetFactory.createButton(Bundles.getCommon("FormatSQL"));
        formatSqlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formatSql();
            }
        });
        sqlTextPanel = new SimpleSqlTextPanel();
        sqlTextPanel.addFocusListener(this);
        sqlTextPanel.getTextPane().addKeyListener(this);

        JPanel sqlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints sqlGbc = new GridBagConstraints();
        Insets sqlIns = new Insets(10, 5, 5, 5);
        sqlGbc.insets = sqlIns;
        sqlGbc.anchor = GridBagConstraints.NORTHWEST;
        sqlGbc.fill = GridBagConstraints.NONE;
        sqlGbc.gridx = 0;
        sqlGbc.gridy = 0;
        sqlPanel.add(formatSqlButton, sqlGbc);
        sqlGbc.gridy++;
        sqlGbc.fill = GridBagConstraints.BOTH;
        sqlGbc.weighty = 1;
        sqlGbc.weightx = 1;
        sqlPanel.add(sqlTextPanel, sqlGbc);

        descriptionTextArea = new SimpleTextArea();
        this.autoCompletePopup = new DefaultAutoCompletePopupProvider(connection, sqlTextPanel.getTextPane());
        String sql;
        if (editing) {
            sql = replaceName(view.getCreateSQLText());
        } else {
            sql = "create view <view_name>\n ( _fields_ )\n" +
                    "as\n" +
                    "select _fields_ from _table_name_\n" +
                    "where _conditions_";
            formatSqlButton.setVisible(false);
        }
        sqlTextPanel.setSQLText(sql);

        connectionsCombo.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            autoCompletePopup.connectionChanged((DatabaseConnection) connectionsCombo.getSelectedItem());
        });

        //create location elements
        tabbedPane.add(bundlesString("SQL"), sqlPanel);
        tabbedPane.add(bundlesString("description"), descriptionTextArea);


        Action autoCompletePopupAction = autoCompletePopup.getPopupAction();

        sqlTextPanel.getTextPane().getActionMap().put(AUTO_COMPLETE_POPUP_ACTION_KEY, autoCompletePopupAction);
        sqlTextPanel.getTextPane().getInputMap().put((KeyStroke)
                        autoCompletePopupAction.getValue(Action.ACCELERATOR_KEY),
                AUTO_COMPLETE_POPUP_ACTION_KEY);
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
    public void setParameters(Object[] params) {

    }

    @Override
    public void focusGained(FocusEvent focusEvent) {
        if (focusEvent.getSource() != sqlTextPanel)
            GUIUtils.requestFocusInWindow(sqlTextPanel);
    }

    @Override
    public void createObject() {
        String query = sqlTextPanel.getSQLText().trim().replace(replacing_name, getNameInQuotes());
        query = query + "^";
        query += "COMMENT ON VIEW " + getNameInQuotes() + " IS '" + descriptionTextArea.getTextAreaComponent().getText() + "'";
        displayExecuteQueryDialog(query, "^");
    }

    @Override
    public void focusLost(FocusEvent focusEvent) {

    }

    private String replaceName(String source) {
        source = source.trim();
        String name = view.getName().trim();
        source = source.replace(" " + name + "(", " " + replacing_name + "(");
        source = source.replace(" " + name + " ", " " + replacing_name + "\n");
        source = source.replace(" " + name + "\n", " " + replacing_name + "\n");
        source = source.replace("\n" + name + "\n", " " + replacing_name + "\n");
        source = source.replace("\n" + name + " ", " " + replacing_name + "\n");
        return source;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        SQLTextPane textPane = (SQLTextPane) e.getSource();
        if (released) {
            notChangedText = textPane.getText();
            released = false;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        SQLTextPane textPane = (SQLTextPane) e.getSource();
        if (!textPane.getText().contains(" " + replacing_name + "\n") && !editing)
            textPane.setText(notChangedText);
        released = true;
    }

    private void formatSql() {
        if (StringUtils.isNotEmpty(sqlTextPanel.getSQLText())) {
            String sqlText = sqlTextPanel.getSQLText();
            sqlTextPanel.setSQLText(new SQLFormatter(sqlText).format());
        }
    }
}
