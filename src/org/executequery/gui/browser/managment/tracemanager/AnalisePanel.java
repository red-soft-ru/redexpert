package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.GUIUtilities;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.LoggingOutputPanel;
import org.executequery.gui.browser.managment.tracemanager.net.AnaliseRow;
import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
import org.executequery.gui.editor.SimpleDataItemViewerPanel;
import org.executequery.gui.resultset.SimpleRecordDataItem;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.executequery.sql.SqlMessages;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.celleditor.picker.DefaultDateTimePicker;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.executequery.gui.browser.managment.tracemanager.net.AnaliseRow.*;

public class AnalisePanel extends JPanel {
    List<AnaliseRow> rows;

    List<String> headers;
    List<LogMessage> messages;
    JTable table;
    AnaliseTableModel model;
    SimpleSqlTextPanel sqlTextArea;
    ListSelectionPanel typesPanel;
    LoggingOutputPanel planPanel;
    /*Color[] colors = new Color[]{new Color(244, 66, 54),
            new Color( 63, 81, 181),
            new Color(33, 150, 243),
            new Color(139, 194, 74),
            new Color(255, 235, 60),
            new Color(0, 188, 213),
            new Color(255, 151, 0)
    };*///dark colors


    JCheckBox[] checkBoxes;

    JCheckBox roundCheckBox;
    JCheckBox filterCheckBox;
    NumberTextField numberSymbolsField;
    JCheckBox showMoreParamsBox;
    JToolBar moreParamsPanels;

    int beginParamIndex = 2;

    String[] types = {"TRACE_INIT", "TRACE_FINI", "CREATE_DATABASE", "ATTACH_DATABASE", "DROP_DATABASE", "DETACH_DATABASE", "START_TRANSACTION",
            "COMMIT_RETAINING", "COMMIT_TRANSACTION", "ROLLBACK_RETAINING", "ROLLBACK_TRANSACTION", "EXECUTE_STATEMENT_START", "EXECUTE_STATEMENT_FINISH",
            "START_SERVICE", "PREPARE_STATEMENT", "FREE_STATEMENT", "CLOSE_CURSOR", "SET_CONTEXT", "PRIVILEGES_CHANGE", "EXECUTE_PROCEDURE_START",
            "EXECUTE_FUNCTION_START", "EXECUTE_PROCEDURE_FINISH", "EXECUTE_FUNCTION_FINISH", "EXECUTE_TRIGGER_START", "EXECUTE_TRIGGER_FINISH", "COMPILE_BLR",
            "EXECUTE_BLR", "EXECUTE_DYN", "ATTACH_SERVICE", "DETACH_SERVICE", "QUERY_SERVICE", "SWEEP_START", "SWEEP_FINISH", "SWEEP_FAILED", "SWEEP_PROGRESS"};

    DefaultDateTimePicker startTimePicker;
    DefaultDateTimePicker endTimePicker;
    TableRowSorter rowSorter;
    JCheckBox showPlanBox;

    public AnalisePanel(List<LogMessage> messages) {
        this.messages = messages;
        init();
    }

    void buildHeaders() {
        headers.clear();
        headers.add("QUERY");
        headers.add("COUNT");
        if (planPanel == null || planPanel.isVisible()) {
            headers.add("PLAN_COUNT");
            beginParamIndex = 3;
        } else beginParamIndex = 2;
        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isSelected()) {
                for (int g = 0; g < PARAMS.length; g++)
                    headers.add(PARAMS[g] + "_" + TYPES[i]);
            }
        }

        model.fireTableStructureChanged();
        setMinWidthCols();
    }

    private void setMinWidthCols() {
        if (table != null) {

            int colWidth = 120;
            if (getWidth() / table.getColumnCount() < colWidth) {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
                    table.getColumnModel().getColumn(i).setPreferredWidth(colWidth);
                    table.getColumnModel().getColumn(i).setWidth(colWidth);
                    table.getColumnModel().getColumn(i).setMinWidth(colWidth);
                }
            } else table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        }
    }

    AnaliseRow sumRow;
    boolean terminate = false;

    public boolean isTerminate() {
        return terminate;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    void runRebuildRowsInThread() {
        SwingWorker sw = new SwingWorker("rebuildAuditAnalise") {
            @Override
            public Object construct() {
                GUIUtilities.showWaitCursor();
                try {
                    rebuildRows();
                } catch (Exception e) {
                    if (!isTerminate())
                        e.printStackTrace();
                }
                setTerminate(false);
                return null;
            }

            @Override
            public void finished() {
                GUIUtilities.showNormalCursor();
            }
        };
        sw.start();
    }

    void showMoreParams(boolean visible) {
        moreParamsPanels.setVisible(visible);
    }

    public List<LogMessage> getMessages() {
        return messages;
    }

    private void updateSorter() {
        if (headers.size() > beginParamIndex) {
            rowSorter.toggleSortOrder(beginParamIndex + 1);
        }


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
            startTimePicker.setDateTime(messages.get(index).getTimestamp().toLocalDateTime());
            index = messages.size() - 1;
            while (messages.get(index).getTimestamp() == null) {
                index--;
                if (index < 0)
                    return;
            }
            endTimePicker.setDateTime(messages.get(index).getTimestamp().toLocalDateTime());
        }
    }

    public void addMessage(LogMessage logMessage, boolean realTime) {
        if (rows == null)
            rows = new ArrayList<>();
        checkLogMessage(logMessage, realTime);
    }

    void checkLogMessage(LogMessage msg, boolean realTime) {
        boolean added = false;
        if (msg.getTimestamp() == null)
            return;
        if (!typesPanel.getSelectedValues().contains(msg.getTypeEvent()))
            return;
        if (!realTime) {
            int compareIndex = msg.getTimestamp().toLocalDateTime().compareTo(startTimePicker.getDateTime());
            if (compareIndex < 0)
                return;
            compareIndex = msg.getTimestamp().toLocalDateTime().compareTo(endTimePicker.getDateTime());
            if (compareIndex > 0)
                return;
        }
        for (AnaliseRow row : rows) {
            if (msg.getStatementText() != null && row.getLogMessage().getStatementText() != null && row.getLogMessage().getStatementText().contentEquals(msg.getStatementText()) && msg.getTypeEvent().contentEquals(row.getLogMessage().getTypeEvent())) {
                row.addMessage(msg);
                added = true;
                break;
            }
            if (msg.getProcedureName() != null && row.getLogMessage().getProcedureName() != null && row.getLogMessage().getProcedureName().contentEquals(msg.getProcedureName()) && msg.getTypeEvent().contentEquals(row.getLogMessage().getTypeEvent())) {
                row.addMessage(msg);
                added = true;
                break;
            }

            if (msg.getStatementText() != null && row.getLogMessage().getStatementText() != null && filterCheckBox.isSelected() && msg.getStatementText().length() > numberSymbolsField.getValue() && row.getLogMessage().getStatementText().length() > numberSymbolsField.getValue())
                if (row.getLogMessage().getStatementText().startsWith(msg.getStatementText().substring(0, numberSymbolsField.getValue()))) {
                    row.addMessage(msg);
                    added = true;
                    break;
                }

        }
        if (!added && (msg.getStatementText() != null || msg.getProcedureName() != null)) {
            AnaliseRow row = new AnaliseRow();
            row.addMessage(msg);
            rows.add(row);
        }
        if (realTime)
            model.fireTableDataChanged();
    }
    AnaliseRow avgRow;

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
            checkBoxes[i].setBackground(COLORS[i]);
            checkBoxes[i].setToolTipText(TOOLTIPS[i]);
        }
        roundCheckBox = new JCheckBox(bundleString("roundValues"));
        roundCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                model.fireTableDataChanged();
            }
        });
        filterCheckBox = new JCheckBox(bundleString("filterText"));
        filterCheckBox.setToolTipText(bundleString("filterToolTip"));
        filterCheckBox.setVerticalAlignment(SwingConstants.CENTER);
        filterCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                numberSymbolsField.setEnabled(filterCheckBox.isSelected());
                runRebuildRowsInThread();
            }
        });
        numberSymbolsField = new NumberTextField();
        numberSymbolsField.setValue(100);
        numberSymbolsField.setEnabled(false);
        numberSymbolsField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                runRebuildRowsInThread();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                runRebuildRowsInThread();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                runRebuildRowsInThread();
            }
        });
        showMoreParamsBox = new JCheckBox(bundleString("showMoreParams"));
        showMoreParamsBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                showMoreParams(showMoreParamsBox.isSelected());
            }
        });
        moreParamsPanels = new JToolBar();
        moreParamsPanels.setBorder(BorderFactory.createTitledBorder(bundleString("extraParams")));
        model = new AnaliseTableModel();
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getLastRow() == TableModelEvent.HEADER_ROW) {
                    if (rowSorter != null)
                        updateSorter();
                }
            }
        });
        buildHeaders();
        table = new JTable(model) {
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);
                String tip = model.getColumnName(realColumnIndex);
                if (realColumnIndex >= beginParamIndex) {

                    int param = (realColumnIndex - beginParamIndex) % PARAMS.length;
                    int type = convertTypeFromCheckBoxes(realColumnIndex);
                    if (sumRow != null) {
                        tip += "   SUM:" + sumRow.getValueFromTypeAndParam(type, param).getDisplayValue(roundCheckBox.isSelected()) + "    ";
                    }
                    if (avgRow != null) {
                        tip += "AVG:" + avgRow.getValueFromTypeAndParam(type, param).getDisplayValue(roundCheckBox.isSelected());
                    }
                }
                return tip;
            }
        };
        table.setDefaultRenderer(AnaliseRow.AnaliseValue.class, new AnaliseRenderer());


        sqlTextArea = new SimpleSqlTextPanel();
        typesPanel = new ListSelectionPanel();
        typesPanel.setVisible(false);
        typesPanel.setLabelText(bundleString("AvailableEvents"), bundleString("SelectedEvents"));
        typesPanel.createAvailableList(types);
        typesPanel.selectOneStringAction("EXECUTE_STATEMENT_FINISH");
        typesPanel.selectOneStringAction("EXECUTE_PROCEDURE_FINISH");
        typesPanel.selectOneStringAction("EXECUTE_FUNCTION_FINISH");
        typesPanel.addListSelectionPanelListener(new ListSelectionPanelListener() {
            @Override
            public void changed(ListSelectionPanelEvent event) {
                rebuildRows();
            }
        });
        startTimePicker = new DefaultDateTimePicker();
        endTimePicker = new DefaultDateTimePicker();
        startTimePicker.setVisibleNullBox(false);
        endTimePicker.setVisibleNullBox(false);
        planPanel = new LoggingOutputPanel();
        planPanel.setBorder(BorderFactory.createTitledBorder(bundleString("Plan")));
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();

        gbh.fullDefaults();

        JScrollPane logListPanel = new JScrollPane();
        logListPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        rowSorter = new AnaliseSorter<>(model);
        table.setRowSorter(rowSorter);
        updateSorter();
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
                    if (rows.get(row).getLogMessage().getStatementText() != null)
                        sqlTextArea.setSQLText(rows.get(row).getLogMessage().getStatementText());
                    else sqlTextArea.setSQLText(rows.get(row).getLogMessage().getProcedureName());
                    planPanel.clear();
                    int type = SqlMessages.PLAIN_MESSAGE;
                    if (rows.get(row).countPlans() > 1)
                        type = SqlMessages.ERROR_MESSAGE;
                    planPanel.append(type, rows.get(row).getPlanText());

                }
            }
        });

        JButton selectEventsButton = new JButton(bundleString("ShowEvents"));
        selectEventsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                typesPanel.setVisible(!typesPanel.isVisible());
                if (typesPanel.isVisible())
                    selectEventsButton.setText(bundleString("HideEvents"));
                else selectEventsButton.setText(bundleString("ShowEvents"));
            }
        });

        showPlanBox = new JCheckBox(bundleString("ShowPlan"));
        showPlanBox.setSelected(true);
        showPlanBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                planPanel.setVisible(showPlanBox.isSelected());
                buildHeaders();
            }
        });

        JButton reloadButton = new JButton(Bundles.getCommon("rebuild"));
        reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runRebuildRowsInThread();
            }
        });
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(logListPanel);

        gbh.setLabelDefault();
        gbh.nextRowFirstCol().previousCol();
        for (int i = 0; i < checkBoxes.length; i++)
            add(checkBoxes[i], gbh.nextCol().setLabelDefault().get());
        add(roundCheckBox, gbh.nextCol().setLabelDefault().get());
        add(showPlanBox, gbh.nextCol().setLabelDefault().get());
        add(showMoreParamsBox, gbh.nextCol().setLabelDefault().get());
        add(reloadButton, gbh.nextCol().setLabelDefault().get());

        moreParamsPanels.setLayout(new GridBagLayout());

        moreParamsPanels.add(new JLabel(bundleString("Period")), gbh.nextRowFirstCol().setLabelDefault().get());
        moreParamsPanels.add(startTimePicker, gbh.nextCol().setLabelDefault().get());
        moreParamsPanels.add(new JLabel(" - "), gbh.nextCol().setLabelDefault().get());
        moreParamsPanels.add(endTimePicker, gbh.nextCol().setLabelDefault().get());
        moreParamsPanels.add(filterCheckBox, gbh.nextCol().setLabelDefault().get());
        moreParamsPanels.add(new JLabel("N:"), gbh.nextCol().setLabelDefault().get());
        moreParamsPanels.add(numberSymbolsField, gbh.nextCol().setLabelDefault().get());
        moreParamsPanels.add(selectEventsButton, gbh.nextCol().setLabelDefault().get());
        moreParamsPanels.add(new JPanel(), gbh.nextCol().fillHorizontally().spanX().get());
        add(moreParamsPanels, gbh.nextRowFirstCol().previousRow().fillHorizontally().spanX().get());
        gbh.setLabelDefault();
        topPanel.add(sqlTextArea, gbh.nextRowFirstCol().fillBoth().setMaxWeightX().setMaxWeightY().get());
        topPanel.add(typesPanel, gbh.nextCol().fillHorizontally().spanY().get());
        topPanel.add(planPanel, gbh.nextRowFirstCol().fillBoth().setMaxWeightX().setWeightY(0.5).get());
        add(splitPane, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        showMoreParams(false);
        rebuildRows();

    }

    public synchronized void rebuildRows() {
        table.setEnabled(false);
        if (rows == null)
            rows = new ArrayList<>();
        rows.clear();
        model.fireTableDataChanged();
        for (LogMessage msg : messages) {
            checkLogMessage(msg, false);
        }
        if (rows.size() > 0) {
            AnaliseRow maxRow = new AnaliseRow();
            avgRow = new AnaliseRow();
            sumRow = new AnaliseRow();
            for (AnaliseRow row : rows) {
                row.calculateValues();
                for (int i = 0; i < TYPES.length; i++) {
                    for (int g = 0; g < PARAMS.length; g++) {
                        if (row.getValueFromTypeAndParam(i, g).getLongValue() > maxRow.getValueFromTypeAndParam(i, g).getLongValue()) {
                            maxRow.getValueFromTypeAndParam(i, g).setLongValue(row.getValueFromTypeAndParam(i, g).getLongValue());
                        }
                        sumRow.getValueFromTypeAndParam(i, g).setLongValue(row.getValueFromTypeAndParam(i, g).getLongValue() + sumRow.getValueFromTypeAndParam(i, g).getLongValue());
                    }
                }
            }
            for (int i = 0; i < TYPES.length; i++) {
                for (int g = 0; g < PARAMS.length; g++) {
                    if (rows.size() > 0)
                        avgRow.getValueFromTypeAndParam(i, g).setLongValue(sumRow.getValueFromTypeAndParam(i, g).getLongValue() / rows.size());
                }
            }
            for (AnaliseRow row : rows) {
                for (int i = 0; i < TYPES.length; i++) {
                    for (int g = 0; g < PARAMS.length; g++) {
                        AnaliseRow.AnaliseValue analiseValue = row.getValueFromTypeAndParam(i, g);
                        if (maxRow.getValueFromTypeAndParam(i, g).getLongValue() > 0)
                            analiseValue.setPercent((int) (analiseValue.getLongValue() * 100 / maxRow.getValueFromTypeAndParam(i, g).getLongValue()));
                        else analiseValue.setPercent(100);
                    }
                }
            }
        }
        model.fireTableDataChanged();
        table.setEnabled(true);
    }

    private String bundleString(String key) {
        return Bundles.get(AnalisePanel.class, key);
    }

    private int convertTypeFromCheckBoxes(int index) {
        index = (index - beginParamIndex) / PARAMS.length;
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
            } else if (columnIndex < beginParamIndex)
                return Long.class;
            else
                return AnaliseRow.AnaliseValue.class;
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
                        if (rows.get(rowIndex).getLogMessage().getStatementText() != null)
                            return rows.get(rowIndex).getLogMessage().getStatementText();
                        else return rows.get(rowIndex).getLogMessage().getProcedureName();
                    case 1:
                        return rows.get(rowIndex).getCountAllRows();
                    case 2:
                        if (headers.contains("PLAN_COUNT"))
                            return rows.get(rowIndex).countPlans();
                    default:
                        int param = (columnIndex - beginParamIndex) % PARAMS.length;
                        int type = convertTypeFromCheckBoxes(columnIndex);
                        return rows.get(rowIndex).getValueFromTypeAndParam(type, param);
                }
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        }

    }

    class AnaliseRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component superComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof AnaliseRow.AnaliseValue) {
                AnaliseRow.AnaliseValue analiseValue = (AnaliseRow.AnaliseValue) value;
                setText(analiseValue.getDisplayValue(roundCheckBox.isSelected()));
                superComp.setBackground(COLORS[analiseValue.getType()]);
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else superComp.setBackground(Color.WHITE);
            return superComp;
        }
    }

    public static class AnaliseSorter<M extends TableModel> extends TableRowSorter {
        public AnaliseSorter(TableModel model) {
            super(model);
        }

        public void toggleSortOrder(int column) {
            checkColumn(column);
            if (isSortable(column)) {
                List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
                SortKey sortKey;
                int sortIndex;
                for (sortIndex = keys.size() - 1; sortIndex >= 0; sortIndex--) {
                    if (keys.get(sortIndex).getColumn() == column) {
                        break;
                    }
                }
                if (sortIndex == -1) {
                    // Key doesn't exist
                    sortKey = new SortKey(column, SortOrder.DESCENDING);
                    keys.add(0, sortKey);
                } else if (sortIndex == 0) {
                    // It's the primary sorting key, toggle it
                    keys.set(0, toggle(keys.get(0)));
                } else {
                    // It's not the first, but was sorted on, remove old
                    // entry, insert as first with ascending.
                    keys.remove(sortIndex);
                    keys.add(0, new SortKey(column, SortOrder.DESCENDING));
                }
                if (keys.size() > getMaxSortKeys()) {
                    keys = keys.subList(0, getMaxSortKeys());
                }
                setSortKeys(keys);
            }
        }

        private SortKey toggle(SortKey key) {
            if (key.getSortOrder() == SortOrder.DESCENDING) {
                return new SortKey(key.getColumn(), SortOrder.ASCENDING);
            }
            return new SortKey(key.getColumn(), SortOrder.DESCENDING);
        }

        private void checkColumn(int column) {
            if (column < 0 || column >= getModelWrapper().getColumnCount()) {
                throw new IndexOutOfBoundsException(
                        "column beyond range of TableModel");
            }
        }
    }

}
