package org.executequery.gui.browser;

import org.executequery.GUIUtilities;

import javax.swing.*;

/**
 * Created by mikhan808 on 15.03.2017.
 */
public class WindowAddRole extends javax.swing.JPanel {
    public WindowAddRole(UserManagerPanel u)
    {
        initComponents();
        ump=u;
    }
    UserManagerPanel ump;
    JTextField nameTextField;
    JButton okButton;
    JLabel jLabel1;
    private void initComponents()
    {
        nameTextField = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel1.setText("Name");
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(null);
        //layout.setHorizontalGroup();
        okButton.setSize(50,20);
        okButton.setLocation(50,100);
        jLabel1.setSize(50,20);
        jLabel1.setLocation(50,50);
        nameTextField.setSize(500,20);
        nameTextField.setLocation(100,50);
        this.add(okButton);
        this.add(jLabel1);
        this.add(nameTextField);

    }
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        ump.addRole(nameTextField.getText());
        GUIUtilities.closeSelectedTab();
    }
}
