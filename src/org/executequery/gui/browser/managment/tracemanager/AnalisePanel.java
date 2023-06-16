package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.TraceManagerPanel;
import org.executequery.gui.browser.managment.tracemanager.ResultSetDataModel;
import org.executequery.gui.browser.managment.tracemanager.net.AnaliseRow;
import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
import org.executequery.gui.editor.SimpleDataItemViewerPanel;
import org.executequery.gui.resultset.SimpleRecordDataItem;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AnalisePanel extends JPanel {
    List<AnaliseRow> rows;

    List<LogMessage> messages;
    JTable table;
    AnaliseTableModel model;

    public AnalisePanel(List<LogMessage> messages) {
        this.messages=messages;
        init();
    }

    void init()
    {
        model= new AnaliseTableModel();
        table = new JTable(model);
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
                        rdi.setValue(table.getValueAt(row, col));
                        BaseDialog dialog = new BaseDialog(Bundles.get("ResultSetTablePopupMenu.RecordDataItemViewer"), true);
                        dialog.addDisplayComponentWithEmptyBorder(
                                new SimpleDataItemViewerPanel(dialog, rdi));
                        dialog.display();
                    }
                }
            }
        });





        add(logListPanel, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        rebuildRows();

    }

    public void addMessage(LogMessage logMessage)
    {
        if(rows==null)
            rows= new ArrayList<>();
        checkLogMessage(logMessage);
    }

    void checkLogMessage(LogMessage msg)
    {
        boolean added=false;
        for(AnaliseRow row:rows)
        {
            if(msg.getStatementText()!=null&&row.getLogMessage().getStatementText().contentEquals(msg.getStatementText())) {
                row.addMessage(msg);
                added=true;
                break;
            }

        }
        if(!added&&msg.getStatementText()!=null)
        {
            AnaliseRow row = new AnaliseRow();
            row.addMessage(msg);
            rows.add(row);
        }
        model.fireTableDataChanged();
    }

    public synchronized void rebuildRows()
    {
        if(rows==null)
            rows= new ArrayList<>();
        rows.clear();
        for(LogMessage msg:messages)
        {
            checkLogMessage(msg);
        }
    }

    class AnaliseTableModel extends AbstractTableModel
    {

        String[] headers = {"QUERY","TOTAL_TIME","AVERAGE_TIME","COUNT"};


        @Override
        public int getRowCount() {
            if(rows!=null)
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
            switch (columnIndex)
            {
                case 0:return String.class;
                default:return Long.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(rowIndex>=0&&columnIndex>=0) {
                switch (columnIndex)
                {
                    case 0:return rows.get(rowIndex).getLogMessage().getStatementText();
                    case 1:return rows.get(rowIndex).getTotalTime();
                    case 2:return rows.get(rowIndex).getAverageTime();
                    case 3:return rows.get(rowIndex).getCount();
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
