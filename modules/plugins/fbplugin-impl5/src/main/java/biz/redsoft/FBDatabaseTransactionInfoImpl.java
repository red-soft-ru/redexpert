package biz.redsoft;

import org.firebirdsql.management.StatisticsManager;

public class FBDatabaseTransactionInfoImpl implements IFBDatabaseTransactionInfo{

    StatisticsManager.DatabaseTransactionInfo databaseTransactionInfo;
    public FBDatabaseTransactionInfoImpl(StatisticsManager.DatabaseTransactionInfo databaseTransactionInfo)
    {
        this.databaseTransactionInfo=databaseTransactionInfo;
    }
    @Override
    public long getOldestTransaction() {
        return databaseTransactionInfo.getOldestTransaction();
    }

    @Override
    public long getOldestActiveTransaction() {
        return databaseTransactionInfo.getOldestActiveTransaction();
    }

    @Override
    public long getOldestSnapshotTransaction() {
        return databaseTransactionInfo.getOldestSnapshotTransaction();
    }

    @Override
    public long getNextTransaction() {
        return databaseTransactionInfo.getNextTransaction();
    }

    @Override
    public long getActiveTransactionCount() {
        return databaseTransactionInfo.getActiveTransactionCount();
    }
}

