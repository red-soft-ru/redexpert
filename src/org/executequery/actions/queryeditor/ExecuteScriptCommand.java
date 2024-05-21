package org.executequery.actions.queryeditor;

import java.awt.event.ActionEvent;

public class ExecuteScriptCommand extends AbstractQueryEditorCommand {

    @Override
    public void execute(ActionEvent e) {
        if (isQueryEditorTheCentralPanel())
            queryEditor().executeScript(null);
    }

}
