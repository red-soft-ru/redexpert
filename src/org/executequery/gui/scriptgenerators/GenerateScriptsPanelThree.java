/*
 * GenerateScriptsPanelThree.java
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

package org.executequery.gui.scriptgenerators;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.ComponentTitledPanel;
import org.underworldlabs.swing.DefaultFieldLabel;
import org.underworldlabs.swing.FileSelector;

/**
 * Step three panel in the generate scripts wizard.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class GenerateScriptsPanelThree extends JPanel 
                                       implements ActionListener,
                                                  ItemListener,
                                                  GenerateScriptsPanel {

    /** save to path field */
    private JTextField pathField;

    private JCheckBox constraintsCheck;
    private JCheckBox consAsAlterCheck;
    private JCheckBox consInCreateCheck;
    
    private JCheckBox writeToFileCheck;
    private JCheckBox useCascadeCheck;
    private JCheckBox openInQueryEditor;
    
    /** the parent controller */
    private GenerateScriptsWizard parent;

    private ComponentTitledPanel createTableOptionsPanel;

    private GridBagConstraints gbc;
    
    public GenerateScriptsPanelThree(GenerateScriptsWizard parent) {
        
        super(new GridBagLayout());        
        this.parent = parent;
        
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {

        openInQueryEditor = new JCheckBox("View in a new Query Editor", true);
        
        gbc = new GridBagConstraints();
        gbc.gridx++;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.top = 10;
        gbc.insets.left = 10;
        add(openInQueryEditor, gbc);
        gbc.gridy++;
        gbc.insets.left = 0;
        add(createFileOutputPanel(), gbc);

        gbc.gridy++;
        gbc.insets.top = 10;
        gbc.insets.bottom = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        
        if (parent.getScriptType() == GenerateScriptsWizard.CREATE_TABLES) {

            createTableOptionsPanel();
            add(createTableOptionsPanel, gbc);

        } else {
          
            createUseCascadeCheck();
            useCascadeCheck.setBorder(BorderFactory.createEmptyBorder(0, 13, 0, 0));
            add(useCascadeCheck, gbc);
        }

    }

    private JPanel createFileOutputPanel() {

        pathField = WidgetFactory.createTextField();

        final JButton browseButton = new DefaultPanelButton("Browse");
        browseButton.setMnemonic('B');
        browseButton.addActionListener(this);

        final DefaultFieldLabel label = new DefaultFieldLabel("Save Path:");

        writeToFileCheck = new JCheckBox("Write to file");
        writeToFileCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean enable = writeToFileCheck.isSelected();
                browseButton.setEnabled(enable);
                pathField.setEnabled(enable);
                label.setEnabled(enable);
            }
        });

        browseButton.setEnabled(false);
        pathField.setEnabled(false);
        label.setEnabled(false);

        ComponentTitledPanel panel = new ComponentTitledPanel(writeToFileCheck);
        JPanel fileOutputPanel = panel.getContentPane();
        fileOutputPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx++;
        gbc.gridy++;
        gbc.insets = new Insets(7,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        fileOutputPanel.add(label, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fileOutputPanel.add(pathField, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets.top = 3;
        gbc.insets.right = 5;
        fileOutputPanel.add(browseButton, gbc);
        
        return panel;
    }

    private void createUseCascadeCheck() {

        useCascadeCheck = new JCheckBox("Use CASCADE in DROP");
    }

    private void createTableOptionsPanel() {

        constraintsCheck = new JCheckBox("Include constraints");
        consAsAlterCheck = new JCheckBox("As ALTER TABLE statements", true);
        consInCreateCheck = new JCheckBox("Within CREATE TABLE statements");

        constraintsCheck.addItemListener(this);

        ButtonGroup bg = new ButtonGroup();
        bg.add(consAsAlterCheck);
        bg.add(consInCreateCheck);

        consInCreateCheck.setEnabled(false);
        consAsAlterCheck.setEnabled(false);

        createTableOptionsPanel = new ComponentTitledPanel(constraintsCheck);
        JPanel _panel = createTableOptionsPanel.getContentPane();
        _panel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));
        _panel.add(consAsAlterCheck);
        _panel.add(consInCreateCheck);
    }
    
    public void panelSelected() {
        
        if (parent.getScriptType() == GenerateScriptsWizard.CREATE_TABLES) {

            if (createTableOptionsPanel == null) {
                
                createTableOptionsPanel();
            }
            
            resetOptionsComponents(createTableOptionsPanel, useCascadeCheck);

        } else {
            
            if (useCascadeCheck == null) {
                
                createUseCascadeCheck();
            }
            
            resetOptionsComponents(useCascadeCheck, createTableOptionsPanel);
        }

    }

    private void resetOptionsComponents(Component componentToAdd, Component componentToRemove) {
        
        if (!contains(componentToAdd)) {
            
            remove(componentToRemove);
            add(componentToAdd, gbc);
        }

    }
    
    private boolean contains(Component component) {
        
        Component[] components = getComponents();
        for (Component c : components) {
            
            if (c == component) {
                
                return true;
            }
            
        }

        return false;
    }
    
    /**
     * Whether to include the CASCADE keyword within DROP statments.
     *
     * @return true | false
     */
    protected boolean cascadeWithDrop() {
        if (parent.getScriptType() == GenerateScriptsWizard.DROP_TABLES) {
            return useCascadeCheck.isSelected();
        }
        return false;
    }

    /**
     * Returns the constraints definition format - 
     * as ALTER TABLE statements or within the CREATE TABLE statements.
     */
    protected int getConstraintsStyle() {
        if (parent.getScriptType() == GenerateScriptsWizard.CREATE_TABLES) {

            if (constraintsCheck.isSelected()) {

                if (consAsAlterCheck.isSelected()) {
                    return GenerateScriptsWizard.ALTER_TABLE_CONSTRAINTS;
                } else {
                    return GenerateScriptsWizard.CREATE_TABLE_CONSTRAINTS;
                }

            }
            
        }
        return -1;
    }

    protected boolean hasOutputStrategy() {

        return openInQueryEditor() || (isWritingToFile() && hasOutputFile()); 
    }
    
    protected boolean openInQueryEditor() {
     
        return openInQueryEditor.isSelected();
    }
    
    protected boolean isWritingToFile() {
        
        return writeToFileCheck.isSelected();
    }
    
    protected boolean hasOutputFile() {
        
        return StringUtils.isNotBlank(getOutputFilePath());
    }
    
    /**
     * Returns the output file path.
     *
     * @return the output file path
     */
    protected String getOutputFilePath() {
        return pathField.getText();
    }

    
    public void actionPerformed(ActionEvent e) {
        FileSelector textFiles = new FileSelector(new String[] {"txt"}, "Text files");
        FileSelector sqlFiles = new FileSelector(new String[] {"sql"}, "SQL files");
        
        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(textFiles);
        fileChooser.addChoosableFileFilter(sqlFiles);
        
        fileChooser.setDialogTitle("Select File...");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        
        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        String path = fileChooser.getSelectedFile().getAbsolutePath();
        if (!path.toUpperCase().endsWith(".SQL")) {
            path += ".sql";
        }
        pathField.setText(path);
    }

    /**
     * Invoked when the include constraints checkbox has 
     * been selected/deselected.
     */    
    public void itemStateChanged(ItemEvent e) {
        enableConstraintChecks(e.getStateChange() == ItemEvent.SELECTED);
    }

    /** 
     * Enables/disables the constraints check boxes as specified.
     */
    private void enableConstraintChecks(boolean enable) {
        consAsAlterCheck.setEnabled(enable);
        consInCreateCheck.setEnabled(enable);
    }

}






