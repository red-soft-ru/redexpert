package org.executequery.gui.export;

import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.table.TableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ExportHelperCSV extends AbstractExportHelper {

    private boolean addQuotes;
    private boolean addHeaders;
    private boolean saveBlobsIndividually;

    private String filePath;
    private String columnDelimiter;
    private String endlReplacement;
    private String nullReplacement;

    public ExportHelperCSV(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    protected void extractExportParameters() {
        filePath = parent.getFilePath();
        addQuotes = parent.isAddQuotes();
        addHeaders = parent.isAddHeaders();
        columnDelimiter = parent.getColumnDelimiter();
        endlReplacement = parent.getEndlReplacement();
        nullReplacement = parent.getNullReplacement();
        saveBlobsIndividually = parent.isSaveBlobsIndividually();
    }

    // ---

    @Override
    protected void exportResultSet(ResultSet resultSet) {
        try (
                FileWriter fileWriter = new FileWriter(filePath, false);
                PrintWriter writer = new PrintWriter(fileWriter, true)
        ) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            List<ColumnData> columns = getCreateColumnData(metaData);
            int columnCount = metaData.getColumnCount();

            if (addHeaders)
                writer.println(String.join(columnDelimiter, getHeaders(metaData, columnCount)));

            int row = 0;
            while (resultSet.next() && !isCancel()) {
                List<String> values = getStringValues(resultSet, columns, columnCount + 1, row);
                writer.println(String.join(columnDelimiter, values));
                row++;
            }

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

    private List<String> getStringValues(ResultSet resultSet, List<ColumnData> columns, int columnCount, int row)
            throws SQLException, IOException {

        List<String> values = new LinkedList<>();
        for (int col = 1; col < columnCount && !isCancel(); col++) {
            if (isFieldSelected(col - 1)) {
                String stringValue = getStringValue(resultSet, columns.get(col - 1), row, col);
                values.add(stringValue != null ? stringValue : nullReplacement);
            }
        }

        return values;
    }

    private String getStringValue(ResultSet resultSet, ColumnData columnData, int row, int col)
            throws SQLException, IOException {

        Object value = resultSet.getObject(col);
        if (value == null)
            return null;

        if (isBlobType(columnData)) {
            return writeBlob(
                    resultSet.getBlob(col),
                    saveBlobsIndividually,
                    getCreateBlobFileName(columnData, col, row)
            );
        }

        return formatted(getFormattedValue(value, endlReplacement, nullReplacement), isCharType(columnData));
    }

    // ---

    @Override
    protected void exportTableModel(TableModel tableModel) {
        try (
                FileWriter fileWriter = new FileWriter(filePath, false);
                PrintWriter writer = new PrintWriter(fileWriter, true)
        ) {

            int rowCount = tableModel.getRowCount();
            int columnCount = tableModel.getColumnCount();

            if (addHeaders)
                writer.println(String.join(columnDelimiter, getHeaders(tableModel, columnCount)));

            for (int row = 0; row < rowCount && !isCancel(); row++) {
                List<String> values = getStringValues(tableModel, columnCount, row);
                writer.println(String.join(columnDelimiter, values));
            }

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

    private List<String> getStringValues(TableModel tableModel, int columnCount, int row) throws IOException {

        List<String> values = new LinkedList<>();
        for (int col = 0; col < columnCount && !isCancel(); col++) {
            if (isFieldSelected(col)) {
                String stringValue = getStringValue(tableModel, row, col);
                values.add(stringValue != null ? stringValue : nullReplacement);
            }
        }

        return values;
    }

    private String getStringValue(TableModel tableModel, int row, int col) throws IOException {

        Object value = tableModel.getValueAt(row, col);
        if (value == null)
            return null;

        if (value instanceof RecordDataItem) {

            if (((RecordDataItem) value).isValueNull())
                return null;

            if (isBlobType(value)) {
                return writeBlob(
                        (AbstractLobRecordDataItem) value,
                        saveBlobsIndividually,
                        getCreateBlobFileName(tableModel, col, row)
                );
            }
        }

        return formatted(getFormattedValue(value, endlReplacement, nullReplacement), isCharType(value));
    }

    // --- helper methods ---

    private String formatted(String value, boolean isChar) {

        if (isChar && addQuotes && !value.isEmpty())
            value = "\"" + value + "\"";

        return value;
    }

}
