package org.executequery.gui.importData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ImportHelperXML extends AbstractImportHelper {

    protected ImportHelperXML(ImportDataPanel parent, String pathToFile, String pathToLob, int previewRowCount, boolean isFirstRowHeaders) {
        super(parent, pathToFile, pathToLob, previewRowCount, isFirstRowHeaders);
    }

    @Override
    public void startImport(
            StringBuilder sourceColumnList,
            boolean[] valuesIndexes,
            int firstRow,
            int lastRow,
            int batchStep,
            JTable mappingTable) throws Exception {

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(pathToFile));
        document.getDocumentElement().normalize();

        String[] sourceFields = sourceColumnList.toString().split(",");
        int executorIndex = 0;
        int linesCount = 0;

        NodeList rows = document.getElementsByTagName("row");
        for (int rowIndex = 0; rowIndex < rows.getLength(); rowIndex++) {

            Map<String, String> cellsValues = new HashMap<>();

            NodeList cells = rows.item(rowIndex).getChildNodes();
            for (int columnIndex = 0; columnIndex < cells.getLength(); columnIndex++) {

                Node cell = cells.item(columnIndex);
                if (!cell.getNodeName().startsWith("#text")) {
                    String value = cell.getFirstChild() != null ? cell.getFirstChild().getNodeValue() : null;
                    cellsValues.put(cell.getNodeName(), value);
                }
            }

            if (parent.isCancel() || linesCount > lastRow)
                break;

            if (linesCount < firstRow) {
                linesCount++;
                continue;
            }

            int fieldIndex = 0;
            int mappedIndex = 0;
            for (boolean valueIndex : valuesIndexes) {
                if (valueIndex) {

                    int columnType = insertStatement.getParameterMetaData().getParameterType(fieldIndex + 1);
                    String columnTypeName = insertStatement.getParameterMetaData().getParameterTypeName(fieldIndex + 1);
                    String columnProperty = mappingTable.getValueAt(mappedIndex, 3).toString();

                    Object insertParameter = cellsValues.get(sourceFields[fieldIndex]);
                    if (insertParameter == null || insertParameter.toString().isEmpty() || insertParameter.toString().equalsIgnoreCase("NULL")) {
                        insertStatement.setNull(fieldIndex + 1, columnType);

                    } else {
                        if (parent.isTimeType(columnTypeName))
                            insertParameter = getFormattedTimeValue(insertParameter);
                        else if (parent.isBlobType(columnTypeName) && columnProperty.equals("true"))
                            insertParameter = getFormattedBlobValue(insertParameter, true);

                        insertStatement.setObject(fieldIndex + 1, insertParameter);
                    }

                    fieldIndex++;
                }
                mappedIndex++;
            }
            insertStatement.addBatch();

            boolean execute = executorIndex % batchStep == 0 && executorIndex != 0;
            updateProgressLabel(executorIndex, execute, false);
            linesCount++;
            executorIndex++;
        }

        updateProgressLabel(executorIndex, true, true);
    }

    @Override
    public List<String> getPreviewData() throws Exception {

        List<String> readData = new LinkedList<>();

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(pathToFile));
        document.getDocumentElement().normalize();

        NodeList rows = document.getElementsByTagName("row");
        for (int rowIndex = 0; rowIndex < rows.getLength(); rowIndex++) {

            NodeList cells = rows.item(rowIndex).getChildNodes();
            if (rowIndex == 0)
                createHeaders(getHeaders(cells));

            readData.add(String.join(delimiter, getRowData(cells)));
        }

        return readData;
    }

    private List<String> getRowData(NodeList cells) {

        List<String> rowData = new LinkedList<>();

        for (int columnIndex = 0; columnIndex < cells.getLength(); columnIndex++) {

            Node cell = cells.item(columnIndex);
            if (cell.getNodeName().startsWith("#text"))
                continue;

            cell = cell.getFirstChild();
            String value = cell != null ? cell.getNodeValue() : null;
            if (value != null && !value.equalsIgnoreCase("NULL"))
                rowData.add(value);
            else
                rowData.add("");
        }

        return rowData;
    }

    private List<String> getHeaders(NodeList cells) {

        List<String> headers = new LinkedList<>();

        for (int columnIndex = 0; columnIndex < cells.getLength(); columnIndex++) {

            Node cell = cells.item(columnIndex);
            if (!cell.getNodeName().startsWith("#text"))
                headers.add(cell.getNodeName());
        }

        return headers;
    }

}
