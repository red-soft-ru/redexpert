/*
 * QueryEditorResultsExporter.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.editor;

import org.apache.poi.ss.SpreadsheetVersion;
import org.executequery.ApplicationContext;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.Types;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.importexport.DefaultExcelWorkbookBuilder;
import org.executequery.gui.importexport.ExcelWorkbookBuilder;
import org.executequery.gui.importexport.ImportExportDataProcess;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.DefaultProgressDialog;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;
import org.underworldlabs.util.SystemProperties;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.List;
import java.util.*;


/**
 * @author Takis Diakoumis
 */
public class QueryEditorResultsExporter extends AbstractBaseDialog {

    public static final String TITLE = bundleString("title");

    // --- GUI components ---

    private JComboBox<?> typeCombo;
    private JTextField filePathField;
    private JTextField blobPathField;
    private JTabbedPane tabbedPane;

    private JTable columnTable;

    private JCheckBox addColumnHeadersCheck;
    private JCheckBox addQuotesCheck;
    private JCheckBox saveBlobsIndividuallyCheck;
    private JCheckBox openQueryEditorCheck;
    private JCheckBox addCreateTableStatementCheck;

    private JCheckBox replaceNullCheck;
    private JTextField replaceNullField;

    private JCheckBox replaceEndlCheck;
    private JTextField replaceEndlField;

    private JLabel exportTableNameLabel;
    private JTextField exportTableNameField;

    private JLabel delimiterLabel;
    private JComboBox<?> columnDelimiterCombo;

    private JButton browseFileButton;
    private JButton browseBlobFileButton;
    private JButton exportButton;
    private JButton cancelButton;

    // ---

    private TableModel exportTableModel;
    private final String tableNameForExport;
    private final List<DatabaseColumn> databaseColumns;
    private Map<String, Component> components;
    private static String columnDelimiterComboName = "";

    public QueryEditorResultsExporter(TableModel exportTableModel, String tableNameForExport) {
        this(exportTableModel, tableNameForExport, null);
    }

    public QueryEditorResultsExporter(TableModel exportTableModel, String tableNameForExport, List<DatabaseColumn> databaseColumns) {

        super(GUIUtilities.getParentFrame(), TITLE, true);

        this.exportTableModel = exportTableModel;
        this.tableNameForExport = tableNameForExport;
        this.databaseColumns = databaseColumns;

        init();
        arrange();
    }

    private void init() {

        components = new HashMap<>();

        columnTable = new JTable(new ColumnTableModel(databaseColumns));
        columnTable.setFillsViewportHeight(true);
        columnTable.getColumnModel().getColumn(0).setMaxWidth(columnTable.getRowHeight() + 6);
        columnTable.getColumnModel().getColumn(0).setMinWidth(columnTable.getColumnModel().getColumn(0).getMaxWidth());
        columnTable.getColumnModel().getColumn(0).setCellRenderer(new ColumnTableRenderer());
        columnTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int selectedRow = columnTable.getSelectedRow();
                int selectedColumn = columnTable.getSelectedColumn();

                if (selectedColumn == 0) {

                    String oldValue = columnTable.getValueAt(selectedRow, selectedColumn).toString().toLowerCase();
                    columnTable.setValueAt(!oldValue.contains("true"), selectedRow, selectedColumn);

                    blobPathField.setEnabled(isContainsBlob());
                    browseBlobFileButton.setEnabled(isContainsBlob());
                    saveBlobsIndividuallyCheck.setEnabled(isContainsBlob());
                }

                columnTable.repaint();
            }
        });

        tabbedPane = new JTabbedPane();
        if (databaseColumns != null)
            tabbedPane.addTab(bundleString("ColumnsTab"), new JScrollPane(columnTable));

        String[] types = {"CSV", "XLSX", "XML", "SQL"};
        typeCombo = WidgetFactory.createComboBox("typeCombo", types);
        typeCombo.addActionListener(e -> updateDialog());
        components.put(typeCombo.getName(), typeCombo);

        String[] columnDelimiters = {";", "|", ",", "#"};
        columnDelimiterCombo = WidgetFactory.createComboBox("columnDelimiterCombo", columnDelimiters);
        columnDelimiterComboName = columnDelimiterCombo.getName();
        columnDelimiterCombo.setEditable(true);
        components.put(columnDelimiterCombo.getName(), columnDelimiterCombo);

        browseFileButton = WidgetFactory.createButton("browseFileButton", Bundles.get("common.browse.button"));
        browseFileButton.addActionListener(e -> browseFile(filePathField));

        browseBlobFileButton = WidgetFactory.createButton("browseFolderButton", Bundles.get("common.browse.button"));
        browseBlobFileButton.addActionListener(e -> browseFile(blobPathField));
        browseBlobFileButton.setEnabled(false);

        exportButton = WidgetFactory.createButton("exportButton", Bundles.get("common.export.button"));
        exportButton.addActionListener(e -> export());

        cancelButton = WidgetFactory.createButton("cancelButton", Bundles.get("common.cancel.button"));
        cancelButton.addActionListener(e -> dispose());

        addColumnHeadersCheck = WidgetFactory.createCheckBox("addColumnHeadersCheck", bundleString("IncludeColumnNamesAsFirstRow"));
        components.put(addColumnHeadersCheck.getName(), addColumnHeadersCheck);

        addQuotesCheck = WidgetFactory.createCheckBox("addQuotesCheck", bundleString("addQuotesCheck"));
        components.put(addQuotesCheck.getName(), addQuotesCheck);

        openQueryEditorCheck = WidgetFactory.createCheckBox("openQueryEditorCheck", bundleString("openQueryEditorCheck"));
        components.put(openQueryEditorCheck.getName(), openQueryEditorCheck);

        addCreateTableStatementCheck = WidgetFactory.createCheckBox("addCreateTableStatementCheck", bundleString("addCreateTableStatementCheck"));
        components.put(addCreateTableStatementCheck.getName(), addCreateTableStatementCheck);

        saveBlobsIndividuallyCheck = WidgetFactory.createCheckBox("saveBlobsIndividuallyCheck", bundleString("saveBlobsIndividually"));
        saveBlobsIndividuallyCheck.setEnabled(false);
        components.put(saveBlobsIndividuallyCheck.getName(), saveBlobsIndividuallyCheck);

        replaceEndlCheck = WidgetFactory.createCheckBox("replaceEndlCheck", bundleString("replaceEndlLabel"));
        replaceEndlCheck.addActionListener(e -> replaceEndlField.setEnabled(replaceEndlCheck.isSelected()));
        components.put(replaceEndlCheck.getName(), replaceEndlCheck);

        replaceNullCheck = WidgetFactory.createCheckBox("replaceNullCheck", bundleString("replaceNullCheck"));
        replaceNullCheck.addActionListener(e -> replaceNullField.setEnabled(replaceNullCheck.isSelected()));
        components.put(replaceNullCheck.getName(), replaceNullCheck);

        replaceEndlField = WidgetFactory.createTextField("replaceEndlCombo");
        replaceEndlField.setText("\\r\\n");
        components.put(replaceEndlField.getName(), replaceEndlField);

        replaceNullField = WidgetFactory.createTextField("replaceNullField");
        replaceNullField.setText(SystemProperties.getProperty("user", "results.table.cell.null.text"));
        components.put(replaceNullField.getName(), replaceNullField);

        exportTableNameField = WidgetFactory.createTextField("exportTableNameField");
        components.put(exportTableNameField.getName(), exportTableNameField);

        blobPathField = WidgetFactory.createTextField("folderPathField");
        blobPathField.setEnabled(false);
        components.put(blobPathField.getName(), blobPathField);

        filePathField = WidgetFactory.createTextField("filePathField");
        components.put(filePathField.getName(), filePathField);

        delimiterLabel = new JLabel(bundleString("delimiterLabel"));
        exportTableNameLabel = new JLabel(bundleString("exportTableNameField"));
    }

    private void arrange() {

        GridBagHelper gridBagHelper;

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gridBagHelper = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();
        buttonPanel.add(exportButton, gridBagHelper.get());
        buttonPanel.add(cancelButton, gridBagHelper.nextCol().get());

        // --- options panel ---

        JPanel optionsPanel = new JPanel(new GridBagLayout());

        gridBagHelper = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();
        optionsPanel.add(addColumnHeadersCheck, gridBagHelper.spanX().get());
        optionsPanel.add(addQuotesCheck, gridBagHelper.nextRowFirstCol().get());
        optionsPanel.add(openQueryEditorCheck, gridBagHelper.nextRowFirstCol().get());
        optionsPanel.add(addCreateTableStatementCheck, gridBagHelper.nextRowFirstCol().get());
        optionsPanel.add(saveBlobsIndividuallyCheck, gridBagHelper.nextRowFirstCol().get());
        optionsPanel.add(replaceNullCheck, gridBagHelper.nextRowFirstCol().setMinWeightX().setWidth(1).get());
        optionsPanel.add(replaceNullField, gridBagHelper.nextCol().setMaxWeightX().spanX().get());
        optionsPanel.add(replaceEndlCheck, gridBagHelper.nextRowFirstCol().setMinWeightX().setWidth(1).get());
        optionsPanel.add(replaceEndlField, gridBagHelper.nextCol().setMaxWeightX().spanX().get());
        optionsPanel.add(delimiterLabel, gridBagHelper.leftGap(10).topGap(8).nextRowFirstCol().setMinWeightX().setWidth(1).get());
        optionsPanel.add(columnDelimiterCombo, gridBagHelper.nextCol().leftGap(5).topGap(5).setMaxWeightX().spanX().get());
        optionsPanel.add(exportTableNameLabel, gridBagHelper.leftGap(10).topGap(8).setWidth(1).setMinWeightX().nextRowFirstCol().get());
        optionsPanel.add(exportTableNameField, gridBagHelper.nextCol().leftGap(5).topGap(5).spanX().get());
        optionsPanel.add(new JPanel(), gridBagHelper.nextRowFirstCol().anchorSouth().fillBoth().setMaxWeightY().spanY().get());

        // --- base panel ---

        JPanel basePanel = new JPanel(new GridBagLayout());

        gridBagHelper = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();
        basePanel.add(new JLabel(bundleString("FileFormat")), gridBagHelper.topGap(8).setWidth(1).setMinWeightX().get());
        basePanel.add(typeCombo, gridBagHelper.nextCol().topGap(5).spanX().get());
        basePanel.add(new JLabel(bundleString("FilePath")), gridBagHelper.setWidth(1).topGap(8).setMinWeightX().nextRowFirstCol().get());
        basePanel.add(filePathField, gridBagHelper.nextCol().topGap(5).setMaxWeightX().get());
        basePanel.add(browseFileButton, gridBagHelper.nextCol().setMinWeightX().get());
        basePanel.add(new JLabel(bundleString("FolderPath")), gridBagHelper.setWidth(1).topGap(8).setMinWeightX().nextRowFirstCol().get());
        basePanel.add(blobPathField, gridBagHelper.nextCol().topGap(5).setMaxWeightX().get());
        basePanel.add(browseBlobFileButton, gridBagHelper.nextCol().setMinWeightX().get());
        basePanel.add(tabbedPane, gridBagHelper.nextRowFirstCol().fillBoth().setMaxWeightY().spanX().get());
        basePanel.add(buttonPanel, gridBagHelper.nextRowFirstCol().anchorSouthWest().fillHorizontally().setMinWeightY().spanX().spanY().get());

        // --- this panel ---

        tabbedPane.addTab(bundleString("OptionsTab"), optionsPanel);
        showDelimiterPanel();
        new ParametersSaver().restore(components);

        replaceEndlField.setEnabled(replaceEndlCheck.isSelected());
        replaceNullField.setEnabled(replaceNullCheck.isSelected());

        setLayout(new GridBagLayout());
        add(basePanel, gridBagHelper.fillBoth().get());

        setPreferredSize(new Dimension(500, 455));
        setSize(getPreferredSize());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocation(GUIUtilities.getLocationForDialog(this.getSize()));

        pack();
        setVisible(true);
    }

    // --- display handlers ---

    private void updateDialog() {

        int type = getExportFileType();

        String validExtension;
        switch (type) {
            case (ImportExportDataProcess.EXCEL):
                showXlsxPanel();
                validExtension = ".xlsx";
                break;
            case (ImportExportDataProcess.XML):
                showXmlPanel();
                validExtension = ".xml";
                break;
            case (ImportExportDataProcess.SQL):
                showSqlPanel(isVisible());
                validExtension = ".sql";
                break;
            default:
                showDelimiterPanel();
                validExtension = ".csv";
        }

        updateFilePath(filePathField, validExtension);
    }

    private void updateFilePath(JTextField field, String validExtension) {

        String filePath = field.getText();
        if (!filePath.isEmpty()) {

            int extensionIndex = filePath.lastIndexOf(".") - 1;
            if (extensionIndex < 0) {
                filePath += validExtension;
                field.setText(filePath);
                return;
            }

            String extension = filePath.substring(extensionIndex);
            if (!extension.equalsIgnoreCase(validExtension))
                filePath = filePath.substring(0, extensionIndex + 1) + validExtension;
        }

        field.setText(filePath);
    }

    private void showDelimiterPanel() {

        replaceEndlField.setEnabled(replaceEndlCheck.isSelected());
        replaceNullField.setEnabled(replaceNullCheck.isSelected());

        addColumnHeadersCheck.setVisible(true);
        addQuotesCheck.setVisible(true);
        openQueryEditorCheck.setVisible(false);
        addCreateTableStatementCheck.setVisible(false);
        delimiterLabel.setVisible(true);
        columnDelimiterCombo.setVisible(true);
        replaceEndlCheck.setVisible(true);
        replaceEndlField.setVisible(true);
        replaceNullCheck.setVisible(true);
        replaceNullField.setVisible(true);
        exportTableNameLabel.setVisible(false);
        exportTableNameField.setVisible(false);
    }

    private void showXlsxPanel() {

        replaceEndlField.setEnabled(replaceEndlCheck.isSelected());
        replaceNullField.setEnabled(replaceNullCheck.isSelected());

        addColumnHeadersCheck.setVisible(true);
        addQuotesCheck.setVisible(false);
        openQueryEditorCheck.setVisible(false);
        addCreateTableStatementCheck.setVisible(false);
        delimiterLabel.setVisible(false);
        columnDelimiterCombo.setVisible(false);
        replaceEndlCheck.setVisible(false);
        replaceEndlField.setVisible(false);
        replaceNullCheck.setVisible(true);
        replaceNullField.setVisible(true);
        exportTableNameLabel.setVisible(false);
        exportTableNameField.setVisible(false);
    }

    private void showXmlPanel() {

        replaceEndlField.setEnabled(replaceEndlCheck.isSelected());
        replaceNullField.setEnabled(replaceNullCheck.isSelected());

        addColumnHeadersCheck.setVisible(false);
        addQuotesCheck.setVisible(false);
        openQueryEditorCheck.setVisible(false);
        addCreateTableStatementCheck.setVisible(false);
        delimiterLabel.setVisible(false);
        columnDelimiterCombo.setVisible(false);
        replaceEndlCheck.setVisible(false);
        replaceEndlField.setVisible(false);
        replaceNullCheck.setVisible(true);
        replaceNullField.setVisible(true);
        exportTableNameLabel.setVisible(false);
        exportTableNameField.setVisible(false);
    }

    private void showSqlPanel(boolean changeValues) {

        if (changeValues)
            exportTableNameField.setText(tableNameForExport);

        addColumnHeadersCheck.setVisible(false);
        addQuotesCheck.setVisible(false);
        openQueryEditorCheck.setVisible(true);
        addCreateTableStatementCheck.setVisible(true);
        delimiterLabel.setVisible(false);
        columnDelimiterCombo.setVisible(false);
        replaceEndlCheck.setVisible(false);
        replaceEndlField.setVisible(false);
        replaceNullCheck.setVisible(false);
        replaceNullField.setVisible(false);
        exportTableNameLabel.setVisible(true);
        exportTableNameField.setVisible(true);
    }

    // --- buttons handlers ---

    private void browseFile(JTextField field) {

        String suffix = "";
        boolean isFile = field.equals(filePathField);

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setDialogTitle(bundleString("SelectExportFilePath"));
        fileChooser.setFileSelectionMode(isFile || !saveBlobsIndividuallyCheck.isSelected() ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setMultiSelectionEnabled(false);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        if (isFile) {
            switch (getExportFileType()) {
                case (ImportExportDataProcess.EXCEL):
                    suffix = ".xlsx";
                    break;
                case (ImportExportDataProcess.XML):
                    suffix = ".xml";
                    break;
                case (ImportExportDataProcess.SQL):
                    suffix = ".sql";
                    break;
                default:
                    suffix = ".csv";
            }

        } else if (!saveBlobsIndividuallyCheck.isSelected()) {
            suffix = ".lob";
        }

        field.setText(fileChooser.getSelectedFile().getAbsolutePath());
        updateFilePath(field, suffix);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void export() {

        String exportFilePath = filePathField.getText();
        String exportBlobPath = blobPathField.getText();

        if (MiscUtils.isNull(exportFilePath)) {
            GUIUtilities.displayErrorMessage(bundleString("YouMustSpecifyAFileToExportTo"));
            return;
        }

        if (isContainsBlob() && MiscUtils.isNull(exportBlobPath)) {
            GUIUtilities.displayErrorMessage(bundleString("YouMustSpecifyAFileToExportTo"));
            return;
        }

        if (columnTable.getRowCount() > 0) {

            boolean selected = false;
            for (int i = 0; i < columnTable.getRowCount(); i++) {
                if (isFieldSelected(i)) {
                    selected = true;
                    break;
                }
            }

            if (!selected) {
                GUIUtilities.displayErrorMessage(bundleString("YouMustSpecifyAColumnToExportTo"));
                return;
            }
        }

        if (FileUtils.fileExists(exportFilePath)) {

            int result = GUIUtilities.displayYesNoDialog(
                    String.format(bundleString("OverwriteFile"), exportFilePath),
                    Bundles.get("common.confirmation")
            );

            if (result == JOptionPane.NO_OPTION) {
                filePathField.selectAll();
                filePathField.requestFocus();
                return;
            }
        }

        if (isContainsBlob()) {

            if (!saveBlobsIndividuallyCheck.isSelected()) {

                if (FileUtils.fileExists(exportBlobPath)) {

                    int result = GUIUtilities.displayYesNoDialog(
                            String.format(bundleString("OverwriteFile"), exportBlobPath),
                            Bundles.get("common.confirmation")
                    );

                    if (result == JOptionPane.NO_OPTION) {
                        blobPathField.selectAll();
                        blobPathField.requestFocus();
                        return;
                    }
                }

                try {
                    Files.write(Paths.get(exportBlobPath), "".getBytes(StandardCharsets.UTF_8));
                } catch (IOException ignored) {
                }

            } else
                new File(exportBlobPath).mkdirs();
        }

        SwingWorker worker = new SwingWorker("ExportFromResultSet") {

            boolean success = false;

            @Override
            public Object construct() {

                switch (getExportFileType()) {

                    case ImportExportDataProcess.DELIMITED:
                        success = exportCSV();
                        break;

                    case ImportExportDataProcess.EXCEL:
                        success = exportXLSX();
                        break;

                    case ImportExportDataProcess.XML:
                        success = exportXML();
                        break;

                    case ImportExportDataProcess.SQL:
                        success = exportSQL();
                        break;
                }

                return null;
            }

            @Override
            public void finished() {
                if (success)
                    GUIUtilities.displayInformationMessage(bundleString("ResultSetExportComplete"));
                dispose();
            }
        };
        worker.start();
    }

    // --- export handlers ---

    private boolean exportCSV() {

        DefaultProgressDialog progressDialog = new DefaultProgressDialog(TITLE);
        SwingWorker worker = new SwingWorker("ExportResultSet", this) {
            @Override
            public Object construct() {
                try {

                    String columnDelimiter = Objects.requireNonNull(columnDelimiterCombo.getSelectedItem()).toString();
                    String endlReplacement = replaceEndlCheck.isSelected() ? replaceEndlField.getText().trim() : null;
                    String nullReplacement = replaceNullCheck.isSelected() ? replaceNullField.getText().trim() : "";

                    int rowCount = exportTableModel.getRowCount();
                    int columnCount = exportTableModel.getColumnCount();

                    StringBuilder resultText = new StringBuilder();
                    PrintWriter writer = new PrintWriter(new FileWriter(filePathField.getText(), false), true);

                    if (addColumnHeadersCheck.isSelected()) {

                        for (int i = 0; i < columnCount; i++) {
                            if (isFieldSelected(i)) {
                                resultText.append(exportTableModel.getColumnName(i));
                                resultText.append(columnDelimiter);
                            }
                        }
                        resultText.deleteCharAt(resultText.length() - 1);

                        writer.println(resultText);
                        resultText.setLength(0);
                    }

                    for (int row = 0; row < rowCount; row++) {

                        if (progressDialog.isCancel())
                            break;

                        for (int col = 0; col < columnCount; col++) {

                            if (progressDialog.isCancel())
                                break;

                            if (!isFieldSelected(col))
                                continue;

                            String stringValue = null;
                            RecordDataItem value = (RecordDataItem) exportTableModel.getValueAt(row, col);

                            if (!value.isValueNull()) {
                                stringValue = getFormattedValue(value, endlReplacement, nullReplacement);

                                if (addQuotesCheck.isSelected() && !stringValue.isEmpty())
                                    if (isCharType(value))
                                        stringValue = "\"" + stringValue + "\"";

                                if (isBlobType(value))
                                    stringValue = writeBlobToFile((AbstractLobRecordDataItem) value, col, row);
                            }

                            resultText.append(stringValue != null ? stringValue : nullReplacement);
                            resultText.append(columnDelimiter);
                        }
                        resultText.deleteCharAt(resultText.length() - 1);

                        writer.println(resultText);
                        resultText.setLength(0);
                    }
                    writer.close();

                } catch (IOException e) {
                    return displayErrorMessage(e);
                }

                return null;
            }

            @Override
            public void finished() {
                progressDialog.dispose();
            }
        };
        worker.start();
        progressDialog.run();

        return true;
    }

    private boolean exportXLSX() {

        DefaultProgressDialog progressDialog = new DefaultProgressDialog(TITLE);
        SwingWorker worker = new SwingWorker("ExportResultSet", this) {
            @Override
            public Object construct() {
                try {

                    String nullReplacement = replaceNullCheck.isSelected() ? replaceNullField.getText().trim() : "";
                    int columnCount = exportTableModel.getColumnCount();
                    int rowCount = exportTableModel.getRowCount();

                    if (rowCount > SpreadsheetVersion.EXCEL2007.getLastRowIndex()) {
                        GUIUtilities.displayWarningMessage(String.format(bundleString("maxRowMessage"), SpreadsheetVersion.EXCEL2007.getLastRowIndex()));
                        rowCount = SpreadsheetVersion.EXCEL2007.getLastRowIndex();
                    }

                    ExcelWorkbookBuilder builder = new DefaultExcelWorkbookBuilder();
                    builder.createSheet("Result Set Export");

                    if (addColumnHeadersCheck.isSelected()) {

                        List<String> headers = new ArrayList<>();
                        for (int i = 0; i < columnCount; i++)
                            if (isFieldSelected(i))
                                headers.add(exportTableModel.getColumnName(i));

                        builder.addRowHeader(headers);
                    }

                    for (int row = 0; row < rowCount; row++) {

                        if (progressDialog.isCancel())
                            break;

                        List<String> values = new ArrayList<>();
                        for (int col = 0; col < columnCount; col++) {

                            if (progressDialog.isCancel())
                                break;

                            if (!isFieldSelected(col))
                                continue;

                            String stringValue = null;
                            RecordDataItem value = (RecordDataItem) exportTableModel.getValueAt(row, col);

                            if (!value.isValueNull()) {
                                stringValue = getFormattedValue(value, null, nullReplacement);

                                if (isCharType(value))
                                    stringValue = getFormattedValue(value, null, nullReplacement);
                                else if (isBlobType(value))
                                    stringValue = writeBlobToFile((AbstractLobRecordDataItem) value, col, row);
                            }

                            values.add(stringValue != null ? stringValue : nullReplacement);
                        }

                        builder.addRow(values);
                    }

                    OutputStream outputStream = new FileOutputStream(filePathField.getText(), false);
                    builder.writeTo(outputStream);
                    outputStream.close();

                } catch (IOException e) {
                    return displayErrorMessage(e);
                }

                return null;
            }

            @Override
            public void finished() {
                progressDialog.dispose();
            }
        };
        worker.start();
        progressDialog.run();

        return true;
    }

    private boolean exportXML() {

        String nullReplacement = replaceNullCheck.isSelected() ? replaceNullField.getText().trim() : "";
        String outputPath = filePathField.getText();
        int rowCount = exportTableModel.getRowCount();
        int columnCount = exportTableModel.getColumnCount();

        DefaultProgressDialog progressDialog = new DefaultProgressDialog(TITLE);
        SwingWorker worker = new SwingWorker("ExportResultSet", this) {

            @Override
            public Object construct() {
                try {

                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    Element rootElement = document.createElement("result-set");
                    document.appendChild(rootElement);

                    if (exportTableModel instanceof ResultSetTableModel) {
                        Element queryElement = document.createElement("query");
                        queryElement.appendChild(document.createTextNode("\n" + ((ResultSetTableModel) exportTableModel).getQuery() + "\n"));
                        rootElement.appendChild(queryElement);
                    }

                    List<String> exportData = new ArrayList<>();
                    Element dataElement = document.createElement("data");

                    for (int row = 0; row < rowCount; row++) {

                        if (progressDialog.isCancel())
                            break;

                        Element rowElement = document.createElement("row");
                        dataElement.appendChild(rowElement);

                        Attr attribute = document.createAttribute("number");
                        attribute.setValue(String.valueOf(row + 1));
                        rowElement.setAttributeNode(attribute);

                        dataElement.appendChild(rowElement);

                        for (int col = 0; col < columnCount; col++) {

                            if (progressDialog.isCancel())
                                break;

                            if (!isFieldSelected(col))
                                continue;

                            Element valueElement = document.createElement(exportTableModel.getColumnName(col));
                            if (exportTableModel instanceof ResultSetTableModel) {

                                RecordDataItem value = (RecordDataItem) exportTableModel.getValueAt(row, col);
                                if (!value.isValueNull()) {

                                    valueElement.appendChild(isBlobType(value) ?
                                            document.createTextNode(writeBlobToFile((AbstractLobRecordDataItem) value, col, row)) :
                                            document.createTextNode(value.toString())
                                    );

                                    String name = value.getName();
                                    if (isCharType(value) && !exportData.contains(name))
                                        exportData.add(name);

                                } else
                                    valueElement.appendChild(document.createTextNode(nullReplacement));

                            } else {

                                Object value = exportTableModel.getValueAt(row, col);
                                valueElement.appendChild(value != null ?
                                        document.createTextNode(value.toString()) :
                                        document.createTextNode(nullReplacement)
                                );
                            }

                            rowElement.appendChild(valueElement);
                        }
                    }
                    rootElement.appendChild(dataElement);

                    StringBuilder query = new StringBuilder("query");
                    exportData.forEach(val -> query.append(" ").append(val));

                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                    transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, query.toString());
                    transformer.transform(new DOMSource(document), new StreamResult(new File(outputPath)));

                } catch (Exception e) {
                    return displayErrorMessage(e);
                }

                return null;
            }

            @Override
            public void finished() {
                progressDialog.dispose();
            }
        };
        worker.start();
        progressDialog.run();

        return true;
    }

    private boolean exportSQL() {

        try {

            StringBuilder result = new StringBuilder();

            int rowCount = exportTableModel.getRowCount();
            int columnCount = exportTableModel.getColumnCount();

            String tableName = !exportTableNameField.getText().isEmpty() ?
                    exportTableNameField.getText() :
                    tableNameForExport;

            DefaultProgressDialog progressDialog = new DefaultProgressDialog(TITLE);
            SwingWorker worker = new SwingWorker("ExportResultSet", this) {

                @Override
                public Object construct() {

                    try {

                        // --- add 'create table' statement ---

                        if (addCreateTableStatementCheck.isSelected()) {

                            String createTableTemplate = "-- table creating --\n\n" +
                                    (databaseColumns != null && !databaseColumns.isEmpty() ?
                                            ((AbstractDatabaseObject) databaseColumns.get(0).getParent()).getCreateSQLText() :
                                            SQLUtils.generateCreateTable(tableName, exportTableModel))
                                    + "\n-- inserting data --\n";

                            result.append(createTableTemplate);
                        }

                        // --- setup *.lob file ---

                        if (!saveBlobsIndividuallyCheck.isSelected() && isContainsBlob())
                            result.append("\nSET BLOBFILE '").append(blobPathField.getText().trim()).append("';\n");

                        // --- create 'insert into' template ---

                        StringBuilder insertTemplate = new StringBuilder();
                        insertTemplate.append("\nINSERT INTO ").append(MiscUtils.getFormattedObject(tableName, null)).append("(");

                        for (int col = 0; col < columnCount; col++) {

                            if (!isFieldSelected(col))
                                continue;

                            insertTemplate.append("\n\t")
                                    .append(MiscUtils.getFormattedObject(exportTableModel.getColumnName(col), null))
                                    .append(",");
                        }

                        insertTemplate.deleteCharAt(insertTemplate.lastIndexOf(","));
                        insertTemplate.append("\n) VALUES (%s\n);\n");

                        // --- add values to script ---

                        StringBuilder values = new StringBuilder();

                        for (int row = 0; row < rowCount; row++) {

                            if (progressDialog.isCancel())
                                break;

                            for (int col = 0; col < columnCount; col++) {

                                if (progressDialog.isCancel())
                                    break;

                                if (!isFieldSelected(col))
                                    continue;

                                values.append("\n\t");

                                Object value = exportTableModel.getValueAt(row, col);
                                String stringValue = getFormattedValue(value, null, "");

                                if (isBlobType(value)) {
                                    if (saveBlobsIndividuallyCheck.isSelected())
                                        values.append("?'").append(writeBlobToFile((AbstractLobRecordDataItem) value, col, row)).append("'");
                                    else
                                        values.append(writeBlobToFile((AbstractLobRecordDataItem) value, col, row));

                                } else if (!stringValue.isEmpty()) {

                                    if (value instanceof RecordDataItem) {

                                        if (isCharType(value))
                                            values.append("'").append(stringValue).append("'");
                                        else if (isDateType(value))
                                            values.append("'").append(stringValue.replace('T', ' ')).append("'");
                                        else
                                            values.append(stringValue);

                                    } else {

                                        if (exportTableModel.getColumnClass(col) == String.class)
                                            values.append("'").append(stringValue).append("'");
                                        else if (exportTableModel.getColumnClass(col) == Timestamp.class)
                                            values.append("'").append(stringValue.replace('T', ' ')).append("'");
                                        else
                                            values.append(stringValue);
                                    }
                                } else
                                    values.append("NULL");

                                values.append(",");
                            }
                            values.deleteCharAt(values.lastIndexOf(","));
                            result.append(String.format(insertTemplate.toString(), values));
                            values.setLength(0);
                        }
                        progressDialog.dispose();

                    } catch (Exception e) {
                        displayErrorMessage(e);
                    }

                    return null;
                }

                @Override
                public void finished() {
                    progressDialog.dispose();
                }
            };
            worker.start();
            progressDialog.run();

            String generatedSqlScript = result.toString().trim();
            PrintWriter writer = new PrintWriter(new FileWriter(filePathField.getText(), false), true);
            writer.println(generatedSqlScript);
            writer.close();

            if (openQueryEditorCheck.isSelected()) {
                GUIUtilities.addCentralPane(
                        QueryEditor.TITLE, QueryEditor.FRAME_ICON,
                        new QueryEditor(generatedSqlScript), null, true
                );
            }

            return true;

        } catch (IOException e) {
            return displayErrorMessage(e);
        }
    }

    // ---

    private boolean isFieldSelected(int fieldIndex) {

        Object value = columnTable.getValueAt(fieldIndex, 0);
        if (value == null)
            return true;

        return value.toString().toLowerCase().contains("true");
    }

    private boolean isContainsBlob() {

        if (exportTableModel == null)
            return false;

        for (int col = 0; col < exportTableModel.getColumnCount(); col++)
            if (isFieldSelected(col) && isBlobType(exportTableModel.getValueAt(0, col)))
                return true;

        return false;
    }

    private boolean isBlobType(Object value) {
        return value instanceof AbstractLobRecordDataItem;
    }

    private boolean isCharType(Object value) {

        if (value instanceof RecordDataItem) {
            int type = ((RecordDataItem) value).getDataType();
            return type == Types.CHAR || type == Types.VARCHAR || type == Types.LONGVARCHAR;
        }

        return false;
    }

    private boolean isDateType(Object value) {

        if (value instanceof RecordDataItem) {
            int type = ((RecordDataItem) value).getDataType();
            return type == Types.DATE || type == Types.TIME || type == Types.TIMESTAMP;
        }

        return false;
    }

    private int getExportFileType() {

        switch (typeCombo.getSelectedIndex()) {
            case 1:
                return ImportExportDataProcess.EXCEL;
            case 2:
                return ImportExportDataProcess.XML;
            case 3:
                return ImportExportDataProcess.SQL;
            default:
                return ImportExportDataProcess.DELIMITED;
        }
    }

    private String getFormattedValue(Object value, String endlReplacement, String nullReplacement) {

        String result = nullReplacement;

        if (value instanceof RecordDataItem) {
            RecordDataItem recordDataItem = (RecordDataItem) value;
            if (!recordDataItem.isValueNull())
                result = recordDataItem.getDisplayValue().toString().replaceAll("'", "''");

        } else if (value != null)
            result = value.toString().replaceAll("'", "''");

        if (!result.isEmpty() && endlReplacement != null)
            result = result.replaceAll("\n", endlReplacement);

        return result;
    }

    private String writeBlobToFile(AbstractLobRecordDataItem lobValue, int col, int row) throws IOException {

        String stringValue = "NULL";

        byte[] lobData = lobValue.getData();
        if (lobData != null) {

            if (saveBlobsIndividuallyCheck.isSelected()) {

                String lobType = lobValue.getLobRecordItemName();
                lobType = lobType.contains("/") ? lobType.split("/")[1] : "txt";

                stringValue = exportTableModel.getColumnName(col) + "_" + row + "." + lobType;

                File outputFile = new File(blobPathField.getText().trim(), stringValue);
                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    outputStream.write(lobData);
                }

                stringValue = outputFile.getAbsolutePath();

            } else {

                String startIndex = String.format("%08x", new File(blobPathField.getText().trim()).length());
                String dataLength = String.format("%08x", lobData.length);
                stringValue = ":h" + startIndex + "_" + dataLength;

                Files.write(Paths.get(blobPathField.getText().trim()), lobData, StandardOpenOption.APPEND);
            }
        }

        return stringValue;
    }

    private boolean displayErrorMessage(Throwable e) {
        GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorWritingToFile") + e.getMessage(), e);
        return false;
    }

    public static String getParametersSaverFilePath() {
        return new ParametersSaver().getFileName();
    }

    public static String getParametersSaverDelimiter() {
        return new ParametersSaver().getDelimiter();
    }

    @Override
    public void dispose() {
        exportTableModel = null;
        new ParametersSaver().save(components);
        super.dispose();
    }

    private static class ParametersSaver {

        private final String FILE_NAME =
                ApplicationContext.getInstance().getUserSettingsHome() + FileSystems.getDefault().getSeparator() + "resultsExporter.save";
        private final String DELIMITER = "===";

        void save(Map<String, Component> components) {

            // clear old values
            try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, false))) {
                writer.print("");

            } catch (IOException e) {
                Log.error("Error saving QueryEditorResultsExporter values", e);
            }

            // save new values
            try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {

                for (String key : components.keySet()) {

                    writer.print(key + DELIMITER);

                    Component component = components.get(key);
                    if (component instanceof JCheckBox) {
                        writer.println(((JCheckBox) component).isSelected());

                    } else if (component instanceof JTextField) {
                        writer.println(((JTextField) component).getText().trim());

                    } else if (component instanceof JComboBox) {

                        if (component.getName().equals(columnDelimiterComboName))
                            writer.println(((JComboBox<?>) component).getSelectedItem());
                        else
                            writer.println(((JComboBox<?>) component).getSelectedIndex());
                    }
                }

            } catch (IOException e) {
                Log.error("Error saving QueryEditorResultsExporter values", e);
            }
        }

        void restore(Map<String, Component> components) {

            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(DELIMITER);

                    Component component = components.get(data[0]);
                    String value = data.length > 1 ? data[1] : "";

                    if (component instanceof JCheckBox) {
                        ((JCheckBox) component).setSelected(value.equalsIgnoreCase("true"));

                    } else if (component instanceof JTextField) {
                        ((JTextField) component).setText(value);

                    } else if (component instanceof JComboBox) {

                        if (component.getName().equals(columnDelimiterComboName))
                            ((JComboBox<?>) component).setSelectedItem(value);
                        else
                            ((JComboBox<?>) component).setSelectedIndex(Integer.parseInt(value));

                    }
                }

            } catch (IOException e) {
                Log.error("Error restoring QueryEditorResultsExporter values", e);
            }
        }

        String getFileName() {
            return FILE_NAME;
        }

        String getDelimiter() {
            return DELIMITER;
        }

    } // class ParameterSaver

    private static class ColumnTableModel implements TableModel {

        private final List<ColumnTableData> rows = new ArrayList<>();
        private final String[] headers = new String[]{"", bundleString("ColumnNameHeader"), bundleString("ColumnTypeHeader")};

        private ColumnTableModel(List<DatabaseColumn> databaseColumns) {

            if (databaseColumns != null) {
                for (DatabaseColumn column : databaseColumns) {
                    rows.add(new ColumnTableData(
                            !column.isGenerated() && !column.getTypeName().toLowerCase().contains("blob"),
                            column.getName(),
                            column.getTypeName()
                    ));
                }
            }
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return headers.length > columnIndex ? headers[columnIndex] : null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            if (rows.size() > rowIndex) {

                switch (columnIndex) {
                    case 0:
                        return rows.get(rowIndex).isSelected();
                    case 1:
                        return rows.get(rowIndex).getName();
                    case 2:
                        return rows.get(rowIndex).getType();
                }
            }

            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0)
                rows.get(rowIndex).setSelected(aValue.toString().toLowerCase().contains("true"));
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
        }

    } // class ColumnTableModel

    private static class ColumnTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (column == 0) {

                JCheckBox checkBox = new JCheckBox();
                checkBox.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                checkBox.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                checkBox.setSelected(value.toString().toLowerCase().contains("true"));

                return checkBox;
            }

            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

    } // class ColumnTableRenderer

    private static class ColumnTableData {

        private boolean isSelected;
        private final String name;
        private final String type;

        public ColumnTableData(boolean isSelected, String name, String type) {
            this.isSelected = isSelected;
            this.name = name;
            this.type = type;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

    } // class TableColumn

    private static String bundleString(String key) {
        return Bundles.get(QueryEditorResultsExporter.class, key);
    }

}
