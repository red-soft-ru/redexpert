package org.executequery.sql.sqlbuilder;

import org.executequery.databasemediators.DatabaseConnection;

import java.util.ArrayList;
import java.util.List;

public class
SelectBuilder extends SQLBuilder {

    boolean unionDistinct = false;
    boolean distinct = false;
    List<Table> tables;
    List<Field> fields;
    List<Join> joins;

    List<Condition> conditions;
    List<SelectBuilder> selectBuilders;

    String ordering;

    public SelectBuilder(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    public static SelectBuilder createSelectBuilder(DatabaseConnection dc) {
        return new SelectBuilder(dc);
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

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
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

    public SelectBuilder appendJoin(Join join) {
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

    public SelectBuilder appendSelectBuilder(SelectBuilder selectBuilder) {
        if (selectBuilders == null)
            selectBuilders = new ArrayList<>();
        selectBuilders.add(selectBuilder);
        return this;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public SelectBuilder setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public boolean isUnionDistinct() {
        return unionDistinct;
    }

    public SelectBuilder setUnionDistinct(boolean unionDistinct) {
        this.unionDistinct = unionDistinct;
        return this;
    }

    public String getSQLQuery() {
        StringBuilder sb = new StringBuilder();
        if (selectBuilders != null) {
            boolean first = true;
            for (SelectBuilder selectBuilder : selectBuilders) {
                if (!first) {
                    sb.append("\nUNION ");
                    if (selectBuilder.isUnionDistinct())
                        sb.append("DISTINCT");
                    else sb.append("ALL");
                    sb.append("\n");
                }
                first = false;
                sb.append(selectBuilder.getSQLQuery());
            }
        } else {
            sb.append("SELECT ");
            if (distinct)
                sb.append("DISTINCT ");
            if (fields != null && fields.size() > 0) {
                boolean first = true;
                for (Field field : fields) {
                    if (!first)
                        sb.append(",\n");
                    first = false;
                    sb.append(field.getFieldForQuery());
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
                    sb.append(table.getTableForQuery());
                }
            }
            if (joins != null) {
                boolean first = true;
                List<Table> usedTables = new ArrayList<>();
                for (Join join : joins) {
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
                    sb.append("\n").append(join.getTypeJoin()).append(" JOIN ");
                    sb.append(join.getRightTable().getName()).append(" ").append(join.getRightTable().getAlias());
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
        }
        return sb.toString();


    }

    public String toString() {
        return getSQLQuery();
    }

}
