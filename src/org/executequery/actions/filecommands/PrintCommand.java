/*
 * PrintCommand.java
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

package org.executequery.actions.filecommands;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import org.executequery.GUIUtilities;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.editor.PrintSelectDialog;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.print.PrintFunction;
import org.executequery.util.Printer;
import org.underworldlabs.swing.actions.BaseCommand;

/**
 * Implements the 'PRINT' command for those objects where this function is
 * available.
 * 
 * @author Takis Diakoumis
 * @version $Revision: 1780 $
 * @date $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class PrintCommand implements BaseCommand {

    public void execute(ActionEvent e) {

        if (!hasPrintableInFocus()) {
            
            return;
        }
        
        PrintFunction printFunction = null;

        try {

            printFunction = GUIUtilities.getPrintableInFocus();
            
            // if the frame in focus is a Query Editor
            // display the print selection dialog (text or table)
            if (printFunction instanceof QueryEditor) {

                BaseDialog dialog = 
                    new BaseDialog(PrintSelectDialog.PRINT_TITLE, true, false);

                dialog.addDisplayComponent(createPanel(dialog, printFunction));
                dialog.display();

                return;
            }

            new Printer().print(printFunction);
            
        } finally {
        
            printFunction = null;
        }

    }

    private boolean hasPrintableInFocus() {

        return GUIUtilities.getPrintableInFocus() != null;
    }

    private JPanel createPanel(BaseDialog dialog, PrintFunction printFunction) {

        return new PrintSelectDialog(
                dialog, (QueryEditor) printFunction, PrintSelectDialog.PRINT);
    }

}











