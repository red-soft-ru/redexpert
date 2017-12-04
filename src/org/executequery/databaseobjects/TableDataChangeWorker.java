/*
 * TableDataChangeWorker.java
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

package org.executequery.databaseobjects;

import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takis Diakoumis
 * @version $Revision$
 * @date $Date$
 */
public class TableDataChangeWorker {

    private Connection connection;

    private DatabaseTable table;

    private DatabaseTableObject tableObject;

    private PreparedStatement statement;

    public TableDataChangeWorker(DatabaseTable table) {

        this.table = table;
    }

    public TableDataChangeWorker(DatabaseTableObject table) {

        this.tableObject = table;
        if (table instanceof DatabaseTable)
            this.table = (DatabaseTable) table;
        else this.table = null;
    }

    public boolean apply(List<TableDataChange> rows) {

        int result = 0;
        for (TableDataChange tableDataChange : rows) {

            if (connection == null) {

                createConnection(tableObject);
            }

            List<RecordDataItem> row = tableDataChange.getRowDataForRow();
            if (row.get(0).isDeleted()) {
                if (table != null && table.hasPrimaryKey())
                    result += executeDeletingWithPK(connection, table, row);
                else
                    result += executedDeleting(connection, tableObject, row);
            } else if (row.get(0).isNew()) {
                result += executeAdding(connection, tableObject, row);
            } else if (table != null && table.hasPrimaryKey())
                result += executeWithPK(connection, table, row);
            else
                result += executeChange(connection, tableObject, row);
        }

        if (result == rows.size()) {

            commit();
            return true;

        } else {

            return false;
        }

    }

    private void createConnection(DatabaseTableObject table) {

        try {

            connection = ConnectionManager.getConnection(table.getHost().getDatabaseConnection());
            connection.setAutoCommit(false);

        } catch (SQLException e) {

            throw handleException(e);
        }

    }

    private int executeWithPK(Connection connection, DatabaseTable table, List<RecordDataItem> values) {

        List<String> columns = new ArrayList<String>();
        List<RecordDataItem> changes = new ArrayList<RecordDataItem>();
        for (RecordDataItem item : values) {

            if (item.isChanged()) {

                changes.add(item);
                columns.add(item.getName());
            }

        }

        if (changes.isEmpty()) {

            return 0;
        }

        try {

            int n = changes.size();
            String sql = table.prepareStatementWithPK(columns);

            Log.info("Executing data change using statement - [ " + sql + " ]");

            statement = connection.prepareStatement(sql);
            for (int i = 0; i < n; i++) {

                RecordDataItem recordDataItem = changes.get(i);
                if (!recordDataItem.isNewValueNull()) {

                    statement.setObject((i + 1), recordDataItem.getNewValue(), recordDataItem.getDataType());

                } else {

                    statement.setNull((i + 1), Types.NULL);
                }

            }

            List<String> primaryKeys = table.getPrimaryKeyColumnNames();
            for (String primaryKey : primaryKeys) {

                n++;
                statement.setObject(n, valueForKey(primaryKey, values));
            }

            return statement.executeUpdate();

        } catch (Exception e) {

            rollback();
            throw handleException(e);

        } finally {

            if (statement != null) {

                try {
                    statement.close();
                } catch (SQLException e) {
                }
                statement = null;
            }

        }

    }

    private int executeDeletingWithPK(Connection connection, DatabaseTable table, List<RecordDataItem> values) {

        List<String> columns = new ArrayList<String>();
        List<RecordDataItem> changes = new ArrayList<RecordDataItem>();
        for (RecordDataItem item : values) {

            if (item.isDeleted()) {

                changes.add(item);
                columns.add(item.getName());
            }

        }

        if (changes.isEmpty()) {

            return 0;
        }

        try {

            int n = 0;
            String sql = table.prepareStatementDeletingWithPK();

            Log.info("Executing data change using statement - [ " + sql + " ]");

            statement = connection.prepareStatement(sql);

            List<String> primaryKeys = table.getPrimaryKeyColumnNames();
            for (String primaryKey : primaryKeys) {

                n++;
                statement.setObject(n, valueForKey(primaryKey, values));
            }

            return statement.executeUpdate();

        } catch (Exception e) {

            rollback();
            throw handleException(e);

        } finally {

            if (statement != null) {

                try {
                    statement.close();
                } catch (SQLException e) {
                }
                statement = null;
            }

        }

    }

    private int executeAdding(Connection connection, DatabaseTableObject table, List<RecordDataItem> values) {
        List<String> columns = new ArrayList<String>();
        List<RecordDataItem> changes = new ArrayList<RecordDataItem>();
        for (RecordDataItem item : values) {

            if (item.isNew()) {

                changes.add(item);
                columns.add(item.getName());
            }

        }

        if (changes.isEmpty()) {

            return 0;
        }

        try {

            int n = changes.size();
            String sql = table.prepareStatementAdding(columns, values);

            Log.info("Executing data change using statement - [ " + sql + " ]");

            statement = connection.prepareStatement(sql);
            for (int i = 0, g = 0; i < n; i++, g++) {
                RecordDataItem recordDataItem = changes.get(g);
                if (!recordDataItem.isGenerated()) {
                    if (!recordDataItem.isNewValueNull()) {

                        statement.setObject((i + 1), recordDataItem.getNewValue(), recordDataItem.getDataType());

                    } else {

                        statement.setNull((i + 1), Types.NULL);
                    }
                } else {
                    i--;
                    n--;
                }

            }
            return statement.executeUpdate();

        } catch (Exception e) {

            rollback();
            throw handleException(e);

        } finally {

            if (statement != null) {

                try {
                    statement.close();
                } catch (SQLException e) {
                }
                statement = null;
            }

        }

    }

    private int executedDeleting(Connection connection, DatabaseTableObject table, List<RecordDataItem> values) {
        List<String> columns = new ArrayList<String>();
        List<RecordDataItem> changes = new ArrayList<RecordDataItem>();
        for (RecordDataItem item : values) {

            if (item.isDeleted()) {

                changes.add(item);
                columns.add(item.getName());
            }

        }

        if (changes.isEmpty()) {

            return 0;
        }

        try {

            int n = 0;
            String sql = table.prepareStatementDeleting(values);

            Log.info("Executing data change using statement - [ " + sql + " ]");

            statement = connection.prepareStatement(sql);

            for (RecordDataItem rdi : values) {

                if (!rdi.isValueNull() && !rdi.isGenerated()) {
                    n++;
                    statement.setObject(n, rdi.getValue());
                }
            }

            return statement.executeUpdate();

        } catch (Exception e) {

            rollback();
            throw handleException(e);

        } finally {

            if (statement != null) {

                try {
                    statement.close();
                } catch (SQLException e) {
                }
                statement = null;
            }

        }

    }

    private int executeChange(Connection connection, DatabaseTableObject table, List<RecordDataItem> values) {
        List<String> columns = new ArrayList<String>();
        List<RecordDataItem> changes = new ArrayList<RecordDataItem>();
        for (RecordDataItem item : values) {

            if (item.isChanged()) {

                changes.add(item);
                columns.add(item.getName());
            }

        }

        if (changes.isEmpty()) {

            return 0;
        }

        try {

            int n = changes.size();
            String sql = table.prepareStatement(columns, values);

            Log.info("Executing data change using statement - [ " + sql + " ]");

            statement = connection.prepareStatement(sql);
            for (int i = 0; i < n; i++) {

                RecordDataItem recordDataItem = changes.get(i);
                if (!recordDataItem.isNewValueNull()) {

                    statement.setObject((i + 1), recordDataItem.getNewValue(), recordDataItem.getDataType());

                } else {

                    statement.setNull((i + 1), Types.NULL);
                }

            }
            for (RecordDataItem rdi : values) {

                if (!rdi.isValueNull() && !rdi.isGenerated()) {
                    n++;
                    statement.setObject(n, rdi.getValue());
                }
            }

            return statement.executeUpdate();

        } catch (Exception e) {

            rollback();
            throw handleException(e);

        } finally {

            if (statement != null) {

                try {
                    statement.close();
                } catch (SQLException e) {
                }
                statement = null;
            }

        }

    }

    private Object valueForKey(String primaryKey, List<RecordDataItem> values) {

        for (RecordDataItem recordDataItem : values) {

            if (primaryKey.equalsIgnoreCase(recordDataItem.getName())) {

                return recordDataItem.getValue();
            }

        }

        return null;
    }

    public void commit() {

        try {

            connection.commit();

        } catch (SQLException e) {

            throw handleException(e);
        }

    }

    private DataSourceException handleException(Throwable e) {

        return new DataSourceException(e);
    }

    public void rollback() {

        try {

            connection.rollback();

        } catch (SQLException e) {

            throw handleException(e);
        }

    }

    public void close() {

        if (connection != null) {

            try {
                boolean keepAlive = true;
                if (!keepAlive)
                    connection.close();
            } catch (SQLException e) {
            }
            connection = null;
        }

    }

    public void cancel() {

        if (statement != null) {

            try {

                statement.cancel();
                rollback();

            } catch (SQLException e) {

                Log.debug(e.getMessage());
            }

        }

    }

}


