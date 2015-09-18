/*
 * UndoCommand.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import org.executequery.gui.UndoableComponent;
import org.underworldlabs.swing.actions.BaseCommand;

/** <p>Query Editor's Undo command execution.
 *
 *  @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class UndoCommand implements BaseCommand {
    
    private UndoableComponent undoable;

    public void execute(ActionEvent e) {
        if (undoable != null) {
            undoable.undo();
        }        
    }

    public void setUndoableComponent(UndoableComponent undoable) {
        this.undoable = undoable;
    }

}



















