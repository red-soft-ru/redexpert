/*
 * DefaultStatementExecutor.java
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.ProcedureParameterSorter;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databaseobjects.DatabaseExecutable;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseSource;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class handles all database query functions
 * such as the execution of SQL SELECT, INSERT, UPDATE
 * etc statements.
 * <p>
 * <p>This class will typically be used by the Database
 * Browser or Query Editor where all SQL statements to be
 * executed will pass through here. In the case of a Query
 * Editor, a dedicated connection is maintained by this class
 * for the editor's use. This was shown to decrease some overhead
 * associated with constantly retrieving conenctions from the
 * pool. Also, when the commit mode is not set to auto-commit
 * within an editor, a dedicated connection is required
 * so as to maintain the correct rollback segment.
 *
 * @author Takis Diakoumis
 */
public class DefaultStatementExecutor implements StatementExecutor, Serializable {


    /**
     * Whether this object is owned by a QueryEditor instance
     */
    private boolean keepAlive;

    /**
     * The connection's commit mode
     */
    private boolean commitMode;

    /**
     * The database connection
     */
    private Connection conn;

    private boolean closeConnectionAfterQuery = true;

    /**
     * The database <code>Statement</code> object
     */
    private Statement stmnt;

    /**
     * The connection use count
     */
    private int useCount = 0;

    /**
     * The specified maximum connection use count
     */
    private int maxUseCount;

    /**
     * the query result object
     */
    private SqlStatementResult statementResult;

    /**
     * the database connection properties object
     */
    private DatabaseConnection databaseConnection;

    /**
     * the transaction isolation level from connection
     */
    private int connectionIsolationLevel;

    /**
     * the isolation level for transaction
     */
    private int transactionIsolation;

    boolean useDatabaseConnection;

    public DefaultStatementExecutor() {
        this(null, false);
    }

    /**
     * Creates a new instance with the specified connection
     * properties object as the connection provider and a keep flag
     * that determines whether connections are retained or closed between
     * requests.
     *
     * @param databaseConnection the connection properties object
     */
    public DefaultStatementExecutor(DatabaseConnection databaseConnection) {
        this(databaseConnection, false);
    }

    /**
     * Creates a new instance with the specified connection
     * properties object as the connection provider and a keep flag
     * that determines whether connections are retained or closed between
     * requests.
     *
     * @param databaseConnection the connection properties object
     * @param keepAlive          whether the connection should be kept between requests
     */
    public DefaultStatementExecutor(DatabaseConnection databaseConnection, boolean keepAlive) {
        this.keepAlive = keepAlive;
        this.databaseConnection = databaseConnection;
        maxUseCount = ConnectionManager.getMaxUseCount();
        statementResult = new SqlStatementResult();
        transactionIsolation = -1;
        setUseDatabaseConnection(true);
    }

    /**
     * <p>Retrieves a description of the specified table using
     * the connection's <code>DatabaseMetaData</code> object
     * and the method <code>getColumns(...)</code>.
     *
     * @param tableName the table name to describe
     * @return the query result
     */
    private SqlStatementResult getTableDescription(String tableName) throws SQLException {

        if (!prepared()) {

            return statementResult;
        }

        DatabaseHost host = null;
        try {

            /* -------------------------------------------------
             * Database meta data values can be case-sensitive.
             * search for a match and use as returned from dmd.
             * -------------------------------------------------
             */

            String name = tableName;
            String catalog = null;
            String schema = null;

            host = new DatabaseObjectFactoryImpl().createDatabaseHost(databaseConnection);

            int nameDelim = tableName.indexOf('.');
            if (nameDelim != -1) {

                name = tableName.substring(nameDelim + 1);
                String value = tableName.substring(0, nameDelim);
                DatabaseMetaData databaseMetaData = host.getDatabaseMetaData();

                if (host.supportsCatalogsInTableDefinitions()) {

                    ResultSet resultSet = databaseMetaData.getCatalogs();
                    while (resultSet.next()) {

                        String _catalog = resultSet.getString(1);
                        if (value.equalsIgnoreCase(_catalog)) {

                            catalog = _catalog;
                            break;
                        }
                    }

                    resultSet.close();

                } else if (host.supportsSchemasInTableDefinitions()) {

                    ResultSet resultSet = databaseMetaData.getCatalogs();
                    while (resultSet.next()) {

                        String _schema = resultSet.getString(1);
                        if (value.equalsIgnoreCase(_schema)) {

                            schema = _schema;
                            break;
                        }
                    }

                    resultSet.close();
                }

            }

            DatabaseMetaData databaseMetaData = host.getDatabaseMetaData();
            ResultSet resultSet = databaseMetaData.getTables(catalog, schema, null, null);

            String nameToSearchOn = null;
            while (resultSet.next()) {

                String _tableName = resultSet.getString(3);
                if (_tableName.equalsIgnoreCase(name)) {

                    nameToSearchOn = _tableName;
                    break;
                }

            }
            resultSet.close();

            if (StringUtils.isNotBlank(nameToSearchOn)) {

                databaseMetaData = conn.getMetaData();
                resultSet = databaseMetaData.getColumns(catalog, schema, nameToSearchOn, null);
                statementResult.setResultSet(resultSet);

            } else {

                statementResult.setMessage("Invalid table name");
            }

        } catch (SQLException e) {

            statementResult.setSqlException(e);
            finished();

        } catch (OutOfMemoryError e) {

            statementResult.setMessage(e.getMessage());
            releaseResources();

        } finally {

            if (host != null) {

                host.close();
            }

        }

        return statementResult;
    }

    private boolean prepared() throws SQLException {

        if (isUseDatabaseConnection() && (databaseConnection == null || !databaseConnection.isConnected())) {

            statementResult.setMessage("Not Connected");
            statementResult.setOtherException(new UndeclaredThrowableException(new Throwable(), "Not Connected"));
            return false;
        }

        // check the connection is valid
        if (conn == null || conn.isClosed()) {

            try {

                if (isUseDatabaseConnection())
                    conn = ConnectionManager.getTemporaryConnection(databaseConnection);
                else throw new DataSourceException("Connection=null or closed");
                if (keepAlive) {

                    try {

                        conn.setAutoCommit(commitMode);

                    } catch (SQLException e) {

                        Log.warning("Error setting default commit mode for statement execution - " + e.getMessage());
                    }
                }

                useCount = 0;

            } catch (DataSourceException e) {

                handleDataSourceException(e);
            }

        } else if (conn.isClosed()) { // check its still open

            statementResult.setMessage("Connection closed.");
            return false;
        }

        statementResult.reset();

        if (conn != null) { // still null?

            conn.clearWarnings();

        } else {

            statementResult.setMessage("Connection closed.");
            return false;
        }
        connectionIsolationLevel = conn.getTransactionIsolation();
        if (transactionIsolation != -1) {
            try {
                conn.setTransactionIsolation(transactionIsolation);
            } catch (Exception e) {
                GUIUtilities.displayExceptionErrorDialog("Error transaction isolation", e);
            }
        }
        return true;
    }

    /**
     * <p>Executes the specified query (SELECT) and returns
     * a <code>ResultSet</code> object from this query.
     * <p>If an exception occurs, null is returned and
     * the relevant error message, if available, assigned
     * to this object for retrieval.
     *
     * @param query the SQL query to execute
     * @return the query result
     */
    @Override
    public SqlStatementResult getResultSet(String query) throws SQLException {

        return getResultSet(query, -1);
    }

    /**
     * <p>Executes the specified query (SELECT) and returns a <code>ResultSet</code> object
     * from this query.
     * <p>
     * <p>If an exception occurs, null is returned and the relevant error message, if available,
     * assigned to this object for retrieval.
     *
     * @param query the SQL query to execute
     * @return the query result
     */
    @Override
    public SqlStatementResult getResultSet(String query, int fetchSize) throws SQLException {

        if (!prepared()) {

            return statementResult;
        }

        stmnt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);

        if (fetchSize != -1) {

            stmnt.setFetchSize(fetchSize);
        }

        // mysql
//        stmnt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
//        stmnt.setFetchSize(Integer.MIN_VALUE);

        try {

            ResultSet rs = stmnt.executeQuery(query);
            statementResult.setResultSet(rs);

            useCount++;

        } catch (SQLException e) {

            statementResult.setSqlException(e);
            finished();
        }

        return statementResult;
    }

    @Override
    public SqlStatementResult getResultSet(int fetchSize, PreparedStatement statement) throws SQLException {
        stmnt = statement;
        if (statement == null || statement.isClosed()) {
            statementResult.setMessage("Statement closed");
            return statementResult;
        }

        if (fetchSize != -1) {

            stmnt.setFetchSize(fetchSize);
        }

        // mysql
//        stmnt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
//        stmnt.setFetchSize(Integer.MIN_VALUE);

        try {

            ResultSet rs = statement.executeQuery();
            statementResult.setResultSet(rs);

            useCount++;

        } catch (SQLException e) {

            statementResult.setSqlException(e);
            finished();
        }

        return statementResult;
    }

    @Override
    public SqlStatementResult getResultSet(String query, int fetchSize, List<Parameter> params) {
        return null;
    }

    /**
     * <p>Executes the specified procedure.
     *
     * @param databaseExecutable the SQL procedure to execute
     * @return the query result
     */
    @Override
    public SqlStatementResult execute(DatabaseExecutable databaseExecutable)
            throws SQLException {

        if (!prepared()) {

            return statementResult;
        }

        ProcedureParameter[] param = databaseExecutable.getParametersArray();
        Arrays.sort(param, new ProcedureParameterSorter());

        String procQuery = null;
        boolean hasOut = false;
        boolean hasParameters = (param != null && param.length > 0);

        List<ProcedureParameter> outs = null;
        List<ProcedureParameter> ins = null;

        if (hasParameters) {

            // split the params into ins and outs
            outs = new ArrayList<ProcedureParameter>();
            ins = new ArrayList<ProcedureParameter>();

            int type = -1;
            for (int i = 0; i < param.length; i++) {
                type = param[i].getType();
                if (type == DatabaseMetaData.procedureColumnIn ||
                        type == DatabaseMetaData.procedureColumnInOut) {

                    // add to the ins list
                    ins.add(param[i]);

                } else if (type == DatabaseMetaData.procedureColumnOut ||
                        type == DatabaseMetaData.procedureColumnResult ||
                        type == DatabaseMetaData.procedureColumnReturn ||
                        type == DatabaseMetaData.procedureColumnUnknown ||
                        type == DatabaseMetaData.procedureColumnInOut) {

                    // add to the outs list
                    outs.add(param[i]);

                }
            }

            char QUESTION_MARK = '?';
            String COMMA = ", ";

            // init the string buffer
            StringBuilder sb = new StringBuilder("{ ");
            if (!outs.isEmpty()) {

                // build the out params place holders
                for (int i = 0, n = outs.size(); i < n; i++) {

                    sb.append(QUESTION_MARK);

                    if (i < n - 1) {

                        sb.append(COMMA);
                    }

                }

                sb.append(" = ");
            }
            sb.append(" call ");

            if (databaseExecutable.supportCatalogOrSchemaInFunctionOrProcedureCalls()) {

                String namePrefix = null;
                if (databaseExecutable.supportCatalogInFunctionOrProcedureCalls()) {

                    namePrefix = databaseExecutable.getCatalogName();

                }
                if (databaseExecutable.supportSchemaInFunctionOrProcedureCalls()) {

                    namePrefix = databaseExecutable.getSchemaName();

                }

                if (namePrefix != null) {

                    sb.append(namePrefix).append('.');
                }
            }

            sb.append(databaseExecutable.getName()).
                    append("( ");

            // build the ins params place holders
            for (int i = 0, n = ins.size(); i < n; i++) {
                sb.append(QUESTION_MARK);
                if (i < n - 1) {
                    sb.append(COMMA);
                }
            }

            sb.append(" ) }");

            // determine if we have out params
            hasOut = !(outs.isEmpty());
            procQuery = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("{ call ");

            if (databaseExecutable.getSchemaName() != null) {
                sb.append(databaseExecutable.getSchemaName()).
                        append('.');
            }

            sb.append(databaseExecutable.getName()).
                    append("( ) }");

            procQuery = sb.toString();
        }

        //Log.debug(procQuery);

        // null value literal
        String NULL = "null";

        // clear any warnings
        conn.clearWarnings();

        Log.info("Executing: " + procQuery);

        CallableStatement cstmnt = null;
        try {
            // prepare the statement
            cstmnt = conn.prepareCall(procQuery);
            stmnt = cstmnt;
        } catch (SQLException e) {
            handleException(e);
            statementResult.setSqlException(e);
            return statementResult;
        }

        // check if we are passing parameters
        if (hasParameters) {
            // the parameter index counter
            int index = 1;

            // the java.sql.Type value
            int dataType = -1;

            // the parameter input value
            String value = null;

            // register the out params
            for (int i = 0, n = outs.size(); i < n; i++) {
                //Log.debug("setting out at index: " + index);
                cstmnt.registerOutParameter(index, outs.get(i).getDataType());
                index++;
            }

            try {

                // register the in params
                for (int i = 0, n = ins.size(); i < n; i++) {

                    ProcedureParameter procedureParameter = ins.get(i);
                    value = procedureParameter.getValue();
                    dataType = procedureParameter.getDataType();

                    // try infer a type if OTHER
                    if (dataType == Types.OTHER) {

                        // checking only for bit/bool for now

                        if (isTrueFalse(value)) {

                            dataType = Types.BOOLEAN;

                        } else if (isBit(value)) {

                            dataType = Types.BIT;
                            value = value.substring(2, value.length() - 1);
                        }

                    }

                    if (MiscUtils.isNull(value) || value.equalsIgnoreCase(NULL)) {

                        cstmnt.setNull(index, dataType);

                    } else {

                        switch (dataType) {

                            case Types.TINYINT:
                                byte _byte = Byte.valueOf(value).byteValue();
                                cstmnt.setShort(index, _byte);
                                break;

                            case Types.SMALLINT:
                                short _short = Short.valueOf(value).shortValue();
                                cstmnt.setShort(index, _short);
                                break;

                            case Types.LONGVARCHAR:
                            case Types.CHAR:
                            case Types.VARCHAR:
                                cstmnt.setString(index, value);
                                break;

                            case Types.BIT:
                            case Types.BOOLEAN:

                                boolean _boolean = false;
                                if (NumberUtils.isNumber(value)) {

                                    int number = Integer.valueOf(value);
                                    if (number > 0) {

                                        _boolean = true;
                                    }

                                } else {

                                    _boolean = Boolean.valueOf(value).booleanValue();
                                }

                                cstmnt.setBoolean(index, _boolean);
                                break;

                            case Types.BIGINT:
                                long _long = Long.valueOf(value).longValue();
                                cstmnt.setLong(index, _long);
                                break;

                            case Types.INTEGER:
                                int _int = Integer.valueOf(value).intValue();
                                cstmnt.setInt(index, _int);
                                break;

                            case Types.REAL:
                                float _float = Float.valueOf(value).floatValue();
                                cstmnt.setFloat(index, _float);
                                break;

                            case Types.NUMERIC:
                            case Types.DECIMAL:
                                cstmnt.setBigDecimal(index, new BigDecimal(value));
                                break;
    /*
                      case Types.DATE:
                      case Types.TIMESTAMP:
                      case Types.TIME:
                        cstmnt.setTimestamp(index, new Timestamp( BigDecimal(value));
    */
                            case Types.FLOAT:
                            case Types.DOUBLE:
                                double _double = Double.valueOf(value).doubleValue();
                                cstmnt.setDouble(index, _double);
                                break;

                            default:
                                cstmnt.setObject(index, value);

                        }

                    }

                    // increment the index
                    index++;
                }

            } catch (Exception e) {

                statementResult.setOtherErrorMessage(e.getClass().getName() + ": " + e.getMessage());
                return statementResult;
            }

        }

        /*
        test creating function for postgres:

        CREATE FUNCTION concat_lower_or_upper(a text, b text, uppercase boolean DEFAULT false)
        RETURNS text
        AS
        $$
         SELECT CASE
                WHEN $3 THEN UPPER($1 || ' ' || $2)
                ELSE LOWER($1 || ' ' || $2)
                END;
        $$
        LANGUAGE SQL IMMUTABLE STRICT;
        */

        try {
            cstmnt.clearWarnings();
            boolean hasResultSet = cstmnt.execute();
            Map<String, Object> results = new HashMap<String, Object>();

            if (hasOut) {
                // incrementing index
                int index = 1;

                // return value from each registered out
                String returnValue = null;

                for (int i = 0; i < param.length; i++) {

                    int type = param[i].getType();
                    int dataType = param[i].getDataType();

                    if (type == DatabaseMetaData.procedureColumnOut ||
                            type == DatabaseMetaData.procedureColumnResult ||
                            type == DatabaseMetaData.procedureColumnReturn ||
                            type == DatabaseMetaData.procedureColumnUnknown ||
                            type == DatabaseMetaData.procedureColumnInOut) {

                        switch (dataType) {

                            case Types.TINYINT:
                                returnValue = Byte.toString(cstmnt.getByte(index));
                                break;

                            case Types.SMALLINT:
                                returnValue = Short.toString(cstmnt.getShort(index));
                                break;

                            case Types.LONGVARCHAR:
                            case Types.CHAR:
                            case Types.VARCHAR:
                                returnValue = cstmnt.getString(index);
                                break;

                            case Types.BIT:
                            case Types.BOOLEAN:
                                returnValue = Boolean.toString(cstmnt.getBoolean(index));
                                break;

                            case Types.INTEGER:
                                returnValue = Integer.toString(cstmnt.getInt(index));
                                break;

                            case Types.BIGINT:
                                returnValue = Long.toString(cstmnt.getLong(index));
                                break;

                            case Types.REAL:
                                returnValue = Float.toString(cstmnt.getFloat(index));
                                break;

                            case Types.NUMERIC:
                            case Types.DECIMAL:
                                returnValue = cstmnt.getBigDecimal(index).toString();
                                break;

                            case Types.DATE:
                            case Types.TIME:
                            case Types.TIMESTAMP:
                                returnValue = cstmnt.getDate(index).toString();
                                break;

                            case Types.FLOAT:
                            case Types.DOUBLE:
                                returnValue = Double.toString(cstmnt.getDouble(index));
                                break;

                        }

                        if (returnValue == null) {
                            returnValue = "NULL";
                        }

                        results.put(param[i].getName(), returnValue);
                        index++;
                    }

                }

            }

            if (!hasResultSet) {

                statementResult.setUpdateCount(cstmnt.getUpdateCount());

            } else {

                statementResult.setResultSet(cstmnt.getResultSet());
            }

            useCount++;
            statementResult.setOtherResult(results);

        } catch (SQLException e) {

            statementResult.setSqlException(e);

        } catch (Exception e) {

            statementResult.setMessage(e.getMessage());
        }

        return statementResult;
    }

    private boolean isBit(String value) {

        String toLower = value.toLowerCase();
        return Pattern.compile("b'\\d+'").matcher(toLower).find();
    }

    private boolean isTrueFalse(String value) {

        String toLower = value.toLowerCase();
        return "true".equals(toLower) || "false".equals(toLower);
    }

    /**
     * <p>Executes the specified procedure and returns
     * a <code>ResultSet</code> object from this query.
     * <p>If an exception occurs, null is returned and
     * the relevant error message, if available, assigned
     * to this object for retrieval.
     *
     * @param query the SQL procedure to execute
     * @return the query result
     */
    private SqlStatementResult executeProcedure(String query) throws SQLException {

        if (!prepared()) {

            return statementResult;
        }

        //Log.debug("query " + query);

        String execString = "EXECUTE ";
        String callString = "CALL ";

        int nameIndex = -1;
        int index = query.toUpperCase().indexOf(execString);

        // check if EXECUTE was entered
        if (index != -1) {

            nameIndex = execString.length();

        } else { // must be CALL

            nameIndex = callString.length();
        }

        String procedureName = null;

        // check for input brackets
        boolean possibleParams = false;
        index = query.indexOf("(", nameIndex);
        if (index != -1) {

            possibleParams = true;
            procedureName = query.substring(nameIndex, index);

        } else {

            procedureName = query.substring(nameIndex);
        }

        String prefix = prefixFromName(procedureName);
        procedureName = suffixFromName(procedureName);

        DatabaseHost host = new DatabaseObjectFactoryImpl().createDatabaseHost(databaseConnection);

        if (prefix == null) {

            prefix = host.getDefaultNamePrefix();
        }

        DatabaseExecutable procedure = host.getDatabaseSource(prefix).getProcedure(procedureName);
        if (procedure == null) { // hedge

            procedure = host.getDatabaseSource(prefix).getFunction(procedureName);
        }

        if (procedure != null) {

            if (possibleParams) {

                String params = query.substring(index + 1, query.indexOf(")"));

                if (!MiscUtils.isNull(params)) {

                    // check that the proc accepts params
//                    if (!procedure.hasParameters()) {
//
//                        statementResult.setSqlException(new SQLException("Procedure call was invalid"));
//                        return statementResult;
//                    }

                    int paramIndex = 0;
                    ProcedureParameter[] parameters = procedure.getParametersArray();

                    // extract the parameters
                    StringTokenizer st = new StringTokenizer(params, ",");

                    // no defined params from the meta data but params supplied ??
                    // attempt to execute as supplied and bubble up db error if an issue
                    if (parameters.length == 0) {

                        parameters = new ProcedureParameter[st.countTokens()];
                        for (int i = 0, n = st.countTokens(); i < n; i++) {

                            procedure.addParameter("UNKNOWN", DatabaseMetaData.procedureColumnIn, Types.OTHER, "OTHER", -1, 0);
                        }

                        parameters = procedure.getParametersArray();
                    }

                    while (st.hasMoreTokens()) {

                        String value = st.nextToken().trim();

                        // check applicable param
                        for (int i = paramIndex; i < parameters.length; i++) {
                            paramIndex++;

                            int type = parameters[i].getType();
                            if (type == DatabaseMetaData.procedureColumnIn ||
                                    type == DatabaseMetaData.procedureColumnInOut) {

                                // check the data type and remove quotes if char
                                int dataType = parameters[i].getDataType();
                                if (dataType == Types.CHAR ||
                                        dataType == Types.VARCHAR ||
                                        dataType == Types.LONGVARCHAR) {

                                    if (value.indexOf("'") != -1) {
                                        // assuming quotes at start and end
                                        value = value.substring(1, value.length() - 1);
                                    }

                                }

                                parameters[i].setValue(value);
                                break;
                            }
                        }

                    }

                }
            }

            // execute the procedure
            return execute(procedure);

        } else {

            // just run it...

            CallableStatement cstmnt = null;
            try {

                cstmnt = conn.prepareCall(query);
                boolean hasResultSet = cstmnt.execute();

                if (!hasResultSet) {

                    statementResult.setUpdateCount(cstmnt.getUpdateCount());

                } else {

                    statementResult.setResultSet(cstmnt.getResultSet());
                }


            } catch (SQLException e) {

                handleException(e);
                statementResult.setSqlException(e);
            }

            return statementResult;
            /*

            statementResult.setSqlException(
                    new SQLException("Procedure or Function name specified is invalid"));

            return statementResult;

            */
        }

    }

    private String suffixFromName(String procedureName) {

        int index = procedureName.indexOf('.');
        if (index != -1) {

            return procedureName.substring(index + 1);
        }

        return procedureName;
    }

    private String prefixFromName(String procedureName) {

        int index = procedureName.indexOf('.');
        if (index != -1) {

            return procedureName.substring(0, index);
        }

        return null;
    }

    @Override
    public SqlStatementResult execute(int type, String query) throws SQLException {

        return execute(type, query, -1);
    }

    @Override
    public SqlStatementResult execute(int type, PreparedStatement statement) throws SQLException {
        return execute(type, statement, -1);
    }

    boolean autoddl = true;

    @Override
    public SqlStatementResult execute(int type, String query, int fetchSize) throws SQLException {

        statementResult.setType(type);

        switch (type) {

            case QueryTypes.SELECT:
            case QueryTypes.EXPLAIN:
                return getResultSet(query, fetchSize);
            case QueryTypes.INSERT:
            case QueryTypes.UPDATE:
            case QueryTypes.DELETE:
                return updateRecords(query);
            case QueryTypes.DROP_TABLE:
            case QueryTypes.CREATE_TABLE:
            case QueryTypes.ALTER_TABLE:
            case QueryTypes.CREATE_SEQUENCE:
            case QueryTypes.CREATE_FUNCTION:
            case QueryTypes.CREATE_PROCEDURE:
            case QueryTypes.GRANT:
            case QueryTypes.CREATE_SYNONYM:
            case QueryTypes.CREATE_ROLE:
            case QueryTypes.REVOKE:
            case QueryTypes.DROP_OBJECT:
            case QueryTypes.COMMENT:
            case QueryTypes.CREATE_TRIGGER:
            case QueryTypes.CREATE_OBJECT:
            case QueryTypes.RECREATE_OBJECT:
            case QueryTypes.CREATE_OR_ALTER:
            case QueryTypes.ALTER_OBJECT:
                return executeDDL(query);

            case QueryTypes.UNKNOWN:
            case QueryTypes.SELECT_INTO:
                return execute(query);

            case QueryTypes.DESCRIBE:
                int tableNameIndex = query.indexOf(" ");
                return getTableDescription(query.substring(tableNameIndex + 1));

            case QueryTypes.EXECUTE:
                //return execute(query);
                return executeProcedure(query);

            case QueryTypes.COMMIT:
                return commitLast(true);

            case QueryTypes.ROLLBACK:
                return commitLast(false);

            case QueryTypes.SHOW_TABLES:
                return showTables();
            case QueryTypes.SET_AUTODDL_ON:
                return setAutoDDL(true);
            case QueryTypes.SET_AUTODDL_OFF:
                return setAutoDDL(false);
            /*
            case CONNECT:
                return establishConnection(query.toUpperCase());
             */
        }
        return statementResult;
    }

    private SqlStatementResult execute(String query) throws SQLException {

        return execute(query, true);
    }

    private SqlStatementResult execute(PreparedStatement statement) throws SQLException {

        return execute(statement, true);
    }

    private SqlStatementResult showTables() throws SQLException {

        DatabaseHost host = null;
        try {

            host = new DatabaseObjectFactoryImpl().createDatabaseHost(databaseConnection);

            DatabaseSource defaultCatalog = host.getDefaultCatalog();
            DatabaseSource defaultSchema = host.getDefaultSchema();

            String catalog = null;
            String schema = null;

            if (defaultCatalog != null) {

                catalog = defaultCatalog.getName();
            }

            if (defaultSchema != null) {

                schema = defaultSchema.getName();
            }

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getTables(catalog, schema, null, new String[]{"TABLE"});
            statementResult.setResultSet(resultSet);

        } catch (SQLException e) {

            statementResult.setSqlException(e);
            finished();

        } finally {

            if (host != null) {

                host.close();
            }
        }

        return statementResult;
    }


    @Override
    public SqlStatementResult execute(String query, boolean enableEscapes)
            throws SQLException {

        if (!prepared()) {
            return statementResult;
        }

        stmnt = conn.createStatement();
        boolean isResultSet = false;

        try {

            setStatementEscapeProcessing(stmnt, enableEscapes);
            isResultSet = stmnt.execute(query);

            if (isResultSet) {

                ResultSet rs = stmnt.getResultSet();
                statementResult.setResultSet(rs);

            } else {

                int updateCount = stmnt.getUpdateCount();
                if (updateCount == -1) {

                    updateCount = -10000;
                }

                statementResult.setUpdateCount(updateCount);
            }

            useCount++;
            statementResult.setSqlWarning(stmnt.getWarnings());
            return statementResult;

        } catch (SQLException e) {

            statementResult.setSqlException(e);

        } finally {

            if (!isResultSet) {

                finished();
            }

        }

        return statementResult;
    }

    @Override
    public SqlStatementResult execute(PreparedStatement statement, boolean enableEscapes)
            throws SQLException {

        stmnt = statement;
        if (statement == null || statement.isClosed()) {
            statementResult.setMessage("Statement closed");
            return statementResult;
        }
        boolean isResultSet = false;

        try {

            setStatementEscapeProcessing(stmnt, enableEscapes);
            isResultSet = statement.execute();

            if (isResultSet) {

                ResultSet rs = stmnt.getResultSet();
                statementResult.setResultSet(rs);

            } else {

                int updateCount = stmnt.getUpdateCount();
                if (updateCount == -1) {

                    updateCount = -10000;
                }

                statementResult.setUpdateCount(updateCount);
            }

            useCount++;
            statementResult.setSqlWarning(stmnt.getWarnings());
            return statementResult;

        } catch (SQLException e) {

            statementResult.setSqlException(e);

        } finally {

            if (!isResultSet) {

                finished();
            }

        }

        return statementResult;
    }


    private void setStatementEscapeProcessing(Statement statement, boolean enableEscapes) {

        try {

            statement.setEscapeProcessing(enableEscapes);

        } catch (SQLException e) {

            Log.warning("Attempt to set statement escape processing failed - " + e.getMessage());
        }
    }

    /**
     * <p>Executes the specified query and returns 0 if this
     * executes successfully. If an exception occurs, -1 is
     * returned and the relevant error message, if available,
     * assigned to this object for retrieval. This will
     * typically be called for a CREATE PROCEDURE/FUNCTION
     * call.
     *
     * @param query the SQL query to execute
     * @return the number of rows affected
     */
    @Override
    public SqlStatementResult createProcedure(String query) throws SQLException {

        if (!prepared()) {

            return statementResult;
        }

        stmnt = conn.createStatement();

        try {

            stmnt.clearWarnings();
            setStatementEscapeProcessing(stmnt, false);

            boolean isResultSet = stmnt.execute(query);

            if (!isResultSet) {
                int updateCount = stmnt.getUpdateCount();

                if (updateCount == -1)
                    updateCount = -10000;

                statementResult.setUpdateCount(updateCount);

            } else { // should never be a result set

                ResultSet rs = stmnt.getResultSet();
                statementResult.setResultSet(rs);
            }

            useCount++;
            statementResult.setSqlWarning(stmnt.getWarnings());
        } catch (SQLException e) {

            statementResult.setSqlException(e);

        } finally {

            finished();
        }

        return statementResult;
    }


    private void finished() throws SQLException {

        if (stmnt != null) {
            if (!stmnt.isClosed())
                stmnt.close();
        }

        closeConnection(conn);
    }

    /**
     * <p>Executes the specified query and returns
     * the number of rows affected by this query.
     * <p>If an exception occurs, -1 is returned and
     * the relevant error message, if available, assigned
     * to this object for retrieval.
     *
     * @param query the SQL query to execute
     * @return the number of rows affected
     */
    @Override
    public SqlStatementResult updateRecords(String query) throws SQLException {

        if (!prepared()) {
            return statementResult;
        }

        stmnt = conn.createStatement();

        try {
            int result = stmnt.executeUpdate(query);
            statementResult.setUpdateCount(result);
            useCount++;
        } catch (SQLException e) {
            statementResult.setSqlException(e);
        } finally {
            finished();
        }

        return statementResult;

    }

    @Override
    public SqlStatementResult execute(int type, PreparedStatement statement, int fetchSize) throws SQLException {
        statementResult.reset();
        statementResult.setType(type);

        switch (type) {

            case QueryTypes.SELECT:
            case QueryTypes.EXPLAIN:
                return getResultSet(fetchSize, statement);
            case QueryTypes.INSERT:
            case QueryTypes.UPDATE:
            case QueryTypes.DELETE:
                return updateRecords(statement);
            case QueryTypes.DROP_TABLE:
            case QueryTypes.CREATE_TABLE:
            case QueryTypes.ALTER_TABLE:
            case QueryTypes.CREATE_SEQUENCE:
            case QueryTypes.CREATE_FUNCTION:
            case QueryTypes.CREATE_PROCEDURE:
            case QueryTypes.GRANT:
            case QueryTypes.CREATE_SYNONYM:
            case QueryTypes.CREATE_ROLE:
            case QueryTypes.REVOKE:
            case QueryTypes.DROP_OBJECT:
            case QueryTypes.COMMENT:
            case QueryTypes.CREATE_TRIGGER:
            case QueryTypes.CREATE_OBJECT:
            case QueryTypes.RECREATE_OBJECT:
            case QueryTypes.CREATE_OR_ALTER:
            case QueryTypes.ALTER_OBJECT:
                return executeDDL(statement);

            case QueryTypes.UNKNOWN:
            case QueryTypes.SELECT_INTO:
            case QueryTypes.EXECUTE:
            case QueryTypes.CALL:
                return execute(statement);

            case QueryTypes.COMMIT:
                return commitLast(true);

            case QueryTypes.ROLLBACK:
                return commitLast(false);

            case QueryTypes.SET_AUTODDL_ON:
                return setAutoDDL(true);
            case QueryTypes.SET_AUTODDL_OFF:
                return setAutoDDL(false);

            case QueryTypes.SHOW_TABLES:
                return showTables();


            /*
            case CONNECT:
                return establishConnection(query.toUpperCase());
             */
        }
        return statementResult;
    }

    @Override
    public SqlStatementResult updateRecords(PreparedStatement statement) throws SQLException {
        stmnt = statement;
        if (statement == null || statement.isClosed()) {
            statementResult.setMessage("Statement closed");
            return statementResult;
        }

        try {
            int result = statement.executeUpdate();
            statementResult.setUpdateCount(result);
            useCount++;
        } catch (SQLException e) {
            statementResult.setSqlException(e);
        } finally {
            //finished();
        }

        return statementResult;
    }

    public SqlStatementResult executeDDL(String query) throws SQLException {

        if (!prepared()) {
            return statementResult;
        }

        stmnt = conn.createStatement();

        try {
            int result = stmnt.executeUpdate(query);
            statementResult.setUpdateCount(result);
            useCount++;
        } catch (SQLException e) {
            statementResult.setSqlException(e);
        } finally {
            finished();
        }

        return statementResult;

    }

    /*
    public SqlStatementResult establishConnection(String query) {
        statementResult.reset();
        String connectString = "CONNECT ";
        int index = query.indexOf("CONNECT ") + connectString.length();
        String name = query.substring(index).trim();
        DatabaseConnection dc = ConnectionProperties.getDatabaseConnection(name, true);

        if (dc == null) {
            statementResult.setMessage("The connection does not exist");
        }

        return statementResult;
    }
     */

    /**
     * <p>Commits or rolls back the last executed
     * SQL query or queries.
     *
     * @param commit true to commit - false to roll back
     */
    private SqlStatementResult commitLast(boolean commit) {

        statementResult.reset();
        statementResult.setUpdateCount(0);
        if (conn != null)
            try {

                if (!conn.isClosed()) {


                    if (conn.getAutoCommit()) {

                        statementResult.setSqlWarning(new SQLWarning("Auto-Commit is set true"));
                        return statementResult;
                    }

                    if (commit) {

                        conn.commit();
                        Log.info("Commit complete.");
                        statementResult.setMessage("Commit complete.");
                        //closeMaxedConn();

                    } else {

                        conn.rollback();
                        Log.info("Rollback complete.");
                        statementResult.setMessage("Rollback complete.");
                        //closeMaxedConn();
                    }

                } else {

                    statementResult.setSqlException(new SQLException("Connection is closed"));
                }

            } catch (SQLException e) {
                handleException(e);
                statementResult.setSqlException(e);
            }
        return statementResult;

    }

    public SqlStatementResult executeDDL(PreparedStatement statement) throws SQLException {
        stmnt = statement;
        if (statement == null || statement.isClosed()) {
            statementResult.setMessage("Statement closed");
            return statementResult;
        }

        try {
            int result = statement.executeUpdate();
            statementResult.setUpdateCount(result);
            useCount++;
        } catch (SQLException e) {
            statementResult.setSqlException(e);
        } finally {
            if (isAutoDDL())
                commitLast(true);
        }

        return statementResult;
    }

    public boolean isAutoDDL() {
        return autoddl;
    }

    private SqlStatementResult setAutoDDL(boolean autocommit) {

        statementResult.reset();
        statementResult.setUpdateCount(0);
        if (conn != null)
            try {

                if (!conn.isClosed()) {


                    autoddl = autocommit;
                    Log.info("Set autoddl " + autocommit);
                    statementResult.setMessage("Set autoddl " + autocommit);

                } else {

                    statementResult.setSqlException(new SQLException("Connection is closed"));
                }

            } catch (SQLException e) {
                handleException(e);
                statementResult.setSqlException(e);
            }
        return statementResult;

    }

    /**
     * <p>Closes a connection which has reached its
     * maximum use count and retrieves a new one from
     * the <code>DBConnection</code> object.
     */
    private void closeMaxedConn() throws SQLException {
        if (keepAlive && useCount > maxUseCount) {
            destroyConnection();
        }
    }

    /**
     * Destroys the open connection.
     */
    @Override
    public void destroyConnection() throws SQLException {
        try {
            ConnectionManager.close(databaseConnection, conn);
            conn = null;
        } catch (DataSourceException e) {
            handleDataSourceException(e);
        }
    }

    /**
     * <p>Sets the connection's commit mode to the
     * specified value.
     *
     * @param commitMode for auto-commit, false otherwise
     */
    @Override
    public void setCommitMode(boolean commitMode) {
        this.commitMode = commitMode;
        //Log.debug("commitMode: " + commitMode);
        try {
            if (keepAlive && (conn != null && !conn.isClosed())) {
                conn.setAutoCommit(commitMode);
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * Cancels the current SQL statement being executed.
     */
    @Override
    public void cancelCurrentStatement() {

        Log.info("Attempting to cancel the current statement...");

        if (stmnt != null) {

            try {
                stmnt.cancel();
                stmnt.close();
                stmnt = null;

                Log.info("Statement cancelled");

                closeConnection(conn);
                statementResult.setMessage("Statement cancelled.");

            } catch (SQLException e) {

                handleException(e);
            }

        }
    }

    /**
     * <p>Closes the specified database connection.
     * <p>If the specified connection is NULL, the open
     * connection held by this class is closed.
     *
     * @param c connection to close
     */
    private void closeConnection(Connection c) {
        try {
            // if this not the connection assigned to this object
            if (c != null && c != conn) {
                c.close();
                c = null;
            } else { // otherwise proceed to close
                closeConnection();
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }

    /**
     * Close the database connection of this object.
     * If destroy is true, the connection will be
     * closed using connection.close(). Otherwise,
     * the value of keepAlive for this instance will
     * be respected.
     *
     * @param destroy to call close() on the connection object
     */
    private void closeConnection(boolean destroy) {
        if (destroy) {
            try {
                if (conn != null) {
                    conn.close();
                }
                conn = null;
            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    private void handleException(Throwable e) {
        if (Log.isDebugEnabled()) {
            e.printStackTrace();
        }

        Log.error(e.getMessage(), e);
    }

    /**
     * Closes the database connection of this object.
     */
    @Override
    public void closeConnection() {
        // if set to keep the connection open
        // for this instance - return
        if (keepAlive) {
            return;
        }
        // otherwise close it
        closeConnection(true);
    }

    /**
     * Indicates a connection has been closed.
     *
     * @param dc connection thats been closed
     */
    @Override
    public void disconnected(DatabaseConnection dc) {
        if (databaseConnection == dc) {
            closeConnection(true);
            databaseConnection = null;
        }
    }

    /**
     * Handles a DataSourceException by rethrowing as a
     * SQLException.
     */
    private void handleDataSourceException(DataSourceException e)
            throws SQLException {
        if (e.getCause() instanceof SQLException) {
            throw (SQLException) e.getCause();
        } else {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * <p>Releases database resources held by this class.
     */
    @Override
    public void releaseResources() {

        try {

            if (stmnt != null) {

                if (!stmnt.isClosed())
                    stmnt.close();
            }
            stmnt = null;
            if (conn != null) {

                if (useDatabaseConnection && closeConnectionAfterQuery) {
                    conn.close();
                    conn = null;
                }

            }

        } catch (SQLException e) {

            handleException(e);
        }

    }

    @Override
    public void setDatabaseConnection(DatabaseConnection _databaseConnection) {
        if (databaseConnection != _databaseConnection) {
            try {
                // close the current connection
                if (databaseConnection != null && conn != null) {
                    ConnectionManager.close(databaseConnection, conn);
                    conn = null;
                }
                // reassign the connection
                databaseConnection = _databaseConnection;
                prepared();
                useCount = 0;
            } catch (DataSourceException e) {
            } catch (SQLException e) {
                handleException(e);
            }
        }
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public Connection getConnection() {
        return conn;
    }

    public int getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(int transactionLevel) {
        this.transactionIsolation = transactionLevel;
    }

    @Override
    public PreparedStatement getPreparedStatement(String query) throws SQLException {
        if (!prepared()) {

            return null;
        }

        stmnt = conn.prepareStatement(query);

        return (PreparedStatement) stmnt;
    }

    public CallableStatement getCallableStatement(String query) throws SQLException {
        if (!prepared()) {

            return null;
        }

        stmnt = conn.prepareCall(query);

        return (CallableStatement) stmnt;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isUseDatabaseConnection() {
        return useDatabaseConnection;
    }

    public void setUseDatabaseConnection(boolean useDatabaseConnection) {
        this.useDatabaseConnection = useDatabaseConnection;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public boolean isCloseConnectionAfterQuery() {
        return closeConnectionAfterQuery;
    }

    public void setCloseConnectionAfterQuery(boolean closeConnectionAfterQuery) {
        this.closeConnectionAfterQuery = closeConnectionAfterQuery;
    }
}




