/*
 * NewTableConstraintsPanel.java
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

package org.executequery.gui.table;

import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.swing.table.ComboBoxCellEditor;

import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class NewTableConstraintsPanel extends TableConstraintsPanel
        implements CreateTableSQLSyntax {

    /**
     * The table creator object - parent to this
     */
    private final TableConstraintFunction creator;

    /**
     * The buffer off all SQL generated
     */
    private final StringBuffer sqlBuffer;

    public NewTableConstraintsPanel(TableConstraintFunction creator) {
        super();
        this.creator = creator;
        sqlBuffer = new StringBuffer(100);
    }

    public ColumnData[] getTableColumnData() {
        return creator.getTableColumnData();
    }

    public int getMode() {
        return CREATE_TABLE_MODE;
    }

    public void updateCellEditor(int col, int row, String value) {
        ColumnConstraint cc = getConstraintAt(row);

        switch (col) {

            case X:
            case TYPE:
                setCellEditor(TABLE_COLUMN, new ComboBoxCellEditor(
                        creator.getTableColumnDataVector()));

                if (cc.getType() != -1 && cc.getTypeName() == ColumnConstraint.FOREIGN) {
                    setCellEditor(REFERENCE_TABLE, new ComboBoxCellEditor(
                            new Vector<>(creator.getTables())));
                    setCellEditor(UPDATE_RULE, new ComboBoxCellEditor(
                            ColumnConstraint.RULES
                    ));
                    setCellEditor(DELETE_RULE, new ComboBoxCellEditor(
                            ColumnConstraint.RULES
                    ));
                }
                break;

            case NAME:
                return;
            case TABLE_COLUMN:
                break;

            case REFERENCE_TABLE:

                try {
                    setCellEditor(REFERENCE_COLUMN, new ComboBoxCellEditor(
                            new Vector<>(creator.getColumns(cc.getRefTable()))));
                } catch (NullPointerException nullExc) {
                } // i forget why
                break;

        }

    }

    public void columnValuesChanged(int col, int row, String value) {

        creator.setSQLText();
    }

    public void resetSQLText() {
        columnValuesChanged(0, 0, null);
    }

    public String getSQLText() {
        return sqlBuffer.toString();
    }

    public void columnValuesChanged() {
        columnValuesChanged(-1, -1, null);
    }


}






