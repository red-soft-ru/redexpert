package org.executequery.sql.sqlbuilder;

import org.executequery.databasemediators.DatabaseConnection;

public abstract class SQLBuilder {
    protected DatabaseConnection connection;

    public SQLBuilder(DatabaseConnection databaseConnection) {
        connection = databaseConnection;
    }

    public static final String PREFIX = "RDB$";

    public abstract String getSQLQuery();
}
