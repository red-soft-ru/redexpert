/*
 * ImportXMLPanel_4.java
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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.executequery.GUIUtilities;
import org.executequery.gui.FormPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.MultiLineLabel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ImportXMLPanel_4 extends JPanel {
    
    private ImportExportXMLPanel parent;
    
    private JTextField tableTagField;
    private JTextField rowTagField;
    
    private JCheckBox isAttCheck;
    private JCheckBox isNameCheck;
    
    private JLabel attNameLabel;
    
    public ImportXMLPanel_4(ImportExportXMLPanel parent) {
        super(new GridBagLayout());
        this.parent = parent;
        
        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void jbInit() throws Exception {
        // fields with default values
        tableTagField = WidgetFactory.createTextField("table,name");
        rowTagField = WidgetFactory.createTextField("row");
        
        JButton sampleButton = new JButton("Sample");
        sampleButton.setMargin(new Insets(2,2,2,2));
        sampleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SampleDialog(); }
        });
        
        isAttCheck = new JCheckBox(
                            "Table name is an attribute (include tag and attribute names comma-separated)",
                            true);
        
        isNameCheck = new JCheckBox("Table name is the tag");
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(isAttCheck);
        bg.add(isNameCheck);
        
        ActionListener checkListen = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableAttributeEntry(); }
        };
        
        isAttCheck.addActionListener(checkListen);
        isNameCheck.addActionListener(checkListen);
        
        attNameLabel = new JLabel("Tag/Attribute Name:");
        
        StringBuffer sb = new StringBuffer();
        sb.append("In order to correctly process the records within the XML data ").
        append("file, those elements denoting table rows and table names (for ").
        append("multiple table imports) are required. Column values are to be ").
        append("identified by their tags alone - that is, the name of the tag ").
        append("is expected to be the name of the column that the value is to be ").
        append("inserted into.");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Identify the primary nodes."), gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        add(new MultiLineLabel(sb.toString()), gbc);
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets.top = 10;
        add(new JLabel("Click the [Sample] button for an example of what is expected."), gbc);
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.insets.top = 5;
        add(sampleButton, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(new JLabel("Table Tag (required for multiple table import):"), gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        add(isAttCheck, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets.left = 20;
        gbc.weightx = 0;
        add(attNameLabel, gbc);
        gbc.gridx = 1;
        gbc.insets.left = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(tableTagField, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.left = 5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(isNameCheck, gbc);
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.insets.top = 10;
        gbc.insets.left = 5;
        add(new JLabel("Row Tag:"), gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.gridwidth = 1;
        gbc.insets.left = 20;
        gbc.weighty = 1.0;
        add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.left = 0;
        gbc.gridwidth = 2;
        add(rowTagField, gbc);

        setPreferredSize(parent.getChildDimension());
    }
    
    /** <p>Determines whether all required information
     *  has been entered correctly prior to moving to the
     *  next panel in the wizard.
     *
     *  @return <code>true</code> if all information has been
     *          entered | <code>false</code> otherwise
     */
    public boolean entriesComplete() {
        String rowTag = rowTagField.getText();
        String tableTag = tableTagField.getText();
        
        if (isAttCheck.isSelected()) {
            return rowTag != null &&
            rowTag.length() > 0 &&
            tableTag != null &&
            tableTag.length() > 0 &&
            tableTag.indexOf(',') != -1;
        }        
        else {
            return rowTag != null && rowTag.length() > 0;
        }
        
    }
    
    /** <p>Returns whether the table name is the
     *  tag itself - ie. <table_name>. This will be
     *  denoted by selection of the checkbox 'Table name
     *  is the tag'.
     *
     *  @return <code>true</code> if the name is the tag |
     *          <code>false</code> otherwise
     */
    public boolean isTableNameTagOnly() {
        return isNameCheck.isSelected();
    }
    
    /** <p>Retrives the row tag label as entered.
     *
     *  @return the row tag
     */
    public String getRowTagString() {
        return rowTagField.getText();
    }
    
    public String getTableTagString() {
        if (isNameCheck.isSelected())
            return parent.getTableName();
        else
            return tableTagField.getText();
    }
    
    public boolean hasRowTag() {
        return rowTagField.getText().length() > 0;
    }
    
    public boolean hasTableNameAsAttribute() {
        return isAttCheck.isSelected();// tableTagField.getText().length() > 0;
    }
    
    public String getTagInfoString() {
        
        if (hasTableNameAsAttribute() && !hasRowTag())
            return tableTagField.getText();
        else if (hasRowTag() && !hasTableNameAsAttribute())
            return rowTagField.getText();
        else if (hasTableNameAsAttribute() && hasRowTag())
            return tableTagField.getText() + " | " + rowTagField.getText();
        else
            return "";
    }
    
    private void enableAttributeEntry() {
        boolean enable = isAttCheck.isSelected();
        
        attNameLabel.setEnabled(enable);
        tableTagField.setEnabled(enable);
        tableTagField.setEditable(enable);
        tableTagField.setOpaque(enable);
        
    }
    
    static class SampleDialog extends AbstractBaseDialog 
                              implements ActionListener {
        
        public SampleDialog() {

            super((Dialog) GUIUtilities.getInFocusDialogOrWindow(), "Import XML Sample", true);
            
            StringBuffer sb = new StringBuffer(100);
            sb.append(" <?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n   ").
            append(" <table name=\"employees\">\n\n       <row rownum=\"1\">\n").
            append("          <employee_id>1234</employee_id>\n").
            append("          <first_name>John</first_name>\n").
            append("          <second_name>Smith</second_name>\n").
            append("          <department>001</department>\n").
            append("          <start_date>20020506</start_date>\n").
            append("          <salary>55000</salary>\n").
            append("       </row>\n       ... ...");
            
            JTextArea sampleArea = new JTextArea(sb.toString());
            JScrollPane scroller = new JScrollPane(sampleArea);
            scroller.setPreferredSize(new Dimension(400, 250));
            sampleArea.setSelectionColor(Color.WHITE);
            sampleArea.setEditable(false);
            sampleArea.setFont(new Font("monospaced", Font.PLAIN, 12));
            
            JButton closeButton = new FormPanelButton(Bundles.get("common.close.button"));
            closeButton.addActionListener(this);

            Container c = this.getContentPane();
            c.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weighty = 1.0;
            gbc.insets = new Insets(5,5,5,5);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            c.add(scroller, gbc);
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets.top = 0;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 1;
            c.add(closeButton, gbc);
            
            pack();
            this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
            setVisible(true);
        }
        
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
        
    } // class SampleDialog
    
}

















