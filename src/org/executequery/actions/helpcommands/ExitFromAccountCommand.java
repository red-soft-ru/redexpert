package org.executequery.actions.helpcommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.actions.BaseCommand;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitFromAccountCommand extends OpenFrameCommand implements BaseCommand {
    @Override
    public void execute(ActionEvent e) {
        if (GUIUtilities.displayConfirmDialog(Bundles.get("GUIUtilities.want-exit")) == JOptionPane.YES_OPTION) {
            SystemProperties.setStringProperty("user", "reddatabase.token", "");
            GUIUtilities.loadAuthorisationInfo();
        }
    }
}
