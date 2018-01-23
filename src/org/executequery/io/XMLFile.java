package org.executequery.io;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;

public class XMLFile {

    DocumentBuilder documentBuilder;
    Document document;

    public XMLFile(String path) {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = documentBuilder.parse(path);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    public static void replaceChild(Node newChild, Node oldChild, Node parentNode) {
        newChild = oldChild.getOwnerDocument().importNode(newChild, true);
        parentNode.replaceChild(newChild, oldChild);
    }

    public static void appendChild(Node newChild, Node parentNode) {
        newChild = parentNode.getOwnerDocument().importNode(newChild, true);
        parentNode.appendChild(newChild);
    }

    public static Node getNodeFromNodes(String key, String value, Node nodes) {
        NodeList childNodes = nodes.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            NodeList child = childNodes.item(i).getChildNodes();
            for (int g = 0; g < child.getLength(); g++) {
                if (child.item(g).getNodeType() != Node.TEXT_NODE) {
                    String nodeName = child.item(g).getNodeName();
                    Node item = child.item(g).getChildNodes().item(0);
                    if (item == null)
                        continue;
                    String textContect = item.getTextContent();
                    if (nodeName.equals(key) && textContect.equals(value))
                        return childNodes.item(i);
                }
            }
        }
        return null;
    }

    public static String getStringValue(String key, Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(key))
                return child.getChildNodes().item(0).getTextContent();
        }
        return null;
    }

    public static boolean equals(Node node1, Node node2) {
        if (node1.hasChildNodes() && node2.hasChildNodes()) {
            if (node1.getChildNodes().getLength() == node2.getChildNodes().getLength()) {
                for (int i = 0; i < node1.getChildNodes().getLength(); i++) {
                    if (!equals(node1.getChildNodes().item(i), node2.getChildNodes().item(i)))
                        return false;
                }
                return true;
            } else return false;
        } else if (node1.hasChildNodes() || node2.hasChildNodes())
            return false;
        else {
            String name1 = node1.getNodeName();
            String name2 = node2.getNodeName();
            String value1 = node1.getTextContent();
            String value2 = node2.getTextContent();
            if (name1 == null && name2 == null) {
                if (value1 == null && value2 == null)
                    return true;
                else
                    return value1 != null && value2 != null && value1.contentEquals(value2);
            } else {
                if (name1 == null || name2 == null)
                    return false;
                else if (name1.contentEquals(name2)) {
                    if (value1 == null && value2 == null)
                        return true;
                    else
                        return value1 != null && value2 != null && value1.contentEquals(value2);
                } else return false;
            }
        }

    }

    public Element getRootNode() {
        return document.getDocumentElement();
    }

    public void save(String path) {
        document.setDocumentURI(path);
        save();
    }

    public void save() {
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(document);
            FileOutputStream fos = new FileOutputStream(document.getDocumentURI());
            StreamResult result = new StreamResult(fos);
            tr.transform(source, result);
        } catch (TransformerException | IOException e) {
            e.printStackTrace();
        }
    }
}
