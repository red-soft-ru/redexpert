/*
 * AbstractDatabaseObjectTable.java
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

import javax.swing.table.TableColumnModel;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.table.ColumnKeyRenderer;
import org.underworldlabs.swing.table.TableSorter;

/**
 * Simple database object table display.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public abstract class AbstractDatabaseObjectTable extends DefaultTable {
    
    /** the table model */
    private DatabaseObjectTableModel model;

    /** Initialises the table with some default properties. */
    protected void initTableDisplayDefaults() {
        getTableHeader().setReorderingAllowed(false);
        setCellSelectionEnabled(true);
        setColumnSelectionAllowed(false);
        setRowSelectionAllowed(false);
        setSurrendersFocusOnKeystroke(true);
    }
    
    /** Initialises with the default model. */
    protected void initDefaultTableModel() {
        createModel();
    }
    
    /** Returns the table model as a DatabaseObjectTableModel. */
    protected DatabaseObjectTableModel getDatabaseTableModel() {
        return model;
    }
    
    /** Initialises the cell renderer. */
    protected void initDefaultCellRenderer() {
        if (getColumnCount() > 0) {
            getColumnModel().getColumn(0).setCellRenderer(new ColumnKeyRenderer());
        }
    }

    /**
     * Sets the table data to that specified.
     *
     * @param columns the column value data to display
     */
    public void setColumnData(List<DatabaseColumn> columns) {
        
        if (model == null) {
        
            createModel();

        } else {

            model.setValues(columns);
        }
    }

    private void createModel() {
        
        model = new DatabaseObjectTableModel();
        setModel(new TableSorter(model, getTableHeader()));
        setColumnProperties();
    }
    
    /** Sets table column display properties and sizes. */
    protected void setColumnProperties() {
        TableColumnModel tcm = getColumnModel();
        tcm.getColumn(0).setPreferredWidth(25);
        tcm.getColumn(0).setMaxWidth(25);
        tcm.getColumn(0).setMinWidth(25);
        tcm.getColumn(1).setPreferredWidth(150);
        tcm.getColumn(2).setPreferredWidth(110);
        tcm.getColumn(3).setPreferredWidth(50);
        tcm.getColumn(4).setPreferredWidth(50);
        tcm.getColumn(5).setPreferredWidth(70);
        tcm.getColumn(5).setMaxWidth(70);
        tcm.getColumn(6).setPreferredWidth(130);
    }
    
}

