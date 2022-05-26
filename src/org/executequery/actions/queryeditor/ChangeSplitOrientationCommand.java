package org.executequery.actions.queryeditor;

import java.awt.event.ActionEvent;

public class ChangeSplitOrientationCommand extends AbstractQueryEditorCommand {
    public void execute(ActionEvent e) {

        if (isQueryEditorTheCentralPanel()) {

            queryEditor().changeOrientationSplit();
        }

    }
}