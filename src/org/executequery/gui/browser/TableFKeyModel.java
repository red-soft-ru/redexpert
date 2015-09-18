/*
 * TableFKeyModel.java
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

import javax.swing.table.AbstractTableModel;

import java.util.Vector;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class TableFKeyModel extends AbstractTableModel {
    
    private String[] header = {"Name", "Column", "Reference Schema",
                               "Reference Table", "Reference Column"};
    
    private Vector keys;
    
    public TableFKeyModel(Vector v) {
        keys = v;
    }
    
    public int getColumnCount() {
        return 5;
    }
    
    public int getRowCount() {
        return keys.size();
    }
    
    public Object getValueAt(int row, int col) {
        ColumnConstraint cc = (ColumnConstraint)keys.elementAt(row);
        
        switch(col) {
            case 0:
                return cc.getName();
            case 1:
                return cc.getColumn();
            case 2:
                return cc.getRefSchema();
            case 3:
                return cc.getRefTable();
            case 4:
                return cc.getRefColumn();
            default:
                return null;
        }
    }
    
    public void setValueAt(Object value, int row, int col) {
        ColumnConstraint cc = (ColumnConstraint)keys.elementAt(row);
        
        switch (col) {
            case 0:
                cc.setName((String)value);
                break;
            case 1:
                cc.setColumn((String)value);
                break;
            case 2:
                cc.setRefSchema((String)value);
                break;
            case 3:
                cc.setRefTable((String)value);
                break;
            case 4:
                cc.setRefColumn((String)value);
                break;
        }
        
        fireTableRowsUpdated(row, row);
    }
    
    public boolean isCellEditable(int row, int col) {
        if (col == 0)
            return true;
        else
            return false;
    }
    
    public String getColumnName(int col) {
        return header[col];
    }
    
}

















