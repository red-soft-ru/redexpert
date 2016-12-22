package org.executequery.gui.jdbclogger;

import ch.sla.jdbcperflogger.StatementType;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;

public class CustomTable extends JTable {

    private static final Color ERROR_COLOR = Color.RED;
    private static final Color DEFAULT_BG_COLOR = Color.WHITE;
    private static final Color HIGHLIGHT_COLOR = Color.ORANGE;
    private static final Color COMMIT_COLOR = new Color(204, 255, 102);
    private static final Color ROLLBACK_COLOR = Color.PINK;

    private String txtToHighlightUpper;

    private Long minDurationNanoToHighlight;

    CustomTable(final ResultSetDataModel tm) {
        super(tm);
    }

    // Implement table header tool tips.
    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getToolTipText(  final MouseEvent e) {
                assert e != null;
                final java.awt.Point p = e.getPoint();
                final int index = columnModel.getColumnIndexAtX(p.x);
                if (index >= 0) {
                    return columnModel.getColumn(index).getHeaderValue().toString();
                } else {
                    return "";
                }
            }
        };
    }

    @Override
    public Component prepareRenderer(  final TableCellRenderer renderer, final int row, final int column) {
        assert renderer != null;
        final Component component = super.prepareRenderer(renderer, row, column);

        if (!this.getSelectionModel().isSelectedIndex(row)) {
            final ResultSetDataModel model = (ResultSetDataModel) getModel();
            final int modelIndex = convertRowIndexToModel(row);

            Color bgColor = DEFAULT_BG_COLOR;
            final StatementType statementType = (StatementType) model.getValueAt(modelIndex,
                    LogConstants.STMT_TYPE_COLUMN);
            final String sql = (String) model.getValueAt(modelIndex, LogConstants.RAW_SQL_COLUMN);

            if (statementType == StatementType.TRANSACTION && sql != null) {
                if (sql.contains("COMMIT")) {
                    bgColor = COMMIT_COLOR;
                } else if (sql.contains("ROLLBACK")) {
                    bgColor = ROLLBACK_COLOR;
                }
            }
            final Integer error = (Integer) model.getValueAt(modelIndex, LogConstants.ERROR_COLUMN);
            if (error != null && error.intValue() != 0) {
                bgColor = ERROR_COLOR;
            } else if (txtToHighlightUpper != null) {
                if (sql != null && sql.toUpperCase().contains(txtToHighlightUpper)) {
                    bgColor = HIGHLIGHT_COLOR;
                }
            } else {
                final Long minDurationNanoToHighlight2 = minDurationNanoToHighlight;
                if (minDurationNanoToHighlight2 != null) {
                    Long duration = (Long) model.getValueAt(modelIndex,
                            LogConstants.EXEC_PLUS_RSET_USAGE_TIME);
                    if (duration == null) {
                        // in case we are in group by mode
                        final BigDecimal val = (BigDecimal) model.getValueAt(modelIndex,
                                LogConstants.TOTAL_EXEC_PLUS_RSET_USAGE_TIME_COLUMN);
                        if (val != null) {
                            duration = val.longValue();
                        }
                    }
                    if (duration != null && duration.longValue() >= minDurationNanoToHighlight2.longValue()) {
                        bgColor = HIGHLIGHT_COLOR;
                    }
                }
            }
            component.setBackground(bgColor);
        }
        return component;
    }

    public void setTxtToHighlight(  final String txtToHighlight) {
        if (txtToHighlight != null) {
            txtToHighlightUpper = txtToHighlight.toUpperCase();
        } else {
            txtToHighlightUpper = null;
        }
    }

    public void setMinDurationNanoToHighlight(  final Long minDurationNanoToHighlight) {
        this.minDurationNanoToHighlight = minDurationNanoToHighlight;
    }
}
