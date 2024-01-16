package org.executequery.gui.exportData;

import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;

import javax.swing.table.TableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

public class ExportHelperCSV extends AbstractExportHelper {

    public ExportHelperCSV(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    protected void exportResultSet(ResultSet resultSet) {

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

            if (addHeaders) {

                for (int i = 0; i < columnCount; i++) {
                    if (isFieldSelected(i)) {
                        resultText.append(tableModel.getColumnName(i));
                        resultText.append(columnDelimiter);
                    }
                }
                resultText.deleteCharAt(resultText.length() - 1);

                writer.println(resultText);
                resultText.setLength(0);
            }

            for (int row = 0; row < rowCount; row++) {

                if (isCancel())
                    break;

                for (int col = 0; col < columnCount; col++) {

                    if (isCancel())
                        break;

                    if (!isFieldSelected(col))
                        continue;

                    String stringValue = null;
                    RecordDataItem value = (RecordDataItem) tableModel.getValueAt(row, col);

                    if (!value.isValueNull()) {
                        stringValue = getFormattedValue(value, endlReplacement, nullReplacement);

                        if (isCharType(value) && addQuotes && !stringValue.isEmpty()) {
                            stringValue = "\"" + stringValue + "\"";

                        } else if (isBlobType(value)) {
                            stringValue = writeBlobToFile((AbstractLobRecordDataItem) value, saveBlobsIndividually, getCreateBlobFileName(tableModel, col, row));
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

        } catch (IOException e) {
            displayErrorMessage(e);
        }
    }

}
