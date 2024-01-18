package org.executequery.gui.exportData;

import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.resultset.AbstractLobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class ExportHelperXML extends AbstractExportHelper {

    public ExportHelperXML(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    void exportResultSet(ResultSet resultSet) {

        String filePath = parent.getFilePath();
        String nullReplacement = parent.getNullReplacement();
        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();

        try {

            ResultSetMetaData metaData = resultSet.getMetaData();
            List<ColumnData> columns = getCreateColumnData(metaData);
            int columnCount = metaData.getColumnCount();

            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = document.createElement("result-set");
            document.appendChild(rootElement);

            List<String> exportData = new ArrayList<>();
            Element dataElement = document.createElement("data");

            int row = 0;
            while (resultSet.next()) {

                if (isCancel())
                    break;

                Element rowElement = document.createElement("row");
                dataElement.appendChild(rowElement);

                Attr attribute = document.createAttribute("number");
                attribute.setValue(String.valueOf(row + 1));
                rowElement.setAttributeNode(attribute);

                dataElement.appendChild(rowElement);

                for (int col = 1; col < columnCount + 1; col++) {

                    if (isCancel())
                        break;

                    if (!isFieldSelected(col - 1))
                        continue;

                    Element valueElement = document.createElement(metaData.getColumnName(col));

                    Object value = resultSet.getObject(col);
                    ColumnData columnData = columns.get(col - 1);

                    if (value != null) {

                        valueElement.appendChild(isBlobType(columnData) ?
                                document.createTextNode(writeBlob(resultSet.getBlob(col), saveBlobsIndividually, getCreateBlobFileName(columnData, col, row))) :
                                document.createTextNode(value.toString())
                        );

                        String name = columnData.getColumnName();
                        if (isCharType(columnData) && !exportData.contains(name))
                            exportData.add(name);

                    } else
                        valueElement.appendChild(document.createTextNode(nullReplacement));

                    rowElement.appendChild(valueElement);
                }

                row++;
            }
            rootElement.appendChild(dataElement);

            StringBuilder query = new StringBuilder("query");
            exportData.forEach(val -> query.append(" ").append(val));

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, query.toString());
            transformer.transform(new DOMSource(document), new StreamResult(new File(filePath)));

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

    @Override
    void exportTableModel(TableModel tableModel) {

        String filePath = parent.getFilePath();
        String nullReplacement = parent.getNullReplacement();
        boolean saveBlobsIndividually = parent.isSaveBlobsIndividually();

        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();

        try {

            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = document.createElement("result-set");
            document.appendChild(rootElement);

            List<String> exportData = new ArrayList<>();
            Element dataElement = document.createElement("data");

            for (int row = 0; row < rowCount; row++) {

                if (isCancel())
                    break;

                Element rowElement = document.createElement("row");
                dataElement.appendChild(rowElement);

                Attr attribute = document.createAttribute("number");
                attribute.setValue(String.valueOf(row + 1));
                rowElement.setAttributeNode(attribute);

                dataElement.appendChild(rowElement);

                for (int col = 0; col < columnCount; col++) {

                    if (isCancel())
                        break;

                    if (!isFieldSelected(col))
                        continue;

                    Element valueElement = document.createElement(tableModel.getColumnName(col));
                    if (tableModel instanceof ResultSetTableModel) {

                        RecordDataItem value = (RecordDataItem) tableModel.getValueAt(row, col);
                        if (!value.isValueNull()) {

                            valueElement.appendChild(isBlobType(value) ?
                                    document.createTextNode(writeBlob((AbstractLobRecordDataItem) value, saveBlobsIndividually, getCreateBlobFileName(tableModel, col, row))) :
                                    document.createTextNode(value.toString())
                            );

                            String name = value.getName();
                            if (isCharType(value) && !exportData.contains(name))
                                exportData.add(name);

                        } else
                            valueElement.appendChild(document.createTextNode(nullReplacement));

                    } else {

                        Object value = tableModel.getValueAt(row, col);
                        valueElement.appendChild(value != null ?
                                document.createTextNode(value.toString()) :
                                document.createTextNode(nullReplacement)
                        );
                    }

                    rowElement.appendChild(valueElement);
                }
            }
            rootElement.appendChild(dataElement);

            StringBuilder query = new StringBuilder("query");
            exportData.forEach(val -> query.append(" ").append(val));

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, query.toString());
            transformer.transform(new DOMSource(document), new StreamResult(new File(filePath)));

        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }

}
