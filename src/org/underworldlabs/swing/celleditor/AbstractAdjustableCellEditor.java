package org.underworldlabs.swing.celleditor;

import javax.swing.*;

abstract class AbstractAdjustableCellEditor extends AbstractCellEditor
        implements AdjustableCellEditor {

    private JTable table;
    private int column;

    private int minimumRowHeight;
    private int minimumColWidth;
    private int oldRowHeight;
    private int oldColWidth;

    protected final void updateSizePreferences(JTable table, int column, JPanel picker) {
        this.table = table;
        this.column = column;
        this.minimumColWidth = picker.getPreferredSize().width + 2;
        this.minimumRowHeight = picker.getPreferredSize().height + 2;
    }

    private void adjustTableRowHeight() {
        this.oldRowHeight = table.getRowHeight();

        if (table.getRowHeight() < this.minimumRowHeight)
            table.setRowHeight(this.minimumRowHeight);
    }

    private void adjustTableColWidth() {
        this.oldColWidth = table.getColumnModel().getColumn(column).getWidth();

        if (table.getColumnModel().getColumn(column).getWidth() < this.minimumColWidth) {
            table.getColumnModel().getColumn(column).setWidth(this.minimumColWidth);
            table.getColumnModel().getColumn(column).setPreferredWidth(this.minimumColWidth);
        }
    }

    private void restoreTableRowHeigh() {
        table.setRowHeight(this.oldRowHeight);
    }

    private void restoreTableColWidth() {
        table.getColumnModel().getColumn(column).setWidth(this.oldColWidth);
        table.getColumnModel().getColumn(column).setPreferredWidth(this.oldColWidth);
    }

    @Override
    public final void adjustCellSize() {
        adjustTableRowHeight();
        adjustTableColWidth();
    }

    @Override
    public final void restoreCellSize() {
        restoreTableRowHeigh();
        restoreTableColWidth();
    }

}
