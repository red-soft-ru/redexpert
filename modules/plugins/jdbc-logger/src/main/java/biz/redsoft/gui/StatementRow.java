package biz.redsoft.gui;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by vasiliy on 26.12.16.
 */
public class StatementRow {

    final List<String> columnNames = new ArrayList<>();
    final List<Class<?>> columnTypes = new ArrayList<>();

    Object[] data;

    StatementRow() {

        // 0
        columnNames.add(LogConstants.ID_COLUMN);
        columnTypes.add(UUID.class);

        // 1
        columnNames.add(LogConstants.TSTAMP_COLUMN);
        columnTypes.add(Timestamp.class);

        // 2
        columnNames.add(LogConstants.STMT_TYPE_COLUMN);
        columnTypes.add(Byte.class);

        // 3
        columnNames.add(LogConstants.RAW_SQL_COLUMN);
        columnTypes.add(String.class);

        // 4
        columnNames.add(LogConstants.EXEC_PLUS_RSET_USAGE_TIME);
        columnTypes.add(Long.class);

        // 5
        columnNames.add(LogConstants.EXEC_TIME_COLUMN);
        columnTypes.add(Long.class);

        // 6
        columnNames.add(LogConstants.RSET_USAGE_TIME);
        columnTypes.add(Long.class);

        // 7
        columnNames.add(LogConstants.FETCH_TIME_COLUMN);
        columnTypes.add(Long.class);

        // 8
        columnNames.add(LogConstants.NB_ROWS_COLUMN);
        columnTypes.add(Integer.class);

        // 9
        columnNames.add(LogConstants.THREAD_NAME_COLUMN);
        columnTypes.add(String.class);

        // 10
        columnNames.add(LogConstants.CONNECTION_NUMBER_COLUMN);
        columnTypes.add(Integer.class);

        // 11
        columnNames.add(LogConstants.TIMEOUT_COLUMN);
        columnTypes.add(Integer.class);

        // 12
        columnNames.add(LogConstants.AUTOCOMMIT_COLUMN);
        columnTypes.add(Boolean.class);

        // 13
        columnNames.add(LogConstants.ERROR_COLUMN);
        columnTypes.add(Integer.class);

        data = new Object[columnNames.size()];
    }

    public Object[] getData() {
        return this.data;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<Class<?>> getColumnTypes() {
        return columnTypes;
    }

    void computeExecPlusRset() {
        if (data[columnNames.indexOf(LogConstants.EXEC_TIME_COLUMN)] != null &&
                data[columnNames.indexOf(LogConstants.RSET_USAGE_TIME)] != null)
            data[columnNames.indexOf(LogConstants.EXEC_PLUS_RSET_USAGE_TIME)] =
                    (Long)data[columnNames.indexOf(LogConstants.EXEC_TIME_COLUMN)] +
                            (Long)data[columnNames.indexOf(LogConstants.RSET_USAGE_TIME)];
    }

    public void addIDColumn(UUID uuid) {
        data[columnNames.indexOf(LogConstants.ID_COLUMN)] = uuid;
    }

    public void addTimestampColumn(long timestamp) {
        data[columnNames.indexOf(LogConstants.TSTAMP_COLUMN)] = new Timestamp(timestamp);
    }

    public void addStatementTypeColumn(Byte type) {
        data[columnNames.indexOf(LogConstants.STMT_TYPE_COLUMN)] = type;
    }

    public void addRawSQLColumn(String rawSql) {
        data[columnNames.indexOf(LogConstants.RAW_SQL_COLUMN)] = rawSql;
    }

    public void addExecTimeColumn(long time) {
        data[columnNames.indexOf(LogConstants.EXEC_TIME_COLUMN)] = time;
        // TODO Optimise this
        computeExecPlusRset();
    }

    public void addRsetTimeColumn(long time) {
        data[columnNames.indexOf(LogConstants.RSET_USAGE_TIME)] = time;
        // TODO Optimise this
        computeExecPlusRset();
    }

    public void addFetchTimeColumn(long time) {
        data[columnNames.indexOf(LogConstants.FETCH_TIME_COLUMN)] = time;
    }

    public void addNBRowsColumn(int rows) {
        data[columnNames.indexOf(LogConstants.NB_ROWS_COLUMN)] = rows;
    }

    public void addThreadNameColumn(String threadName) {
        data[columnNames.indexOf(LogConstants.THREAD_NAME_COLUMN)] = threadName;
    }

    public void addConnectionNumberColumn(int connectionNumber) {
        data[columnNames.indexOf(LogConstants.CONNECTION_NUMBER_COLUMN)] = connectionNumber;
    }

    public void addTimeoutColumn(int timeout) {
        data[columnNames.indexOf(LogConstants.TIMEOUT_COLUMN)] = timeout;
    }

    public void addAutocommitColumn(boolean aoutcommit) {
        data[columnNames.indexOf(LogConstants.AUTOCOMMIT_COLUMN)] = aoutcommit;
    }

    public void addErrorFlag() {
        data[columnNames.indexOf(LogConstants.ERROR_COLUMN)] = 1;
    }
}
