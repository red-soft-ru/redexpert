package org.executequery.gui.browser.managment;

import biz.redsoft.IFBUser;
import org.executequery.GUIUtilities;
import org.executequery.gui.browser.UserManagerPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikhan808 on 20.02.2017.
 */
public class WindowAddUser extends JPanel {

    UserManagerPanel ump;
    int version;
    IFBUser user;
    boolean edit;
    private JButton cancelButton;
    private JPasswordField confirmField;
    private JTextArea descriptionField;
    private JTextField firstNameField;
    private NumberTextField groupIDField;
    private JLabel userIDLabel;
    private JLabel groupIDLabel;
    private JLabel descriptionLabel;
    private JLabel tagLabel;
    private JLabel pluginLabel;
    private JTextField pluginField;
    private JCheckBox adminBox;
    private JScrollPane descriptionScroll;
    private JTextField lastNameField;
    private JTextField middleNameField;
    private JTextField nameTextField;
    private JButton okButton;
    private JPasswordField passTextField;
    private NumberTextField userIDField;
    private JTable tagTable;
    private JScrollPane tagScrol;
    private JCheckBox activeBox;

    private JButton addTag;
    private JButton deleteTag;

    /**
     * Creates new form WindowAddUser
     *
     * @param u
     */
    public WindowAddUser(UserManagerPanel u, int version) {
        this.version = version;
        initComponents();
        ump = u;
        edit = false;
    }

    public WindowAddUser(UserManagerPanel u, IFBUser user, int version) {
        this.version = version;
        initComponents();
        ump = u;
        edit = true;
        this.user = user;
        nameTextField.setText(user.getUserName());
        nameTextField.setEnabled(false);
        firstNameField.setText(user.getFirstName());
        middleNameField.setText(user.getMiddleName());
        lastNameField.setText(user.getLastName());
        groupIDField.setText(Integer.toString(user.getGroupId()));
        userIDField.setText(Integer.toString(user.getUserId()));
        descriptionField.setText(user.getDescription());
        activeBox.setSelected(user.getActive());
        pluginField.setText(user.getPlugin());
        pluginField.setEnabled(false);
        adminBox.setSelected(user.getAdministrator());
        for (String tag : user.getTags().keySet()) {
            ((DefaultTableModel) tagTable.getModel()).addRow(new Object[]{tag, user.getTag(tag)});
        }
    }

    private void initComponents() {

        nameTextField = new JTextField();
        nameTextField.setTransferHandler(null);
        nameTextField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_SPACE || (e.getKeyChar() >= 'А' && e.getKeyChar() <= 'я'))
                    e.consume();
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }

        });
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
        confirmField = new JPasswordField();
        confirmField.setTransferHandler(null);
        confirmField.addKeyListener(new KeyListener() {

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
        descriptionScroll = new JScrollPane();
        descriptionField = new JTextArea();
        userIDField = new NumberTextField();
        groupIDField = new NumberTextField();
        userIDLabel = new JLabel();
        groupIDLabel = new JLabel();
        descriptionLabel = new JLabel();
        tagLabel = new JLabel();
        okButton = new DefaultButton();
        cancelButton = new DefaultButton();
        tagTable = new JTable();
        tagScrol = new JScrollPane();
        activeBox = new JCheckBox();
        addTag = new DefaultButton();
        deleteTag = new DefaultButton();
        pluginLabel = new JLabel();
        pluginField = new JTextField();
        adminBox = new JCheckBox();

        descriptionScroll.setViewportView(descriptionField);
        tagScrol.setViewportView(tagTable);

        addTag.setText(bundleString("addTag"));
        addTag.addActionListener(evt -> addTagActionPerformed(evt));

        deleteTag.setText(bundleString("deleteTag"));
        deleteTag.addActionListener(evt -> deleteTagActionPerformed(evt));

        userIDField.setText("0");

        groupIDField.setText("0");

        userIDLabel.setText(bundleString("UserID"));

        groupIDLabel.setText(bundleString("GroupID"));

        descriptionLabel.setText(bundleString("Description"));

        pluginLabel.setText(bundleString("Plugin"));

        adminBox.setText(bundleString("Administrator"));

        tagLabel.setText(bundleString("Tags"));

        okButton.setText(bundleString("OK"));
        okButton.addActionListener(evt -> okButtonActionPerformed(evt));

        cancelButton.setText(bundleString("Cancel"));
        cancelButton.addActionListener(evt -> cancelButtonActionPerformed(evt));

        tagTable.setModel(new DefaultTableModel(new Object[][]{

        }, bundleStrings(new String[]{
                "tag", "value"
        })));

        activeBox.setText(bundleString("active"));

        setLayout(new GridBagLayout());

        GridBagConstraints gbConst = new GridBagConstraints(
                0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0);
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(gbConst).defaults();

        gbh.insertEmptyRow(this, 10);

        gbh.addLabelFieldPair(this, bundleString("UserName"), nameTextField, null, true, false);

        gbh.addLabelFieldPair(this, bundleString("Password"), passTextField, null, true, false);

        gbh.addLabelFieldPair(this, bundleString("ConfirmPassword"), confirmField, null, true, false);
        gbh.addLabelFieldPair(this, bundleString("FirstName"), firstNameField, null, true, false);
        gbh.addLabelFieldPair(this, bundleString("MiddleName"), middleNameField, null, true, false);
        gbh.addLabelFieldPair(this, bundleString("LastName"), lastNameField, null, true, false);
        gbh.addLabelFieldPair(this, userIDLabel, userIDField, null, true, false);
        gbh.addLabelFieldPair(this, groupIDLabel, groupIDField, null, true, false);
        gbh.addLabelFieldPair(this, pluginLabel, pluginField, null, true, false);
        add(activeBox, gbh.nextRowFirstCol().setLabelDefault().get());
        add(adminBox, gbh.nextCol().get());
        add(descriptionLabel, gbh.nextRowFirstCol().setLabelDefault().get());
        add(descriptionScroll, gbh.nextCol().fillBoth().setMaxWeightX().setMaxWeightY().get());
        gbh.insertEmptyBigRow(this);
        gbh.setXY(2, 1).setLabelDefault().setHeight(2).setMinWeightY();
        add(addTag, gbh.nextRow().setMaxWeightX().get());
        add(deleteTag, gbh.nextCol().setMaxWeightX().get());

        add(tagScrol, gbh.setX(2).nextRowWidth().setWidth(2).setMaxWeightY().setMaxWeightX().fillBoth().setHeight(10).get());

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx++;
        gbc.weightx = 0.5;
        gbc.insets.top = 5;
        gbc.anchor = GridBagConstraints.EAST;
        buttonPanel.add(okButton, gbc);
        gbc.weightx = 0;
        gbc.gridx++;
        gbc.insets.left = 5;
        buttonPanel.add(cancelButton, gbc);
        add(buttonPanel, gbh.setX(0).nextRowWidth().nextRow().setMinWeightY().setHeight(1).spanX().get());
        initVersion();
    }

    private void initVersion() {
        if (version >= 3) {
            userIDLabel.setVisible(false);
            groupIDLabel.setVisible(false);
            userIDField.setVisible(false);
            groupIDField.setVisible(false);

        } else {
            descriptionLabel.setVisible(false);
            descriptionScroll.setVisible(false);
            activeBox.setVisible(false);
            tagScrol.setVisible(false);
            tagLabel.setVisible(false);
            tagTable.setVisible(false);
            addTag.setVisible(false);
            deleteTag.setVisible(false);
            adminBox.setVisible(false);
            pluginField.setVisible(false);
            pluginLabel.setVisible(false);
        }
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (!nameTextField.getText().trim().equals("")) {
            if (new String(passTextField.getPassword()).equals(new String(confirmField.getPassword()))) {
                ump.userAdd.setUserName(nameTextField.getText().trim());
                ump.userAdd.setFirstName(firstNameField.getText().trim());
                ump.userAdd.setMiddleName(middleNameField.getText().trim());
                ump.userAdd.setLastName(lastNameField.getText().trim());
                ump.userAdd.setUserId(Integer.parseInt(userIDField.getText()));
                ump.userAdd.setGroupId(Integer.parseInt(groupIDField.getText()));
                ump.userAdd.setActive(activeBox.isSelected());
                ump.userAdd.setPlugin(pluginField.getText().trim());
                ump.userAdd.setAdministrator(adminBox.isSelected());
                ump.userAdd.setDescription(descriptionField.getText().trim());
                Map<String, String> tags = new HashMap<>();
                for (int i = 0; i < tagTable.getRowCount(); i++) {
                    String tag = tagTable.getModel().getValueAt(i, 0).toString();
                    String value = tagTable.getModel().getValueAt(i, 1).toString();
                    if (tag != null && !tag.isEmpty() && value != null && !value.isEmpty()) {
                        tags.put(tag, value);
                    }
                }
                ump.userAdd.setTags(tags);
                if (edit) {
                    if (passTextField.getPassword().length > 0)
                        ump.userAdd.setPassword(new String(passTextField.getPassword()));
                    try {
                        ump.editUser();
                        GUIUtilities.closeSelectedTab();
                    } catch (Exception e) {
                        GUIUtilities.displayExceptionErrorDialog(bundleString("error.failed-edit-user", ump.userAdd.getUserName()), e);

                    }

                } else {
                    if (passTextField.getPassword().length > 0) {
                        ump.userAdd.setPassword(new String(passTextField.getPassword()));
                        try {
                            ump.addUser();
                            GUIUtilities.closeSelectedTab();
                        } catch (Exception e) {
                            GUIUtilities.displayExceptionErrorDialog(bundleString("error.failed-add-user", ump.userAdd.getUserName()), e);
                        }

                    } else {
                        GUIUtilities.displayErrorMessage(bundleString("error.empty-pwd"));
                    }

                }

            } else {
                GUIUtilities.displayErrorMessage(bundleString("error.pwds-not-match"));
            }
        } else {
            GUIUtilities.displayErrorMessage(bundleString("error.empty-name"));
        }
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        GUIUtilities.closeSelectedTab();
    }

    private void addTagActionPerformed(java.awt.event.ActionEvent evt) {
        ((DefaultTableModel) tagTable.getModel()).addRow(new Object[]{"", ""});
    }

    private void deleteTagActionPerformed(java.awt.event.ActionEvent evt) {
        if (tagTable.getSelectedRow() >= 0) {
            ((DefaultTableModel) tagTable.getModel()).removeRow(tagTable.getSelectedRow());
        }
    }

    private String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

    private String bundleString(String key, Object... args) {
        return Bundles.get(getClass(), key, args);
    }

    private String[] bundleStrings(String[] key) {
        for (int i = 0; i < key.length; i++)
            key[i] = bundleString(key[i]);
        return key;
    }
}

