package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.managment.tracemanager.net.AnaliseRow;
import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
import org.executequery.gui.editor.SimpleDataItemViewerPanel;
import org.executequery.gui.resultset.SimpleRecordDataItem;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.EQDateTimePicker;
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.ListSelectionPanelEvent;
import org.underworldlabs.swing.ListSelectionPanelListener;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.executequery.gui.browser.managment.tracemanager.net.AnaliseRow.TYPES;

public class AnalisePanel extends JPanel {
    List<AnaliseRow> rows;

    List<String> headers;
    List<LogMessage> messages;
    JTable table;
    AnaliseTableModel model;
    SimpleSqlTextPanel sqlTextArea;
    ListSelectionPanel typesPanel;

    JCheckBox[] checkBoxes;
    String[] params = new String[]{

            "TOTAL", "AVERAGE", "MAX", "STD_DEV"
    };

    String[] types = {"TRACE_INIT", "TRACE_FINI", "CREATE_DATABASE", "ATTACH_DATABASE", "DROP_DATABASE", "DETACH_DATABASE", "START_TRANSACTION",
            "COMMIT_RETAINING", "COMMIT_TRANSACTION", "ROLLBACK_RETAINING", "ROLLBACK_TRANSACTION", "EXECUTE_STATEMENT_START", "EXECUTE_STATEMENT_FINISH",
            "START_SERVICE", "PREPARE_STATEMENT", "FREE_STATEMENT", "CLOSE_CURSOR", "SET_CONTEXT", "PRIVILEGES_CHANGE", "EXECUTE_PROCEDURE_START",
            "EXECUTE_FUNCTION_START", "EXECUTE_PROCEDURE_FINISH", "EXECUTE_FUNCTION_FINISH", "EXECUTE_TRIGGER_START", "EXECUTE_TRIGGER_FINISH", "COMPILE_BLR",
            "EXECUTE_BLR", "EXECUTE_DYN", "ATTACH_SERVICE", "DETACH_SERVICE", "QUERY_SERVICE", "SWEEP_START", "SWEEP_FINISH", "SWEEP_FAILED", "SWEEP_PROGRESS"};

    EQDateTimePicker startTimePicker;
    EQDateTimePicker endTimePicker;

    public AnalisePanel(List<LogMessage> messages) {
        this.messages = messages;
        init();
    }

    void buildHeaders() {
        headers.clear();
        headers.add("QUERY");
        headers.add("COUNT");
        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isSelected()) {
                for (int g = 0; g < params.length; g++)
                    headers.add(params[g] + "_" + TYPES[i]);

            }
        }
        model.fireTableStructureChanged();
    }

    void init() {
        headers = new ArrayList<>();
        Arrays.sort(types);
        checkBoxes = new JCheckBox[TYPES.length];
        for (int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i] = new JCheckBox(TYPES[i]);
            if (i == 0)
                checkBoxes[i].setSelected(true);
            checkBoxes[i].addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    buildHeaders();
                }
            });
        }

        model = new AnaliseTableModel();
        buildHeaders();
        table = new JTable(model);
        sqlTextArea = new SimpleSqlTextPanel();
        typesPanel = new ListSelectionPanel();
        typesPanel.setLabelText(bundleString("AvailableEvents"), bundleString("SelectedEvents"));
        typesPanel.createAvailableList(types);
        typesPanel.selectOneStringAction("EXECUTE_STATEMENT_FINISH");
        typesPanel.addListSelectionPanelListener(new ListSelectionPanelListener() {
            @Override
            public void changed(ListSelectionPanelEvent event) {
                rebuildRows();
            }
        });
        startTimePicker = new EQDateTimePicker();
        endTimePicker = new EQDateTimePicker();
        startTimePicker.setVisibleNullBox(false);
        endTimePicker.setVisibleNullBox(false);
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();

        gbh.fullDefaults();

        JScrollPane logListPanel = new JScrollPane();
        logListPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        table.setRowSorter(new TableRowSorter<>(model));
        logListPanel.setViewportView(table);


        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    int row = table.getSelectedRow();
                    int col = table.getSelectedColumn();
                    if (row >= 0 && col >= 0) {

                        SimpleRecordDataItem rdi = new SimpleRecordDataItem("Value", 0, "");
                        row = table.getRowSorter().convertRowIndexToModel(row);
                        rdi.setValue(rows.get(row).getLogMessages());
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

        JButton selectEventsButton = new JButton(bundleString("HideEvents"));
        selectEventsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                typesPanel.setVisible(!typesPanel.isVisible());
                if (typesPanel.isVisible())
                    selectEventsButton.setText(bundleString("HideEvents"));
                else selectEventsButton.setText(bundleString("ShowEvents"));
            }
        });

        JButton reloadButton = new JButton(Bundles.getCommon("rebuild"));
        reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rebuildRows();
            }
        });
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(logListPanel);

        gbh.setLabelDefault();
        add(selectEventsButton, gbh.nextRowFirstCol().setLabelDefault().get());
        for (int i = 0; i < checkBoxes.length; i++)
            add(checkBoxes[i], gbh.nextCol().setLabelDefault().get());
        add(new JLabel(bundleString("Period")), gbh.nextCol().setLabelDefault().get());
        add(startTimePicker, gbh.nextCol().fillHorizontally().setMaxWeightX().get());
        add(new JLabel(" - "), gbh.nextCol().setLabelDefault().get());
        add(endTimePicker, gbh.nextCol().fillHorizontally().setMaxWeightX().get());
        add(reloadButton, gbh.nextCol().setLabelDefault().get());
        topPanel.add(sqlTextArea, gbh.nextRowFirstCol().fillBoth().setMaxWeightX().setMaxWeightY().get());
        topPanel.add(typesPanel, gbh.nextCol().fillHorizontally().get());
        add(splitPane, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        rebuildRows();

    }

    public List<LogMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<LogMessage> messages) {
        this.messages = messages;
        if (messages.size() > 0) {
            int index = 0;
            while (messages.get(index).getTimestamp() == null) {
                index++;
                if (index > messages.size())
                    return;
            }
            startTimePicker.setDateTimePermissive(messages.get(index).getTimestamp().toLocalDateTime());
            index = messages.size() - 1;
            while (messages.get(index).getTimestamp() == null) {
                index--;
                if (index < 0)
                    return;
            }
            endTimePicker.setDateTimePermissive(messages.get(index).getTimestamp().toLocalDateTime());
        }
    }

    public void addMessage(LogMessage logMessage) {
        if (rows == null)
            rows = new ArrayList<>();
        checkLogMessage(logMessage);
    }

    void checkLogMessage(LogMessage msg) {
        boolean added = false;
        if (msg.getTimestamp() == null)
            return;
        if (!typesPanel.getSelectedValues().contains(msg.getTypeEvent()))
            return;
        int compareIndex = msg.getTimestamp().toLocalDateTime().compareTo(startTimePicker.getDateTime());
        if (compareIndex < 0)
            return;
        compareIndex = msg.getTimestamp().toLocalDateTime().compareTo(endTimePicker.getDateTime());
        if (compareIndex > 0)
            return;
        for (AnaliseRow row : rows) {
            if (msg.getStatementText() != null && row.getLogMessage().getStatementText().contentEquals(msg.getStatementText()) && msg.getTypeEvent().contentEquals(row.getLogMessage().getTypeEvent())) {
                row.addMessage(msg);
                added = true;
                break;
            }

        }
        if (!added && msg.getStatementText() != null) {
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
        for (AnaliseRow row : rows) {
            row.calculateValues();
        }
        model.fireTableDataChanged();
    }

    private String bundleString(String key) {
        return Bundles.get(AnalisePanel.class, key);
    }

    private int convertTypeFromCheckBoxes(int index) {
        int type = 0;
        for (int i = 0; i < TYPES.length; i++) {
            if (checkBoxes[i].isSelected())
                if (type == index)
                    return i;
                else type++;
        }
        return -1;
    }

    class AnaliseTableModel extends AbstractTableModel {


        @Override
        public int getRowCount() {
            if (rows != null)
                return rows.size();
            return 0;
        }

        @Override
        public int getColumnCount() {

            return headers.size();
        }

        @Override
        public String getColumnName(int columnIndex) {

            return headers.get(columnIndex);
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
                        return rows.get(rowIndex).getCountAllRows();
                    default:
                        int param = (columnIndex - 2) % 4;
                        int x = (columnIndex - 2) / 4;
                        int type = convertTypeFromCheckBoxes(x);
                        switch (param) {
                            case 0:
                                return rows.get(rowIndex).getTotal()[type];
                            case 1:
                                return rows.get(rowIndex).getAverage()[type];
                            case 2:
                                return rows.get(rowIndex).getMax()[type];
                            case 3:
                                return rows.get(rowIndex).getStd_dev()[type];
                        }
                }
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        }

    }

}
