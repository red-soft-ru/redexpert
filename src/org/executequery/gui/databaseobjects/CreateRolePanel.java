package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseRole;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.managment.WindowAddRole;
import org.executequery.localization.Bundles;
import org.underworldlabs.util.MiscUtils;

public class CreateRolePanel extends AbstractCreateObjectPanel {

    public static final String TITLE = Bundles.get(WindowAddRole.class, "CreateRole");
    DefaultDatabaseRole role;

    public CreateRolePanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        super(dc, dialog, databaseObject);
    }

    public CreateRolePanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject, Object[] params) {
        super(dc, dialog, databaseObject, params);
    }

    @Override
    protected void reset() {

    }

    @Override
    protected void init() {
        centralPanel.setVisible(false);
        addCommentTab(null);
    }

    @Override
    protected void initEdited() {
        tabbedPane.removeAll();
        nameField.setText(role.getName());
        nameField.setEnabled(false);
        addCreateSqlTab(role);
        if (parent == null) {
            addPrivilegesTab(tabbedPane);
            //privilegeListener.stateChanged(null);
        }
        addCommentTab(role);

    }

    @Override
    public void createObject() {
        displayExecuteQueryDialog(generateQuery(), ";");
    }

    @Override
    public String getCreateTitle() {
        return TITLE;
    }

    @Override
    public String getEditTitle() {
        return null;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.ROLE];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        role = (DefaultDatabaseRole) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {

    }

    @Override
    protected String generateQuery() {
        String query = "CREATE ROLE " + MiscUtils.getFormattedObject(nameField.getText()) + ";";
        if (!MiscUtils.isNull(simpleCommentPanel.getComment()))
            query += "COMMENT ON ROLE " + MiscUtils.getFormattedObject(nameField.getText()) + " IS '" + simpleCommentPanel.getComment() + "';";
        return query;
    }
}
