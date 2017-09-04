/*
 * NamedQueryCache.java
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

package org.underworldlabs.jdbc;

import java.io.BufferedInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.underworldlabs.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Dynamic SQL query loader and cache.
 *
 * @author Takis Diakoumis
 * @version $Revision: 1780 $
 * @date $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class NamedQueryCache {
   
    public static final String QUERY = "query";
    public static final String NAME = "name";

    private static Map<String,String> queries;

    public String getNamedQuery(String name) {
        if (queries == null) {
            throw new IllegalArgumentException("Queries not loaded.");
        }        
        if (queries.containsKey(name)) {
            return queries.get(name);
        } else {
            throw new IllegalArgumentException(
                    "Query with name " + name + " not found.");
        }
    }

    public void loadFromResource(String path) {
        ClassLoader cl = NamedQueryCache.class.getClassLoader();

        if (cl != null) {
            load(cl.getResourceAsStream(path));
        } else {
            load(ClassLoader.getSystemResourceAsStream(path));
        }            
    }
    
    public void loadFromFile(String path) {
        File file = new File(path);

        if (file.exists()) {

            try {
                load(new FileInputStream(file));
            } catch (FileNotFoundException e) {}
                
        } else {
            throw new IllegalArgumentException("Specified file not found");
        }
    }
    
    private void load(InputStream input) {
         
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();

            DefaultHandler handler = new NamedQueryHandler();
            parser.parse(new BufferedInputStream(input), handler);

            if (queries == null || queries.size() == 0) {
                Log.info("Query cache loaded - no pre-defined queries found.");
                return;
            }

            Log.info("Query cache loaded.");
        }
        catch (Exception e) {
            Log.error("Error loading named queries from file.", e);
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {}
            }
        }
    }
    
    /**
     * XML handler class to load the properties from file.
     */
    private class NamedQueryHandler extends DefaultHandler {

        private String queryName;
        private CharArrayWriter contents;
        
        public NamedQueryHandler() {
            queries = new HashMap<String,String>();
            contents = new CharArrayWriter();
        }

        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {
            contents.reset();

            if (localName.equals(QUERY)) {
                queryName = attrs.getValue(NAME);
            }
            
        }
        
        public void endElement(String nameSpaceURI, 
                               String localName,
                               String qName) {

            if (localName.equals(QUERY) && queryName != null) {
                queries.put(queryName, contents.toString());
            }
            queryName = null;            
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
        
    } // XMLHandler
    
}











