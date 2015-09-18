/*
 * ImportExportPanelTwo.java
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseSource;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.swing.ListSelectionPanel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ImportExportPanelTwo extends AbstractImportExportPanel {
    
    private JLabel tableLabel;
    
    private ListSelectionPanel list;
    
    public ImportExportPanelTwo(ImportExportWizard importExportWizard) {

        super(new GridBagLayout(), importExportWizard);

        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void jbInit() throws Exception {

        tableLabel = new JLabel("Table:");
        list = new ListSelectionPanel();
        
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
        add(schemaCombo(), gbc);
        gbc.gridy = 1;
        gbc.insets.bottom = 0;
        gbc.insets.top = 0;
        add(tableCombo(), gbc);
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

        tableCombo().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                if (e.getStateChange() == ItemEvent.SELECTED) {
                
                    if (!exportDataModel().isMultipleTableImportExport()) {
                        setColumnsAvailable();
                    }

                }

            }
        });

        schemaCombo().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                if (e.getStateChange() == ItemEvent.SELECTED) {
                
                    if (exportDataModel().isMultipleTableImportExport()) {
                        
                        setTablesAvailable();

                    } else {

                        list.clear();
                    }

                }

            }
        });

    }

    private JComboBox schemaCombo() {
        return importExportWizard().getSchemasCombo();
    }

    public boolean hasSelections() {
        return list.hasSelections();
    }
    
    protected void panelSelected() {
        
        if (exportDataModel().isHostSelectionChanged() ||
                exportDataModel().isImportExportTypeChanged()) {

            setListData();
        }
    }
    
    protected void panelDeselected() {
        
    }

    private ImportExportDataModel exportDataModel() {
        return importExportWizard().getExportDataModel();
    }

    private void setListData() {
        
        if (exportDataModel().isMultipleTableImportExport()) {

            multipleTableImportExport();
            setTablesAvailable();
            
        } else {
            
            singleTableImportExport();
            setColumnsAvailable();
        }

    }

    private void setColumnsAvailable() {
        list.createAvailableList(columnsFromTable());
    }

    private void setTablesAvailable() {
        list.createAvailableList(tablesFromSchema());
    }

    private List<DatabaseColumn> columnsFromTable() {
        
        JComboBox tableCombo = tableCombo();
        if (tableCombo.getSelectedItem() != null) {
        
            return ((DatabaseTable) tableCombo().getSelectedItem()).getColumns();
        }

        return new ArrayList<DatabaseColumn>(0);
    }

    private List<NamedObject> tablesFromSchema() {
        
        DatabaseSource databaseSource = (DatabaseSource) schemaCombo().getSelectedItem();

        if (databaseSource != null) {
        
            DatabaseMetaTag databaseMetaTag = databaseSource.getDatabaseMetaTag("TABLE");
    
            if (databaseMetaTag != null) {
                
                return databaseMetaTag.getObjects();
            }

        }

        return new ArrayList<NamedObject>(0);
    }

    private void multipleTableImportExport() {
        list.setLabelText("Available Tables:", "Selected Tables:");
        enableTableComponents(false);
    }
    
    private void singleTableImportExport() {
        list.setLabelText("Available Columns:", "Selected Columns:");
        enableTableComponents(true);
    }
    
    private JComboBox tableCombo() {
        return importExportWizard().getTablesCombo();
    }
    
    private void enableTableComponents(boolean enable) {
        tableLabel.setEnabled(enable);
    }

    public List<?> getSelectedItems() {
        return list.getSelectedValues();
    }

    public void selectAll() {
        list.selectAllAction();
    }

}






