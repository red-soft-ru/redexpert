package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.ComparerDBPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class ComparerDBCommands extends OpenFrameCommand implements BaseCommand {

    public void execute(ActionEvent e) {

        if (GUIUtilities.getCentralPane(ComparerDBPanel.TITLE) == null)
            GUIUtilities.addCentralPane(ComparerDBPanel.TITLE,
                    ComparerDBPanel.FRAME_ICON,
                    new ComparerDBPanel(),
                    null,
                    true);
        else GUIUtilities.setSelectedCentralPane(ComparerDBPanel.TITLE);
    }
}
