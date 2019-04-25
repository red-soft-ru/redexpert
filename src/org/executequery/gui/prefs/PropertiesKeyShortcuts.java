/*
 * PropertiesKeyShortcuts.java
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

package org.executequery.gui.prefs;


import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.util.SystemResources;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.actions.BaseActionCommand;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * <p>Query Editor syntax highlighting preferences panel.
 *
 * @author Takis Diakoumis
 */
public class PropertiesKeyShortcuts extends AbstractPropertiesBasePanel
        implements Constants {

    private JTable table;
    private Properties userDefinedShortcuts;
    private ShortcutsTableModel tableModel;

    private static String NONE = "<none>";

    public PropertiesKeyShortcuts() {

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void init() throws Exception {

        Vector<ShortcutKey> shortcuts = formatValues(ActionBuilder.getActions());
        tableModel = new ShortcutsTableModel(shortcuts);
        table = new JTable(tableModel);
        table.setFont(AbstractPropertiesBasePanel.panelFont);
        table.addMouseListener(new MouseHandler());
        
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets.left = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(bundledString("KeyboardShortcuts")), gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.top = 10;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        panel.add(new JScrollPane(table), gbc);
        addContent(panel);

        userDefinedShortcuts = SystemResources.getUserActionShortcuts();
        if (userDefinedShortcuts != null) {
            tableModel.loadUserDefined();
        }

    }

    private Vector<ShortcutKey> formatValues(Map<?, ?> actionMap) {

        Set<?> set = actionMap.keySet();
        BaseActionCommand command = null;
        Vector<ShortcutKey> shortcuts = new Vector<ShortcutKey>(actionMap.size());

        for (Iterator<?> i = set.iterator(); i.hasNext(); ) {

            command = (BaseActionCommand) actionMap.get(i.next());

            if (command.isAcceleratorEditable()) {
                shortcuts.add(new ShortcutKey(
                        command.getActionId(),
                        (String) command.getValue(Action.NAME),
                        (KeyStroke) command.getValue(Action.ACCELERATOR_KEY)));
            }

        }

        Collections.sort(shortcuts, new ShortcutKeyComparator());
        return shortcuts;
    }

    public void restoreDefaults() {
        Vector<ShortcutKey> shortcuts = formatValues(
                ActionBuilder.reloadActions(Constants.ACTION_CONF_PATH));
        tableModel.setShortcuts(shortcuts);
    }

    public void save() {

        if (userDefinedShortcuts == null) {
            userDefinedShortcuts = new Properties();
        }

        Vector<ShortcutKey> shortcuts = tableModel.getShortcuts();
        for (int i = 0, k = shortcuts.size(); i < k; i++) {

            ShortcutKey shortcut = shortcuts.get(i);
            if (!MiscUtils.isNull(shortcut.keyStrokeText)) {

                userDefinedShortcuts.setProperty(
                        shortcut.key, shortcut.keyStrokeText);

            } else if (userDefinedShortcuts.containsKey(shortcut.key)) {

                userDefinedShortcuts.setProperty(
                        shortcut.key, ActionBuilder.INVALID_KEYSTROKE);
            }

        }

        if (Log.isDebugEnabled()) {
            Log.debug("Saving user defined action shortcuts");
        }

        SystemResources.setUserActionShortcuts(userDefinedShortcuts);
    }


    class ShortcutsTableModel extends AbstractTableModel {

        private Vector<ShortcutKey> shortcuts;
        private String[] columnHeaders = {bundledString("Command"), bundledString("Shortcut")};

        ShortcutsTableModel(Vector<ShortcutKey> shortcuts) {
            this.shortcuts = shortcuts;
        }

        public void loadUserDefined() {
            if (userDefinedShortcuts == null) {
                return;
            }

            KeyStroke keyStroke = null;
            for (int i = 0, k = shortcuts.size(); i < k; i++) {
                ShortcutKey shortcut = shortcuts.get(i);

                if (userDefinedShortcuts.containsKey(shortcut.key)) {
                    keyStroke = KeyStroke.getKeyStroke(
                            userDefinedShortcuts.getProperty(shortcut.key));
                    shortcut.value = MiscUtils.keyStrokeToString(keyStroke);
                }

            }
        }

        public void setShortcuts(Vector<ShortcutKey> shortcuts) {
            this.shortcuts = shortcuts;
            fireTableDataChanged();
        }

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return shortcuts.size();
        }

        public Object getValueAt(int row, int col) {
            ShortcutKey shortcut = shortcuts.elementAt(row);

            switch (col) {
                case 0:
                    return shortcut.label;
                case 1:
                    return shortcut.value;
                default:
                    return null;
            }
        }

        public ShortcutKey getShortcut(int index) {
            return shortcuts.elementAt(index);
        }

        public void updateShortcut(ShortcutKey shortcut, int row) {
            shortcuts.set(row, shortcut);
            fireTableRowsUpdated(row, row);
        }

        public void setValueAt(Object value, int row, int col) {
            ShortcutKey shortcut = shortcuts.elementAt(row);

            switch (col) {
                case 0:
                    shortcut.label = (String) value;
                    break;
                case 1:
                    shortcut.value = (String) value;
                    break;
            }

            fireTableRowsUpdated(row, row);
        }

        public boolean isCellEditable(int nRow, int nCol) {
            return false;
        }

        public String getColumnName(int col) {
            return columnHeaders[col];
        }

        public Vector<ShortcutKey> getShortcuts() {
            return shortcuts;
        }

    } // ShortcutsTableModel

    class ShortcutDialog extends AbstractBaseDialog
            implements ActionListener {

        private int row;
        private ShortcutInputField shortcutField;
        private ShortcutKey shortcutKey;

        public ShortcutDialog(int row) {

            super((Dialog) GUIUtilities.getInFocusDialogOrWindow(), "Select Shortcut", true);

            this.row = row;

            shortcutKey = tableModel.getShortcut(row);

            JButton okButton = new JButton(Bundles.get("common.ok.button"));
            JButton clearButton = new JButton(Bundles.get("common.clear.button"));
            JButton cancelButton = new JButton(Bundles.get("common.cancel.button"));
            okButton.setActionCommand("OK");
            clearButton.setActionCommand("Clear");
            cancelButton.setActionCommand("Cancel");

            okButton.addActionListener(this);
            clearButton.addActionListener(this);
            cancelButton.addActionListener(this);

            shortcutField = new ShortcutInputField();
            shortcutField.setPreferredSize(new Dimension(300, 20));

            Container c = this.getContentPane();
            c.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy++;
            gbc.insets = new Insets(10, 10, 5, 10);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            c.add(new JLabel("Enter a new shortcut for \"" +
                    shortcutKey.label + "\":"), gbc);
            gbc.gridy++;
            gbc.insets.top = 0;
            c.add(shortcutField, gbc);
            gbc.gridy++;
            c.add(new JLabel("Current assignment: " +
                    (MiscUtils.isNull(shortcutKey.value) ? NONE :
                            shortcutKey.value)), gbc);
            gbc.gridy++;

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(clearButton);
            buttonPanel.add(cancelButton);

            gbc.weighty = 1.0;
            gbc.insets.bottom = 10;
            gbc.fill = GridBagConstraints.BOTH;
            c.add(buttonPanel, gbc);

            pack();
            this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
            shortcutField.requestFocusInWindow();
            this.setVisible(true);

        }

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if (command.equals("Clear")) {
                shortcutField.reset();
                shortcutField.setText(Constants.EMPTY);
                shortcutField.requestFocusInWindow();
            } else if (command.equals("OK")) {
                shortcutKey.value = shortcutField.getText();
                shortcutKey.keyStrokeText = shortcutField.getKeyStrokeText();
                tableModel.updateShortcut(shortcutKey, row);
                dispose();
            } else if (command.equals("Cancel")) {
                dispose();
            }

        }

    }

    class ShortcutInputField extends JTextField {

        private int keyCode;
        private StringBuilder keyStrokeText;

        public void reset() {
            if (keyStrokeText != null) {
                keyStrokeText.delete(0, keyStrokeText.length());
            }
        }

        public boolean getFocusTraversalKeysEnabled() {
            return false;
        }

        protected void processKeyEvent(KeyEvent evt) {
            evt.consume();

            if (evt.getID() == KeyEvent.KEY_PRESSED) {
                int _keyCode = evt.getKeyCode();

                if (_keyCode == keyCode) {
                    return;
                }

                keyCode = _keyCode;
                StringBuilder keyString = new StringBuilder(getText());
                String modifiers = KeyEvent.getKeyText(evt.getKeyCode());
                boolean nullModifiers = MiscUtils.isNull(modifiers);

                if (keyStrokeText == null) {
                    keyStrokeText = new StringBuilder();
                }

                if (getDocument().getLength() != 0) {
                    keyString.append('+');
                    keyStrokeText.append(' ');
                }

                if (!nullModifiers) {
                    keyString.append(modifiers);
                }

                if (((keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z)
                        || (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9))
                        && nullModifiers) {
                    keyStrokeText.append(modifiers);
                } else {
                    keyStrokeText.append(getKeyText(keyCode));
                }

                setText(keyString.toString());
            }

        }

        public String getKeyStrokeText() {
            if (keyStrokeText != null) {
                return keyStrokeText.toString();
            }
            return "";
        }

        private String getKeyText(int keyCode) {

            if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9 ||
                    keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
                return String.valueOf((char) keyCode);
            }

            switch (keyCode) {
                case KeyEvent.VK_COMMA:
                    return "COMMA";
                case KeyEvent.VK_PERIOD:
                    return "PERIOD";
                case KeyEvent.VK_SLASH:
                    return "SLASH";
                case KeyEvent.VK_SEMICOLON:
                    return "SEMICOLON";
                case KeyEvent.VK_EQUALS:
                    return "EQUALS";
                case KeyEvent.VK_OPEN_BRACKET:
                    return "OPEN_BRACKET";
                case KeyEvent.VK_BACK_SLASH:
                    return "BACK_SLASH";
                case KeyEvent.VK_CLOSE_BRACKET:
                    return "CLOSE_BRACKET";

                case KeyEvent.VK_ENTER:
                    return "ENTER";
                case KeyEvent.VK_BACK_SPACE:
                    return "BACK_SPACE";
                case KeyEvent.VK_TAB:
                    return "TAB";
                case KeyEvent.VK_CANCEL:
                    return "CANCEL";
                case KeyEvent.VK_CLEAR:
                    return "CLEAR";
                case KeyEvent.VK_SHIFT:
                    return "shift";
                case KeyEvent.VK_CONTROL:
                    return "control";
                case KeyEvent.VK_ALT:
                    return "alt";
                case KeyEvent.VK_PAUSE:
                    return "PAUSE";
                case KeyEvent.VK_CAPS_LOCK:
                    return "CAPS_LOCK";
                case KeyEvent.VK_ESCAPE:
                    return "ESCAPE";
                case KeyEvent.VK_SPACE:
                    return "SPACE";
                case KeyEvent.VK_PAGE_UP:
                    return "PAGE_UP";
                case KeyEvent.VK_PAGE_DOWN:
                    return "PAGE_DOWN";
                case KeyEvent.VK_END:
                    return "END";
                case KeyEvent.VK_HOME:
                    return "HOME";
                case KeyEvent.VK_LEFT:
                    return "LEFT";
                case KeyEvent.VK_UP:
                    return "UP";
                case KeyEvent.VK_RIGHT:
                    return "RIGHT";
                case KeyEvent.VK_DOWN:
                    return "DOWN";

                // numpad numeric keys handled below
                case KeyEvent.VK_MULTIPLY:
                    return "MULTIPLY";
                case KeyEvent.VK_ADD:
                    return "ADD";
                case KeyEvent.VK_SEPARATOR:
                    return "SEPARATOR";
                case KeyEvent.VK_SUBTRACT:
                    return "SUBTRACT";
                case KeyEvent.VK_DECIMAL:
                    return "DECIMAL";
                case KeyEvent.VK_DIVIDE:
                    return "DIVIDE";
                case KeyEvent.VK_DELETE:
                    return "DELETE";
                case KeyEvent.VK_NUM_LOCK:
                    return "NUM_LOCK";
                case KeyEvent.VK_SCROLL_LOCK:
                    return "SCROLL_LOCK";

                case KeyEvent.VK_F1:
                    return "F1";
                case KeyEvent.VK_F2:
                    return "F2";
                case KeyEvent.VK_F3:
                    return "F3";
                case KeyEvent.VK_F4:
                    return "F4";
                case KeyEvent.VK_F5:
                    return "F5";
                case KeyEvent.VK_F6:
                    return "F6";
                case KeyEvent.VK_F7:
                    return "F7";
                case KeyEvent.VK_F8:
                    return "F8";
                case KeyEvent.VK_F9:
                    return "F9";
                case KeyEvent.VK_F10:
                    return "F10";
                case KeyEvent.VK_F11:
                    return "F11";
                case KeyEvent.VK_F12:
                    return "F12";
                case KeyEvent.VK_F13:
                    return "F13";
                case KeyEvent.VK_F14:
                    return "F14";
                case KeyEvent.VK_F15:
                    return "F15";
                case KeyEvent.VK_F16:
                    return "F16";
                case KeyEvent.VK_F17:
                    return "F17";
                case KeyEvent.VK_F18:
                    return "F18";
                case KeyEvent.VK_F19:
                    return "F19";
                case KeyEvent.VK_F20:
                    return "F20";
                case KeyEvent.VK_F21:
                    return "F21";
                case KeyEvent.VK_F22:
                    return "F22";
                case KeyEvent.VK_F23:
                    return "F23";
                case KeyEvent.VK_F24:
                    return "F24";

                case KeyEvent.VK_PRINTSCREEN:
                    return "PRINTSCREEN";
                case KeyEvent.VK_INSERT:
                    return "INSERT";
                case KeyEvent.VK_HELP:
                    return "HELP";
                case KeyEvent.VK_META:
                    return "META";
                case KeyEvent.VK_BACK_QUOTE:
                    return "BACK_QUOTE";
                case KeyEvent.VK_QUOTE:
                    return "QUOTE";

                case KeyEvent.VK_KP_UP:
                    return "KP_UP";
                case KeyEvent.VK_KP_DOWN:
                    return "KP_DOWN";
                case KeyEvent.VK_KP_LEFT:
                    return "KP_LEFT";
                case KeyEvent.VK_KP_RIGHT:
                    return "KP_RIGHT";

                case KeyEvent.VK_DEAD_GRAVE:
                    return "DEAD_GRAVE";
                case KeyEvent.VK_DEAD_ACUTE:
                    return "DEAD_ACUTE";
                case KeyEvent.VK_DEAD_CIRCUMFLEX:
                    return "DEAD_CIRCUMFLEX";
                case KeyEvent.VK_DEAD_TILDE:
                    return "DEAD_TILDE";
                case KeyEvent.VK_DEAD_MACRON:
                    return "DEAD_MACRON";
                case KeyEvent.VK_DEAD_BREVE:
                    return "DEAD_BREVE";
                case KeyEvent.VK_DEAD_ABOVEDOT:
                    return "DEAD_ABOVEDOT";
                case KeyEvent.VK_DEAD_DIAERESIS:
                    return "DEAD_DIAERESIS";
                case KeyEvent.VK_DEAD_ABOVERING:
                    return "DEAD_ABOVERING";
                case KeyEvent.VK_DEAD_DOUBLEACUTE:
                    return "DEAD_DOUBLEACUTE";
                case KeyEvent.VK_DEAD_CARON:
                    return "DEAD_CARON";
                case KeyEvent.VK_DEAD_CEDILLA:
                    return "DEAD_CEDILLA";
                case KeyEvent.VK_DEAD_OGONEK:
                    return "DEAD_OGONEK";
                case KeyEvent.VK_DEAD_IOTA:
                    return "DEAD_IOTA";
                case KeyEvent.VK_DEAD_VOICED_SOUND:
                    return "DEAD_VOICED_SOUND";
                case KeyEvent.VK_DEAD_SEMIVOICED_SOUND:
                    return "DEAD_SEMIVOICED_SOUND";

                case KeyEvent.VK_AMPERSAND:
                    return "AMPERSAND";
                case KeyEvent.VK_ASTERISK:
                    return "ASTERISK";
                case KeyEvent.VK_QUOTEDBL:
                    return "QUOTEDBL";
                case KeyEvent.VK_LESS:
                    return "LESS";
                case KeyEvent.VK_GREATER:
                    return "GREATER";
                case KeyEvent.VK_BRACELEFT:
                    return "BRACELEFT";
                case KeyEvent.VK_BRACERIGHT:
                    return "BRACERIGHT";
                case KeyEvent.VK_AT:
                    return "AT";
                case KeyEvent.VK_COLON:
                    return "COLON";
                case KeyEvent.VK_CIRCUMFLEX:
                    return "CIRCUMFLEX";
                case KeyEvent.VK_DOLLAR:
                    return "DOLLAR";
                case KeyEvent.VK_EURO_SIGN:
                    return "EURO_SIGN";
                case KeyEvent.VK_EXCLAMATION_MARK:
                    return "EXCLAMATION_MARK";
                case KeyEvent.VK_INVERTED_EXCLAMATION_MARK:
                    return "INVERTED_EXCLAMATION_MARK";
                case KeyEvent.VK_LEFT_PARENTHESIS:
                    return "LEFT_PARENTHESIS";
                case KeyEvent.VK_NUMBER_SIGN:
                    return "NUMBER_SIGN";
                case KeyEvent.VK_MINUS:
                    return "MINUS";
                case KeyEvent.VK_PLUS:
                    return "PLUS";
                case KeyEvent.VK_RIGHT_PARENTHESIS:
                    return "RIGHT_PARENTHESIS";
                case KeyEvent.VK_UNDERSCORE:
                    return "UNDERSCORE";

                case KeyEvent.VK_FINAL:
                    return "FINAL";
                case KeyEvent.VK_CONVERT:
                    return "CONVERT";
                case KeyEvent.VK_NONCONVERT:
                    return "NONCONVERT";
                case KeyEvent.VK_ACCEPT:
                    return "ACCEPT";
                case KeyEvent.VK_MODECHANGE:
                    return "MODECHANGE";
                case KeyEvent.VK_KANA:
                    return "KANA";
                case KeyEvent.VK_KANJI:
                    return "KANJI";
                case KeyEvent.VK_ALPHANUMERIC:
                    return "ALPHANUMERIC";
                case KeyEvent.VK_KATAKANA:
                    return "KATAKANA";
                case KeyEvent.VK_HIRAGANA:
                    return "HIRAGANA";
                case KeyEvent.VK_FULL_WIDTH:
                    return "FULL_WIDTH";
                case KeyEvent.VK_HALF_WIDTH:
                    return "HALF_WIDTH";
                case KeyEvent.VK_ROMAN_CHARACTERS:
                    return "ROMAN_CHARACTERS";
                case KeyEvent.VK_ALL_CANDIDATES:
                    return "ALL_CANDIDATES";
                case KeyEvent.VK_PREVIOUS_CANDIDATE:
                    return "PREVIOUS_CANDIDATE";
                case KeyEvent.VK_CODE_INPUT:
                    return "CODE_INPUT";
                case KeyEvent.VK_JAPANESE_KATAKANA:
                    return "JAPANESE_KATAKANA";
                case KeyEvent.VK_JAPANESE_HIRAGANA:
                    return "JAPANESE_HIRAGANA";
                case KeyEvent.VK_JAPANESE_ROMAN:
                    return "JAPANESE_ROMAN";
                case KeyEvent.VK_KANA_LOCK:
                    return "KANA_LOCK";
                case KeyEvent.VK_INPUT_METHOD_ON_OFF:
                    return "INPUT_METHOD_ON_OFF";

                case KeyEvent.VK_AGAIN:
                    return "AGAIN";
                case KeyEvent.VK_UNDO:
                    return "UNDO";
                case KeyEvent.VK_COPY:
                    return "COPY";
                case KeyEvent.VK_PASTE:
                    return "PASTE";
                case KeyEvent.VK_CUT:
                    return "CUT";
                case KeyEvent.VK_FIND:
                    return "FIND";
                case KeyEvent.VK_PROPS:
                    return "PROPS";
                case KeyEvent.VK_STOP:
                    return "STOP";

                case KeyEvent.VK_COMPOSE:
                    return "COMPOSE";
                case KeyEvent.VK_ALT_GRAPH:
                    return "ALT_GRAPH";
            }

            if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9) {
                char c = (char) (keyCode - KeyEvent.VK_NUMPAD0 + '0');
                return "NUMPAD" + c;
            }

            return null;


        }

    }

    static class ShortcutKey {
        String key;
        String value;
        String label;
        String keyStrokeText;
        KeyStroke keyStroke;

        ShortcutKey(String key, String label, KeyStroke keyStroke) {
            this.key = key;
            this.label = label;
            this.keyStroke = keyStroke;
            value = MiscUtils.keyStrokeToString(keyStroke);
        }

        public String toString() {
            return value;
        }
    } // ShortcutKey

    static class ShortcutKeyComparator implements Comparator<ShortcutKey> {

        public int compare(ShortcutKey key1, ShortcutKey key2) {

            return key1.label.compareTo(key2.label);
        }

    } // ShortcutKeyComparator

    class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            int row = table.rowAtPoint(evt.getPoint());
            if (row == -1) {
                return;
            }
            int col = table.columnAtPoint(evt.getPoint());
            if (col == 1) {
                new ShortcutDialog(row);
            }
        }
    } // MouseHandler

}






