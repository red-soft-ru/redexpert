/*
 * ResultSetPanel.java
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

import org.executequery.UserPreferencesManager;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.underworldlabs.swing.table.RowNumberHeader;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple SQL result set display panel.
 *
 * @author Takis Diakoumis
 */
public class ResultSetPanel extends JPanel {

    /**
     * the table display
     */
    private ResultSetTable table;

    /**
     * the table model
     */
    private ResultSetTableModel model;

    /**
     * the table scroll pane
     */
    private JScrollPane scroller;

    /**
     * the table sorter
     */
    private TableSorter sorter;

    /**
     * whether to display the row header
     */
    private boolean showRowHeader;

    /**
     * the associated meta data panel
     */
    private ResultSetMetaDataPanel metaDataPanel;

    /**
     * table pop-up menu
     */
    private ResultSetTablePopupMenu popupMenu;

    /**
     * the row number header
     */
    private RowNumberHeader rowNumberHeader;

    private final ResultSetTableContainer resultSetTableContainer;

    /**
     * Creates a new instance of ResultSetPanel
     */
    public ResultSetPanel(ResultSetTableContainer resultSetTableContainer) {

        super(new BorderLayout());
        this.resultSetTableContainer = resultSetTableContainer;
        init();
    }

    private void init() {

        Color bg = SystemProperties.getColourProperty("user",
                "editor.results.background.colour");
        table = new ResultSetTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (super.isCellEditable(row, column))
                    return !UserPreferencesManager.doubleClickOpenItemView();
                else return false;
            }
        };

        // this is set for the bg of any remaining
        // header region outside the cells themselves
        table.getTableHeader().setBackground(bg);

        scroller = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.setBackground(bg);
        scroller.setBorder(null);
        scroller.getViewport().setBackground(bg);

        add(scroller, BorderLayout.CENTER);
//        setTableProperties();

        table.addMouseListener(new ResultSetTablePopupMenu(table, resultSetTableContainer));
    }

    /**
     * Sets the results background to that specified.
     *
     * @param the colour to set
     */
    public void setResultBackground(Color c) {
        scroller.setBackground(c);
        scroller.getViewport().setBackground(c);
        if (table != null) {
            table.getTableHeader().setBackground(c);
        }
    }

    public void destroyTable() {
        table = null;
        if (popupMenu != null) {
            popupMenu.removeAll();
        }
        popupMenu = null;
    }

    public void interrupt() {
        if (model != null) {
            model.interrupt();
        }
    }

    public List<List<RecordDataItem>> filter(String pattern) {

        List<List<RecordDataItem>> filteredRows = new ArrayList<List<RecordDataItem>>();
        for (int i = 0, n = model.getRowCount(); i < n; i++) {

            List<RecordDataItem> row = model.getRowDataForRow(i);
            for (RecordDataItem recordDataItem : row) {

                if (recordDataItem.valueContains(pattern)) {

                    filteredRows.add(row);
                    break;
                }

            }

        }
        return filteredRows;
    }

    public int setResultSet(ResultSetTableModel model, boolean showRowNumber) {

        this.model = model;

        int rowCount = model.getRowCount();
        if (rowCount > 0) {

            buildTable(rowCount);

        } else {

            DefaultTable emptyTable = new DefaultTable(model);
            table.setTableColumnWidth(SystemProperties.getIntProperty("user", "results.table.column.width"));
            scroller.getViewport().add(emptyTable);
            emptyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }

        return rowCount;
    }

    /**
     * Builds the result set table display.
     *
     * @param the row count
     */
    private void buildTable(int rowCount) {

        boolean sorterWasNull = false;
        if (sorter == null) {

            sorterWasNull = true;
            sorter = new TableSorter(model);

        } else {

            sorter.setTableModel(model);
        }

        if (table == null) {

            table = new ResultSetTable(sorter);
            scroller.getViewport().add(table);

        } else {

            table.setModel(sorter);
        }

        // reset the table header
        if (sorterWasNull) {

            sorter.setTableHeader(table.getTableHeader());
        }

        setTableProperties();
//        table.resetTableColumnWidth();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

//        if (showRowHeader) {
//
//            addRowNumberHeader();
//        }

    }

    protected void tableDataChanged() {
        if (showRowHeader) {
            addRowNumberHeader();
        }
    }

    private void addRowNumberHeader() {

        if (rowNumberHeader == null) {
            rowNumberHeader = new RowNumberHeader(table);
            rowNumberHeader.setBackground(SystemProperties.getColourProperty(
                    "user", "editor.results.background.colour"));
        } else {
            rowNumberHeader.setTable(table);
        }
        table.addPropertyChangeListener("rowHeight", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                rowNumberHeader.setTable(table);
            }
        });
        scroller.setRowHeaderView(rowNumberHeader);
    }

    /**
     * Sets the user defined (preferences) table properties.
     */
    public void setTableProperties() {

        table.applyUserPreferences();

        showRowHeader = SystemProperties.getBooleanProperty(
                "user", "results.table.row.numbers");
        if (showRowHeader) {

            addRowNumberHeader();

        } else {

            if (rowNumberHeader != null) {

                // remove the row header if its there now
                scroller.setRowHeaderView(null);
            }

            rowNumberHeader = null;
        }

        if (model != null) {

            model.setHoldMetaData(SystemProperties.getBooleanProperty(
                    "user", "editor.results.metadata"));
        }

    }

    /**
     * Returns the model row count.
     *
     * @return the row ount displayed
     */
    public int getRowCount() {

        return model.getRowCount();
    }

    /**
     * Indicates whether the model has retained the ResultSetMetaData.
     *
     * @return true | false
     */
    public boolean hasResultSetMetaData() {
        if (model == null) {
            return false;
        } else {
            return model.hasResultSetMetaData();
        }
    }

    /**
     * Returns the table display.
     *
     * @return the table
     */
    public ResultSetTable getTable() {

        return table;
    }

    /**
     * Sets to display the result set meta data.
     */
    public ResultSetMetaDataPanel getResultSetMetaDataPanel() {

        if (!model.hasResultSetMetaData()) {

            return null;
        }

        if (metaDataPanel == null) {

            metaDataPanel = new ResultSetMetaDataPanel();

        }

        metaDataPanel.setMetaData(model.getResultSetMetaData());

        return metaDataPanel;
    }

    /**
     * Returns the result set table model.
     *
     * @return the table model
     */
    public ResultSetTableModel getResultSetTableModel() {

        return model;
    }

}


