/*
 * ToolBarViewOptionsCommand.java
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

package org.executequery.actions.viewcommands;

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.event.DefaultUserPreferenceEvent;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.toolbars.ToolBarManager;
import org.underworldlabs.swing.toolbar.ToolBarProperties;

import java.awt.event.ActionEvent;

/**
 * @author Takis Diakoumis
 */
public class ToolBarViewOptionsCommand extends AbstractViewOptionsCommand {

    public void viewBrowserTools(ActionEvent e) {

        setToolBarVisible(ToolBarManager.BROWSER_TOOLS, selectionFromEvent(e));
    }

    public void viewFileTools(ActionEvent e) {

        setToolBarVisible(ToolBarManager.FILE_TOOLS, selectionFromEvent(e));
    }

    public void viewEditTools(ActionEvent e) {

        setToolBarVisible(ToolBarManager.EDIT_TOOLS, selectionFromEvent(e));
    }

    public void viewSearchTools(ActionEvent e) {

        setToolBarVisible(ToolBarManager.SEARCH_TOOLS, selectionFromEvent(e));
    }

    public void viewDatabaseTools(ActionEvent e) {

        setToolBarVisible(ToolBarManager.DATABASE_TOOLS, selectionFromEvent(e));
    }

    public void viewImportExportTools(ActionEvent e) {

        setToolBarVisible(ToolBarManager.IMPORT_EXPORT_TOOLS, selectionFromEvent(e));
    }

    public void viewSystemTools(ActionEvent e) {

        setToolBarVisible(ToolBarManager.SYSTEM_TOOLS, selectionFromEvent(e));
    }

    private void setToolBarVisible(String name, boolean visible) {

        ToolBarProperties.setToolBarVisible(name, visible);
        GUIUtilities.resetToolBar();

        EventMediator.fireEvent(
                new DefaultUserPreferenceEvent(
                        this, name, UserPreferenceEvent.TOOL_BAR));
    }

}











