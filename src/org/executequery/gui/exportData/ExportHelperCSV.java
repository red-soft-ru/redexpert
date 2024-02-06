package org.executequery.gui.exportData;

import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.table.TableModel;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class ExportHelperCSV extends AbstractExportHelper {

    public ExportHelperCSV(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    protected void exportResultSet(ResultSet resultSet) {

        String filePath = parent.getFilePath();
        String columnDelimiter = parent.getColumnDelimiter();
        String endlReplacement = parent.getEndlReplacement();
        String nullReplacement = parent.getNullReplacement();

        boolean addHeaders = parent.isAddHeaders();
        boolean addQuotes = parent.isAddQuotes();
        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();

        try {

            ResultSetMetaData metaData = resultSet.getMetaData();
            List<ColumnData> columns = getCreateColumnData(metaData);
            int columnCount = metaData.getColumnCount();

            StringBuilder resultText = new StringBuilder();
            PrintWriter writer = new PrintWriter(new FileWriter(filePath, false), true);

            if (addHeaders)
                writer.println(getHeaders(columnCount, columnDelimiter, metaData));

            int row = 0;
            while (resultSet.next()) {

                if (isCancel())
                    break;

                for (int col = 1; col < columnCount + 1; col++) {

                    if (isCancel())
                        break;

                    if (!isFieldSelected(col - 1))
                        continue;

                    String stringValue = null;
                    Object value = resultSet.getObject(col);
                    ColumnData columnData = columns.get(col - 1);

                    if (value != null) {
                        stringValue = getFormattedValue(value, endlReplacement, nullReplacement);

                        if (isCharType(columnData) && addQuotes && !stringValue.isEmpty()) {
                            stringValue = "\"" + stringValue + "\"";

                        } else if (isBlobType(columnData)) {
                            stringValue = writeBlob(resultSet.getBlob(col), saveBlobsIndividually, getCreateBlobFileName(columnData, col, row));
                        }
                    }

                    resultText.append(stringValue != null ? stringValue : nullReplacement);
                    resultText.append(columnDelimiter);
                }
                resultText.deleteCharAt(resultText.length() - 1);

                writer.println(resultText);
                resultText.setLength(0);
                row++;
            }
            writer.close();

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

    @Override
    protected void exportTableModel(TableModel tableModel) {

        String filePath = parent.getFilePath();
        String columnDelimiter = parent.getColumnDelimiter();
        String endlReplacement = parent.getEndlReplacement();
        String nullReplacement = parent.getNullReplacement();

        boolean addHeaders = parent.isAddHeaders();
        boolean addQuotes = parent.isAddQuotes();
        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();

        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();

        try {
            StringBuilder resultText = new StringBuilder();
            PrintWriter writer = new PrintWriter(new FileWriter(filePath, false), true);

            if (addHeaders)
                writer.println(getHeaders(columnCount, columnDelimiter, tableModel));

            for (int row = 0; row < rowCount; row++) {

                if (isCancel())
                    break;

                for (int col = 0; col < columnCount; col++) {

                    if (isCancel())
                        break;

                    if (!isFieldSelected(col))
                        continue;

                    String stringValue = null;
                    Object value = tableModel.getValueAt(row, col);
                    if (value instanceof RecordDataItem) {
                        RecordDataItem rdi = (RecordDataItem) value;

                        if (!rdi.isValueNull()) {
                            stringValue = getFormattedValue(rdi, endlReplacement, nullReplacement);

                            if (isCharType(rdi) && addQuotes && !stringValue.isEmpty()) {
                                stringValue = "\"" + stringValue + "\"";

                            } else if (isBlobType(rdi)) {
                                stringValue = writeBlob((AbstractLobRecordDataItem) rdi, saveBlobsIndividually, getCreateBlobFileName(tableModel, col, row));
                            }
                        }
                    } else {
                        if (value != null) {
                            stringValue = getFormattedValue(value, endlReplacement, nullReplacement);
                            if (isCharType(value) && addQuotes && !stringValue.isEmpty()) {
                                stringValue = "\"" + stringValue + "\"";
                            }
                        }
                    }

                    resultText.append(stringValue != null ? stringValue : nullReplacement);
                    resultText.append(columnDelimiter);
                }
                resultText.deleteCharAt(resultText.length() - 1);

                writer.println(resultText);
                resultText.setLength(0);
            }
            writer.close();

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

    private String getHeaders(int columnCount, String columnDelimiter, Object columnData) throws SQLException {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < columnCount; i++) {
            if (isFieldSelected(i)) {

                String columnName = null;
                if (columnData instanceof ResultSetMetaData)
                    columnName = ((ResultSetMetaData) columnData).getColumnName(i + 1);
                else if (columnData instanceof TableModel)
                    columnName = ((TableModel) columnData).getColumnName(i);

                sb.append(columnName).append(columnDelimiter);
            }
        }

        return sb.deleteCharAt(sb.length() - 1).toString().trim();
    }

}
