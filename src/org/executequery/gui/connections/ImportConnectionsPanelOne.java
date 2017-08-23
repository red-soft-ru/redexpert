package org.executequery.gui.connections;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.ActionPanel;

public class ImportConnectionsPanelOne extends ActionPanel {
    
    private JTextField fileNameField;

    public ImportConnectionsPanelOne() {

        super(new GridBagLayout());
        init();
    }
    
    private void init() {
        
        fileNameField = WidgetFactory.createTextField();

        JButton button = WidgetFactory.createInlineFieldButton("Browse");
        button.setActionCommand("browse");
        button.addActionListener(this);
        button.setMnemonic('r');
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridheight = 1;
        gbc.insets.top = 7;
        gbc.insets.bottom = 10;
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        add(new JLabel("<html>This allows you to import connections from a file - usually generated from the export on another"
                + " instance of this application.<br />&nbsp;</html>"), gbc);        

        gbc.gridy++;
        add(new JLabel("<html><b><i>Note: </i></b>Any folders with existing names will be merged with "
                + "those in the file.<br />Any connections with existing names will be added with the suffix '_1'.<br />"
                + "Driver libraries are not part of the import process and will need to be added separately."
                + "<br />&nbsp;</html>"), gbc);        
        
        gbc.gridy++;
        gbc.insets.top = 10;
        add(new JLabel("Select the file to import folders and connections from."), gbc);        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 5;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.insets.bottom = 20;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Import File:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(fileNameField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        add(button, gbc);
        
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        setPreferredSize(new Dimension(650, 500));
    }

    public boolean canProceed() {
        
        if (StringUtils.isBlank(fileNameField.getText())) {
            
            GUIUtilities.displayErrorMessage("You must select a file path to import from");
            return false;
        }
        return true;
    }
    
    public String getImportPath() {
        
        return fileNameField.getText();
    }
    
    public void browse() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle("Select Import File Path");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();
        fileNameField.setText(file.getAbsolutePath());
    }

}
