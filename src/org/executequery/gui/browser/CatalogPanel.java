/*
 * CatalogPanel.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
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
import javax.swing.table.AbstractTableModel;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseCatalog;
import org.executequery.databaseobjects.DatabaseSchema;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1783 $
 * @date     $Date: 2017-09-19 00:04:44 +1000 (Tue, 19 Sep 2017) $
 */
public class CatalogPanel extends BrowserNodeBasePanel {
    
    public static final String NAME = "CatalogPanel";
    
    private CatalogModel model;
    
    /** the browser's control object */
    private BrowserController controller;

    public CatalogPanel(BrowserController controller) {

        super(Bundles.get(CatalogPanel.class,"catalogName"));

        this.controller = controller;
        
        try {

            init();

        } catch (Exception e) {
          
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {

        model = new CatalogModel();
        table().setModel(model);
        
        tablePanel().setBorder(BorderFactory.createTitledBorder("Available Schemas"));
        
        setHeaderText(bundleString("DatabaseCatalog"));
        setHeaderIcon(GUIUtilities.loadIcon("DBImage24.png"));        
    }
    
    public String getLayoutName() {
        
        return NAME;
    }

    protected String getPrintablePrefixLabel() {

        return bundleString("DatabaseCatalog") + ": ";
    }

    public void refresh() {}
    
    public void cleanup() {}

    public void setValues(DatabaseCatalog catalog) {
    
        typeField().setText(catalog.getName());
        
        try {

            model.setValues(catalog.getSchemas());

        } catch (DataSourceException e) {
           
            controller.handleException(e);
        }
    }
    
    private class CatalogModel extends AbstractTableModel {
        
        private List<DatabaseSchema> values;
        private String header = bundleString("SchemaName");
        
        public void setValues(List<DatabaseSchema> values) {

            this.values = values;
            fireTableDataChanged();
        }
        
        public int getRowCount() {
            
            if (hasValues()) {

                return values.size();
            }

            return 0;
        }
        
        public int getColumnCount() {
            
            return 1;
        }
        
        public String getColumnName(int col) {

            return header;
        }
        
        public Object getValueAt(int row, int col) {
            
            if (hasValues()) {

                return values.get(row);
            }
            
            return null; 
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {

            return false;
        }
        
        private boolean hasValues() {

            return values != null && !values.isEmpty();
        }

    }
    
}
