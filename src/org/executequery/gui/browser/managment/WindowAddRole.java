package org.executequery.gui.browser.managment;

import org.executequery.GUIUtilities;
import org.executequery.gui.browser.UserManagerPanel;

import javax.swing.*;

/**
 * Created by mikhan808 on 15.03.2017.
 */
public class WindowAddRole extends JPanel {

    UserManagerPanel ump;
    JTextField nameTextField;
    JButton okButton;
    JLabel jLabel1;

    public WindowAddRole(UserManagerPanel u) {
        ump = u;
        initComponents();
    }

    private void initComponents() {
        nameTextField = new JTextField();
        okButton = new JButton();
        jLabel1 = new JLabel();
        jLabel1.setText(ump.bundleString("RoleName"));
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(okButton, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                        )
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(okButton)
                                        .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1)
                                )

                        )
        );

    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        ump.addRole(nameTextField.getText());
        GUIUtilities.closeSelectedTab();
    }
}
