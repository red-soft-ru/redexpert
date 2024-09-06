package biz.redsoft;

import org.firebirdsql.management.FBTableStatisticsManager;
import org.firebirdsql.management.TableStatistics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FBTableStatManager implements IFBTableStatisticManager {
    FBTableStatisticsManager manager;

    public FBTableStatManager(Connection connection) throws SQLException {
        manager = FBTableStatisticsManager.of(connection);
    }

    @Override
    public Map<String, IFBTableStatistics> getTableStatistics() throws SQLException {
        Map<String, IFBTableStatistics> map = new HashMap<>();
        Map<String, TableStatistics> fMap = manager.getTableStatistics();
        for (String key : fMap.keySet()) {
            map.put(key, new FBTableStat(fMap.get(key)));
        }
        return map;
    }
}
