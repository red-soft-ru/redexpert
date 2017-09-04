/*
 * Command.java
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

import org.executequery.gui.console.Console;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * The <code>Command</code> class is an empty implementation of a console
 * command. The commands list is a linked list.
 * @author Romain Guy
 */
public abstract class Command {
    
    public Command next;
    
    /** Return the command name. Displayed in console help summary. */
    public abstract String getCommandName();
    
    /** Return the command summary. Displayed in console help summary. */
    public abstract String getCommandSummary();
    
    /** Handles a command given by the console. If the command can be
     *  handled, return true, false otherwise.
     */
    public abstract boolean handleCommand(Console console, String command);
    
}














