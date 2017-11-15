/*
 * ErdScriptGenerator.java
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

package org.executequery.gui.erd;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.executequery.GUIUtilities;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.scriptgenerators.BaseScriptGeneratorPanel;
import org.executequery.gui.scriptgenerators.CreateTableScriptsGenerator;
import org.executequery.gui.scriptgenerators.ScriptGenerator;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.AbstractBaseDialog;

/**
 *
 * @author   Takis Diakoumis
 */
@SuppressWarnings({"rawtypes"})
public class ErdScriptGenerator extends BaseScriptGeneratorPanel
                                implements ScriptGenerator {
    
    private static final int DIALOG_WIDTH = 790;
    private static final int DIALOG_HEIGHT = 500;

    /** The parent process */
    private ErdViewerPanel parent;
    
    /** The cancel button */
    private JButton cancelButton;
    
    /** The generate button */
    private JButton generateButton;
    
    /** The tables to generate scripts for as an array */
    private ErdTable[] tables;
    
    /** The dialog container */
    private ErdScriptGeneratorDialog dialog;
    
    public ErdScriptGenerator(Vector _tables, ErdViewerPanel parent) {
        super(_tables);
        
        this.parent = parent;
        
        int v_size = _tables.size();
        tables = parent.getAllComponentsArray();
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private void jbInit() throws Exception {

        cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));
        generateButton = new DefaultPanelButton(Bundles.get("common.generate.button"));
        
        Insets btnInsets = new Insets(0,0,0,0);
        cancelButton.setMargin(btnInsets);
        generateButton.setMargin(btnInsets);
        
        ActionListener buttonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttons_actionPerformed(e);
            }
        };
        
        cancelButton.addActionListener(buttonListener);
        generateButton.addActionListener(buttonListener);
        
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,7,7);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0;
        buttonPanel.add(generateButton, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        buttonPanel.add(cancelButton, gbc);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // assign the connection (may be null)
        metaData.setDatabaseConnection(parent.getDatabaseConnection());
        
        dialog = new ErdScriptGeneratorDialog(this);
        dialog.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        dialog.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {

                int width = getWidth();
                int height = getHeight();
                
                if (width < DIALOG_WIDTH)
                    width = DIALOG_WIDTH;
                
                if (height < DIALOG_HEIGHT)
                    height = DIALOG_HEIGHT;
                
                setSize(width, height);
            }
        });

        dialog.display();
        
    }
    
    public void dispose() {        
        dialog.dispose();
    }
    
    public String getScriptFilePath() {
        return pathField.getText();
    }
    
    public void setResult(int result) {
        
    }
    
    public ColumnData[] getColumnDataArray(String tableName) {
        
        ColumnData[] columnData = null;
        
        for (int i = 0; i < tables.length; i++) {
            
            if (tables[i].getTableName().equals(tableName)) {
                columnData = tables[i].getTableColumns();
                break;
            }
            
        }
        
        return columnData;
        
    }
    
    public Vector getSelectedTables() {
        return listPanel.getSelectedValues();
    }
    
    public boolean hasSelectedTables() {
        return listPanel.hasSelections();
    }
    
    public boolean includeConstraintsInCreate() {
        return consInCreateCheck.isSelected();
    }
    
    public boolean includeConstraints() {
        return constraintsCheck.isSelected();
    }
    
    public boolean includeConstraintsAsAlter() {
        return consAsAlterCheck.isSelected();
    }
    
    public void enableButtons(boolean enable) {
        cancelButton.setEnabled(enable);
        generateButton.setEnabled(enable);
    }
    
    public String getDatabaseProductName() {
        if (parent.getDatabaseConnection() == null) {
            return "Not Available";
        }
        try {
            return metaData.getDatabaseProductName();
        }
        catch (DataSourceException e) {
            return "Not Available";
        }
    }
    
    public String getSchemaName() {
        if (parent.getDatabaseConnection() == null) {
            return "Not Available";
        }
        return metaData.getSchemaName().toUpperCase();
    }
    
    private void buttons_actionPerformed(ActionEvent e) {
        Object button = e.getSource();
        
        if (button == cancelButton) {
            dispose();
        }
        else if (button == generateButton) {
            
            if (hasRequiredFields()) {
                CreateTableScriptsGenerator generator =
                                                new CreateTableScriptsGenerator(this);
                generator.generate();
            }

        }
        
    }
    
    
    class ErdScriptGeneratorDialog extends AbstractBaseDialog {
        
        public ErdScriptGeneratorDialog(ErdScriptGenerator _parent) {

            super(GUIUtilities.getParentFrame(), "Generate Scripts", true);
            
            Container c = this.getContentPane();
            c.setLayout(new BorderLayout());
            c.add(_parent, BorderLayout.CENTER);
            
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }
        
        protected void display() {
            pack();
            setLocation(GUIUtilities.getLocationForDialog(getSize()));
            setVisible(true);
        }
        
    } // class ErdScriptGeneratorDialog
    
}







