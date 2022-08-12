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
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
        implements NamedView,
        ConnectionListener {

    public static final String TITLE = bundledString("Title");
    public static final String FRAME_ICON = "ImportDelimited16.png";

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
    private JTextArea dataPreviewTextArea;  //preview of source data
    private LoggingOutputPanel outputPanel; //logging module executing
    private JSpinner firstImportSelector;   //numeric selector of first importing line
    private JSpinner lastImportSelector;    //numeric selector of last importing line
    private JSpinner batchStepSelector; //numeric selector of batch commit step

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
    private String firstSourceRow;
    private String sourceFileName;
    private ArrayList<String> headersOfSourceArray;
    private String nothingHeaderOfMappingTable;

    public ImportDataFromFilePanel() {
        super(new BorderLayout());
        init();

        Log.info("ImportDataFromFilePanel: class was created");
    }

    private void init() {

        String[] delimiters = {bundledString("SelectDelimiter"),
                "|", ",", ";", "#"};

        String[] sourceTypes = {bundledString("SelectFileType"),
                "csv", "xlsx", "json"};

        String[] timeFormats = {bundledString("SelectTimeFormat"),
                "HH:mm", "HH:mm:ss", "HH:mm:ss[.nnn]"};

        String[] dateFormats = {bundledString("SelectDateFormat"),
                "dd.MM.yyyy", "yyyy-MM-dd", "MM/dd/yyyy", "yyyyMMdd"};

        String[] timestampDelimiters = {bundledString("SelectTimestampDelimiterFormat"),
                "\"spase\"", "_", "T"};

        nothingHeaderOfMappingTable = "NOTHING";

        // ---------- comboBoxes ----------

        delimiterCombo = WidgetFactory.createComboBox(delimiters);
        delimiterCombo.setEditable(true);

        sourceCombo = WidgetFactory.createComboBox(sourceTypes);
        sourceCombo.addActionListener(e -> sourceComboChanged());

        timeFormatCombo = WidgetFactory.createComboBox(timeFormats);
        timeFormatCombo.setEditable(true);

        dateFormatCombo = WidgetFactory.createComboBox(dateFormats);
        dateFormatCombo.setEditable(true);

        timestampDelimiterCombo = WidgetFactory.createComboBox(timestampDelimiters);
        timestampDelimiterCombo.setEditable(true);

        connectionsCombo = WidgetFactory.createComboBox();
        connectionsCombo.addActionListener(e -> connectionComboChanged());

        combosGroup = new TableSelectionCombosGroup(connectionsCombo);

        tableCombo = WidgetFactory.createComboBox();
        tableCombo.addActionListener(e -> tableComboChanged());

        // ---------- file path field ----------

        fileNameField = WidgetFactory.createTextField();

        // ---------- checkBoxes ----------

        isFirstColumnNames = new JCheckBox(bundledString("IsFirstColumnNamesText"));
        isEraseDatabase = new JCheckBox(bundledString("IsEraseDatabaseText"));

        // ---------- numeric selectors ----------

        firstImportSelector = new JSpinner(new SpinnerNumberModel(0, 0, 999999999, 1));
        lastImportSelector = new JSpinner(new SpinnerNumberModel(999999999, 0, 999999999, 1));
        batchStepSelector = new JSpinner(new SpinnerNumberModel(100, 100, 1000000, 100));

        // ---------- data preview table ----------

        dataPreviewTextArea = new JTextArea();
        dataPreviewTextArea.setEditable(false);
        dataPreviewTextArea.setBorder(BorderFactory.createEtchedBorder());

        // ---------- output panel & mapping table ----------

        outputPanel = new LoggingOutputPanel();

        createMappingTableModel();
        columnMappingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // ---------------------------------------------
        // Buttons defining
        // ---------------------------------------------

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
        sourceComboChanged();

        componentsArranging();

    }

    private void componentsArranging() {

        // ---------------------------------------------
        // Preparing ScrollPanes and FlatSplitPanes
        // ---------------------------------------------

        JScrollPane dataPreviewTextAreaWithScrolls = new JScrollPane(dataPreviewTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JScrollPane columnMappingTableWithScrolls = new JScrollPane(columnMappingTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        FlatSplitPane splitPane = new FlatSplitPane(
                JSplitPane.VERTICAL_SPLIT, dataPreviewTextAreaWithScrolls, columnMappingTableWithScrolls);
        splitPane.setResizeWeight(0.5);

        FlatSplitPane mainSplitPane = new FlatSplitPane(
                JSplitPane.VERTICAL_SPLIT, splitPane, outputPanel);
        mainSplitPane.setResizeWeight(0.7);

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
                null, true, false, 3);

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("TargetTableLabel"), tableCombo,
                null, false, true, 3);

        // ----------- second row -----------

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("InputDataFileLabel"), sourceCombo,
                null, true, false, 1);

        mainPanel.add(fileNameField, gridBagHelper.nextCol().fillHorizontally().setWidth(5).get());
        mainPanel.add(browseButton, gridBagHelper.nextCol().setLabelDefault().get());

        // ----------- third row -----------

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("DateFormatLabel"), dateFormatCombo,
                null, true, false, 1);

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("TimeFormatLabel"), timeFormatCombo,
                null, false, false, 1);

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("TimestampDelimiterLabel"), timestampDelimiterCombo,
                null, false, true, 1);

        // ----------- fourth row -----------

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("DelimiterLabel"), delimiterCombo,
                null, true, false, 1);

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("CommitSelectorLabel"), batchStepSelector,
                null, false, false, 1);

        mainPanel.add(isEraseDatabase, gridBagHelper.nextCol().setWidth(2).get());

        // ----------- fifth row -----------

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("FirstNumericSelectorsLabel"), firstImportSelector,
                null, true, false, 1);

        gridBagHelper.addLabelFieldPair(mainPanel,
                bundledString("LastNumericSelectorsLabel"), lastImportSelector,
                null, false, false, 1);

        mainPanel.add(isFirstColumnNames, gridBagHelper.nextCol().setWidth(2).get());

        // ----------- sixth row -----------

        mainPanel.add(mainSplitPane, gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().get());

        // ----------- buttonPanel -----------

        buttonPanel.add(readFileButton);
        buttonPanel.add(fillMappingTableButton);
        buttonPanel.add(startImportButton);

        // ---------------------------------------------
        // Panels Settings
        // ---------------------------------------------

        mainPanel.setBorder(BorderFactory.createEtchedBorder());

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
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();
        if (!file.exists()) {

            GUIUtilities.displayWarningMessage(
                    bundledString("FileDoesNotExistMessage") + "\n" + fileNameField.getText());
            return;

        }

        fileNameField.setText(file.getAbsolutePath());
    }

    public void readFileForPreview() {

        String pathToFile = fileNameField.getText();

        if (pathToFile.isEmpty()) {

            GUIUtilities.displayWarningMessage(bundledString("NoFileSelectedMessage"));
            return;
        }

        switch (Objects.requireNonNull(sourceCombo.getSelectedItem()).toString()) {

            case ("csv"): {

                readCSVFileForPreview(pathToFile);
                break;

            }
            case ("xlsx"): {

                GUIUtilities.displayWarningMessage("Import from xlsx is currently unavailable");
                break;

            }
            case ("json"): {

                GUIUtilities.displayWarningMessage("Import from json is currently unavailable");
                break;

            }
            default: {

                GUIUtilities.displayWarningMessage(bundledString("NoFileTypeSelectedMessage"));
            }

        }

    }

    public void fillMappingTable() {

        if (!fillMappingTableAllowed(true)) {

            return;
        }

        int tempRowsCount = columnMappingTable.getRowCount();
        while (tempRowsCount != 0) {

            columnMappingTableModel.removeRow(0);
            tempRowsCount--;
        }

        JComboBox sourceComboBox = createComboBoxMappingTable();
        TableColumn thirdColumn = columnMappingTable.getColumnModel().getColumn(2);
        thirdColumn.setCellEditor(new DefaultCellEditor(sourceComboBox));

        List<DatabaseColumn> dbHostTableColumns = dbHost.getColumns(
                null, null, Objects.requireNonNull(tableCombo.getSelectedItem()).toString());

        for (DatabaseColumn dbHostTableColumn : dbHostTableColumns) {

            columnMappingTableModel.addRow(new Object[]{
                    dbHostTableColumn.getName(),
                    dbHostTableColumn.getTypeName()});
        }

        Log.info("ImportDataFromFilePanel: mapping table was filled in");
    }

    public void startImport() {

        if (!startImportAllowed()) {

            return;
        }

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

            if (dataFromTableVector.get(i).get(2) != nothingHeaderOfMappingTable) {

                targetColumnList.append(dataFromTableVector.get(i).get(0));
                sourceColumnList.append(dataFromTableVector.get(i).get(2));

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
        insertPattern.append(tableCombo.getSelectedItem());
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

                insertCSV(sourceColumnList, valuesIndexes, insertStatement, executor);
                break;

            }
            case ("xlsx"): {

                GUIUtilities.displayWarningMessage("Import from xlsx is currently unavailable");
                break;

            }
            case ("json"): {

                GUIUtilities.displayWarningMessage("Import from json is currently unavailable");
                break;

            }
            default: {

                GUIUtilities.displayWarningMessage(bundledString("NoFileTypeSelectedMessage"));
                Log.info("ImportDataFromFilePanel: import process stopped (no source file type selected)");
            }

        }

        executor.releaseResources();

    }

    // ---------------------------------------------
    // Executing SQL-INSERT request methods
    // ---------------------------------------------

    private void insertCSV(StringBuilder sourceColumnList, boolean[] valuesIndexes,
                           PreparedStatement insertStatement, DefaultStatementExecutor executor) {

        try {

            outputPanel.appendAction("Executing SQL-INSERT request...");

            Statement sourceFileStatement = readCSVFile();

            if (sourceFileStatement == null) {

                return;
            }

            String SQLSourceRequest = "SELECT " + sourceColumnList + " FROM " + sourceFileName;
            ResultSet sourceFileData = sourceFileStatement.executeQuery(SQLSourceRequest);

            String[] sourceFields = sourceColumnList.toString().split(",");

            long startTime = System.currentTimeMillis();

            int executorIndex = 0;
            int linesCount = 0;

            while (sourceFileData.next()) {

                if (linesCount >= (int) lastImportSelector.getValue()) {

                    break;
                }

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
                                            Objects.requireNonNull(timestampDelimiterCombo.getSelectedItem())
                                                    .toString().replace("\"spase\"", " ") +
                                            Objects.requireNonNull(timeFormatCombo.getSelectedItem());

                                    timestampFormat = DateTimeFormatter.
                                            ofPattern(Objects.requireNonNull(timestampPattern));

                                } else {

                                    GUIUtilities.displayWarningMessage(bundledString("NoTimeFormatSelectedMessage"));
                                    outputPanel.appendError("Importing data stopped: no date/time format selected");
                                    Log.info("ImportDataFromFilePanel: import process stopped (no date/time format selected)");

                                    return;
                                }

                                String stringTimestamp = sourceFileData.getString(sourceFields[fieldIndex]);

                                param = LocalDateTime.parse(stringTimestamp, timestampFormat);

                            } else {

                                param = sourceFileData.getString(sourceFields[fieldIndex]);
                            }

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
        for (String tableName : dbHost.getTableNames(
                null, null, NamedObject.META_TYPES[NamedObject.TABLE])) {

            tableCombo.addItem(tableName);
        }

        dbHost.close();

    }

    private void tableComboChanged() {

        if (fillMappingTableAllowed(false)) {

            fillMappingTable();
        }

    }

    private void sourceComboChanged() {

        if (sourceCombo.getSelectedIndex() == 0) {

            fileNameField.setEnabled(false);

            delimiterCombo.setEnabled(false);
            timeFormatCombo.setEnabled(false);
            dateFormatCombo.setEnabled(false);
            timestampDelimiterCombo.setEnabled(false);

            firstImportSelector.setEnabled(false);
            lastImportSelector.setEnabled(false);
            batchStepSelector.setEnabled(false);

            isEraseDatabase.setEnabled(false);
            isFirstColumnNames.setEnabled(false);

            browseButton.setEnabled(false);
            readFileButton.setEnabled(false);
            fillMappingTableButton.setEnabled(false);
            startImportButton.setEnabled(false);

        }

        if (sourceCombo.getSelectedItem() == "csv") {

            fileNameField.setEnabled(true);

            delimiterCombo.setEnabled(true);
            timeFormatCombo.setEnabled(true);
            dateFormatCombo.setEnabled(true);
            timestampDelimiterCombo.setEnabled(true);

            firstImportSelector.setEnabled(true);
            lastImportSelector.setEnabled(true);
            batchStepSelector.setEnabled(true);

            isEraseDatabase.setEnabled(true);
            isFirstColumnNames.setEnabled(true);

            browseButton.setEnabled(true);
            readFileButton.setEnabled(true);
            fillMappingTableButton.setEnabled(true);
            startImportButton.setEnabled(true);

        }

    }

    // ---------------------------------------------
    // Generating and Filling Mapping Table
    // ---------------------------------------------

    private void createMappingTableModel() {

        columnMappingTableModel = new DefaultTableModel();
        columnMappingTable = new JTable(columnMappingTableModel);

        columnMappingTableModel.addColumn(bundledString("ColumnMappingTableTargetC"));
        columnMappingTableModel.addColumn(bundledString("ColumnMappingTableTargetT"));
        columnMappingTableModel.addColumn(bundledString("ColumnMappingTableSource"));

        Log.info("ImportDataFromFilePanel: mapping table was (re)created");

    }

    private JComboBox createComboBoxMappingTable() {

        JComboBox tableColumnComboBox = new JComboBox();
        tableColumnComboBox.addItem(nothingHeaderOfMappingTable);

        headersOfSourceArray = new ArrayList<>();

        String[] firstRowFromSource = firstSourceRow.split(
                Objects.requireNonNull(delimiterCombo.getSelectedItem()).toString());

        if (isFirstColumnNames.isSelected()){

            for (String tempHeaderName : firstRowFromSource) {

                tableColumnComboBox.addItem(tempHeaderName);
                headersOfSourceArray.add(tempHeaderName);
            }

        } else {

            for (int i = 0; i < firstRowFromSource.length ; i++) {

                String tempHeaderName = "COLUMN_" + (i + 1);

                tableColumnComboBox.addItem(tempHeaderName);
                headersOfSourceArray.add(tempHeaderName);
            }
        }

        return tableColumnComboBox;

    }

    // ---------------------------------------------
    // Reading file for preview
    // ---------------------------------------------

    private void readCSVFileForPreview(String pathToFile) {

        try {

            String readData = bundledString("FilePreviewText") + "\n";

            FileReader reader = new FileReader(pathToFile);
            Scanner scan = new Scanner(reader);

            for (int i = 0; i < 10; i++) {

                if (scan.hasNextLine()) {

                    String tempRow = scan.nextLine();
                    readData += tempRow + "\n";

                    if (i == 0) {

                        firstSourceRow = tempRow;
                    }

                } else {

                    break;
                }

            }

            reader.close();
            dataPreviewTextArea.setText(readData);

        } catch (FileNotFoundException e) {

            GUIUtilities.displayWarningMessage(
                    bundledString("FileDoesNotExistMessage") + "\n" + fileNameField.getText());

        } catch (Exception e) {

            GUIUtilities.displayWarningMessage(
                    bundledString("FileReadingErrorMessage") + "\n" + e.getMessage());
        }

    }

    // ---------------------------------------------
    // Reading file for executing SQL-INSERT request
    // ---------------------------------------------

    private Statement readCSVFile() {

        Connection connection;
        Statement statement;

        String pathToFile = fileNameField.getText();
        sourceFileName = FilenameUtils.getBaseName(pathToFile);
        String directoryOfFile = FilenameUtils.getPath(pathToFile);

        try {

            Properties properties = new Properties();
            int headersArraySize = headersOfSourceArray.size();

            // ----------- columns headers propriety -----------

            if (!isFirstColumnNames.isSelected()) {

                StringBuilder headerPropriety = new StringBuilder();

                for (int i = 0; i < headersArraySize - 1; i++) {

                    headerPropriety.append(headersOfSourceArray.get(i)).append(",");
                }
                headerPropriety.append(headersOfSourceArray.get(headersArraySize - 1));

                properties.setProperty("suppressHeaders", "true");
                properties.setProperty("headerline", headerPropriety.toString());

            }

            // ----------- separator propriety -----------

            properties.setProperty("separator", Objects.requireNonNull(delimiterCombo.getSelectedItem()).toString());

            // ----------- open source connection -----------

            connection = DriverManager.getConnection("jdbc:relique:csv:/" +
                    directoryOfFile.replace("\\", "/"), properties);

            statement = connection.createStatement();

            return statement;

        } catch (Exception e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);

            Log.error("ImportDataFromFilePanel: import process was stopped due to an error", e);

            return null;

        }

    }

    // ---------------------------------------------
    // Check Permissions
    // ---------------------------------------------

    public boolean fillMappingTableAllowed(boolean returnMessages) {

        if (firstSourceRow == null) {

            if (returnMessages) {
                GUIUtilities.displayWarningMessage(
                        bundledString("ReadFileMessage"));
            }

            return false;
        }
        if (tableCombo.getSelectedIndex() == 0) {

            if (returnMessages) {
                GUIUtilities.displayWarningMessage(
                        bundledString("SelectTableMessage"));
            }

            return false;
        }
        if ((delimiterCombo.getSelectedIndex() == 0) && (sourceCombo.getSelectedItem() == "csv")) {

            if (returnMessages) {
                GUIUtilities.displayWarningMessage(
                        bundledString("SelectDelimiterMessage"));
            }

            return false;
        }

        return true;
    }

    private boolean startImportAllowed() {

        if (!fillMappingTableAllowed(true)) {
            return false;
        }

        if (isEraseDatabase.isSelected()) {

            eraseDatabaseTable();
        }

        Vector<Vector> tableData = columnMappingTableModel.getDataVector();

        for (int i = 0; i < tableData.size(); i++) {

            if (tableData.get(i).get(2) == null) {

                GUIUtilities.displayWarningMessage(
                        bundledString("NoSourceSelectedMessage") + (i + 1));
                return false;
            }
        }

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






