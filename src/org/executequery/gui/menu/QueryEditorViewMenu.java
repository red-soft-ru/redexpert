/*
 * QueryEditorViewMenu.java
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

package org.executequery.gui.menu;

import org.executequery.EventMediator;
import org.executequery.actions.viewcommands.QueryEditorViewOptionsCommand;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Takis Diakoumis
 */
public class QueryEditorViewMenu extends AbstractOptionsMenu
        implements UserPreferenceListener {

    private final QueryEditorViewOptionsCommand viewOptionsCommand;
    private final Map<String, String> commandsMap;

    public QueryEditorViewMenu() {

        viewOptionsCommand = new QueryEditorViewOptionsCommand();

        commandsMap = new HashMap<>();
        commandsMap.put("viewEditorStatusBar", "editor.display.statusbar");
        commandsMap.put("viewEditorTools", "editor.display.toolsPanel");
        commandsMap.put("viewEditorLineNumbers", "editor.display.linenums");
        commandsMap.put("viewEditorLineHighlight", "editor.display.linehighlight");

        EventMediator.registerListener(this);
    }

    @Override
    protected void addActionForMenuItem(JCheckBoxMenuItem menuItem) {
        menuItem.addActionListener(viewOptionsCommand);
    }

    @Override
    protected void setMenuItemValue(JCheckBoxMenuItem menuItem) {
        String command = menuItem.getActionCommand();
        if (command != null && commandsMap.containsKey(command))
            menuItem.setSelected(booleanValueForKey(commandsMap.get(command)));
    }

    @Override
    protected boolean listeningForEvent(UserPreferenceEvent event) {
        int type = event.getEventType();
        return type == UserPreferenceEvent.QUERY_EDITOR || type == UserPreferenceEvent.ALL;
    }
}
