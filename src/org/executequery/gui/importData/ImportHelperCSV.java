package org.executequery.gui.importData;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

class ImportHelperCSV extends AbstractImportHelper {

    private final String sourceDelimiter;

    public ImportHelperCSV(ImportDataPanel parent, String pathToFile, String pathToLob, int previewRowCount, boolean isFirstRowHeaders) {
        super(parent, pathToFile, pathToLob, previewRowCount, isFirstRowHeaders);

        sourceDelimiter = parent.getDelimiter();
        delimiter = sourceDelimiter.equals("|") ? "\\" + sourceDelimiter : sourceDelimiter;
    }

    @Override
    public void startImport(
            StringBuilder sourceColumnList,
            boolean[] valuesIndexes,
            int firstRow,
            int lastRow,
            int batchStep,
            JTable mappingTable) throws Exception {

        Statement sourceFileStatement = getCreateStatement();
        if (sourceFileStatement == null)
            return;

        String[] sourceFields = sourceColumnList.toString().split(",");
        int executorIndex = 0;
        int linesCount = 0;

        String sourceSelectQuery = "SELECT " + sourceColumnList + " FROM " + MiscUtils.getFormattedObject(parent.getFileName(), executor.getDatabaseConnection());
        ResultSet sourceFileData = sourceFileStatement.executeQuery(sourceSelectQuery);
        while (sourceFileData.next()) {

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

                    Object insertParameter = sourceFileData.getString(sourceFields[fieldIndex]);
                    if (insertParameter == null || insertParameter.toString().isEmpty()) {
                        insertStatement.setNull(fieldIndex + 1, columnType);

                    } else {
                        if (parent.isTimeType(columnTypeName))
                            insertParameter = getFormattedTimeValue(insertParameter);
                        else if (parent.isBlobType(columnTypeName) && columnProperty.equals("true"))
                            insertParameter = getFormattedBlobValue(insertParameter, false);

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
    public List<String> getPreviewData() throws IOException {

        List<String> readData = new LinkedList<>();
        try (
                FileReader reader = new FileReader(pathToFile);
                Scanner scanner = new Scanner(reader)
        ) {

            for (int rowIndex = 0; rowIndex < previewRowCount && scanner.hasNextLine(); rowIndex++) {

                if (rowIndex == 0 && isFirstRowHeaders) {
                    createHeaders(Arrays.asList(scanner.nextLine().split(delimiter)));
                    continue;
                }

                readData.add(scanner.nextLine());
            }
        }

        if (!isFirstRowHeaders)
            createHeaders(readData.get(0).split(delimiter).length);

        return readData;
    }

    private Statement getCreateStatement() {

        Statement statement = null;
        try {

            Properties properties = new Properties();
            properties.setProperty("separator", sourceDelimiter);
            if (!isFirstRowHeaders) {
                properties.setProperty("suppressHeaders", "true");
                properties.setProperty("defectiveHeaders", "true");
            }

            String connectionUrl = "jdbc:relique:csv:" + Paths.get(pathToFile).getParent().toString();
            if (UIUtils.isWindows())
                connectionUrl = connectionUrl.replace("\\", "/");

            statement = DriverManager.getConnection(connectionUrl, properties).createStatement();

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);
        }

        return statement;
    }

}
