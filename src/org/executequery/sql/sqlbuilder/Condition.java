package org.executequery.sql.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

public class Condition {

    String logicOperator = "AND";
    Field leftField;

    String leftStatement;

    String statement;
    String operator;
    String rightStatement;

    List<Condition> conditions;

    public static Condition createCondition() {
        return new Condition();
    }

    public static Condition createCondition(Field leftField, String operator, String rightStatement) {
        return createCondition().setLeftField(leftField).setOperator(operator).setRightStatement(rightStatement);
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

    public String getLogicOperator() {
        return logicOperator;
    }

    public Condition setLogicOperator(String logicOperator) {
        this.logicOperator = logicOperator;
        return this;
    }

    public Condition appendCondition(Condition condition) {
        if (conditions == null)
            conditions = new ArrayList<>();
        conditions.add(condition);
        return this;
    }

    public String getLeftStatement() {
        return leftStatement;
    }

    public Condition setLeftStatement(String leftStatement) {
        this.leftStatement = leftStatement;
        return this;
    }

    public String getStatement() {
        return statement;
    }

    public Condition setStatement(String statement) {
        this.statement = statement;
        return this;
    }

    public String getConditionStatement() {
        StringBuilder sb = new StringBuilder();
        if (conditions == null) {
            if (statement != null)
                return statement;
            if (leftStatement != null)
                sb.append(leftStatement);
            else sb.append(leftField.getFieldTable());
            sb.append(" ").append(operator).append(" ").append(rightStatement);
        } else {
            sb.append("(");
            boolean first = true;
            for (Condition condition : conditions) {
                if (!first)
                    sb.append(" ").append(logicOperator).append(" ");
                first = false;
                sb.append(condition.getConditionStatement());
            }
            sb.append(")");
        }
        return sb.toString();
    }

}
