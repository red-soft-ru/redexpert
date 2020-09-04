/*
 * QueryDispatcher.java
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

package org.executequery.sql;

import biz.redsoft.IFBDatabasePerformance;
import biz.redsoft.IFBPerformanceInfo;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.datasource.PooledResultSet;
import org.executequery.datasource.PooledStatement;
import org.executequery.gui.editor.InputParametersDialog;
import org.executequery.gui.editor.QueryEditorHistory;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.log.Log;
import org.executequery.util.ThreadUtils;
import org.executequery.util.ThreadWorker;
import org.executequery.util.UserProperties;
import org.underworldlabs.sqlParser.REDDATABASESqlBaseListener;
import org.underworldlabs.sqlParser.REDDATABASESqlLexer;
import org.underworldlabs.sqlParser.REDDATABASESqlParser;
import org.underworldlabs.sqlParser.SqlParser;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Determines the type of exeuted query and returns appropriate results.
 *
 * @author Takis Diakoumis
 */
public class QueryDispatcher {

    /**
     * the parent controller
     */
    private QueryDelegate delegate;

    /**
     * thread worker object
     */
    private ThreadWorker worker;

    /**
     * the query sender database mediator
     */
    private StatementExecutor querySender;

    /**
     * indicates verbose logging output
     */
    private boolean verboseLogging;

    /**
     * Indicates that an execute is in progress
     */
    private boolean executing;

    /**
     * connection commit mode
     */
    private boolean autoCommit;

    /**
     * The query execute duration time
     */
    private String duration;

    /**
     * indicates that the current execution has been cancelled
     */
    private boolean statementCancelled;

    private QueryTokenizer queryTokenizer;

    private boolean waiting;

    /**
     * Isolation level for query transaction
     */
    private int transactionLevel;

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
        try {
            this.delegate = runner;

            transactionLevel = -1;

            querySender = new DefaultStatementExecutor(null, true);

            setAutoCommit(userProperties().getBooleanProperty("editor.connection.commit"));

            initialiseLogging();

            queryTokenizer = new QueryTokenizer();
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
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
     * @param autoCommit commit mode
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
        } catch (SQLException sqlExc) {
        }
    }

    /**
     * Indicates a connection has been closed.
     * Propagates the call to the query sender object.
     *
     * @param dc connection thats been closed
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
     * @param query          query string
     * @param executeAsBlock to execute in entirety, false otherwise
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
     * @param dc             connection object
     * @param query          query string
     * @param executeAsBlock to execute in entirety, false otherwise
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

        querySender.setTransactionIsolation(transactionLevel);

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

    public void executeSQLScript(DatabaseConnection dc,
                                 final String script) {

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

        querySender.setTransactionIsolation(transactionLevel);

        statementCancelled = false;

        worker = new ThreadWorker() {

            public Object construct() {

                return executeSQLScript(script);
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

    public void printExecutedPlan(DatabaseConnection dc,
                                  final String query) {

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

        querySender.setTransactionIsolation(transactionLevel);

        try {
            Statement statement = querySender.getPreparedStatement(query);
            printPlan(statement);
        } catch (SQLException e) {
            setOutputMessage(SqlMessages.ERROR_MESSAGE, e.getMessage());
        } finally {
            querySender.releaseResources();
        }
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
     * @param sql            query string
     * @param executeAsBlock to execute in entirety, false otherwise
     */
    private Object executeSQL(String sql, boolean executeAsBlock) {

        IFBPerformanceInfo before, after;
        before = null;
        after = null;

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
                REDDATABASESqlLexer lexer = new REDDATABASESqlLexer(CharStreams.fromString(sql));
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                REDDATABASESqlParser sqlparser = new REDDATABASESqlParser(tokens);
                List<? extends ANTLRErrorListener> listeners = sqlparser.getErrorListeners();
                for (int i = 0; i < listeners.size(); i++) {
                    if (listeners.get(i) instanceof ConsoleErrorListener)
                        sqlparser.removeErrorListener(listeners.get(i));
                }
                ParseTree tree = sqlparser.execute_block_stmt();
                ParseTreeWalker walker = new ParseTreeWalker();
                StringBuilder variables = new StringBuilder();
                walker.walk(new REDDATABASESqlBaseListener() {
                    @Override
                    public void enterDeclare_block(REDDATABASESqlParser.Declare_blockContext ctx) {
                        List<REDDATABASESqlParser.Input_parameterContext> in_pars = ctx.input_parameter();
                        for (int i = 0; i < in_pars.size(); i++) {
                            variables.append("<").append(in_pars.get(i).desciption_parameter().parameter_name().getRuleContext().getText()).append(">");
                        }
                        List<REDDATABASESqlParser.Output_parameterContext> out_pars = ctx.output_parameter();
                        for (int i = 0; i < out_pars.size(); i++) {
                            variables.append("<").append(out_pars.get(i).desciption_parameter().parameter_name().getRuleContext().getText()).append(">");
                        }
                        List<REDDATABASESqlParser.Local_variableContext> vars = ctx.local_variable();
                        for (int i = 0; i < vars.size(); i++) {
                            variables.append("<").append(vars.get(i).variable_name().getRuleContext().getText()).append(">");
                        }
                    }
                }, tree);
                PreparedStatement statement = prepareStatementWithParameters(sql, variables.toString());
                SqlStatementResult result = querySender.execute(statement, true);
                //SqlStatementResult result = querySender.execute(sql, true);

                if (Thread.interrupted()) {

                    throw new InterruptedException();
                }

                if (result.isResultSet()) {

                    ResultSet rset = result.getResultSet();

                    if (rset == null) {

                        setOutputMessage(SqlMessages.ERROR_MESSAGE,
                                result.getErrorMessage(), true);
                        setStatusMessage(ERROR_EXECUTING);

                    } else {

                        setResultSet(rset, sql);
                    }

                } else {

                    int updateCount = result.getUpdateCount();

                    if (updateCount == -1) {

                        setOutputMessage(SqlMessages.ERROR_MESSAGE,
                                result.getErrorMessage(), true);
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

                try {
                    DatabaseConnection databaseConnection = this.querySender.getDatabaseConnection();
                    Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
                    DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
                    Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());

                    if (driver.getClass().getName().contains("FBDriver")) {

                        Connection connection = null;
                        try {
                            connection = querySender.getConnection().unwrap(Connection.class);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        IFBDatabasePerformance db = (IFBDatabasePerformance) DynamicLibraryLoader.loadingObjectFromClassLoader(connection, "FBDatabasePerformanceImpl");
                        try {

                            db.setConnection(connection);
                            before = db.getPerformanceInfo();

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (Exception e) {
                    // nothing to do
                }

                start = System.currentTimeMillis();
                PreparedStatement statement = null;
                CallableStatement callableStatement = null;
                SqlStatementResult result;
                if (queryToExecute.toLowerCase().trim().contentEquals("commit") || queryToExecute.toLowerCase().trim().contentEquals("rollback"))
                    statement = querySender.getPreparedStatement(queryToExecute);
                else {
                    if (query.getQueryType() != QueryTypes.CALL) {
                        statement = prepareStatementWithParameters(queryToExecute, "");
                    } else {
                        callableStatement = prepareCallableStatementWithParameters(queryToExecute, "");
                    }
                }
                if (statement != null)
                    result = querySender.execute(type, statement);
                else
                    result = querySender.execute(type, callableStatement);

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

                        printExecutionPlan(before, after);

                        setOutputMessage(SqlMessages.ERROR_MESSAGE,
                                message, true);
                        setStatusMessage(ERROR_EXECUTING);

                    } else {

                        // Trying to get execution plan of firebird statement

                        printPlan(rset);

                        setResultSet(rset, query.getOriginalQuery());

                        printExecutionPlan(before, after);
                    }

                    end = System.currentTimeMillis();

                } else {

                    end = System.currentTimeMillis();

                    // check that we executed a 'normal' statement (not a proc)
                    if (result.getType() != QueryTypes.EXECUTE) {

                        int updateCount = result.getUpdateCount();
                        if (updateCount == -1) {

                            printExecutionPlan(before, after);

                            setOutputMessage(SqlMessages.ERROR_MESSAGE,
                                    result.getErrorMessage(), true);
                            setStatusMessage(ERROR_EXECUTING);

                        } else {

                            if (result.isException()) {

                                printExecutionPlan(before, after);

                                setOutputMessage(SqlMessages.ERROR_MESSAGE, result.getErrorMessage(), true);
                            } else {

                                type = result.getType();
                                setResultText(updateCount, type);


                                if (type == QueryTypes.COMMIT || type == QueryTypes.ROLLBACK) {

                                    setStatusMessage(" " + result.getMessage());
                                }

                                printExecutionPlan(before, after);

                            }
                        }

                    } else {

                        @SuppressWarnings("rawtypes")
                        Map results = (Map) result.getOtherResult();

                        if (results == null) {

                            printExecutionPlan(before, after);

                            setOutputMessage(SqlMessages.ERROR_MESSAGE, result.getErrorMessage(), true);
                            setStatusMessage(ERROR_EXECUTING);

                        } else {

                            printExecutionPlan(before, after);

                            setOutputMessage(SqlMessages.PLAIN_MESSAGE, "Call executed successfully.");
                            int updateCount = result.getUpdateCount();

                            if (updateCount > 0) {

                                setOutputMessage(SqlMessages.PLAIN_MESSAGE,
                                        updateCount +
                                                ((updateCount > 1) ?
                                                        " rows affected." : " row affected."));
                            }

                            String SPACE = " = ";
                            for (Iterator<?> i = results.keySet().iterator(); i.hasNext(); ) {

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
                    "Resources exhausted while executing query.\n" +
                            "The query result set was too large to return.", true);

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

    private Object executeSQLScript(String script) {

        IFBPerformanceInfo before, after;
        before = null;
        after = null;

        waiting = false;
        long totalDuration = 0l;

        try {

            long start = 0l;
            long end = 0l;

            // check we are executing the whole block of sql text


            executing = true;


            QueryTokenizer queryTokenizer = new QueryTokenizer();
            List<DerivedQuery> queries = queryTokenizer.tokenize(script);

            List<DerivedQuery> executableQueries = new ArrayList<DerivedQuery>();

            for (DerivedQuery query : queries) {
                if (statementCancelled || Thread.interrupted()) {

                    throw new InterruptedException();
                }
                if (query.isExecutable()) {

                    executableQueries.add(query);
                }

            }
            queries.clear();

            try {
                DatabaseConnection databaseConnection = this.querySender.getDatabaseConnection();
                Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
                DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
                Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());

                if (driver.getClass().getName().contains("FBDriver")) {

                    Connection connection = null;
                    try {
                        connection = querySender.getConnection().unwrap(Connection.class);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    IFBDatabasePerformance db = (IFBDatabasePerformance) DynamicLibraryLoader.loadingObjectFromClassLoader(connection, "FBDatabasePerformanceImpl");
                    try {

                        db.setConnection(connection);
                        before = db.getPerformanceInfo();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                // nothing to do
            }
            setOutputMessage(
                    SqlMessages.ACTION_MESSAGE, "Found " + executableQueries.size() + " queries");
            start = System.currentTimeMillis();
            boolean stopOnError = SystemProperties.getBooleanProperty("user", "editor.stop.on.error");
            boolean error = false;
            for (int i = 0; i < executableQueries.size(); i++) {
                try {
                    DerivedQuery query = executableQueries.get(i);
                    setOutputMessage(
                            SqlMessages.ACTION_MESSAGE, (i + 1) + " query");
                    if (statementCancelled || Thread.interrupted()) {

                        throw new InterruptedException();
                    }

                    String queryToExecute = query.getDerivedQuery();

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


                    PreparedStatement statement;
                    if (query.getQueryType() == QueryTypes.SET_AUTODDL_ON || query.getQueryType() == QueryTypes.SET_AUTODDL_OFF)
                        statement = null;
                    else statement = querySender.getPreparedStatement(queryToExecute);
                    SqlStatementResult result = querySender.execute(type, statement);

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

                            printExecutionPlan(before, after);

                            setOutputMessage(SqlMessages.ERROR_MESSAGE,
                                    message, true);
                            setStatusMessage(ERROR_EXECUTING);
                            error = true;

                        } else {

                            // Trying to get execution plan of firebird statement

                            printPlan(rset);

                            setResultSet(rset, query.getOriginalQuery());

                            printExecutionPlan(before, after);
                        }

                        end = System.currentTimeMillis();

                    } else {

                        end = System.currentTimeMillis();

                        // check that we executed a 'normal' statement (not a proc)
                        if (result.getType() != QueryTypes.EXECUTE) {

                            int updateCount = result.getUpdateCount();
                            if (updateCount == -1) {

                                //printExecutionPlan(before, after);

                                setOutputMessage(SqlMessages.ERROR_MESSAGE,
                                        result.getErrorMessage(), true);
                                setStatusMessage(ERROR_EXECUTING);
                                error = true;

                            } else {

                                if (result.isException()) {

                                    //printExecutionPlan(before, after);

                                    setOutputMessage(SqlMessages.ERROR_MESSAGE, result.getErrorMessage(), true);
                                    error = true;
                                } else {

                                    type = result.getType();
                                    setResultText(updateCount, type);


                                    if (type == QueryTypes.COMMIT || type == QueryTypes.ROLLBACK) {

                                        setStatusMessage(" " + result.getMessage());
                                    }

                                    printExecutionPlan(before, after);

                                }
                            }

                        } else {

                            @SuppressWarnings("rawtypes")
                            Map results = (Map) result.getOtherResult();

                            if (results == null) {

                                //printExecutionPlan(before, after);

                                setOutputMessage(SqlMessages.ERROR_MESSAGE, result.getErrorMessage(), true);
                                setStatusMessage(ERROR_EXECUTING);
                                error = true;

                            } else {

                                printExecutionPlan(before, after);

                                setOutputMessage(SqlMessages.PLAIN_MESSAGE, "Call executed successfully.");
                                int updateCount = result.getUpdateCount();

                                if (updateCount > 0) {

                                    setOutputMessage(SqlMessages.PLAIN_MESSAGE,
                                            updateCount +
                                                    ((updateCount > 1) ?
                                                            " rows affected." : " row affected."));
                                }

                                String SPACE = " = ";
                                for (Iterator<?> it = results.keySet().iterator(); it.hasNext(); ) {

                                    String key = it.next().toString();
                                    setOutputMessage(SqlMessages.PLAIN_MESSAGE,
                                            key + SPACE + results.get(key));
                                }

                            }

                        }

                    }

                    // execution times


                } catch (SQLException e) {

                    processException(e);
                    return "SQLException";

                } catch (InterruptedException e) {

                    //Log.debug("InterruptedException");
                    statementCancelled = true; // make sure its set
                    return "Interrupted";

                } catch (OutOfMemoryError e) {

                    setOutputMessage(SqlMessages.ERROR_MESSAGE,
                            "Resources exhausted while executing query.\n" +
                                    "The query result set was too large to return.", true);

                    setStatusMessage(ERROR_EXECUTING);

                } catch (Exception e) {

                    if (!statementCancelled) {


                        e.printStackTrace();


                        processException(e);
                    }

                } finally {

                    querySender.releaseResources();
                    if (error && stopOnError)
                        break;
                }

            }
            if (end == 0) {

                end = System.currentTimeMillis();
            }

            long timeTaken = end - start;
            totalDuration += timeTaken;
            logExecutionTime(timeTaken);

            statementExecuted(script);

        } catch (InterruptedException e) {

            //Log.debug("InterruptedException");
            statementCancelled = true; // make sure its set
            return "Interrupted";

        } catch (OutOfMemoryError e) {

            setOutputMessage(SqlMessages.ERROR_MESSAGE,
                    "Resources exhausted while executing query.\n" +
                            "The query result set was too large to return.", true);

            setStatusMessage(ERROR_EXECUTING);

        } catch (Exception e) {

            if (!statementCancelled) {

                if (Log.isDebugEnabled()) {

                    e.printStackTrace();
                }

                processException(e);
            }

        } finally {

            duration = formatDuration(totalDuration);
        }

        return DONE;
    }

    PreparedStatement prepareStatementWithParameters(String sql, String variables) throws SQLException {
        SqlParser parser = new SqlParser(sql, variables);
        String queryToExecute = parser.getProcessedSql();
        PreparedStatement statement = querySender.getPreparedStatement(queryToExecute);
        statement.setEscapeProcessing(true);
        ParameterMetaData pmd = statement.getParameterMetaData();
        List<Parameter> params = parser.getParameters();
        List<Parameter> displayParams = parser.getDisplayParameters();
            for (int i = 0; i < params.size(); i++) {
                params.get(i).setType(pmd.getParameterType(i + 1));
                params.get(i).setTypeName(pmd.getParameterTypeName(i + 1));
            }
        if (QueryEditorHistory.getHistoryParameters().containsKey(querySender.getDatabaseConnection())) {
            List<Parameter> oldParams = QueryEditorHistory.getHistoryParameters().get(querySender.getDatabaseConnection());
            for (int i = 0; i < displayParams.size(); i++) {
                Parameter dp = displayParams.get(i);
                for (int g = 0; g < oldParams.size(); g++) {
                    Parameter p = oldParams.get(g);
                    if (p.getType() == dp.getType() && p.getName().contentEquals(dp.getName())) {
                        dp.setValue(p.getValue());
                        oldParams.remove(p);
                        break;
                    }
                }
            }
        }
        if (!displayParams.isEmpty()) {
            InputParametersDialog spd = new InputParametersDialog(displayParams);
            spd.display();
        }
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).isNull())
                statement.setNull(i + 1, params.get(i).getType());
            else
                statement.setObject(i + 1, params.get(i).getPreparedValue());
        }
        QueryEditorHistory.getHistoryParameters().put(querySender.getDatabaseConnection(), displayParams);
        return statement;
    }

    private CallableStatement prepareCallableStatementWithParameters(String sql, String parameters) throws SQLException {
        SqlParser parser = new SqlParser(sql, parameters);
        String queryToExecute = parser.getProcessedSql();
        CallableStatement statement = querySender.getCallableStatement(queryToExecute);
        statement.setEscapeProcessing(true);
        ParameterMetaData pmd = statement.getParameterMetaData();
        List<Parameter> params = parser.getParameters();
        List<Parameter> displayParams = parser.getDisplayParameters();
        for (int i = 0; i < params.size(); i++) {
            params.get(i).setType(pmd.getParameterType(i + 1));
            params.get(i).setTypeName(pmd.getParameterTypeName(i + 1));
        }
        if (QueryEditorHistory.getHistoryParameters().containsKey(querySender.getDatabaseConnection())) {
            List<Parameter> oldParams = QueryEditorHistory.getHistoryParameters().get(querySender.getDatabaseConnection());
            for (int i = 0; i < displayParams.size(); i++) {
                Parameter dp = displayParams.get(i);
                for (int g = 0; g < oldParams.size(); g++) {
                    Parameter p = oldParams.get(g);
                    if (p.getType() == dp.getType() && p.getName().contentEquals(dp.getName())) {
                        dp.setValue(p.getValue());
                        oldParams.remove(p);
                        break;
                    }
                }
            }
        }
        if (!displayParams.isEmpty()) {
            InputParametersDialog spd = new InputParametersDialog(displayParams);
            spd.display();
        }
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).isNull())
                statement.setNull(i + 1, params.get(i).getType());
            else
                statement.setObject(i + 1, params.get(i).getPreparedValue());
        }
        QueryEditorHistory.getHistoryParameters().put(querySender.getDatabaseConnection(), displayParams);
        return statement;
    }

    private void printExecutionPlan(IFBPerformanceInfo before, IFBPerformanceInfo after) {
        // Trying to get execution plan of firebird statement
        DatabaseConnection databaseConnection = this.querySender.getDatabaseConnection();
        DefaultDriverLoader driverLoader = new DefaultDriverLoader();
        Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
        DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
        Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());

        if (driver.getClass().getName().contains("FBDriver")) {

            Connection connection = null;
            try {
                connection = querySender.getConnection().unwrap(Connection.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            URL[] urls = new URL[0];
            Class clazzdb = null;
            Object odb = null;
            try {
                urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
                ClassLoader cl = new URLClassLoader(urls, connection.getClass().getClassLoader());
                clazzdb = cl.loadClass("biz.redsoft.FBDatabasePerformanceImpl");
                odb = clazzdb.newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            IFBDatabasePerformance db = (IFBDatabasePerformance) odb;
            try {

                db.setConnection(connection);
                after = db.getPerformanceInfo();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (before != null && after != null) {
                IFBPerformanceInfo resultPerfomanceInfo = after.processInfo(before, after);

                setOutputMessage(SqlMessages.PLAIN_MESSAGE, resultPerfomanceInfo.getPerformanceInfo());
            }
        }

    }

    private void printPlan(ResultSet rs) {
        try {
            DatabaseConnection databaseConnection = this.querySender.getDatabaseConnection();
            DefaultDriverLoader driverLoader = new DefaultDriverLoader();
            Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
            DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
            Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());

            if (driver.getClass().getName().contains("FBDriver")) {
                ResultSet realRS = ((PooledResultSet)rs).getResultSet();

                ResultSet resultSet = null;
                try {
                    resultSet = realRS.unwrap(ResultSet.class);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                URL[] urls = new URL[0];
                Class clazzdb = null;
                Object odb = null;
                try {
                    urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
                    ClassLoader cl = new URLClassLoader(urls, resultSet.getClass().getClassLoader());
                    clazzdb = cl.loadClass("biz.redsoft.FBDatabasePerformanceImpl");
                    odb = clazzdb.newInstance();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                IFBDatabasePerformance db = (IFBDatabasePerformance) odb;
                try {

                    setOutputMessage(SqlMessages.PLAIN_MESSAGE, db.getLastExecutedPlan(resultSet));

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printPlan(Statement st) {
        try {
            DatabaseConnection databaseConnection = this.querySender.getDatabaseConnection();
            DefaultDriverLoader driverLoader = new DefaultDriverLoader();
            Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
            DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
            Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());

            if (driver.getClass().getName().contains("FBDriver")) {
                Statement realST = ((PooledStatement) st).getStatement();

                Statement statement = null;
                try {
                    statement = realST.unwrap(Statement.class);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                URL[] urls = new URL[0];
                Class clazzdb = null;
                Object odb = null;
                try {
                    urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
                    ClassLoader cl = new URLClassLoader(urls, statement.getClass().getClassLoader());
                    clazzdb = cl.loadClass("biz.redsoft.FBDatabasePerformanceImpl");
                    odb = clazzdb.newInstance();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                IFBDatabasePerformance db = (IFBDatabasePerformance) odb;
                try {

                    setOutputMessage(SqlMessages.PLAIN_MESSAGE, db.getLastExecutedPlan(statement), true);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        REDDATABASESqlLexer lexer = new REDDATABASESqlLexer(CharStreams.fromString(sql));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        REDDATABASESqlParser sqlparser = new REDDATABASESqlParser(tokens);
        List<? extends ANTLRErrorListener> listeners = sqlparser.getErrorListeners();
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i) instanceof ConsoleErrorListener)
                sqlparser.removeErrorListener(listeners.get(i));
        }
        ParseTree tree = sqlparser.create_or_alter_procedure_stmt();
        ParseTreeWalker walker = new ParseTreeWalker();
        StringBuilder variables = new StringBuilder();
        walker.walk(new REDDATABASESqlBaseListener() {
            @Override
            public void enterDeclare_block(REDDATABASESqlParser.Declare_blockContext ctx) {
                List<REDDATABASESqlParser.Input_parameterContext> in_pars = ctx.input_parameter();
                for (int i = 0; i < in_pars.size(); i++) {
                    variables.append("<").append(in_pars.get(i).desciption_parameter().parameter_name().getRuleContext().getText()).append(">");
                }
                List<REDDATABASESqlParser.Output_parameterContext> out_pars = ctx.output_parameter();
                for (int i = 0; i < out_pars.size(); i++) {
                    variables.append("<").append(out_pars.get(i).desciption_parameter().parameter_name().getRuleContext().getText()).append(">");
                }
                List<REDDATABASESqlParser.Local_variableContext> vars = ctx.local_variable();
                for (int i = 0; i < vars.size(); i++) {
                    variables.append("<").append(vars.get(i).variable_name().getRuleContext().getText()).append(">");
                }
            }
        }, tree);
        PreparedStatement statement = prepareStatementWithParameters(sql, variables.toString());
        SqlStatementResult result = querySender.execute(QueryTypes.CREATE_PROCEDURE, statement);

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
     * @param end   the end time in millis
     */
    private void logExecutionTime(long start, long end) {

        logExecutionTime(end - start);
    }

    /**
     * Logs the execution duration within the output
     * pane for the specified value.
     *
     * @param time the time in millis
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
                    query.substring(0, subIndex - 1).trim() + SUBSTRING);
        }

    }

    private void processException(Throwable e) {

        if (e != null) {
            setOutputMessage(SqlMessages.ERROR_MESSAGE, e.getMessage());

            if (e instanceof SQLException) {

                SQLException sqlExc = (SQLException) e;
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

        setOutputMessage(type, text, false);
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

    /**
     * matcher to remove new lines from log messages
     */
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

    /**
     * Closes the current connection.
     */
    public void destroyConnection() {
        if (querySender != null) {
            try {
                querySender.destroyConnection();
            } catch (SQLException e) {
            }
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
                (procedureIndex > createIndex || packageIndex > createIndex) || (createIndex != -1) &&
                (tableIndex != -1) && (procedureIndex > createIndex && tableIndex > procedureIndex || packageIndex > createIndex && tableIndex > packageIndex);
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
                functionIndex > createIndex || createIndex != -1 &&
                tableIndex != -1 &&
                functionIndex > createIndex && functionIndex < tableIndex;
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

    public int getTransactionIsolation() {
        return transactionLevel;
    }

    public void setTransactionIsolation(int transactionLevel) {
        this.transactionLevel = transactionLevel;
    }
}











