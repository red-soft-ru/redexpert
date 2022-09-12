package org.executequery.gui.browser.managment.tracemanager;


import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.ListSelectionPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


class ResultSetDataModel extends AbstractTableModel {

    private List<String> columnNames = new ArrayList<>();
    private List<String> visibleColumnNames = new ArrayList<>();
    private List<LogMessage> rows = new ArrayList<>();
    private List<LogMessage> visibleRows = new ArrayList<>();
    private final JComboBox filterTypeBox;
    private final JComboBox filterColumnBox;
    private final JTextField filterTextField;

    private final JCheckBox matchCaseBox;
    private ListSelectionPanel listSelectionPanel;


    public ResultSetDataModel(ListSelectionPanel listSelectionPanel, JComboBox filterTypeBox, JComboBox filterColumnBox, JTextField filterTextField, JCheckBox matchCaseBox) {
        this.filterTypeBox = filterTypeBox;
        this.filterColumnBox = filterColumnBox;
        this.filterTextField = filterTextField;
        this.matchCaseBox = matchCaseBox;
        setListSelectionPanel(listSelectionPanel);
        buildHeaders();
        rebuildModel();
    }

    public void rebuildModel() {

        visibleColumnNames = new ArrayList<>();
        visibleColumnNames.addAll(listSelectionPanel.getSelectedValues());
        visibleRows = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            checkFilterMessage(rows.get(i));
        }
        DynamicComboBoxModel model = (DynamicComboBoxModel) filterColumnBox.getModel();
        Object selectedItem = filterColumnBox.getSelectedItem();
        model.setElements(visibleColumnNames);
        if (visibleColumnNames.contains(selectedItem))
            filterColumnBox.setSelectedItem(selectedItem);
        fireTableStructureChanged();
    }

    private void checkFilterMessage(LogMessage message) {
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
        if (visibleColumnNameFromIndex(columnIndex).contentEquals(LogConstants.ID_COLUMN)
                || visibleColumnNameFromIndex(columnIndex).contentEquals(LogConstants.TIME_EXECUTION_COLUMN)
                || visibleColumnNameFromIndex(columnIndex).contains("COUNT_")
                || visibleColumnNameFromIndex(columnIndex).contentEquals(LogConstants.RECORDS_FETCHED_COLUMN)
        )
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

    public ListSelectionPanel getListSelectionPanel() {
        return listSelectionPanel;
    }

    public void setListSelectionPanel(ListSelectionPanel listSelectionPanel) {
        this.listSelectionPanel = listSelectionPanel;
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
