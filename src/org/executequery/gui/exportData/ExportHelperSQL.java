package org.executequery.gui.exportData;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.table.TableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class ExportHelperSQL extends AbstractExportHelper {

    public ExportHelperSQL(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    void exportResultSet(ResultSet resultSet) {

        String tableName = parent.getExportTableName();
        boolean addCreateTableStatement = parent.isAddCreateTableStatement();
        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();

        StringBuilder result = new StringBuilder();
        try {

            ResultSetMetaData metaData = resultSet.getMetaData();
            List<ColumnData> columns = getCreateColumnData(metaData);
            int columnCount = metaData.getColumnCount();
            List<DatabaseColumn> databaseColumns = parent.getDatabaseColumns();

            String insertTemplate = getInsertTemplate(tableName, columnCount, columns);
            if (addCreateTableStatement)
                result.append(getCreateTableStatement(databaseColumns, tableName, metaData));
            if (!saveBlobsIndividually && parent.isContainsBlob())
                result.append(getSetBlobFileStatement());

            // --- add values to script ---

            int row = 0;
            StringBuilder values = new StringBuilder();
            while (resultSet.next()) {

                if (isCancel())
                    break;

                for (int col = 1; col < columnCount + 1; col++) {

                    if (isCancel())
                        break;

                    if (!isFieldSelected(col - 1))
                        continue;

                    values.append("\n\t");

                    Object value = resultSet.getObject(col);
                    ColumnData columnData = columns.get(col - 1);
                    String stringValue = getFormattedValue(value, null, "");

                    if (value == null) {
                        values.append("NULL");

                    } else if (isBlobType(columnData)) {

                        stringValue = writeBlob(resultSet.getBlob(col), saveBlobsIndividually, getCreateBlobFileName(columnData, col, row));
                        if (saveBlobsIndividually)
                            values.append("?'").append(stringValue).append("'");
                        else
                            values.append(stringValue);

                    } else if (!stringValue.isEmpty()) {

                        if (isCharType(columnData))
                            values.append("'").append(stringValue).append("'");
                        else if (isDateType(columnData))
                            values.append("'").append(stringValue.replace('T', ' ')).append("'");
                        else
                            values.append(stringValue);

                    }

                    values.append(",");
                }
                values.deleteCharAt(values.lastIndexOf(","));
                result.append(String.format(insertTemplate, values));
                values.setLength(0);
                row++;
            }
            write(result.toString().trim());

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

    @Override
    void exportTableModel(TableModel tableModel) {

        String tableName = parent.getExportTableName();
        boolean addCreateTableStatement = parent.isAddCreateTableStatement();
        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();

        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();
        List<DatabaseColumn> databaseColumns = parent.getDatabaseColumns();

        StringBuilder result = new StringBuilder();
        try {

            String insertTemplate = getInsertTemplate(tableName, columnCount, tableModel);
            if (addCreateTableStatement)
                result.append(getCreateTableStatement(databaseColumns, tableName, tableModel));
            if (!saveBlobsIndividually && parent.isContainsBlob())
                result.append(getSetBlobFileStatement());

            // --- add values to script ---

            StringBuilder values = new StringBuilder();
            for (int row = 0; row < rowCount; row++) {

                if (isCancel())
                    break;

                for (int col = 0; col < columnCount; col++) {

                    if (isCancel())
                        break;

                    if (!isFieldSelected(col))
                        continue;

                    values.append("\n\t");

                    Object value = tableModel.getValueAt(row, col);
                    String stringValue = getFormattedValue(value, null, "");

                    if (isBlobType(value)) {

                        stringValue = writeBlob((AbstractLobRecordDataItem) value, saveBlobsIndividually, getCreateBlobFileName(tableModel, col, row));
                        if (saveBlobsIndividually)
                            values.append("?'").append(stringValue).append("'");
                        else
                            values.append(stringValue);

                    } else if (!stringValue.isEmpty()) {

                        if (value instanceof RecordDataItem) {

                            if (isCharType(value))
                                values.append("'").append(stringValue).append("'");
                            else if (isDateType(value))
                                values.append("'").append(stringValue.replace('T', ' ')).append("'");
                            else
                                values.append(stringValue);

                        } else {

                            if (tableModel.getColumnClass(col) == String.class)
                                values.append("'").append(stringValue).append("'");
                            else if (tableModel.getColumnClass(col) == Timestamp.class)
                                values.append("'").append(stringValue.replace('T', ' ')).append("'");
                            else
                                values.append(stringValue);
                        }
                    } else
                        values.append("NULL");

                    values.append(",");
                }
                values.deleteCharAt(values.lastIndexOf(","));
                result.append(String.format(insertTemplate, values));
                values.setLength(0);
            }
            write(result.toString().trim());

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

    private void write(String text) throws IOException {

        PrintWriter writer = new PrintWriter(new FileWriter(parent.getFilePath(), false), true);
        writer.println(text);
        writer.close();

        if (parent.isOpenQueryEditor()) {
            GUIUtilities.addCentralPane(
                    QueryEditor.TITLE, QueryEditor.FRAME_ICON,
                    new QueryEditor(text), null, true
            );
        }
    }

    @SuppressWarnings("unchecked")
    private String getInsertTemplate(String tableName, int columnCount, Object columnData) {

        StringBuilder sb = new StringBuilder();
        sb.append("\nINSERT INTO ").append(MiscUtils.getFormattedObject(tableName, null)).append(" (");

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

        return createTableTemplate + "\n-- inserting data --\n";
    }

    private String getSetBlobFileStatement() {
        return "\nSET BLOBFILE '" + parent.getBlobPath() + "';\n";
    }

}
