package org.executequery.gui;

import org.apache.commons.io.FilenameUtils;
import org.executequery.EventMediator;
import org.executequery.base.DefaultTabViewActionPanel;
import org.executequery.components.FileChooserDialog;
import org.executequery.GUIUtilities;
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
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;

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
    private TableSelectionCombosGroup combosGroup; //connections, that contains comboBox
    private JComboBox tableCombo;   //target table
    private JComboBox sourceCombo;  //source type
    private JTextField fileNameField;   //path to importing file
    private JComboBox delimiterCombo;   //delimiter type
    private JCheckBox isFirstColumnNames;  //check whether the first row stores column names
    private JTextArea dataPreviewTextArea;  //preview of source data

    // ----- table for columns mapping -----

    private  DefaultTableModel columnMappingTableModel;
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
    private ArrayList<String> headersOfSourceTable;

    public ImportDataFromFilePanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {

        String[] delimiters = {bundledString("SelectDelimiter"), "|", ",", ";", "#"};
        String[] sourceTypes = {bundledString("SelectFileType"), "csv", "xlsx", "json"};

        delimiterCombo = WidgetFactory.createComboBox(delimiters);
        sourceCombo = WidgetFactory.createComboBox(sourceTypes);
        fileNameField = WidgetFactory.createTextField();
        isFirstColumnNames = new JCheckBox(bundledString("IsFirstColumnNamesText"));

        connectionsCombo = WidgetFactory.createComboBox();
        connectionsCombo.addActionListener(e -> connectionComboChanged());

        combosGroup = new TableSelectionCombosGroup(connectionsCombo);

        tableCombo = WidgetFactory.createComboBox();
        tableCombo.addActionListener(e -> tableComboChanged());

        connectionComboChanged();

        dataPreviewTextArea = new JTextArea();
        dataPreviewTextArea.setEditable(false);
        dataPreviewTextArea.setBorder(BorderFactory.createEtchedBorder());

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
                bundledString("FillMappingTableButtonText"),"fillMappingTable");

        startImportButton = new MinimumWidthActionButton(
                85, this,
                bundledString("StartImportButtonText"), "startImport");

        componentsArranging();

    }

    private void componentsArranging() {

        JScrollPane dataPreviewTextAreaWithScrolls = new JScrollPane(dataPreviewTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JScrollPane columnMappingTableWithScrolls = new JScrollPane(columnMappingTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        FlatSplitPane splitPane = new FlatSplitPane(
                JSplitPane.VERTICAL_SPLIT, dataPreviewTextAreaWithScrolls, columnMappingTableWithScrolls);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        splitPane.setDividerSize(5);

        // ---------------------------------------------
        // Components arranging
        // ---------------------------------------------

        JPanel mainPanel = new JPanel(new GridBagLayout()); //main panel of this view

        GridBagHelper gridBagHelper = new GridBagHelper();    //GridBagConstraints helper
        gridBagHelper.setInsets(5,5,5,5);   //margins
        gridBagHelper.setHeight(1);   //number of cells in the column for the component display
        gridBagHelper.anchorNorthWest();    //set upper-left displays corner

        // ----------- first row -----------

        mainPanel.add(new JLabel(bundledString("TargetConnectionLabel")), gridBagHelper.get());

        gridBagHelper.nextCol();
        gridBagHelper.spanX();
        gridBagHelper.fillHorizontally();

        mainPanel.add(connectionsCombo, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();
        gridBagHelper.setMinWeightX();
        gridBagHelper.setWidth(1);

        // ----------- second row -----------

        mainPanel.add(new JLabel(bundledString("TargetTableLabel")), gridBagHelper.get());

        gridBagHelper.nextCol();
        gridBagHelper.spanX();
        gridBagHelper.fillHorizontally();

        mainPanel.add(tableCombo, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();
        gridBagHelper.setMinWeightX();
        gridBagHelper.setWidth(1);

        // ----------- third row -----------

        mainPanel.add(new JLabel(bundledString("SourceTypeLabel")), gridBagHelper.get());

        gridBagHelper.nextCol();
        gridBagHelper.spanX();
        gridBagHelper.fillHorizontally();

        mainPanel.add(sourceCombo, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();
        gridBagHelper.setMinWeightX();
        gridBagHelper.setWidth(1);

        // ----------- fourth row -----------

        mainPanel.add(new JLabel(bundledString("InputDataFileLabel")), gridBagHelper.get());

        gridBagHelper.nextCol();
        gridBagHelper.setWeightX(1.0);
        gridBagHelper.fillHorizontally();

        mainPanel.add(fileNameField, gridBagHelper.get());

        gridBagHelper.nextCol();
        gridBagHelper.setMinWeightX();
        gridBagHelper.fillHorizontally();

        mainPanel.add(browseButton, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();
        gridBagHelper.setMinWeightX();
        gridBagHelper.setWidth(1);

        // ----------- fifth row -----------

        mainPanel.add(new JLabel(bundledString("DataDelimiterLabel")), gridBagHelper.get());

        gridBagHelper.nextCol();
        gridBagHelper.spanX();
        gridBagHelper.fillHorizontally();

        mainPanel.add(delimiterCombo, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();
        gridBagHelper.setMinWeightX();
        gridBagHelper.setWidth(1);

        // ----------- sixth row -----------

        mainPanel.add(isFirstColumnNames, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();

        // ----------- seventh row -----------

        mainPanel.add(new JLabel(bundledString("FilePreviewLabel")), gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();

        // ----------- eighth row -----------

        gridBagHelper.spanX();
        gridBagHelper.spanY();
        gridBagHelper.fillBoth();

        mainPanel.add(splitPane, gridBagHelper.get());

        // ---------------------------------------------
        // Panels Settings
        // ---------------------------------------------

        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 5));
        buttonPanel.add(readFileButton);
        buttonPanel.add(fillMappingTableButton);
        buttonPanel.add(startImportButton);

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
                    return;

                } catch (IOException e) {

                    GUIUtilities.displayWarningMessage(
                            bundledString("FileReadingErrorMessage") + "\n" + e.getMessage());
                    return;
                }

                break;

            } case ("xlsx"): {

                GUIUtilities.displayWarningMessage("Import from xlsx is currently unavailable");
                return;

            } case ("json"): {

                GUIUtilities.displayWarningMessage("Import from json is currently unavailable");
                return;
            }

        }

    }

    public void fillMappingTable() {

        if (!fillMappingTableAllowed(true)){

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

    }

    public void startImport() {

        if (!startImportAllowed()) {

            return;
        }

        // ---------------------------------------------
        // Preparing SQL-INSERT pattern
        // ---------------------------------------------

        DefaultStatementExecutor executor = new DefaultStatementExecutor();
        executor.setDatabaseConnection(combosGroup.getSelectedHost().getDatabaseConnection());

        PreparedStatement insertStatement;

        StringBuilder targetColumnList = new StringBuilder();
        StringBuilder sourceColumnList = new StringBuilder();

        Vector<Vector> dataFromTableVector = columnMappingTableModel.getDataVector();
        int valuesCount = 0;

        for (int i = 0; i < dataFromTableVector.size(); i++) {

            if (dataFromTableVector.get(i).get(2) != "Nothing") {

                targetColumnList.append(dataFromTableVector.get(i).get(0));
                sourceColumnList.append(dataFromTableVector.get(i).get(2));

                if (i != dataFromTableVector.size() - 1) {
                    targetColumnList.append(",");
                    sourceColumnList.append(",");
                }

                valuesCount++;

            }

        }

        StringBuilder insertPattern = new StringBuilder();
        insertPattern.append("INSERT INTO ");
        insertPattern.append(tableCombo.getSelectedItem());
        insertPattern.append(" (");
        insertPattern.append(targetColumnList);
        insertPattern.append(") VALUES (");

        int tempValuesCount = valuesCount;
        while (tempValuesCount > 1) {

            insertPattern.append("?,");
            tempValuesCount--;
        }
        insertPattern.append("?);");

        try {

            insertStatement = executor.getPreparedStatement(insertPattern.toString());

        } catch(SQLException e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);
            return;
        }

        // ---------------------------------------------
        // Executing SQL-INSERT request depending on source file type
        // ---------------------------------------------

        switch (Objects.requireNonNull(sourceCombo.getSelectedItem()).toString()) {

            case ("csv"): {

                try {

                    Statement sourceFileStatement = readCSVFile();

                    if (sourceFileStatement == null) {

                        return;
                    }

                    ResultSet sourceFileData = sourceFileStatement.executeQuery(
                            "SELECT " + sourceColumnList + " FROM " + sourceFileName);

                    while (sourceFileData.next()) {

                        for (int j = 0; j < valuesCount; j++) {

                            Object param = sourceFileData.getString(j + 1);
                            insertStatement.setObject(j + 1, param);

                        }

                        executor.execute(QueryTypes.INSERT, insertStatement);

                    }

                } catch (SQLException e) {

                    GUIUtilities.displayExceptionErrorDialog(
                            bundledString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);
                    return;
                }

                dataPreviewTextArea.setText(bundledString("ImportedSuccessfullyText"));
                break;

            } case ("xlsx"): {

                GUIUtilities.displayWarningMessage("Import from xlsx is currently unavailable");
                return;

            } case ("json"): {

                GUIUtilities.displayWarningMessage("Import from json is currently unavailable");
                return;

            }

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

    }

    private void tableComboChanged() {

        if (fillMappingTableAllowed(false)) {

            fillMappingTable();
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

    }

    private JComboBox createComboBoxMappingTable() {

        JComboBox tableColumnComboBox = new JComboBox();
        tableColumnComboBox.addItem("Nothing");

        headersOfSourceTable = new ArrayList<>();

        String[] firstRowFromSource = firstSourceRow.split(
                Objects.requireNonNull(delimiterCombo.getSelectedItem()).toString());

        int sourceColumnsCount = firstRowFromSource.length;

        if (isFirstColumnNames.isSelected()){

            for (String tempHeaderName : firstRowFromSource) {

                tableColumnComboBox.addItem(tempHeaderName);
                headersOfSourceTable.add(tempHeaderName);
            }

        } else {

            for (int i = 1; i < sourceColumnsCount + 1; i++) {

                String tempHeaderName = "Column " + i;

                tableColumnComboBox.addItem(tempHeaderName);
                headersOfSourceTable.add(tempHeaderName);
            }
        }

        return tableColumnComboBox;
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
            properties.put("headerline", headersOfSourceTable);

            connection = DriverManager.getConnection("jdbc:relique:csv:/" + directoryOfFile);
            statement = connection.createStatement();

            return statement;

        } catch (SQLException e) {

            GUIUtilities.displayWarningMessage(
                    bundledString("ImportDataErrorMessage") + "\n" + e.getMessage());
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
        if (delimiterCombo.getSelectedIndex() == 0) {

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

        Vector<Vector> tableData = columnMappingTableModel.getDataVector();

        for (int i = 0; i < tableData.size(); i++){

            if (tableData.get(i).get(2) == null) {

                GUIUtilities.displayWarningMessage(
                        bundledString("NoSourceSelectedMessage") + (i + 1));
                return false;
            }
        }

        return true;
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







