package org.executequery.gui.browser.managment;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.browser.UserManagerPanel;
import org.executequery.localization.Bundles;

import javax.swing.*;

/**
 * Created by mikhan808 on 15.03.2017.
 */
public class WindowAddRole extends JPanel {

    public static final String TITLE = "Create role";
    DatabaseConnection dc;
    ActionContainer parent;
    JTextField nameTextField;
    JButton okButton;
    JButton cancelButton;
    JLabel jLabel1;

    public WindowAddRole(ActionContainer parent, DatabaseConnection dc) {
        this.parent = parent;
        this.dc = dc;
        initComponents();

    }

    private void initComponents() {
        nameTextField = new JTextField();
        okButton = new JButton();
        cancelButton = new JButton();
        jLabel1 = new JLabel();
        jLabel1.setText(Bundles.get(UserManagerPanel.class, "RoleName"));
        okButton.setText("OK");
        cancelButton.setText(Bundles.getCommon("cancel.button"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parent.finished();
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
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                        )
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton)
                                        .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1)
                                )

                        )
        );

    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String query = "CREATE ROLE " + nameTextField.getText();
        ExecuteQueryDialog eqd = new ExecuteQueryDialog("Create Role", query, dc, true);
        eqd.display();
        if (eqd.getCommit())
            parent.finished();
    }
}
