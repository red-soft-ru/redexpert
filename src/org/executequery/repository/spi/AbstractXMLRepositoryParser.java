/*
 * AbstractXMLRepositoryParser.java
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

import java.io.IOException;

import org.executequery.Constants;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
abstract class AbstractXMLRepositoryParser implements XMLReader {

    protected static final String CDDATA = "CDATA";
    protected static final String NSU = Constants.EMPTY;
    protected static final char[] NEW_LINE = {'\n'};
    protected static final String INDENT_ONE = "\n   ";
    protected static final String INDENT_TWO = "\n      ";
    protected static final String INDENT_THREE = "\n         ";

    private ContentHandler handler;
    
    private AttributesImpl attributes;

    AbstractXMLRepositoryParser() {
        attributes = new AttributesImpl();
    }

    protected final ContentHandler handler() {
        return handler;
    }

    protected final AttributesImpl attributes() {
        return attributes;
    }

    protected final void resetAttributes() {

        attributes().clear();
    }
    
    protected final void validateHandler() throws SAXException {

        if (handler == null) {
            
            throw new SAXException("No content handler");
        }

    }

    protected final String valueToString(int value) {
        
        return String.valueOf(value);
    }

    protected final String valueToString(long value) {
        
        return String.valueOf(value);
    }

    protected final String valueToString(boolean value) {
        
        return String.valueOf(value);
    }
    
    protected final void writeXML(String name, 
            String line, String space) throws SAXException {
        
        if (line == null) {

            line = Constants.EMPTY;
        }
        
        int textLength = line.length();

        handler.ignorableWhitespace(space.toCharArray(), 0, space.length());
        handler.startElement(NSU, name, name, attributes);
        handler.characters(line.toCharArray(), 0, textLength);
        handler.endElement(NSU, name, name);
    }
    
    protected final void newLineIndentOne() throws SAXException {
        
        newLineForIndent(INDENT_ONE);
    }
                                             
    protected final void newLineIndentTwo() throws SAXException {
        
        newLineForIndent(INDENT_TWO);
    }

    protected final void newLineIndentThree() throws SAXException {
        
        newLineForIndent(INDENT_THREE);
    }

    protected final void newLine() throws SAXException {
        
        handler().ignorableWhitespace(NEW_LINE, 0, 1);
    }

    private void newLineForIndent(String indent) throws SAXException {
        
        handler().ignorableWhitespace(indent.toCharArray(), 0, indent.length());
    }
    
    public void setContentHandler(ContentHandler handler) {
        this.handler = handler;
    }
    
    public ContentHandler getContentHandler() {
        return this.handler;
    }
    
    public ErrorHandler getErrorHandler() {
        return null;
    }
    
    public DTDHandler getDTDHandler() {
        return null;
    }
    
    public EntityResolver getEntityResolver() {
        return null;
    }

    public Object getProperty(String name) {
        return null;
    }
    
    public boolean getFeature(String name) {
        return false;
    }

    public void setEntityResolver(EntityResolver resolver) {}
    
    public void setDTDHandler(DTDHandler handler) {}

    public void setProperty(String name, java.lang.Object value) {}
    
    public void setFeature(String name, boolean value) {}
    
    public void parse(String systemId) throws IOException, SAXException {}
    
    public void setErrorHandler(ErrorHandler handler) {}

}











