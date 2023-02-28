package org.executequery.sql.sqlbuilder;

public class Table {

    public static Table createTable() {
        return new Table();
    }

    private String name;
    private String alias;

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
}
