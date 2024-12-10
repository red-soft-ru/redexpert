package org.executequery.gui.export;

import org.executequery.actions.filecommands.OpenCommand;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ExportHelperSQL extends AbstractExportHelper {

    private String filePath;
    private String tableName;
    private boolean containsBlob;
    private boolean saveBlobsIndividually;
    private boolean addCreateTableStatement;
    private List<DatabaseColumn> databaseColumns;

    public ExportHelperSQL(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    void extractExportParameters() {
        filePath = parent.getFilePath();
        containsBlob = parent.isContainsBlob();
        tableName = parent.getExportTableName();
        databaseColumns = parent.getDatabaseColumns();
        saveBlobsIndividually = parent.isSaveBlobsIndividually();
        addCreateTableStatement = parent.isAddCreateTableStatement();
    }

    // ---

    @Override
    void exportResultSet(ResultSet resultSet) {
        try (
                FileWriter fileWriter = new FileWriter(filePath, false);
                PrintWriter writer = new PrintWriter(fileWriter, true)
        ) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            List<ColumnData> columns = getCreateColumnData(metaData);
            int columnCount = metaData.getColumnCount();

            String insertTemplate = getInsertTemplate(tableName, columnCount, columns);
            if (addCreateTableStatement)
                writer.println(getCreateTableStatement(databaseColumns, tableName, metaData));
            if (!saveBlobsIndividually && containsBlob)
                writer.println(getSetBlobFileStatement());

            // --- add values to script ---

            int row = 0;
            while (resultSet.next() && !isCancel()) {
                List<String> stringValues = getStringValues(resultSet, columnCount + 1, columns, row);
                writer.println(String.format(insertTemplate, String.join(",", stringValues)));
                row++;
            }

        } catch (Exception e) {
            displayErrorMessage(e);
        }

        openQueryEditor();
    }

    private List<String> getStringValues(ResultSet resultSet, int columnCount, List<ColumnData> columns, int row)
            throws SQLException, IOException {

        List<String> values = new LinkedList<>();
        for (int col = 1; col < columnCount && !isCancel(); col++) {
            if (isFieldSelected(col - 1)) {
                String stringValue = getStringValue(resultSet, columns.get(col - 1), row, col);
                values.add("\n\t" + stringValue);
            }
        }

        return values;
    }

    private String getStringValue(ResultSet resultSet, ColumnData columnData, int row, int col)
            throws SQLException, IOException {

        Object value = resultSet.getObject(col);
        if (value == null)
            return "NULL";

        if (isBlobType(columnData)) {
            return formattedBlob(writeBlob(
                    resultSet.getBlob(col),
                    saveBlobsIndividually,
                    getCreateBlobFileName(columnData, col, row)
            ));
        }

        return formatted(
                getFormattedValue(value, null, ""),
                isCharType(columnData),
                isDateType(columnData)
        );
    }

    // ---

    @Override
    void exportTableModel(TableModel tableModel) {
        try (
                FileWriter fileWriter = new FileWriter(filePath, false);
                PrintWriter writer = new PrintWriter(fileWriter, true)
        ) {

            int rowCount = tableModel.getRowCount();
            int columnCount = tableModel.getColumnCount();

            String insertTemplate = getInsertTemplate(tableName, columnCount, tableModel);
            if (addCreateTableStatement)
                writer.println(getCreateTableStatement(databaseColumns, tableName, tableModel));
            if (!saveBlobsIndividually && containsBlob)
                writer.println(getSetBlobFileStatement());

            // --- add values to script ---

            for (int row = 0; row < rowCount && !isCancel(); row++) {
                List<String> values = getStringValues(tableModel, columnCount, row);
                writer.println(String.format(insertTemplate, String.join(",", values)));
            }

        } catch (Exception e) {
            displayErrorMessage(e);
        }

        openQueryEditor();
    }

    private List<String> getStringValues(TableModel tableModel, int columnCount, int row) throws IOException {

        List<String> values = new LinkedList<>();
        for (int col = 0; col < columnCount && !isCancel(); col++) {
            if (isFieldSelected(col)) {
                String stringValue = getStringValue(tableModel, row, col);
                values.add("\n\t" + stringValue);
            }
        }

        return values;
    }

    private String getStringValue(TableModel tableModel, int row, int col) throws IOException {

        Object value = tableModel.getValueAt(row, col);
        if (value == null)
            return "NULL";

        if (value instanceof RecordDataItem) {

            if (((RecordDataItem) value).isValueNull())
                return "NULL";

            if (isBlobType(value)) {
                return formattedBlob(writeBlob(
                        (AbstractLobRecordDataItem) value,
                        saveBlobsIndividually,
                        getCreateBlobFileName(tableModel, col, row)
                ));
            }
        }

        return formatted(
                getFormattedValue(value, null, ""),
                isCharType(value),
                isDateType(value)
        );
    }

    // ---

    @SuppressWarnings("unchecked")
    private String getInsertTemplate(String tableName, int columnCount, Object columnData) {

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(MiscUtils.getFormattedObject(tableName, null)).append(" (");

        for (int col = 0; col < columnCount; col++) {

            if (!isFieldSelected(col))
                continue;

            String fieldName = null;
            if (columnData instanceof List)
                fieldName = ((List<ColumnData>) columnData).get(col).getColumnName();
            else if (columnData instanceof TableModel)
                fieldName = ((TableModel) columnData).getColumnName(col);

            sb.append("\n\t").append(MiscUtils.getFormattedObject(fieldName, null)).append(",");
        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("\n) VALUES (%s\n);\n");

        return sb.toString();
    }

    private String getCreateTableStatement(List<DatabaseColumn> databaseColumns, String tableName, Object tableData) throws SQLException {

        AbstractDatabaseObject databaseObject = null;
        if (databaseColumns != null && !databaseColumns.isEmpty()) {
            databaseObject = (AbstractDatabaseObject) databaseColumns.get(0).getParent();
            databaseObject.setName(tableName);
        }

        String createTableTemplate = "-- table creating --\n\n";
        if (databaseObject != null) {
            createTableTemplate += databaseObject.getCreateSQLText();

        } else if (tableData instanceof TableModel) {
            createTableTemplate += SQLUtils.generateCreateTable(tableName, (TableModel) tableData);

        } else if (tableData instanceof ResultSetMetaData) {
            createTableTemplate += SQLUtils.generateCreateTable(tableName, (ResultSetMetaData) tableData);

        } else {
            Log.error("Error generating 'CREATE TABLE' template for data export. No data to create table from.");
            return null;
        }

        return createTableTemplate + "\n\n-- inserting data --\n";
    }

    private String getSetBlobFileStatement() {
        return "\nSET BLOBFILE '" + parent.getBlobPath() + "';\n";
    }

    // --- helper methods ---

    private String formatted(String stringValue, boolean isChar, boolean isDate) {

        if (stringValue.isEmpty())
            return "NULL";

        if (isChar)
            return "'" + stringValue + "'";

        if (isDate)
            return "'" + stringValue.replace('T', ' ') + "'";

        return stringValue;
    }

    private String formattedBlob(String value) {

        if (saveBlobsIndividually)
            value = "?'" + value + "'";

        return value;
    }

    private void openQueryEditor() {
        if (parent.isOpenQueryEditor())
            new OpenCommand().openFile(new File(filePath));
    }

}
