/*
 * TransposedRowResultSetPanel.java
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

import javax.swing.table.TableColumnModel;

import org.executequery.gui.resultset.ResultSetTableModel;

public class TransposedRowResultSetPanel extends ResultSetPanel {

    private static final int COLUMN_NAME_COLUMN_WIDTH = 200;
    
    private static final int VALUE_COLUMN_WIDTH = 500;

    public TransposedRowResultSetPanel(
            ResultSetTableContainer resultSetTableContainer, ResultSetTableModel model) {
        
        super(resultSetTableContainer);
        setResultSet(model, false);
     
        setTableProperties();
    }

    public void setTableProperties() {
        
        super.setTableProperties();

        TableColumnModel columnModel = getTable().getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(COLUMN_NAME_COLUMN_WIDTH);
        columnModel.getColumn(1).setPreferredWidth(VALUE_COLUMN_WIDTH);
    }
    
}





