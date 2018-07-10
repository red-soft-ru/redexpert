package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.TraceManagerPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class TraceManagerCommands extends OpenFrameCommand implements BaseCommand {

    public void execute(ActionEvent e) {

        if (GUIUtilities.getCentralPane(TraceManagerPanel.TITLE) == null)
            GUIUtilities.addCentralPane(TraceManagerPanel.TITLE,
                    (Icon) null,
                    new TraceManagerPanel(),
                    null,
                    true);
        else GUIUtilities.setSelectedCentralPane(TraceManagerPanel.TITLE);
    }
}
