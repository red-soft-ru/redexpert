package org.executequery.actions.searchcommands;

import org.executequery.gui.BaseDialog;
import org.executequery.gui.FindReplaceDialog;
import org.executequery.gui.text.TextEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.executequery.GUIUtilities.isDialogOpen;

public abstract class AbstractFindReplaceAction extends AbstractAction {
    TextEditor textEditor;
    int findOrReplace;

    public AbstractFindReplaceAction(TextEditor textEditor, int findOrReplace) {
        super();
        this.findOrReplace = findOrReplace;
        this.textEditor = textEditor;
        putValue(Action.ACCELERATOR_KEY, keyStrokeForAction());
    }

    protected abstract KeyStroke keyStrokeForAction();

    private boolean findReplaceDialogOpen() {

        return isDialogOpen(FindReplaceDialog.TITLE);
    }


    protected final boolean canOpenDialog() {

        return (!findReplaceDialogOpen());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!canOpenDialog()) {

            return;
        }
        BaseDialog dialog = new BaseDialog(FindReplaceDialog.TITLE, false, false);
        dialog.addDisplayComponent(
                new FindReplaceDialog(dialog, findOrReplace, textEditor));
        dialog.display();
    }

}
