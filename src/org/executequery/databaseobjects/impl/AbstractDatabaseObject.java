/*
 * AbstractDatabaseObject.java
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

package org.executequery.databaseobjects.impl;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.*;
import org.executequery.datasource.PooledConnection;
import org.executequery.datasource.PooledStatement;
import org.executequery.sql.sqlbuilder.Condition;
import org.executequery.sql.sqlbuilder.Field;
import org.executequery.sql.sqlbuilder.Function;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.Log;

import java.sql.*;
import java.util.List;

/**
 * Abstract database object implementation.
 *
 * @author Takis Diakoumis
 */
public abstract class AbstractDatabaseObject extends AbstractNamedObject
        implements DatabaseObject {

    /**
     * the host parent object
     */
    private DatabaseHost host;

    /**
     * the catalog name
     */
    private String catalogName;

    /**
     * the schema name
     */
    private String schemaName;

    /**
     * the object's remarks
     */
    private String remarks;

    /**
     * this objects columns
     */
    private List<DatabaseColumn> columns;

    /**
     * the data row count
     */
    private int dataRowCount = -1;

    /**
     * statement object for open queries
     */
    private Statement statement;

    protected DatabaseMetaTag metaTagParent;

    protected String source;

    protected final static String RELATION_NAME = "RELATION_NAME";
    protected final static String FIELD_NAME = "FIELD_NAME";
    protected final static String FIELD_TYPE = "FIELD_TYPE";
    protected final static String FIELD_SUB_TYPE = "FIELD_SUB_TYPE";
    protected final static String SEGMENT_LENGTH = "SEGMENT_LENGTH";
    protected final static String FIELD_PRECISION = "FIELD_PRECISION";
    protected final static String FIELD_SCALE = "FIELD_SCALE";
    protected final static String FIELD_LENGTH = "FIELD_LENGTH";
    protected final static String CHARACTER_LENGTH = "CHAR_LEN";
    protected final static String DESCRIPTION = "DESCRIPTION";
    protected final static String DEFAULT_SOURCE = "DEFAULT_SOURCE";
    protected final static String DOMAIN_DEFAULT_SOURCE = "DOMAIN_DEFAULT_SOURCE";
    protected final static String FIELD_POSITION = "FIELD_POSITION";
    protected final static String NULL_FLAG = "NULL_FLAG";
    protected final static String DOMAIN_NULL_FLAG = "DOMAIN_NULL_FLAG";
    protected final static String COMPUTED_BLR = "COMPUTED_BLR";
    protected final static String IDENTITY_TYPE = "IDENTITY_TYPE";
    protected final static String CHARACTER_SET_ID = "CHARACTER_SET_ID";
    protected final static String CHARACTER_SET_NAME = "CHARACTER_SET_NAME";
    protected final static String COLLATION_NAME = "COLLATION_NAME";
    protected final static String FIELD_SOURCE = "FIELD_SOURCE";
    protected final static String COMPUTED_SOURCE = "COMPUTED_SOURCE";
    protected final static String KEY_SEQ = "KEY_SEQ";
    protected final static String CONSTRAINT_NAME = "CONSTRAINT_NAME";
    protected final static String CONSTRAINT_TYPE = "CONSTRAINT_TYPE";
    protected final static String REF_TABLE = "REF_TABLE";
    protected final static String REF_COLUMN = "REF_COLUMN";
    protected final static String UPDATE_RULE = "UPDATE_RULE";
    protected final static String DELETE_RULE = "DELETE_RULE";
    protected static final String ENGINE_NAME = "ENGINE_NAME";
    protected static final String ENTRYPOINT = "ENTRYPOINT";
    protected static final String SQL_SECURITY = "SQL_SECURITY";
    protected static final String DIMENSIONS = "DIMENSIONS";
    protected static final String DEFAULT_COLLATE_NAME = "DEFAULT_COLLATE_NAME";


    public AbstractDatabaseObject(DatabaseHost host) {
        setHost(host);
    }

    public AbstractDatabaseObject(DatabaseMetaTag metaTagParent) {
        this(metaTagParent.getHost());
        this.metaTagParent = metaTagParent;
    }

    public AbstractDatabaseObject(DatabaseMetaTag metaTagParent, String name) {
        this(metaTagParent);
        setMarkedForReload(true);
        setName(name);
    }

    /**
     * Returns the catalog name parent to this database object.
     *
     * @return the catalog name
     */
    @Override
    public String getCatalogName() {
        return catalogName;
    }

    /**
     * Sets the parent catalog name to that specified.
     *
     * @param catalog the catalog name
     */
    @Override
    public void setCatalogName(String catalog) {
        this.catalogName = catalog;
    }

    /**
     * Returns the schema name parent to this database object.
     *
     * @return the schema name
     */
    @Override
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Sets the parent schema name to that specified.
     *
     * @param schema the schema name
     */
    @Override
    public void setSchemaName(String schema) {
        this.schemaName = schema;
    }

    @Override
    public String getNamePrefix() {

        String _schema = getSchemaName();

        if (StringUtils.isNotBlank(_schema))
            return _schema;

        return getCatalogName(); // may still be null
    }


    /**
     * Returns the column from this table witt the specified name,
     * or null if the column does not exist.
     *
     * @return the table column
     */
    public DatabaseColumn getColumn(String name) throws DataSourceException {

        List<DatabaseColumn> columns = getColumns();
        for (DatabaseColumn column : columns)
            if (column.getName().equalsIgnoreCase(name))
                return column;

        return null;
    }

    protected boolean sqlSecurityCheck() {
        return getDatabaseMajorVersion() >= 3 && isRDB()
                || getDatabaseMajorVersion() >= 4 && !isRDB();
    }

    protected boolean externalCheck() {
        return getDatabaseMajorVersion() >= 3;
    }

    protected boolean tablespaceCheck() {
        return getHost().getDatabaseProductName().toLowerCase().contains("reddatabase")
                && getDatabaseMajorVersion() >= 4;
    }

    protected boolean isRDB() {
        return getHost().getDatabaseProductName().toLowerCase().contains("reddatabase");
    }

    protected boolean moreOrEqualsVersionCheck(int major, int minor) {
        return getDatabaseMajorVersion() > major || getDatabaseMajorVersion() == major && getDatabaseMinorVersion() >= minor;
    }


    protected Field buildSqlSecurityField(Table table) {
        Field sqlSecurity = Field.createField(table, SQL_SECURITY);
        sqlSecurity.setStatement(Function.createFunction("IIF")
                .appendArgument(sqlSecurity.getFieldTable() + " IS NULL").appendArgument("NULL").appendArgument(Function.createFunction().setName("IIF")
                        .appendArgument(sqlSecurity.getFieldTable()).appendArgument("'DEFINER'").appendArgument("'INVOKER'").getStatement()).getStatement());
        sqlSecurity.setNull(!sqlSecurityCheck());
        return sqlSecurity;
    }

    protected Condition buildNameCondition(Table table, String fieldAlias) {
        return Condition.createCondition(Field.createField(table, fieldAlias), "=", "?");
    }


    /**
     * Returns the columns (if any) of this object.
     *
     * @return the columns
     */
    public List<DatabaseColumn> getColumns() throws DataSourceException {

        if (!isMarkedForReload() && columns != null)
            return columns;

        try {

            DatabaseHost host = getHost();
            if (host != null) {

                columns = host.getColumns(
                        getName(), false);

                if (columns != null)
                    for (DatabaseColumn i : columns)
                        i.setParent(this);
            }

        } finally {
            setMarkedForReload(false);
        }

        return columns;
    }

    /**
     * Returns the privileges (if any) of this object.
     *
     * @return the privileges
     */
    @Override
    public List<TablePrivilege> getPrivileges() throws DataSourceException {
        DatabaseHost host = getHost();
        return (host != null) ? host.getPrivileges(getCatalogName(), getSchemaName(), getName()) : null;
    }

    /**
     * Returns the parent host object.
     *
     * @return the parent object
     */
    @Override
    public DatabaseHost getHost() {
        return host;
    }

    /**
     * Sets the host object to that specified.
     *
     * @param host the host object
     */
    @Override
    public void setHost(DatabaseHost host) {
        this.host = host;
    }

    /**
     * Returns any remarks attached to this object.
     *
     * @return database object remarks
     */
    @Override
    public String getRemarks() {
        if (remarks == null || isMarkedForReload())
            getObjectInfo();
        return remarks;
    }

    /**
     * Sets the remarks to that specified.
     *
     * @param remarks the remarks to set
     */
    @Override
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Override to clear the columns.
     */
    public void reset() {
        super.reset();
        dataRowCount = -1;
        columns = null;
    }

    /**
     * Drops this named object in the database.
     *
     * @return drop statement result
     */
    @Override
    public int drop() throws DataSourceException {

        String queryStart = null;
        int _type = getType();

        switch (_type) {
            case FUNCTION:
                queryStart = "DROP FUNCTION ";
                break;
            case INDEX:
            case TABLE_INDEX:
                queryStart = "DROP INDEX ";
                break;
            case PROCEDURE:
                queryStart = "DROP PROCEDURE ";
                break;
            case SEQUENCE:
                queryStart = "DROP SEQUENCE ";
                break;
            case SYNONYM:
                queryStart = "DROP SYNONYM ";
                break;
            case SYSTEM_TABLE:
            case TABLE:
                queryStart = "DROP TABLE ";
                break;
            case TRIGGER:
                queryStart = "DROP TRIGGER ";
                break;
            case VIEW:
                queryStart = "DROP VIEW ";
                break;
            case OTHER:
                throw new DataSourceException(
                        "Dropping objects of this type is not currently supported");
        }

        Statement stmnt = null;

        try {

            Connection connection = getHost().getConnection();
            stmnt = connection.createStatement();

            int result = stmnt.executeUpdate(queryStart + getNameWithPrefixForQuery());
            if (!connection.getAutoCommit())
                connection.commit();

            return result;

        } catch (SQLException e) {
            throw new DataSourceException(e);

        } finally {
            releaseResources(stmnt);
        }

    }

    /**
     * Retrieves the data row count for this object (where applicable).
     *
     * @return the data row count for this object
     */
    public int getDataRowCount() throws DataSourceException {

        if (dataRowCount != -1)
            return dataRowCount;

        ResultSet rs = null;
        Statement stmnt = null;
        Connection connection = null;

        try {

            connection = getHost().getTemporaryConnection();
            stmnt = connection.createStatement();
            rs = stmnt.executeQuery(recordCountQueryString());

            if (rs.next())
                dataRowCount = rs.getInt(1);

            return dataRowCount;

        } catch (SQLException e) {
            throw new DataSourceException(e);

        } finally {
            releaseResources(stmnt, rs);
            releaseResources(connection);
        }

    }

    /**
     * Retrieves the data for this object (where applicable).
     *
     * @return the data for this object
     */
    @Override
    public ResultSet getData() throws DataSourceException {
        return executeQuery(recordsQueryString());
    }

    /**
     * Retrieves the data for this object (where applicable).
     *
     * @param rollbackOnError to rollback if a DataSourceException is thrown
     * @return the data for this object
     */
    @Override
    public ResultSet getData(boolean rollbackOnError) throws DataSourceException {
        try {

            return executeQuery(recordsQueryString());

        } catch (DataSourceException e) {

            if (rollbackOnError) {
                try {
                    getHost().getConnection().rollback();

                } catch (SQLException ignore) {
                }
            }

            throw e;
        }
    }

    @Override
    public ResultSet getMetaData() throws DataSourceException {
        try {

            DatabaseHost databaseHost = getHost();
            String _catalog = null;     //databaseHost.getCatalogNameForQueries(getCatalogName());
            String _schema = null;      //databaseHost.getSchemaNameForQueries(getSchemaName());

            DatabaseMetaData dmd = databaseHost.getDatabaseMetaData();
            return dmd.getColumns(_catalog, _schema, getName(), null);

        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    Connection connection;

    private ResultSet executeQuery(String query) throws DataSourceException {

        ResultSet rs;

        try {

            if (statement != null) {
                try {
                    statement.close();

                } catch (SQLException ignore) {
                }
            }

            if (connection == null || connection.isClosed())
                connection = getHost().getTemporaryConnection();
            else
                connection.commit();

            statement = ((PooledConnection) connection).createIndividualStatement();

            rs = statement.executeQuery(query);
            return new TransactionAgnosticResultSet(connection, statement, rs);

        } catch (SQLException e) {
            throw new DataSourceException(e);
        }

    }

    @Override
    public void releaseResources() {

        try {
            if (connection != null && !connection.isClosed())
                connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (statement != null) {
            try {

                if (!statement.isClosed()) {
                    //Log.info("Close statement");
                    statement.close();
                }
                statement = null;

            } catch (SQLException e) {
                Log.error("Error releaseResources in AbstractDatabaseObject:", e);
            }
        }
    }

    @Override
    public void cancelStatement() {

        if (statement != null) {

            try {

                statement.cancel();
                if (!statement.isClosed())
                    statement.close();
                statement = null;

            } catch (SQLException e) {
                logThrowable(e);
            }
        }
    }

    private String recordCountQueryString() {
        return "SELECT COUNT(*) FROM " + getNameWithPrefixForQuery();
    }

    private String recordsQueryString() {
        return "SELECT * FROM " + getNameWithPrefixForQuery();
    }

    protected final String getNameWithPrefixForQuery() {
        String prefix = getNamePrefix();
        return StringUtils.isNotBlank(prefix) ? prefix + "." + getNameForQuery() : getNameForQuery();
    }

    @Override
    public final String getNameForQuery() {

        String name = getName();

        /*if (name.contains(" ") // eg. access db allows this
                || (isLowerCase(name) && host.storesLowerCaseQuotedIdentifiers())
                || (isUpperCase(name) && host.storesUpperCaseQuotedIdentifiers())
                || (isMixedCase(name) && (host.storesMixedCaseQuotedIdentifiers()
                || host.supportsMixedCaseQuotedIdentifiers()))) {
        }
        return name;*/

        return quotedDatabaseObjectName(name);
    }

    private String quotedDatabaseObjectName(String name) {
        String quoteString = getIdentifierQuoteString();
        return quoteString + name + quoteString;
    }

    protected boolean isMixedCase(String value) {
        return value.matches(".*[A-Z].*") && value.matches(".*[a-z].*");
    }

    protected boolean isLowerCase(String value) {
        return value.matches("[^A-Z]*");
    }

    protected boolean isUpperCase(String value) {
        return value.matches("[^a-z]*");
    }

    protected String getIdentifierQuoteString() {
        try {
            return getHost().getDatabaseMetaData().getIdentifierQuoteString();

        } catch (SQLException e) {
            logThrowable(e);
            return "\"";
        }
    }

    @Override
    public boolean hasSQLDefinition() {
        return false;
    }

    @Override
    public abstract String getCreateSQLText() throws DataSourceException;

    public abstract String getDropSQL() throws DataSourceException;

    public abstract String getCompareCreateSQL() throws DataSourceException;

    public abstract String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException;

    protected abstract String queryForInfo();

    protected abstract void setInfoFromResultSet(ResultSet rs) throws SQLException;

    public int getDatabaseMajorVersion() {
        try {
            return host.getDatabaseMetaData().getDatabaseMajorVersion();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getDatabaseMinorVersion() {
        try {
            return host.getDatabaseMetaData().getDatabaseMinorVersion();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    protected DefaultStatementExecutor querySender;
    protected PooledStatement statementForLoadInfo;
    protected boolean someExecute = false;

    public boolean isSomeExecute() {
        return someExecute;
    }

    public void setSomeExecute(boolean someExecute) {
        this.someExecute = someExecute;
    }

    protected void getObjectInfo() {

        if (querySender == null && someExecute)
            querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());

        DefaultStatementExecutor executor =
                someExecute ? querySender : new DefaultStatementExecutor(getHost().getDatabaseConnection());
        try {

            if (statementForLoadInfo == null || statementForLoadInfo.isClosed())
                statementForLoadInfo = (PooledStatement) executor.getPreparedStatement(queryForInfo());
            statementForLoadInfo.setString(1, getName());
            ResultSet rs = executor.getResultSet(-1, statementForLoadInfo).getResultSet();
            setInfoFromResultSet(rs);

        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog("Error get info about " + getName(), e);

        } finally {
            if (!someExecute)
                executor.releaseResources();
            setMarkedForReload(false);
        }
    }

    protected void checkOnReload(Object object) {
        if (object == null || isMarkedForReload())
            getObjectInfo();
    }

    public void resetRowsCount() {
        dataRowCount = -1;
    }

    @Override
    public String getSource() {
        checkOnReload(source);
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public DatabaseMetaTag getMetaTagParent() {
        return metaTagParent;
    }

    String getFromResultSet(ResultSet rs, String colName) throws SQLException {
        String res = rs.getString(colName);
        return (res == null) ? "" : res.trim();
    }

    public DefaultStatementExecutor getQuerySender() {
        return querySender;
    }

    public void setQuerySender(DefaultStatementExecutor querySender) {
        this.querySender = querySender;
    }

    public PooledStatement getStatementForLoadInfo() {
        return statementForLoadInfo;
    }

    public void setStatementForLoadInfo(PooledStatement statementForLoadInfo) {
        this.statementForLoadInfo = statementForLoadInfo;
    }

}






