package org.executequery.gui.jdbclogger.net;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import ch.sla.jdbcperflogger.model.*;

public class LogProcessor extends Thread implements AutoCloseable {

    private volatile boolean disposed = false;

    private volatile boolean isNeededUpdate;

    private final BlockingQueue<LogMessage> logs = new ArrayBlockingQueue<>(10000);
    private final HashMap <UUID, ConnectionInfo> connections = new HashMap<>();
    private final HashMap<UUID, List<StatementLog>> statementLogs = new HashMap<>();
    private final HashMap<UUID, List<StatementExecutedLog>> statementExecutedLogs = new HashMap<>();
    private final HashMap<UUID, List<ResultSetLog>> resultSetLogs = new HashMap<>();
    private final HashMap<UUID, List<BatchedNonPreparedStatementsLog>> batchedNonPreparedStatementsLogs =
            new HashMap<>();
    private final HashMap<UUID, List<BatchedPreparedStatementsLog>> batchedPreparedStatementsLogs =
            new HashMap<>();
    private final HashMap<UUID, List<TxCompleteLog>> txCompleteLogs = new HashMap<>();
    private final List<BufferFullLogMessage> bufferFullLogMessageLogs = new ArrayList<>();

    public HashMap<UUID, ConnectionInfo> getConnections() {
        return connections;
    }

    public HashMap<UUID, List<StatementLog>> getStatementLogs() {
        return statementLogs;
    }

    public HashMap<UUID, List<StatementExecutedLog>> getStatementExecutedLogs() {
        return statementExecutedLogs;
    }

    public HashMap<UUID, List<ResultSetLog>> getResultSetLogs() {
        return resultSetLogs;
    }

    public HashMap<UUID, List<BatchedNonPreparedStatementsLog>> getBatchedNonPreparedStatementsLogs() {
        return batchedNonPreparedStatementsLogs;
    }

    public HashMap<UUID, List<BatchedPreparedStatementsLog>> getBatchedPreparedStatementsLogs() {
        return batchedPreparedStatementsLogs;
    }

    public HashMap<UUID, List<TxCompleteLog>> getTxCompleteLogs() {
        return txCompleteLogs;
    }

    public List<BufferFullLogMessage> getBufferFullLogMessageLogs() {
        return bufferFullLogMessageLogs;
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
                    List<StatementLog> l = statementLogs.get(statementLog.getConnectionUuid());
                    if (l == null)
                        statementLogs.put(statementLog.getConnectionUuid(), l=new ArrayList<StatementLog>());
                    l.add(statementLog);
                } else if (logMessage instanceof StatementExecutedLog) {
                    StatementExecutedLog statementExecutedLog = (StatementExecutedLog) logMessage;
                    List<StatementExecutedLog> l = statementExecutedLogs.get(statementExecutedLog.getLogId());
                    if (l == null)
                        statementExecutedLogs.put(statementExecutedLog.getLogId(), l=new ArrayList<StatementExecutedLog>());
                    l.add(statementExecutedLog);
                } else if (logMessage instanceof ResultSetLog) {
                    ResultSetLog resultSetLog = (ResultSetLog) logMessage;
                    List<ResultSetLog> l = resultSetLogs.get(resultSetLog.getLogId());
                    if (l == null)
                        resultSetLogs.put(resultSetLog.getLogId(), l=new ArrayList<ResultSetLog>());
                    l.add(resultSetLog);
                } else if (logMessage instanceof BatchedNonPreparedStatementsLog) {
                    BatchedNonPreparedStatementsLog batchedNonPreparedStatementsLog = (BatchedNonPreparedStatementsLog) logMessage;
                    List<BatchedNonPreparedStatementsLog> l = batchedNonPreparedStatementsLogs.get(batchedNonPreparedStatementsLog.getLogId());
                    if (l == null)
                        batchedNonPreparedStatementsLogs.put(batchedNonPreparedStatementsLog.getLogId(), l=new ArrayList<BatchedNonPreparedStatementsLog>());
                    l.add(batchedNonPreparedStatementsLog);
                } else if (logMessage instanceof BatchedPreparedStatementsLog) {
                    BatchedPreparedStatementsLog batchedPreparedStatementsLog = (BatchedPreparedStatementsLog) logMessage;
                    List<BatchedPreparedStatementsLog> l = batchedPreparedStatementsLogs.get(batchedPreparedStatementsLog.getLogId());
                    if (l == null)
                        batchedPreparedStatementsLogs.put(batchedPreparedStatementsLog.getLogId(), l=new ArrayList<BatchedPreparedStatementsLog>());
                    l.add(batchedPreparedStatementsLog);
                } else if (logMessage instanceof TxCompleteLog) {
                    TxCompleteLog txCompleteLog = (TxCompleteLog) logMessage;
                    List<TxCompleteLog> l = txCompleteLogs.get(txCompleteLog.getConnectionUuid());
                    if (l == null)
                        txCompleteLogs.put(txCompleteLog.getConnectionUuid(), l=new ArrayList<TxCompleteLog>());
                    l.add(txCompleteLog);
                } else if (logMessage instanceof BufferFullLogMessage) {
                    BufferFullLogMessage bufferFullLogMessage = (BufferFullLogMessage) logMessage;
                    bufferFullLogMessageLogs.add(bufferFullLogMessage);
                } else {
                    throw new IllegalArgumentException("unexpected log, class=" + logMessage.getClass());
                }

                isNeededUpdate = true;
            }
        }
    }
}
