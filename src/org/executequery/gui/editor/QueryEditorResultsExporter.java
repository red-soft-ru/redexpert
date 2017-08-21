/*
 * QueryEditorResultsExporter.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.DefaultInlineFieldButton;
import org.executequery.gui.importexport.DefaultExcelWorkbookBuilder;
import org.executequery.gui.importexport.ExcelWorkbookBuilder;
import org.executequery.gui.importexport.ImportExportDataProcess;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.gui.resultset.ResultSetTableModelToXMLWriter;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.CharLimitedTextField;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1767 $
 * @date     $Date: 2017-08-16 22:26:50 +1000 (Wed, 16 Aug 2017) $
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
    
    public QueryEditorResultsExporter(TableModel model) {

        super(GUIUtilities.getParentFrame(), "Export Query Results", true);
        this.model = model;
        init();
        
        pack();
        this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
    }
    
    private void init() {

        ReflectiveAction action = new ReflectiveAction(this);       
        
        String[] delims = {"Pipe","Comma","Semi-colon","Hash","Custom"};
        delimCombo = ActionUtilities.createComboBox(action, delims, "delimeterChanged");
        
        String[] types = {"Delimited File", "Excel Spreadsheet", "XML"};
        typeCombo = ActionUtilities.createComboBox(action, types, "exportTypeChanged");
        
        customDelimField = new CharLimitedTextField(1);
        fileNameField = WidgetFactory.createTextField();
        
        JButton browseButton = new DefaultInlineFieldButton(action);
        browseButton.setText("Browse");
        browseButton.setActionCommand("browse");
        
        JButton okButton = new DefaultPanelButton(action, "OK", "export");
        JButton cancelButton = new DefaultPanelButton(action, "Cancel", "cancel");
        
        columnHeadersCheck = new JCheckBox("Include column names as first row");
        applyQuotesCheck = new JCheckBox("Use double quotes for char/varchar/longvarchar columns", true);
        
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

        // the base panel
        JPanel base = new JPanel(new GridBagLayout());
        gbc.insets = new Insets(8,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        base.add(new JLabel("Select the export type, delimiter and file path below."), gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.bottom = 0;
        base.add(columnHeadersCheck, gbc);
        gbc.gridy++;

        base.add(applyQuotesCheck, gbc);
        gbc.gridy++;

        gbc.gridwidth = 1;
        gbc.insets.bottom = 0;
        gbc.insets.top = 6;
        base.add(new JLabel("File Format:"), gbc);
        gbc.gridx = 1;
        gbc.insets.top = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        base.add(typeCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets.bottom = 0;
        gbc.insets.top = 6;
        base.add(new JLabel("Delimiter:"), gbc);
        gbc.gridx = 1;
        gbc.insets.top = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        base.add(delimCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = 5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        base.add(new JLabel("Custom:"), gbc);
        gbc.gridx = 1;
        gbc.insets.top = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        base.add(customDelimField, gbc);        
        gbc.insets.top = 6;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        base.add(new JLabel("File Path:"), gbc);
        gbc.insets.top = 5;
        gbc.weightx = 1.0;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        base.add(fileNameField, gbc);
        gbc.weightx = 0;
        gbc.gridx = 2;
        gbc.insets.left = 0;
        gbc.fill = GridBagConstraints.NONE;
        base.add(browseButton, gbc);
        
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.bottom = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        base.add(btnPanel, gbc);
        
        Dimension baseDim = new Dimension(550, 240);
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
            default:
                return ImportExportDataProcess.DELIMITED;
        }

    }
    
    public void exportTypeChanged(ActionEvent e) {
        int index = typeCombo.getSelectedIndex();
        delimCombo.setEnabled(index == 0);
        columnHeadersCheck.setEnabled(index != 2);
    }
    
    public void delimeterChanged(ActionEvent e) {
        int index = delimCombo.getSelectedIndex();
        boolean enableCustom = (index == 4);
        customDelimField.setEnabled(enableCustom);
        if (enableCustom) {
            customDelimField.requestFocus();
        }
    }
    
    public void browse(ActionEvent e) {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle("Select Export File Path");
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
        if (MiscUtils.isNull(value)) {
            GUIUtilities.displayErrorMessage("You must specify a file to export to.");
            return;
        }
        
        // check if it exists
        if (FileUtils.fileExists(value)) {
            int confirm = GUIUtilities.
                    displayConfirmCancelDialog("Overwrite existing file?");
            if (confirm == JOptionPane.CANCEL_OPTION) {
                return;
            } 
            else if (confirm == JOptionPane.NO_OPTION) {
                fileNameField.selectAll();
                fileNameField.requestFocus();
                return;
            }
        }
        
        if (getExportFormatType() == ImportExportDataProcess.DELIMITED 
                && delimCombo.getSelectedIndex() == 4) {

            value = customDelimField.getText();
            if (MiscUtils.isNull(value)) {

                GUIUtilities.displayErrorMessage("You must enter a custom delimeter");
                return;
            }

        }

        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                
                return doExport();
            }
            public void finished() {

                GUIUtilities.displayInformationMessage("Result set export complete.");
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

        return "done";
    }

    private Object handleError(Throwable e) {

        String message = "Error writing to file:\n\n" + e.getMessage();
        GUIUtilities.displayExceptionErrorDialog(message, e);
      
        return "failed";
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
                progressDialog.increment(i+1);
            }
            
            builder.writeTo(outputStream);
            
            return "done";

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
                } catch (IOException e) {}
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
                writer.println(rowLines.toString());
                rowLines.setLength(0);
            }
            
            boolean applyQuotes = applyQuotesCheck.isSelected();
            for (int i = 0; i < rowCount; i++) {

                for (int j = 0; j < columnCount; j++) {

                    Object value = model.getValueAt(i, j);
                    if (applyQuotes && isCDATA((RecordDataItem) value)) {
                    
                        rowLines.append("\""+valueAsString(value)+"\"");
                    
                    } else {
                        
                        rowLines.append(valueAsString(value));
                    }
                    
                    if (j != columnCount - 1) {

                        rowLines.append(delim);
                    }
                    
                }

                writer.println(rowLines.toString());
                rowLines.setLength(0);
                progressDialog.increment(i+1);
            }
            
            return "done";

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
        // the progess bar
        private JProgressBar progressBar;

        public ResultsProgressDialog(int recordCount) {
            super(GUIUtilities.getParentFrame(), "Exporting Query Results", false);
            progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, recordCount);

            JPanel base = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(5,5,5,5);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            base.add(new JLabel("Exporting result set..."), gbc);
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
            } catch (InterruptedException e) {}
            setVisible(false);
            super.dispose();
        }

    } // class ResultsProgressDialog
    
}










