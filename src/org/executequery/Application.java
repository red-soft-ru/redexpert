/*
 * Application.java
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

package org.executequery;

import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.SaveFunction;
import org.executequery.gui.SaveOnExitDialog;
import org.executequery.log.Log;
import org.executequery.util.UserProperties;
import org.underworldlabs.jdbc.DataSourceException;

public final class Application {

    private static Application application;
    
    private Application() {}
    
    public static synchronized Application getInstance() {
        
        if (application == null) {
            
            application = new Application();
        }
        
        return application;
    }
    
    /**
     * Program shutdown method.
     * Does some logging and closes connections cleanly.
     */
    public void exitProgram() {
        
        if (promptToSave() && GUIUtilities.getOpenSaveFunctionCount() > 0) {

            SaveOnExitDialog exitDialog = new SaveOnExitDialog();

            int result = exitDialog.getResult();
            if (result != SaveFunction.SAVE_COMPLETE ||
                        result != SaveOnExitDialog.DISCARD_OPTION) {

                exitDialog = null;
                return;
            }

        }

        releaseConnections();

        GUIUtilities.shuttingDown();
        GUIUtilities.getParentFrame().dispose();

        System.exit(0);
    }

    private void releaseConnections() {

        Log.info("Releasing database resources...");

        try {

            ConnectionManager.close();

        } catch (DataSourceException e) {}

        Log.info("Connection pools destroyed");
    }

    private boolean promptToSave() {

        return (userProperties().getBooleanProperty("general.save.prompt") 
                && GUIUtilities.hasValidSaveFunction());
    }

    private UserProperties userProperties() {

        return UserProperties.getInstance();
    }

}





