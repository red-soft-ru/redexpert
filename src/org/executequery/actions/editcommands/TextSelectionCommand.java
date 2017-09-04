/*
 * TextSelectionCommand.java
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

package org.executequery.actions.editcommands;

import java.awt.event.ActionEvent;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.actions.BaseCommand;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.executequery.gui.text.TextEditor;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class TextSelectionCommand extends ReflectiveAction 
                                  implements BaseCommand {
    
    public void execute(ActionEvent e) {

        actionPerformed(e);
    }

    public void selectAll(ActionEvent e) {
        
        if (hasTextFunctionInFocus()) {

            textFunction().selectAll();
        }
        
    }

    public void selectNone(ActionEvent e) {

        if (hasTextFunctionInFocus()) {

            textFunction().selectNone();
        }
        
    }

    public void insertAfter(ActionEvent e) {

        if (hasTextFunctionInFocus()) {

            textFunction().insertLineAfter();
        }

    }

    public void insertBefore(ActionEvent e) {
        
        if (hasTextFunctionInFocus()) {

            textFunction().insertLineBefore();
        }

    }

    public void insertFromFile(ActionEvent e) {
        
        if (hasTextFunctionInFocus()) {

            textFunction().insertFromFile();
        }

    }

    public void deleteWord(ActionEvent e) {

        if (hasTextFunctionInFocus()) {

            textFunction().deleteWord();
        }

    }

    public void deleteSelection(ActionEvent e) {

        if (hasTextFunctionInFocus()) {

            textFunction().deleteSelection();
        }
        
    }
    
    public void deleteLine(ActionEvent e) {

        if (hasTextFunctionInFocus()) {

            textFunction().deleteLine();
        }

    }

    public void toLowerCase(ActionEvent e) {
        
        if (hasTextFunctionInFocus()) {

            textFunction().changeSelectionCase(false);
        }

    }

    public void toUpperCase(ActionEvent e) {

        if (hasTextFunctionInFocus()) {

            textFunction().changeSelectionCase(true);
        }

    }

    private boolean hasTextFunctionInFocus() {
        
        return (textFunction() != null);
    }
    
    private TextEditor textFunction() {

        return (TextEditor)GUIUtilities.getTextEditorInFocus();
    }

}









