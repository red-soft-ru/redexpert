/*
 * ToolBarProperties.java
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

package org.underworldlabs.swing.toolbar;

import org.executequery.log.Log;
import org.underworldlabs.swing.GUIUtils;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class ToolBarProperties {

    /**
     * The user defined tools
     */
    private static Vector tools;

    /**
     * The default tools
     */
    private static Vector defaultTools;

    /**
     * the tools XML conf file path
     */
    private static String toolsConfPath;

    /**
     * the default tools XML conf resource file path
     */
    private static String defaultToolsConfPath;

    // -----------------------------------
    // --- XML elements and attributes ---
    // -----------------------------------
    private static final String EQ_TOOLBARS = "system-toolbars";
    private static final String TOOLBAR = "toolbar";
    private static final String ROW = "row";
    private static final String POSITION = "position";
    private static final String LOC_X = "loc-x";
    private static final String RESIZE_OFFSET_X = "resize-offset-x";
    private static final String MINIMUM_WIDTH = "minimum-width";
    private static final String PREFERRED_WIDTH = "preferred-width";
    private static final String CURRENT_WIDTH = "current-width";
    private static final String BUTTONS = "buttons";
    private static final String CONSTRAINTS = "constraints";
    private static final String BUTTON = "button";
    private static final String NAME = "name";
    private static final String ACTION_ID = "action-id";
    private static final String ID = "id";
    private static final String VISIBLE = "visible";
    private static final String ORDER = "order";
    // -----------------------------------

    private static final String EMPTY = "";

    public static void init(String _toolsConfPath, String _defaultToolsConfPath) {
        toolsConfPath = _toolsConfPath;
        defaultToolsConfPath = _defaultToolsConfPath;

        // TODO: do we allow null conf files with defaults only???

        if (toolsConfPath != null) {
            loadTools();
        }

    }

    private static void checkInit() {
        if (toolsConfPath == null) {
            throw new RuntimeException(
                    "Tool configuration XML file is NULL or failed to load. " +
                            "Ensure the init() method is run prior to retrieving " +
                            "any tool conf information");
        }
    }

    private static void checkDefaultInit() {
        if (defaultToolsConfPath == null) {
            throw new RuntimeException(
                    "Default Tool configuration XML file resource is NULL " +
                            "Ensure the init(...) method is called prior to retrieving " +
                            "any tool conf information");
        }
    }

    public static Vector getToolbarButtonsVector() {
        checkInit();
        if (tools == null || tools.size() == 0) {
            loadTools();
        }

        return tools;
    }

    public static ToolBarWrapper[] getToolbarButtonsArray() {
        checkInit();
        if (tools == null || tools.size() == 0) {
            loadTools();
        }

        return (ToolBarWrapper[]) tools.toArray(new ToolBarWrapper[]{});
    }

    public static Vector getDefaultToolbarButtonsVector() {
        checkDefaultInit();
        if (defaultTools == null || defaultTools.size() == 0) {
            loadDefaults(false);
        }

        return defaultTools;
    }

    public static ToolBarWrapper[] getDefaultToolbarButtonsArray() {
        checkDefaultInit();
        if (defaultTools == null || defaultTools.size() == 0) {
            loadDefaults(false);
        }

        return (ToolBarWrapper[]) defaultTools.toArray(new ToolBarWrapper[]{});
    }

    public static void setToolBarConstraints(String name, ToolBarConstraints tbc) {
        ToolBarWrapper toolBar = getToolBar(name);
        toolBar.setConstraints(tbc);
    }

    public static void removeToolBar(String name) {
        tools.remove(getToolBar(name));
    }

    public static void resetToolBar(String name, ToolBarWrapper toolBar) {
        tools.remove(getToolBar(name));
        tools.add(toolBar);
    }

    public static void setToolBarVisible(String name, boolean visible) {
        ToolBarWrapper toolBar = getToolBar(name);
        toolBar.setVisible(visible);
    }

    public static boolean isToolBarVisible(String name) {
        ToolBarWrapper toolBar = getToolBar(name);
        return toolBar.isVisible();
    }

    public static ToolBarWrapper getDefaultToolBar(String name) {
        checkDefaultInit();
        if (defaultTools == null || defaultTools.isEmpty()) {
            loadDefaults(false);
        }

        ToolBarWrapper toolBar = null;
        for (int i = 0, k = defaultTools.size(); i < k; i++) {
            toolBar = (ToolBarWrapper) defaultTools.elementAt(i);

            if (name.compareTo(toolBar.getName()) == 0) {
                break;
            }

        }

        return toolBar;
    }

    public static int getNextToolbarRow() {
        int row;
        int currentMaxRow = -1;
        ToolBarWrapper[] toolBars = getToolbarButtonsArray();

        for (int i = 0; i < toolBars.length; i++) {
            row = toolBars[i].getConstraints().getRow();

            if (row > currentMaxRow) {
                currentMaxRow = row;
            }

        }
        if (currentMaxRow > 0)
            currentMaxRow = 0;

        return currentMaxRow + 1;
    }

    public static ToolBarWrapper getToolBar(String name) {
        if (tools == null || tools.size() == 0) {
            loadTools();
        }

        for (int i = 0, k = tools.size(); i < k; i++) {
            ToolBarWrapper toolBar = (ToolBarWrapper) tools.elementAt(i);
            if (name.compareTo(toolBar.getName()) == 0) {
                return toolBar;
            }
        }

        return null;
    }

    public static int saveTools() {
        OutputStream os = null;
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            ToolsParser cp = new ToolsParser();

            File file = new File(toolsConfPath);

            os = new FileOutputStream(file);
            SAXSource source = new SAXSource(cp, new ToolbarButtonsSource());
            StreamResult r = new StreamResult(os);
            transformer.transform(source, r);
            return 1;
        } catch (Exception e) {
            Log.error("Error method saveTools in class ToolBarProperties:", e);
            return 0;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void reloadTools(boolean loadDefaults) {
        if (loadDefaults) {
            defaultTools = null;
            loadDefaults(false);
        } else {
            loadTools();
        }
    }

    private static synchronized void loadDefaults(boolean setDefaults) {

        if (defaultTools != null && defaultTools.size() > 0) {
            return;
        }

        InputStream input = null;
        ClassLoader cl = ToolBarProperties.class.getClassLoader();

        if (cl != null) {
            input = cl.getResourceAsStream(defaultToolsConfPath);
        } else {
            input = ClassLoader.getSystemResourceAsStream(defaultToolsConfPath);
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);

            SAXParser parser = factory.newSAXParser();
            XMLToolHandler handler = new XMLToolHandler();
            parser.parse(input, handler);
            defaultTools = handler.getToolsVector();

            if (setDefaults) {
                int size = defaultTools.size();
                tools = new Vector(size);
                ToolBarWrapper toolBar = null;
                for (int i = 0; i < size; i++) {
                    toolBar = (ToolBarWrapper) defaultTools.elementAt(i);
                    tools.add(toolBar.clone());
                }

            }
        } catch (Exception e) {
            Log.error("Error method loadDefaults in class ToolBarProperties:", e);
            GUIUtils.displayErrorMessage(
                    null, "Error opening default tools definitions.");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }

    }

    // checks for new tools added from the defaults
    private static void compareTools() {

        boolean hasButton = false;
        boolean rebuild = false;

        ToolBarWrapper[] defaultsArray = getDefaultToolbarButtonsArray();
        ToolBarWrapper[] toolsArray = getToolbarButtonsArray();

        ToolBarWrapper currentToolBar = null;

        for (int i = 0; i < defaultsArray.length; i++) {
            String name = defaultsArray[i].getName();

            ToolBarButton[] buttons = defaultsArray[i].getButtonsArray();
            if (buttons == null) {
                continue;
            }

            for (int j = 0; j < toolsArray.length; j++) {

                if (toolsArray[j].getName().compareTo(name) == 0) {
                    currentToolBar = toolsArray[j];
                    break;
                }

            }

            ToolBarButton[] _buttons = currentToolBar.getButtonsArray();
            if (_buttons == null) {
                continue;
            }

            for (int k = 0; k < buttons.length; k++) {
                int id = buttons[k].getId();

                for (int m = 0; m < _buttons.length; m++) {
                    if (_buttons[m].getId() == id) {
                        hasButton = true;
                        break;
                    }
                    hasButton = false;
                }

                if (!hasButton) {
                    rebuild = true;
                    ToolBarButton newButton = (ToolBarButton) buttons[k].clone();
                    newButton.setVisible(false);
                    newButton.setOrder(1000);
                    currentToolBar.addButton(newButton);
                }

            }

        }

        // regenerate the saved file if required
        if (rebuild) {
            tools = new Vector(toolsArray.length);
            for (int i = 0; i < toolsArray.length; i++) {
                tools.add(toolsArray[i]);
            }
            saveTools();
        }

    }

    private static synchronized void loadTools() {

        File file = new File(toolsConfPath);

        if (file.exists()) {
            InputStream in = null;
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);

                SAXParser parser = factory.newSAXParser();
                XMLToolHandler handler = new XMLToolHandler();

                in = new FileInputStream(file);
                parser.parse(in, handler);
                tools = handler.getToolsVector();

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        compareTools();
                    }
                });

            } catch (Exception e) {
                Log.error("Error method loadTools in class ToolBarProperties:", e);
                GUIUtils.displayErrorMessage(
                        null,
                        "Error opening tools definitions.\nResorting to system defaults.");
                loadDefaults(true);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }

        } else {
            GUIUtils.displayErrorMessage(
                    null,
                    "Tool buttons definition XML file not found.\n" +
                            "Ensure the file toolbars.xml is in the conf " +
                            "directory of this distribution.");
        }
    }

    static class XMLToolHandler extends DefaultHandler {

        private ToolBarWrapper toolBar;
        private ToolBarButton tb;
        private ToolBarConstraints tbc;
        private CharArrayWriter contents = new CharArrayWriter();
        private Vector toolBars = new Vector();

        public XMLToolHandler() {
        }

        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {
            contents.reset();
            if (localName.equals(TOOLBAR)) {
                toolBar = new ToolBarWrapper(attrs.getValue(NAME),
                        Boolean.valueOf(attrs.getValue(VISIBLE)).booleanValue());
            } else if (localName.equals(CONSTRAINTS)) {
                tbc = new ToolBarConstraints();
            } else if (localName.equals(BUTTON)) {
                tb = new ToolBarButton(Integer.parseInt(attrs.getValue(ID)),
                        attrs.getValue(ACTION_ID));
            }
        }

        public void endElement(String nameSpaceURI, String localName, String qName) {

            if (localName.equals(ROW))
                tbc.setRow(Integer.parseInt(contents.toString()));

            else if (localName.equals(POSITION))
                tbc.setPosition(Integer.parseInt(contents.toString()));

            else if (localName.equals(LOC_X))
                tbc.setLocX(Integer.parseInt(contents.toString()));

            else if (localName.equals(RESIZE_OFFSET_X))
                tbc.setResizeOffsetX(Integer.parseInt(contents.toString()));

            else if (localName.equals(MINIMUM_WIDTH))
                tbc.setMinimumWidth(Integer.parseInt(contents.toString()));

            else if (localName.equals(PREFERRED_WIDTH))
                tbc.setPreferredWidth(Integer.parseInt(contents.toString()));

            else if (localName.equals(CURRENT_WIDTH))
                tbc.setCurrentWidth(Integer.parseInt(contents.toString()));

            else if (localName.equals(VISIBLE))
                tb.setVisible(Boolean.valueOf(contents.toString()).booleanValue());

            else if (localName.equals(ORDER)) {
                tb.setOrder(Integer.parseInt(contents.toString()));
                toolBar.addButton(tb);
                tb = null;
            } else if (localName.equals(TOOLBAR)) {
                toolBar.setConstraints(tbc);
                toolBars.add(toolBar);
                tbc = null;
            }

        }

        public Vector getToolsVector() {
            return toolBars;
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

    static class ToolsParser implements XMLReader {
        private String nsu = EMPTY;
        private AttributesImpl atts = new AttributesImpl();

        private String attType1 = "CDATA";
        private ContentHandler handler;

        private static char[] newLine = {'\n'};
        private static String indent_1 = "\n   ";
        private static String indent_2 = "\n      ";
        private static String indent_3 = "\n        ";
        private static String indent_4 = "\n          ";

        public ToolsParser() {
        }

        public void parse(InputSource input) throws SAXException, IOException {
            if (!(input instanceof ToolbarButtonsSource))
                throw new SAXException("Parser can only accept a ToolbarButtonsSource");

            parse((ToolbarButtonsSource) input);
        }

        public void parse(ToolbarButtonsSource input) throws IOException, SAXException {
            try {
                if (handler == null) {
                    throw new SAXException("No content handler");
                }

                ToolBarWrapper[] tb = input.getTools();

                handler.startDocument();
                handler.startElement(nsu, EQ_TOOLBARS, EQ_TOOLBARS, atts);
                handler.ignorableWhitespace(newLine, 0, 1);

                boolean isSeparator = false;
                ToolBarConstraints tbc = null;
                ToolBarButton button = null;
                Vector buttons = null;

                for (int i = 0; i < tb.length; i++) {
                    handler.ignorableWhitespace(indent_1.toCharArray(), 0, indent_1.length());

                    atts.addAttribute(EMPTY, NAME, NAME, attType1, tb[i].getName());
                    atts.addAttribute(EMPTY, VISIBLE, VISIBLE, attType1,
                            Boolean.toString(tb[i].isVisible()));

                    handler.startElement(nsu, TOOLBAR, TOOLBAR, atts);
                    atts.removeAttribute(atts.getIndex(NAME));
                    atts.removeAttribute(atts.getIndex(VISIBLE));

                    handler.ignorableWhitespace(newLine, 0, 1);

                    handler.ignorableWhitespace(indent_2.toCharArray(), 0, indent_2.length());
                    handler.startElement(nsu, CONSTRAINTS, CONSTRAINTS, atts);
                    handler.ignorableWhitespace(newLine, 0, 1);

                    tbc = tb[i].getConstraints();
                    writeXML(ROW, Integer.toString(tbc.getRow()), indent_3);
                    writeXML(POSITION, Integer.toString(tbc.getPosition()), indent_3);
                    writeXML(LOC_X, Integer.toString(tbc.getLocX()), indent_3);
                    writeXML(RESIZE_OFFSET_X, Integer.toString(tbc.getResizeOffsetX()), indent_3);
                    writeXML(MINIMUM_WIDTH, Integer.toString(tbc.getMinimumWidth()), indent_3);
                    writeXML(PREFERRED_WIDTH, Integer.toString(tbc.getPreferredWidth()), indent_3);
                    writeXML(CURRENT_WIDTH, Integer.toString(tbc.getCurrentWidth()), indent_3);

                    handler.ignorableWhitespace(newLine, 0, 1);
                    handler.ignorableWhitespace(indent_2.toCharArray(), 0, indent_2.length());
                    handler.endElement(nsu, CONSTRAINTS, CONSTRAINTS);
                    handler.ignorableWhitespace(newLine, 0, 1);
                    tbc = null;

                    if (tb[i].hasButtons()) {
                        buttons = tb[i].getButtonsVector();

                        handler.ignorableWhitespace(indent_2.toCharArray(), 0, indent_2.length());
                        handler.startElement(nsu, BUTTONS, BUTTONS, atts);
                        handler.ignorableWhitespace(newLine, 0, 1);

                        for (int j = 0, k = buttons.size(); j < k; j++) {
                            button = (ToolBarButton) buttons.elementAt(j);
                            handler.ignorableWhitespace(indent_3.toCharArray(), 0,
                                    indent_3.length());
                            atts.addAttribute(EMPTY, ID, ID, attType1,
                                    Integer.toString(button.getId()));

                            isSeparator = button.isSeparator();

                            if (!isSeparator)
                                atts.addAttribute(EMPTY, ACTION_ID, ACTION_ID, attType1,
                                        button.getActionId());

                            handler.startElement(nsu, BUTTON, BUTTON, atts);
                            atts.removeAttribute(atts.getIndex(ID));

                            if (!isSeparator)
                                atts.removeAttribute(atts.getIndex(ACTION_ID));

                            writeXML(VISIBLE, Boolean.toString(button.isVisible()), indent_4);
                            writeXML(ORDER, Integer.toString(button.getOrder()), indent_4);

                            handler.ignorableWhitespace(indent_3.toCharArray(), 0, indent_3.length());
                            handler.endElement(nsu, BUTTON, BUTTON);
                            handler.ignorableWhitespace(newLine, 0, 1);
                        }

                        handler.ignorableWhitespace(indent_2.toCharArray(), 0, indent_2.length());
                        handler.endElement(nsu, BUTTONS, BUTTONS);
                        handler.ignorableWhitespace(newLine, 0, 1);

                    }

                    handler.ignorableWhitespace(indent_1.toCharArray(), 0, indent_1.length());
                    handler.endElement(nsu, TOOLBAR, TOOLBAR);
                    handler.ignorableWhitespace(newLine, 0, 1);

                }

                handler.ignorableWhitespace(newLine, 0, 1);
                handler.endElement(nsu, EQ_TOOLBARS, EQ_TOOLBARS);
                handler.endDocument();

            } catch (Exception e) {
                Log.error("Error method parse in class ToolBarProperties:", e);
            }

        }

        private void writeXML(String name, String line, String space)
                throws SAXException {
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

        public void setErrorHandler(ErrorHandler handler) {
        }

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

        public void setEntityResolver(EntityResolver resolver) {
        }

        public void setDTDHandler(DTDHandler handler) {
        }

        public Object getProperty(String name) {
            return null;
        }

        public void setProperty(String name, java.lang.Object value) {
        }

        public void setFeature(String name, boolean value) {
        }

        public boolean getFeature(String name) {
            return false;
        }
    } // class DriverParser

    static class ToolbarButtonsSource extends InputSource {

        public ToolBarWrapper[] getTools() {
            int size = tools.size();
            ToolBarWrapper[] toolBars = new ToolBarWrapper[size];
            for (int i = 0; i < size; i++) {
                toolBars[i] = (ToolBarWrapper) tools.elementAt(i);
            }
            return toolBars;
        }

    } // class ToolbarButtonsSource


}















