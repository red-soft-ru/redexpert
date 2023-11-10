package org.executequery.gui;

import org.apache.commons.io.FilenameUtils;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabViewActionPanel;
import org.executequery.components.FileChooserDialog;
import org.executequery.components.MinimumWidthActionButton;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.DefaultProgressDialog;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.*;

/**
 * @author Alexey Kozlov
 */

public class ImportDataFromFilePanel extends DefaultTabViewActionPanel
        implements NamedView, ConnectionListener {

    public static final String TITLE = bundledString("Title");
    public static final String FRAME_ICON = "ImportDelimited16.svg";

    private static final int PREVIEW_ROWS_COUNT = 50;
    private static final int MIN_COLUMN_WIDTH = 100;
    private static final String NOTHING_HEADER = "NOTHING";


    // ----- main components of this panel -----

    private JComboBox connectionsCombo; //target database
    private TableSelectionCombosGroup combosGroup;  //connections, that contains comboBox
    private JComboBox tableCombo;   //target table
    private JComboBox sourceCombo;  //source type
    private JTextField fileNameField;   //path to importing file
    private JComboBox delimiterCombo;   //delimiter type
    private JComboBox timeFormatCombo;   //time format
    private JComboBox dateFormatCombo;  //date format
    private JComboBox timestampDelimiterCombo;  //timestamp delimiter
    private JCheckBox isFirstColumnNames;  //check whether the first row stores column names
    private JCheckBox isEraseDatabase;  //check whether it's necessary to clear DB before import
    private LoggingOutputPanel outputPanel; //logging module executing
    private JSpinner firstImportSelector;   //numeric selector of first importing line
    private JSpinner lastImportSelector;    //numeric selector of last importing line
    private JSpinner batchStepSelector; //numeric selector of batch commit step

    // ----- table for data preview -----

    private DefaultTableModel dataPreviewTableModel;
    private JTable dataPreviewTable;

    // ----- table for columns mapping -----

    private DefaultTableModel columnMappingTableModel;
    private JTable columnMappingTable;

    // ----- buttons -----

    private JButton browseButton;
    private JButton readFileButton;
    private JButton fillMappingTableButton;
    private JButton startImportButton;

    // ----- other -----

    private DefaultDatabaseHost dbHost;
    private String pathToFile;
    private String sourceFileName;
    private ArrayList<String> headersOfSourceArray;
    private DefaultProgressDialog progressDialog;

    public ImportDataFromFilePanel() {
        super(new BorderLayout());
        init();

        Log.info("ImportDataFromFilePanel: class was created");
    }

    private void init() {

        String[] delimiters = {bundledString("SelectDelimiter"),
                "|", ",", ";", "#"};

        String[] sourceTypes = {bundledString("SelectFileType"),
                "csv", "json", "xml", "xlsx"};

        String[] timeFormats = {bundledString("SelectTimeFormat"),
                "HH:mm", "HH:mm:ss", "HH:mm:ss[.n[n[n]]]"};

        String[] dateFormats = {bundledString("SelectDateFormat"),
                "dd.MM.yyyy", "yyyy-MM-dd", "MM/dd/yyyy", "yyyyMMdd"};

        String[] timestampDelimiters = {bundledString("SelectTimestampDelimiterFormat"),
                "\"spase\"", "_", "T"};

        // ---------- comboBoxes ----------

        delimiterCombo = WidgetFactory.createComboBox("delimiterCombo", delimiters);
        delimiterCombo.addActionListener(e -> readDataPropsChanged());
        delimiterCombo.setEditable(true);

        sourceCombo = WidgetFactory.createComboBox("sourceCombo", sourceTypes);
        sourceCombo.setSelectedIndex(1);    //remove when adding support new for file formats
        sourceCombo.setEnabled(false);      //remove when adding support new for file formats

        timeFormatCombo = WidgetFactory.createComboBox("timeFormatCombo", timeFormats);
        timeFormatCombo.setEditable(true);

        dateFormatCombo = WidgetFactory.createComboBox("dateFormatCombo", dateFormats);
        dateFormatCombo.setEditable(true);

        timestampDelimiterCombo = WidgetFactory.createComboBox("timestampDelimiterCombo", timestampDelimiters);
        timestampDelimiterCombo.setEditable(true);

        connectionsCombo = WidgetFactory.createComboBox("connectionsCombo");
        connectionsCombo.addActionListener(e -> connectionComboChanged());

        combosGroup = new TableSelectionCombosGroup(connectionsCombo);

        tableCombo = WidgetFactory.createComboBox("tableCombo");
        tableCombo.addActionListener(e -> tableComboChanged());

        // ---------- checkBoxes ----------

        isFirstColumnNames = new JCheckBox(bundledString("IsFirstColumnNamesText"));
        isFirstColumnNames.addActionListener(e -> readDataPropsChanged());

        isEraseDatabase = new JCheckBox(bundledString("IsEraseDatabaseText"));

        // ---------- numeric selectors ----------

        firstImportSelector = new JSpinner(new SpinnerNumberModel(0, 0, 999999999, 1));
        lastImportSelector = new JSpinner(new SpinnerNumberModel(999999999, 0, 999999999, 1));
        batchStepSelector = new JSpinner(new SpinnerNumberModel(100, 100, 1000000, 100));

        // ---------- data preview table ----------

        dataPreviewTableModel = new DefaultTableModel();
        dataPreviewTable = new JTable(dataPreviewTableModel);

        // ---------- column mapping table ----------

        columnMappingTableModel = new DefaultTableModel();
        columnMappingTable = new JTable(columnMappingTableModel);

        columnMappingTableModel.addColumn(bundledString("ColumnMappingTableTargetC"));
        columnMappingTableModel.addColumn(bundledString("ColumnMappingTableTargetT"));
        columnMappingTableModel.addColumn(bundledString("ColumnMappingTableSource"));

        columnMappingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // ---------- other ----------

        fileNameField = WidgetFactory.createTextField("fileNameField");
        outputPanel = new LoggingOutputPanel();

        // ---------- buttons ----------

        browseButton = WidgetFactory.createInlineFieldButton(bundledString("BrowseButtonText"));
        browseButton.setActionCommand("browse");
        browseButton.addActionListener(this);

        readFileButton = new MinimumWidthActionButton(
                85, this,
                bundledString("ReadFileButtonText"), "readFileForPreview");

        fillMappingTableButton = new MinimumWidthActionButton(
                85, this,
                bundledString("FillMappingTableButtonText"), "fillMappingTable");

        startImportButton = new MinimumWidthActionButton(
                85, this,
                bundledString("StartImportButtonText"), "startImport");

        connectionComboChanged();
        componentsArranging();
    }

    private void componentsArranging() {

        // ---------------------------------------------
        // Preparing ScrollPanes and FlatSplitPanes
        // ---------------------------------------------

        JScrollPane dataPreviewTextAreaWithScrolls = new JScrollPane(dataPreviewTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JScrollPane columnMappingTableWithScrolls = new JScrollPane(columnMappingTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        FlatSplitPane splitPane = new FlatSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, dataPreviewTextAreaWithScrolls, columnMappingTableWithScrolls);
        splitPane.setResizeWeight(0.5);

        FlatSplitPane bigSplitPane = new FlatSplitPane(
                JSplitPane.VERTICAL_SPLIT, splitPane, outputPanel);
        splitPane.setResizeWeight(0.5);

        // ---------------------------------------------
        // Components arranging
        // ---------------------------------------------

        JPanel mainPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 5));

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5);
        gridBagHelper.anchorNorthWest();

        // ----------- first row -----------

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("TargetConnectionLabel"), connectionsCombo,
                null, true, false, 4);

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("TargetTableLabel"), tableCombo,
                null, false, true, 3);

        // ----------- second row -----------

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("InputDataFileLabel"), fileNameField,
                null, true, false, 3);

        mainPanel.add(browseButton, gridBagHelper.nextCol().setLabelDefault().get());

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("ImportFileTypeLabel"), sourceCombo,
                null, false, true, 1);

        // ----------- third row -----------

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("DateFormatLabel"), dateFormatCombo,
                null, true, false, 1);

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("TimeFormatLabel"), timeFormatCombo,
                null, false, false, 2);

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("TimestampDelimiterLabel"), timestampDelimiterCombo,
                null, false, true, 1);

        // ----------- fourth row -----------

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("DelimiterLabel"), delimiterCombo,
                null, true, false, 1);

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("CommitSelectorLabel"), batchStepSelector,
                null, false, false, 2);

        mainPanel.add(isEraseDatabase, gridBagHelper.nextCol().setWidth(2).get());

        // ----------- fifth row -----------

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("FirstNumericSelectorsLabel"), firstImportSelector,
                null, true, false, 1);

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("LastNumericSelectorsLabel"), lastImportSelector,
                null, false, false, 2);

        mainPanel.add(isFirstColumnNames, gridBagHelper.nextCol().setWidth(2).get());

        // ----------- sixth row -----------

        mainPanel.add(new JLabel(bundledString("PreviewAndColumnMappingTablesLabel")),
                gridBagHelper.nextRowFirstCol().setWidth(2).get());

        mainPanel.add(new JPanel(), gridBagHelper.nextCol().setWidth(1).nextCol().setMaxWeightX().fillHorizontally().get());

        // ----------- seventh row -----------

        mainPanel.add(bigSplitPane, gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().get());

        // ----------- buttonPanel -----------

        buttonPanel.add(readFileButton);
        buttonPanel.add(fillMappingTableButton);
        buttonPanel.add(startImportButton);

        // ----------- panels settings -----------

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    }

    // ---------------------------------------------
    // Button handler
    // ---------------------------------------------

    public void browse() {

        if (sourceCombo.getSelectedIndex() == 0) {
            GUIUtilities.displayWarningMessage(bundledString("SelectFileTypeMessage"));
            return;
        }

        String validFileType = Objects.requireNonNull(sourceCombo.getSelectedItem()).toString();

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(
                new FileNameExtensionFilter(validFileType + " Files", validFileType));
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle(bundledString("OpenFileDialogText"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(
                GUIUtilities.getInFocusDialogOrWindow(), bundledString("OpenFileDialogButton"));
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        File file = fileChooser.getSelectedFile();
        if (!file.exists()) {
            GUIUtilities.displayWarningMessage(
                    bundledString("FileDoesNotExistMessage") + "\n" + fileNameField.getText());
            return;
        }

        fileNameField.setText(file.getAbsolutePath());
    }

    public void readFileForPreview() {

        pathToFile = fileNameField.getText();

        if (pathToFile.isEmpty()) {
            GUIUtilities.displayWarningMessage(bundledString("NoFileSelectedMessage"));
            return;
        }

        switch (Objects.requireNonNull(sourceCombo.getSelectedItem()).toString()) {

            case ("csv"): {

                readCSVFileForPreview();
                break;

            }
            case ("json"): {

                GUIUtilities.displayWarningMessage("Import from json is currently unavailable");
                break;

            }
            case ("xml"): {

                GUIUtilities.displayWarningMessage("Import from xml is currently unavailable");
                break;

            }
            case ("xlsx"): {

                GUIUtilities.displayWarningMessage("Import from xlsx is currently unavailable");
                break;

            }
            default: {

                GUIUtilities.displayWarningMessage(bundledString("NoFileTypeSelectedMessage"));
            }

        }

    }

    public void fillMappingTable() {

        if (!fillMappingTableAllowed(true))
            return;

        // ----------- erasing ColumnMappingTable -----------

        columnMappingTableModel.setRowCount(0);

        // ----------- creating source comboBox -----------

        JComboBox sourceComboBox = new JComboBox();
        sourceComboBox.addItem(NOTHING_HEADER);

        for (String tempHeader : headersOfSourceArray)
            sourceComboBox.addItem(tempHeader);

        // ----------- pasting source comboBox in ColumnMappingTable -----------

        TableColumn thirdColumn = columnMappingTable.getColumnModel().getColumn(2);
        thirdColumn.setCellEditor(new DefaultCellEditor(sourceComboBox));

        // ----------- filling ColumnMappingTable -----------

        List<DatabaseColumn> dbHostTableColumns = dbHost.getColumns( Objects.requireNonNull(tableCombo.getSelectedItem()).toString());

        for (DatabaseColumn dbHostTableColumn : dbHostTableColumns)
            columnMappingTableModel.addRow(new Object[]{
                    dbHostTableColumn.getName(),
                    dbHostTableColumn.getTypeName()});

    }

    public void startImport() {

        if (!startImportAllowed())
            return;

        Log.info("ImportDataFromFilePanel: import process started...");

        // ---------------------------------------------
        // Preparing SQL-INSERT pattern
        // ---------------------------------------------

        DefaultStatementExecutor executor = new DefaultStatementExecutor();
        executor.setCommitMode(false);
        executor.setKeepAlive(true);
        executor.setDatabaseConnection(combosGroup.getSelectedHost().getDatabaseConnection());

        PreparedStatement insertStatement;

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

            }
        }

        targetColumnList.deleteCharAt(targetColumnList.length() - 1);
        sourceColumnList.deleteCharAt(sourceColumnList.length() - 1);

        if (valuesCount == 0) {

            GUIUtilities.displayWarningMessage(
                    bundledString("NoDataForImportMessage"));
            Log.info("ImportDataFromFilePanel: import process stopped (no data for import selected)");

            return;
        }

        StringBuilder insertPattern = new StringBuilder();
        insertPattern.append("INSERT INTO ");
        insertPattern.append(MiscUtils.getFormattedObject(
                Objects.requireNonNull(tableCombo.getSelectedItem()).toString(), executor.getDatabaseConnection()));
        insertPattern.append(" (");
        insertPattern.append(targetColumnList);
        insertPattern.append(") VALUES (");

        while (valuesCount > 1) {
            insertPattern.append("?,");
            valuesCount--;
        }
        insertPattern.append("?);");

        try {

            insertStatement = executor.getPreparedStatement(insertPattern.toString());

        } catch (Exception e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);

            Log.error("ImportDataFromFilePanel: import process stopped due to an error", e);

            return;
        }

        outputPanel.append("SQL-INSERT pattern created: " + insertPattern);
        Log.info("ImportDataFromFilePanel: SQL-INSERT pattern created");

        // ---------------------------------------------
        // Executing SQL-INSERT request depending on source file type
        // ---------------------------------------------

        switch (Objects.requireNonNull(sourceCombo.getSelectedItem()).toString()) {

            case ("csv"): {

                progressDialog = new DefaultProgressDialog(bundledString("ExecutingProgressDialog"));

                SwingWorker worker = new SwingWorker("ImportCSV") {
                    @Override
                    public Object construct() {
                        insertCSV(sourceColumnList, valuesIndexes, insertStatement, executor);
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
            case ("json"): {

                GUIUtilities.displayWarningMessage("Import from json is currently unavailable");
                break;

            }
            case ("xml"): {

                GUIUtilities.displayWarningMessage("Import from xml is currently unavailable");
                break;

            }
            case ("xlsx"): {

                GUIUtilities.displayWarningMessage("Import from xlsx is currently unavailable");
                break;

            }
            default: {

                GUIUtilities.displayWarningMessage(bundledString("NoFileTypeSelectedMessage"));
                Log.info("ImportDataFromFilePanel: import process stopped (no source file type selected)");
            }

        }

    }

    // ---------------------------------------------
    // Executing SQL-INSERT request methods
    // ---------------------------------------------

    private void insertCSV(StringBuilder sourceColumnList, boolean[] valuesIndexes,
                           PreparedStatement insertStatement, DefaultStatementExecutor executor) {

        try {

            outputPanel.appendAction("Executing SQL-INSERT request...");

            Statement sourceFileStatement = readCSVFile();

            if (sourceFileStatement == null)
                return;

            String SQLSourceRequest = "SELECT " + sourceColumnList.toString() + " FROM " + MiscUtils.getFormattedObject(sourceFileName, executor.getDatabaseConnection());
            ResultSet sourceFileData = sourceFileStatement.executeQuery(SQLSourceRequest);

            String[] sourceFields = sourceColumnList.toString().split(",");

            long startTime = System.currentTimeMillis();

            int executorIndex = 0;
            int linesCount = 0;

            while (sourceFileData.next()) {

                if (progressDialog.isCancel())
                    break;

                if (linesCount >= (int) lastImportSelector.getValue())
                    break;

                if (linesCount >= (int) firstImportSelector.getValue()) {

                    int fieldIndex = 0;

                    for (boolean valueIndex : valuesIndexes) {

                        if (valueIndex) {

                            Object param;
                            String targetColumnType = insertStatement.getParameterMetaData()
                                    .getParameterTypeName(fieldIndex + 1);

                            if (Objects.equals(targetColumnType, "DATE") ||
                                    Objects.equals(targetColumnType, "TIME") ||
                                    Objects.equals(targetColumnType, "TIMESTAMP")) {

                                DateTimeFormatter timestampFormat;

                                if (Objects.equals(targetColumnType, "DATE") &&
                                        dateFormatCombo.getSelectedIndex() != 0) {

                                    timestampFormat = DateTimeFormatter.
                                            ofPattern(Objects.requireNonNull(dateFormatCombo.getSelectedItem()).toString());

                                } else if (Objects.equals(targetColumnType, "TIME") &&
                                        timeFormatCombo.getSelectedIndex() != 0) {

                                    timestampFormat = DateTimeFormatter.
                                            ofPattern(Objects.requireNonNull(timeFormatCombo.getSelectedItem()).toString());

                                } else if (Objects.equals(targetColumnType, "TIMESTAMP") &&
                                        dateFormatCombo.getSelectedIndex() != 0 &&
                                        timeFormatCombo.getSelectedIndex() != 0) {

                                    String timestampPattern = Objects.requireNonNull(dateFormatCombo.getSelectedItem()) +
                                            " " + Objects.requireNonNull(timeFormatCombo.getSelectedItem());

                                    timestampFormat = DateTimeFormatter.
                                            ofPattern(Objects.requireNonNull(timestampPattern));

                                } else {

                                    GUIUtilities.displayWarningMessage(bundledString("NoTimeFormatSelectedMessage"));
                                    outputPanel.appendError("Importing data stopped: no date/time format selected");
                                    Log.info("ImportDataFromFilePanel: import process stopped (no date/time format selected)");

                                    return;
                                }

                                String stringTimestamp = sourceFileData.getString(sourceFields[fieldIndex])
                                        .replace(Objects.requireNonNull(
                                                timestampDelimiterCombo.getSelectedItem()).toString(), " ");

                                param = LocalDateTime.parse(stringTimestamp, timestampFormat);

                            } else
                                param = sourceFileData.getString(sourceFields[fieldIndex]);

                            insertStatement.setObject(fieldIndex + 1, param);
                            fieldIndex++;

                        }

                    }

                    insertStatement.addBatch();

                    int batchStep = (int) batchStepSelector.getValue();
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

            outputPanel.append("SQL-INSERT request finished, " + executorIndex + " records was added");
            outputPanel.append("Duration: " + MiscUtils.formatDuration(System.currentTimeMillis() - startTime));
            Log.info("ImportDataFromFilePanel: import process has been completed, " + executorIndex + " records was added");

        } catch (DateTimeParseException e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("DateTimeFormatErrorMessage") + "\n" + e.getMessage(), e);

            outputPanel.appendError("Importing data stopped: date/time format incorrect");
            Log.error("ImportDataFromFilePanel: import process was stopped (date/time format incorrect)");

        } catch (Exception e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);

            outputPanel.appendError("Importing data stopped due to an error");
            Log.error("ImportDataFromFilePanel: import process was stopped due to an error", e);

        } finally {

            executor.releaseResources();
        }

    }

    // ---------------------------------------------
    // Event handlers for changing the state of some components
    // ---------------------------------------------

    private void connectionComboChanged() {

        dbHost = new DefaultDatabaseHost(combosGroup.getSelectedHost().getDatabaseConnection());

        tableCombo.removeAllItems();
        tableCombo.addItem(bundledString("SelectTable"));
        for (String tableName : dbHost.getTableNames())
            tableCombo.addItem(tableName);

        dbHost.close();

    }

    private void tableComboChanged() {

        if (fillMappingTableAllowed(false))
            fillMappingTable();

    }

    private void readDataPropsChanged() {

        if (pathToFile != null)
            readFileForPreview();

        if (fillMappingTableAllowed(false))
            fillMappingTable();

    }

    // ---------------------------------------------
    // Reading file for preview
    // ---------------------------------------------

    private void readCSVFileForPreview() {

        try {

            String[] readData = setSourceHeadersFromCSV();

            // ----------- erasing dataPreviewTableModel -----------

            dataPreviewTableModel.setColumnCount(0);
            dataPreviewTableModel.setRowCount(0);

            // ----------- generating columns dataPreviewTableModel -----------

            for (String tempHeader : headersOfSourceArray)
                dataPreviewTableModel.addColumn(tempHeader);
            for (String tempHeader : headersOfSourceArray)
                dataPreviewTable.getColumn(tempHeader).setMinWidth(MIN_COLUMN_WIDTH);

            if (headersOfSourceArray.size() < 6)
                dataPreviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            else
                dataPreviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            // ----------- filling dataPreviewTableModel -----------

            for (int i = 0; i < PREVIEW_ROWS_COUNT; i++) {

                if (readData[i] == null)
                    continue;

                dataPreviewTableModel.addRow(readData[i].split(
                        Objects.requireNonNull(delimiterCombo.getSelectedItem()).toString()));
            }

        } catch (Exception e) {

            GUIUtilities.displayWarningMessage(
                    bundledString("FileReadingErrorMessage") + "\n" + e.getMessage());
        }

    }

    private String[] setSourceHeadersFromCSV() {

        if (headersOfSourceArray == null)
            headersOfSourceArray = new ArrayList<>();
        else
            headersOfSourceArray.clear();

        String[] readData = new String[PREVIEW_ROWS_COUNT];

        try {

            FileReader reader = new FileReader(pathToFile);
            Scanner scan = new Scanner(reader);

            String firstRowFromSource = "";

            for (int i = 0; i < PREVIEW_ROWS_COUNT; i++) {

                if (scan.hasNextLine()) {

                    String tempRow = scan.nextLine();

                    if (i == 0) {

                        firstRowFromSource = tempRow;

                        if (isFirstColumnNames.isSelected())
                            tempRow = (scan.hasNextLine()) ? scan.nextLine() : "";

                    }

                    readData[i] = tempRow;

                } else
                    break;

            }

            reader.close();

            String[] firstRowFromSourceArray = firstRowFromSource.split(
                    Objects.requireNonNull(delimiterCombo.getSelectedItem()).toString());

            if (isFirstColumnNames.isSelected())
                headersOfSourceArray.addAll(Arrays.asList(firstRowFromSourceArray));
            else
                for (int i = 0; i < firstRowFromSourceArray.length; i++)
                    headersOfSourceArray.add("COLUMN" + (i + 1));

        } catch (FileNotFoundException e) {

            GUIUtilities.displayWarningMessage(
                    bundledString("FileDoesNotExistMessage") + "\n" + fileNameField.getText());

        } catch (Exception e) {

            GUIUtilities.displayWarningMessage(
                    bundledString("FileReadingErrorMessage") + "\n" + e.getMessage());
        }

        return readData;

    }

    // ---------------------------------------------
    // Reading file for executing SQL-INSERT request
    // ---------------------------------------------

    private Statement readCSVFile() {

        Log.info("ImportDataFromFilePanel: start reading CSV file...");

        Connection connection;
        Statement statement;

        sourceFileName = FilenameUtils.getBaseName(pathToFile);
        String directoryOfFile = Paths.get(pathToFile).getParent().toString();

        try {

            Properties properties = new Properties();

            // ----------- columns headers propriety -----------

            if (!isFirstColumnNames.isSelected()) {
                properties.setProperty("suppressHeaders", "true");
                properties.setProperty("defectiveHeaders", "true");
            }

            // ----------- separator propriety -----------

            properties.setProperty("separator", Objects.requireNonNull(delimiterCombo.getSelectedItem()).toString());

            // ----------- open source connection -----------

            String connectionUrl = "jdbc:relique:csv:";
            if (UIUtils.isWindows())
                connectionUrl += directoryOfFile.replace("\\", "/");
            else
                connectionUrl += directoryOfFile;

            connection = DriverManager.getConnection(connectionUrl, properties);
            statement = connection.createStatement();

            Log.info("ImportDataFromFilePanel: CSV file was read successfully");
            return statement;

        } catch (Exception e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);

            Log.error("ImportDataFromFilePanel: reading CSV file was stopped due to an error", e);

            return null;
        }

    }

    // ---------------------------------------------
    // Check Permissions
    // ---------------------------------------------

    public boolean fillMappingTableAllowed(boolean returnMessages) {

        if (headersOfSourceArray == null) {
            if (returnMessages)
                GUIUtilities.displayWarningMessage(bundledString("ReadFileMessage"));
            return false;
        }

        if (tableCombo.getSelectedIndex() == 0) {
            if (returnMessages)
                GUIUtilities.displayWarningMessage(bundledString("SelectTableMessage"));
            return false;
        }

        if ((delimiterCombo.getSelectedIndex() == 0) && (sourceCombo.getSelectedItem() == "csv")) {
            if (returnMessages)
                GUIUtilities.displayWarningMessage(bundledString("SelectDelimiterMessage"));
            return false;
        }

        return true;
    }

    private boolean startImportAllowed() {

        if (!fillMappingTableAllowed(true))
            return false;

        if (isEraseDatabase.isSelected())
            eraseDatabaseTable();

        return true;
    }

    // ---------------------------------------------
    // Method for clearing DB table before import
    // ---------------------------------------------

    private void eraseDatabaseTable() {

        DefaultStatementExecutor executor = new DefaultStatementExecutor();

        try {

            executor.setCommitMode(false);
            executor.setKeepAlive(true);
            executor.setDatabaseConnection(combosGroup.getSelectedHost().getDatabaseConnection());

            String deletePattern = "DELETE FROM " + tableCombo.getSelectedItem() + ";";

            outputPanel.appendAction("Erasing the database table...");
            Log.info("ImportDataFromFilePanel: erasing target table process started...");

            executor.execute(QueryTypes.DELETE, deletePattern);
            executor.getConnection().commit();

            outputPanel.append("The database table was successfully erased");
            Log.info("ImportDataFromFilePanel: erasing target table process has been completed");


        } catch (Exception e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("EraseDatabaseErrorMessage") + "\n" + e.getMessage(), e);

            outputPanel.appendError("Erasing the database table was stopped due to an error");
            Log.error("ImportDataFromFilePanel: erasing target table was stopped due to an error", e);

        } finally {

            executor.releaseResources();
        }

    }

    // ---------------------------------------------
    // ConnectionListener implementation
    // ---------------------------------------------

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof DefaultKeywordEvent) || (event instanceof ConnectionEvent);
    }

    public void connected(ConnectionEvent connectionEvent) {
        combosGroup.connectionOpened(connectionEvent.getDatabaseConnection());
    }

    public void disconnected(ConnectionEvent connectionEvent) {
        combosGroup.connectionClosed(connectionEvent.getDatabaseConnection());
    }

    // ---------------------------------------------
    // Closing the panel
    // ---------------------------------------------

    public boolean tabViewClosing() {

        cleanup();
        return true;
    }

    public void cleanup() {

        combosGroup.close();
        EventMediator.deregisterListener(this);
    }

    // ---------------------------------------------
    // Title name refactor by count
    // ---------------------------------------------

    private static int count = 1;

    public String getDisplayName() {
        return TITLE + (count++);
    }

    public String toString() {
        return getDisplayName();
    }

    // ---------------------------------------------
    // Localization settings
    // ---------------------------------------------

    public static String bundledString(String key) {
        return Bundles.get(ImportDataFromFilePanel.class, key);
    }

}






