package biz.redsoft;

public interface IFBTableStatistics {

    String tableName();

    long[] getArrayValues();

    long readSeqCount();

    long readIdxCount();

    long insertCount();

    long updateCount();

    long deleteCount();

    long backoutCount();

    long purgeCount();

    long expungeCount();

    String toString();

}
