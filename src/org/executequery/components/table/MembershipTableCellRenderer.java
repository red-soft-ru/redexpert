package org.executequery.components.table;

import javax.swing.*;
import java.awt.*;

public class MembershipTableCellRenderer extends BrowserTableCellRenderer {
    private static final Color BORDER_COLOR = UIManager.getLookAndFeelDefaults().getColor("Table.selectionBackground");
    private static final Color FOREGROUND_COLOR = UIManager.getLookAndFeelDefaults().getColor("Table.focusCellForeground");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setBackground(null);
        setForeground(FOREGROUND_COLOR);
        if (isSelected && hasFocus)
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        return this;
    }
}
