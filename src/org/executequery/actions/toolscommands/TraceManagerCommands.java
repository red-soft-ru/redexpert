package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.TraceManagerPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class TraceManagerCommands extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {
        GUIUtilities.addCentralPane(
                TraceManagerPanel.TITLE,
                TraceManagerPanel.FRAME_ICON,
                new TraceManagerPanel(),
                null,
                true
        );
    }

}
