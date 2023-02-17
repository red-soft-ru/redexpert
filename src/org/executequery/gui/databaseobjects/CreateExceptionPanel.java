package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseException;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SimpleTextArea;

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
    public void setDatabaseObject(Object databaseObject) {
        this.exception = (DefaultDatabaseException) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {

    }

    protected void init() {
        centralPanel.setVisible(false);
        textExceptionPanel = new SimpleTextArea();
        tabbedPane.add(bundleStaticString("text"), textExceptionPanel);
        addCommentTab(null);
    }

    protected void initEdited() {
        reset();
    }

    void generateScript() {

        displayExecuteQueryDialog(generateQuery(), "^");
    }

    protected String generateQuery() {
        String query = "CREATE OR ALTER EXCEPTION " + getFormattedName() + " '" + textExceptionPanel.getTextAreaComponent().getText() + "'^";
        query += "COMMENT ON EXCEPTION " + getFormattedName() + " IS '" + simpleCommentPanel.getComment() + "'";
        return query;
    }

    protected void reset() {
        nameField.setText(exception.getName().trim());
        nameField.setEnabled(false);
        textExceptionPanel.getTextAreaComponent().setText(exception.getExceptionText());
        simpleCommentPanel.setDatabaseObject(exception);
    }

}
