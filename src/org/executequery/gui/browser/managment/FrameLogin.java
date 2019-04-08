package org.executequery.gui.browser.managment;

import org.executequery.components.BottomButtonPanel;
import org.executequery.gui.browser.UserManagerPanel;
import org.executequery.localization.Bundles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by mikhan808 on 05.06.2017.
 */
public class FrameLogin extends JFrame {

    UserManagerPanel userManagerPanel;
    private JLabel passwordLabel;
    private JTextField roleField;
    private JLabel roleLabel;
    private JTextField usernameField;
    private JLabel usernameLabel;
    private JPasswordField passwordField;
    FrameLogin frameLogin;
    private boolean useCustomServer;

    /**
     * Creates new form NewJFrame
     */
    public FrameLogin(UserManagerPanel userManagerPanel) {
        this.userManagerPanel = userManagerPanel;
        initComponents();
        this.setSize(800, this.getHeight());
        useCustomServer = false;
    }

    public FrameLogin(UserManagerPanel userManagerPanel, String user) {
        this(userManagerPanel);
        usernameField.setText(user);

    }

    public FrameLogin(UserManagerPanel userManagerPanel, String user, String password) {
        this(userManagerPanel, user);
        passwordField.setText(password);
    }

    private void initComponents() {
        setTitle(bundleString("Connect", userManagerPanel.getSelectedDatabaseConnection().getName()));
        frameLogin = this;
        passwordField = new JPasswordField();
        usernameField = new JTextField();
        roleField = new JTextField();
        usernameLabel = new JLabel();
        passwordLabel = new JLabel();
        roleLabel = new JLabel();

        passwordField.setText("passwordField");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        usernameLabel.setText(bundleString("UserName"));
        passwordLabel.setText(bundleString("Password"));
        roleLabel.setText(bundleString("Role"));

        this.addWindowListener(new FrameListener(this));

        this.setLayout(new BorderLayout());
        BottomButtonPanel bottomButtonPanel = new BottomButtonPanel(true);
        bottomButtonPanel.setOkButtonAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okAction();
            }
        });
        bottomButtonPanel.setOkButtonText(Bundles.getCommon("ok.button"));
        bottomButtonPanel.setCancelButtonAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frameLogin.dispose();
            }
        });
        bottomButtonPanel.setCancelButtonText(Bundles.getCommon("cancel.button"));
        bottomButtonPanel.setHelpButtonVisible(false);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(usernameLabel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5),
                0, 0));
        panel.add(usernameField, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5),
                0, 0));
        panel.add(passwordLabel, new GridBagConstraints(2, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5),
                0, 0));
        panel.add(passwordField, new GridBagConstraints(3, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5),
                0, 0));
        panel.add(roleLabel, new GridBagConstraints(4, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5),
                0, 0));
        panel.add(roleField, new GridBagConstraints(5, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5),
                0, 0));
        // add empty panel for stretch
        panel.add(new JPanel(), new GridBagConstraints(0, 1,
                6, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                0, 0));
        this.add(panel, BorderLayout.CENTER);
        this.add(bottomButtonPanel, BorderLayout.SOUTH);

        pack();
    }

    private void okAction() {
        userManagerPanel.userManager.setUser(usernameField.getText());
        userManagerPanel.userManager.setPassword(new String(passwordField.getPassword()));
        try {
            userManagerPanel.setUseCustomServer(getUseCustomServer());
            userManagerPanel.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.dispose();
    }

    private String bundleString(String key) {
        return Bundles.get(UserManagerPanel.class, key);
    }

    private String bundleString(String key, Object... args) {
        return Bundles.get(UserManagerPanel.class, key, args);
    }

    public void setUseCustomServer(boolean useCustomServer) {
        this.useCustomServer = useCustomServer;
    }

    public boolean getUseCustomServer() {
        return useCustomServer;
    }
}