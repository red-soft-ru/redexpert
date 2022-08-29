package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.TraceManagerPanel;
import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
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
import java.util.EnumSet;

public class TablePanel extends JPanel {

    JTable table;
    SimpleSqlTextPanel txtFieldRawSql;
    private ResultSetDataModel dataModel;
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

    private static String filePathWidthCols() {

        UserSettingsProperties settings = new UserSettingsProperties();
        return settings.getUserSettingsDirectory() + "width-columns.properties";
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
            if (!MiscUtils.isNull(strCols)) {
                String[] cols = strCols.split("\n");
                columnsCheckPanel.removeAllAction();
                for (int i = 0; i < cols.length; i++) {
                    columnsCheckPanel.selectOneStringAction(cols[i]);
                }
            }
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
        dataModel = new ResultSetDataModel(columnsCheckPanel, comboBoxFilterType, comboBoxFilterColumn, txtFldSqlFilter, matchCaseBox);
        table = new JTable(dataModel);
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
                dataModel.rebuildModel();
                dataModel.fireTableStructureChanged();
                saveCols();
            }
        });
        /*for (JCheckBox checkBox : columnsCheckPanel.getCheckBoxMap().values()) {
            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    dataModel.rebuildModel();
                    dataModel.fireTableStructureChanged();
                }
            });
        }*/
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();
        JPanel filterPanel = new JPanel();
        filterPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), TraceManagerPanel.bundleString("Filter"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        filterPanel.setLayout(new GridBagLayout());

        comboBoxFilterType
                .setModel(new DefaultComboBoxModel<>(EnumSet.allOf(Filter.FilterType.class).toArray(new Filter.FilterType[0])));
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

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.8);
        add(splitPane, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());

        JScrollPane logListPanel = new JScrollPane();
        logListPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        table.setRowSorter(new CustomTableRowSorter(dataModel));
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        table.setDefaultRenderer(Integer.class, new CustomTableCellRenderer());
        table.setDefaultRenderer(Timestamp.class, new StatementTimestampTableCellRenderer());
        logListPanel.setViewportView(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0)
                    txtFieldRawSql.setSQLText(dataModel.getVisibleRows().get(row).getBody());

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


        splitPane.setTopComponent(logListPanel);
        txtFieldRawSql = new SimpleSqlTextPanel();
        txtFieldRawSql.setBorder(BorderFactory.createTitledBorder(TraceManagerPanel.bundleString("Body")));
        splitPane.setBottomComponent(txtFieldRawSql);


    }

    public void addRow(LogMessage message) {

        dataModel.addRow(message);
    }

    public void clearAll() {
        dataModel.clearAll();
    }


}