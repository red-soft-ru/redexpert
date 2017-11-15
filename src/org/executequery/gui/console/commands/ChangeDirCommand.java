/*
 * ChangeDirCommand.java
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

package org.executequery.gui.console.commands;

import java.io.File;

import org.underworldlabs.util.SystemProperties;
import org.executequery.gui.console.ConsoleUtilities;
import org.executequery.gui.console.Console;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * This command changes current dir.
 * @author Romain Guy
 */

/**
 *
 * @author   Takis Diakoumis
 */
public class ChangeDirCommand extends Command {
    
    private static final String COMMAND_NAME = "cd";
    
    public String getCommandName() {
        return COMMAND_NAME + " <path>";
    }
    
    public String getCommandSummary() {
        return SystemProperties.getProperty("console", "console.cd.command.help");
    }
    
    public boolean handleCommand(Console console, String command) {
        
        if (command.equals(COMMAND_NAME) || command.equals(COMMAND_NAME + " -help")) {
            console.help(SystemProperties.getProperty("user", "console.cd.help"));
            return true;
        }
        
        else if (command.startsWith(COMMAND_NAME)) {
            String newPath = ConsoleUtilities.constructPath(command.substring(3),
            console.getCurrentPath());
            
            if ((new File(newPath)).exists())
                console.setCurrentPath(newPath);
            
            else
                console.error(SystemProperties.getProperty("console", "console.cd.error"));
            
            return true;
        }
        
        return false;
    }
}

// End of ChangeDirCommand.java















