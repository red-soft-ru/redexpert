package biz.redsoft;

import java.sql.SQLException;
import java.util.Map;

public interface IFBTableStatisticManager {

    Map<String, IFBTableStatistics> getTableStatistics() throws SQLException;

    void setTables(Map<Integer, String> tableMap);
}
