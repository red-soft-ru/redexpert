package org.executequery.actions.toolscommands;

import java.awt.event.ActionEvent;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.DatabaseBackupRestorePanel;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;

/**
 * @author Maxim Kozhinov
 */
public class DatabaseBackupRestoreCommands extends OpenFrameCommand implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        String title = DatabaseBackupRestorePanel.TITLE;
        if (isCentralPaneOpen(title))
            return;

        GUIUtilities.addCentralPane(
                title,
                DatabaseBackupRestorePanel.BACKUP_ICON,
                new DatabaseBackupRestorePanel(),
                null,
                true
        );
    }

    public void execute(DatabaseConnection dc) {
        execute((ActionEvent) null);

        JPanel panel = GUIUtilities.getCentralPane(DatabaseBackupRestorePanel.TITLE);
        if (panel instanceof DatabaseBackupRestorePanel)
            ((DatabaseBackupRestorePanel) panel).setSelectedConnection(dc);
    }

}