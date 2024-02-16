package org.executequery.gui.databaseobjects;

import biz.redsoft.IFBUser;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.UserManagerPanel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

/**
 * This CreateUserPanel uses only for RDB v2.6
 * <br>for newest versions use <pre>CreateDatabaseUserPanel</pre>
 *
 * @see CreateDatabaseUserPanel
 */
public class CreateFbUserPanel extends AbstractCreateUserPanel {

    private final IFBUser fbUser;
    private final UserManagerPanel userManagerPanel;

    // --- GUI components ---

    private NumberTextField userIdField;
    private NumberTextField groupIdField;

    // ---

    public CreateFbUserPanel(DatabaseConnection dc, ActionContainer dialog, IFBUser fbUser, UserManagerPanel userManagerPanel, boolean editing) {
        super(dc, dialog);

        this.fbUser = fbUser;
        this.userManagerPanel = userManagerPanel;
        super.editing = editing;

        if (editing)
            initEdited();
    }

    @Override
    protected void init() {
        super.init();

        // --- number fields ---

        userIdField = WidgetFactory.createNumberTextField("userIdField");
        userIdField.setPreferredSize(new Dimension(userIdField.getWidth(), firstNameField.getPreferredSize().height));
        userIdField.setValue(0);

        groupIdField = WidgetFactory.createNumberTextField("groupIdField");
        groupIdField.setPreferredSize(new Dimension(groupIdField.getWidth(), firstNameField.getPreferredSize().height));
        groupIdField.setValue(0);

        // ---

        arrange();
    }

    @Override
    protected void initEdited() {
        reset();
    }

    @Override
    protected void arrange() {

        GridBagHelper gbh = new GridBagHelper()
                .setInsets(5, 5, 5, 0)
                .anchorNorthWest()
                .fillHorizontally();

        JPanel propertiesPanel = new JPanel(new GridBagLayout());
        propertiesPanel.add(new JLabel(bundleString("Password")), gbh.setMinWeightX().get());
        propertiesPanel.add(passTextField, gbh.setMaxWeightX().nextCol().spanX().get());
        propertiesPanel.add(isShowPasswordCheck, gbh.nextRow().get());
        propertiesPanel.add(new JLabel(bundleString("FirstName")), gbh.setMinWeightX().nextRowFirstCol().setWidth(1).get());
        propertiesPanel.add(firstNameField, gbh.setMaxWeightX().nextCol().spanX().get());
        propertiesPanel.add(new JLabel(bundleString("MiddleName")), gbh.setMinWeightX().nextRowFirstCol().setWidth(1).get());
        propertiesPanel.add(middleNameField, gbh.setMaxWeightX().nextCol().spanX().get());
        propertiesPanel.add(new JLabel(bundleString("LastName")), gbh.setMinWeightX().nextRowFirstCol().setWidth(1).get());
        propertiesPanel.add(lastNameField, gbh.setMaxWeightX().nextCol().spanX().get());
        propertiesPanel.add(new JLabel(bundleString("UserID")), gbh.setMinWeightX().nextRowFirstCol().setWidth(1).get());
        propertiesPanel.add(userIdField, gbh.setMaxWeightX().nextCol().spanX().get());
        propertiesPanel.add(new JLabel(bundleString("GroupID")), gbh.setMinWeightX().nextRowFirstCol().setWidth(1).spanY().get());
        propertiesPanel.add(groupIdField, gbh.setMaxWeightX().nextCol().spanX().get());

        // ---

        tabbedPane.add(bundleString("properties"), propertiesPanel);
    }

    @Override
    protected void reset() {
        nameField.setText(fbUser.getUserName());
        nameField.setEditable(false);
        firstNameField.setText(fbUser.getFirstName());
        middleNameField.setText(fbUser.getMiddleName());
        lastNameField.setText(fbUser.getLastName());
        userIdField.setValue(fbUser.getUserId());
        groupIdField.setValue(fbUser.getGroupId());
    }

    @Override
    public void createObject() {

        IFBUser user = getUpdateFbUser();
        if (user == null)
            return;

        if (editing)
            userManagerPanel.editFbUser(user);
        else
            userManagerPanel.addFbUser(user);

        closeDialog();
    }

    @Override
    public void closeDialog() {
        parent.finished();
    }

    private IFBUser getUpdateFbUser() {

        if (!editing && passTextField.getPassword().length < 1) {
            GUIUtilities.displayErrorMessage(bundleString("error.empty-pwd"));
            passTextField.requestFocus();
            return null;
        }

        fbUser.setUserName(nameField.getText().trim());
        fbUser.setFirstName(firstNameField.getText().trim());
        fbUser.setMiddleName(middleNameField.getText().trim());
        fbUser.setLastName(lastNameField.getText().trim());
        fbUser.setUserId(userIdField.getValue());
        fbUser.setGroupId(groupIdField.getValue());

        // by the default password field contains empty string
        // if it wasn't edited, password wouldn't be changed
        if (passTextField.getPassword().length > 0)
            fbUser.setPassword(new String(passTextField.getPassword()));

        return fbUser;
    }

}
