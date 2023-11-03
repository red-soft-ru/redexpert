package org.executequery.gui;

import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowserTableCellRenderer;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.Vector;


public class ExecuteQueryDialog extends BaseDialog {

    String query;

    Boolean commitResult = false;

    StatementExecutor querySender;

    DatabaseConnection dc;

    JPanel mainPanel;

    JScrollPane tableScroll;

    JScrollPane queryScroll;

    JButton copyQueryButton;

    JButton copyErrorButton;

    JButton commitButton;

    JButton rollbackButton;

    QueryTokenizer queryTokenizer;

    JLabel listActionsLabel;

    JLabel operatorLabel;

    JLabel errorLabel;

    DefaultTable tableAction;

    SQLTextArea queryPane;

    LoggingOutputPanel errorPane;

    LoggingOutputPanel logPane;

    ListActionsModel model;

    String delimiter = ";";
    boolean stopOnError;

    JTabbedPane tabbedPane;

    boolean autocommit;

    public static void setClipboard(String str) {
        StringSelection ss = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }

    public ExecuteQueryDialog(String name, String query, DatabaseConnection databaseConnection, boolean keepAlive) {
        this(name, query, databaseConnection, keepAlive, ";");
    }

    public ExecuteQueryDialog(String name, String query, DatabaseConnection databaseConnection, boolean keepAlive, String delimiter) {
        this(name, query, databaseConnection, keepAlive, delimiter, false, true);

    }

    public ExecuteQueryDialog(String name, String query, DatabaseConnection databaseConnection, boolean keepAlive, String delimiter, boolean autocommit, boolean stopOnError) {
        super(name, true, true);
        this.query = query;
        this.delimiter = delimiter;
        this.dc = databaseConnection;
        this.stopOnError = stopOnError;
        queryTokenizer = new QueryTokenizer();
        querySender = new DefaultStatementExecutor(dc, keepAlive);
        this.autocommit = autocommit;
        querySender.setCommitMode(autocommit);
        init();
        SwingWorker sw = new SwingWorker("ExecuteQueriesInDialog") {
            @Override
            public Object construct() {
                execute();
                return null;
            }
        };
        sw.start();

    }


    void init() {
        mainPanel = new JPanel();
        listActionsLabel = new JLabel();
        tableScroll = new JScrollPane();
        operatorLabel = new JLabel();
        queryScroll = new JScrollPane();
        errorLabel = new JLabel();
        copyQueryButton = new JButton();
        copyErrorButton = new JButton();
        commitButton = new JButton();
        rollbackButton = new JButton();
        queryPane = new SQLTextArea();
        errorPane = new LoggingOutputPanel();
        logPane = new LoggingOutputPanel();
        tabbedPane = new JTabbedPane();
        tableAction = new DefaultTable();
        BrowserTableCellRenderer bctr = new BrowserTableCellRenderer();
        tableAction.setDefaultRenderer(Object.class, bctr);
        model = new ListActionsModel();
        tableAction.setModel(model);
        tableScroll.setViewportView(tableAction);
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent tableModelEvent) {

            }
        });
        tableAction.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int row = tableAction.getSelectedRow();
                if (row >= 0) {
                    if (mouseEvent.getClickCount() > 1) {
                        if (tableAction.getSelectedColumn() == model.COPY) {
                            model.data.elementAt(row).copyScript = !model.data.elementAt(row).copyScript;
                            model.fireTableDataChanged();
                        }
                    }
                }
            }
        });
        tableAction.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = tableAction.getSelectedRow();
                if (row >= 0) {
                    setMessages(model.data.elementAt(row));
                    tabbedPane.setSelectedComponent(errorPane);
                }
            }
        });
        tableAction.getTableHeader().setReorderingAllowed(false);
        tableAction.setCellSelectionEnabled(true);
        tableAction.setColumnSelectionAllowed(false);
        tableAction.setSurrendersFocusOnKeystroke(true);
        TableColumnModel tcm = tableAction.getColumnModel();
        tcm.getColumn(model.EXECUTED).setPreferredWidth(25);
        tcm.getColumn(model.NAME_OPERATION).setPreferredWidth(200);
        tcm.getColumn(model.STATUS).setPreferredWidth(200);
        tcm.getColumn(model.COPY).setPreferredWidth(50);
        tableScroll.setPreferredSize(new Dimension(475, 200));
        queryPane.setRows(5);

        listActionsLabel.setText(bundleString("listActions"));

        operatorLabel.setText(bundleString("operator"));

        errorLabel.setText(bundleString("error"));

        copyQueryButton.setText(bundleString("copyQuery"));
        copyQueryButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyQueryButtonActionPerformed(evt);
            }
        });

        copyErrorButton.setText(bundleString("copyError"));
        copyErrorButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyErrorButtonActionPerformed(evt);
            }
        });


        commitButton.setText(bundleString("commit"));
        if (autocommit)
            commitButton.setText(Bundles.getCommon("ok.button"));
        commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commitButtonActionPerformed(evt);
            }
        });

        rollbackButton.setText(bundleString("rollback"));
        rollbackButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rollback();
            }
        });

        queryScroll.setViewportView(queryPane);

        //queryPane.setText(query);
        queryPane.setEditable(false);

        tabbedPane.addTab(bundleString("Single"), errorPane);
        tabbedPane.addTab(bundleString("FullLog"), logPane);
        tabbedPane.setPreferredSize(new Dimension(475, 200));

        mainPanel.setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();
        JSplitPane tableSQL = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        tableSQL.setTopComponent(tableScroll);
        tableSQL.setBottomComponent(queryScroll);
        tableSQL.setDividerLocation(0.5);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(tableSQL);
        splitPane.setBottomComponent(tabbedPane);
        tableSQL.setDividerLocation(0.6);
        mainPanel.add(listActionsLabel, gbh.setLabelDefault().get());
        mainPanel.add(splitPane, gbh.nextRowFirstCol().fillBoth().spanX().setMaxWeightY().get());
        mainPanel.add(copyQueryButton, gbh.nextRowFirstCol().setLabelDefault().anchorSouthWest().get());
        mainPanel.add(copyErrorButton, gbh.nextCol().setLabelDefault().anchorSouthWest().get());
        mainPanel.add(commitButton, gbh.nextCol().setLabelDefault().anchorSouthEast().get());
        if (!autocommit)
            mainPanel.add(rollbackButton, gbh.nextCol().setLabelDefault().anchorSouthEast().get());
        addDisplayComponent(mainPanel);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDialog();
            }
        });
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                closeDialog();

            }
        };
        mainPanel.registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void closeDialog() {
        rollback();
    }

    private void setMessages(RowAction row) {
        queryPane.setText(row.queryAction);
        if (row.executed)
            setOutputMessage(SqlMessages.PLAIN_MESSAGE, row.SQLmessage);
        else setOutputMessage(SqlMessages.ERROR_MESSAGE, row.SQLmessage);
    }

    private void addMessages(RowAction row) {
        if (row.executed)
            addOutputMessage(SqlMessages.PLAIN_MESSAGE, row.SQLmessage);
        else addOutputMessage(SqlMessages.ERROR_MESSAGE, row.SQLmessage);
    }

    private void copyErrorButtonActionPerformed(ActionEvent evt) {
        try {
            Vector<RowAction> v = model.data;
            String copy = "";
            for (int i = 0; i < v.size(); i++) {
                if (v.elementAt(i).copyScript)
                    copy += v.elementAt(i).SQLmessage + "\n";
            }
            setClipboard(copy);
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    private void copyQueryButtonActionPerformed(ActionEvent evt) {
        try {
            Vector<RowAction> v = model.data;
            String copy = "";
            if (!delimiter.equals(";"))
                copy += "SET TERM " + delimiter + ";\n";
            for (int i = 0; i < v.size(); i++) {
                if (v.elementAt(i).copyScript)
                    copy += v.elementAt(i).queryAction + delimiter + "\n";
            }
            if (!delimiter.equals(";"))
                copy += "SET TERM ;" + delimiter;
            setClipboard(copy);
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    private void commitButtonActionPerformed(ActionEvent evt) {
        try {
            SqlStatementResult rs = querySender.execute(QueryTypes.COMMIT, "commit");
            String error = rs.getErrorMessage();
            if (!rs.isException()) {
                commitResult = true;
                finished();
            } else {
                setOutputMessage(SqlMessages.ERROR_MESSAGE, error);
                commitButton.setVisible(false);
            }
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    public boolean getCommit() {
        return commitResult;
    }

    private void rollback() {
        try {
            querySender.execute(QueryTypes.ROLLBACK, "rollback");
            finished();
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
            finished();

        }
    }

    void execute() {
        String queries = query;
        if (queries.endsWith(delimiter)) {
            queries = queries.substring(0, queries.length() - 1);
        }
        String query = "";
        boolean commit = true;
        int startIndex = 0;
        String lowQuery = queries.toLowerCase();
        QueryTokenizer queryTokenizer = new QueryTokenizer();
        queryTokenizer.extractTokens(queries);
        int success = 0;
        int failed = 0;
        if (!stopOnError)
            tabbedPane.setSelectedComponent(logPane);
        while (queries.trim().length() > 0 && (commit || !stopOnError)) {
            QueryTokenizer.QueryTokenized fquery = queryTokenizer.tokenizeFirstQuery(queries, lowQuery, startIndex, delimiter);
            queries = fquery.script;
            delimiter = fquery.delimiter;
            DerivedQuery dQuery = fquery.query;
            lowQuery = fquery.lowScript;
            startIndex = fquery.startIndex;
            if (dQuery == null)
                continue;
            query = dQuery.getDerivedQuery();
            while (query.indexOf("\n") == 0) {
                query = query.substring(1);
            }
            RowAction action = new RowAction(query);
            commit = execute_query(action);
            if (commit)
                model.addRow(action);
            else model.insertRow(action, 0);
            setMessages(action);
            addMessages(action);
            if (action.executed)
                success++;
            else failed++;
        }
        if (success > 0)
            addOutputMessage(SqlMessages.PLAIN_MESSAGE, success + " queries successfully completed");
        if (failed > 0)
            addOutputMessage(SqlMessages.ERROR_MESSAGE, failed + " queries failed");
    }

    boolean execute_query(RowAction query) {
        try {
            DerivedQuery q = new DerivedQuery(query.queryAction);
            String queryToExecute = q.getOriginalQuery();
            int type = q.getQueryType();
            String metaName = q.getMetaName();
            String name = q.getObjectName();
            Log.info("Executing:" + queryToExecute);
            SqlStatementResult result = querySender.execute(type, queryToExecute);
            int updateCount = result.getUpdateCount();
            if (updateCount == -1) {
                query.status = "Error";
                query.SQLmessage = result.getErrorMessage();
                commitButton.setVisible(false);
                return false;

            } else {

                if (result.isException()) {
                    query.status = "Error";
                    query.SQLmessage = result.getErrorMessage();
                    commitButton.setVisible(false);
                    return false;
                } else {
                    type = result.getType();
                    query.SQLmessage = QueryTypes.getResultText(updateCount, type, metaName, name);
                    query.executed = true;
                    query.status = bundleString("Success");
                    return true;

                }
            }
        } catch (Exception e) {
            query.status = "Error";
            query.SQLmessage = e.getMessage();
            commitButton.setVisible(false);
            return false;
        }
    }

    private void logExecution(String query) {
        setOutputMessage(
                SqlMessages.ACTION_MESSAGE, "Executed");
        setOutputMessage(
                SqlMessages.ACTION_MESSAGE_PREFORMAT, query);
    }
    void setOutputMessage(int type, String text) {
        errorPane.clear();
        errorPane.append(type, text);
    }

    void addOutputMessage(int type, String text) {
        logPane.append(type, text);
    }

    String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

    public class RowAction {
        boolean executed;

        String nameOperation;

        String status;

        boolean copyScript;

        String queryAction;

        String SQLmessage;

        public RowAction(String query) {
            queryAction = query;
            executed = false;
            status = "";
            copyScript = true;
            constructName();

        }

        void constructName() {
            DerivedQuery q = new DerivedQuery(queryAction);

            int type = q.getQueryType();
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
                    nameOperation = "DROP " + q.getMetaName();
                    break;
                case QueryTypes.COMMENT:
                    nameOperation = "ADD COMMENT";
                    break;
                case QueryTypes.CREATE_OBJECT:
                    nameOperation = "CREATE " + q.getMetaName();
                    break;
                case QueryTypes.ALTER_OBJECT:
                    nameOperation = "ALTER " + q.getMetaName();
                    break;
                case QueryTypes.CREATE_OR_ALTER:
                    nameOperation = "CREATE OR ALTER " + q.getMetaName();
                    break;
                case QueryTypes.SET_STATISTICS:
                    nameOperation="SET STATISTICS";
                    break;
                default:
                    nameOperation = "OPERATION";
                    break;

            }

        }

    }

    public class ListActionsModel extends AbstractTableModel {
        final int EXECUTED = 0;
        final int NAME_OPERATION = 1;
        final int STATUS = 2;
        final int COPY = 3;

        Vector<RowAction> data;
        String[] headers = {"", bundleString("NameOperation"), bundleString("Status"), bundleString("Copy")};

        public ListActionsModel() {
            data = new Vector<>();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return headers.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            RowAction action = data.elementAt(row);
            switch (col) {
                case EXECUTED:
                    if (action.executed)
                        return GUIUtilities.loadIcon("grant.png");
                    else return GUIUtilities.loadIcon("no_grant.png");
                case NAME_OPERATION:
                    return action.nameOperation;
                case STATUS:
                    return action.status;
                case COPY:
                    if (action.copyScript)
                        return GUIUtilities.loadIcon("CloseDockable.png");
                    else return "";
                default:
                    return "";
            }
        }

        public String getColumnName(int column) {
            return headers[column];
        }

        public void addRow(RowAction row) {
            data.add(row);
            fireTableDataChanged();
        }

        public void insertRow(RowAction row, int index) {
            data.insertElementAt(row, index);
            fireTableDataChanged();
        }
    }

}
