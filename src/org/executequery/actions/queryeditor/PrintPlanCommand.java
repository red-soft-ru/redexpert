package org.executequery.actions.queryeditor;

import java.awt.event.ActionEvent;

public class PrintPlanCommand extends AbstractQueryEditorCommand {
    public void execute(ActionEvent e) {

        if (isQueryEditorTheCentralPanel()) {

            queryEditor().printExecutedPlan();
        }

    }
}
