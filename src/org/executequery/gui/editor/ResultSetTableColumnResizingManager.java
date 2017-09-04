/*
 * ResultSetTableColumnResizingManager.java
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

package org.executequery.gui.editor;

import java.util.Enumeration;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.underworldlabs.util.SystemProperties;

public class ResultSetTableColumnResizingManager {

    private int[] columnWidths;

    private static final String STORING_RESIZED_COLUMNS_PROPERTY_KEY = "results.table.column.width.save";
    
    public void manageResultSetTable(JTable table) {

        if (isStoringResizedColumns()) {
            
            table.getTableHeader().getColumnModel().addColumnModelListener(
                    new ResultSetTableColumnResizeListener());
        }
    }

    public void reinstate(JTable table) {
        
        manageResultSetTable(table);
    }
    
    public void suspend(JTable table) {
        
        TableColumnModel tableColumnModel = table.getTableHeader().getColumnModel();
        if (isStoringResizedColumns() && tableColumnModel instanceof DefaultTableColumnModel) {
            
            DefaultTableColumnModel defaultTableColumnModel = (DefaultTableColumnModel) tableColumnModel; 
            for (TableColumnModelListener listener : defaultTableColumnModel.getColumnModelListeners()) {
                
                if (listener instanceof ResultSetTableColumnResizeListener) {
                    
                    defaultTableColumnModel.removeColumnModelListener(listener);
                }
            }

        }
        
    }
    
    public void setColumnWidthsForTable(JTable table) {
        
        if (canResizeColumns()) {
            
            TableColumnModel tableColumnModel = table.getColumnModel();
            int count = Math.min(columnWidths.length, tableColumnModel.getColumnCount());
            for (int i = 0; i < count; i++) {
                
                tableColumnModel.getColumn(i).setWidth(columnWidths[i]);
                tableColumnModel.getColumn(i).setPreferredWidth(columnWidths[i]);
            }
        
        }        

    }
    
    private boolean canResizeColumns() {

        return isStoringResizedColumns() && columnWidths != null && columnWidths.length > 0;
    }

    private boolean isStoringResizedColumns() {
        
        return SystemProperties.getBooleanProperty("user", STORING_RESIZED_COLUMNS_PROPERTY_KEY);
    }
    
    class ResultSetTableColumnResizeListener implements TableColumnModelListener {

        public void columnMarginChanged(ChangeEvent e) {
            
            Object source = e.getSource();
            if (source instanceof TableColumnModel) {
            
                TableColumnModel columnModel = (TableColumnModel) source;

                int count  = 0;
                columnWidths = new int[columnModel.getColumnCount()];
                
                for (Enumeration<TableColumn> i = columnModel.getColumns(); i.hasMoreElements();) {
                    
                    TableColumn tableColumn = i.nextElement();
                    columnWidths[count++] = tableColumn.getWidth(); 
                }

            }
            
        }

        public void columnAdded(TableColumnModelEvent e) {}
        public void columnMoved(TableColumnModelEvent e) {}
        public void columnRemoved(TableColumnModelEvent e) {}
        public void columnSelectionChanged(ListSelectionEvent e) {}
        
    }
    
}


