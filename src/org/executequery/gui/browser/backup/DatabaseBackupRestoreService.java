package org.executequery.gui.browser.backup;

import biz.redsoft.IFBBackupManager;

import java.io.OutputStream;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Optional;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.log.Log;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;

/**
 * Service class responsible for managing database backup and restore operations using the FBBackupManager. Provides
 * methods for initiating and managing backup and restore processes, including setting the required options, paths, and
 * logging output.
 *
 * @author Maxim Kozhinov
 */
public class DatabaseBackupRestoreService {

    /**
     * Private constructor to prevent instantiation.
     */
    private DatabaseBackupRestoreService() {
    }

    /**
     * Executes the database restore process using the given parameters.
     *
     * @param dc           the database connection details
     * @param fromFile     path to the backup source file
     * @param toFile       path where the restored data will be saved
     * @param options      restore logic options
     * @param pageSize     the page size to be used during restore
     * @param workersCount the number of parallel workers to be used during the restore
     * @param os           an optional output stream for logging
     */
    public static void restoreDatabase(DatabaseConnection dc, String fromFile, String toFile, int options, int pageSize,
                                       int workersCount, OutputStream os) throws SQLException, ClassNotFoundException {

        Optional<IFBBackupManager> backupManagerOptional = getBackupManager(dc);
        if (!backupManagerOptional.isPresent())
            throw new ClassNotFoundException("Couldn't initialise IFBBackupManager");

        IFBBackupManager backupManager = backupManagerOptional.get();
        backupManager.clearRestorePaths();
        if (os != null) {
            backupManager.setLogger(os);
            backupManager.setVerbose(true);
        }
        backupManager.addBackupPath(fromFile);
        backupManager.addBackupPath(toFile);
        backupManager.setRestorePageSize(pageSize);

        backupManager.restoreDatabase(options, workersCount);
        Log.info("Database restored successfully from: " + fromFile + " to: " + toFile);
    }

    /**
     * Executes the database backup process using the given parameters.
     *
     * @param connection   the database connection details
     * @param backupPath   the path where the backup will be saved
     * @param options      backup options
     * @param workersCount the number of parallel workers to be used during the backup
     * @param os           an optional output stream for logging
     */
    public static void backupDatabase(DatabaseConnection connection, String backupPath, int options,
                                      int workersCount, OutputStream os) throws SQLException, ClassNotFoundException {

        Optional<IFBBackupManager> backupManagerOptional = getBackupManager(connection);
        if (!backupManagerOptional.isPresent())
            throw new ClassNotFoundException("Couldn't initialise IFBBackupManager");

        IFBBackupManager backupManager = backupManagerOptional.get();
        backupManager.clearBackupPaths();
        if (os != null) {
            backupManager.setLogger(os);
            backupManager.setVerbose(true);
        }
        backupManager.addBackupPath(backupPath);

        backupManager.backupDatabase(options, workersCount);
        Log.info("Backup successfully created at: " + backupPath);
    }

    /**
     * Retrieves the IFBBackupManager instance based on the given database connection.
     *
     * @param dc the database connection details
     * @return an Optional containing the IFBBackupManager if successfully initialized, or an empty Optional if not
     */
    private static Optional<IFBBackupManager> getBackupManager(DatabaseConnection dc) throws SQLException, ClassNotFoundException {

        Driver driver = getDriverFromDbConnection(dc);
        IFBBackupManager backupManager = (IFBBackupManager) DynamicLibraryLoader.loadingObjectFromClassLoader(
                driver.getMajorVersion(), driver, "FBBackupManagerImpl"
        );
        backupManager.setHost(dc.getHost());
        backupManager.setPort(dc.getPortInt());
        backupManager.setUser(dc.getUserName());
        backupManager.setPassword(dc.getUnencryptedPassword());
        backupManager.setDatabase(dc.getSourceName());
        backupManager.setCharSet(MiscUtils.getJavaCharsetFromSqlCharset(dc.getCharset()));

        return Optional.of(backupManager);
    }

    /**
     * Retrieves the appropriate driver for the current database connection.
     *
     * @param dc the database connection details
     * @return the appropriate driver for the current database connection
     * @throws SQLException if unable to load the driver
     */
    private static Driver getDriverFromDbConnection(DatabaseConnection dc) throws SQLException {
        String driverId = String.valueOf(dc.getDriverId());
        return DefaultDriverLoader.getLoadedDrivers().entrySet().stream()
                .filter(nameToDriverEntry -> nameToDriverEntry.getKey().startsWith(driverId))
                .findFirst().map(Entry::getValue)
                .orElse(DefaultDriverLoader.getDefaultDriver());
    }

}