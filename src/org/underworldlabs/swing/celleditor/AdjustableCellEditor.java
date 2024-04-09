package org.underworldlabs.swing.celleditor;

import javax.swing.table.TableCellEditor;

public interface AdjustableCellEditor extends TableCellEditor {

    void adjustCellSize();

    void restoreCellSize();

}
