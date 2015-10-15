/*
 * PropertiesToolBarGeneral.java
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

package org.executequery.gui.prefs;


import java.util.ArrayList;
import java.util.List;

import org.underworldlabs.swing.toolbar.ToolBarProperties;
import org.executequery.toolbars.ToolBarManager;


/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/** <p>The tool bar general properties panel.
 *
 *  @author   Takis Diakoumis
 * @version  $Revision: 1512 $
 * @date     $Date: 2015-09-27 21:23:07 +1000 (Sun, 27 Sep 2015) $
 */
public class PropertiesToolBarGeneral extends AbstractPropertiesBasePanel {
   
    private SimplePreferencesPanel preferencesPanel;
    
    /** <p>Constructs a new instance. */
    public PropertiesToolBarGeneral() {       
        try {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** <p>Initializes the state of this instance. */
    private void init() throws Exception {

    	List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                    UserPreference.CATEGORY_TYPE,
                    null,
                    "Visibility",
                    null));

        String key = ToolBarManager.FILE_TOOLS;
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "File tool bar",
                    new Boolean(ToolBarProperties.getToolBar(key).isVisible())));

        key = ToolBarManager.EDIT_TOOLS;
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Edit tool bar",
                    new Boolean(ToolBarProperties.getToolBar(key).isVisible())));

        key = ToolBarManager.SEARCH_TOOLS;
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Search tool bar",
                    new Boolean(ToolBarProperties.getToolBar(key).isVisible())));

        key = ToolBarManager.DATABASE_TOOLS;
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Database tool bar",
                    new Boolean(ToolBarProperties.getToolBar(key).isVisible())));

        key = ToolBarManager.BROWSER_TOOLS;
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Browser tool bar",
                    new Boolean(ToolBarProperties.getToolBar(key).isVisible())));

        key = ToolBarManager.IMPORT_EXPORT_TOOLS;
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Import/Export tool bar",
                    new Boolean(ToolBarProperties.getToolBar(key).isVisible())));

        key = ToolBarManager.SYSTEM_TOOLS;
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "System tool bar",
                    new Boolean(ToolBarProperties.getToolBar(key).isVisible())));
/*
        key = ToolBarManager.WINDOW_TOOLS;
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Open windows tool bar",
                    new Boolean(ToolBarProperties.getToolBar(key).isVisible())));

        key = ToolBarManager.COMPONENT_TOOLS;
        list.add(new UserPreference(
                    UserPreference.BOOLEAN_TYPE,
                    key,
                    "Components tool bar",
                    new Boolean(ToolBarProperties.getToolBar(key).isVisible())));
*/

        UserPreference[] preferences = 
                (UserPreference[])list.toArray(new UserPreference[list.size()]);
        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);
    }

    public void restoreDefaults() {
        preferencesPanel.restoreDefaults();
    }

    public void save() {
        UserPreference[] preferences = preferencesPanel.getPreferences();
        for (int i = 0; i < preferences.length; i++) {
            if (preferences[i].getType() != UserPreference.CATEGORY_TYPE) {
                boolean value = Boolean.valueOf(
                        preferences[i].getValue().toString()).booleanValue();
                ToolBarProperties.getToolBar(
                        preferences[i].getKey()).setVisible(value);
            }
        }
    }

}














