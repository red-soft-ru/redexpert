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

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;

import org.executequery.EventMediator;
import org.executequery.actions.viewcommands.QueryEditorViewOptionsCommand;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;

/** 
 * 
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class QueryEditorViewMenu extends AbstractOptionsMenu 
                                 implements UserPreferenceListener {

    private QueryEditorViewOptionsCommand menuItemListener;
    
    public QueryEditorViewMenu() {
        
        menuItemListener = new QueryEditorViewOptionsCommand();
        
        createCommandToPropertiesMap();
        
        EventMediator.registerListener(this);
    }
    
    protected void addActionForMenuItem(JCheckBoxMenuItem menuItem) {
        
        menuItem.addActionListener(menuItemListener);
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
    
        return (event.getEventType() == UserPreferenceEvent.QUERY_EDITOR
                    ||event.getEventType() == UserPreferenceEvent.ALL);
    }

    private Map<String, String> actionCommandsToPropertiesMap; 
    
    private void createCommandToPropertiesMap() {
        
        actionCommandsToPropertiesMap = new HashMap<String, String>();

        actionCommandsToPropertiesMap.put("viewEditorStatusBar", "editor.display.statusbar");
        actionCommandsToPropertiesMap.put("viewEditorLineNumbers", "editor.display.linenums");

    }
    
}











