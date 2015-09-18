/*
 * CancelCommand.java
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

package org.executequery.actions.othercommands;

import java.awt.event.ActionEvent;

import org.executequery.GUIUtilities;

/** <p>This represents a generic close/cancel command
 *  performed from any open <code>JInternalFrame</code>.
 *  The effect is to close that frame and remove it from
 *  the desktop.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class CancelCommand extends AbstractBaseCommand {
    
    private boolean isDialog;
    
    public CancelCommand() {
        this(false);
    }

    /**
     * Creates a new CancelCommand which may be a dialog
     * cancel command or otherwise.
     */
    public CancelCommand(boolean isDialog) {
        super("Cancel");
        this.isDialog = isDialog;
    }

    /** 
     * Performs the close operation.
     *
     *  @param the <code>ActionEvent</code> initiating
     *         this command
     */
    public void execute(ActionEvent e) {
        if (isDialog) {
            GUIUtilities.closeSelectedDialog();
        } else {
            //GUIUtilities.closeSelectedInternalFrame();
        }
    }
    
}










