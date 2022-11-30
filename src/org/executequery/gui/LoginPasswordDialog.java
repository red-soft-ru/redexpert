package org.executequery.gui;

import org.executequery.gui.browser.ConnectionPanel;
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
    private JCheckBox storePassword;
    private JCheckBox showPassword;
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
        password = new JPasswordField();
        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    finished();
            }
        });
        storePassword = new JCheckBox(Bundles.get(ConnectionPanel.class, "StorePassword"));
        showPassword = new JCheckBox(Bundles.get(ConnectionPanel.class, "ShowPassword"));

        showPassword.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    password.setEchoChar((char) 0);
                } else {
                    password.setEchoChar('•');
                }
            }
        });
        LinkButton linkButton = new LinkButton(bundledString("register"));
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
        storePassword.setVisible(urlOfRegistration == null);
        JButton okButton = new JButton(bundledString("login"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                finished();
            }
        });
        JButton cancelButton = new JButton(Bundles.getCommon("cancel.button"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setClosedDialog(true);
                finished();
            }
        });

        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic();
        gbh.defaults();
        mainPanel.add(pane, gbh.spanX().get());
        gbh.addLabelFieldPair(mainPanel, bundledString("username"), username, null);

        gbh.addLabelFieldPair(mainPanel, bundledString("password"), password, null);

        mainPanel.add(password, gbh.nextCol().fillHorizontally().spanX().get());

        mainPanel.add(showPassword, gbh.nextRow().setLabelDefault().get());
        mainPanel.add(storePassword, gbh.nextCol().get());


        mainPanel.add(linkButton, gbh.nextRowFirstCol().get());

        mainPanel.add(okButton, gbh.nextCol().anchorEast().get());
        mainPanel.add(cancelButton, gbh.nextCol().anchorNorthWest().fillHorizontally().setMaxWeightX().get());

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

    public boolean isStorePassword(){return storePassword.isSelected();}

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
