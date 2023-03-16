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
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
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

    // --- selected fields ---

    private static final String PROFILE_ID = "profile_id";
    private static final String STATEMENT_ID = "statement_id";
    private static final String STATEMENT_TYPE = "statement_type";
    private static final String PACKAGE_NAME = "package_name";
    private static final String ROUTINE_NAME = "routine_name";
    private static final String PARENT_STATEMENT_ID = "parent_statement_id";
    private static final String PARENT_STATEMENT_TYPE = "parent_statement_type";
    private static final String PARENT_ROUTINE_NAME = "parent_routine_name";
    private static final String SQL_TEXT = "sql_text";
    private static final String COUNTER = "counter";
    private static final String MIN_ELAPSED_TIME = "min_elapsed_time";
    private static final String MAX_ELAPSED_TIME = "max_elapsed_time";
    private static final String TOTAL_ELAPSED_TIME = "total_elapsed_time";
    private static final String AVG_ELAPSED_TIME = "avg_elapsed_time";

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

    private Map<String, JCheckBox> selectFieldsCheckBoxList;

    private Integer sessionId;
    private int sessionState;
    private int flashInterval;

    private boolean isSelectQueryMarkedForUpdate;
    private String selectQuery;

    // ---

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
        flashIntervalSpinner.setModel(new SpinnerNumberModel(5, 1, 60, 1));

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

        selectFieldsCheckBoxList = new LinkedHashMap<>();
        selectFieldsCheckBoxList.put(PROFILE_ID, new JCheckBox(PROFILE_ID));
        selectFieldsCheckBoxList.put(STATEMENT_ID, new JCheckBox(STATEMENT_ID));
        selectFieldsCheckBoxList.put(STATEMENT_TYPE, new JCheckBox(STATEMENT_TYPE));
        selectFieldsCheckBoxList.put(PACKAGE_NAME, new JCheckBox(PACKAGE_NAME));
        selectFieldsCheckBoxList.put(ROUTINE_NAME, new JCheckBox(ROUTINE_NAME));
        selectFieldsCheckBoxList.put(PARENT_STATEMENT_ID, new JCheckBox(PARENT_STATEMENT_ID));
        selectFieldsCheckBoxList.put(PARENT_STATEMENT_TYPE, new JCheckBox(PARENT_STATEMENT_TYPE));
        selectFieldsCheckBoxList.put(PARENT_ROUTINE_NAME, new JCheckBox(PARENT_ROUTINE_NAME));
        selectFieldsCheckBoxList.put(SQL_TEXT, new JCheckBox(SQL_TEXT));
        selectFieldsCheckBoxList.put(COUNTER, new JCheckBox(COUNTER));
        selectFieldsCheckBoxList.put(MIN_ELAPSED_TIME, new JCheckBox(MIN_ELAPSED_TIME));
        selectFieldsCheckBoxList.put(MAX_ELAPSED_TIME, new JCheckBox(MAX_ELAPSED_TIME));
        selectFieldsCheckBoxList.put(TOTAL_ELAPSED_TIME, new JCheckBox(TOTAL_ELAPSED_TIME));
        selectFieldsCheckBoxList.put(AVG_ELAPSED_TIME, new JCheckBox(AVG_ELAPSED_TIME));
        selectFieldsCheckBoxList.values().forEach(item -> {
            item.setSelected(true);
            item.addActionListener(i -> selectFieldChange());
        });

        buildResultSetTable();
        arrangeComponents();
        switchSessionState(INACTIVE);
        rebuildSelectQuery();
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
        gridBagHelper.setInsets(5, 5, 5, 0).anchorNorthWest().fillHorizontally();
        JPanel toolsPanel = new JPanel(new GridBagLayout());

        gridBagHelper.addLabelFieldPair(toolsPanel,
                bundleString("Connection"), connectionsComboBox, null, false, false);
        toolsPanel.add(buttonPanel, gridBagHelper.nextCol().get());

        // --- checkBox panel ---

        GridBagHelper gbh = new GridBagHelper();
        JPanel checkBoxPanel = new JPanel(new GridBagLayout());
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder(bundleString("Fields")));

        gbh.setInsets(5, 5, 5, 5).anchorNorthWest();
        selectFieldsCheckBoxList.values().forEach(item -> checkBoxPanel.add(item, gbh.nextRowFirstCol().get()));

        // --- properties panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest();
        JPanel propertiesPanel = new JPanel(new GridBagLayout());

        propertiesPanel.add(new JLabel(bundleString("FlashInterval")), gridBagHelper.get());
        propertiesPanel.add(flashIntervalSpinner, gridBagHelper.nextCol().get());
        propertiesPanel.add(checkBoxPanel, gridBagHelper.nextRowFirstCol().setWidth(2).get());

        // --- resultSet panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.anchorNorthWest().fillBoth();
        JPanel resultSetPanel = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(resultSetTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        resultSetPanel.add(scrollPane, gridBagHelper.spanX().spanY().get());

        // --- profiler panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest();
        JPanel profilerPanel = new JPanel(new GridBagLayout());

        profilerPanel.add(toolsPanel, gridBagHelper.fillBoth().get());
        profilerPanel.add(resultSetPanel, gridBagHelper.nextRowFirstCol().spanX().spanY().get());

        // --- main panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();

        setLayout(new GridBagLayout());
        add(profilerPanel, gridBagHelper.setMaxWeightX().spanY().get());
        add(propertiesPanel, gridBagHelper.nextCol().setMinWeightX().spanY().get());

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

    private void selectFieldChange() {
        isSelectQueryMarkedForUpdate = true;
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

            if (isSelectQueryMarkedForUpdate)
                rebuildSelectQuery();

            if (!MiscUtils.isNull(selectQuery)) {

                String formattedQuery = String.format(selectQuery, sessionId);
                while (sessionState == ACTIVE) {

                    executor.execute(QueryTypes.EXECUTE, executor.getPreparedStatement(FLASH));

                    ResultSet rs = flashExecutor.getResultSet(formattedQuery).getResultSet();

                    resultSetTableModel.createTable(rs);
                    flashExecutor.releaseResources();
                    Thread.sleep(flashInterval * 1000L);
                }

            } else if (GUIUtilities.displayConfirmDialog(bundleString("ErrorNoFieldsForDisplay")) == JOptionPane.NO_OPTION)
                cancelSession();

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

        flashInterval = (int) flashIntervalSpinner.getValue();
        String query = String.format(START_SESSION, connection.getName() + "_session");

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

    private void rebuildSelectQuery() {

        StringBuilder sb = new StringBuilder();

        selectFieldsCheckBoxList.keySet().stream()
                .filter(key -> selectFieldsCheckBoxList.get(key).isSelected())
                .forEach(key -> sb.append(key).append(", "));

        if (!sb.toString().equals("")) {

            sb.insert(0, "SELECT ");
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("FROM PLG$PROF_STATEMENT_STATS_VIEW WHERE PROFILE_ID = %s");
            selectQuery = sb.toString();

        } else
            selectQuery = null;

        isSelectQueryMarkedForUpdate = false;
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
