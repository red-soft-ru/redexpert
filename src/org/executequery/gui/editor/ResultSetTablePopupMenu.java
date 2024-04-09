/*
 * ResultSetTablePopupMenu.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.editor;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.UserPreferencesManager;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseTableObject;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.exportData.ExportDataPanel;
import org.executequery.gui.resultset.*;
import org.executequery.localization.Bundles;
import org.executequery.print.PrintingSupport;
import org.executequery.print.TablePrinter;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

public class ResultSetTablePopupMenu extends JPopupMenu implements MouseListener {

    private final ResultSetTable table;
    private final DatabaseTableObject tableObject;
    private final ReflectiveAction reflectiveAction;
    private final ResultSetTableContainer resultSetTableContainer;
    private final JMenuItem autoWidthForColsItem;

    private Point lastPopupPoint;
    private boolean doubleClickCellOpensDialog;

    public ResultSetTablePopupMenu(ResultSetTable table, ResultSetTableContainer resultSetTableContainer) {
        this(table, resultSetTableContainer, null);
    }

    public ResultSetTablePopupMenu(
            ResultSetTable table, ResultSetTableContainer resultSetTableContainer, DatabaseTableObject tableObject) {

        this.tableObject = tableObject;
        this.table = table;
        this.resultSetTableContainer = resultSetTableContainer;

        doubleClickCellOpensDialog = doubleClickCellOpensDialog();
        reflectiveAction = new ReflectiveAction(this);

        // --- init menu items ---

        // the print sub-menu
        JMenu printMenu = MenuItemFactory.createMenu(bundleString("Print"));
        printMenu.add(create(bundleString("Selection"), "printSelection"));
        printMenu.add(create(bundleString("Table"), "printTable"));

        // the export sub-menu
        JMenu exportMenu = MenuItemFactory.createMenu(bundleString("Export"));
        exportMenu.add(create(bundleString("Selection"), "exportSelection"));
        exportMenu.add(create(bundleString("Table"), "exportTable"));

        // the copy sub-menu
        JMenu copyMenu = MenuItemFactory.createMenu(bundleString("CopyOtherOptions"));
        copyMenu.add(create(bundleString("CopySelectedColumnNames"), "copySelectedColumnNames"));
        copyMenu.add(create(bundleString("CopySelectedCells-CommaSeparated"), "copySelectedCellsAsCSV"));
        copyMenu.add(create(bundleString("CopySelectedCells-CommaSeparatedWithNames"), "copySelectedCellsAsCSVWithNames"));
        copyMenu.add(create(bundleString("CopySelectedCells-CommaSeparatedAndQuoted"), "copySelectedCellsAsCSVQuoted"));
        copyMenu.add(create(bundleString("CopySelectedCells-CommaSeparatedAndQuotedWithNames"), "copySelectedCellsAsCSVQuotedWithNames"));

        // the cell opens checkBox menu-item
        JCheckBoxMenuItem cellOpensDialog = MenuItemFactory.createCheckBoxMenuItem(reflectiveAction);
        cellOpensDialog.setText(bundleString("Double-ClickOpensItemView"));
        cellOpensDialog.setSelected(doubleClickCellOpensDialog());
        cellOpensDialog.setActionCommand("cellOpensDialog");

        // the auto column width menu-item
        autoWidthForColsItem = create(bundleString("AutoWidthForCols"), "autoWidthForCols");
        autoWidthForColsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK));

        // --- arrange menu items ---

        add(create(bundleString("CopySelectedCells"), "copySelectedCells"));
        add(copyMenu);
        addSeparator();

        add(create(bundleString("SelectRow"), "selectRow"));
        add(create(bundleString("SelectColumn"), "selectColumn"));
        add(autoWidthForColsItem);
        if (resultSetTableContainer != null && resultSetTableContainer.isTransposeAvailable())
            add(create(bundleString("TransposeRow"), "transposeRow"));
        addSeparator();

        add(create(bundleString("SetNull"), "setNull"));
        addSeparator();

        if (resultSetTableContainer != null && resultSetTableContainer.isTransposeAvailable()) {
            add(createFromAction("editor-show-hide-rs-columns-command", "Show/hide result set columns"));
            addSeparator();
        }

        add(create(bundleString("View"), "openDataItemViewer"));
        add(exportMenu);
        add(printMenu);
        addSeparator();

        add(cellOpensDialog);
    }

    private JMenuItem create(String text, String actionCommand) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(reflectiveAction);
        menuItem.setActionCommand(actionCommand);
        menuItem.setText(text);

        return menuItem;
    }

    @SuppressWarnings("SameParameterValue")
    private JMenuItem createFromAction(String actionId, String toolTipText) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(ActionBuilder.get(actionId));
        menuItem.setToolTipText(toolTipText);
        menuItem.setIcon(null);

        return menuItem;
    }

    // --- handlers ---

    @SuppressWarnings("unused")
    public void printSelection(ActionEvent e) {
        printResultSet(true);
    }

    @SuppressWarnings("unused")
    public void printTable(ActionEvent e) {
        printResultSet(false);
    }

    @SuppressWarnings("unused")
    public void exportSelection(ActionEvent e) {

        TableModel selected = table.selectedCellsAsTableModel();
        if (selected == null)
            return;

        if (tableObject != null) {

            List<DatabaseColumn> columns = new LinkedList<>();
            for (DatabaseColumn column : tableObject.getColumns()) {
                for (int i = 0; i < selected.getColumnCount(); i++) {
                    if (column.getName().equals(selected.getColumnName(i))) {
                        columns.add(column);
                        break;
                    }
                }
            }
            new ExportDataPanel(selected, tableObject.getName(), columns);

        } else
            new ExportDataPanel(selected, null);
    }

    @SuppressWarnings("unused")
    public void exportTable(ActionEvent e) {
        if (tableObject != null)
            new ExportDataPanel(resultSetTableModel(), tableObject.getName(), tableObject.getColumns());
        else
            new ExportDataPanel(resultSetTableModel(), null);
    }

    @SuppressWarnings("unused")
    public void copySelectedColumnNames(ActionEvent e) {
        table.copySelectedColumnNames();
    }

    @SuppressWarnings("unused")
    public void copySelectedCellsAsCSV(ActionEvent e) {
        table.copySelectedCellsAsCSV();
    }

    @SuppressWarnings("unused")
    public void copySelectedCellsAsCSVWithNames(ActionEvent e) {
        table.copySelectedCellsAsCSVWithNames();
    }

    @SuppressWarnings("unused")
    public void copySelectedCellsAsCSVQuoted(ActionEvent e) {
        table.copySelectedCellsAsCSVQuoted();
    }

    @SuppressWarnings("unused")
    public void copySelectedCellsAsCSVQuotedWithNames(ActionEvent e) {
        table.copySelectedCellsAsCSVQuotedWithNames();
    }

    @SuppressWarnings("unused")
    public void cellOpensDialog(ActionEvent e) {

        doubleClickCellOpensDialog = ((JCheckBoxMenuItem) e.getSource()).isSelected();
        resultSetTableModel().setCellsEditable(!doubleClickCellOpensDialog);

        SystemProperties.setBooleanProperty(
                Constants.USER_PROPERTIES_KEY,
                "results.table.double-click.record.dialog",
                doubleClickCellOpensDialog
        );

        UserPreferencesManager.fireUserPreferencesChanged();
    }

    @SuppressWarnings("unused")
    public void autoWidthForCols(ActionEvent e) {

        boolean isAutoResizeable = !table.isAutoResizeable();

        table.setTableColumnWidthFromContents();
        table.setAutoResizeable(isAutoResizeable);
        table.setAutoResizeMode(isAutoResizeable ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);

        autoWidthForColsItem.setText(isAutoResizeable ?
                bundleString("ColumnWidthByContent") :
                bundleString("AutoWidthForCols")
        );
    }

    @SuppressWarnings("unused")
    public void copySelectedCells(ActionEvent e) {
        table.copySelectedCells();
    }

    @SuppressWarnings("unused")
    public void selectRow(ActionEvent e) {
        table.setColumnSelectionInterval(0, table.getColumnCount() - 1);
    }

    @SuppressWarnings("unused")
    public void selectColumn(ActionEvent e) {
        table.setRowSelectionInterval(0, table.getRowCount() - 1);
    }

    @SuppressWarnings("unused")
    public void transposeRow(ActionEvent e) {

        if (resultSetTableContainer != null) {

            table.selectRow(lastPopupPoint);
            int selectedRow = table.getSelectedRow();

            TableSorter model = (TableSorter) table.getModel();
            resultSetTableContainer.transposeRow(model.getTableModel(), selectedRow);
        }
    }

    @SuppressWarnings("unused")
    public void setNull(ActionEvent e) {

        if (table.hasMultipleColumnAndRowSelections()) {

            int[] selectedColumns = table.getSelectedCellsColumnsIndexes();
            int[] selectedRows = table.getSelectedCellsRowsIndexes();

            for (int selectedRow : selectedRows)
                for (int selectedColumn : selectedColumns)
                    table.setValueAt(null, selectedRow, selectedColumn);

        } else
            table.setValueAt(null, table.getSelectedRow(), table.getSelectedColumn());
    }

    public void openDataItemViewer() {

        try {
            GUIUtilities.showWaitCursor();
            showViewerForValueAt(lastPopupPoint);

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    // --- other methods ---

    private boolean doubleClickCellOpensDialog() {
        return UserPreferencesManager.doubleClickOpenItemView();
    }

    private RecordDataItem tableCellDataAtPoint(Point point) {

        Object value = table.valueAtPoint(point);
        if (value instanceof RecordDataItem)
            return (RecordDataItem) value;

        return null;
    }

    private void showViewerForValueAt(Point point) {

        RecordDataItem recordDataItem = tableCellDataAtPoint(point);
        if (recordDataItem != null && !recordDataItem.isDisplayValueNull()) {

            if (recordDataItem instanceof SimpleRecordDataItem)
                showSimpleRecordDataItemDialog(recordDataItem);
            else if (recordDataItem instanceof LobRecordDataItem)
                showLobRecordDataItemDialog(recordDataItem);

        } else if (recordDataItem instanceof LobRecordDataItem)
            showLobRecordDataItemDialog(recordDataItem);
    }

    private void showSimpleRecordDataItemDialog(RecordDataItem recordDataItem) {

        BaseDialog dialog = new BaseDialog(bundleString("RecordDataItemViewer"), true);
        dialog.addDisplayComponentWithEmptyBorder(new SimpleDataItemViewerPanel(dialog, (SimpleRecordDataItem) recordDataItem));
        dialog.display();
    }

    private void showLobRecordDataItemDialog(RecordDataItem recordDataItem) {

        BaseDialog dialog = new BaseDialog(bundleString("LOBRecordDataItemViewer"), true);
        dialog.addDisplayComponentWithEmptyBorder(new LobDataItemViewerPanel(
                dialog,
                (LobRecordDataItem) recordDataItem,
                tableObject,
                ((ResultSetTableModel) ((TableSorter) table.getModel()).getTableModel())
                        .getRowDataForRow(table.getSelectedRow()))
        );
        dialog.display();
    }

    private ResultSetTableModel resultSetTableModel() {
        return (ResultSetTableModel) ((TableSorter) table.getModel()).getReferencedTableModel();
    }

    private void printResultSet(boolean printSelection) {

        JTable printTable = null;
        if (printSelection) {

            TableModel model = table.selectedCellsAsTableModel();
            if (model != null)
                printTable = new JTable(model);

        } else
            printTable = table;

        if (printTable != null)
            new PrintingSupport().print(new TablePrinter(printTable, null), "Red Expert - table");
    }

    private void maybeShowPopup(MouseEvent e) {

        if (e.isPopupTrigger()) {

            lastPopupPoint = e.getPoint();
            if (!table.hasMultipleColumnAndRowSelections())
                table.selectCellAtPoint(lastPopupPoint);

            show(e.getComponent(), lastPopupPoint.x, lastPopupPoint.y);

        } else {
            table.setColumnSelectionAllowed(true);
            table.setRowSelectionAllowed(true);
        }
    }

    // --- mouse listener ---

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (e.getClickCount() >= 2 &&
                (doubleClickCellOpensDialog || table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()) instanceof LobRecordDataItem)
        ) {
            lastPopupPoint = e.getPoint();
            openDataItemViewer();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // ---

    private String bundleString(String key) {
        return Bundles.get(ResultSetTablePopupMenu.class, key);
    }

}