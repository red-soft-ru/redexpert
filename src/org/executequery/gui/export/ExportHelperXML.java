package org.executequery.gui.export;

import org.executequery.Constants;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;
import org.underworldlabs.util.MiscUtils;

import javax.swing.table.TableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class ExportHelperXML extends AbstractExportHelper {

    public static final String DATA_INDENT = "\n\t\t\t<";

    private String filePath;
    private String nullReplacement;
    private boolean blobFileSpecified;
    private boolean saveBlobsIndividually;

    public ExportHelperXML(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    void extractExportParameters() {
        filePath = parent.getFilePath();
        nullReplacement = parent.getNullReplacement();
        blobFileSpecified = parent.isBlobFilePathSpecified();
        saveBlobsIndividually = parent.isSaveBlobsIndividually();
    }

    // ---

    @Override
    protected Object exportResultSet(ResultSet resultSet) {
        try (
                FileWriter fileWriter = new FileWriter(filePath, false);
                PrintWriter writer = new PrintWriter(fileWriter, true)
        ) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            List<ColumnData> columns = getCreateColumnData(metaData);
            int columnCount = metaData.getColumnCount();

            writer.write(getHeader());

            int row = 0;
            while (resultSet.next() && !isCancel()) {
                String rowData = getRowData(resultSet, columns, metaData, columnCount + 1, row);
                writer.println(String.format(getRowDataTemplate(row), rowData));
                row++;
            }

            writer.write(getFooter());

        } catch (Exception e) {
            displayErrorMessage(e);
            return Constants.WORKER_FAIL;
        }

        return Constants.WORKER_SUCCESS;
    }

    private String getRowData(ResultSet resultSet, List<ColumnData> columns, ResultSetMetaData metaData, int columnCount, int row)
            throws SQLException, IOException {

        StringBuilder rowData = new StringBuilder();
        for (int col = 1; col < columnCount && !isCancel(); col++) {
            if (isFieldSelected(col - 1)) {
                String stringValue = getStringValue(resultSet, columns.get(col - 1), metaData.getColumnName(col), row, col);
                rowData.append(stringValue);
            }
        }

        return rowData.toString();
    }

    private String getStringValue(ResultSet resultSet, ColumnData columnData, String columnName, int row, int col)
            throws SQLException, IOException {

        Object value = resultSet.getObject(col);
        if (value == null || MiscUtils.isNull(value.toString()))
            return getNullData(columnName, nullReplacement);

        if (isBlobType(columnData) && blobFileSpecified) {
            return getCellData(columnName, writeBlob(
                    resultSet.getBlob(col),
                    saveBlobsIndividually,
                    getCreateBlobFileName(columnData, col, row)
            ));
        }

        return getCellData(columnName, formatted(value.toString(), isCharType(columnData)));
    }

    // ---

    @Override
    protected Object exportTableModel(TableModel tableModel) {
        try (
                FileWriter fileWriter = new FileWriter(filePath, false);
                PrintWriter writer = new PrintWriter(fileWriter, true)
        ) {

            int rowCount = tableModel.getRowCount();
            int columnCount = tableModel.getColumnCount();

            writer.write(getHeader());

            for (int row = 0; row < rowCount && !isCancel(); row++) {
                String rowData = getRowData(tableModel, columnCount, row);
                writer.println(String.format(getRowDataTemplate(row), rowData));
            }

            writer.write(getFooter());

        } catch (Exception e) {
            displayErrorMessage(e);
            return Constants.WORKER_FAIL;
        }

        return Constants.WORKER_SUCCESS;
    }

    private String getRowData(TableModel tableModel, int columnCount, int row) throws IOException {

        StringBuilder rowData = new StringBuilder();
        for (int col = 0; col < columnCount && !isCancel(); col++) {
            if (isFieldSelected(col)) {
                String stringValue = getStringValue(tableModel, tableModel.getColumnName(col), row, col);
                rowData.append(stringValue);
            }
        }

        return rowData.toString();
    }

    private String getStringValue(TableModel tableModel, String columnName, int row, int col) throws IOException {

        Object value = tableModel.getValueAt(row, col);
        if (value == null || MiscUtils.isNull(value.toString()))
            return getNullData(columnName, nullReplacement);

        if (value instanceof RecordDataItem) {

            if (((RecordDataItem) value).isValueNull())
                return getNullData(columnName, nullReplacement);

            if (isBlobType(value) && blobFileSpecified) {
                return getCellData(columnName, writeBlob(
                        (AbstractLobRecordDataItem) value,
                        saveBlobsIndividually,
                        getCreateBlobFileName(tableModel, col, row)
                ));
            }
        }

        return getCellData(columnName, formatted(value.toString(), isCharType(value)));
    }

    // --- helper methods ---

    private String getHeader() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "\n<result-set>" +
                "\n\t<data>";
    }

    private String getFooter() {
        return "\n\t</data>" +
                "\n</result-set>";
    }

    private String getRowDataTemplate(int rowNumber) {
        return "\n\t\t<row number=\"" + (rowNumber + 1) + "\">%s\n\t\t</row>";
    }

    private String getCellData(String columnName, String value) {
        return DATA_INDENT + columnName + ">" + value + "</" + columnName + ">";
    }

    private String getNullData(String columnName, String nullReplacement) {
        if (nullReplacement.isEmpty())
            return DATA_INDENT + columnName + "/>";
        return getCellData(columnName, nullReplacement);
    }

    private String formatted(String stringValue, boolean isChar) {

        if (isChar)
            stringValue = "<![CDATA[" + stringValue + "]]>";

        return stringValue;
    }

}
