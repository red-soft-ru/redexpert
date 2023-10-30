package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.DatabaseStatisticPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DatabaseStatisticCommands extends OpenFrameCommand implements BaseCommand {

    public void execute(ActionEvent e) {
        GUIUtilities.addCentralPane(DatabaseStatisticPanel.TITLE,
                (Icon) null,
                new DatabaseStatisticPanel(),
                null,
                true);
    }
}