/*
 * CreateTableCommand.java
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

import org.executequery.actions.OpenFrameCommand;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

/**
 * Executes the create table action creating an
 * instance of CreateTablePanel and adding this within
 * an internal frame to the desktop.
 *
 * @author Takis Diakoumis
 */
public class CreateTableCommand extends OpenFrameCommand
        implements BaseCommand {

    /**
     * Performs the execution of this action.
     *
     * @param e originating <code>ActionEvent</code>
     */
    public void execute(ActionEvent e) {

        /*if (!isConnected()) {
            return;
        }


        if (isActionableDialogOpen()) {
            GUIUtilities.acionableDialogToFront();
            return;
        }

        if (!isDialogOpen(CreateTablePanel.TITLE)) {
            try {
                GUIUtilities.showWaitCursor();
                BaseDialog dialog =
                        createDialog(CreateTablePanel.TITLE, false);
                CreateTablePanel panel = new CreateTablePanel(dialog, false);
                dialog.addDisplayComponentWithEmptyBorder(panel);
                dialog.display();
            } finally {
                GUIUtilities.showNormalCursor();
            }
        }*/
    }

}





