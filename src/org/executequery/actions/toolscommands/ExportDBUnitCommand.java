/*
 * ExportDBUnitCommand.java
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

package org.executequery.actions.toolscommands;

import java.awt.event.ActionEvent;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.importexport.ExportAsDBUnitWizard;
import org.underworldlabs.swing.actions.BaseCommand;

/** 
 * Execution for Export Excel
 *
 * @author   Takis Diakoumis
 */
public class ExportDBUnitCommand extends OpenFrameCommand
                                implements BaseCommand {
    
    public void execute(ActionEvent e) {
        if (!isConnected()) {
            return;
        }
        
        if (isActionableDialogOpen()) {
            
            GUIUtilities.actionableDialogToFront();
            return;
        }

        if (!isDialogOpen(ExportAsDBUnitWizard.TITLE)) {
            
            GUIUtilities.showWaitCursor();
            
            try {
            
                BaseDialog dialog = createDialog(ExportAsDBUnitWizard.TITLE, false, false);

                ExportAsDBUnitWizard panel = null;
                if (e.getSource() instanceof DatabaseTable) {

                    panel = new ExportAsDBUnitWizard(dialog, (DatabaseTable) e.getSource());

                } else {
                
                    panel = new ExportAsDBUnitWizard(dialog);
                }
                
                dialog.addDisplayComponent(panel);
                dialog.display();
                
            } finally {
                
                GUIUtilities.showNormalCursor();
            }
        }

    }
    
}

