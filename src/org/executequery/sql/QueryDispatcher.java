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
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.datasource.PooledResultSet;
import org.executequery.datasource.PooledStatement;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.browser.profiler.DefaultProfilerExecutor;
import org.executequery.gui.browser.profiler.ProfilerPanel;
import org.executequery.gui.editor.InputParametersDialog;
import org.executequery.gui.editor.QueryEditorHistory;
import org.executequery.gui.editor.TransactionParametersPanel;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.util.ThreadUtils;
import org.executequery.util.ThreadWorker;
import org.executequery.util.UserProperties;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.sqlParser.REDDATABASESqlBaseListener;
import org.underworldlabs.sqlParser.REDDATABASESqlLexer;
import org.underworldlabs.sqlParser.REDDATABASESqlParser;
import org.underworldlabs.sqlParser.SqlParser;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.sql.*;
import java.util.*;
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
    private DefaultStatementExecutor querySender;

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

    private TransactionParametersPanel tpp;

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
            sqlExc.printStackTrace();
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
    public void executeSQLQuery(String query, boolean executeAsBlock, boolean anyConnections) {

        executeSQLQuery(null, query, executeAsBlock, anyConnections);
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
    public void executeSQLQuery(
            DatabaseConnection dc, final String query, final boolean executeAsBlock, boolean anyConnections) {

        if (!checkBeforeExecuteQuery(query, dc, anyConnections))
            return;

        if (querySender == null) {

            querySender = new DefaultStatementExecutor(null, true);
        }

        if (dc != null) {

            querySender.setDatabaseConnection(dc);
        }

        querySender.setTpb(tpp.getTpb(dc));

        statementCancelled = false;

        worker = new ThreadWorker("ExecutingQueryInQueryDispatcher") {

            public Object construct() {

                return executeSQL(query, executeAsBlock, anyConnections);
            }

            public void finished() {

                delegate.finished(duration);

                if (statementCancelled) {

                    setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE,
                            "Statement cancelled", anyConnections);
                    delegate.setStatusMessage(" Statement cancelled");
                }
                querySender.setCloseConnectionAfterQuery(false);
                querySender.releaseResourcesWithoutCommit();
                executing = false;
            }

        };

        setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, "---\nUsing connection: " + dc, anyConnections);

        delegate.executing();
        delegate.setStatusMessage(Constants.EMPTY);
        worker.start();
    }

    public void executeSQLQueryInProfiler(
            DatabaseConnection dc, final String query, final boolean executeAsBlock) {

        if (!checkBeforeExecuteQuery(query, dc, false))
            return;

        if (querySender == null)
            querySender = new DefaultStatementExecutor(null, true);

        if (dc != null)
            querySender.setDatabaseConnection(dc);

        querySender.setTpb(tpp.getTpb(dc));
        statementCancelled = false;

        worker = new ThreadWorker("ExecutingQueryInQueryDispatcher") {

            @Override
            public Object construct() {

                try {

                    DefaultProfilerExecutor profilerExecutor = new DefaultProfilerExecutor(dc, null);
                    int sessionId = profilerExecutor.startSession();

                    if (sessionId != -1) {
                        executeSQL(query, executeAsBlock, false);
                        profilerExecutor.finishSession();
                        GUIUtilities.addCentralPane(ProfilerPanel.TITLE,
                                (Icon) null, new ProfilerPanel(sessionId, dc), null, true);

                    } else {
                        GUIUtilities.displayWarningMessage(Bundles.get(ProfilerPanel.class, "VersionNotSupported"));
                        setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, "Action canceled, DB version is not supported", false);
                    }

                } catch (SQLException ex) {
                    Log.error("Error executing script in profiler session", ex);
                }

                return null;
            }

            @Override
            public void finished() {

                delegate.finished(duration);

                if (statementCancelled) {
                    setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, "Statement cancelled", false);
                    delegate.setStatusMessage(" Statement cancelled");
                }

                querySender.setCloseConnectionAfterQuery(false);
                querySender.releaseResourcesWithoutCommit();
                executing = false;
            }

        };

        setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, "---\nUsing connection: " + dc, false);

        delegate.executing();
        delegate.setStatusMessage(Constants.EMPTY);
        worker.start();
    }

    public void executeSQLScript(DatabaseConnection dc,
                                 final String script, boolean anyConnections) {

        if (!ConnectionManager.hasConnections()) {

            setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, "Not Connected", false);
            setStatusMessage(ERROR_EXECUTING);

            return;
        }

        if (querySender == null) {

            querySender = new DefaultStatementExecutor(null, true);
        }

        if (dc != null) {

            querySender.setDatabaseConnection(dc);
        }

        querySender.setTpb(tpp.getTpb(dc));

        statementCancelled = false;

        worker = new ThreadWorker("ExecutingScriptInQueryDispatcher") {

            public Object construct() {

                return executeSQLScript(script, anyConnections);
            }

            public void finished() {

                delegate.finished(duration);

                if (statementCancelled) {

                    setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE,
                            "Statement cancelled", anyConnections);
                    delegate.setStatusMessage(" Statement cancelled");
                }

                querySender.releaseResources();
                executing = false;
            }

        };

        setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, "---\nUsing connection: " + dc, anyConnections);

        delegate.executing();
        delegate.setStatusMessage(Constants.EMPTY);
        worker.start();
    }

    public void printExecutedPlan(DatabaseConnection dc,
                                  final String query, boolean explained, boolean anyConnections) {

        if (!ConnectionManager.hasConnections()) {

            setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, "Not Connected", anyConnections);
            setStatusMessage(ERROR_EXECUTING);

            return;
        }

        if (querySender == null) {

            querySender = new DefaultStatementExecutor(null, true);
        }

        if (dc != null) {

            querySender.setDatabaseConnection(dc);
        }

        querySender.setTpb(tpp.getTpb(dc));

        try {
            Statement statement = querySender.getPreparedStatement(query);
            printPlan(statement, explained, anyConnections);
        } catch (SQLException e) {
            setOutputMessage(dc, SqlMessages.ERROR_MESSAGE, e.getMessage(), anyConnections);
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
    private Object executeSQL(String sql, boolean executeAsBlock, boolean anyConnections) {

        IFBPerformanceInfo before;
        before = null;

        waiting = false;
        long totalDuration = 0L;
        querySender.setCloseConnectionAfterQuery(false);

        try {

            long start = 0L;
            long end = 0L;

            // check we are executing the whole block of sql text
            if (executeAsBlock) {

                // print the query
                logExecution(sql.trim(), anyConnections);

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
                        setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE,
                                result.getErrorMessage(), true, anyConnections);
                        setStatusMessage(ERROR_EXECUTING);

                    } else {

                        setResultSet(rset, sql, anyConnections);
                    }

                } else {

                    int updateCount = result.getUpdateCount();

                    if (updateCount == -1) {
                        setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE,
                                result.getErrorMessage(), true, anyConnections);
                        setStatusMessage(ERROR_EXECUTING);

                    } else {
                        if (anyConnections)
                            setResult(querySender.getDatabaseConnection(), updateCount, QueryTypes.UNKNOWN, null);
                        else setResult(null, updateCount, QueryTypes.UNKNOWN, null);
                    }

                }

                end = System.currentTimeMillis();
                statementExecuted(sql);

                long timeTaken = end - start;

                logExecutionTime(timeTaken, anyConnections);

                duration = formatDuration(totalDuration);

                return DONE;
            }

            executing = true;

            String procQuery = sql.toUpperCase();
            String noCommentsQuery = queryTokenizer.removeComments(procQuery);
            DerivedQuery derivedQuery = new DerivedQuery(noCommentsQuery);
            // check if its a procedure creation or execution
            if (isBeginEndQuery(derivedQuery)) {

                return executeCreateOrAlterObject(sql, derivedQuery, anyConnections);
            }

            //List<DerivedQuery> queries = queryTokenizer.tokenize(sql);
            boolean removeQueryComments = userProperties().getBooleanProperty("editor.execute.remove.comments");

            DerivedQuery query = new DerivedQuery(sql);

            if (!query.isExecutable()) {

                    setOutputMessage(querySender.getDatabaseConnection(),
                            SqlMessages.WARNING_MESSAGE, "Non executable query provided", anyConnections);
                    return DONE;
                }

            // reset clock
            end = 0L;
            start = 0L;

            String derivedQueryString = query.getDerivedQuery();
            String queryToExecute = removeQueryComments ? derivedQueryString : query.getOriginalQuery();

            int type = query.getQueryType();
            if (type != QueryTypes.COMMIT && type != QueryTypes.ROLLBACK) {

                    logExecution(queryToExecute, anyConnections);

            } else {

                if (type == QueryTypes.COMMIT) {

                        setOutputMessage(querySender.getDatabaseConnection(),
                                SqlMessages.ACTION_MESSAGE,
                                COMMITTING_LAST, anyConnections);

                } else if (type == QueryTypes.ROLLBACK) {

                        setOutputMessage(
                                querySender.getDatabaseConnection(),
                                SqlMessages.ACTION_MESSAGE,
                                ROLLING_BACK_LAST, anyConnections);
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

                    IFBDatabasePerformance db = (IFBDatabasePerformance) DynamicLibraryLoader.loadingObjectFromClassLoader(databaseConnection.getDriverMajorVersion(), connection, "FBDatabasePerformanceImpl");
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

                        printExecutionPlan(before, anyConnections);
                        setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE,
                                message, true, anyConnections);
                        setStatusMessage(ERROR_EXECUTING);

                } else {

                    // Trying to get execution plan of firebird statement

                        printPlan(rset, anyConnections);

                        setResultSet(rset, query.getOriginalQuery(), anyConnections);

                        printExecutionPlan(before,anyConnections);
                    }

                end = System.currentTimeMillis();

            } else {

                end = System.currentTimeMillis();

                // check that we executed a 'normal' statement (not a proc)
                if (result.getType() != QueryTypes.EXECUTE) {

                    int updateCount = result.getUpdateCount();
                    if (updateCount == -1) {

                            printExecutionPlan(before, anyConnections);
                            setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE,
                                    result.getErrorMessage(), true, anyConnections);
                            setStatusMessage(ERROR_EXECUTING);

                    } else {

                        if (result.isException()) {

                                printExecutionPlan(before, anyConnections);
                                setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE, result.getErrorMessage(), true, anyConnections);
                            } else {

                                type = result.getType();
                                setResultText(querySender.getDatabaseConnection(), updateCount, query.getQueryType(), query.getMetaName(), anyConnections);
                                if (type == QueryTypes.CREATE_OBJECT || type == QueryTypes.DROP_OBJECT
                                        || type == QueryTypes.CREATE_OR_ALTER || type == QueryTypes.RECREATE_OBJECT || type == QueryTypes.ALTER_OBJECT) {
                                    DatabaseObjectNode hostNode = ConnectionsTreePanel.getPanelFromBrowser().getHostNode(querySender.getDatabaseConnection());
                                    for (DatabaseObjectNode metaTagNode : hostNode.getChildObjects()) {
                                        if (metaTagNode.getMetaDataKey().equals(query.getMetaName())) {
                                            ConnectionsTreePanel.getPanelFromBrowser().reloadPath(metaTagNode.getTreePath());

                                    } else if ((NamedObject.META_TYPES[NamedObject.TABLE].contentEquals(query.getMetaName()) || NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY].contentEquals(query.getMetaName())) && metaTagNode.isSystem()) {
                                        ConnectionsTreePanel.getPanelFromBrowser().reloadPath(metaTagNode.getTreePath());
                                    }
                                }


                            }

                            if (type == QueryTypes.COMMIT || type == QueryTypes.ROLLBACK) {

                                setStatusMessage(" " + result.getMessage());
                            }

                                printExecutionPlan(before, anyConnections);

                        }
                    }

                } else {

                    @SuppressWarnings("rawtypes")
                    Map results = (Map) result.getOtherResult();

                    if (results == null) {

                            printExecutionPlan(before, anyConnections);

                            setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE, result.getErrorMessage(), true, anyConnections);
                            setStatusMessage(ERROR_EXECUTING);

                    } else {

                            printExecutionPlan(before,  anyConnections);

                            setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.PLAIN_MESSAGE, "Call executed successfully.", anyConnections);
                            int updateCount = result.getUpdateCount();

                        if (updateCount > 0) {

                                setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.PLAIN_MESSAGE,
                                        updateCount +
                                                ((updateCount > 1) ?
                                                        " rows affected." : " row affected."), anyConnections);
                            }

                        String SPACE = " = ";
                        for (Iterator<?> i = results.keySet().iterator(); i.hasNext(); ) {

                                String key = i.next().toString();
                                setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.PLAIN_MESSAGE,
                                        key + SPACE + results.get(key), anyConnections);
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
                logExecutionTime(timeTaken, anyConnections);


            statementExecuted(sql);

        } catch (SQLException e) {

            processException(e, anyConnections);
            return "SQLException";

        } catch (InterruptedException e) {

            //Log.debug("InterruptedException");
            statementCancelled = true; // make sure its set
            return "Interrupted";

        } catch (OutOfMemoryError e) {

            setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE,
                    "Resources exhausted while executing query.\n" +
                            "The query result set was too large to return.", true, anyConnections);

            setStatusMessage(ERROR_EXECUTING);

        } catch (Exception e) {

            if (!statementCancelled) {

                if (Log.isDebugEnabled()) {

                    e.printStackTrace();
                }

                processException(e, anyConnections);
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

    private Object executeSQLScript(String script, boolean anyConnections) {

        IFBPerformanceInfo before, after;
        before = null;
        after = null;

        waiting = false;
        long totalDuration = 0L;
        querySender.setCloseConnectionAfterQuery(false);

        try {

            long start = 0L;
            long end = 0L;

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

                    IFBDatabasePerformance db = (IFBDatabasePerformance) DynamicLibraryLoader.loadingObjectFromClassLoader(driver.getMajorVersion(), connection, "FBDatabasePerformanceImpl");
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
            setOutputMessage(querySender.getDatabaseConnection(),
                    SqlMessages.ACTION_MESSAGE, "Found " + executableQueries.size() + " queries", anyConnections);
            start = System.currentTimeMillis();
            boolean stopOnError = SystemProperties.getBooleanProperty("user", "editor.stop.on.error");
            boolean error = false;
            TreeSet<String> createsMetaNames = new TreeSet<>();
            for (int i = 0; i < executableQueries.size(); i++) {
                try {
                    DerivedQuery query = executableQueries.get(i);
                    setOutputMessage(querySender.getDatabaseConnection(),
                            SqlMessages.ACTION_MESSAGE, (i + 1) + " query", anyConnections);
                    if (statementCancelled || Thread.interrupted()) {

                        throw new InterruptedException();
                    }

                    String queryToExecute = query.getDerivedQuery();

                    int type = query.getQueryType();
                    if (type != QueryTypes.COMMIT && type != QueryTypes.ROLLBACK) {

                        logExecution(queryToExecute, anyConnections);

                    } else {

                        if (type == QueryTypes.COMMIT) {

                            setOutputMessage(querySender.getDatabaseConnection(),
                                    SqlMessages.ACTION_MESSAGE,
                                    COMMITTING_LAST, anyConnections);

                        } else if (type == QueryTypes.ROLLBACK) {

                            setOutputMessage(querySender.getDatabaseConnection(),
                                    SqlMessages.ACTION_MESSAGE,
                                    ROLLING_BACK_LAST, anyConnections);
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

                            printExecutionPlan(before, anyConnections);

                            setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE,
                                    message, true, anyConnections);
                            setStatusMessage(ERROR_EXECUTING);
                            error = true;

                        } else {

                            // Trying to get execution plan of firebird statement

                            printPlan(rset, anyConnections);

                            setResultSet(rset, query.getOriginalQuery(), anyConnections);

                            printExecutionPlan(before, anyConnections);
                        }

                        end = System.currentTimeMillis();

                    } else {

                        end = System.currentTimeMillis();

                        // check that we executed a 'normal' statement (not a proc)
                        if (result.getType() != QueryTypes.EXECUTE) {

                            int updateCount = result.getUpdateCount();
                            if (updateCount == -1) {

                                //printExecutionPlan(before, after);

                                setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE,
                                        result.getErrorMessage(), true, anyConnections);
                                setStatusMessage(ERROR_EXECUTING);
                                error = true;

                            } else {

                                if (result.isException()) {

                                    //printExecutionPlan(before, after);

                                    setOutputMessage(querySender.getDatabaseConnection(),
                                            SqlMessages.ERROR_MESSAGE, result.getErrorMessage(), true, anyConnections);
                                    error = true;
                                } else {

                                    type = result.getType();
                                    setResultText(querySender.getDatabaseConnection(),
                                            updateCount, query.getQueryType(), query.getMetaName(), anyConnections);
                                    if (type == QueryTypes.CREATE_OBJECT || type == QueryTypes.DROP_OBJECT
                                            || type == QueryTypes.CREATE_OR_ALTER || type == QueryTypes.RECREATE_OBJECT || type == QueryTypes.ALTER_OBJECT) {
                                        createsMetaNames.add(query.getMetaName());
                                    }
                                    if (type == QueryTypes.COMMIT || type == QueryTypes.ROLLBACK) {

                                        setStatusMessage(" " + result.getMessage());
                                    }

                                    printExecutionPlan(before, anyConnections);

                                }
                            }

                        } else {

                            @SuppressWarnings("rawtypes")
                            Map results = (Map) result.getOtherResult();

                            if (results == null && result.getUpdateCount() < 0) {

                                //printExecutionPlan(before, after);

                                setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE, result.getErrorMessage(), true, anyConnections);
                                setStatusMessage(ERROR_EXECUTING);
                                error = true;

                            } else {

                                printExecutionPlan(before, anyConnections);

                                setOutputMessage(querySender.getDatabaseConnection(),
                                        SqlMessages.PLAIN_MESSAGE, "Call executed successfully.", anyConnections);
                                int updateCount = result.getUpdateCount();

                                if (updateCount > 0) {

                                    setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.PLAIN_MESSAGE,
                                            updateCount +
                                                    ((updateCount > 1) ?
                                                            " rows affected." : " row affected."), anyConnections);
                                }

                                String SPACE = " = ";
                                if (results != null)
                                    for (Iterator<?> it = results.keySet().iterator(); it.hasNext(); ) {

                                    String key = it.next().toString();
                                    setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.PLAIN_MESSAGE,
                                            key + SPACE + results.get(key), anyConnections);
                                }

                            }

                        }

                    }

                    // execution times


                } catch (SQLException e) {

                    processException(e, anyConnections);
                    return "SQLException";

                } catch (InterruptedException e) {

                    //Log.debug("InterruptedException");
                    statementCancelled = true; // make sure its set
                    return "Interrupted";

                } catch (OutOfMemoryError e) {

                    setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE,
                            "Resources exhausted while executing query.\n" +
                                    "The query result set was too large to return.", true, anyConnections);

                    setStatusMessage(ERROR_EXECUTING);

                } catch (Exception e) {

                    if (!statementCancelled) {


                        e.printStackTrace();


                        processException(e, anyConnections);
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
            logExecutionTime(timeTaken, anyConnections);
            DatabaseObjectNode hostNode = ConnectionsTreePanel.getPanelFromBrowser().getHostNode(querySender.getDatabaseConnection());

            for (DatabaseObjectNode metaTagNode : hostNode.getChildObjects()) {
                if (createsMetaNames.contains(metaTagNode.getMetaDataKey()) || metaTagNode.isSystem()) {
                    ConnectionsTreePanel.getPanelFromBrowser().reloadPath(metaTagNode.getTreePath());
                }
            }
            statementExecuted(script);

        } catch (InterruptedException e) {

            //Log.debug("InterruptedException");
            statementCancelled = true; // make sure its set
            return "Interrupted";

        } catch (OutOfMemoryError e) {

            setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE,
                    "Resources exhausted while executing query.\n" +
                            "The query result set was too large to return.", true, anyConnections);

            setStatusMessage(ERROR_EXECUTING);

        } catch (Exception e) {

            if (!statementCancelled) {

                if (Log.isDebugEnabled()) {

                    e.printStackTrace();
                }

                processException(e, anyConnections);
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
            if (spd.isCanceled()) {
                statementCancelled = true;
                throw new DataSourceException("Canceled");
            }
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
            if (spd.isCanceled()) {
                statementCancelled = true;
                throw new DataSourceException("Canceled");
            }
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

    private void printExecutionPlan(IFBPerformanceInfo before, boolean anyConnections) {
        // Trying to get execution plan of firebird statement
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
            IFBPerformanceInfo after = null;
            try {
                IFBDatabasePerformance db = (IFBDatabasePerformance) DynamicLibraryLoader.loadingObjectFromClassLoader(databaseConnection.getDriverMajorVersion(), connection, "FBDatabasePerformanceImpl");
                db.setConnection(connection);
                after = db.getPerformanceInfo();

            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (before != null && after != null) {
                IFBPerformanceInfo resultPerfomanceInfo = after.processInfo(before, after);

                setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.PLAIN_MESSAGE, resultPerfomanceInfo.getPerformanceInfo(), anyConnections);
            }
        }

    }

    private void printPlan(ResultSet rs, boolean anyConnections) {
        try {
            DatabaseConnection databaseConnection = this.querySender.getDatabaseConnection();
            Map<String, Driver> loadedDrivers = DefaultDriverLoader.getLoadedDrivers();
            DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
            Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());

            if (driver.getClass().getName().contains("FBDriver")) {
                ResultSet realRS = ((PooledResultSet) rs).getResultSet();

                ResultSet resultSet = null;
                try {
                    resultSet = realRS.unwrap(ResultSet.class);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    IFBDatabasePerformance db = (IFBDatabasePerformance) DynamicLibraryLoader.loadingObjectFromClassLoader(databaseConnection.getDriverMajorVersion(), resultSet, "FBDatabasePerformanceImpl");

                    setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.PLAIN_MESSAGE, db.getLastExecutedPlan(resultSet), anyConnections);

                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printPlan(Statement st, boolean explained, boolean anyConnections) {
        try {
            DatabaseConnection databaseConnection = this.querySender.getDatabaseConnection();
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
                try {
                    IFBDatabasePerformance db = (IFBDatabasePerformance) DynamicLibraryLoader.loadingObjectFromClassLoader(databaseConnection.getDriverMajorVersion(), statement, "FBDatabasePerformanceImpl");
                    String plan;
                    if (explained)
                        plan = db.getLastExplainExecutedPlan(statement);
                    else plan = db.getLastExecutedPlan(statement);
                    setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.PLAIN_MESSAGE, plan, true, anyConnections);

                } catch (SQLException | ClassNotFoundException e) {
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

    private void setResult(DatabaseConnection dc, int updateCount, int type, String metaName) {

        delegate.setResult(dc, updateCount, type, metaName);
    }

    private void statementExecuted(String sql) {

        delegate.statementExecuted(sql);
    }

    private Object executeCreateOrAlterObject(String sql, DerivedQuery procQuery, boolean anyConnection)
            throws SQLException {

        logExecution(sql.trim(), anyConnection);

        long start = System.currentTimeMillis();
        PreparedStatement statement = querySender.getPreparedStatement(sql);
        SqlStatementResult result = querySender.execute(procQuery.getQueryType(), statement);

        if (result.getUpdateCount() == -1 || result.isException()) {

            setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE, result.getErrorMessage(), anyConnection);
            setStatusMessage(ERROR_EXECUTING);

        } else {
            setResultText(querySender.getDatabaseConnection(), result.getUpdateCount(), procQuery.getQueryType(), procQuery.getMetaName(), anyConnection);
            DatabaseObjectNode hostNode = ConnectionsTreePanel.getPanelFromBrowser().getHostNode(querySender.getDatabaseConnection());

            for (DatabaseObjectNode metaTagNode : hostNode.getChildObjects()) {
                if (metaTagNode.getMetaDataKey().equals(procQuery.getMetaName()) || metaTagNode.isSystem()) {
                    ConnectionsTreePanel.getPanelFromBrowser().reloadPath(metaTagNode.getTreePath());
                }
            }
        }

        long end = System.currentTimeMillis();

        outputWarnings(result.getSqlWarning(), anyConnection);

        logExecutionTime(start, end, anyConnection);

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
    private void logExecutionTime(long start, long end, boolean anyConnections) {

        logExecutionTime(end - start, anyConnections);
    }

    /**
     * Logs the execution duration within the output
     * pane for the specified value.
     *
     * @param time the time in millis
     */
    private void logExecutionTime(long time, boolean anyConnections) {
        setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.PLAIN_MESSAGE,
                "Execution time: " + formatDuration(time), false, anyConnections);
    }

    /**
     * Logs the specified query being executed.
     *
     * @param query - the executed query
     */
    private void logExecution(String query, boolean anyConnections) {

        Log.info(EXECUTING + query);

        if (verboseLogging) {

            setOutputMessage(querySender.getDatabaseConnection(),
                    SqlMessages.ACTION_MESSAGE, EXECUTING, anyConnections);
            setOutputMessage(querySender.getDatabaseConnection(),
                    SqlMessages.ACTION_MESSAGE_PREFORMAT, query, anyConnections);

        } else {

            int queryLength = query.length();
            int subIndex = queryLength < 50 ? (queryLength + 1) : 50;

            setOutputMessage(querySender.getDatabaseConnection(),
                    SqlMessages.ACTION_MESSAGE, EXECUTING, anyConnections);
            setOutputMessage(querySender.getDatabaseConnection(),
                    SqlMessages.ACTION_MESSAGE_PREFORMAT,
                    query.substring(0, subIndex - 1).trim() + SUBSTRING, anyConnections);
        }

    }

    private void processException(Throwable e, boolean anyConnections) {

        if (e != null) {
            setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.ERROR_MESSAGE, e.getMessage(), anyConnections);

            if (e instanceof SQLException) {

                SQLException sqlExc = (SQLException) e;
                sqlExc = sqlExc.getNextException();

                if (sqlExc != null) {

                    setOutputMessage(querySender.getDatabaseConnection(),
                            SqlMessages.ERROR_MESSAGE, sqlExc.getMessage(), anyConnections);
                }

            } else {

                setStatusMessage(ERROR_EXECUTING);
            }
        }

    }

    private void setResultText(DatabaseConnection dc, final int result, final int type, String metaName, boolean anyConnections) {
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                if (!anyConnections)
                    delegate.setResult(null, result, type, metaName);
                else delegate.setResult(dc, result, type, metaName);
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

    private void setOutputMessage(DatabaseConnection databaseConnection, final int type, final String text, boolean useDatabaseConnection) {

        setOutputMessage(databaseConnection, type, text, false, useDatabaseConnection);
    }

    private void setOutputMessage(DatabaseConnection databaseConnection, final int type, final String text, final boolean selectTab, boolean useDatabaseConnection) {
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                DatabaseConnection dc = null;
                if (useDatabaseConnection)
                    dc = databaseConnection;
                delegate.setOutputMessage(dc, type, text, selectTab);
                if (text != null) {
                    logOutput(text);
                }
            }
        });
    }

    private void setResultSet(final ResultSet rs, final String query, boolean anyConnections) {
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
            processException(e, anyConnections);
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
    private void outputWarnings(SQLWarning warning, boolean anyConnections) {

        if (warning == null) {
            return;
        }

        String dash = " - ";
        // print the first warning
        setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.WARNING_MESSAGE,
                warning.getErrorCode() + dash + warning.getMessage(), anyConnections);

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
            setOutputMessage(querySender.getDatabaseConnection(), SqlMessages.WARNING_MESSAGE,
                    _errorCode + dash + _warning.getMessage(), anyConnections);
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
    private boolean isBeginEndQuery(DerivedQuery query) {
        if (isNotSingleStatementExecution(query.getQueryType())) {
            return (isCreateProcedure(query))
                    || isCreateFunction(query)
                    || isCreatePackage(query)
                    || isCreateTrigger(query);
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
    private boolean isCreateProcedure(DerivedQuery query) {

        return query.getTypeObject() == NamedObject.PROCEDURE;
    }

    /**
     * Determines whether the specified query is attempting
     * to create a SQL FUNCTION.
     *
     * @param query - the query to be executed
     * @return true | false
     */
    private boolean isCreateFunction(DerivedQuery query) {
        return query.getTypeObject() == NamedObject.FUNCTION;
    }

    private boolean isCreateTrigger(DerivedQuery query) {
        return query.getTypeObject() == NamedObject.TRIGGER
                || query.getTypeObject() == NamedObject.DDL_TRIGGER
                || query.getTypeObject() == NamedObject.DATABASE_TRIGGER;
    }

    private boolean isCreatePackage(DerivedQuery query) {
        return query.getTypeObject() == NamedObject.PACKAGE;
    }

    private boolean isNotSingleStatementExecution(int typeQuery) {


        int[] nonSingleStatementExecutionTypes = {
                QueryTypes.CREATE_OBJECT,
                QueryTypes.ALTER_OBJECT,
                QueryTypes.CREATE_OR_ALTER
        };

        for (int i = 0; i < nonSingleStatementExecutionTypes.length; i++) {

            if (typeQuery == nonSingleStatementExecutionTypes[i]) {

                return true;
            }

        }

        return false;
    }

    public TransactionParametersPanel getTpp() {
        return tpp;
    }

    public void setTpp(TransactionParametersPanel tpp) {
        this.tpp = tpp;
    }

    private boolean checkBeforeExecuteQuery(String query, DatabaseConnection dc, boolean anyConnections) {

        String checkUpdatesToLog = "Checking for updates from the release hub is ";
        if (query.toLowerCase().trim().startsWith("releasehub on")) {
            SystemProperties.setProperty("user", "releasehub",
                    "true");
            checkUpdatesToLog += "enabled";
            Log.info(checkUpdatesToLog);
            setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, checkUpdatesToLog, anyConnections);
            return false;
        }
        if (query.toLowerCase().trim().startsWith("releasehub off")) {
            SystemProperties.setProperty("user", "releasehub",
                    "false");
            checkUpdatesToLog += "disabled";
            Log.info(checkUpdatesToLog);
            setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, checkUpdatesToLog, anyConnections);
            return false;
        }

        if (!ConnectionManager.hasConnections()) {

            setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, "Not Connected", anyConnections);
            setStatusMessage(ERROR_EXECUTING);

            return false;
        }

        return true;
    }
}











