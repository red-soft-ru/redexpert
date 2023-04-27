package org.executequery.sql.sqlbuilder;

public abstract class SQLBuilder {
    public static final String PREFIX = "RDB$";

    public abstract String getSQLQuery();
}
