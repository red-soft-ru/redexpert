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

import com.github.lgooddatepicker.components.DatePicker;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultDatabaseConnection;
import org.executequery.databaseobjects.*;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.StandardTable;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.*;
import org.executequery.gui.browser.comparer.Table;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.gui.erd.ErdTable;
import org.executequery.gui.importexport.DefaultExcelWorkbookBuilder;
import org.executequery.gui.importexport.ExcelWorkbookBuilder;
import org.executequery.gui.importexport.ImportExportDataProcess;
import org.executequery.gui.resultset.*;
import org.executequery.gui.scriptgenerators.ScriptGenerator;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.EditorSQLShortcut;
import org.executequery.repository.RepositoryCache;
import org.executequery.sql.TokenizingFormatter;
import org.executequery.util.UserProperties;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.swing.table.SortableTableModel;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.executequery.GUIUtilities.getDockedTabComponent;
import static org.executequery.gui.browser.BrowserTableEditingPanel.tableNameForExport;


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
    private JTextField customDelimField;

    // the file text field
    private JTextField fileNameField;

    // The table model to be exported
    private TableModel model;

    // The sender to QueryEditor
    private static StatementToEditorWriter statementWriter;

    // SQL format to QueryEditor
    TokenizingFormatter formatter;

    // Used to view the contents of a file
    private JButton browse;

    // The text to specify the path to the file
    private JLabel filePath;

    // custom name table
    private JTextField customNameTable;

    // The text to specify the custom separator
    private JLabel Custom;

    // The text to specify the separator
    private JLabel Delimiter;

    // The text to specify the name of table
    private JLabel TableName;

    // The text to specify the SQL export
    private JLabel SQLExport;

    // the SQL Query export combo
    private JComboBox SQLQueryCombo;


    public QueryEditorResultsExporter(TableModel model) {

        super(GUIUtilities.getParentFrame(), Bundles.get("QueryEditorResultsExporter.ExportQueryResults"), true);
        this.model = model;
        init();

        pack();
        this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
    }


    private void init() {

        ReflectiveAction action = new ReflectiveAction(this);

        String[] delims = {"Pipe", "Comma", "Semi-colon", "Hash", "Custom"};
        delimCombo = ActionUtilities.createComboBox(action, delims, "delimeterChanged");

        String[] SQLQuery = {"Query Editor In File", "To Query Editor"};
        SQLQueryCombo = ActionUtilities.createComboBox(action, SQLQuery, "SQLQueryChanged");

        String[] types = {"Delimited File", "Excel Spreadsheet", "XML", "SQL"};
        typeCombo = ActionUtilities.createComboBox(action, types, "exportTypeChanged");

        customDelimField = new CharLimitedTextField(1);
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
        Delimiter = new JLabel(bundleString("Delimiter"));
        SQLExport = new JLabel(bundleString("SQLExport"));
        base.add(Delimiter, gbc);
        base.add(SQLExport, gbc);
        gbc.gridx = 1;
        gbc.insets.top = fieldInsetsTop;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        base.add(delimCombo, gbc);
        base.add(SQLQueryCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = labelInsetsTop;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        Custom = new JLabel(bundleString("Custom"));
        base.add(Custom, gbc);
        TableName = new JLabel(bundleString("TableName"));
        base.add(TableName, gbc);

        gbc.gridx = 1;
        gbc.insets.top = fieldInsetsTop;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        base.add(customDelimField, gbc);
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
        customDelimField.setEnabled(false);
        customNameTable.setVisible(false);
        TableName.setVisible(false);
        SQLExport.setVisible(false);
        SQLQueryCombo.setVisible(false);
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
        int index2 = SQLQueryCombo.getSelectedIndex();
        delimCombo.setEnabled(index == 0);
        columnHeadersCheck.setEnabled(index < 2);
        applyQuotesCheck.setEnabled(index < 3);
        Custom.setVisible(index != 3);
        customDelimField.setVisible(index < 3);
        TableName.setVisible(index >= 3);
        customNameTable.setVisible(index >= 3);
        customNameTable.setText(tableNameForExport);
        SQLExport.setVisible(index >= 3);
        Delimiter.setVisible(index < 3);
        if(index == 3 && index2 == 1){
            fileNameField.setVisible(false);
            browse.setVisible(false);
            filePath.setVisible(false);
        }
        else{
            fileNameField.setVisible(true);
            browse.setVisible(true);
            filePath.setVisible(true);
        }
        delimCombo.setVisible(index < 3);
        SQLQueryCombo.setVisible(index >= 3);
    }

    public void delimeterChanged(ActionEvent e) {
        int index = delimCombo.getSelectedIndex();
        boolean enableCustom = (index == 4);
        customDelimField.setEnabled(enableCustom);
        if (enableCustom) {
            customDelimField.requestFocus();
        }
    }

    public void SQLQueryChanged(ActionEvent e){
        int index2 = SQLQueryCombo.getSelectedIndex();
        int index = typeCombo.getSelectedIndex();
        if(index == 3 && index2 == 1){
            fileNameField.setVisible(false);
            browse.setVisible(false);
            filePath.setVisible(false);
        }
        else{
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

        int index = SQLQueryCombo.getSelectedIndex();
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

            value = customDelimField.getText();
            if (MiscUtils.isNull(value)) {

                GUIUtilities.displayErrorMessage(bundleString("YouMustEnterACustomDelimeter"));
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

        } else if (exportFormatType == ImportExportDataProcess.SQL && SQLQueryCombo.getSelectedIndex() == 0) {

            return SQLQueryToFile();

        } else if (exportFormatType == ImportExportDataProcess.SQL && SQLQueryCombo.getSelectedIndex() == 1) {

            return QueryEditor();

        } else {

            return exportXML();
        }
    }

    private Object exportXML() {

        ResultSetTableModelToXMLWriter writer = new ResultSetTableModelToXMLWriter((ResultSetTableModel) model, fileNameField.getText());
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
                delim = customDelimField.getText().charAt(0);
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
                    if (applyQuotes && isCDATA((RecordDataItem) value)) {

                        rowLines.append("\"" + valueAsString(value) + "\"");

                    } else {

                        rowLines.append(valueAsString(value));
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

                return recordDataItem.getDisplayValue().toString();

            } else {

                return "";
            }

        } else {

            return (value != null ? value.toString() : "");
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

    public Object QueryEditor() {
        ConnectionsTreePanel connectionsTreePanel = (ConnectionsTreePanel) getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        DatabaseConnection databaseConnection = connectionsTreePanel.getSelectedDatabaseConnection();
        getStatementWriter().writeToOpenEditor(databaseConnection, SQLQueryInQueryEditor());
        return Bundles.get("PrintPreviewCommand.done");
    }


    public Object SQLQueryToFile() {
        char Separator = ',';
        ResultsProgressDialog progressDialog = null;
        PrintWriter writer = null;
        File exportFile = null;
        String tableNameForExport = customNameTable.getText();
        try {
            exportFile = new File(fileNameField.getText());

            StringBuilder rowLines = new StringBuilder(5000);
            writer = new PrintWriter(new FileWriter(exportFile, false), true);

            int rowCount = model.getRowCount();
            int columnCount = model.getColumnCount();

            progressDialog = progressDialog(rowCount);

            if(tableNameForExport.isEmpty()== true){
                tableNameForExport = tableNameForExport;
            }

            boolean applyQuotes = applyQuotesCheck.isSelected();
            for (int i = 0; i < rowCount; i++) {
                rowLines.append("INSERT INTO " + tableNameForExport + '(');
                for (int countColumnName = 0; countColumnName < model.getColumnCount() - 1; countColumnName++) {
                    rowLines.append('\"' + model.getColumnName(countColumnName) + '\"' + Separator);
                }
                rowLines.append('\"' + model.getColumnName(model.getColumnCount() - 1) + "\" ) VALUES (");
                for (int j = 0; j < columnCount; j++) {
                    Object value = model.getValueAt(i, j);
                    if (applyQuotes && isCDATA((RecordDataItem) value)) {
                        if (valueAsString(value) == "") {
                            rowLines.append("NULL");
                        } else {
                            rowLines.append('\'' + valueAsString(value) + '\'');
                        }
                    } else if (isDataTime((RecordDataItem) value)) {
                        String clearText = value.toString();
                        clearText = clearText.replace('T', ' ');
                        rowLines.append('\'' + valueAsString(clearText) + '\'');
                        clearText = null;
                    } else {
                        rowLines.append(valueAsString(value));
                    }

                    if (j != columnCount - 1) {
                        rowLines.append(Separator);
                    }

                }

                writer.println(rowLines + ");");
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
            if (exportFile != null) {
                exportFile = null;
            }
            if (tableNameForExport != null) {
                tableNameForExport = null;
            }
        }
    }

    public String SQLQueryInQueryEditor() {
        char Separator = ',';
        ResultsProgressDialog progressDialog = null;
        StringBuilder stringBuilder = new StringBuilder();
        String tableNameForExport = customNameTable.getText();
        try {
            StringBuilder rowLines = new StringBuilder(5000);
            stringBuilder.setLength(0);
            int rowCount = model.getRowCount();
            int columnCount = model.getColumnCount();
            String query = "";
            progressDialog = progressDialog(rowCount);

            if(tableNameForExport.isEmpty()== true){
                tableNameForExport = tableNameForExport;
            }

            boolean applyQuotes = applyQuotesCheck.isSelected();
            for (int i = 0; i < rowCount; i++) {
                rowLines.append("INSERT INTO " + tableNameForExport + '(');
                for (int countColumnName = 0; countColumnName < model.getColumnCount() - 1; countColumnName++) {
                    rowLines.append('\"' + model.getColumnName(countColumnName) + '\"' + Separator);
                }
                rowLines.append('\"' + model.getColumnName(model.getColumnCount() - 1) + "\" ) VALUES (");
                for (int j = 0; j < columnCount; j++) {

                    Object value = model.getValueAt(i, j);
                    if (applyQuotes && isCDATA((RecordDataItem) value)) {

                        if (valueAsString(value) == "") {
                            rowLines.append("NULL");
                        } else {
                            rowLines.append('\'' + valueAsString(value) + '\'');
                        }

                    } else if (isDataTime((RecordDataItem) value)) {

                        String clearText = value.toString();
                        clearText = clearText.replace('T', ' ');
                        rowLines.append('\'' + valueAsString(clearText) + '\'');
                        clearText = null;
                    } else {

                        rowLines.append(valueAsString(value));
                    }

                    if (j != columnCount - 1) {

                        rowLines.append(Separator);
                    }

                }
                stringBuilder.append(rowLines + ");");
                rowLines.setLength(0);

                query += stringBuilder;
                stringBuilder.setLength(0);
                progressDialog.increment(i + 1);
            }

            return getFormatter().format(query);

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
            if (tableNameForExport != null) {
                tableNameForExport = null;
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