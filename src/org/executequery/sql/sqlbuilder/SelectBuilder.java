package org.executequery.sql.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

public class
SelectBuilder extends SQLBuilder {

    boolean distinct = false;
    List<Table> tables;
    List<Field> fields;
    List<LeftJoin> joins;

    List<Condition> conditions;

    String ordering;

    public static SelectBuilder createSelectBuilder() {
        return new SelectBuilder();
    }

    public List<Table> getTables() {
        return tables;
    }

    public SelectBuilder setTables(List<Table> tables) {
        this.tables = tables;
        return this;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<LeftJoin> getJoins() {
        return joins;
    }

    public void setJoins(List<LeftJoin> joins) {
        this.joins = joins;
    }

    public List<Condition> getCondition() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public String getOrdering() {
        return ordering;
    }

    public SelectBuilder setOrdering(String ordering) {
        this.ordering = ordering;
        return this;
    }

    public SelectBuilder appendField(Field field) {
        if (fields == null)
            fields = new ArrayList<>();
        fields.add(field);
        return this;
    }

    public SelectBuilder appendFields(Table table, String... aliases) {
        return appendFields(table, false, aliases);
    }

    public SelectBuilder appendFields(Table table, boolean nullFlag, String... aliases) {
        return appendFields("", table, nullFlag, aliases);
    }

    public SelectBuilder appendFields(String aliasPrefix, Table table, String... aliases) {
        return appendFields(aliasPrefix, table, false, aliases);
    }

    public SelectBuilder appendFields(String aliasPrefix, Table table, boolean nullFlag, String... aliases) {
        if (aliases != null)
            for (String alias : aliases) {
                Field field = Field.createField(table, alias).setAlias(aliasPrefix + alias).setNull(nullFlag);
                appendField(field);
            }
        return this;
    }

    public SelectBuilder appendJoin(LeftJoin join) {
        if (joins == null)
            joins = new ArrayList<>();
        joins.add(join);
        return this;
    }

    public SelectBuilder appendCondition(Condition condition) {
        if (conditions == null)
            conditions = new ArrayList<>();
        conditions.add(condition);
        return this;
    }

    public SelectBuilder appendTable(Table table) {
        if (tables == null)
            tables = new ArrayList<>();
        tables.add(table);
        return this;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public SelectBuilder setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public String getSQLQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if (distinct)
            sb.append("DISTINCT ");
        if (fields != null && fields.size() > 0) {
            boolean first = true;
            for (Field field : fields) {
                if (!first)
                    sb.append(",\n");
                first = false;
                if (field.isNull)
                    sb.append("NULL");
                else if (field.statement != null)
                    sb.append(field.statement);
                else sb.append(field.getFieldTable());
                sb.append(" AS ").append(field.alias);
            }
        } else sb.append("*");
        sb.append("\n");
        sb.append("FROM ");
        if (tables != null) {
            boolean first = true;
            for (Table table : tables) {
                if (!first)
                    sb.append(", ");
                first = false;
                sb.append(table.getName()).append(" ").append(table.getAlias());
            }
        }
        if (joins != null) {
            boolean first = true;
            List<Table> usedTables = new ArrayList<>();
            for (LeftJoin join : joins) {
                if (first) {
                    if (tables != null)
                        sb.append(",\n");
                }
                first = false;
                if (!usedTables.contains(join.getLeftTable())) {
                    if (!usedTables.isEmpty())
                        sb.append(",\n");
                    sb.append(join.getLeftTable().getName()).append(" ").append(join.getLeftTable().getAlias());
                    usedTables.add(join.getLeftTable());
                }
                sb.append("\nLEFT JOIN ").append(join.getRightTable().getName()).append(" ").append(join.getRightTable().getAlias());
                usedTables.add(join.getRightTable());
                boolean firstField = true;
                for (Field leftField : join.getMapField().keySet()) {
                    Field rightField = join.getMapField().get(leftField);
                    if (!firstField) {
                        sb.append(" AND ");
                    } else sb.append(" ON ");
                    firstField = false;
                    sb.append(leftField.getFieldTable());
                    sb.append(" = ").append(rightField.getFieldTable());
                }
                if (join.getCondition() != null) {
                    sb.append("\nAND (").append(join.getCondition().getConditionStatement()).append(")");
                }
            }
        }
        sb.append("\n");
        if (conditions != null) {
            sb.append("WHERE ");
            boolean first = true;
            for (Condition condition : conditions) {
                if (!first)
                    sb.append("AND ");
                first = false;
                sb.append(condition.getConditionStatement());
                sb.append("\n");
            }
        }
        if (ordering != null) {
            sb.append("ORDER BY ").append(ordering);

        }
        return sb.toString();


    }

    public String toString() {
        return getSQLQuery();
    }

}
