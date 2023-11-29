package org.executequery.gui.importFromFile;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.underworldlabs.swing.DefaultProgressDialog;

import javax.swing.*;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.List;

interface ImportHelper {

    void importData(
            StringBuilder sourceColumnList,
            boolean[] valuesIndexes,
            PreparedStatement insertStatement,
            DefaultStatementExecutor executor
    );

    List<String> getPreviewData() throws IOException;

    List<String> getHeaders();

    int getColumnsCount();
}
