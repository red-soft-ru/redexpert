package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.profiler.ProfilerPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ProfilerCommand extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        if (!isConnected()) {
            return;
        }

        if (isActionableDialogOpen()) {
            GUIUtilities.actionableDialogToFront();
            return;
        }

        GUIUtilities.addCentralPane(ProfilerPanel.TITLE,
                (Icon) null,
                new ProfilerPanel(),
                null,
                true);
    }
}