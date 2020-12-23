/*
 * SSHTunnelConnectionPanel.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.browser;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.TextFieldPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.ComponentTitledPanel;
import org.underworldlabs.swing.DefaultFieldLabel;
import org.underworldlabs.swing.LinkButton;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class SSHTunnelConnectionPanel extends AbstractConnectionPanel {

    GridBagHelper gbh;
    private JTextField userNameField;
    private JPasswordField passwordField;
    private NumberTextField portField;
    private JCheckBox savePwdCheck;
    private JCheckBox useSshCheckbox;
    private TextFieldPanel mainPanel;

    public SSHTunnelConnectionPanel() {

        super(new BorderLayout());
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JTextField hostField;

    private void init() throws IOException {

        gbh = new GridBagHelper();
        hostField = new JTextField();
        userNameField = WidgetFactory.createTextField();
        passwordField = WidgetFactory.createPasswordField();
        portField = WidgetFactory.createNumberTextField();

        mainPanel = new TextFieldPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridy = 0;
        gbc.gridx = 0;

        gbc.insets.bottom = 5;

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(new DefaultFieldLabel(
                FileUtils.loadResource(bundleString("path_to_html"))), gbc);
        gbh.setDefaults(gbc).defaults();
        gbh.addLabelFieldPair(mainPanel, bundleString("hostField"), hostField,
                bundleString("hostField.tool-tip"));

        gbh.addLabelFieldPair(mainPanel, bundleString("portField"), portField,
                bundleString("portField.tool-tip"));

        gbh.addLabelFieldPair(mainPanel, bundleString("userField"), userNameField,
                bundleString("userField.tool-tip"));

        gbh.addLabelFieldPair(mainPanel, bundleString("passwordField"), passwordField,
                bundleString("passwordField"));

        savePwdCheck = ActionUtilities.createCheckBox(bundleString("StorePassword"), "setStorePassword");

        JButton showPassword = new LinkButton(bundleString("ShowPassword"));
        showPassword.setActionCommand("showPassword");
        showPassword.addActionListener(this);

        JPanel passwordOptionsPanel = new JPanel(new GridBagLayout());
        addComponents(passwordOptionsPanel,
                new ComponentToolTipPair[]{
                        new ComponentToolTipPair(savePwdCheck, bundleString("StorePassword.tool-tip")),
                        new ComponentToolTipPair(showPassword, bundleString("ShowPassword.tool-tip"))});
        gbc = gbh.get();
        gbc.gridy++;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(passwordOptionsPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);

        useSshCheckbox = ActionUtilities.createCheckBox(this, bundleString("borderTitle"), "useSshSelected");
        ComponentTitledPanel titledPanel = new ComponentTitledPanel(useSshCheckbox);

        JPanel panel = titledPanel.getContentPane();
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(titledPanel, BorderLayout.NORTH);
    }

    public void useSshSelected() {

        if (useSshCheckbox.isSelected()) {

            enableFields(true);

        } else {

            enableFields(false);
        }

    }

    private void enableFields(boolean enable) {

        enableComponents(mainPanel.getComponents(), enable);
    }

    private void enableComponents(Component[] components, boolean enable) {

        for (Component component : components) {

            component.setEnabled(enable);
            if (component instanceof JPanel) {

                enableComponents(((JPanel) component).getComponents(), enable);
            }
        }

    }

    public void setValues(DatabaseConnection databaseConnection) {

        hostField.setText(databaseConnection.getHost());
        userNameField.setText(databaseConnection.getSshUserName());
        passwordField.setText(databaseConnection.getUnencryptedSshPassword());

        if (databaseConnection.getSshPort() <= 0) {

            portField.setText("22");

        } else {

            portField.setText(String.valueOf(databaseConnection.getSshPort()));
        }

        savePwdCheck.setSelected(databaseConnection.isSshPasswordStored());
        useSshCheckbox.setSelected(databaseConnection.isSshTunnel());
        enableFields(databaseConnection.isSshTunnel());
    }

    public void showPassword() {

        new ShowPasswordDialog(hostField.getText(),
                MiscUtils.charsToString(passwordField.getPassword()));
    }

    public void update(DatabaseConnection databaseConnection) {

        databaseConnection.setSshTunnel(useSshCheckbox.isSelected());
        databaseConnection.setSshUserName(userNameField.getText());
        databaseConnection.setSshPassword(MiscUtils.charsToString(passwordField.getPassword()));
        databaseConnection.setSshPort(portField.getValue());
        databaseConnection.setSshPasswordStored(savePwdCheck.isSelected());
    }

    public boolean canConnect() {

        if (useSshCheckbox.isSelected()) {

            if (!hasValue(userNameField)) {

                GUIUtilities.displayErrorMessage("You have selected SSH Tunnel but have not provided an SSH user name");
                return false;
            }

            if (!hasValue(portField)) {

                GUIUtilities.displayErrorMessage("You have selected SSH Tunnel but have not provided an SSH port");
                return false;
            }

            if (!hasValue(passwordField)) {

                final JPasswordField field = WidgetFactory.createPasswordField();

                JOptionPane optionPane = new JOptionPane(field, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                JDialog dialog = optionPane.createDialog("Enter SSH password");

                dialog.addWindowFocusListener(new WindowAdapter() {
                    @Override
                    public void windowGainedFocus(WindowEvent e) {
                        field.requestFocusInWindow();
                    }
                });

                dialog.pack();
                dialog.setLocation(GUIUtilities.getLocationForDialog(dialog.getSize()));
                dialog.setVisible(true);
                dialog.dispose();

                int result = Integer.parseInt(optionPane.getValue().toString());
                if (result == JOptionPane.OK_OPTION) {

                    String password = MiscUtils.charsToString(field.getPassword());
                    if (StringUtils.isNotBlank(password)) {

                        passwordField.setText(password);
                        return true;

                    } else {

                        GUIUtilities.displayErrorMessage("You have selected SSH Tunnel but have not provided an SSH password");

                        // send back here and force them to select cancel if they want to bail

                        return canConnect();
                    }

                }
                return false;
            }

        }

        return true;
    }

    private boolean hasValue(JTextField textField) {

        return StringUtils.isNotBlank(textField.getText());
    }

    protected String bundleString(String key) {
        return Bundles.get(ConnectionPanel.class, key);
    }

}


