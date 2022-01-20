package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.databaseobjects.impl.DefaultDatabaseUser;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.managment.WindowAddUser;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private JScrollPane tagScrol;
    private JCheckBox activeBox;
    private JButton addTag;
    private JButton deleteTag;
    private SimpleSqlTextPanel sqlTextPanel;
    private SimpleSqlTextPanel descriptionPanel;

    public CreateUserPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseUser databaseObject) {
        super(dc, dialog, databaseObject);
    }

    public CreateUserPanel(DatabaseConnection dc, ActionContainer dialog) {
        super(dc, dialog, null);
    }

    @Override
    protected void init() {
        mainPanel = new JPanel();
        sqlTextPanel = new SimpleSqlTextPanel();
        descriptionPanel = new SimpleSqlTextPanel();
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

        firstNameField = new JTextField();
        middleNameField = new JTextField();
        lastNameField = new JTextField();
        tagLabel = new JLabel();
        tagTable = new JTable();
        tagScrol = new JScrollPane();
        activeBox = new JCheckBox();
        addTag = new DefaultButton();
        deleteTag = new DefaultButton();
        pluginLabel = new JLabel();
        pluginField = new JComboBox(new String[]{"Srp", "Legacy_UserManager", "GostPassword_Manager"});
        pluginField.setEditable(true);
        adminBox = new JCheckBox();

        tagScrol.setViewportView(tagTable);

        addTag.setText(bundleString("addTag"));
        addTag.addActionListener(evt -> addTagActionPerformed(evt));

        deleteTag.setText(bundleString("deleteTag"));
        deleteTag.addActionListener(evt -> deleteTagActionPerformed(evt));


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

        //gbh.addLabelFieldPair(mainPanel, bundleString("UserName"), nameTextField, null, true, false);

        gbh.addLabelFieldPair(mainPanel, bundleString("Password"), passTextField, null, true, false);

        //gbh.addLabelFieldPair(mainPanel, bundleString("ConfirmPassword"), confirmField, null, true, false);
        gbh.addLabelFieldPair(mainPanel, bundleString("FirstName"), firstNameField, null, true, false);
        gbh.addLabelFieldPair(mainPanel, bundleString("MiddleName"), middleNameField, null, true, false);
        gbh.addLabelFieldPair(mainPanel, bundleString("LastName"), lastNameField, null, true, false);
        //gbh.addLabelFieldPair(mainPanel, userIDLabel, userIDField, null, true, false);
        //gbh.addLabelFieldPair(mainPanel, groupIDLabel, groupIDField, null, true, false);
        gbh.addLabelFieldPair(mainPanel, pluginLabel, pluginField, null, true, false);
        mainPanel.add(activeBox, gbh.nextRowFirstCol().setLabelDefault().get());
        mainPanel.add(adminBox, gbh.nextCol().get());
        gbh.insertEmptyBigRow(mainPanel);
        gbh.setXY(2, 1).setLabelDefault().setHeight(2).setMinWeightY();
        mainPanel.add(addTag, gbh.nextRow().setMaxWeightX().get());
        mainPanel.add(deleteTag, gbh.nextCol().setMaxWeightX().get());
        mainPanel.add(tagScrol, gbh.setX(2).nextRowWidth().setWidth(2).setMaxWeightY().setMaxWeightX().fillBoth().setHeight(10).get());
        tabbedPane.add(bundleString("properties"), mainPanel);
        tabbedPane.add(bundleString("Description"), descriptionPanel);
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
        nameField.setText(user.getName());
        nameField.setEnabled(false);
        firstNameField.setText(user.getFirstName());
        middleNameField.setText(user.getMiddleName());
        lastNameField.setText(user.getLastName());
        //groupIDField.setText(Integer.toString(user.getGroupId()));
        //userIDField.setText(Integer.toString(user.getUserId()));
        descriptionPanel.setSQLText(user.getComment());
        activeBox.setSelected(user.getActive());
        pluginField.setSelectedItem(user.getPlugin());
        pluginField.setEnabled(false);
        adminBox.setSelected(user.getAdministrator());
        for (String tag : user.getTags().keySet()) {
            ((DefaultTableModel) tagTable.getModel()).addRow(new Object[]{tag, user.getTag(tag)});
        }
    }

    protected String generateQuery() {

        user.setName(nameField.getText());
        user.setPassword(new String(passTextField.getPassword()));
        user.setFirstName(firstNameField.getText());
        user.setMiddleName(middleNameField.getText());
        user.setLastName(lastNameField.getText());
        user.setPlugin((String) pluginField.getSelectedItem());
        user.setComment(descriptionPanel.getSQLText());
        user.setActive(activeBox.isSelected());
        user.setAdministrator(adminBox.isSelected());
        user.setTags(new HashMap<>());
        for (int i = 0; i < tagTable.getRowCount(); i++) {
            String tag = tagTable.getModel().getValueAt(i, 0).toString();
            String value = tagTable.getModel().getValueAt(i, 1).toString();
            if (tag != null && !tag.isEmpty() && value != null && !value.isEmpty()) {
                user.setTag(tag, value);
            }
        }
        StringBuilder sb = new StringBuilder();
        if (editing) {
            sb.append("ALTER");
            sb.append(" USER ").append(MiscUtils.getFormattedObject(user.getName()));
            if (!Objects.equals(user.getFirstName(), beginUser.getFirstName()))
                sb.append("\nFIRSTNAME '").append(user.getFirstName()).append("'");
            if (!Objects.equals(user.getMiddleName(), beginUser.getMiddleName()))
                sb.append("\nMIDDLENAME '").append(user.getMiddleName()).append("'");
            if (!Objects.equals(user.getLastName(), beginUser.getLastName()))
                sb.append("\nLASTNAME '").append(user.getLastName()).append("'");
            if (!MiscUtils.isNull(user.getPassword())) {
                sb.append("\nPASSWORD '").append(user.getPassword()).append("'");
            }
            if (user.getActive() != beginUser.getActive()) {
                if (user.getActive()) {
                    sb.append("\nACTIVE");
                } else {
                    sb.append("\nINACTIVE");
                }
            }
            if (user.getAdministrator() != beginUser.getAdministrator())
                if (user.getAdministrator()) {
                    sb.append("\nGRANT ADMIN ROLE");
                } else {
                    sb.append("\nREVOKE ADMIN ROLE");
                }
            if (!user.getPlugin().equals(""))
                sb.append("\nUSING PLUGIN ").append(user.getPlugin());
            Map<String, String> tags = user.getTags();
            Map<String, String> tags1 = beginUser.getTags();
            if (!tags.equals(tags1)) {
                sb.append("\nTAGS (");
                for (String tag : tags1.keySet()) {
                    if (!tags.containsKey(tag)) {
                        sb.append("DROP ").append(tag).append(" , ");
                    }
                }
                boolean first = true;
                for (String tag : tags.keySet()) {
                    if (!first)
                        sb.append(", ");
                    first = false;
                    sb.append(tag).append(" = '").append(tags.get(tag)).append("'");
                }
                sb.append(" )");
            }
            sb.append(";\n");
            if (!Objects.equals(user.getComment(), beginUser.getComment()) && !(beginUser.getComment() == null && MiscUtils.isNull(user.getComment())))
                sb.append("COMMENT ON USER ").append(MiscUtils.getFormattedObject(user.getName())).append(" is '").append(user.getComment()).append("'");
        } else sb.append(SQLUtils.generateCreateUser(user));
        return sb.toString();
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
