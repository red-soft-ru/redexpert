/*
 * Application.java
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

import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.SaveFunction;
import org.executequery.gui.SaveOnExitDialog;
import org.executequery.log.Log;
import org.executequery.repository.ConnectionFoldersRepository;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.SystemResources;
import org.executequery.util.UserProperties;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.toolbar.ToolBarProperties;

import java.util.Properties;

public final class Application {

    /// Private constructor to prevent installation
    private Application() {
    }

    /**
     * Program shutdown method.
     * Does some logging and closes connections cleanly.
     */
    public static void exitProgram() {
        exitProgram(false);
    }

    /**
     * Program shutdown method.
     * Does some logging and closes connections cleanly.
     */
    public static void exitProgram(boolean force) {

        if (force) {
            System.exit(0);
            return;
        }

        if (!trySaveOpenedFiles())
            return;

        releaseConnections();
        storeProperties();
        shutdown();
    }

    // ---

    private static boolean trySaveOpenedFiles() {

        boolean shouldDisplaySaveDialog = UserProperties.getInstance().getBooleanProperty("general.save.prompt")
                && GUIUtilities.hasValidSaveFunction()
                && GUIUtilities.getOpenSaveFunctionCount() > 0;

        if (shouldDisplaySaveDialog) {
            int result = new SaveOnExitDialog().getResult();
            return result == SaveFunction.SAVE_COMPLETE || result == SaveFunction.SAVE_CANCELLED;
        }

        return true;
    }

    private static void releaseConnections() {
        Log.info("Releasing database resources...");

        try {
            ConnectionManager.close();
            Log.info("Connection pools destroyed");

        } catch (DataSourceException e) {
            Log.error("Releasing database resources interrupted by the exception", e);
        }
    }

    // --- store app properties files ---

    private static void storeProperties() {
        Log.info("Saving properties files...");

        storeUserProperties();
        storeDatabaseConnections();
        storeDatabaseFolders();
        storeToolbars();
    }

    private static void storeUserProperties() {
        Properties properties = UserProperties.getInstance().getProperties();
        SystemResources.setUserPreferences(properties);
    }

    private static void storeDatabaseConnections() {
        try {
            Repository repo = RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
            if (repo instanceof DatabaseConnectionRepository)
                ((DatabaseConnectionRepository) repo).save();

        } catch (Exception e) {
            Log.error(String.format("Saving %s properties failed", DatabaseConnectionRepository.REPOSITORY_ID), e);
        }
    }

    private static void storeDatabaseFolders() {
        try {
            Repository repo = RepositoryCache.load(ConnectionFoldersRepository.REPOSITORY_ID);
            if (repo instanceof ConnectionFoldersRepository)
                ((ConnectionFoldersRepository) repo).save();

        } catch (Exception e) {
            Log.error(String.format("Saving %s properties failed", ConnectionFoldersRepository.REPOSITORY_ID), e);
        }
    }

    private static void storeToolbars() {
        try {
            ToolBarProperties.saveTools();

        } catch (Exception e) {
            Log.error("Saving toolbar properties failed", e);
        }
    }

    // ---

    private static void shutdown() {
        Log.info("System exiting...");

        GUIUtilities.getParentFrame().dispose();
        ApplicationInstanceCounter.remove();

        System.exit(0);
    }

}
