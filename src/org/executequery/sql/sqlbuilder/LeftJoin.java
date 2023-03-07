package org.executequery.sql.sqlbuilder;

import java.util.HashMap;
import java.util.Map;

public class LeftJoin {
    Table leftTable;

    Table rightTable;
    Map<Field, Field> mapField;

    public static LeftJoin createLeftJoin() {
        return new LeftJoin();
    }

    public LeftJoin appendFields(Field leftField, Field rightField) {
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

    public LeftJoin setCondition(Condition condition) {
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
}
