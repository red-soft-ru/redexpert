package org.executequery.sql.sqlbuilder;

public class Condition {

    Field leftField;
    String operator;
    String rightStatement;

    public static Condition createCondition() {
        return new Condition();
    }

    public Field getLeftField() {
        return leftField;
    }

    public Condition setLeftField(Field leftField) {
        this.leftField = leftField;
        return this;
    }

    public String getOperator() {
        return operator;
    }

    public Condition setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public String getRightStatement() {
        return rightStatement;
    }

    public Condition setRightStatement(String rightStatement) {
        this.rightStatement = rightStatement;
        return this;
    }
}
