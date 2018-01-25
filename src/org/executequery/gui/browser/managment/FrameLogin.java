package org.executequery.gui.browser.managment;

import org.executequery.gui.browser.UserManagerPanel;
import org.executequery.localization.Bundles;

import javax.swing.*;

/**
 * Created by mikhan808 on 05.06.2017.
 */
public class FrameLogin extends JFrame {

    UserManagerPanel ump;
    private JLabel Password_label;
    private JTextField Role_Field;
    private JLabel Role_Label;
    private JTextField Username_field;
    private JLabel Username_label;
    private JButton buttonOK;
    private JPasswordField jPasswordField1;
    private JPasswordField passw_field;

    /**
     * Creates new form NewJFrame
     */
    public FrameLogin(UserManagerPanel u) {
        initComponents();
        setResizable(false);
        ump = u;
    }

    public FrameLogin(UserManagerPanel u, String user) {
        this(u);
        Username_field.setText(user);

    }

    public FrameLogin(UserManagerPanel u, String user, String passw) {
        this(u, user);
        passw_field.setText(passw);

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrameLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrameLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrameLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrameLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrame().setVisible(true);
            }

        });
    }

    private void initComponents() {

        jPasswordField1 = new JPasswordField();
        Username_field = new JTextField();
        passw_field = new JPasswordField();
        Role_Field = new JTextField();
        buttonOK = new JButton();
        Username_label = new JLabel();
        Password_label = new JLabel();
        Role_Label = new JLabel();

        jPasswordField1.setText("jPasswordField1");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Role_Field.setHorizontalAlignment(JTextField.TRAILING);
        Role_Field.setToolTipText("");

        buttonOK.setText("OK");
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        Username_label.setText(bundleString("UserName"));

        Password_label.setText(bundleString("Password"));
        Password_label.setToolTipText("");

        Role_Label.setText(bundleString("Role"));
        this.addWindowListener(new FrameListener(this));


        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(Username_field, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(Username_label))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(passw_field, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(Password_label))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(Role_Field)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(Role_Label)
                                                        .addComponent(buttonOK))
                                                .addGap(0, 30, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(Username_label)
                                        .addComponent(Password_label)
                                        .addComponent(Role_Label))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(Username_field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(passw_field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(Role_Field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(buttonOK)
                                .addContainerGap(22, Short.MAX_VALUE))
        );

        pack();
    }

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {
        ump.userManager.setUser(Username_field.getText());
        ump.userManager.setPassword(new String(passw_field.getPassword()));
        try {
            ump.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.dispose();
    }

    private String bundleString(String key) {
        return Bundles.get(UserManagerPanel.class, key);
    }
}