/*
 * ResultSetTableModelToXMLWriter.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.resultset;

import java.io.File;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision$
 * @date     $Date$
 */
public class ResultSetTableModelToXMLWriter {

    private String outputPath;
    private ResultSetTableModel model;

    public ResultSetTableModelToXMLWriter(ResultSetTableModel model, String outputPath) {

        this.model = model;
        this.outputPath = outputPath;
    }

    public void write() throws ParserConfigurationException, TransformerException {
        
        DocumentBuilder documentBuilder = documentBuilder();
        
        Document document = documentBuilder.newDocument();
        Element rootElement = element(document, "result-set");
        document.appendChild(rootElement);

        Element queryElement = element(document, "query");
        queryElement.appendChild(document.createTextNode("\n" + model.getQuery() + "\n"));
        rootElement.appendChild(queryElement);

        Map<String, String> cdata = new HashMap<String, String>();
        Element dataElement = element(document, "data");
        for (int i = 0, n = model.getRowCount(); i < n; i++) {
            
            Element rowElement = element(document, "row");
            dataElement.appendChild(rowElement);
            attribute(document, rowElement, "number", String.valueOf(i + 1));

            dataElement.appendChild(rowElement);
            
            for (int j = 0, m = model.getColumnCount(); j < m; j++) {

                Element valueElement = element(document, model.getColumnName(j));
                RecordDataItem valueAt = (RecordDataItem) model.getValueAt(i, j);
                if (!valueAt.isValueNull()) {

                    valueElement.appendChild(document.createTextNode(valueAt.toString()));
                    
                    String name = valueAt.getName();
                    if (isCDATA(valueAt) && !cdata.containsKey(name)) {
                        
                        cdata.put(name, name);
                    }
                    
                
                } else {

                    valueElement.appendChild(document.createTextNode("NULL"));
                }
                
                rowElement.appendChild(valueElement);
            }
            
        }
        rootElement.appendChild(dataElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        StringBuilder sb = new StringBuilder("query");
        for (Entry<String, String> entry : cdata.entrySet()) {

            sb.append(" ").append(entry.getKey());
        }
        transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, sb.toString()); 
        System.out.println(sb);
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(outputPath));
 
        transformer.transform(source, result);
    }

    private boolean isCDATA(RecordDataItem valueAt) {
        
        int type = valueAt.getDataType();
        return (type == Types.CHAR ||
                type == Types.VARCHAR ||
                type == Types.LONGVARCHAR);
    }

    private Attr attribute(Document document, Element element, String name, String value) {

        Attr attr = document.createAttribute(name);
        attr.setValue(value);
        element.setAttributeNode(attr);
        
        return attr;
    }

    private Element element(Document document, String name) {
        
        return document.createElement(name);
    }
    
    private DocumentBuilder documentBuilder() throws ParserConfigurationException {

        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    
}


