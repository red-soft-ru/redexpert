package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.GrantManagerPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class GrantManagerCommands extends OpenFrameCommand implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        String title = GrantManagerPanel.TITLE;
        if (!isConnected() || isCentralPaneOpen(title))
            return;

        GUIUtilities.addCentralPane(
                title,
                GrantManagerPanel.FRAME_ICON,
                new GrantManagerPanel(),
                null,
                true
        );
    }

}
