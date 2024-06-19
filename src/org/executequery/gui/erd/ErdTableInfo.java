package org.executequery.gui.erd;

import org.executequery.gui.browser.ColumnData;

public class ErdTableInfo {
    String name;
    ColumnData[] columns;
    String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnData[] getColumns() {
        return columns;
    }

    public void setColumns(ColumnData[] columns) {
        this.columns = columns;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
