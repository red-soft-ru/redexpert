package org.underworldlabs.swing;

import org.executequery.gui.IconManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;

/**
 * Class that's enable password visibility changing.
 *
 * @author Alexey Kozlov
 */
public class ViewablePasswordField extends JPanel {

    private boolean visible;

    // --- gui components ---

    private JTextField textField;
    private RolloverButton toggleButton;
    private JPasswordField passwordField;

    // ---

    public ViewablePasswordField() {
        this.visible = false;

        init();
        update();
        arrange();
    }

    private void init() {
        Document document = new PlainDocument();

        textField = WidgetFactory.createTextField("textField");
        textField.setDocument(document);
        textField.setBorder(null);

        passwordField = WidgetFactory.createPasswordField("passwordField");
        passwordField.setDocument(document);
        passwordField.setBorder(null);

        toggleButton = WidgetFactory.createRolloverButton("toggleButton");
        toggleButton.addActionListener(e -> togglePasswordVisible());
        toggleButton.setBackground(textField.getBackground());
        toggleButton.enableSelectionRollover(false);
    }

    private void arrange() {

        setLayout(new GridBagLayout());
        setBorder(new JTextField().getBorder());
        setBackground(textField.getBackground());

        GridBagHelper gbh = new GridBagHelper().setMaxWeightX().fillBoth();
        add(textField, gbh.get());
        add(passwordField, gbh.get());
        add(toggleButton, gbh.nextCol().setMinWeightX().get());
    }

    private void update() {

        toggleButton.setToolTipText(bundleString(visible ?
                "buttonTooltip.hide" :
                "buttonTooltip.show"
        ));

        toggleButton.setIcon(IconManager.getIcon(visible ?
                "icon_password_show" :
                "icon_password_hide"
        ));

        textField.setVisible(visible);
        passwordField.setVisible(!visible);

        if (visible)
            textField.requestFocusInWindow();
        else
            passwordField.requestFocusInWindow();
    }

    private void togglePasswordVisible() {
        this.visible = !visible;
        update();
    }

    // ---

    public void setPassword(String text) {
        passwordField.setText(text);
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(ViewablePasswordField.class, key, args);
    }

}
