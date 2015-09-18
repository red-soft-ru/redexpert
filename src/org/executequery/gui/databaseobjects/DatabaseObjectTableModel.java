/*
 * DatabaseObjectTableModel.java
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;
import org.executequery.databaseobjects.impl.DefaultDatabaseColumn;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;

/**
 * Table model for db objects display.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DatabaseObjectTableModel extends AbstractPrintableTableModel {

    protected String[] header = {"", "Name", "Datatype", "Size", "Scale", "Required", "Default" };

    /** the database table columns */
    protected List<DatabaseColumn> columns;

    /** indicates whether this model is editable */
    private boolean editable;

    /** Creates a new instance of DatabaseObjectTableModel */
    public DatabaseObjectTableModel() {
        this(null);
    }

    /** Creates a new instance of DatabaseObjectTableModel */
    public DatabaseObjectTableModel(List<DatabaseColumn> columns) {
        this(columns, false);
    }

    /** Creates a new instance of DatabaseObjectTableModel */
    public DatabaseObjectTableModel(boolean editable) {
        this(null, editable);
    }

    /** Creates a new instance of DatabaseObjectTableModel */
    public DatabaseObjectTableModel(List<DatabaseColumn> columns, boolean editable) {
        this.columns = columns;
        setEditable(editable);
    }

    @Override
    public boolean canSortColumn(int column) {
        return (column > 0);
    }
    
    public void setValues(List<DatabaseColumn> columns) {
        this.columns = columns;
        fireTableDataChanged();
    }

    public int getColumnCount() {
        return header.length;
    }

    public int getRowCount() {
        if (columns == null) {
            return 0;
        }
        return columns.size();
    }

    public int indexOf(DatabaseColumn column) {
        if (columns == null) {
            return -1;
        }
        return columns.indexOf(column);
    }

    public Object getValueAt(int row, int col) {

        if (row >= getRowCount()) {

            return null;
        }

        DatabaseColumn column = columns.get(row);

        switch(col) {
            case 0:
                return column;
            case 1:
//                return stringValueToUpper(column.getShortName());
                return column.getShortName();
            case 2:
                return column.getTypeName();
            case 3:
                return Integer.valueOf(column.getColumnSize());
            case 4:
                return Integer.valueOf(column.getColumnScale());
            case 5:
                return Boolean.valueOf(column.isRequired());
            case 6:
                return column.getDefaultValue();
            default:
                return null;
        }

    }

    @SuppressWarnings("unused")
    private String stringValueToUpper(String value) {

        if (StringUtils.isNotBlank(value)) {

            return value.toUpperCase();
        }

        return "";
    }

    public void setValueAt(Object value, int row, int col) {

        // bail if we're not editable
        if (!isEditable()) {

            return;
        }

        DatabaseColumn column = columns.get(row);

        // only the DefaultDatabaseColumn implementations are editable
        if (!(column instanceof DefaultDatabaseColumn)) {

            return;
        }

        if (column instanceof DatabaseTableColumn) {

            DatabaseTableColumn tableColumn = (DatabaseTableColumn)column;

            // if its not currently modified or isn't new
            // ensure a copy is made for later comparison
            // and SQL text generation.

            if (!tableColumn.isNewColumn() && !tableColumn.isMarkedDeleted()) {

                tableColumn.makeCopy();
            }

        }

        DefaultDatabaseColumn defaultDatabaseColumn = (DefaultDatabaseColumn) column;

        switch(col) {
            case 1:
                defaultDatabaseColumn.setName((String)value);
                break;
            case 2:
                defaultDatabaseColumn.setTypeName((String)value);
                break;
            case 3:
                if (value == null) {
                    value = Integer.valueOf(0);
                }
                defaultDatabaseColumn.setColumnSize(((Integer)value).intValue());
                break;
            case 4:
                if (value == null) {
                    value = Integer.valueOf(0);
                }
                defaultDatabaseColumn.setColumnScale(((Integer)value).intValue());
                break;
            case 5:
                defaultDatabaseColumn.setRequired(((Boolean)value).booleanValue());
                break;
            case 6:
                defaultDatabaseColumn.setDefaultValue((String)value);
                break;
        }

        fireTableRowsUpdated(row, row);
    }

    /**
     * Removes the column value at the specified index.
     *
     * @param index the index to remove
     */
    public void deleteDatabaseColumnAt(int index) {
        if (columns != null) {
            columns.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    public void addNewDatabaseColumn(DatabaseColumn column, int toIndex) {

        if (!isEditable()) {
            return;
        }

        if (columns == null) {
            columns = new ArrayList<DatabaseColumn>();
        }

        int row = -1;
        if (toIndex != -1) {
            columns.add(toIndex, column);
            row = toIndex;
        }
        else {
            columns.add(column);
            row = columns.size() - 1;
        }
        fireTableRowsInserted(row, row);
    }

    public String getColumnName(int col) {
        return header[col];
    }

    public Class<?> getColumnClass(int col) {

        if (col == 5) {

            return Boolean.class;

        } else if (col == 3 || col == 4) {

            return Integer.class;

        } else {

            return String.class;
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
        if (value != null) {
            if (col > 0) {
                return value.toString();
            }
            else if (col == 0) {
                DatabaseColumn dc = (DatabaseColumn)value;
                if (dc.isPrimaryKey()) {
                    if (dc.isForeignKey()) {
                        return "PFK";
                    }
                    return "PK";
                }
                else if (dc.isForeignKey()) {
                    return "FK";
                }
            }
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return isEditable() && columnIndex != 0;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}











