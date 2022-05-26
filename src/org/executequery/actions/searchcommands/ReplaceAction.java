package org.executequery.actions.searchcommands;

import org.executequery.gui.FindReplaceDialog;
import org.executequery.gui.text.TextEditor;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ReplaceAction extends AbstractFindReplaceAction {
    public ReplaceAction(TextEditor textEditor) {
        super(textEditor, FindReplaceDialog.REPLACE);
    }

    protected KeyStroke keyStrokeForAction() {

        return KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK);
    }
}
