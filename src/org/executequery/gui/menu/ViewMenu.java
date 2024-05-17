/*
 * ViewMenu.java
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
import org.executequery.actions.viewcommands.ViewOptionsCommand;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Takis Diakoumis
 */
public class ViewMenu extends AbstractOptionsMenu
        implements UserPreferenceListener {

    private final ViewOptionsCommand viewOptionsCommand;
    private final Map<String, String> commandsMap;

    public ViewMenu() {

        viewOptionsCommand = new ViewOptionsCommand();

        commandsMap = new HashMap<>();
        commandsMap.put("viewConsole", "system.display.console");
        commandsMap.put("viewConnections", "system.display.connections");
        commandsMap.put("viewStatusBar", "system.display.statusbar");

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
        return type == UserPreferenceEvent.ALL || type == UserPreferenceEvent.DOCKED_COMPONENT_CLOSED;
    }

}
