/*
 * TablePrivilegeTab.java
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

package org.executequery.gui.browser;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.executequery.databaseobjects.TablePrivilege;
import org.executequery.gui.DefaultTable;
import org.underworldlabs.swing.table.TableSorter;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class TablePrivilegeTab extends JPanel {
    
    private JTable table;
    private JPanel tablePanel;
    private JLabel noResultsLabel;
    private TablePrivilegeModel model;
    
    public TablePrivilegeTab() {
        super(new BorderLayout());
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    private void jbInit() throws Exception {
        
        model = new TablePrivilegeModel();
        table = new DefaultTable();
        table.setModel(new TableSorter(model, table.getTableHeader()));
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Table Access Rights"));
        
        noResultsLabel = new JLabel("No privilege information for this object is available.",
        JLabel.CENTER);
        
    }
    
    public JTable getTable() {
        return table;
    }
    
    public void setValues(List<TablePrivilege> values) {
        if (values == null) {
            setValues(new TablePrivilege[0]);
        } 
        else {
            TablePrivilege[] _values = new TablePrivilege[values.size()];
            for (int i = 0; i < _values.length; i++) {
                _values[i] = values.get(i);
            }
            setValues(_values);
        }
    }
    
    public void setValues(TablePrivilege[] values) {
        if (values == model.getValues()) {
            return;
        }
        removeAll();
        
        if (values == null || values.length == 0) {
            add(noResultsLabel, BorderLayout.CENTER);
        }
        else {
            model.setValues(values);
            add(tablePanel, BorderLayout.CENTER);
        }
    }
    
    private class TablePrivilegeModel extends AbstractDatabaseTableViewModel {
        
        private TablePrivilege[] values;
        private String[] header = {"Grantor","Grantee","Privilege","Grantable"};
        
        public TablePrivilegeModel() {
            values = new TablePrivilege[0];
        }
        
        public TablePrivilege[] getValues() {
            return values;
        }
        
        public void setValues(TablePrivilege[] values) {
            this.values = values;
            fireTableDataChanged();
        }
        
        public int getRowCount() {
            return values.length;
        }
        
        public int getColumnCount() {
            return 4;
        }
        
        public String getColumnName(int col) {
            return header[col];
        }
        
        public Object getValueAt(int row, int col) {
            TablePrivilege object = values[row];
            
            switch (col) {
                
                case 0:
                    return object.getGrantor();
                    
                case 1:
                    return object.getGrantee();
                    
                case 2:
                    return object.getPrivilege();
                    
                case 3:
                    return object.getGrantable();
                    
                default:
                    return "NULL";
                    
            }
            
        }
        
        public Class getColumnClass(int col) {
            return String.class;
        }
        
        public boolean isCellEditable() {
            return false;
        }
        
    }
    
    
}














