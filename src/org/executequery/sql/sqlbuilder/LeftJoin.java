package org.executequery.sql.sqlbuilder;

public class LeftJoin {
    Field leftField;

    Field rightField;

    public Field getLeftField() {
        return leftField;
    }

    public void setLeftField(Field leftField) {
        this.leftField = leftField;
    }

    public Field getRightField() {
        return rightField;
    }

    public void setRightField(Field rightField) {
        this.rightField = rightField;
    }
}
