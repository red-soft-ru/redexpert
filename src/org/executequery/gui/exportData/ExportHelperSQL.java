package org.executequery.gui.exportData;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.table.TableModel;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

public class ExportHelperSQL extends AbstractExportHelper {

    public ExportHelperSQL(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    void exportResultSet(ResultSet resultSet) {

    }

    @Override
    void exportTableModel(TableModel tableModel) {

        String filePath = parent.getFilePath();
        String blobPath = parent.getBlobPath();
        String tableName = parent.getExportTableName();

        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();
        boolean addCreateTableStatement = parent.isAddCreateTableStatement();
        boolean openQueryEditor = parent.isOpenQueryEditor();

        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();
        List<DatabaseColumn> databaseColumns = parent.getDatabaseColumns();

        StringBuilder result = new StringBuilder();
        try {

            // --- add 'create table' statement ---

            if (addCreateTableStatement) {

                AbstractDatabaseObject databaseObject = null;
                if (databaseColumns != null && !databaseColumns.isEmpty()) {
                    databaseObject = (AbstractDatabaseObject) databaseColumns.get(0).getParent();
                    databaseObject.setName(tableName);
                }

                String createTableTemplate = "-- table creating --\n\n"
                        + (databaseObject != null ? databaseObject.getCreateSQLText() : SQLUtils.generateCreateTable(tableName, tableModel))
                        + "\n-- inserting data --\n";

                result.append(createTableTemplate);
            }

            // --- setup *.lob file ---

            if (!saveBlobsIndividually && parent.isContainsBlob())
                result.append("\nSET BLOBFILE '").append(blobPath).append("';\n");

            // --- create 'insert into' template ---

            StringBuilder insertTemplate = new StringBuilder();
            insertTemplate.append("\nINSERT INTO ").append(MiscUtils.getFormattedObject(tableName, null)).append("(");

            for (int col = 0; col < columnCount; col++) {

                if (!isFieldSelected(col))
                    continue;

                insertTemplate.append("\n\t")
                        .append(MiscUtils.getFormattedObject(tableModel.getColumnName(col), null))
                        .append(",");
            }

            insertTemplate.deleteCharAt(insertTemplate.lastIndexOf(","));
            insertTemplate.append("\n) VALUES (%s\n);\n");

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

                        stringValue = writeBlobToFile((AbstractLobRecordDataItem) value, saveBlobsIndividually, getCreateBlobFileName(tableModel, col, row));
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
                result.append(String.format(insertTemplate.toString(), values));
                values.setLength(0);
            }

            String generatedSqlScript = result.toString().trim();
            PrintWriter writer = new PrintWriter(new FileWriter(filePath, false), true);
            writer.println(generatedSqlScript);
            writer.close();

            if (openQueryEditor) {
                GUIUtilities.addCentralPane(
                        QueryEditor.TITLE, QueryEditor.FRAME_ICON,
                        new QueryEditor(generatedSqlScript), null, true
                );
            }

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

}
