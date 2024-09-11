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
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.GUIUtils;

import java.awt.print.Printable;

/**
 * Database connection host panel.
 * Displays connection/host info and database properties once connected.
 *
 * @author Takis Diakoumis
 */
public class HostPanel extends AbstractFormObjectViewPanel implements ConnectionListener {

    public static final String NAME = "HostPanel";

    private final BrowserController controller;
    private DatabaseHost host;

    private DataTypesPanel dataTypesPanel;
    private ConnectionPanel connectionPanel;
    private DatabasePropertiesPanel propertiesPanel;

    public HostPanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    private void init() {

        dataTypesPanel = new DataTypesPanel();
        connectionPanel = new ConnectionPanel(controller);
        propertiesPanel = new DatabasePropertiesPanel();

        setContentPanel(connectionPanel);
        connectionPanel.addTab(bundleString("DatabaseProperties"), propertiesPanel);

        EventMediator.registerListener(this);
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        connectionPanel.selectActualDriver();
        return connectionPanel.tabViewSelected();
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

    public void setValues(DatabaseHost host, boolean updatePropertiesPanel) {

        this.host = host;
        connectionPanel.setConnectionValue(host);

        DatabaseConnection databaseConnection = host.getDatabaseConnection();
        if (databaseConnection.isConnected())
            changePanelData();
        else if (updatePropertiesPanel)
            updateDatabaseProperties(true);
    }

    /**
     * Reloads the database properties meta data table panel.
     */
    protected void updateDatabaseProperties() {
        updateDatabaseProperties(false);
    }

    /**
     * Reloads the database properties meta data table panel.
     */
    protected void updateDatabaseProperties(boolean useStaticMethhod) {
        propertiesPanel.setDatabaseProperties(useStaticMethhod ?
                DefaultDatabaseHost.getDatabaseProperties(host.getDatabaseConnection(), false) :
                host.getDatabaseProperties()
        );
    }

    /**
     * Loads the data type info for this host.
     */
    protected void updateDatabaseTypeInfo() {

        if (!host.getDatabaseConnection().isConnected())
            return;

        try {
            dataTypesPanel.setDataTypes(host.getDataTypeInfo());

        } catch (DataSourceException e) {
            dataTypesPanel.setDataTypeError(e.getExtendedMessage());
            Log.warning("Error retriving type info for host: " + e.getExtendedMessage());
        }
    }

    private void changePanelData() {
        updateDatabaseProperties();
        updateDatabaseTypeInfo();
    }

    /**
     * Indicates a connection has been established.
     */
    @Override
    public void connected(ConnectionEvent connectionEvent) {
        connectionPanel.connected(connectionEvent.getDatabaseConnection());
        GUIUtils.startWorker(this::changePanelData);
        GUIUtilities.scheduleGC();
    }

    /**
     * Indicates a connection has been closed.
     */
    @Override
    public void disconnected(ConnectionEvent connectionEvent) {
        connectionPanel.disconnected();
        GUIUtilities.scheduleGC();
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent);
    }

    @Override
    public String getLayoutName() {
        return NAME;
    }

    @Override
    public void refresh() {
    }

    @Override
    public void cleanup() {
    }

    @Override
    public Printable getPrintable() {
        return null;
    }

}
