package org.executequery.gui.browser;

import biz.redsoft.IFBBatch;
import biz.redsoft.IFBBatchCompletionState;
import biz.redsoft.IFBDatabaseConnection;
import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.gui.LoggingOutputPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.generatortestdata.FieldGenerator;
import org.executequery.gui.browser.generatortestdata.FieldsPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.DynamicLibraryLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class GeneratorTestDataPanel extends JPanel
        implements TabView {

    public static final String FRAME_ICON = "icon_generator";
    public static final String TITLE = bundleString("TITLE");

    // --- GUI components ---

    private JComboBox<?> tablesCombo;
    private JComboBox<?> connectionsCombo;

    private JButton stopButton;
    private JButton startButton;

    private NumberTextField batchSizeField;
    private NumberTextField commitAfterField;
    private NumberTextField recordsCountField;

    private JCheckBox useBatchesCheck;
    private JCheckBox stopOnErrorCheck;
    private JCheckBox loggingEnabledCheck;

    private JLabel batchLabel;
    private JTabbedPane tabbedPane;
    private FieldsPanel fieldsPanel;
    private JProgressBar progressBar;
    private LoggingOutputPanel logPanel;

    // ---

    private boolean stop;
    private DynamicComboBoxModel tablesModel;
    private DefaultStatementExecutor executor;

    public GeneratorTestDataPanel() {
        init();
        arrange();
        checkBatchToolsEnable();
        updateBatchToolsEnable();
        loadTableColumns();
    }

    private void init() {

        fieldsPanel = new FieldsPanel();
        progressBar = new JProgressBar();
        logPanel = new LoggingOutputPanel();
        batchLabel = new JLabel(bundleString("BatchCount"));

        tabbedPane = new JTabbedPane();
        tabbedPane.add(bundleString("Generator"), fieldsPanel);
        tabbedPane.add(bundleString("Output"), logPanel);

        connectionsCombo = WidgetFactory.createComboBox("connectionsCombo", ConnectionManager.getActiveConnections());
        connectionsCombo.setMinimumSize(new Dimension(400, WidgetFactory.defaultHeight()));
        connectionsCombo.addItemListener(this::connectionsComboTriggered);

        executor = new DefaultStatementExecutor();
        executor.setDatabaseConnection(getSelectedConnection());
        executor.setCommitMode(false);
        executor.setKeepAlive(true);

        stopButton = WidgetFactory.createButton("stopButton", bundleString("Stop"));
        stopButton.addActionListener(e -> stop = true);
        stopButton.setEnabled(false);

        startButton = WidgetFactory.createButton("startButton", bundleString("Start"));
        startButton.addActionListener(e -> start());

        recordsCountField = WidgetFactory.createNumberTextField("recordsCountField");
        recordsCountField.setEnableNegativeNumbers(false);
        recordsCountField.setText("100");

        batchSizeField = WidgetFactory.createNumberTextField("batchSizeField");
        batchSizeField.setEnableNegativeNumbers(false);
        batchSizeField.setText("100");

        commitAfterField = WidgetFactory.createNumberTextField("commitAfterField");
        commitAfterField.setEnableNegativeNumbers(false);
        commitAfterField.setText("500");

        stopOnErrorCheck = WidgetFactory.createCheckBox("stopOnErrorCheck", bundleString("StopOnError"));

        useBatchesCheck = WidgetFactory.createCheckBox("useBatchesCheck", bundleString("useBatchesBox"));
        useBatchesCheck.addActionListener(e -> updateBatchToolsEnable());

        loggingEnabledCheck = WidgetFactory.createCheckBox("loggingEnabledCheck", bundleString("OutputLog"));
        loggingEnabledCheck.setSelected(true);

        tablesModel = new DynamicComboBoxModel();
        tablesModel.setElements(getDatabaseTables());
        tablesCombo = WidgetFactory.createComboBox("tablesCombo", tablesModel);
        tablesCombo.addItemListener(this::tablesComboTriggered);
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- check panel ---

        JPanel checkBoxesPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper();
        checkBoxesPanel.add(loggingEnabledCheck, gbh.get());
        checkBoxesPanel.add(stopOnErrorCheck, gbh.nextCol().leftGap(5).get());
        checkBoxesPanel.add(useBatchesCheck, gbh.nextCol().get());
        checkBoxesPanel.add(new JPanel(), gbh.nextCol().spanX().get());

        // --- buttons panel ---

        JPanel buttonsPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().fillHorizontally();
        buttonsPanel.add(checkBoxesPanel, gbh.leftGap(1).setMaxWeightX().get());
        buttonsPanel.add(startButton, gbh.nextCol().setMinWeightX().fillNone().get());
        buttonsPanel.add(stopButton, gbh.nextCol().leftGap(5).get());

        // --- preferences panel ---

        JPanel preferencesPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().leftGap(5).fillHorizontally();
        preferencesPanel.add(new JLabel(Bundles.get("common.connection")), gbh.setMinWeightX().topGap(3).get());
        preferencesPanel.add(connectionsCombo, gbh.nextCol().setMaxWeightX().topGap(0).spanX().get());
        preferencesPanel.add(new JLabel(bundleString("Table")), gbh.setMinWeightX().setWidth(1).nextRowFirstCol().topGap(8).get());
        preferencesPanel.add(tablesCombo, gbh.nextCol().setMaxWeightX().topGap(5).spanX().get());
        preferencesPanel.add(new JLabel(bundleString("CountRecords")), gbh.setMinWeightX().setWidth(1).nextRowFirstCol().topGap(8).get());
        preferencesPanel.add(recordsCountField, gbh.nextCol().setMaxWeightX().topGap(5).spanX().get());
        preferencesPanel.add(new JLabel(bundleString("AfterCommit")), gbh.setMinWeightX().setWidth(1).nextRowFirstCol().topGap(8).get());
        preferencesPanel.add(commitAfterField, gbh.nextCol().setMaxWeightX().topGap(5).spanX().get());
        preferencesPanel.add(batchLabel, gbh.setMinWeightX().setWidth(1).nextRowFirstCol().topGap(8).get());
        preferencesPanel.add(batchSizeField, gbh.nextCol().setMaxWeightX().topGap(5).spanX().get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        mainPanel.add(preferencesPanel, gbh.spanX().get());
        mainPanel.add(buttonsPanel, gbh.nextRow().topGap(5).get());
        mainPanel.add(tabbedPane, gbh.nextRow().setMaxWeightY().fillBoth().get());
        mainPanel.add(progressBar, gbh.nextRow().setMinWeightY().fillHorizontally().get());

        // --- base ---

        setLayout(new GridBagLayout());
        gbh = new GridBagHelper().fillBoth().setInsets(5, 5, 5, 5).spanY().spanX();
        add(mainPanel, gbh.get());
    }

    private void start() {
        new SwingWorker("TestDataGenerator") {

            @Override
            public Object construct() {
                tabbedPane.setSelectedIndex(1);
                runGeneration();
                return Constants.WORKER_SUCCESS;
            }

            @Override
            public void finished() {
                stopButton.setEnabled(false);
                startButton.setEnabled(true);
                progressBar.setValue(0);
            }

        }.start();
    }

    private void runGeneration() {

        int recordsCount = recordsCountField.getValue();
        if (recordsCount <= 0) {
            GUIUtilities.displayErrorMessage(bundleString("nothingToGenerate"));
            return;
        }

        List<FieldGenerator> selectedFields = new ArrayList<>();
        for (FieldGenerator fieldGenerator : fieldsPanel.getFieldGenerators()) {
            if (fieldGenerator.isSelectedField()) {
                selectedFields.add(fieldGenerator);
                fieldGenerator.setFirst();
            }
        }

        if (selectedFields.isEmpty()) {
            GUIUtilities.displayErrorMessage(bundleString("noColumnsSelected"));
            return;
        }

        stopButton.setEnabled(true);
        startButton.setEnabled(false);

        stop = false;
        progressBar.setMinimum(0);
        progressBar.setMaximum(recordsCount);

        long startTime = System.currentTimeMillis();
        try {
            if (useBatchesCheck.isSelected())
                generateUsingBatches(recordsCount, selectedFields);
            else
                generate(recordsCount, selectedFields);

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e, this.getClass());

        } finally {
            logPanel.append("Execution time: " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    private void generate(int recordsCount, List<FieldGenerator> selectedFields) throws SQLException {

        int commitAfter = commitAfterField.getValue();
        boolean isStopOnError = stopOnErrorCheck.isSelected();
        boolean loggingEnabled = loggingEnabledCheck.isSelected();

        int countError = 0;
        int countSuccess = 0;
        int lastErrorIndex = -1;
        String lastErrorMessage = null;
        boolean isGenerationError = false;

        PreparedStatement statement = executor.getPreparedStatement(getInsertQuery(selectedFields));
        for (int recordIndex = 0; recordIndex < recordsCount && !stop; recordIndex++) {
            progressBar.setValue(recordIndex);

            if (recordIndex % commitAfter == 0 && recordIndex != 0)
                executor.getConnection().commit();

            // > try to set generate and set values

            try {
                for (int fieldIndex = 0; fieldIndex < selectedFields.size(); fieldIndex++) {
                    Object parameter = selectedFields.get(fieldIndex).getMethodGeneratorPanel().getTestDataObject();
                    statement.setObject(fieldIndex + 1, parameter);
                }

                if (loggingEnabled && isGenerationError && lastErrorIndex > -1) {
                    if (recordIndex - lastErrorIndex + 1 > 1)
                        logPanel.appendError("Failed on [" + lastErrorIndex + ", " + (recordIndex - 1) + "] records");
                    else
                        logPanel.appendError("Failed on " + lastErrorIndex + " record");

                    lastErrorIndex = -1;
                }

                isGenerationError = false;

            } catch (SQLException e) {

                if (loggingEnabled && lastErrorIndex == -1) {
                    String errorMessage = e.getMessage();
                    if (!Objects.equals(errorMessage, lastErrorMessage)) {
                        lastErrorIndex = recordIndex;
                        lastErrorMessage = errorMessage;
                        logPanel.appendError(errorMessage);
                    }
                }

                isGenerationError = true;
                continue; // if there is an exception with generating data then go to the next iteration
            }

            // > try to insert generated values

            SqlStatementResult result = executor.execute(QueryTypes.INSERT, statement);
            if (result.isException()) {
                countError++;

                if (loggingEnabled && lastErrorIndex == -1) {
                    String errorMessage = result.getSqlException().getMessage();
                    if (!Objects.equals(errorMessage, lastErrorMessage)) {
                        lastErrorIndex = recordIndex;
                        lastErrorMessage = errorMessage;
                        logPanel.appendError(errorMessage);
                    }
                }

                if (isStopOnError) {
                    GUIUtilities.displayExceptionErrorDialog(
                            result.getSqlException().getMessage(),
                            result.getSqlException(),
                            this.getClass()
                    );
                    break;
                }

            } else {
                countSuccess++;
                if (loggingEnabled && lastErrorIndex > -1) {
                    if (recordIndex - lastErrorIndex + 1 > 1)
                        logPanel.appendError("Failed on [" + lastErrorIndex + ", " + (recordIndex - 1) + "] records");
                    else
                        logPanel.appendError("Failed on " + lastErrorIndex + " record");

                    lastErrorIndex = -1;
                }
            }
        }

        if (loggingEnabled && lastErrorIndex > -1) {
            if (recordsCount - lastErrorIndex + 1 > 1)
                logPanel.appendError("Failed on [" + lastErrorIndex + ", " + recordsCount + "] records");
            else
                logPanel.appendError("Failed on " + lastErrorIndex + " record");
        }

        executor.getConnection().commit();
        executor.releaseResources();

        logPanel.appendAction("Added: " + countSuccess + "\nFailed: " + countError);
        GUIUtilities.displayInformationMessage(bundleString("generationEndMessage", countSuccess, recordsCount, countError));
    }

    private void generateUsingBatches(int recordsCount, List<FieldGenerator> selectedFields) throws SQLException {

        int batchCount = batchSizeField.getValue();
        int commitAfter = commitAfterField.getValue();
        boolean loggingEnabled = loggingEnabledCheck.isSelected();

        Connection realConnection = ConnectionManager.getTemporaryConnection(getSelectedConnection());
        Connection fbConnection = realConnection.unwrap(Connection.class);
        if (!fbConnection.getClass().getName().contains("FBConnection")) {
            GUIUtilities.displayWarningMessage(bundleString("batchesNotSupported"));
            return;
        }

        IFBBatch batch;
        try {
            IFBDatabaseConnection db = (IFBDatabaseConnection) DynamicLibraryLoader.loadingObjectFromClassLoader(
                    getSelectedConnection().getDriverMajorVersion(),
                    fbConnection,
                    "FBDatabaseConnectionImpl4"
            );
            db.setConnection(fbConnection);
            batch = db.createBatch(getInsertQuery(selectedFields));

        } catch (ClassNotFoundException e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e, getClass());
            return;
        }

        int recordIndex = 0;
        while (recordIndex < recordsCount && !stop) {

            if (recordIndex + batchCount > recordsCount)
                batchCount = recordIndex + batchCount - recordsCount;

            for (int batchRecordIndex = 0; batchRecordIndex < batchCount; batchRecordIndex++) {
                for (int columnIndex = 0; columnIndex < selectedFields.size(); columnIndex++) {

                    String parameterType = selectedFields.get(columnIndex).getColumn().getTypeName();
                    Object parameterValue = selectedFields.get(columnIndex).getMethodGeneratorPanel().getTestDataObject();

                    if (parameterType.contains("BLOB")) {
                        if (((byte[]) parameterValue).length == 0)
                            parameterValue = new byte[1];
                        batch.addBlob(columnIndex + 1, (byte[]) parameterValue);

                    } else
                        batch.setObject(columnIndex + 1, parameterValue);
                }

                batch.addBatch();
            }

            IFBBatchCompletionState execute = batch.execute();
            if (loggingEnabled)
                logPanel.append(execute.printAllStates());

            if (recordIndex % commitAfter == 0 && recordIndex != 0) {
                batch.commit();
                batch.startTransaction();
            }

            recordIndex += batchCount;
            progressBar.setValue(recordIndex);
        }

        batch.commit();
        GUIUtilities.displayInformationMessage(bundleString("batchGenerationEndMessage"));
    }

    private String getInsertQuery(List<FieldGenerator> selectedFields) {
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();

        boolean first = true;
        for (FieldGenerator fieldGenerator : selectedFields) {
            if (!first) {
                fields.append(",");
                values.append(", ");
            }

            fields.append("\n   \"").append(fieldGenerator.getColumn().getName()).append("\"");
            values.append("?");

            first = false;
        }

        String query = String.format(
                "INSERT INTO \"%s\" (%s\n) VALUES (%s);",
                tablesCombo.getSelectedItem(),
                fields,
                values
        );

        logPanel.appendPlain("Prepared query:");
        logPanel.appendAction(query);

        return query;
    }

    private Vector<String> getDatabaseTables() {
        Vector<String> tables = new Vector<>();

        String query = "SELECT RDB$RELATION_NAME\n" +
                "FROM RDB$RELATIONS\n" +
                "WHERE RDB$VIEW_BLR IS NULL \n" +
                "AND (RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0)\n" +
                "AND RDB$RELATION_TYPE = 0 OR RDB$RELATION_TYPE = 2\n" +
                "ORDER BY RDB$RELATION_NAME";

        try {
            ResultSet rs = executor.getResultSet(query).getResultSet();
            while (rs.next())
                tables.add(rs.getString(1).trim());

            if (tables.isEmpty()) {
                tables.add(null);
                GUIUtilities.displayErrorMessage(bundleString("noTables"));
            }

        } catch (SQLException | NullPointerException e) {
            Log.error(e.getMessage(), e);

        } finally {
            executor.releaseResources();
        }

        return tables;
    }

    private void loadTableColumns() {

        String selectedTable = getSelectedTable();
        if (selectedTable == null)
            return;

        DatabaseHost host = null;
        JPanel tabComponent = GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        if (tabComponent instanceof ConnectionsTreePanel) {
            ConnectionsTreePanel connectionsTreePanel = (ConnectionsTreePanel) tabComponent;
            NamedObject namedObject = connectionsTreePanel.getHostNode(getSelectedConnection()).getDatabaseObject();
            host = (DatabaseHost) namedObject;
        }

        if (host == null)
            return;

        List<FieldGenerator> fieldGenerators = new ArrayList<>();
        for (DatabaseColumn column : host.getColumns(selectedTable))
            fieldGenerators.add(new FieldGenerator(column, executor));

        fieldsPanel.setFieldGenerators(fieldGenerators);
    }

    private void checkBatchToolsEnable() {
        boolean enable = new DefaultDriverLoader().load(getSelectedConnection().getJDBCDriver()).getMajorVersion() > 3
                && getSelectedConnection().getMajorServerVersion() > 3
                && getSelectedConnection().useNewAPI();

        useBatchesCheck.setEnabled(enable);
        useBatchesCheck.setSelected(false);
        updateBatchToolsEnable();
    }

    private void updateBatchToolsEnable() {
        boolean enable = useBatchesCheck.isSelected();
        batchLabel.setEnabled(enable);
        batchSizeField.setEnabled(enable);
    }

    private void tablesComboTriggered(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED)
            loadTableColumns();
    }

    private void connectionsComboTriggered(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED)
            return;

        executor.setDatabaseConnection(getSelectedConnection());
        tablesModel.setElements(getDatabaseTables());
        checkBatchToolsEnable();
    }

    private String getSelectedTable() {
        return (String) tablesCombo.getSelectedItem();
    }

    private DatabaseConnection getSelectedConnection() {
        return (DatabaseConnection) connectionsCombo.getSelectedItem();
    }

    // --- TabView impl ---

    @Override
    public boolean tabViewSelected() {
        return true;
    }

    @Override
    public boolean tabViewDeselected() {
        return true;
    }

    @Override
    public boolean tabViewClosing() {
        return true;
    }

    // ---

    private static String bundleString(String key, Object... args) {
        return Bundles.get(GeneratorTestDataPanel.class, key, args);
    }

}
