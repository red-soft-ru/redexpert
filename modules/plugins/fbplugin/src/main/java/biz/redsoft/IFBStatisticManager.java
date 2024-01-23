package biz.redsoft;

import java.sql.Connection;
import java.sql.SQLException;

public interface IFBStatisticManager extends IFBServiceManager {
    int DATA_TABLE_STATISTICS = 0x01;

    /**
     * Request statistics on indexes.
     */
    int INDEX_STATISTICS = 0x08;

    /**
     * Request statistics on system tables.
     */
    int SYSTEM_TABLE_STATISTICS = 0x10;

    /**
     * Request statistics on record versions.
     */
    int RECORD_VERSION_STATISTICS = 0x20;

    void getHeaderPage() throws SQLException;

    void getDatabaseStatistics() throws SQLException;

    void getDatabaseStatistics(int options) throws SQLException;

    void getTableStatistics(String[] tableNames) throws SQLException;

    IFBDatabaseTransactionInfo getDatabaseTransactionInfo() throws SQLException;

    //static
    IFBDatabaseTransactionInfo getDatabaseTransactionInfo(Connection connection) throws SQLException;
    //IFBDatabaseTransactionInfo getDatabaseTransactionInfo(FbDatabase database) throws SQLException;
}
