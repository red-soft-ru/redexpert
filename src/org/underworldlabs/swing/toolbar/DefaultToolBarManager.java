/*
 * DefaultToolBarManager.java
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

package org.underworldlabs.swing.toolbar;

import org.executequery.gui.WidgetFactory;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.swing.actions.ActionBuilder;

import java.util.*;

/**
 * Toolbar manager class for managing and creating
 * toolbars and associated components.
 *
 * @author Takis Diakoumis
 */
public class DefaultToolBarManager {

    /**
     * All toolbars added
     */
    private final Map<String, ToolBar> toolbars;

    /**
     * The toolbar base panel
     */
    private final ToolBarBase toolbarBase;

    /**
     * Creates a new instance of DefaultToolBarManager.
     * The toolbarConfig variable may be null and is usually
     * a user defined/modified file system path differing from the
     * defaults specified as a package resource XML path.
     *
     * @param toolbarConfig            users toolbarconfing file path
     * @param defaultToolsResourcePath the default XML conf resource file
     */
    public DefaultToolBarManager(String toolbarConfig, String defaultToolsResourcePath) {

        if (toolbarConfig != null)
            toolbarConfig = new UserSettingsProperties().getUserSettingsDirectory() + toolbarConfig;

        ToolBarProperties.init(toolbarConfig, defaultToolsResourcePath);

        toolbars = new HashMap<>();
        toolbarBase = new ToolBarBase(ToolBarProperties.getNextToolbarRow());
    }

    protected void initToolBar() {
        toolbarBase.removeAll();

        ToolBar toolBar = new ToolBar(toolbarBase, "Tool Bar");
        toolBar.removeAllButtons();
        toolBar.invalidate();

        toolbars.put("Tool Bar", toolBar);
        toolbarBase.addToolBar(toolBar, new ToolBarConstraints(0, 0));
    }

    public ToolBarBase getToolBarBasePanel() {
        return toolbarBase;
    }

    protected void reset() {
        toolbarBase.removeAll();
        toolbarBase.setRows(ToolBarProperties.getNextToolbarRow());
    }

    protected void fireToolbarsChanged() {
        toolbarBase.repaint();
        toolbarBase.revalidate();
    }

    @SuppressWarnings({"unchecked"})
    protected void buildToolBar(String name) {

        ToolBarWrapper wrapper = ToolBarProperties.getToolBar(name);
        if (wrapper == null || !wrapper.isVisible() || !wrapper.hasButtons())
            return;

        toolbarBase.removeAll();
        ToolBar toolBar = toolbars.get("Tool Bar");

        wrapper.getButtonsVector().stream()
                .sorted(new ButtonComparator())
                .filter(button -> ((ToolBarButton) button).isVisible())
                .forEachOrdered(button -> {

                    ToolBarButton toolbarButton = (ToolBarButton) button;
                    if (toolbarButton.isSeparator()) {
                        toolBar.addSeparator();
                        return;
                    }

                    toolBar.addButton(WidgetFactory.createRolloverButton(
                            toolbarButton.getActionId(),
                            ActionBuilder.get(toolbarButton.getActionId()),
                            toolbarButton.getName()
                    ));
                });

        toolBar.addSeparator();
        toolBar.buildToolBar();
        toolbarBase.addToolBar(toolBar, new ToolBarConstraints(0, 0));
    }

}
