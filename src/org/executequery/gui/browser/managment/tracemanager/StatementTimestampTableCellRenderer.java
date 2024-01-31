package org.executequery.gui.browser.managment.tracemanager;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class StatementTimestampTableCellRenderer extends CustomTableCellRenderer {

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat tstampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private long deltaTimestampBaseMillis;

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                   final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        assert table != null;

        final StatementTimestampTableCellRenderer component = (StatementTimestampTableCellRenderer) super
                .getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        final TraceDataModel dataModel = (TraceDataModel) table.getModel();
        if (value != null && LogConstants.TSTAMP_COLUMN.equals(dataModel.getColumnName(column))) {
            final Timestamp tstamp = (Timestamp) value;
            String str = tstampFormat.format(tstamp);
            if (deltaTimestampBaseMillis != 0) {
                str += " (";
                final long delta = tstamp.getTime() - deltaTimestampBaseMillis;
                if (delta > 0) {
                    str += "+";
                }
                str += delta + "ms)";
            }
            component.setText(str);
        }
        if (value != null) {
            component.setToolTipText(value.toString());
        }

        return component;
    }

    public void setDeltaTimestampBaseMillis(final long deltaTimestampBaseMillis) {
        this.deltaTimestampBaseMillis = deltaTimestampBaseMillis;
    }

}
