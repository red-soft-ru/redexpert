/*
 * CreateTablePanel.java
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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Vector;

import org.executequery.ActiveComponent;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.BottomButtonPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.event.KeywordEvent;
import org.executequery.event.KeywordListener;
import org.executequery.gui.table.CreateTableFunctionPanel;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.MiscUtils;

/**
 * <p>The Create Panel function
 *
 * @author Takis Diakoumis
 * @version $Revision: 1780 $
 * @date $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class CreateTablePanel extends CreateTableFunctionPanel
        implements ActionListener,
        KeywordListener,
        ActiveComponent {

    /**
     * This objects title as an internal frame
     */
    public static final String TITLE = "Create Table";

    /**
     * This objects icon as an internal frame
     */
    public static final String FRAME_ICON = "NewTable16.png";

    /**
     * the parent container
     */
    private ActionContainer parent;

    /**
     * <p> Constructs a new instance.
     */
    public CreateTablePanel(ActionContainer parent) {
        super();
        this.parent = parent;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setFocusComponent();
    }

    public CreateTablePanel(DatabaseConnection dc, ActionContainer parent) {
        this(parent);
        connectionsCombo.setSelectedItem(dc);
    }

    /**
     * <p>Initializes the state of this instance.
     */
    private void jbInit() throws Exception {

        addButtonsPanel(new BottomButtonPanel(
                this, "Create", "create-table", parent.isDialog()));
        setPreferredSize(new Dimension(750, 480));
        EventMediator.registerListener(this);
    }

    /**
     * Indicates that a [long-running] process has begun or ended
     * as specified. This may trigger the glass pane on or off
     * or set the cursor appropriately.
     *
     * @param inProcess - true | false
     */
    public void setInProcess(boolean inProcess) {
        if (parent != null) {

            if (inProcess) {

                parent.block();

            } else {

                parent.unblock();
            }
        }
    }

    /**
     * Notification of a new keyword added to the list.
     */
    public void keywordsAdded(KeywordEvent e) {
        sqlText.setSQLKeywords(true);
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof DefaultKeywordEvent);
    }

    /**
     * Notification of a keyword removed from the list.
     */
    public void keywordsRemoved(KeywordEvent e) {
        sqlText.setSQLKeywords(true);
    }

    public Vector<String> getHostedSchemasVector() {
        try {
            return metaData.getHostedSchemasVector();
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the catalog/schema list for the " +
                            "selected connection.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
            return new Vector<String>(0);
        }
    }

    public Vector<String> getSchemaTables(String schemaName) {
        try {
            return metaData.getSchemaTables(schemaName);
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the table list for the " +
                            "selected catalog/schema.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
            return new Vector<String>(0);
        }
    }

    public Vector<String> getColumnNamesVector(String tableName, String schemaName) {
        try {
            return metaData.getColumnNamesVector(tableName, schemaName);
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the column names for the " +
                            "selected table.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
            return new Vector<String>(0);
        }
    }

    /**
     * Releases database resources before closing.
     */
    public void cleanup() {
        EventMediator.deregisterListener(this);
        metaData.closeConnection();
    }

    /**
     * Action listener implementation.<br>
     * Executes the create table script.
     *
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {

        DatabaseConnection dc = getSelectedConnection();
        if (dc == null) {
            GUIUtilities.displayErrorMessage(
                    "No database connection is available.");
            return;
        }
        createTable();
    }

    private void createTable() {
        try {
            String querys = getSQLText();
            if (querys.endsWith(";")) {
                querys = querys.substring(0, querys.length() - 1);
            }
            String query = "";
            boolean commit = true;
            while (querys.trim().length() > 0 && commit) {
                if (querys.contains(";")) {
                    query = querys.substring(0, querys.indexOf(";"));
                    querys = querys.substring(querys.indexOf(";") + 1, querys.length());
                } else {
                    query = querys;
                    querys = "";
                }
                while (query.indexOf("\n") == 0) {
                    query = query.substring(1, query.length());
                }

                DatabaseConnection dc = getSelectedConnection();
                ExecuteQueryDialog eqd = new ExecuteQueryDialog("Creating table", query, dc, true);
                eqd.display();
                commit = eqd.getCommit();
            }
            if (commit)
                parent.finished();

        } catch (Exception exc) {
            GUIUtilities.displayExceptionErrorDialog("Error:\n" + exc.getMessage(), exc);
        }

    }

    public String toString() {
        return TITLE;
    }

}

