package org.underworldlabs.swing;

import org.executequery.gui.IconManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Class that's enable password visibility changing.
 *
 * @author Alexey Kozlov
 */
public class ViewablePasswordField extends JPanel {

    private boolean passwordHidden;

    // --- gui components ---

    private RolloverButton toggleButton;
    private JPasswordField passwordField;

    // ---

    public ViewablePasswordField() {
        init();
        update();
        arrange();
    }

    private void init() {
        passwordHidden = true;

        passwordField = WidgetFactory.createPasswordField("passwordField");
        passwordField.setBorder(null);

        toggleButton = WidgetFactory.createRolloverButton("toggleButton");
        toggleButton.addActionListener(e -> togglePasswordVisible());
        toggleButton.setBackground(passwordField.getBackground());
        toggleButton.enableSelectionRollover(false);
    }

    private void arrange() {

        setLayout(new GridBagLayout());
        setBorder(new JTextField().getBorder());
        setBackground(new JTextField().getBackground());
        setPreferredSize(new Dimension(getPreferredSize().width, WidgetFactory.defaultHeight()));

        GridBagHelper gbh = new GridBagHelper().setMaxWeightX().fillBoth();
        add(passwordField, gbh.get());
        add(toggleButton, gbh.nextCol().setMinWeightX().get());
    }

    private void update() {

        toggleButton.setToolTipText(bundleString(passwordHidden ?
                "buttonTooltip.show" :
                "buttonTooltip.hide"
        ));

        toggleButton.setIcon(IconManager.getIcon(passwordHidden ?
                "icon_password_hide" :
                "icon_password_show"
        ));

        passwordField.setEchoChar(passwordHidden ? 8226 : (char) 0);
    }

    private void togglePasswordVisible() {
        this.passwordHidden = !passwordHidden;
        update();
    }

    // --- JComponent impl ---

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        toggleButton.setEnabled(enabled);
        passwordField.setEnabled(enabled);
    }

    // ---

    public void setPassword(String text) {
        passwordField.setText(text);
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(ViewablePasswordField.class, key, args);
    }

}
