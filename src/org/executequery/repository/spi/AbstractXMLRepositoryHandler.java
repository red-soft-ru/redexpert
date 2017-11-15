/*
 * AbstractXMLRepositoryHandler.java
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

import java.io.CharArrayWriter;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author   Takis Diakoumis
 */
abstract class AbstractXMLRepositoryHandler<T> extends DefaultHandler 
    implements XMLRepositoryHandler<T> {

    private CharArrayWriter contents;
    
    AbstractXMLRepositoryHandler() {
        
        super();
        
        contents = new CharArrayWriter();
    }

    public final void characters(char[] data, int start, int length) {
        
        contents.write(data, start, length);
    }
    
    public final void ignorableWhitespace(char[] data, int start, int length) {
        
        characters(data, start, length);
    }
    
    public final void error(SAXParseException spe) throws SAXException {
        
        throw new SAXException(spe.getMessage());
    }

    protected final CharArrayWriter contents() {
        
        return contents;
    }

    protected final boolean localNameIsKey(String localName, String key) {
        
        return localName.equalsIgnoreCase(key);
    }
    
    protected final String contentsAsString() {

        if (hasContents()) {
        
            return contents.toString();
        }

        return "";
    }

    protected final long contentsAsLong() {
        
        if (hasContents()) {
            
            return Long.parseLong(contentsAsString());
        }

        return 0l;
    }

    protected final int contentsAsInt() {
        
        if (hasContents()) {
            
            return Integer.parseInt(contentsAsString());
        }

        return 0;
    }

    protected final boolean contentsAsBoolean() {
        
        if (hasContents()) {
            
            return Boolean.parseBoolean(contentsAsString());
        }

        return false;
    }

    protected final boolean hasContents() {

        return contents != null && contents.size() > 0;
    }
    
    public abstract List<T> getRepositoryItemsList();

}











