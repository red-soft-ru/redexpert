package org.executequery.gui.importData;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

abstract class AbstractImportHelper implements ImportHelper {

    private final List<String> headers;

    protected final ImportDataPanel parent;
    protected PreparedStatement insertStatement;
    protected DefaultStatementExecutor executor;

    protected final boolean isFirstRowHeaders;
    protected final int previewRowCount;
    protected final String pathToFile;
    protected final String pathToLob;
    protected String delimiter = ";;;";
    protected int addedRecordsCount;

    protected AbstractImportHelper(ImportDataPanel parent, String pathToFile, String pathToLob, int previewRowCount, boolean isFirstRowHeaders) {
        this.headers = new LinkedList<>();
        this.parent = parent;
        this.pathToFile = pathToFile;
        this.pathToLob = pathToLob;
        this.previewRowCount = previewRowCount;
        this.isFirstRowHeaders = isFirstRowHeaders;
    }

    protected final void createHeaders(int count) {
        headers.clear();
        for (int i = 0; i < count; i++)
            headers.add("COLUMN" + (i + 1));
    }

    protected final void createHeaders(List<String> newHeaders) {
        headers.clear();
        headers.addAll(newHeaders);
    }

    protected final void createHeaders(ResultSetMetaData metaData) {

        try {
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++)
                headers.add(metaData.getColumnName(i));

        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
    }

    @Override
    public final void importData(
            StringBuilder sourceColumnList,
            boolean[] valuesIndexes,
            PreparedStatement insertStatement,
            DefaultStatementExecutor executor) {

        this.insertStatement = insertStatement;
        this.executor = executor;

        try {

            startImport(
                    sourceColumnList,
                    valuesIndexes,
                    parent.getFirstRowIndex(),
                    parent.getLastRowIndex(),
                    parent.getBathStep(),
                    parent.getMappingTable()
            );

        } catch (DateTimeParseException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("DateTimeFormatErrorMessage") + "\n" + e.getMessage(), e);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ImportDataErrorMessage") + "\n" + e.getMessage(), e);

        } finally {
            executor.releaseResources();
        }
    }

    @Override
    public final List<String> getHeaders() {
        return headers;
    }

    @Override
    public final int getColumnsCount() {
        return headers.size();
    }

    @Override
    public String getDelimiter() {
        return delimiter;
    }

    @Override
    public int getAddedRecordsCount() {
        return addedRecordsCount;
    }

    protected String getFormattedTimeValue(Object value) {
        return value.toString().replace("T", " ");
    }

    protected String getFormattedIntValue(Object value) {
        return value.toString().split("\\.")[0].trim();
    }

    @SuppressWarnings("resource")
    protected Object getFormattedBlobValue(Object value, boolean formatPath) throws IOException {

        if (value.toString().startsWith(":h")) {

            String parameter = value.toString();
            int startIndex = Integer.parseInt(parameter.substring(2).split("_")[0], 16);
            int endIndex = startIndex + Integer.parseInt(parameter.split("_")[1], 16);

            value = Arrays.copyOfRange(Files.readAllBytes(Paths.get(pathToLob)), startIndex, endIndex);

        } else {

            String path = value.toString();
            if (formatPath)
                path = path.replace("\"", "").replace("'", "");

            value = Files.newInputStream(new File(path).toPath());
        }

        return value;
    }

    protected void updateProgressLabel(int executorIndex, boolean execute, boolean finish) throws SQLException {

        parent.setProgressLabel(String.format(bundleString("RecordsAddedLabel"), executorIndex));
        if (finish) {
            Log.info("Import finished, " + executorIndex + " records was added");
            addedRecordsCount = executorIndex;
        }

        if (execute)
            execute();
    }

    protected final String bundleString(String key) {
        return Bundles.get(ImportDataPanel.class, key);
    }

    private void execute() throws SQLException {
        insertStatement.executeBatch();
        executor.getConnection().commit();
    }

}
