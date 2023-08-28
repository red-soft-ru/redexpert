package org.executequery.gui.browser.profiler;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.log.Log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class responsible for the interaction of external modules with the profiler.
 *
 * @author Alexey Kozlov
 */
public class DefaultProfilerExecutor {

    private static final String START_SESSION =
            "SELECT RDB$PROFILER.START_SESSION('%s', NULL, %s, NULL, 'DETAILED_REQUESTS') FROM RDB$DATABASE";
    private static final String PAUSE_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.PAUSE_SESSION(TRUE, %s)";
    private static final String RESUME_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.RESUME_SESSION(%s)";
    private static final String FINISH_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.FINISH_SESSION(TRUE, %s)";
    private static final String CANCEL_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.CANCEL_SESSION(%s)";
    private static final String DISCARD =
            "EXECUTE PROCEDURE RDB$PROFILER.DISCARD(%s)";

    private final DefaultStatementExecutor executor;
    private final String attachmentId;
    private int sessionId;

    public DefaultProfilerExecutor(DatabaseConnection connection, String attachmentId) {

        this.attachmentId = attachmentId != null ? attachmentId : "(SELECT CURRENT_CONNECTION FROM RDB$DATABASE)";

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

        String query = String.format(START_SESSION, executor.getDatabaseConnection().getName() + "_session", attachmentId);
        ResultSet resultSet = executor.execute(query, true).getResultSet();
        sessionId = resultSet.next() ? resultSet.getInt(1) : -1;
        executor.getConnection().commit();
        executor.releaseResources();

        Log.info(String.format("Profiler session with ID %s started", sessionId));
        return sessionId;
    }

    /**
     * Pause current profiler session
     */
    public void pauseSession() throws SQLException {
        executeAndReleaseResources(String.format(PAUSE_SESSION, attachmentId));
        Log.info(String.format("Profiler session with ID %s paused", sessionId));
    }

    /**
     * Resume current profiler session
     */
    public void resumeSession() throws SQLException {
        executeAndReleaseResources(String.format(RESUME_SESSION, attachmentId));
        Log.info(String.format("Profiler session with ID %s resumed", sessionId));
    }

    /**
     * Finish current profiler session
     */
    public void finishSession() throws SQLException {
        executeAndReleaseResources(String.format(FINISH_SESSION, attachmentId));
        Log.info(String.format("Profiler session with ID %s finished", sessionId));
    }

    /**
     * Cancel current profiler session
     */
    public void cancelSession() throws SQLException {
        executeAndReleaseResources(String.format(CANCEL_SESSION, attachmentId));
        Log.info(String.format("Profiler session with ID %s canceled", sessionId));
    }

    /**
     * Discard all profiler session
     */
    public void discardSession() throws SQLException {
        executeAndReleaseResources(String.format(DISCARD, attachmentId));
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
                "PSQL.LINE_NUM,\n" +
                "PSQL.COUNTER,\n" +
                "PSQL.TOTAL_ELAPSED_TIME LINE_TIME,\n" +
                "REQ.TOTAL_ELAPSED_TIME TOTAL_TIME,\n" +
                "PROC.RDB$PROCEDURE_SOURCE,\n" +
                "FUNC.RDB$FUNCTION_SOURCE\n" +
                "FROM PLG$PROF_REQUESTS REQ\n" +
                "LEFT OUTER JOIN PLG$PROF_STATEMENTS STA USING (PROFILE_ID, STATEMENT_ID)\n" +
                "LEFT OUTER JOIN PLG$PROF_PSQL_STATS PSQL USING (PROFILE_ID, REQUEST_ID)\n" +
                "LEFT OUTER JOIN RDB$PROCEDURES PROC ON STA.ROUTINE_NAME = RDB$PROCEDURE_NAME\n" +
                "LEFT OUTER JOIN RDB$FUNCTIONS FUNC ON STA.ROUTINE_NAME = RDB$FUNCTION_NAME\n" +
                "WHERE STA.PROFILE_ID = '" + sessionId + "'\n" +
                (showProfilerProcesses ? "" :
                        "AND (STA.PACKAGE_NAME IS NULL OR STA.PACKAGE_NAME NOT CONTAINING 'RDB$PROFILER')\n" +
                                "AND (STA.SQL_TEXT IS NULL OR STA.SQL_TEXT NOT CONTAINING 'RDB$PROFILER')\n"
                ) +
                "ORDER BY REQ.CALLER_REQUEST_ID, REQ.REQUEST_ID, PSQL.LINE_NUM;";

        List<ProfilerData> profilerDataList = new LinkedList<>();
        try {

            int oldId = -1;
            int previousIndex = -1;
            List<ProfilerData.PsqlLine> psqlStats = new ArrayList<>();

            ResultSet rs = executor.execute(query, true).getResultSet();
            while (rs.next()) {

                int id = rs.getInt(1);
                int callerId = rs.getInt(2);
                String packageName = rs.getNString(3);
                String routineName = rs.getNString(4);
                String sqlText = rs.getNString(5);
                String statementType = rs.getNString(6);
                int lineNumber = rs.getInt(7);
                int lineCounter = rs.getInt(8);
                long lineTime = rs.getLong(9);
                long totalTime = rs.getLong(10);
                String sourceCode = rs.getNString(11) != null ? rs.getNString(11) : rs.getNString(12);

                if (oldId != id) {

                    if (previousIndex != -1)
                        psqlStats = new ArrayList<>();

                    ProfilerData data = sqlText != null ?
                            new ProfilerData(id, callerId, sqlText, statementType, null, totalTime) :
                            new ProfilerData(id, callerId, packageName, routineName, statementType, sourceCode, totalTime);
                    profilerDataList.add(data);

                    previousIndex = profilerDataList.size() - 1;
                    oldId = id;
                }

                if (lineNumber != 0 || lineCounter != 0 || lineTime != 0) {
                    psqlStats.add(new ProfilerData.PsqlLine(lineNumber, lineTime, lineCounter));
                    profilerDataList.get(previousIndex).setPsqlStats(psqlStats);
                }

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
            e.printStackTrace(System.out);
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
