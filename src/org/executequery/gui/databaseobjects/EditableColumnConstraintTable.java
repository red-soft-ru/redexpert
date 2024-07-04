/*
 * EditableColumnConstraintTable.java
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

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.impl.TableColumnConstraint;

/**
 * @author Takis Diakoumis
 */
public class EditableColumnConstraintTable extends DefaultColumnConstraintTable {

    public EditableColumnConstraintTable() {
        super();
    }

    /**
     * Resets and clears the currently displayed table.
     */
    public void resetConstraintsTable() {
        setDatabaseTable(null);
    }

    /**
     * Deletes or marks to delete the currently selected
     * database table constraint (JTable row).
     */
    public void deleteSelectedConstraint() {

        int selectedRow = getSelectedRow();
        if (selectedRow == -1)
            return;

        ColumnConstraintTableModel constraintTableModel = getColumnConstraintTableModel();
        TableColumnConstraint constraint = (TableColumnConstraint) constraintTableModel.getValueAt(selectedRow, 0);
        if (constraint.isNewConstraint()) {
            constraintTableModel.deleteConstraintAt(selectedRow);

        } else {
            constraint.setMarkedDeleted(true);
            constraintTableModel.fireTableRowsUpdated(selectedRow, selectedRow);
        }
    }

    /**
     * Sets the currently displayed table to that specified.
     *
     * @param databaseTable the db table shown
     */
    public void setDatabaseTable(DatabaseTable databaseTable) {
        setConstraintData(databaseTable != null ? databaseTable.getConstraints() : null);
    }

}
