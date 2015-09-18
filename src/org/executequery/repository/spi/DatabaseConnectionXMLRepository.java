/*
 * DatabaseConnectionXMLRepository.java
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
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseConnectionFactory;
import org.executequery.databasemediators.spi.DatabaseConnectionFactoryImpl;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryException;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DatabaseConnectionXMLRepository extends AbstractXMLRepository<DatabaseConnection>
                                             implements DatabaseConnectionRepository {

    private static final String FILE_PATH = "savedconnections.xml";

    private static final String DEFAULT_XML_RESOURCE = "org/executequery/savedconnections-default.xml";

    private List<DatabaseConnection> connections;

    public List<DatabaseConnection> findAll() {

        return connections();
    }

    public DatabaseConnection findById(String id) {

        for (DatabaseConnection connection : connections()) {

            if (connection.getId().equals(id)) {

                return connection;
            }

        }

        return null;
    }

    public DatabaseConnection findByName(String name) {

        List<DatabaseConnection> _connections = connections();
        synchronized (_connections) {

            for (DatabaseConnection connection : _connections) {
                
                if (connection.getName().equals(name)) {
                    
                    return connection;
                }
                
            }
            
        }

        return null;
    }

    public boolean nameExists(DatabaseConnection exclude, String name) {

        DatabaseConnection connection = findByName(name);
        if (connection != null && connection != exclude) {

            return true;
        }

        return false;
    }

    public synchronized void save() {

        if (namesValid()) {
            
            write(filePath(), new DatabaseConnectionParser(),
                    new DatabaseConnectionInputSource(connections));
        }

    }

    public String getId() {

        return REPOSITORY_ID;
    }

    private List<DatabaseConnection> connections() {

        if (connections == null) {

            connections = open();
        }

        return connections;
    }

    private String filePath() {

        UserSettingsProperties settings = new UserSettingsProperties();
        return settings.getUserSettingsDirectory() + FILE_PATH;
    }

    private List<DatabaseConnection> open() {

        try {
        
            ensureFileExists();
            return (List<DatabaseConnection>) read(filePath(), new DatabaseConnectionHandler());

        } catch (RepositoryException e) {
            
            Log.error("Error reading saved connections file - " + e.getMessage(), e);
            return new ArrayList<DatabaseConnection>(0);
        }

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

    private boolean namesValid() {

        for (DatabaseConnection connection : connections()) {

            if (nameExists(connection, connection.getName())) {

                throw new RepositoryException(
                        String.format("The connection name %s already exists.",
                                connection.getName()));
            }

        }

        return true;
    }


    private static final String SAVED_CONNECTIONS = "savedconnections";
    private static final String CONNECTION = "connection";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String FOLDER_ID = "folderid";
    private static final String ENCRYPTED = "encrypted";
    private static final String DRIVER_ID = "driverid";
    private static final String HOST = "host";
    private static final String DATA_SOURCE = "datasource";
    private static final String TX_ISOLATION = "txisolation";
    private static final String AUTO_COMMIT = "autocommit";
    private static final String PORT = "port";
    private static final String URL = "url";
    private static final String DRIVER_NAME = "drivername";
    private static final String ADVANCED = "advanced";
    private static final String PROPERTY = "property";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String STORE_PASSWORD = "storepassword";
    private static final String SSH_TUNNEL = "sshtunnel";
    private static final String SSH_USER_NAME = "sshusername";
    private static final String SSH_PASSWORD = "sshpassword";
    private static final String SSH_PORT = "sshport";
    private static final String SSH_STORE_PASSWORD = "sshstorepassword";

    class DatabaseConnectionHandler extends AbstractXMLRepositoryHandler<DatabaseConnection> {

        private List<DatabaseConnection> connections;

        private DatabaseConnection connection;

        private Properties advancedProperties;

        DatabaseConnectionHandler() {

            connections = new Vector<DatabaseConnection>();
        }

        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {

            contents().reset();

            if (localName.equals(CONNECTION)) {

                connection = createConnection();

                String value = attrs.getValue(STORE_PASSWORD);
                if (!MiscUtils.isNull(value)) {

                    connection.setPasswordStored(Boolean.parseBoolean(value));
                }

            } else if (localName.equals(PASSWORD)) {

                String value = attrs.getValue(ENCRYPTED);
                if (!MiscUtils.isNull(value)) {

                    connection().setPasswordEncrypted(Boolean.parseBoolean(value));
                }

            }
            else if (localName.equals(PROPERTY)) {

                if (advancedProperties == null) {

                    advancedProperties = new Properties();
                }

                advancedProperties.setProperty(
                        attrs.getValue(KEY), attrs.getValue(VALUE));

            }

        }

        public void endElement(String nameSpaceURI, String localName, String qName) {

            // this could be better... o_O
            
            String contentsAsString = contentsAsString();
            DatabaseConnection databaseConnection = connection();
            if (localNameIsKey(localName, NAME)) {

                databaseConnection.setName(contentsAsString);

            } else if (localNameIsKey(localName, ID)) {

                databaseConnection.setId(contentsAsString);

            } else if (localNameIsKey(localName, USER)) {

                databaseConnection.setUserName(contentsAsString);

            } else if (localNameIsKey(localName, PASSWORD)) {

                if (hasContents()) {

                    String value = contentsAsString;
                    if (databaseConnection.isPasswordEncrypted()) {

                        databaseConnection.setEncryptedPassword(value);

                    } else {

                        databaseConnection.setPassword(value);
                    }

                    databaseConnection.setPasswordStored(true);

                } else {

                    databaseConnection.setPasswordStored(false);
                }

            } else if (localNameIsKey(localName, HOST)) {

                databaseConnection.setHost(contentsAsString);

            } else if (localNameIsKey(localName, DATA_SOURCE)) {

                databaseConnection.setSourceName(contentsAsString);

            } else if (localNameIsKey(localName, PORT)) {

                databaseConnection.setPort(contentsAsString);

            } else if (localNameIsKey(localName, URL)) {

                databaseConnection.setURL(contentsAsString);

            } else if (localNameIsKey(localName, DRIVER_ID)) {

                databaseConnection.setDriverId(contentsAsLong());

            } else if (localNameIsKey(localName, FOLDER_ID)) {
                
                databaseConnection.setFolderId(contentsAsString);
                
            } else if (localNameIsKey(localName, DRIVER_NAME)) {

                databaseConnection.setDriverName(contentsAsString);

            } else if (localNameIsKey(localName, SSH_STORE_PASSWORD)) {
                
                if (hasContents()) {
                
                    databaseConnection.setSshPasswordStored(Boolean.valueOf(contentsAsString));
                
                } else {

                    databaseConnection.setSshPasswordStored(false);
                }
                
            } else if (localNameIsKey(localName, SSH_TUNNEL)) {
                
                if (hasContents()) {
                    
                    databaseConnection.setSshTunnel(Boolean.valueOf(contentsAsString));
                    
                } else {
                    
                    databaseConnection.setSshTunnel(false);
                }
                
            } else if (localNameIsKey(localName, SSH_USER_NAME)) {
                
                databaseConnection.setSshUserName(contentsAsString);
                
            } else if (localNameIsKey(localName, SSH_PASSWORD)) {
                
                databaseConnection.setEncryptedSshPassword(contentsAsString);
                
            } else if (localNameIsKey(localName, SSH_PORT)) {
                
                if (hasContents()) {
                 
                    databaseConnection.setSshPort(contentsAsInt());
                }
                
            } else if (localNameIsKey(localName, AUTO_COMMIT)) {

                if (hasContents()) {

                    databaseConnection.setAutoCommit(contentsAsBoolean());
                }

            }
            else if (localNameIsKey(localName, TX_ISOLATION)) {

                if (hasContents()) {

                    databaseConnection.setTransactionIsolation(contentsAsInt());

                } else {

                    databaseConnection.setTransactionIsolation(-1);
                }

            }
            else if (localNameIsKey(localName, ADVANCED)) {

                if (advancedProperties != null && advancedProperties.size() > 0) {

                    databaseConnection.setJdbcProperties(advancedProperties);
                }

            }
            else if (localNameIsKey(localName, CONNECTION)) {

                if (databaseConnection != null) {

                    connections.add(connection);

                    connection = createConnection();

                    advancedProperties = null;
                }

            }

        }

        public List<DatabaseConnection> getRepositoryItemsList() {

            return connections;
        }

        private DatabaseConnection connection() {

            if (connection != null) {

                return connection;
            }

            connection = createConnection();

            return connection;
        }

        private DatabaseConnection createConnection() {

            if (connectionFactory == null) {

                connectionFactory = new DatabaseConnectionFactoryImpl();
            }

            return connectionFactory.create();
        }

        private DatabaseConnectionFactory connectionFactory;

    } // class DatabaseConnectionHandler

    class DatabaseConnectionInputSource extends InputSource {

        private List<DatabaseConnection> connections;

        public DatabaseConnectionInputSource(List<DatabaseConnection> connections) {

            super();
            this.connections = connections;
        }

        public List<DatabaseConnection> getConnections() {

            return connections;
        }

    } // class DatabaseConnectionInputSource

    class DatabaseConnectionParser extends AbstractXMLRepositoryParser {

        public DatabaseConnectionParser() {}

        public void parse(InputSource input) throws SAXException, IOException {

            if (!(input instanceof DatabaseConnectionInputSource)) {

                throw new SAXException(
                        "Parser can only accept a DatabaseDriverInputSource");
            }

            parse((DatabaseConnectionInputSource)input);
        }

        public void parse(DatabaseConnectionInputSource input)
            throws IOException, SAXException {

            validateHandler();

            List<DatabaseConnection> connections = input.getConnections();

            handler().startDocument();
            newLine();
            handler().startElement(NSU, SAVED_CONNECTIONS, SAVED_CONNECTIONS, attributes());
            newLine();

            if (connections != null) {

                writeXMLRows(connections);
            }

            newLine();
            handler().endElement(NSU, SAVED_CONNECTIONS, SAVED_CONNECTIONS);
            handler().endDocument();

        }

        private void writeXMLRows(List<DatabaseConnection> connections)
            throws SAXException {

            for (DatabaseConnection connection : connections) {

                handler().ignorableWhitespace(
                        INDENT_ONE.toCharArray(), 0, INDENT_ONE.length());

                attributes().addAttribute(NSU, STORE_PASSWORD, STORE_PASSWORD,
                        CDDATA, valueToString(connection.isPasswordStored()));

                handler().startElement(NSU, CONNECTION, CONNECTION, attributes());

                resetAttributes();

                writeXML(ID, connection.getId(), INDENT_TWO);
                writeXML(NAME, connection.getName(), INDENT_TWO);
                writeXML(USER, connection.getUserName(), INDENT_TWO);

                attributes().addAttribute(NSU, ENCRYPTED, ENCRYPTED, CDDATA,
                                  valueToString(connection.isPasswordEncrypted()));

                if (connection.isPasswordStored()) {

                    writeXML(PASSWORD, connection.getPassword(), INDENT_TWO);

                } else {

                    writeXML(PASSWORD, Constants.EMPTY, INDENT_TWO);
                }

                resetAttributes();

                writeXML(HOST, connection.getHost(), INDENT_TWO);
                writeXML(DATA_SOURCE, connection.getSourceName(), INDENT_TWO);
                writeXML(PORT, connection.getPort(), INDENT_TWO);
                writeXML(URL, connection.getURL(), INDENT_TWO);

                // TODO: remove driver name from save
                writeXML(DRIVER_NAME, connection.getDriverName(), INDENT_TWO);

                writeXML(DRIVER_ID, valueToString(connection.getDriverId()), INDENT_TWO);
                writeXML(FOLDER_ID, connection.getFolderId(), INDENT_TWO);

                writeXML(AUTO_COMMIT,
                        valueToString(connection.isAutoCommit()), INDENT_TWO);

                writeXML(TX_ISOLATION,
                        valueToString(connection.getTransactionIsolation()), INDENT_TWO);

                writeXML(SSH_TUNNEL,
                        valueToString(connection.isSshTunnel()), INDENT_TWO);
                
                writeXML(SSH_USER_NAME, connection.getSshUserName(), INDENT_TWO);

                writeXML(SSH_STORE_PASSWORD, 
                        valueToString(connection.isSshPasswordStored()), INDENT_TWO);
                
                writeXML(SSH_PORT, 
                        valueToString(connection.getSshPort()), INDENT_TWO);
                
                if (connection.isSshPasswordStored()) {
                 
                    writeXML(SSH_PASSWORD, connection.getSshPassword(), INDENT_TWO);
                }

                if (connection.hasAdvancedProperties()) {

                    handler().ignorableWhitespace(INDENT_TWO.toCharArray(), 0, INDENT_TWO.length());
                    handler().startElement(NSU, ADVANCED, ADVANCED, attributes());

                    Properties properties = connection.getJdbcProperties();

                    for (Enumeration<?> i = properties.keys(); i.hasMoreElements();) {

                        String key  = (String)i.nextElement();

                        attributes().addAttribute(
                                Constants.EMPTY, KEY, KEY, CDDATA, key);
                        attributes().addAttribute(
                                Constants.EMPTY, VALUE, VALUE, CDDATA,
                                properties.getProperty(key));

                        writeXML(PROPERTY, null, INDENT_THREE);

                        resetAttributes();
                    }

                    newLineIndentTwo();
                    handler().endElement(NSU, ADVANCED, ADVANCED);

                } else {

                    writeXML(ADVANCED, null, INDENT_TWO);
                }

                newLineIndentOne();
                handler().endElement(NSU, CONNECTION, CONNECTION);
                newLine();

            }

        }

    } // class DatabaseConnectionParser

}










