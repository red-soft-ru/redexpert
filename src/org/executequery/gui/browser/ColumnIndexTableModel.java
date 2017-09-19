/*
 * ColumnIndexTableModel.java
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

import org.executequery.localization.Bundles;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1783 $
 * @date     $Date: 2017-09-19 00:04:44 +1000 (Tue, 19 Sep 2017) $
 */
public class ColumnIndexTableModel extends AbstractTableModel {
    
    /** The index data */
    private ColumnIndex[] data;
    
    private static final String[] header = Bundles.get(ColumnIndexTableModel.class,new String[]{"", "IndexName", "IndexedColumn", "Non-Unique"});
    
    public ColumnIndexTableModel() {}
    
    public ColumnIndexTableModel(ColumnIndex[] data) {
        this.data = data;
    }
    
    public void setIndexData(ColumnIndex[] data) {
        if (this.data == data) {
            return;
        }
        this.data = data;
        fireTableDataChanged();
    }
    
    public int getRowCount() {
        if (data == null) {
            return 0;
        }
        return data.length;
    }
    
    public int getColumnCount() {
        return header.length;
    }
    
    public String getColumnName(int col) {
        return header[col];
    }
    
    public boolean isCellEditable(int row, int col) {
        return false;
    }
    
    public Object getValueAt(int row, int col) {
        ColumnIndex cid = data[row];        
        switch(col) {
            case 1:
                return cid.getIndexName();
            case 2:
                return cid.getIndexedColumn();
            case 3:
                return Boolean.valueOf(cid.isNonUnique());
            default:
                return null;
        }
    }
    
    public void setValueAt(Object value, int row, int col) {
        ColumnIndex cid = data[row];        
        switch (col) {
            case 1:
                cid.setIndexName((String) value);
                break;
            case 2:
                cid.setIndexedColumn((String) value);
                break;
            case 3:
                cid.setNonUnique(((Boolean) value).booleanValue());
                break;
        }
        
        fireTableRowsUpdated(row, row);
    }
    
    public Class<?> getColumnClass(int col) {
        if (col == 3) {
            return Boolean.class;
        }
        return String.class;
    }
    
    
}


