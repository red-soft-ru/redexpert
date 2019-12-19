package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.*;
import org.executequery.gui.resultset.RecordDataItem;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTableObject extends DefaultDatabaseObject implements DatabaseTableObject {
    public AbstractTableObject(DatabaseHost host, String metaDataKey) {
        super(host, metaDataKey);
    }

    protected List<TableDataChange> tableDataChanges;

    protected List<TableDataChange> tableDataChanges() {

        if (tableDataChanges == null) {

            tableDataChanges = new ArrayList<TableDataChange>();
        }
        return tableDataChanges;
    }

    public void clearDataChanges() {
        if (tableDataChanges != null) {
            tableDataChanges.clear();
        }
        tableDataChanges = null;
    }

    public String prepareStatement(List<String> columns, List<RecordDataItem> changes) {

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(getNameWithPrefixForQuery()).append(" SET ");
        for (String column : columns) {
            sb.append(column).append(" = ?,");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(" WHERE ");

        boolean applied = false;
        List<DatabaseColumn> cols = getColumns();
        for (int i = 0; i < cols.size(); i++) {
            DatabaseColumn column = cols.get(i);
            String col = cols.get(i).getName();
            RecordDataItem rdi = changes.get(i);
            if (column.isGenerated())
                rdi.setGenerated(true);
            else {
                if (applied) {
                    sb.append(" AND ");
                }
                if (rdi.isValueNull())
                    sb.append(col).append(" is NULL ");
                else
                    sb.append(col).append(" = ? ");
                applied = true;
            }
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append("\nORDER BY " + cols.get(0) + " \n");
        sb.append("ROWS 1");
        return sb.toString();
    }

    public String prepareStatementDeleting(List<RecordDataItem> changes) {

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(getNameWithPrefixForQuery());
        sb.append(" WHERE ");

        boolean applied = false;
        List<DatabaseColumn> cols = getColumns();
        for (int i = 0; i < cols.size(); i++) {
            DatabaseColumn column = cols.get(i);
            String col = cols.get(i).getName();
            RecordDataItem rdi = changes.get(i);
            if (column.isGenerated())
                rdi.setGenerated(true);
            else {
                if (applied) {

                    sb.append(" AND ");
                }
                if (rdi.isValueNull())
                    sb.append(col).append(" is NULL ");
                else
                    sb.append(col).append(" = ? ");
                applied = true;
            }
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append("\nORDER BY " + cols.get(0) + " \n");
        sb.append("ROWS 1");
        return sb.toString();
    }

    public String prepareStatementAdding(List<String> columns, List<RecordDataItem> changes) {

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(getNameWithPrefixForQuery());
        String columnsForQuery = " (";
        String values = " VALUES (";
        boolean applied = false;
        List<DatabaseColumn> cols = getColumns();
        for (int i = 0; i < cols.size(); i++) {
            DatabaseColumn column = cols.get(i);
            String col = cols.get(i).getName();
            RecordDataItem rdi = changes.get(i);
            if (column.isGenerated() || rdi.isNewValueNull() && column.isIdentity())
                rdi.setGenerated(true);
            else {
                if (applied) {

                    columnsForQuery += " , ";
                    values += " , ";
                }
                columnsForQuery += col;
                values += "?";
                applied = true;
            }
        }
        columnsForQuery += ") ";
        values += ") ";
        sb.append(columnsForQuery).append(values);
        return sb.toString();
    }

    public void addTableDataChange(TableDataChange tableDataChange) {

        if (tableDataChanges != null) {
            for (int i = 0; i < tableDataChanges.size(); i++)
                if (tableDataChange.getRowDataForRow() == tableDataChanges.get(i).getRowDataForRow()) {
                    tableDataChanges.remove(i);
                    i--;
                }
        }
        tableDataChanges().add(tableDataChange);
    }

    @Override
    public void removeTableDataChange(List<RecordDataItem> row) {
        if (tableDataChanges != null) {
            for (int i = 0; i < tableDataChanges.size(); i++)
                if (row == tableDataChanges.get(i).getRowDataForRow()) {
                    tableDataChanges.remove(i);
                    i--;
                }
        }
    }

    TableDataChangeWorker tableDataChangeExecutor;

    public int applyTableDataChanges() {

        if (!hasTableDataChanges()) {

            return 1;
        }

        tableDataChangeExecutor = new TableDataChangeWorker(this);
        boolean success = tableDataChangeExecutor.apply(tableDataChanges);
        if (success) {

            clearDataChanges();
        }

        return success ? 1 : 0;
    }

    public boolean hasTableDataChanges() {

        return tableDataChanges != null && !tableDataChanges.isEmpty();
    }

    public boolean allowsChildren() {
        return true;
    }
}