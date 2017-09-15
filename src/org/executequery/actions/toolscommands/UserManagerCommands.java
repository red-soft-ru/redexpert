package org.executequery.actions.toolscommands;
import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.CreateDatabasePanel;
import org.executequery.gui.browser.UserManagerPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

/**
 * Created by mikhan808 on 09.02.2017.
 */
public class UserManagerCommands extends OpenFrameCommand implements BaseCommand {

    public void execute(ActionEvent e) {

        if (GUIUtilities.getCentralPane(UserManagerPanel.TITLE)==null)
        GUIUtilities.addCentralPane(UserManagerPanel.TITLE,
                UserManagerPanel.FRAME_ICON,
                new UserManagerPanel(),
                null,
                true);
        else GUIUtilities.setSelectedCentralPane(UserManagerPanel.TITLE);
    }
}



