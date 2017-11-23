package biz.redsoft.gui;

import biz.redsoft.model.FullStatementLog;
import ch.sla.jdbcperflogger.model.ConnectionInfo;
import biz.redsoft.net.AbstractLogReceiver;
import biz.redsoft.net.LogProcessor;
import biz.redsoft.net.ServerLogReceiver;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PerfLoggerController {
    private final AbstractLogReceiver logReceiver;
    private final LoggerPanel loggerPanel;

    private volatile String txtFilter;

    private volatile Long minDurationNanos;

    private final RefreshDataTask refreshDataTask;

    private Filter.FilterType filterType = Filter.FilterType.HIGHLIGHT;
    private final ScheduledExecutorService refreshDataScheduledExecutorService;
    private boolean forceRefresh;

    /** The Log4J Logger object */
    private final static Logger logger = Logger.getLogger(PerfLoggerController.class);

    boolean logOpened;

    PerfLoggerController(final AbstractLogReceiver logReceiver, LoggerPanel loggerPanel) {
        this.logReceiver = logReceiver;

        this.loggerPanel = loggerPanel;

        refreshDataTask = new RefreshDataTask();

        refreshDataScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        refreshDataScheduledExecutorService.scheduleWithFixedDelay(refreshDataTask, 1, 2, TimeUnit.SECONDS);

        forceRefresh = false;

        logOpened = false;
        String filePath = "sql.log";
        PatternLayout layout = new PatternLayout("%-5p %d %m%n");
        RollingFileAppender appender = null;
        try {
            appender = new RollingFileAppender(layout, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        appender.setName("JdbcLog");
        appender.setMaxFileSize("1MB");
        appender.activateOptions();
        Logger.getRootLogger().addAppender(appender);
    }

    void startReciever() {
        this.logReceiver.start();
    }

    LoggerPanel getPanel() {
        return this.loggerPanel;
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
            loggerPanel.table.setTxtToHighlight(null);
            loggerPanel.table.setMinDurationNanoToHighlight(null);
        } else {
            loggerPanel.table.setTxtToHighlight(txtFilter);
            loggerPanel.table.setMinDurationNanoToHighlight(minDurationNanos);
        }
        loggerPanel.setTxtToHighlight(txtFilter);

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
                    loggerPanel.txtFieldFilledSql.setText(statement.getFilledSql());
                    loggerPanel.txtFieldRawSql.setText(statement.getRawSql());
                    ConnectionInfo connectionInfo = connections.get(statement.getConnectionId());

                    loggerPanel.connectionCreationDateField.setText(connectionInfo.getCreationDate().toString());
                    long millis = TimeUnit.NANOSECONDS
                            .toMillis(connectionInfo.getConnectionCreationDuration());
                    loggerPanel.connectionCreationDurationField.setText(String.valueOf(millis));
                    loggerPanel.connectionUrlField.setText(connectionInfo.getUrl());
                    loggerPanel.connectionPropertiesField.setText(connectionInfo.getConnectionProperties().toString());

                    if (statement.getSqlException() != null && !statement.getSqlException().isEmpty()) {
                        loggerPanel.txtFieldFilledSql.append(statement.getSqlException());
                        loggerPanel.txtFieldRawSql.append(statement.getSqlException());
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

                            if (loggerPanel.getCheckLogToFile().isSelected()) {
                                // log to file
                                Date d = new Date(statement.getTimestamp()/* * 1000*/);
                                StringBuilder builder = new StringBuilder();
                                builder.append("Time: ");
                                builder.append(d.toString());
                                builder.append("; ");
                                builder.append("Sql query: ");
                                builder.append(statement.getRawSql());

                                logger.info(builder.toString());
                            }

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
                        loggerPanel.setData(tempRows, finalTempColumnNames, finalTempColumnTypes,
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