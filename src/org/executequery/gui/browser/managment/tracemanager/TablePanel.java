package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.TraceManagerPanel;
import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
import org.executequery.gui.browser.managment.tracemanager.net.TableCounter;
import org.executequery.gui.editor.SimpleDataItemViewerPanel;
import org.executequery.gui.resultset.SimpleRecordDataItem;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.RepositoryException;
import org.executequery.util.AuditProperties;
import org.executequery.util.SystemResources;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.ListSelectionPanelEvent;
import org.underworldlabs.swing.ListSelectionPanelListener;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;

public class TablePanel extends JPanel {

    JTable table;

    JTable tableCounter;

    TableCounterModel tableCounterModel;
    SimpleSqlTextPanel txtFieldRawSql;
    private TraceDataModel dataModel;
    private JComboBox<Filter.FilterType> comboBoxFilterType;
    private JComboBox<String> comboBoxFilterColumn;
    private JTextField txtFldSqlFilter;
    private final ListSelectionPanel columnsCheckPanel;

    private JCheckBox matchCaseBox;


    public TablePanel(ListSelectionPanel columnsCheckPanel) {
        super(new BorderLayout());
        this.columnsCheckPanel = columnsCheckPanel;
        init();
    }

    private static String filePathVisibleCols() {

        UserSettingsProperties settings = new UserSettingsProperties();
        return settings.getUserSettingsDirectory() + "audit-columns.txt";
    }

    private static void ensureFileExists(String path) {

        File file = new File(path);
        if (!file.exists()) {

            try {

                FileUtils.writeFile(path, "#Red Expert - User Defined System Properties");

            } catch (IOException e) {

                throw new RepositoryException(e);
            }

        }

    }

    private void loadCols() {
        ensureFileExists(filePathVisibleCols());
        try {
            String strCols = FileUtils.loadFile(filePathVisibleCols());
            if (!MiscUtils.isNull(strCols) && !strCols.trim().contentEquals("#Red Expert - User Defined System Properties")) {
                String[] cols = strCols.split("\n");
                columnsCheckPanel.removeAllAction();
                for (int i = 0; i < cols.length; i++) {
                    columnsCheckPanel.selectOneStringAction(cols[i]);
                }
            } else columnsCheckPanel.selectAllAction();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    AuditProperties widthProps;
    boolean loadWidthCols = false;
    private void loadWidthCols() {
        loadWidthCols = true;
        widthProps = AuditProperties.getInstance();
        try {
            for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
                if (widthProps.getProperty(getWidthKey(table.getColumnName(i))) != null)
                    table.getColumnModel().getColumn(i).setPreferredWidth(Integer.parseInt(widthProps.getProperty(getWidthKey(table.getColumnName(i)))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadWidthCols = false;
        }
    }

    String getWidthKey(String key) {
        return key + ".width";
    }

    private void saveCols() {
        ensureFileExists(filePathVisibleCols());
        try {
            StringBuilder sb = new StringBuilder();
            for (Object col : columnsCheckPanel.getSelectedValues()) {
                sb.append(col).append("\n");
            }
            FileUtils.writeFile(filePathVisibleCols(), sb.toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        comboBoxFilterType = new JComboBox<>();
        comboBoxFilterColumn = new JComboBox<>();
        txtFldSqlFilter = new JTextField();
        matchCaseBox = new JCheckBox(TraceManagerPanel.bundleString("matchCase"));
        DynamicComboBoxModel dynamicComboBoxModel = new DynamicComboBoxModel();
        dynamicComboBoxModel = new DynamicComboBoxModel();
        comboBoxFilterColumn.setModel(dynamicComboBoxModel);
        loadCols();
        dataModel = new TraceDataModel(columnsCheckPanel, comboBoxFilterType, comboBoxFilterColumn, txtFldSqlFilter, matchCaseBox);
        table = new JTable(dataModel);
        tableCounterModel = new TableCounterModel();
        tableCounter = new JTable(tableCounterModel) {
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);
                String tip = tableCounterModel.getColumnName(realColumnIndex);
                if (realColumnIndex >= 1) {
                    long sum = 0;
                    long avg = 0;
                    for (TableCounter counter : tableCounterModel.getVisibleRows()) {
                        if (counter.getCounter(realColumnIndex) != null)
                            sum += (long) counter.getCounter(realColumnIndex);
                    }
                    if (tableCounterModel.getVisibleRows().size() > 0) {
                        avg = sum / tableCounterModel.getVisibleRows().size();
                    }
                    tip += "   SUM:" + sum + "    AVG:" + avg;
                }
                return tip;
            }
        };
        tableCounter.addMouseListener(new ServiceManagerPopupMenu(tableCounter));
        loadWidthCols();
        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                loadWidthCols();
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
                //loadWidthCols();
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                //loadWidthCols();
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                if (!loadWidthCols) {
                    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
                        widthProps.setProperty(getWidthKey(table.getColumnName(i)), String.valueOf(table.getColumnModel().getColumn(i).getPreferredWidth()));
                    }
                    SystemResources.setAuditPreferences(widthProps.getProperties());
                }
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {

            }
        });

        columnsCheckPanel.addListSelectionPanelListener(new ListSelectionPanelListener() {
            @Override
            public void changed(ListSelectionPanelEvent event) {
                columnsCheckPanel.getAvailableValues().sort(Comparator.naturalOrder());
                dataModel.rebuildModel();
                dataModel.fireTableStructureChanged();
                saveCols();
            }
        });
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();
        JPanel filterPanel = new JPanel();
        filterPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), TraceManagerPanel.bundleString("Filter"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        filterPanel.setLayout(new GridBagLayout());

        comboBoxFilterType.addItem(Filter.FilterType.HIGHLIGHT);
        comboBoxFilterType.addItem(Filter.FilterType.FILTER);
        comboBoxFilterType.setSelectedItem(Filter.FilterType.HIGHLIGHT);
        comboBoxFilterType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dataModel.rebuildModel();
            }
        });
        filterPanel.add(comboBoxFilterType, gbh.get());

        gbh.addLabelFieldPair(filterPanel, TraceManagerPanel.bundleString("FilterColumn"), comboBoxFilterColumn, null, false, false);
        filterPanel.add(matchCaseBox, gbh.nextCol().setLabelDefault().get());

        gbh.addLabelFieldPair(filterPanel, TraceManagerPanel.bundleString("Text"), txtFldSqlFilter, null, false);
        txtFldSqlFilter.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(final UndoableEditEvent e) {
                dataModel.rebuildModel();
            }
        });
        gbh.fullDefaults();
        add(filterPanel, gbh.fillHorizontally().spanX().get());

        JSplitPane littleSplit = new JSplitPane();
        littleSplit.setOneTouchExpandable(true);
        littleSplit.setBorder(null);
        littleSplit.setContinuousLayout(true);
        littleSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        littleSplit.setResizeWeight(0.8);


        JScrollPane logListPanel = new JScrollPane();
        logListPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        table.setRowSorter(new CustomTableRowSorter(dataModel));
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        table.setDefaultRenderer(Long.class, new CustomTableCellRenderer());
        table.setDefaultRenderer(Timestamp.class, new StatementTimestampTableCellRenderer());
        logListPanel.setViewportView(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    row = table.getRowSorter().convertRowIndexToModel(row);
                    txtFieldRawSql.setSQLText(dataModel.getVisibleRows().get(row).getBody());
                    fillTableCounters(row);
                }

              /*
                    int col = dataModel.getVisibleColumnNames().indexOf(comboBoxRawSql.getSelectedItem());
                    if (row >= 0 && col >= 0) {
                        Object obj = table.getValueAt(row, col);
                        if (obj == null) {
                            txtFieldRawSql.setSQLText("null");
                        } else {
                            txtFieldRawSql.setSQLText(String.valueOf(obj));
                        }
                    }
                }*/
            }
        });

        table.addMouseListener(new ServiceManagerPopupMenu(table));

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


        littleSplit.setTopComponent(logListPanel);
        txtFieldRawSql = new SimpleSqlTextPanel();
        txtFieldRawSql.setBorder(BorderFactory.createTitledBorder(TraceManagerPanel.bundleString("Body")));
        littleSplit.setBottomComponent(txtFieldRawSql);

        JSplitPane bigSplit = new JSplitPane();
        bigSplit.setOneTouchExpandable(true);
        bigSplit.setBorder(null);
        bigSplit.setContinuousLayout(true);
        bigSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
        bigSplit.setResizeWeight(0.8);

        bigSplit.setTopComponent(littleSplit);
        JScrollPane scroll = new JScrollPane(tableCounter);
        scroll.setBorder(BorderFactory.createTitledBorder("Table counters"));
        bigSplit.setBottomComponent(scroll);


        add(bigSplit, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());

    }

    public List<LogMessage> getTableRows()
    {
        return dataModel.getRows();
    }

    private void fillTableCounters(int row) {
        tableCounterModel.clearAll();
        LogMessage logMessage = dataModel.getVisibleRows().get(row);
        String fullBody = logMessage.getTableCounters();
        if (!MiscUtils.isNull(fullBody)) {
            String[] rows = fullBody.split("\n");
            for (int i = 2; i < rows.length; i++) {
                TableCounter tc = new TableCounter(rows[0], rows[i]);
                tableCounterModel.addRow(tc);
            }
        }
    }

    public void setEnableElements(boolean flag) {
        setEnableElements(this, flag);
    }

    public void setEnableElements(Container container, boolean flag) {
        for (Component component : container.getComponents()) {
            component.setEnabled(flag);
            if (component instanceof Container)
                setEnableElements((Container) component, flag);
        }
    }

    public void addRow(LogMessage message) {

        dataModel.addRow(message);
    }

    public void clearAll() {
        dataModel.clearAll();
        txtFieldRawSql.setSQLText("");
    }

    public void cleanup() {
        txtFieldRawSql.cleanup();
        txtFieldRawSql = null;
    }

    public int countRows() {
        return dataModel.getRowCount();
    }

    public int getSelectedRow() {
        int row = table.getSelectedRow();
        if (row >= 0)
            row = table.convertRowIndexToModel(row);
        return row;
    }

    public int getSelectedCol() {
        int col = table.getSelectedColumn();
        if (col >= 0)
            col = table.convertColumnIndexToModel(col);
        return col;
    }

    public void setSelectedRow(int row) {
        table.getSelectionModel().setSelectionInterval(row, row);
    }

    public void setSelectedCol(int col) {
        table.getColumnModel().getSelectionModel().setSelectionInterval(col, col);
    }


}