package org.executequery.datasource;

import com.mchange.v2.sql.filter.FilterDatabaseMetaData;
import org.executequery.log.Log;

import java.sql.*;

public class PooledDatabaseMetaData extends FilterDatabaseMetaData {
    PooledConnection connection;

    public PooledDatabaseMetaData(PooledConnection con, DatabaseMetaData dmd) {
        super(dmd);
        connection = con;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return inner.getRowIdLifetime();
    }

    @Override
    public ResultSet getSchemas(String s, String s1) throws SQLException {
        return asPooledResultSet(inner.getSchemas(s, s1));
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return inner.supportsStoredFunctionsUsingCallSyntax();
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return inner.autoCommitFailureClosesAllResultSets();
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return asPooledResultSet(inner.getClientInfoProperties());
    }

    @Override
    public ResultSet getFunctions(String s, String s1, String s2) throws SQLException {
        return asPooledResultSet(inner.getFunctions(s, s1, s2));
    }

    @Override
    public ResultSet getFunctionColumns(String s, String s1, String s2, String s3) throws SQLException {
        return asPooledResultSet(inner.getFunctionColumns(s, s1, s2, s3));
    }

    @Override
    public ResultSet getPseudoColumns(String s, String s1, String s2, String s3) throws SQLException {
        return asPooledResultSet(inner.getPseudoColumns(s, s1, s2, s3));
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return inner.generatedKeyAlwaysReturned();
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return inner.unwrap(aClass);
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return inner.isWrapperFor(aClass);
    }

    public Connection getConnection() {
        return connection;
    }

    public Connection getRealConnection() throws SQLException {
        return inner.getConnection();
    }
    public ResultSet getProcedures(String a, String b, String c) throws SQLException {
        return asPooledResultSet(this.inner.getProcedures(a, b, c));
    }

    public ResultSet getProcedureColumns(String a, String b, String c, String d) throws SQLException {
        return asPooledResultSet(this.inner.getProcedureColumns(a, b, c, d));
    }

    public ResultSet getTables(String a, String b, String c, String[] d) throws SQLException {
        return asPooledResultSet(this.inner.getTables(a, b, c, d));
    }

    public ResultSet getSchemas() throws SQLException {
        return asPooledResultSet(this.inner.getSchemas());
    }

    public ResultSet getCatalogs() throws SQLException {
        return asPooledResultSet(this.inner.getCatalogs());
    }

    public ResultSet getTableTypes() throws SQLException {
        return asPooledResultSet(this.inner.getTableTypes());
    }

    public ResultSet getColumnPrivileges(String a, String b, String c, String d) throws SQLException {
        return asPooledResultSet(this.inner.getColumnPrivileges(a, b, c, d));
    }

    public ResultSet getTablePrivileges(String a, String b, String c) throws SQLException {
        return asPooledResultSet(this.inner.getTablePrivileges(a, b, c));
    }

    public ResultSet getBestRowIdentifier(String a, String b, String c, int d, boolean e) throws SQLException {
        return asPooledResultSet(this.inner.getBestRowIdentifier(a, b, c, d, e));
    }

    public ResultSet getVersionColumns(String a, String b, String c) throws SQLException {
        return asPooledResultSet(this.inner.getVersionColumns(a, b, c));
    }

    public ResultSet getPrimaryKeys(String a, String b, String c) throws SQLException {
        return asPooledResultSet(this.inner.getPrimaryKeys(a, b, c));
    }

    public ResultSet getImportedKeys(String a, String b, String c) throws SQLException {
        return asPooledResultSet(this.inner.getImportedKeys(a, b, c));
    }

    public ResultSet getExportedKeys(String a, String b, String c) throws SQLException {
        return asPooledResultSet(this.inner.getExportedKeys(a, b, c));
    }

    public ResultSet getCrossReference(String a, String b, String c, String d, String e, String f) throws SQLException {
        return asPooledResultSet(this.inner.getCrossReference(a, b, c, d, e, f));
    }

    public ResultSet getTypeInfo() throws SQLException {
        return asPooledResultSet(this.inner.getTypeInfo());
    }

    public ResultSet getIndexInfo(String a, String b, String c, boolean d, boolean e) throws SQLException {
        return asPooledResultSet(this.inner.getIndexInfo(a, b, c, d, e));
    }

    public ResultSet getUDTs(String a, String b, String c, int[] d) throws SQLException {
        return asPooledResultSet(this.inner.getUDTs(a, b, c, d));
    }

    public ResultSet getSuperTypes(String a, String b, String c) throws SQLException {
        return asPooledResultSet(this.inner.getSuperTypes(a, b, c));
    }

    public ResultSet getSuperTables(String a, String b, String c) throws SQLException {
        return asPooledResultSet(this.inner.getSuperTables(a, b, c));
    }

    public ResultSet getAttributes(String a, String b, String c, String d) throws SQLException {
        return asPooledResultSet(this.inner.getAttributes(a, b, c, d));
    }

    public ResultSet getColumns(String a, String b, String c, String d) throws SQLException {
        return asPooledResultSet(this.inner.getColumns(a, b, c, d));
    }

    private PooledResultSet asPooledResultSet(ResultSet rs) throws SQLException {
        connection.lock(true);
        PooledStatement st = new PooledStatement(connection,rs.getStatement());
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for(int i=0;i<stack.length;i++)
        Log.debug(stack[stack.length-1-i]);
        return new PooledResultSet(st,rs);
    }
}
