/*
 * ExecuteSqlScriptPanel.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui;

import org.apache.commons.lang.StringUtils;
import org.executequery.ActiveComponent;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabViewActionPanel;
import org.executequery.components.FileChooserDialog;
import org.executequery.components.SplitPaneFactory;
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextFileWriter;
import org.executequery.localization.Bundles;
import org.executequery.sql.ExecutionController;
import org.executequery.sql.SqlScriptRunner;
import org.executequery.sql.SqlStatementResult;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Takis Diakoumis
 */
public class ExecuteSqlScriptPanel extends DefaultTabViewActionPanel
        implements NamedView,
        ActiveComponent,
        ExecutionController {

    public static final String TITLE = Bundles.get(ExecuteSqlScriptPanel.class, "title");
    public static final String FRAME_ICON = "icon_execute_script";

    public static final int MAX_LENGTH_TEXT_PANE = 1000000;
    private static final int STATUS_BAR_HEIGHT = 26;

    private static int instanceCounter = 1;

    // --- GUI components ---

    private ConnectionsComboBox connectionsCombo;

    private JCheckBox useConnectionCheck;
    private JCheckBox stopOnErrorCheck;
    private JCheckBox logOutputCheck;

    private JTextField fileNameField;
    private SimpleSqlTextPanel sqlText;
    private LoggingOutputPanel outputPanel;
    private SqlTextPaneStatusBar statusBar;

    private JButton saveScriptButton;
    private JButton stopButton;
    private JButton startButton;
    private JButton commitButton;
    private JButton rollbackButton;
    private JButton browseFileButton;

    private ProgressBar progressBar;

    // ---

    private String script = null;

    private boolean executing;
    private boolean resetButtons;

    private SwingWorker swingWorker;
    private SqlScriptRunner sqlScriptRunner;

    public ExecuteSqlScriptPanel() {
        super(new BorderLayout());
        init();
        arrange();
    }

    private void init() {

        // --- comboBoxes ---

        connectionsCombo = WidgetFactory.createConnectionComboBox("connectionsCombo", false);
        connectionsCombo.setEnabled(false);

        // --- checkBoxes ---

        logOutputCheck = WidgetFactory.createCheckBox("logOutputCheck", bundleString("logOutput"));
        logOutputCheck.setSelected(true);

        useConnectionCheck = WidgetFactory.createCheckBox("useConnectionCheck", bundleString("UseConnection"));
        useConnectionCheck.addItemListener(e -> connectionUsageChanged());
        useConnectionCheck.setSelected(false);

        stopOnErrorCheck = WidgetFactory.createCheckBox("stopOnErrorCheck", bundleString("stopOnErrorCheck"));
        stopOnErrorCheck.setSelected(true);

        // --- buttons ---

        browseFileButton = WidgetFactory.createButton("browseFileButton", bundleString("Browse"));
        browseFileButton.addActionListener(e -> browse());

        saveScriptButton = WidgetFactory.createButton("saveScriptButton", bundleString("SaveScript"));
        saveScriptButton.addActionListener(e -> saveScript());

        startButton = WidgetFactory.createButton("startButton", bundleString("Start"));
        startButton.addActionListener(e -> start());

        commitButton = WidgetFactory.createButton("commitButton", bundleString("Commit"));
        commitButton.addActionListener(e -> commit());
        commitButton.setEnabled(false);

        rollbackButton = WidgetFactory.createButton("rollbackButton", bundleString("Rollback"));
        rollbackButton.addActionListener(e -> rollback());
        rollbackButton.setEnabled(false);

        stopButton = WidgetFactory.createButton("stopButton", bundleString("Stop"));
        stopButton.addActionListener(e -> stop());
        stopButton.setEnabled(false);

        // --- others ---

        fileNameField = WidgetFactory.createTextField("fileNameField");
        fileNameField.setPreferredSize(browseFileButton.getPreferredSize());
        fileNameField.addActionListener(e -> fileNameChanged());

        sqlText = new SimpleSqlTextPanel(false, false);
        sqlText.setBorder(null);
        sqlText.setScrollPaneBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, UIUtils.getDefaultBorderColour()));

        statusBar = new SqlTextPaneStatusBar();
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 1));

        outputPanel = new LoggingOutputPanel();

    }

    private void arrange() {

        GridBagHelper gbh;

        // --- split pane ---

        JSplitPane splitPane = new SplitPaneFactory().create(JSplitPane.VERTICAL_SPLIT, sqlText, outputPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(400);
        splitPane.setDividerSize(5);

        // --- file panel ---

        JPanel filePanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally().rightGap(5);
        filePanel.add(new JLabel(bundleString("InputFile")), gbh.setMinWeightX().topGap(3).get());
        filePanel.add(fileNameField, gbh.nextCol().setMaxWeightX().topGap(0).get());
        filePanel.add(browseFileButton, gbh.nextCol().setMinWeightX().get());
        filePanel.add(saveScriptButton, gbh.nextCol().rightGap(0).get());

        // --- properties panel ---

        JPanel propertiesPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally().rightGap(5);
        propertiesPanel.add(stopOnErrorCheck, gbh.get());
        propertiesPanel.add(logOutputCheck, gbh.nextCol().get());
        propertiesPanel.add(useConnectionCheck, gbh.nextCol().get());
        propertiesPanel.add(connectionsCombo, gbh.nextCol().rightGap(0).spanX().get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        gbh = new GridBagHelper().anchorNorthWest().fillBoth().setInsets(5, 5, 5, 0);
        mainPanel.add(filePanel, gbh.fillBoth().spanX().get());
        mainPanel.add(propertiesPanel, gbh.nextRowFirstCol().spanX().get());
        mainPanel.add(splitPane, gbh.nextRowFirstCol().setMaxWeightY().spanX().get());
        mainPanel.add(statusBar, gbh.nextRowFirstCol().setMinWeightY().bottomGap(5).spanX().get());

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().fillHorizontally().anchorNorthWest();
        buttonPanel.add(new JPanel(), gbh.setMaxWeightX().get());
        buttonPanel.add(startButton, gbh.setMinWeightX().nextCol().get());
        buttonPanel.add(rollbackButton, gbh.nextCol().get());
        buttonPanel.add(commitButton, gbh.nextCol().get());
        buttonPanel.add(stopButton, gbh.nextCol().get());

        // --- base ---

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    private void enableButtons(boolean start, boolean stop, boolean commit, boolean rollback) {
        GUIUtils.invokeLater(() -> {
            startButton.setEnabled(start);
            stopButton.setEnabled(stop);
            commitButton.setEnabled(commit);
            rollbackButton.setEnabled(rollback);
        });
    }

    // --- handlers ---

    public void connectionUsageChanged() {

        boolean useConnection = useConnectionCheck.isSelected();

        connectionsCombo.setEnabled(useConnection);
        sqlText.getTextPane().setDatabaseConnection(useConnection ? getSelectedConnection() : null);
    }

    public void fileNameChanged() {
        try {
            script = FileUtils.loadFile(new File(fileNameField.getText()).getPath());
            if (script.length() < MAX_LENGTH_TEXT_PANE)
                sqlText.setSQLText(script);
            else
                GUIUtilities.displayWarningMessage(bundleString("FileLengthOverrun"));

        } catch (IOException e) {
            GUIUtilities.displayErrorMessage(bundleString("error.load-file"));
        }
    }

    public void saveScript() {
        new TextFileWriter(sqlText.getSQLText()).write();
    }

    public void browse() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("SQL", "sql"));

        fileChooser.setDialogTitle(bundleString("title.file-chooser"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), bundleString("Select"));
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        File file = fileChooser.getSelectedFile();
        fileNameField.setText(file.getAbsolutePath());
        fileNameChanged();
    }

    public void start() {

        if (executing || !fieldsValid())
            return;

        resetButtons = false;
        enableButtons(false, true, false, false);

        swingWorker = new SwingWorker("ExecuteSQLScript") {

            @Override
            public Object construct() {
                executing = true;
                return execute();
            }

            @Override
            public void finished() {
                try {
                    SqlStatementResult sqlStatementResult = (SqlStatementResult) get();
                    outputPanel.append("Statements executed: " + sqlStatementResult.getStatementCount());
                    outputPanel.append("Total records affected: " + sqlStatementResult.getUpdateCount());

                } finally {
                    executing = false;
                    enableButtons(true, false, !resetButtons, !resetButtons);
                }
            }
        };
        swingWorker.start();
    }

    public void stop() {

        if (!executing)
            return;

        if (swingWorker != null)
            swingWorker.interrupt();

        sqlScriptRunner.stop();
        executing = false;
    }

    public void commit() {

        if (sqlScriptRunner == null)
            return;

        try {
            sqlScriptRunner.commit();
            outputPanel.append("Commit complete");

        } catch (SQLException e) {
            outputPanel.appendError("Error during commit:\n" + e.getMessage());

        } finally {
            enableButtons(true, false, false, false);
        }
    }

    public void rollback() {

        if (sqlScriptRunner == null)
            return;

        try {
            sqlScriptRunner.rollback();
            outputPanel.append("Rollback complete");

        } catch (SQLException e) {
            outputPanel.appendError("Error during rollback:\n" + e.getMessage());

        } finally {
            enableButtons(true, false, false, false);
        }
    }

    // --- other methods ---

    private SqlStatementResult execute() {

        if (sqlScriptRunner == null)
            sqlScriptRunner = new SqlScriptRunner(this);

        long startTime = System.currentTimeMillis();
        SqlStatementResult sqlStatementResult = null;
        DatabaseConnection connection = null;

        try {

            outputPanel.clear();
            statusBar.setStatusText(Bundles.getCommon("executing"));
            statusBar.startProgressBar();

            if (useConnectionCheck.isSelected()) {
                connection = getSelectedConnection();
                if (connection != null && !connection.isConnected()) {
                    outputPanel.appendAction("Connecting to the DB...");
                    ConnectionMediator.getInstance().connect(connection, true);
                }
            }

            if (script == null || script.length() < MAX_LENGTH_TEXT_PANE)
                script = sqlText.getSQLText();

            sqlStatementResult = sqlScriptRunner.execute(
                    connection,
                    script,
                    stopOnErrorCheck.isSelected()
            );

        } finally {

            if (sqlStatementResult != null && sqlStatementResult.isException()) {
                if (sqlStatementResult.isInterrupted())
                    outputPanel.appendWarning("Operation cancelled by user action");
                else
                    outputPanel.appendError("Execution error:\n" + sqlStatementResult.getErrorMessage());
            }

            outputPanel.append("Total duration: " + MiscUtils.formatDuration(System.currentTimeMillis() - startTime));
            statusBar.setStatusText("Done");
            statusBar.stopProgressBar();

            if (sqlScriptRunner.isCloseConnection())
                resetButtons = true;

            GUIUtilities.scheduleGC();
        }

        return sqlStatementResult;
    }

    private DatabaseConnection getSelectedConnection() {
        return connectionsCombo.getSelectedConnection();
    }

    private boolean fieldsValid() {

        String fileName = fileNameField.getText();

        if (StringUtils.isBlank(fileName) && StringUtils.isEmpty(sqlText.getSQLText())) {
            GUIUtilities.displayErrorMessage(bundleString("error.select-input-file"));
            return false;
        }

        if (StringUtils.isNotEmpty(fileName) && !new File(fileName).exists()) {
            GUIUtilities.displayErrorMessage(bundleString("FileNotExist"));
            return false;
        }

        return true;
    }

    @Override
    public boolean tabViewClosing() {
        cleanup();
        return true;
    }

    @Override
    public void cleanup() {
        sqlText.cleanup();
        if (statusBar != null)
            statusBar.cleanup();
    }

    @Override
    public boolean logOutput() {
        return logOutputCheck.isSelected();
    }

    @Override
    public String getDisplayName() {
        return TITLE + " " + (instanceCounter++);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    // --- logging methods ---

    @Override
    public void message(final String message) {
        ThreadUtils.invokeAndWait(() -> outputPanel.append(message));
    }

    @Override
    public void actionMessage(final String message) {
        ThreadUtils.invokeAndWait(() -> outputPanel.appendAction(message));
    }

    @Override
    public void errorMessage(final String message) {
        ThreadUtils.invokeAndWait(() -> outputPanel.appendError(message));
    }

    @Override
    public void queryMessage(final String message) {
        ThreadUtils.invokeAndWait(() -> outputPanel.appendActionFixedWidth(message));
    }

    // ---

    class SqlTextPaneStatusBar extends AbstractStatusBarPanel {

        protected SqlTextPaneStatusBar() {

            super(STATUS_BAR_HEIGHT);

            addLabel(0, 200, true);
            progressBar = ProgressBarFactory.create(false);
            addComponent(((JComponent) progressBar), 1, 120, false);
        }

        public void setStatusText(String text) {
            setLabelText(0, text);
        }

        public void startProgressBar() {
            GUIUtils.invokeLater(() -> progressBar.start());
        }

        public void stopProgressBar() {
            GUIUtils.invokeLater(() -> progressBar.stop());
        }

        public void cleanup() {
            progressBar.cleanup();
            progressBar = null;
        }

    } // SqlTextPaneStatusBar class

}
