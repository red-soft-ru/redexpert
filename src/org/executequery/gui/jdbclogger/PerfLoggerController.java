package org.executequery.gui.jdbclogger;

import ch.sla.jdbcperflogger.model.ConnectionInfo;
import org.executequery.gui.jdbclogger.model.FullStatementLog;
import org.executequery.gui.jdbclogger.net.AbstractLogReceiver;
import org.executequery.gui.jdbclogger.net.LogProcessor;
import org.executequery.gui.jdbclogger.net.ServerLogReceiver;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PerfLoggerController {
    private final AbstractLogReceiver logReceiver;
    private final JdbcLoggerPanel jdbcLoggerPanel;

    private volatile String txtFilter;

    private volatile Long minDurationNanos;

    private final RefreshDataTask refreshDataTask;

    private Filter.FilterType filterType = Filter.FilterType.HIGHLIGHT;
    private final ScheduledExecutorService refreshDataScheduledExecutorService;
    private boolean forceRefresh;

    PerfLoggerController(final AbstractLogReceiver logReceiver, JdbcLoggerPanel loggerPanel) {
        this.logReceiver = logReceiver;

        jdbcLoggerPanel = loggerPanel;

        refreshDataTask = new RefreshDataTask();

        refreshDataScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        refreshDataScheduledExecutorService.scheduleWithFixedDelay(refreshDataTask, 1, 2, TimeUnit.SECONDS);

        forceRefresh = false;
    }

    void startReciever() {
        this.logReceiver.start();
    }

    JdbcLoggerPanel getPanel() {
        return jdbcLoggerPanel;
    }

    void setTextFilter(final String filter) {
        if (filter == null || filter.isEmpty()) {
            txtFilter = null;
        } else {
            txtFilter = filter;
        }
        refresh();
    }

    void setMinDurationFilter(final Long durationMs) {
        if (durationMs == null) {
            minDurationNanos = null;
        } else {
            minDurationNanos = TimeUnit.MILLISECONDS.toNanos(durationMs);
        }
        refresh();
    }

    void setFilterType(final Filter.FilterType filterType) {
        this.filterType = filterType;
        refresh();
    }

    public void onDeleteSelectedStatements(final UUID... logIds) {
        refresh();
    }

    void onClear() {
        refresh();
    }

    private void refresh() {
        if (filterType == Filter.FilterType.FILTER) {
            jdbcLoggerPanel.table.setTxtToHighlight(null);
            jdbcLoggerPanel.table.setMinDurationNanoToHighlight(null);
        } else {
            jdbcLoggerPanel.table.setTxtToHighlight(txtFilter);
            jdbcLoggerPanel.table.setMinDurationNanoToHighlight(minDurationNanos);
        }
        jdbcLoggerPanel.setTxtToHighlight(txtFilter);

        refreshDataTask.forceRefresh();
        refreshDataScheduledExecutorService.submit(refreshDataTask);
    }

    public void onSelectStatement(UUID selectedLogId) {
        Set<AbstractLogReceiver> childReceivers = ((ServerLogReceiver) logReceiver).getChildReceivers();
        if (childReceivers == null || selectedLogId == null)
            return;

        for (AbstractLogReceiver logReceiver : childReceivers) {
            LogProcessor logProcessor = logReceiver.getLogProcessor();

            HashMap<UUID, ConnectionInfo> connections = logProcessor.getConnections();
            LinkedHashMap<UUID, FullStatementLog> fullStatementLogs = logProcessor.getFullStatementLogs();

            for (Map.Entry<UUID, FullStatementLog> entry : fullStatementLogs.entrySet()) {
                FullStatementLog statement = entry.getValue();
                if (selectedLogId.equals(statement.getLogId())) {
                    jdbcLoggerPanel.txtFieldFilledSql.setText(statement.getFilledSql());
                    jdbcLoggerPanel.txtFieldRawSql.setText(statement.getRawSql());
                    ConnectionInfo connectionInfo = connections.get(statement.getConnectionId());

                    jdbcLoggerPanel.connectionCreationDateField.setText(connectionInfo.getCreationDate().toString());
                    long millis = TimeUnit.NANOSECONDS
                            .toMillis(connectionInfo.getConnectionCreationDuration());
                    jdbcLoggerPanel.connectionCreationDurationField.setText(String.valueOf(millis));
                    jdbcLoggerPanel.connectionUrlField.setText(connectionInfo.getUrl());
                    jdbcLoggerPanel.connectionPropertiesField.setText(connectionInfo.getConnectionProperties().toString());

                    if (statement.getSqlException() != null && !statement.getSqlException().isEmpty()) {
                        jdbcLoggerPanel.txtFieldFilledSql.append(statement.getSqlException());
                        jdbcLoggerPanel.txtFieldRawSql.append(statement.getSqlException());
                    }

                    return;
                }
            }
        }
    }

    private class RefreshDataTask implements Runnable {

        @Override
        public void run() {
            try {
                doRun();
            } catch (final Exception exc) {
                exc.printStackTrace();
            }
        }

        private void doRun() {

            doRefreshData();

        }

        void doRefreshData() {
            List<String> tempColumnNames = new ArrayList<>();
            List<Class<?>> tempColumnTypes = new ArrayList<>();
            final List<Object[]> tempRows = new ArrayList<>();

            try {
                Set<AbstractLogReceiver> childReceivers = ((ServerLogReceiver) logReceiver).getChildReceivers();
                if (childReceivers == null)
                    return;

                for (AbstractLogReceiver logReceiver : childReceivers) {
                    LogProcessor logProcessor = logReceiver.getLogProcessor();

                    if (!logProcessor.isNeededUpdate() && !forceRefresh)
                        return;

                    LinkedHashMap<UUID, FullStatementLog> fullStatementLogs = logProcessor.getFullStatementLogs();
                    HashMap<UUID, ConnectionInfo> connections = logProcessor.getConnections();
                    if (fullStatementLogs.size() > 0) {
                        for (Map.Entry<UUID, FullStatementLog> entry : fullStatementLogs.entrySet()) {

                            StatementRow row = new StatementRow();
                            FullStatementLog statement = entry.getValue();

                            if (filterType.equals(Filter.FilterType.FILTER) && txtFilter != null)
                                if (!statement.getRawSql().toLowerCase().contains(txtFilter.toLowerCase()))
                                    continue;

                            row.addIDColumn(statement.getLogId());
                            row.addTimestampColumn(statement.getTimestamp());
                            row.addStatementTypeColumn(Byte.valueOf(String.valueOf(statement.getStatementType().getId())));
                            row.addRawSQLColumn(statement.getRawSql());
                            row.addRsetTimeColumn(statement.getResultSetUsageDurationNanos());
                            row.addExecTimeColumn(statement.getExecutionTimeNanos());
                            row.addFetchTimeColumn(statement.getFetchDurationNanos());
                            row.addNBRowsColumn(statement.getNbRowsIterated());
                            row.addThreadNameColumn(statement.getThreadName());
                            row.addConnectionNumberColumn(connections.get(statement.getConnectionId()).getConnectionNumber());
                            row.addTimeoutColumn(statement.getTimeout());
                            row.addAutocommitColumn(statement.isAutoCommit());

                            if (statement.getSqlException() != null && !statement.getSqlException().isEmpty()) {
                                row.addErrorFlag();
                            }

                            tempColumnNames = row.getColumnNames();
                            tempColumnTypes = row.getColumnTypes();
                            tempRows.add(row.getData());
                        }
                    }

                    logProcessor.setNeededUpdate(false);
                    forceRefresh = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            final List<String> finalTempColumnNames = tempColumnNames;
            final List<Class<?>> finalTempColumnTypes = tempColumnTypes;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        jdbcLoggerPanel.setData(tempRows, finalTempColumnNames, finalTempColumnTypes,
                                true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void forceRefresh() {
            forceRefresh = true;
        }
    }
}