/*
 * DatabaseDriverXMLRepository.java
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

package org.executequery.repository.spi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.DatabaseDriverFactory;
import org.executequery.databasemediators.spi.DatabaseDriverFactoryImpl;
import org.executequery.datasource.ConnectionDataSource;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryException;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DatabaseDriverXMLRepository extends AbstractXMLResourceReaderWriter<DatabaseDriver> 
                                         implements DatabaseDriverRepository {

    private static final String DEFAULT_XML_RESOURCE = "org/executequery/jdbcdrivers-default.xml";

    private static final String FILE_PATH = "jdbcdrivers.xml";
        
    private List<DatabaseDriver> drivers;
    
    public List<DatabaseDriver> findAll() {

        return drivers();
    }

    public DatabaseDriver findById(long id) {

        for (DatabaseDriver driver : drivers()) {
            
            if (driver.getId() == id) {
                
                return driver;
            }
            
        }

        return null;
    }

    public DatabaseDriver findByName(String name) {

        for (DatabaseDriver driver : drivers()) {
            
            if (driver.getName().equals(name)) {
                
                return driver;
            }
            
        }        
        
        return null;
    }

    public boolean nameExists(DatabaseDriver exclude, String name) {

        DatabaseDriver driver = findByName(name);
        
        if (driver != null && driver != exclude) {
            
            return true;
        }

        return false;
    }

    public synchronized void save() {

        if (namesValid()) {

            write(filePath(), new DatabaseDriverParser(), new DatabaseDriverInputSource(drivers));
        }

    }

    public String getId() {

        return REPOSITORY_ID;
    }

    private List<DatabaseDriver> drivers() {

        if (drivers == null) {
            
            drivers = open();
            sanatise(drivers);
        }

        return drivers;
    }

    private String filePath() {

        UserSettingsProperties settings = new UserSettingsProperties();
        
        return settings.getUserSettingsDirectory() + FILE_PATH;
    }

    private List<DatabaseDriver> open() {

        ensureFileExists();
        
        return (List<DatabaseDriver>)read(filePath(), new DatabaseDriverHandler());
    }

    private boolean namesValid() {

        for (DatabaseDriver driver : drivers()) {

            if (nameExists(driver, driver.getName())) {

                throw new RepositoryException(
                        String.format("The driver name %s already exists.", 
                                driver.getName()));
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

    private static final String SERVER = "[server]";
    
    private void sanatise(List<DatabaseDriver> drivers) {

        boolean doSave = false;
        
        int count = 0;
        
        for (DatabaseDriver driver : drivers) {
        
            // check the default id is set against the ODBC driver
            if (!driver.isDefaultSunOdbc() &&
                    DatabaseDriver.SUN_ODBC_DRIVER.equals(driver.getClassName())) {
    
                driver.setId(DatabaseDriver.SUN_ODBC_ID);
                doSave = true;
            }

            // check we have a valid id
            if (!driver.isIdValid()) {

                driver.setId(System.currentTimeMillis() + (++count));
                doSave = true;
            }

            // replace the old format of [server] etc with the new
            if (driver.getURL().contains(SERVER)) {
                
                String url = driver.getURL();
                
                driver.setURL(url.replaceAll(
                        "\\[server\\]", ConnectionDataSource.HOST));
                doSave = true;
            }
            
        }

        if (doSave) {
            
            save();
        }
        
    }
    
    private static final String JDBC_DRIVERS = "jdbcdrivers";
    private static final String DRIVER = "databasedrivers";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String TYPE = "type";
    private static final String PATH = "path";
    private static final String CLASS_NAME = "classname";
    private static final String URL = "url";

    class DatabaseDriverHandler extends AbstractXMLRepositoryHandler<DatabaseDriver> {

        private List<DatabaseDriver> drivers;
        
        private DatabaseDriver driver;
        
        DatabaseDriverHandler() {

            drivers = new ArrayList<DatabaseDriver>();
        }

        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {

            contents().reset();            
        }
        
        public void endElement(String nameSpaceURI, String localName,
                               String qName) {

            if (localNameIsKey(localName, ID)) {

                driver().setId(contentsAsLong());

            } else if (localNameIsKey(localName, NAME)) {

                driver().setName(contentsAsString());
                
            } else if (localNameIsKey(localName, DESCRIPTION)) {
                
                driver().setDescription(contentsAsString());

            } else if (localNameIsKey(localName, TYPE)) {

                driver().setDatabaseType(contentsAsInt());

            } else if (localNameIsKey(localName, PATH)) {

                driver().setPath(contentsAsString());

            } else if (localNameIsKey(localName, CLASS_NAME)) {

                driver().setClassName(contentsAsString());
                
            } else if (localNameIsKey(localName, URL)) {

                driver().setURL(contentsAsString());

            } else if (localNameIsKey(localName, DRIVER)) {
                
                if (driver != null) {

                    drivers.add(driver);

                    driver = createDriver();
                }

            }

        }

        public List<DatabaseDriver> getRepositoryItemsList() {
            
            return drivers;
        }
        
        private DatabaseDriver driver() {
            
            if (driver != null) {
                
                return driver;
            }

            driver = createDriver();

            return driver;
        }

        private DatabaseDriver createDriver() {
            
            if (driverFactory == null) {
                
                driverFactory = new DatabaseDriverFactoryImpl();
            }
            
            return driverFactory.create();
        }
        
        private DatabaseDriverFactory driverFactory;
        
    } // class DatabaseDriverHandler

    class DatabaseDriverInputSource extends InputSource {
        
        private List<DatabaseDriver> drivers;

        public DatabaseDriverInputSource(List<DatabaseDriver> drivers) {

            super();
            this.drivers = drivers;
        }
        
        public List<DatabaseDriver> getDrivers() {

            return drivers;
        }
        
    } // class DatabaseDriverInputSource

    class DatabaseDriverParser extends AbstractXMLRepositoryParser {

        public DatabaseDriverParser() {}

        public void parse(InputSource input) throws SAXException, IOException {

            if (!(input instanceof DatabaseDriverInputSource)) {

                throw new SAXException(
                        "Parser can only accept a DatabaseDriverInputSource");
            }
            
            parse((DatabaseDriverInputSource)input);
        }
        
        public void parse(DatabaseDriverInputSource input) 
            throws IOException, SAXException {

            validateHandler();
            
            List<DatabaseDriver> drivers = input.getDrivers();
            
            handler().startDocument();
            newLine();
            handler().startElement(NSU, JDBC_DRIVERS, JDBC_DRIVERS, attributes());
            newLine();

            if (drivers != null) {

                writeXMLRows(drivers);
            }
            
            newLine();
            handler().endElement(NSU, JDBC_DRIVERS, JDBC_DRIVERS);
            handler().endDocument();

        }

        private void writeXMLRows(List<DatabaseDriver> drivers)
            throws SAXException {

            int count = 0;
            
            for (DatabaseDriver driver : drivers) {

                if (!driver.isIdValid()) {
                    
                    driver.setId(System.currentTimeMillis() + (++count));
                }

                newLineIndentOne();
                handler().startElement(NSU, DRIVER, DRIVER, attributes());

                writeXML(ID, String.valueOf(driver.getId()), INDENT_TWO);
                writeXML(NAME, driver.getName(), INDENT_TWO);
                writeXML(DESCRIPTION, driver.getDescription(), INDENT_TWO);
                writeXML(TYPE, String.valueOf(driver.getType()), INDENT_TWO);
                writeXML(PATH, driver.getPath(), INDENT_TWO);
                writeXML(CLASS_NAME, driver.getClassName(), INDENT_TWO);
                writeXML(URL, driver.getURL(), INDENT_TWO);
                
                newLineIndentOne();
                handler().endElement(NSU, DRIVER, DRIVER);

                handler().ignorableWhitespace(NEW_LINE, 0, 1);
            }

        }

    } // class QueryBookmarkParser

}











