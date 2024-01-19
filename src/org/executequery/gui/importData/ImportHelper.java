package org.executequery.gui.importData;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;

import javax.swing.*;
import java.sql.PreparedStatement;
import java.util.List;

interface ImportHelper {

    void importData(
            StringBuilder sourceColumnList,
            boolean[] valuesIndexes,
            PreparedStatement insertStatement,
            DefaultStatementExecutor executor
    );

    void startImport(
            StringBuilder sourceColumnList,
            boolean[] valuesIndexes,
            int firstRow,
            int lastRow,
            int batchStep,
            JTable mappingTable
    ) throws Exception;

    List<String> getPreviewData() throws Exception;

    List<String> getHeaders();

    int getColumnsCount();

    String getDelimiter();

    int getAddedRecordsCount();

}
