package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.databaseobjects.impl.DefaultDatabaseUser;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ConnectionPanel;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.managment.WindowAddUser;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

public class CreateUserPanel extends AbstractCreateObjectPanel {
    public static final String CREATE_TITLE = getCreateTitle(NamedObject.USER);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.USER);
    DefaultDatabaseUser user;
    DefaultDatabaseUser beginUser;
    private JPanel mainPanel;
    private JTextField firstNameField;
    private JLabel tagLabel;
    private JLabel pluginLabel;
    private JComboBox pluginField;
    private JCheckBox adminBox;
    private JTextField lastNameField;
    private JTextField middleNameField;
    private JPasswordField passTextField;
    private JTable tagTable;
    private JScrollPane tagScroll;
    private JCheckBox activeBox;
    private JButton addTag;
    private JButton deleteTag;
    private SimpleSqlTextPanel sqlTextPanel;

    private JCheckBox showPassword;

    public CreateUserPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseUser databaseObject) {
        super(dc, dialog, databaseObject);
    }

    public CreateUserPanel(DatabaseConnection dc, ActionContainer dialog) {
        super(dc, dialog, null);
    }

    @Override
    protected void init() {
        centralPanel.setVisible(false);
        mainPanel = new JPanel();
        sqlTextPanel = new SimpleSqlTextPanel();
        passTextField = new JPasswordField();
        passTextField.setTransferHandler(null);
        passTextField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_SPACE)
                    e.consume();
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        showPassword = new JCheckBox(Bundles.get(ConnectionPanel.class, "ShowPassword"));

        showPassword.addItemListener(e -> passTextField.setEchoChar((e.getStateChange() == ItemEvent.SELECTED) ? (char) 0 : '•'));

        firstNameField = new JTextField();
        middleNameField = new JTextField();
        lastNameField = new JTextField();
        tagLabel = new JLabel();
        tagTable = new JTable();
        tagScroll = new JScrollPane();
        activeBox = new JCheckBox();
        addTag = new DefaultButton();
        deleteTag = new DefaultButton();
        pluginLabel = new JLabel();
        pluginField = new JComboBox(new String[]{"Srp", "Legacy_UserManager", "GostPassword_Manager"});
        pluginField.setEditable(true);
        adminBox = new JCheckBox();

        tagScroll.setViewportView(tagTable);

        addTag.setText(bundleString("addTag"));
        addTag.addActionListener(this::addTagActionPerformed);

        deleteTag.setText(bundleString("deleteTag"));
        deleteTag.addActionListener(this::deleteTagActionPerformed);


        pluginLabel.setText(bundleString("Plugin"));

        adminBox.setText(bundleString("Administrator"));

        tagLabel.setText(bundleString("Tags"));

        tagTable.setModel(new DefaultTableModel(new Object[][]{

        }, bundleStrings(new String[]{
                "tag", "value"
        })));

        activeBox.setText(bundleString("active"));
        activeBox.setSelected(true);

        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbConst = new GridBagConstraints(
                0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0);
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(gbConst).defaults();

        gbh.insertEmptyRow(mainPanel, 10);


        gbh.addLabelFieldPair(mainPanel, bundleString("Password"), passTextField, null, true, false);
        mainPanel.add(showPassword, gbh.nextRowFirstCol().nextCol().get());


        gbh.addLabelFieldPair(mainPanel, bundleString("FirstName"), firstNameField, null, true, false);
        gbh.addLabelFieldPair(mainPanel, bundleString("MiddleName"), middleNameField, null, true, false);
        gbh.addLabelFieldPair(mainPanel, bundleString("LastName"), lastNameField, null, true, false);
        gbh.addLabelFieldPair(mainPanel, pluginLabel, pluginField, null, true, false);
        mainPanel.add(activeBox, gbh.nextRowFirstCol().setLabelDefault().get());
        mainPanel.add(adminBox, gbh.nextCol().get());
        gbh.insertEmptyBigRow(mainPanel);
        gbh.setXY(2, 1).setLabelDefault().setHeight(2).setMinWeightY();
        mainPanel.add(addTag, gbh.nextRow().setMaxWeightX().get());
        mainPanel.add(deleteTag, gbh.nextCol().setMaxWeightX().get());
        mainPanel.add(tagScroll, gbh.setX(2).nextRowWidth().setWidth(2).setMaxWeightY().setMaxWeightX().fillBoth().setHeight(10).get());
        tabbedPane.add(bundleString("properties"), mainPanel);
        addCommentTab(null);
        tabbedPane.add("SQL", sqlTextPanel);
        tabbedPane.addChangeListener(changeEvent -> {
            if (tabbedPane.getSelectedComponent() == sqlTextPanel) {
                generateSQL();
            }
        });
    }

    private void addTagActionPerformed(java.awt.event.ActionEvent evt) {
        ((DefaultTableModel) tagTable.getModel()).addRow(new Object[]{"", ""});
    }

    private void deleteTagActionPerformed(java.awt.event.ActionEvent evt) {
        if (tagTable.getSelectedRow() >= 0) {
            ((DefaultTableModel) tagTable.getModel()).removeRow(tagTable.getSelectedRow());
        }
    }

    @Override
    protected void initEdited() {
        reset();
        tabbedPane.remove(sqlTextPanel);
        if (parent == null)
            addPrivilegesTab(tabbedPane, beginUser);
        addCreateSqlTab(beginUser);
    }

    protected void reset() {
        nameField.setText(beginUser.getName());
        nameField.setEnabled(false);
        firstNameField.setText(beginUser.getFirstName());
        middleNameField.setText(beginUser.getMiddleName());
        lastNameField.setText(beginUser.getLastName());
        simpleCommentPanel.setDatabaseObject(beginUser);
        activeBox.setSelected(beginUser.getActive());
        pluginField.setSelectedItem(beginUser.getPlugin());
        pluginField.setEnabled(false);
        adminBox.setSelected(beginUser.getAdministrator());
        for (String tag : beginUser.getTags().keySet()) {
            ((DefaultTableModel) tagTable.getModel()).addRow(new Object[]{tag, beginUser.getTag(tag)});
        }
    }

    protected String generateQuery() {

        user.setName(nameField.getText());
        user.setPassword(new String(passTextField.getPassword()));
        user.setFirstName(firstNameField.getText());
        user.setMiddleName(middleNameField.getText());
        user.setLastName(lastNameField.getText());
        user.setPlugin((String) pluginField.getSelectedItem());
        user.setComment(simpleCommentPanel.getComment());
        user.setActive(activeBox.isSelected());
        user.setAdministrator(adminBox.isSelected());
        user.setTags(new HashMap<>());

        for (int i = 0; i < tagTable.getRowCount(); i++) {

            String tag = tagTable.getModel().getValueAt(i, 0).toString();
            String value = tagTable.getModel().getValueAt(i, 1).toString();

            if (tag != null && !tag.isEmpty() && value != null && !value.isEmpty())
                user.setTag(tag, value);
        }

        return editing ? SQLUtils.generateAlterUser(beginUser, user, true) : SQLUtils.generateCreateUser(user, true);
    }

    protected void generateSQL() {
        if (!editing && passTextField.getPassword().length < 1) {
            GUIUtilities.displayErrorMessage(bundleString("error.empty-pwd"));
            passTextField.requestFocus();
            return;
        }
        sqlTextPanel.setSQLText(generateQuery());
    }

    @Override
    public void createObject() {
        if (tabbedPane.getSelectedComponent() != sqlTextPanel)
            generateSQL();
        displayExecuteQueryDialog(sqlTextPanel.getSQLText(), ";");
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
        this.user = (DefaultDatabaseUser) databaseObject;
        if (user == null) {
            ConnectionsTreePanel panel = ConnectionsTreePanel.getPanelFromBrowser();
            DatabaseHost databaseHost = (DatabaseHost) panel.getHostNode(connection).getDatabaseObject();
            DefaultDatabaseMetaTag metaTag = new DefaultDatabaseMetaTag(databaseHost, null, null, getTypeObject());
            user = new DefaultDatabaseUser(metaTag, "");
            user.setParent(metaTag);
        } else user.loadData();
        this.beginUser = user.getCopy();
    }

    @Override
    public void setParameters(Object[] params) {

    }

    public String bundleString(String key) {
        return Bundles.get(WindowAddUser.class, key);
    }

    private String bundleString(String key, Object... args) {
        return Bundles.get(WindowAddUser.class, key, args);
    }

    private String[] bundleStrings(String[] key) {
        for (int i = 0; i < key.length; i++)
            key[i] = bundleString(key[i]);
        return key;
    }
}
