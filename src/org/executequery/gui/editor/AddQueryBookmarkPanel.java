/*
 * AddQueryBookmarkPanel.java
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

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.event.DefaultQueryBookmarkEvent;
import org.executequery.gui.*;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.repository.QueryBookmark;
import org.executequery.repository.QueryBookmarks;
import org.executequery.repository.RepositoryException;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class AddQueryBookmarkPanel extends DefaultActionButtonsPanel
        implements FocusComponentPanel {

    public static final String TITLE = "Add Query Bookmark";
    public static final String FRAME_ICON = "Bookmarks16.svg";

    private static final String SAVE_COMMAND_NAME = "save";
    private static final String CANCEL_COMMAND_NAME = "cancel";

    private JComboBox nameField;

    private SQLTextArea textPane;

    private final ActionContainer parent;

    private final String queryText;

    public AddQueryBookmarkPanel(ActionContainer parent, String queryText) {

        this.parent = parent;
        this.queryText = queryText.trim();

        init();
    }

    private void init() {

        createNameComboBox();

        textPane = new SQLTextArea();
        textPane.setText(queryText);

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.insets.top = 7;
        gbc.insets.left = 5;
        panel.add(new JLabel(bundleString("AddQueryBookmarkPanel.bookmarkName")), gbc);
        gbc.gridx = 1;
        gbc.insets.top = 5;
        gbc.insets.right = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets.bottom = 5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(new JScrollPane(textPane), gbc);

        addContentPanel(panel);

        addActionButton(createSaveButton());
        addActionButton(createCancelButton());

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

    private void createNameComboBox() {

        List<QueryBookmark> bookmarks = bookmarks().getQueryBookmarks();

        Vector<String> names = new Vector<String>(bookmarks.size() + 1);
        names.add("");

        for (QueryBookmark bookmark : bookmarks) {

            names.add(bookmark.getName());
        }

        nameField = WidgetFactory.createComboBox("nameField", names);
        nameField.setEditable(true);
        nameField.setActionCommand(SAVE_COMMAND_NAME);

        nameField.setMinimumSize(new Dimension(nameField.getWidth(), 22));

        nameFieldEditor().setActionCommand(SAVE_COMMAND_NAME);
        nameFieldEditor().addActionListener(this);
    }

    private JTextField nameFieldEditor() {
        return (JTextField) ((ComboBoxEditor) nameField.getEditor()).getEditorComponent();
    }

    public void cancel() {
        parent.finished();
    }

    public void save() {

        if (!fieldsValid()) {

            return;
        }

        QueryBookmark bookmark = createBookmark();

        if (bookmark == null) {

            return;
        }

        try {

            if (bookmark.isNew()) {

                bookmarks().addBookmark(bookmark);

            } else {

                bookmarks().save();
            }

            EventMediator.fireEvent(
                    new DefaultQueryBookmarkEvent(this, DefaultQueryBookmarkEvent.BOOKMARK_ADDED));

            parent.finished();

        } catch (RepositoryException e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundleString("saveError"), e);
        }

    }

    private QueryBookmark createBookmark() {

        QueryBookmark bookmark = null;

        String name = getNameFieldText();
        if (bookmarks().nameExists(name)) {

            int result = GUIUtilities.displayConfirmCancelDialog(
                    bundleString("validation.uniqueName"));

            if (result == JOptionPane.YES_OPTION) {

                bookmark = bookmarks().findBookmarkByName(name);
                bookmark.setQuery(textPane.getText());

            } else {

                return null;
            }

        } else {

            bookmark = newBookmark();
        }

        return bookmark;
    }

    private String getNameFieldText() {
        return nameField.getEditor().getItem().toString();
    }

    private QueryBookmarks bookmarks() {
        return QueryBookmarks.getInstance();
    }

    private boolean fieldsValid() {

        String value = getNameFieldText();
        if (MiscUtils.isNull(value)) {

            GUIUtilities.displayErrorMessage(bundleString("validation.name"));
            return false;
        }

        value = textPane.getText();
        if (MiscUtils.isNull(value)) {

            GUIUtilities.displayErrorMessage(bundleString("validation.query"));
            return false;
        }

        return true;
    }

    private QueryBookmark newBookmark() {

        QueryBookmark bookmark = new QueryBookmark();
        bookmark.setName(getNameFieldText());
        bookmark.setQuery(textPane.getText());

        return bookmark;
    }

    public Component getDefaultFocusComponent() {
        return nameField;
    }

}

