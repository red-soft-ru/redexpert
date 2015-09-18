/*
 * ViewOptionsCommand.java
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

package org.executequery.actions.viewcommands;

import java.awt.event.ActionEvent;

import org.executequery.GUIUtilities;
import org.executequery.gui.NotepadDockedPanel;
import org.executequery.gui.SystemOutputPanel;
import org.executequery.gui.SystemPropertiesDockedTab;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.drivers.DriversTreePanel;
import org.executequery.gui.keywords.KeywordsDockedPanel;
import org.executequery.gui.sqlstates.SQLStateCodesDockedPanel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ViewOptionsCommand extends AbstractViewOptionsCommand {
    
    public void viewStatusBar(ActionEvent e) {
        
        GUIUtilities.displayStatusBar(selectionFromEvent(e));
    }
    
    public void viewConsole(ActionEvent e) {

        displayDockedComponent(e, SystemOutputPanel.PROPERTY_KEY);
    }

    public void viewConnections(ActionEvent e) {

        displayDockedComponent(e, ConnectionsTreePanel.PROPERTY_KEY);
    }

    public void viewKeywords(ActionEvent e) {

        displayDockedComponent(e, KeywordsDockedPanel.PROPERTY_KEY);
    }

    public void viewSqlStateCodes(ActionEvent e) {

        displayDockedComponent(e, SQLStateCodesDockedPanel.PROPERTY_KEY);
    }

    public void viewDrivers(ActionEvent e) {

        displayDockedComponent(e, DriversTreePanel.PROPERTY_KEY);
    }

    public void viewNotepad(ActionEvent e) {

        displayDockedComponent(e, NotepadDockedPanel.PROPERTY_KEY);
    }

    public void viewSystemProperties(ActionEvent e) {

        displayDockedComponent(e, SystemPropertiesDockedTab.PROPERTY_KEY);
    }

    private void displayDockedComponent(ActionEvent e, String key) {

        GUIUtilities.displayDockedComponent(key, selectionFromEvent(e));
    }
    
}










