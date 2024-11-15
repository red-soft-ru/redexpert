package org.executequery.gui;

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.LinkLabel;
import org.underworldlabs.swing.ViewablePasswordField;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.event.*;

public class LoginPasswordDialog extends BaseDialog {

    private final String message;
    private final String username;
    private final String registrationUrl;

    private boolean closedDialog = false;

    // --- GUI components ---

    private JButton submitButton;
    private JButton cancelButton;
    private LinkLabel linkButton;

    private JPanel mainPanel;
    private JTextField usernameField;
    private JCheckBox storePasswordCheck;
    private ViewablePasswordField passwordField;

    // ---

    public LoginPasswordDialog(String name, String message, String registrationUrl) {
        this(name, message, registrationUrl, null);
    }

    public LoginPasswordDialog(String name, String message, String registrationUrl, String username) {
        super(name, true, true);
        this.registrationUrl = registrationUrl;
        this.username = username;
        this.message = message;

        init();
        arrange();
        initListeners();
    }

    private void init() {

        mainPanel = WidgetFactory.createPanel("mainPanel");
        usernameField = WidgetFactory.createTextField("usernameField", username);
        passwordField = WidgetFactory.createViewablePasswordField("passwordField");

        submitButton = WidgetFactory.createButton("submitButton", bundledString("login"), e -> finished());
        linkButton = WidgetFactory.createLinkLabel("linkButton", bundledString("register"), registrationUrl);
        cancelButton = WidgetFactory.createButton("submitButton", Bundles.getCommon("cancel.button"), e -> cancel());
        storePasswordCheck = WidgetFactory.createCheckBox("storePasswordCheck", Bundles.get("AbstractConnectionPanel.StorePassword"));

        linkButton.setVisible(registrationUrl != null);
        storePasswordCheck.setVisible(registrationUrl == null);
    }

    private void cancel() {
        setClosedDialog(true);
        finished();
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- button panel ---

        JPanel buttonPanel = WidgetFactory.createPanel("buttonPanel");

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        buttonPanel.add(submitButton, gbh.get());
        buttonPanel.add(cancelButton, gbh.nextCol().leftGap(5).get());

        // --- main panel ---

        gbh = new GridBagHelper().anchorNorthWest().setInsets(5, 5, 5, 0).fillHorizontally();
        mainPanel.add(WidgetFactory.createLabel(format(message)), gbh.setMaxWeightY().spanX().get());
        mainPanel.add(WidgetFactory.createLabel(bundledString("username")), gbh.nextRowFirstCol().setWidth(1).setMinWeightY().setMinWeightX().get());
        mainPanel.add(usernameField, gbh.nextCol().leftGap(0).setMaxWeightX().get());
        mainPanel.add(WidgetFactory.createLabel(bundledString("password")), gbh.nextRowFirstCol().leftGap(5).setMinWeightX().get());
        mainPanel.add(passwordField, gbh.nextCol().leftGap(0).setMaxWeightX().get());
        mainPanel.add(storePasswordCheck, gbh.nextRow().get());
        mainPanel.add(linkButton, gbh.get());
        mainPanel.add(buttonPanel, gbh.nextRowFirstCol().leftGap(5).bottomGap(5).spanX().get());

        setResizable(false);
        addDisplayComponent(mainPanel);
    }

    private void initListeners() {

        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    passwordField.getField().requestFocusInWindow();
            }
        });

        passwordField.getField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    finished();
            }
        });

        mainPanel.registerKeyboardAction(
                e -> cancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

    }

    // --- helper methods ---

    private String format(String message) {

        if (message == null)
            return "";

        String formatted = message.replace("\n", "<br>");
        formatted = String.format("<html>%s</html>", formatted);

        return formatted;
    }

    private static String bundledString(String key) {
        return Bundles.get(LoginPasswordDialog.class, key);
    }

    // --- getters/setters ---

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return passwordField.getPassword();
    }

    public boolean isStorePassword() {
        return storePasswordCheck.isSelected();
    }

    public boolean isClosedDialog() {
        return closedDialog;
    }

    public void setClosedDialog(boolean closedDialog) {
        this.closedDialog = closedDialog;
    }

}
