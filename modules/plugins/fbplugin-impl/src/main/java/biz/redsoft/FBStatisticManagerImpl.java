package biz.redsoft;

import org.firebirdsql.management.FBStatisticsManager;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class FBStatisticManagerImpl implements IFBStatisticManager{

    FBStatisticsManager fbStatisticsManager;
    FBStatisticManagerImpl()
    {
        fbStatisticsManager=new FBStatisticsManager();
    }
    @Override
    public String getCharSet() {
        return fbStatisticsManager.getCharSet();
    }

    @Override
    public void setCharSet(String var1) {
        fbStatisticsManager.setCharSet(var1);
    }

    @Override
    public String getUser() {
        return fbStatisticsManager.getUser();
    }

    @Override
    public void setUser(String var1) {
        fbStatisticsManager.setUser(var1);
    }

    @Override
    public String getPassword() {
        return fbStatisticsManager.getPassword();
    }

    @Override
    public void setPassword(String var1) {
        fbStatisticsManager.setPassword(var1);
    }

    @Override
    public String getDatabase() {
        return fbStatisticsManager.getDatabase();
    }

    @Override
    public void setDatabase(String var1) {
        fbStatisticsManager.setDatabase(var1);
    }

    @Override
    public String getHost() {
        return fbStatisticsManager.getHost();
    }

    @Override
    public void setHost(String var1) {
        fbStatisticsManager.setHost(var1);
    }

    @Override
    public int getPort() {
        return fbStatisticsManager.getPort();
    }

    @Override
    public void setPort(int var1) {
        fbStatisticsManager.setPort(var1);
    }

    @Override
    public OutputStream getLogger() {
        return fbStatisticsManager.getLogger();
    }

    @Override
    public void setLogger(OutputStream var1) {
        fbStatisticsManager.setLogger(var1);
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
