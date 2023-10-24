package org.executequery.actions.queryeditor;

import java.awt.event.ActionEvent;

public class ExecuteInAnyConnectionsCommand extends AbstractQueryEditorCommand {

    public void execute(ActionEvent e) {

        if (isQueryEditorTheCentralPanel()) {

            queryEditor().executeSQLQueryInAnyConnections(null);
        }

    }

}
