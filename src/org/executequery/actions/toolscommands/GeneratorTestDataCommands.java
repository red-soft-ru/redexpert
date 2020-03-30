package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.GeneratorTestDataPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GeneratorTestDataCommands extends OpenFrameCommand implements BaseCommand {

    public void execute(ActionEvent e) {

        if (GUIUtilities.getCentralPane(GeneratorTestDataPanel.TITLE) == null)
            GUIUtilities.addCentralPane(GeneratorTestDataPanel.TITLE,
                    (Icon) null,
                    new GeneratorTestDataPanel(),
                    null,
                    true);
        else GUIUtilities.setSelectedCentralPane(GeneratorTestDataPanel.TITLE);
    }
}