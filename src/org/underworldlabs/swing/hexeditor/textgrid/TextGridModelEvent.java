package org.underworldlabs.swing.hexeditor.textgrid;

import java.util.EventObject;

public class TextGridModelEvent extends EventObject {

    public static final int FIRST_ROW = -1;
    public static final int FIRST_COLUMN = -2;
    public static final int LAST_ROW = -3;
    public static final int LAST_COLUMN = -4;
    public static final int UPDATE = 0;
    public static final int INSERT = 1;
    public static final int DELETE = 2;

    private int firstRow = FIRST_ROW;
    private int lastRow = LAST_ROW;
    private int firstColumn = FIRST_COLUMN;
    private int lastColumn = LAST_COLUMN;
    private int type = UPDATE;

    public TextGridModelEvent(TextGridModel source) {
        super(source);
    }

    public TextGridModelEvent(TextGridModel source, int firstRow, int firstColumn,
                              int lastRow, int lastColumn, int type) {
        super(source);
        this.firstRow = firstRow;
        this.firstColumn = firstColumn;
        this.lastRow = lastRow;
        this.lastColumn = lastColumn;
        this.type = type;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public int getFirstColumn() {
        return firstColumn;
    }

    public int getLastRow() {
        return lastRow;
    }

    public int getLastColumn() {
        return lastColumn;
    }

    public int getType() {
        return type;
    }

}
