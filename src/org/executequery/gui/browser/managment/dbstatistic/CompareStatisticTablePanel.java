package org.executequery.gui.browser.managment.dbstatistic;

import org.underworldlabs.statParser.TableModelObject;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;


public class CompareStatisticTablePanel extends StatisticTablePanel {

    public CompareStatisticTablePanel() {
        super();
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        CompareTableCellRenderer renderer = new CompareTableCellRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        table.setDefaultRenderer(Long.class, renderer);
        table.setDefaultRenderer(Float.class, renderer);
        table.setDefaultRenderer(String.class, renderer);
        table.setDefaultRenderer(Double.class, renderer);
    }

    class CompareTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int x = table.convertRowIndexToModel(row);
            int y = table.convertColumnIndexToModel(column);
            if (((TableModelObject) rows.get(x)).getCompared() == TableModelObject.ADDED) {
                comp.setBackground(Color.GREEN);
            } else if (((TableModelObject) rows.get(x)).getCompared() == TableModelObject.DELETED) {
                comp.setBackground(Color.RED);
            } else comp.setBackground(Color.WHITE);
            if (value instanceof Double) {
                double v = (double) value;
                String str = String.format("%.5f", v);
                setValue(str);
            }
            return comp;
        }
    }
}
