/*
 * ViewMenu.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;

import org.executequery.EventMediator;
import org.executequery.actions.viewcommands.ViewOptionsCommand;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;

/** 
 * 
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ViewMenu extends AbstractOptionsMenu 
                      implements UserPreferenceListener {

    private ViewOptionsCommand viewOptionsCommand;
    
    public ViewMenu() {
        
        viewOptionsCommand = new ViewOptionsCommand();
        
        createCommandToPropertiesMap();
        
        EventMediator.registerListener(this);
    }
    
    protected void addActionForMenuItem(JCheckBoxMenuItem menuItem) {
        
        menuItem.addActionListener(viewOptionsCommand);
    }
    
    protected void setMenuItemValue(JCheckBoxMenuItem menuItem) {

        String actionCommand = menuItem.getActionCommand();
        
        if (actionCommand != null && 
                actionCommandsToPropertiesMap.containsKey(actionCommand)) {

            menuItem.setSelected(booleanValueForKey(
                    actionCommandsToPropertiesMap.get(actionCommand)));
        }

    }

    protected boolean listeningForEvent(UserPreferenceEvent event) {
        
        return (event.getEventType() == UserPreferenceEvent.ALL 
                || event.getEventType() == UserPreferenceEvent.DOCKED_COMPONENT_CLOSED);
    }

    private Map<String, String> actionCommandsToPropertiesMap; 
    
    private void createCommandToPropertiesMap() {
        
        actionCommandsToPropertiesMap = new HashMap<String, String>();

        actionCommandsToPropertiesMap.put("viewConsole", "system.display.console");
        actionCommandsToPropertiesMap.put("viewConnections", "system.display.connections");
        actionCommandsToPropertiesMap.put("viewKeywords", "system.display.keywords");
        actionCommandsToPropertiesMap.put("viewSqlStateCodes", "system.display.state-codes");
        actionCommandsToPropertiesMap.put("viewDrivers", "system.display.drivers");
        actionCommandsToPropertiesMap.put("viewSystemProperties", "system.display.systemprops");
        actionCommandsToPropertiesMap.put("viewNotepad", "system.display.notepad");
        actionCommandsToPropertiesMap.put("viewStatusBar", "system.display.statusbar");

    }
    
}





