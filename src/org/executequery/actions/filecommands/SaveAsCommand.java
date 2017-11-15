/*
 * SaveAsCommand.java
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

import org.executequery.GUIUtilities;
import org.executequery.gui.SaveFunction;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.actions.BaseCommand;

/** 
 * <p>Executes the File | Save As... command
 *
 * @author   Takis Diakoumis
 */
public class SaveAsCommand implements BaseCommand {
    
    public void execute(ActionEvent e) {

        SaveFunction saveFunction = null;

        try {
        
            saveFunction = GUIUtilities.getSaveFunctionInFocus();
            
            if (saveFunction != null) {
            
                saveFunction.save(true);
                GUIUtils.scheduleGC();
            }

        } finally {

            saveFunction = null;
        }

    }
    
}


















