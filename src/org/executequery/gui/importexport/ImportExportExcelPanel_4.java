/*
 * ImportExportExcelPanel_4.java
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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.executequery.gui.DefaultTable;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ImportExportExcelPanel_4 extends JPanel {
    
    /** The sheets name table */
    private JTable sheetsTable;

    /** The column labels check box */
    private JCheckBox columnLabelCheck;

    /** The column labels check box */
    private JCheckBox mapTypesCheck;
    
    /** The tabel model */
    private SheetNameTableModel tableModel;
    
    /** The controlling object for this process */
    private ImportExportProcess parent;
    
    /** <p>Creates a new instance with the specified
     *  process as the parent.
     *
     *  @param the parent controlling the process
     */
    public ImportExportExcelPanel_4(ImportExportProcess parent) {
        super(new GridBagLayout());
        this.parent = parent;
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /** <p>Initialises the state of this instance and
     *  lays out components on the panel. */
    private void jbInit() throws Exception {
        mapTypesCheck = new JCheckBox("Map data types to cell formats", false);
        columnLabelCheck = new JCheckBox("Include column names on row 1", true);
        JLabel instructLabel = new JLabel("Enter the names of the individual sheet(s):");
        
        String[] tables = parent.getSelectedTables();
        String[][] values = new String[tables.length][2];
        
        for (int i = 0; i < tables.length; i++) {
            values[i][0] = tables[i];
            values[i][1] = tables[i];
        }
        
        tableModel = new SheetNameTableModel(values);
        sheetsTable = new DefaultTable(tableModel);
        
        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(5,10,5,10);
        gbc.insets = ins;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        this.add(columnLabelCheck, gbc);
        gbc.gridy = 1;
        gbc.insets.top = 0;
        this.add(mapTypesCheck, gbc);
        gbc.gridy = 2;
        gbc.insets.top = 5;
        this.add(instructLabel, gbc);
        gbc.gridy = 3;
        gbc.insets.top = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(new JScrollPane(sheetsTable), gbc);
        
        //    int type = parent.getTransferType();
        
    }
    
    public void reset(String[] tables) {
        
        String[][] values = new String[tables.length][2];
        
        for (int i = 0; i < tables.length; i++) {
            values[i][0] = tables[i];
            values[i][1] = tables[i];
        }
        
        tableModel.setValues(values);
        sheetsTable.tableChanged(new TableModelEvent(tableModel));
        //    sheetsTable.setModel(tableModel);
        //    sheetsTable.revalidate();
        
    }
    
    public String[][] getSheetNameValues() {
        return tableModel.getValues();
    }
    
    public boolean entriesComplete() {
        
        String[][] values = tableModel.getValues();
        
        for (int i = 0; i < values.length; i++) {
            
            if (values[i][1] == null || values[i][1].length() == 0)
                return false;
            
        }
        
        return true;
        
    }
    
    public boolean mapDataTypesToCells() {
        return mapTypesCheck.isSelected();
    }
    
    public boolean includeColumnNamesRowOne() {
        return columnLabelCheck.isSelected();
    }
    
    class SheetNameTableModel extends AbstractTableModel {
        
        private String[][] values;
        private String[] columnNames;
        
        public SheetNameTableModel(String[][] _values) {
            values = _values;
            columnNames = new String[]{"Table Name", "Sheet Name"};
        }
        
        public int getRowCount() {
            return values.length;
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public void setValues(String[][] _values) {
            values = _values;
            fireTableRowsUpdated(0, values.length);
        }
        
        public String[][] getValues() {
            return values;
        }
        
        public Object getValueAt(int row, int col) {
            return values[row][col];
        }
        
        public void setValueAt(Object value, int row, int col) {
            values[row][col] = value.toString();
            fireTableRowsUpdated(row, row);
        }
        
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        public boolean isCellEditable(int row, int col) {
            return col == 1;
        }
        
        
    } // class SheetNameTableModel
    
    
} // class














