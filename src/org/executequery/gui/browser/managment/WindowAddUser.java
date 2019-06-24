package org.executequery.gui.browser.managment;

import biz.redsoft.IFBUser;
import org.executequery.GUIUtilities;
import org.executequery.gui.browser.UserManagerPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.NumberTextField;

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
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JLabel tagLabel;
    private JLabel pluginLabel;
    private JTextField pluginField;
    private JCheckBox adminBox;
    private JScrollPane jScrollPane1;
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
        jScrollPane1 = new JScrollPane();
        descriptionField = new JTextArea();
        userIDField = new NumberTextField();
        groupIDField = new NumberTextField();
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        jLabel6 = new JLabel();
        jLabel7 = new JLabel();
        jLabel8 = new JLabel();
        jLabel9 = new JLabel();
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

        jScrollPane1.setViewportView(descriptionField);
        tagScrol.setViewportView(tagTable);

        addTag.setText(bundleString("addTag"));
        addTag.addActionListener(evt -> addTagActionPerformed(evt));

        deleteTag.setText(bundleString("deleteTag"));
        deleteTag.addActionListener(evt -> deleteTagActionPerformed(evt));

        userIDField.setText("0");

        groupIDField.setText("0");

        jLabel1.setText(bundleString("UserName"));

        jLabel2.setText(bundleString("Password"));

        jLabel3.setText(bundleString("ConfirmPassword"));

        jLabel4.setText(bundleString("FirstName"));

        jLabel5.setText(bundleString("MiddleName"));

        jLabel6.setText(bundleString("LastName"));

        jLabel7.setText(bundleString("UserID"));

        jLabel8.setText(bundleString("GroupID"));

        jLabel9.setText(bundleString("Description"));

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

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbConst = new GridBagConstraints(
                0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0);
        panel.add(jLabel1, gbConst);
        gbConst.gridy++;
        panel.add(jLabel2, gbConst);
        gbConst.gridy++;
        panel.add(jLabel3, gbConst);
        gbConst.gridy++;
        panel.add(jLabel4, gbConst);
        gbConst.gridy++;
        panel.add(jLabel5, gbConst);
        gbConst.gridy++;
        panel.add(jLabel6, gbConst);
        gbConst.gridy++;
        panel.add(jLabel7, gbConst);
        gbConst.gridy++;
        panel.add(jLabel8, gbConst);
        gbConst.gridy++;
        panel.add(pluginLabel, gbConst);
        gbConst.weightx = 0.5;
        gbConst.gridx = 1;
        gbConst.gridy = 0;
        gbConst.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameTextField, gbConst);
        gbConst.gridy++;
        panel.add(passTextField, gbConst);
        gbConst.gridy++;
        panel.add(confirmField, gbConst);
        gbConst.gridy++;
        panel.add(firstNameField, gbConst);
        gbConst.gridy++;
        panel.add(middleNameField, gbConst);
        gbConst.gridy++;
        panel.add(lastNameField, gbConst);
        gbConst.gridy++;
        panel.add(userIDField, gbConst);
        gbConst.gridy++;
        panel.add(groupIDField, gbConst);
        gbConst.gridy++;
        panel.add(pluginField, gbConst);
        gbConst.gridy++;
        panel.add(activeBox, gbConst);
        gbConst.gridy++;
        panel.add(adminBox, gbConst);

        gbConst.fill = GridBagConstraints.NONE;
        gbConst.weightx = 0;
        gbConst.gridy = 0;
        gbConst.gridx = 2;
        panel.add(jLabel9, gbConst);

        gbConst.fill = GridBagConstraints.BOTH;
        gbConst.weightx = 0;
        gbConst.weighty = 0;
        gbConst.gridx = 3;
        gbConst.gridheight = 3;
        panel.add(jScrollPane1, gbConst);

        gbConst.fill = GridBagConstraints.NONE;
        gbConst.weightx = 0;
        gbConst.weighty = 0;
        gbConst.gridy = 3;
        gbConst.gridx = 2;
        gbConst.gridheight = 1;
        panel.add(tagLabel, gbConst);

        gbConst.gridy++;
        panel.add(addTag, gbConst);

        gbConst.gridy++;
        panel.add(deleteTag, gbConst);

        gbConst.fill = GridBagConstraints.BOTH;
        gbConst.weightx = 1;
        gbConst.gridy = 3;
        gbConst.gridx = 3;
        gbConst.gridheight = 8;
        gbConst.weighty = 1;
        panel.add(tagScrol, gbConst);

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

        gbConst.fill = GridBagConstraints.HORIZONTAL;
        gbConst.gridy = 11;
        gbConst.gridx = 3;
        gbConst.gridheight = 1;
        gbConst.weightx = 0;
        gbConst.weighty = 0;
        panel.add(buttonPanel, gbConst);

        setLayout(new GridLayout());
        add(panel);
        initVersion();
    }

    private void initVersion() {
        if (version >= 3) {
            jLabel7.setVisible(false);
            jLabel8.setVisible(false);
            userIDField.setVisible(false);
            groupIDField.setVisible(false);

        } else {
            jLabel9.setVisible(false);
            descriptionField.setVisible(false);
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

