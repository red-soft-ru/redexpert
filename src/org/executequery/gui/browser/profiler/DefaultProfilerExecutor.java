package org.executequery.gui.browser.profiler;

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
     * Start new profiler session
     *
     * @return profiler session id or '-1' if failed
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

    /**
     * Pause current profiler session
     */
    public void pauseSession() throws SQLException {
        executeAndReleaseResources(PAUSE_SESSION);
        Log.info("Profiler session paused");
    }

    /**
     * Resume current profiler session
     */
    public void resumeSession() throws SQLException {
        executeAndReleaseResources(RESUME_SESSION);
        Log.info("Profiler session resumed");
    }

    /**
     * Finish current profiler session
     */
    public void finishSession() throws SQLException {
        executeAndReleaseResources(FINISH_SESSION);
        Log.info("Profiler session finished");
    }

    /**
     * Cancel current profiler session
     */
    public void cancelSession() throws SQLException {
        executeAndReleaseResources(CANCEL_SESSION);
        Log.info("Profiler session canceled");
    }

    /**
     * Discard all profiler session
     */
    public void discardSession() throws SQLException {
        executeAndReleaseResources(DISCARD);
        Log.info("Profiler sessions discarded");
    }

    /**
     * Get list of filtered profiler data from system tables
     *
     * @param sessionId             profiler session id
     * @param showProfilerProcesses whether to include data about profiler operations in the result
     * @return linked list of profiler data
     */
    public List<ProfilerData> getProfilerData(int sessionId, boolean showProfilerProcesses) {

        String query = "SELECT DISTINCT\n" +
                "REQ.REQUEST_ID,\n" +
                "REQ.CALLER_REQUEST_ID CALLER_ID,\n" +
                "STA.PACKAGE_NAME,\n" +
                "STA.ROUTINE_NAME,\n" +
                "STA.SQL_TEXT,\n" +
                "STA.STATEMENT_TYPE,\n" +
                "REQ.TOTAL_ELAPSED_TIME TOTAL_TIME\n" +
                "FROM PLG$PROF_REQUESTS REQ\n" +
                "LEFT OUTER JOIN PLG$PROF_STATEMENTS STA USING (PROFILE_ID, STATEMENT_ID)\n" +
                "WHERE STA.PROFILE_ID = '" + sessionId + "'\n" +
                (showProfilerProcesses ? "" :
                        "AND (STA.PACKAGE_NAME IS NULL OR STA.PACKAGE_NAME NOT CONTAINING 'RDB$PROFILER')\n" +
                                "AND (STA.SQL_TEXT IS NULL OR STA.SQL_TEXT NOT CONTAINING 'RDB$PROFILER')\n"
                ) +
                "ORDER BY REQ.CALLER_REQUEST_ID, REQ.REQUEST_ID;";

        List<ProfilerData> profilerDataList = new LinkedList<>();
        try {

            ResultSet rs = executor.execute(query, true).getResultSet();
            while (rs.next()) {

                int id = rs.getInt(1);
                int callerId = rs.getInt(2);
                String packageName = rs.getNString(3);
                String routineName = rs.getNString(4);
                String sqlText = rs.getNString(5);
                String statementType = rs.getNString(6);
                long totalTime = rs.getLong(7);

                ProfilerData data = sqlText != null ?
                        new ProfilerData(id, callerId, sqlText, statementType, totalTime) :
                        new ProfilerData(id, callerId, packageName, routineName, statementType, totalTime);

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
