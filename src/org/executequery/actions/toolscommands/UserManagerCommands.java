package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.UserManagerPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

/**
 * Created by Mikhail Kalyashin on 09.02.2017
 */
public class UserManagerCommands extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        String title = UserManagerPanel.TITLE;
        if (!isConnected() || isCentralPaneOpen(title))
            return;

        GUIUtilities.addCentralPane(
                title,
                UserManagerPanel.FRAME_ICON,
                new UserManagerPanel(),
                null,
                true
        );
    }

}
