package org.executequery.gui.importFromFile;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.localization.Bundles;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;

abstract class AbstractImportHelper implements ImportHelper {

    private final List<String> headers;

    protected final ImportDataFromFilePanel parent;
    protected final boolean isFirstRowHeaders;
    protected final int previewRowCount;
    protected final String pathToFile;
    protected String delimiter = ";;;";

    protected AbstractImportHelper(ImportDataFromFilePanel parent, String pathToFile, int previewRowCount, boolean isFirstRowHeaders) {
        this.headers = new LinkedList<>();
        this.parent = parent;
        this.pathToFile = pathToFile;
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

    protected final String bundleString(String key) {
        return Bundles.get(ImportDataFromFilePanel.class, key);
    }

    @Override
    public final void importData(
            StringBuilder sourceColumnList,
            boolean[] valuesIndexes,
            PreparedStatement insertStatement,
            DefaultStatementExecutor executor) {

        try {
            startImport(
                    sourceColumnList,
                    valuesIndexes,
                    insertStatement,
                    executor,
                    parent.getFirstRowIndex(),
                    parent.getLastRowIndex(),
                    parent.getBathStep(),
                    parent.getMappingTable(),
                    parent.getProgressDialog()
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

}
