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
import org.executequery.gui.IconManager;
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
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The Query Editor's toolbar
 *
 * @author Takis Diakoumis
 */
class QueryEditorToolBar extends PanelToolBar {

    private static final String EXECUTE_SCRIPT_COMMAND = "execute-statement-command";
    private static final String EXECUTE_STATEMENT_COMMAND = "execute-statement-command";
    private static final String EXECUTE_IN_PROFILER_COMMAND = "execute-statement-command";
    private static final String STOP_EXECUTION_COMMAND = "stop-execution-command";
    private static final String STOP_ON_ERROR_COMMAND = "editor-stop-on-error-command";
    private static final String EXECUTE_TO_FILE_COMMAND = "editor-execute-to-file-command";

    private static final String COMMIT_COMMAND = "commit-command";
    private static final String ROLLBACK_COMMAND = "rollback-command";
    private static final String EDITOR_NEXT_COMMAND = "editor-next-command";
    private static final String QUERY_BOOKMARKS = "manage-bookmarks-command";
    private static final String EDITOR_EXPORT_COMMAND = "editor-export-command";
    private static final String EDITOR_PREVIOUS_COMMAND = "editor-previous-command";
    private static final String EDITOR_RS_METADATA_COMMAND = "editor-rs-metadata-command";
    private static final String EDITOR_SHOW_HIDE_RS_COLUMNS_COMMAND = "editor-result-set-filter-command";

    private final Component[] connectionCombos;
    private final InputMap queryEditorInputMap;
    private final ActionMap queryEditorActionMap;
    private final Map<String, RolloverButton> buttons;

    public QueryEditorToolBar(Component[] connectionCombos, ActionMap queryEditorActionMap, InputMap queryEditorInputMap) {
        this.queryEditorActionMap = queryEditorActionMap;
        this.queryEditorInputMap = queryEditorInputMap;
        this.connectionCombos = connectionCombos;
        this.buttons = new HashMap<>();
        init();
    }

    private void init() {

        ToolBarWrapper wrapper = ToolBarProperties.getToolBar(ToolBarManager.QUERY_EDITOR_TOOLS);
        if (wrapper == null || !wrapper.isVisible() || !wrapper.hasButtons())
            return;

        wrapper.getButtonsVector().stream()
                .sorted(new ButtonComparator())
                .filter(ToolBarButton::isVisible)
                .forEachOrdered(this::addToolBarButton);
    }

    private void addToolBarButton(ToolBarButton button) {

        if (button.isSeparator()) {
            addSeparator();

        } else if (Objects.equals(button.getActionId(), "connection-combo-template")) {
            Arrays.stream(connectionCombos).forEach(this::add);

        } else if (Objects.equals(button.getActionId(), QUERY_BOOKMARKS)) {
            addButton(createQueryBookmarkButton());

        } else
            addButton(createButton(button.getActionId()));
    }

    private JButton createQueryBookmarkButton() {

        PopupMenuButton button = new PopupMenuButton(
                IconManager.getIcon("icon_bookmarks"),
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
        if (buttons.containsKey(STOP_EXECUTION_COMMAND))
            buttons.get(STOP_EXECUTION_COMMAND).setEnabled(enable);
        if (buttons.containsKey(EXECUTE_SCRIPT_COMMAND))
            buttons.get(EXECUTE_SCRIPT_COMMAND).setEnabled(!enable);
        if (buttons.containsKey(EXECUTE_STATEMENT_COMMAND))
            buttons.get(EXECUTE_STATEMENT_COMMAND).setEnabled(!enable);
        if (buttons.containsKey(EXECUTE_IN_PROFILER_COMMAND))
            buttons.get(EXECUTE_IN_PROFILER_COMMAND).setEnabled(!enable);
        if (buttons.containsKey(STOP_ON_ERROR_COMMAND))
            buttons.get(STOP_ON_ERROR_COMMAND).setEnabled(!enable);
        if (buttons.containsKey(EXECUTE_TO_FILE_COMMAND))
            buttons.get(EXECUTE_TO_FILE_COMMAND).setEnabled(!enable);
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

    public JButton getButton(String actionId) {
        return buttons.get(actionId);
    }

    @Override
    public String toString() {
        return "Query Editor Tool Bar";
    }

    private String bundleString(String key) {
        return Bundles.get(QueryEditorToolBar.class, key);
    }

}
