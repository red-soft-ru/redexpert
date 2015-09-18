/*
 * QueryDispatcher.java
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

package org.executequery.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.datasource.ConnectionManager;
import org.executequery.log.Log;
import org.executequery.util.ThreadUtils;
import org.executequery.util.ThreadWorker;
import org.executequery.util.UserProperties;
import org.underworldlabs.util.MiscUtils;

/**
 * Determines the type of exeuted query and returns appropriate results.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class QueryDispatcher {

    /** the parent controller */
    private QueryDelegate delegate;

    /** thread worker object */
    private ThreadWorker worker;

    /** the query sender database mediator */
    private StatementExecutor querySender;

    /** indicates verbose logging output */
    private boolean verboseLogging;

    /** Indicates that an execute is in progress */
    private boolean executing;

    /** connection commit mode */
    private boolean autoCommit;

    /** The query execute duration time */
    private String duration;

    /** indicates that the current execution has been cancelled */
    private boolean statementCancelled;

    private QueryTokenizer queryTokenizer;

    private boolean waiting;

    // ------------------------------------------------
    // static string outputs
    // ------------------------------------------------

    private static final String SUBSTRING = "...";
    private static final String EXECUTING = "Executing: ";
    private static final String ERROR_EXECUTING = " Error executing statement";
    private static final String DONE = " Done";
    private static final String COMMITTING_LAST = "Committing last transaction block...";
    private static final String ROLLING_BACK_LAST = "Rolling back last transaction block...";

    // ------------------------------------------------


    public QueryDispatcher(QueryDelegate runner) {

        this.delegate = runner;

        querySender = new DefaultStatementExecutor(null, true);

        setAutoCommit(userProperties().getBooleanProperty("editor.connection.commit"));

        initialiseLogging();

        queryTokenizer = new QueryTokenizer();
    }

    public void preferencesChanged() {

        initialiseLogging();
    }

    private UserProperties userProperties() {

        return UserProperties.getInstance();
    }

    private void initialiseLogging() {

        verboseLogging = userProperties().getBooleanProperty("editor.logging.verbose");

        newLineMatcher = Pattern.compile("\n").matcher("");
    }

    /**
     * Sets the commit mode to that specified.
     *
     * @param the commit mode
     */
    public void setAutoCommit(boolean autoCommit) {

        this.autoCommit = autoCommit;

        querySender.setCommitMode(autoCommit);

        delegate.commitModeChanged(autoCommit);
    }

    /**
     * Propagates the call to close the connection to
     * the QuerySender object.
     */
    public void closeConnection() {
        try {
            if (querySender != null) {
                querySender.closeConnection();
            }
        }
        catch (SQLException sqlExc) {}
    }

    /**
     * Indicates a connection has been closed.
     * Propagates the call to the query sender object.
     *
     * @param the connection thats been closed
     */
    public void disconnected(DatabaseConnection dc) {
        querySender.disconnected(dc);
    }

    /**
     * Returns the current commit mode.
     *
     * @return the commit mode
     */
    public boolean getCommitMode() {
        return autoCommit;
    }

    /**
     * Executes the query(ies) as specified. The executeAsBlock flag
     * indicates that the query should be executed in its entirety -
     * not split up into mulitple queries (where applicable).
     *
     * @param the query string
     * @param true to execute in entirety, false otherwise
     */
    public void executeSQLQuery(String query, boolean executeAsBlock) {

        executeSQLQuery(null, query, executeAsBlock);
    }

    /**
     * Executes the query(ies) as specified on the provided database
     * connection properties object. The executeAsBlock flag
     * indicates that the query should be executed in its entirety -
     * not split up into mulitple queries (where applicable).
     *
     * @param the connection object
     * @param the query string
     * @param true to execute in entirety, false otherwise
     */
    public void executeSQLQuery(DatabaseConnection dc,
                                final String query,
                                final boolean executeAsBlock) {

        if (!ConnectionManager.hasConnections()) {

            setOutputMessage(SqlMessages.PLAIN_MESSAGE, "Not Connected");
            setStatusMessage(ERROR_EXECUTING);

            return;
        }

        if (querySender == null) {

            querySender = new DefaultStatementExecutor(null, true);
        }

        if (dc != null) {

            querySender.setDatabaseConnection(dc);
        }

        statementCancelled = false;

        worker = new ThreadWorker() {

            public Object construct() {

                return executeSQL(query, executeAsBlock);
            }

            public void finished() {

                delegate.finished(duration);

                if (statementCancelled) {

                    setOutputMessage(SqlMessages.PLAIN_MESSAGE,
                                     "Statement cancelled");
                    delegate.setStatusMessage(" Statement cancelled");
                }

                querySender.releaseResources();
                executing = false;
            }

        };

        setOutputMessage(SqlMessages.PLAIN_MESSAGE, "---\nUsing connection: " + dc);

        delegate.executing();
        delegate.setStatusMessage(Constants.EMPTY);
        worker.start();
    }

    /**
     * Interrupts the statement currently being executed.
     */
    public void interruptStatement() {

        ThreadUtils.startWorker(new Runnable() {

            public void run() {

                if (Log.isDebugEnabled()) {

                    Log.debug("QueryAnalyser: interruptStatement()");
                    Log.debug("Was currently executing " + executing);
                }

                if (!executing) {

                    return;
                }

                if (querySender != null) {

                    querySender.cancelCurrentStatement();
                }

                executing = false;
                statementCancelled = true;
            }

        });

    }

    public void pauseExecution() {

        if (isExecuting() && worker != null) {

            try {

                waiting = true;
                worker.wait();

            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }
    }

    public void resumeExecution() {

        if (isExecuting() && waiting && worker != null) {

            try {

                worker.notify();

            } finally {

                waiting = false;
            }

        }
    }


    /**
     * Returns whether a a query is currently being executed.
     *
     * @param true if in an execution is in progress, false otherwise
     */
    public boolean isExecuting() {

        return executing;
    }

    /**
     * Executes the query(ies) as specified. This method performs the
     * actual execution following query 'massaging'.The executeAsBlock
     * flag indicates that the query should be executed in its entirety -
     * not split up into mulitple queries (where applicable).
     *
     * @param the query string
     * @param true to execute in entirety, false otherwise
     */
    private Object executeSQL(String sql, boolean executeAsBlock) {

        waiting = false;
        long totalDuration = 0l;

        try {

            long start = 0l;
            long end = 0l;

            // check we are executing the whole block of sql text
            if (executeAsBlock) {

                // print the query
                logExecution(sql.trim());

                executing = true;

                start = System.currentTimeMillis();

                SqlStatementResult result = querySender.execute(sql, true);

                if (Thread.interrupted()) {

                    throw new InterruptedException();
                }

                if (result.isResultSet()) {

                    ResultSet rset = result.getResultSet();

                    if (rset == null) {

                        setOutputMessage(SqlMessages.ERROR_MESSAGE,
                                result.getErrorMessage());
                        setStatusMessage(ERROR_EXECUTING);

                    } else {

                        setResultSet(rset, sql);
                    }

                } else {

                    int updateCount = result.getUpdateCount();

                    if (updateCount == -1) {

                        setOutputMessage(SqlMessages.ERROR_MESSAGE,
                                result.getErrorMessage());
                        setStatusMessage(ERROR_EXECUTING);

                    } else {

                        setResult(updateCount, QueryTypes.UNKNOWN);
                    }

                }

                end = System.currentTimeMillis();
                statementExecuted(sql);

                long timeTaken = end - start;

                logExecutionTime(timeTaken);

                duration = formatDuration(totalDuration);

                return DONE;
            }

            executing = true;

            String procQuery = sql.toUpperCase();

            // check if its a procedure creation or execution
            if (isCreateProcedureOrFunction(procQuery)) {

                return executeProcedureOrFunction(sql, procQuery);
            }

            List<DerivedQuery> queries = queryTokenizer.tokenize(sql);
            boolean removeQueryComments = userProperties().getBooleanProperty("editor.execute.remove.comments");

            for (DerivedQuery query : queries) {

                if (!query.isExecutable()) {

                    setOutputMessage(
                            SqlMessages.WARNING_MESSAGE, "Non executable query provided");
                    continue;
                }

                // reset clock
                end = 0l;
                start = 0l;

                String derivedQueryString = query.getDerivedQuery();
                String queryToExecute = removeQueryComments ? derivedQueryString : query.getOriginalQuery();

                int type = query.getQueryType();
                if (type != QueryTypes.COMMIT && type != QueryTypes.ROLLBACK) {

                    logExecution(queryToExecute);

                } else {

                    if (type == QueryTypes.COMMIT) {

                        setOutputMessage(
                                SqlMessages.ACTION_MESSAGE,
                                COMMITTING_LAST);

                    } else if (type == QueryTypes.ROLLBACK) {

                        setOutputMessage(
                                SqlMessages.ACTION_MESSAGE,
                                ROLLING_BACK_LAST);
                    }

                }

                start = System.currentTimeMillis();
                SqlStatementResult result = querySender.execute(type, queryToExecute);

                if (statementCancelled || Thread.interrupted()) {

                    throw new InterruptedException();
                }

                if (result.isResultSet()) {

                    ResultSet rset = result.getResultSet();

                    if (rset == null) {

                        String message = result.getErrorMessage();
                        if (message == null) {

                            message = result.getMessage();
                            // if still null dump simple message
                            if (message == null) {

                                message = "A NULL result set was returned.";
                            }

                        }

                        setOutputMessage(SqlMessages.ERROR_MESSAGE,
                                         message);
                        setStatusMessage(ERROR_EXECUTING);

                    } else {

                        setResultSet(rset, query.getOriginalQuery());
                    }

                    end = System.currentTimeMillis();

                } else {

                    end  = System.currentTimeMillis();

                    // check that we executed a 'normal' statement (not a proc)
                    if (result.getType() != QueryTypes.EXECUTE) {

                        int updateCount = result.getUpdateCount();
                        if (updateCount == -1) {
                            
                            setOutputMessage(SqlMessages.ERROR_MESSAGE,
                                    result.getErrorMessage());
                            setStatusMessage(ERROR_EXECUTING);

                        } else {
                        
                            type = result.getType();
                            setResultText(updateCount, type);

                            if (type == QueryTypes.COMMIT || type == QueryTypes.ROLLBACK) {
                            
                                setStatusMessage(" " + result.getMessage());
                            }

                        }

                    } else {

                        @SuppressWarnings("rawtypes")
                        Map results = (Map)result.getOtherResult();

                        if (results == null) {

                            setOutputMessage(SqlMessages.ERROR_MESSAGE,result.getErrorMessage());
                            setStatusMessage(ERROR_EXECUTING);

                        } else {

                            setOutputMessage(SqlMessages.PLAIN_MESSAGE, "Call executed successfully.");
                            int updateCount = result.getUpdateCount();

                            if (updateCount > 0) {

                                setOutputMessage(SqlMessages.PLAIN_MESSAGE,
                                        updateCount +
                                        ((updateCount > 1) ?
                                            " rows affected." : " row affected."));
                            }

                            String SPACE = " = ";
                            for (Iterator<?> i = results.keySet().iterator(); i.hasNext();) {

                                String key = i.next().toString();
                                setOutputMessage(SqlMessages.PLAIN_MESSAGE,
                                                 key + SPACE + results.get(key));
                            }

                        }

                    }

                }

                // execution times
                if (end == 0) {

                    end = System.currentTimeMillis();
                }

                long timeTaken = end - start;
                totalDuration += timeTaken;
                logExecutionTime(timeTaken);

            }

            statementExecuted(sql);

        } catch (SQLException e) {

            processException(e);
            return "SQLException";

        } catch (InterruptedException e) {

            //Log.debug("InterruptedException");
            statementCancelled = true; // make sure its set
            return "Interrupted";

        } catch (OutOfMemoryError e) {

            setOutputMessage(SqlMessages.ERROR_MESSAGE,
                    "Resources exhausted while executing query.\n"+
                    "The query result set was too large to return.");

            setStatusMessage(ERROR_EXECUTING);

        } catch (Exception e) {

            if (!statementCancelled) {

                if (Log.isDebugEnabled()) {

                    e.printStackTrace();
                }

                processException(e);
            }

        } finally {

            /*
            if (endAll == 0) {
                endAll = System.currentTimeMillis();
            }
            duration = MiscUtils.formatDuration(endAll - startAll);
            */

            duration = formatDuration(totalDuration);
        }

        return DONE;
    }

    private String formatDuration(long totalDuration) {

        return MiscUtils.formatDuration(totalDuration);
    }

    private void setResult(int updateCount, int type) {

        delegate.setResult(updateCount, type);
    }

    private void statementExecuted(String sql) {

        delegate.statementExecuted(sql);
    }

    private Object executeProcedureOrFunction(String sql, String procQuery)
        throws SQLException {

        logExecution(sql.trim());

        long start = System.currentTimeMillis();

        SqlStatementResult result = querySender.createProcedure(sql);

        if (result.getUpdateCount() == -1) {

            setOutputMessage(SqlMessages.ERROR_MESSAGE, result.getErrorMessage());
            setStatusMessage(ERROR_EXECUTING);

        } else {

            if (isCreateProcedure(procQuery)) {

                setResultText(result.getUpdateCount(), QueryTypes.CREATE_PROCEDURE);

            } else if (isCreateFunction(procQuery)) {

                setResultText(result.getUpdateCount(), QueryTypes.CREATE_FUNCTION);
            }

        }

        long end = System.currentTimeMillis();

        outputWarnings(result.getSqlWarning());

        logExecutionTime(start, end);

        statementExecuted(sql);

        return DONE;
    }

    /**
     * Logs the execution duration within the output
     * pane for the specified start and end values.
     *
     * @param start the start time in millis
     * @param end the end time in millis
     */
    private void logExecutionTime(long start, long end) {

        logExecutionTime(end - start);
    }

    /**
     * Logs the execution duration within the output
     * pane for the specified value.
     *
     * @param start the time in millis
     */
    private void logExecutionTime(long time) {
        setOutputMessage(SqlMessages.PLAIN_MESSAGE,
                "Execution time: " + formatDuration(time), false);
    }

    /**
     * Logs the specified query being executed.
     *
     * @param query - the executed query
     */
    private void logExecution(String query) {

        Log.info(EXECUTING + query);

        if (verboseLogging) {

            setOutputMessage(
                    SqlMessages.ACTION_MESSAGE, EXECUTING);
            setOutputMessage(
                    SqlMessages.ACTION_MESSAGE_PREFORMAT, query);

        } else {

            int queryLength = query.length();
            int subIndex = queryLength < 50 ? (queryLength + 1) : 50;

            setOutputMessage(
                    SqlMessages.ACTION_MESSAGE, EXECUTING);
            setOutputMessage(
                    SqlMessages.ACTION_MESSAGE_PREFORMAT,
                    query.substring(0, subIndex-1).trim() + SUBSTRING);
        }

    }

    private void processException(Throwable e) {

        if (e != null) {
            setOutputMessage(SqlMessages.ERROR_MESSAGE, e.getMessage());

            if (e instanceof SQLException) {

                SQLException sqlExc = (SQLException)e;
                sqlExc = sqlExc.getNextException();

                if (sqlExc != null) {

                    setOutputMessage(SqlMessages.ERROR_MESSAGE, sqlExc.getMessage());
                }

            } else {

                setStatusMessage(ERROR_EXECUTING);
            }
        }

    }

    private void setResultText(final int result, final int type) {
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                delegate.setResult(result, type);
            }
        });
    }

    private void setStatusMessage(final String text) {
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                delegate.setStatusMessage(text);
            }
        });
    }

    private void setOutputMessage(final int type, final String text) {

        setOutputMessage(type, text, true);
    }

    private void setOutputMessage(final int type, final String text, final boolean selectTab) {
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                delegate.setOutputMessage(type, text, selectTab);
                if (text != null) {
                    logOutput(text);
                }
            }
        });
    }

    private void setResultSet(final ResultSet rs, final String query) {
/*
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    delegate.setResultSet(rs, query);
                } catch (SQLException e) {
                    processException(e);
                }
            }
        });
*/
        try {
            delegate.setResultSet(rs, query);
        } catch (SQLException e) {
            processException(e);
        }

    }

    /** matcher to remove new lines from log messages */
    private Matcher newLineMatcher;

    /**
     * Logs the specified text to the logger.
     *
     * @param text - the text to log
     */
    private void logOutput(String text) {

        if (delegate.isLogEnabled()) {

            newLineMatcher.reset(text);
            delegate.log(newLineMatcher.replaceAll(" "));
        }

    }

    /**
     * Formats and prints to the output pane the specified warning.
     *
     * @param warning - the warning to be printed
     */
    private void outputWarnings(SQLWarning warning) {

        if (warning == null) {
            return;
        }

        String dash = " - ";
        // print the first warning
        setOutputMessage(SqlMessages.WARNING_MESSAGE,
                warning.getErrorCode() + dash + warning.getMessage());

        // retrieve subsequent warnings
        SQLWarning _warning = null;

        int errorCode = -1000;
        int _errorCode = warning.getErrorCode();

        while ((_warning = warning.getNextWarning()) != null) {
            errorCode = _warning.getErrorCode();

            if (errorCode == _errorCode) {
                return;
            }

            _errorCode = errorCode;
            setOutputMessage(SqlMessages.WARNING_MESSAGE,
                    _errorCode + dash + _warning.getMessage());
            warning = _warning;
        }

    }

    /** Closes the current connection. */
    public void destroyConnection() {
        if (querySender != null) {
            try {
                querySender.destroyConnection();
            } catch (SQLException e) {}
        }
    }

    /**
     * Dtermines whether the specified query is attempting
     * to create a SQL PROCEDURE or FUNCTION.
     *
     * @param query - the query to be executed
     * @return true | false
     */
    private boolean isCreateProcedureOrFunction(String query) {

        String noCommentsQuery = queryTokenizer.removeComments(query);
        if (isNotSingleStatementExecution(noCommentsQuery)) {

            return isCreateProcedure(noCommentsQuery) || isCreateFunction(noCommentsQuery);
        }

        return false;
    }

    /**
     * Dtermines whether the specified query is attempting
     * to create a SQL PROCEDURE.
     *
     * @param query - the query to be executed
     * @return true | false
     */
    private boolean isCreateProcedure(String query) {

        int createIndex = query.indexOf("CREATE");
        int tableIndex = query.indexOf("TABLE");
        int procedureIndex = query.indexOf("PROCEDURE");
        int packageIndex = query.indexOf("PACKAGE");

        return (createIndex != -1) && (tableIndex == -1) &&
                (procedureIndex > createIndex || packageIndex > createIndex);
    }

    /**
     * Determines whether the specified query is attempting
     * to create a SQL FUNCTION.
     *
     * @param query - the query to be executed
     * @return true | false
     */
    private boolean isCreateFunction(String query) {
        int createIndex = query.indexOf("CREATE");
        int tableIndex = query.indexOf("TABLE");
        int functionIndex = query.indexOf("FUNCTION");
        return createIndex != -1 &&
               tableIndex == -1 &&
               functionIndex > createIndex;
    }

    private boolean isNotSingleStatementExecution(String query) {

        DerivedQuery derivedQuery = new DerivedQuery(query);
        int type = derivedQuery.getQueryType();

        int[] nonSingleStatementExecutionTypes = {
                QueryTypes.CREATE_FUNCTION,
                QueryTypes.CREATE_PROCEDURE,
                QueryTypes.UNKNOWN,
                QueryTypes.EXECUTE
        };

        for (int i = 0; i < nonSingleStatementExecutionTypes.length; i++) {

            if (type == nonSingleStatementExecutionTypes[i]) {

                return true;
            }

        }

        return false;
    }

}










