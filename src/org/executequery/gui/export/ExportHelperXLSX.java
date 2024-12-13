package org.executequery.gui.export;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.executequery.GUIUtilities;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.table.TableModel;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExportHelperXLSX extends AbstractExportHelper {

    private String filePath;
    private String nullReplacement;

    private boolean addHeaders;
    private boolean blobFileSpecified;
    private boolean saveBlobsIndividually;

    public ExportHelperXLSX(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    void extractExportParameters() {
        filePath = parent.getFilePath();
        addHeaders = parent.isAddHeaders();
        nullReplacement = parent.getNullReplacement();
        blobFileSpecified = parent.isBlobFilePathSpecified();
        saveBlobsIndividually = parent.isSaveBlobsIndividually();
    }

    // ---

    @Override
    void exportResultSet(ResultSet resultSet) {
        try {

            ResultSetMetaData metaData = resultSet.getMetaData();
            List<ColumnData> columns = getCreateColumnData(metaData);
            int columnCount = metaData.getColumnCount();

            ExcelBookBuilder builder = new ExcelBookBuilder();
            builder.createSheet("Exported Data");

            if (addHeaders)
                builder.addRowHeader(getHeaders(metaData, columnCount));

            int row = 0;
            while (resultSet.next() && canAdd(row) && !isCancel()) {
                List<String> values = getStringValues(resultSet, columns, columnCount + 1, row);
                builder.addRow(values);
                row++;
            }

            OutputStream outputStream = new FileOutputStream(filePath, false);
            builder.writeTo(outputStream);
            outputStream.close();

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

        if (isBlobType(columnData) && blobFileSpecified) {
            return writeBlob(
                    resultSet.getBlob(col),
                    saveBlobsIndividually,
                    getCreateBlobFileName(columnData, col, row)
            );
        }

        return getFormattedValue(value, null, nullReplacement);
    }

    // ---

    @Override
    void exportTableModel(TableModel tableModel) {
        try {

            int rowCount = tableModel.getRowCount();
            int columnCount = tableModel.getColumnCount();

            ExcelBookBuilder builder = new ExcelBookBuilder();
            builder.createSheet("Exported Data");

            if (addHeaders)
                builder.addRowHeader(getHeaders(tableModel, columnCount));

            for (int row = 0; row < rowCount && canAdd(row) && !isCancel(); row++) {
                List<String> values = getStringValues(tableModel, columnCount, row);
                builder.addRow(values);
            }

            OutputStream outputStream = new FileOutputStream(filePath, false);
            builder.writeTo(outputStream);
            outputStream.close();

        } catch (IOException e) {
            displayErrorMessage(e);
        }
    }

    private List<String> getStringValues(TableModel tableModel, int columnCount, int row) throws IOException {

        List<String> values = new ArrayList<>();
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

            if (isBlobType(value) && blobFileSpecified) {
                return writeBlob(
                        (AbstractLobRecordDataItem) value,
                        saveBlobsIndividually,
                        getCreateBlobFileName(tableModel, col, row)
                );
            }
        }

        return getFormattedValue(value, null, nullReplacement);
    }

    // --- helper methods ---

    private boolean canAdd(int row) {

        if (row >= SpreadsheetVersion.EXCEL2007.getLastRowIndex()) {
            GUIUtilities.displayWarningMessage(String.format(
                    bundleString("maxRowMessage"),
                    SpreadsheetVersion.EXCEL2007.getLastRowIndex()
            ));

            return false;
        }

        return true;
    }

    // ---

    private static class ExcelBookBuilder {

        private final SXSSFWorkbook workbook;
        private final CellStyle defaultCellStyle;

        private int currentRow;
        private SXSSFSheet sheet;

        public ExcelBookBuilder() {
            workbook = new SXSSFWorkbook();
            defaultCellStyle = createStyle();
        }

        public void writeTo(OutputStream outputStream) throws IOException {
            workbook.write(outputStream);
        }

        public void createSheet(String sheetName) {
            sheet = workbook.createSheet(sheetName);
        }

        public void addRow(List<String> values) {
            fillRow(values, createRow(++currentRow), defaultCellStyle);
        }

        public void addRowHeader(List<String> values) {

            if (currentRow > 0)
                currentRow++;

            Font font = workbook.createFont();
            font.setBold(true);

            CellStyle style = createStyle();
            style.setFont(font);

            fillRow(values, createRow(currentRow), style);
        }

        private SXSSFRow createRow(int rowNumber) {
            return sheet.createRow(rowNumber);
        }

        private void fillRow(List<String> values, SXSSFRow row, CellStyle style) {

            for (int i = 0, n = values.size(); i < n; i++) {
                SXSSFCell cell = row.createCell(i);
                cell.setCellStyle(style);
                try {
                    double doubleValue = Double.parseDouble(values.get(i));
                    cell.setCellValue(doubleValue);
                } catch (Exception e) {
                    cell.setCellValue(new XSSFRichTextString(values.get(i)));
                }
            }
        }

        private CellStyle createStyle() {
            return workbook.createCellStyle();
        }

    } // ExcelBookBuilder class

}
