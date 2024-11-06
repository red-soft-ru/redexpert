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
import org.executequery.gui.editor.history.QueryEditorsManager;
import org.executequery.log.Log;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.swing.toolbar.DefaultToolBarManager;
import org.underworldlabs.swing.toolbar.ToolBarProperties;
import org.underworldlabs.util.SystemProperties;

/**
 * @author Takis Diakoumis
 */
public class ToolBarManager extends DefaultToolBarManager
        implements UserPreferenceListener {

    private static final String TOOLBARS_XML = "toolbars.xml";

    public static final String DATABASE_TOOLS = "Database Tools";
    public static final String APPLICATION_TOOLS = "Application Tools";
    public static final String SYSTEM_TOOLS = "System Tools";
    public static final String QUERY_EDITOR_TOOLS = "Query Editor Tools";

    public ToolBarManager() {
        super(TOOLBARS_XML, SystemProperties.getProperty("system", "toolbars.defaults"));

        try {
            buildToolbars(false);

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }

        EventMediator.registerListener(this);
    }

    /**
     * Builds (or rebuilds) the toolbars for the current application.
     *
     * @param rebuild whether this is a rebuild of an existing toolbar
     */
    public void buildToolbars(boolean rebuild) {

        if (rebuild)
            reset();

        initToolBar();
        buildToolBar(DATABASE_TOOLS);
        buildToolBar(APPLICATION_TOOLS);
        buildToolBar(SYSTEM_TOOLS);

        setToolbarVisible(ToolBarProperties.isToolBarVisible(DATABASE_TOOLS)
                || ToolBarProperties.isToolBarVisible(APPLICATION_TOOLS)
                || ToolBarProperties.isToolBarVisible(SYSTEM_TOOLS)
        );

        if (rebuild)
            fireToolbarsChanged();
    }

    @Override
    protected void fireToolbarsChanged() {
        super.fireToolbarsChanged();
        EventMediator.fireEvent(new DefaultToolBarEvent(this, ToolBarEvent.TOOL_BAR_CHANGED, ToolBarEvent.DEFAULT_KEY));
    }

    @Override
    public void preferencesChanged(UserPreferenceEvent event) {
        int eventType = event.getEventType();
        if (eventType == UserPreferenceEvent.ALL || eventType == UserPreferenceEvent.TOOL_BAR) {
            ThreadUtils.invokeLater(() -> buildToolbars(true));
            QueryEditorsManager.rebuildToolbars();
        }
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return event instanceof UserPreferenceEvent;
    }

}
