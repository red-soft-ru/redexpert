package org.executequery.actions.databasecommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.databaseobjects.CreateGeneratorPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class CreateSequenceCommand extends OpenFrameCommand implements BaseCommand {
    @Override
    public void execute(ActionEvent e) {
        if (!isConnected()) {
            return;
        }

        if (isActionableDialogOpen()) {
            GUIUtilities.actionableDialogToFront();
            return;
        }

        if (!isDialogOpen(CreateGeneratorPanel.CREATE_TITLE)) {
            try {
                GUIUtilities.showWaitCursor();
                BaseDialog dialog =
                        createDialog(CreateGeneratorPanel.CREATE_TITLE, false);
                CreateGeneratorPanel panel = new CreateGeneratorPanel(null, dialog);
                dialog.addDisplayComponentWithEmptyBorder(panel);
                dialog.display();
            } finally {
                GUIUtilities.showNormalCursor();
            }
        }
    }
}
