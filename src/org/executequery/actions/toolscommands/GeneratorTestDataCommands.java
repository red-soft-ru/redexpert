package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.GeneratorTestDataPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class GeneratorTestDataCommands extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        String title = GeneratorTestDataPanel.TITLE;
        if (!isConnected() || isCentralPaneOpen(title))
            return;

        GUIUtilities.addCentralPane(
                title,
                GeneratorTestDataPanel.FRAME_ICON,
                new GeneratorTestDataPanel(),
                null,
                true
        );
    }

}
