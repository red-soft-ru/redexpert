/*
 * DatabaseDefinitionCache.java
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

package org.executequery.repository;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.executequery.ExecuteQuerySystemError;
import org.executequery.datasource.DatabaseDefinition;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Database definition loader and cache.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DatabaseDefinitionCache {

    /** database definition cache */
    private static List<DatabaseDefinition> databaseDefinitions;
    
    private static final DatabaseDefinition nullDatabaseDefinition = 
            new DatabaseDefinition(DatabaseDefinition.INVALID_DATABASE_ID, "");

    private DatabaseDefinitionCache() {}

    public static DatabaseDefinition getDatabaseDefinition(int id) {
        
        if (databaseDefinitions == null) {
        
            load();
        }

        if (id == -1) {

            return nullDatabaseDefinition;
        }

        for (int i = 0, n = databaseDefinitions.size(); i < n; i++) {
            DatabaseDefinition dd = databaseDefinitions.get(i);
            if (dd.getId() == id) {
                return dd;
            }
        }

        return null;
    }

    public static DatabaseDefinition getDatabaseDefinitionAt(int index) {
        if (databaseDefinitions == null) {
            load();
        }
        return databaseDefinitions.get(index);
    }
    
    /**
     * Returns the database definitions within a collection.
     */
    public static List<DatabaseDefinition> getDatabaseDefinitions() {
        if (databaseDefinitions == null) {
            load();
        }
        return databaseDefinitions;
    }
    
    /**
     * Loads the definitions from file.
     */
    public static synchronized void load() {
        InputStream input = null;
        ClassLoader cl = ActionBuilder.class.getClassLoader();
        
        String path = "org/executequery/databases.xml";
        if (cl != null) {
            input = cl.getResourceAsStream(path);
        }
        else {
            input = ClassLoader.getSystemResourceAsStream(path);
        }

        databaseDefinitions = new ArrayList<DatabaseDefinition>();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            
            SAXParser parser = factory.newSAXParser();
            DatabaseHandler handler = new DatabaseHandler();
            parser.parse(input, handler);
        } 
        catch (Exception e) {
            e.printStackTrace();
            throw new ExecuteQuerySystemError();
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {}
            }
        }

    }

    static class DatabaseHandler extends DefaultHandler {
        
        private DatabaseDefinition database = new DatabaseDefinition();
        private CharArrayWriter contents = new CharArrayWriter();

        public DatabaseHandler() {}
        
        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {           
            contents.reset();
            if (localName.equals("database")) {
                database = new DatabaseDefinition();
            }            
        }
        
        public void endElement(String nameSpaceURI, String localName,
                               String qName) {
            if (localName.equals("id")) {
                database.setId(Integer.parseInt(contents.toString()));
            } 
            else if (localName.equals("name")) {
                database.setName(contents.toString());
            } 
            else if (localName.equals("url")) {
                database.addUrlPattern(contents.toString());
            }
            else if (localName.equals("database")) {
                databaseDefinitions.add(database);
            }
        }
        
        public void characters(char[] data, int start, int length) {
            contents.write(data, start, length);
        }
        
        public void ignorableWhitespace(char[] data, int start, int length) {
            characters(data, start, length);
        }
        
        public void error(SAXParseException spe) throws SAXException {
            throw new SAXException(spe.getMessage());
        }
    } // DatabaseHandler
    
}






