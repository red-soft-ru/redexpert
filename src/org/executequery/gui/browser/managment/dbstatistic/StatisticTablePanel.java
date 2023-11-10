package org.executequery.gui.browser.managment.dbstatistic;

import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.managment.tracemanager.AnalisePanel;
import org.underworldlabs.statParser.StatDatabase;
import org.underworldlabs.statParser.StatIndex;
import org.underworldlabs.statParser.StatTable;
import org.underworldlabs.statParser.TableModelObject;
import org.underworldlabs.swing.AbstractPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Vector;

public class StatisticTablePanel extends AbstractPanel {
    public static final int DATABASE = 0;
    public static final int TABLE = 1;
    public static final int INDEX = 2;
    JTable table;
    StatisticTableModel model;
    List rows;

    @Override
    protected void initComponents() {
        table = WidgetFactory.createTable("StatisticTable");
        rows = new Vector();
    }

    @Override
    protected void arrangeComponents() {
        add(new JScrollPane(table), gbh.spanX().spanY().fillBoth().get());
    }

    @Override
    protected void postInitActions() {

    }

    public void initModel(int type) {
        TableModelObject tableModelObject;
        switch (type) {
            case DATABASE:
                tableModelObject = new StatDatabase();
                break;
            case TABLE:
                tableModelObject = new StatTable();
                break;
            case INDEX:
                tableModelObject = new StatIndex(null);
                break;
            default:
                return;
        }
        model = new StatisticTableModel(tableModelObject);
        table.setModel(model);
        AnalisePanel.AnaliseSorter sorter = new AnalisePanel.AnaliseSorter<>(model);
        table.setRowSorter(sorter);
        int colWidth = 120;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidth);
            table.getColumnModel().getColumn(i).setWidth(colWidth);
            table.getColumnModel().getColumn(i).setMinWidth(colWidth);
        }
    }

    public List<TableModelObject> getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
        model.fireTableDataChanged();
    }

    class StatisticTableModel extends AbstractTableModel {
        TableModelObject tableModelObject;

        public StatisticTableModel(TableModelObject tableModelObject) {
            this.tableModelObject = tableModelObject;
        }

        @Override
        public String getColumnName(int column) {
            return tableModelObject.getColumnName(column);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return tableModelObject.getColumnClass(columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return tableModelObject.getColumnCount();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return ((TableModelObject) rows.get(rowIndex)).getValueAt(columnIndex);
        }
    }
}
