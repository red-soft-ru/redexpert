package org.executequery.gui.browser.managment.dbstatistic;


import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.IconManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.managment.tracemanager.AnalisePanel;
import org.executequery.gui.browser.managment.tracemanager.ServiceManagerPopupMenu;
import org.underworldlabs.statParser.*;
import org.underworldlabs.swing.AbstractPanel;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class StatisticTablePanel extends AbstractPanel {
    public static final int DATABASE = 0;
    public static final int TABLE = 1;
    public static final int INDEX = 2;
    public static final int TABLESPACE = 3;
    JTable table;
    protected StatisticTableModel model;
    protected HeaderTableModel headerModel;
    protected List rows;
    protected JTable headerRows;
    JScrollPane scrollPane;

    @Override
    protected void initComponents() {
        table = WidgetFactory.createTable("StatisticTable");
        table.addMouseListener(new ServiceManagerPopupMenu(table));
        headerRows = WidgetFactory.createTable("HeaderRows");
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        headerRows.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rows = new Vector();
        scrollPane = new JScrollPane(table);
        table.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
        table.setDefaultRenderer(Long.class, new LongTableCellRenderer());
        headerRows.setDefaultRenderer(TableModelObject.class, new StatisticHeaderCellRenderer());
        headerRows.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        headerRows.getTableHeader().setResizingAllowed(true);
        headerRows.setPreferredScrollableViewportSize(new Dimension(headerRows.getWidth() + 2, 0));
        headerRows.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {

            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {

            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {

            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                headerRows.setPreferredScrollableViewportSize(new Dimension(headerRows.getWidth() + 2, 0));
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {

            }
        });
        scrollPane.setRowHeaderView(headerRows);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, headerRows.getTableHeader());
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (headerRows.getSelectedRow() != table.getSelectedRow())
                    headerRows.getSelectionModel().setSelectionInterval(table.getSelectedRow(), table.getSelectedRow());
            }
        });
        headerRows.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (headerRows.getSelectedRow() != table.getSelectedRow())
                    table.getSelectionModel().setSelectionInterval(headerRows.getSelectedRow(), headerRows.getSelectedRow());
            }
        });
    }

    @Override
    protected void arrangeComponents() {

        add(scrollPane, gbh.spanX().spanY().fillBoth().get());
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
            case TABLESPACE:
                tableModelObject = new StatTablespace();
                break;
            default:
                return;
        }
        model = new StatisticTableModel(tableModelObject);
        table.setModel(model);
        headerModel = new HeaderTableModel(tableModelObject);
        headerRows.setModel(headerModel);
        headerRows.getColumnModel().getColumn(0).setPreferredWidth(200);
        AnalisePanel.AnaliseSorter sorter = new AnalisePanel.AnaliseSorter<>(model);
        table.setRowSorter(sorter);
        headerRows.setRowSorter(sorter);
        int colWidth = 120;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidth);
            table.getColumnModel().getColumn(i).setWidth(colWidth);
            table.getColumnModel().getColumn(i).setMinWidth(colWidth);
        }
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(0));
        if (tableModelObject instanceof StatIndex) {
            table.getColumnModel().removeColumn(table.getColumnModel().getColumn(0));
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

    class HeaderTableModel extends AbstractTableModel {
        TableModelObject tableModelObject;
        String[] headers;
        public HeaderTableModel(TableModelObject tableModelObject) {
            this.tableModelObject = tableModelObject;
            if (tableModelObject instanceof StatIndex)
                headers = new String[]{"name", "table name"};
            else headers = new String[]{"name"};
        }

        @Override
        public String getColumnName(int column) {
            return headers[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return TableModelObject.class;
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
            return headers.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0)
                return rows.get(rowIndex);
            else return ((StatIndex) rows.get(rowIndex)).table_name;
        }
    }

    class DoubleTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            double v = (double) value;
            if (v > 0.01 && v != Double.POSITIVE_INFINITY) {
                comp.setBackground(Color.RED);
            } else {
                if (!isSelected)
                    comp.setBackground(table.getBackground());
                else comp.setBackground(table.getSelectionBackground());
            }
            String str = String.format("%.5f", v);
            setValue(str);
            return comp;
        }
    }

    class StatisticHeaderCellRenderer extends DefaultTableCellRenderer {

        private final Icon tableIcon;
        private final Icon indexIcon;

        public StatisticHeaderCellRenderer() {
            super();
            tableIcon = IconManager.getInstance().getIconFromType(NamedObject.TABLE);
            indexIcon = IconManager.getInstance().getIconFromType(NamedObject.INDEX);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            DefaultTableCellRenderer superComp = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof TableModelObject) {
                superComp.setText(((TableModelObject) value).name);
                if (value instanceof StatTable)
                    superComp.setIcon(tableIcon);
                else if (value instanceof StatIndex)
                    superComp.setIcon(indexIcon);
                if (StatisticTablePanel.this instanceof CompareStatisticTablePanel) {
                    int x = table.convertRowIndexToModel(row);


                    if (((TableModelObject) rows.get(x)).getCompared() == TableModelObject.ADDED) {
                        superComp.setBackground(Color.GREEN);
                    } else if (((TableModelObject) rows.get(x)).getCompared() == TableModelObject.DELETED) {
                        superComp.setBackground(Color.RED);
                    } else superComp.setBackground(table.getBackground());
                }
            } else superComp.setIcon(tableIcon);
            return superComp;
        }
    }

    class LongTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            long v = (long) value;
            String str = "";
            str = delimitValue(v, str);
            setValue(str);
            return comp;
        }

        private String delimitValue(long value, String result) {
            if (value >= 1000) {
                String div = String.valueOf(value % 1000);
                while (div.length() < 3)
                    div = "0" + div;
                return delimitValue(value / 1000, " " + div + result);
            } else return value + result;
        }
    }
}
