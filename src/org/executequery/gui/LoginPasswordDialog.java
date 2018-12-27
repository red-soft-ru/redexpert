package org.executequery.gui;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginPasswordDialog extends BaseDialog {
    private JTextField username;
    private JPasswordField password;
    private String message;
    private String user;

    public LoginPasswordDialog(String name, String message) {
        this(name, message, null);
    }

    public LoginPasswordDialog(String name, String message, String username) {
        super(name, true, true);
        this.message = message;
        user = username;
        init();
    }

    void init() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setText(message);
        mainPanel.add(pane, new GridBagConstraints(0, 0,
                2, 1, 1, 0,
                GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        JLabel label = new JLabel("Username");
        mainPanel.add(label, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        username = new JTextField();
        if (user != null) {
            username.setText(user);
        }
        username.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    password.requestFocusInWindow();
            }
        });
        mainPanel.add(username, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        label = new JLabel("Password");
        mainPanel.add(label, new GridBagConstraints(0, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        password = new JPasswordField();
        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    finished();
            }
        });
        mainPanel.add(password, new GridBagConstraints(1, 2,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        JButton button = new JButton("OK");
        mainPanel.add(button, new GridBagConstraints(0, 3,
                2, 1, 1, 0,
                GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                finished();
            }
        });
        mainPanel.setPreferredSize(new Dimension(300, 200));
        addDisplayComponent(mainPanel);
        mainPanel.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (user == null) {
                    username.requestFocusInWindow();
                } else {
                    password.requestFocusInWindow();
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {

            }

            @Override
            public void ancestorMoved(AncestorEvent event) {

            }
        });

    }

    public String getUsername() {
        return username.getText();
    }

    public String getPassword() {
        return new String(password.getPassword());
    }
}
