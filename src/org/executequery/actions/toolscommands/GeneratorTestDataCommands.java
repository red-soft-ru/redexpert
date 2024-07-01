package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.GeneratorTestDataPanel;
import org.executequery.gui.browser.GrantManagerPanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;
import java.util.List;

public class GeneratorTestDataCommands extends OpenFrameCommand implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {
        boolean hasActiveConnection = false;

        Repository repo = RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
        if (repo instanceof DatabaseConnectionRepository) {
            List<DatabaseConnection> listConnections = ((DatabaseConnectionRepository) repo).findAll();
            hasActiveConnection = listConnections.stream().anyMatch(DatabaseConnection::isConnected);
        }

        if (!hasActiveConnection) {
            GUIUtilities.displayErrorMessage(Bundles.get(GrantManagerPanel.class, "message.notConnected"));
            return;
        }

        String frameTitle = GeneratorTestDataPanel.TITLE;
        if (GUIUtilities.getCentralPane(frameTitle) != null) {
            GUIUtilities.setSelectedCentralPane(frameTitle);
            return;
        }

        GUIUtilities.addCentralPane(
                frameTitle,
                GeneratorTestDataPanel.FRAME_ICON,
                new GeneratorTestDataPanel(),
                null,
                true
        );
    }

}
