package biz.redsoft;

import org.firebirdsql.logging.LoggerFactory;

import java.util.Objects;

public class FBTableStat implements IFBTableStatistics {
    private final String tableName;
    private final long readSeqCount;
    private final long readIdxCount;
    private final long insertCount;
    private final long updateCount;
    private final long deleteCount;
    private final long backoutCount;
    private final long purgeCount;
    private final long expungeCount;

    private FBTableStat(String tableName, long readSeqCount, long readIdxCount, long insertCount, long updateCount, long deleteCount, long backoutCount, long purgeCount, long expungeCount) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.readSeqCount = readSeqCount;
        this.readIdxCount = readIdxCount;
        this.insertCount = insertCount;
        this.updateCount = updateCount;
        this.deleteCount = deleteCount;
        this.backoutCount = backoutCount;
        this.purgeCount = purgeCount;
        this.expungeCount = expungeCount;
    }

    public String tableName() {
        return this.tableName;
    }

    public long readSeqCount() {
        return this.readSeqCount;
    }

    public long readIdxCount() {
        return this.readIdxCount;
    }

    public long insertCount() {
        return this.insertCount;
    }

    public long updateCount() {
        return this.updateCount;
    }

    public long deleteCount() {
        return this.deleteCount;
    }

    public long backoutCount() {
        return this.backoutCount;
    }

    public long purgeCount() {
        return this.purgeCount;
    }

    public long expungeCount() {
        return this.expungeCount;
    }

    @Override
    public long[] getArrayValues() {
        return new long[]{readSeqCount(), readIdxCount(), insertCount(), updateCount(), deleteCount(), backoutCount(), purgeCount(), expungeCount()};
    }

    public String toString() {
        return "TableStatistics{tableName='" + this.tableName + '\'' + ", readSeqCount=" + this.readSeqCount + ", readIdxCount=" + this.readIdxCount + ", insertCount=" + this.insertCount + ", updateCount=" + this.updateCount + ", deleteCount=" + this.deleteCount + ", backoutCount=" + this.backoutCount + ", purgeCount=" + this.purgeCount + ", expungeCount=" + this.expungeCount + '}';
    }

    static TableStatisticsBuilder builder(String tableName) {
        return new TableStatisticsBuilder(tableName);
    }

    static final class TableStatisticsBuilder {
        private final String tableName;
        private long readSeqCount;
        private long readIdxCount;
        private long insertCount;
        private long updateCount;
        private long deleteCount;
        private long backoutCount;
        private long purgeCount;
        private long expungeCount;

        private TableStatisticsBuilder(String tableName) {
            this.tableName = tableName;
        }

        void addStatistic(int statistic, long value) {
            switch (statistic) {
                case 23:
                    this.readSeqCount = value;
                    break;
                case 24:
                    this.readIdxCount = value;
                    break;
                case 25:
                    this.insertCount = value;
                    break;
                case 26:
                    this.updateCount = value;
                    break;
                case 27:
                    this.deleteCount = value;
                    break;
                case 28:
                    this.backoutCount = value;
                    break;
                case 29:
                    this.purgeCount = value;
                    break;
                case 30:
                    this.expungeCount = value;
                    break;
                default:
                    LoggerFactory.getLogger(TableStatisticsBuilder.class).debugf("Unexpected information item %d with value %d, this is likely an implementation bug.", statistic, value);
            }

        }


        FBTableStat toTableStatistics() {
            return new FBTableStat(this.tableName, this.readSeqCount, this.readIdxCount, this.insertCount, this.updateCount, this.deleteCount, this.backoutCount, this.purgeCount, this.expungeCount);
        }
    }


}
