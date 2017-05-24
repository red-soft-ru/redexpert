/*
 * QueryEditorDelegate.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.repository.RepositoryCache;
import org.executequery.repository.SqlCommandHistoryRepository;
import org.executequery.sql.QueryDelegate;
import org.executequery.sql.QueryDispatcher;
import org.executequery.util.ThreadUtils;

public class QueryEditorDelegate implements QueryDelegate {

    private int currentStatementHistoryIndex = -1;

    private final QueryDispatcher dispatcher;

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
     * @param the connection thats been closed
     */
    public void disconnected(DatabaseConnection dc) {

        dispatcher.disconnected(dc);
    }

    public void close() {

        interrupt();
        dispatcher.closeConnection();
    }

    public void commit() {

        executeQuery("commit");
    }

    public void rollback() {

        executeQuery("rollback");
    }

    public void commitModeChanged(boolean autoCommit) {

        queryEditor.commitModeChanged(autoCommit);
    }

    public void executeQuery(String query) {

        executeQuery(queryEditor.getSelectedConnection(), query, false);
    }

    public void executeQuery(String query, boolean executeAsBlock) {

        queryEditor.preExecute();

        executeQuery(queryEditor.getSelectedConnection(), query, executeAsBlock);
    }

    public void executeQuery(DatabaseConnection selectedConnection,
            String query, boolean executeAsBlock) {

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
            dispatcher.executeSQLQuery(selectedConnection, query, executeAsBlock);
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

    public boolean isLogEnabled() {

        return OutputLogger.isLogEnabled();
    }

    public void log(String message) {

        if (isLogEnabled()) {

            OutputLogger.info(message);
        }
    }

    public void setOutputMessage(int type, String text) {

        queryEditor.setOutputMessage(type, text);
    }

    public void setOutputMessage(int type, String text, boolean selectTab) {

        queryEditor.setOutputMessage(type, text, selectTab);
    }

    public void setResult(int result, int type) {

        queryEditor.setResultText(result, type);
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

                sqlCommandHistoryRepository().addSqlCommand(query);
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

        return sqlCommandHistoryRepository().getSqlCommandHistory();
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

    /** ignored statements for the history list */
    private final String[] HISTORY_IGNORE = {"COMMIT", "ROLLBACK"};

    private SqlCommandHistoryRepository sqlCommandHistoryRepository() {

        return (SqlCommandHistoryRepository)RepositoryCache.load(
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








