package org.executequery.gui.importFromFile;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.log.Log;
import org.underworldlabs.swing.DefaultProgressDialog;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ImportHelperXLSX extends AbstractImportHelper {

    public ImportHelperXLSX(ImportDataFromFilePanel parent, String pathToFile, String pathToLob, int previewRowCount, boolean isFirstRowHeaders) {
        super(parent, pathToFile, pathToLob, previewRowCount, isFirstRowHeaders);
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

        XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(Paths.get(pathToFile)));
        XSSFSheet sheet = workbook.getSheetAt(parent.getSheetNumber() - 1);

        int executorIndex = 0;
        int linesCount = 0;

        for (int rowIndex = 0; rowIndex < sheet.getLastRowNum(); rowIndex++) {

            if (progressDialog.isCancel() || linesCount > lastRow)
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

                        if (parent.isIntegerType(columnTypeName)) {
                            insertParameter = insertParameter.toString().split("\\.")[0].trim();

                        } else if (parent.isTimeType(columnTypeName)) {
                            insertParameter = LocalDateTime.parse(insertParameter.toString(), DateTimeFormatter.ofPattern(columnProperty));

                        } else if (parent.isBlobType(columnTypeName) && columnProperty.equals("true")) {

                            if (insertParameter.toString().startsWith(":h")) {

                                String parameter = insertParameter.toString();
                                int startIndex = Integer.parseInt(parameter.substring(2).split("_")[0], 16);
                                int endIndex = startIndex + Integer.parseInt(parameter.split("_")[1], 16);

                                insertParameter = Arrays.copyOfRange(Files.readAllBytes(Paths.get(pathToLob)), startIndex, endIndex);

                            } else
                                insertParameter = Files.newInputStream(new File(insertParameter.toString()).toPath());
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
    public List<String> getPreviewData() throws IOException {

        XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(Paths.get(pathToFile)));

        JSpinner sheetNumberSpinner = parent.getSheetNumberSpinner();
        ((SpinnerNumberModel) sheetNumberSpinner.getModel()).setMaximum(workbook.getNumberOfSheets());

        List<String> readData = new LinkedList<>();
        XSSFSheet sheet = workbook.getSheetAt(parent.getSheetNumber() - 1);

        for (int rowIndex = 0; rowIndex < previewRowCount && rowIndex < sheet.getLastRowNum(); rowIndex++) {

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
