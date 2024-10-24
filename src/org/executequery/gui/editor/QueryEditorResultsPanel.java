/*
 * QueryEditorResultsPanel.java
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
import org.executequery.UserPreferencesManager;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.gui.IconManager;
import org.executequery.gui.LoggingOutputPanel;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.localization.Bundles;
import org.executequery.sql.SqlMessages;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.SimpleCloseTabbedPane;
import org.underworldlabs.swing.plaf.TabRollOverListener;
import org.underworldlabs.swing.plaf.TabRolloverEvent;
import org.underworldlabs.swing.plaf.TabSelectionListener;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The Query Editor's results panel.
 *
 * @author Takis Diakoumis
 */
public class QueryEditorResultsPanel extends SimpleCloseTabbedPane
        implements TabRollOverListener,
        TabSelectionListener,
        ResultSetTableContainer,
        ChangeListener {

    private static final String OUTPUT_TAB_TITLE = bundleString("title");

    /**
     * the editor parent
     */
    private QueryEditor queryEditor;

    /**
     * the text output message pane
     */
    private LoggingOutputPanel outputTextPane;

    /**
     * the result set tab count
     */
    private int resultSetTabTitleCounter;

    /**
     * the result tab icon
     */
    private Icon resultSetTabIcon;

    /**
     * the text output tab icon
     */
    private Icon outputTabIcon;

    private static final String SUCCESS = bundleString("SUCCESS");
    private static final String NO_ROWS = bundleString("NO_ROWS");
    private static final String SUCCESS_NO_ROWS = SUCCESS + "\n" + NO_ROWS;

    private static final Icon WARNING_ICON = IconManager.getIcon(
            "icon_warning_animated",
            "gif",
            20,
            IconManager.IconFolder.BASE
    );

    private ResultSetTableColumnResizingManager resultSetTableColumnResizingManager;

    public QueryEditorResultsPanel(QueryEditor queryEditor) {

        this(queryEditor, null);
    }

    public QueryEditorResultsPanel() {

        this(null, null);
    }

    public QueryEditorResultsPanel(QueryEditor queryEditor, ResultSet rs) {

        super(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);

        this.queryEditor = queryEditor;

        setTabPopupEnabled(true);

        if (queryEditor != null) {
            addTabRollOverListener(this);
            addTabSelectionListener(this);
        }

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void init() {

        outputTextPane = new LoggingOutputPanel();

        outputTabIcon = IconManager.getIcon("icon_console");
        resultSetTabIcon = IconManager.getIcon("icon_frame");

        addTextOutputTab();

        if (queryEditor == null) { // editor calls this also

            setTableProperties();
        }

        resultSetTableColumnResizingManager = new ResultSetTableColumnResizingManager();
        addChangeListener(this);
    }

    private TransposedRowTableModelBuilder transposedRowTableModelBuilder;

    private TransposedRowTableModelBuilder transposedRowTableModelBuilder() {

        if (transposedRowTableModelBuilder == null) {

            transposedRowTableModelBuilder = new DefaultTransposedRowTableModelBuilder();
        }

        return transposedRowTableModelBuilder;
    }

    public void transposeRow(TableModel tableModel, int row) {

        if (!(tableModel instanceof ResultSetTableModel)) {

            throw new IllegalArgumentException("Table model must of type ResultSetTableModel.");
        }

        ResultSetTableModel resultSetTableModel = (ResultSetTableModel) tableModel;
        ResultSetTableModel model = transposedRowTableModelBuilder().transpose(resultSetTableModel, row);

        TransposedRowResultSetPanel resultSetPanel = new TransposedRowResultSetPanel(this, model);
        addResultSetPanel(queryForModel(tableModel), model.getRowCount(), resultSetPanel, null);
    }

    public void filter(String pattern) {

        ResultSetPanel selectedResultSetPanel = getSelectedResultSetPanel();
        if (selectedResultSetPanel == null || selectedResultSetPanel.getRowCount() == 0) {

            return;
        }

        List<List<RecordDataItem>> list = selectedResultSetPanel.filter(pattern);
        ResultSetPanel resultSetPanel = createResultSetPanel();
        ResultSetTableModel model = new ResultSetTableModel(selectedResultSetPanel.getResultSetTableModel().getColumnNames(), list);

        resultSetPanel.setResultSet(model, false);
        addResultSetPanel(selectedResultSetPanel.getResultSetTableModel().getQuery(), model.getRowCount(), resultSetPanel, true, null);
    }

    private String queryForModel(TableModel tableModel) {

        for (int i = 0, n = getTabCount(); i < n; i++) {

            Component c = getTabComponentAt(i);
            if (c instanceof ResultSetPanel) {

                ResultSetPanel panel = (ResultSetPanel) c;
                if (panel.getResultSetTableModel() == tableModel) {

                    return getToolTipTextAt(i);
                }

            }

        }
        return "";
    }

    protected void removePopupComponent(JComponent component) {

        if (queryEditor != null) {

            queryEditor.removePopupComponent(component);
        }
    }

    protected void addPopupComponent(JComponent component) {

        if (queryEditor != null) {

            queryEditor.addPopupComponent(component);
        }
    }

    public void cleanup() {
        try {
            destroyTable();
        } finally {
            queryEditor = null;
        }
    }

    private void addTextOutputTab() {

        if (indexOfTab(OUTPUT_TAB_TITLE) == -1) {

            insertTab(OUTPUT_TAB_TITLE, outputTabIcon, outputTextPane, bundleString("DatabaseOutput"), 0);
        }

    }

    private void addTextOutputTab(DatabaseConnection databaseConnection) {
        if (databaseConnection == null)
            addTextOutputTab();
        else {
            if (indexOfTab(databaseConnection.getName()) == -1) {

                insertTab(databaseConnection.getName(), outputTabIcon, new LoggingOutputPanel(), databaseConnection.getName(), 0);
            }
        }

    }

    /**
     * Sets the user defined (preferences) table properties.
     */
    public void setTableProperties() {

        Component[] tabs = getComponents();
        for (int i = 0; i < tabs.length; i++) {

            Component c = tabs[i];
            if (c instanceof ResultSetPanel) {

                ResultSetPanel panel = (ResultSetPanel) c;
                panel.setTableProperties();
            }

        }

    }

    public int getResultSetTabCount() {

        int count = 0;
        Component[] components = getComponents();
        for (Component component : components) {

            if (component instanceof ResultSetPanel) {

                count++;
            }

        }

        return count;
    }

    public void removeAll() {

        super.removeAll();
        setVisible(true);
    }

    public void remove(int index) {

        super.remove(index);
        setVisible(true);
    }

    public boolean hasOutputPane() {

        return getResultSetTabCount() == (getTabCount() - 1);
    }

    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e - the event object
     */
    @Override
    public void stateChanged(ChangeEvent e) {

        Component selectedComponent = getSelectedComponent();
        if (selectedComponent instanceof ResultSetPanel) {

            int rowCount = ((ResultSetPanel) selectedComponent).getRowCount();
            resetEditorRowCount(rowCount);
        }

        if (hasNoTabs() && queryEditor != null)
            queryEditor.allResultTabsClosed();
    }

    /**
     * Indicates whether the current model displayed has
     * retained the ResultSetMetaData.
     *
     * @return true | false
     */
    public boolean hasResultSetMetaData() {
        ResultSetPanel panel = getSelectedResultSetPanel();
        if (panel != null) {
            return panel.hasResultSetMetaData();
        }
        return false;
    }

    public void interrupt() {
        Component[] tabs = getComponents();
        for (int i = 0; i < tabs.length; i++) {
            Component c = tabs[i];
            if (c instanceof ResultSetPanel) {
                ResultSetPanel panel = (ResultSetPanel) c;
                panel.interrupt();
            }
        }
    }

    /**
     * Sets the result set object.
     *
     * @param rset          - the executed result set
     * @param showRowNumber - whether to return the result set row count
     * @param maxRecords    - the maximum records to return
     */
    public int setResultSet(ResultSet rset, boolean showRowNumber, int maxRecords, DatabaseConnection dc) throws SQLException {

        return setResultSet(rset, showRowNumber, maxRecords, null, dc);
    }

    /**
     * Sets the result set object.
     *
     * @param rset          - the executed result set
     * @param showRowNumber - whether to return the result set row count
     * @param maxRecords    - the maximum records to return
     * @param query         - the executed query of the result set
     */
    public synchronized int setResultSet(ResultSet rset, boolean showRowNumber, int maxRecords, String query, DatabaseConnection dc) throws SQLException {

        ResultSetTableModel model = new ResultSetTableModel(rset, maxRecords, query, false);
        if (model.getException() != null) {
            setOutputMessage(dc,
                    SqlMessages.ERROR_MESSAGE, model.getException().getMessage(), true);
        }

        int rowCount = getResultSetRowCount(model, showRowNumber);
        if (rowCount == 0) {

            return rowCount;
        }

        if (rowCount == 1 && transposeSingleRowResultSets()) {

            transposeRow(model, 0);

        } else {

            ResultSetPanel panel = createResultSetPanel();
            ResultSetTable table = panel.getTable();
            try {

                resultSetTableColumnResizingManager.suspend(table);

                panel.setResultSet(model, showRowNumber);
                /*double thisWidth = getParent().getParent().getSize().getWidth();
                int colWidth = SystemProperties.getIntProperty("user", "results.table.column.width");
                if (thisWidth / table.getColumnCount() < colWidth)
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                else table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);*/
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                table.setTableColumnWidthFromContents();
                resultSetTableColumnResizingManager.setColumnWidthsForTable(table);

            } finally {

                resultSetTableColumnResizingManager.reinstate(table);
            }
            addResultSetPanel(query, rowCount, panel, dc);

        }

        return rowCount;
    }

    private ResultSetPanel createResultSetPanel() {

        ResultSetPanel panel = new ResultSetPanel(this);
        resultSetTableColumnResizingManager.manageResultSetTable(panel.getTable());

        return panel;
    }

    private void resetTabCount() {

        int tabCount = getTabCount();
        if (tabCount == 0 || (tabCount == 1 && hasOutputPane())) {

            resultSetTabTitleCounter = 0;
        }

        resultSetTabTitleCounter++;
    }

    private void addResultSetPanel(String query, int rowCount, final ResultSetPanel panel, DatabaseConnection dc) {

        addResultSetPanel(query, rowCount, panel, false, dc);
    }

    private void addResultSetPanel(String query, int rowCount, final ResultSetPanel panel, boolean filtered, DatabaseConnection dc) {

        resetTabCount();

        String title = bundleString("ResultSetTitle") + resultSetTabTitleCounter
                + (filtered ? " " + bundleString("ResultSetTitleFiltered") : "") + (dc == null ? "" : ":" + dc);

        if (useSingleResultSetTabs()) {

            if (getResultSetTabCount() >= 1) {

                closeResultSetTabs();
            }
        }

        addTab(title, resulSetTabIcon(), panel, query);

        if (queryEditor != null) {

            queryEditor.setMetaDataButtonEnabled(true);
            resetEditorRowCount(rowCount);
            queryEditor.setExportButtonEnabled(true);
        }

        GUIUtils.invokeLater(new Runnable() {
            public void run() {
                setSelectedComponent(panel);
            }
        });

    }

    private void closeResultSetTabs() {

        Component[] components = getComponents();
        for (Component component : components) {

            if (component instanceof ResultSetPanel) {

                remove(component);
            }

        }

    }

    private boolean useSingleResultSetTabs() {

        return UserPreferencesManager.isResultSetTabSingle();
    }

    private boolean transposeSingleRowResultSets() {

        return UserPreferencesManager.isTransposingSingleRowResultSets();
    }

    private Icon resulSetTabIcon() {

        return resultSetTabIcon;
    }

    /**
     * Sets the returned rows status text using the specified row count.
     *
     * @param rowCount - the result set row count
     */
    private void resetEditorRowCount(int rowCount) {

        if (queryEditor == null)
            return;

        String text = bundleString("ZERO_ROWS");
        if (rowCount > 1) {
            text = bundleString("ROWS_RETURNED", rowCount);

        } else if (rowCount == 1)
            text = bundleString("ROW_RETURNED", rowCount);

        boolean isRowsCountLimited = SystemProperties.getBooleanProperty("user", "editor.limit.records.count");
        if (isRowsCountLimited) {

            int limit = SystemProperties.getIntProperty("user", "editor.max.records.count");
            if (limit <= rowCount) {
                queryEditor.setLeftStatus(text, bundleString("LIMIT_ON", limit), WARNING_ICON);
                return;
            }
        }

        queryEditor.setLeftStatusText(text);
    }

    private int getResultSetRowCount(ResultSetTableModel model, boolean showRowNumber) {
        int rowCount = model.getRowCount();
        if (rowCount == 0) {
            if (showRowNumber) {
                setOutputMessage(null, SqlMessages.PLAIN_MESSAGE, SUCCESS_NO_ROWS.trim(), true);
                resetEditorRowCount(rowCount);
                queryEditor.setMetaDataButtonEnabled(false);
            }
        }
        return rowCount;
    }

    public void setResultText(DatabaseConnection dc, int result, int type, String metaName) {

        if (hasNoTabs()) {

            addTextOutputTab();
        }

        setSelectedIndex(0);


        setOutputMessage(dc, SqlMessages.PLAIN_MESSAGE, QueryTypes.getResultText(result, type, metaName, ""), true);
        queryEditor.setLeftStatusText(SUCCESS);
    }

    private boolean hasNoTabs() {
        return getTabCount() == 0;
    }

    private boolean hasOutputTab() {

        if (hasNoTabs())
            return false;

        for (int i =0; i < getTabCount(); i++)
            if (getTitleAt(0).equals(OUTPUT_TAB_TITLE))
                return true;

        return false;
    }

    public void setResultBackground(Color colour) {

        outputTextPane.setBackground(colour);

        Component[] tabs = getComponents();
        for (int i = 0; i < tabs.length; i++) {
            Component c = tabs[i];
            if (c instanceof ResultSetPanel) {
                ResultSetPanel panel = (ResultSetPanel) c;
                panel.setResultBackground(colour);
            }
        }
    }

    public void destroyTable() {
        Component[] tabs = getComponents();
        for (int i = 0; i < tabs.length; i++) {
            Component c = tabs[i];
            if (c instanceof ResultSetPanel) {
                ResultSetPanel panel = (ResultSetPanel) c;
                panel.destroyTable();
            }
        }
    }

    private ResultSetPanel getSelectedResultSetPanel() {

        int selectedIndex = getSelectedIndex();
        if (selectedIndex <= 0) {

            return null;
        }

        Component c = getComponentAt(selectedIndex);
        if (c instanceof ResultSetPanel) {

            return (ResultSetPanel) c;
        }

        return null;
    }

    /**
     * Returns whether a result set panel is selected and that
     * that panel has a result set row count > 0.
     *
     * @return true | false
     */
    public boolean isResultSetSelected() {
        ResultSetPanel panel = getSelectedResultSetPanel();
        if (panel != null) {
            return panel.getRowCount() > 0;
        }
        return false;
    }

    public ResultSetTable getResultSetTable() {
        ResultSetPanel panel = getSelectedResultSetPanel();
        if (panel != null) {
            return panel.getTable();
        }
        return null;
    }

    public ResultSetTableModel getResultSetTableModel() {
        ResultSetPanel panel = getSelectedResultSetPanel();
        if (panel != null) {
            return panel.getResultSetTableModel();
        }
        return null;
    }

    public void setWarningMessage(String s) {
        appendOutput(SqlMessages.WARNING_MESSAGE, s);
    }

    public void setPlainMessage(String s) {
        appendOutput(SqlMessages.PLAIN_MESSAGE, s);
    }

    public void setActionMessage(String s) {
        appendOutput(SqlMessages.ACTION_MESSAGE, s);
    }

    public void setErrorMessage(String s) {
        if (hasNoTabs()) {
            addTextOutputTab();
        }

        setSelectedIndex(0);
        if (!MiscUtils.isNull(s)) {
            appendOutput(SqlMessages.ERROR_MESSAGE, s);
        }
        if (queryEditor != null) {
            queryEditor.setExportButtonEnabled(false);
            queryEditor.setMetaDataButtonEnabled(false);
        }
    }

    public void setOutputMessage(DatabaseConnection dc, int type, String text) {
        setOutputMessage(dc, type, text, true);
    }

    public void setOutputMessage(DatabaseConnection dc, int type, String text, boolean selectTab) {

        if (hasNoTabs()) {
            addTextOutputTab(dc);
        }

        if (selectTab) {
            if (dc == null)
                setSelectedIndex(0);
            else {
                if (indexOfTab(dc.getName()) < 0)
                    addTextOutputTab(dc);
                setSelectedIndex(indexOfTab(dc.getName()));
            }
        }

        if (StringUtils.isNotBlank(text)) {
            if (dc == null)
                appendOutput(type, text);
            else {
                appendOutput(dc, type, text);
            }
        }

        if (queryEditor != null) {

            if (!isResultSetSelected()) {
                queryEditor.setExportButtonEnabled(false);
                queryEditor.setMetaDataButtonEnabled(false);
            }

        }
    }

    protected void appendOutput(int type, String text) {

        if (!hasOutputTab())
            addTextOutputTab();

        outputTextPane.append(type, text);
    }

    protected void appendOutput(DatabaseConnection dc, int type, String text) {
        int index = indexOfTab(dc.getName());
        if (index < 0) {
            addTextOutputTab(dc);
            index = indexOfTab(dc.getName());
        }
        ((LoggingOutputPanel) getComponentAt(index)).append(type, text);
    }

    public void clearOutputPane() {
        outputTextPane.clear();
    }

    /**
     * Indicates the current execute has completed to
     * clear the temp panel availability cache.
     */
    public void finished() {
    }

    private boolean panelHasResultSetMetaData(ResultSetPanel panel) {
        return panel != null && panel.hasResultSetMetaData();
    }

    /**
     * Sets to display the result set meta data for the
     * currently selected result set tab.
     */
    public void displayResultSetMetaData() {

        ResultSetPanel panel = getSelectedResultSetPanel();
        if (panelHasResultSetMetaData(panel)) {

            int index = getSelectedIndex();
            ResultSetMetaDataPanel metaDataPanel = panel.getResultSetMetaDataPanel();

            // check if the meta data is already displayed
            // at the index next to the result panel
            if (index != getTabCount() - 1) {

                Component c = getComponentAt(index + 1);
                if (c == metaDataPanel) {

                    setSelectedIndex(index + 1);
                    return;
                }
            }

            // otherwise add it
            insertTab(ResultSetMetaDataPanel.TITLE,
                    IconManager.getIcon("icon_rs_metadata"),
                    metaDataPanel,
                    getToolTipTextAt(index),
                    index + 1);
            setSelectedIndex(index + 1);
        }
    }

    /**
     * Indicates a query is about to be executed
     */
    public void preExecute() {

        addTextOutputTab();
        setSelectedIndex(indexOfTab(OUTPUT_TAB_TITLE));
    }

    public void preExecute(DatabaseConnection databaseConnection) {

        addTextOutputTab(databaseConnection);
        setSelectedIndex(indexOfTab(databaseConnection.getName()));
    }

    /**
     * Moves the caret to the beginning of the specified query.
     *
     * @param query - the query to move the cursor to
     */
    public void caretToQuery(String query) {
        queryEditor.caretToQuery(query);
    }

    /**
     * the query display popup
     */
    private static QueryTextPopup queryPopup;

    /**
     * last popup rollover index
     */
    private int lastRolloverIndex = -1;

    /**
     * Returns the result set's query at the specified index.
     *
     * @param index - the result set index
     * @return the query string
     */
    public String getQueryTextAt(int index) {
        return getToolTipTextAt(index);
    }

    private boolean isQueryPopupVisible() {

        return (queryPopup != null && queryPopup.isVisible());
    }

    public void tabSelected(MouseEvent e) {

        if (e.getClickCount() >= 2) {

            queryEditor.toggleResultPane();
        }

    }


    /**
     * Reacts to a tab rollover.
     *
     * @param e associated event
     */
    public void tabRollOver(TabRolloverEvent e) {

        int index = e.getIndex();

        // check if we're over the output panel (index 0)
        if (index == 0 && hasOutputPane()) {

            lastRolloverIndex = index;
            if (isQueryPopupVisible()) {

                queryPopup.dispose();
            }

            return;
        }

        if (isQueryPopupVisible() && lastRolloverIndex == index) {

            return;
        }

        if (index != -1) {
            String query = getToolTipTextAt(index);
            if (!MiscUtils.isNull(query)) {
                if (queryPopup == null) {
                    queryPopup = new QueryTextPopup(this);
                }
                lastRolloverIndex = index;
                queryPopup.showPopup(e.getX(), e.getY(), query, getTitleAt(index), index);
            }
        }
    }

    /**
     * Reacts to a tab rollover finishing.
     *
     * @param e associated event
     */
    public void tabRollOverFinished(TabRolloverEvent e) {
        int index = e.getIndex();
        if (index == -1) {
            lastRolloverIndex = index;
            if (queryPopup != null) {
                queryPopup.dispose();
            }
        }
    }

    /**
     * Reacts to a tab rollover finishing.
     *
     * @param e associated event
     */
    public void tabRollOverCancelled(TabRolloverEvent e) {
        if (queryPopup != null) {
            queryPopup.forceDispose();
        }
    }

    public boolean isTransposeAvailable() {
        return true;
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(QueryEditorResultsPanel.class, key, args);
    }

}
