package org.underworldlabs.swing.celleditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

abstract class AbstractAdjustableCellEditor extends AbstractCellEditor
        implements AdjustableCellEditor {

    private boolean updateOldSize = true;
    private boolean adjusted = false;
    private int column = -1;
    private JTable table;

    private int minimumRowHeight;
    private int minimumColWidth;
    private int oldRowHeight;
    private int oldColWidth;

    private void adjustTableRowHeight() {
        if (updateOldSize)
            this.oldRowHeight = table.getRowHeight();

        if (table.getRowHeight() < this.minimumRowHeight)
            table.setRowHeight(this.minimumRowHeight);
    }

    private void adjustTableColWidth() {
        if (updateOldSize)
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

    protected final void adjustCellSize(JTable table, int column, JPanel picker) {

        this.updateOldSize = this.column != column;
        this.table = table;
        this.column = column;
        this.minimumColWidth = picker.getPreferredSize().width;
        this.minimumRowHeight = picker.getPreferredSize().height;

        adjustCellSize();
    }

    private void scrollToSelection(boolean increment) {

        int rowToView = table.getSelectedRow();
        rowToView += table.getVisibleRect().height / table.getRowHeight() / 2 * (increment ? 1 : -1);

        Rectangle rectangleToView = table.getCellRect(rowToView, table.getSelectedColumn(), true);
        table.scrollRectToVisible(rectangleToView);
    }

    @Override
    public final void adjustCellSize() {

        adjustTableRowHeight();
        adjustTableColWidth();

        if (!adjusted)
            scrollToSelection(true);

        adjusted = true;
    }

    @Override
    public final void restoreCellSize() {
        restoreTableRowHeigh();
        restoreTableColWidth();
        scrollToSelection(false);

        adjusted = false;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent)
            return ((MouseEvent) e).getClickCount() >= 1;
        else
            return true;
    }

    @Override
    public void cancelCellEditing() {
        restoreCellSize();
        super.cancelCellEditing();
    }

}
