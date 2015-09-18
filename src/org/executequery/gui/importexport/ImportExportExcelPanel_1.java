/*
 * ImportExportExcelPanel_1.java
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.DynamicComboBoxModel;


/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ImportExportExcelPanel_1 extends JPanel  {
    
    /** single table transfer radio button */
    private JRadioButton singleRadio;
    
    /** multiple table transfer radio button */
    private JRadioButton multipleRadio;
    
    /** multiple table single file transfer radio button */
    private JRadioButton singleFileRadio;
    
    /** multiple table multiple file transfer radio button */
    private JRadioButton multipleFileRadio;
    
    /** The connection combo selection */
    private JComboBox connectionsCombo; 

    /** the schema combo box model */
    private DynamicComboBoxModel connectionsModel;

    /** The parent controller for this process */
    private ImportExportProcess parent;
    
    /** <p>Creates a new instance with the specified parent
     *  object as the controller
     *
     *  @param the parent object
     */
    public ImportExportExcelPanel_1(ImportExportProcess parent) {
        super(new GridBagLayout());
        this.parent = parent;
        
        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /** <p>Initialises the state of this instance. */
    private void jbInit() throws Exception {
        singleRadio = new JRadioButton("Single Table");
        multipleRadio = new JRadioButton("Multiple Tables");
        
        singleRadio.setMnemonic('S');
        multipleRadio.setMnemonic('M');
        
        ButtonGroup bg1 = new ButtonGroup();
        bg1.add(singleRadio);
        bg1.add(multipleRadio);
        singleRadio.setSelected(true);
        
        singleFileRadio = new JRadioButton("One file for all tables");
        multipleFileRadio = new JRadioButton("One file per table");
        
        ButtonGroup bg2 = new ButtonGroup();
        bg2.add(singleFileRadio);
        bg2.add(multipleFileRadio);
        singleFileRadio.setSelected(true);
        
        singleFileRadio.setEnabled(false);
        multipleFileRadio.setEnabled(false);
        
        final JLabel typeLabel = new JLabel("Select multiple table transfer type.");
        typeLabel.setEnabled(false);
        
        ActionListener radioListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                singleFileRadio.setEnabled(multipleRadio.isSelected());
                multipleFileRadio.setEnabled(multipleRadio.isSelected());
                typeLabel.setEnabled(multipleRadio.isSelected());
            }
        };
        singleRadio.addActionListener(radioListener);
        multipleRadio.addActionListener(radioListener);
        
        StringBuilder sb = new StringBuilder(500);
        
        int type = parent.getTransferType();
        if (type == ImportExportProcess.EXPORT) {
            sb.append("Single table export retrieves requested data from one ").
            append("table only. This will also allow for the selection of individual ").
            append("columns from that table.\n\nSelecting a multiple table export ").
            append("does not allow for individual column selection and all ").
            append("columns within selected tables are exported. A multiple table ").
            append("export also allows for a single file for all tables within separate ").
            append("sheets of the generated spreadsheet file.");
        } else if (type == ImportExportProcess.IMPORT) {
            sb.append("Single table import inserts data into one table only.").
            append(" This will also allow for the selection of individual ").
            append("columns from that table.\n\nSelecting a multiple table import ").
            append("does not allow for individual column selection and all ").
            append("columns within selected tables are assumed to be held within ").
            append("separate sheets of the\nselected Excel file.");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSelectionColor(getBackground());
        textArea.setSelectedTextColor(Color.BLACK);
        
        // combo boxes
        Vector connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets = new Insets(7,10,5,10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Connection:"), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.insets.top = 5;
        add(connectionsCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        add(textArea, gbc);
        gbc.insets.left = 20;
        gbc.gridy++;
        add(new JLabel("Select single or multiple table transfer."), gbc);
        gbc.insets.top = 0;
        gbc.insets.left = 40;
        gbc.gridy++;
        add(singleRadio, gbc);
        gbc.gridy++;
        add(multipleRadio, gbc);
        gbc.insets.left = 20;
        gbc.gridy++;
        add(typeLabel, gbc);
        gbc.insets.left = 40;
        gbc.gridy++;
        add(singleFileRadio, gbc);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy++;
        add(multipleFileRadio, gbc);
        
        setPreferredSize(parent.getChildDimension());
        
    }
    
    /** <p>Returns the type of transfer - single or
     *  multiple table.
     *
     *  @return the type of transfer
     */
    public int getTableTransferType() {
        if (singleRadio.isSelected())
            return ImportExportProcess.SINGLE_TABLE;
        else
            return ImportExportProcess.MULTIPLE_TABLE;
    }
    
    /** <p>Returns the type of multiple table
     *  transfer - single or multiple file.
     *
     *  @return the type of multiple table transfer
     */
    public int getMutlipleTableTransferType() {
        
        if (singleFileRadio.isSelected())
            return ImportExportProcess.SINGLE_FILE;
        
        else
            return ImportExportProcess.MULTIPLE_FILE;
        
    }
    
    /**
     * Returns the selected database connection properties object.
     *
     * @return the connection properties object
     */
    public DatabaseConnection getDatabaseConnection() {
        return (DatabaseConnection)connectionsCombo.getSelectedItem();
    }

    /**
     * Sets the connection selection to that specified.
     *
     * @param dc - the connection to select
     */
    public void setDatabaseConnection(DatabaseConnection dc) {
        connectionsCombo.setSelectedItem(dc);
    }

}














