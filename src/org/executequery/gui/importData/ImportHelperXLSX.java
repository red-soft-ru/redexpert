package org.executequery.gui.importData;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DataTruncation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ImportHelperXLSX extends AbstractImportHelper {

    public ImportHelperXLSX(ImportDataPanel parent, String pathToFile, String pathToLob, int previewRowCount, boolean isFirstRowHeaders) {
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

        XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(Paths.get(pathToFile)));
        XSSFSheet sheet = workbook.getSheetAt(parent.getSheetNumber() - 1);

        int executorIndex = 0;
        int linesCount = 0;

        for (int rowIndex = isFirstRowHeaders ? 1 : 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

            if (parent.isCancel() || linesCount > lastRow)
                break;

            if (linesCount < firstRow) {
                linesCount++;
                continue;
            }

            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                linesCount++;
                continue;
            }

            int fieldIndex = 0;
            int mappedIndex = 0;
            String[] sourceColumns = sourceColumnList.toString().split(",");

            for (boolean valueIndex : valuesIndexes) {
                if (valueIndex) {

                    int columnType = insertStatement.getParameterMetaData().getParameterType(fieldIndex + 1);
                    String columnTypeName = insertStatement.getParameterMetaData().getParameterTypeName(fieldIndex + 1);
                    String columnProperty = mappingTable.getValueAt(mappedIndex, 3).toString();
                    int sourceColumnIndex = parent.getSourceColumnIndex(sourceColumns[fieldIndex]);

                    Object insertParameter = row.getCell(sourceColumnIndex);
                    if (insertParameter == null || insertParameter.toString().isEmpty()) {
                        insertStatement.setNull(fieldIndex + 1, columnType);

                    } else {
                        insertParameter = insertParameter.toString();

                        if (parent.isIntegerType(columnTypeName))
                            insertParameter = getFormattedIntValue(insertParameter);
                        else if (parent.isTimeType(columnTypeName))
                            insertParameter = getFormattedTimeValue(insertParameter);
                        else if (parent.isBlobType(columnTypeName) && columnProperty.equals("true"))
                            insertParameter = getFormattedBlobValue(insertParameter, false);

                        try {
                            insertStatement.setObject(fieldIndex + 1, insertParameter);
                        } catch (DataTruncation e) {
                            insertStatement.setObject(fieldIndex + 1, getFormattedIntValue(insertParameter));
                        }
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
    public List<String> getPreviewData() throws IOException {

        XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(Paths.get(pathToFile)));

        JSpinner sheetNumberSpinner = parent.getSheetNumberSpinner();
        ((SpinnerNumberModel) sheetNumberSpinner.getModel()).setMaximum(workbook.getNumberOfSheets());

        List<String> readData = new LinkedList<>();
        XSSFSheet sheet = workbook.getSheetAt(parent.getSheetNumber() - 1);

        for (int rowIndex = 0; rowIndex < previewRowCount && rowIndex <= sheet.getLastRowNum(); rowIndex++) {

            String stringRow = String.join(delimiter, getRowData(sheet.getRow(rowIndex)));
            if (rowIndex == 0 && isFirstRowHeaders) {
                createHeaders(Arrays.asList(stringRow.split(delimiter)));
                continue;
            }

            readData.add(stringRow);
        }

        if (!isFirstRowHeaders)
            createHeaders(readData.get(0).split(delimiter).length);

        return readData;
    }

    private List<String> getRowData(XSSFRow row) {

        List<String> rowData = new LinkedList<>();
        if (row != null) {
            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                XSSFCell sheetCell = row.getCell(colIndex);
                rowData.add(sheetCell != null ? sheetCell.toString() : "");
            }
        }

        return rowData;
    }

}
