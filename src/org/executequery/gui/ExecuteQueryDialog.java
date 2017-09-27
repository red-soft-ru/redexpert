package org.executequery.gui;

import org.executequery.GUIUtilities;
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
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class ExecuteQueryDialog extends BaseDialog {

    String query;

    Boolean commitResult=false;

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

    JTable tableAction;

    SQLTextPane queryPane;

    LoggingOutputPanel errorPane;

    public ExecuteQueryDialog(String name, String query, DatabaseConnection databaseConnection, boolean keepAlive) {
        super(name, true, false);
        this.query = query;
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

        listActionsLabel.setText("listActions");

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

        queryPane.setText(query);
        queryPane.setEditable(false);

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGap(10)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
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

    private void copyErrorButtonActionPerformed(ActionEvent evt) {
        try
        {
            errorPane.selectAll();
            errorPane.copy();
        }
        catch (Exception e)
        {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    private void copyQueryButtonActionPerformed(ActionEvent evt) {
        try
        {
            queryPane.selectAll();
            queryPane.copy();
        }
        catch (Exception e)
        {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    private void commitButtonActionPerformed(ActionEvent evt) {
        try {
            querySender.execute(QueryTypes.COMMIT, "commit");
            commitResult=true;
            super.finished();
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    public boolean getCommit()
    {
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
        try {
            DerivedQuery q = new DerivedQuery(query);

            String derivedQueryString = q.getDerivedQuery();
            String queryToExecute = q.getOriginalQuery();

            int type = q.getQueryType();
            long start = System.currentTimeMillis();
            SqlStatementResult result = querySender.execute(type, queryToExecute);
            int updateCount = result.getUpdateCount();
            if (updateCount == -1) {

                setOutputMessage(SqlMessages.ERROR_MESSAGE,
                        result.getErrorMessage());
                commitButton.setVisible(false);

            } else {

                if (result.isException()) {

                    setOutputMessage(SqlMessages.ERROR_MESSAGE, result.getErrorMessage());
                    commitButton.setVisible(false);
                } else {

                    type = result.getType();
                    setResultText(updateCount, type);

                }
            }
        } catch (Exception e) {
            setOutputMessage(SqlMessages.ERROR_MESSAGE, e.getMessage());
            commitButton.setVisible(false);
        }


    }

    public void setResultText(int result, int type) {

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

        setOutputMessage(SqlMessages.PLAIN_MESSAGE, sb.toString());

    }

    private void logExecution(String query) {
        setOutputMessage(
                SqlMessages.ACTION_MESSAGE, "Executed");
        setOutputMessage(
                SqlMessages.ACTION_MESSAGE_PREFORMAT, query);
    }

    void setOutputMessage(int type, String text) {
        errorPane.append(type, text);
    }

    String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

}
