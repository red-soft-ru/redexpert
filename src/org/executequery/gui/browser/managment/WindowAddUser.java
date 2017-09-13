package org.executequery.gui.browser.managment;

import biz.redsoft.IFBUser;
import org.executequery.GUIUtilities;
import org.executequery.gui.browser.UserManagerPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.NumberTextField;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikhan808 on 20.02.2017.
 */
public class WindowAddUser extends javax.swing.JPanel {

    private javax.swing.JButton cancelButton;
    private javax.swing.JPasswordField confirmField;
    private javax.swing.JTextArea descriptionField;
    private javax.swing.JTextField firstNameField;
    private NumberTextField groupIDField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel tagLabel;
    private JLabel pluginLabel;
    private JTextField pluginField;
    private JCheckBox adminBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField lastNameField;
    private javax.swing.JTextField middleNameField;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JPasswordField passTextField;
    private NumberTextField userIDField;
    private JTable tagTable;
    private JScrollPane tagScrol;
    private JCheckBox activeBox;
    private JButton addTag;
    private JButton deleteTag;

    UserManagerPanel ump;

    int version;
    IFBUser user;
    boolean edit;

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

        nameTextField = new javax.swing.JTextField();
        passTextField = new javax.swing.JPasswordField();
        confirmField = new javax.swing.JPasswordField();
        firstNameField = new javax.swing.JTextField();
        middleNameField = new javax.swing.JTextField();
        lastNameField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionField = new javax.swing.JTextArea();
        userIDField = new NumberTextField();
        groupIDField = new NumberTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        tagLabel = new JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        tagTable = new JTable();
        tagScrol = new JScrollPane();
        activeBox = new JCheckBox();
        addTag = new JButton();
        deleteTag = new JButton();
        pluginLabel=new JLabel();
        pluginField=new JTextField();
        adminBox=new JCheckBox();

        //setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        descriptionField.setColumns(20);
        descriptionField.setRows(5);
        jScrollPane1.setViewportView(descriptionField);
        tagScrol.setViewportView(tagTable);

        addTag.setText(bundleString("addTag"));
        addTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTagActionPerformed(evt);
            }
        });

        deleteTag.setText(bundleString("deleteTag"));
        deleteTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteTagActionPerformed(evt);
            }
        });

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
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(bundleString("Cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        tagTable.setModel(new DefaultTableModel(new Object[][]{

        }, bundleStrings(new String[]{
                "tag", "value"
        })));

        activeBox.setText(bundleString("active"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGap(14, 14, 14)
                                                        .addComponent(jLabel1))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(jLabel2))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(jLabel3))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(jLabel4))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(jLabel5))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(jLabel6))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(jLabel7))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(jLabel8))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(pluginLabel))
                                        )
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(nameTextField)
                                                .addComponent(passTextField)
                                                .addComponent(confirmField)
                                                .addComponent(firstNameField)
                                                .addComponent(middleNameField)
                                                .addComponent(lastNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                                .addComponent(userIDField)
                                                .addComponent(groupIDField)
                                                .addComponent(pluginField)
                                                .addComponent(activeBox)
                                                .addComponent(adminBox)
                                        ))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel9)
                                                                        .addComponent(tagLabel)))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jScrollPane1)
                                                                        .addComponent(tagScrol))
                                                        )))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(addTag, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(deleteTag, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton)
                                                .addGap(19, 19, 19))))

        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel1))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(passTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel2))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(confirmField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel3))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(firstNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel4))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(middleNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel5))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(lastNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel6))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(userIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel7))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(groupIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel8))
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(pluginField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(pluginLabel))
                                                        .addGap(18,18,18)
                                                        .addComponent(activeBox)
                                                        .addGap(18,18,18)
                                                        .addComponent(adminBox)
                                                ))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel9)
                                                        .addComponent(jScrollPane1))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tagLabel)
                                                        .addComponent(tagScrol))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(cancelButton)
                                                        .addComponent(okButton)
                                                        .addComponent(deleteTag)
                                                        .addComponent(addTag)
                                                )
                                                .addGap(10, 10, 10))))

        );
        init_version();
        //pack();
    }

    private void init_version() {
        if (version == 3) {
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
        if (!nameTextField.getText().equals("")) {
            if (new String(passTextField.getPassword()).equals(new String(confirmField.getPassword()))) {
                ump.userAdd.setUserName(nameTextField.getText());
                ump.userAdd.setFirstName(firstNameField.getText());
                ump.userAdd.setMiddleName(middleNameField.getText());
                ump.userAdd.setLastName(lastNameField.getText());
                ump.userAdd.setUserId(Integer.parseInt(userIDField.getText()));
                ump.userAdd.setGroupId(Integer.parseInt(groupIDField.getText()));
                ump.userAdd.setActive(activeBox.isSelected());
                ump.userAdd.setPlugin(pluginField.getText());
                ump.userAdd.setAdministrator(adminBox.isSelected());
                ump.userAdd.setDescription(descriptionField.getText());
                Map<String, String> tags = new HashMap<>();
                for (int i = 0; i < tagTable.getRowCount(); i++) {
                    tags.put((String) ((DefaultTableModel) tagTable.getModel()).getValueAt(i, 0), (String) ((DefaultTableModel) tagTable.getModel()).getValueAt(i, 1));
                }
                ump.userAdd.setTags(tags);
                if (edit) {
                    if (passTextField.getPassword().length > 0)
                        ump.userAdd.setPassword(new String(passTextField.getPassword()));
                    ump.editUser();
                    GUIUtilities.closeSelectedTab();
                } else {
                    if (passTextField.getPassword().length > 0) {
                        ump.userAdd.setPassword(new String(passTextField.getPassword()));
                        ump.addUser();
                        GUIUtilities.closeSelectedTab();
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

    private String[] bundleStrings(String[] key) {
        for (int i = 0; i < key.length; i++)
            key[i] = bundleString(key[i]);
        return key;
    }
}

