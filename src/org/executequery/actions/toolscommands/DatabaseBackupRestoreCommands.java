package org.executequery.actions.toolscommands;

import java.awt.event.ActionEvent;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.browser.DatabaseBackupRestorePanel;
import org.underworldlabs.swing.actions.BaseCommand;

/**
 * @author Maxim Kozhinov
 */
public class DatabaseBackupRestoreCommands extends OpenFrameCommand implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        String title = DatabaseBackupRestorePanel.TITLE;
        if (isCentralPaneOpen(title)) {
            return;
        }

        GUIUtilities.addCentralPane(
                title,
                DatabaseBackupRestorePanel.BACKUP_ICON,
                new DatabaseBackupRestorePanel(),
                null,
                true
        );
    }

    public void openTab() {
        String title = DatabaseBackupRestorePanel.TITLE;
        if (isCentralPaneOpen(title)) {
            return;
        }

        GUIUtilities.addCentralPane(
                title,
                DatabaseBackupRestorePanel.BACKUP_ICON,
                new DatabaseBackupRestorePanel(),
                null,
                true
        );
    }
}