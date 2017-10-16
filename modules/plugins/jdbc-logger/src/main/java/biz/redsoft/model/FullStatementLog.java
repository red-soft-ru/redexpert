package biz.redsoft.model;

import ch.sla.jdbcperflogger.StatementType;
import ch.sla.jdbcperflogger.TxCompletionType;

import java.util.List;
import java.util.UUID;

/**
 * Created by vasiliy on 26.12.16.
 */
public class FullStatementLog {

    private UUID connectionId;
    private UUID logId;
    private long timestamp;
    private StatementType statementType;
    private String threadName;
    private int timeout;
    private boolean autoCommit;
    private List<String> sqlList;
    private String rawSql;
    private String filledSql;
    private boolean preparedStatement;
    private long executionTimeNanos;
    private Long updateCount;
    private String sqlException;
    private long resultSetUsageDurationNanos;
    private long fetchDurationNanos;
    private int nbRowsIterated;
    private TxCompletionType completionType;
    private String savePointDescription;

    public FullStatementLog() {

    }

    public UUID getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(UUID connectionId) {
        this.connectionId = connectionId;
    }

    public UUID getLogId() {
        return logId;
    }

    public void setLogId(UUID logId) {
        this.logId = logId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public void setStatementType(StatementType statementType) {
        this.statementType = statementType;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public TxCompletionType getCompletionType() {
        return completionType;
    }

    public String getSavePointDescription() {
        return savePointDescription;
    }

    public void setSqlList(List<String> sqlList) {
        this.sqlList = sqlList;

        for (String sql : sqlList) {
            this.rawSql += sql;
            this.filledSql += sql;
        }
    }

    public void setRawSql(String rawSql) {
        this.rawSql = rawSql;
    }

    public void setFilledSql(String filledSql) {
        this.filledSql = filledSql;
    }

    public void setPreparedStatement(boolean preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public void setExecutionTimeNanos(long executionTimeNanos) {
        this.executionTimeNanos = executionTimeNanos;
    }

    public void setUpdateCount(Long updateCount) {
        this.updateCount = updateCount;
    }

    public void setSqlException(String sqlException) {
        this.sqlException = sqlException;
    }

    public void setResultSetUsageDurationNanos(long resultSetUsageDurationNanos) {
        this.resultSetUsageDurationNanos = resultSetUsageDurationNanos;
    }

    public void setFetchDurationNanos(long fetchDurationNanos) {
        this.fetchDurationNanos = fetchDurationNanos;
    }

    public void setNbRowsIterated(int nbRowsIterated) {
        this.nbRowsIterated = nbRowsIterated;
    }

    public void setCompletionType(TxCompletionType completionType) {
        this.completionType = completionType;
        this.rawSql = this.completionType.name();
        this.filledSql = "/*" + this.rawSql + "*/";
    }

    public void setSavePointDescription(String savePointDescription) {
        this.savePointDescription = savePointDescription;
    }

    public String getRawSql() {
        return this.rawSql;
    }

    public String getFilledSql() {
        return this.filledSql;
    }

    public boolean isPreparedStatement() {
        return this.preparedStatement;
    }

    public List<String> getSqlList() {
        return this.sqlList;
    }

    public long getExecutionTimeNanos() {
        return this.executionTimeNanos;
    }

    public Long getUpdateCount() {
        return this.updateCount;
    }

    public String getSqlException() {
        return this.sqlException;
    }

    public long getResultSetUsageDurationNanos() {
        return this.resultSetUsageDurationNanos;
    }

    public long getFetchDurationNanos() {
        return this.fetchDurationNanos;
    }

    public int getNbRowsIterated() {
        return this.nbRowsIterated;
    }
}
