/*
 * ExportConnectionsPanelTwo.java
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

public class ExportConnectionsPanelTwo extends ActionPanel {
    
    private JTextField fileNameField;

    public ExportConnectionsPanelTwo() {

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
        add(new JLabel("Select the file to export the selections below."), gbc);        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 5;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.insets.bottom = 20;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Export File:"), gbc);
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
            
            GUIUtilities.displayErrorMessage("You must select a file path to export to");
            return false;
        }
        return true;
    }
    
    public String getExportPath() {
        
        return fileNameField.getText();
    }
    
    public void browse() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle("Select Export File Path");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();
        fileNameField.setText(file.getAbsolutePath());
    }

}

