package org.executequery.gui.importData;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FilenameUtils;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabViewActionPanel;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.NamedView;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.editor.ResultSetTablePopupMenu;
import org.executequery.gui.exportData.ExportDataPanel;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

/**
 * @author Alexey Kozlov
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ImportDataPanel extends DefaultTabViewActionPanel
        implements NamedView {

    public static final String TITLE = Bundles.get(ImportDataPanel.class, "Title");
    public static final String FRAME_ICON = "ImportDelimited16.png";

    private static final int PREVIEW_ROWS_COUNT = 50;
    private static final int MIN_COLUMN_WIDTH = 100;
    private static final String NOTHING_HEADER = "NOTHING";

    // --- GUI components ---

    private JPanel importFilePanel;
    private JPanel importConnectionPanel;
    private JPanel csvPropsPanel;
    private JPanel xlsxPropsPanel;

    private JComboBox targetConnectionsCombo;
    private JComboBox targetTableCombo;
    private JComboBox sourceConnectionsCombo;
    private JComboBox sourceTableCombo;
    private JComboBox delimiterCombo;

    private JTextField fileNameField;
    private JTextField lobFileField;

    private JCheckBox firstRowIsNamesCheck;
    private JCheckBox eraseTableCheck;
    private JCheckBox importFromConnectionCheck;

    private JSpinner sheetNumberSpinner;
    private JSpinner firstImportedRowSelector;
    private JSpinner lastImportedRowSelector;
    private JSpinner commitStepSelector;

    private DefaultTableModel filePreviewTableModel;
    private JTable filePreviewTable;

    private ResultSetTableModel connectionPreviewTableModel;
    private ResultSetTable connectionPreviewTable;

    private DefaultTableModel columnMappingTableModel;
    private JTable columnMappingTable;

    private JButton browseDataFileButton;
    private JButton browseLobFileButton;
    private JButton correlateButton;
    private JButton startImportButton;

    private JLabel progressLabel;

    // ---

    private DefaultDatabaseHost targetHost;
    private ImportHelper importHelper;
    private List<String> sourceHeaders;
    private List<String> targetTablesList;
    private List<String> sourceTablesList;

    private boolean isCancel;
    private String pathToFile;
    private String pathToLob;
    private String fileName;
    private String fileType;

    public ImportDataPanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {

        targetTablesList = new LinkedList<>();
        sourceTablesList = new LinkedList<>();

        // --- comboBoxes ---

        String[] delimiters = {";", "|", ",", "#"};
        List<DatabaseConnection> connections = ((DatabaseConnectionRepository) Objects.requireNonNull(RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID))).findAll();

        delimiterCombo = WidgetFactory.createComboBox("delimiterCombo", delimiters);
        delimiterCombo.setEditable(true);
        setDelimiterComboSelectedValue("columnDelimiterCombo", delimiterCombo);
        delimiterCombo.addActionListener(e -> previewSourceFile(false));

        targetTableCombo = WidgetFactory.createComboBox("targetTableCombo");
        targetTableCombo.addActionListener(e -> updateMappingTable());
        targetTableCombo.setEditable(true);
        targetTableCombo.setEnabled(false);
        targetTableCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == '\n')
                    setSelectedComboItem(targetTableCombo, targetTablesList);
            }
        });

        targetConnectionsCombo = WidgetFactory.createComboBox("targetConnectionsCombo");
        targetConnectionsCombo.addActionListener(e -> targetConnectionChanged());
        targetConnectionsCombo.addItem(bundleString("SelectDB"));
        connections.forEach(dc -> targetConnectionsCombo.addItem(dc));

        sourceTableCombo = WidgetFactory.createComboBox("sourceTableCombo");
        sourceTableCombo.addActionListener(e -> previewSourceTable());
        sourceTableCombo.setEditable(true);
        sourceTableCombo.setEnabled(false);
        sourceTableCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == '\n')
                    setSelectedComboItem(sourceTableCombo, sourceTablesList);
            }
        });

        sourceConnectionsCombo = WidgetFactory.createComboBox("sourceConnectionsCombo");
        sourceConnectionsCombo.addActionListener(e -> sourceConnectionChanged());
        sourceConnectionsCombo.addItem(bundleString("SelectDB"));
        connections.forEach(dc -> sourceConnectionsCombo.addItem(dc));

        targetTableCombo.setPreferredSize(targetConnectionsCombo.getPreferredSize());
        sourceTableCombo.setPreferredSize(sourceConnectionsCombo.getPreferredSize());

        // --- checkBoxes ---

        firstRowIsNamesCheck = WidgetFactory.createCheckBox("firstRowIsNamesCheck", bundleString("IsFirstColumnNamesText"));
        firstRowIsNamesCheck.addActionListener(e -> previewSourceFile(false));
        firstRowIsNamesCheck.setVisible(false);

        importFromConnectionCheck = WidgetFactory.createCheckBox("importFromConnectionCheck", bundleString("importFromConnectionCheck"));
        importFromConnectionCheck.addActionListener(e -> updateSourcePropertiesFields());

        eraseTableCheck = WidgetFactory.createCheckBox("eraseTableCheck", bundleString("IsEraseDatabaseText"));

        // --- numeric selectors ---

        firstImportedRowSelector = WidgetFactory.createSpinner("firstImportedRowSelector", 0, 0, Integer.MAX_VALUE, 1);
        lastImportedRowSelector = WidgetFactory.createSpinner("lastImportedRowSelector", 999999999, 0, Integer.MAX_VALUE, 1);
        commitStepSelector = WidgetFactory.createSpinner("commitStepSelector", 100, 100, 1000000, 100);

        sheetNumberSpinner = WidgetFactory.createSpinner("sheetNumberSpinner", 1, 1, 1, 1);
        sheetNumberSpinner.addChangeListener(e -> previewSourceFile(false));

        // --- file preview table ---

        filePreviewTableModel = new DefaultTableModel();
        filePreviewTable = WidgetFactory.createTable("filePreviewTable", filePreviewTableModel);

        // --- connection preview table ---

        try {
            connectionPreviewTableModel = new ResultSetTableModel(PREVIEW_ROWS_COUNT, true);

            connectionPreviewTable = new ResultSetTable();
            connectionPreviewTable.setModel(new TableSorter(connectionPreviewTableModel));
            connectionPreviewTable.addMouseListener(new ResultSetTablePopupMenu(connectionPreviewTable, null));
        } catch (SQLException ignored) {
        }

        // --- column mapping table ---

        columnMappingTableModel = new DefaultTableModel();
        columnMappingTableModel.addColumn(bundleString("ColumnMappingTableTargetC"));
        columnMappingTableModel.addColumn(bundleString("ColumnMappingTableTargetT"));
        columnMappingTableModel.addColumn(bundleString("ColumnMappingTableSource"));
        columnMappingTableModel.addColumn(bundleString("ColumnMappingTableProps"));

        columnMappingTable = WidgetFactory.createTable("columnMappingTable", columnMappingTableModel);
        columnMappingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        columnMappingTable.getColumn(bundleString("ColumnMappingTableProps")).setCellRenderer(new MappingCellRenderer());
        columnMappingTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int selectedRow = columnMappingTable.getSelectedRow();
                int selectedColumn = columnMappingTable.getSelectedColumn();

                if (selectedColumn == 3) {
                    if (isBlobType(columnMappingTable.getValueAt(selectedRow, 1).toString())) {
                        String oldValue = columnMappingTable.getValueAt(selectedRow, selectedColumn).toString();
                        columnMappingTable.setValueAt(!oldValue.equals("true"), selectedRow, selectedColumn);
                    }
                }

                columnMappingTable.repaint();
            }
        });

        // --- buttons ---

        browseDataFileButton = WidgetFactory.createButton("browseDataFileButton", bundleString("BrowseButtonText"));
        browseDataFileButton.addActionListener(e -> browseFile(fileNameField));

        browseLobFileButton = WidgetFactory.createButton("browseLobFileButton", bundleString("BrowseButtonText"));
        browseLobFileButton.addActionListener(e -> browseFile(lobFileField));

        correlateButton = WidgetFactory.createButton("correlateButton", bundleString("CorrelateButtonText"));
        correlateButton.addActionListener(e -> correlateFields());
        correlateButton.setEnabled(false);

        startImportButton = WidgetFactory.createButton("startImportButton", bundleString("StartImportButtonText"));
        startImportButton.addActionListener(e -> {

            if (startImportButton.getText().equals(Bundles.getCommon("cancel.button"))) {
                startImportButton.setEnabled(false);
                isCancel = true;
                return;
            }

            importData();
        });

        // --- panels ---

        importFilePanel = new JPanel(new GridBagLayout());

        importConnectionPanel = new JPanel(new GridBagLayout());
        importConnectionPanel.setVisible(false);

        csvPropsPanel = new JPanel(new GridBagLayout());
        csvPropsPanel.setVisible(false);

        xlsxPropsPanel = new JPanel(new GridBagLayout());
        xlsxPropsPanel.setVisible(false);

        // --- fields ---

        fileNameField = WidgetFactory.createTextField("fileNameField");
        fileNameField.setPreferredSize(browseDataFileButton.getPreferredSize());
        fileNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER)
                    previewSourceFile(true);
            }
        });

        lobFileField = WidgetFactory.createTextField("lobFileField");
        lobFileField.setPreferredSize(browseLobFileButton.getPreferredSize());

        // ---

        progressLabel = new JLabel();
        progressLabel.setVisible(false);

        // ---

        arrange();
    }

    private void arrange() {

        GridBagHelper gridBagHelper;

        // --- scroll panes ---

        JScrollPane filePreviewScrollPane = new JScrollPane(filePreviewTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JScrollPane connectionPreviewScrollPane = new JScrollPane(connectionPreviewTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JScrollPane mappingTableScrollPane = new JScrollPane(columnMappingTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // --- csv properties panel ---

        gridBagHelper = new GridBagHelper().fillNone().anchorNorthWest();
        csvPropsPanel.add(new JLabel(bundleString("DelimiterLabel")), gridBagHelper.get());
        csvPropsPanel.add(delimiterCombo, gridBagHelper.nextCol().setInsets(5, 0, 0, 0).get());

        // --- xlsx properties panel ---

        gridBagHelper = new GridBagHelper().fillNone().anchorNorthWest();
        xlsxPropsPanel.add(new JLabel(bundleString("SheetNumberLabel")), gridBagHelper.get());
        xlsxPropsPanel.add(sheetNumberSpinner, gridBagHelper.nextCol().setInsets(5, 0, 0, 0).get());

        // --- select file panel ---

        JPanel selectFilePanel = new JPanel(new GridBagLayout());

        gridBagHelper = new GridBagHelper().setInsets(0, 0, 5, 5).anchorNorthWest();
        selectFilePanel.add(new JLabel(bundleString("fileNameFieldLabel")), gridBagHelper.topGap(3).setMinWeightX().get());
        selectFilePanel.add(fileNameField, gridBagHelper.topGap(0).nextCol().fillHorizontally().setMaxWeightX().get());
        selectFilePanel.add(browseDataFileButton, gridBagHelper.nextCol().rightGap(0).bottomGap(0).setMinWeightX().get());
        selectFilePanel.add(new JLabel(bundleString("lobFileFieldLabel")), gridBagHelper.nextRowFirstCol().setHeight(1).topGap(3).rightGap(5).setMinWeightX().get());
        selectFilePanel.add(lobFileField, gridBagHelper.nextCol().topGap(0).bottomGap(0).fillHorizontally().setMaxWeightX().get());
        selectFilePanel.add(browseLobFileButton, gridBagHelper.nextCol().rightGap(0).setMinWeightX().get());

        // --- file preview panel ---

        gridBagHelper = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest();
        importFilePanel.add(selectFilePanel, gridBagHelper.fillHorizontally().spanX().get());
        importFilePanel.add(filePreviewScrollPane, gridBagHelper.nextRowFirstCol().fillBoth().setMaxWeightY().spanX().get());
        importFilePanel.add(firstRowIsNamesCheck, gridBagHelper.nextRowFirstCol().leftGap(0).setMaxWeightX().setMinWeightY().setWidth(1).get());
        importFilePanel.add(csvPropsPanel, gridBagHelper.nextCol().leftGap(5).spanX().setMinWeightX().get());
        importFilePanel.add(xlsxPropsPanel, gridBagHelper.get());

        // --- connection preview panel ---

        gridBagHelper = new GridBagHelper().fillHorizontally().setInsets(5, 5, 5, 5).anchorNorthWest();
        importConnectionPanel.add(new JLabel(bundleString("sourceConnectionLabel")), gridBagHelper.topGap(8).setMinWeightX().get());
        importConnectionPanel.add(sourceConnectionsCombo, gridBagHelper.nextCol().setMaxWeightX().topGap(5).get());
        importConnectionPanel.add(new JLabel(bundleString("sourceTableLabel")), gridBagHelper.setMinWeightX().topGap(8).nextCol().get());
        importConnectionPanel.add(sourceTableCombo, gridBagHelper.nextCol().topGap(5).setMaxWeightX().get());
        importConnectionPanel.add(connectionPreviewScrollPane, gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().get());

        // --- start import panel ---

        JPanel startImportPanel = new JPanel(new GridBagLayout());

        gridBagHelper = new GridBagHelper().fillBoth().anchorNorthWest();
        startImportPanel.add(eraseTableCheck, gridBagHelper.setMaxWeightX().get());
        startImportPanel.add(correlateButton, gridBagHelper.nextCol().rightGap(5).anchorNorthEast().setMinWeightX().get());
        startImportPanel.add(startImportButton, gridBagHelper.nextCol().rightGap(0).get());

        // --- mapping panel ---

        JPanel mappingPanel = new JPanel(new GridBagLayout());
        mappingPanel.setBorder(BorderFactory.createTitledBorder(bundleString("MappingTableLabel")));

        gridBagHelper = new GridBagHelper().fillHorizontally().setInsets(5, 5, 5, 5).anchorNorthWest();
        mappingPanel.add(new JLabel(bundleString("TargetConnectionLabel")), gridBagHelper.topGap(8).setMinWeightX().get());
        mappingPanel.add(targetConnectionsCombo, gridBagHelper.nextCol().topGap(5).setMaxWeightX().get());
        mappingPanel.add(new JLabel(bundleString("TargetTableLabel")), gridBagHelper.topGap(8).setMinWeightX().nextCol().get());
        mappingPanel.add(targetTableCombo, gridBagHelper.nextCol().topGap(5).setMaxWeightX().get());
        mappingPanel.add(mappingTableScrollPane, gridBagHelper.nextRowFirstCol().setMaxWeightY().fillBoth().spanX().get());
        mappingPanel.add(startImportPanel, gridBagHelper.leftGap(2).nextRowFirstCol().setMinWeightY().spanX().get());

        // --- preview panel ---

        JPanel previewPanel = new JPanel(new GridBagLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder(bundleString("PreviewTableLabel")));

        gridBagHelper = new GridBagHelper().fillHorizontally().setInsets(5, 5, 5, 0).anchorNorthWest();
        previewPanel.add(importFromConnectionCheck, gridBagHelper.spanX().get());
        previewPanel.add(importFilePanel, gridBagHelper.setInsets(5, 5, 0, 5).nextRowFirstCol().fillBoth().spanX().spanY().get());
        previewPanel.add(importConnectionPanel, gridBagHelper.get());

        // --- split pane ---

        FlatSplitPane splitPane = new FlatSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, mappingPanel);
        splitPane.setResizeWeight(0.5);

        // --- bottom panel ---

        JPanel bottomPanel = new JPanel(new GridBagLayout());

        gridBagHelper = new GridBagHelper().fillHorizontally().setInsets(3, 8, 0, 5).anchorNorthWest();
        bottomPanel.add(new JLabel(bundleString("FirstNumericSelectorsLabel")), gridBagHelper.setMinWeightX().get());
        bottomPanel.add(firstImportedRowSelector, gridBagHelper.nextCol().rightGap(10).setMaxWeightX().get());
        bottomPanel.add(new JLabel(bundleString("LastNumericSelectorsLabel")), gridBagHelper.nextCol().rightGap(0).setMinWeightX().get());
        bottomPanel.add(lastImportedRowSelector, gridBagHelper.nextCol().rightGap(10).setMaxWeightX().get());
        bottomPanel.add(new JLabel(bundleString("CommitSelectorLabel")), gridBagHelper.nextCol().rightGap(0).setMinWeightX().get());
        bottomPanel.add(commitStepSelector, gridBagHelper.nextCol().rightGap(10).setMaxWeightX().get());
        bottomPanel.add(progressLabel, gridBagHelper.nextCol().rightGap(3).spanX().setMinWeightX().get());

        // --- panels settings ---

        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    // --- buttons handlers ---

    private void correlateFields() {

        if (targetNotSelected(true) || importHelper == null)
            return;

        if (importHelper instanceof ImportHelperCSV || importHelper instanceof ImportHelperXLSX) {
            if (!firstRowIsNamesCheck.isSelected()) {

                for (int i = 0; i < columnMappingTable.getRowCount() && i < importHelper.getHeaders().size(); i++)
                    columnMappingTable.setValueAt(importHelper.getHeaders().get(i), i, 2);
                return;
            }
        }

        for (int i = 0; i < columnMappingTable.getRowCount(); i++) {
            for (int j = 0; j < importHelper.getHeaders().size(); j++) {

                String value = importHelper.getHeaders().get(j);
                if (columnMappingTable.getValueAt(i, 0).toString().equals(value)) {
                    columnMappingTable.setValueAt(value, i, 2);
                    break;
                }
            }
        }
    }

    private void browseFile(JTextField field) {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogTitle(bundleString("OpenFileDialogText"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileFilter(field.equals(fileNameField) ?
                new FileNameExtensionFilter("Data Files", "csv", "xml", "xlsx") :
                new FileNameExtensionFilter("Lob Files", "lob")
        );

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), bundleString("OpenFileDialogButton"));
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        File file = fileChooser.getSelectedFile();
        if (!file.exists()) {
            GUIUtilities.displayWarningMessage(bundleString("FileDoesNotExistMessage") + "\n" + field.getText());
            return;
        }

        field.setText(file.getAbsolutePath());
        previewSourceFile(true);
    }

    private synchronized void previewSourceFile(boolean displayWarnings) {

        if (fileNameField.getText().isEmpty()) {
            if (displayWarnings)
                GUIUtilities.displayWarningMessage(bundleString("NoFileSelectedMessage"));
            return;
        }

        File sourceFile = new File(fileNameField.getText());
        String oldPathToFile = pathToFile;
        pathToFile = sourceFile.getAbsolutePath();
        fileName = FilenameUtils.getBaseName(sourceFile.getName());
        fileType = FileNameUtils.getExtension(sourceFile.getName()).toLowerCase();

        if (!fileType.equals("csv") && !fileType.equals("xlsx") && !fileType.equals("xml")) {
            if (displayWarnings)
                GUIUtilities.displayWarningMessage(bundleString("FileTypeNotSupported"));
            fileNameField.setText(oldPathToFile != null ? oldPathToFile : "");
            return;
        }

        importHelper = getImportHelper(fileType);
        if (importHelper == null)
            return;

        try {
            List<String> readData = importHelper.getPreviewData();

            filePreviewTableModel.setColumnCount(0);
            filePreviewTableModel.setRowCount(0);

            sourceHeaders = new LinkedList<>(importHelper.getHeaders());
            for (String header : sourceHeaders) {
                filePreviewTableModel.addColumn(header);
                filePreviewTable.getColumn(header).setMinWidth(MIN_COLUMN_WIDTH);
            }

            filePreviewTable.setAutoResizeMode(importHelper.getColumnsCount() < 7 ?
                    JTable.AUTO_RESIZE_ALL_COLUMNS :
                    JTable.AUTO_RESIZE_OFF
            );

            for (int i = 0; i < PREVIEW_ROWS_COUNT && i < readData.size(); i++)
                if (readData.get(i) != null)
                    filePreviewTableModel.addRow(readData.get(i).split(importHelper.getDelimiter()));

        } catch (FileNotFoundException e) {
            if (displayWarnings)
                GUIUtilities.displayWarningMessage(bundleString("FileDoesNotExistMessage") + "\n" + pathToFile);

        } catch (Exception e) {
            if (displayWarnings)
                GUIUtilities.displayWarningMessage(bundleString("FileReadingErrorMessage") + "\n" + e.getMessage());
        }

        firstRowIsNamesCheck.setVisible(fileType.equals("csv") || fileType.equals("xlsx"));
        csvPropsPanel.setVisible(fileType.equals("csv"));
        xlsxPropsPanel.setVisible(fileType.equals("xlsx"));

        updateMappingTable();
    }

    private synchronized void previewSourceTable() {

        fileType = "db";
        fileName = (String) sourceTableCombo.getSelectedItem();
        if (fileName == null || fileName.isEmpty())
            return;

        importHelper = getImportHelper(fileType);
        if (importHelper == null)
            return;

        try {

            ResultSet resultSet = ((ImportHelperDB) importHelper).getPreviewResultSet();
            connectionPreviewTableModel.createTable(resultSet);

            connectionPreviewTable.setAutoResizeMode(importHelper.getColumnsCount() < 7 ?
                    JTable.AUTO_RESIZE_ALL_COLUMNS :
                    JTable.AUTO_RESIZE_OFF
            );

        } catch (SQLException e) {
            GUIUtilities.displayWarningMessage(bundleString("FileReadingErrorMessage") + "\n" + e.getMessage());
        }

        updateMappingTable();
    }

    private synchronized void updateMappingTable() {

        columnMappingTableModel.setRowCount(0);
        correlateButton.setEnabled(false);

        if (targetNotSelected(false) || importHelper == null)
            return;

        JComboBox sourceComboBox = new JComboBox();
        sourceComboBox.addItem(NOTHING_HEADER);
        importHelper.getHeaders().forEach(sourceComboBox::addItem);

        TableColumn thirdColumn = columnMappingTable.getColumnModel().getColumn(2);
        thirdColumn.setCellEditor(new DefaultCellEditor(sourceComboBox));

        if (targetTableCombo.getSelectedItem() != null) {

            List<DatabaseColumn> columns = targetHost.getColumns(targetTableCombo.getSelectedItem().toString());
            if (columns != null) {
                for (DatabaseColumn column : columns) {

                    String fieldProperty = "";
                    if (isBlobType(column.getTypeName()))
                        fieldProperty = "true";

                    columnMappingTableModel.addRow(new Object[]{
                            column.getName(),
                            column.getTypeName(),
                            NOTHING_HEADER,
                            fieldProperty
                    });
                }
            }

            correlateButton.setEnabled(columns != null);
        }
    }

    // --- import data methods ---

    private void importData() {

        if (targetNotSelected(true))
            return;

        DefaultStatementExecutor executor = new DefaultStatementExecutor();
        executor.setCommitMode(false);
        executor.setKeepAlive(true);
        executor.setDatabaseConnection((DatabaseConnection) targetConnectionsCombo.getSelectedItem());

        // -- generate  mapped columns lists

        StringBuilder targetColumnList = new StringBuilder();
        StringBuilder sourceColumnList = new StringBuilder();

        int valuesCount = 0;
        Vector<Vector> dataFromTableVector = columnMappingTableModel.getDataVector();
        boolean[] valuesIndexes = new boolean[dataFromTableVector.size()];

        for (int i = 0; i < dataFromTableVector.size(); i++) {

            if (dataFromTableVector.get(i).get(2) != NOTHING_HEADER && dataFromTableVector.get(i).get(2) != null) {

                targetColumnList.append(MiscUtils.getFormattedObject(dataFromTableVector.get(i).get(0).toString(), executor.getDatabaseConnection()));
                sourceColumnList.append(dataFromTableVector.get(i).get(2).toString());

                targetColumnList.append(",");
                sourceColumnList.append(",");

                valuesCount++;
                valuesIndexes[i] = true;

            } else
                valuesIndexes[i] = false;
        }

        if (valuesCount == 0) {
            GUIUtilities.displayWarningMessage(bundleString("NoDataForImportMessage"));
            return;
        }

        targetColumnList.deleteCharAt(targetColumnList.length() - 1);
        sourceColumnList.deleteCharAt(sourceColumnList.length() - 1);

        // -- generate insert query

        StringBuilder insertQuery = new StringBuilder()
                .append("INSERT INTO ")
                .append(MiscUtils.getFormattedObject(Objects.requireNonNull(targetTableCombo.getSelectedItem()).toString(), executor.getDatabaseConnection()))
                .append(" (")
                .append(targetColumnList)
                .append(") VALUES (");

        while (valuesCount > 1) {
            insertQuery.append("?,");
            valuesCount--;
        }
        insertQuery.append("?);");

        // -- generate insert statement

        PreparedStatement insertStatement;
        try {
            insertStatement = executor.getPreparedStatement(insertQuery.toString());
            insertStatement.setEscapeProcessing(true);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);
            return;
        }

        // -- start import

        isCancel = false;
        pathToLob = !lobFileField.getText().trim().isEmpty() ? lobFileField.getText().trim() : null;

        SwingWorker worker = new SwingWorker("ImportData") {

            private final ImportHelper thisImportHelper = getImportHelper(fileType);

            @Override
            public Object construct() {

                progressLabel.setVisible(true);
                startImportButton.setText(Bundles.getCommon("cancel.button"));

                if (eraseTableCheck.isSelected())
                    eraseTable(Objects.requireNonNull(targetTableCombo.getSelectedItem()).toString());
                thisImportHelper.importData(sourceColumnList, valuesIndexes, insertStatement, executor);

                return null;
            }

            @Override
            public void finished() {

                progressLabel.setVisible(false);
                startImportButton.setEnabled(true);
                startImportButton.setText(bundleString("StartImportButtonText"));

                GUIUtilities.displayInformationMessage(bundleString("ImportDataFinished", thisImportHelper.getAddedRecordsCount()));
            }
        };

        worker.start();
    }

    // --- helper methods ---

    private ImportHelper getImportHelper(String fileType) {

        ImportHelper importHelper = null;
        switch (fileType) {

            case ("csv"):
                importHelper = new ImportHelperCSV(
                        this,
                        pathToFile,
                        pathToLob,
                        PREVIEW_ROWS_COUNT,
                        firstRowIsNamesCheck.isSelected()
                );
                break;

            case ("xlsx"):
                importHelper = new ImportHelperXLSX(
                        this,
                        pathToFile,
                        pathToLob,
                        PREVIEW_ROWS_COUNT,
                        firstRowIsNamesCheck.isSelected()
                );
                break;

            case ("xml"):
                importHelper = new ImportHelperXML(
                        this,
                        pathToFile,
                        pathToLob,
                        PREVIEW_ROWS_COUNT,
                        firstRowIsNamesCheck.isSelected()
                );
                break;

            case ("db"):
                importHelper = new ImportHelperDB(
                        this,
                        fileName,
                        PREVIEW_ROWS_COUNT,
                        (DatabaseConnection) sourceConnectionsCombo.getSelectedItem()
                );
                break;
        }

        return importHelper;
    }

    private void eraseTable(String tableName) {

        try {

            DefaultStatementExecutor executor = new DefaultStatementExecutor();
            executor.setCommitMode(false);
            executor.setKeepAlive(true);
            executor.setDatabaseConnection((DatabaseConnection) targetConnectionsCombo.getSelectedItem());

            String query = "DELETE FROM " + tableName + ";";
            executor.execute(QueryTypes.DELETE, query);
            executor.getConnection().commit();
            executor.releaseResources();

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("EraseDatabaseErrorMessage") + "\n" + e.getMessage(), e);
        }
    }

    private void targetConnectionChanged() {

        if (columnMappingTable != null)
            columnMappingTableModel.setRowCount(0);

        targetTableCombo.removeAllItems();
        if (targetConnectionsCombo.getSelectedIndex() == 0) {
            targetTableCombo.setEnabled(false);
            return;
        }

        targetHost = new DefaultDatabaseHost((DatabaseConnection) targetConnectionsCombo.getSelectedItem());
        if (!targetHost.isConnected())
            ConnectionManager.createDataSource(targetHost.getDatabaseConnection());

        targetTablesList.clear();
        targetTablesList.add(bundleString("SelectTable"));
        targetHost.getTables().stream()
                .filter(table -> !table.isSystem())
                .forEach(table -> targetTablesList.add(table.getName()));

        targetTablesList.forEach(targetTableCombo::addItem);
        targetTableCombo.setEnabled(true);

        targetHost.close();
    }

    private void sourceConnectionChanged() {

        if (connectionPreviewTableModel != null)
            for (int i = 0; i < connectionPreviewTableModel.getRowCount(); i++)
                connectionPreviewTableModel.deleteRow(i);

        sourceTableCombo.removeAllItems();
        if (sourceConnectionsCombo.getSelectedIndex() == 0) {
            sourceTableCombo.setEnabled(false);
            return;
        }

        DefaultDatabaseHost host = new DefaultDatabaseHost((DatabaseConnection) sourceConnectionsCombo.getSelectedItem());
        if (!host.isConnected())
            ConnectionManager.createDataSource(host.getDatabaseConnection());

        sourceTablesList.clear();
        sourceTablesList.add(bundleString("SelectTable"));
        host.getTables().stream()
                .filter(table -> !table.isSystem())
                .forEach(table -> sourceTablesList.add(table.getName()));

        sourceTablesList.forEach(sourceTableCombo::addItem);
        sourceTableCombo.setEnabled(true);

        host.close();
    }

    private void setSelectedComboItem(JComboBox comboBox, List<String> itemsList) {

        String filter = comboBox.getEditor().getItem().toString().toLowerCase().trim();
        if (!filter.isEmpty()) {

            String selected = itemsList.stream()
                    .filter(item -> item.toLowerCase().contains(filter))
                    .findFirst().orElse(null);

            if (selected != null) {
                comboBox.setSelectedItem(selected);
                return;
            }
        }

        comboBox.setSelectedIndex(0);
    }

    private void updateSourcePropertiesFields() {

        importConnectionPanel.setVisible(importFromConnectionCheck.isSelected());
        importFilePanel.setVisible(!importFromConnectionCheck.isSelected());

        if (importFromConnectionCheck.isSelected())
            previewSourceTable();
        else
            previewSourceFile(false);
    }

    private void setDelimiterComboSelectedValue(@SuppressWarnings("SameParameterValue") String key, JComboBox combo) {

        try (BufferedReader reader = new BufferedReader(new FileReader(ExportDataPanel.getParametersSaverFilePath()))) {

            String line;
            while ((line = reader.readLine()) != null) {

                String[] data = line.split(ExportDataPanel.getParametersSaverDelimiter());
                if (data[0].equals(key)) {
                    if (data.length > 1)
                        combo.setSelectedItem(data[1]);
                    break;
                }
            }

        } catch (IOException ignored) {
        }
    }

    private boolean targetNotSelected(boolean displayWarnings) {

        if (targetConnectionsCombo.getSelectedIndex() == 0) {
            if (displayWarnings)
                GUIUtilities.displayWarningMessage(bundleString("SelectDBMessage"));
            return true;
        }

        if (targetTableCombo.getSelectedIndex() == 0) {
            if (displayWarnings)
                GUIUtilities.displayWarningMessage(bundleString("SelectTableMessage"));
            return true;
        }

        return false;
    }

    public boolean isIntegerType(String type) {
        return type.toUpperCase().contains("INT");
    }

    public boolean isTimeType(String type) {
        return Objects.equals(type, "DATE") ||
                Objects.equals(type, "TIME") ||
                Objects.equals(type, "TIMESTAMP");
    }

    public boolean isBlobType(String type) {
        return type.toUpperCase().contains("BLOB");
    }

    // --- getters ---

    public String getDelimiter() {
        return (String) delimiterCombo.getSelectedItem();
    }

    public int getFirstRowIndex() {
        return (int) firstImportedRowSelector.getValue();
    }

    public int getLastRowIndex() {
        return (int) lastImportedRowSelector.getValue();
    }

    public int getBathStep() {
        return (int) commitStepSelector.getValue();
    }

    public int getSheetNumber() {
        return (int) sheetNumberSpinner.getValue();
    }

    public int getSourceColumnIndex(String header) {

        for (int i = 0; i < sourceHeaders.size(); i++)
            if (sourceHeaders.get(i).equals(header))
                return i;

        return -1;
    }

    public JTable getMappingTable() {
        return columnMappingTable;
    }

    public JSpinner getSheetNumberSpinner() {
        return sheetNumberSpinner;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setProgressLabel(String text) {

        FontMetrics fontMetrics = progressLabel.getFontMetrics(progressLabel.getFont());

        progressLabel.setText(text);
        progressLabel.setPreferredSize(new Dimension(
                fontMetrics.stringWidth(text),
                fontMetrics.getHeight()
        ));
    }

    // ---

    @Override
    public String getDisplayName() {
        return TITLE;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public String bundleString(String key) {
        return Bundles.get(ImportDataPanel.class, key);
    }

    public String bundleString(String key, Object... args) {
        return Bundles.get(ImportDataPanel.class, key, args);
    }

    private class MappingCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (isBlobType(columnMappingTableModel.getValueAt(row, 1).toString())) {

                JCheckBox loadAsFileCheck = new JCheckBox(bundleString("InsertAsFile"));
                loadAsFileCheck.setSelected(value.toString().equals("true"));

                return loadAsFileCheck;
            }

            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

    } // MappingCellRenderer class

}
