/*
 * ExportXMLPanel_4.java
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

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.underworldlabs.swing.MultiLineLabel;

import org.executequery.gui.importexport.ImportExportXMLPanel;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1767 $
 * @date     $Date: 2017-08-16 22:26:50 +1000 (Wed, 16 Aug 2017) $
 */
public class ExportXMLPanel_4 extends JPanel  {
    
    private ImportExportXMLPanel parent;
    
    private JTextArea sampleArea;
    
    private JRadioButton schemaRadio;
    private JRadioButton tableRadio;
    
    private String schemaSample;
    private String tableSample;
    
    public ExportXMLPanel_4(ImportExportXMLPanel parent) {
        super(new GridBagLayout());
        this.parent = parent;
        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void jbInit() throws Exception {
        sampleArea = new JTextArea();
        JScrollPane scroller = new JScrollPane(sampleArea);
        scroller.setPreferredSize(new Dimension(475, 120));
        sampleArea.setSelectionColor(Color.WHITE);
        
        sampleArea.setEditable(false);
        sampleArea.setFont(new Font("monospaced", 0, 12));
        
        schemaRadio = new JRadioButton("Schema details (includes connection information" +
        " as attributes)");
        tableRadio = new JRadioButton("Table name only");
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(schemaRadio);
        bg.add(tableRadio);
        
        schemaRadio.setSelected(true);
        
        ActionListener radioListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setSampleArea(); }
        };
        
        schemaRadio.addActionListener(radioListener);
        tableRadio.addActionListener(radioListener);
        
        String line_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        String line_2 = "\n<schema name=\"schema_name\" jdbcurl=\"jdbc:oracle:thin:@" +
                        "[server]:[port]:[source]\" user=\"username\">";
        String line_3 = "\n   <table name=\"table_name\">\n";
        String line_4 = "      <row rownum=\"1\">\n         <column_name>3000</column_name>" +
                        "\n          ...";
        
        schemaSample = line_1 + line_2 + line_3 + line_4;
        tableSample = line_1 + line_3 + line_4;
        
        StringBuffer sb = new StringBuffer();
        sb.append("Selecting schema includes table nodes and is the only ");
        sb.append("available option for a multiple table export using a single XML file.");
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy++;
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Select the XML root element."), gbc);
        gbc.insets.top = 0;
        gbc.insets.bottom = 0;
        gbc.gridy++;
        add(new MultiLineLabel(sb.toString()), gbc);
        gbc.gridy++;
        gbc.insets.top = 5;
        gbc.insets.left = 20;
        add(schemaRadio, gbc);
        gbc.insets.top = 7;
        gbc.gridy++;
        add(tableRadio, gbc);
        gbc.gridy++;
        gbc.insets.left = 10;
        add(new JLabel("Sample:"), gbc);
        gbc.gridy++;
        gbc.insets.left = 20;
        gbc.insets.right = 20;
        gbc.insets.bottom = 20;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(scroller, gbc);
        
        setPreferredSize(parent.getChildDimension());
        
        setSelectedRadios();
        setSampleArea();
    }
    
    public void setSelectedRadios() {
        if (parent.getTableTransferType() == ImportExportDataProcess.MULTIPLE_TABLE) {
            schemaRadio.setSelected(true);
            
            if (parent.getMutlipleTableTransferType() == ImportExportDataProcess.SINGLE_FILE)
                tableRadio.setEnabled(false);
            else
                tableRadio.setEnabled(true);
            
        } else
            tableRadio.setEnabled(true);
    }
    
    public void setSampleArea() {
        if (schemaRadio.isSelected()) {
            sampleArea.setText(schemaSample);
        } else {
            sampleArea.setText(tableSample);
        }
    }
    
    public int getSelection() {
        if (schemaRadio.isSelected()) {
            return ImportExportDataProcess.SCHEMA_ELEMENT;
        } else {
            return ImportExportDataProcess.TABLE_ELEMENT;
        }
    }
    
}














