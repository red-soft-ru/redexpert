/*
 * QueryEditorToolBar.java
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

import org.executequery.GUIUtilities;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.repository.QueryBookmark;
import org.executequery.repository.QueryBookmarks;
import org.underworldlabs.swing.PopupMenuButton;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.swing.toolbar.PanelToolBar;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The Query Editor's toolbar
 *
 * @author Takis Diakoumis
 */
class QueryEditorToolBar extends PanelToolBar {

    private static final String EXECUTE_COMMAND = "execute-command";
    private static final String EXECUTE_IN_ANY_CONNECTIONS_COMMAND = "execute-in-any-connections-command";
    private static final String EXECUTE_SCRIPT_COMMAND = "execute-script-command";
    private static final String EXECUTE_AT_CURSOR_COMMAND = "execute-at-cursor-command";
    private static final String EXECUTE_SELECTION_COMMAND = "execute-selection-command";
    private static final String EXECUTE_IN_PROFILER_COMMAND = "execute-in-profiler-command";
    private static final String EDITOR_STOP_COMMAND = "editor-stop-command";

    private static final String COMMIT_COMMAND = "commit-command";
    private static final String ROLLBACK_COMMAND = "rollback-command";
    private static final String TOGGLE_AUTOCOMMIT_COMMAND = "toggle-autocommit-command";

    private static final String QUERY_BOOKMARKS = "query-bookmarks";
    private static final String QUERY_SHORTCUTS = "manage-shortcuts-command";
    private static final String SQL_HISTORY_COMMAND = "sql-history-command";
    private static final String EDITOR_NEXT_COMMAND = "editor-next-command";
    private static final String EDITOR_PREVIOUS_COMMAND = "editor-previous-command";

    private static final String EDITOR_EXPORT_COMMAND = "editor-export-command";
    private static final String EDITOR_RS_METADATA_COMMAND = "editor-rs-metadata-command";
    private static final String EDITOR_SHOW_HIDE_RS_COLUMNS_COMMAND = "editor-show-hide-rs-columns-command";

    private static final String OPEN_COMMAND = "open-command";
    private static final String SAVE_AS_COMMAND = "save-as-command";
    private static final String PRINT_COMMAND = "print-command";
    private static final String PRINT_PLAN_COMMAND = "print-plan-command";
    private static final String PRINT_EXPLAINED_PLAN_COMMAND = "print-explained-plan-command";

    private static final String FIND_COMMAND = "find-command";
    private static final String FIND_NEXT_COMMAND = "find-next-command";
    private static final String REPLACE_COMMAND = "replace-command";

    private static final String TOGGLE_EDITOR_OUTPUT_COMMAND = "toggle-editor-output-command";
    private static final String CHANGE_SPLIT_ORIENTATION = "change-split-orientation-command";

    private final InputMap queryEditorInputMap;
    private final ActionMap queryEditorActionMap;

    private Map<String, RolloverButton> buttons;

    public QueryEditorToolBar(ActionMap queryEditorActionMap, InputMap queryEditorInputMap) {
        this.queryEditorActionMap = queryEditorActionMap;
        this.queryEditorInputMap = queryEditorInputMap;

        init();
    }

    private void init() {
        buttons = new HashMap<>();

        addButton(createButton(EXECUTE_COMMAND));
        addButton(createButton(EXECUTE_IN_ANY_CONNECTIONS_COMMAND));
        addButton(createButton(EXECUTE_SCRIPT_COMMAND));
        addButton(createButton(EXECUTE_AT_CURSOR_COMMAND));
        addButton(createButton(EXECUTE_SELECTION_COMMAND));
        addButton(createButton(EXECUTE_IN_PROFILER_COMMAND));
        addButton(createButton(EDITOR_STOP_COMMAND));

        addSeparator();
        addButton(createButton(COMMIT_COMMAND));
        addButton(createButton(ROLLBACK_COMMAND));
        addButton(createButton(TOGGLE_AUTOCOMMIT_COMMAND));

        addSeparator();
        addButton(createQueryBookmarkButton());
        addButton(createButton(QUERY_SHORTCUTS));
        addButton(createButton(SQL_HISTORY_COMMAND));
        addButton(createButton(EDITOR_PREVIOUS_COMMAND));
        addButton(createButton(EDITOR_NEXT_COMMAND));

        addSeparator();
        addButton(createButton(EDITOR_SHOW_HIDE_RS_COLUMNS_COMMAND));
        addButton(createButton(EDITOR_RS_METADATA_COMMAND));
        addButton(createButton(EDITOR_EXPORT_COMMAND));

        addSeparator();
        addButton(createButton(OPEN_COMMAND, Bundles.get("action." + OPEN_COMMAND)));
        addButton(createButton(SAVE_AS_COMMAND, Bundles.get("action." + SAVE_AS_COMMAND)));
        addButton(createButton(PRINT_COMMAND, Bundles.get("action." + PRINT_COMMAND)));
        addButton(createButton(PRINT_PLAN_COMMAND));
        addButton(createButton(PRINT_EXPLAINED_PLAN_COMMAND));

        addSeparator();
        addButton(createButton(FIND_COMMAND, Bundles.get("action." + FIND_COMMAND)));
        addButton(createButton(FIND_NEXT_COMMAND, Bundles.get("action." + FIND_NEXT_COMMAND)));
        addButton(createButton(REPLACE_COMMAND, Bundles.get("action." + REPLACE_COMMAND)));

        addSeparator();
        addButton(createButton(CHANGE_SPLIT_ORIENTATION));
        addButton(createButton(TOGGLE_EDITOR_OUTPUT_COMMAND));
    }

    private JButton createQueryBookmarkButton() {

        PopupMenuButton button = new PopupMenuButton(
                GUIUtilities.loadIcon("Bookmarks16.png"),
                bundleString("query-bookmarks")
        );
        button.setKeyStroke(KeyStroke.getKeyStroke("control B"));

        String actionMapKey = "bookmarks-button";
        KeyStroke keyStroke = KeyStroke.getKeyStroke("control B");
        queryEditorActionMap.put(actionMapKey, button.getAction());
        queryEditorInputMap.put(keyStroke, actionMapKey);

        actionMapKey = "add-bookmark-command";
        keyStroke = KeyStroke.getKeyStroke("control shift B");
        queryEditorActionMap.put(actionMapKey, ActionBuilder.get(actionMapKey));
        queryEditorInputMap.put(keyStroke, actionMapKey);

        createQueryBookmarkMenuItems(button);
        buttons.put(QUERY_BOOKMARKS, button);

        return button;
    }

    private void createQueryBookmarkMenuItems(PopupMenuButton button) {

        button.removeMenuItems();
        button.addMenuItem(createMenuItemFromCommand("add-bookmark-command"));
        button.addMenuItem(createMenuItemFromCommand("manage-bookmarks-command"));

        if (!QueryBookmarks.getInstance().hasQueryBookmarks())
            return;

        button.addSeparator();

        for (QueryBookmark bookmark : QueryBookmarks.getInstance().getQueryBookmarks()) {

            JMenuItem menuItem = createMenuItemFromCommand("select-bookmark-command");
            menuItem.setActionCommand(bookmark.getName());
            menuItem.setText(bookmark.getName());

            button.addMenuItem(menuItem);
        }
    }

    protected void reloadBookmarkItems() {
        createQueryBookmarkMenuItems((PopupMenuButton) buttons.get(QUERY_BOOKMARKS));
    }

    private JMenuItem createMenuItemFromCommand(String actionId) {
        return MenuItemFactory.createMenuItem(ActionBuilder.get(actionId));
    }

    private RolloverButton createButton(String actionId) {
        return createButton(actionId, bundleString(actionId));
    }

    private RolloverButton createButton(String actionId, String toolTipText) {
        RolloverButton button = WidgetFactory.createRolloverButton(actionId, ActionBuilder.get(actionId), toolTipText);
        buttons.put(actionId, button);
        return button;
    }

    public void setMetaDataButtonEnabled(boolean enable) {
        buttons.get(EDITOR_RS_METADATA_COMMAND).setEnabled(enable);
    }

    public void setPreviousButtonEnabled(boolean enable) {
        buttons.get(EDITOR_PREVIOUS_COMMAND).setEnabled(enable);
    }

    public void setNextButtonEnabled(boolean enable) {
        buttons.get(EDITOR_NEXT_COMMAND).setEnabled(enable);
    }

    public void setStopButtonEnabled(boolean enable) {
        buttons.get(EDITOR_STOP_COMMAND).setEnabled(enable);
        buttons.get(EXECUTE_COMMAND).setEnabled(!enable);
        buttons.get(EXECUTE_AT_CURSOR_COMMAND).setEnabled(!enable);
        buttons.get(EXECUTE_SELECTION_COMMAND).setEnabled(!enable);
    }

    public void setCommitsEnabled(boolean enable) {
        buttons.get(COMMIT_COMMAND).setEnabled(enable);
        buttons.get(ROLLBACK_COMMAND).setEnabled(enable);
    }

    public void setExportButtonEnabled(boolean enable) {
        buttons.get(EDITOR_EXPORT_COMMAND).setEnabled(enable);
        buttons.get(EDITOR_SHOW_HIDE_RS_COLUMNS_COMMAND).setEnabled(enable);
    }

    @Override
    public String toString() {
        return "Query Editor Tool Bar";
    }

    private String bundleString(String key) {
        return Bundles.get(QueryEditorToolBar.class, key);
    }

}
