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
import org.executequery.databaseobjects.Types;
import org.executequery.databaseobjects.*;
import org.executequery.datasource.PooledConnection;
import org.executequery.datasource.PooledStatement;
import org.executequery.log.Log;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

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
    protected String remarks;

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
    protected static final String DIMENSION = "DIMENSION";
    protected static final String LOWER_BOUND = "LOWER_BOUND";
    protected static final String UPPER_BOUND = "UPPER_BOUND";
    protected static final String DEFAULT_COLLATE_NAME = "DEFAULT_COLLATE_NAME";
    protected static final String VALID_BLR = "VALID_BLR";
    protected boolean fullLoadCols = false;


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

    protected boolean fullLoad = false;
    protected DefaultStatementExecutor querySenderForColumns;

    protected Condition buildNameCondition(Field field) {
        return Condition.createCondition(field, "=", "?");
    }
    protected PooledStatement statementForLoadInfoForColumns;
    protected boolean someExecuteForColumns = false;
    DefaultDatabaseColumn previousColumn = null;
    List<DatabaseColumn> preColumns;

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
        String query = recordsQueryString();
        return executeQuery(query);
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
        return quoteString + name.replace(quoteString,quoteString+quoteString) + quoteString;
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

    public abstract String getCreateSQLTextWithoutComment() throws DataSourceException;

    public abstract String getDropSQL() throws DataSourceException;

    public abstract String getCompareCreateSQL() throws DataSourceException;

    public abstract String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException;

    protected Condition buildNameCondition(Table table, String fieldName) {
        return buildNameCondition(Field.createField(table, fieldName));
    }

    protected abstract String getFieldName();

    protected abstract Table getMainTable();

    protected Field getObjectField() {
        return Field.createField(getMainTable(), getFieldName());
    }

    protected String queryForInfo() {
        SelectBuilder sb = builderCommonQuery();
        sb.appendCondition(buildNameCondition(getObjectField()));
        return sb.getSQLQuery();
    }
    Semaphore mutex = new Semaphore(1);
    private boolean markedForReloadCols = true;

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

                loadColumns();

                if (columns != null)
                    for (DatabaseColumn i : columns)
                        i.setParent(this);
            }

        } finally {
            setMarkedForReloadCols(false);
        }

        return columns;
    }

    protected abstract SelectBuilder builderCommonQuery();

    protected Condition checkSystemCondition() {
        if (!isSystem()) {
            return Condition.createCondition()
                    .appendCondition(Condition.createCondition(Field.createField(getMainTable(), "SYSTEM_FLAG"), "IS", "NULL"))
                    .appendCondition(Condition.createCondition(Field.createField(getMainTable(), "SYSTEM_FLAG"), "=", "0"))
                    .setLogicOperator("OR");
        } else return Condition.createCondition()
                .appendCondition(Condition.createCondition(Field.createField(getMainTable(), "SYSTEM_FLAG"), "IS", "NOT NULL"))
                .appendCondition(Condition.createCondition(Field.createField(getMainTable(), "SYSTEM_FLAG"), "<>", "0"))
                .setLogicOperator("AND");
    }

    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        prepareLoadingInfo();
        boolean first = true;
        if (isAnyRowsResultSet())
            while (rs.next()) {
                setInfoFromSingleRowResultSet(rs, first);
                first = false;
            }
        else if (rs.next())
            setInfoFromSingleRowResultSet(rs, true);
        finishLoadingInfo();
    }

    public SelectBuilder getBuilderForCons(boolean allTables) {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        Table relations = getMainTable();
        Table constraints = Table.createTable("RDB$RELATION_CONSTRAINTS", "RC");
        Table constraints1 = Table.createTable("RDB$RELATION_CONSTRAINTS", "RCO");
        Table indexSegments = Table.createTable("RDB$INDEX_SEGMENTS", "ISGMT");
        Table refTable = Table.createTable("RDB$RELATION_CONSTRAINTS", "RC_REF");
        Table refColumn = Table.createTable("RDB$INDEX_SEGMENTS", "ISGMT_REF");
        Table refCons = Table.createTable("RDB$REF_CONSTRAINTS", "REF_CONS");
        Table relationFields = Table.createTable("RDB$RELATION_FIELDS", "RF");
        Field relName = Field.createField(constraints1, RELATION_NAME);
        sb.appendField(relName);
        Field fieldName = Field.createField(indexSegments, FIELD_NAME);
        sb.appendField(fieldName);
        sb.appendField(Field.createField().setNull(true).setAlias(FIELD_TYPE));
        sb.appendField(Field.createField().setNull(true).setAlias(FIELD_SUB_TYPE));
        sb.appendField(Field.createField().setNull(true).setAlias(SEGMENT_LENGTH));
        sb.appendField(Field.createField().setNull(true).setAlias(FIELD_PRECISION));
        sb.appendField(Field.createField().setNull(true).setAlias(FIELD_SCALE));
        sb.appendField(Field.createField().setNull(true).setAlias(FIELD_LENGTH));
        sb.appendField(Field.createField().setNull(true).setAlias(CHARACTER_LENGTH));
        sb.appendField(Field.createField().setNull(true).setAlias(DEFAULT_SOURCE).setAlias(DOMAIN_DEFAULT_SOURCE));
        sb.appendField(Field.createField().setNull(true).setAlias(NULL_FLAG).setAlias(DOMAIN_NULL_FLAG));
        sb.appendField(Field.createField().setNull(true).setAlias(COMPUTED_BLR));
        sb.appendField(Field.createField().setNull(true).setAlias(CHARACTER_SET_ID));
        sb.appendField(Field.createField().setNull(true).setAlias(COMPUTED_SOURCE));
        sb.appendField(Field.createField().setNull(true).setAlias(CHARACTER_SET_NAME));
        sb.appendField(Field.createField().setNull(true).setAlias(COLLATION_NAME));
        sb.appendField(Field.createField().setNull(true).setAlias(DEFAULT_SOURCE));
        sb.appendField(Field.createField().setNull(true).setAlias(NULL_FLAG));
        sb.appendField(Field.createField().setNull(true).setAlias(FIELD_SOURCE));
        sb.appendField(Field.createField().setNull(true).setAlias(DESCRIPTION));
        sb.appendField(Field.createField().setNull(true).setAlias(IDENTITY_TYPE));
        sb.appendField(Field.createField().setNull(true).setAlias(DIMENSION));
        sb.appendField(Field.createField().setNull(true).setAlias(LOWER_BOUND));
        sb.appendField(Field.createField().setNull(true).setAlias(UPPER_BOUND));
        Field fieldPosition = Field.createField(relationFields, FIELD_POSITION);
        fieldPosition.setStatement(fieldPosition.getFieldTable() + " + 1");
        sb.appendField(fieldPosition);
        sb.appendField(Field.createField(constraints, CONSTRAINT_NAME));
        sb.appendField(Field.createField(constraints, CONSTRAINT_TYPE));
        Field keyPosition = Field.createField(indexSegments, FIELD_POSITION).setAlias(KEY_SEQ);
        keyPosition.setStatement(keyPosition.getFieldTable() + " + 1");
        sb.appendField(keyPosition);
        sb.appendField(Field.createField(refTable, relName.getAlias()).setAlias(REF_TABLE));
        sb.appendField(Field.createField(refColumn, fieldName.getAlias()).setAlias(REF_COLUMN));
        sb.appendField(Field.createField(refCons, UPDATE_RULE));
        sb.appendField(Field.createField(refCons, DELETE_RULE));
        sb.appendJoin(Join.createInnerJoin().appendFields(relName, Field.createField(relations, relName.getAlias())));
        sb.appendJoin(Join.createInnerJoin()
                .appendFields(Field.createField(constraints1, "INDEX_NAME"), Field.createField(indexSegments, "INDEX_NAME"))
                .appendFields(fieldName, Field.createField(indexSegments, fieldName.getAlias())));
        sb.appendJoin(Join.createInnerJoin().appendFields(relName, Field.createField(relationFields, RELATION_NAME))
                .appendFields(fieldName, Field.createField(relationFields, FIELD_NAME)));
        sb.appendJoin(Join.createInnerJoin().appendFields(Field.createField(indexSegments, "INDEX_NAME"),
                Field.createField(constraints, "INDEX_NAME")));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(constraints, CONSTRAINT_NAME),
                Field.createField(refCons, CONSTRAINT_NAME)));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(refCons, "CONST_NAME_UQ"),
                Field.createField(refTable, CONSTRAINT_NAME)));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(refTable, "INDEX_NAME"),
                        Field.createField(refColumn, "INDEX_NAME"))
                .appendFields(Field.createField(indexSegments, "FIELD_POSITION"), Field.createField(refColumn, "FIELD_POSITION")));
        if (allTables)
            sb = builderForInfoAllObjects(sb);
        return sb;
    }

    public SelectBuilder getBuilderForCols(boolean allTables) {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        Table relations = getMainTable();
        Table relationFields = Table.createTable("RDB$RELATION_FIELDS", "RF");
        Table fields = Table.createTable("RDB$FIELDS", "F");
        Table charsets = Table.createTable("RDB$CHARACTER_SETS", "CH");
        Table collations = Table.createTable("RDB$COLLATIONS", "CO");
        Table dimensions = Table.createTable("RDB$FIELD_DIMENSIONS", "FD");
        Field relName = Field.createField(relationFields, RELATION_NAME);
        sb.appendField(relName);
        Field fieldName = Field.createField(relationFields, FIELD_NAME);
        sb.appendField(fieldName);
        sb.appendField(Field.createField(fields, FIELD_TYPE));
        sb.appendField(Field.createField(fields, FIELD_SUB_TYPE));
        sb.appendField(Field.createField(fields, SEGMENT_LENGTH));
        sb.appendField(Field.createField(fields, FIELD_PRECISION));
        sb.appendField(Field.createField(fields, FIELD_SCALE));
        sb.appendField(Field.createField(fields, FIELD_LENGTH));
        sb.appendField(Field.createField(fields, "CHARACTER_LENGTH").setAlias(CHARACTER_LENGTH));
        sb.appendField(Field.createField(fields, DEFAULT_SOURCE).setAlias(DOMAIN_DEFAULT_SOURCE));
        sb.appendField(Field.createField(fields, NULL_FLAG).setAlias(DOMAIN_NULL_FLAG));
        sb.appendField(Field.createField(fields, COMPUTED_BLR));
        sb.appendField(Field.createField(fields, CHARACTER_SET_ID));
        sb.appendField(Field.createField(fields, COMPUTED_SOURCE));
        sb.appendField(Field.createField(charsets, CHARACTER_SET_NAME));
        sb.appendField(Field.createField(collations, COLLATION_NAME));
        sb.appendField(Field.createField(relationFields, DEFAULT_SOURCE));
        sb.appendField(Field.createField(relationFields, NULL_FLAG));
        Field fieldSource = Field.createField(relationFields, FIELD_SOURCE);
        sb.appendField(fieldSource);
        sb.appendField(Field.createField(relationFields, DESCRIPTION));
        sb.appendField(Field.createField(relationFields, IDENTITY_TYPE).setNull(getDatabaseMajorVersion() < 3));
        sb.appendField(Field.createField(dimensions, DIMENSION));
        sb.appendField(Field.createField(dimensions, LOWER_BOUND));
        sb.appendField(Field.createField(dimensions, UPPER_BOUND));
        Field fieldPosition = Field.createField(relationFields, FIELD_POSITION);
        fieldPosition.setStatement(fieldPosition.getFieldTable() + " + 1");
        sb.appendField(fieldPosition);
        sb.appendField(Field.createField().setNull(true).setAlias(CONSTRAINT_NAME));
        sb.appendField(Field.createField().setNull(true).setAlias(CONSTRAINT_TYPE));
        sb.appendField(Field.createField().setNull(true).setAlias(KEY_SEQ));
        sb.appendField(Field.createField().setNull(true).setAlias(REF_TABLE));
        sb.appendField(Field.createField().setNull(true).setAlias(REF_COLUMN));
        sb.appendField(Field.createField().setNull(true).setAlias(UPDATE_RULE));
        sb.appendField(Field.createField().setNull(true).setAlias(DELETE_RULE));
        sb.appendJoin(Join.createInnerJoin().appendFields(getObjectField(), relName));
        sb.appendJoin(Join.createInnerJoin().appendFields(fieldSource, Field.createField(fields, FIELD_NAME)));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(fields, CHARACTER_SET_ID),
                Field.createField(charsets, CHARACTER_SET_ID)));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(fields, CHARACTER_SET_ID),
                        Field.createField(collations, CHARACTER_SET_ID))
                .appendFields(Field.createField(fields, "COLLATION_ID"),
                        Field.createField(collations, "COLLATION_ID")));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(fields, FIELD_NAME),
                Field.createField(dimensions, FIELD_NAME)));
        if (allTables)
            sb = builderForInfoAllObjects(sb);
        return sb;
    }

    public SelectBuilder getBuilderLoadColsCommon(boolean allTables) {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        sb.appendTable(Table.createTable().setStatement(new SelectBuilder(getHost().getDatabaseConnection()).appendSelectBuilder(getBuilderForCols(allTables)).appendSelectBuilder(getBuilderForCons(allTables)).getSQLQuery()));
        sb.setOrdering(RELATION_NAME + ", " + FIELD_POSITION + ", " + FIELD_NAME + ", " + FIELD_TYPE + " NULLS LAST");
        return sb;
    }

    public SelectBuilder getBuilderLoadColsForSingleTable() {
        SelectBuilder sb = getBuilderLoadColsCommon(false);
        sb.appendCondition(Condition.createCondition().setStatement(RELATION_NAME + " = ?"));
        //sb.appendCondition(buildNameCondition(getObjectField()));
        return sb;
    }

    protected void loadColumns() {
        if (fullLoadCols) {
            getMetaTagParent().loadColumnsForAllTables();
        }

        if (querySenderForColumns == null && someExecuteForColumns)
            querySenderForColumns = new DefaultStatementExecutor(getHost().getDatabaseConnection());

        DefaultStatementExecutor executor =
                someExecuteForColumns ? querySenderForColumns : new DefaultStatementExecutor(getHost().getDatabaseConnection());
        try {

            if (statementForLoadInfoForColumns == null || statementForLoadInfoForColumns.isClosed()) {
                String query = getBuilderLoadColsForSingleTable().getSQLQuery();
                statementForLoadInfoForColumns = (PooledStatement) executor.getPreparedStatement(query);
            }
            statementForLoadInfoForColumns.setString(1, getName());
            ResultSet rs = executor.getResultSet(-1, statementForLoadInfoForColumns).getResultSet();
            setColumnsFromResultSet(rs);

        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog("Error get info about " + getName(), e);

        } finally {
            if (!someExecuteForColumns)
                executor.releaseResources();
            setMarkedForReloadCols(false);
        }
    }

    protected SelectBuilder builderForInfoAllObjects(SelectBuilder commonBuilder) {
        return commonBuilder.appendCondition(checkSystemCondition());
    }

    public SelectBuilder getBuilderLoadColsForAllTables() {
        return getBuilderLoadColsCommon(true);
    }

    public String queryForInfoAllObjects() {
        return builderForInfoAllObjects(builderCommonQuery()).getSQLQuery();
    }

    protected void lockLoadingCols(boolean flag) {

        if (flag) {
            try {
                mutex.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            mutex.release();
        }
    }

    public void prepareLoadColumns() {
        lockLoadingCols(true);
        preColumns = new ArrayList<>();
        previousColumn = null;
    }

    //ResultSet.next cannot be called inside this function
    public abstract Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException;

    public abstract void prepareLoadingInfo();

    public abstract void finishLoadingInfo();

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


    public void addColumnFromResultSet(ResultSet rs) throws SQLException {
        String colName = MiscUtils.trimEnd(rs.getString(FIELD_NAME));
        if (previousColumn == null || !colName.contentEquals(previousColumn.getName())) {
            DefaultDatabaseColumn column = new DefaultDatabaseColumn();
            previousColumn = column;
            final short fieldType = rs.getShort(FIELD_TYPE);
            final short fieldSubType = rs.getShort(FIELD_SUB_TYPE);
            final short fieldScale = rs.getShort(FIELD_SCALE);
            final int characterSetId = rs.getInt(CHARACTER_SET_ID);
            final int dataType = DefaultDatabaseHost.getDataType(fieldType, fieldSubType, fieldScale, characterSetId);
            column.setPosition(rs.getInt(FIELD_POSITION));
            column.setTypeInt(dataType);
            column.setColumnSubtype(fieldSubType);
            column.setColumnScale(fieldScale);
            column.setName(colName);
            column.setTypeName(DatabaseTypeConverter.getDataTypeName(fieldType, fieldSubType, fieldScale));
            switch (dataType) {
                case Types.DECIMAL:
                case Types.NUMERIC:
                    // TODO column precision
                    column.setColumnScale(fieldScale * (-1));
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.BINARY:
                case Types.VARBINARY:
                    //valueBuilder.at(15).set(createInt(rs.getShort("FIELD_LENGTH")));
                    column.setColumnSize(rs.getShort(FIELD_LENGTH));
                    break;
                case Types.FLOAT:
                    // TODO column precision
//                    valueBuilder.at(6).set(FLOAT_PRECISION);
                    break;
                case Types.DOUBLE:
                    // TODO column precision
//                    valueBuilder.at(6).set(DOUBLE_PRECISION);
                    break;
                case Types.INT128:
                    // TODO column precision
                    break;
                case Types.BIGINT:
                    // TODO column precision
//                    valueBuilder
//                            .at(6).set(BIGINT_PRECISION)
//                            .at(8).set(INT_ZERO);
                    break;
                case Types.INTEGER:
                    // TODO column precision
//                    valueBuilder
//                            .at(6).set(INTEGER_PRECISION)
//                            .at(8).set(INT_ZERO);
                    break;
                case Types.SMALLINT:
                    // TODO column precision
//                    valueBuilder
//                            .at(6).set(SMALLINT_PRECISION)
//                            .at(8).set(INT_ZERO);
                    break;
                case Types.DATE:
                    // TODO column precision
//                    valueBuilder.at(6).set(DATE_PRECISION);
                    break;
                case Types.TIME:
                    // TODO column precision
//                    valueBuilder.at(6).set(TIME_PRECISION);
                    break;
                case Types.TIMESTAMP:
                    // TODO column precision
//                    valueBuilder.at(6).set(TIMESTAMP_PRECISION);
                    break;
                case Types.BOOLEAN:
                    // TODO column precision
//                    valueBuilder
//                            .at(6).set(BOOLEAN_PRECISION)
//                            .at(9).set(RADIX_BINARY);
                    break;
            }
            column.setColumnSize(rs.getInt(FIELD_LENGTH));
            if (rs.getInt(FIELD_PRECISION) != 0)
                column.setColumnSize(rs.getInt(FIELD_PRECISION));
            if (rs.getInt(CHARACTER_LENGTH) != 0)
                column.setColumnSize(rs.getInt(CHARACTER_LENGTH));

            final short nullFlag = rs.getShort(NULL_FLAG);
            final short sourceNullFlag = rs.getShort(DOMAIN_NULL_FLAG);
            column.setRemarks(rs.getString(DESCRIPTION));
            column.setRequired(nullFlag == 1);
            column.setDomainNotNull(sourceNullFlag == 1);

            String column_def = rs.getString(DEFAULT_SOURCE);
            if (column_def != null) {
                // TODO This looks suspicious (what if it contains default)
                int defaultPos = column_def.toUpperCase().trim().indexOf("DEFAULT");
                if (defaultPos == 0)
                    column_def = column_def.substring(7).trim();
                column.setDefaultValue(column_def);
            }
            column_def = rs.getString(DOMAIN_DEFAULT_SOURCE);
            if (column_def != null) {
                // TODO This looks suspicious (what if it contains default)
                int defaultPos = column_def.toUpperCase().trim().indexOf("DEFAULT");
                if (defaultPos == 0)
                    column_def = column_def.substring(7).trim();
                column.setDomainDefaultValue(column_def);
            }
            column.setIdentity(rs.getInt(IDENTITY_TYPE) == 1);
            String charset = rs.getString(CHARACTER_SET_NAME);
            String collate = rs.getString(COLLATION_NAME);
            if (charset != null)
                charset = charset.trim();
            if (collate != null)
                collate = collate.trim();
            column.setCharset(charset);
            column.setCollate(collate);
            String domain = rs.getString(FIELD_SOURCE);
            if (domain != null && !domain.isEmpty()) {
                column.setDomain(domain);
            }
            String computedSource = rs.getString(COMPUTED_SOURCE);
            if (computedSource != null && !computedSource.isEmpty()) {
                column.setGenerated(true);
                if (computedSource.startsWith("(") && computedSource.endsWith(")"))
                    computedSource = computedSource.substring(1, computedSource.length() - 1);
                column.setComputedSource(computedSource);
            }
            if (column.getTypeInt() == Types.LONGVARBINARY ||
                    column.getTypeInt() == Types.LONGVARCHAR ||
                    column.getTypeInt() == Types.BLOB) {
                column.setColumnSubtype(fieldSubType);
                column.setColumnSize(rs.getInt(SEGMENT_LENGTH));
            }
            preColumns.add(column);
        }
        if (rs.getObject(DIMENSION) != null) {
            previousColumn.appendDimension(rs.getInt(DIMENSION), rs.getInt(LOWER_BOUND), rs.getInt(UPPER_BOUND));
        }
        String conType = rs.getString(CONSTRAINT_TYPE);
        if (conType != null) {
            conType = conType.trim();
            switch (conType) {
                case "PRIMARY KEY":
                    previousColumn.setPrimaryKey(true);
                    TableColumnConstraint constraint = new TableColumnConstraint(null, ColumnConstraint.PRIMARY_KEY);
                    constraint.setName(rs.getString(CONSTRAINT_NAME));
                    previousColumn.addConstraint(constraint);
                    break;
                case "UNIQUE":
                    previousColumn.setUnique(true);
                    constraint = new TableColumnConstraint(null, ColumnConstraint.UNIQUE_KEY);
                    constraint.setName(rs.getString(CONSTRAINT_NAME));
                    previousColumn.addConstraint(constraint);
                    break;
                case "FOREIGN KEY":
                    previousColumn.setForeignKey(true);
                    constraint = new TableColumnConstraint(null, ColumnConstraint.FOREIGN_KEY);
                    constraint.setName(rs.getString(CONSTRAINT_NAME));
                    constraint.setReferencedTable(rs.getString(REF_TABLE));
                    constraint.setReferencedColumn(rs.getString(REF_COLUMN));
                    String rule = rs.getString(UPDATE_RULE);
                    if (rule != null)
                        constraint.setUpdateRule(rule.trim());
                    rule = rs.getString(DELETE_RULE);
                    if (rule != null)
                        constraint.setDeleteRule(rule.trim());
                    previousColumn.addConstraint(constraint);
                    break;
                default:
                    break;
            }
        }
    }
    protected PooledStatement statementForLoadInfo;

    public void finishLoadColumns() {
        if (preColumns != null) {
            columns = preColumns;
        }
        lockLoadingCols(false);
    }

    protected boolean someExecute = false;

    protected void setColumnsFromResultSet(ResultSet rs) throws SQLException {
        prepareLoadColumns();
        while (rs.next()) {
            addColumnFromResultSet(rs);
        }
        finishLoadColumns();
    }

    public abstract boolean isAnyRowsResultSet();

    public boolean isSomeExecute() {
        return someExecute;
    }

    public void setSomeExecute(boolean someExecute) {
        this.someExecute = someExecute;
    }

    public boolean isFullLoad() {
        return fullLoad;
    }

    public void setFullLoad(boolean fullLoad) {
        this.fullLoad = fullLoad;
    }

    protected void getObjectInfo() {

        if (fullLoad)
            getMetaTagParent().loadFullInfoForObjects();

        if (querySender == null && someExecute)
            querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());

        DefaultStatementExecutor executor =
                someExecute ? querySender : new DefaultStatementExecutor(getHost().getDatabaseConnection());
        try {

            // get\create statement
            if (statementForLoadInfo == null || statementForLoadInfo.isClosed()) {
                String query = queryForInfo();
                statementForLoadInfo = (PooledStatement) executor.getPreparedStatement(query);
            }

            // set statement parameters
            statementForLoadInfo.setString(1, getName());
            if (this instanceof DefaultDatabaseUser)
                statementForLoadInfo.setString(2, ((DefaultDatabaseUser) this).getPlugin());

            // execute statement
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

    public boolean isFullLoadCols() {
        return fullLoadCols;
    }

    public void setFullLoadCols(boolean fullLoadCols) {
        this.fullLoadCols = fullLoadCols;
    }

    public DefaultStatementExecutor getQuerySenderForColumns() {
        return querySenderForColumns;
    }

    public void setQuerySenderForColumns(DefaultStatementExecutor querySenderForColumns) {
        this.querySenderForColumns = querySenderForColumns;
    }

    public PooledStatement getStatementForLoadInfoForColumns() {
        return statementForLoadInfoForColumns;
    }

    public void setStatementForLoadInfoForColumns(PooledStatement statementForLoadInfoForColumns) {
        this.statementForLoadInfoForColumns = statementForLoadInfoForColumns;
    }

    public boolean isSomeExecuteForColumns() {
        return someExecuteForColumns;
    }

    public void setSomeExecuteForColumns(boolean someExecuteForColumns) {
        this.someExecuteForColumns = someExecuteForColumns;
    }

    public boolean isMarkedForReloadCols() {
        return markedForReloadCols;
    }

    public void setMarkedForReloadCols(boolean markedForReloadCols) {
        this.markedForReloadCols = markedForReloadCols;
    }

    public static int getRDBTypeFromType(int type) {
        switch (type) {
            case NamedObject.TABLE:
            case NamedObject.GLOBAL_TEMPORARY:
            case NamedObject.SYSTEM_TABLE:
                return 0;
            case NamedObject.VIEW:
            case NamedObject.SYSTEM_VIEW:
                return 1;
            case NamedObject.TRIGGER:
            case NamedObject.DATABASE_TRIGGER:
            case NamedObject.DDL_TRIGGER:
            case NamedObject.SYSTEM_TRIGGER:
                return 2;
            case NamedObject.PROCEDURE:
                return 5;
            case NamedObject.EXCEPTION:
                return 7;
            case NamedObject.USER:
                return 8;
            case NamedObject.DOMAIN:
                return 9;
            case NamedObject.INDEX:
                return 10;
            case NamedObject.ROLE:
            case NamedObject.SYSTEM_ROLE:
                return 13;
            case NamedObject.SEQUENCE:
            case NamedObject.SYSTEM_SEQUENCE:
                return 14;
            case NamedObject.FUNCTION:
            case NamedObject.UDF:
            case NamedObject.SYSTEM_FUNCTION:
                return 15;
            case NamedObject.COLLATION:
                return 17;
            case NamedObject.PACKAGE:
            case NamedObject.SYSTEM_PACKAGE:
                return 18;
            default:
                return -1;
        }
    }

    public int getRDBType() {
        return getRDBTypeFromType(getType());
    }
}






