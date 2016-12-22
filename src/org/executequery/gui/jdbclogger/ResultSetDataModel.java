package org.executequery.gui.jdbclogger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


import javax.swing.table.AbstractTableModel;

import ch.sla.jdbcperflogger.StatementType;

class ResultSetDataModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private List<String> columnNames = new ArrayList<>();
    private List<Class<?>> columnTypes = new ArrayList<>();
    private List<Object[]> rows = new ArrayList<>();

    void setNewData(final List<Object[]> rows, final List<String> columnNames, final List<Class<?>> columnTypes) {
        final boolean columnsChanged = !this.columnNames.equals(columnNames);
        if (columnsChanged) {
            ResultSetDataModel.this.fireTableStructureChanged();
        }

        this.rows = rows;
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;

        if (columnsChanged) {
            ResultSetDataModel.this.fireTableStructureChanged();
        } else {
            ResultSetDataModel.this.fireTableDataChanged();
        }
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override

    public Object getValueAt(final int rowIndex, final int columnIndex) {
        Object o = rows.get(rowIndex)[columnIndex];
        if (o == null) {
            return null;
        }
        final String col = getColumnName(columnIndex);
        if (LogConstants.STMT_TYPE_COLUMN.equals(col)) {
            o = StatementType.fromId(((Byte) o).byteValue());
        } else if (col.endsWith("TIME")) {
            o = TimeUnit.NANOSECONDS.toMillis(((Number) o).longValue());
        }
        return o;

    }


    public Object getRawValueAt(final int rowIndex, final int columnIndex) {
        return rows.get(rowIndex)[columnIndex];
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return columnNames.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return columnTypes.get(columnIndex);
    }

    public UUID getIdAtRow(final int rowIndex) {
        return (UUID) rows.get(rowIndex)[0];
    }


    public Object getValueAt(final int rowIndex, final String columnName) {
        final int columnIndex = columnNames.indexOf(columnName);
        if (columnIndex < 0) {
            return null;
        }
        Object o = rows.get(rowIndex)[columnIndex];
        if (LogConstants.STMT_TYPE_COLUMN.equals(columnName)) {
            o = StatementType.fromId(((Byte) o).byteValue());
        }
        return o;
    }
}
