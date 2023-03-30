package org.executequery.gui.browser;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.log.Log;

import java.sql.ResultSet;
import java.sql.SQLException;

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
