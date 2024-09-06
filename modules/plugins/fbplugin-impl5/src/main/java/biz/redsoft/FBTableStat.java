package biz.redsoft;

import org.firebirdsql.management.TableStatistics;

public class FBTableStat implements IFBTableStatistics {
    TableStatistics ts;

    public FBTableStat(TableStatistics ts) {
        this.ts = ts;
    }

    @Override
    public String tableName() {
        return ts.tableName();
    }

    @Override
    public long readSeqCount() {
        return ts.readSeqCount();
    }

    @Override
    public long readIdxCount() {
        return ts.readIdxCount();
    }

    @Override
    public long insertCount() {
        return ts.insertCount();
    }

    @Override
    public long updateCount() {
        return ts.updateCount();
    }

    @Override
    public long deleteCount() {
        return ts.deleteCount();
    }

    @Override
    public long backoutCount() {
        return ts.backoutCount();
    }

    @Override
    public long purgeCount() {
        return ts.purgeCount();
    }

    @Override
    public long expungeCount() {
        return ts.expungeCount();
    }

    @Override
    public long[] getArrayValues() {
        return new long[]{readSeqCount(), readIdxCount(), insertCount(), updateCount(), deleteCount(), backoutCount(), purgeCount(), expungeCount()};
    }

    @Override
    public String toString() {
        return ts.toString();
    }
}
