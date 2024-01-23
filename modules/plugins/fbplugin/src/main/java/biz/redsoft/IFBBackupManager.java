package biz.redsoft;

import java.sql.SQLException;

public interface IFBBackupManager extends IFBServiceManager {

    int BACKUP_IGNORE_CHECKSUMS = 0x01;
    int BACKUP_IGNORE_LIMBO = 0x02;
    int BACKUP_METADATA_ONLY = 0x04;
    int BACKUP_NO_GARBAGE_COLLECT = 0x08;
    int BACKUP_OLD_DESCRIPTIONS = 0x10;
    int BACKUP_NON_TRANSPORTABLE = 0x20;
    int BACKUP_CONVERT = 0x40;
    int BACKUP_EXPAND = 0x80;

    int RESTORE_DEACTIVATE_IDX = 0x0100;
    int RESTORE_NO_SHADOW = 0x0200;
    int RESTORE_NO_VALIDITY = 0x0400;
    int RESTORE_ONE_AT_A_TIME = 0x0800;
    int RESTORE_OVERWRITE = 0x1000;
    int RESTORE_CREATE = 0x2000;
    int RESTORE_USE_ALL_SPACE = 0x4000;

    /**
     * Add the file to the backup of the specified size. Firebird allows
     * splitting the backup into multiple files, limiting the size of the backup
     * file. This can be useful for example for creating a backup on CD or DVD.
     *
     * @param path path to the backup file.
     * @param size max size of the file in bytes.
     */
    void addBackupPath(String path, int size);

    /**
     * Add backup file to the list. This method is used only during restoring
     * the database to specify multi-file backups. The call is equivalent to
     * passing the size -1 to {@link #addBackupPath(String, int)} call.
     * <p>
     * If application invokes backup operation, an error is generated in that
     * call.
     * </p>
     *
     * @param path path to the backup file.
     */
    void addBackupPath(String path);

    /**
     * Clear the information about backup paths. This method undoes all
     * parameters set in the {@link #addBackupPath(String, int)} or
     * {@link #addBackupPath(String)} methods.
     */
    void clearBackupPaths();

    /**
     * Set the path to the database. This method is used both for backup and
     * restore operation.
     *
     * @param path path to the database file.
     *             <p>
     *             In case of backup, value specifies the path of the existing database on the server that will be
     *             backed up.
     *             </p>
     *             <p>
     *             In case of restore, value specifies the path of the single-file database where the backup will be
     *             restored to.
     *             </p>
     */
    void setDatabase(String path);

    /**
     * Add the file to the multi-file database of the specified size for restore
     * operation.
     *
     * @param path path to the backup file.
     * @param size max size of the database file in pages.
     */
    void addRestorePath(String path, int size);

    /**
     * Clear the information about restore paths. This method undoes all
     * parameters set in the {@link #addRestorePath(String, int)} or
     * {@link #setDatabase(String)} methods.
     */
    void clearRestorePaths();

    /**
     * Perform the backup operation.
     *
     * @throws SQLException if a database error occurs during the backup
     */
    void backupDatabase() throws SQLException;

    /**
     * Perform the backup operation, metadata only.
     *
     * @throws SQLException if a database error occurs during the backup
     */
    void backupMetadata() throws SQLException;

    /**
     * Perform the backup operation.
     *
     * @param options a bitmask combination of the {@code BACKUP_*} constants for the backup operation
     * @throws SQLException if a database error occurs during the backup
     */
    void backupDatabase(int options) throws SQLException;

    /**
     * Perform the parallel backup operation specifying the number of parallel workers.
     *
     * @param options         a bitmask combination of the {@code BACKUP_*} constants for the backup operation
     * @param parallelWorkers Valid values must be greater than 1 (no parallelism).
     *                        Values less than 1 is silently ignored and default value of 1 is used.
     * @throws SQLException if a database error occurs during the backup
     */
    void backupDatabase(int options, int parallelWorkers) throws SQLException;

    /**
     * Set whether the operations of this {@code BackupManager} will result in verbose logging to the configured logger.
     *
     * @param verbose If {@code true}, operations will be logged verbosely, otherwise they will not be logged verbosely
     */
    void setVerbose(boolean verbose);

    /**
     * Set the default number of pages to be buffered (cached) by default in a
     * restored database.
     *
     * @param bufferCount The page-buffer size to be used, a positive value
     */
    void setRestorePageBufferCount(int bufferCount);

    /**
     * Set the page size that will be used for a restored database. The value for {@code pageSize} must be one
     * of: 1024, 2048, 4096, 8192 or 16384. The default value depends on the Firebird version.
     *
     * @param pageSize The page size to be used in a restored database, one of 1024, 2048, 4196, 8192 or 16384
     * @see PageSizeConstants
     */
    void setRestorePageSize(int pageSize);

    /**
     * Set the restore operation to create a new database, as opposed to
     * overwriting an existing database.
     *
     * @param replace If {@code true}, the restore operation will attempt to create a new database if it does not exit or
     *                overwrite an existing one when it exists, {@code false} when restore should fail if database already
     *                exist (if it doesn't, a database will be successfully created).
     */
    void setRestoreReplace(boolean replace);

    /**
     * Set the read-only attribute on a restored database.
     *
     * @param readOnly If {@code true}, a restored database will be
     *                 read-only, otherwise it will be read-write.
     */
    void setRestoreReadOnly(boolean readOnly);

    /**
     * Set the number of parallel workers for the backup/restore task.
     *
     * @param parallelWorkers Valid values must be greater than 1 (no parallelism).
     *                        Values less than 1 is silently ignored and default value of 1 is used.
     */
    void setParallelWorkers(int parallelWorkers);

    /**
     * Perform the restore operation.
     *
     * @throws SQLException if a database error occurs during the restore
     */
    void restoreDatabase() throws SQLException;

    /**
     * Perform the restore operation.
     *
     * @param options A bitmask combination of {@code RESTORE_*} constants
     * @throws SQLException if a database error occurs during the restore
     */
    void restoreDatabase(int options) throws SQLException;

    /**
     * Perform the parallel restore operation specifying the number of parallel workers.
     *
     * @param options         A bitmask combination of {@code RESTORE_*} constants
     * @param parallelWorkers Valid values must be greater than 1 (no parallelism).
     *                        Values less than 1 is silently ignored and default value of 1 is used.
     * @throws SQLException if a database error occurs during the restore
     */
    void restoreDatabase(int options, int parallelWorkers) throws SQLException;

}
