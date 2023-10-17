package org.executequery.sql.sqlbuilder;

public class Table {

    public static Table createTable() {
        return new Table();
    }

    public static Table createTable(String name, String alias) {
        return createTable().setName(name).setAlias(alias);
    }

    public static Table createTable(String name, String alias, boolean usePrefix) {
        return createTable(usePrefix ? SelectBuilder.PREFIX + name : name, alias);
    }

    private String name;
    private String alias;
    private String statement;

    public String getName() {
        return name;
    }

    public Table setName(String name) {
        this.name = name;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Table setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getStatement() {
        return statement;
    }

    public Table setStatement(String statement) {
        this.statement = statement;
        return this;
    }

    public String getTableForQuery() {
        StringBuilder sb = new StringBuilder();
        if (getStatement() == null)
            sb.append(getName()).append(" ").append(getAlias());
        else sb.append("(").append(getStatement()).append(")");
        return sb.toString();
    }

    public String toString() {
        return getTableForQuery();
    }
}
