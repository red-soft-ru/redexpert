package org.executequery.gui;

import biz.redsoft.IFBBatch;
import biz.redsoft.IFBBatchCompletionState;
import biz.redsoft.IFBDatabaseConnection;
import org.executequery.EventMediator;
import org.executequery.base.DefaultTabViewActionPanel;
import org.executequery.components.FileChooserDialog;
import org.executequery.GUIUtilities;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.event.DefaultKeywordEvent;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

/**
 * @author Alexey Kozlov
 */

public class ImportDataFromFilePanel extends DefaultTabViewActionPanel
        implements NamedView,
        ConnectionListener {

    public static final String TITLE = "Import data ";
    public static final String FRAME_ICON = "ImportDelimited16.png";

    private JComboBox connectionsCombo; //target database
    private TableSelectionCombosGroup combosGroup; //connections, that contains comboBox
    private JComboBox tableCombo;   //target table
    private JComboBox sourceCombo;  //source type
    private JTextField fileNameField;   //path to importing file
    private JComboBox delimiterCombo;   //delimiter type
    private JCheckBox is_firstColumnNames;  //check whether the first row stores column names
    private JTextArea dataPreviewTextArea;  //preview of source data

    // ----- table for columns mapping -----
    private  DefaultTableModel columnMappingTableModel;
    private JTable columnMappingTable;
    // -----

    // ----- buttons -----
    private JButton browseButton;
    private JButton readFileButton;
    private JButton fillMappingTableButton;
    private JButton startImportButton;
    // -----

    private DefaultDatabaseHost dbHost; //database host
    private ArrayList<String> sourceFileData; //data, that has been read from file

    public ImportDataFromFilePanel() {
        super(new BorderLayout());
        init();
    }
    private void init() {

        String[] delimiters = {"Select the delimiter", "|", ",", ";", "#"};
        String[] sourceTypes = {"Select the file type", "csv", "xlsx"};

        delimiterCombo = WidgetFactory.createComboBox(delimiters);
        sourceCombo = WidgetFactory.createComboBox(sourceTypes);
        fileNameField = WidgetFactory.createTextField();
        is_firstColumnNames = new JCheckBox("Columns names as first sources row");

        connectionsCombo = WidgetFactory.createComboBox();
        connectionsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectionComboChanged();
            }
        });

        combosGroup = new TableSelectionCombosGroup(connectionsCombo);

        tableCombo = WidgetFactory.createComboBox();
        tableCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableComboChanged();
            }
        });

        connectionComboChanged();

        dataPreviewTextArea = new JTextArea();
        dataPreviewTextArea.setEditable(false);
        dataPreviewTextArea.setBorder(BorderFactory.createEtchedBorder());

        createMappingTableModel();
        columnMappingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // ---------------------------------------------
        // Buttons defining
        // ---------------------------------------------

        browseButton = WidgetFactory.createInlineFieldButton("Browse");
        browseButton.setActionCommand("browse");
        browseButton.addActionListener(this);

        readFileButton = WidgetFactory.createInlineFieldButton("Read File");
        readFileButton.setActionCommand("readFile");
        readFileButton.addActionListener(this);

        fillMappingTableButton = WidgetFactory.createInlineFieldButton("Fill Mapping Table");
        fillMappingTableButton.setActionCommand("fillMappingTable");
        fillMappingTableButton.addActionListener(this);

        startImportButton = WidgetFactory.createInlineFieldButton("Start Import");
        startImportButton.setActionCommand("startImport");
        startImportButton.addActionListener(this);

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
        gridBagHelper.setInsets(5,5,5,5); //margins
        gridBagHelper.setHeight(1);   //number of cells in the column for the component display
        gridBagHelper.anchorNorthWest(); //set upper-left displays corner

        // ----------- first row -----------

        mainPanel.add(new JLabel("Target connection:"), gridBagHelper.get());

        gridBagHelper.nextCol();
        gridBagHelper.spanX();
        gridBagHelper.fillHorizontally();

        mainPanel.add(connectionsCombo, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();
        gridBagHelper.setMinWeightX();
        gridBagHelper.setWidth(1);

        // ----------- second row -----------

        mainPanel.add(new JLabel("Target table:"), gridBagHelper.get());

        gridBagHelper.nextCol();
        gridBagHelper.spanX();
        gridBagHelper.fillHorizontally();

        mainPanel.add(tableCombo, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();
        gridBagHelper.setMinWeightX();
        gridBagHelper.setWidth(1);

        // ----------- third row -----------

        mainPanel.add(new JLabel("Source type:"), gridBagHelper.get());

        gridBagHelper.nextCol();
        gridBagHelper.spanX();
        gridBagHelper.fillHorizontally();

        mainPanel.add(sourceCombo, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();
        gridBagHelper.setMinWeightX();
        gridBagHelper.setWidth(1);

        // ----------- fourth row -----------

        mainPanel.add(new JLabel("Input Data File:"), gridBagHelper.get());

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

        mainPanel.add(new JLabel("Data Delimiter:"), gridBagHelper.get());

        gridBagHelper.nextCol();
        gridBagHelper.spanX();
        gridBagHelper.fillHorizontally();

        mainPanel.add(delimiterCombo, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();
        gridBagHelper.setMinWeightX();
        gridBagHelper.setWidth(1);

        // ----------- sixth row -----------

        mainPanel.add(is_firstColumnNames, gridBagHelper.get());

        gridBagHelper.nextRowFirstCol();

        // ----------- seventh row -----------

        mainPanel.add(new JLabel("Imported file preview and columns mapping"), gridBagHelper.get());

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

            GUIUtilities.displayWarningMessage("Select file type firstly");
            return;
        }

        String validFileType = sourceCombo.getSelectedItem().toString();

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("Valid Files", validFileType));
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle("Select Import Data File Path");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();
        if (!file.exists()) {

            GUIUtilities.displayWarningMessage("The selected file does not exist.");
            return;

        }

        fileNameField.setText(file.getAbsolutePath());
    }

    public void readFile() {

        dataPreviewTextArea.setText("Reading file...");

        String readData = "";
        String pathToFile = fileNameField.getText();

        if (pathToFile.isEmpty()) {

            GUIUtilities.displayWarningMessage("No file selected.");
            return;
        }

        try {

            sourceFileData = new ArrayList<String>();

            String tempRow;
            FileReader reader = new FileReader(pathToFile);
            Scanner scan = new Scanner(reader);

            while (scan.hasNextLine()){

                tempRow = scan.nextLine();
                readData += tempRow + '\n';

                sourceFileData.add(tempRow);
            }

            reader.close();

            dataPreviewTextArea.setText(readData);

        } catch (FileNotFoundException e) {

            GUIUtilities.displayWarningMessage("The selected file does not exist.");
            return;

        } catch (IOException e) {

            GUIUtilities.displayWarningMessage("Something Error.");
            return;

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
                null, null, tableCombo.getSelectedItem().toString());

        for (int i = 0; i < dbHostTableColumns.size(); i++) {

            columnMappingTableModel.addRow(new Object[] {
                    dbHostTableColumns.get(i).getName(),
                    dbHostTableColumns.get(i).getTypeName()});
        }

    }

    private void startImport() {

        if (!startImportAllowed()) {

            GUIUtilities.displayWarningMessage("Something incorrect.");
            return;
        }

        StringBuilder targetColumnList = new StringBuilder();

        ArrayList<Integer> indexes = new ArrayList<Integer>();
        Vector<Vector> dataFromTable = columnMappingTableModel.getDataVector();

        for (int i = 0; i < dataFromTable.size(); i++) {

            if (dataFromTable.get(i).get(2) != "Nothing") {

                targetColumnList.append(dataFromTable.get(i).get(0));
                indexes.add(i);

                if (i != dataFromTable.size() - 1) {
                    targetColumnList.append(",");
                }

            }

        }

        StringBuilder insertPattern = new StringBuilder();
        insertPattern.append("INSERT INTO ");
        insertPattern.append(tableCombo.getName());
        insertPattern.append(" (");
        insertPattern.append(targetColumnList);
        insertPattern.append(") VALUES (");

        StringBuilder insertRow = new StringBuilder(insertPattern);
        for (int i = 0; i < sourceFileData.size(); i++) {

            String[] dataFromThisRow = sourceFileData.get(i).split(delimiterCombo.getSelectedItem().toString());

            for (int j = 0; j < dataFromThisRow.length; j++) {

                if (indexes.contains(j)) {

                    insertRow.append(dataFromThisRow[j]);

                    if (i != sourceFileData.size() - 1) {
                        insertRow.append(",");
                    }

                }

            }

            insertRow.append(");");

            // <<------------------------------------------------------------------ SQL INSERT
        }

    }

    // ---------------------------------------------
    // Event handlers for changing the state of some components
    // ---------------------------------------------

    private void connectionComboChanged() {

        dbHost = new DefaultDatabaseHost(combosGroup.getSelectedHost().getDatabaseConnection());
        List<NamedObject> dbHostTables = dbHost.getTables(
                null, null,
                NamedObject.META_TYPES[NamedObject.TABLE]);

        tableCombo.removeAllItems();
        tableCombo.addItem("Select the table");
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

        columnMappingTableModel.addColumn("Target column");
        columnMappingTableModel.addColumn("Target column type");
        columnMappingTableModel.addColumn("Source column");

    }

    private JComboBox createComboBoxMappingTable() {

        JComboBox tableColumnComboBox = new JComboBox();
        tableColumnComboBox.addItem("Nothing");


        String[] firstRowFromSource = sourceFileData.get(0).split(delimiterCombo.getSelectedItem().toString());
        int sourceColumnsCount = firstRowFromSource.length;

        if (is_firstColumnNames.isSelected()){

            for (int i = 0; i < sourceColumnsCount; i++) {

                tableColumnComboBox.addItem(firstRowFromSource[i]);
            }

        } else {

            for (int i = 1; i < sourceColumnsCount + 1; i++) {

                tableColumnComboBox.addItem(("Column " + i).toString());
            }
        }

        return tableColumnComboBox;
    }

    // ---------------------------------------------
    // Check Permissions
    // ---------------------------------------------

    public boolean fillMappingTableAllowed(boolean returnMessages) {

        if (sourceFileData == null) {

            if (returnMessages == true) {
                GUIUtilities.displayWarningMessage("Read source file firstly.");
            }

            return false;
        }
        if (tableCombo.getSelectedIndex() == 0) {

            if (returnMessages == true) {
                GUIUtilities.displayWarningMessage("Select the target table firstly.");
            }

            return false;
        }
        if (delimiterCombo.getSelectedIndex() == 0) {

            if (returnMessages == true) {
                GUIUtilities.displayWarningMessage("Select the delimiter firstly.");
            }

            return false;
        }

        return true;
    }

    private boolean startImportAllowed() {

        if (!fillMappingTableAllowed(false)) {
            return false;
        }

        Vector<Vector> tableData = columnMappingTableModel.getDataVector();

        for (int i = 0; i < tableData.size(); i++){

            if (tableData.get(i).get(2) == null) {

                GUIUtilities.displayWarningMessage("No source column selected in row " + (i - 1));
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

}







