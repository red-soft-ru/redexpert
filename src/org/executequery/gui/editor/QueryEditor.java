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
import org.executequery.gui.FocusablePanel;
import org.executequery.gui.SaveFunction;
import org.executequery.gui.editor.autocomplete.QueryEditorAutoCompletePopupProvider;
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
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.Printable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * The Query Editor.
 *
 * @author Takis Diakoumis
 */
public class QueryEditor extends DefaultTabView
        implements ConnectionListener,
        QueryBookmarkListener,
        QueryShortcutListener,
        UserPreferenceListener,
        TextEditor,
        KeywordListener,
        FocusablePanel

{

    public static final String TITLE = Bundles.get(QueryEditor.class, "title");
    public static final String FRAME_ICON = "Edit16.png";

    private static final String DEFAULT_SCRIPT_PREFIX = Bundles.get(QueryEditor.class, "script");

    private static final String DEFAULT_SCRIPT_SUFFIX = ".sql";


    /**
     * editor open count for title numbering
     */
    private static int editorCountSequence = 1;

    /**
     * The editor's status bar
     */
    private QueryEditorStatusBar statusBar;

    /**
     * The editor's text pan panel
     */
    private QueryEditorTextPanel editorPanel;

    /**
     * The editor's results panel
     */
    private QueryEditorResultsPanel resultsPanel;

    private ScriptFile scriptFile;

    /**
     * flags the content as having being changed
     */
    private boolean contentChanged;

    /**
     * the editor's tool bar
     */
    private QueryEditorToolBar toolBar;

    /**
     * the active connections combo
     */
    private OpenConnectionsComboBox connectionsCombo;

    /**
     * The text pane's popup menu
     */
    private QueryEditorPopupMenu popup;

    /**
     * enable/disable max rows
     */
    private JCheckBox maxRowCountCheckBox;

    /**
     * the max row count returned field
     */
    private JCheckBox stopOnErrorCheckBox;

    /**
     * the max row count returned field
     */
    private NumberTextField maxRowCountField;

    /**
     * the result pane base panel
     */
    private JPanel resultsBase;

    /**
     * the editor split pane
     */
    private JSplitPane splitPane;

    /**
     * sql query execution delegate
     */
    private QueryEditorDelegate delegate;

    private TokenizingFormatter formatter;

    private JPanel toolsPanel;

    private List<ConnectionChangeListener> connectionChangeListeners;

    private TransactionIsolationCombobox txBox;

    /**
     * Constructs a new instance.
     */
    public QueryEditor() {

        this(null, null);
    }

    /**
     * Creates a new query editor with the specified text content.
     *
     * @param the text content to be set
     */
    public QueryEditor(String text) {

        this(text, null);
    }

    DatabaseConnection oldConnection;
    private int number = -1;

    /**
     * Creates a new query editor with the specified text content
     * and the specified absolute file path.
     *
     * @param the text content to be set
     * @param the absolute file path;
     */
    public QueryEditor(String text, String absolutePath) {

        super(new GridBagLayout());

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }
        scriptFile = new ScriptFile();
        scriptFile.setFileName(defaultScriptName());
        if (absolutePath == null) {
            QueryEditorHistory.checkAndCreateDir();
            absolutePath = QueryEditorHistory.editorDirectory() + scriptFile.getFileName();
        }
        String connectionID;
        if (getSelectedConnection() != null)
            connectionID = getSelectedConnection().getId();
        else connectionID = QueryEditorHistory.NULL_CONNECTION;
        QueryEditorHistory.addEditor(connectionID, absolutePath, number);
        scriptFile.setAbsolutePath(absolutePath);

        contentChanged = false;

        formatter = new TokenizingFormatter();
        if (getSelectedConnection() != null)
            ((QueryEditorTextPane) editorPanel.getQueryArea()).setDBObjects(getSelectedConnection().getListObjectsDB());
        if (text != null) {
            loadText(text);
        }
    }

    private String defaultScriptName() {
        number = QueryEditorHistory.getMinNumber();
        return DEFAULT_SCRIPT_PREFIX
                + (number) + DEFAULT_SCRIPT_SUFFIX;
    }

    private void init() {

        // construct the two query text area and results panels
        statusBar = new QueryEditorStatusBar();
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, -1, -2, -2));

        editorPanel = new QueryEditorTextPanel(this);
        resultsPanel = new QueryEditorResultsPanel(this);

        delegate = new QueryEditorDelegate(this);

        popup = new QueryEditorPopupMenu(delegate);
        editorPanel.addEditorPaneMouseListener(popup);

        baseEditorPanel = new JPanel(new BorderLayout());
        baseEditorPanel.add(editorPanel, BorderLayout.CENTER);
        baseEditorPanel.add(statusBar, BorderLayout.SOUTH);
        baseEditorPanel.setBorder(BorderFactory.createMatteBorder(
                1, 1, 1, 1, GUIUtilities.getDefaultBorderColour()));

        // add to a base panel - when last tab closed visible set
        // to false on the tab pane and split collapses - want to avoid this
        resultsBase = new JPanel(new BorderLayout());
        resultsBase.add(resultsPanel, BorderLayout.CENTER);

        if (new SplitPaneFactory().usesCustomSplitPane()) {

            splitPane = new EditorSplitPane(JSplitPane.VERTICAL_SPLIT,
                    baseEditorPanel, resultsBase);
        } else {
            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    baseEditorPanel, resultsBase);
        }

        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(0.5);

        // ---------------------------------------
        // the tool bar and conn combo
        toolBar = new QueryEditorToolBar(
                editorPanel.getTextPaneActionMap(), editorPanel.getTextPaneInputMap());

        Vector<DatabaseConnection> connections =
                ConnectionManager.getActiveConnections();
        connectionsCombo = new OpenConnectionsComboBox(this, connections);
        connectionsCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String idConnection = null;
                    if (oldConnection == null)
                        idConnection = QueryEditorHistory.NULL_CONNECTION;
                    else idConnection = oldConnection.getId();
                    QueryEditorHistory.changedConnectionEditor(idConnection, getSelectedConnection().getId(), scriptFile.getAbsolutePath());
                    oldConnection = getSelectedConnection();
                }
            }
        });
                /*(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (oldConnection != null)
                    QueryEditorHistory.changedConnectionEditor(oldConnection.getId(), getSelectedConnection().getId(), scriptFile.getAbsolutePath());
                oldConnection = getSelectedConnection();
            }
        });*/
        oldConnection = (DatabaseConnection) connectionsCombo.getSelectedItem();

        txBox = new TransactionIsolationCombobox();
        txBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                delegate.setTransactionIsolation(txBox.getSelectedLevel());
            }
        });

        maxRowCountCheckBox = new JCheckBox(bundleString("MaxRows"));
        maxRowCountCheckBox.setToolTipText(bundleString("MaxRows.tool-tip"));
        maxRowCountCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                maxRowCountCheckBoxSelected();
            }
        });
        stopOnErrorCheckBox = new JCheckBox(bundleString("StopOnError"));
        stopOnErrorCheckBox.setToolTipText(bundleString("StopOnError.tool-tip"));
        stopOnErrorCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                SystemProperties.setBooleanProperty("user", "editor.stop.on.error", stopOnErrorCheckBox.isSelected());
            }
        });

        maxRowCountField = new MaxRowCountField(this);

        toolsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy++;
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        toolsPanel.add(toolBar, gbc);
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 7;
        gbc.insets.left = 5;
        gbc.insets.right = 10;
        toolsPanel.add(createLabel(Bundles.getCommon("connection"), 'C'), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.insets.top = 2;
        gbc.insets.bottom = 2;
        gbc.insets.left = 0;
        gbc.insets.right = 0;
        toolsPanel.add(connectionsCombo, gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 7;
        gbc.insets.left = 5;
        gbc.insets.right = 10;
        toolsPanel.add(createLabel(bundleString("TransactionIsolationLevel"), 'T'), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.insets.top = 2;
        gbc.insets.bottom = 2;
        gbc.insets.left = 0;
        gbc.insets.right = 0;
        toolsPanel.add(txBox, gbc);

        gbc.gridx++;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        gbc.insets.top = 7;
        gbc.insets.right = 10;
        gbc.insets.left = 10;
        toolsPanel.add(createLabel(bundleString("Filter"), 'l'), gbc);
        gbc.gridx++;
        gbc.weightx = 0.8;
        gbc.insets.top = 2;
        gbc.insets.bottom = 2;
        gbc.insets.right = 2;
        gbc.insets.left = 0;
        gbc.fill = GridBagConstraints.BOTH;
        toolsPanel.add(createResultSetFilterTextField(), gbc);

        gbc.gridx++;
        gbc.weightx = 0;
        gbc.insets.top = 5;
        gbc.insets.left = 10;
        toolsPanel.add(maxRowCountCheckBox, gbc);
        gbc.gridx++;
        gbc.insets.left = 0;
        gbc.insets.top = 7;
        gbc.insets.right = 10;
        //.add(createLabel("Max Rows:", 'R'), gbc);
        gbc.gridx++;
        gbc.weightx = 0.3;
        gbc.insets.top = 2;
        gbc.insets.bottom = 2;
        gbc.insets.right = 2;
        gbc.fill = GridBagConstraints.BOTH;
        toolsPanel.add(maxRowCountField, gbc);

        gbc.gridx++;
        gbc.weightx = 0;
        gbc.insets.top = 5;
        gbc.insets.left = 10;
        toolsPanel.add(stopOnErrorCheckBox, gbc);

        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));

        JPanel base = new JPanel(new BorderLayout());
        base.add(toolsPanel, BorderLayout.NORTH);
        base.add(splitPane, BorderLayout.CENTER);

        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets.top = 0;
        gbc.insets.bottom = 0;
        gbc.insets.left = 0;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        add(base, gbc);

        // register for connection and keyword events
        EventMediator.registerListener(this);

        addDeleteLineActionMapping();

        setEditorPreferences();

        statusBar.setCaretPosition(1, 1);
        statusBar.setInsertionMode("INS");
    }

    private JTextField createResultSetFilterTextField() {

        filterTextField = new DefaultTextField();
        filterTextField.setFocusAccelerator('l');
        filterTextField.setToolTipText("Apply filter to current result set");

        filterTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                resultsPanel.filter(filterTextField.getText());
            }
        });

        return filterTextField;
    }

    private void maxRowCountCheckBoxSelected() {

        maxRowCountField.setEnabled(maxRowCountCheckBox.isSelected());
        maxRowCountField.requestFocus();
    }

    public void addConnectionChangeListener(
            final ConnectionChangeListener connectionChangeListener) {

        connectionsCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                connectionChangeListener.connectionChanged(getSelectedConnection());
            }
        });

        if (connectionChangeListeners == null) {

            connectionChangeListeners = new ArrayList<ConnectionChangeListener>();
        }

        connectionChangeListeners.add(connectionChangeListener);
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

            public void actionPerformed(ActionEvent e) {

                deleteLine();
            }
        };

        KeyStroke keyStroke = KeyStroke.getKeyStroke("control D");

        ActionMap textPaneActionMap = editorPanel.getTextPaneActionMap();
        InputMap textPaneInputMap = editorPanel.getTextPaneInputMap();

        String actionMapKey = "editor-delete-line";

        textPaneActionMap.put(actionMapKey, action);
        textPaneInputMap.put(keyStroke, actionMapKey);
    }

    private JLabel createLabel(String text, char mnemonic) {

        final JLabel label = new JLabel(text);
        label.setDisplayedMnemonic(mnemonic);

        return label;
    }

    /**
     * the last divider location before a output hide
     */
    private int lastDividerLocation;

    private QueryEditorAutoCompletePopupProvider queryEditorAutoCompletePopupProvider;
    private DatabaseConnection selectConnection;
    private JPanel baseEditorPanel;
    private JTextField filterTextField;

    /**
     * Toggles the output pane visible or not.
     */
    public void toggleOutputPaneVisible() {
        if (resultsBase.isVisible()) {
            lastDividerLocation = splitPane.getDividerLocation();
            resultsBase.setVisible(false);
        } else {
            resultsBase.setVisible(true);
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

        if (isAutoCompleteOn()) {

            queryEditorAutoCompletePopupProvider = new QueryEditorAutoCompletePopupProvider(this);
            editorPanel.registerAutoCompletePopup(queryEditorAutoCompletePopupProvider);

        } else {

            editorPanel.deregisterAutoCompletePopup();
        }

        int maxRecords = SystemProperties.getIntProperty("user", "editor.max.records");
        maxRowCountCheckBox.setSelected((maxRecords > 0));
        maxRowCountCheckBoxSelected();
        stopOnErrorCheckBox.setSelected(SystemProperties.getBooleanProperty("user", "editor.stop.on.error"));
    }

    private boolean isAutoCompleteOn() {

        UserProperties userProperties = userProperties();
        if (userProperties.containsKey("editor.autocomplete.on")
                && (!userProperties.containsKey("editor.autocomplete.keywords.on"))
                && !userProperties.containsKey("editor.autocomplete.schema.on")) {

            // old property key
            boolean allOn = true;
            if (!userProperties.getBooleanProperty("editor.autocomplete.on")) {

                allOn = false;
            }
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
     * to the user defined key words.
     */
    public void updateSQLKeywords() {

        editorPanel.setSQLKeywords(true);
    }

    /**
     * Notification of a new keyword added to the list.
     */
    public void keywordsAdded(KeywordEvent e) {

        editorPanel.setSQLKeywords(true);
    }

    /**
     * Notification of a keyword removed from the list.
     */
    public void keywordsRemoved(KeywordEvent e) {

        editorPanel.setSQLKeywords(true);
    }

    /**
     * Sets the activity status bar label text to
     * that specified.
     *
     * @param the activity label text
     */
    public void executing() {

        popup.statementExecuting();

        setStopButtonEnabled(true);

        statusBar.startProgressBar();
        statusBar.setExecutionTime(" Executing.. ");
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
        resetPanels();
    }

    /**
     * Sets the right status bar label text to
     * that specified.
     *
     * @param the right label text
     */
    public void commitModeChanged(boolean autoCommit) {

        statusBar.setCommitStatus(autoCommit);
    }

    /**
     * Sets the text of the left status label.
     *
     * @param the text to be set
     */
    public void setLeftStatusText(String s) {

        statusBar.setStatus(s);
    }

    /**
     * Propagates the call to interrupt an executing process.
     */
    public void interrupt() {

        resultsPanel.interrupt();
    }

    /**
     * Sets the result set object.
     *
     * @param the     executed result set
     * @param whether to return the result set row count
     */
    public int setResultSet(ResultSet rset, boolean showRowNumber) throws SQLException {

        return setResultSet(rset, showRowNumber, null);
    }

    /**
     * Returns the vakue from the max record count fields.
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
     * @param the     executed result set
     * @param whether to return the result set row count
     * @param the     executed query of the result set
     */
    public int setResultSet(ResultSet rset, boolean showRowNumber, String query) {

        int rowCount = resultsPanel.setResultSet(rset, showRowNumber, getMaxRecords());
        revalidate();
        return rowCount;
    }

    /**
     * Sets the result set object.
     *
     * @param rset the executed result set
     */
    public void setResultSet(ResultSet rset) {

        resultsPanel.setResultSet(rset, true, getMaxRecords());
        revalidate();
    }

    /**
     * Sets the result set object.
     *
     * @param the executed result set
     * @param the executed query of the result set
     */
    public void setResultSet(ResultSet rset, String query) {

        resultsPanel.setResultSet(rset, true, getMaxRecords(), query);
    }

    public void destroyTable() {

        resultsPanel.destroyTable();
    }

    /**
     * Sets to display the result set meta data for the
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
    public void disableUpdates(boolean disable) {

        editorPanel.disableUpdates(disable);
    }

    /**
     * Returns true that a search can be performed on the editor.
     */
    public boolean canSearch() {

        return true;
    }

    /**
     * Disables/enables the caret update as specified.
     */
    public void disableCaretUpdate(boolean disable) {

        editorPanel.disableCaretUpdate(disable);
    }

    public ResultSetTableModel getResultSetTableModel() {

        return resultsPanel.getResultSetTableModel();
    }

    public ResultSetTable getResultSetTable() {

        return resultsPanel.getResultSetTable();
    }

    public void setResultText(int updateCount, int type) {

        resultsPanel.setResultText(updateCount, type);
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
        editorPanel.setTextPaneBackground(
                userProperties().getColourProperty("editor.text.background.colour"));
        resultsPanel.setResultBackground(
                userProperties().getColourProperty("editor.results.background.colour"));
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
     * Enables/disables the show meta data button.
     */
    public void setMetaDataButtonEnabled(boolean enable) {

        if (retainMetaData()) {

            getTools().setMetaDataButtonEnabled(enable);

        } else {

            getTools().setMetaDataButtonEnabled(false);
        }
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
     * Enables/disables the transaction related buttons.
     */
    public void setCommitsEnabled(boolean enable) {

        getTools().setCommitsEnabled(enable);
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
    public void focusGained() {

        QueryEditorToolBar tools = getTools();

        tools.setMetaDataButtonEnabled(
                resultsPanel.hasResultSetMetaData() && retainMetaData());

        tools.setCommitsEnabled(!delegate.getCommitMode());
        tools.setNextButtonEnabled(delegate.hasNextStatement());
        tools.setPreviousButtonEnabled(delegate.hasPreviousStatement());
        tools.setStopButtonEnabled(delegate.isExecuting());
        tools.setExportButtonEnabled(resultsPanel.isResultSetSelected());

        editorPanel.setTextFocus();
    }

    public void focusLost() {
        // nothing to do here
    }

    private boolean retainMetaData() {

        return userProperties().getBooleanProperty("editor.results.metadata");
    }

    private QueryEditorToolBar getTools() {

        return toolBar;
    }

    public void destroyConnection() {

        delegate.destroyConnection();
        queryEditorAutoCompletePopupProvider.reset();
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
    public boolean tabViewClosing() {

        if (isExecuting() && oldConnection.isConnected()) {


            if (MiscUtils.isMinJavaVersion(1, 6)) {

                GUIUtilities.displayWarningMessage(
                        "Editor is currently executing.\nPlease wait until " +
                                "finished or attempt to cancel the running query.");
            }
            return false;
        }

        UserProperties properties = UserProperties.getInstance();
        boolean conFlag = oldConnection == null || oldConnection.isConnected();
        if (properties.getBooleanProperty("general.save.prompt") && contentChanged && conFlag) {

            String oldPath = getAbsolutePath();

            if (!GUIUtilities.saveOpenChanges(this)) {

                return false;
            } else {
                String newPath = getAbsolutePath();
                String connectionID;
                if (oldConnection != null)
                    connectionID = oldConnection.getId();
                else connectionID = QueryEditorHistory.NULL_CONNECTION;
                if (!oldPath.equals(newPath))
                    scriptFile.setAbsolutePath(oldPath);
                QueryEditorHistory.removeEditor(connectionID, getAbsolutePath());
                if (QueryEditorHistory.isDefaultEditorDirectory(this)) {
                    QueryEditorHistory.removeFile(oldPath);
                }
                scriptFile.setAbsolutePath(newPath);
                QueryEditorHistory.addEditor(connectionID, getAbsolutePath(), -1);
            }

        }

        try {

            cleanup();

        } catch (Exception e) {

            GUIUtilities.displayExceptionErrorDialog(
                    "An error occurred when closing this editor.\nWhile this could " +
                            "be nothing, sometimes it helps to check the stack trace to see if anything " +
                            "peculiar happened.\n\nThe system returned:\n" + e.getMessage(), e);
        }
        String connectionID;
        if (getSelectedConnection() != null)
            connectionID = getSelectedConnection().getId();
        else connectionID = QueryEditorHistory.NULL_CONNECTION;
        try {
            QueryEditorHistory.removeEditor(connectionID, scriptFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {

        focusGained();

        return true;
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    public boolean tabViewDeselected() {

        return true;
    }

    // --------------------------------------------

    /**
     * Indicates a connection has been closed.
     *
     * @param the encapsulating event
     */
    private boolean closed = false;

    public void interruptStatement() {

        if (Log.isDebugEnabled()) {

            Log.debug("Interrupt statement selected");
        }

        delegate.interrupt();
    }

    public void clearOutputPane() {

        if (!delegate.isExecuting()) {

            resultsPanel.clearOutputPane();
        }

    }

    public void selectAll() {

        editorPanel.selectAll();
    }

    public void goToRow(int row) {

        editorPanel.goToRow(row);
    }

    public void selectNone() {

        editorPanel.selectNone();
    }

    public Vector<String> getHistoryList() {

        return delegate.getHistoryList();
    }

    /**
     * Executes the currently selected query text.
     */
    public void executeSelection() {

        String query = editorPanel.getSelectedText();
        executeSQLQuery(query);

    }

    /**
     * Returns the currently selcted connection properties object.
     *
     * @return the selected connection
     */
    public DatabaseConnection getSelectedConnection() {
        if (connectionsCombo.getSelectedIndex() != -1) {

            return (DatabaseConnection) connectionsCombo.getSelectedItem();
        }
        return null;
    }

    public void setSelectedConnection(DatabaseConnection databaseConnection) {

        if (connectionsCombo.contains(databaseConnection)) {

            connectionsCombo.getModel().setSelectedItem(databaseConnection);

        } else {

            selectConnection = databaseConnection;
        }

    }

    public void preExecute() {

        filterTextField.setText("");
        resultsPanel.preExecute();
    }

    public String getWordToCursor() {

        return editorPanel.getWordToCursor();
    }

    public String getCompleteWordEndingAtCursor() {

        return editorPanel.getCompleteWordEndingAtCursor();
    }

    private boolean isExecuting() {

        return delegate.isExecuting();
    }

    public void executeAsBlock() {

        delegate.executeQuery(null, true);
    }

    /**
     * Executes the specified query.
     *
     * @param the query
     */
    public void executeSQLQuery(String query) {

        preExecute();

        if (query == null) {

            query = editorPanel.getQueryAreaText();
        }

        editorPanel.resetExecutingLine();
        boolean executeAsBlock = new SqlParser(query).isExecuteBlock();

        delegate.executeQuery(getSelectedConnection(), query, executeAsBlock);
    }

    public void executeSQLScript(String script) {

        preExecute();

        if (script == null) {

            script = editorPanel.getQueryAreaText();
        }

        editorPanel.resetExecutingLine();

        delegate.executeScript(getSelectedConnection(), script);
    }

    public void printExecutedPlan() {

        preExecute();

        String query = editorPanel.getQueryAreaText();

        editorPanel.resetExecutingLine();

        delegate.printExecutedPlan(getSelectedConnection(), query);
    }

    public void executeSQLAtCursor() {

        preExecute();
        String query = getQueryAtCursor().getQuery();
        if (StringUtils.isNotBlank(query)) {
            editorPanel.setExecutingQuery(query);
            delegate.executeQuery(query);
        }
    }

    public QueryWithPosition getQueryAtCursor() {

        return editorPanel.getQueryAtCursor();
    }

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
     * @param s - the text to be set
     */
    public void setEditorText(String s) {
        editorPanel.setQueryAreaText(s);
        save(false);
    }

    /**
     * Moves the caret to the beginning of the specified query.
     *
     * @param query - the query to move the cursor to
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

    public String getEditorText() {
        return editorPanel.getQueryAreaText();
    }

    public void setOutputMessage(int type, String text) {
        resultsPanel.setOutputMessage(type, text);
    }

    public void setOutputMessage(int type, String text, boolean selectTab) {
        resultsPanel.setOutputMessage(type, text, selectTab);
        //revalidate();
    }

    /**
     * Sets the state for an open file.
     *
     * @param the absolute file path
     */
    public void setOpenFilePath(String absolutePath) {
        scriptFile.setAbsolutePath(absolutePath);
    }

    /**
     * Returns whether the text component is in a printable state.
     *
     * @return true | false
     */
    public boolean canPrint() {
        return true;
    }

    public Printable getPrintable() {

        return getPrintableForResultSet();
    }

    public Printable getPrintableForResultSet() {

        return new TablePrinter(resultsPanel.getResultSetTable(),
                "Query: " + editorPanel.getQueryAreaText());
    }

    public Printable getPrintableForQueryArea() {

        return new TextPrinter(editorPanel.getQueryAreaText());
    }

    public String getPrintJobName() {
        return "RedXpert - editor";
    }

    // ---------------------------------------------
    // TextEditor implementation
    // ---------------------------------------------

    public void paste() {
        editorPanel.paste();
    }

    public void copy() {
        editorPanel.copy();
    }

    public void cut() {
        editorPanel.cut();
    }

    public void deleteLine() {
        editorPanel.deleteLine();
    }

    public void deleteWord() {
        editorPanel.deleteWord();
    }

    public void deleteSelection() {
        editorPanel.deleteSelection();
    }

    public void insertFromFile() {
        editorPanel.insertFromFile();
    }

    public void insertLineAfter() {
        editorPanel.insertLineAfter();
    }

    public void insertLineBefore() {
        editorPanel.insertLineBefore();
    }

    public void changeSelectionCase(boolean upper) {
        editorPanel.changeSelectionCase(upper);
    }

    public void changeSelectionToUnderscore() {
        editorPanel.changeSelectionToUnderscore();
    }

    public void changeSelectionToCamelCase() {
        editorPanel.changeSelectionToCamelCase();
    }

    // ---------------------------------------------

    // ---------------------------------------------
    // SaveFunction implementation
    // ---------------------------------------------

    public String getDisplayName() {
        return toString();
    }

    public boolean contentCanBeSaved() {
        return contentChanged;
    }

    public int save(boolean saveAs) {

        String text = editorPanel.getQueryAreaText();

        QueryEditorFileWriter writer = new QueryEditorFileWriter();

        boolean saved = writer.write(text, scriptFile, saveAs);

        if (saved) {

            GUIUtilities.setTabTitleForComponent(this, getDisplayName());
            statusBar.setStatus(" File saved to " + scriptFile.getFileName());

            contentChanged = false;
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
    public String toString() {

        return String.format("%s - %s", TITLE, scriptFile.getFileName());
    }

    /**
     * Returns whether the content has changed for a
     * possible document save.
     *
     * @return true if text content changed, false otherwise
     */
    public boolean isContentChanged() {
        return contentChanged;
    }

    /**
     * Sets that the text content of the editor has changed from
     * the original or previously saved state.
     *
     * @param true | false
     */
    public void setContentChanged(boolean contentChanged) {
        this.contentChanged = contentChanged;
        if (this.contentChanged == true) {
            save(false);
            this.contentChanged = true;
        }
    }

    // ---------------------------------------------
    // ConnectionListener implementation
    // ---------------------------------------------

    /**
     * Performs any resource clean up for a pending removal.
     */
    public void cleanup() {

        /* ------------------------------------------------
         * profiling found the popup keeps the
         * editor from being garbage collected at all!!
         * a call to removeAll() is a work around for now.
         * ------------------------------------------------
         */
        closed = true;
        popup.removeAll();

        editorPanel.closingEditor();
        resultsPanel.cleanup();
        statusBar.cleanup();

        resultsPanel = null;
        statusBar = null;
        toolBar = null;
        editorPanel = null;
        queryEditorAutoCompletePopupProvider = null;

        delegate.disconnected(getSelectedConnection());

        if (connectionChangeListeners != null) {

            for (ConnectionChangeListener listener : connectionChangeListeners) {

                listener.connectionChanged(null);
            }

        }

        removeAll();
        EventMediator.deregisterListener(this);
        GUIUtilities.registerUndoRedoComponent(null);
    }

    /**
     * Indicates a connection has been established.
     *
     * @param the encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {
        if (!closed) {
            if (connectionsCombo.getModel().getSize() == 0) {
                connectionsCombo.addElement(null);
            }
            connectionsCombo.addElement(connectionEvent.getDatabaseConnection());
            DatabaseConnection databaseConnection = connectionEvent.getDatabaseConnection();
            if (databaseConnection == selectConnection) {
                connectionsCombo.getModel().setSelectedItem(databaseConnection);
                selectConnection = null;
            }

        }
    }

    public void disconnected(ConnectionEvent connectionEvent) {
        if (!closed) {
            connectionsCombo.removeElement(connectionEvent.getDatabaseConnection());
            // TODO: NEED TO CHECK OPEN CONN
        }
    }

    // ---------------------------------------------

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent)
                || (event instanceof KeywordEvent)
                || (event instanceof UserPreferenceEvent)
                || (event instanceof QueryShortcutEvent)
                || (event instanceof QueryBookmarkEvent);
    }

    public void queryBookmarkAdded(QueryBookmarkEvent e) {
        handleBookmarkEvent(e);
    }

    public void queryBookmarkRemoved(QueryBookmarkEvent e) {
        handleBookmarkEvent(e);
    }

    private void handleBookmarkEvent(QueryBookmarkEvent e) {
        toolBar.reloadBookmarkItems();
    }

    public void formatSQLtext() {

        int start = editorPanel.getSelectionStart();
        int end = editorPanel.getSelectionEnd();

        String text = getSelectedText();
        if (text == null) {

            QueryWithPosition queryAtCursor = getQueryAtCursor();
            start = queryAtCursor.getStart();
            end = queryAtCursor.getEnd();
            text = getQueryAtCursor().getQuery();

        }

        String formattedText = formatter.format(text);
        editorPanel.replaceRegion(start, end, formattedText);
//        setEditorText(formattedText);
    }

    private String getSelectedText() {

        return editorPanel.getSelectedText();
    }

    public void preferencesChanged(UserPreferenceEvent event) {

        QueryEditorSettings.initialise();

        if (event.getEventType() == UserPreferenceEvent.QUERY_EDITOR
                || event.getEventType() == UserPreferenceEvent.ALL) {

            setEditorPreferences();
        }

    }

    public void queryShortcutAdded(QueryShortcutEvent e) {

        editorPanel.editorShortcutsUpdated();
    }

    public void queryShortcutRemoved(QueryShortcutEvent e) {

        editorPanel.editorShortcutsUpdated();
    }

    public void refreshAutocompleteList() {

        queryEditorAutoCompletePopupProvider.reset();
    }

    public void allResultTabsClosed() {

        lastDividerLocation = splitPane.getDividerLocation();

        baseEditorPanel.setVisible(true);
        resultsBase.setVisible(false);
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

    private void resetPanels() {

        resultsBase.setVisible(true);
        baseEditorPanel.setVisible(true);

        if (lastDividerLocation > 0) {

            splitPane.setDividerLocation(lastDividerLocation);

        } else {

            splitPane.setDividerLocation(0.5);
        }
    }

    private String bundleString(String key) {
        return Bundles.get(this.getClass(), key);
    }

}


