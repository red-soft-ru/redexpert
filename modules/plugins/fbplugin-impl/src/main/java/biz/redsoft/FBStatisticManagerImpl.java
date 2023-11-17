package biz.redsoft;

import org.firebirdsql.management.FBStatisticsManager;

import java.sql.Connection;
import java.sql.SQLException;

public class FBStatisticManagerImpl extends AbstractServiceManager implements IFBStatisticManager {

    FBStatisticsManager fbStatisticsManager;

    public FBStatisticManagerImpl() {
        super();
    }

    @Override
    protected void initServiceManager() {
        fbStatisticsManager = new FBStatisticsManager();
        fbServiceManager = fbStatisticsManager;
    }


    @Override
    public void getHeaderPage() throws SQLException {
        fbStatisticsManager.getHeaderPage();
    }

    @Override
    public void getDatabaseStatistics() throws SQLException {
        fbStatisticsManager.getDatabaseStatistics();
    }

    @Override
    public void getDatabaseStatistics(int options) throws SQLException {
        fbStatisticsManager.getDatabaseStatistics(options);
    }

    @Override
    public void getTableStatistics(String[] tableNames) throws SQLException {
        fbStatisticsManager.getTableStatistics(tableNames);
    }

    @Override
    public IFBDatabaseTransactionInfo getDatabaseTransactionInfo() throws SQLException {
        return new FBDatabaseTransactionInfoImpl(fbStatisticsManager.getDatabaseTransactionInfo());
    }

    @Override
    public IFBDatabaseTransactionInfo getDatabaseTransactionInfo(Connection connection) throws SQLException {
        return new FBDatabaseTransactionInfoImpl(FBStatisticsManager.getDatabaseTransactionInfo(connection));
    }
}
