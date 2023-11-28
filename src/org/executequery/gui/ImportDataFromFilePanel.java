package org.executequery.gui;

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
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.DefaultProgressDialog;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * @author Alexey Kozlov
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ImportDataFromFilePanel extends DefaultTabViewActionPanel
        implements NamedView {

    public static final String TITLE = Bundles.get(ImportDataFromFilePanel.class, "Title");
    public static final String FRAME_ICON = "ImportDelimited16.png";

    private static final int PREVIEW_ROWS_COUNT = 50;
    private static final int MIN_COLUMN_WIDTH = 100;
    private static final String NOTHING_HEADER = "NOTHING";

    // --- GUI components ---

    private JComboBox connectionsCombo;
    private JComboBox tableCombo;
    private JTextField fileNameField;
    private JComboBox delimiterCombo;
    private JLabel delimiterLabel;
    private JCheckBox firstRowIsNamesCheck;
    private JCheckBox eraseTableCheck;
    private JSpinner firstImportedRowSelector;
    private JSpinner lastImportedRowSelector;
    private JSpinner commitStepSelector;

    private DefaultTableModel dataPreviewTableModel;
    private JTable dataPreviewTable;

    private DefaultTableModel columnMappingTableModel;
    private JTable columnMappingTable;

    private JButton browseButton;
    private JButton readFileButton;
    private JButton refreshMappingTableButton;
    private JButton startImportButton;

    private DefaultProgressDialog progressDialog;

    // ---

    private DefaultDatabaseHost dbHost;
    private ArrayList<String> sourceHeadersArray;
    private String pathToFile;
    private String fileName;
    private String fileType;

    public ImportDataFromFilePanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {

        // --- comboBoxes ---

        String[] delimiters = {";", "|", ",", "#"};
        delimiterCombo = WidgetFactory.createComboBox("delimiterCombo", delimiters);
        delimiterCombo.addActionListener(e -> sourcePropertiesChanged());
        delimiterCombo.setEditable(true);
        delimiterCombo.setVisible(false);

        tableCombo = WidgetFactory.createComboBox("tableCombo");
        tableCombo.addActionListener(e -> tableComboChanged());
        tableCombo.setEnabled(false);

        connectionsCombo = WidgetFactory.createComboBox("connectionsCombo");
        connectionsCombo.addActionListener(e -> connectionComboChanged());
        connectionsCombo.addItem(bundleString("SelectDB"));
        ((DatabaseConnectionRepository) Objects.requireNonNull(RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)))
                .findAll().forEach(dc -> connectionsCombo.addItem(dc));

        // --- checkBoxes ---

        firstRowIsNamesCheck = new JCheckBox(bundleString("IsFirstColumnNamesText"));
        firstRowIsNamesCheck.addActionListener(e -> sourcePropertiesChanged());

        eraseTableCheck = new JCheckBox(bundleString("IsEraseDatabaseText"));

        // --- numeric selectors ---

        firstImportedRowSelector = new JSpinner(new SpinnerNumberModel(0, 0, 999999999, 1));
        lastImportedRowSelector = new JSpinner(new SpinnerNumberModel(999999999, 0, 999999999, 1));
        commitStepSelector = new JSpinner(new SpinnerNumberModel(100, 100, 1000000, 100));

        // --- data preview table ---

        dataPreviewTableModel = new DefaultTableModel();
        dataPreviewTable = new JTable(dataPreviewTableModel);

        // --- column mapping table ---

        columnMappingTableModel = new DefaultTableModel();
        columnMappingTableModel.addColumn(bundleString("ColumnMappingTableTargetC"));
        columnMappingTableModel.addColumn(bundleString("ColumnMappingTableTargetT"));
        columnMappingTableModel.addColumn(bundleString("ColumnMappingTableSource"));
        columnMappingTableModel.addColumn(bundleString("ColumnMappingTableProps"));

        columnMappingTable = new JTable(columnMappingTableModel);
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

        browseButton = WidgetFactory.createButton("browseButton", bundleString("BrowseButtonText"));
        browseButton.addActionListener(e -> browseFile());

        readFileButton = WidgetFactory.createButton("readFileButton", bundleString("ReadFileButtonText"));
        readFileButton.addActionListener(e -> openSourceFile());

        refreshMappingTableButton = WidgetFactory.createButton("refreshMappingTableButton", bundleString("RefreshButtonText"));
        refreshMappingTableButton.addActionListener(e -> updateMappingTable());

        startImportButton = WidgetFactory.createButton("startImportButton", bundleString("StartImportButtonText"));
        startImportButton.addActionListener(e -> importData());

        // --- other ---

        fileNameField = WidgetFactory.createTextField("fileNameField");

        delimiterLabel = new JLabel(bundleString("DelimiterLabel"));
        delimiterLabel.setVisible(false);

        // ---

        connectionComboChanged();
        arrange();
    }

    private void arrange() {

        GridBagHelper gridBagHelper = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest();

        // --- scroll panes ---

        JScrollPane dataPreviewScrollPane = new JScrollPane(dataPreviewTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JScrollPane mappingTableScrollPane = new JScrollPane(columnMappingTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // --- preview panel ---

        JPanel previewPanel = new JPanel(new GridBagLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder(bundleString("PreviewTableLabel")));

        previewPanel.add(new JLabel(bundleString("InputDataFileLabel")), gridBagHelper.get());
        previewPanel.add(fileNameField, gridBagHelper.nextCol().fillHorizontally().setWidth(3).setMaxWeightX().get());
        previewPanel.add(browseButton, gridBagHelper.nextCol().setLabelDefault().get());
        previewPanel.add(readFileButton, gridBagHelper.nextCol().get());
        previewPanel.add(dataPreviewScrollPane, gridBagHelper.nextRowFirstCol().fillBoth().setMaxWeightY().spanX().get());
        previewPanel.add(firstRowIsNamesCheck, gridBagHelper.nextRowFirstCol().setMinWeightY().setWidth(2).get());
        previewPanel.add(delimiterLabel, gridBagHelper.nextCol().get());
        previewPanel.add(delimiterCombo, gridBagHelper.nextCol().get());

        // --- mapping panel ---

        JPanel mappingPanel = new JPanel(new GridBagLayout());
        mappingPanel.setBorder(BorderFactory.createTitledBorder(bundleString("MappingTableLabel")));

        gridBagHelper.fillHorizontally().addLabelFieldPair(mappingPanel,
                bundleString("TargetConnectionLabel"), connectionsCombo, null, true, false);
        gridBagHelper.addLabelFieldPair(mappingPanel,
                bundleString("TargetTableLabel"), tableCombo, null, false, false);
        mappingPanel.add(refreshMappingTableButton, gridBagHelper.nextCol().spanX().get());
        mappingPanel.add(mappingTableScrollPane, gridBagHelper.nextRowFirstCol().setMaxWeightY().fillBoth().spanX().get());
        mappingPanel.add(eraseTableCheck, gridBagHelper.nextRowFirstCol().fillHorizontally().setMinWeightY().spanX().get());

        // --- split pane ---

        FlatSplitPane splitPane = new FlatSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, mappingPanel);
        splitPane.setResizeWeight(0.5);

        // --- bottom panel ---

        JPanel bottomPanel = new JPanel(new GridBagLayout());

        gridBagHelper.addLabelFieldPair(bottomPanel,
                bundleString("FirstNumericSelectorsLabel"), firstImportedRowSelector, null, true, false);
        gridBagHelper.addLabelFieldPair(bottomPanel,
                bundleString("LastNumericSelectorsLabel"), lastImportedRowSelector, null, false, false);
        gridBagHelper.addLabelFieldPair(bottomPanel,
                bundleString("CommitSelectorLabel"), commitStepSelector, null, false, true);
        bottomPanel.add(startImportButton, gridBagHelper.nextRow().anchorSouthEast().setLabelDefault().get());

        // --- panels settings ---

        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void browseFile() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Data Files", "csv", "xml", "xlsx"));
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle(bundleString("OpenFileDialogText"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), bundleString("OpenFileDialogButton"));
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        File file = fileChooser.getSelectedFile();
        if (!file.exists()) {
            GUIUtilities.displayWarningMessage(bundleString("FileDoesNotExistMessage") + "\n" + fileNameField.getText());
            return;
        }

        fileNameField.setText(file.getAbsolutePath());
        openSourceFile();
    }

    public void openSourceFile() {

        if (fileNameField.getText().isEmpty()) {
            GUIUtilities.displayWarningMessage(bundleString("NoFileSelectedMessage"));
            return;
        }

        File sourceFile = new File(fileNameField.getText());
        pathToFile = sourceFile.getAbsolutePath();
        fileName = FilenameUtils.getBaseName(sourceFile.getName());
        fileType = FileNameUtils.getExtension(sourceFile.getName());

        switch (fileType) {

            case ("csv"): {
                previewCSV();
                break;
            }
            case ("xml"):
            case ("xlsx"):
            default: {
                GUIUtilities.displayWarningMessage(bundleString("FileTypeNotSupported"));
            }
        }

        delimiterLabel.setVisible(fileType.equalsIgnoreCase("csv"));
        delimiterCombo.setVisible(fileType.equalsIgnoreCase("csv"));
    }

    public void updateMappingTable() {

        columnMappingTableModel.setRowCount(0);

        if (!updateMappingTableAllowed(true))
            return;

        JComboBox sourceComboBox = new JComboBox();
        sourceComboBox.addItem(NOTHING_HEADER);
        sourceHeadersArray.forEach(sourceComboBox::addItem);

        TableColumn thirdColumn = columnMappingTable.getColumnModel().getColumn(2);
        thirdColumn.setCellEditor(new DefaultCellEditor(sourceComboBox));

        if (tableCombo.getSelectedItem() != null) {
            for (DatabaseColumn column : dbHost.getColumns(tableCombo.getSelectedItem().toString())) {

                String fieldProperty = "";
                if (isTimeType(column.getTypeName())) {
                    switch (column.getTypeName()) {

                        case "DATE":
                            fieldProperty = SystemProperties.getProperty("user", "results.date.pattern");
                            if (fieldProperty == null || fieldProperty.isEmpty())
                                fieldProperty = SystemProperties.getProperty("system", "results.date.pattern");
                            break;

                        case "TIME":
                            fieldProperty = SystemProperties.getProperty("user", "results.time.pattern");
                            if (fieldProperty == null || fieldProperty.isEmpty())
                                fieldProperty = SystemProperties.getProperty("system", "results.time.pattern");
                            break;

                        case "TIMESTAMP":
                            fieldProperty = SystemProperties.getProperty("user", "results.timestamp.pattern");
                            if (fieldProperty == null || fieldProperty.isEmpty())
                                fieldProperty = SystemProperties.getProperty("system", "results.timestamp.pattern");
                            break;
                    }
                }

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
    }

    public void importData() {

        if (!startImportAllowed())
            return;

        Log.info("ImportDataFromFilePanel: importing data...");

        DefaultStatementExecutor executor = new DefaultStatementExecutor();
        executor.setCommitMode(false);
        executor.setKeepAlive(true);
        executor.setDatabaseConnection((DatabaseConnection) connectionsCombo.getSelectedItem());

        StringBuilder targetColumnList = new StringBuilder();
        StringBuilder sourceColumnList = new StringBuilder();

        Vector<Vector> dataFromTableVector = columnMappingTableModel.getDataVector();

        int valuesCount = 0;
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
            Log.info("ImportDataFromFilePanel: import process stopped (no data for import selected)");
            return;
        }

        targetColumnList.deleteCharAt(targetColumnList.length() - 1);
        sourceColumnList.deleteCharAt(sourceColumnList.length() - 1);

        StringBuilder insertPattern = new StringBuilder()
                .append("INSERT INTO ")
                .append(MiscUtils.getFormattedObject(Objects.requireNonNull(tableCombo.getSelectedItem()).toString(), executor.getDatabaseConnection()))
                .append(" (")
                .append(targetColumnList)
                .append(") VALUES (");

        while (valuesCount > 1) {
            insertPattern.append("?,");
            valuesCount--;
        }
        insertPattern.append("?);");

        PreparedStatement insertStatement;
        try {
            insertStatement = executor.getPreparedStatement(insertPattern.toString());
            insertStatement.setEscapeProcessing(true);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);
            Log.error("ImportDataFromFilePanel: import process stopped due to an error", e);
            return;
        }

        Log.info("ImportDataFromFilePanel: SQL-INSERT pattern created");
        switch (fileType) {

            case ("csv"): {

                progressDialog = new DefaultProgressDialog(bundleString("ExecutingProgressDialog"));

                SwingWorker worker = new SwingWorker("ImportCSV") {
                    @Override
                    public Object construct() {
                        importCSV(sourceColumnList, valuesIndexes, insertStatement, executor);
                        return null;
                    }

                    @Override
                    public void finished() {
                        if (progressDialog != null)
                            progressDialog.dispose();
                    }
                };

                worker.start();
                progressDialog.run();

                break;
            }
            case ("xml"):
            case ("xlsx"):
            default:
                Log.info("ImportDataFromFilePanel: import process stopped (no source file type selected)");
        }
    }

    private void importCSV(StringBuilder sourceColumnList, boolean[] valuesIndexes,
                           PreparedStatement insertStatement, DefaultStatementExecutor executor) {

        try {

            Statement sourceFileStatement = getStatementCSV();

            if (sourceFileStatement == null)
                return;

            String sourceSelectQuery = "SELECT " + sourceColumnList.toString() + " FROM " + MiscUtils.getFormattedObject(fileName, executor.getDatabaseConnection());
            ResultSet sourceFileData = sourceFileStatement.executeQuery(sourceSelectQuery);

            int executorIndex = 0;
            int linesCount = 0;

            String[] sourceFields = sourceColumnList.toString().split(",");
            while (sourceFileData.next()) {

                if (progressDialog.isCancel())
                    break;

                if (linesCount >= (int) lastImportedRowSelector.getValue())
                    break;

                if (linesCount >= (int) firstImportedRowSelector.getValue()) {

                    int fieldIndex = 0;
                    int mappedIndex = 0;
                    for (boolean valueIndex : valuesIndexes) {

                        if (valueIndex) {

                            Object insertParameter = sourceFileData.getString(sourceFields[fieldIndex]);
                            String columnType = insertStatement.getParameterMetaData().getParameterTypeName(fieldIndex + 1);
                            String columnProperty = columnMappingTable.getValueAt(mappedIndex, 3).toString();

                            if (isTimeType(columnType)) {
                                insertParameter = LocalDateTime.parse(insertParameter.toString(), DateTimeFormatter.ofPattern(columnProperty));

                            } else if (isBlobType(columnType) && columnProperty.equals("true")) {
                                insertParameter = Files.newInputStream(new File(insertParameter.toString()).toPath());
                            }

                            insertStatement.setObject(fieldIndex + 1, insertParameter);
                            fieldIndex++;
                        }
                        mappedIndex++;
                    }

                    insertStatement.addBatch();

                    int batchStep = (int) commitStepSelector.getValue();
                    if (executorIndex % batchStep == 0 && executorIndex != 0) {

                        insertStatement.executeBatch();
                        executor.getConnection().commit();

                        Log.info("ImportDataFromFilePanel: " + batchStep + " records was added");
                    }
                    executorIndex++;
                }
                linesCount++;
            }

            insertStatement.executeBatch();
            executor.getConnection().commit();

            Log.info("ImportDataFromFilePanel: import process has been completed, " + executorIndex + " records was added");

        } catch (DateTimeParseException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("DateTimeFormatErrorMessage") + "\n" + e.getMessage(), e);
            Log.error("ImportDataFromFilePanel: import process was stopped (date/time format incorrect)");

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);
            Log.error("ImportDataFromFilePanel: import process was stopped due to an error", e);

        } finally {
            executor.releaseResources();
        }
    }

    private void connectionComboChanged() {

        tableCombo.removeAllItems();

        if (columnMappingTable != null)
            columnMappingTableModel.setRowCount(0);

        if (connectionsCombo.getSelectedIndex() == 0) {
            tableCombo.setEnabled(false);
            return;
        }

        dbHost = new DefaultDatabaseHost((DatabaseConnection) connectionsCombo.getSelectedItem());
        if (!dbHost.isConnected())
            ConnectionManager.createDataSource(dbHost.getDatabaseConnection());

        tableCombo.setEnabled(true);
        tableCombo.addItem(bundleString("SelectTable"));
        dbHost.getTables().stream()
                .filter(table -> !table.isSystem())
                .forEach(table -> tableCombo.addItem(table.getName()));

        dbHost.close();
    }

    private void tableComboChanged() {
        if (updateMappingTableAllowed(false))
            updateMappingTable();
    }

    private void sourcePropertiesChanged() {

        if (pathToFile != null)
            openSourceFile();

        if (updateMappingTableAllowed(false))
            updateMappingTable();
    }

    private void previewCSV() {

        try {
            String[] readData = getHeadersCSV();

            dataPreviewTableModel.setColumnCount(0);
            dataPreviewTableModel.setRowCount(0);

            for (String tempHeader : sourceHeadersArray)
                dataPreviewTableModel.addColumn(tempHeader);
            for (String tempHeader : sourceHeadersArray)
                dataPreviewTable.getColumn(tempHeader).setMinWidth(MIN_COLUMN_WIDTH);

            if (sourceHeadersArray.size() < 7)
                dataPreviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            else
                dataPreviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            for (int i = 0; i < PREVIEW_ROWS_COUNT; i++) {

                if (readData[i] == null)
                    continue;

                dataPreviewTableModel.addRow(readData[i].split(
                        Objects.requireNonNull(delimiterCombo.getSelectedItem()).toString()));
            }

        } catch (Exception e) {
            GUIUtilities.displayWarningMessage(bundleString("FileReadingErrorMessage") + "\n" + e.getMessage());
        }
    }

    private String[] getHeadersCSV() {

        if (sourceHeadersArray == null)
            sourceHeadersArray = new ArrayList<>();
        else
            sourceHeadersArray.clear();

        String[] readData = new String[PREVIEW_ROWS_COUNT];
        try {

            FileReader reader = new FileReader(pathToFile);
            Scanner scanner = new Scanner(reader);

            String firstRowFromSource = "";
            for (int i = 0; i < PREVIEW_ROWS_COUNT; i++) {

                if (scanner.hasNextLine()) {
                    String tempRow = scanner.nextLine();

                    if (i == 0) {
                        firstRowFromSource = tempRow;
                        if (firstRowIsNamesCheck.isSelected())
                            tempRow = scanner.hasNextLine() ? scanner.nextLine() : "";
                    }
                    readData[i] = tempRow;

                } else
                    break;
            }
            reader.close();

            String[] firstRowFromSourceArray = firstRowFromSource.split(
                    Objects.requireNonNull(delimiterCombo.getSelectedItem()).toString());

            if (firstRowIsNamesCheck.isSelected()) {
                sourceHeadersArray.addAll(Arrays.asList(firstRowFromSourceArray));
            } else {
                for (int i = 0; i < firstRowFromSourceArray.length; i++)
                    sourceHeadersArray.add("COLUMN" + (i + 1));
            }

        } catch (FileNotFoundException e) {
            GUIUtilities.displayWarningMessage(bundleString("FileDoesNotExistMessage") + "\n" + fileNameField.getText());

        } catch (Exception e) {
            GUIUtilities.displayWarningMessage(bundleString("FileReadingErrorMessage") + "\n" + e.getMessage());
        }

        return readData;
    }

    private Statement getStatementCSV() {

        Statement statement = null;
        try {

            Properties properties = new Properties();
            properties.setProperty("separator", Objects.requireNonNull(delimiterCombo.getSelectedItem()).toString());
            if (!firstRowIsNamesCheck.isSelected()) {
                properties.setProperty("suppressHeaders", "true");
                properties.setProperty("defectiveHeaders", "true");
            }

            String connectionUrl = "jdbc:relique:csv:" + Paths.get(pathToFile).getParent().toString();
            if (UIUtils.isWindows())
                connectionUrl = connectionUrl.replace("\\", "/");

            statement = DriverManager.getConnection(connectionUrl, properties).createStatement();

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);
            Log.error("ImportDataFromFilePanel: reading CSV file was stopped due to an error", e);
        }

        return statement;
    }

    public boolean updateMappingTableAllowed(boolean displayWarnings) {

        if (sourceHeadersArray == null) {
            if (displayWarnings)
                GUIUtilities.displayWarningMessage(bundleString("ReadFileMessage"));
            return false;
        }

        if (connectionsCombo.getSelectedIndex() == 0) {
            if (displayWarnings)
                GUIUtilities.displayWarningMessage(bundleString("SelectDBMessage"));
            return false;
        }

        if (tableCombo.getSelectedIndex() == 0) {
            if (displayWarnings)
                GUIUtilities.displayWarningMessage(bundleString("SelectTableMessage"));
            return false;
        }

        return true;
    }

    private boolean startImportAllowed() {

        if (!updateMappingTableAllowed(true))
            return false;

        if (eraseTableCheck.isSelected())
            eraseTable(Objects.requireNonNull(tableCombo.getSelectedItem()).toString());

        return true;
    }

    private void eraseTable(String tableName) {

        DefaultStatementExecutor executor = new DefaultStatementExecutor();

        try {
            executor.setCommitMode(false);
            executor.setKeepAlive(true);
            executor.setDatabaseConnection((DatabaseConnection) connectionsCombo.getSelectedItem());

            Log.info("ImportDataFromFilePanel: erasing target table...");

            String query = "DELETE FROM " + tableName + ";";
            executor.execute(QueryTypes.DELETE, query);
            executor.getConnection().commit();

            Log.info("ImportDataFromFilePanel: erasing target table finished");

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("EraseDatabaseErrorMessage") + "\n" + e.getMessage(), e);
            Log.error("ImportDataFromFilePanel: erasing target table was stopped due to an error", e);

        } finally {
            executor.releaseResources();
        }
    }

    private boolean isTimeType(String type) {
        return Objects.equals(type, "DATE") ||
                Objects.equals(type, "TIME") ||
                Objects.equals(type, "TIMESTAMP");
    }

    private boolean isBlobType(String type) {
        return type.toUpperCase().contains("BLOB");
    }

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
        return Bundles.get(ImportDataFromFilePanel.class, key);
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
