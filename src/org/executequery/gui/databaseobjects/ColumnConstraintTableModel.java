/*
 * ColumnConstraintTableModel.java
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

import org.executequery.Constants;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;
import org.executequery.databaseobjects.impl.TableColumnConstraint;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Table model for db objects display.
 *
 * @author Takis Diakoumis
 */
public class ColumnConstraintTableModel extends AbstractPrintableTableModel {

    protected String[] header = {"",
            "Name",
            "Type",
            "Table Column",
            "Reference Schema",
            "Reference Table",
            "Reference Column",
            "Check",
            "Update rule",
            "Delete rule"
    };

    /**
     * the constraints list
     */
    private List<ColumnConstraint> constraints;

    /**
     * indicates whether this model is editable
     */
    private boolean editable;

    /**
     * Creates a new instance of ColumnConstraintTableModel
     */
    public ColumnConstraintTableModel() {
        this(null);
    }

    /**
     * Creates a new instance of ColumnConstraintTableModel
     */
    public ColumnConstraintTableModel(List<ColumnConstraint> constraints) {
        this(constraints, false);
    }

    /**
     * Creates a new instance of ColumnConstraintTableModel
     */
    public ColumnConstraintTableModel(boolean editable) {
        this(null, editable);
    }

    /**
     * Creates a new instance of ColumnConstraintTableModel
     */
    public ColumnConstraintTableModel(
            List<ColumnConstraint> constraints, boolean editable) {
        this.constraints = constraints;
        setEditable(editable);
    }

    /**
     * Returns the constraints collection.
     *
     * @return the constraints
     */
    public List<ColumnConstraint> getConstraints() {
        return constraints;
    }

    /**
     * Appends the specified constraint to end of the list.
     *
     * @param constraint the new constraint
     */
    public void addConstraint(TableColumnConstraint constraint) {
        // bail if we're not editable
        if (!isEditable()) {
            return;
        }

        if (constraints == null) {
            constraints = new ArrayList<ColumnConstraint>();
        }
        constraints.add(constraint);
        int newRow = getRowCount() - 1;
        fireTableRowsInserted(newRow, newRow);
    }

    /**
     * Removes the column value at the specified index.
     *
     * @param index the index to remove
     */
    public void deleteConstraintAt(int index) {
        if (constraints != null) {

            ColumnConstraint columnConstraint = constraints.get(index);
            columnConstraint.detachFromColumn();

            constraints.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    public void setValues(List<ColumnConstraint> constraints) {
        this.constraints = constraints;
        fireTableDataChanged();
    }

    public String getColumnName(int col) {
        return header[col];
    }

    public int getColumnCount() {
        return header.length;
    }

    public int getRowCount() {
        if (constraints == null) {
            return 0;
        }
        return constraints.size();
    }

    public void setValueAt(Object value, int row, int col) {
        // bail if we're not editable
        if (!isEditable()) {
            return;
        }

        // init a string representation of the value
        String _value = null;
        if (value != null) {
            _value = value.toString();
        }

        ColumnConstraint constraint = constraints.get(row);

        // if its not currently modified or isn't new
        // ensure a copy is made for later comparison 
        // and SQL text generation.
        if (!constraint.isNewConstraint() && !constraint.isAltered()) {

            ((TableColumnConstraint) constraint).makeCopy();
        }

        TableColumnConstraint tableConstraint = (TableColumnConstraint) constraint;

        switch (col) {
            case 1:
                tableConstraint.setName(_value);
                break;
            case 2:
                if (value == ColumnConstraint.PRIMARY) {
                    tableConstraint.setKeyType(ColumnConstraint.PRIMARY_KEY);
                } else if (value == ColumnConstraint.FOREIGN) {
                    tableConstraint.setKeyType(ColumnConstraint.FOREIGN_KEY);
                } else if (value == ColumnConstraint.UNIQUE) {
                    tableConstraint.setKeyType(ColumnConstraint.UNIQUE_KEY);
                } else if (value == ColumnConstraint.CHECK) {
                    tableConstraint.setKeyType(ColumnConstraint.CHECK_KEY);
                }

                tableConstraint.setReferencedCatalog(Constants.EMPTY);
                tableConstraint.setReferencedSchema(Constants.EMPTY);
                tableConstraint.setReferencedTable(Constants.EMPTY);
                tableConstraint.setReferencedColumn(Constants.EMPTY);
                break;
            case 3:
                // before we set the new column remove any 
                // existing column-constraint references
                DatabaseTableColumn column = constraint.getColumn();
                if (column != null) {
                    column.removeConstraint(constraint);
                }

                // set the new reference
                column = (DatabaseTableColumn) value;
                constraint.setColumn(column);

                // make sure that column contains this constraint
                if (column != null) {
                    column.addConstraint(constraint);
                }
                break;
            case 4:
                tableConstraint.setReferencedSchema(_value);
                tableConstraint.setReferencedTable(Constants.EMPTY);
                tableConstraint.setReferencedColumn(Constants.EMPTY);
                break;
            case 5:
                tableConstraint.setReferencedTable(_value);
                tableConstraint.setReferencedColumn(Constants.EMPTY);
                break;
            case 6:
                tableConstraint.setReferencedColumn(_value);
                break;
            case 7:
                tableConstraint.setCheck(_value);
                break;
            case 8:
                tableConstraint.setUpdateRule(_value);
                break;
            case 9:
                tableConstraint.setDeleteRule(_value);
                break;
        }
        fireTableRowsUpdated(row, row);
    }

    public Object getValueAt(int row, int col) {
        ColumnConstraint constraint = constraints.get(row);
        switch (col) {
            case 0:
                return constraint;
            case 1:
                return constraint.getShortName();
            case 2:
                return constraint.getTypeName();
            case 3:
                return constraint.getColumnName();
            case 4:
                return constraint.getReferencedSchema();
            case 5:
                return constraint.getReferencedTable();
            case 6:
                return constraint.getReferencedColumn();
            case 7:
                return constraint.getCheck();
            case 8:
                return constraint.getUpdateRule();
            case 9:
                return constraint.getDeleteRule();
            default:
                return null;
        }
    }

    /**
     * Returns the printable value at the specified row and column.
     *
     * @param row - the row index
     * @param col - the column index
     * @return the value to print
     */
    public String getPrintValueAt(int row, int col) {
        Object value = getValueAt(row, col);
        if (value != null && col > 0) {
            return value.toString();
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (isEditable() && columnIndex > 0) {

            if (columnIndex == 1) {
                return true;
            }

            ColumnConstraint constraint = constraints.get(rowIndex);
            if (constraint.isNewConstraint()) {
                if (columnIndex > 3 &&
                        (constraint.isPrimaryKey() || constraint.isUniqueKey())) {
                    return false;
                } else {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}












