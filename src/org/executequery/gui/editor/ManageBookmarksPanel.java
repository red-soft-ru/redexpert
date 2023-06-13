/*
 * ManageBookmarksPanel.java
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

import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.event.DefaultQueryBookmarkEvent;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.DefaultActionButtonsPanel;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.repository.QueryBookmark;
import org.executequery.repository.QueryBookmarks;
import org.executequery.repository.RepositoryException;
import org.underworldlabs.swing.DefaultMutableListModel;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.MoveJListItemsStrategy;
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
public class ManageBookmarksPanel extends DefaultActionButtonsPanel
        implements ListSelectionListener {

    public static final String TITLE = "Manage Query Bookmark";
    public static final String FRAME_ICON = "Bookmarks16.svg";

    private static final String SAVE_COMMAND_NAME = "save";
    private static final String CANCEL_COMMAND_NAME = "cancel";

    private JList list;

    private SQLTextArea textPane;

    private MoveJListItemsStrategy moveStrategy;

    private int lastSelectedIndex = -1;

    private final ActionContainer parent;

    public ManageBookmarksPanel(ActionContainer parent) {

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
        panel.add(labelForKey("bookmarks"), gbc);
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

        setPreferredSize(new Dimension(800, 500));
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

        moveStrategy = new MoveJListItemsStrategy(list);
    }

    public void moveUp() {

        moveSelection(true);
    }

    public void moveDown() {

        moveSelection(false);
    }

    public void deleteBookmark() {

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

                    bookmarkSelected();

                } else {

                    textPane.setText("");
                }

            } finally {

                list.addListSelectionListener(this);
            }

        }

    }

    public void addBookmark() {

        QueryBookmark queryBookmark = new QueryBookmark();
        queryBookmark.setName(bundleString("newBookmarkName"));
        queryBookmark.setQuery(Constants.EMPTY);

        DefaultListModel model = modelFromList();

        model.addElement(queryBookmark);
        int index = model.indexOf(queryBookmark);

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

            storeQueryForBookmark();

            List<QueryBookmark> bookmarks = bookmarksFromList();

            if (!bookmarksValid(bookmarks)) {

                GUIUtilities.displayErrorMessage(
                        bundleString("invalidBookmarks"));

                return;
            }

            bookmarks().save(bookmarks);

            EventMediator.fireEvent(
                    new DefaultQueryBookmarkEvent(this, DefaultQueryBookmarkEvent.BOOKMARK_ADDED));

            parent.finished();

        } catch (RepositoryException e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundleString("saveError"), e);
        }
    }

    private boolean bookmarksValid(List<QueryBookmark> bookmarks) {

        for (QueryBookmark bookmark : bookmarks) {

            if (nameExists(bookmark, bookmark.getName())) {

                return false;
            }

            if (MiscUtils.isNull(bookmark.getQuery())) {

                return false;
            }

        }

        return true;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {

        if (lastSelectedIndex != -1) {

            storeQueryForBookmark();
        }

        if (selectedIndex() != -1) {

            bookmarkSelected();
        }
    }

    private void moveSelection(boolean moveUp) {

        if (selectedIndex() == -1) {

            return;
        }

        try {

            storeQueryForBookmark();
            list.removeListSelectionListener(this);

            if (moveUp) {

                moveStrategy.moveSelectionUp();

            } else {

                moveStrategy.moveSelectionDown();
            }

        } finally {

            lastSelectedIndex = selectedIndex();
            list.addListSelectionListener(this);
        }

    }

    private int selectedIndex() {
        return list.getSelectedIndex();
    }

    private void storeQueryForBookmark() {

        QueryBookmark bookmark = getBookmarkAt(lastSelectedIndex);

        if (bookmark != null) {

            bookmark.setQuery(textPane.getText().trim());
        }

    }

    private void bookmarkSelected() {

        QueryBookmark bookmark = getSelectedBookmark();
        textPane.setText(bookmark.getQuery().trim());

        lastSelectedIndex = selectedIndex();
    }

    private QueryBookmark getBookmarkAt(int index) {

        DefaultListModel model = modelFromList();

        if (index >= model.size()) {

            return null;
        }

        return (QueryBookmark) model.elementAt(index);
    }

    private QueryBookmark getSelectedBookmark() {
        return (QueryBookmark) list.getSelectedValue();
    }

    private QueryBookmarks bookmarks() {
        return QueryBookmarks.getInstance();
    }

    private List<QueryBookmark> bookmarksFromList() {

        Object[] bookmarks = modelFromList().toArray();

        List<QueryBookmark> bookmarkList = new ArrayList<>(bookmarks.length);
        for (Object bookmark : bookmarks) {

            bookmarkList.add((QueryBookmark) bookmark);
        }

        return bookmarkList;
    }

    private DefaultListModel modelFromList() {

        return (DefaultListModel) list.getModel();
    }

    private ListModel createModel() {

        QueryBookmarksListModel model = new QueryBookmarksListModel();

        List<QueryBookmark> bookmarks = bookmarks().getQueryBookmarks();
        for (QueryBookmark bookmark : bookmarks) {

            model.addElement(bookmark);
        }

        return model;
    }

    class QueryBookmarksListModel extends DefaultMutableListModel {

        public void setValueAt(Object value, int index) {

            if (value == null) {

                return;
            }

            String name = value.toString();

            if (MiscUtils.isNull(name)) {

                return;
            }

            QueryBookmark bookmark = (QueryBookmark) modelFromList().get(index);

            if (!nameExists(bookmark, name)) {

                bookmark.setName(name);

            } else {

                GUIUtilities.displayErrorMessage(
                        bundleString("validation.uniqueName"));
            }

        }

    }

    public boolean nameExists(QueryBookmark bookmark, String name) {

        for (Enumeration<?> i = modelFromList().elements(); i.hasMoreElements(); ) {

            QueryBookmark _bookmark = (QueryBookmark) i.nextElement();

            if (name.equals(_bookmark.getName())
                    && _bookmark != bookmark) {

                return true;
            }

        }

        return false;
    }

    private JPanel createMoveButtonsPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        JButton upButton = ActionUtilities.createToolbarButton(
                this,
                "Up16.svg",
                "Move selection up",
                "moveUp");

        JButton downButton = ActionUtilities.createToolbarButton(
                this,
                "Down16.svg",
                "Move selection down",
                "moveDown");

        JButton addButton = ActionUtilities.createToolbarButton(
                this,
                "addBookmark",
                GUIUtilities.loadIcon("AddBookmark16.svg"),
                "Add bookmark");

        JButton deleteButton = ActionUtilities.createToolbarButton(
                this,
                "deleteBookmark",
                GUIUtilities.loadIcon("DeleteBookmark16.svg"),
                "Delete bookmark");

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
        gbc.gridy++;
        gbc.insets.top = 10;
        gbc.insets.bottom = 10;
        panel.add(upButton, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.weighty = 1.0;
        panel.add(downButton, gbc);

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







