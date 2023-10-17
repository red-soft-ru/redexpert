package org.executequery.sql.sqlbuilder;

import java.util.HashMap;
import java.util.Map;

public class Join {
    Table leftTable;

    Table rightTable;
    Map<Field, Field> mapField;

    String typeJoin;

    public static Join createLeftJoin() {
        return new Join().setTypeJoin("LEFT");
    }

    public static Join createInnerJoin() {
        return new Join().setTypeJoin("");
    }

    public Join appendFields(Field leftField, Field rightField) {
        if (mapField == null) {
            mapField = new HashMap<>();
            leftTable = leftField.getTable();
            rightTable = rightField.getTable();
        }
        mapField.put(leftField, rightField);
        return this;
    }

    private Condition condition;

    public Condition getCondition() {
        return condition;
    }

    public Join setCondition(Condition condition) {
        this.condition = condition;
        return this;
    }

    public Table getLeftTable() {
        return leftTable;
    }

    public Table getRightTable() {
        return rightTable;
    }

    public Map<Field, Field> getMapField() {
        return mapField;
    }

    public String getTypeJoin() {
        return typeJoin;
    }

    public Join setTypeJoin(String typeJoin) {
        this.typeJoin = typeJoin;
        return this;
    }
}
