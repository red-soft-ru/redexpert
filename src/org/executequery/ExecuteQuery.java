/*
 * ExecuteQuery.java
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

package org.executequery;

import org.executequery.gui.HelpWindow;

/** 
 * The entry point for Execute Query.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public final class ExecuteQuery {
    
    public ExecuteQuery() {
        
        new ApplicationLauncher().startup();
    }
    
    public static void main(String[] args) {
        
        // make sure the installed java version is at least 1.7
        /*
        if (!MiscUtils.isMinJavaVersion(1, 7)) {

            JOptionPane.showMessageDialog(null, 
                    "The minimum required Java version is 1.7.\n" +
                    "The reported version is " + 
                    System.getProperty("java.vm.version") +
                    ".\n\nPlease download and install the latest Java " +
                    "version\nfrom http://java.sun.com and try again.\n\n",
                    "Java Version Error",
                    JOptionPane.ERROR_MESSAGE);

            System.exit(1);
        }
        */

        if (isHelpStartupOnly(args)) {
            
            HelpWindow.main(args);
            
        } else {

            ApplicationContext.getInstance().startup(args);
            new ExecuteQuery();
        }
            
    }
    
    private static boolean isHelpStartupOnly(String[] args) {
        
        if (args.length > 0) {

            return args[0].toUpperCase().equals("HELP");
        }

        return false; 
    }
    
}






