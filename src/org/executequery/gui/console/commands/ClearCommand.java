/*
 * ClearCommand.java
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

package org.executequery.gui.console.commands;

import javax.swing.text.BadLocationException;

import org.underworldlabs.util.SystemProperties;
import org.executequery.gui.console.Console;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * This command clears the console output.
 * @author Romain Guy
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ClearCommand extends Command {
    
    private static final String COMMAND_NAME = "clear";
    
    public String getCommandName() {
        return COMMAND_NAME;
    }
    
    public String getCommandSummary() {
        return SystemProperties.getProperty("console", "console.clear.command.help");
    }
    
    public boolean handleCommand(Console console, String command) {
        
        if (command.equals(COMMAND_NAME)) {
            
            try {
                console.getOutputDocument().remove(0, console.getOutputDocument().getLength());
            } catch (BadLocationException ble) { }
            
            return true;
            
        }
        
        return false;
    }
    
}

// End of ClearCommand.java














