package org.executequery.gui.jdbclogger;

import ch.sla.jdbcperflogger.model.ConnectionInfo;
import ch.sla.jdbcperflogger.model.StatementLog;
import org.executequery.gui.jdbclogger.net.AbstractLogReceiver;
import org.executequery.gui.jdbclogger.net.LogProcessor;
import org.executequery.gui.jdbclogger.net.ServerLogReceiver;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.sql.Timestamp;
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
            HashMap<UUID, List<StatementLog>> statements = logProcessor.getStatementLogs();

            for (Map.Entry<UUID, List<StatementLog>> entry : statements.entrySet())
            {
                List<StatementLog> statementLogs = entry.getValue();
                for (StatementLog statement : statementLogs) {
                    if (selectedLogId.equals(statement.getLogId())) {
                        jdbcLoggerPanel.txtFieldFilledSql.setText(statement.getFilledSql());
                        jdbcLoggerPanel.txtFieldRawSql.setText(statement.getRawSql());
                        ConnectionInfo connectionInfo = connections.get(statement.getConnectionUuid());

                        jdbcLoggerPanel.connectionCreationDateField.setText(connectionInfo.getCreationDate().toString());
                        long millis = TimeUnit.NANOSECONDS
                                .toMillis(connectionInfo.getConnectionCreationDuration());
                        jdbcLoggerPanel.connectionCreationDurationField.setText(String.valueOf(millis));
                        jdbcLoggerPanel.connectionUrlField.setText(connectionInfo.getUrl());
                        jdbcLoggerPanel.connectionPropertiesField.setText(connectionInfo.getConnectionProperties().toString());
                        return;
                    }
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

            final StringBuilder txt = new StringBuilder();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        jdbcLoggerPanel.lblStatus.setText(txt.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        void doRefreshData() {
            final List<String> tempColumnNames = new ArrayList<>();
            final List<Class<?>> tempColumnTypes = new ArrayList<>();
            final List<Object[]> tempRows = new ArrayList<>();

            try {
                Set<AbstractLogReceiver> childReceivers = ((ServerLogReceiver) logReceiver).getChildReceivers();
                if (childReceivers == null)
                    return;

                for (AbstractLogReceiver logReceiver : childReceivers) {
                    LogProcessor logProcessor = logReceiver.getLogProcessor();

                    if (!logProcessor.isNeededUpdate() && !forceRefresh)
                        return;

                    HashMap<UUID, List<StatementLog>> statementLogs = logProcessor.getStatementLogs();
                    HashMap<UUID, ConnectionInfo> connections = logProcessor.getConnections();
                    if (statementLogs.size() > 0) {
                        List<List<StatementLog>> statementList = new ArrayList<>(statementLogs.values());
                        for (List<StatementLog> statements : statementList) {
                            for (StatementLog statement : statements) {
                                List<Object> objectList = new ArrayList<>();

                                objectList.add( statement.getLogId());
                                if (!tempColumnNames.contains(LogConstants.ID_COLUMN)) {
                                    tempColumnNames.add(LogConstants.ID_COLUMN);
                                    tempColumnTypes.add(statement.getLogId().getClass());
                                }

                                objectList.add(new Timestamp(statement.getTimestamp()));
                                if (!tempColumnNames.contains(LogConstants.TSTAMP_COLUMN)) {
                                    tempColumnNames.add(LogConstants.TSTAMP_COLUMN);
                                    tempColumnTypes.add(Timestamp.class);
                                }

                                objectList.add( connections.get(statement.getConnectionUuid()).getConnectionNumber());
                                if (!tempColumnNames.contains(LogConstants.CONNECTION_NUMBER_COLUMN)) {
                                    tempColumnNames.add(LogConstants.CONNECTION_NUMBER_COLUMN);
                                    tempColumnTypes.add(statement.getConnectionUuid().getClass());
                                }

                                Byte statementTypeByte = Byte.valueOf(String.valueOf(statement.getStatementType().getId()));
                                objectList.add(statementTypeByte);
                                if (!tempColumnNames.contains(LogConstants.STMT_TYPE_COLUMN)) {
                                    tempColumnNames.add(LogConstants.STMT_TYPE_COLUMN);
                                    tempColumnTypes.add(statementTypeByte.getClass());
                                }

                                String rawSql = statement.getRawSql();
                                if (filterType.equals(Filter.FilterType.FILTER) && txtFilter != null)
                                    if (!rawSql.toLowerCase().contains(txtFilter.toLowerCase()))
                                        continue;
                                objectList.add(rawSql);
                                if (!tempColumnNames.contains(LogConstants.RAW_SQL_COLUMN)) {
                                    tempColumnNames.add(LogConstants.RAW_SQL_COLUMN);
                                    tempColumnTypes.add(rawSql.getClass());
                                }

                                Object[] row = objectList.toArray();
                                tempRows.add(row);
                            }
                        }
                    }

                    logProcessor.setNeededUpdate(false);
                    forceRefresh = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        jdbcLoggerPanel.setData(tempRows, tempColumnNames, tempColumnTypes,
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