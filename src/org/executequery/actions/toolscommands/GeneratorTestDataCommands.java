package org.executequery.actions.toolscommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.GeneratorTestDataPanel;
import org.executequery.gui.browser.GrantManagerPanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class GeneratorTestDataCommands extends OpenFrameCommand implements BaseCommand {

    public void execute(ActionEvent e) {
        boolean execute_w = false;
        List<DatabaseConnection> listConnections = ((DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)).findAll();
        for (DatabaseConnection dc : listConnections) {
            if (dc.isConnected()) {
                execute_w = true;
                break;
            }
        }
        if (execute_w) {
            if (GUIUtilities.getCentralPane(GeneratorTestDataPanel.TITLE) == null)
                GUIUtilities.addCentralPane(GeneratorTestDataPanel.TITLE,
                        (Icon) null,
                        new GeneratorTestDataPanel(),
                        null,
                        true);
            else GUIUtilities.setSelectedCentralPane(GeneratorTestDataPanel.TITLE);
        } else
            GUIUtilities.displayErrorMessage(Bundles.get(GrantManagerPanel.class, "message.notConnected"));
    }
}