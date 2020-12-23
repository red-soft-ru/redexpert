/*
 * HostPanel.java
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

package org.executequery.gui.browser;

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.log.Log;
import org.executequery.print.TablePrinter;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.GUIUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.print.Printable;
import java.util.HashMap;
import java.util.Vector;

/**
 * Database connection host panel.
 * Displays connection/host info and database properties once connected.
 *
 * @author Takis Diakoumis
 */
public class HostPanel extends AbstractFormObjectViewPanel implements ConnectionListener {

    public static final String NAME = "HostPanel";

    private DisabledField databaseProductField;
    private DisabledField hostField;
    private DisabledField sourceField;
    private DisabledField schemaField;
    private DisabledField urlField;

    private JTable schemaTable;
    private HostModel model;

    private boolean initialised;

    /**
     * the current host object
     */
    private DatabaseHost host;

    /**
     * the tab pane display
     */
    private JTabbedPane tabPane;

    /**
     * the connection info pane
     */
    private ConnectionPanel connectionPanel;

    /**
     * the key words panel
     */
    private KeyWordsPanel keyWordsPanel;

    /**
     * the java sql types panel
     */
    private JavaSQLTypesPanel javaSqlTypesPanel;

    /**
     * the database properties pane
     */
    private DatabasePropertiesPanel databasePropertiesPanel;

    /**
     * the data types panel
     */
    private DataTypesPanel dataTypesPanel;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    public HostPanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {

        connectionPanel = new ConnectionPanel(controller);
        databasePropertiesPanel = new DatabasePropertiesPanel();
        keyWordsPanel = new KeyWordsPanel();
        dataTypesPanel = new DataTypesPanel();
        javaSqlTypesPanel = new JavaSQLTypesPanel();

        /*tabPane = new JTabbedPane(JTabbedPane.TOP);
        tabPane.addTab(Bundles.getCommon("connection"), connectionPanel);
        tabPane.addTab(bundleString("DatabaseProperties"), databasePropertiesPanel);
        tabPane.addTab(bundleString("SQLKeywords"), keyWordsPanel);
        tabPane.addTab(bundleString("DataTypes"), dataTypesPanel);
        tabPane.addTab("java.sql.Types", javaSqlTypesPanel);
        enableConnectionTabs(false);*/

        setHeaderText(bundleString("DatabaseConnection"));
        setHeaderIcon(GUIUtilities.loadIcon("Database24.png"));
        setContentPanel(connectionPanel);

        // register with the event listener
        EventMediator.registerListener(this);
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        connectionPanel.buildDriversList();
        return connectionPanel.tabViewSelected();
    }

    private void enableConnectionTabs(boolean enabled) {
        /*int[] tabs = new int[]{1, 2, 3};
        for (int i = 0; i < tabs.length; i++) {
            tabPane.setEnabledAt(tabs[i], enabled);
        }
        if (!enabled) {
            tabPane.setSelectedIndex(0);
        }*/
    }

    public void connectionNameChanged(String name) {
        connectionPanel.connectionNameChanged(name);
    }

    /**
     * Informs any panels of a new selection being made.
     */
    protected void selectionChanging() {
        connectionPanel.selectionChanging();
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    public boolean tabViewDeselected() {
        return connectionPanel.tabViewDeselected();
    }

    public void setValues(DatabaseHost host) {

        this.host = host;
        connectionPanel.setConnectionValue(host);

        DatabaseConnection databaseConnection = host.getDatabaseConnection();
        if (databaseConnection.isConnected()) {

            changePanelData();

        } else {

            //enableConnectionTabs(false);
        }

    }

    /**
     * Reloads the database properties meta data table panel.
     */
    protected void updateDatabaseProperties() {

        if (!host.getDatabaseConnection().isConnected()) {

            return;
        }

        try {

            databasePropertiesPanel.setDatabaseProperties(host.getDatabaseProperties());

        } catch (DataSourceException e) {

            controller.handleException(e);
            databasePropertiesPanel.setDatabaseProperties(
                    new HashMap<Object, Object>(0));
        }

    }

    /**
     * Loads the sql key words for this host.
     */
    protected void updateDatabaseKeywords() {

        if (!host.getDatabaseConnection().isConnected()) {
            return;
        }

        try {
            keyWordsPanel.setDatabaseKeywords(host.getDatabaseKeywords());
        } catch (DataSourceException e) {
            controller.handleException(e);
            keyWordsPanel.setDatabaseKeywords(new String[0]);
        }
    }

    /**
     * Loads the data type info for this host.
     */
    protected void updateDatabaseTypeInfo() {

        if (!host.getDatabaseConnection().isConnected()) {

            return;
        }

        try {

            dataTypesPanel.setDataTypes(host.getDataTypeInfo());

        } catch (DataSourceException e) {

            dataTypesPanel.setDataTypeError(e.getExtendedMessage());
            Log.warning("Error retriving type info for host: " + e.getExtendedMessage());
//            controller.handleException(e);
        }
    }

    private void changePanelData() {

        // notify the database properties
        updateDatabaseProperties();
        //Hashtable properties = controller.getDatabaseProperties();
        //databasePropertiesPanel.setDatabaseProperties(properties);

        // notify the keywords panel
        updateDatabaseKeywords();
        //String[] keywords = controller.getDatabaseKeywords();
        //keyWordsPanel.setDatabaseKeywords(keywords);

        // notify the data types panel
        updateDatabaseTypeInfo();
        //dataTypesPanel.setDataTypes(controller.getDataTypesResultSet());

        // enable the tabs
    }

    /**
     * Indicates a connection has been established.
     *
     * @param the encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {

        // notify connection panel
        connectionPanel.connected(connectionEvent.getDatabaseConnection());
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                // notify other panels
                changePanelData();
            }
        });
        GUIUtilities.scheduleGC();
    }

    /**
     * Indicates a connection has been closed.
     *
     * @param the encapsulating event
     */
    public void disconnected(ConnectionEvent connectionEvent) {
        connectionPanel.disconnected(connectionEvent.getDatabaseConnection());
        GUIUtilities.scheduleGC();
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent);
    }


    public String getLayoutName() {
        return NAME;
    }

    public void refresh() {
    }

    public void cleanup() {
    }

    public Printable getPrintable() {

        String hostText = hostField != null ? hostField.getText() : "";
        return new TablePrinter(schemaTable, "Database Server: " + hostText);
    }

    public JTable getTable() {
        return schemaTable;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public void setValues(String sourceName, String schemaName, Vector values) {
        sourceField.setText(sourceName);
        schemaField.setText(schemaName);
        model.setValues(values);
        initialised = true;
    }

    public void setValues(String databaseName, String hostName, String sourceName,
                          String schemaName, String urlName, Vector values) {
        databaseProductField.setText(databaseName);
        hostField.setText(hostName);
        sourceField.setText(sourceName);
        schemaField.setText(schemaName);
        urlField.setText(urlName);
        model.setValues(values);
    }

    private class HostModel extends AbstractTableModel {

        private Vector values = new Vector(0);
        private String header = "Catalog Name";

        public void setValues(Vector values) {
            this.values = values;
            fireTableDataChanged();
        }

        public int getRowCount() {
            return values.size();
        }

        public int getColumnCount() {
            return 1;
        }

        public String getColumnName(int col) {
            return header;
        }

        public Object getValueAt(int row, int col) {
            return values.elementAt(row);
        }

    } // class HostModel

} 





