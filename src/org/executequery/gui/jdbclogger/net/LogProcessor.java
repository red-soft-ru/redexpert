package org.executequery.gui.jdbclogger.net;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import ch.sla.jdbcperflogger.StatementType;
import ch.sla.jdbcperflogger.model.*;
import org.executequery.gui.jdbclogger.model.FullStatementLog;

public class LogProcessor extends Thread implements AutoCloseable {

    private volatile boolean disposed = false;

    private volatile boolean isNeededUpdate;

    private final BlockingQueue<LogMessage> logs = new ArrayBlockingQueue<>(10000);
    private final HashMap<UUID, ConnectionInfo> connections = new HashMap<>();
    private final LinkedHashMap<UUID, FullStatementLog> fullStatementLogs = new LinkedHashMap<>();

    public HashMap<UUID, ConnectionInfo> getConnections() {
        return connections;
    }
    public LinkedHashMap<UUID, FullStatementLog> getFullStatementLogs() {
        return fullStatementLogs;
    }

    public boolean isNeededUpdate() {
        return isNeededUpdate;
    }

    public void setNeededUpdate(boolean neededUpdate) {
        isNeededUpdate = neededUpdate;
    }

    LogProcessor() {
        this.setName("LogProcessor");
    }

    void putMessage(final LogMessage msg) {
        try {
            logs.put(msg);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        disposed = true;
        try {
            this.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        final List<LogMessage> drainedLogs = new ArrayList<>(1000);

        while (!disposed) {

            LogMessage logMessage;
            try {
                logMessage = logs.poll(1, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
                continue;
            }

            if (logMessage == null) {
                continue;
            }
            drainedLogs.clear();
            drainedLogs.add(logMessage);
            logs.drainTo(drainedLogs);

            for (int i = 0; i < drainedLogs.size(); i++) {
                logMessage = drainedLogs.get(i);

                if (logMessage instanceof ConnectionInfo) {
                    ConnectionInfo connectionInfo = (ConnectionInfo) logMessage;
                    connections.put(connectionInfo.getUuid(), connectionInfo);
                } else if (logMessage instanceof StatementLog) {
                    StatementLog statementLog = (StatementLog) logMessage;
                    FullStatementLog fullStatementLog = new FullStatementLog();
                    fullStatementLog.setConnectionId(statementLog.getConnectionUuid());
                    fullStatementLog.setLogId(statementLog.getLogId());
                    fullStatementLog.setTimestamp(statementLog.getTimestamp());
                    fullStatementLog.setStatementType(statementLog.getStatementType());
                    fullStatementLog.setRawSql(statementLog.getRawSql());
                    fullStatementLog.setFilledSql(statementLog.getFilledSql());
                    fullStatementLog.setAutoCommit(statementLog.isAutoCommit());
                    fullStatementLog.setThreadName(statementLog.getThreadName());

                    fullStatementLogs.put(fullStatementLog.getLogId(), fullStatementLog);
                } else if (logMessage instanceof StatementExecutedLog) {
                    StatementExecutedLog log = (StatementExecutedLog) logMessage;
                    FullStatementLog fullStatementLog = fullStatementLogs.get(log.getLogId());
                    fullStatementLog.setExecutionTimeNanos(log.getExecutionTimeNanos());
                    fullStatementLog.setSqlException(log.getSqlException());
                    fullStatementLog.setUpdateCount(log.getUpdateCount());
                    fullStatementLogs.put(fullStatementLog.getLogId(), fullStatementLog);
                } else if (logMessage instanceof ResultSetLog) {
                    ResultSetLog log = (ResultSetLog) logMessage;
                    FullStatementLog fullStatementLog = fullStatementLogs.get(log.getLogId());
                    fullStatementLog.setFetchDurationNanos(log.getFetchDurationNanos());
                    fullStatementLog.setNbRowsIterated(log.getNbRowsIterated());
                    fullStatementLog.setResultSetUsageDurationNanos(log.getResultSetUsageDurationNanos());
                    fullStatementLogs.put(fullStatementLog.getLogId(), fullStatementLog);
                } else if (logMessage instanceof BatchedNonPreparedStatementsLog) {
                    BatchedNonPreparedStatementsLog log = (BatchedNonPreparedStatementsLog) logMessage;
                    FullStatementLog fullStatementLog = new FullStatementLog();
                    fullStatementLog.setSqlList(log.getSqlList());
                    fullStatementLog.setConnectionId(log.getConnectionUuid());
                    fullStatementLog.setLogId(log.getLogId());
                    fullStatementLog.setTimestamp(log.getTimestamp());
                    fullStatementLog.setStatementType(log.getStatementType());
                    fullStatementLog.setAutoCommit(log.isAutoCommit());
                    fullStatementLog.setThreadName(log.getThreadName());
                    fullStatementLogs.put(log.getLogId(), fullStatementLog);
                } else if (logMessage instanceof BatchedPreparedStatementsLog) {
                    BatchedPreparedStatementsLog log = (BatchedPreparedStatementsLog) logMessage;
                    FullStatementLog fullStatementLog = new FullStatementLog();
                    fullStatementLog.setSqlList(log.getSqlList());
                    fullStatementLog.setConnectionId(log.getConnectionUuid());
                    fullStatementLog.setLogId(log.getLogId());
                    fullStatementLog.setTimestamp(log.getTimestamp());
                    fullStatementLog.setStatementType(log.getStatementType());
                    fullStatementLog.setAutoCommit(log.isAutoCommit());
                    fullStatementLog.setThreadName(log.getThreadName());
                    fullStatementLog.setRawSql(log.getRawSql());
                    fullStatementLogs.put(log.getLogId(), fullStatementLog);
                } else if (logMessage instanceof TxCompleteLog) {
                    TxCompleteLog txCompleteLog = (TxCompleteLog) logMessage;
                    FullStatementLog fullStatementLog = new FullStatementLog();
                    fullStatementLog.setSavePointDescription(txCompleteLog.getSavePointDescription());
                    fullStatementLog.setTimestamp(txCompleteLog.getTimestamp());
                    fullStatementLog.setStatementType(StatementType.TRANSACTION);
                    fullStatementLog.setExecutionTimeNanos(txCompleteLog.getExecutionTimeNanos());
                    fullStatementLog.setCompletionType(txCompleteLog.getCompletionType());
                    fullStatementLog.setConnectionId(txCompleteLog.getConnectionUuid());
                    fullStatementLog.setThreadName(txCompleteLog.getThreadName());
                    fullStatementLog.setLogId(UUID.randomUUID()); // 'cause it doesn't have own logId

                    fullStatementLogs.put(fullStatementLog.getLogId(), fullStatementLog);
                } else if (logMessage instanceof BufferFullLogMessage) {
                    // nothing to do???
                } else {
                    throw new IllegalArgumentException("unexpected log, class=" + logMessage.getClass());
                }

                isNeededUpdate = true;
            }
        }
    }
}
