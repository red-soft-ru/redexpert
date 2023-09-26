/*
 * ResultSetTable.java
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

package org.executequery.gui.resultset;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.gui.StandardTable;
import org.underworldlabs.swing.DateCellEditor;
import org.underworldlabs.swing.DateTimeCellEditor;
import org.underworldlabs.swing.ForeignKeyCellEditor;
import org.underworldlabs.swing.TimeCellEditor;
import org.underworldlabs.swing.table.MultiLineStringCellEditor;
import org.underworldlabs.swing.table.StringCellEditor;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.swing.table.ValidatedNumberCellEditor;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.sql.Types;
import java.util.*;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ResultSetTable extends JTable implements StandardTable {

    private DefaultCellEditor defaultCellEditor;
    private DefaultCellEditor bigintCellEditor;
    private DefaultCellEditor integerCellEditor;
    private DefaultCellEditor smallintCellEditor;
    private DefaultCellEditor multiLineCellEditor;
    private DateCellEditor dateEditor;
    private DateTimeCellEditor dateTimeCellEditor;
    private TimeCellEditor timeCellEditor;

    private boolean isAutoResizeable;
    private List<Integer> foreignColumnsIndexes;
    private ResultsTableColumnModel columnModel;
    private ResultSetTableCellRenderer cellRenderer;

    private final TableColumn dummyColumn = new TableColumn();
    private final ArrayList<ForeignData> foreignColumnsData = new ArrayList<>();

    public ResultSetTable() {

        super();
        setDefaultOptions();

        // --- stringCellEditor ---

        final StringCellEditor stringCellEditor = new StringCellEditor();
        stringCellEditor.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        defaultCellEditor = new DefaultCellEditor(stringCellEditor) {
            @Override
            public Object getCellEditorValue() {
                return stringCellEditor.getValue();
            }
        };

        // --- multiLineStringCellEditor ---

        final MultiLineStringCellEditor multiLineStringCellEditor = new MultiLineStringCellEditor();
        multiLineStringCellEditor.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        multiLineCellEditor = new DefaultCellEditor(multiLineStringCellEditor) {
            @Override
            public Object getCellEditorValue() {
                return multiLineStringCellEditor.getValue();
            }
        };

        // --- bigintEditor ---

        final ValidatedNumberCellEditor bigintEditor = new ValidatedNumberCellEditor(Types.BIGINT);
        bigintEditor.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        bigintCellEditor = new DefaultCellEditor(bigintEditor) {
            @Override
            public Object getCellEditorValue() {
                return bigintEditor.getValue();
            }
        };

        // --- integerEditor ---

        final ValidatedNumberCellEditor integerEditor = new ValidatedNumberCellEditor(Types.INTEGER);
        integerEditor.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        integerCellEditor = new DefaultCellEditor(integerEditor) {
            @Override
            public Object getCellEditorValue() {
                return integerEditor.getValue();
            }
        };

        // --- smallintEditor ---

        final ValidatedNumberCellEditor smallintEditor = new ValidatedNumberCellEditor(Types.SMALLINT);
        smallintEditor.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        smallintCellEditor = new DefaultCellEditor(smallintEditor) {
            @Override
            public Object getCellEditorValue() {
                return smallintEditor.getValue();
            }
        };

        // ---

        dateEditor = new DateCellEditor();
        dateTimeCellEditor = new DateTimeCellEditor();
        timeCellEditor = new TimeCellEditor();

        isAutoResizeable = false;
    }

    public ResultSetTable(TableModel model) {
        super(model);
        setDefaultOptions();

        isAutoResizeable = false;
    }

    private void setDefaultOptions() {

        setColumnSelectionAllowed(true);
        columnModel = new ResultsTableColumnModel();
        setColumnModel(columnModel);
        setDefaultColumnOptions();

        cellRenderer = new ResultSetTableCellRenderer();
        cellRenderer.setFont(getFont());

        applyUserPreferences();
    }

    private void setDefaultColumnOptions() {

        foreignColumnsIndexes = new ArrayList<>();
        int cols = dataModel.getColumnCount();
        if (columnModel != null)
            columnModel.initTCS(cols);

    }

    @Override
    public void setModel(TableModel model) {
        super.setModel(model);
        setDefaultColumnOptions();
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {

            @Override
            public String getToolTipText(MouseEvent e) {

                if (getModel() instanceof TableSorter) {

                    TableSorter model = (TableSorter) getModel();
                    if (model.getTableModel() instanceof ResultSetTableModel) {

                        ResultSetTableModel resultSetTableModel = (ResultSetTableModel) model.getTableModel();

                        Point point = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(point.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();

                        return resultSetTableModel.getColumnNameHint(realIndex);
                    }
                }

                return super.getToolTipText(e);
            }
        };
    }

    public void selectCellAtPoint(Point point) {

        int row = rowAtPoint(point);
        int col = columnAtPoint(point);

        setColumnSelectionInterval(col, col);
        setRowSelectionInterval(row, row);
    }

    public boolean hasMultipleColumnAndRowSelections() {
        return (getSelectedColumnCount() > 1 || getSelectedRowCount() > 1);
    }

    public void selectRow(Point point) {

        if (point != null) {

            setColumnSelectionAllowed(false);
            setRowSelectionAllowed(true);

            int selectedRowCount = getSelectedRowCount();
            if (selectedRowCount > 1) {

                int[] selectedRows = getSelectedRows();
                setRowSelectionInterval(selectedRows[0], selectedRows[selectedRows.length - 1]);

            } else {

                clearSelection();
                int row = rowAtPoint(point);
                setRowSelectionInterval(row, row);
            }
        }

    }

    public void selectColumn(Point point) {

        if (point != null) {

            setColumnSelectionAllowed(true);
            setRowSelectionAllowed(false);

            int columnCount = getSelectedColumnCount();
            if (columnCount > 1) {

                int[] selectedColumns = getSelectedColumns();
                setColumnSelectionInterval(selectedColumns[0], selectedColumns[selectedColumns.length - 1]);

            } else {

                clearSelection();
                int column = columnAtPoint(point);
                setColumnSelectionInterval(column, column);
            }
        }

    }

    public void copySelectedCells() {
        copySelectedCells('\t', false, false);
    }

    public void copySelectedCellsAsCSV() {
        copySelectedCells(',', false, false);
    }

    public void copySelectedCellsAsCSVWithNames() {
        copySelectedCells(',', false, true);
    }

    public void copySelectedCellsAsCSVQuoted() {
        copySelectedCells(',', true, false);
    }

    public void copySelectedCellsAsCSVQuotedWithNames() {
        copySelectedCells(',', true, true);
    }

    private void copySelectedCells(char delimiter, boolean quoted, boolean withNames) {

        StringBuilder sb = new StringBuilder();
        int cols = getSelectedColumnCount();
        int rows = getSelectedRowCount();

        if (cols == 0 && rows == 0)
            return;

        int[] selectedRows = getSelectedRows();
        int[] selectedCols = getSelectedColumns();

        if (withNames) {
            sb.append("#");
            List<String> list = new ArrayList<>();
            for (int j = 0; j < cols; j++)
                list.add(getColumnName(selectedCols[j]));
            sb.append(StringUtils.join(list, delimiter)).append('\n');
        }

        String quote = quoted ? "'" : "";
        for (int i = 0; i < rows; i++) {

            for (int j = 0; j < cols; j++) {
                sb.append(quote);
                sb.append(getValueAt(selectedRows[i], selectedCols[j]));
                sb.append(quote);
                sb.append(delimiter);
            }

            if (cols > 1)
                sb.deleteCharAt(sb.length() - 1);
            if (i < rows - 1)
                sb.append('\n');
        }

        if (cols == 1)
            sb.deleteCharAt(sb.length() - 1);

        GUIUtilities.copyToClipBoard(sb.toString());
    }

    public Object valueAtPoint(Point point) {
        return getValueAt(rowAtPoint(point), columnAtPoint(point));
    }

    public int[] getSelectedCellsRowsIndexes() {
        return getSelectedRows();
    }

    public int[] getSelectedCellsColumnsIndexes() {
        return getSelectedColumns();
    }

    public TableModel selectedCellsAsTableModel() {

        int cols = getSelectedColumnCount();
        int rows = getSelectedRowCount();

        if (cols == 0 && rows == 0)
            return null;

        int[] selectedRows = getSelectedRows();
        int[] selectedCols = getSelectedColumns();

        Vector data = new Vector(rows);
        Vector columns = new Vector(cols);

        for (int i = 0; i < rows; i++) {

            Vector rowVector = new Vector(cols);
            for (int j = 0; j < cols; j++) {

                rowVector.add(getValueAt(selectedRows[i], selectedCols[j]));
                if (i == 0)
                    columns.add(getColumnName(selectedCols[j]));
            }

            data.add(rowVector);
        }

        return new DefaultTableModel(data, columns);
    }

    public void applyUserPreferences() {

        setDragEnabled(true);
        setCellSelectionEnabled(true);

        setBackground(SystemProperties.getColourProperty(
                "user", "results.table.cell.background.colour"));

        setRowHeight(SystemProperties.getIntProperty(
                "user", "results.table.column.height"));

        setRowSelectionAllowed(SystemProperties.getBooleanProperty(
                "user", "results.table.row.select"));

        getTableHeader().setResizingAllowed(SystemProperties.getBooleanProperty(
                "user", "results.table.column.resize"));

        getTableHeader().setReorderingAllowed(SystemProperties.getBooleanProperty(
                "user", "results.table.column.reorder"));

        setTableColumnWidth(getUserPreferredColumnWidth());

        cellRenderer.applyUserPreferences();
    }

    @Override
    public void setBackground(Color background) {
        if (cellRenderer != null)
            cellRenderer.setTableBackground(background);
        super.setBackground(background);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (cellRenderer != null)
            cellRenderer.setFont(font);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return cellRenderer;
    }

    public Boolean isComboColumn(int index) {
        return foreignColumnsIndexes.contains(index);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {

        RecordDataItem value = (RecordDataItem) getValueAt(row, column);
        if (isComboColumn(column)) {

            int columnIndex = 0;
            for (int i = 0; i < foreignColumnsData.size(); i++) {
                if (foreignColumnsData.get(i).getColumnIndex() == column) {
                    columnIndex = i;
                    break;
                }
            }

            return new ForeignKeyCellEditor(
                    getModel(),
                    foreignColumnsData.get(columnIndex).getTableModel(),
                    foreignColumnsData.get(columnIndex).getItems(),
                    foreignColumnsData.get(columnIndex).getNames(),
                    value.getValue(), row,
                    foreignColumnsData.get(columnIndex).getChildColumnIndexes());
        }

        int sqlType = value.getDataType();
        switch (sqlType) {

            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
                return multiLineCellEditor;

            case Types.BIGINT:
                return bigintCellEditor;

            case Types.INTEGER:
                return integerCellEditor;

            case Types.SMALLINT:
                return smallintCellEditor;

            case Types.DATE:
                return dateEditor;

            case Types.TIMESTAMP:
                dateTimeCellEditor.getDateTimePicker().setVisibleTimeZone(false);
                return dateTimeCellEditor;

            case Types.TIMESTAMP_WITH_TIMEZONE:
                dateTimeCellEditor.getDateTimePicker().setVisibleTimeZone(true);
                return dateTimeCellEditor;

            case Types.TIME_WITH_TIMEZONE:
                timeCellEditor.getPicker().setVisibleTimeZone(true);
                return timeCellEditor;

            case Types.TIME:
                timeCellEditor.getPicker().setVisibleTimeZone(false);
                return timeCellEditor;

            case Types.BOOLEAN:
                JComboBox comboBox = new JComboBox(new String[]{"true", "false", "null"});
                String booleanValue = String.valueOf(value.getValue());

                if (MiscUtils.isNull(booleanValue))
                    comboBox.setSelectedItem(2);
                else if (booleanValue.equalsIgnoreCase("true"))
                    comboBox.setSelectedIndex(0);
                else if (booleanValue.equalsIgnoreCase("false"))
                    comboBox.setSelectedIndex(1);
                else
                    comboBox.setSelectedItem(2);
                return new DefaultCellEditor(comboBox);

        }

        return defaultCellEditor;
    }

    private int getUserPreferredColumnWidth() {
        return SystemProperties.getIntProperty("user", "results.table.column.width");
    }

    @Override
    public void setTableColumnWidth(int columnWidth) {

        TableColumnModel tcm = getColumnModel();
        if (columnWidth != 75) {

            TableColumn col;
            for (Enumeration<TableColumn> i = tcm.getColumns(); i.hasMoreElements(); ) {
                col = i.nextElement();
                col.setWidth(columnWidth);
                col.setPreferredWidth(columnWidth);
            }
        }

    }

    public void setTableColumnWidthFromContents() {

        if (getModel() instanceof ResultSetTableModel) {
            ResultSetTableModel tableModel = (ResultSetTableModel) getModel();
            setColWidthFromContents(tableModel);

        } else if (getModel() instanceof TableSorter) {
            if (((TableSorter) getModel()).getTableModel() instanceof ResultSetTableModel) {
                ResultSetTableModel tableModel = (ResultSetTableModel) ((TableSorter) getModel()).getTableModel();
                setColWidthFromContents(tableModel);
            }
        }
    }

    private void setColWidthFromContents(ResultSetTableModel tableModel) {

        TableColumnModel tcm = getColumnModel();
        TableColumn col;

        for (int i = 0; i < tcm.getColumnCount(); i++) {
            ResultSetColumnHeader header = tableModel.getColumnHeaders().get(i);
            col = tcm.getColumn(i);
            col.setWidth(header.getColWidth());
            col.setPreferredWidth(header.getColWidth());
        }

    }

    public void setForeignKeyTable(
            int ind, DefaultTableModel defaultTableModel, Vector<Vector<Object>> items,
            Vector<String> names, int[] childColumnIndexes) {

        foreignColumnsIndexes.add(ind);
        foreignColumnsData.add(new ForeignData(ind, defaultTableModel, items, names, childColumnIndexes));
        columnModel.setColumn(new TableColumn(), ind);
    }

    public void columnVisibilityChanged() {
        ResultSetTableModel model = (ResultSetTableModel) ((TableSorter) getModel()).getTableModel();
        model.fireTableStructureChanged();
        applyUserPreferences();
    }

    public void stopEditing() {
        if (isEditing())
            getCellEditor().stopCellEditing();
    }

    public void switchAutoResizeMode() {
        isAutoResizeable = !isAutoResizeable;
        setAutoResizeMode(isAutoResizeable ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);
    }

    class ResultsTableColumnModel extends DefaultTableColumnModel {

        Vector<TableColumn> tcs;

        // dumb work-around for update issue noted
        public void initTCS(int cols) {
            tcs = new Vector<>();
            for (int i = 0; i < cols; i++)
                tcs.add(new TableColumn());
        }

        @Override
        public TableColumn getColumn(int columnIndex) {
            try {
                return super.getColumn(columnIndex);
            } catch (Exception e) {
                return dummyColumn;
            }
        }

        public void setColumn(TableColumn column, int index) {
            tcs.set(index, column);
        }

    } // class ResultsTableColumnModel

    static class ComboBoxRenderer extends JTable implements ListCellRenderer {

        public DefaultTableModel rowTableModel;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setModel(rowTableModel);
            return this;
        }

    } // class ComboBoxRenderer

    private static class ForeignData {

        private final int columnIndex;
        private final int[] childColumnIndexes;
        private final DefaultTableModel tableModel;
        private final Vector<Vector<Object>> items;
        private final Vector<String> names;

        public ForeignData(
                int columnIndex, DefaultTableModel tableModel, Vector<Vector<Object>> items,
                Vector<String> names, int[] childColumnIndexes) {

            this.columnIndex = columnIndex;
            this.tableModel = tableModel;
            this.items = items;
            this.names = names;
            this.childColumnIndexes = childColumnIndexes;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public DefaultTableModel getTableModel() {
            return tableModel;
        }

        public Vector<Vector<Object>> getItems() {
            return items;
        }

        public Vector<String> getNames() {
            return names;
        }

        public int[] getChildColumnIndexes() {
            return childColumnIndexes;
        }

    } // class ForeignData

}
