package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.managment.tracemanager.net.AnaliseRow;
import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
import org.executequery.gui.editor.SimpleDataItemViewerPanel;
import org.executequery.gui.resultset.SimpleRecordDataItem;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.ListSelectionPanelEvent;
import org.underworldlabs.swing.ListSelectionPanelListener;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class AnalisePanel extends JPanel {
    List<AnaliseRow> rows;

    List<LogMessage> messages;
    JTable table;
    AnaliseTableModel model;
    SimpleSqlTextPanel sqlTextArea;
    ListSelectionPanel typesPanel;

    String[] types = {"TRACE_INIT", "TRACE_FINI", "CREATE_DATABASE", "ATTACH_DATABASE", "DROP_DATABASE", "DETACH_DATABASE", "START_TRANSACTION",
            "COMMIT_RETAINING", "COMMIT_TRANSACTION", "ROLLBACK_RETAINING", "ROLLBACK_TRANSACTION", "EXECUTE_STATEMENT_START", "EXECUTE_STATEMENT_FINISH",
            "START_SERVICE", "PREPARE_STATEMENT", "FREE_STATEMENT", "CLOSE_CURSOR", "SET_CONTEXT", "PRIVILEGES_CHANGE", "EXECUTE_PROCEDURE_START",
            "EXECUTE_FUNCTION_START", "EXECUTE_PROCEDURE_FINISH", "EXECUTE_FUNCTION_FINISH", "EXECUTE_TRIGGER_START", "EXECUTE_TRIGGER_FINISH", "COMPILE_BLR",
            "EXECUTE_BLR", "EXECUTE_DYN", "ATTACH_SERVICE", "DETACH_SERVICE", "QUERY_SERVICE", "SWEEP_START", "SWEEP_FINISH", "SWEEP_FAILED", "SWEEP_PROGRESS"};

    public AnalisePanel(List<LogMessage> messages) {
        this.messages = messages;
        init();
    }

    void init() {
        model = new AnaliseTableModel();
        table = new JTable(model);
        sqlTextArea = new SimpleSqlTextPanel();
        typesPanel = new ListSelectionPanel();
        typesPanel.createAvailableList(types);
        typesPanel.selectOneStringAction("EXECUTE_STATEMENT_FINISH");
        typesPanel.addListSelectionPanelListener(new ListSelectionPanelListener() {
            @Override
            public void changed(ListSelectionPanelEvent event) {
                rebuildRows();
            }
        });
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();

        gbh.fullDefaults();

        JScrollPane logListPanel = new JScrollPane();
        logListPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        table.setRowSorter(new TableRowSorter<>(model));
        /*table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        table.setDefaultRenderer(Long.class, new CustomTableCellRenderer());
        table.setDefaultRenderer(Timestamp.class, new StatementTimestampTableCellRenderer());*/
        logListPanel.setViewportView(table);
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    int row = table.getSelectedRow();
                    int col = table.getSelectedColumn();
                    if (row >= 0 && col >= 0) {

                        SimpleRecordDataItem rdi = new SimpleRecordDataItem("Value", 0, "");
                        row = table.getRowSorter().convertRowIndexToModel(row);
                        rdi.setValue(rows.get(row).getLogMessage().getBody());
                        BaseDialog dialog = new BaseDialog(Bundles.get("ResultSetTablePopupMenu.RecordDataItemViewer"), true);
                        dialog.addDisplayComponentWithEmptyBorder(
                                new SimpleDataItemViewerPanel(dialog, rdi));
                        dialog.display();
                    }
                }
            }
        });
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    row = table.getRowSorter().convertRowIndexToModel(row);
                    sqlTextArea.setSQLText(rows.get(row).getLogMessage().getStatementText());
                }
            }
        });


        gbh.setLabelDefault();
        add(sqlTextArea, gbh.nextRowFirstCol().fillBoth().setMaxWeightX().get());
        add(typesPanel, gbh.nextCol().fillHorizontally().get());
        add(logListPanel, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        rebuildRows();

    }

    public List<LogMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<LogMessage> messages) {
        this.messages = messages;
    }

    public void addMessage(LogMessage logMessage) {
        if (rows == null)
            rows = new ArrayList<>();
        checkLogMessage(logMessage);
    }

    void checkLogMessage(LogMessage msg) {
        boolean added = false;
        if (!typesPanel.getSelectedValues().contains(msg.getTypeEvent()))
            return;
        for (AnaliseRow row : rows) {
            if (msg.getStatementText() != null && row.getLogMessage().getStatementText().contentEquals(msg.getStatementText()) && msg.getTypeEvent().contentEquals(row.getLogMessage().getTypeEvent())) {
                row.addMessage(msg);
                added = true;
                break;
            }

        }
        if (!added && msg.getStatementText() != null)
        {
            AnaliseRow row = new AnaliseRow();
            row.addMessage(msg);
            rows.add(row);
        }
        model.fireTableDataChanged();
    }

    public synchronized void rebuildRows() {
        if (rows == null)
            rows = new ArrayList<>();
        rows.clear();
        for (LogMessage msg : messages) {
            checkLogMessage(msg);
        }
    }

    public void repaintTable() {
        table.updateUI();
    }

    class AnaliseTableModel extends AbstractTableModel {

        String[] headers = {"QUERY", "TOTAL_TIME", "AVERAGE_TIME", "MAX_EXEC_TIME", "DISPERSION", "COUNT"};


        @Override
        public int getRowCount() {
            if (rows != null)
            return rows.size();
            return 0;
        }

        @Override
        public int getColumnCount() {
            return headers.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return headers[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            }
            return Long.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(rowIndex>=0&&columnIndex>=0) {
                switch (columnIndex) {
                    case 0:
                        return rows.get(rowIndex).getLogMessage().getStatementText();
                    case 1:
                        return rows.get(rowIndex).getTotalTime();
                    case 2:
                        return rows.get(rowIndex).getAverageTime();
                    case 3:
                        return rows.get(rowIndex).getMaxTime();
                    case 4:
                        return rows.get(rowIndex).getDispersionTime();
                    case 5:
                        return rows.get(rowIndex).getCount();
                }
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        }

        List<TableModelListener> listeners;

        @Override
        public void addTableModelListener(TableModelListener l) {
            if(listeners==null)
                listeners=new ArrayList<>();
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            if(listeners!=null)
                listeners.remove(l);
        }

    }

}
