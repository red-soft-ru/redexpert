package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.gui.browser.TraceManagerPanel;
import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.ListSelectionPanelEvent;
import org.underworldlabs.swing.ListSelectionPanelListener;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.EnumSet;

public class TablePanel extends JPanel {

    JTable table;
    SimpleSqlTextPanel txtFieldRawSql;
    private ResultSetDataModel dataModel;
    private JComboBox<Filter.FilterType> comboBoxFilterType;

    private JComboBox<String> comboBoxRawSql;
    private JComboBox<String> comboBoxFilterColumn;
    private JTextField txtFldSqlFilter;
    private final ListSelectionPanel columnsCheckPanel;


    public TablePanel(ListSelectionPanel columnsCheckPanel) {
        super(new BorderLayout());
        this.columnsCheckPanel = columnsCheckPanel;
        init();
    }

    private void init() {
        comboBoxFilterType = new JComboBox<>();
        comboBoxFilterColumn = new JComboBox<>();
        txtFldSqlFilter = new JTextField();
        comboBoxRawSql = new JComboBox<>();
        DynamicComboBoxModel dynamicComboBoxModel = new DynamicComboBoxModel();
        comboBoxRawSql.setModel(dynamicComboBoxModel);
        dynamicComboBoxModel = new DynamicComboBoxModel();
        comboBoxFilterColumn.setModel(dynamicComboBoxModel);
        dataModel = new ResultSetDataModel(columnsCheckPanel, comboBoxFilterType, comboBoxFilterColumn, comboBoxRawSql, txtFldSqlFilter);
        columnsCheckPanel.addListSelectionPanelListener(new ListSelectionPanelListener() {
            @Override
            public void changed(ListSelectionPanelEvent event) {
                dataModel.rebuildModel();
                dataModel.fireTableStructureChanged();
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

        JPanel topPanel = new JPanel();
        GridBagConstraints gbc_topPanel = new GridBagConstraints();
        gbc_topPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_topPanel.insets = new Insets(1, 1, 1, 1);
        gbc_topPanel.gridx = 0;
        gbc_topPanel.gridy = 0;
        gbc_topPanel.gridwidth = 1;
        gbc_topPanel.gridheight = 1;
        gbc_topPanel.anchor = GridBagConstraints.NORTHWEST;
        gbc_topPanel.weightx = 1;
        add(topPanel, gbc_topPanel);
        GridBagLayout gbl_topPanel = new GridBagLayout();
        topPanel.setLayout(gbl_topPanel);

        JPanel filterPanel = new JPanel();
        filterPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), TraceManagerPanel.bundleString("Filter"),
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagConstraints gbc_filterPanel = new GridBagConstraints();
        gbc_filterPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_filterPanel.insets = new Insets(0, 0, 0, 0);
        gbc_filterPanel.gridx = 0;
        gbc_filterPanel.gridy = 0;
        gbc_filterPanel.gridwidth = 1;
        gbc_filterPanel.gridheight = 1;
        gbc_filterPanel.weightx = 1;
        gbc_filterPanel.anchor = GridBagConstraints.NORTHWEST;
        topPanel.add(filterPanel, gbc_filterPanel);
        GridBagLayout gbl_filterPanel = new GridBagLayout();
        filterPanel.setLayout(gbl_filterPanel);

        comboBoxFilterType
                .setModel(new DefaultComboBoxModel<>(EnumSet.allOf(Filter.FilterType.class).toArray(new Filter.FilterType[0])));
        comboBoxFilterType.setSelectedItem(Filter.FilterType.HIGHLIGHT);
        comboBoxFilterType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dataModel.rebuildModel();
            }
        });


        GridBagConstraints gbc_filterTypeComboBox = new GridBagConstraints();
        gbc_filterTypeComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_filterTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_filterTypeComboBox.gridx = 0;
        gbc_filterTypeComboBox.gridy = 0;
        gbc_filterTypeComboBox.gridheight = 1;
        gbc_filterTypeComboBox.gridwidth = 1;
        gbc_filterTypeComboBox.anchor = GridBagConstraints.NORTHWEST;
        filterPanel.add(comboBoxFilterType, gbc_filterTypeComboBox);

        JLabel lblText = new JLabel(TraceManagerPanel.bundleString("Text"));
        GridBagConstraints gbc_lblText = new GridBagConstraints();
        gbc_lblText.anchor = GridBagConstraints.BASELINE_TRAILING;
        gbc_lblText.insets = new Insets(0, 0, 5, 5);
        gbc_lblText.gridx = 1;
        gbc_lblText.gridy = 0;
        gbc_lblText.gridwidth = 1;
        gbc_lblText.gridheight = 1;
        gbc_lblText.anchor = GridBagConstraints.NORTHWEST;
        filterPanel.add(lblText, gbc_lblText);

        GridBagConstraints gbc_txtFldSqlFilter = new GridBagConstraints();
        gbc_txtFldSqlFilter.anchor = GridBagConstraints.NORTHWEST;
        gbc_txtFldSqlFilter.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtFldSqlFilter.insets = new Insets(0, 0, 5, 0);
        gbc_txtFldSqlFilter.gridx = 2;
        gbc_txtFldSqlFilter.gridy = 0;
        gbc_txtFldSqlFilter.weightx = 1;
        filterPanel.add(txtFldSqlFilter, gbc_txtFldSqlFilter);

        lblText = new JLabel(TraceManagerPanel.bundleString("FilterColumn"));
        gbc_lblText = new GridBagConstraints();
        gbc_lblText.anchor = GridBagConstraints.NORTHWEST;
        gbc_lblText.insets = new Insets(0, 5, 5, 5);
        gbc_lblText.gridx = 3;
        gbc_lblText.gridy = 0;
        gbc_lblText.gridwidth = 1;
        gbc_lblText.gridheight = 1;
        filterPanel.add(lblText, gbc_lblText);

        GridBagConstraints gbc_comboBoxRawSql = new GridBagConstraints();
        gbc_comboBoxRawSql.insets = new Insets(0, 0, 5, 5);
        gbc_comboBoxRawSql.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBoxRawSql.gridx = 4;
        gbc_comboBoxRawSql.gridy = 0;
        gbc_comboBoxRawSql.gridheight = 1;
        gbc_comboBoxRawSql.gridwidth = 1;
        gbc_comboBoxRawSql.anchor = GridBagConstraints.NORTHWEST;
        filterPanel.add(comboBoxFilterColumn, gbc_comboBoxRawSql);

        lblText = new JLabel(TraceManagerPanel.bundleString("RawSQLColumn"));
        gbc_lblText = new GridBagConstraints();
        gbc_lblText.anchor = GridBagConstraints.NORTHWEST;
        gbc_lblText.insets = new Insets(0, 5, 5, 5);
        gbc_lblText.gridx = 5;
        gbc_lblText.gridy = 0;
        gbc_lblText.gridwidth = 1;
        gbc_lblText.gridheight = 1;
        filterPanel.add(lblText, gbc_lblText);

        gbc_comboBoxRawSql = new GridBagConstraints();
        gbc_comboBoxRawSql.insets = new Insets(0, 0, 5, 5);
        gbc_comboBoxRawSql.fill = GridBagConstraints.HORIZONTAL;
        gbc_comboBoxRawSql.gridx = 6;
        gbc_comboBoxRawSql.gridy = 0;
        gbc_comboBoxRawSql.gridheight = 1;
        gbc_comboBoxRawSql.gridwidth = 1;
        gbc_comboBoxRawSql.anchor = GridBagConstraints.NORTHWEST;
        filterPanel.add(comboBoxRawSql, gbc_comboBoxRawSql);

        txtFldSqlFilter.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(final UndoableEditEvent e) {
                dataModel.rebuildModel();
            }
        });

        //topPanel.add(hideShowColumnsButton, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));

        /*columnsCheckPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Columns",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GridBagConstraints gbc_typeEventPanel = new GridBagConstraints();
        gbc_typeEventPanel.fill = GridBagConstraints.BOTH;
        gbc_typeEventPanel.insets = new Insets(5, 5, 5, 5);
        gbc_typeEventPanel.gridx = 0;
        gbc_typeEventPanel.gridy = 2;
        gbc_typeEventPanel.gridwidth = 1;
        gbc_typeEventPanel.gridheight = 1;
        gbc_typeEventPanel.weightx = 0;
        gbc_typeEventPanel.weighty = 0;
        gbc_typeEventPanel.anchor = GridBagConstraints.NORTHWEST;*/
        //topPanel.add(columnsCheckPanel, gbc_typeEventPanel);


        JSplitPane splitPane = new JSplitPane();
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.8);
        GridBagConstraints gbc_splitPane = new GridBagConstraints();
        gbc_splitPane.fill = GridBagConstraints.BOTH;
        gbc_splitPane.insets = new Insets(5, 5, 5, 5);
        gbc_splitPane.gridx = 0;
        gbc_splitPane.gridy = 1;
        gbc_splitPane.gridheight = 1;
        gbc_splitPane.gridwidth = 1;
        gbc_splitPane.weighty = 1;
        gbc_splitPane.anchor = GridBagConstraints.NORTH;
        add(splitPane, gbc_splitPane);

        JScrollPane logListPanel = new JScrollPane();
        logListPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        table = new JTable(dataModel);
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
                int col = dataModel.getVisibleColumnNames().indexOf(comboBoxRawSql.getSelectedItem());
                if (row >= 0 && col >= 0) {
                    Object obj = table.getValueAt(row, col);
                    if (obj == null) {
                        txtFieldRawSql.setSQLText("null");
                    } else {
                        txtFieldRawSql.setSQLText(String.valueOf(obj));
                    }
                }
            }
        });


        splitPane.setTopComponent(logListPanel);
        txtFieldRawSql = new SimpleSqlTextPanel();
        splitPane.setBottomComponent(txtFieldRawSql);


    }

    public void addRow(LogMessage message) {

        dataModel.addRow(message);
    }

    public void clearAll() {
        dataModel.clearAll();
    }


}