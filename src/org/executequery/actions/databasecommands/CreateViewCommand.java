package org.executequery.actions.databasecommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.databaseobjects.CreateViewPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class CreateViewCommand extends OpenFrameCommand implements BaseCommand {
    @Override
    public void execute(ActionEvent e) {
        if (!isConnected()) {
            return;
        }


        if (isActionableDialogOpen()) {
            GUIUtilities.actionableDialogToFront();
            return;
        }

        if (!isDialogOpen(CreateViewPanel.TITLE)) {
            try {
                GUIUtilities.showWaitCursor();
                BaseDialog dialog =
                        createDialog(CreateViewPanel.TITLE, false);
                CreateViewPanel panel = new CreateViewPanel(null, dialog);
                dialog.addDisplayComponentWithEmptyBorder(panel);
                dialog.display();
            } finally {
                GUIUtilities.showNormalCursor();
            }
        }
    }
}

