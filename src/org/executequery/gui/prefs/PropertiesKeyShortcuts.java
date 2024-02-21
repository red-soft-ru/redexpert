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
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.util.SystemResources;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.ShortcutTextField;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.actions.BaseActionCommand;
import org.underworldlabs.swing.layouts.GridBagHelper;
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

    private class ShortcutDialog extends AbstractBaseDialog {

        private final int row;
        private final ShortcutKey shortcutKey;

        private ShortcutTextField shortcutField;
        private JButton okButton;
        private JButton clearButton;
        private JButton cancelButton;

        public ShortcutDialog(int row) {
            super((Dialog) GUIUtilities.getInFocusDialogOrWindow(), Bundles.get("ShortcutDialog.title"), true);

            this.row = row;
            this.shortcutKey = tableModel.getShortcut(row);

            init();
            arrange();
        }

        private void init() {

            okButton = WidgetFactory.createButton("okButton", Bundles.get("common.ok.button"));
            okButton.addActionListener(e -> ok());

            clearButton = WidgetFactory.createButton("clearButton", Bundles.get("common.clear.button"));
            clearButton.addActionListener(e -> clear());

            cancelButton = WidgetFactory.createButton("cancelButton", Bundles.get("common.cancel.button"));
            cancelButton.addActionListener(e -> dispose());

            shortcutField = new ShortcutTextField();
            shortcutField.setName("shortcutField");
            shortcutField.setPreferredSize(new Dimension(300, okButton.getPreferredSize().height));
            shortcutField.requestFocusInWindow();
        }

        private void arrange() {

            GridBagHelper gbh;

            String enterShortcutLabelText = String.format(
                    bundleString("enterShortcutLabel"),
                    shortcutKey.label
            );
            String currentAssignmentLabelText = String.format(
                    bundleString("currentAssignmentLabel"),
                    !MiscUtils.isNull(shortcutKey.value) ? shortcutKey.value : ""
            );

            // --- button panel ---

            JPanel buttonPanel = new JPanel(new GridBagLayout());

            gbh = new GridBagHelper().fillNone().rightGap(5);
            buttonPanel.add(okButton, gbh.get());
            buttonPanel.add(clearButton, gbh.nextCol().get());
            buttonPanel.add(cancelButton, gbh.nextCol().rightGap(0).get());

            // --- main panel ---

            JPanel mainPanel = new JPanel(new GridBagLayout());

            gbh = new GridBagHelper().anchorNorthWest().fillHorizontally().setInsets(5, 5, 5, 0);
            mainPanel.add(new JLabel(enterShortcutLabelText), gbh.spanX().get());
            mainPanel.add(shortcutField, gbh.nextRow().get());
            mainPanel.add(new JLabel(currentAssignmentLabelText), gbh.nextRow().get());
            mainPanel.add(buttonPanel, gbh.nextRow().anchorCenter().fillNone().bottomGap(5).get());

            // --- base ---

            add(mainPanel);

            pack();
            setResizable(false);
            setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
            setVisible(true);
        }

        private void ok() {
            shortcutKey.value = shortcutField.getText();
            shortcutKey.keyStrokeText = shortcutField.getKeyStrokeText();
            tableModel.updateShortcut(shortcutKey, row);
            dispose();
        }

        private void clear() {
            shortcutField.reset();
            shortcutField.setText(Constants.EMPTY);
            shortcutField.requestFocusInWindow();
        }

        private String bundleString(String key) {
            return Bundles.get(ShortcutDialog.class, key);
        }

    } // ShortcutDialog class

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






