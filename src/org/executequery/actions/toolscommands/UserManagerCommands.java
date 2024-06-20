package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.UserManagerPanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Created by Mikhail Kalyashin on 09.02.2017
 */
public class UserManagerCommands extends OpenFrameCommand implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {
        boolean hasActiveConnection = false;

        Repository repo = RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
        if (repo instanceof DatabaseConnectionRepository) {
            List<DatabaseConnection> listConnections = ((DatabaseConnectionRepository) repo).findAll();
            hasActiveConnection = listConnections.stream().anyMatch(DatabaseConnection::isConnected);
        }

        if (!hasActiveConnection) {
            GUIUtilities.displayErrorMessage(Bundles.get("GrantManagerPanel.message.notConnected"));
            return;
        }

        if (GUIUtilities.getCentralPane(UserManagerPanel.TITLE) != null) {
            GUIUtilities.setSelectedCentralPane(UserManagerPanel.TITLE);
            return;
        }

        GUIUtilities.addCentralPane(
                UserManagerPanel.TITLE,
                UserManagerPanel.FRAME_ICON,
                new UserManagerPanel(),
                null,
                true
        );
    }

}
