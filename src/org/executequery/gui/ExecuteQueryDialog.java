package org.executequery.gui;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowserTableCellRenderer;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.gui.browser.BrowserConstants;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.DerivedQuery;
import org.executequery.sql.QueryTokenizer;
import org.executequery.sql.SqlMessages;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;


public class ExecuteQueryDialog extends BaseDialog {

    private String query;
    private String delimiter;
    private List<String> queries;
    private ListActionsModel model;
    private StatementExecutor executor;

    private boolean stopOnError;
    private boolean stop = false;
    private boolean commitResult = false;

    // --- GUI components ---

    private DefaultTable actionsTable;

    private SQLTextArea queryPane;
    private JTabbedPane tabbedPane;
    private LoggingOutputPanel fullOutPane;
    private LoggingOutputPanel singleOutPane;

    private JButton stopButton;
    private JButton commitButton;
    private JButton rollbackButton;
    private JButton copyErrorButton;
    private JButton copyQueryButton;

    // ---

    public ExecuteQueryDialog(String name, String query, DatabaseConnection connection, boolean keepAlive) {
        this(name, query, connection, keepAlive, ";");
    }

    public ExecuteQueryDialog(String name, String query, DatabaseConnection connection, boolean keepAlive, String delimiter) {
        this(name, query, connection, keepAlive, delimiter, false, true);
    }

    public ExecuteQueryDialog(String name, String query, DatabaseConnection connection, boolean keepAlive, String delimiter, boolean autocommit, boolean stopOnError) {
        super(name, true, true);
        this.query = query;

        SwingWorker worker = new SwingWorker("ExecuteQueriesInDialog") {
            @Override
            public Object construct() {
                execute();
                return null;
            }
        };

        init(connection, keepAlive, delimiter, autocommit, stopOnError);
        worker.start();
        arrange(autocommit);
    }

    public ExecuteQueryDialog(String name, List<String> queries, DatabaseConnection connection, boolean keepAlive, String delimiter, boolean autocommit, boolean stopOnError) {
        super(name, true, true);
        this.queries = queries;

        SwingWorker worker = new SwingWorker("ExecuteQueriesInDialog") {
            @Override
            public Object construct() {
                executeList();
                return null;
            }
        };

        init(connection, keepAlive, delimiter, autocommit, stopOnError);
        worker.start();
        arrange(autocommit);
    }

    private void init(DatabaseConnection connection, boolean keepAlive, String delimiter, boolean autocommit, boolean stopOnError) {
        this.delimiter = delimiter;
        this.stopOnError = stopOnError;

        executor = new DefaultStatementExecutor(connection, keepAlive);
        executor.setCommitMode(autocommit);

        model = new ListActionsModel();
        fullOutPane = new LoggingOutputPanel();
        singleOutPane = new LoggingOutputPanel();

        // --- actions table ---

        actionsTable = new DefaultTable(model);
        actionsTable.setCellSelectionEnabled(true);
        actionsTable.setColumnSelectionAllowed(false);
        actionsTable.setSurrendersFocusOnKeystroke(true);
        actionsTable.getTableHeader().setReorderingAllowed(false);
        actionsTable.setDefaultRenderer(Object.class, new BrowserTableCellRenderer());

        actionsTable.addMouseListener(new ActionMouseListener());
        actionsTable.getSelectionModel().addListSelectionListener(new ActionSelectionListener());

        TableColumnModel tcm = actionsTable.getColumnModel();
        tcm.getColumn(ListActionsModel.VALID).setPreferredWidth(25);
        tcm.getColumn(ListActionsModel.NAME).setPreferredWidth(200);
        tcm.getColumn(ListActionsModel.STATUS).setPreferredWidth(200);
        tcm.getColumn(ListActionsModel.COPY).setPreferredWidth(50);

        // --- buttons ---

        copyQueryButton = WidgetFactory.createButton(
                "copyQueryButton",
                bundleString("copyQuery"),
                e -> copyQuery()
        );

        copyErrorButton = WidgetFactory.createButton(
                "copyErrorButton",
                bundleString("copyError"),
                e -> copyError()
        );

        commitButton = WidgetFactory.createButton(
                "commitButton",
                Bundles.get(autocommit ?
                        "common.ok.button" :
                        "common.commit.button"
                ),
                e -> commit()
        );

        rollbackButton = WidgetFactory.createButton(
                "rollbackButton",
                Bundles.get("common.rollback.button"),
                e -> rollback()
        );

        stopButton = WidgetFactory.createButton(
                "stopButton",
                Bundles.get("common.stop"),
                e -> stop()
        );

        // --- query pane ---

        queryPane = new SQLTextArea();
        queryPane.setEnableUndoManager(false);
        queryPane.setEditable(false);
        queryPane.setRows(5);

        // --- tabbed pane ---

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(bundleString("Single"), singleOutPane);
        tabbedPane.addTab(bundleString("FullLog"), fullOutPane);
        tabbedPane.setPreferredSize(new Dimension(475, 200));
    }

    private void arrange(boolean autocommit) {
        GridBagHelper gbh;

        // --- table scroll ---

        JScrollPane tableScroll = new JScrollPane(actionsTable);
        tableScroll.setPreferredSize(new Dimension(475, 200));

        // --- action split pane ---

        JSplitPane actionSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        actionSplitPane.setTopComponent(tableScroll);
        actionSplitPane.setBottomComponent(new JScrollPane(queryPane));
        actionSplitPane.setDividerLocation(0.5);

        // --- base split pane ---

        JSplitPane baseSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        baseSplitPane.setTopComponent(actionSplitPane);
        baseSplitPane.setBottomComponent(tabbedPane);
        baseSplitPane.setDividerLocation(0.6);

        // --- main panel ---

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.registerKeyboardAction(
                e -> closeDialog(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        gbh = new GridBagHelper().anchorNorthWest().setInsets(5, 5, 5, 0).fillBoth();
        mainPanel.add(baseSplitPane, gbh.setMaxWeightY().spanX().get());
        mainPanel.add(copyQueryButton, gbh.nextRowFirstCol().setWidth(1).bottomGap(5).setMinWeightY().get());
        mainPanel.add(copyErrorButton, gbh.nextCol().leftGap(0).get());
        mainPanel.add(stopButton, gbh.nextCol().get());
        mainPanel.add(commitButton, gbh.nextCol().get());
        if (!autocommit)
            mainPanel.add(rollbackButton, gbh.nextCol().get());

        // --- base ---

        addDisplayComponent(mainPanel);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDialog();
            }
        });
    }

    // --- buttons handlers ---

    private void copyQuery() {
        StringBuilder copy = new StringBuilder();

        try {

            if (!delimiter.equals(";"))
                copy.append("SET TERM ").append(delimiter).append(";\n");

            Vector<RowAction> rowActions = model.data;
            for (int i = 0; i < rowActions.size(); i++)
                if (rowActions.elementAt(i).copyScript)
                    copy.append(rowActions.elementAt(i).queryAction).append(delimiter).append("\n");

            if (!delimiter.equals(";"))
                copy.append("SET TERM ;").append(delimiter);

            setClipboard(copy.toString());

        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    private void copyError() {
        StringBuilder copy = new StringBuilder();

        try {
            Vector<RowAction> rowActions = model.data;
            for (int i = 0; i < rowActions.size(); i++)
                if (rowActions.elementAt(i).copyScript)
                    copy.append(rowActions.elementAt(i).sqlMessage).append("\n");

            setClipboard(copy.toString());

        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    private void commit() {
        try {

            SqlStatementResult result = executor.execute(QueryTypes.COMMIT, "commit");
            if (!result.isException()) {
                commitResult = true;
                finished();

            } else {
                setOutputMessage(SqlMessages.ERROR_MESSAGE, result.getErrorMessage());
                commitButton.setVisible(false);
            }

        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    private void rollback() {
        try {
            executor.execute(QueryTypes.ROLLBACK, "rollback");
            finished();

        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
            finished();
        }
    }

    private void stop() {
        stop = true;
    }

    // ---

    private void execute() {

        String queries = query;
        if (queries.endsWith(delimiter))
            queries = queries.substring(0, queries.length() - 1);

        int failed = 0;
        int success = 0;
        int startIndex = 0;
        boolean commit = true;

        String lowQuery = queries.toLowerCase();
        QueryTokenizer queryTokenizer = new QueryTokenizer();
        queryTokenizer.extractTokens(queries);

        if (!stopOnError)
            tabbedPane.setSelectedComponent(fullOutPane);

        while (!queries.trim().isEmpty() && (commit || !stopOnError) && !stop) {

            QueryTokenizer.QueryTokenized firstQuery = queryTokenizer.tokenizeFirstQuery(queries, lowQuery, startIndex, delimiter);
            queries = firstQuery.script;
            delimiter = firstQuery.delimiter;

            DerivedQuery derivedQuery = firstQuery.query;
            lowQuery = firstQuery.lowScript;
            startIndex = firstQuery.startIndex;
            if (derivedQuery == null)
                continue;

            String query = derivedQuery.getDerivedQuery();
            while (query.indexOf("\n") == 0)
                query = query.substring(1);

            RowAction action = new RowAction(query);
            commit = executeQuery(action);
            if (commit)
                model.addRow(action);
            else
                model.insertRow(action, 0);

            setMessages(action);
            if (action.executed)
                success++;
            else
                failed++;
        }

        stopButton.setVisible(false);
        printResult(success, failed);
    }

    private void executeList() {

        int failed = 0;
        int success = 0;
        boolean commit = true;

        if (!stopOnError)
            tabbedPane.setSelectedComponent(fullOutPane);

        for (String query : queries) {

            if (!((commit || !stopOnError) && !stop))
                break;

            query = query.trim();
            if (query.endsWith(delimiter))
                query = query.substring(0, query.length() - 1);

            RowAction action = new RowAction(query);
            commit = executeQuery(action);
            if (commit)
                model.addRow(action);
            else
                model.insertRow(action, 0);

            setMessages(action);
            if (action.executed)
                success++;
            else
                failed++;
        }

        stopButton.setVisible(false);
        printResult(success, failed);
    }

    private boolean executeQuery(RowAction query) {
        try {

            DerivedQuery derivedQuery = new DerivedQuery(query.queryAction);
            String queryToExecute = derivedQuery.getOriginalQuery();

            int type = derivedQuery.getQueryType();
            String name = derivedQuery.getObjectName();
            String metaName = derivedQuery.getMetaName();

            Log.info("Executing: " + queryToExecute);
            SqlStatementResult result = executor.execute(type, queryToExecute);

            int updateCount = result.getUpdateCount();
            if (updateCount == -1) {
                query.status = Bundles.get("common.error");
                query.sqlMessage = result.getErrorMessage();
                commitButton.setVisible(false);
                return false;

            } else if (result.isException()) {
                query.status = Bundles.get("common.error");
                query.sqlMessage = result.getErrorMessage();
                commitButton.setVisible(false);
                return false;

            } else {
                type = result.getType();
                query.sqlMessage = QueryTypes.getResultText(updateCount, type, metaName, name);
                query.executed = true;
                query.status = Bundles.get("common.success");
                return true;
            }

        } catch (Exception e) {
            query.status = Bundles.get("common.error");
            query.sqlMessage = e.getMessage();
            commitButton.setVisible(false);
            return false;
        }
    }

    // ---

    private void setMessages(RowAction row) {
        queryPane.setText(row.queryAction);
        setOutputMessage(row.executed ?
                        SqlMessages.PLAIN_MESSAGE :
                        SqlMessages.ERROR_MESSAGE,
                row.sqlMessage
        );
    }

    private void printResult(int success, int failed) {
        if (success > 0)
            fullOutPane.append(SqlMessages.PLAIN_MESSAGE, String.format(bundleString("queriesComplete"), success));
        if (failed > 0)
            fullOutPane.append(SqlMessages.ERROR_MESSAGE, String.format(bundleString("queriesFailed"), failed));
    }

    private void setOutputMessage(int type, String text) {
        singleOutPane.clear();
        singleOutPane.append(type, text);
    }

    public static void setClipboard(String str) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), null);
    }

    private void closeDialog() {
        rollback();
    }

    public boolean getCommit() {
        return commitResult;
    }

    private static String bundleString(String key) {
        return Bundles.get(ExecuteQueryDialog.class, key);
    }

    private class ActionSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int row = actionsTable.getSelectedRow();
            if (row >= 0) {
                setMessages(model.data.elementAt(row));
                tabbedPane.setSelectedComponent(singleOutPane);
            }
        }

    } // ActionSelectionListener class

    private class ActionMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            int row = actionsTable.getSelectedRow();
            if (row >= 0) {

                if (mouseEvent.getClickCount() > 1) {
                    if (actionsTable.getSelectedColumn() == ListActionsModel.COPY) {
                        model.data.elementAt(row).copyScript = !model.data.elementAt(row).copyScript;
                        model.fireTableDataChanged();
                    }
                }
            }
        }

    } // ActionMouseListener class

    private static class RowAction {

        private final String queryAction;
        private boolean copyScript;

        private String status;
        private boolean executed;
        private String sqlMessage;
        private String nameOperation;

        protected RowAction(String query) {
            executed = false;
            copyScript = true;
            queryAction = query;
            status = Constants.EMPTY;

            init();
        }

        private void init() {
            DerivedQuery derivedQuery = new DerivedQuery(queryAction);
            int type = derivedQuery.getQueryType();

            switch (type) {
                case QueryTypes.INSERT:
                    nameOperation = "INSERT RECORDS";
                    break;

                case QueryTypes.UPDATE:
                    nameOperation = "UPDATE RECORDS";
                    break;

                case QueryTypes.DELETE:
                    nameOperation = "DELETE RECORDS";
                    break;

                case QueryTypes.GRANT:
                    nameOperation = "GRANT PRIVILEGE";
                    break;

                case QueryTypes.COMMIT:
                    nameOperation = "COMMIT";
                    break;

                case QueryTypes.ROLLBACK:
                    nameOperation = "ROLLBACK";
                    break;

                case QueryTypes.SELECT_INTO:
                    nameOperation = "SELECT INTO";
                    break;

                case QueryTypes.REVOKE:
                    nameOperation = "REVOKE PRIVILEGE";
                    break;

                case QueryTypes.DROP_OBJECT:
                    nameOperation = "DROP " + derivedQuery.getMetaName();
                    break;

                case QueryTypes.COMMENT:
                    nameOperation = "ADD COMMENT";
                    break;

                case QueryTypes.CREATE_OBJECT:
                    nameOperation = "CREATE " + derivedQuery.getMetaName();
                    break;

                case QueryTypes.ALTER_OBJECT:
                    nameOperation = "ALTER " + derivedQuery.getMetaName();
                    break;

                case QueryTypes.CREATE_OR_ALTER:
                    nameOperation = "CREATE OR ALTER " + derivedQuery.getMetaName();
                    break;

                case QueryTypes.DECLARE_OBJECT:
                    nameOperation = "DECLARE " + derivedQuery.getMetaName();
                    break;

                case QueryTypes.SET_STATISTICS:
                    nameOperation = "SET STATISTICS";
                    break;

                default:
                    nameOperation = "OPERATION";
                    break;
            }
        }

    } // RowAction class

    private static class ListActionsModel extends AbstractTableModel {

        private static final int VALID = 0;
        private static final int NAME = VALID + 1;
        private static final int STATUS = NAME + 1;
        private static final int COPY = STATUS + 1;

        private final Vector<RowAction> data;
        private final String[] HEADERS = {
                Constants.EMPTY,
                bundleString("NameOperation"),
                bundleString("Status"),
                bundleString("Copy")
        };

        public ListActionsModel() {
            data = new Vector<>();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return HEADERS.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            RowAction action = data.elementAt(row);

            switch (col) {
                case VALID:
                    return IconManager.getIcon(action.executed ?
                            BrowserConstants.GRANT_IMAGE :
                            BrowserConstants.REVOKE_IMAGE
                    );

                case NAME:
                    return action.nameOperation;

                case STATUS:
                    return action.status;

                case COPY:
                    return action.copyScript ?
                            IconManager.getIcon("icon_close") :
                            Constants.EMPTY;

                default:
                    return Constants.EMPTY;
            }
        }

        @Override
        public String getColumnName(int column) {
            return HEADERS[column];
        }

        public void addRow(RowAction row) {
            data.add(row);
            fireTableDataChanged();
        }

        public void insertRow(RowAction row, int index) {
            data.insertElementAt(row, index);
            fireTableDataChanged();
        }

    } // ListActionsModel class

}
