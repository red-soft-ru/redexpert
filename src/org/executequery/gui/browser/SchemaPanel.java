/*
 * SchemaPanel.java
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

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseSchema;
import org.executequery.databaseobjects.SimpleDatabaseObject;
import org.underworldlabs.jdbc.DataSourceException;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class SchemaPanel extends BrowserNodeBasePanel {
    
    public static final String NAME = "SchemaPanel";
    
    private JLabel noResultsLabel;
    
    private SchemaModel model;
    
    private BrowserController controller;

    public SchemaPanel(BrowserController controller) {

        super("Schema Name:");
        this.controller = controller;

        try {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {

        noResultsLabel = new JLabel("No information for this object is available.",
                                    JLabel.CENTER);

        JTable table = table();
        model = new SchemaModel();
        table.setModel(model);        
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        tablePanel().setBorder(BorderFactory.createTitledBorder("Available Objects"));
        setHeaderText("Database Schema");
        setHeaderIcon(GUIUtilities.loadIcon("User24.png"));
    }
    
    protected String getPrintablePrefixLabel() {

        return "Database Schema: ";
    }
    
    public String getLayoutName() {
        return NAME;
    }
    
    public void refresh() {}
    public void cleanup() {}
    
    public JTable getTable() {
        return table();
    }
    
    public void setValues(DatabaseSchema schema) {
        typeField().setText(schema.getName());
        boolean hadResults = model.getRowCount() > 0;
        
        List<SimpleDatabaseObject> values = null;
        try {
            values = schema.getSchemaObjects();
        } catch (DataSourceException e) {
            controller.handleException(e);
        }
        model.setValues(values);

        if (values == null || values.isEmpty()) {
            tablePanel().remove(scroller());
            tablePanel().add(noResultsLabel, getPanelConstraints());
            tablePanel().validate();
        }
        else {
            if (!hadResults) {
                tablePanel().remove(noResultsLabel);
                tablePanel().add(scroller(), getPanelConstraints());
                tablePanel().validate();
            }
        }

    }
    
    private class SchemaModel extends AbstractTableModel {

        private List<SimpleDatabaseObject> values;
        private String[] header = {"Catalog","Schema","Name","Type","Remarks"};
        
        public SchemaModel() {}
        
        public void setValues(List<SimpleDatabaseObject> values) {
            this.values = values;
            fireTableDataChanged();
        }
        
        public int getRowCount() {
            if (values == null) {
                return 0;
            }
            return values.size();
        }
        
        public int getColumnCount() {
            return 5;
        }
        
        public String getColumnName(int col) {
            return header[col];
        }
        
        public Object getValueAt(int row, int col) {
            
            if (values.size() <= row) {

                return "NULL";
            }
            
            SimpleDatabaseObject object = values.get(row);
            switch (col) {
                case 0:
                    return object.getCatalogName();
                case 1:
                    return object.getSchemaName();
                case 2:
                    return object.getName();
                case 3:
                    return object.getMetaDataKey();
                case 4:
                    return object.getRemarks();
                default:
                    return "NULL";                    
            }
        }
        
    }
    
}





