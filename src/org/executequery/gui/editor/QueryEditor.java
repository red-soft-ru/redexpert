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

import org.apache.commons.lang.StringUtils;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabView;
import org.executequery.components.SplitPaneFactory;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.*;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.FocusablePanel;
import org.executequery.gui.SaveFunction;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.components.SelectConnectionsPanel;
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
import org.underworldlabs.sqlParser.SqlParser;
import org.underworldlabs.swing.DefaultTextField;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.print.Printable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The Query Editor.
 *
 * @author Takis Diakoumis
 */
@SuppressWarnings("FieldCanBeLocal")
public class QueryEditor extends DefaultTabView
        implements ConnectionListener,
        QueryBookmarkListener,
        QueryShortcutListener,
        UserPreferenceListener,
        TextEditor,
        KeywordListener,
        FocusablePanel {

    public static final String TITLE = Bundles.get(QueryEditor.class, "title");
    public static final String FRAME_ICON = "Edit16.png";

    private static final String DEFAULT_SCRIPT_PREFIX = Bundles.get(QueryEditor.class, "script");
    private static final String DEFAULT_SCRIPT_SUFFIX = ".sql";

    // --- GUI elements ---

    private QueryEditorStatusBar statusBar;
    private QueryEditorTextPanel editorPanel;
    private QueryEditorResultsPanel resultsPanel;
    private QueryEditorToolBar toolBar;
    private QueryEditorPopupMenu popup;
    private TransactionParametersPanel transactionParametersPanel;

    private OpenConnectionsComboBox connectionsCombo;

    private JCheckBox maxRowCountCheckBox;
    private JCheckBox stopOnErrorCheckBox;
    private JCheckBox showTPPCheckBox;
    private JCheckBox lineWrapperCheckBox;
    private JCheckBox executeToFileCheckBox;

    private JPanel resultsBasePanel;
    private JPanel toolsPanel;
    private JPanel baseEditorPanel;

    private JSplitPane splitPane;
    private JTextField filterTextField;
    private NumberTextField maxRowCountField;

    // ---

    private final ScriptFile scriptFile = new ScriptFile();
    private final TokenizingFormatter tokenizingFormatter = new TokenizingFormatter();

    private DatabaseConnection selectConnection;
    private DatabaseConnection oldConnection;
    private QueryEditorDelegate delegate;
    private Map<DatabaseConnection, QueryEditorDelegate> delegates;

    private int lastDividerLocation;
    private int queryEditorNumber;
    private boolean isQueryEditorClosed;
    private boolean isContentChanged;

    /**
     * Creates a new query editor with the specified text content
     * and the specified absolute file path.
     */
    public QueryEditor() {
        this(null, null);
    }

    /**
     * Creates a new query editor with the specified text content
     * and the specified absolute file path.
     *
     * @param text the text content to be set
     */
    public QueryEditor(String text) {
        this(text, null);
    }

    /**
     * Creates a new query editor with the specified text content
     * and the specified absolute file path.
     *
     * @param text         the text content to be set
     * @param absolutePath the absolute file path;
     */
    public QueryEditor(String text, String absolutePath) {

        super(new GridBagLayout());

        try {
            init();
            arrange();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

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

        QueryEditorHistory.addEditor(connectionID, absolutePath, queryEditorNumber);
        isContentChanged = false;

        if (text != null)
            loadText(text);
    }

    private void init() {

        queryEditorNumber = -1;
        isQueryEditorClosed = false;
        delegates = new HashMap<>();

        // --- status bar ---

        statusBar = new QueryEditorStatusBar();
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, -1, -2, -2));
        statusBar.setCaretPosition(1, 1);
        statusBar.setInsertionMode("INS");

        // --- combo boxes ---

        connectionsCombo = new OpenConnectionsComboBox(this, ConnectionManager.getActiveConnections());
        connectionsCombo.addItemListener(this::connectionChanged);

        oldConnection = (DatabaseConnection) connectionsCombo.getSelectedItem();

        // --- check boxes ---

        maxRowCountCheckBox = WidgetFactory.createCheckBox("maxRowCountCheckBox", bundleString("MaxRows"));
        maxRowCountCheckBox.setToolTipText(bundleString("MaxRows.tool-tip"));
        maxRowCountCheckBox.addChangeListener(e -> maxRowCountCheckBoxSelected());

        stopOnErrorCheckBox = WidgetFactory.createCheckBox("stopOnErrorCheckBox", bundleString("StopOnError"));
        stopOnErrorCheckBox.setToolTipText(bundleString("StopOnError.tool-tip"));
        stopOnErrorCheckBox.addChangeListener(e -> SystemProperties.setBooleanProperty(
                "user", "editor.stop.on.error", stopOnErrorCheckBox.isSelected())
        );

        lineWrapperCheckBox = WidgetFactory.createCheckBox("lineWrapperCheckBox", bundleString("LineWrapper"));
        lineWrapperCheckBox.setToolTipText(bundleString("LineWrapper.tool-tip"));
        lineWrapperCheckBox.addChangeListener(e -> switchLineWrapping());

        executeToFileCheckBox = WidgetFactory.createCheckBox("exportToFileCheckBox", bundleString("exportToFileCheckBox"));

        showTPPCheckBox = WidgetFactory.createCheckBox("showTPPCheckBox", bundleString("ShowTPP"));
        showTPPCheckBox.addChangeListener(e -> transactionParametersPanel.setVisible(showTPPCheckBox.isSelected()));

        // --- transaction parameters panel ---

        transactionParametersPanel = new TransactionParametersPanel(getSelectedConnection());
        transactionParametersPanel.setVisible(showTPPCheckBox.isSelected());

        // --- delegate ---

        delegate = new QueryEditorDelegate(this);
        delegate.setTPP(transactionParametersPanel);
        popup = new QueryEditorPopupMenu(delegate);

        // --- panels ---

        editorPanel = new QueryEditorTextPanel(this);
        editorPanel.addEditorPaneMouseListener(popup);

        toolBar = new QueryEditorToolBar(editorPanel.getTextPaneActionMap(), editorPanel.getTextPaneInputMap());
        resultsPanel = new QueryEditorResultsPanel(this);
        baseEditorPanel = new JPanel(new BorderLayout());
        resultsBasePanel = new JPanel(new BorderLayout());
        toolsPanel = new JPanel(new GridBagLayout());

        splitPane = new SplitPaneFactory().usesCustomSplitPane() ?
                new EditorSplitPane(JSplitPane.VERTICAL_SPLIT, baseEditorPanel, resultsBasePanel) :
                new JSplitPane(JSplitPane.VERTICAL_SPLIT, baseEditorPanel, resultsBasePanel);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(4);

        // --- others ---

        maxRowCountField = new MaxRowCountField(this);
        maxRowCountField.setName("maxRowCountField");
    }

    private void arrange() {

        GridBagHelper gbh;

        // --- base editor panel ---

        baseEditorPanel.add(editorPanel, BorderLayout.CENTER);
        baseEditorPanel.add(statusBar, BorderLayout.SOUTH);
        baseEditorPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, GUIUtilities.getDefaultBorderColour()));

        // --- results base panel ---

        resultsBasePanel.add(resultsPanel, BorderLayout.CENTER);

        // --- check box panel ---

        JPanel checkBoxPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(0, 0, 5, 0).anchorNorthWest().fillHorizontally();
        checkBoxPanel.add(stopOnErrorCheckBox, gbh.get());
        checkBoxPanel.add(lineWrapperCheckBox, gbh.nextCol().get());
        checkBoxPanel.add(executeToFileCheckBox, gbh.nextCol().get());
        checkBoxPanel.add(showTPPCheckBox, gbh.nextCol().spanX().get());

        // --- tools panel ---

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();
        toolsPanel.add(toolBar, gbh.spanX().get());
        toolsPanel.add(createLabel(Bundles.getCommon("connection"), 'C'), gbh.nextRowFirstCol().setMinWeightX().setWidth(1).get());
        toolsPanel.add(connectionsCombo, gbh.nextCol().setWeightX(0.5).get());
        toolsPanel.add(createLabel(bundleString("Filter"), 'l'), gbh.nextCol().setMinWeightX().get());
        toolsPanel.add(createResultSetFilterTextField(), gbh.nextCol().fillHorizontally().setMaxWeightX().get());
        toolsPanel.add(maxRowCountCheckBox, gbh.setLabelDefault().nextCol().get());
        toolsPanel.add(maxRowCountField, gbh.nextCol().fillHorizontally().setWeightX(0.3).get());
        toolsPanel.add(checkBoxPanel, gbh.nextRowFirstCol().setMaxWeightX().spanX().get());
        toolsPanel.add(transactionParametersPanel, gbh.nextRowFirstCol().fillHorizontally().spanX().get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolsPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // --- base ---

        add(mainPanel, new GridBagHelper().anchorSouthEast().fillBoth().spanX().spanY().get());
        EventMediator.registerListener(this);
        addDeleteLineActionMapping();
        setEditorPreferences();
    }

    private void connectionChanged(ItemEvent e) {

        if (e.getStateChange() != ItemEvent.SELECTED)
            return;

        String idConnection = oldConnection != null ?
                oldConnection.getId() :
                QueryEditorHistory.NULL_CONNECTION;

        QueryEditorHistory.changedConnectionEditor(idConnection, getSelectedConnection().getId(), scriptFile.getAbsolutePath());
        oldConnection = getSelectedConnection();
        editorPanel.getQueryArea().setDatabaseConnection(getSelectedConnection());
        transactionParametersPanel.setDatabaseConnection(getSelectedConnection());
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

    private JTextField createResultSetFilterTextField() {

        filterTextField = new DefaultTextField();
        filterTextField.setFocusAccelerator('l');
        filterTextField.setToolTipText(bundleString("FilterToolTip"));
        filterTextField.addActionListener(e -> resultsPanel.filter(filterTextField.getText()));

        return filterTextField;
    }

    private void switchLineWrapping() {
        editorPanel.getQueryArea().setLineWrap(!editorPanel.getQueryArea().getLineWrap());
        editorPanel.getQueryArea().changedUpdate(null);
    }

    private void maxRowCountCheckBoxSelected() {
        maxRowCountField.setEnabled(maxRowCountCheckBox.isSelected());
        maxRowCountField.requestFocus();
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

    private JLabel createLabel(String text, char mnemonic) {

        JLabel label = new JLabel(text);
        label.setDisplayedMnemonic(mnemonic);

        return label;
    }

    /**
     * Toggles the output pane visible or not.
     */
    public void toggleOutputPaneVisible() {

        if (resultsBasePanel.isVisible()) {
            lastDividerLocation = splitPane.getDividerLocation();
            resultsBasePanel.setVisible(false);

        } else {
            resultsBasePanel.setVisible(true);
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

        setPanelBackgrounds();

        statusBar.setVisible(isStatusBarVisible());
        toolsPanel.setVisible(isToolsPanelVisible());
        editorPanel.showLineNumbers(isLineNumbersVisible());
        editorPanel.preferencesChanged();
        delegate.preferencesChanged();
        delegate.setCommitMode(isAutoCommit());
        popup.setCommitMode(isAutoCommit());
        resultsPanel.setTableProperties();
        editorPanel.getQueryArea().setAutocompleteOnlyHotKey(SystemProperties.getBooleanProperty("user", "editor.autocomplete.only.hotkey"));

        if (!isAutoCompleteOn())
            editorPanel.getQueryArea().deregisterAutoCompletePopup();

        int maxRecords = SystemProperties.getIntProperty("user", "editor.max.records");
        maxRowCountCheckBox.setSelected(maxRecords > 0);
        maxRowCountCheckBoxSelected();
        stopOnErrorCheckBox.setSelected(SystemProperties.getBooleanProperty("user", "editor.stop.on.error"));

    }

    private boolean isAutoCompleteOn() {

        UserProperties userProperties = userProperties();

        if (userProperties.containsKey("editor.autocomplete.on")
                && (!userProperties.containsKey("editor.autocomplete.keywords.on"))
                && !userProperties.containsKey("editor.autocomplete.schema.on")) {

            boolean allOn = userProperties.getBooleanProperty("editor.autocomplete.on");
            userProperties.setBooleanProperty("editor.autocomplete.keywords.on", allOn);
            userProperties.setBooleanProperty("editor.autocomplete.schema.on", allOn);
        }

        return userProperties.getBooleanProperty("editor.autocomplete.keywords.on")
                || userProperties.getBooleanProperty("editor.autocomplete.schema.on");
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
     * Notification of a new keyword added to the list.
     */
    @Override
    public void keywordsAdded(KeywordEvent e) {
        editorPanel.setSQLKeywords(true);
    }

    /**
     * Notification of a keyword removed from the list.
     */
    @Override
    public void keywordsRemoved(KeywordEvent e) {
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

        if (maxRowCountCheckBox.isSelected()) {

            int maxRecords = maxRowCountField.getValue();
            if (maxRecords <= 0) {
                maxRecords = -1;
                maxRowCountField.setValue(-1);
            }

            return maxRecords;
        }

        return -1;
    }

    /**
     * Requests focus on connection combo
     */
    public void selectConnectionCombo() {
        connectionsCombo.attemptToFocus();
    }

    /**
     * Sets the result set object.
     *
     * @param resultSet     the executed result set
     * @param showRowNumber to return the result set row count
     */
    public int setResultSet(ResultSet resultSet, boolean showRowNumber, DatabaseConnection dc) throws SQLException {

        int rowCount = -1;

        if (!executeToFileCheckBox.isSelected()) {
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

        if (!executeToFileCheckBox.isSelected()) {
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

        if (!executeToFileCheckBox.isSelected())
            resultsPanel.setResultSet(resultSet, true, getMaxRecords(), query, dc);
        else
            new ExportDataPanel(resultSet, getDisplayName());
    }

    public void destroyTable() {
        resultsPanel.destroyTable();
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
     * Sets the respective panel background colours within
     * the editor as specified by the user defined properties.
     */
    public void setPanelBackgrounds() {
        editorPanel.setTextPaneBackground(userProperties().getColourProperty("editor.text.background.colour"));
        resultsPanel.setResultBackground(userProperties().getColourProperty("editor.results.background.colour"));
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

    public void destroyConnection() {
        delegate.destroyConnection();
        editorPanel.getQueryArea().resetAutocomplete();
    }

    public void toggleCommitMode() {

        boolean mode = !delegate.getCommitMode();

        delegate.setCommitMode(mode);
        popup.setCommitMode(mode);
        getTools().setCommitsEnabled(!mode);

    }

    // --------------------------------------------
    // TabView implementation
    // --------------------------------------------

    /**
     * Indicates the panel is being removed from the pane
     */
    @Override
    public boolean tabViewClosing() {

        if (isExecuting() && oldConnection.isConnected()) {

            if (MiscUtils.isMinJavaVersion(1, 6))
                GUIUtilities.displayWarningMessage("Editor is currently executing.\n" +
                        "Please wait until finished or attempt to cancel the running query.");

            return false;
        }
        if (delegate.getIDTransaction() != -1) {
            int result = GUIUtilities.displayConfirmCancelDialog(bundleString("requestTransactionMessage"));
            if (result == JOptionPane.YES_OPTION)
                delegate.commit(false);
            else if (result == JOptionPane.NO_OPTION)
                delegate.rollback(false);
            else return false;
            return tabViewClosing();
        }

        UserProperties properties = UserProperties.getInstance();
        boolean conFlag = oldConnection == null || oldConnection.isConnected();

        if (properties.getBooleanProperty("general.save.prompt") && isContentChanged && conFlag) {

            String oldPath = getAbsolutePath();

            if (GUIUtilities.saveOpenChanges(this)) {

                String newPath = getAbsolutePath();
                String connectionID = (oldConnection != null) ? oldConnection.getId() : QueryEditorHistory.NULL_CONNECTION;

                if (!oldPath.equals(newPath))
                    scriptFile.setAbsolutePath(oldPath);

                QueryEditorHistory.removeEditor(connectionID, getAbsolutePath());
                if (QueryEditorHistory.isDefaultEditorDirectory(this))
                    QueryEditorHistory.removeFile(oldPath);

                scriptFile.setAbsolutePath(newPath);
                QueryEditorHistory.addEditor(connectionID, getAbsolutePath(), -1);

            } else
                return false;
        }

        try {
            cleanup();

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog("An error occurred when closing this editor." +
                    "\nWhile this could be nothing, sometimes it helps to check the stack trace to see if anything peculiar happened." +
                    "\n\nThe system returned:\n" + e.getMessage(), e);
        }

        String connectionID = (getSelectedConnection() != null) ?
                getSelectedConnection().getId() : QueryEditorHistory.NULL_CONNECTION;

        try {
            QueryEditorHistory.removeEditor(connectionID, scriptFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        return true;
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

    public void clearOutputPane() {

        if (!delegate.isExecuting())
            resultsPanel.clearOutputPane();
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
     * Executes the currently selected query text.
     */
    public void executeSelection() {

        String query = editorPanel.getSelectedText();
        executeSQLQuery(query);
    }

    /**
     * Returns the currently selected connection properties object.
     *
     * @return the selected connection
     */
    public DatabaseConnection getSelectedConnection() {

        if (connectionsCombo.getSelectedIndex() != -1)
            return (DatabaseConnection) connectionsCombo.getSelectedItem();
        return null;
    }

    public void setSelectedConnection(DatabaseConnection databaseConnection) {

        if (connectionsCombo.contains(databaseConnection))
            connectionsCombo.getModel().setSelectedItem(databaseConnection);
        else
            selectConnection = databaseConnection;
    }

    public void preExecute() {
        filterTextField.setText("");
        resultsPanel.preExecute();
    }

    public void preExecute(DatabaseConnection dc) {

        filterTextField.setText("");
        resultsPanel.preExecute(dc);
    }

    public String getCompleteWordEndingAtCursor() {
        return editorPanel.getCompleteWordEndingAtCursor();
    }

    private boolean isExecuting() {
        return delegate.isExecuting();
    }

    public void executeAsBlock() {
        delegate.executeQuery(null, true, true);
    }

    /**
     * Executes the specified query.
     *
     * @param query query to execute
     */
    public void executeSQLQuery(String query) {

        preExecute();
        if (query == null)
            query = editorPanel.getQueryAreaText();

        boolean executeAsBlock = new SqlParser(query).isExecuteBlock();
        delegate.executeQuery(getSelectedConnection(), query, executeAsBlock, false, true);
    }

    public void executeSQLQueryInAnyConnections(String query) {
        showSelectConnectionsDialog();
        if (selectConnectionsPanel.isSuccess()) {


            if (query == null) {
                query = editorPanel.getQueryAreaText();
            }

            for (DatabaseConnection dc : selectConnectionsPanel.getSelectedConnections()) {
                preExecute(dc);
                delegates.get(dc).executeQuery(dc, query, true, true, true);
            }
        }
    }

    SelectConnectionsPanel selectConnectionsPanel;

    private void showSelectConnectionsDialog() {

        selectConnectionsPanel = new SelectConnectionsPanel();
        BaseDialog dialog = new BaseDialog("", true);
        dialog.setContentPane(selectConnectionsPanel);
        selectConnectionsPanel.setDialog(dialog);
        selectConnectionsPanel.display();
        dialog.display();
        List<DatabaseConnection> selectedConnections = selectConnectionsPanel.getSelectedConnections();
        for (DatabaseConnection dc : selectedConnections) {
            if (!delegates.containsKey(dc)) {
                QueryEditorDelegate queryEditorDelegate = new QueryEditorDelegate(this);
                delegates.put(dc, queryEditorDelegate);
                queryEditorDelegate.setTPP(transactionParametersPanel);
            }
        }

    }

    public void executeSQLScript(String script) {

        preExecute();
        if (script == null)
            script = editorPanel.getQueryAreaText();

        delegate.executeScript(getSelectedConnection(), script, false);
    }

    public void executeInProfiler(String query) {

        preExecute();

        if (query == null)
            query = editorPanel.getQueryAreaText();

        boolean executeAsBlock = new SqlParser(query).isExecuteBlock();
        delegate.executeQueryInProfiler(getSelectedConnection(), query, executeAsBlock);

    }

    public void printExecutedPlan(boolean explained) {

        preExecute();
        String query = editorPanel.getSelectedText();
        if (MiscUtils.isNull(query))
            query = editorPanel.getQueryAreaText();

        delegate.printExecutedPlan(getSelectedConnection(), query, explained);
    }

    public void executeSQLAtCursor() {

        preExecute();
        String query = getQueryAtCursor().getQuery();

        if (StringUtils.isNotBlank(query)) {
            editorPanel.setExecutingQuery(query);
            delegate.executeQuery(query, false, true);
        }
    }

    public QueryWithPosition getQueryAtCursor() {
        return editorPanel.getQueryAtCursor();
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
            statusBar.setStatus(" File saved to " + scriptFile.getFileName());

            isContentChanged = false;
        }
        if (!scriptFile.getAbsolutePath().contentEquals(oldAbsolutePath)) {
            String connectionID = (getSelectedConnection() != null) ?
                    getSelectedConnection().getId() : QueryEditorHistory.NULL_CONNECTION;
            QueryEditorHistory.removeEditor(connectionID, oldAbsolutePath);
            QueryEditorHistory.addEditor(connectionID, getAbsolutePath(), -1);
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

        this.isContentChanged = contentChanged;
        if (this.isContentChanged) {
            save(false);
            this.isContentChanged = true;
        }
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

            if (connectionsCombo.getModel().getSize() == 0)
                connectionsCombo.addElement(null);

            connectionsCombo.addElement(connectionEvent.getDatabaseConnection());
            DatabaseConnection databaseConnection = connectionEvent.getDatabaseConnection();

            if (databaseConnection == selectConnection) {
                connectionsCombo.getModel().setSelectedItem(databaseConnection);
                selectConnection = null;
            }
        }
    }

    @Override
    public void disconnected(ConnectionEvent connectionEvent) {
        if (!isQueryEditorClosed) {
            connectionsCombo.removeElement(connectionEvent.getDatabaseConnection());
            // TODO: NEED TO CHECK OPEN CONN
        }
    }

    // ---------------------------------------------

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent)
                || (event instanceof KeywordEvent)
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

    public void refreshAutocompleteList() {
        editorPanel.getQueryArea().resetAutocomplete();
    }

    public void allResultTabsClosed() {
        lastDividerLocation = splitPane.getDividerLocation();
        baseEditorPanel.setVisible(true);
        resultsBasePanel.setVisible(false);
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

    private String bundleString(String key) {
        return Bundles.get(this.getClass(), key);
    }

}
