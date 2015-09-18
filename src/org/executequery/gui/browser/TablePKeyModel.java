/*
 * TablePKeyModel.java
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

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class TablePKeyModel extends AbstractTableModel {
    
    private String[] header = {"Name", "Column"};
    
    private String keyName;
    private String column;
    
    public TablePKeyModel(String s1, String s2) {
        keyName = s1;
        column = s2;
    }
    
    public int getColumnCount() {
        return 2;
    }
    
    public int getRowCount() {
        return 1;
    }
    
    public Object getValueAt(int row, int col) {
        
        switch(col) {
            case 0:
                return keyName;
            case 1:
                return column;
            default:
                return null;
        }
    }
    
    public void setValueAt(Object value, int row, int col) {
        
        switch (col) {
            case 0:
                keyName = (String)value;
                break;
            case 1:
                column = (String)value;
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

















