package org.executequery.sql.sqlbuilder;

public class Field {
    String name;
    Table table;
    String alias;
    boolean isNull = false;
    String statement;

    public static Field createField() {
        return new Field();
    }

    public String getName() {
        return name;
    }

    public Field setName(String name) {
        this.name = name;
        return this;
    }

    public Table getTable() {
        return table;
    }

    public Field setTable(Table table) {
        this.table = table;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Field setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getStatement() {
        return statement;
    }

    public Field setStatement(String statement) {
        this.statement = statement;
        return this;
    }

    public boolean isNull() {
        return isNull;
    }

    public Field setNull(boolean aNull) {
        isNull = aNull;
        return this;
    }

    public String getFieldTable() {
        return table.getAlias() + "." + name;
    }
}
