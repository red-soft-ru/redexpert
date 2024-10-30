package biz.redsoft;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class FBTableStatManager implements IFBTableStatisticManager {
    CustomTableStatisticManager manager;

    public FBTableStatManager(Connection connection) throws SQLException {
        manager = CustomTableStatisticManager.of(connection);
    }

    @Override
    public Map<String, IFBTableStatistics> getTableStatistics() throws SQLException {
        return manager.getTableStatistics();
    }

    @Override
    public void setTables(Map<Integer, String> tableMap) {
        manager.setTableMapping(tableMap);
    }
}
