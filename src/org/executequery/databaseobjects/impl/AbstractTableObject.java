package org.executequery.databaseobjects.impl;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
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
    public AbstractTableObject(DatabaseHost host, String metaDataKey) {
        super(host, metaDataKey);
    }

    protected int typeTree;
    protected List<TableDataChange> tableDataChanges;

    protected DatabaseObject dependObject;

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
            sb.append(MiscUtils.getFormattedObject(column)).append(" = ?,");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(" WHERE ");

        boolean applied = false;
        List<DatabaseColumn> cols = getColumns();
        for (int i = 0; i < cols.size(); i++) {
            DatabaseColumn column = cols.get(i);
            String col = MiscUtils.getFormattedObject(cols.get(i).getName());
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
            String col = MiscUtils.getFormattedObject(cols.get(i).getName());
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
            String col = MiscUtils.getFormattedObject(cols.get(i).getName());
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

    protected List<DatabaseColumn> columns;

    public synchronized List<DatabaseColumn> getColumns() throws DataSourceException {

        if (!isMarkedForReload() && columns != null) {

            return columns;
        }

        // otherwise cleanup existing references
        if (columns != null) {

            columns.clear();
            columns = null;
        }

        DatabaseHost host = getHost();
        if (host != null) {

            ResultSet rs = null;
            try {

                List<DatabaseColumn> _columns = null;
                if (typeTree == TreePanel.DEFAULT)
                    _columns = host.getColumns(getCatalogName(),
                            getSchemaName(),
                            getName());
                if (typeTree == TreePanel.DEPENDED_ON)
                    _columns = getDependedColumns();
                if (typeTree == TreePanel.DEPENDENT)
                    _columns = getDependentColumns();
                if (_columns != null) {

                    columns = databaseColumnListWithSize(_columns.size());
                    for (DatabaseColumn i : _columns) {

                        columns.add(new DatabaseTableColumn(this, i));
                    }

                    // reload and define the constraints
                    String _catalog = host.getCatalogNameForQueries(getCatalogName());
                    String _schema = host.getSchemaNameForQueries(getSchemaName());
                    DatabaseMetaData dmd = host.getDatabaseMetaData();
                    rs = dmd.getPrimaryKeys(_catalog, _schema, getName());
                    while (rs.next()) {

                        String pkColumn = rs.getString(4);
                        for (DatabaseColumn i : columns) {

                            if (i.getName().equalsIgnoreCase(pkColumn)) {

                                DatabaseTableColumn column = (DatabaseTableColumn) i;
                                TableColumnConstraint constraint = new TableColumnConstraint(column, ColumnConstraint.PRIMARY_KEY);

                                constraint.setName(rs.getString(6));
                                constraint.setMetaData(resultSetRowToMap(rs));
                                column.addConstraint(constraint);
                                break;

                            }
                        }
                    }
                    releaseResources(rs, null);

                    try {

                        // TODO: XXX

                        // sapdb amd maxdb dump on imported/exported keys
                        // surround with try/catch hack to get at least a columns list

                        rs = dmd.getImportedKeys(_catalog, _schema, getName());

                        while (rs.next()) {

                            String fkColumn = rs.getString(8);

                            for (DatabaseColumn i : columns) {

                                if (i.getName().equalsIgnoreCase(fkColumn)) {
                                    DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
                                    DatabaseTableColumn column = (DatabaseTableColumn) i;
                                    List<String> row = new ArrayList<>();
                                    for (int g = 1; g <= rs.getMetaData().getColumnCount(); g++)
                                        row.add(rs.getString(g));
                                    TableColumnConstraint constraint = new TableColumnConstraint(column, ColumnConstraint.FOREIGN_KEY);
                                    constraint.setReferencedCatalog(rs.getString(1));
                                    constraint.setReferencedSchema(rs.getString(2));
                                    constraint.setReferencedTable(rs.getString(3));
                                    constraint.setReferencedColumn(rs.getString(4));
                                    constraint.setName(rs.getString(12));
                                    constraint.setDeferrability(rs.getShort(14));
                                    constraint.setMetaData(resultSetRowToMap(rs));
                                    ResultSet rulesRS = querySender.getResultSet("select RDB$REF_CONSTRAINTS.RDB$UPDATE_RULE, RDB$REF_CONSTRAINTS.RDB$DELETE_RULE" +
                                            " from rdb$ref_constraints where RDB$REF_CONSTRAINTS.RDB$CONSTRAINT_NAME='" + constraint.getName() + "'").getResultSet();
                                    try {
                                        if (rulesRS.next()) {
                                            for (int g = 1; g <= 2; g++) {
                                                String rule = rulesRS.getString(g);
                                                if (rule != null) {
                                                    if (g == 1)
                                                        constraint.setUpdateRule(rule.trim());
                                                    else
                                                        constraint.setDeleteRule(rule.trim());
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        querySender.releaseResources();
                                    }
                                    column.addConstraint(constraint);
                                    break;

                                }
                            }
                        }

                    } catch (SQLException e) {
                        Log.error("Error get imported keys for " + getName() + ": " + e.getMessage());
                    }
                }

            } catch (DataSourceException e) {

                // catch and re-throw here to create
                // an empty column list so we don't
                // keep hitting the same error
                columns = databaseColumnListWithSize(0);
                throw e;

            } catch (SQLException e) {

                // catch and re-throw here to create
                // an empty column list so we don't
                // keep hitting the same error
                columns = databaseColumnListWithSize(0);
                throw new DataSourceException(e);

            } finally {

                releaseResources(rs, null);
                setMarkedForReload(false);
            }

        }
        return columns;
    }

    public List<NamedObject> getObjects() throws DataSourceException {

        List<DatabaseColumn> _columns = getColumns();
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

}