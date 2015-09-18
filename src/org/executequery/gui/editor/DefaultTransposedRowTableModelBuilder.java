/*
 * DefaultTransposedRowTableModelBuilder.java
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

package org.executequery.gui.editor;

import java.util.ArrayList;
import java.util.List;

import org.executequery.gui.resultset.StringRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTableModel;

public class DefaultTransposedRowTableModelBuilder implements TransposedRowTableModelBuilder {

    private static final String[] TRANSPOSED_ROW_COLUMN_HEADERS = {"Column Name", "Value"}; 
    
    public ResultSetTableModel transpose(ResultSetTableModel resultSetTableModel, int rowIndex) {
        
        List<RecordDataItem> rowDataForRow = resultSetTableModel.getRowDataForRow(rowIndex);
        
        int size = rowDataForRow.size();
        List<List<RecordDataItem>> transposedRow = new ArrayList<List<RecordDataItem>>(size);
        
        int columnCount = 2;
        for (int i = 0, n = resultSetTableModel.getColumnCount(); i < n; i++) {
            
            String columnName = resultSetTableModel.getColumnName(i);

            List<RecordDataItem> row = new ArrayList<RecordDataItem>(columnCount);

            row.add(dataItemForColumnName(columnName));
            row.add(rowDataForRow.get(i));
            
            transposedRow.add(row);
        }
        
        return new TransposedRowResultSetTableModel(columnHeaderAsList(), transposedRow);
    }

    private List<String> columnHeaderAsList() {

        List<String> columnHeaders = new ArrayList<String>(TRANSPOSED_ROW_COLUMN_HEADERS.length);
        for (String columnName : TRANSPOSED_ROW_COLUMN_HEADERS) {
            
            columnHeaders.add(columnName);
        }
        
        return columnHeaders;
    }

    private RecordDataItem dataItemForColumnName(String columnName) {

        return new StringRecordDataItem(columnName);
    }
    
}

