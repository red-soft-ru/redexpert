package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public abstract class AbstractCreateUserPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.USER);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.USER);

    // --- GUI components ---

    protected JTextField lastNameField;
    protected JTextField firstNameField;
    protected JTextField middleNameField;

    protected JPasswordField passTextField;
    protected JCheckBox isShowPasswordCheck;

    // ---

    protected AbstractCreateUserPanel(DatabaseConnection dc, ActionContainer dialog) {
        super(dc, dialog, null);
    }

    protected AbstractCreateUserPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        super(dc, dialog, databaseObject);
    }

    protected abstract void arrange();

    @Override
    protected void init() {
        centralPanel.setVisible(false);

        // --- text fields ---

        firstNameField = WidgetFactory.createTextField("firstNameField");
        middleNameField = WidgetFactory.createTextField("middleNameField");
        lastNameField = WidgetFactory.createTextField("lastNameField");

        passTextField = WidgetFactory.createPasswordField("passTextField");
        passTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_SPACE)
                    e.consume();
            }
        });

        // --- check box ---

        isShowPasswordCheck = WidgetFactory.createCheckBox("isShowPasswordCheck", Bundles.get("ConnectionPanel.ShowPassword"));
        isShowPasswordCheck.addItemListener(e -> passTextField.setEchoChar((e.getStateChange() == ItemEvent.SELECTED) ? (char) 0 : '•'));

    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return EDIT_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.USER];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
    }

    @Override
    public void setParameters(Object[] params) {
    }

    @Override
    protected String generateQuery() {
        return null;
    }

    @Override
    public String bundleString(String key) {
        return Bundles.get("WindowAddUser." + key);
    }

}
