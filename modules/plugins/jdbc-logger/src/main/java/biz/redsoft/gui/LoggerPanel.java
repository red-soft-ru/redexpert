package biz.redsoft.gui;

import biz.redsoft.net.AbstractLogReceiver;
import biz.redsoft.net.ServerLogReceiver;
import ch.sla.jdbcperflogger.model.ConnectionInfo;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

import static java.awt.event.InputEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_DELETE;

/**
 * @author vasiliy
 */
public class LoggerPanel extends JPanel {

    private static final Map<String, Integer> COLUMNS_WIDTH;

    static {
        COLUMNS_WIDTH = new HashMap<>();
        COLUMNS_WIDTH.put(LogConstants.ID_COLUMN, 0);
        COLUMNS_WIDTH.put(LogConstants.TSTAMP_COLUMN, 150);
        COLUMNS_WIDTH.put(LogConstants.FETCH_TIME_COLUMN, 50);
        COLUMNS_WIDTH.put(LogConstants.EXEC_TIME_COLUMN, 50);
        COLUMNS_WIDTH.put(LogConstants.EXEC_PLUS_RSET_USAGE_TIME, 50);
        COLUMNS_WIDTH.put(LogConstants.STMT_TYPE_COLUMN, 40);
        COLUMNS_WIDTH.put(LogConstants.RAW_SQL_COLUMN, 350);
        COLUMNS_WIDTH.put(LogConstants.FILLED_SQL_COLUMN, 200);
        COLUMNS_WIDTH.put(LogConstants.NB_ROWS_COLUMN, 60);
        COLUMNS_WIDTH.put(LogConstants.THREAD_NAME_COLUMN, 200);
        COLUMNS_WIDTH.put(LogConstants.EXEC_COUNT_COLUMN, 100);
        COLUMNS_WIDTH.put(LogConstants.TOTAL_EXEC_PLUS_RSET_USAGE_TIME_COLUMN, 100);
        COLUMNS_WIDTH.put(LogConstants.TIMEOUT_COLUMN, 70);
        COLUMNS_WIDTH.put(LogConstants.AUTOCOMMIT_COLUMN, 40);
        COLUMNS_WIDTH.put(LogConstants.ERROR_COLUMN, 0);
    }

    CustomTable table;
    private ResultSetDataModel dataModel;

    RSyntaxTextArea txtFieldRawSql;
    RSyntaxTextArea txtFieldFilledSql;
    JLabel lblStatus;
    private StatementTimestampTableCellRenderer stmtTimestampCellRenderer;
    JTextField connectionUrlField;
    JTextField connectionCreationDateField;
    JTextField connectionPropertiesField;
    JTextField connectionCreationDurationField;
    JLabel lblConnectionStatus;

    private JComboBox<Filter.FilterType> comboBoxFilterType;
    private JTextField txtFldSqlFilter;

    public LoggerPanel() {
        super(new BorderLayout());

        ConnectionInfo info = new ConnectionInfo(null,
                0, null, null, 0, null);

        try {
            final PerfLoggerController serverPerfLoggerController = createServer(4561);
            init(serverPerfLoggerController);
            serverPerfLoggerController.startReciever();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static URL[] loadURLs(String paths) throws MalformedURLException {
        String token = ";";
        Vector<String> pathsVector = new Vector<String>();

        if (paths.indexOf(token) != -1) {
            StringTokenizer st = new StringTokenizer(paths, token);
            while (st.hasMoreTokens()) {
                pathsVector.add(st.nextToken());
            }
        }
        else {
            pathsVector.add(paths);
        }

        URL[] urls = new URL[pathsVector.size()];
        for (int i = 0; i < urls.length; i++) {
            File f = new File((String)pathsVector.elementAt(i));
            urls[i] = f.toURI().toURL();
        }
        return urls;
    }

    private PerfLoggerController createServer(final int listeningPort) {

        final AbstractLogReceiver logReceiver = new ServerLogReceiver(listeningPort);
        return new PerfLoggerController(logReceiver, this);
    }

    private void init(final PerfLoggerController perfLoggerController) throws Exception {

        dataModel = new ResultSetDataModel();
        final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{36, 0};
        gridBagLayout.rowHeights = new int[]{30, 316, 29, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final JPanel topPanel = new JPanel();
        final GridBagConstraints gbc_topPanel = new GridBagConstraints();
        gbc_topPanel.fill = GridBagConstraints.BOTH;
        gbc_topPanel.insets = new Insets(0, 0, 5, 0);
        gbc_topPanel.gridx = 0;
        gbc_topPanel.gridy = 0;
        add(topPanel, gbc_topPanel);
        final GridBagLayout gbl_topPanel = new GridBagLayout();
        gbl_topPanel.columnWidths = new int[]{51, 0, 0, 0, 0};
        gbl_topPanel.rowHeights = new int[]{0, 0};
        gbl_topPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_topPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        topPanel.setLayout(gbl_topPanel);

        final JPanel filterPanel = new JPanel();
        filterPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Filter",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        final GridBagConstraints gbc_filterPanel = new GridBagConstraints();
        gbc_filterPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_filterPanel.insets = new Insets(0, 0, 0, 5);
        gbc_filterPanel.gridx = 0;
        gbc_filterPanel.gridy = 0;
        topPanel.add(filterPanel, gbc_filterPanel);
        final GridBagLayout gbl_filterPanel = new GridBagLayout();
        gbl_filterPanel.columnWidths = new int[]{0, 51, 246, 0};
        gbl_filterPanel.rowHeights = new int[]{30, 0, 0};
        gbl_filterPanel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_filterPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        filterPanel.setLayout(gbl_filterPanel);

        comboBoxFilterType = new JComboBox<>();
        comboBoxFilterType
                .setModel(new DefaultComboBoxModel<>(EnumSet.allOf(Filter.FilterType.class).toArray(new Filter.FilterType[0])));
        comboBoxFilterType.setSelectedItem(Filter.FilterType.HIGHLIGHT);
        comboBoxFilterType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Filter.FilterType filterType = comboBoxFilterType.getItemAt(comboBoxFilterType.getSelectedIndex());
                perfLoggerController.setFilterType(filterType != null ? filterType : Filter.FilterType.HIGHLIGHT);
            }
        });
        final GridBagConstraints gbc_filterTypeComboBox = new GridBagConstraints();
        gbc_filterTypeComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_filterTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_filterTypeComboBox.gridx = 0;
        gbc_filterTypeComboBox.gridy = 0;
        filterPanel.add(comboBoxFilterType, gbc_filterTypeComboBox);

        final JLabel lblText = new JLabel("Text:");
        final GridBagConstraints gbc_lblText = new GridBagConstraints();
        gbc_lblText.anchor = GridBagConstraints.BASELINE_TRAILING;
        gbc_lblText.insets = new Insets(0, 0, 5, 5);
        gbc_lblText.gridx = 1;
        gbc_lblText.gridy = 0;
        filterPanel.add(lblText, gbc_lblText);

        txtFldSqlFilter = new JTextField();
        final GridBagConstraints gbc_txtFldSqlFilter = new GridBagConstraints();
        gbc_txtFldSqlFilter.anchor = GridBagConstraints.BASELINE;
        gbc_txtFldSqlFilter.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtFldSqlFilter.insets = new Insets(0, 0, 5, 0);
        gbc_txtFldSqlFilter.gridx = 2;
        gbc_txtFldSqlFilter.gridy = 0;
        filterPanel.add(txtFldSqlFilter, gbc_txtFldSqlFilter);
        txtFldSqlFilter.setColumns(10);

        txtFldSqlFilter.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(final UndoableEditEvent e) {
                perfLoggerController.setTextFilter(txtFldSqlFilter.getText());
            }
        });

        final JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.8);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        final JScrollPane logListPanel = new JScrollPane();
        logListPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        table = new CustomTable(dataModel);
        table.setDefaultRenderer(Byte.class, new CustomTableCellRenderer());
        table.setDefaultRenderer(String.class, new CustomTableCellRenderer());
        stmtTimestampCellRenderer = new StatementTimestampTableCellRenderer();
        table.setDefaultRenderer(Timestamp.class, stmtTimestampCellRenderer);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setRowSorter(new CustomTableRowSorter(dataModel));
        logListPanel.setViewportView(table);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                assert e != null;
                if (!e.getValueIsAdjusting()) {
                    int i = 0;
                    perfLoggerController.onSelectStatement(getSelectedLogId());
                }
            }
        });

        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(final KeyEvent e) {
                assert e != null;
                if (e.getKeyCode() == VK_BACK_SPACE || e.getKeyCode() == VK_DELETE) {
                    if (e.getModifiers() == CTRL_MASK) {
                        perfLoggerController.onClear();
                    } else {
                        final int[] selectedRowsTableIndexes = table.getSelectedRows();
                        final UUID[] logIds = new UUID[selectedRowsTableIndexes.length];
                        for (int i = 0; i < selectedRowsTableIndexes.length; i++) {
                            logIds[i] = dataModel.getIdAtRow(table.convertRowIndexToModel(selectedRowsTableIndexes[i]));
                        }
                        perfLoggerController.onDeleteSelectedStatements(logIds);
                    }
                }

            }

        });

        final JPopupMenu popupMenu = new JPopupMenu();
        final JMenuItem deleteItem = new JMenuItem("Append to advanced filter");
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {

            }
        });
        popupMenu.add(deleteItem);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                assert e != null;
                handlePotentialRightClick(e);
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                assert e != null;
                handlePotentialRightClick(e);
            }

            private void handlePotentialRightClick(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    final JTable source = (JTable) e.getSource();
                    final int row = source.rowAtPoint(e.getPoint());
                    final int column = source.columnAtPoint(e.getPoint());
                    if (row >= 0) {
                        if (!source.isRowSelected(row) || !source.isColumnSelected(column)) {
                            source.changeSelection(row, column, false, false);
                        }

                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        splitPane.setTopComponent(logListPanel);

        final JPanel sqlDetailPanel = new JPanel();
        sqlDetailPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "SQL detail",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
        splitPane.setBottomComponent(sqlDetailPanel);
        final GridBagLayout gbl_sqlDetailPanel = new GridBagLayout();
        gbl_sqlDetailPanel.columnWidths = new int[]{842, 0};
        gbl_sqlDetailPanel.rowHeights = new int[]{112, 0};
        gbl_sqlDetailPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_sqlDetailPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        sqlDetailPanel.setLayout(gbl_sqlDetailPanel);

        final JTabbedPane tabbedPanelsqlDetails = new JTabbedPane();
        tabbedPanelsqlDetails.setBorder(null);
        final GridBagConstraints gbc_tabbedPanelsqlDetails = new GridBagConstraints();
        gbc_tabbedPanelsqlDetails.fill = GridBagConstraints.BOTH;
        gbc_tabbedPanelsqlDetails.gridx = 0;
        gbc_tabbedPanelsqlDetails.gridy = 0;
        sqlDetailPanel.add(tabbedPanelsqlDetails, gbc_tabbedPanelsqlDetails);

        final JPanel panelRawSql = new JPanel();
        tabbedPanelsqlDetails.addTab("Raw SQL", panelRawSql);
        final GridBagLayout gbl_panelRawSql = new GridBagLayout();
        gbl_panelRawSql.columnWidths = new int[]{0, 0, 0};
        gbl_panelRawSql.rowHeights = new int[]{0, 0};
        gbl_panelRawSql.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_panelRawSql.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        panelRawSql.setLayout(gbl_panelRawSql);

        final JScrollPane scrollPaneRawSql = new JScrollPane();
        final GridBagConstraints gbc_scrollPaneRawSql = new GridBagConstraints();
        gbc_scrollPaneRawSql.fill = GridBagConstraints.BOTH;
        gbc_scrollPaneRawSql.gridx = 1;
        gbc_scrollPaneRawSql.gridy = 0;
        panelRawSql.add(scrollPaneRawSql, gbc_scrollPaneRawSql);

        txtFieldRawSql = new RSyntaxTextArea();
        scrollPaneRawSql.setViewportView(txtFieldRawSql);
        applySqlSyntaxColoring(txtFieldRawSql);
        txtFieldRawSql.setOpaque(false);
        txtFieldRawSql.setEditable(false);
        txtFieldRawSql.setLineWrap(true);

        final JPanel panelFilledSql = new JPanel();
        tabbedPanelsqlDetails.addTab("FilledSQL", panelFilledSql);
        final GridBagLayout gbl_panelFilledSql = new GridBagLayout();
        gbl_panelFilledSql.columnWidths = new int[]{0, 0, 0};
        gbl_panelFilledSql.rowHeights = new int[]{0, 0};
        gbl_panelFilledSql.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_panelFilledSql.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        panelFilledSql.setLayout(gbl_panelFilledSql);

        final JScrollPane scrollPaneFilledSql = new JScrollPane();
        final GridBagConstraints gbc_scrollPaneFilledSql = new GridBagConstraints();
        gbc_scrollPaneFilledSql.fill = GridBagConstraints.BOTH;
        gbc_scrollPaneFilledSql.gridx = 1;
        gbc_scrollPaneFilledSql.gridy = 0;
        panelFilledSql.add(scrollPaneFilledSql, gbc_scrollPaneFilledSql);

        txtFieldFilledSql = new RSyntaxTextArea();
        scrollPaneFilledSql.setViewportView(txtFieldFilledSql);
        applySqlSyntaxColoring(txtFieldFilledSql);
        txtFieldFilledSql.setOpaque(false);
        txtFieldFilledSql.setEditable(false);
        txtFieldFilledSql.setLineWrap(true);

        final JPanel panelConnectionInfo = new JPanel();
        tabbedPanelsqlDetails.addTab("Connection", null, panelConnectionInfo, null);
        panelConnectionInfo.setBorder(new TitledBorder(
                new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Connection info",
                        TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)),
                "Connection info", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
        final GridBagLayout gbl_panelConnectionInfo = new GridBagLayout();
        gbl_panelConnectionInfo.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_panelConnectionInfo.rowHeights = new int[]{0, 0, 0, 0};
        gbl_panelConnectionInfo.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_panelConnectionInfo.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        panelConnectionInfo.setLayout(gbl_panelConnectionInfo);

        final JLabel lblConnectionUrl = new JLabel("URL:");
        final GridBagConstraints gbc_lblConnectionUrl = new GridBagConstraints();
        gbc_lblConnectionUrl.anchor = GridBagConstraints.EAST;
        gbc_lblConnectionUrl.insets = new Insets(0, 0, 5, 5);
        gbc_lblConnectionUrl.gridx = 0;
        gbc_lblConnectionUrl.gridy = 0;
        panelConnectionInfo.add(lblConnectionUrl, gbc_lblConnectionUrl);

        connectionUrlField = new JTextField();
        final GridBagConstraints gbc_connectionUrlField = new GridBagConstraints();
        gbc_connectionUrlField.gridwidth = 3;
        gbc_connectionUrlField.fill = GridBagConstraints.HORIZONTAL;
        gbc_connectionUrlField.insets = new Insets(0, 0, 5, 0);
        gbc_connectionUrlField.gridx = 1;
        gbc_connectionUrlField.gridy = 0;
        panelConnectionInfo.add(connectionUrlField, gbc_connectionUrlField);
        connectionUrlField.setColumns(20);

        final JLabel lblCreated = new JLabel("Created:");
        final GridBagConstraints gbc_lblCreated = new GridBagConstraints();
        gbc_lblCreated.anchor = GridBagConstraints.EAST;
        gbc_lblCreated.insets = new Insets(0, 0, 5, 5);
        gbc_lblCreated.gridx = 0;
        gbc_lblCreated.gridy = 1;
        panelConnectionInfo.add(lblCreated, gbc_lblCreated);

        connectionCreationDateField = new JTextField();
        final GridBagConstraints gbc_connectionCreationDateField = new GridBagConstraints();
        gbc_connectionCreationDateField.fill = GridBagConstraints.HORIZONTAL;
        gbc_connectionCreationDateField.insets = new Insets(0, 0, 5, 5);
        gbc_connectionCreationDateField.gridx = 1;
        gbc_connectionCreationDateField.gridy = 1;
        panelConnectionInfo.add(connectionCreationDateField, gbc_connectionCreationDateField);
        connectionCreationDateField.setColumns(15);

        final JLabel lblCreationDuration = new JLabel("Creation duration (ms):");
        final GridBagConstraints gbc_lblCreationDuration = new GridBagConstraints();
        gbc_lblCreationDuration.anchor = GridBagConstraints.EAST;
        gbc_lblCreationDuration.insets = new Insets(0, 0, 5, 5);
        gbc_lblCreationDuration.gridx = 2;
        gbc_lblCreationDuration.gridy = 1;
        panelConnectionInfo.add(lblCreationDuration, gbc_lblCreationDuration);

        connectionCreationDurationField = new JTextField();
        final GridBagConstraints gbc_creationDurationField = new GridBagConstraints();
        gbc_creationDurationField.insets = new Insets(0, 0, 5, 0);
        gbc_creationDurationField.fill = GridBagConstraints.HORIZONTAL;
        gbc_creationDurationField.gridx = 3;
        gbc_creationDurationField.gridy = 1;
        panelConnectionInfo.add(connectionCreationDurationField, gbc_creationDurationField);
        connectionCreationDurationField.setColumns(10);

        final JLabel lblConectionProperties = new JLabel("Properties:");
        final GridBagConstraints gbc_lblConectionProperties = new GridBagConstraints();
        gbc_lblConectionProperties.anchor = GridBagConstraints.EAST;
        gbc_lblConectionProperties.insets = new Insets(0, 0, 0, 5);
        gbc_lblConectionProperties.gridx = 0;
        gbc_lblConectionProperties.gridy = 2;
        panelConnectionInfo.add(lblConectionProperties, gbc_lblConectionProperties);
        lblConectionProperties.setToolTipText("(Password property removed)");

        connectionPropertiesField = new JTextField();
        final GridBagConstraints gbc_connectionPropertiesField = new GridBagConstraints();
        gbc_connectionPropertiesField.fill = GridBagConstraints.HORIZONTAL;
        gbc_connectionPropertiesField.gridwidth = 3;
        gbc_connectionPropertiesField.gridx = 1;
        gbc_connectionPropertiesField.gridy = 2;
        panelConnectionInfo.add(connectionPropertiesField, gbc_connectionPropertiesField);
        connectionPropertiesField.setColumns(10);

        final GridBagConstraints gbc_splitPane = new GridBagConstraints();
        gbc_splitPane.fill = GridBagConstraints.BOTH;
        gbc_splitPane.insets = new Insets(0, 0, 5, 0);
        gbc_splitPane.gridx = 0;
        gbc_splitPane.gridy = 1;
        add(splitPane, gbc_splitPane);

        final JPanel bottomPanel = new JPanel();
        final GridBagConstraints gbc_bottomPanel = new GridBagConstraints();
        gbc_bottomPanel.anchor = GridBagConstraints.NORTH;
        gbc_bottomPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_bottomPanel.gridx = 0;
        gbc_bottomPanel.gridy = 2;
        add(bottomPanel, gbc_bottomPanel);
        final GridBagLayout gbl_bottomPanel = new GridBagLayout();
        gbl_bottomPanel.columnWidths = new int[]{0, 507, 125, 125, 79, 0};
        gbl_bottomPanel.rowHeights = new int[]{29, 0};
        gbl_bottomPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_bottomPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        bottomPanel.setLayout(gbl_bottomPanel);

        lblConnectionStatus = new JLabel("");
        final GridBagConstraints gbc_lblConnectionStatus = new GridBagConstraints();
        gbc_lblConnectionStatus.insets = new Insets(0, 0, 0, 5);
        gbc_lblConnectionStatus.gridx = 0;
        gbc_lblConnectionStatus.gridy = 0;
        bottomPanel.add(lblConnectionStatus, gbc_lblConnectionStatus);

        lblStatus = new JLabel(" ");
        final GridBagConstraints gbc_lblStatus = new GridBagConstraints();
        gbc_lblStatus.anchor = GridBagConstraints.BASELINE;
        gbc_lblStatus.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblStatus.insets = new Insets(0, 0, 0, 5);
        gbc_lblStatus.gridx = 1;
        gbc_lblStatus.gridy = 0;
        bottomPanel.add(lblStatus, gbc_lblStatus);
        final GridBagConstraints gbc_btnExportSql = new GridBagConstraints();
        gbc_btnExportSql.anchor = GridBagConstraints.BASELINE_LEADING;
        gbc_btnExportSql.insets = new Insets(0, 0, 0, 5);
        gbc_btnExportSql.gridx = 2;
        gbc_btnExportSql.gridy = 0;
    }

    private void applySqlSyntaxColoring(final RSyntaxTextArea txtArea) {
        txtArea.setCurrentLineHighlightColor(Color.WHITE);
        txtArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        final SyntaxScheme scheme = txtArea.getSyntaxScheme();
        scheme.getStyle(TokenTypes.LITERAL_CHAR).background = Color.CYAN;
        scheme.getStyle(TokenTypes.LITERAL_NUMBER_DECIMAL_INT).background = Color.YELLOW;
        // scheme.getStyle(TokenTypes.LITERAL_NUMBER_FLOAT).background = Color.YELLOW;
    }

    void setData(final java.util.List<Object[]> rows, final java.util.List<String> columnNames, final List<Class<?>> columnTypes,
                 final boolean tableStructureChanged) {
        final int selectedRow = table.getSelectedRow();
        int modelRowIndex = -1;
        if (selectedRow >= 0) {
            modelRowIndex = table.convertRowIndexToModel(selectedRow);
        }

        dataModel.setNewData(rows, columnNames, columnTypes);
        if (tableStructureChanged) {
            for (int i = 0; i < dataModel.getColumnCount(); i++) {
                final Integer width = COLUMNS_WIDTH.get(dataModel.getColumnName(i));
                if (width != null) {
                    if (width == 0) {
                        table.getColumnModel().getColumn(i).setMinWidth(0);
                        table.getColumnModel().getColumn(i).setMaxWidth(0);
                    } else {
                        table.getColumnModel().getColumn(i).setPreferredWidth(width.intValue());
                    }
                }
            }
        } else if (selectedRow >= 0 && selectedRow < rows.size() && modelRowIndex < rows.size()) {
            final int newSelectedRowIndex = table.convertRowIndexToView(modelRowIndex);
            table.setRowSelectionInterval(newSelectedRowIndex, newSelectedRowIndex);
        }

    }

    UUID getSelectedLogId() {
        final ListSelectionModel lsm = table.getSelectionModel();
        if (lsm.getMinSelectionIndex() >= 0) {
            return dataModel.getIdAtRow(table.convertRowIndexToModel(lsm.getMinSelectionIndex()));
        }
        return null;
    }

    public void setTxtToHighlight(final String txtToHighlight) {
        final SearchContext searchContext = new SearchContext(txtToHighlight);
        searchContext.setMarkAll(true);
        SearchEngine.markAll(txtFieldRawSql, searchContext);
        SearchEngine.markAll(txtFieldFilledSql, searchContext);
    }
}
