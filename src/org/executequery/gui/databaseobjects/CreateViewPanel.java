package org.executequery.gui.databaseobjects;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
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
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

public class CreateViewPanel extends AbstractCreateObjectPanel
        implements FocusListener, KeyListener {

    public static final String TITLE = getCreateTitle(NamedObject.VIEW);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.VIEW);

    private SimpleSqlTextPanel sqlTextPanel;
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

        JButton formatSqlButton = WidgetFactory.createButton(Bundles.getCommon("FormatSQL"));
        formatSqlButton.addActionListener(e -> formatSql());

        sqlTextPanel = new SimpleSqlTextPanel();
        sqlTextPanel.getTextPane().setDatabaseConnection(connection);

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest();

        JPanel sqlPanel = new JPanel(new GridBagLayout());
        sqlPanel.add(formatSqlButton, gridBagHelper.fillNone().get());
        sqlPanel.add(sqlTextPanel, gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().get());

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
    public void createObject() {
        displayExecuteQueryDialog(sqlTextPanel.getSQLText(), ";");
    }

    @Override
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

        } catch (Exception ignored) {
        }

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

        String sqlText = sqlTextPanel.getSQLText().trim().replaceAll(
                "VIEW ((\".*\")|(\\w*\\b)|)",
                "VIEW " + MiscUtils.getFormattedObject(nameField.getText()));
        sqlTextPanel.setSQLText(sqlText);
    }

    private void changeComment() {

        String sqlText = sqlTextPanel.getSQLText().trim().replaceAll("COMMENT ON VIEW \"?.*\"? IS '.*';", "") +
                SQLUtils.generateComment(nameField.getText(), "VIEW", simpleCommentPanel.getComment().trim(), ";", false);
        sqlTextPanel.setSQLText(sqlText);
    }

    private void formatSql() {
        String sqlText = sqlTextPanel.getSQLText();
        if (StringUtils.isNotEmpty(sqlText))
            sqlTextPanel.setSQLText(SqlFormatter.of(Dialect.StandardSql).format(sqlText));
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
        if (focusEvent.getSource() != sqlTextPanel)
            GUIUtils.requestFocusInWindow(sqlTextPanel);
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
