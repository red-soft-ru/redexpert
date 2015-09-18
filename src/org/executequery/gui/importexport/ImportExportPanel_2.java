/*
 * ImportExportPanel_2.java
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.ListSelectionPanel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ImportExportPanel_2 extends JPanel
                                 implements ItemListener {
    
    /** The table label */
    private JLabel tableLabel;
    
    /** The table selection combo box */
    private JComboBox tableCombo;
    
    /** the schema combo box model */
    private DynamicComboBoxModel tableSelectionModel;

    private boolean useCatalogs;
    
    /** The object to retrieve table details */
    private MetaDataValues metaData;
    
    /** The list table/column list selection panel */
    private ListSelectionPanel list;
    
    /** The selected transfer type - single/multiple tables */
    private int selectedTransferType;
    
    /** The schema combo box */
    private JComboBox schemaCombo;
    
    /** The schema combo box model */
    private DynamicComboBoxModel schemaSelectionModel;
    
    /** The controlling object for this process */
    private ImportExportProcess parent;
    
    public ImportExportPanel_2(ImportExportProcess parent) {
        super(new GridBagLayout());
        
        this.parent = parent;
        
        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void jbInit() throws Exception {
        selectedTransferType = -1;        
        metaData = parent.getMetaDataUtility();
        
        Vector schemas = metaData.getHostedSchemasVector();
        if (schemas == null || schemas.size() == 0) {
            useCatalogs = true;
            schemas = metaData.getHostedCatalogsVector();
        }

        schemaSelectionModel = new DynamicComboBoxModel(schemas);
        schemaCombo = WidgetFactory.createComboBox(schemaSelectionModel);
        schemaCombo.addItemListener(this);

        tableSelectionModel = new DynamicComboBoxModel();
        tableCombo = WidgetFactory.createComboBox(tableSelectionModel);
        tableCombo.addItemListener(this);

        tableLabel = new JLabel("Table:");
        list = new ListSelectionPanel();
        setListData(parent.getTableTransferType());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Schema:"), gbc);
        gbc.gridy = 1;
        gbc.insets.top = 3;
        add(tableLabel, gbc);
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.insets.top = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(schemaCombo, gbc);
        gbc.gridy = 1;
        gbc.insets.bottom = 0;
        gbc.insets.top = 0;
        add(tableCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(list, gbc);

        setPreferredSize(parent.getChildDimension());
    }
    
    public String getSelectedSchema() {
        Object schema = schemaCombo.getSelectedItem();
        if (schema != null) {
            return schema.toString();
        }
        else {
            return Constants.EMPTY;
        }
    }
    
    /** the last selected database connection */
    private DatabaseConnection databaseConnection;

    public void setListData(int transferType) {
        boolean reload = false;
        try {
            tableCombo.removeItemListener(this);
            schemaCombo.removeItemListener(this);
            
            // check if we need to reload the meta data
            // based on the selected connection
            if (databaseConnection == null || 
                    databaseConnection != parent.getDatabaseConnection()) {
                reload = true;
                databaseConnection = parent.getDatabaseConnection();
            }

            // reload schema combo if connection has changed
            if (reload) {

                // clear the lists and any selections
                list.clear();

                // clear the table values and disable the box
                tableSelectionModel.removeAllElements();
                tableCombo.setEnabled(false);

                // clear the schema values
                schemaSelectionModel.removeAllElements();

                Vector schemas = null;
                try {
                    schemas = metaData.getHostedSchemasVector();
                    if (schemas == null || schemas.isEmpty()) {
                        useCatalogs = true;
                        schemas = metaData.getHostedCatalogsVector();
                    } else {
                        useCatalogs = false;
                    }
                }
                catch (DataSourceException e) {
                    GUIUtilities.displayExceptionErrorDialog(
                            "Error retrieving the catalog/schema names for " +
                            "the current connection.\n\nThe system returned:\n" + 
                            e.getExtendedMessage(), e);
                    schemas = new Vector<String>(0);
                }

                if (schemas != null && schemas.size() > 0) {
                    schemaSelectionModel.setElements(schemas);
                    schemaCombo.setSelectedIndex(0);
                }

            }

            // check if the transfer type has changed
            reload = true;
            if (selectedTransferType == transferType) {
                reload = false;
                return;
            }

//            String catalogName = null;
//            String schemaName = null;
//
//            Object value = schemaCombo.getSelectedItem();
//            if (value != null) {
//                if (useCatalogs) {
//                    catalogName = value.toString();
//                }
//                else {                    
//                    schemaName = value.toString();
//                }
//            }

            if (transferType == ImportExportProcess.MULTIPLE_TABLE) {
                // label the selection lists
                list.setLabelText("Available Tables:", "Selected Tables:");
                // remove values and disable table combo
                tableSelectionModel.removeAllElements();
                enableTableComponents(false);
            }
            else if (transferType == ImportExportProcess.SINGLE_TABLE) {
                // label the selection lists
                list.setLabelText("Available Columns:", "Selected Columns:");
                enableTableComponents(true);
                tableCombo.setEnabled(false);
            }

        }
        finally {
            if (reload) {
                schemaComboSelection();
                tableComboSelection();
            }
            tableCombo.addItemListener(this);
            schemaCombo.addItemListener(this);
        }
        selectedTransferType = transferType;
    }
    
    private void enableTableComponents(boolean enable) {
        tableCombo.setEnabled(enable);
        tableLabel.setEnabled(enable);
    }
    
    private void tableComboSelection() {
        if (parent.getTableTransferType() == 
                ImportExportProcess.MULTIPLE_TABLE || 
                tableSelectionModel.getSize() == 0) {
            return;
        }

        String catalogName = null;
        String schemaName = null;

        Object value = schemaCombo.getSelectedItem();
        if (value != null) {
            if (useCatalogs) {
                catalogName = value.toString();
            }
            else {                    
                schemaName = value.toString();
            }
        }
        
        try {
            String table = (String)tableCombo.getSelectedItem();
            list.createAvailableList(
                    metaData.getColumnMetaDataVector(table, schemaName, catalogName));
        }
        catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the table names for the selected " +
                    "catalog/schema.\n\nThe system returned:\n" + 
                    e.getExtendedMessage(), e);
        }

    }
    
    public void setSelectedSchema(String schema) {
        schemaCombo.setSelectedItem(schema);
    }
    
    public void setSelectedTable(String table) {
        tableCombo.setSelectedItem(table);
    }
    
    public void selectAllAvailable() {
        list.selectAllAction();
    }
    
    private void schemaComboSelection() {
        String catalogName = null;
        String schemaName = null;

        Object value = schemaCombo.getSelectedItem();
        if (value != null) {
            if (useCatalogs) {
                catalogName = value.toString();
            }
            else {                    
                schemaName = value.toString();
            }
        }

        try {
            int type = parent.getTableTransferType();
            if (type == ImportExportProcess.MULTIPLE_TABLE) {
                list.createAvailableList(
                        metaData.getTables(catalogName, schemaName, "TABLE"));
            }
            else {
                tableSelectionModel.removeAllElements();

                String[] tables = metaData.getTables(catalogName, schemaName, "TABLE");
                if (tables != null && tables.length > 0) {
                    enableTableComponents(true);
                    tableSelectionModel.setElements(tables);
                }
                else {
                    list.clear();
                    tableCombo.setEnabled(false);
                }

            }
        }
        catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the table names for the selected " +
                    "catalog/schema.\n\nThe system returned:\n" + 
                    e.getExtendedMessage(), e);
        }

    }
    
    /**
     * Invoked when an item has been selected or deselected by the user.
     * The code written for this method performs the operations
     * that need to occur when an item is selected (or deselected).
     */    
    public void itemStateChanged(ItemEvent event) {
        // interested in selections only
        if (event.getStateChange() == ItemEvent.DESELECTED) {
            return;
        }

        Object source = event.getSource();
        if (source == tableCombo) {
            tableComboSelection();
        } else if (source == schemaCombo) {
            schemaComboSelection();
        }
    }
    
    public boolean hasSelections() {
        return list.hasSelections();
    }
    
    public Vector<ColumnData> getSelectedColumns() {
        int type = parent.getTableTransferType();
        if (type == ImportExportProcess.SINGLE_TABLE) {
            return list.getSelectedValues();
        } else {
            return null;
        }
    }
    
    public String[] getSelectedTables() {
        int type = parent.getTableTransferType();
        String[] tables = null;
        
        if (type == ImportExportProcess.SINGLE_TABLE) {
            tables = new String[]{(String)tableCombo.getSelectedItem()};
        }
        else if (type == ImportExportProcess.MULTIPLE_TABLE) {
            Vector v = list.getSelectedValues();
            tables = new String[v.size()];
            for (int i = 0; i < tables.length; i++) {
                tables[i] = (String)v.elementAt(i);
            }            
        }
        
        return tables;
    }
    
}










