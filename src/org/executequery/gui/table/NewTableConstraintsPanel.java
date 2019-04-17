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

import org.apache.commons.lang.StringUtils;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.swing.table.ComboBoxCellEditor;
import org.underworldlabs.util.MiscUtils;

import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class NewTableConstraintsPanel extends TableConstraintsPanel
        implements CreateTableSQLSyntax {

    /**
     * The table creator object - parent to this
     */
    private TableConstraintFunction creator;

    /**
     * The buffer off all SQL generated
     */
    private StringBuffer sqlBuffer;

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

            case 0:
            case 1:
                setCellEditor(3, new ComboBoxCellEditor(
                        creator.getTableColumnDataVector()));

                if (cc.getType() != -1 && cc.getTypeName() == ColumnConstraint.FOREIGN) {
                    setCellEditor(4, new ComboBoxCellEditor(
                            creator.getSchemaTables(value)));

                }
                break;

            case 2:
                return;
            case 3:
                break;

            case 4:

                try {
                    setCellEditor(5, new ComboBoxCellEditor(
                            creator.getColumnNamesVector(value, null)));
                } catch (NullPointerException nullExc) {
                } // i forget why
                break;

        }

    }

    public void columnValuesChanged(int col, int row, String value) {

        Vector v = getKeys();
        String name = null;
        boolean hasName = false;

        sqlBuffer.setLength(0);

        for (int i = 0, n = v.size(); i < n; i++) {

            ColumnConstraint cc = (ColumnConstraint) v.elementAt(i);

            if (i == row && StringUtils.isNotBlank(value)) {

                name = value;
                hasName = true;

            } else if (!MiscUtils.isNull(cc.getName())) {

                name = cc.getName();
                hasName = true;

            } else {

                hasName = false;
            }

            if (hasName) {

                sqlBuffer.append(COMMA).append(NEW_LINE_2).append(CONSTRAINT);
                sqlBuffer.append(MiscUtils.wordInQuotes(name)).append(SPACE);

                if (cc.getType() != -1) {

                    if (cc.getType() == ColumnConstraint.UNIQUE_KEY) {
                        sqlBuffer.append(ColumnConstraint.UNIQUE).append(B_OPEN);
                        sqlBuffer.append(MiscUtils.wordInQuotes(cc.getColumn())).append(B_CLOSE);
                    } else {
                        sqlBuffer.append(cc.getTypeName()).append(KEY).append(B_OPEN);
                        sqlBuffer.append(MiscUtils.wordInQuotes(cc.getColumn()));
                        sqlBuffer.append(B_CLOSE);

                        if (cc.getType() == ColumnConstraint.FOREIGN_KEY) {
                            sqlBuffer.append(INDENT).append(REFERENCES);

                            if (cc.hasSchema())
                                sqlBuffer.append(cc.getRefSchema()).append(DOT);

                            sqlBuffer.append(MiscUtils.wordInQuotes(cc.getRefTable())).
                                    append(B_OPEN).append(MiscUtils.wordInQuotes(cc.getRefColumn())).
                                    append(B_CLOSE);
                        }

                    }

                }

            }

        }
        creator.setSQLText(sqlBuffer.toString(), TableModifier.CONSTRAINT_VALUES);
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






