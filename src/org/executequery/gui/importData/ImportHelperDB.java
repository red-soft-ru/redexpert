package org.executequery.gui.importData;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseTable;
import org.executequery.gui.resultset.ResultSetColumnHeader;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ImportHelperDB extends AbstractImportHelper {

    private final String sourceTableName;
    private final DatabaseConnection sourceConnection;

    protected ImportHelperDB(ImportDataPanel parent, String sourceTableName, int previewRowCount, DatabaseConnection sourceConnection) {
        super(parent, null, null, previewRowCount, false);
        this.sourceTableName = sourceTableName;
        this.sourceConnection = sourceConnection;
    }

    @Override
    public void startImport(
            StringBuilder sourceColumnList,
            boolean[] valuesIndexes,
            int firstRow,
            int lastRow,
            int batchStep,
            JTable mappingTable) throws Exception {

        String[] sourceFields = sourceColumnList.toString().split(",");
        int executorIndex = 0;
        int linesCount = 0;

        String sourceSelectQuery = "SELECT " + sourceColumnList + " FROM " + MiscUtils.getFormattedObject(sourceTableName, sourceConnection);

        DefaultStatementExecutor sourceExecutor = getExecutor(sourceConnection);
        ResultSet sourceFileData = getSourceResultSet(sourceSelectQuery, sourceExecutor);
        if (sourceFileData == null)
            return;

        while (sourceFileData.next()) {

            if (parent.isCancel() || linesCount > lastRow)
                break;

            if (linesCount < firstRow) {
                linesCount++;
                continue;
            }

            int fieldIndex = 0;
            for (boolean valueIndex : valuesIndexes) {
                if (valueIndex) {

                    int columnType = insertStatement.getParameterMetaData().getParameterType(fieldIndex + 1);
                    String columnTypeName = insertStatement.getParameterMetaData().getParameterTypeName(fieldIndex + 1);

                    Object insertParameter = sourceFileData.getString(sourceFields[fieldIndex]);
                    if (insertParameter == null || insertParameter.toString().isEmpty() || insertParameter.toString().equalsIgnoreCase("null")) {
                        insertStatement.setNull(fieldIndex + 1, columnType);

                    } else {
                        if (parent.isIntegerType(columnTypeName))
                            insertParameter = getFormattedIntValue(insertParameter);

                        insertStatement.setObject(fieldIndex + 1, insertParameter);
                    }

                    fieldIndex++;
                }
            }
            insertStatement.addBatch();

            boolean execute = executorIndex % batchStep == 0 && executorIndex != 0;
            updateProgressLabel(executorIndex, execute, false);
            linesCount++;
            executorIndex++;
        }

        updateProgressLabel(executorIndex, true, true);
        sourceExecutor.releaseResources();
    }

    @Override
    public List<String> getPreviewData() {
        return null;
    }

    public ResultSet getPreviewResultSet() throws SQLException {
        String query = "SELECT FIRST " + previewRowCount + " * FROM " + MiscUtils.getFormattedObject(sourceTableName, sourceConnection);
        return getSourceResultSet(query, getExecutor(sourceConnection));
    }

    private DefaultStatementExecutor getExecutor(DatabaseConnection connection) {

        DefaultStatementExecutor executor = new DefaultStatementExecutor();
        executor.setCommitMode(false);
        executor.setKeepAlive(true);
        executor.setDatabaseConnection(connection);

        return executor;
    }

    private synchronized ResultSet getSourceResultSet(String query, DefaultStatementExecutor executor) throws SQLException {

        ResultSet resultSet = executor.execute(QueryTypes.SELECT, query).getResultSet();
        if (resultSet != null) {
            createHeaders(resultSet.getMetaData());
            return resultSet;
        }

        NamedObject databaseObject = new DefaultDatabaseHost(sourceConnection).getTables().stream()
                .filter(table -> Objects.equals(table.getName(), sourceTableName))
                .findFirst().orElse(null);

        if (databaseObject == null)
            return null;

        int columnCount = 0;
        String tableName = "";
        List<ResultSetColumnHeader> columnHeaders = new LinkedList<>();

        ResultSet rs = ((DefaultDatabaseTable) databaseObject).getMetaData();
        while (rs.next()) {

            columnHeaders.add(
                    new ResultSetColumnHeader(
                            columnCount,
                            rs.getString(4),
                            rs.getString(4),
                            rs.getInt(5),
                            rs.getString(6),
                            rs.getInt(7)
                    )
            );
            tableName = rs.getString(3);
            columnCount++;
        }
        createHeaders(columnHeaders.stream().map(ResultSetColumnHeader::getName).collect(Collectors.toList()));

        Statement statement = rs.getStatement();
        if (statement != null && !statement.isClosed())
            statement.close();

        StringBuilder formattedQuery = new StringBuilder("SELECT ");
        for (int i = 0; i < columnCount; i++) {
            try {

                String columnName = columnHeaders.get(i).getName();
                String getColumnQuery = "SELECT " + columnName + " FROM " + tableName;
                SqlStatementResult result = executor.execute(QueryTypes.SELECT, getColumnQuery);

                rs = result.getResultSet();
                if (rs == null)
                    formattedQuery.append("'").append(result.getErrorMessage()).append("' AS ");
                formattedQuery.append(columnName);

            } catch (Exception ignored) {
            }

            if (i < columnCount - 1)
                formattedQuery.append(", ");

            executor.releaseResources();
        }
        formattedQuery.append(" FROM ").append(tableName);

        return executor.execute(QueryTypes.SELECT, formattedQuery.toString()).getResultSet();
    }

}
