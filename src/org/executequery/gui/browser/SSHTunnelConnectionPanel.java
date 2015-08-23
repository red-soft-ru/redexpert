package org.executequery.gui.browser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.TextFieldPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.ComponentTitledPanel;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.LinkButton;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

public class SSHTunnelConnectionPanel extends AbstractConnectionPanel {

    private DisabledField hostField;
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

    private void init() throws IOException {

        hostField = new DisabledField();
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
        mainPanel.add(new JLabel(
                FileUtils.loadResource("org/executequery/gui/browser/resource/ssh-tunnel.html")), gbc);
        
        addLabelFieldPair(mainPanel, "SSH Host:", hostField,
                "The SSH host server for the tunnel", gbc);

        addLabelFieldPair(mainPanel, "SSH Port:", portField,
                "The SSH server port", gbc);

        addLabelFieldPair(mainPanel, "SSH User Name:", userNameField,
                "The SSH user name for the tunnel", gbc);

        addLabelFieldPair(mainPanel, "SSH Password:", passwordField,
                "The SSH user password for the tunnel", gbc);

        savePwdCheck = ActionUtilities.createCheckBox("Store Password", "setStorePassword");

        JButton showPassword = new LinkButton("Show Password");
        showPassword.setActionCommand("showPassword");
        showPassword.addActionListener(this);
        
        JPanel passwordOptionsPanel = new JPanel(new GridBagLayout());
        addComponents(passwordOptionsPanel,
                      new ComponentToolTipPair[]{
                        new ComponentToolTipPair(savePwdCheck, "Store the password with the connection information"),
                        new ComponentToolTipPair(showPassword, "Show the password in plain text")});

        gbc.gridy++;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(passwordOptionsPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);

        useSshCheckbox = ActionUtilities.createCheckBox(this, "Connect Using an SSH Tunnel", "useSshSelected");
        ComponentTitledPanel titledPanel = new ComponentTitledPanel(useSshCheckbox);
        
        JPanel panel = titledPanel.getContentPane();
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
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

}
