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
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabViewActionPanel;
import org.executequery.components.FileChooserDialog;
import org.executequery.components.MinimumWidthActionButton;
import org.executequery.components.SplitPaneFactory;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextFileWriter;
import org.executequery.localization.Bundles;
import org.executequery.sql.ActionOnError;
import org.executequery.sql.ExecutionController;
import org.executequery.sql.SqlScriptRunner;
import org.executequery.sql.SqlStatementResult;
import org.executequery.util.ThreadUtils;
import org.executequery.util.ThreadWorker;
import org.underworldlabs.swing.AbstractStatusBarPanel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.ProgressBar;
import org.underworldlabs.swing.ProgressBarFactory;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Takis Diakoumis
 */
public class ExecuteSqlScriptPanel extends DefaultTabViewActionPanel
        implements NamedView,
        FocusComponentPanel,
        ActiveComponent,
        ExecutionController,
        ConnectionListener {

    public static final String TITLE = Bundles.get(ExecuteSqlScriptPanel.class, "title");
    public static final String FRAME_ICON = "ExecuteSqlScript16.png";

    private JComboBox connectionsCombo;

    private JTextField fileNameField;

    private TableSelectionCombosGroup combosGroup;

    private SimpleSqlTextPanel sqlText;

    private LoggingOutputPanel outputPanel;

    private SqlTextPaneStatusBar statusBar;

    private JComboBox actionOnErrorCombo;

    private JCheckBox logOutputCheckBox;

    private JCheckBox useConnection;

    private JButton saveButton;

    private JButton startButton;

    private JButton rollbackButton;

    private JButton commitButton;

    private JButton stopButton;

    private SqlScriptRunner sqlScriptRunner;

    private boolean resetButtons;

    private String script = null;

    public ExecuteSqlScriptPanel() {

        super(new BorderLayout());

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void init() throws Exception {

        fileNameField = WidgetFactory.createTextField();
        fileNameField.setActionCommand("fileNameChanged");
        fileNameField.addActionListener(this);
        connectionsCombo = WidgetFactory.createComboBox();
        combosGroup = new TableSelectionCombosGroup(connectionsCombo);
        connectionsCombo.setEnabled(false);

        sqlText = new SimpleSqlTextPanel();
        sqlText.setBorder(null);
        sqlText.setScrollPaneBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, UIUtils.getDefaultBorderColour()));

        actionOnErrorCombo = WidgetFactory.createComboBox();

        ActionOnError[] actionsOnError = {
                ActionOnError.HALT,
                ActionOnError.CONTINUE
        };

        actionOnErrorCombo.setModel(new DefaultComboBoxModel(actionsOnError));

        outputPanel = new LoggingOutputPanel();
        statusBar = new SqlTextPaneStatusBar();
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 1));

        JButton button = new MinimumWidthActionButton(85, this, bundleString("Browse"), "browse");
        button.setMnemonic('r');

        logOutputCheckBox = new JCheckBox(bundleString("logOutput"));
        logOutputCheckBox.setSelected(true);

        useConnection = new JCheckBox(bundleString("UseConnection"));
        useConnection.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                activateConnUsage();
            }
        });
        useConnection.setSelected(false);

        saveButton = new MinimumWidthActionButton(85, this,
                bundleString("SaveScript"), "saveScript");

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridheight = 1;
        gbc.insets.top = 7;
        gbc.insets.bottom = 5;
        gbc.insets.right = 10;
        gbc.insets.left = 10;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(useConnection, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.top = 5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(connectionsCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 5;
        mainPanel.add(new JLabel(bundleString("ActionOnError")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.top = 0;
        mainPanel.add(actionOnErrorCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 5;
        mainPanel.add(new JLabel(bundleString("InputFile")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        mainPanel.add(fileNameField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(button, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.top = 2;
        gbc.insets.left = 10;
        gbc.gridwidth = 2;
        mainPanel.add(logOutputCheckBox, gbc);
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.insets.left = 0;
        mainPanel.add(saveButton, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets.top = 5;
        gbc.insets.left = 10;
        gbc.insets.bottom = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        JSplitPane splitPane = new SplitPaneFactory().create(JSplitPane.VERTICAL_SPLIT,
                sqlText, outputPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(400);
        splitPane.setDividerSize(5);
        mainPanel.add(splitPane, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 0;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(statusBar, gbc);

        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        int minimumButtonWidth = 85;
        startButton = new MinimumWidthActionButton(minimumButtonWidth, this, "Start", "start");
        rollbackButton = new MinimumWidthActionButton(minimumButtonWidth, this, "Commit", "commit");
        commitButton = new MinimumWidthActionButton(minimumButtonWidth, this, "Rollback", "rollback");
        stopButton = new MinimumWidthActionButton(minimumButtonWidth, this, "Stop", "stop");

        rollbackButton.setEnabled(false);
        commitButton.setEnabled(false);
        stopButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 5));
        buttonPanel.add(startButton);
        buttonPanel.add(rollbackButton);
        buttonPanel.add(commitButton);
        buttonPanel.add(stopButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        EventMediator.registerListener(this);
    }

    public int saveScript() {
        TextFileWriter writer = new TextFileWriter(sqlText.getSQLText());
        int result = writer.write();
        writer = null;
        return result;
    }

    public void activateConnUsage() {
        if (useConnection.isSelected())
            connectionsCombo.setEnabled(true);
        else
            connectionsCombo.setEnabled(false);
    }

    public void fileNameChanged() {
        File file = new File(fileNameField.getText());

        try {
            sqlText.setSQLText(FileUtils.loadFile(file.getPath()));
        } catch (IOException e) {
            GUIUtilities.displayErrorMessage(bundleString("error.load-file"));
        }
    }

    public boolean logOutput() {

        return logOutputCheckBox.isSelected();
    }

    public void browse() throws IOException {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle(bundleString("title.file-chooser"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), bundleString("Select"));
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();
        fileNameField.setText(file.getAbsolutePath());
        script = FileUtils.loadFile(file.getPath());
        //sqlText.setSQLText();
    }

    private boolean fieldsValid() {

        String fileName = fileNameField.getText();
        if (StringUtils.isBlank(fileName) && StringUtils.isEmpty(sqlText.getSQLText())) {

            GUIUtilities.displayErrorMessage(bundleString("error.select-input-file"));
            return false;

        } else if (StringUtils.isNotEmpty(fileName)){

            File file = new File(fileName);

            if (!file.exists()) {

                GUIUtilities.displayErrorMessage("The selected file does not exists in the file system");
                return false;
            }

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

        combosGroup.close();

        if (statusBar != null) {

            statusBar.cleanup();
        }
        boolean keepAlive = true;
        if (!keepAlive)
            closeConnection();
        EventMediator.deregisterListener(this);
    }

    private void closeConnection() {

        if (sqlScriptRunner != null) {

            try {

                sqlScriptRunner.close();

            } catch (SQLException e) {

                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof DefaultKeywordEvent) || (event instanceof ConnectionEvent);
    }

    @Override
    public Component getDefaultFocusComponent() {

        return fileNameField;
    }

    private void enableButtons(final boolean enableStart, final boolean enableStop,
                               final boolean enableCommit, final boolean enableRollback) {

        GUIUtils.invokeLater(new Runnable() {
            @Override
            public void run() {
                startButton.setEnabled(enableStart);
                stopButton.setEnabled(enableStop);
                commitButton.setEnabled(enableCommit);
                rollbackButton.setEnabled(enableRollback);
            }

        });
    }

    private SwingWorker swingWorker;
    private boolean executing;

    public void start() {

        if (!executing) {

            resetButtons = false;

            if (fieldsValid()) {

                enableButtons(false, true, false, false);

                swingWorker = new ThreadWorker() {
                    public Object construct() {

                        executing = true;
                        return execute();
                    }

                    public void finished() {

                        SqlStatementResult sqlStatementResult = (SqlStatementResult) get();

                        try {

                            outputPanel.append("Statements executed: " + sqlStatementResult.getStatementCount());
                            outputPanel.append("Total records affected: " + sqlStatementResult.getUpdateCount());

                        } finally {

                            executing = false;
                            // if connected to database from script, connection will be committed from script executor
                            if (resetButtons)
                                enableButtons(true, false, false, false);
                            else
                                enableButtons(false, false, true, true);
                        }
                    }
                };
                swingWorker.start();
            }

        }
    }

    public void stop() {

        ThreadUtils.startWorker(new Runnable() {

            public void run() {

                if (executing) {
                    try {

                        if (swingWorker != null) {

                            swingWorker.interrupt();
                        }
                        sqlScriptRunner.stop();

                    } finally {

                        executing = false;
                    }
                }
            }
        });
    }

    public void commit() {

        if (sqlScriptRunner != null) {

            try {

                sqlScriptRunner.commit();
                outputPanel.append("Commit complete");

            } catch (SQLException e) {

                outputPanel.appendError("Error during commit:\n" + e.getMessage());

            } finally {

                boolean keepAlive = true;
                if (!keepAlive)
                    closeConnection();
                enableButtons(true, false, false, false);
            }
        }

    }

    public void rollback() {

        if (sqlScriptRunner != null) {

            try {

                sqlScriptRunner.rollback();
                outputPanel.append("Rollback complete");

            } catch (SQLException e) {

                outputPanel.appendError("Error during rollback:\n" + e.getMessage());

            } finally {

                boolean keepAlive = true;
                if (!keepAlive)
                    closeConnection();
                enableButtons(true, false, false, false);
            }
        }

    }

    private SqlStatementResult execute() {

        if (sqlScriptRunner == null) {

            sqlScriptRunner = new SqlScriptRunner(this);
        }

        outputPanel.clear();
        long startTime = System.currentTimeMillis();
        SqlStatementResult sqlStatementResult = null;
        DatabaseConnection connection = null;

        try {

            statusBar.setStatusText("Executing...");
            statusBar.startProgressBar();

            if (useConnection.isSelected()) {
                DatabaseHost selectedHost = combosGroup.getSelectedHost();
                if (selectedHost != null)
                    connection = selectedHost.getDatabaseConnection();
            }

            sqlStatementResult = sqlScriptRunner.execute(
                    connection,
                    script,
                    (ActionOnError) actionOnErrorCombo.getSelectedItem());

        } finally {

            if (sqlStatementResult != null && sqlStatementResult.isException()) {

                if (sqlStatementResult.isInterrupted()) {

                    outputPanel.appendWarning("Operation cancelled by user action");

                } else {

                    outputPanel.appendError("Execution error:\n" + sqlStatementResult.getErrorMessage());
                }

            }

            long endTime = System.currentTimeMillis();

            statusBar.setStatusText("Done");
            statusBar.stopProgressBar();

            outputPanel.append("Total duration: " + MiscUtils.formatDuration(endTime - startTime));

            // if connected to database from script, connection will be committed from script executor
            if (sqlScriptRunner.isNeedCloseDatabase()) {
                resetButtons = true;
            }

            GUIUtilities.scheduleGC();
        }

        return sqlStatementResult;
    }

    private static int instanceCount = 1;

    public String getDisplayName() {

        return TITLE + (instanceCount++);
    }

    public String toString() {

        return getDisplayName();
    }

    // ---------------------------------------------
    // ConnectionListener implementation
    // ---------------------------------------------

    /**
     * Indicates a connection has been established.
     *
     * @param connectionEvent encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {

        combosGroup.connectionOpened(connectionEvent.getDatabaseConnection());
    }

    /**
     * Indicates a connection has been closed.
     *
     * @param connectionEvent encapsulating event
     */
    public void disconnected(ConnectionEvent connectionEvent) {

        combosGroup.connectionClosed(connectionEvent.getDatabaseConnection());
    }

    private static final int STATUS_BAR_HEIGHT = 26;

    private ProgressBar progressBar;

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

        public void cleanup() {

            progressBar.cleanup();
            progressBar = null;
        }

        public void startProgressBar() {

            GUIUtils.invokeLater(new Runnable() {
                public void run() {

                    progressBar.start();
                }
            });
        }

        public void stopProgressBar() {

            GUIUtils.invokeLater(new Runnable() {
                public void run() {

                    progressBar.stop();
                }
            });
        }

    } // SqlTextPaneStatusBar

    public void message(final String message) {
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                outputPanel.append(message);
            }
        });
    }

    public void actionMessage(final String message) {
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                outputPanel.appendAction(message);
            }
        });
    }

    public void errorMessage(final String message) {
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                outputPanel.appendError(message);
            }
        });
    }

    public void queryMessage(final String message) {
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                outputPanel.appendActionFixedWidth(message);
            }
        });
    }

    public void warningMessage(final String message) {
        ThreadUtils.invokeAndWait(new Runnable() {
            public void run() {
                outputPanel.appendWarning(message);
            }
        });
    }

}


