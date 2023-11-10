/*
 * ExportResultSetPanel.java
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
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.event.*;
import org.executequery.gui.importexport.ImportExportDataException;
import org.executequery.gui.importexport.ResultSetDelimitedFileWriter;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.DerivedQuery;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

/**
 * @author Takis Diakoumis
 */
public class ExportResultSetPanel extends DefaultTabViewActionPanel
        implements NamedView,
        FocusComponentPanel,
        ActiveComponent,
        KeywordListener,
        ConnectionListener,
        TextEditorContainer {

    public static final String TITLE = bundledString("title");
    public static final String FRAME_ICON = "ExportDelimited16.svg";

    private JComboBox connectionsCombo;

    private JComboBox delimiterCombo;

    private JCheckBox applyQuotesCheck;

    private JTextField fileNameField;

    private JCheckBox includeColumNamesCheck;

    private SimpleSqlTextPanel sqlText;

    private TableSelectionCombosGroup combosGroup;

    private LoggingOutputPanel outputPanel;

    private SqlTextPaneStatusBar statusBar;
    private MinimumWidthActionButton stopButton;

    private static final KeyStroke EXECUTE_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);

    public ExportResultSetPanel() {

        super(new BorderLayout());
        init();
    }

    private void init() {

        fileNameField = WidgetFactory.createTextField("fileNameField");
        connectionsCombo = WidgetFactory.createComboBox("connectionsCombo");

        String[] delims = {"|", ",", ";", "#"};
        delimiterCombo = WidgetFactory.createComboBox("delimiterCombo", delims);
        delimiterCombo.setEditable(true);

        combosGroup = new TableSelectionCombosGroup(connectionsCombo);

        includeColumNamesCheck = new JCheckBox(bundledString("IncludeColumnNames"));
        applyQuotesCheck = new JCheckBox(bundledString("UseDoubleQuotes"), true);

        sqlText = new SimpleSqlTextPanel();
//        sqlText.getTextPane().setBackground(Color.WHITE);
        sqlText.setBorder(null);
        sqlText.setScrollPaneBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, UIUtils.getDefaultBorderColour()));

        statusBar = new SqlTextPaneStatusBar();
        JPanel sqlPanel = new JPanel(new BorderLayout());
        sqlPanel.add(sqlText, BorderLayout.CENTER);
        sqlPanel.add(statusBar, BorderLayout.SOUTH);
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));

        outputPanel = new LoggingOutputPanel();
        FlatSplitPane splitPane = new FlatSplitPane(
                JSplitPane.VERTICAL_SPLIT, sqlPanel, outputPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.8);
        splitPane.setDividerSize(5);

        JButton button = WidgetFactory.createInlineFieldButton(bundledString("Browse"));
        button.setActionCommand("browse");
        button.addActionListener(this);
        button.setMnemonic('r');

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridheight = 1;
        gbc.insets.top = 5;
        gbc.insets.bottom = 5;
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel(bundledString("Connection")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(connectionsCombo, gbc);
        gbc.insets.left = 5;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 0;
        mainPanel.add(new JLabel(bundledString("DataDelimiter")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(delimiterCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 2;
        mainPanel.add(new JLabel(bundledString("OutputFile")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(fileNameField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(button, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets.top = 2;
        gbc.insets.left = 5;
        gbc.insets.bottom = 0;
        mainPanel.add(includeColumNamesCheck, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        mainPanel.add(applyQuotesCheck, gbc);
        gbc.insets.top = 5;
        gbc.gridy++;
        gbc.insets.bottom = 10;
        mainPanel.add(new JLabel(instructionNote()), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        gbc.insets.left = 5;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(splitPane, gbc);

        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        int minimumButtonWidth = 85;
        executeButton = new MinimumWidthActionButton(minimumButtonWidth, this, Bundles.getCommon("execute"), "executeAndExport");
        stopButton = new MinimumWidthActionButton(minimumButtonWidth, this, Bundles.getCommon("stop"), "stop");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 5));
        buttonPanel.add(executeButton);
        buttonPanel.add(stopButton);

        stopButton.setEnabled(false);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // register as a keyword and connection listener
        EventMediator.registerListener(this);

        SQLTextArea textPane = sqlText.getTextPane();
        ActionMap actionMap = textPane.getActionMap();

        String actionKey = "executeQueryAction";
        actionMap.put(actionKey, executeQueryAction);

        InputMap inputMap = textPane.getInputMap();
        inputMap.put(EXECUTE_KEYSTROKE, actionKey);

        JPopupMenu popupMenu = sqlText.getPopup();
        popupMenu.addSeparator();
        popupMenu.add(executeQueryAction);
    }

    public void browse() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle(bundledString("FileChooserTitle"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), Bundles.getCommon("select"));
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();
        if (file.exists()) {

            result = GUIUtilities.displayConfirmCancelDialog(bundledString("FileExists"));

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION) {

                browse();
                return;
            }

        }

        fileNameField.setText(file.getAbsolutePath());
    }

    private boolean fieldsValid() {

        if (StringUtils.isBlank(delimiterCombo.getSelectedItem().toString())) {

            GUIUtilities.displayErrorMessage(bundledString("SelectDelimiter"));
            return false;
        }

        if (StringUtils.isBlank(fileNameField.getText())) {

            GUIUtilities.displayErrorMessage(bundledString("SelectFile"));
            return false;
        }

        if (StringUtils.isBlank(sqlText.getEditorText())) {

            GUIUtilities.displayErrorMessage(bundledString("EnterQuery"));
            return false;
        }

        return true;
    }

    public boolean tabViewClosing() {

        cleanup();
        return true;
    }

    public void cleanup() {
        sqlText.cleanup();
        combosGroup.close();

        if (statusBar != null) {

            statusBar.cleanup();
        }

        EventMediator.deregisterListener(this);
    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof DefaultKeywordEvent) || (event instanceof ConnectionEvent);
    }

    /**
     * Notification of a new keyword added to the list.
     */
    public void keywordsAdded(KeywordEvent e) {

        sqlText.setSQLKeywords(true);
    }

    /**
     * Notification of a keyword removed from the list.
     */
    public void keywordsRemoved(KeywordEvent e) {

        sqlText.setSQLKeywords(true);
    }

    public Component getDefaultFocusComponent() {

        return fileNameField;
    }

    public void stop() {

        if (executing) {

            if (swingWorker != null) {

                swingWorker.interrupt();
            }

        }

    }

    private void enableButtons(final boolean enableExecute, final boolean enableStop) {

        GUIUtils.invokeLater(new Runnable() {
            public void run() {
                executeButton.setEnabled(enableExecute);
                stopButton.setEnabled(enableStop);
            }

        });
    }

    private SwingWorker swingWorker;
    private boolean executing;

    public void executeAndExport() {

        if (!executing) {

            if (fieldsValid()) {

                enableButtons(false, true);

                swingWorker = new SwingWorker("ExportResultSet") {
                    public Object construct() {

                        executing = true;
                        return execute();
                    }

                    public void finished() {

                        try {

                            Integer recordCount = (Integer) get();
                            if (recordCount != -1) {

                                outputPanel.append(bundledString("RecordsTransferred") + recordCount);

                                File file = outputFile();
                                StringBuilder sb = new StringBuilder();
                                sb.append(bundledString("OutputFile"));
                                sb.append(file.getName());
                                sb.append(" (");
                                sb.append(new DecimalFormat("###,###.##").format(MiscUtils.bytesToMegaBytes(file.length())));
                                sb.append("Mb)");

                                outputPanel.append(sb.toString());
                            }

                        } finally {

                            executing = false;
                            enableButtons(true, false);
                        }
                    }
                };
                swingWorker.start();
            }

        }
    }

    private File outputFile() {

        return new File(fileNameField.getText());
    }

    private int execute() {

        ResultSet resultSet = null;
        DatabaseHost host = combosGroup.getSelectedHost();
        StatementExecutor statementExecutor = new DefaultStatementExecutor(host.getDatabaseConnection(), true);
        statementExecutor.setCommitMode(false);

        int result = -1;
        long startTime = System.currentTimeMillis();
        SqlStatementResult statementResult = null;

        try {

            String query = sqlText.getEditorText();

            statusBar.setStatusText(Bundles.getCommon("executing"));
            statusBar.startProgressBar();

            outputPanel.appendAction(bundledString("Executing"));
            outputPanel.appendActionFixedWidth(query);

            DerivedQuery derivedQuery = new DerivedQuery(query);
            statementResult = statementExecutor.execute(derivedQuery.getQueryType(), query, fetchSizeForDatabaseProduct(host));

            if (statementResult.isException()) {

                throw statementResult.getSqlException();

            } else if (statementResult.isResultSet()) {

                resultSet = statementResult.getResultSet();
                result = writeToFile(resultSet);

            } else {

                outputPanel.appendWarning(bundledString("NoValidResultSet"));
            }

        } catch (SQLException e) {

            if (statementResult != null) {

                outputPanel.appendError(statementResult.getErrorMessage());

            } else {

                outputPanel.appendError(bundledString("ExecutionError") + e.getMessage());
            }

        } catch (ImportExportDataException e) {

            outputPanel.appendError(bundledString("ExecutionError") + e.getMessage());

        } catch (InterruptedException e) {

            outputPanel.appendWarning(bundledString("OperationCancelled"));

        } finally {

            long endTime = System.currentTimeMillis();

            statusBar.setStatusText(Bundles.getCommon("done"));
            statusBar.stopProgressBar();

            outputPanel.append(bundledString("Duration") + MiscUtils.formatDuration(endTime - startTime));

            try {

                if (resultSet != null) {

                    resultSet.close();
                }

                statementExecutor.destroyConnection();

            } catch (SQLException e) {

                e.printStackTrace();
            }

            host.close();
            GUIUtilities.scheduleGC();
        }

        return result;
    }

    private int fetchSizeForDatabaseProduct(DatabaseHost host) {

        // we only care about mysql right now which needs Integer.MIN_VALUE
        // to provide row-by-row return on the result set cursor
        // otherwise default to 10000 row fetch size...

        if (host.getDatabaseProductName().toUpperCase().contains("MYSQL")) {

            return Integer.MIN_VALUE;
        }

        return 10000;
    }


    private int writeToFile(ResultSet resultSet) throws InterruptedException {

        ResultSetDelimitedFileWriter writer = new ResultSetDelimitedFileWriter();
        return writer.write(fileNameField.getText(),
                delimiterCombo.getSelectedItem().toString(), resultSet,
                includeColumNamesCheck.isSelected(), applyQuotesCheck.isSelected());
    }

    // ------------------------------------------------
    // ----- TextEditorContainer implementations ------
    // ------------------------------------------------

    /**
     * Returns the SQL text pane as the TextEditor component
     * that this container holds.
     */
    public TextEditor getTextEditor() {

        return sqlText;
    }

    private static int count = 1;

    public String getDisplayName() {

        return TITLE + (count++);
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
     * @param the encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {

        combosGroup.connectionOpened(connectionEvent.getDatabaseConnection());
    }

    /**
     * Indicates a connection has been closed.
     *
     * @param the encapsulating event
     */
    public void disconnected(ConnectionEvent connectionEvent) {

        combosGroup.connectionClosed(connectionEvent.getDatabaseConnection());
    }

    private static final int STATUS_BAR_HEIGHT = 21;

    private ProgressBar progressBar;
    private JButton executeButton;

    class SqlTextPaneStatusBar extends AbstractStatusBarPanel {

        protected SqlTextPaneStatusBar() {

            super(STATUS_BAR_HEIGHT);

            addLabel(0, 200, true);
            progressBar = ProgressBarFactory.create(false, true);
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

            progressBar.start();
        }

        public void stopProgressBar() {

            progressBar.stop();
        }

    } // SqlTextPaneStatusBar

    private final ExecuteQueryAction executeQueryAction = new ExecuteQueryAction();

    class ExecuteQueryAction extends AbstractAction {

        public ExecuteQueryAction() {

            super("Execute");
            putValue(Action.ACCELERATOR_KEY, EXECUTE_KEYSTROKE);
        }

        public void actionPerformed(ActionEvent e) {

            executeAndExport();
        }

    } // ExecuteQueryAction

    private String instructionNote() {

        try {

            return FileUtils.loadResource(Bundles.getCommon("locale").equals("en") ?
                    "org/executequery/gui/resource/exportResultSetInstruction.html" :
                    "org/executequery/gui/resource/exportResultSetInstruction_ru.html");

        } catch (IOException e) {

            if (Log.isDebugEnabled()) {

                Log.debug("Error loading export result set instruction note", e);
            }

        }

        return bundledString("EnterSQL");
    }

    public static String bundledString(String key) {
        return Bundles.get(ExportResultSetPanel.class, key);
    }

}






