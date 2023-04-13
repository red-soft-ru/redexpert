package org.executequery.gui.databaseobjects;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseView;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.executequery.sql.SQLFormatter;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CreateViewPanel extends AbstractCreateObjectPanel implements FocusListener, KeyListener {
    public static final String TITLE = getCreateTitle(NamedObject.VIEW);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.VIEW);

    private SimpleSqlTextPanel sqlTextPanel;
    private JButton formatSqlButton;
    private static final String replacing_name = "<view_name>";
    String notChangedText;
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
        simpleCommentPanel.setDatabaseObject(view);
    }

    protected void init() {
        formatSqlButton = WidgetFactory.createButton(Bundles.getCommon("FormatSQL"));
        formatSqlButton.addActionListener(e -> formatSql());

        sqlTextPanel = new SimpleSqlTextPanel();
        sqlTextPanel.getTextPane().setDatabaseConnection(connection);
//        sqlTextPanel.addFocusListener(this);
//        sqlTextPanel.getTextPane().addKeyListener(this);

        JPanel sqlPanel = new JPanel(new GridBagLayout());

        GridBagHelper gridBagHelper = new GridBagHelper();
        sqlPanel.add(formatSqlButton,
                gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest().fillNone().get());
        sqlPanel.add(sqlTextPanel,
                gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().get());

        connectionsCombo.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
        });

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changeName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changeName();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changeName();
            }
        });


        //create location elements
        tabbedPane.add(bundleStaticString("SQL"), sqlPanel);
        addCommentTab(null);

        simpleCommentPanel.getCommentField().getTextAreaComponent().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changeComment();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changeComment();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changeComment();
            }
        });

        sqlTextPanel.setSQLText(generateQuery());
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
    public void setParameters(Object[] params) {}

    @Override
    public void focusGained(FocusEvent focusEvent) {
        if (focusEvent.getSource() != sqlTextPanel)
            GUIUtils.requestFocusInWindow(sqlTextPanel);
    }

    protected String generateQuery() {

        String fields = null;
        String query = "";

        try {

            List<DatabaseColumn> columns = view.getColumns();
            if (columns != null) {
                fields = "";

                for (int i = 0; i < columns.size(); i++) {
                    fields += " " + MiscUtils.getFormattedObject(columns.get(i).getName());
                    if (i != columns.size() - 1)
                        fields += ",\n";
                }
            }

        } catch (Exception ignored) {}

        try {

            String selectStatement = "SELECT _fields_ FROM _table_ WHERE _conditions_";
            if (view != null)
                selectStatement = view.getSource();

            query = SQLUtils.generateCreateView(nameField.getText(), fields, selectStatement,
                    simpleCommentPanel.getComment(), getDatabaseVersion(), editing);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
            e.printStackTrace();
        }

        return query;
    }

    private void changeName() {

        String sqlText = sqlTextPanel.getSQLText().trim()
                .replaceAll("VIEW ((\".*\")|(\\w*\\b)|)", "VIEW " + format(nameField.getText().trim()));
        sqlTextPanel.setSQLText(sqlText);
    }

    private void changeComment() {

        String sqlText = sqlTextPanel.getSQLText().trim().replaceAll("\nCOMMENT ON VIEW \"?.*\"? IS '.*';", "") +
                SQLUtils.generateComment(nameField.getText().trim(), "VIEW",
                        simpleCommentPanel.getComment().trim(), ";", false);
        sqlTextPanel.setSQLText(sqlText);
    }

    @Override
    public void createObject() {
        displayExecuteQueryDialog(sqlTextPanel.getSQLText(), ";");
    }

    @Override
    public void focusLost(FocusEvent focusEvent) {}

    @Override
    public void keyTyped(KeyEvent e) {}

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

    protected void reset() {}

    private static String format(String object) {
        return MiscUtils.getFormattedObject(object);
    }

}
