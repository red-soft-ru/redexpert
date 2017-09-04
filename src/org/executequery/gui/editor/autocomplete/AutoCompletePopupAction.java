/*
 * AutoCompletePopupAction.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.editor.autocomplete;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

public class AutoCompletePopupAction extends AbstractAction {

    private final AutoCompletePopupProvider autoCompletePopup; 
    
    public AutoCompletePopupAction(AutoCompletePopupProvider autoCompletePopup) {

        super();

        this.autoCompletePopup = autoCompletePopup;
        putValue(Action.ACCELERATOR_KEY, keyStrokeForAction());
    }

    private KeyStroke keyStrokeForAction() {

        return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK);
    }

    public void actionPerformed(ActionEvent e) {

        autoCompletePopup.firePopupTrigger();
    }

}






