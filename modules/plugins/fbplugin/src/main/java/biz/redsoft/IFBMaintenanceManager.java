package biz.redsoft;

import java.sql.SQLException;
import java.util.List;

public interface IFBMaintenanceManager extends IFBServiceManager {

    int ACCESS_MODE_READ_WRITE = 40;
    int ACCESS_MODE_READ_ONLY = 39;
    int SHUTDOWN_ATTACH = 9;
    int SHUTDOWN_TRANSACTIONAL = 10;
    int SHUTDOWN_FORCE = 7;
    int VALIDATE_READ_ONLY = 16;
    int VALIDATE_IGNORE_CHECKSUM = 32;
    int VALIDATE_FULL = 128;
    int PAGE_FILL_FULL = 35;
    int PAGE_FILL_RESERVE = 36;
    byte OPERATION_MODE_NORMAL = 0;
    byte OPERATION_MODE_MULTI = 1;
    byte OPERATION_MODE_SINGLE = 2;
    byte OPERATION_MODE_FULL_SHUTDOWN = 3;
    int SHUTDOWNEX_FORCE = 41;
    int SHUTDOWNEX_ATTACHMENTS = 42;
    int SHUTDOWNEX_TRANSACTIONS = 43;
    int PARALLEL_WORKERS = 52;

    void setDatabaseAccessMode(int var1) throws SQLException;

    void setDatabaseDialect(int var1) throws SQLException;

    void setDefaultCacheBuffer(int var1) throws SQLException;

    void setForcedWrites(boolean var1) throws SQLException;

    void setPageFill(int var1) throws SQLException;

    void shutdownDatabase(int var1, int var2) throws SQLException;

    void shutdownDatabase(byte var1, int var2, int var3) throws SQLException;

    void bringDatabaseOnline() throws SQLException;

    void bringDatabaseOnline(byte var1) throws SQLException;

    void markCorruptRecords() throws SQLException;

    void validateDatabase() throws SQLException;

    void validateDatabase(int var1) throws SQLException;

    void validateTable(String var1, String var2, String var3, String var4) throws SQLException;

    void setParallelWorkers(int var1);

    void setSweepThreshold(int var1) throws SQLException;

    void sweepDatabase() throws SQLException;

    void sweepDatabase(int var1) throws SQLException;

    void activateShadowFile() throws SQLException;

    void killUnavailableShadows() throws SQLException;

    @Deprecated
    void listLimboTransactions() throws SQLException;

    List<Long> limboTransactionsAsList() throws SQLException;

    long[] getLimboTransactions() throws SQLException;

    void commitTransaction(long var1) throws SQLException;

    void rollbackTransaction(long var1) throws SQLException;

}
