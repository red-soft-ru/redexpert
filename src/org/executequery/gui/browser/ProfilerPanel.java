package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.localization.Bundles;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class ProfilerPanel extends JPanel
        implements TabView {

    public final static String TITLE = bundleString("title");

    // --- profiler commands ---

    private static final String START_SESSION_PATTERN =
            "SELECT RDB$PROFILER.START_SESSION('%s') FROM RDB$DATABASE";
    private static final String FINISH_SESSION_QUERY =
            "EXECUTE PROCEDURE RDB$PROFILER.FINISH_SESSION(true)";

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
    private boolean isSessionActive;

    public ProfilerPanel() {
        init();
    }

    private void init() {

        connectionsComboBox = WidgetFactory.createComboBox();
        connectionsComboBox.setModel(
                new DynamicComboBoxModel(new Vector<>(ConnectionManager.getActiveConnections()))
        );
        connectionsComboBox.addActionListener(e -> connectionChange());
        combosGroup = new TableSelectionCombosGroup(connectionsComboBox);

        flashIntervalSpinner = new JSpinner();
        flashIntervalSpinner.setModel(new SpinnerNumberModel(1, 1, 120, 1));

        startButton = new JButton("Start");
        startButton.addActionListener(e -> startSession());

        pauseButton = new JButton("Pause");
        //TODO button handler

        resumeButton = new JButton("Resume");
        //TODO button handler

        finishButton = new JButton("Stop");
        finishButton.addActionListener(e -> finishSession());

        cancelButton = new JButton("Cancel");
        //TODO button handler

        discardButton = new JButton("Discard");
        //TODO button handler

        buildResultSetTable();
        arrangeComponents();
    }

    private void arrangeComponents() {

        // --- button panel ---

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(0, 0, 5, 0).anchorNorthWest();
        JPanel buttonPanel = new JPanel(new GridBagLayout());

        buttonPanel.add(startButton, gridBagHelper.get());
//        buttonPanel.add(pauseButton, gridBagHelper.nextCol().get());
//        buttonPanel.add(resumeButton, gridBagHelper.nextCol().get());
        buttonPanel.add(finishButton, gridBagHelper.nextCol().get());
//        buttonPanel.add(cancelButton, gridBagHelper.nextCol().get());
//        buttonPanel.add(discardButton, gridBagHelper.nextCol().get());

        // --- tools panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 0, 5, 0).anchorNorthWest();
        JPanel toolsPanel = new JPanel(new GridBagLayout());

        gridBagHelper.addLabelFieldPair(toolsPanel, "Connection:", connectionsComboBox,
                null, false, false);
        gridBagHelper.addLabelFieldPair(toolsPanel, "Flash interval:", flashIntervalSpinner,
                null, false, false);
        toolsPanel.add(buttonPanel, gridBagHelper.nextCol().get());

        // --- resultSet panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.anchorNorthWest();
        JPanel resultSetPanel = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(resultSetTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        resultSetPanel.add(scrollPane);

        // --- main panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest();
        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(toolsPanel, gridBagHelper.fillHorizontally().spanX().get());
        mainPanel.add(resultSetPanel, gridBagHelper.nextRowFirstCol().fillBoth().setMaxWeightY().spanX().get());

        // ---

        add(mainPanel, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
    }

    private void buildResultSetTable() {

        try {

            resultSetTableModel = new ResultSetTableModel(
                    SystemProperties.getIntProperty("user", "browser.max.records"), false);
            resultSetTableModel.setHoldMetaData(false);

            if (resultSetTable == null)
                resultSetTable = new ResultSetTable(resultSetTableModel);
            else
                resultSetTable.setModel(resultSetTableModel);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog("Error occurred when building resultSetTable", e);
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

    // --- button handlers ---

    private void startSession() {

        connectionChange();
        if (!isConnectionVersionSupported(connection)) {
            GUIUtilities.displayWarningMessage("Unable to start profiler session\nDB version below 5.0");
            return;
        }

        executor = new DefaultStatementExecutor();
        executor.setCommitMode(false);
        executor.setKeepAlive(true);
        executor.setDatabaseConnection(connection);

        sessionName = connection.getName() + "_" + System.currentTimeMillis();
        String query = String.format(START_SESSION_PATTERN, sessionName);

        try {

            ResultSet resultSet = executor.execute(query, true).getResultSet();
            resultSetTableModel.createTable(resultSet);
            isSessionActive = true;

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog("Error occurred when starting profiler session", e);
        }
    }

    private void finishSession() {

        if (!isSessionActive)
            return;

        try {

            executor.execute(FINISH_SESSION_QUERY, true);
            executor.getConnection().commit();
            isSessionActive = false;

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog("Error occurred when finishing profiler session", e);
        }
    }

    // ---

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
