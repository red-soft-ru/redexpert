package biz.redsoft;

import org.firebirdsql.management.FBBackupManager;

import java.sql.SQLException;

public class FBBackupManagerImpl extends AbstractServiceManager implements IFBBackupManager {
    FBBackupManager fbBackupManager;

    public FBBackupManagerImpl() {
        super();
    }

    @Override
    protected void initServiceManager() {
        fbBackupManager = new FBBackupManager();
        fbServiceManager = fbBackupManager;
    }

    @Override
    public void addBackupPath(String path, int size) {
        fbBackupManager.addBackupPath(path, size);
    }

    @Override
    public void addBackupPath(String path) {
        fbBackupManager.addBackupPath(path);
    }

    @Override
    public void clearBackupPaths() {
        fbBackupManager.clearBackupPaths();
    }

    @Override
    public void addRestorePath(String path, int size) {
        fbBackupManager.addRestorePath(path, size);
    }

    @Override
    public void clearRestorePaths() {
        fbBackupManager.clearRestorePaths();
    }

    @Override
    public void backupDatabase() throws SQLException {
        fbBackupManager.backupDatabase();
    }

    @Override
    public void backupMetadata() throws SQLException {
        fbBackupManager.backupMetadata();
    }

    @Override
    public void backupDatabase(int options) throws SQLException {
        fbBackupManager.backupDatabase(options);
    }

    @Override
    public void backupDatabase(int options, int parallelWorkers) throws SQLException {
        fbBackupManager.backupDatabase(options, parallelWorkers);
    }

    @Override
    public void setVerbose(boolean verbose) {
        fbBackupManager.setVerbose(verbose);
    }

    @Override
    public void setRestorePageBufferCount(int bufferCount) {
        fbBackupManager.setRestorePageBufferCount(bufferCount);
    }

    @Override
    public void setRestorePageSize(int pageSize) {
        fbBackupManager.setRestorePageSize(pageSize);
    }

    @Override
    public void setRestoreReplace(boolean replace) {
        fbBackupManager.setRestoreReplace(replace);
    }

    @Override
    public void setRestoreReadOnly(boolean readOnly) {
        fbBackupManager.setRestoreReadOnly(readOnly);
    }

    @Override
    public void setParallelWorkers(int parallelWorkers) {
        fbBackupManager.setParallelWorkers(parallelWorkers);
    }

    @Override
    public void restoreDatabase() throws SQLException {
        fbBackupManager.restoreDatabase();
    }

    @Override
    public void restoreDatabase(int options) throws SQLException {
        fbBackupManager.restoreDatabase(options);
    }

    @Override
    public void restoreDatabase(int options, int parallelWorkers) throws SQLException {
        fbBackupManager.restoreDatabase(options, parallelWorkers);
    }
}
