package org.executequery.actions.databasecommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.ImportDataFromFilePanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

/**
 * Execution for Import data into DB from local files
 *
 * @author Alexey Kozlov
 */

public class ImportDataFromFileCommand extends OpenFrameCommand
        implements BaseCommand {


    public void execute(ActionEvent e) {

        if (!isConnected()) {
            return;
        }

        if (isActionableDialogOpen()) {
            GUIUtilities.actionableDialogToFront();
            return;
        }

        if (!isDialogOpen(ImportDataFromFilePanel.TITLE)) {

            try {

                GUIUtilities.showWaitCursor();

                GUIUtilities.addCentralPane(ImportDataFromFilePanel.TITLE,
                        ImportDataFromFilePanel.FRAME_ICON,
                        new ImportDataFromFilePanel(),
                        null,
                        true);

            } finally {

                GUIUtilities.showNormalCursor();
            }

        }

    }

}






