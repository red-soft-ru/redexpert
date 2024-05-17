/*
 * ViewOptionsCommand.java
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

import org.executequery.GUIUtilities;
import org.executequery.gui.SystemOutputPanel;
import org.executequery.gui.browser.ConnectionsTreePanel;

import java.awt.event.ActionEvent;

/**
 * @author Takis Diakoumis
 */
public class ViewOptionsCommand extends AbstractViewOptionsCommand {

    @SuppressWarnings("unused")
    public void viewStatusBar(ActionEvent e) {
        GUIUtilities.displayStatusBar(selectionFromEvent(e));
    }

    @SuppressWarnings("unused")
    public void viewConsole(ActionEvent e) {
        displayDockedComponent(e, SystemOutputPanel.PROPERTY_KEY);
    }

    @SuppressWarnings("unused")
    public void viewConnections(ActionEvent e) {
        displayDockedComponent(e, ConnectionsTreePanel.PROPERTY_KEY);
    }

    private void displayDockedComponent(ActionEvent e, String key) {
        GUIUtilities.displayDockedComponent(key, selectionFromEvent(e));
    }

}
