/*
 * UserLayoutProperties.java
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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.base.DockedDragEvent;
import org.executequery.base.DockedTabDragListener;
import org.executequery.base.DockedTabEvent;
import org.executequery.base.DockedTabListener;
import org.executequery.base.DockedTabView;
import org.executequery.base.TabComponent;
import org.executequery.log.Log;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.SystemProperties;
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

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class UserLayoutProperties implements Serializable,
                                             DockedTabDragListener,
                                             DockedTabListener,
                                             PropertyChangeListener {

	private static final String DEFAULT_LAYOUT_PREFERENCES_XML = "org/executequery/layout-preferences.xml";

    private static final String LAYOUT_PREFERENCES_XML = "layout-preferences.xml";

    private static final long serialVersionUID = 1L;
	
    /** layout prefs objects */
    private Map<String, UserLayoutObject> layoutObjects;
    
    /** Creates a new instance of UserLayoutProperties */
    public UserLayoutProperties() {
        load();
    }

    /**
     * Indicates a tab minimised event.
     *
     * @param the event 
     */
    public void tabMinimised(DockedTabEvent e) {
        TabComponent tc = (TabComponent)e.getSource();
        if (tc == null) {
            return;
        }
        
        Component c = tc.getComponent();
        if (c instanceof DockedTabView) {
            DockedTabView tabView = (DockedTabView)c;
            String key = tabView.getPropertyKey();
            
            UserLayoutObject object = layoutObjects.get(key);
            if (object == null) {
                object = new UserLayoutObject(key);
            }
            object.setMinimised(true);
            object.setVisible(true);
            layoutObjects.put(key, object);

            // save the layout options
            save();
        }

    }

    /**
     * Indicates a tab restored from minimised event.
     *
     * @param the event 
     */
    public void tabRestored(DockedTabEvent e) {
        TabComponent tc = (TabComponent)e.getSource();
        if (tc == null) {
            return;
        }
        
        Component c = tc.getComponent();
        if (c instanceof DockedTabView) {
            DockedTabView tabView = (DockedTabView)c;
            String key = tabView.getPropertyKey();
            
            UserLayoutObject object = layoutObjects.get(key);
            if (object == null) {
                object = new UserLayoutObject(key);
            }
            object.setMinimised(false);
            object.setVisible(true);
            layoutObjects.put(key, object);

            // save the layout options
            save();
        }
    }

    /**
     * Indicates a tab selected event.
     *
     * @param the event 
     */
    public void tabSelected(DockedTabEvent e) {}

    /**
     * Indicates a tab deselected event.
     *
     * @param the event 
     */
    public void tabDeselected(DockedTabEvent e) {}
    
    /**
     * Indicates a tab closed event.
     *
     * @param the event 
     */
    public void tabClosed(DockedTabEvent e) {
        TabComponent tc = (TabComponent)e.getSource();
        if (tc == null) {
            return;
        }
        
        Component c = tc.getComponent();
        if (c instanceof DockedTabView) {
            DockedTabView tabView = (DockedTabView)c;
            String key = tabView.getPropertyKey();
            setDockedPaneVisible(key, false);
        }
    }

    /**
     * Invoked when a mouse button is pressed on a tab and then dragged.
     *
     * @param the encapsulating event object
     */
    public void dockedTabDragged(DockedDragEvent e) {}
 
    /**
     *  Invoked when a mouse button has been released on a tab.
     *
     * @param the encapsulating event object
     */
    public void dockedTabReleased(DockedDragEvent e) {
        TabComponent tc = e.getTabComponent();
        if (tc == null) {
            return;
        }
        
        Component c = tc.getComponent();
        if (c instanceof DockedTabView) {
            DockedTabView tabView = (DockedTabView)c;
            String key = tabView.getPropertyKey();

            UserLayoutObject object = layoutObjects.get(key);
            if (object == null) {
                object = new UserLayoutObject(key);
            }
            
            int newIndex = e.getTabComponent().getIndex();
            int position = e.getTabComponent().getPosition();
            if (newIndex == object.getIndex() && position == object.getPosition()) {
                return;
            }

            // store the old index
            int oldIndex = object.getIndex();
            
            object.setPosition(position);
            object.setIndex(newIndex);
            object.setVisible(true);

            // check existing object indexes for the same position
            for (Iterator i = layoutObjects.keySet().iterator(); i.hasNext();) {
                Object next = i.next();
                if (next != null) {

                    String _key = next.toString();
                    if (!_key.equals(key)) {
                        UserLayoutObject _object = layoutObjects.get(_key);
                        if (_object.getPosition() == position) {
                            int _index = _object.getIndex();
                            if (oldIndex < newIndex) { // move right
                                if (newIndex >= _index && _index > 0) {
                                    _object.setIndex(_index - 1);
                                }
                            }
                            else if (oldIndex > newIndex) { // move left
                                if (newIndex <= _index) {
                                    _object.setIndex(_index + 1);
                                }
                            }
                        }
                    }

                }
            }

            layoutObjects.put(key, object);

            // save the layout options
            save();
        }

    }

    /** the layout sorter */
    private static LayoutSorter layoutSorter;
    
    public List<UserLayoutObject> getLayoutObjectsSorted() {

        if (layoutObjects == null || layoutObjects.isEmpty()) {
        
            return null;
        }

        int size = layoutObjects.size();
        
        // place in temp list
        List<UserLayoutObject> list = new ArrayList<UserLayoutObject>(size);
        
        for (Iterator i = layoutObjects.keySet().iterator(); i.hasNext();) {
        
            String key = i.next().toString();
            list.add(layoutObjects.get(key));
        }

        if (layoutSorter == null) {
            layoutSorter = new LayoutSorter();
        }

        Collections.sort(list, layoutSorter);
        return list;
    }
    
    /**
     * Property change implementation to record the movements
     * of the divider locations on all panes. The new value is stored
     * and saved in the user properties/preferences.
     *
     * @param the event
     */
    public void propertyChange(PropertyChangeEvent e) {

        String name = e.getPropertyName();
        int value = ((Integer)e.getNewValue()).intValue();
        
        SystemProperties.setIntProperty(
                Constants.USER_PROPERTIES_KEY, name, value);

    }

    /**
     * Returns a map of layout definition objects.
     *
     * @return the user's saved layout definitions
     */
    public Map<String, UserLayoutObject> getLayoutObjects() {
        if (layoutObjects == null) {
            load();
        }
        return layoutObjects;
    }

    /**
     * Persists the properties to file.<br>
     * This method will simply call save() in a separate thread.
     */
    public void save() {
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                writeToFile();
            }
        });
    }

    /** 
     * Returns the position of the docked tab with the specified name.
     *
     * @return the tab position
     */
    public int getPosition(String key) {
        UserLayoutObject object = layoutObjects.get(key);
        if (object != null) {
            return object.getPosition();
        }
        return -1;
    }
    
    /**
     * Sets the visibility property of the docked tab with the specified name.
     *
     * @param true | false
     */
    public void setDockedPaneVisible(String key, boolean visible) {
        setDockedPaneVisible(key, visible, true);
    }

    /**
     * Sets the visibility property of the docked tab with the specified name.
     *
     * @param true | false
     */
    public void setDockedPaneVisible(String key, boolean visible, boolean save) {
        UserLayoutObject object = layoutObjects.get(key);
        if (object != null) {
            object.setVisible(visible);
            if (save) {
                save();
            }
        }
    }

    /**
     * Sets the layout definition objects.
     *
     * @param the layout objects
     */
    public void setLayoutObjects(Map<String, UserLayoutObject> layoutObjects) {
        this.layoutObjects = layoutObjects;
    }

    /**
     * Loads the layout definitions from file.
     * If the user file does not exist this will create it for the first time.
     */
    private synchronized void load() {
        
        File file = new File(layoutPropertiesFilePath());
        
        if (layoutObjects == null) {

            layoutObjects = new HashMap<String,UserLayoutObject>();
        }

        if (file.exists()) {
            InputStream in = null;
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                
                SAXParser parser = factory.newSAXParser();
                XMLLayoutHandler handler = new XMLLayoutHandler();
                
                if (Log.isDebugEnabled()) {

                    Log.debug("Loading layout preferences from: " + 
                                                    file.getAbsolutePath());
                }
                
                in = new FileInputStream(file);
                parser.parse(in, handler);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {}
                }
            }
            
            // set the system property values
            for (Iterator i = layoutObjects.keySet().iterator(); i.hasNext();) {
                String key = i.next().toString();
                UserLayoutObject object = layoutObjects.get(key);
                SystemProperties.setBooleanProperty("user", key, object.isVisible());
            }
            
        } else {
           
            // create the file for the first time
            try {
                
                FileUtils.copyResource(
                        DEFAULT_LAYOUT_PREFERENCES_XML, layoutPropertiesFilePath());

            } catch (IOException ioExc) {}

            // reload the new file
            load();

        }
        
    }
    
    /**
     * Saves the layout definitions to file.
     */
    private synchronized void writeToFile() {
        
        if (layoutObjects == null) {
        
            return;
        }

        OutputStream os = null;
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            LayoutParser parser = new LayoutParser();
            
            File file = new File(layoutPropertiesFilePath());

            if (Log.isDebugEnabled()) {
            
                Log.debug("Saving layout preferences to: " + file.getAbsolutePath());
            }
            
            os = new FileOutputStream(file);
            SAXSource source = new SAXSource(parser, new LayoutInputSource());
            StreamResult r = new StreamResult(os);
            transformer.transform(source, r);
        } 
        catch (Exception e) {
            Log.debug("Error saving layout-preferences.xml", e);
            GUIUtilities.displayExceptionErrorDialog(
                    "Error storing user layout change:\n" + 
                    e.getMessage(), e);
        } 
        finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {}
        }

        /*
        XMLEncoder encoder = null;
        BufferedOutputStream outputStream = null;
        
        try {
            String path = SystemUtilities.getUserPropertiesPath() +
                          "layout-preferences.xml";
            outputStream = new BufferedOutputStream(new FileOutputStream(path));
            encoder = new XMLEncoder(outputStream);
            encoder.writeObject(layoutObjects);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (encoder != null) {
                    encoder.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {}
        }
         */
    }

    private String layoutPropertiesFilePath() {
        
        UserSettingsProperties settings = new UserSettingsProperties();

        return settings.getUserSettingsDirectory() + LAYOUT_PREFERENCES_XML;
    }
    
    
    // ----------------------------------
    // XML parsing
    // ----------------------------------

    
    class XMLLayoutHandler extends DefaultHandler {
        
        private UserLayoutObject object = new UserLayoutObject();
        private CharArrayWriter contents = new CharArrayWriter();

        public XMLLayoutHandler() {}
        
        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {
            contents.reset();
        }
        
        public void endElement(String nameSpaceURI, String localName,
                               String qName) {
            
            if (object == null) {
                object = new UserLayoutObject();
            }
            
            if (localName.equals("key")) {
                object.setKey(contents.toString());
            } 
            else if (localName.equals("index")) {
                object.setIndex(Integer.parseInt(contents.toString()));
            } 
            else if (localName.equals("position")) {
                object.setPosition(Integer.parseInt(contents.toString()));
            }
            else if (localName.equals("visible")) {
                object.setVisible(Boolean.valueOf(contents.toString()).booleanValue());
            }
            else if (localName.equals("minimised")) {
                object.setMinimised(Boolean.valueOf(contents.toString()).booleanValue());
            }
            else if (localName.equals("docked-tab")) {
                layoutObjects.put(object.getKey(), object);
                object = null;
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
    } // XMLHandler

    static class LayoutParser implements XMLReader {
        private String nsu = "";
        private Attributes atts = new AttributesImpl();
        
        private static String rootElement = "docked-tab-components";
        private static String DOCKED_TAB = "docked-tab";
        private static String KEY = "key";
        private static String INDEX = "index";
        private static String POSITION = "position";
        private static String VISIBLE = "visible";
        private static String MINIMISED = "minimised";

        private ContentHandler handler;
        
        private static char[] newLine = {'\n'};
        private static String indent_1 = "\n   ";
        private static String indent_2 = "\n      ";
        
        public LayoutParser() {}
        
        public void parse(InputSource input) throws SAXException, IOException {
            if (!(input instanceof LayoutInputSource))
                throw new SAXException("Parser can only accept a LayoutInputSource");
            
            parse((LayoutInputSource)input);
        }
        
        public void parse(LayoutInputSource input) throws IOException, SAXException {
            try {
                if (handler == null) {
                    throw new SAXException("No content handler");
                }
                
                Map<String,UserLayoutObject> objects = input.getLayoutObjects();
                
                handler.startDocument();
                handler.ignorableWhitespace(newLine, 0, 1);
                handler.startElement(nsu, rootElement, rootElement, atts);
                handler.ignorableWhitespace(newLine, 0, 1);
                
                for (Iterator i = objects.keySet().iterator(); i.hasNext();) {
                    handler.ignorableWhitespace(indent_1.toCharArray(), 0, indent_1.length());
                    handler.startElement(nsu, DOCKED_TAB, DOCKED_TAB, atts);
                    
                    String key = i.next().toString();
                    UserLayoutObject object = objects.get(key);
                    
                    writeXML(KEY, object.getKey(), indent_2);
                    writeXML(INDEX, Integer.toString(object.getIndex()), indent_2);
                    writeXML(POSITION, Integer.toString(object.getPosition()), indent_2);
                    writeXML(VISIBLE, Boolean.toString(object.isVisible()), indent_2);
                    writeXML(MINIMISED, Boolean.toString(object.isMinimised()), indent_2);
                    
                    handler.ignorableWhitespace(indent_1.toCharArray(), 0, indent_1.length());
                    handler.endElement(nsu, DOCKED_TAB, DOCKED_TAB);
                    handler.ignorableWhitespace(newLine, 0, 1);
                }
                
                handler.ignorableWhitespace(newLine, 0, 1);
                handler.endElement(nsu, rootElement, rootElement);
                handler.endDocument();
                
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
        
        private void writeXML(String name, String line, String space)
        throws SAXException {
            
            if (line == null) {
                line = Constants.EMPTY;
            }
            
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
    }
    
    class LayoutInputSource extends InputSource {
        public LayoutInputSource() {}
        public Map<String,UserLayoutObject> getLayoutObjects() {
            return layoutObjects;
        }
        
    } // class LayoutInputSource

    static class LayoutSorter implements Comparator<UserLayoutObject> {

        public int compare(UserLayoutObject value1, UserLayoutObject value2) {

            return value1.getIndex() - value2.getIndex();
        }

    } // class LayoutSorter
    
}











