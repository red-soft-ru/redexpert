/*
 * ImportExportDelimitedPanel_1.java
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
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.MultiLineLabel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ImportExportDelimitedPanel_1 extends JPanel {
    
    /** Single table export radio button */
    private JRadioButton singleRadio;

    /** Multiple table export radio button */
    private JRadioButton multipleRadio;
    
    /** The connection combo selection */
    private JComboBox connectionsCombo; 

    /** the schema combo box model */
    private DynamicComboBoxModel connectionsModel;

    /** The controlling object for this process */
    private ImportExportProcess parent;
    
    /** <p>Creates a new instance with the specified
     *  process as the parent.
     *
     *  @param the parent controlling the process
     */
    public ImportExportDelimitedPanel_1(ImportExportProcess parent) {
        super(new GridBagLayout());
        this.parent = parent;
        
        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /** <p>Initialises the state of this instance and
     *  lays out components on the panel. */
    private void jbInit() throws Exception {
        singleRadio = new JRadioButton("Single Table");
        multipleRadio = new JRadioButton("Multiple Tables");
        
        singleRadio.setMnemonic('S');
        multipleRadio.setMnemonic('M');
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(singleRadio);
        bg.add(multipleRadio);
        singleRadio.setSelected(true);
        
        StringBuffer sb = new StringBuffer(500);

        int type = parent.getTransferType();
        if (type == ImportExportProcess.EXPORT) {
            sb.append("Single table export retrieves requested data from one ").
            append("table only. This will also allow for the selection of individual ").
            append("columns from that table.\n\nSelecting a multiple table export ").
            append("does not allow for individual column selection and all ").
            append("columns within selected tables are exported.\n");
        } 
        else if (type == ImportExportProcess.IMPORT) {
            sb.append("Single table import inserts data into one table only.").
            append(" This will also allow for the selection of individual ").
            append("columns from that table.\n\nSelecting a multiple table import ").
            append("does not allow for individual column selection and all ").
            append("columns within selected tables are assumed to be held within the ").
            append("file or files selected.");
        }
        
        // combo boxes
        Vector connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets = new Insets(7,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Connection:"), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.insets.top = 5;
        add(connectionsCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        add(new MultiLineLabel(sb.toString()), gbc);
        gbc.insets.left = 20;
        gbc.gridy++;
        add(new JLabel("Select single or multiple table data transfer."), gbc);
        gbc.insets.top = 0;
        gbc.insets.left = 40;
        gbc.gridy++;
        add(singleRadio, gbc);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(multipleRadio, gbc);
        
        setPreferredSize(parent.getChildDimension());
    }
    
    /** 
     * Returns the type of import/export process
     * to be conducted - single or multiple table.
     *
     * @return the type of process
     */
    public int getSelection() {
        if (singleRadio.isSelected()) {
            return ImportExportProcess.SINGLE_TABLE;
        } else {
            return ImportExportProcess.MULTIPLE_TABLE;
        }
    }
    
    /**
     * Sets the connection selection to that specified.
     *
     * @param dc - the connection to select
     */
    public void setDatabaseConnection(DatabaseConnection dc) {
        connectionsCombo.setSelectedItem(dc);
    }
    
    /**
     * Returns the selected database connection properties object.
     *
     * @return the connection properties object
     */
    public DatabaseConnection getDatabaseConnection() {
        return (DatabaseConnection)connectionsCombo.getSelectedItem();
    }

}














