package org.executequery.gui.exportData;

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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ExportHelperXML extends AbstractExportHelper {

    public ExportHelperXML(ExportDataPanel parent) {
        super(parent);
    }

    @Override
    void exportResultSet(ResultSet resultSet) {

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

            if (tableModel instanceof ResultSetTableModel) {
                Element queryElement = document.createElement("query");
                queryElement.appendChild(document.createTextNode("\n" + ((ResultSetTableModel) tableModel).getQuery() + "\n"));
                rootElement.appendChild(queryElement);
            }

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
                                    document.createTextNode(writeBlobToFile((AbstractLobRecordDataItem) value, saveBlobsIndividually, getCreateBlobFileName(tableModel, col, row))) :
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
