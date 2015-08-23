package org.executequery.databaseobjects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision$
 * @date     $Date$
 */
public class TableDataChangeWorker {

    private Connection connection;
    
    private DatabaseTable table;

    private PreparedStatement statement;

    public TableDataChangeWorker(DatabaseTable table) {

        this.table = table;
    }

    public boolean apply(List<TableDataChange> rows) {

        int result = 0;
        for (TableDataChange tableDataChange : rows) {

            if (connection == null) {
                
                createConnection(table);
            }
            
            List<RecordDataItem> row = tableDataChange.getRowDataForRow();
            result += execute(connection, table, row);
        }

        if (result == rows.size()) {
            
            commit();
            return true;
        
        } else {
            
            return false;
        }

    }

    private void createConnection(DatabaseTable table) {

        try {

            connection = ConnectionManager.getConnection(table.getHost().getDatabaseConnection());
            connection.setAutoCommit(false);

        } catch (SQLException e) {
        
            throw handleException(e);
        }
        
    }

    private int execute(Connection connection, DatabaseTable table, List<RecordDataItem> values) {

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
            String sql = table.prepareStatement(columns);
            
            Log.info("Executing data change using statement - [ " + sql + " ]");
            
            statement = connection.prepareStatement(sql);
            for (int i = 0; i < n; i++) {

                RecordDataItem recordDataItem = changes.get(i);
                if (!recordDataItem.isSQLValueNull()) {

                    statement.setObject((i + 1), recordDataItem.getValueAsType(), recordDataItem.getDataType());

                } else {
                    
                    statement.setNull((i + 1), recordDataItem.getDataType());
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
                } catch (SQLException e) {}
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
                connection.close();
            } catch (SQLException e) {}
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
