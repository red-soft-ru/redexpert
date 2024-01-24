package org.executequery.gui.exportData;

import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;

import javax.swing.table.TableModel;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

public class ExportHelperXML extends AbstractExportHelper {

    public ExportHelperXML(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    void exportResultSet(ResultSet resultSet) {

        String filePath = parent.getFilePath();
        String nullReplacement = parent.getNullReplacement();
        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();

        try {

            ResultSetMetaData metaData = resultSet.getMetaData();
            List<ColumnData> columns = getCreateColumnData(metaData);
            int columnCount = metaData.getColumnCount();

            StringBuilder rowData = new StringBuilder();
            PrintWriter writer = new PrintWriter(new FileWriter(filePath, false), true);
            writer.write(getHeader());

            int row = 0;
            while (resultSet.next()) {

                if (isCancel())
                    break;

                for (int col = 1; col < columnCount + 1; col++) {

                    if (isCancel())
                        break;

                    if (!isFieldSelected(col - 1))
                        continue;

                    Object value = resultSet.getObject(col);
                    ColumnData columnData = columns.get(col - 1);

                    if (value != null) {

                        if (isCharType(columnData))
                            rowData.append(getCellData(metaData.getColumnName(col), "<![CDATA[" + value + "]]>"));
                        else if (isBlobType(columnData))
                            rowData.append(getCellData(metaData.getColumnName(col), writeBlob(resultSet.getBlob(col), saveBlobsIndividually, getCreateBlobFileName(columnData, col, row))));
                        else
                            rowData.append(getCellData(metaData.getColumnName(col), value.toString()));
                    } else
                        rowData.append(getNullData(metaData.getColumnName(col), nullReplacement));
                }

                writer.println(String.format(getRowDataTemplate(row), rowData));
                rowData.setLength(0);
                row++;
            }

            writer.write(getFooter());
            writer.close();

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

    @Override
    void exportTableModel(TableModel tableModel) {

        String filePath = parent.getFilePath();
        String nullReplacement = parent.getNullReplacement();
        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();

        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();

        try {

            StringBuilder rowData = new StringBuilder();
            PrintWriter writer = new PrintWriter(new FileWriter(filePath, false), true);
            writer.write(getHeader());

            for (int row = 0; row < rowCount; row++) {

                if (isCancel())
                    break;

                for (int col = 0; col < columnCount; col++) {

                    if (isCancel())
                        break;

                    if (!isFieldSelected(col))
                        continue;

                    Object value = tableModel.getValueAt(row, col);
                    if (value != null && value.toString() != null) {

                        if (isCharType(value))
                            rowData.append(getCellData(tableModel.getColumnName(col), "<![CDATA[" + value + "]]>"));
                        else if (isBlobType(value))
                            rowData.append(getCellData(tableModel.getColumnName(col), writeBlob((AbstractLobRecordDataItem) value, saveBlobsIndividually, getCreateBlobFileName(tableModel, col, row))));
                        else
                            rowData.append(getCellData(tableModel.getColumnName(col), value.toString()));
                    } else
                        rowData.append(getNullData(tableModel.getColumnName(col), nullReplacement));
                }

                writer.println(String.format(getRowDataTemplate(row), rowData));
                rowData.setLength(0);
            }

            writer.write(getFooter());
            writer.close();

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

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
        return "\n\t\t\t<" + columnName + ">" + value + "</" + columnName + ">";
    }

    private String getNullData(String columnName, String nullReplacement) {
        return nullReplacement.isEmpty() ?
                "\n\t\t\t<" + columnName + "/>" :
                "\n\t\t\t<" + columnName + ">" + nullReplacement + "</" + columnName + ">";
    }

}
