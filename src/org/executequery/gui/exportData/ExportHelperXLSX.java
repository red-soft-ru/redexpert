package org.executequery.gui.exportData;

import org.apache.poi.ss.SpreadsheetVersion;
import org.executequery.GUIUtilities;
import org.executequery.gui.importexport.DefaultExcelWorkbookBuilder;
import org.executequery.gui.importexport.ExcelWorkbookBuilder;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.table.TableModel;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ExportHelperXLSX extends AbstractExportHelper {

    public ExportHelperXLSX(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    void exportResultSet(ResultSet resultSet) {

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
                    RecordDataItem value = (RecordDataItem) tableModel.getValueAt(row, col);

                    if (!value.isValueNull()) {

                        if (isBlobType(value))
                            stringValue = writeBlobToFile((AbstractLobRecordDataItem) value, saveBlobsIndividually, getCreateBlobFileName(tableModel, col, row));
                        else
                            stringValue = getFormattedValue(value, null, nullReplacement);
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
