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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        String charset = null;
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
                    controller.actionMessage("Creating database...");

                    this.localDataSource = createDatabase(query, sqlDialect, charset);
                    this.connection = localDataSource.getConnection();
                    this.connection.setAutoCommit(false);

                    querySender.setUseDatabaseConnection(false);
                    querySender.setConn(this.connection);

                    closeConnection = true;
                    controller.actionMessage("Database created and connected");
                    continue;

                } else if (query.getQueryType() == QueryTypes.CONNECT) {
                    controller.actionMessage("Connecting to the database...");

                    this.localDataSource = connectDatabase(query, sqlDialect, charset);
                    this.connection = localDataSource.getConnection();
                    this.connection.setAutoCommit(false);

                    querySender.setUseDatabaseConnection(false);
                    querySender.setConn(this.connection);

                    closeConnection = true;
                    controller.actionMessage("Database connected");
                    continue;

                } else if (query.getQueryType() == QueryTypes.SET_AUTODDL_ON) {
                    this.connection.setAutoCommit(true);

                    controller.actionMessage("Autocommit is on");
                    continue;

                } else if (query.getQueryType() == QueryTypes.SET_AUTODDL_OFF) {
                    this.connection.setAutoCommit(false);

                    controller.actionMessage("Autocommit is off");
                    continue;

                } else if (query.getQueryType() == QueryTypes.SET_NAMES) {

                    Pattern pattern = Pattern.compile("SET\\s*NAMES\\s*", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(query.getQueryWithoutComments());
                    if (matcher.find())
                        charset = query.getQueryWithoutComments().substring(matcher.end());

                    if (charset != null && charset.endsWith(delimiter))
                        charset = charset.substring(0, charset.length() - 1);

                    controller.actionMessage("Character set is " + charset);
                    continue;

                } else if (query.getQueryType() == QueryTypes.SQL_DIALECT) {

                    Pattern pattern = Pattern.compile("\\d", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(query.getQueryWithoutComments());
                    if (matcher.find())
                        sqlDialect = matcher.group().trim();

                    controller.actionMessage("SQL dialect is " + sqlDialect);
                    continue;

                } else if (query.getQueryType() == QueryTypes.SET_BLOBFILE) {

                    derivedQuery = query.getQueryWithoutComments().trim();
                    blobFilePath = "blobfile=" + derivedQuery.substring(14, derivedQuery.length() - 1);

                    controller.actionMessage("Blob file is " + blobFilePath.replace("blobfile=", ""));
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

    private SimpleDataSource createDatabase(DerivedQuery query, String sqlDialect, String charSet) throws SQLException {

        String derivedQuery = query.getDerivedQuery();
        Map<String, String> dbProperties = getExtractProperties(derivedQuery);
        if (dbProperties == null)
            throw new SQLException("Cannot create database. Check creating database syntax: " + derivedQuery);

        if (!MiscUtils.isNull(dbProperties.get("charSet")))
            charSet = dbProperties.get("charSet");

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
            db.setEncoding(charSet);
            db.setUser(dbProperties.get("user"));
            db.setServer(dbProperties.get("server"));
            db.setPassword(dbProperties.get("password"));
            db.setDatabaseName(dbProperties.get("database"));
            db.setPort(Integer.parseInt(dbProperties.get("port")));
            if (!MiscUtils.isNull(dbProperties.get("pageSize")))
                db.setPageSize(Integer.parseInt(dbProperties.get("pageSize")));

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
            if (!MiscUtils.isNull(charSet))
                properties.setProperty("lc_ctype", charSet);

            temporaryConnection.setCharset(charSet);
            temporaryConnection.setJdbcProperties(properties);
            temporaryConnection.setPort(dbProperties.get("port"));
            temporaryConnection.setHost(dbProperties.get("server"));
            temporaryConnection.setUserName(dbProperties.get("user"));
            temporaryConnection.setPassword(dbProperties.get("password"));
            temporaryConnection.setSourceName(dbProperties.get("database"));
            temporaryConnection.setJDBCDriver(DefaultDriverLoader.getDefaultDatabaseDriver());

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }

        return new SimpleDataSource(temporaryConnection);
    }

    private SimpleDataSource connectDatabase(DerivedQuery query, String sqlDialect, String charSet) throws SQLException {

        String derivedQuery = query.getDerivedQuery();
        Map<String, String> dbProperties = getExtractProperties(derivedQuery);
        if (dbProperties == null)
            throw new SQLException("Cannot connect to the database. Check connection query syntax: " + derivedQuery);

        if (!MiscUtils.isNull(dbProperties.get("charSet")))
            charSet = dbProperties.get("charSet");

        String numBuffers = null;
        if (!MiscUtils.isNull(dbProperties.get("numBuffers")))
            numBuffers = dbProperties.get("numBuffers");


        // --- setting properties for the connection ---

        Properties properties = new Properties();
        properties.setProperty("connectTimeout", String.valueOf(SystemProperties.getIntProperty("user", "connection.connect.timeout")));
        properties.setProperty("sqlDialect", sqlDialect);
        if (!MiscUtils.isNull(charSet))
            properties.setProperty("lc_ctype", charSet);
        if (!MiscUtils.isNull(numBuffers))
            properties.setProperty("num_buffers", numBuffers);

        DatabaseConnection temporaryConnection = new DefaultDatabaseConnection();
        temporaryConnection.setCharset(charSet);
        temporaryConnection.setJdbcProperties(properties);
        temporaryConnection.setPort(dbProperties.get("port"));
        temporaryConnection.setRole(dbProperties.get("role"));
        temporaryConnection.setHost(dbProperties.get("server"));
        temporaryConnection.setUserName(dbProperties.get("user"));
        temporaryConnection.setPassword(dbProperties.get("password"));
        temporaryConnection.setSourceName(dbProperties.get("database"));
        temporaryConnection.setJDBCDriver(DefaultDriverLoader.getDefaultDatabaseDriver());

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

    private Map<String, String> getExtractProperties(String query) {
        Map<String, String> properties = new HashMap<>();

        String key;
        String val;
        int index = -1;
        boolean isConnectQuery = true;

        // --- check for the connection query ---

        Pattern pattern = Pattern.compile("CONNECT\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.find())
            index = matcher.end();

        // --- check for the create db query ---

        if (index < 0) {
            pattern = Pattern.compile("CREATE\\s+DATABASE\\s+", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(query);

            if (matcher.find())
                index = matcher.end();

            isConnectQuery = false;
        }

        if (index < 0)
            return null;

        // --- extrect database file ---

        String database = query.substring(index).trim();
        String firstSymbol = String.valueOf(database.charAt(0));
        database = StringUtils.substringBetween(database, firstSymbol, firstSymbol);

        // --- extract db server ---

        key = "server";
        val = "localhost";

        int separatorIndex = database.indexOf(isConnectQuery ? "/" : ":");
        if (separatorIndex != -1 && database.charAt(separatorIndex + 1) != '\\') {
            val = database.substring(0, separatorIndex);
            database = database.substring(separatorIndex + 1);
        }

        properties.put(key, val);

        // --- extract db port ---

        key = "port";
        val = "3050";

        separatorIndex = database.indexOf(":");
        if (separatorIndex > 2 && database.charAt(separatorIndex + 1) != '\\') {
            val = database.substring(0, separatorIndex);
            database = database.substring(separatorIndex + 1);
        }

        properties.put(key, val);

        // --- put database file ---

        key = "database";
        val = database;
        properties.put(key, val);

        query = query.substring(query.lastIndexOf(val) + val.length()).trim();

        // --- extract user name ---

        index = StringUtils.indexOfIgnoreCase(query, "USER");
        if (index != -1) {

            key = "user";
            val = query
                    .substring(index + "USER".length())
                    .trim()
                    .replaceAll("'", "")
                    .replaceAll("\"", "");

            index = getFirstWhitespaceIndex(val);
            if (index > 0)
                val = val.substring(0, index);

            properties.put(key, val);
        }

        // --- extract user password ---

        index = StringUtils.indexOfIgnoreCase(query, "PASSWORD");
        if (index != -1) {

            key = "password";
            val = query
                    .substring(index + "PASSWORD".length())
                    .trim()
                    .replaceAll("'", "")
                    .replaceAll("\"", "");

            index = getFirstWhitespaceIndex(val);
            if (index > 0)
                val = val.substring(0, index);

            properties.put(key, val);
        }

        // --- extract user role ---

        index = StringUtils.indexOfIgnoreCase(query, "ROLE");
        if (index != -1) {

            key = "role";
            val = query
                    .substring(index + "ROLE".length())
                    .trim()
                    .replaceAll("'", "")
                    .replaceAll("\"", "");

            index = getFirstWhitespaceIndex(val);
            if (index > 0)
                val = val.substring(0, index);

            properties.put(key, val);
        }

        // --- extract page size ---

        index = StringUtils.indexOfIgnoreCase(query, "PAGE_SIZE");
        if (index != -1) {

            key = "pageSize";
            val = query.substring(index + "PAGE_SIZE".length()).trim();

            index = getFirstWhitespaceIndex(val);
            if (index > 0)
                val = val.substring(0, index);

            properties.put(key, val);
        }

        // --- extract character set ---

        index = StringUtils.indexOfIgnoreCase(query, "DEFAULT CHARACTER SET");
        if (index != -1) {

            key = "charSet";
            val = query.substring(index + "DEFAULT CHARACTER SET".length()).trim();

            index = getFirstWhitespaceIndex(val);
            if (index > 0)
                val = val.substring(0, index);

            properties.put(key, val);
        }

        // --- extract character set ---

        index = StringUtils.indexOfIgnoreCase(query, "CACHE");
        if (index != -1) {

            key = "numBuffers";
            val = query.substring(index + "CACHE".length()).trim();

            index = getFirstWhitespaceIndex(val);
            if (index > 0)
                val = val.substring(0, index);

            properties.put(key, val);
        }

        // ---

        return properties;
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
