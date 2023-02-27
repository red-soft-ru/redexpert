package org.executequery.sql.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

public class SelectBuilder {

    Table table;
    List<Field> fields;
    List<LeftJoin> joins;

    List<Condition> conditions;

    String ordering;

    public Table getTable() {
        return table;
    }

    public SelectBuilder setTable(Table table) {
        this.table = table;
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

    public String getSQLQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        if (fields != null) {
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
            sb.append("\n");
        }
        sb.append("FROM ");
        if (joins != null) {
            boolean first = true;
            for (LeftJoin join : joins) {
                if (first)
                    sb.append(join.leftField.table.getName()).append(" ").append(join.leftField.table.getAlias());
                first = false;
                sb.append(" LEFT JOIN ").append(join.rightField.table.getName()).append(" ").append(join.rightField.table.getAlias());
                sb.append(" ON ").append(join.leftField.getFieldTable());
                sb.append(" = ").append(join.rightField.getFieldTable());
                sb.append("\n");
            }
        }
        if (table != null) {
            sb.append(table.getName()).append(" ").append(table.getAlias()).append("\n");
        }
        if (conditions != null) {
            sb.append("WHERE ");
            boolean first = true;
            for (Condition condition : conditions) {
                if (!first)
                    sb.append("AND ");
                first = false;
                sb.append(condition.leftField.getFieldTable()).append(" ").append(condition.operator).append(" ").append(condition.rightStatement);
                sb.append("\n");
            }
        }
        if (ordering != null) {
            sb.append("ORDER BY ").append(ordering);

        }
        return sb.toString();

    }

}
