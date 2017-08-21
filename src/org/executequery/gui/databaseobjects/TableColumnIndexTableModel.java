/*
 * TableColumnIndexTableModel.java
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

package org.executequery.gui.databaseobjects;

import java.util.List;

import org.executequery.databaseobjects.impl.TableColumnIndex;
import org.executequery.gui.browser.AbstractDatabaseTableViewModel;

/**
 *
 * @author takisd
 */
public class TableColumnIndexTableModel extends AbstractDatabaseTableViewModel {
    
    /** the table indexed columns */
    private List<TableColumnIndex> indexes;

    private static final String[] header = {"", "Index Name", 
                                            "Indexed Column", "Non-Unique"};

    /** Creates a new instance of DatabaseTableColumnIndexTableModel */
    public TableColumnIndexTableModel() {}
    
    public void setIndexData(List<TableColumnIndex> indexes) {
        if (this.indexes == indexes) {
            return;
        }
        this.indexes = indexes;
        fireTableDataChanged();
    }

    public int getRowCount() {
        if (indexes == null) {
            return 0;
        }
        return indexes.size();
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
        TableColumnIndex index = indexes.get(row);
        switch(col) {
            case 1:
                return index.getName();
            case 2:
                return index.getIndexedColumn();
            case 3:
                return Boolean.valueOf(index.isNonUnique());
            default:
                return null;
        }
    }
    
    public void setValueAt(Object value, int row, int col) {
        TableColumnIndex index = indexes.get(row);
        switch (col) {
            case 1:
                index.setName((String) value);
                break;
            case 2:
                index.setIndexedColumn((String) value);
                break;
            case 3:
                index.setNonUnique(((Boolean) value).booleanValue());
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











