/*
 * DatabaseDefinitionCache.java
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

import org.executequery.ExecuteQuerySystemError;
import org.executequery.datasource.DatabaseDefinition;
import org.executequery.log.Log;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Database definition loader and cache.
 *
 * @author Takis Diakoumis
 */
public class DatabaseDefinitionCache {
    private static List<DatabaseDefinition> databaseDefinitions;

    public static DatabaseDefinition getDatabaseDefinition(int id) {

        if (id == -1)
            return new DatabaseDefinition(DatabaseDefinition.INVALID_DATABASE_ID, "");

        if (databaseDefinitions == null)
            load();

        for (DatabaseDefinition databaseDefinition : databaseDefinitions)
            if (databaseDefinition.getId() == id)
                return databaseDefinition;

        return new DatabaseDefinition(DatabaseDefinition.INVALID_DATABASE_ID, "");
    }

    /**
     * Returns the database definitions within a collection.
     */
    public static List<DatabaseDefinition> getDatabaseDefinitions() {
        if (databaseDefinitions == null)
            load();
        return databaseDefinitions;
    }

    /**
     * Loads the definitions from file.
     */
    public static synchronized void load() {
        databaseDefinitions = new ArrayList<>();

        String path = "org/executequery/databases.xml";
        ClassLoader classLoader = ActionBuilder.class.getClassLoader();

        try (InputStream input = classLoader != null ?
                classLoader.getResourceAsStream(path) :
                ClassLoader.getSystemResourceAsStream(path)) {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);

            SAXParser parser = factory.newSAXParser();
            DatabaseHandler handler = new DatabaseHandler();
            parser.parse(input, handler);

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            throw new ExecuteQuerySystemError(e);
        }
    }

    private static class DatabaseHandler extends DefaultHandler {

        private DatabaseDefinition database = new DatabaseDefinition();
        private final CharArrayWriter contents = new CharArrayWriter();

        @Override
        public void startElement(String nameSpaceURI, String localName, String qName, Attributes attrs) {
            contents.reset();
            if (localName.equals("database"))
                database = new DatabaseDefinition();
        }

        @Override
        public void endElement(String nameSpaceURI, String localName, String qName) {
            switch (localName) {
                case "id":
                    database.setId(Integer.parseInt(contents.toString()));
                    break;
                case "name":
                    database.setName(contents.toString());
                    break;
                case "url":
                    database.addUrlPattern(contents.toString());
                    break;
                case "database":
                    databaseDefinitions.add(database);
                    break;
            }
        }

        @Override
        public void characters(char[] data, int start, int length) {
            contents.write(data, start, length);
        }

        @Override
        public void ignorableWhitespace(char[] data, int start, int length) {
            characters(data, start, length);
        }

        @Override
        public void error(SAXParseException spe) throws SAXException {
            throw new SAXException(spe.getMessage());
        }

    } // DatabaseHandler class

}
