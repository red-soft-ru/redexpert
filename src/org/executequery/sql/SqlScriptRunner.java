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
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultDatabaseConnection;
import org.executequery.databasemediators.spi.DefaultDatabaseDriver;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.datasource.SimpleDataSource;
import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;

import javax.resource.ResourceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlScriptRunner {

    private Connection connection;

    DefaultStatementExecutor querySender;

    private final ExecutionController executionController;

    private boolean cancel;

    private SimpleDataSource localDataSource;

    boolean needCloseDatabase;

    public SqlScriptRunner(ExecutionController executionController) {

        super();
        this.executionController = executionController;
    }

    public void stop() {

        cancel = true;
    }

    public SqlStatementResult execute(DatabaseConnection databaseConnection,
                                      String script, ActionOnError actionOnError) {

        int count = 0;
        int result = 0;

        PreparedStatement statement = null;
        SqlStatementResult sqlStatementResult = new SqlStatementResult();
        needCloseDatabase = false;
        querySender = new DefaultStatementExecutor();

        try {

            cancel = false;

            executionController.message("Scanning and tokenizing queries...");
            QueryTokenizer queryTokenizer = new QueryTokenizer();
            //List<DerivedQuery> queries =

            close();
            if (databaseConnection != null) {
                querySender.setDatabaseConnection(databaseConnection);
            }

            List<DerivedQuery> executableQueries = new ArrayList<DerivedQuery>();
            DerivedQuery createDBQuery = null;
            String sqlDialect = "3";
            String delimiter = ";";
            //queries.clear();



            //executionController.message("Found " + executableQueries.size() + " executable queries");
            executionController.message("Executing...");

            /*if (connection == null)
                throw new SQLException("There is no connection. Select a connection from the available connections " +
                        "list or add a database creation statement.");*/

            long start = 0L;
            long end = 0L;
            int thisResult = 0;
            boolean logOutput = executionController.logOutput();
            int startIndex = 0;
            String lowQuery = script.toLowerCase();
            executionController.message("Start extracting comments and String constants");
            queryTokenizer.extractTokens(script);
            executionController.message("Finish extracting comments and String constants");
            while (script!=null &&!script.isEmpty()) {
                QueryTokenizer.QueryTokenized fquery=queryTokenizer.tokenizeFirstQuery(script,lowQuery,startIndex,delimiter);
                script= fquery.script;
                delimiter = fquery.delimiter;
                DerivedQuery query =  fquery.query;
                lowQuery = fquery.lowScript;
                startIndex = fquery.startIndex;
                if(query==null||!query.isExecutable())
                    continue;
                if (shouldNotContinue()) {

                    throw new InterruptedException();
                }
                if (query.getQueryType() == QueryTypes.CREATE_DATABASE) {
                    createDBQuery = query;
                    localDataSource = createDatabase(createDBQuery, sqlDialect);
                    connection = localDataSource.getConnection();
                    connection.setAutoCommit(false);
                    querySender.setUseDatabaseConnection(false);
                    querySender.setConn(connection);
                    createDBQuery = null;
                    needCloseDatabase = true;
                    continue;
                }
                if (query.getQueryType() == QueryTypes.SQL_DIALECT) {
                    Pattern pattern = Pattern.compile("\\d",
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(query.getDerivedQuery());
                    if (matcher.find())
                        sqlDialect = matcher.group().trim();
                    continue;
                }
                String derivedQuery = query.getDerivedQuery();
                try {

                    count++;

                    if (logOutput) {

                        executionController.message("Executing query " + count + ":");
                        executionController.queryMessage(query.getDerivedQuery());
                    }

                    statement = querySender.getPreparedStatement(derivedQuery);
                    start = System.currentTimeMillis();
                    sqlStatementResult = querySender.execute(query.getQueryType(), statement);
                    if (sqlStatementResult.isException())
                        if (sqlStatementResult.getSqlException() != null)
                            throw sqlStatementResult.getSqlException();
                    result += sqlStatementResult.getUpdateCount();

                } catch (SQLException e) {

                    executionController.errorMessage("Error executing statement:");
                    executionController.actionMessage(derivedQuery);

                    if (actionOnError != ActionOnError.CONTINUE) {

                        throw e;

                    } else {

                        executionController.errorMessage(e.getMessage());
                    }

                } finally {

                    if (statement != null && !statement.isClosed()) {

                        try {
                            statement.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                }

                end = System.currentTimeMillis();
                if (logOutput) {

                    executionController.message("Records affected: " + thisResult + "\nDuration: " + MiscUtils.formatDuration(end - start));
                }

            }

        } catch (SQLException e) {

            sqlStatementResult.setSqlException(e);

        } catch (InterruptedException e) {

            sqlStatementResult.setOtherException(e);

        } catch (org.underworldlabs.util.InterruptedException e) {

            sqlStatementResult.setOtherException(e);

        } finally {
            if (needCloseDatabase) {
                try {
                    localDataSource.close();
                } catch (ResourceException e) {
                    e.printStackTrace();
                }
            }
            System.gc();
        }

        sqlStatementResult.setUpdateCount(result);
        sqlStatementResult.setStatementCount(count);

        return sqlStatementResult;
    }

    public boolean isNeedCloseDatabase() {
        return needCloseDatabase;
    }

    private SimpleDataSource createDatabase(DerivedQuery query, String sqlDialect) throws SQLException {
        String derivedQuery = query.getDerivedQuery();

        String database = null;
        String server = "localhost";
        String port = "3050";
        String user = null;
        String password = null;
        String pageSize = null;
        String charSet = null;

        int idx = -1;
        Pattern pat = Pattern.compile("create\\s+database\\s+", Pattern.CASE_INSENSITIVE);
        Matcher m = pat.matcher(derivedQuery);
        if ( m.find() ) {
            idx = m.end();
        }
        if (idx == -1)
            throw new SQLException("Cannot create database. Check creating database syntax: " + derivedQuery);
        database = derivedQuery.substring(idx).trim();
        String sym = String.valueOf(database.charAt(0));
        database = StringUtils.substringBetween(database, sym, sym);
        int separatorIdx = database.indexOf(":");
        if (separatorIdx != -1) {
            if (database.charAt(separatorIdx + 1) != '\\') {
                server = database.substring(0, separatorIdx);
                database = database.substring(separatorIdx + 1);
            }
        }
        separatorIdx = database.indexOf(":");
        if (separatorIdx != -1 && separatorIdx > 2) {
            if (database.charAt(separatorIdx + 1) != '\\') {
                port = database.substring(0, separatorIdx);
                database = database.substring(separatorIdx + 1);
            }
        }

        derivedQuery = derivedQuery.substring(derivedQuery.lastIndexOf(database) + database.length()).trim();
        idx = StringUtils.indexOfIgnoreCase(derivedQuery, "USER");
        if (idx != -1) {
            user = derivedQuery.substring(idx + "USER".length()).trim();
            user = user.replaceAll("'", "");
            user = user.replaceAll("\"", "");
            idx = getFirstWhitespaceIndex(user);
            if (idx > 0)
                user = user.substring(0, idx);
        }
        idx = StringUtils.indexOfIgnoreCase(derivedQuery, "PASSWORD");
        if (idx != -1) {
            password = derivedQuery.substring(idx + "PASSWORD".length()).trim();
            password = password.replaceAll("'", "");
            password = password.replaceAll("\"", "");
            idx = getFirstWhitespaceIndex(password);
            if (idx > 0)
                password = password.substring(0, idx);
        }
        idx = StringUtils.indexOfIgnoreCase(derivedQuery, "PAGE_SIZE");
        if (idx != -1) {
            pageSize = derivedQuery.substring(idx + "PAGE_SIZE".length()).trim();
            idx = getFirstWhitespaceIndex(pageSize);
            if (idx > 0)
                pageSize = pageSize.substring(0, idx);
        }
        idx = StringUtils.indexOfIgnoreCase(derivedQuery, "DEFAULT CHARACTER SET");
        if (idx != -1) {
            charSet = derivedQuery.substring(idx + "DEFAULT CHARACTER SET".length()).trim();
            idx = getFirstWhitespaceIndex(charSet);
            if (idx > 0)
                charSet = charSet.substring(0, idx);
        }

        URL[] urlDriver = new URL[0];
        Class clazzDriver = null;
        URL[] urls = new URL[0];
        Class clazzdb = null;
        Object odb = null;
        try {
            urlDriver = MiscUtils.loadURLs("./lib/jaybird-3.jar"); // TODO Must use DriverLoader
            ClassLoader clD = new URLClassLoader(urlDriver);
            clazzDriver = clD.loadClass("org.firebirdsql.jdbc.FBDriver");
            Object o = clazzDriver.newInstance();
            Driver driver = (Driver) o;

            Log.info("Database creation via jaybird");
            Log.info("Driver version: " + driver.getMajorVersion() + "." + driver.getMinorVersion());

            if (driver.getMajorVersion() == 2) {
                StringBuilder sb = new StringBuilder();
                sb.append("Cannot create database, Jaybird 2.x has no implementation for creation database.");
                throw new SQLException(sb.toString());
            }

            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
            ClassLoader cl = new URLClassLoader(urls, o.getClass().getClassLoader());
            clazzdb = cl.loadClass("biz.redsoft.FBCreateDatabaseImpl");
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

        IFBCreateDatabase db = (IFBCreateDatabase) odb;
        db.setServer(server);
        db.setPort(Integer.valueOf(port));
        db.setUser(user);
        db.setPassword(password);
        db.setDatabaseName(database);
        db.setEncoding(charSet);
        if (StringUtils.isNotEmpty(pageSize))
            db.setPageSize(Integer.valueOf(pageSize));

        try {
            db.exec();

        } catch (UnsatisfiedLinkError linkError) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot create database, because fbclient library not found in environment path variable. \n");
            sb.append("Please, add fbclient library to environment path variable.\n");
            sb.append("Example for Windows system: setx path \"%path%;C:\\Program Files (x86)\\RedDatabase\\bin\\\"\n\n");
            sb.append("Example for Linux system: export PATH=$PATH:/opt/RedDatabase/lib\n\n");
            sb.append(linkError.getMessage());
            throw new SQLException(sb.toString());
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("The connection to the database could not be established.");
            sb.append("\nPlease ensure all required fields have been entered ");
            sb.append("correctly and try again.\n\nThe system returned:\n");
            sb.append(e.getMessage());
            throw new SQLException(sb.toString());
        }

        DatabaseConnection temp = new DefaultDatabaseConnection();
        temp.setHost(server);
        temp.setPort(port);
        temp.setSourceName(database);
        temp.setUserName(user);
        temp.setPassword(password);
        temp.setCharset(charSet);
        Properties properties = new Properties();
        properties.setProperty("sqlDialect", sqlDialect);
        if (StringUtils.isNotEmpty(charSet))
            properties.setProperty("lc_ctype", charSet);
        temp.setJdbcProperties(properties);
        DatabaseDriver driver = new DefaultDatabaseDriver();
        driver.setPath("./lib/jaybird-3.jar"); // TODO Must use DriverLoader
        driver.setClassName("org.firebirdsql.jdbc.FBDriver");
        driver.setURL("jdbc:firebirdsql://[host]:[port]/[source]");
        temp.setJDBCDriver(driver);

        return new SimpleDataSource(temp);
    }

    private int getFirstWhitespaceIndex(String s) {
        Pattern pat = Pattern.compile("\\s");
        Matcher m = pat.matcher(s);
        if ( m.find() ) {
            return m.start();
        }
        return -1;
    }

    private boolean shouldNotContinue() {

        return Thread.interrupted() || cancel;
    }

    public void close() throws SQLException {

        if (connection != null) {

            connection.close();
        }
    }

    public void rollback() throws SQLException {

        if (connection != null) {

            connection.rollback();
        }
    }

    public void commit() throws SQLException {

        if (connection != null) {

            connection.commit();
        }
    }


}






