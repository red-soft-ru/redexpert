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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings("unchecked")
public class ToolBarProperties {

    private static Vector<ToolBarWrapper> userTools;
    private static Vector<ToolBarWrapper> defaultTools;

    private static String toolsConfPath;
    private static String defaultToolsConfPath;

    // --- XML elements and attributes ---

    private static final String ID = "id";
    private static final String EMPTY = "";
    private static final String ROW = "row";
    private static final String NAME = "name";
    private static final String ORDER = "order";
    private static final String LOC_X = "loc-x";
    private static final String BUTTON = "button";
    private static final String VISIBLE = "visible";
    private static final String BUTTONS = "buttons";
    private static final String TOOLBAR = "toolbar";
    private static final String POSITION = "position";
    private static final String ACTION_ID = "action-id";
    private static final String CONSTRAINTS = "constraints";
    private static final String MINIMUM_WIDTH = "minimum-width";
    private static final String EQ_TOOLBARS = "system-toolbars";
    private static final String CURRENT_WIDTH = "current-width";
    private static final String PREFERRED_WIDTH = "preferred-width";
    private static final String RESIZE_OFFSET_X = "resize-offset-x";

    // ---

    public static void init(String toolsConfPath, String defaultToolsConfPath) {
        ToolBarProperties.toolsConfPath = toolsConfPath;
        ToolBarProperties.defaultToolsConfPath = defaultToolsConfPath;

        if (toolsConfPath != null)
            loadTools();
    }

    private static void checkInit() {
        if (toolsConfPath == null) {
            throw new RuntimeException(
                    "Tool configuration XML file is NULL or failed to load. " +
                            "Ensure the init() method is run prior to retrieving " +
                            "any tool conf information"
            );
        }
    }

    private static void checkDefaultInit() {
        if (defaultToolsConfPath == null) {
            throw new RuntimeException(
                    "Default Tool configuration XML file resource is NULL " +
                            "Ensure the init(...) method is called prior to retrieving " +
                            "any tool conf information"
            );
        }
    }

    public static ToolBarWrapper[] getToolbarButtonsArray() {
        checkInit();

        if (userTools == null || userTools.isEmpty())
            loadTools();

        return userTools.toArray(new ToolBarWrapper[]{});
    }

    public static ToolBarWrapper[] getDefaultToolbarButtonsArray() {
        checkDefaultInit();

        if (defaultTools == null || defaultTools.isEmpty())
            loadDefaults(false);

        return defaultTools.toArray(new ToolBarWrapper[]{});
    }

    public static void setToolBarConstraints(String name, ToolBarConstraints tbc) {
        ToolBarWrapper toolBar = getToolBar(name);
        if (toolBar != null)
            toolBar.setConstraints(tbc);
    }

    public static void resetToolBar(String name, ToolBarWrapper toolBar) {
        userTools.remove(getToolBar(name));
        userTools.add(toolBar);
    }

    public static void setToolBarVisible(String name, boolean visible) {
        ToolBarWrapper toolBar = getToolBar(name);
        if (toolBar != null)
            toolBar.setVisible(visible);
    }

    public static boolean isToolBarVisible(String name) {
        ToolBarWrapper toolBar = getToolBar(name);
        return toolBar != null
                && toolBar.isVisible()
                && Arrays.stream(toolBar.getButtonsArray()).anyMatch(ToolBarButton::isVisible);
    }

    public static ToolBarWrapper getDefaultToolBar(String name) {
        checkDefaultInit();

        if (defaultTools == null || defaultTools.isEmpty())
            loadDefaults(false);

        for (int i = 0, k = defaultTools.size(); i < k; i++) {
            ToolBarWrapper toolBar = (ToolBarWrapper) defaultTools.elementAt(i);
            if (name.compareTo(toolBar.getName()) == 0)
                break;
        }

        return null;
    }

    public static int getNextToolbarRow() {
        int currentMaxRow = -1;

        for (ToolBarWrapper toolBar : getToolbarButtonsArray()) {
            int row = toolBar.getConstraints().getRow();
            if (row > currentMaxRow)
                currentMaxRow = row;
        }

        if (currentMaxRow > 0)
            currentMaxRow = 0;

        return currentMaxRow + 1;
    }

    public static ToolBarWrapper getToolBar(String name) {

        if (userTools == null || userTools.isEmpty())
            loadTools();

        for (int i = 0, k = userTools.size(); i < k; i++) {
            ToolBarWrapper toolBar = userTools.elementAt(i);
            if (name.compareTo(toolBar.getName()) == 0)
                return toolBar;
        }

        return null;
    }

    public static void saveTools() {

        try (OutputStream os = Files.newOutputStream(new File(toolsConfPath).toPath())) {

            SAXSource source = new SAXSource(new ToolsParser(), new ToolbarButtonsSource());
            StreamResult streamResult = new StreamResult(os);

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            transformer.transform(source, streamResult);

        } catch (Exception e) {
            Log.error("Error method saveTools in class ToolBarProperties:", e);
        }
    }

    private static synchronized void loadDefaults(boolean setDefaults) {

        if (defaultTools != null && !defaultTools.isEmpty())
            return;

        ClassLoader classLoader = ToolBarProperties.class.getClassLoader();
        try (
                InputStream input = classLoader != null ?
                        classLoader.getResourceAsStream(defaultToolsConfPath) :
                        ClassLoader.getSystemResourceAsStream(defaultToolsConfPath)
        ) {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);

            SAXParser parser = factory.newSAXParser();
            XMLToolHandler handler = new XMLToolHandler();
            parser.parse(input, handler);
            defaultTools = handler.getToolsVector();

            if (setDefaults) {
                int size = defaultTools.size();
                userTools = new Vector<>(size);
                for (int i = 0; i < size; i++)
                    userTools.add((ToolBarWrapper) defaultTools.elementAt(i).clone());
            }

        } catch (Exception e) {
            Log.error("Error method loadDefaults in class ToolBarProperties:", e);
            GUIUtils.displayErrorMessage(null, "Error opening default tools definitions.");
        }
    }

    // checks for new tools added from the defaults
    private static void compareTools() {

        boolean hasButton = false;
        boolean rebuild = false;

        ToolBarWrapper[] defaultsArray = getDefaultToolbarButtonsArray();
        ToolBarWrapper[] toolsArray = getToolbarButtonsArray();

        ToolBarWrapper currentToolBar = null;

        for (ToolBarWrapper toolBarWrapper : defaultsArray) {
            String name = toolBarWrapper.getName();

            ToolBarButton[] buttons = toolBarWrapper.getButtonsArray();
            if (buttons == null)
                continue;

            for (ToolBarWrapper barWrapper : toolsArray) {
                if (barWrapper.getName().compareTo(name) == 0) {
                    currentToolBar = barWrapper;
                    break;
                }
            }

            if (currentToolBar == null)
                continue;

            ToolBarButton[] _buttons = currentToolBar.getButtonsArray();
            if (_buttons == null)
                continue;

            for (ToolBarButton button : buttons) {
                for (ToolBarButton toolBarButton : _buttons) {
                    if (toolBarButton.getId() == button.getId()) {
                        hasButton = true;
                        break;
                    }
                    hasButton = false;
                }

                if (!hasButton) {
                    rebuild = true;

                    ToolBarButton newButton = (ToolBarButton) button.clone();
                    newButton.setVisible(false);
                    newButton.setOrder(1000);

                    currentToolBar.addButton(newButton);
                }
            }
        }

        // regenerate the saved file if required
        if (rebuild) {
            userTools = new Vector<>(toolsArray.length);
            Collections.addAll(userTools, toolsArray);
            saveTools();
        }
    }

    private static synchronized void loadTools() {

        File file = new File(toolsConfPath);
        if (!file.exists()) {
            GUIUtils.displayErrorMessage(null,
                    "Tool buttons definition XML file not found.\n" +
                            "Ensure the file toolbars.xml is in the conf directory of this distribution."
            );
            return;
        }


        try (InputStream inputStream = Files.newInputStream(file.toPath())) {

            XMLToolHandler handler = new XMLToolHandler();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);

            SAXParser parser = factory.newSAXParser();
            parser.parse(inputStream, handler);
            userTools = handler.getToolsVector();

            SwingUtilities.invokeLater(ToolBarProperties::compareTools);

        } catch (Exception e) {
            Log.error("Error method loadTools in class ToolBarProperties:", e);
            GUIUtils.displayErrorMessage(null, "Error opening tools definitions.\nResorting to system defaults.");
            loadDefaults(true);
        }
    }

    private static class XMLToolHandler extends DefaultHandler {

        private ToolBarButton button;
        private ToolBarWrapper toolBar;
        private ToolBarConstraints tbc;

        private final CharArrayWriter contents;
        private final Vector<ToolBarWrapper> toolBars;

        public XMLToolHandler() {
            toolBars = new Vector<>();
            contents = new CharArrayWriter();
        }

        @Override
        public void startElement(String nameSpaceURI, String localName, String qName, Attributes attrs) {
            contents.reset();

            if (localName.equals(TOOLBAR)) {
                toolBar = new ToolBarWrapper(
                        attrs.getValue(NAME),
                        Boolean.parseBoolean(attrs.getValue(VISIBLE))
                );

            } else if (localName.equals(BUTTON)) {
                button = new ToolBarButton(
                        Integer.parseInt(attrs.getValue(ID)),
                        attrs.getValue(ACTION_ID)
                );

            } else if (localName.equals(CONSTRAINTS))
                tbc = new ToolBarConstraints();
        }

        @Override
        public void endElement(String nameSpaceURI, String localName, String qName) {

            switch (localName) {
                case ROW:
                    tbc.setRow(Integer.parseInt(contents.toString()));
                    break;

                case POSITION:
                    tbc.setPosition(Integer.parseInt(contents.toString()));
                    break;

                case LOC_X:
                    tbc.setLocX(Integer.parseInt(contents.toString()));
                    break;

                case RESIZE_OFFSET_X:
                    tbc.setResizeOffsetX(Integer.parseInt(contents.toString()));
                    break;

                case MINIMUM_WIDTH:
                    tbc.setMinimumWidth(Integer.parseInt(contents.toString()));
                    break;

                case PREFERRED_WIDTH:
                    tbc.setPreferredWidth(Integer.parseInt(contents.toString()));
                    break;

                case CURRENT_WIDTH:
                    tbc.setCurrentWidth(Integer.parseInt(contents.toString()));
                    break;

                case VISIBLE:
                    button.setVisible(Boolean.parseBoolean(contents.toString()));
                    break;

                case ORDER:
                    button.setOrder(Integer.parseInt(contents.toString()));
                    toolBar.addButton(button);
                    button = null;
                    break;

                case TOOLBAR:
                    toolBar.setConstraints(tbc);
                    toolBars.add(toolBar);
                    tbc = null;
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

        public Vector<ToolBarWrapper> getToolsVector() {
            return toolBars;
        }

    } // XMLHandler class

    private static class ToolsParser implements XMLReader {

        private static final char[] NEW_LINE = {'\n'};
        private static final String INDENT_1 = "\n   ";
        private static final String INDENT_2 = "\n      ";
        private static final String INDENT_3 = "\n        ";
        private static final String INDENT_4 = "\n          ";

        private final AttributesImpl attributes;
        private ContentHandler handler;

        public ToolsParser() {
            attributes = new AttributesImpl();
        }

        @Override
        public void parse(InputSource input) throws SAXException, IOException {
            if (!(input instanceof ToolbarButtonsSource))
                throw new SAXException("Parser can only accept a ToolbarButtonsSource");

            parse((ToolbarButtonsSource) input);
        }

        public void parse(ToolbarButtonsSource input) throws IOException, SAXException {
            try {
                if (handler == null)
                    throw new SAXException("No content handler");

                handler.startDocument();
                handler.startElement(EMPTY, EQ_TOOLBARS, EQ_TOOLBARS, attributes);
                handler.ignorableWhitespace(NEW_LINE, 0, 1);

                String cdata = "CDATA";
                for (ToolBarWrapper toolBarWrapper : input.getTools()) {
                    handler.ignorableWhitespace(INDENT_1.toCharArray(), 0, INDENT_1.length());

                    attributes.addAttribute(EMPTY, NAME, NAME, cdata, toolBarWrapper.getName());
                    attributes.addAttribute(EMPTY, VISIBLE, VISIBLE, cdata, Boolean.toString(toolBarWrapper.isVisible()));

                    handler.startElement(EMPTY, TOOLBAR, TOOLBAR, attributes);

                    attributes.removeAttribute(attributes.getIndex(NAME));
                    attributes.removeAttribute(attributes.getIndex(VISIBLE));

                    handler.ignorableWhitespace(NEW_LINE, 0, 1);
                    handler.ignorableWhitespace(INDENT_2.toCharArray(), 0, INDENT_2.length());
                    handler.startElement(EMPTY, CONSTRAINTS, CONSTRAINTS, attributes);
                    handler.ignorableWhitespace(NEW_LINE, 0, 1);

                    ToolBarConstraints tbc = toolBarWrapper.getConstraints();
                    writeXML(ROW, Integer.toString(tbc.getRow()), INDENT_3);
                    writeXML(POSITION, Integer.toString(tbc.getPosition()), INDENT_3);
                    writeXML(LOC_X, Integer.toString(tbc.getLocX()), INDENT_3);
                    writeXML(RESIZE_OFFSET_X, Integer.toString(tbc.getResizeOffsetX()), INDENT_3);
                    writeXML(MINIMUM_WIDTH, Integer.toString(tbc.getMinimumWidth()), INDENT_3);
                    writeXML(PREFERRED_WIDTH, Integer.toString(tbc.getPreferredWidth()), INDENT_3);
                    writeXML(CURRENT_WIDTH, Integer.toString(tbc.getCurrentWidth()), INDENT_3);

                    handler.ignorableWhitespace(NEW_LINE, 0, 1);
                    handler.ignorableWhitespace(INDENT_2.toCharArray(), 0, INDENT_2.length());
                    handler.endElement(EMPTY, CONSTRAINTS, CONSTRAINTS);
                    handler.ignorableWhitespace(NEW_LINE, 0, 1);

                    if (toolBarWrapper.hasButtons()) {
                        Vector<ToolBarButton> buttonsVector = toolBarWrapper.getButtonsVector();

                        handler.ignorableWhitespace(INDENT_2.toCharArray(), 0, INDENT_2.length());
                        handler.startElement(EMPTY, BUTTONS, BUTTONS, attributes);
                        handler.ignorableWhitespace(NEW_LINE, 0, 1);

                        for (int j = 0, k = buttonsVector.size(); j < k; j++) {
                            ToolBarButton button = buttonsVector.elementAt(j);
                            boolean isSeparator = button.isSeparator();

                            handler.ignorableWhitespace(INDENT_3.toCharArray(), 0, INDENT_3.length());

                            attributes.addAttribute(EMPTY, ID, ID, cdata, Integer.toString(button.getId()));
                            if (!isSeparator)
                                attributes.addAttribute(EMPTY, ACTION_ID, ACTION_ID, cdata, button.getActionId());

                            handler.startElement(EMPTY, BUTTON, BUTTON, attributes);

                            attributes.removeAttribute(attributes.getIndex(ID));
                            if (!isSeparator)
                                attributes.removeAttribute(attributes.getIndex(ACTION_ID));

                            writeXML(VISIBLE, Boolean.toString(button.isVisible()), INDENT_4);
                            writeXML(ORDER, Integer.toString(button.getOrder()), INDENT_4);

                            handler.ignorableWhitespace(INDENT_3.toCharArray(), 0, INDENT_3.length());
                            handler.endElement(EMPTY, BUTTON, BUTTON);
                            handler.ignorableWhitespace(NEW_LINE, 0, 1);
                        }

                        handler.ignorableWhitespace(INDENT_2.toCharArray(), 0, INDENT_2.length());
                        handler.endElement(EMPTY, BUTTONS, BUTTONS);
                        handler.ignorableWhitespace(NEW_LINE, 0, 1);
                    }

                    handler.ignorableWhitespace(INDENT_1.toCharArray(), 0, INDENT_1.length());
                    handler.endElement(EMPTY, TOOLBAR, TOOLBAR);
                    handler.ignorableWhitespace(NEW_LINE, 0, 1);
                }

                handler.ignorableWhitespace(NEW_LINE, 0, 1);
                handler.endElement(EMPTY, EQ_TOOLBARS, EQ_TOOLBARS);
                handler.endDocument();

            } catch (Exception e) {
                Log.error("Error method parse in class ToolBarProperties:", e);
            }
        }

        private void writeXML(String name, String line, String space) throws SAXException {
            handler.ignorableWhitespace(space.toCharArray(), 0, space.length());
            handler.startElement(EMPTY, name, name, attributes);
            handler.characters(line.toCharArray(), 0, line.length());
            handler.endElement(EMPTY, name, name);
        }

        @Override
        public void setContentHandler(ContentHandler handler) {
            this.handler = handler;
        }

        @Override
        public ContentHandler getContentHandler() {
            return this.handler;
        }

        @Override
        public void setErrorHandler(ErrorHandler handler) {
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }

        @Override
        public void parse(String systemId) throws IOException, SAXException {
        }

        @Override
        public DTDHandler getDTDHandler() {
            return null;
        }

        @Override
        public EntityResolver getEntityResolver() {
            return null;
        }

        @Override
        public void setEntityResolver(EntityResolver resolver) {
        }

        @Override
        public void setDTDHandler(DTDHandler handler) {
        }

        @Override
        public Object getProperty(String name) {
            return null;
        }

        @Override
        public void setProperty(String name, java.lang.Object value) {
        }

        @Override
        public void setFeature(String name, boolean value) {
        }

        @Override
        public boolean getFeature(String name) {
            return false;
        }

    } // DriverParser class

    private static class ToolbarButtonsSource extends InputSource {

        public ToolBarWrapper[] getTools() {
            int size = userTools.size();

            ToolBarWrapper[] toolBars = new ToolBarWrapper[size];
            for (int i = 0; i < size; i++)
                toolBars[i] = userTools.elementAt(i);

            return toolBars;
        }

    } // ToolbarButtonsSource class

}
