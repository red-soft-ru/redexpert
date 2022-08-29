package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.gui.browser.managment.tracemanager.net.TableCounter;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TableCounterModel extends AbstractTableModel {

    private List<String> columnNames = new ArrayList<>();
    private List<String> visibleColumnNames = new ArrayList<>();
    private List<TableCounter> rows = new ArrayList<>();
    private List<TableCounter> visibleRows = new ArrayList<>();

    private JTable traceTable;
    private final ResultSetDataModel traceModel;


    public TableCounterModel(JTable traceTable) {
        setTraceTable(traceTable);
        traceModel = (ResultSetDataModel) traceTable.getModel();
        buildHeaders();
        rebuildModel();
    }

    public void rebuildModel() {

        visibleColumnNames = new ArrayList<>();
        visibleColumnNames.addAll(columnNames);
        visibleRows = new ArrayList<>();
        visibleRows.addAll(rows);
       /* for (int i = 0; i < rows.size(); i++) {
            checkFilterMessage(rows.get(i));
        }*/
        /*DynamicComboBoxModel model = (DynamicComboBoxModel) filterColumnBox.getModel();
        Object selectedItem = filterColumnBox.getSelectedItem();
        model.setElements(visibleColumnNames);
        if (visibleColumnNames.contains(selectedItem))
            filterColumnBox.setSelectedItem(selectedItem);*/
        fireTableStructureChanged();
    }

    /*private void checkFilterMessage(LogMessage message) {
        String filter = filterTextField.getText();
        if (filterColumnBox.getSelectedItem() != null) {
            String field = String.valueOf(message.getFieldOfName((String) filterColumnBox.getSelectedItem()));
            if (!matchCaseBox.isSelected()) {
                field = field.toLowerCase();
                filter = filter.toLowerCase();
            }
            if (filterTypeBox.getSelectedItem() == Filter.FilterType.FILTER) {
                if (field.contains(filter))
                    visibleRows.add(message);
                message.setHighlight(false);
            } else {
                visibleRows.add(message);
                message.setHighlight(field.contains(filter) && !filter.isEmpty());
            }
        }
    }*/

    @Override
    public int getRowCount() {
        return visibleRows.size();
    }

    @Override
    public int getColumnCount() {
        return visibleColumnNames.size();
    }

    @Override

    public Object getValueAt(final int rowIndex, final int columnIndex) {
        return rows.get(rowIndex).getCounter(columnIndex);
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return visibleColumnNames.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        if (visibleColumnNameFromIndex(columnIndex).contentEquals(LogConstants.TABLE))
            return String.class;
        else
            return Integer.class;
    }

    private String visibleColumnNameFromIndex(int i) {
        return visibleColumnNames.get(i);
    }

    public void addRow(TableCounter message) {
        rows.add(message);
        //checkFilterMessage(message);
        rebuildModel();
        fireTableDataChanged();
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setTraceTable(JTable traceTable) {
        this.traceTable = traceTable;
    }

    private void buildHeaders() {
        columnNames = new ArrayList<>();
        for (int i = 0; i < LogConstants.TABLE_COUNTERS.length; i++) {
            columnNames.add(LogConstants.TABLE_COUNTERS[i]);
        }
    }

    public void clearAll() {
        rows.clear();
        rebuildModel();
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<String> getVisibleColumnNames() {
        return visibleColumnNames;
    }

    public void setVisibleColumnNames(List<String> visibleColumnNames) {
        this.visibleColumnNames = visibleColumnNames;
    }

    public List<TableCounter> getRows() {
        return rows;
    }

    public void setRows(List<TableCounter> rows) {
        this.rows = rows;
    }

    public List<TableCounter> getVisibleRows() {
        return visibleRows;
    }

    public void setVisibleRows(List<TableCounter> visibleRows) {
        this.visibleRows = visibleRows;
    }
}
