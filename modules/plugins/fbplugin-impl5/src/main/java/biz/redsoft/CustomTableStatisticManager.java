package biz.redsoft;

import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.InfoProcessor;
import org.firebirdsql.gds.ng.InfoTruncatedException;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomTableStatisticManager implements AutoCloseable {
    private static final int MAX_RETRIES = 3;
    private Map<Integer, String> tableMapping = new HashMap();
    private FirebirdConnection connection;
    private int tableSlack;

    private CustomTableStatisticManager(FirebirdConnection connection) throws SQLException {
        if (connection.isClosed()) {
            throw new SQLNonTransientConnectionException("This connection is closed and cannot be used now.", "08003");
        } else {
            this.connection = connection;
        }
    }

    public static CustomTableStatisticManager of(Connection connection) throws SQLException {
        return new CustomTableStatisticManager(connection.unwrap(FirebirdConnection.class));
    }

    public Map<String, IFBTableStatistics> getTableStatistics() throws SQLException {
        this.checkClosed();
        FbDatabase db = this.connection.getFbDatabase();
        TableStatisticsProcessor tableStatisticsProcessor = new TableStatisticsProcessor();
        int attempt = 0;

        while (true) {
            try {
                return (Map) db.getDatabaseInfo(getInfoItems(), this.bufferSize(this.getTableCount()), tableStatisticsProcessor);
            } catch (InfoTruncatedException var6) {
                ++this.tableSlack;
                this.updateTableMapping();
                if (attempt++ >= 3) {
                    throw var6;
                }
            }
        }
    }

    private int getTableCount() throws SQLException {
        int size = this.tableMapping.size();
        if (size != 0) {
            return size;
        } else {
            this.updateTableMapping();
            return this.tableMapping.size();
        }
    }

    public void close() {
        this.connection = null;
        this.tableMapping.clear();
        this.tableMapping = null;
    }

    private void checkClosed() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            if (this.connection != null) {
                this.close();
            }

            throw new SQLNonTransientException("This statistics manager is closed and cannot be used now.");
        }
    }

    public Map<Integer, String> getTableMapping() {
        return tableMapping;
    }

    public void setTableMapping(Map<Integer, String> tableMapping) {
        this.tableMapping = tableMapping;
    }

    private void updateTableMapping() throws SQLException {
        DatabaseMetaData md = this.connection.getMetaData();
        ResultSet rs = md.getTables(null, null, "%", new String[]{"SYSTEM TABLE", "TABLE", "GLOBAL TEMPORARY"});
        Throwable var3 = null;

        try {
            while (rs.next()) {
                this.tableMapping.put(rs.getInt("JB_RELATION_ID"), rs.getString("TABLE_NAME"));
            }
        } catch (Throwable var12) {
            var3 = var12;
            throw var12;
        } finally {
            if (rs != null) {
                if (var3 != null) {
                    try {
                        rs.close();
                    } catch (Throwable var11) {
                        var3.addSuppressed(var11);
                    }
                } else {
                    rs.close();
                }
            }

        }

    }

    private int bufferSize(int maxTables) {
        long size = 1L + 8L * (3L + 6L * (long) (maxTables + this.tableSlack));
        return size <= 0L ? Integer.MAX_VALUE : (int) Math.min(size, 2147483647L);
    }

    private static byte[] getInfoItems() {
        return new byte[]{23, 24, 25, 26, 27, 28, 29, 30};
    }

    private final class TableStatisticsProcessor implements InfoProcessor<Map<String, FBTableStat>> {
        private final Map<String, FBTableStat.TableStatisticsBuilder> statisticsBuilders;
        private boolean allowTableMappingUpdate;

        private TableStatisticsProcessor() {
            this.statisticsBuilders = new HashMap();
            //this.allowTableMappingUpdate = true;
        }

        public Map<String, FBTableStat> process(byte[] infoResponse) throws SQLException {
            try {
                int idx = 0;

                while (true) {
                    if (idx < infoResponse.length) {
                        int infoItem = infoResponse[idx++];
                        switch (infoItem) {
                            case 1:
                                break;
                            case 2:
                                throw new InfoTruncatedException("Received isc_info_truncated, and this processor cannot recover automatically", infoResponse.length);
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            default:
                                LoggerFactory.getLogger(TableStatisticsProcessor.class).debugf("Received unexpected info item %d, this is likely an implementation bug.", Integer.valueOf(infoItem));
                                break;
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                            case 29:
                            case 30:
                                int length = VaxEncoding.iscVaxInteger2(infoResponse, idx);
                                idx += 2;
                                this.processStatistics(infoItem, infoResponse, idx, idx += length);
                                continue;
                        }
                    }

                    Map var8 = this.statisticsBuilders.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> {
                        return e.getValue().toTableStatistics();
                    }));
                    return var8;
                }
            } finally {
                this.statisticsBuilders.clear();
            }
        }

        void processStatistics(int statistic, byte[] buffer, int start, int end) throws SQLException {
            int idx = start;

            while (idx <= end - 6) {
                int tableId = VaxEncoding.iscVaxInteger2(buffer, idx);
                idx += 2;
                long value = VaxEncoding.iscVaxInteger(buffer, idx, 4);
                idx += 4;
                this.getBuilder(tableId).addStatistic(statistic, value);
            }

        }

        private String getTableName(Integer tableId) throws SQLException {
            String tableName = CustomTableStatisticManager.this.tableMapping.get(tableId);
            if (tableName == null) {
                if (this.allowTableMappingUpdate) {
                    CustomTableStatisticManager.this.updateTableMapping();
                    this.allowTableMappingUpdate = false;
                    tableName = CustomTableStatisticManager.this.tableMapping.get(tableId);
                }

                if (tableName == null) {
                    tableName = "UNKNOWN_TABLE_ID_" + tableId;
                }
            }

            return tableName;
        }

        private FBTableStat.TableStatisticsBuilder getBuilder(int tableId) throws SQLException {
            String tableName = this.getTableName(tableId);
            return this.statisticsBuilders.computeIfAbsent(tableName, FBTableStat::builder);
        }
    }
}

