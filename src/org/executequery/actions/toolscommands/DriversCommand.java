package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.drivers.DriversTreePanel;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DriversCommand extends OpenFrameCommand
        implements BaseCommand {

    public void execute(ActionEvent e) {
        GUIUtilities.addCentralPane(DriversTreePanel.TITLE,
                (Icon) null,
                new DriversTreePanel(),
                null,
                true);
    }

}