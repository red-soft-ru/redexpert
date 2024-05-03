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
import org.executequery.toolbars.ToolBarManager;
import org.underworldlabs.swing.PopupMenuButton;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.swing.toolbar.*;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The Query Editor's toolbar
 *
 * @author Takis Diakoumis
 */
class QueryEditorToolBar extends PanelToolBar {

    private static final String COMMIT_COMMAND = "commit-command";
    private static final String EXECUTE_COMMAND = "execute-command";
    private static final String ROLLBACK_COMMAND = "rollback-command";
    private static final String EDITOR_NEXT_COMMAND = "editor-next-command";
    private static final String EDITOR_STOP_COMMAND = "editor-stop-command";
    private static final String QUERY_BOOKMARKS = "manage-bookmarks-command";
    private static final String EDITOR_EXPORT_COMMAND = "editor-export-command";
    private static final String EDITOR_PREVIOUS_COMMAND = "editor-previous-command";
    private static final String EXECUTE_AT_CURSOR_COMMAND = "execute-at-cursor-command";
    private static final String EXECUTE_SELECTION_COMMAND = "execute-selection-command";
    private static final String EDITOR_RS_METADATA_COMMAND = "editor-rs-metadata-command";
    private static final String EDITOR_SHOW_HIDE_RS_COLUMNS_COMMAND = "editor-show-hide-rs-columns-command";

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
        buildToolBar();
    }

    public void buildToolBar() {
        removeAll();

        ToolBarWrapper wrapper = ToolBarProperties.getToolBar(ToolBarManager.QUERY_EDITOR_TOOLS);
        if (wrapper == null || !wrapper.isVisible() || !wrapper.hasButtons())
            return;

        wrapper.getButtonsVector().stream()
                .sorted(new ButtonComparator())
                .filter(ToolBarButton::isVisible)
                .forEachOrdered(button -> {
                    if (button.isSeparator()) {
                        addSeparator();
                    } else if (Objects.equals(button.getActionId(), QUERY_BOOKMARKS)) {
                        addButton(createQueryBookmarkButton());
                    } else
                        addButton(createButton(button.getActionId()));
                });
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
        JMenuItem menuItem = MenuItemFactory.createMenuItem(ActionBuilder.get(actionId));
        menuItem.setIcon(null);
        return menuItem;
    }

    private RolloverButton createButton(String actionId) {

        RolloverButton button = WidgetFactory.createRolloverButton(
                actionId,
                ActionBuilder.get(actionId),
                bundleString(actionId)
        );

        buttons.put(actionId, button);
        return button;
    }

    public void setMetaDataButtonEnabled(boolean enable) {
        if (buttons.containsKey(EDITOR_RS_METADATA_COMMAND))
            buttons.get(EDITOR_RS_METADATA_COMMAND).setEnabled(enable);
    }

    public void setPreviousButtonEnabled(boolean enable) {
        if (buttons.containsKey(EDITOR_PREVIOUS_COMMAND))
            buttons.get(EDITOR_PREVIOUS_COMMAND).setEnabled(enable);
    }

    public void setNextButtonEnabled(boolean enable) {
        if (buttons.containsKey(EDITOR_NEXT_COMMAND))
            buttons.get(EDITOR_NEXT_COMMAND).setEnabled(enable);
    }

    public void setStopButtonEnabled(boolean enable) {
        if (buttons.containsKey(EDITOR_STOP_COMMAND))
            buttons.get(EDITOR_STOP_COMMAND).setEnabled(enable);
        if (buttons.containsKey(EXECUTE_COMMAND))
            buttons.get(EXECUTE_COMMAND).setEnabled(!enable);
        if (buttons.containsKey(EXECUTE_AT_CURSOR_COMMAND))
            buttons.get(EXECUTE_AT_CURSOR_COMMAND).setEnabled(!enable);
        if (buttons.containsKey(EXECUTE_SELECTION_COMMAND))
            buttons.get(EXECUTE_SELECTION_COMMAND).setEnabled(!enable);
    }

    public void setCommitsEnabled(boolean enable) {
        if (buttons.containsKey(COMMIT_COMMAND))
            buttons.get(COMMIT_COMMAND).setEnabled(enable);
        if (buttons.containsKey(ROLLBACK_COMMAND))
            buttons.get(ROLLBACK_COMMAND).setEnabled(enable);
    }

    public void setExportButtonEnabled(boolean enable) {
        if (buttons.containsKey(EDITOR_EXPORT_COMMAND))
            buttons.get(EDITOR_EXPORT_COMMAND).setEnabled(enable);
        if (buttons.containsKey(EDITOR_SHOW_HIDE_RS_COLUMNS_COMMAND))
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
