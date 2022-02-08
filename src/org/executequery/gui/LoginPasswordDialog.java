package org.executequery.gui;

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.LinkButton;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class LoginPasswordDialog extends BaseDialog {
    private JTextField username;
    private JPasswordField password;
    private final String message;
    private final String user;
    private boolean closedDialog = false;
    private final String urlOfRegistration;

    public LoginPasswordDialog(String name, String message, String urlOfRegistration) {
        this(name, message, urlOfRegistration, null);
    }

    public LoginPasswordDialog(String name, String message, String urlOfRegistration, String username) {
        super(name, true, true);
        this.message = message;
        user = username;
        this.urlOfRegistration = urlOfRegistration;
        init();
    }

    void init() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setText(message);

        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        gbh.defaults();
        mainPanel.add(pane, gbh.spanX().get());
        JLabel label = new JLabel(bundledString("username"));
        mainPanel.add(label, gbh.nextRowFirstCol().setLabelDefault().get());
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
        mainPanel.add(username, gbh.nextCol().spanX().get());
        label = new JLabel(bundledString("password"));
        mainPanel.add(label, gbh.nextRowFirstCol().setLabelDefault().get());
        password = new JPasswordField();
        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    finished();
            }
        });
        mainPanel.add(password, gbh.nextCol().spanX().get());
        LinkButton linkButton = new LinkButton(bundledString("register"));
        mainPanel.add(linkButton, gbh.nextRowFirstCol().setLabelDefault().get());
        linkButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URL(urlOfRegistration).toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        linkButton.setVisible(urlOfRegistration != null);
        JButton okButton = new JButton(bundledString("login"));
        mainPanel.add(okButton, gbh.nextCol().get());
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                finished();
            }
        });
        JButton cancelButton = new JButton(Bundles.getCommon("cancel.button"));
        mainPanel.add(cancelButton, gbh.nextCol().get());
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setClosedDialog(true);
                finished();
            }
        });
        setResizable(false);
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
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setClosedDialog(true);
                finished();
            }
        });

    }

    public String getUsername() {
        return username.getText();
    }

    public String getPassword() {
        return new String(password.getPassword());
    }

    public boolean isClosedDialog() {
        return closedDialog;
    }

    public void setClosedDialog(boolean closedDialog) {
        this.closedDialog = closedDialog;
    }

    private String bundledString(String key) {
        return Bundles.get(this.getClass(), key);
    }
}
