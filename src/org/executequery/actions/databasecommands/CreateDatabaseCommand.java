package org.executequery.actions.databasecommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.CreateDatabasePanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

/**
 * Created by vasiliy.yashkov on 10.07.2015.
 */
public class CreateDatabaseCommand extends OpenFrameCommand implements BaseCommand {

    public void execute(ActionEvent e) {

        GUIUtilities.addCentralPane(CreateDatabasePanel.TITLE,
                CreateDatabasePanel.FRAME_ICON,
                new CreateDatabasePanel(null),
                null,
                true);
    }
}
