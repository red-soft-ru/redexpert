package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;

public class CreatePackagePanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = "Create package";
    public static final String ALTER_TITLE = "Alter package";
    private static final String replacing_name = "<name_package>";
    private SimpleSqlTextPanel headerPanel;
    private SimpleSqlTextPanel bodyPanel;
    private SimpleTextArea descriptionPanel;

    public CreatePackagePanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreatePackagePanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        super(dc, dialog, databaseObject);
    }

    @Override
    protected void init() {
        headerPanel = new SimpleSqlTextPanel();
        bodyPanel = new SimpleSqlTextPanel();
        descriptionPanel = new SimpleTextArea();
        tabbedPane.add("Header", headerPanel);
        tabbedPane.add("Body", bodyPanel);
        tabbedPane.add("Description", descriptionPanel);
        String headerText = "create or alter package " + replacing_name + "\n" +
                "as\n" +
                "begin\n" +
                " \n" +
                "end";
        headerPanel.setSQLText(headerText);
        String bodyText = "recreate package body " + replacing_name + "\n" +
                "as\n" +
                "begin\n" +
                " \n" +
                "end";
        bodyPanel.setSQLText(bodyText);

    }

    @Override
    protected void initEdited() {

    }

    @Override
    public void createObject() {
        headerPanel.setSQLText(headerPanel.getSQLText().replace(replacing_name, nameField.getText()));
        bodyPanel.setSQLText(bodyPanel.getSQLText().replace(replacing_name, nameField.getText()));
        StringBuilder sb = new StringBuilder();
        sb.append(headerPanel.getSQLText()).append("^\n");
        sb.append(bodyPanel.getSQLText()).append("^\n");
        sb.append("COMMENT ON PACKAGE " + nameField.getText() + " IS '" + descriptionPanel.getTextAreaComponent().getText() + "'");
        displayExecuteQueryDialog(sb.toString(), "^");

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
        return NamedObject.META_TYPES[NamedObject.PACKAGE];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {

    }

    @Override
    public void setParameters(Object[] params) {

    }
}
