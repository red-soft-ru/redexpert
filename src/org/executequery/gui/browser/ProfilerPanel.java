package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 * @author Alexey Kozlov
 */
public class ProfilerPanel extends JPanel
        implements TabView {

    public final static String TITLE = bundleString("title");

    private static final int ACTIVE = 0;
    private static final int PAUSED = ACTIVE + 1;
    private static final int INACTIVE = PAUSED + 1;

    // --- profiler commands ---

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
    private static final String FLASH =
            "EXECUTE PROCEDURE RDB$PROFILER.FLUSH";

    private static final String SELECT_FROM_PROF_STATEMENT_STATS_VIEW =
            "SELECT * FROM PLG$PROF_STATEMENT_STATS_VIEW WHERE PROFILE_ID = %s";

//    private static final String DEFAULT_SELECT_PROFILER_DATA_QUERY =
//            "SELECT req.statement_id,\n" +
//                    "\tsta.statement_type,\n" +
//                    "\tsta.package_name,\n" +
//                    "\tsta.routine_name,\n" +
//                    "\tsta.parent_statement_id,\n" +
//                    "\tsta_parent.statement_type parent_statement_type,\n" +
//                    "\tsta_parent.routine_name parent_routine_name,\n" +
//                    "\t(SELECT sql_text\n" +
//                        "\t\tFROM plg$prof_statements\n" +
//                        "\t\tWHERE profile_id = req.profile_id AND\n" +
//                        "\t\t\tstatement_id = COALESCE(sta.parent_statement_id, req.statement_id)\n" +
//                    "\t) sql_text,\n" +
//                    "\tcount(*) counter,\n" +
//                    "\tmin(req.total_elapsed_time) min_elapsed_time,\n" +
//                    "\tmax(req.total_elapsed_time) max_elapsed_time,\n" +
//                    "\tcast(sum(req.total_elapsed_time) as bigint) total_elapsed_time,\n" +
//                    "\tcast(sum(req.total_elapsed_time) / count(*) as bigint) avg_elapsed_time\n" +
//            "FROM plg$prof_requests req\n" +
//            "JOIN plg$prof_statements sta\n" +
//                    "\tON sta.profile_id = req.profile_id AND\n" +
//                    "\t\tsta.statement_id = req.statement_id\n" +
//            "LEFT JOIN plg$prof_statements sta_parent\n" +
//                    "\tON sta_parent.profile_id = sta.profile_id AND\n" +
//                    "\t\tsta_parent.statement_id = sta.parent_statement_id\n" +
//            "WHERE req.profile_id = %s\n" +
//            "GROUP BY req.profile_id,\n" +
//                    "\treq.statement_id,\n" +
//                    "\tsta.statement_type,\n" +
//                    "\tsta.package_name,\n" +
//                    "\tsta.routine_name,\n" +
//                    "\tsta.parent_statement_id,\n" +
//                    "\tsta_parent.statement_type,\n" +
//                    "\tsta_parent.routine_name\n" +
//            "ORDER BY req.statement_id ASCENDING;";

    // --- GUI objects ---

    private JComboBox<?> connectionsComboBox;
    private JSpinner flashIntervalSpinner;

    private ResultSetTable resultSetTable;
    private ResultSetTableModel resultSetTableModel;

    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton finishButton;
    private JButton cancelButton;
    private JButton discardButton;

    // ---

    private DatabaseConnection connection;
    private DefaultStatementExecutor executor;
    private TableSelectionCombosGroup combosGroup;

    private String sessionName;
    private Integer sessionId;
    private int sessionState;
    private int flashInterval;

    public ProfilerPanel() {
        init();
    }

    private void init() {

        executor = new DefaultStatementExecutor();
        executor.setCommitMode(false);
        executor.setKeepAlive(true);

        connectionsComboBox = WidgetFactory.createComboBox();
        connectionsComboBox.setModel(
                new DynamicComboBoxModel(new Vector<>(ConnectionManager.getActiveConnections()))
        );
        connectionsComboBox.addActionListener(e -> connectionChange());
        combosGroup = new TableSelectionCombosGroup(connectionsComboBox);

        flashIntervalSpinner = new JSpinner();
        flashIntervalSpinner.setModel(new SpinnerNumberModel(5, 0, 120, 1));

        startButton = new JButton(bundleString("Start"));
        startButton.addActionListener(e -> startSession());

        pauseButton = new JButton(bundleString("Pause"));
        pauseButton.addActionListener(e -> pauseSession());

        resumeButton = new JButton(bundleString("Resume"));
        resumeButton.addActionListener(e -> resumeSession());

        finishButton = new JButton(bundleString("Stop"));
        finishButton.addActionListener(e -> finishSession());

        cancelButton = new JButton(bundleString("Cancel"));
        cancelButton.addActionListener(e -> cancelSession());

        discardButton = new JButton(bundleString("Discard"));
        discardButton.addActionListener(e -> discardSession());

        buildResultSetTable();
        arrangeComponents();
        switchSessionState(INACTIVE);
    }

    private void arrangeComponents() {

        // --- button panel ---

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(0, 0, 5, 0).anchorNorthWest();
        JPanel buttonPanel = new JPanel(new GridBagLayout());

        buttonPanel.add(startButton, gridBagHelper.get());
        buttonPanel.add(pauseButton, gridBagHelper.nextCol().get());
        buttonPanel.add(resumeButton, gridBagHelper.nextCol().get());
        buttonPanel.add(finishButton, gridBagHelper.nextCol().get());
        buttonPanel.add(cancelButton, gridBagHelper.nextCol().get());
        buttonPanel.add(discardButton, gridBagHelper.nextCol().get());

        // --- tools panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 0, 5, 0).anchorNorthWest().fillHorizontally();
        JPanel toolsPanel = new JPanel(new GridBagLayout());

        gridBagHelper.addLabelFieldPair(toolsPanel,
                bundleString("Connection"), connectionsComboBox, null, false, false);
        gridBagHelper.addLabelFieldPair(toolsPanel,
                bundleString("FlashInterval"), flashIntervalSpinner, null, false, false);
        toolsPanel.add(buttonPanel, gridBagHelper.nextCol().get());

        // --- resultSet panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.anchorNorthWest().fillBoth();
        JPanel resultSetPanel = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(resultSetTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        resultSetPanel.add(scrollPane, gridBagHelper.spanX().spanY().get());

        // --- main panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest();

        setLayout(new GridBagLayout());
        add(toolsPanel, gridBagHelper.fillHorizontally().spanX().get());
        add(resultSetPanel, gridBagHelper.nextRowFirstCol().setMaxWeightY().fillBoth().spanX().spanY().get());

    }

    private void buildResultSetTable() {

        try {

            resultSetTableModel = new ResultSetTableModel(false);
            resultSetTableModel.setHoldMetaData(false);
            resultSetTableModel.setFetchAll(true);

            if (resultSetTable == null)
                resultSetTable = new ResultSetTable(resultSetTableModel);
            else
                resultSetTable.setModel(resultSetTableModel);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorBuildingTable"), e);
        }

    }

    private void connectionChange() {
        connection = combosGroup.getSelectedHost().getDatabaseConnection();
    }

    private boolean isConnectionVersionSupported(DatabaseConnection connection) {

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

    private synchronized void monitorProfilerData() {
        try {

            DefaultStatementExecutor flashExecutor = new DefaultStatementExecutor();
            flashExecutor.setDatabaseConnection(connection);
            flashExecutor.setCommitMode(false);
            flashExecutor.setKeepAlive(true);

            String formattedQuery = String.format(SELECT_FROM_PROF_STATEMENT_STATS_VIEW, sessionId);

            while (sessionState == ACTIVE) {

                executor.execute(QueryTypes.EXECUTE, executor.getPreparedStatement(FLASH));
                ResultSet rs = flashExecutor.getResultSet(formattedQuery).getResultSet();

                resultSetTableModel.createTable(rs);
                flashExecutor.releaseResources();
                Thread.sleep(flashInterval * 1000L);
            }

        } catch (Exception e) {
            Log.error(bundleString("ErrorUpdatingData"), e);
        }
    }

    // --- button handlers ---

    private void startSession() {

        connectionChange();
        if (!isConnectionVersionSupported(connection)) {
            GUIUtilities.displayWarningMessage(bundleString("VersionNotSupported"));
            return;
        }

        executor.setDatabaseConnection(connection);

        sessionName = connection.getName() + "_session";
        flashInterval = (int) flashIntervalSpinner.getValue();
        String query = String.format(START_SESSION, sessionName);

        try {

            ResultSet resultSet = executor.execute(query, true).getResultSet();
            if (resultSet.next())
                sessionId = resultSet.getInt(1);
            resultSetTableModel.createTable(resultSet);
            executor.getConnection().commit();

            switchSessionState(ACTIVE);
            SwingWorker worker = new SwingWorker("Profiler data updater") {
                @Override
                public Object construct() {
                    monitorProfilerData();
                    return null;
                }
            };
            worker.start();

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionStart"), e);
        }
    }

    private void pauseSession() {
        try {

            executor.execute(PAUSE_SESSION, true);
            executor.getConnection().commit();
            switchSessionState(PAUSED);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionPause"), e);
        }
    }

    private void resumeSession() {
        try {

            executor.execute(RESUME_SESSION, true);
            executor.getConnection().commit();

            switchSessionState(ACTIVE);
            SwingWorker worker = new SwingWorker("Profiler data updater") {
                @Override
                public Object construct() {
                    monitorProfilerData();
                    return null;
                }
            };
            worker.start();

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionResume"), e);
        }
    }

    private void finishSession() {
        try {

            executor.execute(FINISH_SESSION, true);
            executor.getConnection().commit();
            switchSessionState(INACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionFinish"), e);
        }
    }

    private void cancelSession() {
        try {

            executor.execute(CANCEL_SESSION, true);
            executor.getConnection().commit();
            switchSessionState(INACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionCancel"), e);
        }
    }

    private void discardSession() {
        try {

            executor.execute(DISCARD, true);
            executor.getConnection().commit();
            switchSessionState(INACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionDiscard"), e);
        }
    }

    // ---

    private void switchSessionState(int state) {

        sessionState = state;

        switch (state) {
            case ACTIVE:
                startButton.setEnabled(false);
                pauseButton.setEnabled(true);
                resumeButton.setEnabled(false);
                finishButton.setEnabled(true);
                cancelButton.setEnabled(true);
                break;
            case PAUSED:
                startButton.setEnabled(false);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(true);
                finishButton.setEnabled(true);
                cancelButton.setEnabled(true);
                break;
            case INACTIVE:
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(false);
                finishButton.setEnabled(false);
                cancelButton.setEnabled(false);
                break;
        }
    }

    @Override
    public boolean tabViewClosing() {
        return true;
    }

    @Override
    public boolean tabViewSelected() {
        return true;
    }

    @Override
    public boolean tabViewDeselected() {
        return true;
    }

    private static String bundleString(String key) {
        return Bundles.get(ProfilerPanel.class, key);
    }
}
