package biz.redsoft;

public interface IFBDatabaseTransactionInfo {
    long getOldestTransaction();
    long getOldestActiveTransaction();
    long getOldestSnapshotTransaction();
    long getNextTransaction();
    long getActiveTransactionCount();
}
