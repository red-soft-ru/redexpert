/*
 * PropertiesSqlShortcuts.java
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

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.event.DefaultQueryShortcutEvent;
import org.executequery.event.QueryShortcutEvent;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.repository.EditorSQLShortcut;
import org.executequery.repository.EditorSQLShortcuts;
import org.executequery.repository.RepositoryException;
import org.underworldlabs.swing.DefaultMutableListModel;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.MutableValueJList;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * @author Takis Diakoumis
 */
public class PropertiesSqlShortcuts extends AbstractPropertiesBasePanel
        implements ListSelectionListener {

    private JList<?> list;
    private JButton addButton;
    private JButton deleteButton;
    private SQLTextArea textPane;

    private int lastSelectedIndex = -1;

    public PropertiesSqlShortcuts(PropertiesPanel parent) {
        super(parent);
        init();
        arrange();
    }

    private void init() {

        textPane = new SQLTextArea();
        textPane.setPreferredSize(new Dimension(300, 350));

        list = new MutableValueJList(createModel());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        list.setSelectedIndex(0);

        addButton = WidgetFactory.createButton(
                "addButton",
                bundledString("addShortcut"),
                e -> addShortcut()
        );

        deleteButton = WidgetFactory.createButton(
                "deleteButton",
                bundledString("deleteShortcut"),
                e -> deleteShortcut()
        );
    }

    private void arrange() {

        GridBagHelper gbh;

        // --- split pane ---

        JSplitPane splitPane = new FlatSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setRightComponent(new JScrollPane(textPane));
        splitPane.setLeftComponent(new JScrollPane(list));
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(4);

        // --- buttons panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().fillHorizontally().anchorNorthWest();
        buttonPanel.add(addButton, gbh.setWeightX(1.0).get());
        buttonPanel.add(deleteButton, gbh.nextCol().leftGap(5).spanX().get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().fillBoth().anchorNorthWest();
        mainPanel.add(splitPane, gbh.setMaxWeightY().bottomGap(5).spanX().get());
        mainPanel.add(buttonPanel, gbh.nextRow().setMinWeightY().get());

        // --- base ---

        addContent(mainPanel);
        hideBottomButtons();
    }

    @SuppressWarnings("unchecked")
    private ListModel<?> createModel() {

        DefaultMutableListModel model = new DefaultMutableListModel() {

            @Override
            public void setValueAt(Object value, int index) {

                if (value == null)
                    return;

                String name = value.toString();
                if (MiscUtils.isNull(name))
                    return;

                EditorSQLShortcut shortcut = getListModel().get(index);
                if (nameExists(shortcut, name)) {
                    GUIUtilities.displayErrorMessage(bundledString("validation.uniqueName"));
                    return;
                }

                shortcut.setShortcut(name);
            }
        };

        for (EditorSQLShortcut shortcut : getShortcuts().getEditorShortcuts())
            model.addElement(shortcut);

        return model;
    }

    // --- Buttons handlers ---

    public void addShortcut() {

        EditorSQLShortcut shortcut = new EditorSQLShortcut();
        shortcut.setShortcut(bundledString("newShortcutName"));
        shortcut.setQuery(Constants.EMPTY);

        DefaultListModel<EditorSQLShortcut> model = getListModel();
        model.addElement(shortcut);

        int index = model.indexOf(shortcut);
        list.setSelectedIndex(index);
        list.scrollRectToVisible(list.getCellBounds(index, index));

        list.getActionMap().get("startEditing").actionPerformed(new ActionEvent(list, ActionEvent.ACTION_FIRST, null));
    }

    public void deleteShortcut() {

        int index = getSelectedIndex();
        if (index < 0)
            return;

        try {
            list.removeListSelectionListener(this);

            DefaultListModel<?> model = getListModel();
            model.remove(index);

            lastSelectedIndex = -1;

            int size = model.getSize();
            if (size > 0) {
                list.setSelectedIndex(Math.min(index, size - 1));
                shortcutSelected();

            } else
                textPane.setText("");

        } finally {
            list.addListSelectionListener(this);
        }
    }

    // ---

    private int getSelectedIndex() {
        return list.getSelectedIndex();
    }

    private EditorSQLShortcuts getShortcuts() {
        return EditorSQLShortcuts.getInstance();
    }

    private EditorSQLShortcut getShortcutAt(int index) {

        DefaultListModel<?> model = getListModel();
        if (index >= model.size())
            return null;

        return (EditorSQLShortcut) model.elementAt(index);
    }

    @SuppressWarnings("unchecked")
    private DefaultListModel<EditorSQLShortcut> getListModel() {
        return (DefaultListModel<EditorSQLShortcut>) list.getModel();
    }

    private void storeQueryForShortcut() {
        EditorSQLShortcut shortcut = getShortcutAt(lastSelectedIndex);
        if (shortcut != null)
            shortcut.setQuery(textPane.getText().trim());
    }

    private void shortcutSelected() {
        textPane.setText(((EditorSQLShortcut) list.getSelectedValue()).getQuery().trim());
        lastSelectedIndex = getSelectedIndex();
    }

    private boolean nameExists(EditorSQLShortcut originalShortcut, String name) {

        for (Enumeration<?> i = getListModel().elements(); i.hasMoreElements(); ) {
            EditorSQLShortcut shortcut = (EditorSQLShortcut) i.nextElement();
            if (Objects.equals(name, shortcut.getShortcut()) && shortcut != originalShortcut)
                return true;
        }

        return false;
    }

    // --- UserPreferenceFunction impl ---

    @Override
    public void save() {
        try {
            storeQueryForShortcut();

            List<EditorSQLShortcut> shortcuts = new ArrayList<>();
            for (Object shortcut : getListModel().toArray())
                shortcuts.add((EditorSQLShortcut) shortcut);

            boolean shortcutsValid = shortcuts.stream().noneMatch(shortcut ->
                    nameExists(shortcut, shortcut.getShortcut())
                            || StringUtils.containsAny(shortcut.getShortcut(), new char[]{' ', '\n', '\r', '\t'})
                            || MiscUtils.isNull(shortcut.getQuery())
            );

            if (!shortcutsValid) {
                GUIUtilities.displayErrorMessage(bundledString("invalidShortcuts"));
                return;
            }

            getShortcuts().save(shortcuts);
            EventMediator.fireEvent(new DefaultQueryShortcutEvent(this, QueryShortcutEvent.SHORTCUT_ADDED));

        } catch (RepositoryException e) {
            GUIUtilities.displayExceptionErrorDialog(bundledString("saveError"), e);
        }
    }

    @Override
    public void restoreDefaults() {
    }

    // --- ListSelectionListener impl ---

    @Override
    public void valueChanged(ListSelectionEvent e) {

        if (lastSelectedIndex != -1)
            storeQueryForShortcut();

        if (getSelectedIndex() != -1)
            shortcutSelected();
    }

}
