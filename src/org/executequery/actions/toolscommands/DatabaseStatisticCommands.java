package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.DatabaseStatisticPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class DatabaseStatisticCommands extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        String title = DatabaseStatisticPanel.TITLE;
        if (isCentralPaneOpen(title))
            return;

        GUIUtilities.addCentralPane(
                title,
                DatabaseStatisticPanel.FRAME_ICON,
                new DatabaseStatisticPanel(),
                null,
                true
        );
    }

}