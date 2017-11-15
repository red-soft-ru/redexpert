/*
 * AbstractUrlLauncherCommand.java
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

package org.executequery.actions;

import java.awt.event.ActionEvent;

import org.executequery.ApplicationException;
import org.executequery.GUIUtilities;
import org.executequery.actions.othercommands.AbstractBaseCommand;
import org.executequery.util.SystemWebBrowserLauncher;

/** 
 *
 * @author   Takis Diakoumis
 */
public abstract class AbstractUrlLauncherCommand extends AbstractBaseCommand {

    public void execute(ActionEvent e) {
        
        try {
            
            new SystemWebBrowserLauncher().launch(url());
            
        } catch (ApplicationException applicationException) {
            
            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("error.launchBrowser") +
                    applicationException.getMessage(), applicationException);
            
        }

    }

    public abstract String url();
    
}

