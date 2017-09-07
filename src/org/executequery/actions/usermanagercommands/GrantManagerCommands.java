package org.executequery.actions.usermanagercommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.GrantManagerPanel;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;
import java.util.List;

public class GrantManagerCommands extends OpenFrameCommand implements BaseCommand {

    public void execute(ActionEvent e) {
        boolean execute_w=false;
        List<DatabaseConnection> listConnections = ((DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)).findAll();
        for (DatabaseConnection dc : listConnections) {
            if (dc.isConnected()) {
                execute_w = true;
                break;
            }
        }
        if (execute_w) {
            if (GUIUtilities.getCentralPane(GrantManagerPanel.TITLE)==null)
            GUIUtilities.addCentralPane(GrantManagerPanel.TITLE,
                    GrantManagerPanel.FRAME_ICON,
                    new GrantManagerPanel(),
                    null,
                    true);
            else GUIUtilities.setSelectedCentralPane(GrantManagerPanel.TITLE);
        }
        else GUIUtilities.displayErrorMessage("No connections available!");
    }
}
