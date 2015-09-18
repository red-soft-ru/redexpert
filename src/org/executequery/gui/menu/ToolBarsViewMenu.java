/*
 * ToolBarsViewMenu.java
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
import org.executequery.actions.viewcommands.ToolBarViewOptionsCommand;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;
import org.executequery.toolbars.ToolBarManager;
import org.underworldlabs.swing.toolbar.ToolBarProperties;

/** 
 * 
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ToolBarsViewMenu extends AbstractOptionsMenu 
                              implements UserPreferenceListener {

    private ToolBarViewOptionsCommand menuItemListener;
    
    public ToolBarsViewMenu() {
        
        menuItemListener = new ToolBarViewOptionsCommand();
        
        createCommandToToolBarNameMap();
        
        EventMediator.registerListener(this);
    }
    
    protected boolean listeningForEvent(UserPreferenceEvent event) {
        
        return (event.getEventType() == UserPreferenceEvent.ALL
                || event.getEventType() == UserPreferenceEvent.TOOL_BAR);
    }

    protected void addActionForMenuItem(JCheckBoxMenuItem menuItem) {

        menuItem.addActionListener(menuItemListener);
    }
    
    protected void setMenuItemValue(JCheckBoxMenuItem menuItem) {

        String actionCommand = menuItem.getActionCommand();
     
        if (actionCommand != null && 
                actionCommandToToolBarNameMap.containsKey(actionCommand)) {

            menuItem.setSelected(toolBarVisible(actionCommand));
        }
        
    }

    private boolean toolBarVisible(String key) {

        return ToolBarProperties.isToolBarVisible(
                actionCommandToToolBarNameMap.get(key));
    }

    private Map<String, String> actionCommandToToolBarNameMap; 
    
    private void createCommandToToolBarNameMap() {
        
        actionCommandToToolBarNameMap = new HashMap<String, String>();

        actionCommandToToolBarNameMap.put("viewFileTools", ToolBarManager.FILE_TOOLS);
        actionCommandToToolBarNameMap.put("viewEditTools", ToolBarManager.EDIT_TOOLS);
        actionCommandToToolBarNameMap.put("viewSearchTools", ToolBarManager.SEARCH_TOOLS);
        actionCommandToToolBarNameMap.put("viewDatabaseTools", ToolBarManager.DATABASE_TOOLS);
        actionCommandToToolBarNameMap.put("viewBrowserTools", ToolBarManager.BROWSER_TOOLS);
        actionCommandToToolBarNameMap.put("viewImportExportTools", ToolBarManager.IMPORT_EXPORT_TOOLS);
        actionCommandToToolBarNameMap.put("viewSystemTools", ToolBarManager.SYSTEM_TOOLS);
        
    }

}










