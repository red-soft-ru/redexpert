package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.*;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractTableObject extends DefaultDatabaseObject implements DatabaseTableObject {
    public AbstractTableObject(DatabaseMetaTag metaTag, String metaDataKey) {
        super(metaTag, metaDataKey);
    }

    public AbstractTableObject(DatabaseHost host, String metaDataKey) {
        super(host, metaDataKey);
    }

    protected int typeTree;
    protected List<TableDataChange> tableDataChanges;

    protected DatabaseObject dependObject;

    protected String sqlSecurity;

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
        for (String column : columns)
            sb.append(MiscUtils.getFormattedObject(column, getHost().getDatabaseConnection())).append(" = ?,");

        sb.deleteCharAt(sb.length() - 1);
        sb.append(" WHERE ");

        boolean applied = false;
        List<DatabaseColumn> cols = getColumns();
        for (int i = 0; i < cols.size(); i++) {

            DatabaseColumn column = cols.get(i);
            String col = MiscUtils.getFormattedObject(cols.get(i).getName(), getHost().getDatabaseConnection());
            RecordDataItem rdi = changes.get(i);

            if (column.isGenerated()) {
                rdi.setGenerated(true);

            } else {

                if (applied)
                    sb.append(" AND ");
                sb.append(col).append(rdi.isValueNull() ? " is NULL " : " = ? ");
                applied = true;
            }
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append("\nORDER BY ").append(MiscUtils.getFormattedObject(cols.get(0).getName(), getHost().getDatabaseConnection())).append(" \n");
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
            String col = MiscUtils.getFormattedObject(cols.get(i).getName(), getHost().getDatabaseConnection());
            RecordDataItem rdi = changes.get(i);

            if (column.isGenerated()) {
                rdi.setGenerated(true);

            } else {

                if (applied)
                    sb.append(" AND ");
                sb.append(col).append(rdi.isValueNull() ? " is NULL " : " = ? ");
                applied = true;
            }
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append("\nORDER BY ").append(MiscUtils.getFormattedObject(cols.get(0).getName(), getHost().getDatabaseConnection())).append(" \n");
        sb.append("ROWS 1");

        return sb.toString();
    }

    public String prepareStatementAdding(List<String> columns, List<RecordDataItem> changes) {

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(getNameWithPrefixForQuery());
        StringBuilder columnsForQuery = new StringBuilder(" (");
        StringBuilder values = new StringBuilder(" VALUES (");

        boolean applied = false;
        List<DatabaseColumn> cols = getColumns();
        for (int i = 0; i < cols.size(); i++) {

            DatabaseColumn column = cols.get(i);
            String col = MiscUtils.getFormattedObject(cols.get(i).getName(), getHost().getDatabaseConnection());
            RecordDataItem rdi = changes.get(i);

            if (column.isGenerated() || column.isIdentity()
                    && rdi.isNewValueNull() || column.getDefaultValue() != null && rdi.isNewValueNull()
                    || column.getDomainDefaultValue() != null && rdi.isNewValueNull()) {
                rdi.setGenerated(true);

            } else {

                if (applied) {
                    columnsForQuery.append(" , ");
                    values.append(" , ");
                }
                columnsForQuery.append(col);
                values.append("?");
                applied = true;
            }
        }

        columnsForQuery.append(") ");
        values.append(") ");
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

    protected List<DatabaseColumn> columns;

    public List<DatabaseColumn> getColumns()
    {
        return getColumns(false);
    }

    public synchronized List<DatabaseColumn> getColumns(boolean loadForAllTables) throws DataSourceException {

        if (!isMarkedForReloadCols() && columns != null) {

            return columns;
        }

        // otherwise cleanup existing references
        if (columns != null) {

            columns.clear();
            columns = null;
        }

        DatabaseHost host = getHost();
        if (host != null) {
            try {

                if (typeTree == TreePanel.DEFAULT) {
                    fullLoadCols = loadForAllTables;
                    loadColumns();
                }
                if (typeTree == TreePanel.DEPENDED_ON) {
                    preColumns = getDependedColumns();
                    preColumnsToColumns();
                }
                if (typeTree == TreePanel.DEPENDENT) {
                    preColumns = getDependentColumns();
                    preColumnsToColumns();
                }

            } catch (DataSourceException e) {

                // catch and re-throw here to create
                // an empty column list so we don't
                // keep hitting the same error
                columns = databaseColumnListWithSize(0);
                throw e;

            } finally {
                setMarkedForReloadCols(false);
            }
        }

        if (columns == null)
            columns = databaseColumnListWithSize(0);

        return columns;
    }

    protected void preColumnsToColumns() {
        if (preColumns != null) {
            columns = databaseColumnListWithSize(preColumns.size());
            for (DatabaseColumn i : preColumns) {
                DatabaseTableColumn databaseTableColumn = new DatabaseTableColumn(this, i);
                if (i.getConstraints() != null) {
                    for (ColumnConstraint constraint : i.getConstraints()) {
                        constraint.setColumn(databaseTableColumn);
                        databaseTableColumn.addConstraint(constraint);
                    }
                }
                columns.add(databaseTableColumn);
            }
        }
    }

    public void finishLoadColumns() {
        preColumnsToColumns();
        lockLoadingCols(false);
    }


    public List<NamedObject> getObjects() throws DataSourceException {

        List<DatabaseColumn> _columns = getColumns(false);
        if (_columns == null) {

            return null;
        }

        List<NamedObject> objects = new ArrayList<NamedObject>(_columns.size());
        for (DatabaseColumn i : _columns) {

            objects.add(i);
        }

        return objects;
    }

    private List<DatabaseColumn> getDependedColumns() {
        ResultSet rs = null;

        List<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();

        try {
            DatabaseMetaData dmd = getHost().getDatabaseMetaData();
            String packageField = "";
            if (dmd.getDatabaseMajorVersion() > 2)
                packageField = "and (T1.RDB$PACKAGE_NAME IS NULL)\n";
            Connection connection = dmd.getConnection();
            Statement statement = null;
            String firebirdSql = "select distinct \n" +
                    "D.RDB$FIELD_NAME as FK_Field\n" +
                    "from RDB$REF_CONSTRAINTS B, RDB$RELATION_CONSTRAINTS A, RDB$RELATION_CONSTRAINTS C,\n" +
                    "RDB$INDEX_SEGMENTS D, RDB$INDEX_SEGMENTS E, RDB$INDICES I\n" +
                    "where (A.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY') and\n" +
                    "(A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME) and\n" +
                    "(B.RDB$CONST_NAME_UQ=C.RDB$CONSTRAINT_NAME) and (C.RDB$INDEX_NAME=D.RDB$INDEX_NAME) and\n" +
                    "(A.RDB$INDEX_NAME=E.RDB$INDEX_NAME) and\n" +
                    "(A.RDB$INDEX_NAME=I.RDB$INDEX_NAME)\n" +
                    "and (A.RDB$RELATION_NAME = '" + dependObject.getName() + "')\n" +
                    "and (C.RDB$RELATION_NAME = '" + getName() + "')\n" +
                    "union all\n" +
                    "select cast(t1.RDB$FIELD_NAME as varchar(64))\n" +
                    "from RDB$DEPENDENCIES t1 where (t1.RDB$DEPENDENT_NAME = '" + dependObject.getName() + "')\n" +
                    "and (t1.RDB$DEPENDENT_TYPE = 0)\n" +
                    packageField +
                    "and (T1.RDB$DEPENDED_ON_NAME = '" + getName() + "')\n" +
                    "union all\n" +
                    "select distinct cast(d.rdb$field_name as varchar(64))\n" +
                    "from rdb$dependencies d, rdb$relation_fields f\n" +
                    "where (d.rdb$dependent_type = 3) and\n" +
                    "(d.rdb$dependent_name = f.rdb$field_source)\n" +
                    "and (f.rdb$relation_name = '" + dependObject.getName() + "')\n" +
                    "and (d.RDB$DEPENDED_ON_NAME = '" + getName() + "')\n" +
                    "order by 1";

            statement = connection.createStatement();
            rs = statement.executeQuery(firebirdSql);


            while (rs.next()) {

                DefaultDatabaseColumn column = new DefaultDatabaseColumn();

                column.setName(rs.getString(1));

                columns.add(column);
            }
            releaseResources(rs, connection);

            int columnCount = columns.size();
            if (columnCount > 0) {

                // check for primary keys
                rs = dmd.getPrimaryKeys(null, null, getName());
                while (rs.next()) {

                    String pkColumn = rs.getString(4);

                    // find the pk column in the previous list
                    for (int i = 0; i < columnCount; i++) {

                        DatabaseColumn column = columns.get(i);
                        String columnName = column.getName();

                        if (columnName.equalsIgnoreCase(pkColumn)) {
                            ((DefaultDatabaseColumn) column).setPrimaryKey(true);
                            break;
                        }

                    }

                }
                releaseResources(rs, connection);

                // check for foreign keys
                rs = dmd.getImportedKeys(null, null, getName());
                while (rs.next()) {
                    String fkColumn = rs.getString(8);

                    // find the fk column in the previous list
                    for (int i = 0; i < columnCount; i++) {
                        DatabaseColumn column = columns.get(i);
                        String columnName = column.getName();
                        if (columnName.equalsIgnoreCase(fkColumn)) {
                            ((DefaultDatabaseColumn) column).setForeignKey(true);
                            break;
                        }
                    }

                }

            }

            return columns;

        } catch (SQLException e) {

            if (Log.isDebugEnabled()) {

                Log.error("Error retrieving column data for table " + getName()
                        + " using connection " + getHost().getDatabaseConnection(), e);
            }

            return columns;

//            throw new DataSourceException(e);

        } finally {

            try {
                releaseResources(rs, getHost().getDatabaseMetaData().getConnection());
            } catch (SQLException throwables) {
                releaseResources(rs, null);
            }
        }
    }

    private List<DatabaseColumn> getDependentColumns() {
        ResultSet rs = null;

        List<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();

        try {
            DatabaseMetaData dmd = getHost().getDatabaseMetaData();
            String packageField = "";
            if (dmd.getDatabaseMajorVersion() > 2) {
                packageField = "and (T1.RDB$PACKAGE_NAME IS NULL)\n";
            }
            Connection connection = dmd.getConnection();
            Statement statement = null;
            String firebirdSql = "select " +
                    "E.RDB$FIELD_NAME as OnField\n" +
                    "from RDB$REF_CONSTRAINTS B, RDB$RELATION_CONSTRAINTS A, RDB$RELATION_CONSTRAINTS C,\n" +
                    "RDB$INDEX_SEGMENTS D, RDB$INDEX_SEGMENTS E\n" +
                    "where (A.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY') and\n" +
                    "(A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME) and\n" +
                    "(B.RDB$CONST_NAME_UQ=C.RDB$CONSTRAINT_NAME) and (C.RDB$INDEX_NAME=D.RDB$INDEX_NAME) and\n" +
                    "(A.RDB$INDEX_NAME=E.RDB$INDEX_NAME)\n" +
                    "and (C.RDB$RELATION_NAME = '" + dependObject.getName() + "')\n" +
                    "and (A.RDB$RELATION_NAME = '" + getName() + "')\n" +
                    "union all\n" +
                    "select cast(t1.RDB$FIELD_NAME as varchar(64))\n" +
                    "from RDB$DEPENDENCIES t1 where (t1.RDB$DEPENDENT_NAME = '" + dependObject.getName() + "')\n" +
                    "and (t1.RDB$DEPENDENT_TYPE = 0)\n" +
                    packageField +
                    "and t1.RDB$DEPENDED_ON_NAME = '" + getName() + "'\n" +
                    "union all\n" +
                    "select distinct cast(d.rdb$field_name as varchar(64))\n" +
                    "from rdb$dependencies d, rdb$relation_fields f\n" +
                    "where (d.rdb$dependent_type = 3) and\n" +
                    "(d.rdb$dependent_name = f.rdb$field_source)\n" +
                    "and (f.rdb$relation_name = '" + dependObject.getName() + "')\n" +
                    "and  d.rdb$depended_on_name = '" + getName() + "'\n" +
                    "order by 1";

            statement = connection.createStatement();
            rs = statement.executeQuery(firebirdSql);


            while (rs.next()) {

                DefaultDatabaseColumn column = new DefaultDatabaseColumn();

                column.setName(rs.getString(1));

                columns.add(column);
            }
            releaseResources(rs, connection);

            int columnCount = columns.size();
            if (columnCount > 0) {

                // check for primary keys
                rs = dmd.getPrimaryKeys(null, null, getName());
                while (rs.next()) {

                    String pkColumn = rs.getString(4);

                    // find the pk column in the previous list
                    for (int i = 0; i < columnCount; i++) {

                        DatabaseColumn column = columns.get(i);
                        String columnName = column.getName();

                        if (columnName.equalsIgnoreCase(pkColumn)) {
                            ((DefaultDatabaseColumn) column).setPrimaryKey(true);
                            break;
                        }

                    }

                }
                releaseResources(rs, connection);

                // check for foreign keys
                rs = dmd.getImportedKeys(null, null, getName());
                while (rs.next()) {
                    String fkColumn = rs.getString(8);

                    // find the fk column in the previous list
                    for (int i = 0; i < columnCount; i++) {
                        DatabaseColumn column = columns.get(i);
                        String columnName = column.getName();
                        if (columnName.equalsIgnoreCase(fkColumn)) {
                            ((DefaultDatabaseColumn) column).setForeignKey(true);
                            break;
                        }
                    }

                }

            }

            return columns;

        } catch (SQLException e) {

            if (Log.isDebugEnabled()) {

                Log.error("Error retrieving column data for table " + getName()
                        + " using connection " + getHost().getDatabaseConnection(), e);
            }

            return columns;

//            throw new DataSourceException(e);

        } finally {

            try {
                releaseResources(rs, getHost().getDatabaseMetaData().getConnection());
            } catch (SQLException throwables) {
                releaseResources(rs, null);
            }
        }
    }





    private List<DatabaseColumn> databaseColumnListWithSize(int size) {

        return Collections.synchronizedList(new ArrayList<DatabaseColumn>(size));
    }

    @Override
    public int getTypeTree() {
        return typeTree;
    }

    @Override
    public void setTypeTree(int typeTree) {
        this.typeTree = typeTree;
    }

    @Override
    public DatabaseObject getDependObject() {
        return dependObject;
    }

    public void setDependObject(DatabaseObject dependObject) {
        this.dependObject = dependObject;
    }

    public String getSqlSecurity() {
        checkOnReload(sqlSecurity);
        return sqlSecurity;
    }

    public void setSqlSecurity(String sqlSecurity) {
        this.sqlSecurity = sqlSecurity;
    }

    @Override
    public void reset() {
        super.reset();
        setMarkedForReloadCols(true);
    }
}