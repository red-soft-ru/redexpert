package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.TraceManagerPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class TraceManagerCommands extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        String title = TraceManagerPanel.TITLE;
        if (isCentralPaneOpen(title))
            return;

        GUIUtilities.addCentralPane(
                title,
                (Icon) null,
                new TraceManagerPanel(),
                null,
                true
        );
    }

}
