package org.executequery.actions.queryeditor;

import java.awt.event.ActionEvent;

public class PrintExplainedPlanCommand extends AbstractQueryEditorCommand {
    public void execute(ActionEvent e) {

        if (isQueryEditorTheCentralPanel()) {

            queryEditor().printExecutedPlan(true);
        }

    }
}