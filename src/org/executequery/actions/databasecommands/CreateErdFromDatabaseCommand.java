/*
 * CreateErdFromDatabaseCommand.java
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
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.GenerateErdPanel;
import org.executequery.gui.browser.GrantManagerPanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class CreateErdFromDatabaseCommand extends OpenFrameCommand
        implements BaseCommand {

    public void execute(ActionEvent e) {

        boolean execute_w = false;
        List<DatabaseConnection> listConnections = ((DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)).findAll();
        for (DatabaseConnection dc : listConnections) {
            if (dc.isConnected()) {
                execute_w = true;
                break;
            }
        }
        if (execute_w) {


            if (isActionableDialogOpen()) {
                GUIUtilities.actionableDialogToFront();
                return;
            }

            if (!isDialogOpen(GenerateErdPanel.TITLE)) {
                try {
                    GUIUtilities.showWaitCursor();
                    BaseDialog dialog = createDialog(GenerateErdPanel.TITLE, false);
                    GenerateErdPanel panel = new GenerateErdPanel(dialog);
                    dialog.addDisplayComponentWithEmptyBorder(panel);
                    dialog.setResizable(false);
                    dialog.display();
                } finally {
                    GUIUtilities.showNormalCursor();
                }
            }
        } else
            GUIUtilities.displayErrorMessage(Bundles.get(GrantManagerPanel.class, "message.notConnected"));


    }

}















