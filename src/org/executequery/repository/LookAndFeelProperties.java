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

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.log.Log;
import org.executequery.plaf.LookAndFeelDefinition;
import org.executequery.util.UserSettingsProperties;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Vector;

/**
 * Look and feel property definition controller.
 *
 * @author Takis Diakoumis
 */
public class LookAndFeelProperties {

    private static Vector<LookAndFeelDefinition> looks;

    private static final String NAME = "name";
    private static final String PATH = "path";
    private static final String INSTALLED = "installed";
    private static final String THEME_PACK = "themepack";
    private static final String CLASS_NAME = "classname";
    private static final String MAIN_NODE = "lookandfeel";
    private static final String SKIN_LOOK = "skinlookfeel";
    private static final String ROOT = "customlookandfeels";

    public static LookAndFeelDefinition[] getLookAndFeelArray() {

        if (looks == null)
            loadLookAndFeels();

        int lafsVectorSize = looks.size();
        if (lafsVectorSize < 1)
            return null;

        LookAndFeelDefinition[] lookAndFeelDefinitions = new LookAndFeelDefinition[lafsVectorSize];
        for (int i = 0; i < lafsVectorSize; i++)
            lookAndFeelDefinitions[i] = looks.elementAt(i);

        return lookAndFeelDefinitions;
    }

    public static LookAndFeelDefinition getInstalledCustomLook() {
        return looks.stream()
                .filter(LookAndFeelDefinition::isInstalled)
                .findFirst().orElse(null);
    }

    public static synchronized void saveLookAndFeels(LookAndFeelDefinition[] lfda) {

        File fileToSave = new File(filePath());
        try (OutputStream os = Files.newOutputStream(fileToSave.toPath())) {

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            LookAndFeelParser lookAndFeelParser = new LookAndFeelParser();
            SAXSource source = new SAXSource(lookAndFeelParser, new LookAndFeelInputSource(lfda));
            StreamResult streamResult = new StreamResult(os);
            transformer.transform(source, streamResult);

            looks.clear();
            Collections.addAll(looks, lfda);

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    public static synchronized void loadLookAndFeels() {

        File file = new File(filePath());
        if (!file.exists()) {
            GUIUtilities.displayErrorMessage(
                    "Look & Feel definition XML file not found.\n" +
                            "EConstants.EMPTYre the file lookandfeel.xml is in ~/.executequery/conf"
            );
        }

        try (InputStream in = Files.newInputStream(file.toPath())) {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            XMLLookAndFeelHandler handler = new XMLLookAndFeelHandler();
            parser.parse(in, handler);

            looks = handler.getLooksVector();

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayErrorMessage("Error opening look and feel definitions.");
        }
    }

    private static String filePath() {
        return new UserSettingsProperties().getUserSettingsDirectory() + "lookandfeel.xml";
    }

    private static class XMLLookAndFeelHandler extends DefaultHandler {

        private LookAndFeelDefinition lookAndFeel;
        private final CharArrayWriter contents;
        private final Vector<LookAndFeelDefinition> lookAndFeelDefinitions;

        public XMLLookAndFeelHandler() {
            lookAndFeel = new LookAndFeelDefinition();
            contents = new CharArrayWriter();
            lookAndFeelDefinitions = new Vector<>();
        }

        public Vector<LookAndFeelDefinition> getLooksVector() {
            return lookAndFeelDefinitions;
        }

        @Override
        public void startElement(String nameSpaceURI, String localName, String qName, Attributes attrs) {
            contents.reset();

            if (localName.equals(MAIN_NODE))
                lookAndFeel.setInstalled(Boolean.parseBoolean(attrs.getValue(INSTALLED)));
        }

        @Override
        public void endElement(String nameSpaceURI, String localName, String qName) {

            if (lookAndFeel == null)
                lookAndFeel = new LookAndFeelDefinition();

            switch (localName) {
                case NAME:
                    lookAndFeel.setName(contents.toString());
                    break;

                case PATH:
                    lookAndFeel.setLibraryPath(contents.toString());
                    break;

                case CLASS_NAME:
                    lookAndFeel.setClassName(contents.toString());
                    break;

                case SKIN_LOOK:
                    lookAndFeel.setIsSkinLookAndFeel(Integer.parseInt(contents.toString()));
                    break;

                case THEME_PACK:
                    lookAndFeel.setThemePack(contents.toString());
                    lookAndFeelDefinitions.add(lookAndFeel);
                    lookAndFeel = null;
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

    } // XMLLookAndFeelHandler class

    private static class LookAndFeelParser implements XMLReader {

        private static final String CDATA = "CDATA";
        private static final char[] NEW_LINE = {'\n'};
        private static final String INDENT_1 = "\n   ";
        private static final String INDENT_2 = "\n      ";

        private ContentHandler handler;
        private final AttributesImpl attributes;

        public LookAndFeelParser() {
            attributes = new AttributesImpl();
        }

        @Override
        public void parse(InputSource input) throws SAXException, IOException {
            if (!(input instanceof LookAndFeelInputSource))
                throw new SAXException("Parser can only accept a LookAndFeelInputSource");

            parse((LookAndFeelInputSource) input);
        }

        public void parse(LookAndFeelInputSource input) throws IOException, SAXException {
            try {

                if (handler == null)
                    throw new SAXException("No content handler");

                handler.startDocument();
                handler.startElement(Constants.EMPTY, ROOT, ROOT, attributes);
                handler.ignorableWhitespace(NEW_LINE, 0, 1);

                String ZERO = "0";
                String ONE = "1";

                for (LookAndFeelDefinition lookAndFeelDefinition : input.getLookAndFeelArray()) {
                    handler.ignorableWhitespace(INDENT_1.toCharArray(), 0, INDENT_1.length());
                    attributes.addAttribute(Constants.EMPTY, INSTALLED, INSTALLED, CDATA, Boolean.toString(lookAndFeelDefinition.isInstalled()));

                    handler.startElement(Constants.EMPTY, MAIN_NODE, MAIN_NODE, attributes);
                    attributes.removeAttribute(attributes.getIndex(INSTALLED));

                    writeXML(NAME, lookAndFeelDefinition.getName(), INDENT_2);
                    writeXML(PATH, lookAndFeelDefinition.getLibraryPath(), INDENT_2);
                    writeXML(CLASS_NAME, lookAndFeelDefinition.getClassName(), INDENT_2);
                    writeXML(SKIN_LOOK, lookAndFeelDefinition.isSkinLookAndFeel() ? ONE : ZERO, INDENT_2);
                    writeXML(THEME_PACK, lookAndFeelDefinition.getThemePack(), INDENT_2);

                    handler.ignorableWhitespace(INDENT_1.toCharArray(), 0, INDENT_1.length());
                    handler.endElement(Constants.EMPTY, MAIN_NODE, MAIN_NODE);
                    handler.ignorableWhitespace(NEW_LINE, 0, 1);
                }

                handler.ignorableWhitespace(NEW_LINE, 0, 1);
                handler.endElement(Constants.EMPTY, ROOT, ROOT);
                handler.endDocument();

            } catch (Exception e) {
                Log.error(e.getMessage(), e);
            }
        }

        @SuppressWarnings("SameParameterValue")
        private void writeXML(String name, String line, String space) throws SAXException {

            if (line == null)
                line = Constants.EMPTY;

            handler.ignorableWhitespace(space.toCharArray(), 0, space.length());
            handler.startElement(Constants.EMPTY, name, name, attributes);
            handler.characters(line.toCharArray(), 0, line.length());
            handler.endElement(Constants.EMPTY, name, name);
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

    } // LookAndFeelParser class

    private static class LookAndFeelInputSource extends InputSource {
        private final LookAndFeelDefinition[] lookAndFeelDefinitions;

        public LookAndFeelInputSource(LookAndFeelDefinition[] lookAndFeelDefinitions) {
            this.lookAndFeelDefinitions = lookAndFeelDefinitions;
        }

        public LookAndFeelDefinition[] getLookAndFeelArray() {
            return lookAndFeelDefinitions;
        }

    } // LookAndFeelInputSource class

}
