package org.executequery.gui.editor;

import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.components.AbstractDialogPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionTablesTable extends AbstractDialogPanel {
    JTable table;
    TransactionTableModel model;
    private final List<NamedObject> tables;

    public TransactionTablesTable(List<NamedObject> tables) {
        this.tables = tables;
        init();
    }

    @Override
    protected void ok() {

    }

    private void init() {
        model = new TransactionTableModel(tables);
        table = new JTable(model);
        mainPanel.setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();

        mainPanel.add(new JScrollPane(table), gbh.fillBoth().spanX().spanY().get());
    }

    public List<ReservingTable> getReservingTables() {
        List<ReservingTable> result = new ArrayList<>();
        for (ReservingTable rTable : model.reservingTables) {
            if (rTable.isReserving())
                result.add(rTable);
        }
        return result;
    }

    class ReservingTable {
        String name;

        boolean reserving;
        boolean sharedTable;
        boolean protectedTable;
        boolean readTable;
        boolean writeTable;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isReserving() {
            return reserving;
        }

        public void setReserving(boolean reserving) {
            this.reserving = reserving;
        }

        public boolean isSharedTable() {
            return sharedTable;
        }

        public void setSharedTable(boolean sharedTable) {
            this.sharedTable = sharedTable;
            protectedTable = !sharedTable;
        }

        public boolean isProtectedTable() {
            return protectedTable;
        }

        public void setProtectedTable(boolean protectedTable) {
            this.protectedTable = protectedTable;
            sharedTable = !protectedTable;
        }

        public boolean isReadTable() {
            return readTable;
        }

        public void setReadTable(boolean readTable) {
            this.readTable = readTable;
            writeTable = !readTable;
        }

        public boolean isWriteTable() {
            return writeTable;
        }

        public void setWriteTable(boolean writeTable) {
            this.writeTable = writeTable;
            readTable = !writeTable;
        }
    }

    class TransactionTableModel extends AbstractPrintableTableModel {
        List<ReservingTable> reservingTables;

        public TransactionTableModel(List<NamedObject> list) {
            reservingTables = new ArrayList<>();
            for (NamedObject table : list) {
                ReservingTable rTable = new ReservingTable();
                rTable.setName(table.getName());
                rTable.setReserving(false);
                rTable.setSharedTable(true);
                rTable.setReadTable(true);
                reservingTables.add(rTable);
            }
        }

        String[] header = new String[]{"name", "reserving", "shared", "protected", "read", "write"};

        @Override
        public int getRowCount() {
            return reservingTables.size();
        }

        @Override
        public int getColumnCount() {
            return header.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return header[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0)
                return String.class;
            else return Boolean.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || columnIndex < 0 || rowIndex >= reservingTables.size() || columnIndex >= header.length)
                return null;

            switch (columnIndex) {
                case 0:
                    return reservingTables.get(rowIndex).getName();
                case 1:
                    return reservingTables.get(rowIndex).isReserving();
                case 2:
                    return reservingTables.get(rowIndex).isSharedTable();
                case 3:
                    return reservingTables.get(rowIndex).isProtectedTable();
                case 4:
                    return reservingTables.get(rowIndex).isReadTable();
                case 5:
                    return reservingTables.get(rowIndex).isWriteTable();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex < 0 || columnIndex < 0 || rowIndex >= reservingTables.size() || columnIndex >= header.length)
                return;

            switch (columnIndex) {
                case 0:
                    reservingTables.get(rowIndex).setName((String) aValue);
                    break;
                case 1:
                    reservingTables.get(rowIndex).setReserving((Boolean) aValue);
                    break;
                case 2:
                    reservingTables.get(rowIndex).setSharedTable((Boolean) aValue);
                    break;
                case 3:
                    reservingTables.get(rowIndex).setProtectedTable((Boolean) aValue);
                    break;
                case 4:
                    reservingTables.get(rowIndex).setReadTable((Boolean) aValue);
                    break;
                case 5:
                    reservingTables.get(rowIndex).setWriteTable((Boolean) aValue);
                    break;
            }
            fireTableDataChanged();
        }

        @Override
        public String getPrintValueAt(int row, int col) {
            return null;
        }
    }
}
