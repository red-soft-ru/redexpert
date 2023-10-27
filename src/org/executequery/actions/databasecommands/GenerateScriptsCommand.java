/*
 * GenerateScriptsCommand.java
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

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.scriptgenerators.GenerateScriptsWizard;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

/**
 * <p>Execution for CREATE TABLE script generation.
 *
 * @author Takis Diakoumis
 */
public class GenerateScriptsCommand extends OpenFrameCommand
        implements BaseCommand {

    public void execute(ActionEvent e) {

        if (!isConnected()) {
            return;
        }

        if (isActionableDialogOpen()) {
            GUIUtilities.actionableDialogToFront();
            return;
        }

        if (!isDialogOpen(GenerateScriptsWizard.TITLE)) {
            try {
                GUIUtilities.showWaitCursor();
                BaseDialog dialog =
                        createDialog(GenerateScriptsWizard.TITLE, false);
                GenerateScriptsWizard panel = new GenerateScriptsWizard(dialog);
                dialog.addDisplayComponent(panel);
                dialog.display();
            } finally {
                GUIUtilities.showNormalCursor();
            }
        }
/*
        if (!isConnected()) {
            return;
        }
        else if (!isFrameOpen(GenerateScriptsPanel.TITLE)) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GUIUtilities.addInternalFrame(GenerateScriptsPanel.TITLE,
                                                  GenerateScriptsPanel.FRAME_ICON,
                                                  new GenerateScriptsPanel(),
                                                  true, false, false, false);
                }
            });
        }
*/

    }

}





