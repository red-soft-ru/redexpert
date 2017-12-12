package org.underworldlabs.swing.hexeditor.textgrid;

import java.util.EventObject;

public class TextGridCursorEvent extends EventObject {

    public static final int CURSOR_MOVED = 0;
    public static final int MARK_SET = 1;
    public static final int MARK_CLEARED = 2;

    private int row;
    private int column;
    private int markedRow;
    private int markedColumn;
    private int type = CURSOR_MOVED;

    public TextGridCursorEvent(TextGridCursor source, int row, int column,
                               int markedRow, int markedColumn, int type) {
        super(source);
        this.row = row;
        this.column = column;
        this.markedRow = markedRow;
        this.markedColumn = markedColumn;
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getMarkedRow() {
        return markedRow;
    }

    public int getLastColumn() {
        return markedColumn;
    }

    public int getType() {
        return type;
    }

}
