/*
 * QueryEditorViewOptionsCommand.java
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

package org.executequery.actions.viewcommands;

import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.event.DefaultUserPreferenceEvent;
import org.executequery.event.UserPreferenceEvent;
import org.underworldlabs.util.SystemProperties;

import java.awt.event.ActionEvent;

/**
 * @author Takis Diakoumis
 */
public class QueryEditorViewOptionsCommand extends AbstractViewOptionsCommand {

    private static final String EDITOR_DISPLAY_STATUSBAR = "editor.display.statusbar";
    private static final String EDITOR_DISPLAY_TOOLS = "editor.display.toolsPanel";
    private static final String EDITOR_DISPLAY_LINE_NUMS = "editor.display.linenums";
    private static final String EDITOR_DISPLAY_LINE_HIGHLIGHT = "editor.display.linehighlight";
    private static final String EDITOR_WRAP_LINES = "editor.wrap.lines";
    private static final String EDITOR_DISPLAY_TRANSACTION_PARAMS = "editor.display.transaction.params";

    @SuppressWarnings("unused")
    public void viewEditorStatusBar(ActionEvent e) {
        setBooleanProperty(EDITOR_DISPLAY_STATUSBAR, selectionFromEvent(e));
        fireEditorPreferencesChangedEvent(EDITOR_DISPLAY_STATUSBAR);
    }

    @SuppressWarnings("unused")
    public void viewEditorTools(ActionEvent e) {
        setBooleanProperty(EDITOR_DISPLAY_TOOLS, selectionFromEvent(e));
        fireEditorPreferencesChangedEvent(EDITOR_DISPLAY_TOOLS);
    }

    @SuppressWarnings("unused")
    public void viewEditorLineNumbers(ActionEvent e) {
        setBooleanProperty(EDITOR_DISPLAY_LINE_NUMS, selectionFromEvent(e));
        fireEditorPreferencesChangedEvent(EDITOR_DISPLAY_LINE_NUMS);
    }

    @SuppressWarnings("unused")
    public void viewEditorLineHighlight(ActionEvent e) {
        setBooleanProperty(EDITOR_DISPLAY_LINE_HIGHLIGHT, selectionFromEvent(e));
        fireEditorPreferencesChangedEvent(EDITOR_DISPLAY_LINE_HIGHLIGHT);
    }

    @SuppressWarnings("unused")
    public void viewEditorWrapLines(ActionEvent e) {
        setBooleanProperty(EDITOR_WRAP_LINES, selectionFromEvent(e));
        fireEditorPreferencesChangedEvent(EDITOR_WRAP_LINES);
    }

    @SuppressWarnings("unused")
    public void viewEditorTransactionParams(ActionEvent e) {
        setBooleanProperty(EDITOR_DISPLAY_TRANSACTION_PARAMS, selectionFromEvent(e));
        fireEditorPreferencesChangedEvent(EDITOR_DISPLAY_TRANSACTION_PARAMS);
    }

    private void fireEditorPreferencesChangedEvent(String key) {
        EventMediator.fireEvent(new DefaultUserPreferenceEvent(this, key, UserPreferenceEvent.QUERY_EDITOR));
    }

    private void setBooleanProperty(String key, boolean value) {
        SystemProperties.setBooleanProperty(Constants.USER_PROPERTIES_KEY, key, value);
    }

}
