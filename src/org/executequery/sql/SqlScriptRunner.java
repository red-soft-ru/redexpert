/*
 * SqlScriptRunner.java
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

import biz.redsoft.IFBCreateDatabase;
import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultDatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.datasource.SimpleDataSource;
import org.executequery.gui.editor.InputParametersDialog;
import org.executequery.gui.editor.QueryEditorHistory;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.log.Log;
import org.underworldlabs.sqlParser.SqlParser;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.resource.ResourceException;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlScriptRunner {

    private final ExecutionController controller;

    private boolean cancel;
    private boolean closeConnection;

    private Connection connection;
    private SimpleDataSource localDataSource;

    public SqlScriptRunner(ExecutionController controller) {
        super();
        this.controller = controller;
    }

    public SqlStatementResult execute(DatabaseConnection connection, String script, boolean stopOnError) {

        int count = 0;
        int result = 0;
        long start = 0L;
        int thisResult = 0;
        int startIndex = 0;

        String delimiter = ";";
        String sqlDialect = "3";
        String blobFilePath = null;

        DerivedQuery query;
        PreparedStatement statement = null;
        QueryTokenizer queryTokenizer = new QueryTokenizer();
        SqlStatementResult statementResult = new SqlStatementResult();
        DefaultStatementExecutor querySender = new DefaultStatementExecutor();

        cancel = false;
        closeConnection = false;
        boolean logOutput = controller.logOutput();

        try {

            close();
            if (connection != null)
                querySender.setDatabaseConnection(connection);

            controller.actionMessage("Extracting comments and String constants...");
            queryTokenizer.extractTokens(script);

            controller.actionMessage("Executing...");
            while (script != null && !script.isEmpty()) {

                QueryTokenizer.QueryTokenized formattedScript = queryTokenizer.tokenizeFirstQuery(script, script.toLowerCase(), startIndex, delimiter);
                query = formattedScript.query;
                script = formattedScript.script;
                delimiter = formattedScript.delimiter;
                startIndex = formattedScript.startIndex;

                if (query == null || !query.isExecutable())
                    continue;

                if (maybeStop())
                    throw new InterruptedException();

                String derivedQuery = query.getDerivedQuery().trim();
                if (query.getQueryType() == QueryTypes.CREATE_DATABASE) {

                    this.localDataSource = createDatabase(query, sqlDialect);
                    this.connection = localDataSource.getConnection();
                    this.connection.setAutoCommit(false);

                    querySender.setUseDatabaseConnection(false);
                    querySender.setConn(this.connection);

                    closeConnection = true;
                    continue;

                } else if (query.getQueryType() == QueryTypes.SQL_DIALECT) {

                    Pattern pattern = Pattern.compile("\\d", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(query.getQueryWithoutComments());
                    if (matcher.find())
                        sqlDialect = matcher.group().trim();

                    continue;

                } else if (derivedQuery.toUpperCase().contains("SET BLOBFILE ")) {
                    derivedQuery = query.getQueryWithoutComments().trim();
                    blobFilePath = "blobfile=" + derivedQuery.substring(14, derivedQuery.length() - 1);
                    continue;
                }

                count++;
                try {

                    start = System.currentTimeMillis();
                    if (logOutput) {
                        controller.message("Executing query " + count + ":");
                        controller.queryMessage(derivedQuery);
                    }

                    if (query.getQueryType() == QueryTypes.INSERT)
                        statement = getPreparedStatementWithParameters(querySender, derivedQuery, blobFilePath);
                    else
                        statement = querySender.getPreparedStatement(derivedQuery);

                    if (statement == null)
                        continue;

                    statementResult = querySender.execute(query.getQueryType(), statement);

                    if (statementResult.isException()) {
                        if (statementResult.getSqlException() != null)
                            throw statementResult.getSqlException();
                        if (statementResult.getOtherException() != null)
                            throw statementResult.getOtherException();
                    }

                    result += statementResult.getUpdateCount();

                } catch (Throwable e) {

                    controller.errorMessage("Error executing statement:\n" + e.getMessage());

                    if (stopOnError)
                        throw e;

                } finally {
                    try {
                        if (statement != null && !statement.isClosed())
                            statement.close();

                    } catch (SQLException e) {
                        Log.error(e.getMessage(), e);
                    }
                }

                if (logOutput) {
                    controller.message("Records affected: " + thisResult);
                    controller.message("Duration: " + MiscUtils.formatDuration(System.currentTimeMillis() - start));
                }
            }

        } catch (SQLException e) {
            statementResult.setSqlException(e);

        } catch (Throwable e) {
            statementResult.setOtherException(e);

        } finally {
            try {
                if (closeConnection)
                    localDataSource.close();

            } catch (ResourceException e) {
                Log.error(e.getMessage(), e);
            }

            System.gc();
        }

        statementResult.setUpdateCount(result);
        statementResult.setStatementCount(count);

        return statementResult;
    }

    private SimpleDataSource createDatabase(DerivedQuery query, String sqlDialect) throws SQLException {

        String derivedQuery = query.getDerivedQuery();

        String server = "localhost";
        String port = "3050";
        String user = null;
        String password = null;
        String pageSize = null;
        String charSet = null;

        Pattern pattern = Pattern.compile("create\\s+database\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(derivedQuery);

        int index = -1;
        if (matcher.find())
            index = matcher.end();

        if (index == -1)
            throw new SQLException("Cannot create database. Check creating database syntax: " + derivedQuery);

        String database = derivedQuery.substring(index).trim();
        String firstSymbol = String.valueOf(database.charAt(0));
        database = StringUtils.substringBetween(database, firstSymbol, firstSymbol);

        // --- extracting DB properties ---

        int separatorIndex = database.indexOf(":");
        if (separatorIndex != -1) {
            if (database.charAt(separatorIndex + 1) != '\\') {
                server = database.substring(0, separatorIndex);
                database = database.substring(separatorIndex + 1);
            }
        }

        separatorIndex = database.indexOf(":");
        if (separatorIndex > 2) {
            if (database.charAt(separatorIndex + 1) != '\\') {
                port = database.substring(0, separatorIndex);
                database = database.substring(separatorIndex + 1);
            }
        }

        derivedQuery = derivedQuery.substring(derivedQuery.lastIndexOf(database) + database.length()).trim();

        index = StringUtils.indexOfIgnoreCase(derivedQuery, "USER");
        if (index != -1) {

            user = derivedQuery
                    .substring(index + "USER".length())
                    .trim()
                    .replaceAll("'", "")
                    .replaceAll("\"", "");

            index = getFirstWhitespaceIndex(user);
            if (index > 0)
                user = user.substring(0, index);
        }

        index = StringUtils.indexOfIgnoreCase(derivedQuery, "PASSWORD");
        if (index != -1) {

            password = derivedQuery
                    .substring(index + "PASSWORD".length())
                    .trim()
                    .replaceAll("'", "")
                    .replaceAll("\"", "");

            index = getFirstWhitespaceIndex(password);
            if (index > 0)
                password = password.substring(0, index);
        }

        index = StringUtils.indexOfIgnoreCase(derivedQuery, "PAGE_SIZE");
        if (index != -1) {

            pageSize = derivedQuery.substring(index + "PAGE_SIZE".length()).trim();

            index = getFirstWhitespaceIndex(pageSize);
            if (index > 0)
                pageSize = pageSize.substring(0, index);
        }

        index = StringUtils.indexOfIgnoreCase(derivedQuery, "DEFAULT CHARACTER SET");
        if (index != -1) {

            charSet = derivedQuery.substring(index + "DEFAULT CHARACTER SET".length()).trim();

            index = getFirstWhitespaceIndex(charSet);
            if (index > 0)
                charSet = charSet.substring(0, index);
        }

        // --- creating new database connection ---

        DatabaseConnection temporaryConnection = new DefaultDatabaseConnection();
        try {

            Driver driver = DefaultDriverLoader.getDefaultDriver();
            if (driver.getMajorVersion() == 2)
                throw new SQLException("Cannot create database, Jaybird 2.x has no implementation for creation database.");

            Log.info("Database creation via jaybird");
            Log.info("Driver version: " + driver.getMajorVersion() + "." + driver.getMinorVersion());


            IFBCreateDatabase db = (IFBCreateDatabase) DynamicLibraryLoader.loadingObjectFromClassLoader(
                    driver.getMajorVersion(),
                    connection,
                    "FBCreateDatabaseImpl"
            );
            db.setServer(server);
            db.setPort(Integer.parseInt(port));
            db.setUser(user);
            db.setPassword(password);
            db.setDatabaseName(database);
            db.setEncoding(charSet);
            if (StringUtils.isNotEmpty(pageSize))
                db.setPageSize(Integer.parseInt(pageSize));

            try {
                db.exec();

            } catch (UnsatisfiedLinkError linkError) {
                String message = "Cannot create database, because fbclient library not found in environment path variable.\n" +
                        "Please, add fbclient library to environment path variable.\n" +
                        "Example for Windows system: setx path \"%path%;C:\\Program Files (x86)\\RedDatabase\\bin\\\"" + "\n\n" +
                        "Example for Linux system: export PATH=$PATH:/opt/RedDatabase/lib" + "\n\n" +
                        linkError.getMessage();

                throw new SQLException(message);

            } catch (Exception e) {
                String message = "The connection to the database could not be established." +
                        "\nPlease ensure all required fields have been entered correctly and try again." +
                        "\n\nThe system returned:\n" +
                        e.getMessage();

                throw new SQLException(message);
            }

            // --- setting properties for the connection ---

            Properties properties = new Properties();
            properties.setProperty("connectTimeout", String.valueOf(SystemProperties.getIntProperty("user", "connection.connect.timeout")));
            properties.setProperty("sqlDialect", sqlDialect);
            if (StringUtils.isNotEmpty(charSet))
                properties.setProperty("lc_ctype", charSet);

            temporaryConnection.setHost(server);
            temporaryConnection.setPort(port);
            temporaryConnection.setSourceName(database);
            temporaryConnection.setUserName(user);
            temporaryConnection.setPassword(password);
            temporaryConnection.setCharset(charSet);
            temporaryConnection.setJdbcProperties(properties);
            temporaryConnection.setJDBCDriver(DefaultDriverLoader.getDefaultDatabaseDriver());

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }

        return new SimpleDataSource(temporaryConnection);
    }

    public PreparedStatement getPreparedStatementWithParameters(DefaultStatementExecutor querySender, String sql, String variables) throws SQLException {

        SqlParser parser = new SqlParser(sql, variables);
        List<Parameter> params = parser.getParameters();
        List<Parameter> displayParams = parser.getDisplayParameters();

        PreparedStatement statement = querySender.getPreparedStatement(parser.getProcessedSql());
        statement.setEscapeProcessing(true);

        ParameterMetaData parameterMetaData = statement.getParameterMetaData();
        for (int i = 0; i < params.size(); i++) {
            params.get(i).setType(parameterMetaData.getParameterType(i + 1));
            params.get(i).setTypeName(parameterMetaData.getParameterTypeName(i + 1));
        }

        // restore old params if needed
        if (QueryEditorHistory.getHistoryParameters().containsKey(querySender.getDatabaseConnection())) {
            List<Parameter> oldParams = QueryEditorHistory.getHistoryParameters().get(querySender.getDatabaseConnection());

            for (Parameter displayParam : displayParams) {
                for (Parameter oldParam : oldParams) {

                    if (displayParam.getValue() == null
                            && oldParam.getType() == displayParam.getType()
                            && oldParam.getName().contentEquals(displayParam.getName())) {

                        displayParam.setValue(oldParam.getValue());
                        oldParams.remove(oldParam);
                        break;
                    }
                }
            }
        }

        // check for input params
        if (!displayParams.isEmpty()) {
            if (displayParams.stream().anyMatch(param -> param.getValue() == null)) {

                InputParametersDialog dialog = new InputParametersDialog(displayParams);
                dialog.display();

                if (dialog.isCanceled())
                    return null;
            }
        }

        // remember inputted params
        QueryEditorHistory.getHistoryParameters().put(querySender.getDatabaseConnection(), displayParams);

        // add params to the statement
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).isNull())
                statement.setNull(i + 1, params.get(i).getType());
            else
                statement.setObject(i + 1, params.get(i).getPreparedValue());
        }

        return statement;
    }

    private int getFirstWhitespaceIndex(String text) {
        Matcher matcher = Pattern.compile("\\s").matcher(text);
        return matcher.find() ? matcher.start() : -1;
    }

    private boolean maybeStop() {
        return Thread.interrupted() || cancel;
    }

    public void close() throws SQLException {
        if (connection != null)
            connection.close();
    }

    public void rollback() throws SQLException {
        if (connection != null)
            connection.rollback();
    }

    public void commit() throws SQLException {
        if (connection != null)
            connection.commit();
    }

    public void stop() {
        cancel = true;
    }

    public boolean isCloseConnection() {
        return closeConnection;
    }

}
