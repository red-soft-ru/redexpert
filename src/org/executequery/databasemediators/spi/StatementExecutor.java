/*
 * StatementExecutor.java
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

package org.executequery.databasemediators.spi;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseExecutable;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.sql.SqlStatementResult;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface StatementExecutor {

    /**
     * <p>Executes the specified query (SELECT) and returns
     * a <code>ResultSet</code> object from this query.
     * <p>If an exception occurs, null is returned and
     * the relevant error message, if available, assigned
     * to this object for retrieval.
     *
     * @param the SQL query to execute
     * @return the query result
     */
    SqlStatementResult getResultSet(String query) throws SQLException;

    /**
     * <p>Executes the specified procedure.
     *
     * @param the SQL procedure to execute
     * @return the query result
     */
    SqlStatementResult execute(DatabaseExecutable databaseExecutable) throws SQLException;

    SqlStatementResult execute(int type, String query) throws SQLException;

    SqlStatementResult execute(int type, PreparedStatement statement) throws SQLException;

    SqlStatementResult execute(int type, String query, int fetchSize) throws SQLException;

    SqlStatementResult execute(int type, PreparedStatement statement, int fetchSize) throws SQLException;

    SqlStatementResult execute(String query, boolean enableEscapes) throws SQLException;

    SqlStatementResult execute(PreparedStatement statement, boolean enableEscapes) throws SQLException;

    /**
     * Executes the specified query and returns 0 if this
     * executes successfully. If an exception occurs, -1 is
     * returned and the relevant error message, if available,
     * assigned to this object for retrieval. This will
     * typically be called for a CREATE PROCEDURE/FUNCTION
     * call.
     *
     * @param the SQL query to execute
     * @return the number of rows affected
     */
    SqlStatementResult createProcedure(String query) throws SQLException;

    /**
     * Executes the specified query and returns
     * the number of rows affected by this query.
     * <p>If an exception occurs, -1 is returned and
     * the relevant error message, if available, assigned
     * to this object for retrieval.
     *
     * @param the SQL query to execute
     * @return the number of rows affected
     */
    SqlStatementResult updateRecords(String query) throws SQLException;

    SqlStatementResult executeDDL(String query) throws SQLException;

    SqlStatementResult updateRecords(PreparedStatement statement) throws SQLException;

    SqlStatementResult executeDDL(PreparedStatement statement) throws SQLException;

    /**
     * Destroys the open connection.
     */
    void destroyConnection() throws SQLException;

    /**
     * <p>Sets the connection's commit mode to the
     * specified value.
     *
     * @param true for auto-commit, false otherwise
     */
    void setCommitMode(boolean commitMode);

    /**
     * Cancels the current SQL statement being executed.
     */
    void cancelCurrentStatement();

    /**
     * Closes the database connection of this object.
     */
    void closeConnection() throws SQLException;

    /**
     * Indicates a connection has been closed.
     *
     * @param the connection thats been closed
     */
    void disconnected(DatabaseConnection dc);

    /**
     * Releases database resources held by this class.
     */
    void releaseResources();

    public boolean isCloseConnectionAfterQuery();

    public void setCloseConnectionAfterQuery(boolean closeConnectionAfterQuery);

    void setDatabaseConnection(DatabaseConnection _databaseConnection);

    DatabaseConnection getDatabaseConnection();

    Connection getConnection();

    SqlStatementResult getResultSet(String query, int fetchSize) throws SQLException;

    SqlStatementResult getResultSet(int fetchSize, PreparedStatement statement) throws SQLException;

    SqlStatementResult getResultSet(String query, int fetchSize, List<Parameter> params) throws SQLException;

    int getTransactionIsolation();

    void setTransactionIsolation(int transactionLevel);

    PreparedStatement getPreparedStatement(String query) throws SQLException;

    CallableStatement getCallableStatement(String query) throws SQLException;

}


