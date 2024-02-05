package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.ComparerDBPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ComparerDBCommands extends OpenFrameCommand implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        if (GUIUtilities.getCentralPane(ComparerDBPanel.TITLE) == null)
            display(new ComparerDBPanel(), ComparerDBPanel.TITLE, ComparerDBPanel.COMPARE_ICON);
        else
            GUIUtilities.setSelectedCentralPane(ComparerDBPanel.TITLE);
    }

    public void exportMetadata(DatabaseConnection connection) {
        display(new ComparerDBPanel(connection), ComparerDBPanel.TITLE_EXPORT, ComparerDBPanel.EXTRACT_ICON);
    }

    private void display(JPanel panel, String title, String icon) {
        GUIUtilities.addCentralPane(title, icon, panel, null, true);
    }

}
