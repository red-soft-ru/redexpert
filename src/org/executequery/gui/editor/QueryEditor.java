/*
 * QueryEditor.java
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

package org.executequery.gui.editor;

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.*;
import org.executequery.gui.FocusablePanel;
import org.executequery.gui.SaveFunction;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.profiler.ProfilerPanel;
import org.executequery.gui.exportData.ExportDataPanel;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.gui.text.TextEditor;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.print.TablePrinter;
import org.executequery.print.TextPrinter;
import org.executequery.sql.TokenizingFormatter;
import org.executequery.util.UserProperties;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.underworldlabs.sqlParser.SqlParser;
import org.underworldlabs.swing.DefaultCheckComboBox;
import org.underworldlabs.swing.ConnectionsComboBox;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Query Editor.
 *
 * @author Takis Diakoumis
 */
@SuppressWarnings("FieldCanBeLocal")
public class QueryEditor extends DefaultTabView
        implements ConnectionListener,
        ListCheckListener,
        QueryBookmarkListener,
        QueryShortcutListener,
        UserPreferenceListener,
        TextEditor,
        FocusablePanel,
        PropertyChangeListener {

    public static final String TITLE = Bundles.get(QueryEditor.class, "title");
    public static final String FRAME_ICON = "icon_query_editor";

    private static final String DEFAULT_SCRIPT_PREFIX = Bundles.get(QueryEditor.class, "script");
    private static final String DEFAULT_SCRIPT_SUFFIX = ".sql";

    public static final String STOP_ON_ERROR_CMD = "editor-stop-on-error-command";
    public static final String EXECUTE_TO_FILE_CMD = "editor-execute-to-file-command";
    public static final String AUTOCOMMIT_MODE_CMD = "toggle-autocommit-command";
    public static final String LIMIT_RS_MODE_CMD = "toggle-rs-limit-command";
    public static final String STOP_ON_ERROR_PROP = "editor.stop.on.error";
    public static final String AUTOCOMMIT_MODE_PROP = "editor.connection.commit";
    public static final String LIMIT_RS_MODE_PROP = "editor.limit.records.count";
    public static final String LIMIT_RS_COUNT_PROP = "editor.max.records.count";

    // --- GUI elements ---

    private QueryEditorStatusBar statusBar;
    private QueryEditorTextPanel editorPanel;
    private QueryEditorResultsPanel resultsPanel;
    private QueryEditorToolBar toolBar;
    private QueryEditorPopupMenu popup;
    private TransactionParametersPanel transactionParametersPanel;

    private DefaultCheckComboBox connectionsCheckCombo;
    private ConnectionsComboBox connectionsCombo;

    private JPanel baseEditorPanel;
    private JSplitPane splitPane;

    private RolloverButton stopOnErrorButton;
    private RolloverButton executeToFileButton;
    private RolloverButton autoCommitModeButton;
    private RolloverButton resultSetLimitModeButton;

    // ---

    private final ScriptFile scriptFile = new ScriptFile();
    private final TokenizingFormatter tokenizingFormatter = new TokenizingFormatter();

    private DatabaseConnection oldConnection;
    private DatabaseConnection connectionToSelect;
    private List<Object> selectedConnections;

    private QueryEditorDelegate delegate;
    private Map<DatabaseConnection, QueryEditorDelegate> delegates;

    private int lastDividerLocation;
    private int queryEditorNumber;
    private int maxRecordsCount;

    private boolean useMultipleConnections;
    private boolean isQueryEditorClosed;
    private boolean isContentChanged;
    private boolean autosaveEnabled;
    private boolean executeToFile;

    public QueryEditor() {
        this(null, null, -1);
    }

    public QueryEditor(String text) {
        this(text, null, -1);
    }

    public QueryEditor(String text, String absolutePath, int splitDividerLocation) {
        this(text, absolutePath, splitDividerLocation, true);
    }

    public QueryEditor(String text, String absolutePath, int splitDividerLocation, boolean autosaveEnabled) {
        super(new GridBagLayout());
        this.autosaveEnabled = autosaveEnabled;

        init();
        arrange();

        String fileName = defaultScriptName();
        String connectionID = QueryEditorHistory.NULL_CONNECTION;

        if (absolutePath == null) {
            QueryEditorHistory.checkAndCreateDir();
            absolutePath = QueryEditorHistory.editorDirectory() + fileName;
        }

        scriptFile.setFileName(defaultScriptName());
        scriptFile.setAbsolutePath(absolutePath);

        if (getSelectedConnection() != null) {
            connectionID = getSelectedConnection().getId();
            editorPanel.getQueryArea().setDatabaseConnection(getSelectedConnection());
        }

        if (splitDividerLocation > 0)
            splitPane.setDividerLocation(splitDividerLocation);

        QueryEditorHistory.addEditor(connectionID, absolutePath, queryEditorNumber, splitPane.getDividerLocation(), autosaveEnabled);
        splitPane.addPropertyChangeListener("dividerLocation", this);
        isContentChanged = false;

        if (text != null)
            loadText(text);
    }

    private void init() {

        queryEditorNumber = -1;
        executeToFile = false;
        isQueryEditorClosed = false;
        useMultipleConnections = SystemProperties.getBooleanProperty("user", "editor.use.multiple.connections");
        delegates = new HashMap<>();
        selectedConnections = new ArrayList<>();

        // --- status bar ---

        statusBar = new QueryEditorStatusBar();
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, -1, -2, -2));
        statusBar.setCaretPosition(1, 1);
        statusBar.setInsertionMode("INS");

        // --- connection combos ---

        connectionsCombo = WidgetFactory.createConnectionComboBox("connectionsCombo", true);
        connectionsCombo.setMaximumSize(new Dimension(200, 30));
        connectionsCombo.addItemListener(this::connectionChanged);
        connectionsCombo.setVisible(!useMultipleConnections);

        connectionsCheckCombo = WidgetFactory.createCheckComboBox("connectionCheckCombo", ConnectionManager.getActiveConnections().toArray());
        connectionsCheckCombo.setMaximumSize(new Dimension(200, 30));
        connectionsCheckCombo.getModel().addListCheckListener(this);
        connectionsCheckCombo.setVisible(useMultipleConnections);

        oldConnection = useMultipleConnections ?
                getSelectedConnection() :
                connectionsCombo.getSelectedConnection();

        // --- transaction parameters panel ---

        transactionParametersPanel = new TransactionParametersPanel(getSelectedConnection());

        // --- delegate ---

        delegate = new QueryEditorDelegate(this);
        delegate.setTPP(transactionParametersPanel);
        popup = new QueryEditorPopupMenu();

        // --- panels ---

        editorPanel = new QueryEditorTextPanel(this);
        editorPanel.addEditorPaneMouseListener(popup);

        toolBar = new QueryEditorToolBar(
                new Component[]{connectionsCombo, connectionsCheckCombo},
                editorPanel.getTextPaneActionMap(),
                editorPanel.getTextPaneInputMap()
        );

        baseEditorPanel = new JPanel(new BorderLayout());
        resultsPanel = new QueryEditorResultsPanel(this);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, baseEditorPanel, resultsPanel);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(4);

        // --- toolbar buttons ---

        stopOnErrorButton = (RolloverButton) toolBar.getButton(STOP_ON_ERROR_CMD);
        if (stopOnErrorButton != null) {
            stopOnErrorButton.addActionListener(e -> updateStopOnError(true));
            updateStopOnError(false);
        }

        executeToFileButton = (RolloverButton) toolBar.getButton(EXECUTE_TO_FILE_CMD);
        if (executeToFileButton != null) {
            executeToFileButton.addActionListener(e -> updateExecuteDestination(true));
            updateExecuteDestination(false);
        }

        autoCommitModeButton = (RolloverButton) toolBar.getButton(AUTOCOMMIT_MODE_CMD);
        if (autoCommitModeButton != null) {
            autoCommitModeButton.addActionListener(e -> updateAutoCommitMode(true));
            updateAutoCommitMode(false);
        }

        resultSetLimitModeButton = (RolloverButton) toolBar.getButton(LIMIT_RS_MODE_CMD);
        if (resultSetLimitModeButton != null) {
            resultSetLimitModeButton.addActionListener(e -> updateResultSetLimitMode(true));
            updateResultSetLimitMode(false);
        }
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- base editor panel ---

        baseEditorPanel.add(editorPanel, BorderLayout.CENTER);
        baseEditorPanel.add(statusBar, BorderLayout.SOUTH);
        baseEditorPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, GUIUtilities.getDefaultBorderColour()));

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(6, 8, 8, 0).anchorNorthWest().fillHorizontally();
        mainPanel.add(toolBar, gbh.setWeightX(0).spanX().get());
        mainPanel.add(transactionParametersPanel, gbh.leftGap(0).topGap(5).nextRow().get());
        mainPanel.add(splitPane, gbh.nextRow().setInsets(5, 8, 5, 5).setWeightX(1).fillBoth().spanY().get());

        // --- base ---

        add(mainPanel, new GridBagHelper().anchorSouthEast().fillBoth().spanX().spanY().get());
        EventMediator.registerListener(this);
        addDeleteLineActionMapping();
        setEditorPreferences();
    }

    private void connectionChanged(ItemEvent e) {

        if (e.getStateChange() != ItemEvent.SELECTED)
            return;

        DatabaseConnection newConnection = getSelectedConnection();

        String oldConnectionId = oldConnection != null ?
                oldConnection.getId() :
                QueryEditorHistory.NULL_CONNECTION;

        String newConnectionId = newConnection != null ?
                newConnection.getId() :
                QueryEditorHistory.NULL_CONNECTION;

        if (Objects.equals(oldConnectionId, newConnectionId))
            return;

        QueryEditorHistory.changedConnectionEditor(oldConnectionId, newConnectionId, scriptFile.getAbsolutePath());
        oldConnection = newConnection;

        if (editorPanel != null)
            editorPanel.getQueryArea().setDatabaseConnection(newConnection);
        if (transactionParametersPanel != null)
            transactionParametersPanel.setDatabaseConnection(newConnection);
    }

    private void updateExecuteDestination(boolean toggle) {

        if (toggle)
            executeToFile = !executeToFile;

        if (executeToFileButton == null)
            return;

        executeToFileButton.setPressed(executeToFile);
        executeToFileButton.setToolTipText(Bundles.get(executeToFile ?
                "action.editor-execute-to-file-command-off" :
                "action.editor-execute-to-file-command-on"
        ));
    }

    private void updateStopOnError(boolean toggle) {

        boolean newValue = SystemProperties.getBooleanProperty("user", STOP_ON_ERROR_PROP);
        if (toggle) {
            newValue = !newValue;
            SystemProperties.setBooleanProperty("user", STOP_ON_ERROR_PROP, newValue);
            EventMediator.fireEvent(new DefaultUserPreferenceEvent(this, STOP_ON_ERROR_PROP, UserPreferenceEvent.QUERY_EDITOR));
        }

        if (stopOnErrorButton == null)
            return;

        stopOnErrorButton.setPressed(newValue);
        stopOnErrorButton.setToolTipText(Bundles.get(newValue ?
                "action.editor-stop-on-error-command-off" :
                "action.editor-stop-on-error-command-on"
        ));
    }

    private void updateAutoCommitMode(boolean toggle) {

        boolean newValue = SystemProperties.getBooleanProperty("user", AUTOCOMMIT_MODE_PROP);
        if (toggle) {
            newValue = !newValue;

            delegate.setCommitMode(newValue);
            popup.setCommitMode(newValue);
            getTools().setCommitsEnabled(!newValue);

            SystemProperties.setBooleanProperty("user", AUTOCOMMIT_MODE_PROP, newValue);
            EventMediator.fireEvent(new DefaultUserPreferenceEvent(this, AUTOCOMMIT_MODE_PROP, UserPreferenceEvent.QUERY_EDITOR));
        }

        if (autoCommitModeButton == null)
            return;

        autoCommitModeButton.setPressed(newValue);
        autoCommitModeButton.setToolTipText(Bundles.get(newValue ?
                "action.toggle-autocommit-command-off" :
                "action.toggle-autocommit-command-on"
        ));
    }

    private void updateResultSetLimitMode(boolean toggle) {

        boolean newValue = SystemProperties.getBooleanProperty("user", LIMIT_RS_MODE_PROP);
        if (toggle) {
            newValue = !newValue;

            SystemProperties.setBooleanProperty("user", LIMIT_RS_MODE_PROP, newValue);
            EventMediator.fireEvent(new DefaultUserPreferenceEvent(this, LIMIT_RS_MODE_PROP, UserPreferenceEvent.QUERY_EDITOR));
        }

        if (resultSetLimitModeButton == null)
            return;

        resultSetLimitModeButton.setPressed(newValue);
        resultSetLimitModeButton.setToolTipText(String.format(Bundles.get(newValue ?
                        "action.toggle-rs-limit-command-off" :
                        "action.toggle-rs-limit-command-on"
                ),
                SystemProperties.getIntProperty("user", LIMIT_RS_COUNT_PROP)
        ));
    }

    private String defaultScriptName() {
        queryEditorNumber = QueryEditorHistory.getMinNumber();
        return DEFAULT_SCRIPT_PREFIX + queryEditorNumber + DEFAULT_SCRIPT_SUFFIX;
    }

    public void changeOrientationSplit() {
        splitPane.setOrientation(Objects.equals(splitPane.getOrientation(), JSplitPane.VERTICAL_SPLIT) ?
                JSplitPane.HORIZONTAL_SPLIT :
                JSplitPane.VERTICAL_SPLIT
        );
    }

    public void removePopupComponent(JComponent component) {
        GUIUtilities.getFrameLayeredPane().remove(component);
        GUIUtilities.getFrameLayeredPane().repaint();
    }

    public void addPopupComponent(JComponent component) {
        GUIUtilities.getFrameLayeredPane().add(component, JLayeredPane.POPUP_LAYER);
        GUIUtilities.getFrameLayeredPane().repaint();
    }

    private void addDeleteLineActionMapping() {

        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteLine();
            }
        };

        String actionMapKey = "editor-delete-line";
        KeyStroke keyStroke = KeyStroke.getKeyStroke("control D");

        ActionMap textPaneActionMap = editorPanel.getTextPaneActionMap();
        textPaneActionMap.put(actionMapKey, action);

        InputMap textPaneInputMap = editorPanel.getTextPaneInputMap();
        textPaneInputMap.put(keyStroke, actionMapKey);

    }

    /**
     * Toggles the output pane visible or not.
     */
    public void toggleOutputPaneVisible() {

        if (resultsPanel.isVisible()) {
            lastDividerLocation = splitPane.getDividerLocation();
            resultsPanel.setVisible(false);

        } else {
            resultsPanel.setVisible(true);
            splitPane.setDividerLocation(lastDividerLocation);
        }
    }

    /**
     * Enters the specified text at the editor's current
     * insertion point.
     *
     * @param text - the text to insert
     */
    public void insertTextAtCaret(String text) {
        editorPanel.insertTextAtCaret(text);
    }

    /**
     * Returns the default focus component, the query text
     * editor component.
     *
     * @return the editor component
     */
    @Override
    public Component getDefaultFocusComponent() {
        return editorPanel.getQueryArea();
    }

    /**
     * Sets the editor user preferences.
     */
    public void setEditorPreferences() {
        useMultipleConnections = SystemProperties.getBooleanProperty("user", "editor.use.multiple.connections");
        maxRecordsCount = SystemProperties.getBooleanProperty("user", "editor.limit.records.count") ?
                SystemProperties.getIntProperty("user", "editor.max.records.count") : -1;

        statusBar.setVisible(isStatusBarVisible());
        toolBar.setVisible(isToolsPanelVisible());
        editorPanel.showLineNumbers(isLineNumbersVisible(), editorPanel.getQueryArea().getEditorTextComponent().getFont());
        editorPanel.setTextPaneBackground(userProperties().getColourProperty("editor.text.background.colour"));
        editorPanel.preferencesChanged();
        delegate.preferencesChanged();
        delegate.setCommitMode(isAutoCommit());
        popup.setCommitMode(isAutoCommit());
        resultsPanel.setResultBackground(userProperties().getColourProperty("editor.output.background"));
        resultsPanel.setTableProperties();

        transactionParametersPanel.setVisible(isToolsPanelVisible() && SystemProperties.getBooleanProperty("user", "editor.display.transaction.params"));

        editorPanel.getQueryArea().setLineWrap(SystemProperties.getBooleanProperty("user", "editor.wrap.lines"));
        editorPanel.getQueryArea().setAutocompleteOnlyHotKey(SystemProperties.getBooleanProperty("user", "editor.autocomplete.only.hotkey"));
        if (!isAutoCompleteOn())
            editorPanel.getQueryArea().deregisterAutoCompletePopup();

        connectionsCombo.setVisible(isToolsPanelVisible() && !useMultipleConnections);
        connectionsCheckCombo.setVisible(isToolsPanelVisible() && useMultipleConnections);

        updateStopOnError(false);
        updateAutoCommitMode(false);
        updateResultSetLimitMode(false);
    }

    private boolean isAutoCompleteOn() {

        UserProperties userProperties = userProperties();

        if (userProperties.containsKey("editor.autocomplete.on")
                && (!userProperties.containsKey("editor.autocomplete.keywords.on"))
                && !userProperties.containsKey("editor.autocomplete.objects.on")) {

            boolean allOn = userProperties.getBooleanProperty("editor.autocomplete.on");
            userProperties.setBooleanProperty("editor.autocomplete.keywords.on", allOn);
            userProperties.setBooleanProperty("editor.autocomplete.objects.on", allOn);
        }

        return userProperties.getBooleanProperty("editor.autocomplete.keywords.on")
                || userProperties.getBooleanProperty("editor.autocomplete.objects.on");
    }

    private boolean isAutoCommit() {
        return userProperties().getBooleanProperty("editor.connection.commit");
    }

    private boolean isLineNumbersVisible() {
        return userProperties().getBooleanProperty("editor.display.linenums");
    }

    private boolean isStatusBarVisible() {
        return userProperties().getBooleanProperty("editor.display.statusbar");
    }

    private boolean isToolsPanelVisible() {
        return userProperties().getBooleanProperty("editor.display.toolsPanel");
    }

    private UserProperties userProperties() {
        return UserProperties.getInstance();
    }

    /**
     * Called to inform this component of a change/update
     * to the user defined keywords.
     */
    public void updateSQLKeywords() {
        editorPanel.setSQLKeywords(true);
    }

    /**
     * Notifies that the query is executing
     */
    public void executing() {

        popup.statementExecuting();
        setStopButtonEnabled(true);
        statusBar.startProgressBar();
        statusBar.setExecutionTime(Bundles.getCommon("executing"));
    }

    /**
     * Notifies that the query execute is finished.
     */
    public void finished(String message) {

        popup.statementFinished();
        resultsPanel.finished();
        statusBar.stopProgressBar();
        setStopButtonEnabled(false);
        statusBar.setExecutionTime(message);
    }

    public void commitModeChanged(boolean autoCommit) {
        statusBar.setCommitStatus(autoCommit);
    }

    /**
     * Sets the text of the left status label.
     *
     * @param text the text to be set
     */
    public void setLeftStatusText(String text) {
        statusBar.setStatus(text);
    }

    /**
     * Sets the text of the left status label.
     *
     * @param text    the text to be set
     * @param toolTip the label toolTip
     * @param icon    the icon to be set
     */
    public void setLeftStatus(String text, String toolTip, Icon icon) {
        statusBar.setStatus(text, toolTip, icon);
    }

    /**
     * Propagates the call to interrupt an executing process.
     */
    public void interrupt() {
        resultsPanel.interrupt();
    }

    /**
     * Returns the value from the max record count fields.
     *
     * @return the max row count to be shown
     */
    public int getMaxRecords() {
        return maxRecordsCount;
    }

    /**
     * Sets the result set object.
     *
     * @param resultSet     the executed result set
     * @param showRowNumber to return the result set row count
     */
    public int setResultSet(ResultSet resultSet, boolean showRowNumber, DatabaseConnection dc) throws SQLException {

        int rowCount = -1;

        if (!executeToFile) {
            rowCount = resultsPanel.setResultSet(resultSet, showRowNumber, getMaxRecords(), dc);
            revalidate();

        } else
            new ExportDataPanel(resultSet, getDisplayName());

        return rowCount;
    }

    /**
     * Sets the result set object.
     *
     * @param resultSet the executed result set
     */
    public void setResultSet(ResultSet resultSet, DatabaseConnection dc) throws SQLException {

        if (!executeToFile) {
            resultsPanel.setResultSet(resultSet, true, getMaxRecords(), dc);
            revalidate();

        } else
            new ExportDataPanel(resultSet, getDisplayName());
    }

    /**
     * Sets the result set object.
     *
     * @param resultSet the executed result set
     * @param query     the executed query of the result set
     */
    public void setResultSet(ResultSet resultSet, String query, DatabaseConnection dc) throws SQLException {

        if (!executeToFile)
            resultsPanel.setResultSet(resultSet, true, getMaxRecords(), query, dc);
        else
            new ExportDataPanel(resultSet, getDisplayName());
    }

    /**
     * Sets to display the result set metadata for the
     * currently selected result set tab.
     */
    public void displayResultSetMetaData() {
        resultsPanel.displayResultSetMetaData();
    }

    /**
     * Returns the editor status bar.
     *
     * @return the editor's status bar panel
     */
    public QueryEditorStatusBar getStatusBar() {
        return statusBar;
    }

    /**
     * Disables/enables the listener updates as specified.
     */
    @Override
    public void disableUpdates(boolean disable) {
        editorPanel.disableUpdates(disable);
    }

    /**
     * Returns true that a search can be performed on the editor.
     */
    @Override
    public boolean canSearch() {
        return true;
    }

    public ResultSetTableModel getResultSetTableModel() {
        return resultsPanel.getResultSetTableModel();
    }

    public ResultSetTable getResultSetTable() {
        return resultsPanel.getResultSetTable();
    }

    public QueryEditorResultsPanel getResultsPanel() {
        return resultsPanel;
    }

    public void setResultText(DatabaseConnection dc, int updateCount, int type, String metaName) {
        resultsPanel.setResultText(dc, updateCount, type, metaName);
    }

    /**
     * Returns whether a result set panel is selected and that
     * that panel has a result set row count > 0.
     *
     * @return true | false
     */
    public boolean isResultSetSelected() {
        return resultsPanel.isResultSetSelected();
    }

    /**
     * Sets the text of the editor pane to the previous
     * query available in the history list. Where no previous
     * query exists, nothing is changed.
     */
    public void selectPreviousQuery() {
        try {

            GUIUtilities.showWaitCursor();
            String query = delegate.getPreviousQuery();
            setEditorText(query);

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    /**
     * Sets the text of the editor pane to the next
     * query available in the history list. Where no
     * next query exists, nothing is changed.
     */
    public void selectNextQuery() {
        try {

            String query = delegate.getNextQuery();
            setEditorText(query);

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    /**
     * Enables/disables the show metadata button.
     */
    public void setMetaDataButtonEnabled(boolean enable) {
        getTools().setMetaDataButtonEnabled(retainMetaData() && enable);
    }

    /**
     * Sets the history next button enabled as specified.
     */
    public void setHasNextStatement(boolean enabled) {
        getTools().setNextButtonEnabled(enabled);
    }

    /**
     * Sets the history previous button enabled as specified.
     */
    public void setHasPreviousStatement(boolean enabled) {
        getTools().setPreviousButtonEnabled(enabled);
    }

    /**
     * Enables/disables the export result set button.
     */
    public void setExportButtonEnabled(boolean enable) {
        getTools().setExportButtonEnabled(enable);
    }

    /**
     * Enables/disables the query execution stop button.
     */
    public void setStopButtonEnabled(boolean enable) {
        getTools().setStopButtonEnabled(enable);
    }

    public void resetCaretPositionToLast() {
        editorPanel.setTextFocus();
    }

    /**
     * Updates the interface and any system buttons as
     * required on a focus gain.
     */
    @Override
    public void focusGained() {

        QueryEditorToolBar tools = getTools();

        tools.setMetaDataButtonEnabled(resultsPanel.hasResultSetMetaData() && retainMetaData());
        tools.setCommitsEnabled(!delegate.getCommitMode());
        tools.setNextButtonEnabled(delegate.hasNextStatement());
        tools.setPreviousButtonEnabled(delegate.hasPreviousStatement());
        tools.setStopButtonEnabled(delegate.isExecuting());
        tools.setExportButtonEnabled(resultsPanel.isResultSetSelected());

        editorPanel.setTextFocus();
    }

    @Override
    public void focusLost() {
    }

    private boolean retainMetaData() {
        return userProperties().getBooleanProperty("editor.results.metadata");
    }

    private QueryEditorToolBar getTools() {
        return toolBar;
    }

    // --------------------------------------------
    // TabView implementation
    // --------------------------------------------

    /**
     * Indicates the panel is being removed from the pane
     */
    @Override
    public boolean tabViewClosing() {

        if (isExecutionActive())
            return false;

        if (isTransactionActive())
            return maybeCloseTransaction();

        if (saveUserFile())
            return false;

        tryCleanup();
        removeFromHistory();

        return true;
    }

    private boolean isExecutionActive() {

        if (isExecuting() && oldConnection.isConnected()) {
            if (MiscUtils.isMinJavaVersion(1, 6))
                GUIUtilities.displayWarningMessage(bundleString("isExecutionActive"));
            return true;
        }

        return false;
    }

    private boolean isTransactionActive() {
        return delegate.getIDTransaction() != -1;
    }

    private boolean maybeCloseTransaction() {

        int result = GUIUtilities.displayYesNoCancelDialog(
                bundleString("requestTransactionMessage"),
                bundleString("requestTransactionTitle")
        );

        if (result == JOptionPane.YES_OPTION) {
            delegate.commit(false);
            return tabViewClosing();

        } else if (result == JOptionPane.NO_OPTION) {
            delegate.rollback(false);
            return tabViewClosing();
        }

        return false;
    }

    private boolean saveUserFile() {
        UserProperties properties = UserProperties.getInstance();
        boolean conFlag = oldConnection == null || oldConnection.isConnected();

        if (properties.getBooleanProperty("general.save.prompt") && isContentChanged && conFlag) {

            String oldPath = getAbsolutePath();

            if (GUIUtilities.saveOpenChanges(this)) {

                String newPath = getAbsolutePath();
                String connectionID = (oldConnection != null) ? oldConnection.getId() : QueryEditorHistory.NULL_CONNECTION;

                if (!Objects.equals(oldPath, newPath))
                    scriptFile.setAbsolutePath(oldPath);

                QueryEditorHistory.removeEditor(connectionID, getAbsolutePath());
                if (QueryEditorHistory.isDefaultEditorDirectory(this))
                    QueryEditorHistory.removeFile(oldPath);

                scriptFile.setAbsolutePath(newPath);
                QueryEditorHistory.addEditor(connectionID, getAbsolutePath(), -1, splitPane.getDividerLocation(), autosaveEnabled);

            } else
                return true;
        }

        return false;
    }

    private void tryCleanup() {
        try {
            cleanup();

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ExceptionOnClose", e.getMessage()), e, this.getClass());
        }
    }

    private void removeFromHistory() {
        try {
            DatabaseConnection dc = getSelectedConnection();
            if (dc == null || dc.isConnected()) {
                String connectionID = dc != null ? dc.getId() : QueryEditorHistory.NULL_CONNECTION;
                String editorPath = getAbsolutePath();

                QueryEditorHistory.removeEditor(connectionID, editorPath);
                if (QueryEditorHistory.isDefaultEditorDirectory(this))
                    QueryEditorHistory.removeFile(editorPath);
            }

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    @Override
    public boolean tabViewSelected() {
        focusGained();
        return true;
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    @Override
    public boolean tabViewDeselected() {
        return true;
    }

    // --------------------------------------------

    public void interruptStatement() {

        if (Log.isDebugEnabled())
            Log.debug("Interrupt statement selected");

        delegate.interrupt();
    }

    @Override
    public void selectAll() {
        editorPanel.selectAll();
    }

    public void goToRow(int row) {
        editorPanel.goToRow(row);
    }

    @Override
    public void selectNone() {
        editorPanel.selectNone();
    }

    /**
     * Returns the currently selected connection properties object.
     *
     * @return the selected connection
     */
    public DatabaseConnection getSelectedConnection() {

        if (useMultipleConnections) {
            return selectedConnections != null && !selectedConnections.isEmpty() ?
                    (DatabaseConnection) selectedConnections.get(0) :
                    null;
        }

        return connectionsCombo.getSelectedConnection();
    }

    public void setSelectedConnection(DatabaseConnection databaseConnection) {

        if (connectionsCheckCombo.getModel().contains(databaseConnection)) {
            connectionsCheckCombo.getModel().removeChecks();
            connectionsCheckCombo.getModel().addCheck(databaseConnection);

        } else if (useMultipleConnections)
            connectionToSelect = databaseConnection;

        if (connectionsCombo.hasConnection(databaseConnection)) {
            connectionsCombo.setSelectedItem(databaseConnection);

        } else if (!useMultipleConnections)
            connectionToSelect = databaseConnection;
    }

    public void preExecute() {
        resultsPanel.preExecute();
    }

    public void preExecute(DatabaseConnection dc) {
        resultsPanel.preExecute(dc);
    }

    private boolean isExecuting() {
        return delegate.isExecuting();
    }

    /**
     * Execute specified query as single statement
     *
     * @param query query to execute
     */
    public void executeStatement(String query) {

        query = getQueryToExecute(query);
        boolean executeAsBlock = new SqlParser(query).isExecuteBlock();

        if (useMultipleConnections && selectedConnections.size() > 1) {
            for (Object object : selectedConnections) {
                DatabaseConnection connection = (DatabaseConnection) object;

                preExecute(connection);
                delegates.get(connection).executeStatement(
                        connection,
                        query,
                        executeAsBlock,
                        true,
                        true
                );
            }

        } else {
            preExecute();
            delegate.executeStatement(
                    getSelectedConnection(),
                    query,
                    executeAsBlock,
                    false,
                    true
            );
        }
    }

    /**
     * Parse specified query into several single statements
     * and execute them
     *
     * @param query query to execute
     */
    public void executeScript(String query) {

        query = getQueryToExecute(query);

        if (useMultipleConnections && selectedConnections.size() > 1) {
            for (Object object : selectedConnections) {
                DatabaseConnection connection = (DatabaseConnection) object;

                preExecute(connection);
                delegates.get(connection).executeScript(
                        connection,
                        query,
                        true
                );
            }

        } else {
            preExecute();
            delegate.executeScript(getSelectedConnection(), query, false);
        }
    }

    /**
     * Execute specified query as single statement
     * with starting profiler
     *
     * @param query query to execute
     */
    public void executeInProfiler(String query) {

        if (useMultipleConnections && selectedConnections.size() > 1) {
            GUIUtilities.displayWarningMessage(Bundles.get(ProfilerPanel.class, "MultipleConnectionsNotSupported"));
            return;
        }

        query = getQueryToExecute(query);
        preExecute();
        delegate.executeInProfiler(getSelectedConnection(), query);
    }

    private String getQueryToExecute(String query) {

        if (MiscUtils.isNull(query)) {
            query = MiscUtils.isNull(editorPanel.getSelectedText()) ?
                    editorPanel.getQueryAreaText() :
                    editorPanel.getSelectedText();
        }

        return query;
    }

    public void printExecutedPlan() {
        preExecute();
        delegate.printExecutedPlan(getSelectedConnection(), getQueryToExecute(null));
    }

    @Override
    public JTextComponent getEditorTextComponent() {
        return editorPanel.getQueryArea();
    }

    /**
     * Adds a comment tag to the beginning of the current line
     * or selected lines.
     */
    public void commentLines() {
        editorPanel.commentLines();
    }

    /**
     * Shifts the text on the current line or the currently
     * selected text to the right one TAB.
     */
    public void shiftTextRight() {
        editorPanel.shiftTextRight();
    }

    /**
     * Shifts the text on the current line or the currently
     * selected text to the left one TAB.
     */
    public void shiftTextLeft() {
        editorPanel.shiftTextLeft();
    }

    public void moveSelectionUp() {
        editorPanel.moveSelectionUp();
    }

    public void moveSelectionDown() {
        editorPanel.moveSelectionDown();
    }

    /**
     * Duplicates the cursor current row up
     */
    public void duplicateRowUp() {
        editorPanel.duplicateRowUp();
    }

    /**
     * Duplicates the cursor current row down
     */
    public void duplicateRowDown() {
        editorPanel.duplicateRowDown();
    }

    /**
     * Sets the editor's text content that specified.
     *
     * @param text the text to be set
     */
    public void setEditorText(String text) {
        editorPanel.setQueryAreaText(text);
        if (autosaveEnabled)
            save(false);
    }

    /**
     * Moves the caret to the beginning of the specified query.
     *
     * @param query the query to move the cursor to
     */
    public void caretToQuery(String query) {
        editorPanel.caretToQuery(query);
    }

    /**
     * Loads the specified text into a blank 'offscreen' document
     * before switching to the SQL document.
     */
    public void loadText(String text) {
        editorPanel.loadText(text);
    }

    public void insertTextAtEnd(String text) {

        int end = getEditorText().length();
        insertTextAfter(end - 1, text);
        caretToQuery(text);
    }

    public void insertTextAfter(int after, String text) {
        editorPanel.insertTextAfter(after, text);
    }

    public boolean hasText() {
        return !(MiscUtils.isNull(getEditorText()));
    }

    @Override
    public String getEditorText() {
        return editorPanel.getQueryAreaText();
    }

    public void setOutputMessage(DatabaseConnection dc, int type, String text) {
        resultsPanel.setOutputMessage(dc, type, text);
    }

    public void setOutputMessage(DatabaseConnection dc, int type, String text, boolean selectTab) {
        resultsPanel.setOutputMessage(dc, type, text, selectTab);
    }


    /**
     * Sets the state for an open file.
     *
     * @param absolutePath the absolute file path
     */
    public void setOpenFilePath(String absolutePath) {
        scriptFile.setAbsolutePath(absolutePath);
    }

    /**
     * Returns whether the text component is in a printable state.
     */
    @Override
    public boolean canPrint() {
        return true;
    }

    @Override
    public Printable getPrintable() {
        return getPrintableForResultSet();
    }

    public Printable getPrintableForResultSet() {
        return new TablePrinter(resultsPanel.getResultSetTable(), "Query: " + editorPanel.getQueryAreaText());
    }

    public Printable getPrintableForQueryArea() {
        return new TextPrinter(editorPanel.getQueryAreaText());
    }

    @Override
    public String getPrintJobName() {
        return "Red Expert - editor";
    }

    // ---------------------------------------------
    // TextEditor implementation
    // ---------------------------------------------

    @Override
    public void paste() {
        editorPanel.paste();
    }

    @Override
    public void copy() {
        editorPanel.copy();
    }

    @Override
    public void cut() {
        editorPanel.cut();
    }

    @Override
    public void deleteLine() {
        editorPanel.deleteLine();
    }

    @Override
    public void deleteWord() {
        editorPanel.deleteWord();
    }

    @Override
    public void deleteSelection() {
        editorPanel.deleteSelection();
    }

    @Override
    public void insertFromFile() {
        editorPanel.insertFromFile();
    }

    @Override
    public void insertLineAfter() {
        editorPanel.insertLineAfter();
    }

    @Override
    public void insertLineBefore() {
        editorPanel.insertLineBefore();
    }

    @Override
    public void changeSelectionCase(boolean upper) {
        editorPanel.changeSelectionCase(upper);
    }

    @Override
    public void changeSelectionToUnderscore() {
        editorPanel.changeSelectionToUnderscore();
    }

    @Override
    public void changeSelectionToCamelCase() {
        editorPanel.changeSelectionToCamelCase();
    }

    // ---------------------------------------------
    // SaveFunction implementation
    // ---------------------------------------------

    @Override
    public String getDisplayName() {
        return toString();
    }

    @Override
    public boolean contentCanBeSaved() {
        return isContentChanged;
    }

    @Override
    public int save(boolean saveAs) {

        String text = editorPanel.getQueryAreaText();
        QueryEditorFileWriter writer = new QueryEditorFileWriter();
        String oldAbsolutePath = scriptFile.getAbsolutePath();
        boolean saved = writer.write(text, scriptFile, saveAs);
        if (saved) {

            GUIUtilities.setTabTitleForComponent(this, getDisplayName());
            statusBar.setStatus(bundleString("FileSavedTo", scriptFile.getFileName()));

            isContentChanged = false;
        }
        if (!scriptFile.getAbsolutePath().contentEquals(oldAbsolutePath)) {
            String connectionID = (getSelectedConnection() != null) ?
                    getSelectedConnection().getId() : QueryEditorHistory.NULL_CONNECTION;
            QueryEditorHistory.PathNumber editor = QueryEditorHistory.getEditor(connectionID, oldAbsolutePath);
            QueryEditorHistory.removeEditor(connectionID, oldAbsolutePath);
            QueryEditorHistory.addEditor(connectionID, getAbsolutePath(), editor.number, splitPane.getDividerLocation(), autosaveEnabled);
        }
        return SaveFunction.SAVE_COMPLETE;
    }

    public String getAbsolutePath() {
        return scriptFile.getAbsolutePath();
    }

    // ---------------------------------------------

    /**
     * Returns the display name of this panel. This may
     * include the path of any open file.
     *
     * @return the display name
     */
    @Override
    public String toString() {
        return String.format("%s - %s", TITLE, scriptFile.getFileName());
    }

    /**
     * Sets that the text content of the editor has changed from
     * the original or previously saved state.
     */
    public void setContentChanged(boolean contentChanged) {
        isContentChanged = contentChanged;

        if (isContentChanged && autosaveEnabled) {
            isContentChanged = false;
            save(false);

        } else if (isContentChanged)
            statusBar.setStatus(bundleString("UnsavedChanges"));
    }

    // ---------------------------------------------
    // ConnectionListener implementation
    // ---------------------------------------------

    /**
     * Performs any resource clean up for a pending removal.
     */
    public void cleanup() {

        /*
         * profiling found the popup keeps the
         * editor from being garbage collected at all!!
         * a call to removeAll() is a workaround for now
         */

        isQueryEditorClosed = true;
        popup.removeAll();

        editorPanel.closingEditor();
        resultsPanel.cleanup();
        statusBar.cleanup();

        resultsPanel = null;
        statusBar = null;
        toolBar = null;
        editorPanel = null;

        Arrays.stream(connectionsCombo.getItemListeners()).forEach(l -> connectionsCombo.removeItemListener(l));
        delegate.disconnected(getSelectedConnection());

        removeAll();
        EventMediator.deregisterListener(this);
        GUIUtilities.registerUndoRedoComponent(null);

    }

    /**
     * Indicates a connection has been established.
     *
     * @param connectionEvent the encapsulating event
     */
    @Override
    public void connected(ConnectionEvent connectionEvent) {

        if (!isQueryEditorClosed) {
            connectionsCheckCombo.getModel().addElement(connectionEvent.getDatabaseConnection());

            DatabaseConnection databaseConnection = connectionEvent.getDatabaseConnection();
            if (databaseConnection == connectionToSelect) {
                connectionsCheckCombo.getModel().addCheck(databaseConnection);
                connectionsCombo.setSelectedItem(databaseConnection);
                connectionToSelect = null;
            }
        }
    }

    @Override
    public void disconnected(ConnectionEvent connectionEvent) {
        if (!isQueryEditorClosed)
            connectionsCheckCombo.getModel().removeElement(connectionEvent.getDatabaseConnection());
    }

    // ---------------------------------------------

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent)
                || (event instanceof UserPreferenceEvent)
                || (event instanceof QueryShortcutEvent)
                || (event instanceof QueryBookmarkEvent);
    }

    @Override
    public void queryBookmarkAdded(QueryBookmarkEvent e) {
        handleBookmarkEvent();
    }

    @Override
    public void queryBookmarkRemoved(QueryBookmarkEvent e) {
        handleBookmarkEvent();
    }

    private void handleBookmarkEvent() {
        toolBar.reloadBookmarkItems();
    }

    public void formatSQLText() {

        int start = editorPanel.getSelectionStart();
        int end = editorPanel.getSelectionEnd();

        // if there is no selection, then select all text
        if (start == end) {
            start = 0;
            end = editorPanel.getQueryAreaText().length();
        }

        String text = getSelectedText();
        if (text == null)
            text = editorPanel.getQueryAreaText();

        String formattedText = tokenizingFormatter.format(text);
        editorPanel.replaceRegion(start, end, formattedText);
    }

    private String getSelectedText() {
        return editorPanel.getSelectedText();
    }

    @Override
    public void preferencesChanged(UserPreferenceEvent event) {
        QueryEditorSettings.initialise();
        if (event.getEventType() == UserPreferenceEvent.QUERY_EDITOR || event.getEventType() == UserPreferenceEvent.ALL)
            setEditorPreferences();
    }

    @Override
    public void queryShortcutAdded(QueryShortcutEvent e) {
        editorPanel.editorShortcutsUpdated();
    }

    @Override
    public void queryShortcutRemoved(QueryShortcutEvent e) {
        editorPanel.editorShortcutsUpdated();
    }

    public void allResultTabsClosed() {
        lastDividerLocation = splitPane.getDividerLocation();
        baseEditorPanel.setVisible(true);
        resultsPanel.setVisible(false);
    }

    public void toggleResultPane() {

        if (baseEditorPanel.isVisible()) {
            lastDividerLocation = splitPane.getDividerLocation();
            baseEditorPanel.setVisible(false);

        } else {
            baseEditorPanel.setVisible(true);
            splitPane.setDividerLocation(lastDividerLocation);
        }

    }

    public void setAutosaveEnabled(boolean autosaveEnabled) {
        this.autosaveEnabled = autosaveEnabled;
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(QueryEditor.class, key, args);
    }

    // --- ListCheckListener impl ---

    @Override
    public void addCheck(ListEvent listEvent) {
        if (useMultipleConnections)
            connectionChanged();
    }

    @Override
    public void removeCheck(ListEvent listEvent) {
        if (useMultipleConnections)
            connectionChanged();
    }

    private void connectionChanged() {

        selectedConnections = connectionsCheckCombo.getModel()
                .getCheckeds().stream()
                .filter(connectionsCheckCombo.getModel()::isChecked)
                .collect(Collectors.toList());

        selectedConnections.forEach(dc -> {
            if (!delegates.containsKey((DatabaseConnection) dc)) {
                QueryEditorDelegate queryEditorDelegate = new QueryEditorDelegate(this);
                delegates.put((DatabaseConnection) dc, queryEditorDelegate);
                queryEditorDelegate.setTPP(transactionParametersPanel);
            }
        });

        String oldConnectionId = oldConnection != null ?
                oldConnection.getId() :
                QueryEditorHistory.NULL_CONNECTION;

        String newConnectionId = getSelectedConnection() != null ?
                getSelectedConnection().getId() :
                QueryEditorHistory.NULL_CONNECTION;

        QueryEditorHistory.changedConnectionEditor(oldConnectionId, newConnectionId, scriptFile.getAbsolutePath());

        oldConnection = getSelectedConnection();
        editorPanel.getQueryArea().setDatabaseConnection(getSelectedConnection());
        transactionParametersPanel.setDatabaseConnection(getSelectedConnection());
    }

    // --- PropertyChangeListener impl ---

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        DatabaseConnection dc = getSelectedConnection();
        String oldAbsolutePath = scriptFile.getAbsolutePath();
        String connectionID = dc != null ? dc.getId() : QueryEditorHistory.NULL_CONNECTION;

        QueryEditorHistory.PathNumber editor = QueryEditorHistory.getEditor(connectionID, oldAbsolutePath);
        QueryEditorHistory.removeEditor(connectionID, oldAbsolutePath);
        QueryEditorHistory.addEditor(connectionID, getAbsolutePath(), editor.number, splitPane.getDividerLocation(), autosaveEnabled);
    }

}
