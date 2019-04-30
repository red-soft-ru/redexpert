/*
 * TableSorter.java
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

package org.underworldlabs.swing.table;

import org.executequery.event.DefaultSortingEvent;
import org.executequery.event.SortingEvent;
import org.executequery.event.SortingListener;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.*;

/**
 * TableSorter is a decorator for TableModels; adding sorting
 * functionality to a supplied TableModel. TableSorter does
 * not store or copy the data in its TableModel; instead it maintains
 * a map from the row indexes of the view to the row indexes of the
 * model. As requests are made of the sorter (like getValueAt(row, col))
 * they are passed to the underlying model after the row numbers
 * have been translated via the internal mapping array. This way,
 * the TableSorter appears to hold another copy of the table
 * with the rows in a different order.
 * <p/>
 * TableSorter registers itself as a listener to the underlying model,
 * just as the JTable itself would. Events recieved from the model
 * are examined, sometimes manipulated (typically widened), and then
 * passed on to the TableSorter's listeners (typically the JTable).
 * If a change to the model has invalidated the order of TableSorter's
 * rows, a note of this is made and the sorter will resort the
 * rows the next time a value is requested.
 * <p/>
 * When the tableHeader property is set, either by using the
 * setTableHeader() method or the two argument constructor, the
 * table header may be used as a complete UI for TableSorter.
 * The default renderer of the tableHeader is decorated with a renderer
 * that indicates the sorting status of each column. In addition,
 * a mouse listener is installed with the following behavior:
 * <ul>
 * <li>
 * Mouse-click: Clears the sorting status of all other columns
 * and advances the sorting status of that column through three
 * values: {NOT_SORTED, ASCENDING, DESCENDING} (then back to
 * NOT_SORTED again).
 * <li>
 * SHIFT-mouse-click: Clears the sorting status of all other columns
 * and cycles the sorting status of the column through the same
 * three values, in the opposite order: {NOT_SORTED, DESCENDING, ASCENDING}.
 * <li>
 * CONTROL-mouse-click and CONTROL-SHIFT-mouse-click: as above except
 * that the changes to the column do not cancel the statuses of columns
 * that are already sorting - giving a way to initiate a compound
 * sort.
 * </ul>
 * <p/>
 * This is a long overdue rewrite of a class of the same name that
 * first appeared in the swing table demos in 1997.
 *
 * @author Philip Milne
 * @author Brendon McLean
 * @author Dan van Enckevort
 * @author Parwinder Sekhon
 * @author Takis Diakoumis
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class TableSorter extends AbstractTableModel {

    private List<SortingListener> sortingListeners;

    protected SortableTableModel tableModel;

    public static final int DESCENDING = -1;
    public static final int NOT_SORTED = 0;
    public static final int ASCENDING = 1;

    private static Directive EMPTY_DIRECTIVE = new Directive(-1, NOT_SORTED);

    private SortableHeaderRenderer headerRenderer;

    public static final Comparator COMPARABLE_COMAPRATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }
    };

    public static final Comparator LEXICAL_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };

    private Row[] viewToModel;
    private int[] modelToView;

    private JTableHeader tableHeader;
    private MouseListener mouseListener;
    private TableModelListener tableModelListener;
    private Map columnComparators = new HashMap();
    private List sortingColumns = new ArrayList();

    public TableSorter(SortableTableModel tableModel) {

        this(tableModel, null);
    }

    public TableSorter(SortableTableModel tableModel, JTableHeader tableHeader) {

        this.mouseListener = new MouseHandler();
        this.tableModelListener = new TableModelHandler();
        this.headerRenderer = new SortableHeaderRenderer(this);
        sortingListeners = new ArrayList<>();

        if (tableHeader != null) {

            setTableHeader(tableHeader);
        }

        if (tableModel != null) {

            setTableModel(tableModel);
        }

    }

    private void clearSortingState() {
        viewToModel = null;
        modelToView = null;
    }

    public TableModel getReferencedTableModel() {
        return tableModel;
    }

    public TableModel getTableModel() {

        return this.tableModel;
    }

    public void setTableModel(SortableTableModel tableModel) {

        if (this.tableModel != null) {

            this.tableModel.removeTableModelListener(tableModelListener);
        }

        this.tableModel = tableModel;
        if (this.tableModel != null) {

            this.tableModel.addTableModelListener(tableModelListener);
        }

        reset();
    }

    public void reset() {
        sortingColumns.clear();
        clearSortingState();
        fireTableStructureChanged();
    }

    public JTableHeader getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(JTableHeader tableHeader) {

        this.tableHeader = tableHeader;
        if (tableHeader != null) {
            tableHeader.addMouseListener(mouseListener);
            tableHeader.setDefaultRenderer(headerRenderer);
        }

    }

    public void setTableHeaderRenderer(SortableHeaderRenderer headerRenderer) {
        this.headerRenderer = headerRenderer;
        tableHeader.setDefaultRenderer(headerRenderer);
    }

    public boolean isSorting() {
        return sortingColumns.size() != 0;
    }

    private Directive getDirective(int column) {
        for (int i = 0, n = sortingColumns.size(); i < n; i++) {
            Directive directive = (Directive) sortingColumns.get(i);
            if (directive.column == column) {
                return directive;
            }
        }
        return EMPTY_DIRECTIVE;
    }

    public int getSortingStatus(int column) {
        return getDirective(column).direction;
    }

    private void sortingStatusChanged() {
        clearSortingState();
        fireTableDataChanged();

        if (tableHeader != null) {
            tableHeader.repaint();
        }
    }

    public void setSortingStatus(int column, int status) {
        Directive directive = getDirective(column);
        if (directive != EMPTY_DIRECTIVE) {
            sortingColumns.remove(directive);
        }
        if (status != NOT_SORTED) {
            sortingColumns.add(new Directive(column, status));
        }
        sortingStatusChanged();
    }

    public Icon getHeaderRendererIcon(int column, int size) {
        Directive directive = getDirective(column);

        if (directive == EMPTY_DIRECTIVE) {
            return null;
        }

        boolean isAscend = directive.direction == ASCENDING;
        return new ArrowIcon(isAscend ? ArrowIcon.DOWN : ArrowIcon.UP);
    }

    public int getHeaderRendererIcon(int column) {
        Directive directive = getDirective(column);
        if (directive == EMPTY_DIRECTIVE) {
            return -1;
        }

        boolean isAscend = (directive.direction == ASCENDING);
        return isAscend ? ArrowIcon.UP : ArrowIcon.DOWN;
    }

    private void cancelSorting() {
        sortingColumns.clear();
        sortingStatusChanged();
    }

    public void setColumnComparator(Class type, Comparator comparator) {
        if (comparator == null) {
            columnComparators.remove(type);
        } else {
            columnComparators.put(type, comparator);
        }
    }

    protected Comparator getComparator(int column) {

        Class columnType = tableModel.getColumnClass(column);
        Comparator comparator = (Comparator) columnComparators.get(columnType);

        if (comparator != null) {
            return comparator;
        }


        if (Comparable.class.isAssignableFrom(columnType)) {
            return COMPARABLE_COMAPRATOR;
        }

        return LEXICAL_COMPARATOR;
    }

    private Row[] getViewToModel() {

        if (viewToModel == null) {

            int tableModelRowCount = tableModel.getRowCount();

            viewToModel = new Row[tableModelRowCount];
            for (int row = 0; row < tableModelRowCount; row++) {

                viewToModel[row] = new Row(row);
            }

            if (isSorting()) {

                Arrays.sort(viewToModel);
            }

        }

        return viewToModel;
    }

    public int modelIndex(int viewIndex) {

        Row[] rows = getViewToModel();
        if (viewIndex >= rows.length) {
            return -1;
        }

        return rows[viewIndex].modelIndex;
    }

    private int[] getModelToView() {
        if (modelToView == null) {
            int n = getViewToModel().length;
            modelToView = new int[n];
            for (int i = 0; i < n; i++) {

                int modelIndex = modelIndex(i);
                if (modelIndex == -1) {
                    break;
                }

                modelToView[modelIndex] = i;
            }
        }
        return modelToView;
    }

    // TableModel interface methods

    public int getRowCount() {
        return (tableModel == null) ? 0 : tableModel.getRowCount();
    }

    public int getColumnCount() {
        return (tableModel == null) ? 0 : tableModel.getColumnCount();
    }

    public String getColumnName(int column) {
        return tableModel.getColumnName(column);
    }

    public Class<?> getColumnClass(int column) {
        return tableModel.getColumnClass(column);
    }

    public boolean isCellEditable(int row, int column) {

        int modelIndex = modelIndex(row);
        if (modelIndex == -1) {
            return false;
        }

        return tableModel.isCellEditable(modelIndex, column);
    }

    public Object getValueAt(int row, int column) {

        if (row >= getRowCount() || column >= getColumnCount()) {
            return null;
        }

        int modelIndex = modelIndex(row);
        if (modelIndex == -1) {
            return null;
        }

        return tableModel.getValueAt(modelIndex, column);
    }

    public void setValueAt(Object aValue, int row, int column) {

        int modelIndex = modelIndex(row);
        if (modelIndex == -1) {
            return;
        }

        tableModel.setValueAt(aValue, modelIndex, column);
    }

    public void addSortingListener(SortingListener listener) {
        sortingListeners.add(listener);
    }

    public void removeSortingListener(SortingListener listener) {
        sortingListeners.remove(listener);
    }

    public void clearSortingListeners() {
        sortingListeners.clear();
    }

    // Helper classes

    private class Row implements Comparable {

        private int modelIndex;

        public Row(int index) {

            this.modelIndex = index;
        }

        public int compareTo(Object o) {

            int row1 = modelIndex;
            int row2 = ((Row) o).modelIndex;

            for (Iterator it = sortingColumns.iterator(); it.hasNext(); ) {

                Directive directive = (Directive) it.next();

                int column = directive.column;

                Object o1 = valueToCompareFromModel(row1, column);
                Object o2 = valueToCompareFromModel(row2, column);

                int comparison = 0;

                // Define null less than everything, except null.
                if (o1 == null && o2 == null) {

                    comparison = 0;

                } else if (o1 == null) {

                    comparison = -1;

                } else if (o2 == null) {

                    comparison = 1;

                } else {

                    Class type = tableModel.getColumnClass(column);

                    if (columnComparators.containsKey(type)) {

                        comparison = ((Comparator) columnComparators.get(type)).compare(o1, o2);

                    } else {

                        comparison = compareByColumn(type, o1, o2);
                        //  comparison = getComparator(column).compare(o1, o2);
                    }

                }

                if (comparison != 0) {

                    return directive.direction == DESCENDING ? -comparison : comparison;
                }

            }

            return 0;
        }

        private int compareByColumn(Class type, Object o1, Object o2) {

            if (type.getSuperclass() == java.lang.Number.class) {

                return compareAsNumber(o1, o2);

            } else if (type == java.util.Date.class) {

                return compareAsDate(o1, o2);

            } else if (type == String.class) {

                return compareAsString(o1, o2);

            } else if (type == Boolean.class) {

                return compareAsBoolean(o1, o2);

            } else {

                try {

                    return compareAsNumber(o1, o2);

                } catch (ClassCastException e) {

                    return compareAsString(o1, o2);
                }

            }

        }

        private int compareAsNumber(Object o1, Object o2) {

            Number n1 = (Number) o1;
            double d1 = n1.doubleValue();
            Number n2 = (Number) o2;
            double d2 = n2.doubleValue();

            if (d1 < d2)
                return -1;
            else if (d1 > d2)
                return 1;
            else
                return 0;
        }

        private int compareAsDate(Object o1, Object o2) {

            long n1 = -1;
            try {
                Date d1 = (Date) o1;
                n1 = d1.getTime();
            } catch (ClassCastException e) {
            }

            long n2 = -1;
            try {
                Date d2 = (Date) o2;
                n2 = d2.getTime();
            } catch (ClassCastException e) {
            }

            if (n1 < n2)
                return -1;
            else if (n1 > n2)
                return 1;
            else
                return 0;
        }

        private int compareAsString(Object o1, Object o2) {

            try {

                String s1 = o1.toString();
                String s2 = o2.toString();
                int result = s1.compareTo(s2);

                if (result < 0)
                    return -1;
                else if (result > 0)
                    return 1;
                else
                    return 0;

            } catch (ClassCastException e) {

                return 0;
            }
        }

        private int compareAsBoolean(Object o1, Object o2) {

            Boolean bool1 = (Boolean) o1;
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) o2;
            boolean b2 = bool2.booleanValue();

            if (b1 == b2)
                return 0;
            else if (b1) // Define false < true
                return 1;
            else
                return -1;
        }

        private Object valueToCompareFromModel(int row, int column) {

            Object object = tableModel.getValueAt(row, column);

            if (object instanceof TableCellValue) {

                object = ((TableCellValue) object).getValue();
            }

            return object;
        }

    } // class Row

    private class TableModelHandler implements TableModelListener {

        public void tableChanged(TableModelEvent e) {

            // If we're not sorting by anything, just pass the event along.
            if (!isSorting()) {
                clearSortingState();
                fireTableChanged(e);
                return;
            }

            // If the table structure has changed, cancel the sorting; the
            // sorting columns may have been either moved or deleted from
            // the model.
            if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                cancelSorting();
                fireTableChanged(e);
                return;
            }

            // We can map a cell event through to the view without widening
            // when the following conditions apply:
            //
            // a) all the changes are on one row (e.getFirstRow() == e.getLastRow()) and,
            // b) all the changes are in one column (column != TableModelEvent.ALL_COLUMNS) and,
            // c) we are not sorting on that column (getSortingStatus(column) == NOT_SORTED) and,
            // d) a reverse lookup will not trigger a sort (modelToView != null)
            //
            // Note: INSERT and DELETE events fail this test as they have column == ALL_COLUMNS.
            //
            // The last check, for (modelToView != null) is to see if modelToView
            // is already allocated. If we don't do this check; sorting can become
            // a performance bottleneck for applications where cells
            // change rapidly in different parts of the table. If cells
            // change alternately in the sorting column and then outside of
            // it this class can end up re-sorting on alternate cell updates -
            // which can be a performance problem for large tables. The last
            // clause avoids this problem.
            int column = e.getColumn();
            if (e.getFirstRow() == e.getLastRow()
                    && column != TableModelEvent.ALL_COLUMNS
                    && getSortingStatus(column) == NOT_SORTED
                    && modelToView != null) {
                int viewIndex = getModelToView()[e.getFirstRow()];
                fireTableChanged(new TableModelEvent(TableSorter.this,
                        viewIndex, viewIndex,
                        column, e.getType()));
                return;
            }

            // Something has happened to the data that may have invalidated the row order.
            clearSortingState();
            fireTableDataChanged();
            return;
        }
    }

    private class MouseHandler extends MouseAdapter {

        int column, status;

        public void mouseClicked(MouseEvent e) {

            if (e.getButton() > 1) {

                return;
            }

            JTableHeader header = (JTableHeader) e.getSource();
            TableColumnModel columnModel = header.getColumnModel();

            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            if (viewColumn == -1) {

                return;
            }

            int x = e.getX();
            int n = columnModel.getColumnCount();

            int resizeColumn = -1;
            for (int i = 0; i < n; i++) {
                x = x - columnModel.getColumn(i).getWidth();
                if (x > -5 && x < 5) { // within 5px either side

                    resizeColumn = i;
                    break;
                }
            }

            if (e.getClickCount() >= 2 && resizeColumn != -1) {

                final int selectedColumn = resizeColumn;
                SwingWorker worker = new SwingWorker() {
                    public Object construct() {
                        resizeColumn(selectedColumn);
                        return "done";
                    }
                };
                worker.start();
                return;

            } else if (resizeColumn == -1) {

                column = columnModel.getColumn(viewColumn).getModelIndex();
                if (column != -1 && tableModel.canSortColumn(column)) {

                    status = getSortingStatus(column);

                    if (!e.isControlDown()) {

                        cancelSorting();
                    }

                    // Cycle the sorting states through {NOT_SORTED, ASCENDING, DESCENDING} or
                    // {NOT_SORTED, DESCENDING, ASCENDING} depending on whether shift is pressed.
                    status = status + (e.isShiftDown() ? -1 : 1);
                    status = (status + 4) % 3 - 1; // signed mod, returning {-1, 0, 1}

                    SwingWorker worker = new SwingWorker() {
                        public Object construct() {
                            setSortingStatus(column, status);
                            for (SortingListener listener : sortingListeners) {
                                listener.postsorting(new DefaultSortingEvent(TableSorter.this, SortingEvent.POSTSORTING));
                            }
                            return "done";
                        }
                    };
                    for (SortingListener listener : sortingListeners) {
                        listener.presorting(new DefaultSortingEvent(TableSorter.this, SortingEvent.PRESORTING));
                    }
                    worker.start();

                }

            }

        }

        private void resizeColumn(int selectedColumn) {

            TableColumnModel columnModel = tableHeader.getColumnModel();
            TableColumn tableColumn = columnModel.getColumn(selectedColumn);
            int size = sizeToFit(selectedColumn);
            if (tableColumn.getWidth() < size) {

                tableColumn.setPreferredWidth(size + 10);
            }

        }

        private int sizeToFit(int selectedColumn) {

            JTable table = getTableHeader().getTable();
            Font font = table.getFont();
            FontMetrics fontMetrics = table.getFontMetrics(font);

            int longestValue = 0;
            for (int i = 0, n = tableModel.getRowCount(); i < n; i++) {

                Object object = tableModel.getValueAt(i, selectedColumn);
                if (object != null) {

                    String stringValue = object.toString();
                    if (stringValue != null) {

                        longestValue = Math.max(longestValue, fontMetrics.stringWidth(stringValue));
                    }

                }
            }

            // check table header label too
            String label = tableModel.getColumnName(selectedColumn);

            JTableHeader header = getTableHeader();
            font = header.getFont();
            fontMetrics = header.getFontMetrics(font);

            longestValue = Math.max(longestValue, fontMetrics.stringWidth(label));

            return longestValue;
        }

    } // class MouseHandler
    
/*
    private class SortableHeaderRenderer extends JButton
                                         implements TableCellRenderer {
 
      public SortableHeaderRenderer() {
        setMargin(btnMargin);
      }
 
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {
 
        String label = value.toString();
        setText(label);
        setToolTipText(label);
 
        setHorizontalTextPosition(SwingConstants.LEFT);
        int modelColumn = table.convertColumnIndexToModel(column);
        setIcon(getHeaderRendererIcon(modelColumn, getFont().getSize()));
 
        return this;
 
      }
 
    } // class SortableHeaderRenderer
 */

    private static class Directive {

        private int column;
        private int direction;

        public Directive(int column, int direction) {
            this.column = column;
            this.direction = direction;
        }

    } // class Directive

}

