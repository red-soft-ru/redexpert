/*
 * ActionBuilder.java
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

package org.underworldlabs.swing.actions;

import org.apache.commons.lang.StringUtils;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.plaf.SVGImage;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.util.MiscUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// Utilitiy class to build all actions to be
// associated with buttons, menu items and key strokes.

/**
 * @author Takis Diakoumis
 */
public final class ActionBuilder {

    public static final String INVALID_KEYSTROKE = "<undefined>";

    private static final String ACTION = "action";
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String MNEMONIC = "mnemonic";
    private static final String SMALL_ICON = "small-icon";
    //    private static final String LARGE_ICON = "large-icon";
    private static final String ACCEL_KEY = "accel-key";
    private static final String DESCRIPTION = "description";
    private static final String EXECUTE_CLASS = "execute-class";
    private static final String ACCEL_EDITABLE = "accel-editable";

    private static Map<String, Action> actionsMap;

    /**
     * Builds the action map for the specified action and input maps
     * using the actions as specified by the XML conf file at path.
     *
     * @param actionMap - the action map to bind to
     * @param inputMap  - the input map to bind to
     * @param path      - the path to the action XML conf file
     */
    public static void build(ActionMap actionMap, InputMap inputMap, String path) {
        actionsMap = loadActions(path);
        build(actionMap, inputMap);
    }

    public static void setActionMaps(JComponent component, Properties shortcuts) {
        ActionMap actionMap = component.getActionMap();
        InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        build(actionMap, inputMap);

        if (shortcuts == null) {
            return;
        }

        buildUserKeymap(shortcuts, inputMap);
    }

    public static void build(ActionMap actionMap, InputMap inputMap) {

        if (actionMap == null) {

            return;
        }

        for (Map.Entry<String, Action> entry : actionsMap.entrySet()) {

            BaseActionCommand command = (BaseActionCommand) entry.getValue();
            String actionId = command.getActionId();
            actionMap.put(actionId, command);

            if (command.hasAccelerator()) {

                inputMap.put((KeyStroke) command.getValue(Action.ACCELERATOR_KEY), actionId);
            }

        }

    }

    private static void buildUserKeymap(Properties shortcuts, InputMap inputMap) {

        String actionId = null;
        BaseActionCommand command = null;

        for (Enumeration<?> i = shortcuts.keys(); i.hasMoreElements(); ) {

            actionId = (String) i.nextElement();

            KeyStroke keyStroke = null;
            String keyStrokeString = shortcuts.getProperty(actionId);
            if (!INVALID_KEYSTROKE.equals(keyStrokeString)) {

                keyStroke = KeyStroke.getKeyStroke(keyStrokeString);

            } else {

                keyStroke = KeyStroke.getKeyStroke(KeyEvent.CHAR_UNDEFINED);
            }

            command = (BaseActionCommand) actionsMap.get(actionId);
            if (command != null) {

                command.putValue(Action.ACCELERATOR_KEY, keyStroke);
            }

            inputMap.put(keyStroke, actionId);

        }

    }

    /**
     * Reloads the actions from the action conf file at the specified path.
     */
    public static Map<String, Action> reloadActions(String path) {
        return loadActions(path);
    }

    /**
     * Updates the action shortcut keys based on the properties specified.
     *
     * @param inputMap  - the input map to bind to
     * @param shortcuts - the new shortcut keys
     */
    public static void updateUserDefinedShortcuts(InputMap inputMap, Properties shortcuts) {
        if (shortcuts == null) {

            return;
        }
        buildUserKeymap(shortcuts, inputMap);
    }

    /**
     * Returns a map containing key/value pairs of all the currently
     * bound actions.
     */
    public static Map<String, Action> getActions() {
        return actionsMap;
    }

    /**
     * Returns the action with the specified key name.
     */
    public static Action get(Object key) {

        return (Action) actionsMap.get(key);
    }

    private static Map<String, Action> loadActions(String path) {

        InputStream input = null;
        ClassLoader cl = ActionBuilder.class.getClassLoader();

        if (cl != null) {

            input = cl.getResourceAsStream(path);

        } else {

            input = ClassLoader.getSystemResourceAsStream(path);
        }

        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);

            SAXParser parser = factory.newSAXParser();
            ActionHandler handler = new ActionHandler();
            parser.parse(input, handler);

            return handler.getActions();

        } catch (Exception e) {

            e.printStackTrace();
            throw new InternalError();

        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
            }
        }

    }

    static class ActionHandler extends DefaultHandler {

        private Map<String, Action> map;
        private CharArrayWriter contents;
        private BaseActionCommand actionCommand;

        public ActionHandler() {
            contents = new CharArrayWriter();
            map = new HashMap<String, Action>();
        }

        private ImageIcon loadIcon(String path) {

            URL url = ActionHandler.class.getResource(path);
            if (url != null) {
                if (url.getPath().endsWith(".svg")) {
                    try {
                        BufferedImage image = SVGImage.fromSvg(url, UserProperties.getInstance().getIntProperty("icons.size"),UserProperties.getInstance().getIntProperty("icons.size"));
                        return new ImageIcon(image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else
                    return new ImageIcon(url);

            }

            return null;
        }

        public Map<String, Action> getActions() {
            return map;
        }

        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {
            String value = null;
            contents.reset();

            if (localName.equals(ACTION)) {

                actionCommand = new BaseActionCommand();

                String id = attrs.getValue(ID);
                actionCommand.setActionId(id);

                String name = attrs.getValue(NAME);
                actionCommand.putValue(Action.NAME, nameOrBundleValue(id, name));

                value = attrs.getValue(MNEMONIC);
                if (!MiscUtils.isNull(value)) {

                    actionCommand.putValue(Action.MNEMONIC_KEY, Integer.valueOf(value.charAt(0)));
                }

                //value = attrs.getValue(LARGE_ICON);
                value = attrs.getValue(SMALL_ICON);
                if (!MiscUtils.isNull(value)) {

                    actionCommand.putValue(Action.SMALL_ICON, loadIcon(value));
                }

                value = attrs.getValue(ACCEL_EDITABLE);
                if (!MiscUtils.isNull(value)) {

                    actionCommand.setAcceleratorEditable(Boolean.valueOf(value).booleanValue());
                }

                value = attrs.getValue(ACCEL_KEY);
                if (!MiscUtils.isNull(value)) {

                    if (UIUtils.isMac() && value.contains("control")) {

                        value = value.replaceAll("control", "meta");
                        if (Log.isDebugEnabled()) {

                            Log.debug("Modifying accelerator to MAC meta key for action - " + attrs.getValue(NAME));
                        }
                            
                        /*
                        if (keyStroke.getModifiers() == (KeyEvent.CTRL_MASK|KeyEvent.CTRL_DOWN_MASK)) {
                            if (Log.isDebugEnabled()) {
                                Log.debug("Modifying accelerator to MAC meta key for action - " + attrs.getValue(NAME));
                            }

                            keyStroke = KeyStroke.getKeyStroke(keyStroke.getKeyCode(), KeyEvent.META_DOWN_MASK);                            
                        }
                        */

                    }

                    KeyStroke keyStroke = KeyStroke.getKeyStroke(value);
                    actionCommand.putValue(Action.ACCELERATOR_KEY, keyStroke);
                    
                    /*
                    if (value.contains("control")) {
                        
                        Log.debug(attrs.getValue(NAME) + " - " + keyStroke);
                        
                    }
                    */
                }

                String description = attrs.getValue(DESCRIPTION);
                actionCommand.putValue(Action.SHORT_DESCRIPTION, descriptionOrBundleValue(id, description));

                actionCommand.setCommand(attrs.getValue(EXECUTE_CLASS));
            }

        }

        public void endElement(String nameSpaceURI, String localName, String qName) {

            if (localName.equals(ACTION)) {

                map.put(actionCommand.getActionId(), actionCommand);
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

        private String nameOrBundleValue(String id, String name) {

            String value = Bundles.get("action." + id);
            if (StringUtils.isNotBlank(value)) {

                return value;
            }
            return name;
        }

        private String descriptionOrBundleValue(String id, String description) {

            String value = Bundles.get("action." + id + ".description");
            if (StringUtils.isNotBlank(value)) {

                return value;
            }
            return description;
        }


    } // ActionHandler

}




