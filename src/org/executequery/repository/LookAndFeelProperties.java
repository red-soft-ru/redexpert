/*
 * LookAndFeelProperties.java
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

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.executequery.GUIUtilities;
import org.executequery.plaf.LookAndFeelDefinition;
import org.executequery.util.UserSettingsProperties;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/** 
 * Look and feel property definition controller.
 *
 * @author   Takis Diakoumis
 */
public class LookAndFeelProperties {
    
    private static Vector<LookAndFeelDefinition> looks;
    
    private static final String ROOT = "customlookandfeels";
    private static final String MAIN_NODE = "lookandfeel";
    private static final String NAME = "name";
    private static final String PATH = "path";
    private static final String CLASS_NAME = "classname";
    private static final String SKIN_LOOK = "skinlookfeel";
    private static final String INSTALLED = "installed";
    private static final String THEME_PACK = "themepack";
    
    private static final String EMPTY = "";
    
    public LookAndFeelProperties() {}
    
    public static void newInstance() {
        new LookAndFeelProperties();
    }
    
    public static LookAndFeelDefinition getLookAndFeel(String name) {

        for (LookAndFeelDefinition lafd : looks) {
            
            if (name.equals(lafd.getName())) {
                return lafd;
            } 
            
        } 
        
        return null;
    }
    
    public static Vector<LookAndFeelDefinition> getLookAndFeelPropertiesVector() {
        if (looks == null) {
            loadLookAndFeels();
        }
        return looks;
    }
    
    public static LookAndFeelDefinition[] getLookAndFeelArray() {
        if (looks == null) {
            loadLookAndFeels();
        }
        
        int v_size = looks.size();
        if (v_size > 0) {
            LookAndFeelDefinition[] lfda = new LookAndFeelDefinition[v_size];            
            for (int i = 0; i < v_size; i++) {
                lfda[i] = (LookAndFeelDefinition)looks.elementAt(i);
            } 
            return lfda;            
        } 
        else {
            return null;
        } 
        
    }
    
    public static LookAndFeelDefinition getInstalledCustomLook() {
        LookAndFeelDefinition lfd = null;        
        for (int i = 0; i < looks.size(); i++) {
            lfd = (LookAndFeelDefinition)looks.elementAt(i);
            if (lfd.isInstalled()) {
                break;
            }
            lfd = null;
        } 
        return lfd;        
    }
    
    public static synchronized int saveLookAndFeels(LookAndFeelDefinition[] lfda) {
        OutputStream os = null;
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            LookAndFeelParser cp = new LookAndFeelParser();
            
            //      File lfXML = new File("conf/lookandfeel.xml");
            File lfXML = new File(filePath());
            
            os = new FileOutputStream(lfXML);
            SAXSource source = new SAXSource(cp, new LookAndFeelInputSource(lfda));
            StreamResult r = new StreamResult(os);
            transformer.transform(source, r);
            
            looks.clear();
            for (int i = 0; i < lfda.length; i++) {
                looks.add(lfda[i]);
            }
            
            return 1;
        } 
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        } 
        finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {}
        }
    }
    
    public static synchronized void loadLookAndFeels() {
        
        File file = new File(filePath());

        if (file.exists()) {
            InputStream in = null;            
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
              
                SAXParser parser = factory.newSAXParser();
                XMLLookAndFeelHandler handler = new XMLLookAndFeelHandler();
                
                in = new FileInputStream(file);
                parser.parse(in, handler);
                
                looks = handler.getLooksVector();
            }             
            catch (Exception e) {
                e.printStackTrace();
                GUIUtilities.displayErrorMessage("Error opening look and feel definitions.");
            } 
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {}
                }
            }
            
        } 
        
        else
            GUIUtilities.displayErrorMessage(
                    "Look & Feel definition XML file not found.\n" +
                    "Ensure the file lookandfeel.xml " +
                    "is in ~/.executequery/conf");
        
    }
    
    private static String filePath() {

        UserSettingsProperties settings = new UserSettingsProperties();

        return settings.getUserSettingsDirectory() + "lookandfeel.xml";
    }

    static class XMLLookAndFeelHandler extends DefaultHandler {
        
        private LookAndFeelDefinition lfd = new LookAndFeelDefinition();
        private CharArrayWriter contents = new CharArrayWriter();
        private Vector<LookAndFeelDefinition> v = new Vector<LookAndFeelDefinition>();
        
        public XMLLookAndFeelHandler() {}
        
        public void startElement(String nameSpaceURI, String localName,
        String qName, Attributes attrs) {
            contents.reset();
            
            if (localName.equals(MAIN_NODE))
                lfd.setInstalled(Boolean.valueOf(
                attrs.getValue(INSTALLED)).booleanValue());
            
        }
        
        public void endElement(String nameSpaceURI, String localName, String qName) {
            
            if (lfd == null)
                lfd = new LookAndFeelDefinition();
            
            if (localName.equals(NAME))
                lfd.setName(contents.toString());
            else if (localName.equals(PATH))
                lfd.setLibraryPath(contents.toString());
            else if (localName.equals(CLASS_NAME))
                lfd.setClassName(contents.toString());
            else if (localName.equals(SKIN_LOOK))
                lfd.setIsSkinLookAndFeel(Integer.parseInt(contents.toString()));
            else if (localName.equals(THEME_PACK)) {
                lfd.setThemePack(contents.toString());
                v.add(lfd);
                lfd = null;
            } 
            
        }
        
        public Vector<LookAndFeelDefinition> getLooksVector() {
            return v;
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
    
    
    static class LookAndFeelParser implements XMLReader {
        private String nsu = "";
        private AttributesImpl atts = new AttributesImpl();
        
        private ContentHandler handler;
        private static final String attType1 = "CDATA";
        private static final char[] newLine = {'\n'};
        private static final String indent_1 = "\n   ";
        private static final String indent_2 = "\n      ";
        
        public LookAndFeelParser() {}
        
        public void parse(InputSource input) throws SAXException, IOException {
            if (!(input instanceof LookAndFeelInputSource))
                throw new SAXException("Parser can only accept a LookAndFeelInputSource");
            
            parse((LookAndFeelInputSource)input);
        }
        
        public void parse(LookAndFeelInputSource input) throws IOException, SAXException {
            try {
                
                if (handler == null)
                    throw new SAXException("No content handler");
                
                LookAndFeelDefinition[] lfda = input.getLookAndFeelArray();
                
                handler.startDocument();
                handler.startElement(nsu, ROOT, ROOT, atts);
                handler.ignorableWhitespace(newLine, 0, 1);
                
                String ZERO = "0";
                String ONE = "1";
                
                for (int i = 0; i < lfda.length; i++) {
                    handler.ignorableWhitespace(indent_1.toCharArray(), 0, indent_1.length());
                    
                    atts.addAttribute(nsu, INSTALLED, INSTALLED, attType1,
                    Boolean.toString(lfda[i].isInstalled()));
                    
                    handler.startElement(nsu, MAIN_NODE, MAIN_NODE, atts);
                    atts.removeAttribute(atts.getIndex(INSTALLED));
                    
                    writeXML(NAME, lfda[i].getName(), indent_2);
                    writeXML(PATH, lfda[i].getLibraryPath(), indent_2);
                    writeXML(CLASS_NAME, lfda[i].getClassName(), indent_2);
                    writeXML(SKIN_LOOK, lfda[i].isSkinLookAndFeel() ? ONE : ZERO, indent_2);
                    writeXML(THEME_PACK, lfda[i].getThemePack(), indent_2);
                    
                    handler.ignorableWhitespace(indent_1.toCharArray(), 0, indent_1.length());
                    handler.endElement(nsu, MAIN_NODE, MAIN_NODE);
                    handler.ignorableWhitespace(newLine, 0, 1);
                    
                } 
                
                handler.ignorableWhitespace(newLine, 0, 1);
                handler.endElement(nsu, ROOT, ROOT);
                handler.endDocument();
                
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
        
        private void writeXML(String name, String line, String space)
        throws SAXException {
            
            if (line == null)
                line = EMPTY;
            
            int textLength = line.length();
            
            handler.ignorableWhitespace(space.toCharArray(), 0, space.length());
            
            handler.startElement(nsu, name, name, atts);
            
            handler.characters(line.toCharArray(), 0, textLength);
            
            handler.endElement(nsu, name, name);
        }
        
        public void setContentHandler(ContentHandler handler) {
            this.handler = handler;
        }
        
        public ContentHandler getContentHandler() {
            return this.handler;
        }
        
        public void setErrorHandler(ErrorHandler handler) {}
        
        public ErrorHandler getErrorHandler() {
            return null;
        }
        
        public void parse(String systemId) throws IOException, SAXException {
        }
        
        public DTDHandler getDTDHandler() {
            return null;
        }
        
        public EntityResolver getEntityResolver() {
            return null;
        }
        
        public void setEntityResolver(EntityResolver resolver) {}
        
        public void setDTDHandler(DTDHandler handler) {}
        
        public Object getProperty(String name) {
            return null;
        }
        
        public void setProperty(String name, java.lang.Object value) {}
        
        public void setFeature(String name, boolean value) {}
        
        public boolean getFeature(String name) {
            return false;
        }
        
    } // class LookAndFeelParser
    
    static class LookAndFeelInputSource extends InputSource {
        private LookAndFeelDefinition[] lfda;
        
        public LookAndFeelInputSource(LookAndFeelDefinition[] la) {
            lfda = la;
        }
        
        public LookAndFeelDefinition[] getLookAndFeelArray() {
            return lfda;
        }
        
    } // class LookAndFeelInputSource

}











