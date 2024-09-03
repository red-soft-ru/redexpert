package org.executequery.actions.databasecommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.connections.ImportConnectionsDBPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class ImportConnectionsFromDBCommand extends OpenFrameCommand implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        if (!isConnected())
            return;

        GUIUtilities.showWaitCursor();
        try {
            BaseDialog dialog = new BaseDialog(ImportConnectionsDBPanel.TITLE, true);
            ImportConnectionsDBPanel panel = new ImportConnectionsDBPanel(dialog);

            dialog.addDisplayComponentWithEmptyBorder(panel);
            dialog.display();

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

}
