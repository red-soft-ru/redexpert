package org.executequery.gui.exportData;

import org.apache.poi.ss.SpreadsheetVersion;
import org.executequery.GUIUtilities;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.importexport.DefaultExcelWorkbookBuilder;
import org.executequery.gui.importexport.ExcelWorkbookBuilder;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.table.TableModel;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class ExportHelperXLSX extends AbstractExportHelper {

    public ExportHelperXLSX(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    void exportResultSet(ResultSet resultSet) {

        String filePath = parent.getFilePath();
        String nullReplacement = parent.getNullReplacement();

        boolean addHeaders = parent.isAddHeaders();
        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();

        try {

            ResultSetMetaData metaData = resultSet.getMetaData();
            List<ColumnData> columns = getCreateColumnData(metaData);
            int columnCount = metaData.getColumnCount();

            ExcelWorkbookBuilder builder = new DefaultExcelWorkbookBuilder();
            builder.createSheet("Exported Data");

            if (addHeaders) {

                List<String> headers = new ArrayList<>();
                for (int i = 1; i < columnCount + 1; i++)
                    if (isFieldSelected(i))
                        headers.add(metaData.getColumnName(i));

                builder.addRowHeader(headers);
            }

            int row = 0;
            while (resultSet.next()) {

                if (row >= SpreadsheetVersion.EXCEL2007.getLastRowIndex()) {
                    GUIUtilities.displayWarningMessage(String.format(bundleString("maxRowMessage"), SpreadsheetVersion.EXCEL2007.getLastRowIndex()));
                    break;
                }

                if (isCancel())
                    break;

                List<String> values = new ArrayList<>();
                for (int col = 1; col < columnCount + 1; col++) {

                    if (isCancel())
                        break;

                    if (!isFieldSelected(col - 1))
                        continue;

                    String stringValue = null;
                    Object value = resultSet.getObject(col);
                    ColumnData columnData = columns.get(col - 1);

                    if (value != null) {

                        if (isBlobType(columnData))
                            stringValue = writeBlob(resultSet.getBlob(col), saveBlobsIndividually, getCreateBlobFileName(columnData, col, row));
                        else
                            stringValue = getFormattedValue(value, null, nullReplacement);
                    }

                    values.add(stringValue != null ? stringValue : nullReplacement);
                }

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

    @Override
    void exportTableModel(TableModel tableModel) {

        String filePath = parent.getFilePath();
        String nullReplacement = parent.getNullReplacement();

        boolean addHeaders = parent.isAddHeaders();
        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();

        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();

        try {

            if (rowCount > SpreadsheetVersion.EXCEL2007.getLastRowIndex()) {
                GUIUtilities.displayWarningMessage(String.format(bundleString("maxRowMessage"), SpreadsheetVersion.EXCEL2007.getLastRowIndex()));
                rowCount = SpreadsheetVersion.EXCEL2007.getLastRowIndex();
            }

            ExcelWorkbookBuilder builder = new DefaultExcelWorkbookBuilder();
            builder.createSheet("Exported Data");

            if (addHeaders) {

                List<String> headers = new ArrayList<>();
                for (int i = 0; i < columnCount; i++)
                    if (isFieldSelected(i))
                        headers.add(tableModel.getColumnName(i));

                builder.addRowHeader(headers);
            }

            for (int row = 0; row < rowCount; row++) {

                if (isCancel())
                    break;

                List<String> values = new ArrayList<>();
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

                            if (isBlobType(rdi))
                                stringValue = writeBlob((AbstractLobRecordDataItem) rdi, saveBlobsIndividually, getCreateBlobFileName(tableModel, col, row));
                            else
                                stringValue = getFormattedValue(rdi, null, nullReplacement);
                        }
                    } else {
                        if (value != null) {
                            stringValue = getFormattedValue(value, null, nullReplacement);
                        }
                    }
                    values.add(stringValue != null ? stringValue : nullReplacement);
                }

                builder.addRow(values);
            }

            OutputStream outputStream = new FileOutputStream(filePath, false);
            builder.writeTo(outputStream);
            outputStream.close();

        } catch (IOException e) {
            displayErrorMessage(e);
        }
    }

}
