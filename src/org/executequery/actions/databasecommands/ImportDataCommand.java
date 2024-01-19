package org.executequery.actions.databasecommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.importData.ImportDataPanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

/**
 * Execution for Import data into DB from local files
 *
 * @author Alexey Kozlov
 */

public class ImportDataCommand extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {

        if (GUIUtilities.getCentralPane(ImportDataPanel.TITLE) == null) {
            GUIUtilities.addCentralPane(ImportDataPanel.TITLE,
                    ImportDataPanel.FRAME_ICON,
                    new ImportDataPanel(),
                    null,
                    true
            );
        } else
            GUIUtilities.setSelectedCentralPane(ImportDataPanel.TITLE);
    }

}
