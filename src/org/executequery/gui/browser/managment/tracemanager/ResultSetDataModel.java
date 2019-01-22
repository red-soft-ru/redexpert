package org.executequery.gui.browser.managment.tracemanager;


import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
import org.underworldlabs.swing.DynamicComboBoxModel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


class ResultSetDataModel extends AbstractTableModel {

    private List<String> columnNames = new ArrayList<>();
    private List<String> visibleColumnNames = new ArrayList<>();
    private List<LogMessage> rows = new ArrayList<>();
    private List<LogMessage> visibleRows = new ArrayList<>();
    private JComboBox filterTypeBox;
    private JComboBox filterColumnBox;
    private JComboBox rawSqlBox;
    private JTextField filterTextField;
    private Map<String, JCheckBox> mapCheckBox;


    public ResultSetDataModel(Map<String, JCheckBox> mapCheckBox, JComboBox filterTypeBox, JComboBox filterColumnBox, JComboBox rawSqlBox, JTextField filterTextField) {
        this.filterTypeBox = filterTypeBox;
        this.filterColumnBox = filterColumnBox;
        this.filterTextField = filterTextField;
        this.rawSqlBox = rawSqlBox;
        setMapCheckBox(mapCheckBox);
        buildHeaders();
        rebuildModel();
    }

    public void rebuildModel() {

        visibleColumnNames = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++) {
            if (mapCheckBox.get(columnNames.get(i)).isSelected())
                visibleColumnNames.add(columnNames.get(i));
        }
        visibleRows = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            checkFilterMessage(rows.get(i));
        }
        DynamicComboBoxModel model = (DynamicComboBoxModel) filterColumnBox.getModel();
        Object selectedItem = filterColumnBox.getSelectedItem();
        model.setElements(visibleColumnNames);
        if (visibleColumnNames.contains(selectedItem))
            filterColumnBox.setSelectedItem(selectedItem);
        model = (DynamicComboBoxModel) rawSqlBox.getModel();
        selectedItem = rawSqlBox.getSelectedItem();
        model.setElements(visibleColumnNames);
        if (visibleColumnNames.contains(selectedItem))
            rawSqlBox.setSelectedItem(selectedItem);
        fireTableStructureChanged();
    }

    private void checkFilterMessage(LogMessage message) {
        String filter = filterTextField.getText();
        if (filterColumnBox.getSelectedItem() != null) {
            String field = String.valueOf(message.getFieldOfName((String) filterColumnBox.getSelectedItem()));
            if (filterTypeBox.getSelectedItem() == Filter.FilterType.FILTER) {
                if (field.contains(filter))
                    visibleRows.add(message);
                message.setHighlight(false);
            } else {
                visibleRows.add(message);
                message.setHighlight(field.contains(filter) && !filter.isEmpty());
            }
        }
    }

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
        return getValueAt(rowIndex, visibleColumnNames.get(columnIndex));
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return visibleColumnNames.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        if (visibleColumnNameFromIndex(columnIndex).contentEquals(LogConstants.TSTAMP_COLUMN))
            return Timestamp.class;
        if (visibleColumnNameFromIndex(columnIndex).contentEquals(LogConstants.ID_COLUMN))
            return Integer.class;
        return String.class;
    }

    public Object getValueAt(final int rowIndex, final String columnName) {
        return visibleRows.get(rowIndex).getFieldOfName(columnName);
    }

    private String visibleColumnNameFromIndex(int i) {
        return visibleColumnNames.get(i);
    }

    public void addRow(LogMessage message) {
        rows.add(message);
        checkFilterMessage(message);
        fireTableDataChanged();
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public Map<String, JCheckBox> getMapCheckBox() {
        return mapCheckBox;
    }

    public void setMapCheckBox(Map<String, JCheckBox> mapCheckBox) {
        this.mapCheckBox = mapCheckBox;
    }

    private void buildHeaders() {
        columnNames = new ArrayList<>();
        for (int i = 0; i < LogConstants.COLUMNS.length; i++) {
            columnNames.add(LogConstants.COLUMNS[i]);
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

    public List<LogMessage> getRows() {
        return rows;
    }

    public void setRows(List<LogMessage> rows) {
        this.rows = rows;
    }

    public List<LogMessage> getVisibleRows() {
        return visibleRows;
    }

    public void setVisibleRows(List<LogMessage> visibleRows) {
        this.visibleRows = visibleRows;
    }
}
