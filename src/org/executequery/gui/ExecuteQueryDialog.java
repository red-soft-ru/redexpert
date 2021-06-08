package org.executequery.gui;

import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowserTableCellRenderer;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.gui.text.SQLTextPane;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.DerivedQuery;
import org.executequery.sql.QueryTokenizer;
import org.executequery.sql.SqlMessages;
import org.executequery.sql.SqlStatementResult;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

    SQLTextPane queryPane;

    LoggingOutputPanel errorPane;

    ListActionsModel model;

    String delimiter = ";";

    public static void setClipboard(String str) {
        StringSelection ss = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }

    public ExecuteQueryDialog(String name, String query, DatabaseConnection databaseConnection, boolean keepAlive) {
        this(name, query, databaseConnection, keepAlive, ";");
    }

    public ExecuteQueryDialog(String name, String query, DatabaseConnection databaseConnection, boolean keepAlive, String delimiter) {
        super(name, true, true);
        this.query = query;
        this.delimiter = delimiter;
        this.dc = databaseConnection;
        queryTokenizer = new QueryTokenizer();
        querySender = new DefaultStatementExecutor(dc, keepAlive);
        querySender.setCommitMode(false);
        init();
        execute();

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
        queryPane = new SQLTextPane();
        errorPane = new LoggingOutputPanel();
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
        tableAction.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int row = tableAction.getSelectedRow();
                if (row >= 0) {
                    if (mouseEvent.getClickCount() > 1) {
                        if (tableAction.getSelectedColumn() == model.COPY) {
                            model.data.elementAt(row).copyScript = !model.data.elementAt(row).copyScript;
                            model.fireTableDataChanged();
                        }
                    } else {
                        setMessages(model.data.elementAt(row));
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

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
        commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commitButtonActionPerformed(evt);
            }
        });

        rollbackButton.setText(bundleString("rollback"));
        rollbackButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rollbackButtonActionPerformed(evt);
            }
        });

        queryScroll.setViewportView(queryPane);

        //queryPane.setText(query);
        queryPane.setEditable(false);

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGap(10)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(listActionsLabel)
                                                .addComponent(tableScroll)
                                                .addComponent(operatorLabel)
                                                .addComponent(queryScroll, GroupLayout.PREFERRED_SIZE, 500, Short.MAX_VALUE)
                                                .addComponent(errorLabel)
                                                .addComponent(errorPane)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(copyQueryButton)
                                                                .addContainerGap()
                                                                .addComponent(copyErrorButton)
                                                        )
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(commitButton)
                                                                .addContainerGap()
                                                                .addComponent(rollbackButton)
                                                        )
                                                )
                                        )
                        )
                        .addGap(10)
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(10)
                                .addComponent(listActionsLabel)
                                .addContainerGap()
                                .addComponent(tableScroll, GroupLayout.PREFERRED_SIZE, 150, /*GroupLayout.PREFERRED_SIZE*/Short.MAX_VALUE)
                                .addContainerGap()
                                .addComponent(operatorLabel)
                                .addContainerGap()
                                .addComponent(queryScroll, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()
                                .addComponent(errorLabel)
                                .addContainerGap()
                                .addComponent(errorPane, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .addGap(18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(rollbackButton)
                                                .addComponent(commitButton)
                                        )
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(copyErrorButton)
                                                .addComponent(copyQueryButton)
                                        )
                                )
                                .addGap(10)
                        )
        );
        addDisplayComponent(mainPanel);
    }

    private void setMessages(RowAction row) {
        queryPane.setText(row.queryAction);
        if (row.executed)
            setOutputMessage(SqlMessages.PLAIN_MESSAGE, row.SQLmessage);
        else setOutputMessage(SqlMessages.ERROR_MESSAGE, row.SQLmessage);
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
                    copy += v.elementAt(i).queryAction + ";\n";
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
                super.finished();
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

    private void rollbackButtonActionPerformed(ActionEvent evt) {
        try {
            querySender.execute(QueryTypes.ROLLBACK, "rollback");
            super.finished();
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
            super.finished();

        }
    }

    void execute() {
        String querys = query;
        if (querys.endsWith(";")) {
            querys = querys.substring(0, querys.length() - 1);
        }
        String query = "";
        boolean commit = true;
        while (querys.trim().length() > 0 && commit) {
            if (querys.contains(delimiter)) {
                query = querys.substring(0, querys.indexOf(delimiter));
                querys = querys.substring(querys.indexOf(delimiter) + 1, querys.length());
            } else {
                query = querys;
                querys = "";
            }
            while (query.indexOf("\n") == 0) {
                query = query.substring(1, query.length());
            }
            RowAction action = new RowAction(query);
            commit = execute_query(action);
            model.addRow(action);
            setMessages(action);
        }
    }

    boolean execute_query(RowAction query) {
        try {
            DerivedQuery q = new DerivedQuery(query.queryAction);
            String queryToExecute = q.getOriginalQuery();
            int type = q.getQueryType();
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
                    query.SQLmessage = getResultText(updateCount, type);
                    query.executed = true;
                    query.status = "Success";
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

    public String getResultText(int result, int type) {

        String row = " row ";
        if (result > 1 || result == 0) {

            row = " rows ";
        }

        String rText = null;
        switch (type) {
            case QueryTypes.INSERT:
                rText = row + "created.";
                break;
            case QueryTypes.UPDATE:
                rText = row + "updated.";
                break;
            case QueryTypes.DELETE:
                rText = row + "deleted.";
                break;
            case QueryTypes.DROP_TABLE:
                rText = "Table dropped.";
                break;
            case QueryTypes.CREATE_TABLE:
                rText = "Table created.";
                break;
            case QueryTypes.ALTER_TABLE:
                rText = "Table altered.";
                break;
            case QueryTypes.CREATE_SEQUENCE:
                rText = "Sequence created.";
                break;
            case QueryTypes.CREATE_PROCEDURE:
                rText = "Procedure created.";
                break;
            case QueryTypes.CREATE_TRIGGER:
                rText = "Trigger created.";
                break;
            case QueryTypes.CREATE_FUNCTION:
                rText = "Function created.";
                break;
            case QueryTypes.GRANT:
                rText = "Grant succeeded.";
                break;
            case QueryTypes.CREATE_SYNONYM:
                rText = "Synonym created.";
                break;
            case QueryTypes.COMMIT:
                rText = "Commit complete.";
                break;
            case QueryTypes.ROLLBACK:
                rText = "Rollback complete.";
                break;
            case QueryTypes.SELECT_INTO:
                rText = "Statement executed successfully.";
                break;
            case QueryTypes.CREATE_ROLE:
                rText = "Role created.";
                break;
            case QueryTypes.REVOKE:
                rText = "Revoke succeeded.";
                break;
            case QueryTypes.DROP_OBJECT:
                rText = "Object dropped.";
                break;
            case QueryTypes.COMMENT:
                rText = "Description added";
                break;
            case QueryTypes.CREATE_OBJECT:
            case QueryTypes.CREATE_OR_ALTER:
                rText = "Object created";
                break;
            case QueryTypes.ALTER_OBJECT:
                rText = "Object altered";
                break;
            case QueryTypes.UNKNOWN:
            case QueryTypes.EXECUTE:
                if (result > -1) {
                    rText = result + row + "affected.\nStatement executed successfully.";
                } else {
                    rText = "Statement executed successfully.";
                }
                break;
        }

        StringBuilder sb = new StringBuilder();
        if ((result > -1 && type >= QueryTypes.ALL_UPDATES) && type != QueryTypes.UNKNOWN) {

            sb.append(result);
        }

        sb.append(rText);

        return sb.toString();//setOutputMessage(SqlMessages.PLAIN_MESSAGE, sb.toString());

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
            ConctructName();

        }

        void ConctructName() {
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
                case QueryTypes.DROP_TABLE:
                    nameOperation = "DROP TABLE";
                    break;
                case QueryTypes.CREATE_TABLE:
                    nameOperation = "CREATE TABLE";
                    break;
                case QueryTypes.ALTER_TABLE:
                    nameOperation = "ALTER TABLE";
                    break;
                case QueryTypes.CREATE_TRIGGER:
                    nameOperation = "CREATE OR ALTER TRIGGER";
                    break;
                case QueryTypes.CREATE_SEQUENCE:
                    nameOperation = "CREATE SEQUENCE";
                    break;
                case QueryTypes.CREATE_PROCEDURE:
                    nameOperation = "CREATE PROCEDURE";
                    break;
                case QueryTypes.CREATE_FUNCTION:
                    nameOperation = "CREATE FUNCTION";
                    break;
                case QueryTypes.GRANT:
                    nameOperation = "GRANT PRIVILEGE";
                    break;
                case QueryTypes.CREATE_SYNONYM:
                    nameOperation = "CREATE SYNONYM";
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
                case QueryTypes.CREATE_ROLE:
                    nameOperation = "CREATE ROLE";
                    break;
                case QueryTypes.REVOKE:
                    nameOperation = "REVOKE PRIVILEGE";
                    break;
                case QueryTypes.DROP_OBJECT:
                    nameOperation = "DROP OBJECT";
                    break;
                case QueryTypes.COMMENT:
                    nameOperation = "ADD DESCRIPTION";
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
        String[] headers = {"", "Name operation", "Status", "Copy"};

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
    }

}
