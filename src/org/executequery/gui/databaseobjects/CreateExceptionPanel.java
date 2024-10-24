package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseException;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SimpleTextArea;

import java.util.Objects;

public class CreateExceptionPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.EXCEPTION);
    public static final String ALTER_TITLE = getEditTitle(NamedObject.EXCEPTION);
    private SimpleTextArea textExceptionPanel;
    private DefaultDatabaseException exception;

    public CreateExceptionPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseException exception) {
        super(dc, dialog, exception);
    }

    public CreateExceptionPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    @Override
    public void createObject() {
        generateScript();
    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return ALTER_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.EXCEPTION];
    }

    @Override
    public int getType() {
        return NamedObject.EXCEPTION;
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        this.exception = (DefaultDatabaseException) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {

    }

    @Override
    protected void init() {
        centralPanel.setVisible(false);
        textExceptionPanel = new SimpleTextArea();
        tabbedPane.add(bundleStaticString("text"), textExceptionPanel);
        addCommentTab(null);
    }

    @Override
    protected void initEdited() {
        reset();
        if (parent == null)
            addPrivilegesTab(tabbedPane, exception);
        addCreateSqlTab(exception);
    }

    void generateScript() {
        displayExecuteQueryDialog(generateQuery(), "^");
    }

    @Override
    protected String generateQuery() {

        String query = "CREATE OR ALTER EXCEPTION " + getFormattedName() + " '" + textExceptionPanel.getTextAreaComponent().getText() + "'^\n";

        String comment = simpleCommentPanel.getComment();
        if (!Objects.equals(comment, ""))
            query += "COMMENT ON EXCEPTION " + getFormattedName() + " IS '" + comment + "'\n";

        return query;
    }

    protected void reset() {
        nameField.setText(exception.getName().trim());
        nameField.setEditable(false);
        textExceptionPanel.getTextAreaComponent().setText(exception.getExceptionText());
        simpleCommentPanel.setDatabaseObject(exception);
    }

}
