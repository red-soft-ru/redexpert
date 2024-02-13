package biz.redsoft;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBResultSet;
import org.firebirdsql.jdbc.FBStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Vasiliy on 4/6/2017.
 */
public class FBDatabasePerformanceImpl implements IFBDatabasePerformance {

    byte[] perfomanceInfoBytes =
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
    public FBPerformanceInfoImpl getPerformanceInfo(int driverMajorVersion) throws SQLException {
        byte[] databaseInfo = null;
        FBPerformanceInfoImpl performanceInfo = null;
        if (driverMajorVersion >= 3) {

            databaseInfo = fbConnection.getFbDatabase().getDatabaseInfo(perfomanceInfoBytes, 256);

            FBPerformanceInfoProcessorImpl fbPerformanceInfoProcessor = new FBPerformanceInfoProcessorImpl();
            performanceInfo = fbPerformanceInfoProcessor.process(databaseInfo);
        }

        return performanceInfo;
    }

    @Override
    public String getLastExecutedPlan(ResultSet resultSet) throws SQLException {
        FBResultSet fbResultSet = (FBResultSet) resultSet;
        return fbResultSet.getExecutionPlan();
    }

    @Override
    public String getLastExecutedPlan(Statement st) throws SQLException {
        FBStatement fbStatement = (FBStatement) st;
        return fbStatement.getLastExecutionPlan();
    }

    @Override
    public String getLastExplainExecutedPlan(ResultSet rs) throws SQLException {
        FBResultSet fbResultSet = (FBResultSet) rs;
        return fbResultSet.getExplainedExecutionPlan();
    }

    @Override
    public String getLastExplainExecutedPlan(Statement st) throws SQLException {
        FBStatement fbStatement = (FBStatement) st;
        return fbStatement.getLastExplainedExecutionPlan();
    }

}
