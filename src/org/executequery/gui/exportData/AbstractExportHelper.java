package org.executequery.gui.exportData;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.Types;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.util.mime.MimeType;
import org.executequery.util.mime.MimeTypes;
import org.underworldlabs.swing.DefaultProgressDialog;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractExportHelper implements ExportHelper {

    protected final ExportDataPanel parent;
    private DefaultProgressDialog progressDialog;

    protected AbstractExportHelper(ExportDataPanel parent) {
        this.parent = parent;
    }

    // --- export methods ---

    @Override
    public final void export(Object data) {

        progressDialog = new DefaultProgressDialog(ExportDataPanel.TITLE);
        SwingWorker worker = new SwingWorker("ExportData", parent) {

            @Override
            public Object construct() {

                if (data instanceof ResultSet)
                    exportResultSet((ResultSet) data);
                else if (data instanceof TableModel)
                    exportTableModel((TableModel) data);

                return null;
            }

            @Override
            public void finished() {
                progressDialog.dispose();
            }
        };

        worker.start();
        progressDialog.run();
    }

    abstract void exportResultSet(ResultSet resultSet);

    abstract void exportTableModel(TableModel tableModel);

    // --- export helper methods ---

    protected String getFormattedValue(Object value, String endlReplacement, String nullReplacement) {

        String formattedValue = nullReplacement;

        if (value instanceof RecordDataItem) {
            RecordDataItem recordDataItem = (RecordDataItem) value;
            if (!recordDataItem.isValueNull())
                formattedValue = recordDataItem.getDisplayValue().toString().replaceAll("'", "''");

        } else if (value != null)
            formattedValue = value.toString().replaceAll("'", "''");

        if (!formattedValue.isEmpty() && endlReplacement != null)
            formattedValue = formattedValue.replaceAll("\n", endlReplacement);

        return formattedValue;
    }

    protected List<ColumnData> getCreateColumnData(ResultSetMetaData metaData) throws SQLException {

        List<ColumnData> columns = new LinkedList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            ColumnData columnData = new ColumnData(metaData.getColumnLabel(i), null);
            columnData.setSQLType(metaData.getColumnType(i));
            columns.add(columnData);
        }

        return columns;
    }

    protected final boolean isBlobType(Object value) {
        return (value instanceof ColumnData && ((ColumnData) value).isLOB())
                || value instanceof AbstractLobRecordDataItem;
    }

    protected final boolean isCharType(Object value) {

        if (value instanceof RecordDataItem) {
            int type = ((RecordDataItem) value).getDataType();
            return type == Types.CHAR || type == Types.VARCHAR || type == Types.LONGVARCHAR;

        } else if (value instanceof ColumnData) {
            return ((ColumnData) value).isCharacterType();
        } else return value instanceof String;
    }

    protected final boolean isDateType(Object value) {

        if (value instanceof RecordDataItem) {
            int type = ((RecordDataItem) value).getDataType();
            return type == Types.DATE || type == Types.TIME || type == Types.TIMESTAMP;

        } else if (value instanceof ColumnData) {
            return ((ColumnData) value).isDateDataType();
        }

        return false;
    }

    protected final boolean isFieldSelected(int col) {
        return parent.isFieldSelected(col);
    }

    protected final boolean isCancel() {
        return progressDialog == null || progressDialog.isCancel();
    }

    // -- blob save methods ---

    protected final String getCreateBlobFileName(Object data, int col, int row) {

        if (data instanceof TableModel)
            return ((TableModel) data).getColumnName(col) + "_" + row;

        else if (data instanceof ColumnData)
            return ((ColumnData) data).getColumnName() + "_" + row;

        else
            return "BLOB_FILE_" + row;
    }

    protected String writeBlob(AbstractLobRecordDataItem lobValue, boolean saveIndividually, String fileName) throws IOException {
        return saveBlobToFile(lobValue.getData(), lobValue.getLobRecordItemName(), fileName, saveIndividually);
    }

    protected String writeBlob(Blob lobValue, boolean saveIndividually, String fileName) throws IOException {

        byte[] lobData = null;
        String lobType = "txt";

        try {
            lobData = lobValue.getBytes(1, (int) lobValue.length());
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }

        MimeType mimeType = MimeTypes.get().getMimeType(lobData);
        if (mimeType != null)
            lobType = MimeTypes.get().getMimeType(lobData).getName();

        return saveBlobToFile(lobData, lobType, fileName, saveIndividually);
    }

    private String saveBlobToFile(byte[] lobData, String lobType, String fileName, boolean saveIndividually) throws IOException {

        String stringValue = "NULL";
        String blobFilePath = parent.getBlobPath();

        if (lobData != null) {

            if (saveIndividually) {

                lobType = lobType.contains("/") ? lobType.split("/")[1] : "txt";
                fileName += "." + lobType;

                File outputFile = new File(blobFilePath, fileName);
                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    outputStream.write(lobData);
                }

                stringValue = outputFile.getAbsolutePath();

            } else {

                String startIndex = String.format("%08x", new File(blobFilePath).length());
                String dataLength = String.format("%08x", lobData.length);
                stringValue = ":h" + startIndex + "_" + dataLength;

                Files.write(Paths.get(blobFilePath), lobData, StandardOpenOption.APPEND);
            }
        }

        return stringValue;
    }

    // ---

    protected final void displayErrorMessage(Throwable e) {
        GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorWritingToFile") + e.getMessage(), e);
    }

    protected final String bundleString(String key) {
        return Bundles.get(ExportDataPanel.class, key);
    }

}
