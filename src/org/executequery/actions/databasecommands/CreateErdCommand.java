/*
 * CreateErdCommand.java
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

package org.executequery.actions.databasecommands;

import java.awt.event.ActionEvent;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.actions.BaseCommand;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.erd.ErdViewerPanel;

/** 
 * Command execution for Database | Create New ERD.
 *
 * @author   Takis Diakoumis
 */
public class CreateErdCommand extends OpenFrameCommand
                              implements BaseCommand {
    
    
    public void execute(ActionEvent e) {
        GUIUtilities.addCentralPane(ErdViewerPanel.TITLE,
                                    ErdViewerPanel.FRAME_ICON, 
                                    new ErdViewerPanel(null, null, true),
                                    null,
                                    true);
    }
   
}















