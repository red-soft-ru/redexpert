package org.executequery.gui.exportData;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.Types;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DefaultProgressDialog;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;

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

    protected String writeBlobToFile(AbstractLobRecordDataItem lobValue, boolean saveIndividually, String fileName) throws IOException {

        String stringValue = "NULL";
        String blobFilePath = parent.getBlobPath();

        byte[] lobData = lobValue.getData();
        if (lobData != null) {

            if (saveIndividually) {

                String lobType = lobValue.getLobRecordItemName();
                lobType = lobType.contains("/") ? lobType.split("/")[1] : "txt";

                stringValue = fileName + "." + lobType;

                File outputFile = new File(blobFilePath, stringValue);
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

    protected final boolean isBlobType(Object value) {
        return value instanceof AbstractLobRecordDataItem;
    }

    protected final boolean isCharType(Object value) {

        if (value instanceof RecordDataItem) {
            int type = ((RecordDataItem) value).getDataType();
            return type == Types.CHAR || type == Types.VARCHAR || type == Types.LONGVARCHAR;
        }

        return false;
    }

    protected final boolean isDateType(Object value) {

        if (value instanceof RecordDataItem) {
            int type = ((RecordDataItem) value).getDataType();
            return type == Types.DATE || type == Types.TIME || type == Types.TIMESTAMP;
        }

        return false;
    }

    protected final boolean isFieldSelected(int col) {
        return parent.isFieldSelected(col);
    }

    protected final boolean isCancel() {
        return progressDialog == null || progressDialog.isCancel();
    }

    protected final String getCreateBlobFileName(TableModel model, int col, int row) {
        return model.getColumnName(col) + "_" + row;
    }

    protected final void displayErrorMessage(Throwable e) {
        GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorWritingToFile") + e.getMessage(), e);
    }

    protected final String bundleString(String key) {
        return Bundles.get(ExportDataPanel.class, key);
    }

}
