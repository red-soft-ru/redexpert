/*
 * SystemPropertiesDockedTab.java
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

package org.executequery.gui;

import java.awt.BorderLayout;
import javax.swing.JTabbedPane;
import org.underworldlabs.swing.HeapMemoryPanel;

/**
 * System properties docked component.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class SystemPropertiesDockedTab extends AbstractDockedTabActionPanel {
    
    public static final String TITLE = "System Properties";
    
    /** the system properties panel */
    private SystemPropertiesPanel propertiesPanel;
    
    /** the heap resources panel */
    private HeapMemoryPanel resourcesPanel;
    
    /** Creates a new instance of SystemPropertiesDockedTab */
    public SystemPropertiesDockedTab() {
        super(new BorderLayout());
        init();
    }
    
    private void init() {
        propertiesPanel = new SystemPropertiesPanel();
        resourcesPanel = new HeapMemoryPanel();
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("System", propertiesPanel);
        tabs.add("Resources", resourcesPanel);
        
        add(tabs, BorderLayout.CENTER);
    }

    /**
     * Override to clean up the mem thread.
     */
    public boolean tabViewClosing() {
        resourcesPanel.stopTimer();
        return true;
    }

    /**
     * Override to make sure the timer has started.
     */
    public boolean tabViewSelected() {
        propertiesPanel.reload();
        resourcesPanel.startTimer();
        return true;
    }

    // ----------------------------------------
    // DockedTabView Implementation
    // ----------------------------------------

    public static final String MENU_ITEM_KEY = "viewSystemProperties";
    
    public static final String PROPERTY_KEY = "system.display.systemprops";
    
    /**
     * Returns the display title for this view.
     *
     * @return the title displayed for this view
     */
    public String getTitle() {
        return TITLE;
    }

    /**
     * Returns the name defining the property name for this docked tab view.
     *
     * @return the key
     */
    public String getPropertyKey() {
        return PROPERTY_KEY;
    }

    /**
     * Returns the name defining the menu cache property
     * for this docked tab view.
     *
     * @return the preferences key
     */
    public String getMenuItemKey() {
        return MENU_ITEM_KEY;
    }

}




