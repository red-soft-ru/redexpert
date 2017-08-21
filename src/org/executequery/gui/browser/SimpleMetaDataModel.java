/*
 * SimpleMetaDataModel.java
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

import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import org.executequery.Constants;

public class SimpleMetaDataModel extends AbstractTableModel {

    private String[] columns = {"Property", "Value"};
    private String[] propertyNames;
    private String[] propertyValues;
    private Map<String, String> map;
    
    public int getRowCount() {
        if (propertyNames == null) {
            return 0;
        }
        return propertyNames.length;
    }
    
    public int getColumnCount() {
        return columns.length;
    }

    public void setValues(Map<String, String> map) {

        if (this.map == map) {

            return;
        }
        this.map = map;
        
        if (map == null) {

            propertyNames = new String[0];
            propertyValues = new String[0];
            fireTableDataChanged();
            return;
        }

        int size = map.size();
        propertyNames = new String[size];
        propertyValues = new String[size];
        
        int count = 0;
        for (Entry<String, String> entry : map.entrySet()) {

            propertyNames[count] = entry.getKey();
            propertyValues[count] = (String) map.get(entry.getValue());
            count++;            
        }        
        
        fireTableDataChanged();
    }
    
    public Object getValueAt(int row, int col) {
        switch (col) {                
            case 0:
                return propertyNames[row];
            case 1:
                return propertyValues[row];
            default:
                return Constants.EMPTY;                   
        }
    }
    
    public void setValueAt(Object value, int row, int col) {
        
        switch (col) {
            case 0:
                propertyNames[row] = ((String)value);
                break;                    
            case 1:
                propertyValues[row] = ((String)value);
                break;                    
        }
        
        fireTableCellUpdated(row, col);            
    }
    
    public String getColumnName(int col) {
        return columns[col];
    }
    
    public boolean isCellEditable(int row, int col) {
        return false;
    }
    

}

