/*
 * ResultSetTable.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.gui.StandardTable;
import org.underworldlabs.swing.table.MultiLineStringCellEditor;
import org.underworldlabs.swing.table.StringCellEditor;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.util.SystemProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1546 $
 * @date     $Date: 2015-12-22 15:54:37 +1100 (Tue, 22 Dec 2015) $
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class ResultSetTable extends JTable implements StandardTable {

    private DefaultCellEditor defaultCellEditor;

    private DefaultCellEditor multiLineCellEditor;
    
    private ResultsTableColumnModel columnModel;

    private ResultSetTableCellRenderer cellRenderer;

    private TableColumn dummyColumn = new TableColumn();

    public ResultSetTable() {

        super();
        setDefaultOptions();

        final StringCellEditor stringCellEditor = new StringCellEditor();
        stringCellEditor.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        defaultCellEditor = new DefaultCellEditor(stringCellEditor) {
            public Object getCellEditorValue() {
                return stringCellEditor.getValue(); }
        };

        final MultiLineStringCellEditor multiLineStringCellEditor = new MultiLineStringCellEditor();
        multiLineStringCellEditor.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        multiLineCellEditor = new DefaultCellEditor(multiLineStringCellEditor) {
            public Object getCellEditorValue() {
                return multiLineStringCellEditor.getValue(); }
        };
        
    }

    public ResultSetTable(TableModel model) {

        super(model);
        setDefaultOptions();
    }

    private void setDefaultOptions() {

        setColumnSelectionAllowed(true);
        columnModel = new ResultsTableColumnModel();
        setColumnModel(columnModel);

        cellRenderer = new ResultSetTableCellRenderer();
        cellRenderer.setFont(getFont());

        applyUserPreferences();
    }

    protected JTableHeader createDefaultTableHeader() {

        return new JTableHeader(columnModel) {
        
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

        int cols = getSelectedColumnCount();
        int rows = getSelectedRowCount();

        return (cols > 1 || rows > 1);
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
        
        /*
        
        StringBuilder sb = new StringBuilder();

        int cols = getSelectedColumnCount();
        int rows = getSelectedRowCount();

        if (cols == 0 && rows == 0) {

            return;
        }

        int[] selectedRows = getSelectedRows();
        int[] selectedCols = getSelectedColumns();

        for (int i = 0; i < rows; i++) {

            for (int j = 0; j < cols; j++) {

                sb.append(getValueAt(selectedRows[i], selectedCols[j]));

                if (j < cols - 1) {

                    sb.append('\t');
                }

            }

            if (i < rows - 1) {

                sb.append('\n');
            }

        }

        GUIUtilities.copyToClipBoard(sb.toString());
        
        */
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
        
        if (cols == 0 && rows == 0) {
            
            return;
        }
        
        int[] selectedRows = getSelectedRows();
        int[] selectedCols = getSelectedColumns();

        if (withNames) {
            
            sb.append("#");
            List<String> list = new ArrayList<String>();
            for (int j = 0; j < cols; j++) {
    
                list.add(getColumnName(selectedCols[j]));
            }
    
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
            
            if (cols > 1) {
             
                sb.deleteCharAt(sb.length() - 1);
            }

            if (i < rows - 1) {
                
                sb.append('\n');
            }

        }

        if (cols == 1) {

            sb.deleteCharAt(sb.length() - 1);            
        }

        GUIUtilities.copyToClipBoard(sb.toString());
    }
    
    public Object valueAtPoint(Point point) {

        int row = rowAtPoint(point);
        int col = columnAtPoint(point);

        return getValueAt(row, col);
    }

    public TableModel selectedCellsAsTableModel() {

        int cols = getSelectedColumnCount();
        int rows = getSelectedRowCount();

        if (cols == 0 && rows == 0) {
            return null;
        }

        int[] selectedRows = getSelectedRows();
        int[] selectedCols = getSelectedColumns();

        Vector data = new Vector(rows);
        Vector columns = new Vector(cols);

        for (int i = 0; i < rows; i++) {

            Vector rowVector = new Vector(cols);

            for (int j = 0; j < cols; j++) {

                rowVector.add(getValueAt(selectedRows[i], selectedCols[j]));

                if (i == 0) {

                    columns.add(getColumnName(selectedCols[j]));
                }

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

    public void resetTableColumnWidth() {
        setTableColumnWidth(getUserPreferredColumnWidth());
    }

    public void setBackground(Color background) {
        if (cellRenderer != null) {
            cellRenderer.setTableBackground(background);
        }
        super.setBackground(background);
    }

    public void setFont(Font font) {
        super.setFont(font);
        if (cellRenderer != null) {
            cellRenderer.setFont(font);
        }
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        return cellRenderer;
    }

    public TableCellEditor getCellEditor(int row, int column) {
        
        RecordDataItem value = (RecordDataItem) getValueAt(row, column);
        int sqlType = value.getDataType();
        switch (sqlType) {

            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.CLOB:
                return multiLineCellEditor;
        }
        
        return defaultCellEditor;
    }

    private int getUserPreferredColumnWidth() {
        return SystemProperties.getIntProperty("user", "results.table.column.width");
    }

    public void setTableColumnWidth(int columnWidth) {

        TableColumnModel tcm = getColumnModel();
        if (columnWidth != 75) {

            TableColumn col = null;
            for (Enumeration<TableColumn> i = tcm.getColumns(); i.hasMoreElements();) {
                col = i.nextElement();
                col.setWidth(columnWidth);
                col.setPreferredWidth(columnWidth);
            }

        }
    }

    class ResultsTableColumnModel extends DefaultTableColumnModel {

        // dumb work-around for update issue noted
        public TableColumn getColumn(int columnIndex) {
            try {
                return super.getColumn(columnIndex);
            } catch (Exception e) {
                return dummyColumn;
            }
        }

    } // class ResultsTableColumnModel

    
    
    class ResultSetTableHeader extends JTableHeader {
        
        @Override
        public String getToolTipText(MouseEvent event) {

            return super.getToolTipText(event);
        }
        
    }

    public void columnVisibilityChanged() {

        ResultSetTableModel model = (ResultSetTableModel) ((TableSorter) getModel()).getTableModel();
        model.fireTableStructureChanged();
        applyUserPreferences();
    }
    
}
