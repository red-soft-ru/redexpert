package org.executequery.actions.databasecommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.importFromFile.ImportDataFromFilePanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

/**
 * Execution for Import data into DB from local files
 *
 * @author Alexey Kozlov
 */

public class ImportDataFromFileCommand extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        if (GUIUtilities.getCentralPane(ImportDataFromFilePanel.TITLE) == null) {
            GUIUtilities.addCentralPane(ImportDataFromFilePanel.TITLE,
                    ImportDataFromFilePanel.FRAME_ICON,
                    new ImportDataFromFilePanel(),
                    null,
                    true
            );
        } else
            GUIUtilities.setSelectedCentralPane(ImportDataFromFilePanel.TITLE);
    }

}
