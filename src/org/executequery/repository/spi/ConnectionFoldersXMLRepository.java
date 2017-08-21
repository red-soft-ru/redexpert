/*
 * ConnectionFoldersXMLRepository.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.executequery.repository.spi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.executequery.gui.browser.ConnectionsFolder;
import org.executequery.repository.ConnectionFoldersRepository;
import org.executequery.repository.RepositoryException;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ConnectionFoldersXMLRepository extends AbstractXMLResourceReaderWriter<ConnectionsFolder> 
                                         implements ConnectionFoldersRepository {

    private static final String DEFAULT_XML_RESOURCE = "org/executequery/connection-folders-default.xml";

    private static final String FILE_PATH = "connection-folders.xml";
        
    private List<ConnectionsFolder> folders;
    
    public List<ConnectionsFolder> findAll() {

        return folders();
    }

    @Override
    public ConnectionsFolder add(ConnectionsFolder connectionsFolder) {

        String name = connectionsFolder.getName();
        
        int count = 1;
        while (nameExists(null, name)) {

            name += "_" + (count++);
        }

        connectionsFolder.setName(name);
        folders().add(connectionsFolder);
        
        return connectionsFolder;
    }
    
    public ConnectionsFolder findById(String id) {

        for (ConnectionsFolder folder : folders()) {
            
            if (StringUtils.equals(folder.getId(), id)) {
                
                return folder;
            }
            
        }

        return null;
    }

    public ConnectionsFolder findByName(String name) {

        for (ConnectionsFolder folder : folders()) {
            
            if (folder.getName().equals(name)) {
                
                return folder;
            }
            
        }        
        
        return null;
    }

    public boolean nameExists(ConnectionsFolder exclude, String name) {

        ConnectionsFolder folder = findByName(name);
        if (folder != null && folder != exclude) {
            
            return true;
        }

        return false;
    }

    public synchronized void save() {

        if (namesValid()) {

            save(filePath(), folders);
        }

    }

    public void save(String path, List<ConnectionsFolder> connectionFolders) {
        
        write(path, new ConnectionsFolderParser(), new ConnectionsFolderInputSource(connectionFolders));
    }
    
    public String getId() {

        return REPOSITORY_ID;
    }

    private List<ConnectionsFolder> folders() {

        if (folders == null) {
            
            folders = open();
        }

        return folders;
    }

    private String filePath() {

        UserSettingsProperties settings = new UserSettingsProperties();
        return settings.getUserSettingsDirectory() + FILE_PATH;
    }

    public List<ConnectionsFolder> open(String filePath) {

        return (List<ConnectionsFolder>) read(filePath, new ConnectionsFolderHandler());
    }

    private List<ConnectionsFolder> open() {
        
        ensureFileExists();
        return open(filePath());
    }
    
    private boolean namesValid() {

        for (ConnectionsFolder driver : folders()) {

            if (nameExists(driver, driver.getName())) {

                throw new RepositoryException(
                        String.format("The driver name %s already exists.", driver.getName()));
            }
            
        }

        return true;
    }

    private void ensureFileExists() {

        File file = new File(filePath());
        
        if (!file.exists()) {
            
            try {

                FileUtils.copyResource(DEFAULT_XML_RESOURCE, filePath());

            } catch (IOException e) {

                throw new RepositoryException(e);
            }

        }
        
    }

    private static final String FOLDERS = "connection-folders";
    private static final String FOLDER = "folder";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String CONNECTIONS = "connections";

    class ConnectionsFolderHandler extends AbstractXMLRepositoryHandler<ConnectionsFolder> {

        private List<ConnectionsFolder> folders;
        
        private ConnectionsFolder folder;
        
        ConnectionsFolderHandler() {

            folders = new ArrayList<ConnectionsFolder>();
        }

        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {

            contents().reset();            
        }
        
        public void endElement(String nameSpaceURI, String localName,
                               String qName) {

            if (localNameIsKey(localName, ID)) {

                folder().setId(contentsAsString());

            } else if (localNameIsKey(localName, NAME)) {

                folder().setName(contentsAsString());
                
            } else if (localNameIsKey(localName, CONNECTIONS)) {
                
                folder().setConnections(contentsAsString());

            } else if (localNameIsKey(localName, FOLDER)) {
                
                if (folder != null) {

                    folders.add(folder);
                    folder = new ConnectionsFolder();
                }

            }

        }

        public List<ConnectionsFolder> getRepositoryItemsList() {
            
            return folders;
        }
        
        private ConnectionsFolder folder() {
            
            if (folder != null) {
                
                return folder;
            }

            folder = new ConnectionsFolder();
            return folder;
        }

    } // class ConnectionsFolderHandler

    class ConnectionsFolderInputSource extends InputSource {
        
        private List<ConnectionsFolder> folders;

        public ConnectionsFolderInputSource(List<ConnectionsFolder> folders) {

            super();
            this.folders = folders;
        }
        
        public List<ConnectionsFolder> getFolders() {

            return folders;
        }
        
    } // class ConnectionsFolderInputSource

    class ConnectionsFolderParser extends AbstractXMLRepositoryParser {

        public ConnectionsFolderParser() {}

        public void parse(InputSource input) throws SAXException, IOException {

            if (!(input instanceof ConnectionsFolderInputSource)) {

                throw new SAXException("Parser can only accept a ConnectionsFolderInputSource");
            }
            
            parse((ConnectionsFolderInputSource) input);
        }
        
        public void parse(ConnectionsFolderInputSource input) throws IOException, SAXException {

            validateHandler();
            
            List<ConnectionsFolder> folders = input.getFolders();
            
            handler().startDocument();
            newLine();
            handler().startElement(NSU, FOLDERS, FOLDERS, attributes());
            newLine();

            if (folders != null) {

                writeXMLRows(folders);
            }
            
            newLine();
            handler().endElement(NSU, FOLDERS, FOLDERS);
            handler().endDocument();

        }

        private void writeXMLRows(List<ConnectionsFolder> folders)
            throws SAXException {

            for (ConnectionsFolder folder : folders) {

                newLineIndentOne();
                handler().startElement(NSU, FOLDER, FOLDER, attributes());

                writeXML(ID, folder.getId(), INDENT_TWO);
                writeXML(NAME, folder.getName(), INDENT_TWO);
                writeXML(CONNECTIONS, folder.getConnectionsCommaSeparated(), INDENT_TWO);
                
                newLineIndentOne();
                handler().endElement(NSU, FOLDER, FOLDER);

                handler().ignorableWhitespace(NEW_LINE, 0, 1);
            }

        }

    } // class ConnectionsFolderParser

}



