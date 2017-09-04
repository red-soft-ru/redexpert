/*
 * AbstractColumnConstraintTable.java
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

package org.executequery.gui.databaseobjects;

import java.util.List;

import javax.swing.table.TableColumnModel;

import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.table.ColumnConstraintRenderer;
import org.underworldlabs.swing.table.TableSorter;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public abstract class AbstractColumnConstraintTable extends DefaultTable {
    
    /** the table model */
    private ColumnConstraintTableModel model;
    
    /**
     * Initialises the table with some default properties.
     */
    protected void initTableDisplayDefaults() {
        getTableHeader().setReorderingAllowed(false);
        setCellSelectionEnabled(true);
        setColumnSelectionAllowed(false);
        setRowSelectionAllowed(false);
        setSurrendersFocusOnKeystroke(true);
    }
    
    /**
     * Initialises with the default model.
     */
    protected void initDefaultTableModel() {
        createModel();
    }
    
    /**
     * Returns the table model as a DatabaseObjectTableModel.
     */
    protected ColumnConstraintTableModel getColumnConstraintTableModel() {
        return model;
    }
    
    /** 
     * Initialises the cell renderer.
     */
    protected void initDefaultCellRenderer() {
        if (getColumnCount() > 0) {
            getColumnModel().getColumn(0).setCellRenderer(new ColumnConstraintRenderer());
        }
    }

    /**
     * Sets the table constraint data to that specified.
     *
     * @param columns the column value data to display
     */
    public void setConstraintData(List<ColumnConstraint> constraints) {
        if (model == null) {
            createModel();
        }
        else {
            model.setValues(constraints);
        }
    }

    private void createModel() {
        
        model = new ColumnConstraintTableModel();
        setModel(new TableSorter(model, getTableHeader()));
        setColumnProperties();
    }

    /**
     * Sets table column display properties and sizes.
     */
    protected void setColumnProperties() {
        TableColumnModel tcm = getColumnModel();
        tcm.getColumn(0).setPreferredWidth(25);
        tcm.getColumn(0).setMaxWidth(25);
        tcm.getColumn(0).setMinWidth(25);
        tcm.getColumn(1).setPreferredWidth(125);
        tcm.getColumn(2).setPreferredWidth(75);
        tcm.getColumn(3).setPreferredWidth(110);
        tcm.getColumn(4).setPreferredWidth(120);
        tcm.getColumn(5).setPreferredWidth(120);
        tcm.getColumn(6).setPreferredWidth(120);
    }
    
}






