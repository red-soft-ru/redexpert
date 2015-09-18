/*
 * ImportExportXMLPanel_6.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.executequery.gui.importexport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.executequery.gui.DefaultList;
import org.executequery.gui.WidgetFactory;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ImportExportXMLPanel_6 extends JPanel {
    
    private ImportExportXMLPanel parent;
    
    private JList columnsList;
    
    private JLabel tableLabel;
    private JLabel selectedLabel;
    
    private JTextField typeField;
    private JTextField pathField;
    private JTextField formatField;
    private JTextField tableField;
    
    public ImportExportXMLPanel_6(ImportExportXMLPanel parent) {
        super(new GridBagLayout());
        this.parent = parent;
        try {
            init();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {

        typeField = WidgetFactory.createTextField();
        pathField = WidgetFactory.createTextField();
        formatField = WidgetFactory.createTextField();
        tableField = WidgetFactory.createTextField();
        
        setFieldProperties(typeField);
        setFieldProperties(pathField);
        setFieldProperties(formatField);
        setFieldProperties(tableField);
        
        tableLabel = new JLabel("Table Name:");
        selectedLabel = new JLabel();
        
        JLabel typeLabel;
        JLabel pathLabel;
        JLabel formatLabel;
        
        if (parent.getTransferType() == ImportExportXMLPanel.EXPORT) {
            typeLabel = new JLabel("Export Type:");
            pathLabel = new JLabel("Export Files:");
            formatLabel = new JLabel("XML Format:");
        } else {
            typeLabel = new JLabel("Import Type:");
            pathLabel = new JLabel("Import Files:");
            formatLabel = new JLabel("Primary Nodes:");
        }
        
        columnsList = new DefaultList();
        JScrollPane scroller = new JScrollPane(columnsList);
        scroller.setPreferredSize(new Dimension(475, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(5,10,5,5);
        gbc.insets = ins;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        this.add(typeLabel, gbc);
        gbc.gridy = 1;
        this.add(pathLabel, gbc);
        gbc.gridy = 2;
        this.add(formatLabel, gbc);
        gbc.gridy = 3;
        this.add(tableLabel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 4;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        this.add(selectedLabel, gbc);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 5;
        gbc.insets.top = 0;
        gbc.insets.right = 20;
        gbc.insets.left = 20;
        gbc.weighty = 1.0;
        this.add(scroller, gbc);
        gbc.gridy = 6;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        this.add(new JLabel("Click the Next button to begin."), gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets.top = 5;
        gbc.insets.right = 10;
        gbc.insets.left = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        this.add(typeField, gbc);
        gbc.gridy = 1;
        this.add(pathField, gbc);
        gbc.gridy = 2;
        this.add(formatField, gbc);
        gbc.gridy = 3;
        this.add(tableField, gbc);
        
        this.setPreferredSize(parent.getChildDimension());
        
    }
    
    public void setValues() {

        String typeString = null;
        int transferType = parent.getTransferType();

        if (parent.getTableTransferType() == ImportExportProcess.MULTIPLE_TABLE) {
            selectedLabel.setText("Selected Tables:");
            tableLabel.setEnabled(false);
            
            typeString = transferType == ImportExportProcess.EXPORT ?
                        "Multiple table export - " : "Multiple table import - ";
            
            columnsList.setListData(parent.getSelectedTables());
            
        } 
        else if (parent.getTableTransferType() == ImportExportProcess.SINGLE_TABLE) {
            tableField.setText(parent.getTableName());
            selectedLabel.setText("Selected Columns:");
            tableLabel.setEnabled(true);
            
            typeString = transferType == ImportExportProcess.IMPORT ?
                        "Single table import - " : "Single table export - ";

            columnsList.setListData(parent.getSelectedColumns());
        }
        
        
        if (parent.getMutlipleTableTransferType() == 
                ImportExportProcess.SINGLE_FILE) {
            typeString += "single file";
        } else {
            typeString += "multiple file";
        }

        typeField.setText(typeString);

        // set the file names to display
        Vector transfers = parent.getDataFileVector();
        StringBuffer sb = new StringBuffer();
        char COLON = ';';

        for (int i = 0, k = transfers.size(); i < k; i++) {
            DataTransferObject obj = (DataTransferObject)transfers.get(i);
            File file = new File(obj.getFileName());
            sb.append(file.getName());

            if (i != k - 1) {
                sb.append(COLON);
            }

        }

        pathField.setText(sb.toString());

        formatField.setText(parent.getTransferType() == ImportExportXMLPanel.EXPORT ?
            parent.getXMLFormatString() : parent.getPrimaryImportNodes());

//        columnsList.setListData(parent.getSelectedColumns());
    }
    
    private void setFieldProperties(JTextField field) {
        field.setEditable(false);
        field.setOpaque(false);
        field.setDisabledTextColor(Color.BLACK);
        field.setSelectionColor((Color)UIManager.get("Panel.background"));
        
        field.setPreferredSize(new Dimension((int)field.getSize().getWidth(), 20));
    }
    
}

















