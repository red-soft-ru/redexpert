/*
 * ToolBarManager.java
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

package org.executequery.toolbars;

import org.executequery.EventMediator;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;
import org.executequery.util.ThreadUtils;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.swing.toolbar.DefaultToolBarManager;
import org.underworldlabs.util.SystemProperties;

/**
 * @author Takis Diakoumis
 */
public class ToolBarManager extends DefaultToolBarManager
        implements UserPreferenceListener {

    private static final String TOOLBARS_XML = "toolbars.xml";

    /**
     * Reference to the file tool bar
     */
    public static final String FILE_TOOLS = "File Tools";

    /**
     * Reference to the edit tool bar
     */
    public static final String EDIT_TOOLS = "Edit Tools";

    /**
     * Reference to the file tool bar
     */
    public static final String SEARCH_TOOLS = "Search Tools";

    /**
     * Reference to the database tool bar
     */
    public static final String DATABASE_TOOLS = "Database Tools";

    /**
     * Reference to the database tool bar
     */
    public static final String BROWSER_TOOLS = "Browser Tools";

    /**
     * Reference to the import/export tool bar
     */
    public static final String IMPORT_EXPORT_TOOLS = "Import/Export Tools";

    /**
     * Reference to the file tool bar
     */
    public static final String SYSTEM_TOOLS = "System Tools";

    private static final String DEFINITION_FILE;

    static {

        UserSettingsProperties settings = new UserSettingsProperties();

        DEFINITION_FILE = settings.getUserSettingsDirectory() + TOOLBARS_XML;
    }

    public ToolBarManager() {

        super(DEFINITION_FILE,
                SystemProperties.getProperty("system", "toolbars.defaults"));

        try {
            buildToolbars(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventMediator.registerListener(this);

    }

    /**
     * Builds (or rebuilds) the tool bars for the current application.
     *
     * @param rebuild - whether this is a rebuild of an existing tool bar
     */
    public void buildToolbars(boolean rebuild) {

        if (rebuild) {

            reset();
        }
        initToolBar();

        buildToolBar(FILE_TOOLS, rebuild);
        buildToolBar(EDIT_TOOLS, rebuild);
        buildToolBar(SEARCH_TOOLS, rebuild);
        buildToolBar(DATABASE_TOOLS, rebuild);
        buildToolBar(IMPORT_EXPORT_TOOLS, rebuild);
        buildToolBar(SYSTEM_TOOLS, rebuild);

        if (rebuild) {

            fireToolbarsChanged();
        }

    }

    protected void fireToolbarsChanged() {

        super.fireToolbarsChanged();

        EventMediator.fireEvent(new DefaultToolBarEvent(
                this, ToolBarEvent.TOOL_BAR_CHANGED, ToolBarEvent.DEFAULT_KEY));
    }

    public void preferencesChanged(UserPreferenceEvent event) {

        if (event.getEventType() == UserPreferenceEvent.ALL
                || event.getEventType() == UserPreferenceEvent.TOOL_BAR) {

            ThreadUtils.invokeLater(

                    new Runnable() {

                        public void run() {

                            buildToolbars(true);
                        }
                    }

            );

        }

    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof UserPreferenceEvent);
    }

}











