package org.executequery.gui.importFromFile;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.log.Log;
import org.underworldlabs.swing.DefaultProgressDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ImportHelperXML extends AbstractImportHelper {

    protected ImportHelperXML(ImportDataFromFilePanel parent, String pathToFile, int previewRowCount, boolean isFirstRowHeaders) {
        super(parent, pathToFile, previewRowCount, isFirstRowHeaders);
    }

    @Override
    public void startImport(
            StringBuilder sourceColumnList,
            boolean[] valuesIndexes,
            PreparedStatement insertStatement,
            DefaultStatementExecutor executor,
            int firstRow,
            int lastRow,
            int batchStep,
            JTable mappingTable,
            DefaultProgressDialog progressDialog) throws Exception {

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
                if (!cell.getNodeName().startsWith("#text"))
                    cellsValues.put(cell.getNodeName(), cell.getFirstChild().getNodeValue());
            }

            if (progressDialog.isCancel() || linesCount > lastRow)
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

                        if (parent.isTimeType(columnTypeName)) {
                            insertParameter = LocalDateTime.parse(insertParameter.toString(), DateTimeFormatter.ofPattern(columnProperty));

                        } else if (parent.isBlobType(columnTypeName) && columnProperty.equals("true")) {
                            String path = insertParameter.toString().replace("\"", "").replace("'", "");
                            insertParameter = Files.newInputStream(new File(path).toPath());
                        }

                        insertStatement.setObject(fieldIndex + 1, insertParameter);
                    }

                    fieldIndex++;
                }
                mappedIndex++;
            }
            insertStatement.addBatch();

            if (executorIndex % batchStep == 0 && executorIndex != 0) {
                insertStatement.executeBatch();
                executor.getConnection().commit();
            }
            linesCount++;
            executorIndex++;
        }

        insertStatement.executeBatch();
        executor.getConnection().commit();
        Log.info("Import finished, " + executorIndex + " records was added");
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

            String value = cell.getFirstChild().getNodeValue();
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
