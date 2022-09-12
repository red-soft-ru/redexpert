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

import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.DefaultInlineFieldButton;
import org.executequery.gui.browser.StatementToEditorWriter;
import org.executequery.gui.importexport.DefaultExcelWorkbookBuilder;
import org.executequery.gui.importexport.ExcelWorkbookBuilder;
import org.executequery.gui.importexport.ImportExportDataProcess;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTableModelToXMLWriter;
import org.executequery.localization.Bundles;
import org.executequery.sql.TokenizingFormatter;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.CharLimitedTextField;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.executequery.GUIUtilities.getDockedTabComponent;


/**
 * @author Takis Diakoumis
 */
public class QueryEditorResultsExporter extends AbstractBaseDialog {

    // column headers check
    private JCheckBox columnHeadersCheck;

    // use quotes check
    private JCheckBox applyQuotesCheck;

    // the export type combo
    private JComboBox typeCombo;

    // the delimiter combo
    private JComboBox delimCombo;

    // custom delimiter field
    private JTextField customDelimiterField;

    // the file text field
    private JTextField fileNameField;

    // The table model to be exported
    private TableModel model;

    // The sender to QueryEditor
    private StatementToEditorWriter statementWriter;

    // SQL format to QueryEditor
    private TokenizingFormatter formatter;

    // Used to view the contents of a file
    private JButton browse;

    // The text to specify the path to the file
    private JLabel filePath;

    // custom name table
    private JTextField customNameTable;

    // The text to specify the custom separator
    private JLabel custom;

    // The text to specify the separator
    private JLabel delimiter;

    // The text to specify the name of table
    private JLabel tableName;

    // The text to specify the SQL export
    private JLabel exportSQL;

    // the SQL Query export combo
    private JComboBox queryComboSQL;

    // Name of selected table
    private final String tableNameForExport;

    private final List<DatabaseColumn> databaseColumns;

    public QueryEditorResultsExporter(TableModel model, String tableNameForExport) {
        this(model, tableNameForExport, null);
    }

    public QueryEditorResultsExporter(TableModel model, String tableNameForExport, List<DatabaseColumn> databaseColumns) {

        super(GUIUtilities.getParentFrame(), Bundles.get("QueryEditorResultsExporter.ExportQueryResults"), true);
        this.model = model;
        this.tableNameForExport = tableNameForExport;
        this.databaseColumns = databaseColumns;
        init();

        pack();
        this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
    }

    private void init() {

        ReflectiveAction action = new ReflectiveAction(this);

        String[] delims = {"|", ",", ";", "#", "Custom"};
        delimCombo = ActionUtilities.createComboBox(action, delims, "delimeterChanged");

        String[] querySQL = {bundleString("SQLInFile"), bundleString("SQLQueryEditor")};
        queryComboSQL = ActionUtilities.createComboBox(action, querySQL, "queryChangedSQL");

        String[] types = {"Delimited File", "Excel Spreadsheet", "XML", "SQL"};
        typeCombo = ActionUtilities.createComboBox(action, types, "exportTypeChanged");

        customDelimiterField = new CharLimitedTextField(1);
        customNameTable = new CharLimitedTextField(20);
        fileNameField = WidgetFactory.createTextField();

        browse = new DefaultInlineFieldButton(action);
        browse.setText(Bundles.get("ExecuteSqlScriptPanel.Browse"));
        browse.setActionCommand("browse");

        JButton okButton = new DefaultPanelButton(action, Bundles.get("common.ok.button"), "export");
        JButton cancelButton = new DefaultPanelButton(action, Bundles.get("common.cancel.button"), "cancel");

        columnHeadersCheck = new JCheckBox(bundleString("IncludeColumnNamesAsFirstRow"));
        applyQuotesCheck = new JCheckBox(bundleString("UseDoubleQuotesForChar/varchar/longvarcharColumns"), true);

        // the button panel
        JPanel btnPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0;
        btnPanel.add(okButton, gbc);

        gbc.weightx = 0;
        gbc.gridx = 1;
        gbc.insets.left = 5;
        btnPanel.add(cancelButton, gbc);

        int labelInsetsTop = 10;
        int fieldInsetsTop = 5;

        // the base panel
        JPanel base = new JPanel(new GridBagLayout());
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        base.add(new JLabel(bundleString("SelectTheExportTypeDelimiterAndFilePathBelow")), gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.bottom = 0;
        base.add(columnHeadersCheck, gbc);
        gbc.gridy++;

        gbc.insets.bottom = 10;
        base.add(applyQuotesCheck, gbc);
        gbc.gridy++;

        gbc.gridwidth = 1;
        gbc.insets.bottom = 0;
        gbc.insets.top = labelInsetsTop;
        base.add(new JLabel(bundleString("FileFormat")), gbc);
        gbc.gridx = 1;
        gbc.insets.top = fieldInsetsTop;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        base.add(typeCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets.bottom = 0;
        gbc.insets.top = labelInsetsTop;
        delimiter = new JLabel(bundleString("Delimiter"));
        exportSQL = new JLabel(bundleString("SQLExport"));
        base.add(delimiter, gbc);
        base.add(exportSQL, gbc);
        gbc.gridx = 1;
        gbc.insets.top = fieldInsetsTop;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        base.add(delimCombo, gbc);
        base.add(queryComboSQL, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = labelInsetsTop;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        custom = new JLabel(bundleString("Custom"));
        base.add(custom, gbc);
        tableName = new JLabel(bundleString("TableName"));
        base.add(tableName, gbc);

        gbc.gridx = 1;
        gbc.insets.top = fieldInsetsTop;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        base.add(customDelimiterField, gbc);
        base.add(customNameTable, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets.top = labelInsetsTop;
        gbc.fill = GridBagConstraints.NONE;
        filePath = new JLabel(bundleString("FilePath"));
        base.add(filePath, gbc);

        gbc.insets.top = fieldInsetsTop;
        gbc.weightx = 1.0;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        base.add(fileNameField, gbc);
        gbc.weightx = 0;
        gbc.gridx = 2;
        gbc.insets.left = 0;
        gbc.fill = GridBagConstraints.NONE;
        base.add(browse, gbc);

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.bottom = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        base.add(btnPanel, gbc);

        Dimension baseDim = new Dimension(650, 280);
        base.setPreferredSize(baseDim);

        base.setBorder(BorderFactory.createEtchedBorder());

        Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        c.add(base, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));

        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        customDelimiterField.setEnabled(false);
        customNameTable.setVisible(false);
        tableName.setVisible(false);
        exportSQL.setVisible(false);
        queryComboSQL.setVisible(false);
    }


    public void dispose() {
        model = null;
        super.dispose();
    }

    private int getExportFormatType() {
        int index = typeCombo.getSelectedIndex();
        switch (index) {
            case 0:
                return ImportExportDataProcess.DELIMITED;
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

    public void exportTypeChanged(ActionEvent e) {
        int index = typeCombo.getSelectedIndex();
        int indexSQL = queryComboSQL.getSelectedIndex();
        delimCombo.setEnabled(index == 0);
        columnHeadersCheck.setEnabled(index < 2);
        applyQuotesCheck.setEnabled(index < 3);
        custom.setVisible(index == 0);
        customDelimiterField.setVisible(index == 0);
        tableName.setVisible(index == 3);
        customNameTable.setVisible(index == 3);
        customNameTable.setText(tableNameForExport);
        exportSQL.setVisible(index >= 3);
        delimiter.setVisible(index == 0);
        if (index == 3 && indexSQL == 1) {
            fileNameField.setVisible(false);
            browse.setVisible(false);
            filePath.setVisible(false);
        } else {
            fileNameField.setVisible(true);
            browse.setVisible(true);
            filePath.setVisible(true);
        }
        delimCombo.setVisible(index == 0);
        queryComboSQL.setVisible(index >= 3);
    }

    public void delimeterChanged(ActionEvent e) {
        int index = delimCombo.getSelectedIndex();
        boolean enableCustom = (index == 4);
        customDelimiterField.setEnabled(enableCustom);
        if (enableCustom) {
            customDelimiterField.requestFocus();
        }
    }

    public void queryChangedSQL(ActionEvent e) {
        int index = typeCombo.getSelectedIndex();
        int indexSQL = queryComboSQL.getSelectedIndex();
        if (index == 3 && indexSQL == 1) {
            fileNameField.setVisible(false);
            browse.setVisible(false);
            filePath.setVisible(false);
        } else {
            fileNameField.setVisible(true);
            browse.setVisible(true);
            filePath.setVisible(true);
        }
    }

    public void browse(ActionEvent e) {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle(bundleString("SelectExportFilePath"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        String suffix = null;
        File file = fileChooser.getSelectedFile();
        String path = file.getAbsolutePath();

        int exportFormatType = getExportFormatType();
        if (exportFormatType == ImportExportDataProcess.EXCEL) {

            suffix = ".xls";

        } else if (exportFormatType == ImportExportDataProcess.XML) {

            suffix = ".xml";
        }

        path = appendToPath(path, suffix);
        fileNameField.setText(path);
    }

    private String appendToPath(String path, String suffix) {
        if (suffix != null && !path.endsWith(suffix)) {
            path += suffix;
        }
        return path;
    }

    public void cancel(ActionEvent e) {
        dispose();
    }

    public void export(ActionEvent e) {
        String value = fileNameField.getText();

        int index = queryComboSQL.getSelectedIndex();
        if (index != 1) {
            if (MiscUtils.isNull(value)) {
                GUIUtilities.displayErrorMessage(bundleString("YouMustSpecifyAFileToExportTo"));
                return;
            }
        }

        // check if it exists
        if (FileUtils.fileExists(value)) {
            int confirm = GUIUtilities.
                    displayConfirmCancelDialog(Bundles.get("FileChooserDialog.new-command.overwrite-file"));
            if (confirm == JOptionPane.CANCEL_OPTION) {
                return;
            } else if (confirm == JOptionPane.NO_OPTION) {
                fileNameField.selectAll();
                fileNameField.requestFocus();
                return;
            }
        }

        if (getExportFormatType() == ImportExportDataProcess.DELIMITED
                && delimCombo.getSelectedIndex() == 4) {

            value = customDelimiterField.getText();
            if (MiscUtils.isNull(value)) {

                GUIUtilities.displayErrorMessage(bundleString("YouMustEnterACustomDelimiter"));
                return;
            }

        }

        SwingWorker worker = new SwingWorker() {
            public Object construct() {

                return doExport();
            }

            public void finished() {

                GUIUtilities.displayInformationMessage(bundleString("ResultSetExportComplete"));
                dispose();
            }
        };
        worker.start();
    }

    private Object doExport() {
        int exportFormatType = getExportFormatType();
        if (exportFormatType == ImportExportDataProcess.DELIMITED) {

            return exportDelimited();

        } else if (exportFormatType == ImportExportDataProcess.EXCEL) {

            return exportExcel();

        } else if (exportFormatType == ImportExportDataProcess.SQL && queryComboSQL.getSelectedIndex() == 0) {

            return querySQLToFile();

        } else if (exportFormatType == ImportExportDataProcess.SQL && queryComboSQL.getSelectedIndex() == 1) {

            return queryEditor();

        } else {

            return exportXML();
        }
    }

    private Object exportXML() {

        ResultSetTableModelToXMLWriter writer = new ResultSetTableModelToXMLWriter(model, fileNameField.getText());
        try {
            writer.write();

        } catch (ParserConfigurationException e) {

            return handleError(e);

        } catch (TransformerException e) {

            return handleError(e);
        }

        return Bundles.get("PrintPreviewCommand.done");
    }

    private Object handleError(Throwable e) {

        String message = bundleString("ErrorWritingToFile") + e.getMessage();
        GUIUtilities.displayExceptionErrorDialog(message, e);

        return bundleString("Failed");
    }

    private Object exportExcel() {

        OutputStream outputStream = null;
        ResultsProgressDialog progressDialog = null;

        try {

            outputStream = createOutputStream();

            ExcelWorkbookBuilder builder = createExcelWorkbookBuilder();

            builder.createSheet("Result Set Export");

            int rowCount = model.getRowCount();
            int columnCount = model.getColumnCount();

            progressDialog = progressDialog(rowCount);

            List<String> values = new ArrayList<String>(columnCount);

            if (columnHeadersCheck.isSelected()) {

                for (int i = 0; i < columnCount; i++) {

                    values.add(model.getColumnName(i));
                }

                builder.addRowHeader(values);
            }

            for (int i = 0; i < rowCount; i++) {

                values.clear();

                for (int j = 0; j < columnCount; j++) {

                    Object value = model.getValueAt(i, j);
                    values.add(valueAsString(value));
                }

                builder.addRow(values);
                progressDialog.increment(i + 1);
            }

            builder.writeTo(outputStream);

            return Bundles.get("PrintPreviewCommand.done");

        } catch (IOException e) {

            return handleError(e);

        } finally {

            if (progressDialog != null && progressDialog.isVisible()) {
                progressDialog.dispose();
                progressDialog = null;
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }

        }

    }

    private ResultsProgressDialog progressDialog(int rowCount) {

        ResultsProgressDialog progressDialog;
        progressDialog = new ResultsProgressDialog(rowCount);
        setVisible(false);
        progressDialog.pack();

        progressDialog.setLocation(GUIUtilities.getLocationForDialog(progressDialog.getSize()));
        progressDialog.setVisible(true);

        return progressDialog;
    }

    private OutputStream createOutputStream() throws FileNotFoundException {

        return new FileOutputStream(fileNameField.getText(), false);
    }

    private ExcelWorkbookBuilder createExcelWorkbookBuilder() {

        return new DefaultExcelWorkbookBuilder();
    }

    private Object exportDelimited() {
        int delimIndex = delimCombo.getSelectedIndex();
        char delim = 0;

        switch (delimIndex) {
            case 0:
                delim = '|';
                break;
            case 1:
                delim = ',';
                break;
            case 2:
                delim = ';';
                break;
            case 3:
                delim = '#';
                break;
            case 4:
                delim = customDelimiterField.getText().charAt(0);
                break;
        }

        ResultsProgressDialog progressDialog = null;
        PrintWriter writer = null;
        File exportFile = null;

        try {
            exportFile = new File(fileNameField.getText());

            StringBuilder rowLines = new StringBuilder(5000);
            writer = new PrintWriter(new FileWriter(exportFile, false), true);

            int rowCount = model.getRowCount();
            int columnCount = model.getColumnCount();

            progressDialog = progressDialog(rowCount);

            if (columnHeadersCheck.isSelected()) {
                for (int i = 0; i < columnCount; i++) {
                    rowLines.append(model.getColumnName(i));
                    if (i != columnCount - 1) {
                        rowLines.append(delim);
                    }
                }
                writer.println(rowLines);
                rowLines.setLength(0);
            }

            boolean applyQuotes = applyQuotesCheck.isSelected();
            for (int i = 0; i < rowCount; i++) {

                for (int j = 0; j < columnCount; j++) {

                    Object value = model.getValueAt(i, j);
                    if (value instanceof RecordDataItem) {
                        if (applyQuotes && isCDATA((RecordDataItem) value)) {

                            rowLines.append("\"").append(valueAsString(value)).append("\"");

                        } else {

                            rowLines.append(valueAsString(value));
                        }
                    } else {
                        if (model.getColumnClass(j) == String.class && applyQuotes)
                            rowLines.append("\"").append(valueAsString(value).replaceAll("\n", " ")).append("\\n");
                        else
                            rowLines.append(valueAsString(value).replaceAll("\n", "\\n"));
                    }

                    if (j != columnCount - 1) {

                        rowLines.append(delim);
                    }

                }

                writer.println(rowLines);
                rowLines.setLength(0);
                progressDialog.increment(i + 1);
            }

            return Bundles.get("PrintPreviewCommand.done");

        } catch (IOException e) {

            return handleError(e);

        } finally {
            if (progressDialog != null && progressDialog.isVisible()) {
                progressDialog.dispose();
                progressDialog = null;
            }
            if (writer != null) {
                writer.close();
            }
        }

    }

    private boolean isCDATA(RecordDataItem valueAt) {

        int type = valueAt.getDataType();
        return (type == Types.CHAR ||
                type == Types.VARCHAR ||
                type == Types.LONGVARCHAR);
    }

    private boolean isDataTime(RecordDataItem valueAt) {
        int type = valueAt.getDataType();
        return (type == Types.TIME ||
                type == Types.TIMESTAMP);
    }

    private String valueAsString(Object value) {

        if (value instanceof RecordDataItem) {

            RecordDataItem recordDataItem = (RecordDataItem) value;
            if (!recordDataItem.isValueNull()) {

                return recordDataItem.getDisplayValue().toString().replaceAll("'", "''");

            } else {

                return "";
            }

        } else {

            return (value != null ? value.toString().replaceAll("'", "''") : "");
        }

    }


    class ResultsProgressDialog extends JDialog {
        // the progress bar
        private final JProgressBar progressBar;

        public ResultsProgressDialog(int recordCount) {
            super(GUIUtilities.getParentFrame(), bundleString("ExportingQueryResults"), false);
            progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, recordCount);

            JPanel base = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            base.add(new JLabel(bundleString("ExportingResultSet")), gbc);
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.insets.top = 0;
            gbc.ipadx = 180;
            gbc.insets.bottom = 10;
            gbc.fill = GridBagConstraints.BOTH;
            base.add(progressBar, gbc);

            base.setBorder(BorderFactory.createEtchedBorder());
            Container c = this.getContentPane();
            c.setLayout(new GridBagLayout());
            c.add(base, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                    GridBagConstraints.SOUTHEAST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));

            setResizable(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        public void increment(int value) {
            progressBar.setValue(value);
        }

        public void dispose() {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            setVisible(false);
            super.dispose();
        }

    } // class ResultsProgressDialog

    private String bundleString(String key) {

        return Bundles.get(getClass(), key);
    }

    public Object queryEditor() {
        ConnectionsTreePanel connectionsTreePanel = (ConnectionsTreePanel) getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        DatabaseConnection databaseConnection = connectionsTreePanel.getSelectedDatabaseConnection();
        getStatementWriter().writeToOpenEditor(databaseConnection, generateSQLScript());
        return Bundles.get("PrintPreviewCommand.done");
    }

    public Object querySQLToFile() {
        ResultsProgressDialog progressDialog = null;
        PrintWriter writer = null;
        File exportFile = null;
        try {
            exportFile = new File(fileNameField.getText());
            writer = new PrintWriter(new FileWriter(exportFile, false), true);
            writer.println(generateSQLScript());

            return Bundles.get("PrintPreviewCommand.done");

        } catch (IOException e) {

            return handleError(e);

        } finally {
            if (progressDialog != null && progressDialog.isVisible()) {
                progressDialog.dispose();
                progressDialog = null;
            }
            if (writer != null) {
                writer.close();
            }
            if (exportFile != null) {
                exportFile = null;
            }
        }
    }

    public String generateSQLScript() {
        char separator = ',';
        ResultsProgressDialog progressDialog = null;
        StringBuilder stringBuilder = new StringBuilder();
        String nameOfTableForExport = customNameTable.getText();
        try {
            StringBuilder rowLines = new StringBuilder(5000);
            int rowCount = model.getRowCount();
            int columnCount = model.getColumnCount();
            progressDialog = progressDialog(rowCount);

            if (nameOfTableForExport.isEmpty()) {
                nameOfTableForExport = tableNameForExport;
            }
            rowLines.append("/*--uncomment this block if the table does not exist in the database\n");
            if (databaseColumns != null && !databaseColumns.isEmpty()) {
                rowLines.append(((AbstractDatabaseObject) databaseColumns.get(0).getParent()).getCreateSQLText());
            } else {
                rowLines.append("CREATE TABLE ").append(MiscUtils.getFormattedObject(nameOfTableForExport)).append(" (\n");
                for (int i = 0; i < columnCount; i++) {
                    rowLines.append("\t").append(model.getColumnName(i));
                    String type = "BLOB SUB_TYPE TEXT";
                    if (model.getColumnClass(i) == Integer.class)
                        type = "INTEGER";
                    else if (model.getColumnClass(i) == Timestamp.class)
                        type = "TIMESTAMP";
                    rowLines.append(" ").append(type);
                    if (i < columnCount - 1)
                        rowLines.append(",");
                    rowLines.append("\n");
                }
                rowLines.append(");");
            }
            rowLines.append("*/\n");
            boolean applyQuotes = applyQuotesCheck.isSelected();
            StringBuilder insertHeader = new StringBuilder();
            insertHeader.append("\nINSERT INTO ").append(MiscUtils.getFormattedObject(nameOfTableForExport)).append("(");
            boolean first = true;
            for (int countColumnName = 0; countColumnName < model.getColumnCount(); countColumnName++) {
                if (databaseColumns != null && databaseColumns.get(countColumnName).isGenerated())
                    continue;
                if (first) {
                    first = false;
                } else insertHeader.append(separator);
                insertHeader.append("\n").append("\t").append(MiscUtils.getFormattedObject(model.getColumnName(countColumnName)));
            }
            insertHeader.append(")\nVALUES (");
            for (int i = 0; i < rowCount; i++) {

                rowLines.append(insertHeader);
                first = true;
                for (int j = 0; j < columnCount; j++) {
                    if (databaseColumns != null && databaseColumns.get(j).isGenerated())
                        continue;
                    if (first) {
                        first = false;
                    } else rowLines.append(separator);
                    Object value = model.getValueAt(i, j);
                    rowLines.append("\n\t");
                    if (value instanceof RecordDataItem) {
                        if (applyQuotes && isCDATA((RecordDataItem) value)) {

                            if (valueAsString(value).isEmpty()) {
                                rowLines.append("NULL");
                            } else {
                                rowLines.append('\'').append(valueAsString(value)).append('\'');
                            }

                        } else if (isDataTime((RecordDataItem) value)) {

                            String clearText = value.toString();
                            clearText = clearText.replace('T', ' ');
                            rowLines.append('\'' + valueAsString(clearText) + '\'');
                            clearText = null;
                        } else {

                            rowLines.append(valueAsString(value));
                        }
                    } else {
                        if (applyQuotes && model.getColumnClass(j) == String.class) {

                            if (MiscUtils.isNull((String) value)) {
                                rowLines.append("NULL");
                            } else {
                                rowLines.append('\'' + valueAsString(value) + '\'');
                            }

                        } else if (model.getColumnClass(j) == Timestamp.class && value != null) {

                            String clearText = value.toString();
                            clearText = clearText.replace('T', ' ');
                            rowLines.append('\'' + valueAsString(clearText) + '\'');
                            clearText = null;
                        } else {

                            rowLines.append(value);
                        }
                    }

                }
                stringBuilder.append(rowLines + ");");
                rowLines.setLength(0);
                progressDialog.increment(i + 1);
            }

            return stringBuilder.toString();

        } catch (Exception e) {

            return handleError(e).toString();

        } finally {
            if (progressDialog != null && progressDialog.isVisible()) {
                progressDialog.dispose();
                progressDialog = null;
            }
            if (stringBuilder != null) {
                stringBuilder = null;
            }
            if (nameOfTableForExport != null) {
                nameOfTableForExport = null;
            }
        }
    }

    protected TokenizingFormatter getFormatter() {
        if (formatter == null)
            formatter = new TokenizingFormatter();
        return formatter;
    }

    private StatementToEditorWriter getStatementWriter() {
        if (statementWriter == null) {
            statementWriter = new StatementToEditorWriter();
        }
        return statementWriter;
    }
}