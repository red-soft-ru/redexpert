package org.executequery.actions.queryeditor;

import java.awt.event.ActionEvent;

public class ExecuteScriptCommand extends AbstractQueryEditorCommand {

    public void execute(ActionEvent e) {

        if (isQueryEditorTheCentralPanel()) {

            queryEditor().executeSQLScript(null);
        }

    }

}