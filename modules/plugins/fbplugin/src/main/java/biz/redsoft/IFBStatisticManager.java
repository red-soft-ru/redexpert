package biz.redsoft;

import java.sql.Connection;
import java.sql.SQLException;

public interface IFBStatisticManager extends IFBServiceManager{
    void getHeaderPage() throws SQLException;
    void getDatabaseStatistics() throws SQLException;
    void getDatabaseStatistics(int options) throws SQLException;
    void getTableStatistics(String[] tableNames) throws SQLException;
    IFBDatabaseTransactionInfo getDatabaseTransactionInfo() throws SQLException;
    //static
    IFBDatabaseTransactionInfo getDatabaseTransactionInfo(Connection connection) throws SQLException;
    //IFBDatabaseTransactionInfo getDatabaseTransactionInfo(FbDatabase database) throws SQLException;
}
