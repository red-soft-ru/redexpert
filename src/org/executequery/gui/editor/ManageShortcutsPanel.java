/*
 * ManageShortcutsPanel.java
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

package org.executequery.gui.editor;

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.event.DefaultQueryShortcutEvent;
import org.executequery.event.QueryShortcutEvent;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.DefaultActionButtonsPanel;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.localization.Bundles;
import org.executequery.repository.EditorSQLShortcut;
import org.executequery.repository.EditorSQLShortcuts;
import org.executequery.repository.RepositoryException;
import org.underworldlabs.swing.DefaultMutableListModel;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.MutableValueJList;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ManageShortcutsPanel extends DefaultActionButtonsPanel
        implements ListSelectionListener {

    public static final String TITLE = Bundles.get("ManageShortcutsPanel.title");
    public static final String FRAME_ICON = "Shortcut16.svg";

    private static final String SAVE_COMMAND_NAME = "save";
    private static final String CANCEL_COMMAND_NAME = "cancel";

    private JList list;

    private SQLTextArea textPane;

    private int lastSelectedIndex = -1;

    private final ActionContainer parent;

    public ManageShortcutsPanel(ActionContainer parent) {

        this.parent = parent;

        init();
    }

    private void init() {

        createTextPane();
        createList();

        JSplitPane splitPane = createSplitPane();
        splitPane.setLeftComponent(new JScrollPane(list));
        splitPane.setRightComponent(new JScrollPane(textPane));

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        panel.add(labelForKey("shortcuts"), gbc);
        gbc.gridy++;
        gbc.insets.bottom = 5;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(splitPane, gbc);
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets.left = 5;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(createMoveButtonsPanel(), gbc);

        addActionButton(createSaveButton());
        addActionButton(createCancelButton());

        addContentPanel(panel);

        setPreferredSize(new Dimension(650, 400));
    }

    private JButton createCancelButton() {

        JButton button = new DefaultPanelButton(bundleString("cancelButton"));

        button.setActionCommand(CANCEL_COMMAND_NAME);
        button.addActionListener(this);

        return button;
    }

    private JButton createSaveButton() {

        JButton button = new DefaultPanelButton(bundleString("okButton"));

        button.setActionCommand(SAVE_COMMAND_NAME);
        button.addActionListener(this);

        return button;
    }

    private void createTextPane() {

        textPane = new SQLTextArea();
        textPane.setPreferredSize(new Dimension(300, 350));
    }

    private void createList() {

        list = new MutableValueJList(createModel());

        list.addListSelectionListener(this);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (modelFromList().size() >= 0) {

            list.setSelectedIndex(0);
        }

    }

    public void deleteShortcut() {

        int index = selectedIndex();

        if (index != -1) {

            try {

                list.removeListSelectionListener(this);

                DefaultListModel model = modelFromList();
                model.remove(index);

                lastSelectedIndex = -1;

                int size = model.getSize();
                if (size > 0) {

                    if (index > size - 1) {

                        list.setSelectedIndex(size - 1);

                    } else {

                        list.setSelectedIndex(index);
                    }

                    shortcutSelected();

                } else {

                    textPane.setText("");
                }

            } finally {

                list.addListSelectionListener(this);
            }

        }

    }

    public void addShortcut() {

        EditorSQLShortcut shortcut = new EditorSQLShortcut();
        shortcut.setShortcut(bundleString("newShortcutName"));
        shortcut.setQuery(Constants.EMPTY);

        DefaultListModel model = modelFromList();

        model.addElement(shortcut);
        int index = model.indexOf(shortcut);

        list.setSelectedIndex(index);
        list.scrollRectToVisible(list.getCellBounds(index, index));

        listEditingAction().actionPerformed(actionEventForEdit());
    }

    private Action listEditingAction() {
        return list.getActionMap().get("startEditing");
    }

    private ActionEvent actionEventForEdit() {
        return new ActionEvent(list, ActionEvent.ACTION_FIRST, null);
    }

    public void cancel() {
        parent.finished();
    }

    public void save() {

        try {

            storeQueryForShortcut();
            List<EditorSQLShortcut> shortcuts = shortcutsFromList();
            if (!shortcutsValid(shortcuts)) {

                GUIUtilities.displayErrorMessage(bundleString("invalidShortcuts"));
                return;
            }

            shortcuts().save(shortcuts);
            EventMediator.fireEvent(
                    new DefaultQueryShortcutEvent(this, QueryShortcutEvent.SHORTCUT_ADDED));

            parent.finished();

        } catch (RepositoryException e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundleString("saveError"), e);
        }
    }

    private boolean shortcutsValid(List<EditorSQLShortcut> shortcuts) {

        char[] whitespaces = {' ', '\n', '\r', '\t'};
        for (EditorSQLShortcut shortcut : shortcuts) {

            if (nameExists(shortcut, shortcut.getShortcut()) ||
                    StringUtils.containsAny(shortcut.getShortcut(), whitespaces) ||
                    MiscUtils.isNull(shortcut.getQuery())) {

                return false;
            }

        }

        return true;
    }

    public void valueChanged(ListSelectionEvent e) {

        if (lastSelectedIndex != -1) {

            storeQueryForShortcut();
        }

        if (selectedIndex() != -1) {

            shortcutSelected();
        }
    }

    private int selectedIndex() {
        return list.getSelectedIndex();
    }

    private void storeQueryForShortcut() {

        EditorSQLShortcut shortcut = getShortcutAt(lastSelectedIndex);

        if (shortcut != null) {

            shortcut.setQuery(textPane.getText().trim());
        }

    }

    private void shortcutSelected() {

        EditorSQLShortcut shortcut = getSelectedShortcut();
        textPane.setText(shortcut.getQuery().trim());

        lastSelectedIndex = selectedIndex();
    }

    private EditorSQLShortcut getShortcutAt(int index) {

        DefaultListModel model = modelFromList();

        if (index >= model.size()) {

            return null;
        }

        return (EditorSQLShortcut) model.elementAt(index);
    }

    private EditorSQLShortcut getSelectedShortcut() {
        return (EditorSQLShortcut) list.getSelectedValue();
    }

    private EditorSQLShortcuts shortcuts() {
        return EditorSQLShortcuts.getInstance();
    }

    private List<EditorSQLShortcut> shortcutsFromList() {

        Object[] shortcuts = modelFromList().toArray();
        List<EditorSQLShortcut> shortcutList =
                new ArrayList<EditorSQLShortcut>(shortcuts.length);

        for (Object shortcut : shortcuts) {

            shortcutList.add((EditorSQLShortcut) shortcut);
        }

        return shortcutList;
    }

    private DefaultListModel modelFromList() {

        return (DefaultListModel) list.getModel();
    }

    private ListModel createModel() {

        EditorSQLShortcutsListModel model = new EditorSQLShortcutsListModel();

        List<EditorSQLShortcut> shortcuts = shortcuts().getEditorShortcuts();
        for (EditorSQLShortcut shortcut : shortcuts) {

            model.addElement(shortcut);
        }

        return model;
    }

    class EditorSQLShortcutsListModel extends DefaultMutableListModel {

        public void setValueAt(Object value, int index) {

            if (value == null) {

                return;
            }

            String name = value.toString();

            if (MiscUtils.isNull(name)) {

                return;
            }

            EditorSQLShortcut shortcut = (EditorSQLShortcut) modelFromList().get(index);

            if (!nameExists(shortcut, name)) {

                shortcut.setShortcut(name);

            } else {

                GUIUtilities.displayErrorMessage(
                        bundleString("validation.uniqueName"));
            }

        }

    }

    public boolean nameExists(EditorSQLShortcut shortcut, String name) {

        for (Enumeration<?> i = modelFromList().elements(); i.hasMoreElements(); ) {

            EditorSQLShortcut _shortcut = (EditorSQLShortcut) i.nextElement();

            if (name.equals(_shortcut.getShortcut())
                    && _shortcut != shortcut) {

                return true;
            }

        }

        return false;
    }

    private JPanel createMoveButtonsPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        JButton addButton = ActionUtilities.createToolbarButton(
                this,
                "addShortcut",
                GUIUtilities.loadIcon("ShortcutAdd16.svg"),
                bundleString("addShortcut"));

        JButton deleteButton = ActionUtilities.createToolbarButton(
                this,
                "deleteShortcut",
                GUIUtilities.loadIcon("ShortcutDelete16.svg"),
                bundleString("deleteShortcut"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.insets.top = 0;
        gbc.insets.bottom = 10;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(addButton, gbc);
        gbc.gridy++;
        panel.add(deleteButton, gbc);

        return panel;
    }

    private JLabel labelForKey(String key) {
        return new JLabel(bundleString(key));
    }

    private JSplitPane createSplitPane() {

        JSplitPane splitPane = new FlatSplitPane(JSplitPane.VERTICAL_SPLIT);

        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);

        return splitPane;
    }

}
