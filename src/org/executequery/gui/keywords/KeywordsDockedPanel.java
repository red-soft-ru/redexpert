/*
 * KeywordsDockedPanel.java
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

package org.executequery.gui.keywords;

import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.*;
import org.executequery.gui.AbstractDockedTabActionPanel;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.log.Log;
import org.executequery.repository.KeywordRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.toolbar.PanelToolBar;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.*;

/**
 * Docked keywords panel.
 *
 * @author Takis Diakoumis
 */
public class KeywordsDockedPanel extends AbstractDockedTabActionPanel
        implements KeyListener,
        MouseListener,
        ConnectionListener,
        KeywordListener {

    public static final String TITLE = "Keywords";

    /**
     * sql keywords
     */
    private List<SqlKeyword> keywords;

    /**
     * the table display
     */
    private JTable table;

    /**
     * the table model
     */
    private KeywordModel model;

    /**
     * the scroller
     */
    private JScrollPane scroller;

    /**
     * the search text field
     */
    private JTextField searchField;

    /**
     * Creates a new instance of KeywordsDockedPanel
     */
    public KeywordsDockedPanel() {

        super(new BorderLayout());

        init();
    }

    private void init() {
        Font font = new Font("Dialog", Font.PLAIN, Constants.DEFAULT_FONT_SIZE);

        // retrieve the keywords
        loadKeywords();

        // add any keywords from open connections
        Vector<DatabaseConnection> activeConns =
                ConnectionManager.getActiveConnections();
        if (activeConns != null && !activeConns.isEmpty()) {

            for (int i = 0, n = activeConns.size(); i < n; i++) {
                addDatabaseConnectionKewords(activeConns.get(i), false);
            }

        }

        // sort the keywords
        Collections.sort(keywords, new KeywordComparator());

        model = new KeywordModel();
        table = new DefaultTable(model);
        table.setTableHeader(null);
        table.setShowVerticalLines(false);
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setCellRenderer(new KeywordCellRenderer());
        table.setFont(font);
        table.setDragEnabled(true);
        table.addMouseListener(this);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ---------------------------------------
        // the tool bar
        PanelToolBar tools = new PanelToolBar();

        tools.addLabel("Find: ");
        searchField = WidgetFactory.createTextField("searchField");
        searchField.addActionListener(this);
        searchField.setActionCommand("search");
        tools.addTextField(searchField);
        tools.addButton(this, "search",
                GUIUtilities.getAbsoluteIconPath("Zoom16.svg"),
                "Search for a key word in the list");

        searchField.addKeyListener(this);

        scroller = new JScrollPane(table);

        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(tools, BorderLayout.NORTH);
        add(scroller, BorderLayout.CENTER);

        // register as a connection and keyword listener
        EventMediator.registerListener(this);
    }

    private void loadKeywords() {
        List<String> sql92 = keywords().getServerKeywords(0, 0, "");
        List<String> user = keywords().getUserDefinedSQL();

        int sql92Size = sql92.size();
        int userSize = user.size();

        // build the displayed list
        keywords = new ArrayList<SqlKeyword>(sql92Size + userSize);

        for (int i = 0; i < sql92Size; i++) {

            keywords.add(new SqlKeyword(sql92.get(i).trim(), true, false, false));
        }

        for (int i = 0; i < userSize; i++) {

            keywords.add(new SqlKeyword(user.get(i).trim(), false, false, true));
        }
    }

    private KeywordRepository keywords() {

        return (KeywordRepository) RepositoryCache.load(KeywordRepository.REPOSITORY_ID);
    }

    /**
     * Notification of a new keyword added to the list.
     */
    public void keywordsAdded(KeywordEvent e) {
        // reload the sql92 and user defined words
        loadKeywords();

        // reload all connected db words
        Vector<DatabaseConnection> conns = ConnectionManager.getActiveConnections();
        for (int i = 0, n = conns.size(); i < n; i++) {
            addDatabaseConnectionKewords(conns.get(i), false);
        }

        // resort and update the model
        Collections.sort(keywords, new KeywordComparator());
        model.fireTableDataChanged();
    }

    /**
     * Notification of a keyword removed from the list.
     */
    public void keywordsRemoved(KeywordEvent e) {
        keywordsAdded(e);
    }

    /**
     * Performs a keyword search based.
     */
    public void search() {

        String text = searchField.getText();
        if (MiscUtils.isNull(text)) {

            return;
        }

        text = text.toUpperCase();
        for (int i = 0, n = keywords.size(); i < n; i++) {

            if (keywords.get(i).getText().startsWith(text)) {

                // select the table row
                table.setRowSelectionInterval(i, i);

                // scroll the view
                Rectangle cell = table.getCellRect(i, 0, true);

                scroller.getViewport().
                        setViewPosition(new Point(cell.x, cell.y));

                break;
            }

        }

    }

    private void addDatabaseConnectionKewords(DatabaseConnection databaseConnection, boolean reset) {

        DatabaseHost databaseHost =
                new DatabaseObjectFactoryImpl().createDatabaseHost(databaseConnection);

        try {

            // retrieve db keywords
            String[] words = databaseHost.getDatabaseKeywords();

            // retrieve db product name
            String databaseProductName = databaseHost.getDatabaseProductNameVersion();

            // check existing words
            boolean exists = false;
            boolean wordsAdded = false;

            for (int i = 0; i < words.length; i++) {

                exists = false;
                String word = words[i].trim().toUpperCase();

                for (int j = 0, n = keywords.size(); j < n; j++) {

                    SqlKeyword keyword = keywords.get(j);

                    // check if it exists
                    if (keyword.getText().equals(word)) {

                        exists = true;

                        // if its a user defined one - override
                        if (keyword.isUserDefined()) {
                            keyword.setDatabaseSpecific(true);
                            keyword.setDatabaseProductName(databaseProductName);
                        }

                        break;
                    }

                }

                // add it if it doesn't exist
                if (!exists) {

                    wordsAdded = true;
                    keywords.add(new SqlKeyword(word, databaseProductName, false, true, false));
                }

            }

            if (wordsAdded && reset) {
                // reset the keywords
                Collections.sort(keywords, new KeywordComparator());
                // fire the table event
                model.fireTableDataChanged();
            }

        } catch (DataSourceException e) {

            Log.error("Error retrieving database key words for connection " + databaseConnection.getName());

        } finally {

            databaseHost.close();
        }

    }

    // ------------------------------------------
    // ConnectionListner implementation
    // ------------------------------------------

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent)
                || (event instanceof KeywordEvent);
    }

    /**
     * Indicates a connection has been established.
     * Reloads the keywords to add any db specific words
     * as retrieved from the meta data.
     *
     * @param the encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {

        addDatabaseConnectionKewords(connectionEvent.getDatabaseConnection(), true);

    }

    /**
     * Indicates a connection has been closed.
     *
     * @param the encapsulating event
     */
    public void disconnected(ConnectionEvent connectionEvent) {

        // not really interested at the moment
    }

    // ------------------------------------------

    // -------------------------------------------------------------
    // MouseListener implementation 
    // -------------------------------------------------------------

    /**
     * indicates that text has been inserted
     */
    private boolean inserted = false;

    public void mouseClicked(MouseEvent e) {
        int count = e.getClickCount();
        if (count == 2 && inserted) {
            table.setFocusable(true);
            return;
        } else if (count < 2) {
            inserted = false;
            return;
        }

        int row = table.rowAtPoint(new Point(e.getX(), e.getY()));
        String keyword = keywords.get(row).getText();

        JPanel panel = GUIUtilities.getSelectedCentralPane();
        if (panel instanceof QueryEditor) {
            table.setFocusable(false);
            ((QueryEditor) panel).insertTextAtCaret(keyword);
            inserted = true;
        }
    }

    public void mousePressed(MouseEvent e) {
        mouseClicked(e);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    // -------------------------------------------------------------

    // ---------------------------------------------
    // KeyListener implementation
    // ---------------------------------------------

    /**
     * Invoked when a key has been typed.
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Invoked when a key has been pressed.
     */
    public void keyPressed(KeyEvent e) {
    }

    /**
     * Invoked when a key has been released.
     */
    public void keyReleased(KeyEvent e) {
        search();
    }


    // ----------------------------------------
    // DockedTabView Implementation
    // ----------------------------------------

    public static final String MENU_ITEM_KEY = "viewKeywords";

    public static final String PROPERTY_KEY = "system.display.keywords";

    /**
     * Returns the display title for this view.
     *
     * @return the title displayed for this view
     */
    public String getTitle() {
        return TITLE;
    }

    /**
     * Returns the name defining the property name for this docked tab view.
     *
     * @return the key
     */
    public String getPropertyKey() {
        return PROPERTY_KEY;
    }

    /**
     * Returns the name defining the menu cache property
     * for this docked tab view.
     *
     * @return the preferences key
     */
    public String getMenuItemKey() {
        return MENU_ITEM_KEY;
    }

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewDeselected() {
        return true;
    }

    public String toString() {
        return TITLE;
    }

    /**
     * Keyword table model.
     */
    private class KeywordModel extends AbstractTableModel {

        public KeywordModel() {
        }

        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            if (keywords == null) {
                return 0;
            }
            return keywords.size();
        }

        public Object getValueAt(int row, int col) {
            return keywords.get(row);
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public Class<?> getColumnClass(int col) {
            return String.class;
        }

    } // class KeywordModel

    /**
     * Simple sorter for keywords in alphabetical order.
     */
    private class KeywordComparator implements Comparator<SqlKeyword> {
        public int compare(SqlKeyword obj1, SqlKeyword obj2) {
            return obj1.getText().compareTo(obj2.getText());
        }
    } // class KeywordComparator

}






