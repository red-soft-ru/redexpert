package biz.redsoft;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBResultSet;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Vasiliy on 4/6/2017.
 */
public class FBDatabasePerformance implements IFBDatabasePerformance {

    byte perfomanceInfoBytes[] =
            {
                    ISCConstants.isc_info_reads,
                    ISCConstants.isc_info_writes,
                    ISCConstants.isc_info_fetches,
                    ISCConstants.isc_info_marks,
                    ISCConstants.isc_info_page_size, ISCConstants.isc_info_num_buffers,
                    ISCConstants.isc_info_current_memory, ISCConstants.isc_info_max_memory
            };

    private FBConnection fbConnection = null;

    @Override
    public void setConnection(Connection connection) throws SQLException {

        this.fbConnection = (FBConnection) connection;
    }

    @Override
    public FBPerformanceInfo getPerformanceInfo() throws SQLException {
        byte[] databaseInfo = null;
        FBPerformanceInfo performanceInfo = null;
        if (fbConnection.getMetaData().getDriverMajorVersion() >= 3) {

            databaseInfo = fbConnection.getFbDatabase().getDatabaseInfo(perfomanceInfoBytes, 256);

            FBPerformanceInfoProcessor fbPerformanceInfoProcessor = new FBPerformanceInfoProcessor();
            performanceInfo = fbPerformanceInfoProcessor.process(databaseInfo);
        }

        return performanceInfo;
    }

    @Override
    public String getLastExecutedPlan(ResultSet resultSet) throws SQLException {
        FBResultSet fbResultSet = (FBResultSet)resultSet;
        return fbResultSet.getExecutionPlan();
    }

}
