package org.executequery.gui.browser;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.log.Log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class responsible for the interaction of external modules with the profiler.
 *
 * @author Alexey Kozlov
 */
public class DefaultProfilerExecutor {

    private static final String START_SESSION =
            "SELECT RDB$PROFILER.START_SESSION('%s') FROM RDB$DATABASE";
    private static final String PAUSE_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.PAUSE_SESSION(TRUE)";
    private static final String RESUME_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.RESUME_SESSION";
    private static final String FINISH_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.FINISH_SESSION(TRUE)";
    private static final String CANCEL_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.CANCEL_SESSION";
    private static final String DISCARD =
            "EXECUTE PROCEDURE RDB$PROFILER.DISCARD";

    private final DefaultStatementExecutor executor;

    public DefaultProfilerExecutor(DatabaseConnection connection) {
        executor = new DefaultStatementExecutor();
        executor.setDatabaseConnection(connection);
        executor.setKeepAlive(true);
        executor.setCommitMode(false);
    }

    /**
     * @return profiler session <code>id</code> or '<code>-1</code>' if failed
     */
    public int startSession() throws SQLException {

        if (!isVersionSupported(executor.getDatabaseConnection()))
            return -1;

        String query = String.format(START_SESSION, executor.getDatabaseConnection().getName() + "_session");
        ResultSet resultSet = executor.execute(query, true).getResultSet();
        int sessionId = resultSet.next() ? resultSet.getInt(1) : -1;
        executor.getConnection().commit();
        executor.releaseResources();

        Log.info("Profiler session started");
        return sessionId;
    }

    public void pauseSession() throws SQLException {
        executeAndReleaseResources(PAUSE_SESSION);
        Log.info("Profiler session paused");
    }

    public void resumeSession() throws SQLException {
        executeAndReleaseResources(RESUME_SESSION);
        Log.info("Profiler session resumed");
    }

    public void finishSession() throws SQLException {
        executeAndReleaseResources(FINISH_SESSION);
        Log.info("Profiler session finished");
    }

    public void cancelSession() throws SQLException {
        executeAndReleaseResources(CANCEL_SESSION);
        Log.info("Profiler session canceled");
    }

    public void discardSession() throws SQLException {
        executeAndReleaseResources(DISCARD);
        Log.info("Profiler sessions discarded");
    }

    public List<ProfilerPanel.ProfilerData> getProfilerData(int sessionId, boolean showProfilerProcesses) {

        String query = "SELECT DISTINCT\n" +
                "REQ.REQUEST_ID,\n" +
                "REQ.CALLER_REQUEST_ID CALLER_ID,\n" +
                "STA.PACKAGE_NAME,\n" +
                "STA.ROUTINE_NAME,\n" +
                "STA.SQL_TEXT,\n" +
                "STA.TOTAL_ELAPSED_TIME TOTAL_TIME,\n" +
                "STA.AVG_ELAPSED_TIME AVG_TIME,\n" +
                "STA.COUNTER\n" +
                "FROM PLG$PROF_STATEMENT_STATS_VIEW STA\n" +
                "JOIN PLG$PROF_REQUESTS REQ ON\n" +
                "STA.PROFILE_ID = REQ.PROFILE_ID AND STA.STATEMENT_ID = REQ.STATEMENT_ID\n" +
                "WHERE STA.PROFILE_ID = '" + sessionId + "'\n" +
                (showProfilerProcesses ? "" :
                        "AND (STA.PACKAGE_NAME IS NULL OR STA.PACKAGE_NAME NOT CONTAINING 'RDB$PROFILER')\n" +
                                "AND (STA.SQL_TEXT IS NULL OR STA.SQL_TEXT NOT CONTAINING 'RDB$PROFILER')\n"
                ) +
                "ORDER BY REQ.CALLER_REQUEST_ID;";

        List<ProfilerPanel.ProfilerData> profilerDataList = new LinkedList<>();
        try {

            ResultSet rs = executor.execute(query, true).getResultSet();
            while (rs.next()) {

                int id = rs.getInt(1);
                int callerId = rs.getInt(2);
                String packageName = rs.getNString(3);
                String routineName = rs.getNString(4);
                String sqlText = rs.getNString(5);
                long totalTime = rs.getLong(6);
                long avgTime = rs.getLong(7);
                long callCount = rs.getLong(8);

                ProfilerPanel.ProfilerData data = sqlText != null ?
                        new ProfilerPanel.ProfilerData(id, callerId, sqlText, totalTime, avgTime, callCount) :
                        new ProfilerPanel.ProfilerData(id, callerId, packageName, routineName, totalTime, avgTime, callCount);

                if (profilerDataList.stream().noneMatch(obj -> obj.isSame(data)))
                    profilerDataList.add(data);

            }
            executor.getConnection().commit();

        } catch (SQLException | NullPointerException e) {
            Log.error("Error loading profiler data", e);
        }

        executor.releaseResources();
        return profilerDataList;
    }

    private static boolean isVersionSupported(DatabaseConnection connection) {

        boolean isSupported;

        try {

            DefaultDatabaseHost dbHost = new DefaultDatabaseHost(connection);
            isSupported = dbHost.getDatabaseMajorVersion() > 4;
            dbHost.close();

        } catch (SQLException e) {
            e.printStackTrace();
            isSupported = false;
        }

        return isSupported;
    }

    private void executeAndReleaseResources(String query) throws SQLException {
        executor.execute(query, true);
        executor.getConnection().commit();
        executor.releaseResources();
    }

}
