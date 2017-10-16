package biz.redsoft.gui;

import java.awt.Component;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class StatementTimestampTableCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;
    private static final SimpleDateFormat tstampFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private long deltaTimestampBaseMillis;

    @Override
    public Component getTableCellRendererComponent(  final JTable table,   final Object value,
                                                   final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        assert table != null;

        final StatementTimestampTableCellRenderer component = (StatementTimestampTableCellRenderer) super
                .getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        final ResultSetDataModel dataModel = (ResultSetDataModel) table.getModel();
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
