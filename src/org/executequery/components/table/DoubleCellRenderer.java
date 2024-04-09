package org.executequery.components.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class DoubleCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        double v = (double) value;
        if (v > 0.01 && v != Double.POSITIVE_INFINITY) {
            //comp.setBackground(Color.RED);
        } else {
            if (!isSelected)
                comp.setBackground(table.getBackground());
            else comp.setBackground(table.getSelectionBackground());
        }
        String str = String.format("%.20f", v);
        if (v == 0)
            str = "0";
        setValue(str);
        return comp;
    }
}
