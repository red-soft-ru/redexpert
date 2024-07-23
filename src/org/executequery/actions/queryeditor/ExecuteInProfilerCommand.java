package org.executequery.actions.queryeditor;

import java.awt.event.ActionEvent;

/**
 * @author Alexey Kozlov
 */
public class ExecuteInProfilerCommand extends AbstractQueryEditorCommand {

    @Override
    public void execute(ActionEvent e) {
        if (isQueryEditorTheCentralPanel())
            queryEditor().executeInProfiler(null);
    }
}
