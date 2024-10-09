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

package org.executequery.gui.exportData;

import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.*;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.*;


/**
 * @author Takis Diakoumis
 */
public class ExportDataPanel extends AbstractBaseDialog {

    public static final String TITLE = bundleString("title");

    private static final int XML = 0;
    private static final int DELIMITED = XML + 1;
    private static final int EXCEL = DELIMITED + 1;
    private static final int SQL = EXCEL + 1;

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

    private final Object exportData;
    private final String tableNameForExport;
    private final ParameterSaver parametersSaver;
    private final List<DatabaseColumn> databaseColumns;

    // ---

    public ExportDataPanel(Object exportData, String tableNameForExport) {
        this(exportData, tableNameForExport, null);
    }

    public ExportDataPanel(Object exportData, String tableNameForExport, List<DatabaseColumn> databaseColumns) {
        super(GUIUtilities.getParentFrame(), TITLE, true);

        this.exportData = exportData;
        this.parametersSaver = new ParameterSaver(ExportDataPanel.class.getName());
        this.tableNameForExport = tableNameForExport;
        this.databaseColumns = databaseColumns;

        if (exportData == null) {
            GUIUtilities.displayWarningMessage(bundleString("NoDataForExport"));
            super.dispose();
            return;
        }

        init();
        arrange();
    }

    private void init() {
        Map<String, Component> components = new HashMap<>();

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
        columnDelimiterCombo.setEditable(true);
        components.put(columnDelimiterCombo.getName(), columnDelimiterCombo);

        browseFileButton = WidgetFactory.createButton("browseFileButton", Bundles.get("common.browse.button"));
        browseFileButton.addActionListener(e -> browseFile(filePathField));

        browseBlobFileButton = WidgetFactory.createButton("browseFolderButton", Bundles.get("common.browse.button"));
        browseBlobFileButton.addActionListener(e -> browseFile(blobPathField));
        browseBlobFileButton.setEnabled(isContainsBlob());

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
        saveBlobsIndividuallyCheck.setEnabled(isContainsBlob());
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
        blobPathField.setEnabled(isContainsBlob());
        components.put(blobPathField.getName(), blobPathField);

        filePathField = WidgetFactory.createTextField("filePathField");
        components.put(filePathField.getName(), filePathField);

        delimiterLabel = new JLabel(bundleString("delimiterLabel"));
        exportTableNameLabel = new JLabel(bundleString("exportTableNameField"));

        parametersSaver.set(components);
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
        parametersSaver.restore();

        replaceEndlField.setEnabled(replaceEndlCheck.isSelected());
        replaceNullField.setEnabled(replaceNullCheck.isSelected());

        setLayout(new GridBagLayout());
        add(basePanel, gridBagHelper.fillBoth().get());

        setPreferredSize(new Dimension(500, 455));
        setMinimumSize(getPreferredSize());
        setSize(getPreferredSize());
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
            case (EXCEL):
                showXlsxPanel();
                validExtension = ".xlsx";
                break;
            case (XML):
                showXmlPanel();
                validExtension = ".xml";
                break;
            case (SQL):
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
                case (EXCEL):
                    suffix = ".xlsx";
                    break;
                case (XML):
                    suffix = ".xml";
                    break;
                case (SQL):
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

    private void export() {

        if (exportAllow()) {
            Objects.requireNonNull(getExportHelper()).export(exportData);
            GUIUtilities.displayInformationMessage(bundleString("ResultSetExportComplete"));
            dispose();
        }
    }

    // --- helper method ---

    private boolean exportAllow() {

        String exportFilePath = getFilePath();
        String exportBlobPath = getBlobPath();

        // export file defined
        if (MiscUtils.isNull(exportFilePath)) {
            GUIUtilities.displayErrorMessage(bundleString("YouMustSpecifyAFileToExportTo"));
            return false;
        }

        // export file writable
        if (!new File(exportFilePath).getAbsoluteFile().getParentFile().exists()) {
            GUIUtilities.displayErrorMessage(bundleString("FileNotWritable", exportFilePath));
            return false;
        }

        // blob file defined
        if (isContainsBlob() && MiscUtils.isNull(exportBlobPath)) {
            GUIUtilities.displayErrorMessage(bundleString("YouMustSpecifyAFileToExportTo"));
            return false;
        }

        // blob file writable
        if (!new File(exportBlobPath).getAbsoluteFile().getParentFile().exists()) {
            GUIUtilities.displayErrorMessage(bundleString("FileNotWritable", exportBlobPath));
            return false;
        }

        // fields for export defined
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
                return false;
            }
        }

        // overwrite export file check
        if (FileUtils.fileExists(exportFilePath)) {

            int result = GUIUtilities.displayYesNoDialog(
                    String.format(bundleString("OverwriteFile"), exportFilePath),
                    Bundles.get("common.confirmation")
            );

            if (result == JOptionPane.NO_OPTION) {
                filePathField.selectAll();
                filePathField.requestFocus();
                return false;
            }
        }

        // overwrite blob file check
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
                        return false;
                    }
                }

                try {
                    Files.write(Paths.get(exportBlobPath), "".getBytes(StandardCharsets.UTF_8));
                } catch (IOException ignored) {
                }

            } else
                //noinspection ResultOfMethodCallIgnored
                new File(exportBlobPath).mkdirs();
        }

        return true;
    }

    protected boolean isFieldSelected(int fieldIndex) {

        Object value = columnTable.getValueAt(fieldIndex, 0);
        if (value == null)
            return true;

        return value.toString().toLowerCase().contains("true");
    }

    protected boolean isContainsBlob() {

        if (exportData instanceof TableModel) {

            TableModel tableModel = (TableModel) exportData;
            for (int col = 0; col < tableModel.getColumnCount(); col++)
                if (isFieldSelected(col) && tableModel.getValueAt(0, col) instanceof AbstractLobRecordDataItem)
                    return true;

        } else if (exportData instanceof ResultSet) {
            try {

                ResultSetMetaData metaData = ((ResultSet) exportData).getMetaData();
                for (int i = 1; i < metaData.getColumnCount() + 1; i++)
                    if (metaData.getColumnTypeName(i).toLowerCase().contains("blob"))
                        return true;

            } catch (SQLException e) {
                Log.error(e.getMessage(), e);
            }
        }

        return false;
    }

    private int getExportFileType() {

        switch (typeCombo.getSelectedIndex()) {
            case 1:
                return EXCEL;
            case 2:
                return XML;
            case 3:
                return SQL;
            default:
                return DELIMITED;
        }
    }

    private ExportHelper getExportHelper() {

        switch (getExportFileType()) {

            case DELIMITED:
                return new ExportHelperCSV(this);
            case EXCEL:
                return new ExportHelperXLSX(this);
            case XML:
                return new ExportHelperXML(this);
            case SQL:
                return new ExportHelperSQL(this);
            default:
                return null;
        }
    }

    // --- getters ---

    protected List<DatabaseColumn> getDatabaseColumns() {
        return databaseColumns;
    }

    protected String getColumnDelimiter() {
        return Objects.requireNonNull(columnDelimiterCombo.getSelectedItem()).toString();
    }

    protected String getEndlReplacement() {
        return replaceEndlCheck.isSelected() ? replaceEndlField.getText().trim() : null;
    }

    protected String getNullReplacement() {
        return replaceNullCheck.isSelected() ? replaceNullField.getText().trim() : "";
    }

    protected String getExportTableName() {
        return !exportTableNameField.getText().isEmpty() ? exportTableNameField.getText().trim() : tableNameForExport;
    }

    protected String getFilePath() {
        return filePathField.getText().trim();
    }

    protected String getBlobPath() {
        return blobPathField.getText().trim();
    }

    protected boolean isAddHeaders() {
        return addColumnHeadersCheck.isSelected();
    }

    protected boolean isAddQuotes() {
        return addQuotesCheck.isSelected();
    }

    protected boolean isSaveBlobsIndividually() {
        return saveBlobsIndividuallyCheck.isSelected();
    }

    protected boolean isAddCreateTableStatement() {
        return addCreateTableStatementCheck.isSelected();
    }

    protected boolean isOpenQueryEditor() {
        return openQueryEditorCheck.isSelected();
    }

    // --- inner classes ---

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

    // ---

    @Override
    public void dispose() {
        parametersSaver.save();
        super.dispose();
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(ExportDataPanel.class, key, args);
    }

}
