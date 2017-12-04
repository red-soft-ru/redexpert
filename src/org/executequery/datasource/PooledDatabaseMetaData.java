package org.executequery.datasource;

import com.mchange.v2.sql.filter.FilterDatabaseMetaData;

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
        return inner.getSchemas(s, s1);
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
        return inner.getClientInfoProperties();
    }

    @Override
    public ResultSet getFunctions(String s, String s1, String s2) throws SQLException {
        return inner.getFunctions(s, s1, s2);
    }

    @Override
    public ResultSet getFunctionColumns(String s, String s1, String s2, String s3) throws SQLException {
        return inner.getFunctionColumns(s, s1, s2, s3);
    }

    @Override
    public ResultSet getPseudoColumns(String s, String s1, String s2, String s3) throws SQLException {
        return inner.getPseudoColumns(s, s1, s2, s3);
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
}
