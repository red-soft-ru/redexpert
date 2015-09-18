/*
 * ExportAsSQLPanelOne.java
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.underworldlabs.swing.MultiLineLabel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
class ExportAsSQLPanelOne extends AbstractImportExportPanel  {
    
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

    public ExportAsSQLPanelOne(ImportExportWizard parent) {
        
        super(new GridBagLayout(), parent);
        
        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
    
    /** <p>Initialises the state of this instance. */
    private void jbInit() throws Exception {

        singleRadio = new JRadioButton("Single Table");
        singleRadio.setMnemonic('S');

        multipleRadio = new JRadioButton("Multiple Tables");
        multipleRadio.setMnemonic('M');
        
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(singleRadio);
        buttonGroup1.add(multipleRadio);
        singleRadio.setSelected(true);

        singleFileRadio = new JRadioButton("One file for all tables");
        multipleFileRadio = new JRadioButton("One file per table");
        
        ButtonGroup buttonGroup2 = new ButtonGroup();
        buttonGroup2.add(singleFileRadio);
        buttonGroup2.add(multipleFileRadio);
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
        
        // combo boxes
        connectionsCombo = importExportWizard().getConnectionsCombo();

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
        add(new MultiLineLabel(getString("ExportAsSQLPanelOne.exportTip")), gbc);
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
        
    }
    
    public ImportExportType getExportType() {
        
        if (singleRadio.isSelected()) {
            
            return ImportExportType.EXPORT_SQL_ONE_TABLE;
            
        } else {

            return ImportExportType.EXPORT_SQL_ALL_TABLES;
        }
    }
    
    public ImportExportFileType getExportFileType() {

        if (singleFileRadio.isSelected()) {
            
            return ImportExportFileType.SINGLE_FILE;

        } else {

            return ImportExportFileType.MULTIPLE_FILES;
        }
    }
    
}






