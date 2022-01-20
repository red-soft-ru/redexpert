package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabasePackage;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class CreatePackagePanel extends AbstractCreateObjectPanel implements KeyListener {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.PACKAGE);
    public static final String ALTER_TITLE = getEditTitle(NamedObject.PACKAGE);
    private static final String replacing_name = "<name_package>";
    private SimpleSqlTextPanel headerPanel;
    private SimpleSqlTextPanel bodyPanel;
    private SimpleTextArea descriptionPanel;
    private DefaultDatabasePackage databasePackage;

    public CreatePackagePanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreatePackagePanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabasePackage databaseObject) {
        super(dc, dialog, databaseObject);
    }

    String notChangedText;

    @Override
    protected void initEdited() {
        nameField.setText(databasePackage.getName().trim());
        headerPanel.setSQLText(replaceName(databasePackage.getHeaderSource()));
        bodyPanel.setSQLText(replaceName(databasePackage.getBodySource()));
        descriptionPanel.getTextAreaComponent().setText(databasePackage.getDescription());
    }

    protected String generateQuery() {
        String header = headerPanel.getSQLText().replace(replacing_name, getFormattedName());
        String body = bodyPanel.getSQLText().replace(replacing_name, getFormattedName());
        StringBuilder sb = new StringBuilder();
        sb.append(header).append("^\n");
        sb.append(body).append("^\n");
        sb.append("COMMENT ON PACKAGE " + getFormattedName() + " IS '" + descriptionPanel.getTextAreaComponent().getText() + "'");
        return sb.toString();
    }

    @Override
    public void createObject() {

        displayExecuteQueryDialog(generateQuery(), "^");

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
        databasePackage = (DefaultDatabasePackage) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {

    }

    private String replaceName(String source) {
        source = source.trim();
        source = source.replace(" " + nameField.getText() + " ", " " + replacing_name + "\n");
        source = source.replace(" " + nameField.getText() + "\n", " " + replacing_name + "\n");
        source = source.replace("\n" + nameField.getText() + "\n", " " + replacing_name + "\n");
        source = source.replace("\n" + nameField.getText() + " ", " " + replacing_name + "\n");
        return source;
    }

    @Override
    protected void init() {
        headerPanel = new SimpleSqlTextPanel();
        headerPanel.getTextPane().addKeyListener(this);
        bodyPanel = new SimpleSqlTextPanel();
        bodyPanel.getTextPane().addKeyListener(this);
        descriptionPanel = new SimpleTextArea();
        tabbedPane.add(bundleString("Header"), headerPanel);
        tabbedPane.add(bundleString("Body"), bodyPanel);
        tabbedPane.add(bundleString("Description"), descriptionPanel);
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

    boolean released = true;

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        SQLTextArea textPane = (SQLTextArea) keyEvent.getSource();
        if (released) {
            notChangedText = textPane.getText();
            released = false;
        }


    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        SQLTextArea textPane = (SQLTextArea) keyEvent.getSource();
        if (!textPane.getText().contains(" " + replacing_name + "\n"))
            textPane.setText(notChangedText);
        released = true;
    }
}
