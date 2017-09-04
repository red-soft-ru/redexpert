/*
 * ConnectionExporter.java
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

package org.executequery.repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.RandomStringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.ConnectionsFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ConnectionExporter {

    public void write(String fileName, List<ConnectionsFolder> folders, List<DatabaseConnection> connections) {
        
        String tempDir = System.getProperty("java.io.tmpdir");
        
        String foldersOutput = randomString();
        String connectionsOutput = randomString();
        
        File foldersFile = new File(tempDir, foldersOutput);
        File connectionsFile = new File(tempDir, connectionsOutput);
        
        connectionFolderRepository().save(foldersFile.getAbsolutePath(), folders);
        databaseConnectionRepository().save(connectionsFile.getAbsolutePath(), connections);
        
        try {

            Document document = merge(foldersFile, connectionsFile);
            write(document, fileName);

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {

            throw new RepositoryException(e);
        
        } finally {

            foldersFile.delete();
            connectionsFile.delete();
        }

    }

    private Document merge(File...files) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document base = documentBuilder.newDocument();

        Node root = base.createElement("executequery-connections");
        base.appendChild(root);

        for (int i = 0; i < files.length; i++) {

            Document document = documentBuilder.parse(files[i]);
            Node nextResults = document.getFirstChild();

            Node importNode = base.importNode(nextResults, true);
            root.appendChild(importNode);
        }

        return base;
    }

    private void write(Document doc, String fileName) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        Result result = new StreamResult(new File(fileName));
        transformer.transform(source, result);
    }

    private String randomString() {

        return RandomStringUtils.randomAlphanumeric(12);
    }
    
    private ConnectionFoldersRepository connectionFolderRepository() {
        
        return (ConnectionFoldersRepository) RepositoryCache.load(ConnectionFoldersRepository.REPOSITORY_ID);
    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
    }

}

