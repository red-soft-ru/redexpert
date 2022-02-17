package org.executequery.actions.searchcommands;


import org.executequery.gui.FindReplaceDialog;
import org.executequery.gui.text.TextEditor;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class FindAction extends AbstractFindReplaceAction {


    public FindAction(TextEditor textEditor) {
        super(textEditor, FindReplaceDialog.FIND);
    }

    protected KeyStroke keyStrokeForAction() {

        return KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
    }

}
