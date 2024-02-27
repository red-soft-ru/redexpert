package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.databaseobjects.impl.DefaultDatabaseUser;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;

public class CreateDatabaseUserPanel extends AbstractCreateUserPanel {

    private static final String[] PLUGINS = new String[]{
            "Srp",
            "Legacy_UserManager",
            "GostPassword_Manager"
    };

    private DefaultDatabaseUser user;
    private DefaultDatabaseUser beginUser;

    // --- GUI components ---

    protected JTable tagTable;
    protected JButton addTagButton;
    protected JButton deleteTagButton;

    protected JCheckBox isAdminCheck;
    protected JCheckBox isActiveCheck;

    protected JComboBox<?> pluginCombo;
    protected SimpleSqlTextPanel sqlTextPanel;

    // ---

    public CreateDatabaseUserPanel(DatabaseConnection dc, ActionContainer dialog) {
        super(dc, dialog, null);
    }

    public CreateDatabaseUserPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseUser databaseObject) {
        super(dc, dialog, databaseObject);
    }

    @Override
    protected void init() {
        super.init();

        sqlTextPanel = new SimpleSqlTextPanel();

        // --- buttons ---

        addTagButton = WidgetFactory.createButton("addTagButton", bundleString("addTag"));
        addTagButton.setMinimumSize(addTagButton.getPreferredSize());
        addTagButton.addActionListener(e -> addTag());

        deleteTagButton = WidgetFactory.createButton("deleteTagButton", bundleString("deleteTag"));
        deleteTagButton.setMinimumSize(deleteTagButton.getPreferredSize());
        deleteTagButton.addActionListener(e -> deleteTag());

        // --- check boxes ---

        isActiveCheck = WidgetFactory.createCheckBox("isActiveCheck", bundleString("active"));
        isActiveCheck.setSelected(true);

        isAdminCheck = WidgetFactory.createCheckBox("isAdminCheck", bundleString("Administrator"));

        // --- combo boxes  ---

        pluginCombo = WidgetFactory.createComboBox("pluginCombo", PLUGINS);
        pluginCombo.setEditable(true);

        // --- tag table ---

        tagTable = WidgetFactory.createTable("tagTable", new String[]{
                bundleString("tag"),
                bundleString("value")
        });

        // ---

        arrange();
    }

    @Override
    protected void initEdited() {
        reset();
        tabbedPane.remove(sqlTextPanel);
        if (parent == null)
            addPrivilegesTab(tabbedPane, beginUser);
        addCreateSqlTab(beginUser);
    }

    @Override
    protected void arrange() {

        GridBagHelper gbh = new GridBagHelper()
                .setInsets(0, 0, 5, 5)
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
        propertiesPanel.add(new JLabel(bundleString("Plugin")), gbh.setMinWeightX().nextRowFirstCol().setWidth(1).get());
        propertiesPanel.add(pluginCombo, gbh.setMaxWeightX().nextCol().spanX().get());
        propertiesPanel.add(isActiveCheck, gbh.setMinWeightX().nextRowFirstCol().setWidth(2).spanY().get());
        propertiesPanel.add(isAdminCheck, gbh.nextCol().get());

        gbh = new GridBagHelper()
                .setInsets(0, 0, 0, 5)
                .anchorNorthWest()
                .fillBoth();

        JPanel tagPanel = new JPanel(new GridBagLayout());
        tagPanel.add(new JScrollPane(tagTable), gbh.setMaxWeightY().spanX().get());
        tagPanel.add(new JPanel(), gbh.nextRowFirstCol().setWidth(1).setMinWeightY().setMaxWeightX().fillHorizontally().get());
        tagPanel.add(addTagButton, gbh.nextCol().setMinWeightX().fillNone().get());
        tagPanel.add(deleteTagButton, gbh.nextCol().leftGap(5).get());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(propertiesPanel, new GridBagHelper().fillBoth().setInsets(5, 5, 5, 5).spanY().get());
        mainPanel.add(tagPanel, new GridBagHelper().nextCol().setInsets(0, 5, 5, 5).fillBoth().spanX().spanY().get());

        // ---

        tabbedPane.add(bundleString("properties"), mainPanel);
        addCommentTab(null);
        tabbedPane.add("SQL", sqlTextPanel);
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == sqlTextPanel)
                generateSQL();
        });
    }

    @Override
    protected void reset() {

        nameField.setText(beginUser.getName());
        nameField.setEditable(false);
        firstNameField.setText(beginUser.getFirstName());
        middleNameField.setText(beginUser.getMiddleName());
        lastNameField.setText(beginUser.getLastName());
        simpleCommentPanel.setDatabaseObject(beginUser);
        isActiveCheck.setSelected(beginUser.getActive());
        pluginCombo.setSelectedItem(beginUser.getPlugin());
        pluginCombo.setEnabled(false);
        isAdminCheck.setSelected(beginUser.getAdministrator());

        for (String tag : beginUser.getTags().keySet())
            ((DefaultTableModel) tagTable.getModel()).addRow(new Object[]{tag, beginUser.getTag(tag)});
    }

    @Override
    protected String generateQuery() {

        user.setName(nameField.getText());
        user.setPassword(new String(passTextField.getPassword()));
        user.setFirstName(firstNameField.getText());
        user.setMiddleName(middleNameField.getText());
        user.setLastName(lastNameField.getText());
        user.setPlugin((String) pluginCombo.getSelectedItem());
        user.setRemarks(simpleCommentPanel.getComment());
        user.setActive(isActiveCheck.isSelected());
        user.setAdministrator(isAdminCheck.isSelected());
        user.setTags(new HashMap<>());

        for (int i = 0; i < tagTable.getRowCount(); i++) {
            String tag = tagTable.getModel().getValueAt(i, 0).toString();
            String value = tagTable.getModel().getValueAt(i, 1).toString();

            if (isValid(tag) && isValid(value))
                user.setTag(tag, value);
        }

        return editing ?
                SQLUtils.generateAlterUser(beginUser, user, true) :
                SQLUtils.generateCreateUser(user, true);
    }

    @Override
    public void createObject() {
        if (tabbedPane.getSelectedComponent() != sqlTextPanel) {
            if (generateSQL())
                displayExecuteQueryDialog(sqlTextPanel.getSQLText(), ";");
        } else
            displayExecuteQueryDialog(sqlTextPanel.getSQLText(), ";");
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {

        this.user = (DefaultDatabaseUser) databaseObject;
        if (user == null) {

            ConnectionsTreePanel panel = ConnectionsTreePanel.getPanelFromBrowser();
            DatabaseHost databaseHost = (DatabaseHost) panel.getHostNode(connection).getDatabaseObject();
            DefaultDatabaseMetaTag metaTag = new DefaultDatabaseMetaTag(databaseHost, null, null, getTypeObject());

            user = new DefaultDatabaseUser(metaTag, "", "");
            user.setParent(metaTag);

        } else
            user.loadData();

        this.beginUser = user.copy();
    }

    private boolean generateSQL() {

        if (!editing && passTextField.getPassword().length < 1) {
            GUIUtilities.displayErrorMessage(bundleString("error.empty-pwd"));
            passTextField.requestFocus();
            return false;
        }

        sqlTextPanel.setSQLText(generateQuery());
        return true;
    }

    private void addTag() {
        ((DefaultTableModel) tagTable.getModel()).addRow(new Object[]{"", ""});
    }

    private void deleteTag() {
        if (tagTable.getSelectedRow() >= 0)
            ((DefaultTableModel) tagTable.getModel()).removeRow(tagTable.getSelectedRow());
    }

    private boolean isValid(String value) {
        return value != null && !value.isEmpty();
    }

}
