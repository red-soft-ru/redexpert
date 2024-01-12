package org.executequery.actions.toolscommands;

import java.awt.event.ActionEvent;

public class ExportDbMetadataCommands extends ComparerDBCommands {
    @Override
    public void execute(ActionEvent e) {
        exportMetadata(null);
    }
}
