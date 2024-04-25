/*
 * ToolBarsViewMenu.java
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
import org.executequery.actions.viewcommands.ToolBarViewOptionsCommand;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;
import org.executequery.toolbars.ToolBarManager;
import org.underworldlabs.swing.toolbar.ToolBarProperties;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Takis Diakoumis
 */
public class ToolBarsViewMenu extends AbstractOptionsMenu
        implements UserPreferenceListener {

    private final ToolBarViewOptionsCommand menuItemListener;
    private final Map<String, String> toolbarActionCommands;

    public ToolBarsViewMenu() {
        menuItemListener = new ToolBarViewOptionsCommand();

        toolbarActionCommands = new HashMap<>();
        toolbarActionCommands.put("viewEditTools", ToolBarManager.EDIT_TOOLS);
        toolbarActionCommands.put("viewSearchTools", ToolBarManager.SEARCH_TOOLS);
        toolbarActionCommands.put("viewDatabaseTools", ToolBarManager.DATABASE_TOOLS);
        toolbarActionCommands.put("viewImportExportTools", ToolBarManager.IMPORT_EXPORT_TOOLS);
        toolbarActionCommands.put("viewSystemTools", ToolBarManager.SYSTEM_TOOLS);

        EventMediator.registerListener(this);
    }

    @Override
    protected boolean listeningForEvent(UserPreferenceEvent event) {
        return event.getEventType() == UserPreferenceEvent.ALL || event.getEventType() == UserPreferenceEvent.TOOL_BAR;
    }

    @Override
    protected void addActionForMenuItem(JCheckBoxMenuItem menuItem) {
        menuItem.addActionListener(menuItemListener);
    }

    @Override
    protected void setMenuItemValue(JCheckBoxMenuItem menuItem) {
        String actionCommand = menuItem.getActionCommand();
        if (actionCommand != null && toolbarActionCommands.containsKey(actionCommand))
            menuItem.setSelected(toolBarVisible(actionCommand));
    }

    private boolean toolBarVisible(String key) {
        return ToolBarProperties.isToolBarVisible(toolbarActionCommands.get(key));
    }

}
