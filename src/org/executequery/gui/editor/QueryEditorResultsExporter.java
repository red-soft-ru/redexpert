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
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author Takis Diakoumis
 */
public class QueryEditorResultsExporter extends AbstractBaseDialog {

    public static final String TITLE = bundleString("title");

    // --- all panels ---

    private JCheckBox addColumnHeadersCheck;
    private JCheckBox addQuotesCheck;
    private JCheckBox useAbsoluteBlobPathCheck;
    private JComboBox<?> typeCombo;
    private JTextField filePathField;
    private JTextField folderPathField;
    private JButton browseFileButton;
    private JButton browseFolderButton;
    private JButton exportButton;
    private JButton cancelButton;

    // --- delimiter file panel ---

    private JLabel delimiterLabel;
    private JCheckBox replaceEndlCheck;
    private JComboBox<?> columnDelimiterCombo;
    private JTextField replaceEndlField;

    // --- sql file panel ---

    private JLabel exportTableNameLabel;
    private JCheckBox openQueryEditorCheck;
    private JTextField exportTableNameField;

    // ---

    private TableModel exportTableModel;
    private final boolean isContainsBlob;
    private final String tableNameForExport;
    private final List<DatabaseColumn> databaseColumns;

    public QueryEditorResultsExporter(TableModel exportTableModel, String tableNameForExport) {
        this(exportTableModel, tableNameForExport, null);
    }

    public QueryEditorResultsExporter(TableModel exportTableModel, String tableNameForExport, List<DatabaseColumn> databaseColumns) {

        super(GUIUtilities.getParentFrame(), TITLE, true);

        this.exportTableModel = exportTableModel;
        this.tableNameForExport = tableNameForExport;
        this.databaseColumns = databaseColumns;
        this.isContainsBlob = isContainsBlob();

        init();
    }

    private void init() {

        String[] types = {"CSV", "XLSX", "XML", "SQL"};
        typeCombo = WidgetFactory.createComboBox("typeCombo", types);
        typeCombo.addActionListener(e -> updateDialog());

        String[] columnDelimiters = {"|", ",", ";", "#"};
        columnDelimiterCombo = WidgetFactory.createComboBox("columnDelimiterCombo", columnDelimiters);
        columnDelimiterCombo.setEditable(true);

        browseFileButton = WidgetFactory.createButton("browseFileButton", Bundles.get("common.browse.button"));
        browseFileButton.addActionListener(e -> browseFile(filePathField));

        browseFolderButton = WidgetFactory.createButton("browseFolderButton", Bundles.get("common.browse.button"));
        browseFolderButton.addActionListener(e -> browseFile(folderPathField));

        exportButton = WidgetFactory.createButton("exportButton", Bundles.get("common.export.button"));
        exportButton.addActionListener(e -> export());

        cancelButton = WidgetFactory.createButton("cancelButton", Bundles.get("common.cancel.button"));
        cancelButton.addActionListener(e -> dispose());

        addColumnHeadersCheck = WidgetFactory.createCheckBox("addColumnHeadersCheck", bundleString("IncludeColumnNamesAsFirstRow"));
        addQuotesCheck = WidgetFactory.createCheckBox("addQuotesCheck", bundleString("addQuotesCheck"));
        openQueryEditorCheck = WidgetFactory.createCheckBox("openQueryEditorCheck", bundleString("openQueryEditorCheck"));
        useAbsoluteBlobPathCheck = WidgetFactory.createCheckBox("useAbsoluteBlobPathCheck", bundleString("useAbsoluteBlobPathCheck"));

        replaceEndlCheck = WidgetFactory.createCheckBox("replaceEndlLabel", bundleString("replaceEndlLabel"));
        replaceEndlCheck.addActionListener(e -> {
            replaceEndlField.setText("");
            replaceEndlField.setEditable(replaceEndlCheck.isSelected());
        });

        replaceEndlField = WidgetFactory.createTextField("replaceEndlCombo");
        replaceEndlField.setEditable(false);

        exportTableNameField = WidgetFactory.createTextField("exportTableNameField");
        folderPathField = WidgetFactory.createTextField("folderPathField");
        filePathField = WidgetFactory.createTextField("filePathField");

        delimiterLabel = new JLabel(bundleString("delimiterLabel"));
        exportTableNameLabel = new JLabel(bundleString("exportTableNameField"));

        arrange();
    }

    private void arrange() {

        GridBagHelper gridBagHelper = new GridBagHelper()
                .setInsets(5, 5, 5, 5)
                .anchorNorthWest()
                .fillHorizontally();

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        buttonPanel.add(exportButton, gridBagHelper.get());
        buttonPanel.add(cancelButton, gridBagHelper.nextCol().get());

        // --- base panel ---

        JPanel basePanel = new JPanel(new GridBagLayout());
        basePanel.setPreferredSize(new Dimension(650, isContainsBlob ? 375 : 300));

        // for all files
        basePanel.add(addColumnHeadersCheck, gridBagHelper.nextRowFirstCol().setWidth(3).get());
        basePanel.add(addQuotesCheck, gridBagHelper.nextRowFirstCol().get());
        basePanel.add(openQueryEditorCheck, gridBagHelper.nextRowFirstCol().get());
        if (isContainsBlob)
            basePanel.add(useAbsoluteBlobPathCheck, gridBagHelper.nextRowFirstCol().get());
        gridBagHelper.addLabelFieldPair(basePanel, bundleString("FileFormat"), typeCombo, null, true, true);

        // for delimiter file
        gridBagHelper.addLabelFieldPair(basePanel, delimiterLabel, columnDelimiterCombo, null, true, true);
        gridBagHelper.addLabelFieldPair(basePanel, replaceEndlCheck, replaceEndlField, null, true, true);

        // for sql file
        basePanel.add(exportTableNameLabel, gridBagHelper.setWidth(1).setMinWeightX().nextRowFirstCol().get());
        basePanel.add(exportTableNameField, gridBagHelper.nextCol().spanX().get());

        // for all files
        if (isContainsBlob) {
            basePanel.add(new JLabel(bundleString("FolderPath")), gridBagHelper.setWidth(1).setMinWeightX().nextRowFirstCol().get());
            basePanel.add(folderPathField, gridBagHelper.nextCol().setMaxWeightX().get());
            basePanel.add(browseFolderButton, gridBagHelper.nextCol().setMinWeightX().get());
        }
        basePanel.add(new JLabel(bundleString("FilePath")), gridBagHelper.setWidth(1).setMinWeightX().nextRowFirstCol().get());
        basePanel.add(filePathField, gridBagHelper.nextCol().setMaxWeightX().get());
        basePanel.add(browseFileButton, gridBagHelper.nextCol().setMinWeightX().get());
        basePanel.add(buttonPanel, gridBagHelper.nextRowFirstCol().anchorSouth().spanX().spanY().get());

        // --- this panel ---

        showDelimiterPanel();
        setLayout(new GridBagLayout());
        add(basePanel, gridBagHelper.fillBoth().get());

        setResizable(false);
        setSize(buttonPanel.getPreferredSize());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocation(GUIUtilities.getLocationForDialog(this.getSize()));

        pack();
        setVisible(true);
    }

    // --- display handlers ---

    private void updateDialog() {

        int type = getExportFileType();

        if (type == ImportExportDataProcess.DELIMITED)
            showDelimiterPanel();
        else if (type == ImportExportDataProcess.EXCEL)
            showXlsxPanel();
        else if (type == ImportExportDataProcess.XML)
            showXmlPanel();
        else if (type == ImportExportDataProcess.SQL)
            showSqlPanel();
    }

    private void showDelimiterPanel() {

        openQueryEditorCheck.setSelected(false);

        addColumnHeadersCheck.setEnabled(true);
        addQuotesCheck.setEnabled(true);
        openQueryEditorCheck.setEnabled(false);
        useAbsoluteBlobPathCheck.setEnabled(true);

        delimiterLabel.setVisible(true);
        columnDelimiterCombo.setVisible(true);
        replaceEndlCheck.setVisible(true);
        replaceEndlField.setVisible(true);
        exportTableNameLabel.setVisible(false);
        exportTableNameField.setVisible(false);
    }

    private void showXlsxPanel() {

        addQuotesCheck.setSelected(false);
        openQueryEditorCheck.setSelected(false);

        addColumnHeadersCheck.setEnabled(true);
        addQuotesCheck.setEnabled(false);
        openQueryEditorCheck.setEnabled(false);
        useAbsoluteBlobPathCheck.setEnabled(true);

        delimiterLabel.setVisible(false);
        columnDelimiterCombo.setVisible(false);
        replaceEndlCheck.setVisible(false);
        replaceEndlField.setVisible(false);
        exportTableNameLabel.setVisible(false);
        exportTableNameField.setVisible(false);
    }

    private void showXmlPanel() {

        addColumnHeadersCheck.setSelected(false);
        addQuotesCheck.setSelected(false);
        openQueryEditorCheck.setSelected(false);

        addColumnHeadersCheck.setEnabled(false);
        addQuotesCheck.setEnabled(false);
        openQueryEditorCheck.setEnabled(false);
        useAbsoluteBlobPathCheck.setEnabled(true);

        delimiterLabel.setVisible(false);
        columnDelimiterCombo.setVisible(false);
        replaceEndlCheck.setVisible(false);
        replaceEndlField.setVisible(false);
        exportTableNameLabel.setVisible(false);
        exportTableNameField.setVisible(false);
    }

    private void showSqlPanel() {

        addColumnHeadersCheck.setSelected(false);
        addQuotesCheck.setSelected(false);
        useAbsoluteBlobPathCheck.setSelected(true);

        addColumnHeadersCheck.setEnabled(false);
        addQuotesCheck.setEnabled(false);
        openQueryEditorCheck.setEnabled(true);
        useAbsoluteBlobPathCheck.setEnabled(false);

        delimiterLabel.setVisible(false);
        columnDelimiterCombo.setVisible(false);
        replaceEndlCheck.setVisible(false);
        replaceEndlField.setVisible(false);
        exportTableNameLabel.setVisible(true);
        exportTableNameField.setVisible(true);

        exportTableNameField.setText(tableNameForExport);
    }

    // --- buttons handlers ---

    private void browseFile(JTextField field) {

        String suffix = "";
        boolean isFile = field.equals(filePathField);

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setDialogTitle(bundleString("SelectExportFilePath"));
        fileChooser.setFileSelectionMode(isFile ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY);
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
        }

        String path = fileChooser.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(suffix))
            path += suffix;

        field.setText(path);
    }

    private void export() {

        String exportFilePath = filePathField.getText();
        String exportFolderPath = folderPathField.getText();

        if (MiscUtils.isNull(exportFilePath)) {
            GUIUtilities.displayErrorMessage(bundleString("YouMustSpecifyAFileToExportTo"));
            return;
        }

        if (isContainsBlob && MiscUtils.isNull(exportFolderPath)) {
            GUIUtilities.displayErrorMessage(bundleString("YouMustSpecifyAFileToExportTo"));
            return;
        }

        new File(exportFolderPath).mkdirs();

        if (FileUtils.fileExists(exportFilePath)) {
            int result = GUIUtilities.displayYesNoDialog(bundleString("OverwriteFile"), Bundles.get("common.confirmation"));
            if (result == JOptionPane.NO_OPTION) {
                filePathField.selectAll();
                filePathField.requestFocus();
                return;
            }
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

        try {

            String columnDelimiter = Objects.requireNonNull(columnDelimiterCombo.getSelectedItem()).toString();
            String endlReplacement = replaceEndlCheck.isSelected() ? replaceEndlField.getText().trim() : null;

            int rowCount = exportTableModel.getRowCount();
            int columnCount = exportTableModel.getColumnCount();

            StringBuilder resultText = new StringBuilder();
            PrintWriter writer = new PrintWriter(new FileWriter(filePathField.getText(), false), true);

            if (addColumnHeadersCheck.isSelected()) {

                for (int i = 0; i < columnCount; i++) {
                    resultText.append(exportTableModel.getColumnName(i));
                    if (i != columnCount - 1)
                        resultText.append(columnDelimiter);
                }

                writer.println(resultText);
                resultText.setLength(0);
            }

            ResultsProgressDialog progressDialog = getProgressDialog(rowCount);
            for (int row = 0; row < rowCount; row++) {

                for (int col = 0; col < columnCount; col++) {

                    Object value = exportTableModel.getValueAt(row, col);
                    String stringValue = getFormattedValue(value, endlReplacement);

                    if (addQuotesCheck.isSelected() && !stringValue.isEmpty())
                        if (isCharType(value) || exportTableModel.getColumnClass(col) == String.class)
                            stringValue = "\"" + stringValue + "\"";

                    if (isBlobType(value))
                        stringValue = writeBlobToFile((AbstractLobRecordDataItem) value, col, row);

                    resultText.append(stringValue);
                    if (col != columnCount - 1)
                        resultText.append(columnDelimiter);
                }

                writer.println(resultText);
                resultText.setLength(0);

                progressDialog.increment();
            }

            progressDialog.dispose();
            writer.close();

            return true;

        } catch (IOException e) {
            return displayErrorMessage(e);
        }
    }

    private boolean exportXLSX() {

        try {

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
                    headers.add(exportTableModel.getColumnName(i));

                builder.addRowHeader(headers);
            }

            ResultsProgressDialog progressDialog = getProgressDialog(rowCount);
            for (int row = 0; row < rowCount; row++) {

                List<String> values = new ArrayList<>();
                for (int col = 0; col < columnCount; col++) {

                    Object value = exportTableModel.getValueAt(row, col);
                    String stringValue = getFormattedValue(value, null);

                    if (isBlobType(value))
                        stringValue = writeBlobToFile((AbstractLobRecordDataItem) value, col, row);

                    values.add(stringValue);
                }

                builder.addRow(values);
                progressDialog.increment();
            }

            OutputStream outputStream = new FileOutputStream(filePathField.getText(), false);
            builder.writeTo(outputStream);
            outputStream.close();

            progressDialog.dispose();
            return true;

        } catch (IOException e) {
            return displayErrorMessage(e);
        }
    }

    private boolean exportXML() {

        try {
            new XmlWriter().write(filePathField.getText());
            return true;

        } catch (Exception e) {
            return displayErrorMessage(e);
        }
    }

    private boolean exportSQL() {

        try {

            String generatedSqlScript = getGenerateSqlScript();
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

    private boolean isContainsBlob() {

        if (exportTableModel == null)
            return false;

        for (int col = 0; col < exportTableModel.getColumnCount(); col++) {
            Object value = exportTableModel.getValueAt(0, col);
            if (isBlobType(value))
                return true;
        }

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

    private String getFormattedValue(Object value, String endlReplacement) {

        String result = "";

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

    private ResultsProgressDialog getProgressDialog(int rowCount) {

        ResultsProgressDialog progressDialog = new ResultsProgressDialog(rowCount);
        progressDialog.setLocation(GUIUtilities.getLocationForDialog(progressDialog.getSize()));
        progressDialog.setVisible(true);
        progressDialog.pack();

        return progressDialog;
    }

    private String writeBlobToFile(AbstractLobRecordDataItem lobValue, int col, int row) throws IOException {

        String stringValue = "NULL";

        byte[] lobData = lobValue.getData();
        if (lobData != null) {

            String lobType = lobValue.getLobRecordItemName();
            lobType = lobType.contains("/") ? lobType.split("/")[1] : "txt";

            stringValue = exportTableModel.getColumnName(col) + "_" + row + "." + lobType;

            File outputFile = new File(folderPathField.getText().trim(), stringValue);
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(lobData);
            }

            if (useAbsoluteBlobPathCheck.isSelected())
                stringValue = outputFile.getAbsolutePath();
        }

        return stringValue;
    }

    private String getGenerateSqlScript() {

        StringBuilder result = new StringBuilder();

        int rowCount = exportTableModel.getRowCount();
        int columnCount = exportTableModel.getColumnCount();

        String tableName = !exportTableNameField.getText().isEmpty() ?
                exportTableNameField.getText() :
                tableNameForExport;

        try {

            // --- add simple 'create table' statement ---

            String createTableTemplate = "/* Uncomment this block if the table doesn't exist\n\n" +
                    (databaseColumns != null && !databaseColumns.isEmpty() ?
                            ((AbstractDatabaseObject) databaseColumns.get(0).getParent()).getCreateSQLText() :
                            SQLUtils.generateCreateTable(tableName, exportTableModel)) +
                    "*/\n";

            result.append(createTableTemplate);

            // --- create 'insert into' template ---

            StringBuilder insertTemplate = new StringBuilder();
            insertTemplate.append("\nINSERT INTO ").append(MiscUtils.getFormattedObject(tableName, null)).append("(");

            for (int col = 0; col < columnCount; col++) {

                if (databaseColumns != null && databaseColumns.get(col).isGenerated())
                    continue;

                insertTemplate.append("\n\t")
                        .append(MiscUtils.getFormattedObject(exportTableModel.getColumnName(col), null))
                        .append(",");
            }

            insertTemplate.deleteCharAt(insertTemplate.lastIndexOf(","));
            insertTemplate.append("\n) VALUES (%s\n);\n");

            // --- add values to script ---

            StringBuilder values = new StringBuilder();

            ResultsProgressDialog progressDialog = getProgressDialog(rowCount);
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < columnCount; col++) {

                    if (databaseColumns != null && databaseColumns.get(col).isGenerated())
                        continue;

                    values.append("\n\t");

                    Object value = exportTableModel.getValueAt(row, col);
                    String stringValue = getFormattedValue(value, null);

                    if (isBlobType(value)) {
                        values.append("?'").append(writeBlobToFile((AbstractLobRecordDataItem) value, col, row)).append("'");

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

                progressDialog.increment();
            }
            progressDialog.dispose();

        } catch (Exception e) {
            displayErrorMessage(e);
        }

        return result.toString();
    }

    private boolean displayErrorMessage(Throwable e) {
        GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorWritingToFile") + e.getMessage(), e);
        return false;
    }

    @Override
    public void dispose() {
        exportTableModel = null;
        super.dispose();
    }

    private class XmlWriter {

        void write(String outputPath) throws ParserConfigurationException, TransformerException, IOException {

            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            int rowCount = exportTableModel.getRowCount();
            int columnCount = exportTableModel.getColumnCount();

            Element rootElement = getCreateElement(document, "result-set");
            document.appendChild(rootElement);

            if (exportTableModel instanceof ResultSetTableModel) {
                Element queryElement = getCreateElement(document, "query");
                queryElement.appendChild(document.createTextNode("\n" + ((ResultSetTableModel) exportTableModel).getQuery() + "\n"));
                rootElement.appendChild(queryElement);
            }

            List<String> exportData = new ArrayList<>();
            Element dataElement = getCreateElement(document, "data");

            for (int row = 0; row < rowCount; row++) {

                Element rowElement = getCreateElement(document, "row");
                dataElement.appendChild(rowElement);

                Attr attribute = document.createAttribute("number");
                attribute.setValue(String.valueOf(row + 1));
                rowElement.setAttributeNode(attribute);

                dataElement.appendChild(rowElement);

                for (int col = 0; col < columnCount; col++) {

                    Element valueElement = getCreateElement(document, exportTableModel.getColumnName(col));
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
                            valueElement.appendChild(document.createTextNode("NULL"));

                    } else {

                        Object value = exportTableModel.getValueAt(row, col);
                        valueElement.appendChild(value != null ?
                                document.createTextNode(value.toString()) :
                                document.createTextNode("NULL")
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
        }

        private Element getCreateElement(Document document, String name) {
            return document.createElement(name);
        }

    } // class XmlWriter

    private static class ResultsProgressDialog extends JDialog {

        private final JProgressBar progressBar;

        public ResultsProgressDialog(int recordCount) {

            super(GUIUtilities.getParentFrame(), bundleString("ExportingQueryResults"), false);
            progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, recordCount);

            add(new JLabel(bundleString("ExportingResultSet")));
            add(progressBar);

            setResizable(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        public void increment() {
            progressBar.setValue(progressBar.getValue() + 1);
        }

        @Override
        public void dispose() {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            super.dispose();
        }

    } // class ResultsProgressDialog

    private static String bundleString(String key) {
        return Bundles.get(QueryEditorResultsExporter.class, key);
    }

}
