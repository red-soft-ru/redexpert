package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.drivers.DriversPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class DriversCommand extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {
        GUIUtilities.addCentralPane(
                DriversPanel.TITLE,
                DriversPanel.FRAME_ICON,
                new DriversPanel(),
                null,
                true
        );
    }

}
