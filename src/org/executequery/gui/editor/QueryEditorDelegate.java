/*
 * QueryEditorDelegate.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.editor;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.log.Log;
import org.executequery.repository.RepositoryCache;
import org.executequery.repository.SqlCommandHistoryRepository;
import org.executequery.sql.QueryDelegate;
import org.executequery.sql.QueryDispatcher;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.sqlParser.SqlParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class QueryEditorDelegate implements QueryDelegate {

    private int currentStatementHistoryIndex = -1;

    private QueryDispatcher dispatcher = null;

    private final QueryEditor queryEditor;

    public QueryEditorDelegate(QueryEditor queryEditor) {

        super();
        this.queryEditor = queryEditor;

        dispatcher = new QueryDispatcher(this);

    }

    public void destroyConnection() {

        dispatcher.destroyConnection();
    }

    public void pauseExecution() {

        dispatcher.pauseExecution();
    }

    public void resumeExecution() {

        dispatcher.resumeExecution();
    }

    /**
     * Returns whether a statement execution is in progress.
     *
     * @return true | false
     */
    public boolean isExecuting() {

        return dispatcher.isExecuting();
    }

    /**
     * Sets the editor's auto-commit mode to that specified.
     */
    public void setCommitMode(boolean mode) {

        dispatcher.setAutoCommit(mode);
    }

    /**
     * Returns the editor's current auto-commit mode.
     */
    public boolean getCommitMode() {

        return dispatcher.getCommitMode();
    }

    public void preferencesChanged() {

        dispatcher.preferencesChanged();
    }

    /**
     * Indicates a connection has been closed.
     *
     * @param dc connection thats been closed
     */
    public void disconnected(DatabaseConnection dc) {

        dispatcher.disconnected(dc);
    }

    public void close() {

        interrupt();
        dispatcher.closeConnection();
    }

    public void commit(boolean anyConnections) {

        executeQuery("commit", anyConnections);
    }

    public void rollback(boolean anyConnections) {

        executeQuery("rollback", anyConnections);
    }

    public void commitModeChanged(boolean autoCommit) {

        queryEditor.commitModeChanged(autoCommit);
    }

    public void executeQuery(String query, boolean anyConnections) {

        executeQuery(queryEditor.getSelectedConnection(), query, false, anyConnections);
    }

    public void executeQuery(String query, boolean executeAsBlock, boolean anyConnections) {

        queryEditor.preExecute();

        executeQuery(queryEditor.getSelectedConnection(), query, executeAsBlock, anyConnections);
    }

    @Override
    public void setTPP(TransactionParametersPanel tpp) {
        dispatcher.setTpp(tpp);
    }

    @Override
    public TransactionParametersPanel getTPP() {
        return dispatcher.getTpp();
    }

    public void executeQuery(DatabaseConnection selectedConnection,
                             String query, boolean executeAsBlock, boolean anyConnections) {

        if (dispatcher.isExecuting()) {

            return;
        }

        if (query == null) {

            query = queryEditor.getEditorText();
        }

        if (StringUtils.isNotBlank(query)) {

            currentStatementHistoryIndex = -1;
            queryEditor.setHasPreviousStatement(true);
            queryEditor.setHasNextStatement(false);
            dispatcher.executeSQLQuery(selectedConnection, query, executeAsBlock, anyConnections);
        }

    }

    public void executeQueryInProfiler(
            DatabaseConnection selectedConnection, String query, boolean executeAsBlock) {

        if (dispatcher.isExecuting())
            return;

        if (query == null)
            query = queryEditor.getEditorText();

        if (StringUtils.isNotBlank(query)) {

            currentStatementHistoryIndex = -1;
            queryEditor.setHasPreviousStatement(true);
            queryEditor.setHasNextStatement(false);
            dispatcher.executeSQLQueryInProfiler(selectedConnection, query, executeAsBlock);
        }

    }

    public void executeScript(DatabaseConnection selectedConnection,
                              String script, boolean anyConnections) {

        if (dispatcher.isExecuting()) {

            return;
        }

        if (script == null) {

            script = queryEditor.getEditorText();
        }

        if (StringUtils.isNotBlank(script)) {

            currentStatementHistoryIndex = -1;
            queryEditor.setHasPreviousStatement(true);
            queryEditor.setHasNextStatement(false);
            dispatcher.executeSQLScript(selectedConnection, script, anyConnections);
        }

    }

    public void printExecutedPlan(DatabaseConnection selectedConnection,
                                  String query, boolean explained) {

        if (dispatcher.isExecuting()) {

            return;
        }

        if (query == null) {

            query = queryEditor.getEditorText();
        }

        if (StringUtils.isNotBlank(query)) {
            query = new SqlParser(query, "").getProcessedSql();
            dispatcher.printExecutedPlan(selectedConnection, query, explained, false);
        }

    }

    public void executing() {

        queryEditor.executing();
    }

    public void finished(String message) {

        queryEditor.finished(message);
    }

    public void interrupt() {

        dispatcher.interruptStatement();
    }

    @Override
    public boolean isLogEnabled() {
        return true;
    }

    @Override
    public void log(String message) {
        Log.info(message);
    }

    public void setOutputMessage(DatabaseConnection dc, int type, String text) {

        queryEditor.setOutputMessage(dc, type, text);
    }

    public void setOutputMessage(DatabaseConnection dc, int type, String text, boolean selectTab) {

        queryEditor.setOutputMessage(dc, type, text, selectTab);
    }

    public void setResult(DatabaseConnection dc, int result, int type, String metaName) {

        queryEditor.setResultText(dc, result, type, metaName);
    }

    public void setResultSet(ResultSet rs, String query) throws SQLException {

        queryEditor.setResultSet(rs, query);
    }

    public void setStatusMessage(String text) {

        queryEditor.setLeftStatusText(text);
    }

    public void statementExecuted(String statement) {

        String _query = statement.toUpperCase();

        for (int i = 0; i < HISTORY_IGNORE.length; i++) {

            if (HISTORY_IGNORE[i].compareTo(_query) == 0) {

                return;
            }

        }

        addSqlCommandToHistory(statement);
    }

    /**
     * Selects the next query from the history list and places the
     * query text into the editor.
     */
    public String getNextQuery() {

        int index = decrementHistoryNum();

        if (index >= 0) {

            return getSqlCommandHistory().get(index);
        }

        return "";
    }

    /**
     * Selects the previous query from the history list and places the
     * query text into the editor.
     */
    public String getPreviousQuery() {

        int index = incrementHistoryNum();

        if (index >= 0) {

            return getSqlCommandHistory().get(index);
        }
        return "";
    }

    private void addSqlCommandToHistory(final String query) {

        ThreadUtils.startWorker(new Runnable() {
            public void run() {

                sqlCommandHistoryRepository().addSqlCommand(query, queryEditor.getSelectedConnection().getId());
            }
        });

    }

    /**
     * Increments the history index value.
     */
    private int incrementHistoryNum() {

        //  for previous
        Vector<String> history = getSqlCommandHistory();

        if (!history.isEmpty()) {

            int historyCount = history.size();

            if (currentStatementHistoryIndex < historyCount - 1) {

                currentStatementHistoryIndex++;
            }

            queryEditor.setHasNextStatement(true);

            if (currentStatementHistoryIndex == historyCount - 1) {

                queryEditor.setHasPreviousStatement(false);
            }
        }

        return currentStatementHistoryIndex;
    }

    private Vector<String> getSqlCommandHistory() {
        String id;
        if (queryEditor.getSelectedConnection() == null)
            id = QueryEditorHistory.NULL_CONNECTION;
        else id = queryEditor.getSelectedConnection().getId();
        return sqlCommandHistoryRepository().getSqlCommandHistory(id);
    }

    /**
     * Decrements the history index value.
     */
    private int decrementHistoryNum() {

        if (!getSqlCommandHistory().isEmpty()) {

            if (currentStatementHistoryIndex > 0) {

                currentStatementHistoryIndex--;
            }

            queryEditor.setHasPreviousStatement(true);

            if (currentStatementHistoryIndex == 0) {

                queryEditor.setHasNextStatement(false);
            }
        }
        return currentStatementHistoryIndex;
    }

    /**
     * ignored statements for the history list
     */
    private final String[] HISTORY_IGNORE = {"COMMIT", "ROLLBACK"};

    private SqlCommandHistoryRepository sqlCommandHistoryRepository() {

        return (SqlCommandHistoryRepository) RepositoryCache.load(
                SqlCommandHistoryRepository.REPOSITORY_ID);
    }

    /**
     * Returns whether a call to previous history would be successful.
     */
    public boolean hasPreviousStatement() {

        return currentStatementHistoryIndex < getSqlCommandHistory().size() - 1;
    }

    /**
     * Returns whether a call to next history would be successful.
     */
    public boolean hasNextStatement() {

        return currentStatementHistoryIndex > 0;
    }

    /**
     * Returns the executed query history list.
     */
    public Vector<String> getHistoryList() {

        return getSqlCommandHistory();
    }

}









