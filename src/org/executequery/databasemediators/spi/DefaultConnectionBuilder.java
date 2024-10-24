/*
 * DefaultConnectionBuilder.java
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

package org.executequery.databasemediators.spi;

import org.executequery.ApplicationContext;
import org.executequery.ExecuteQuery;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.ConnectionBuilder;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * @author Takis Diakoumis
 */
public class DefaultConnectionBuilder implements ConnectionBuilder {

    private final DatabaseConnection databaseConnection;

    private boolean cancelled;
    private SwingWorker worker;
    private DataSourceException exception;

    public DefaultConnectionBuilder(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public void connect() throws IllegalArgumentException {

        ConnectionBuilder connectionBuilder = this;
        ConnectionProgressDialog progressDialog = new ConnectionProgressDialog(this);

        worker = new SwingWorker("Connection to " + databaseConnection.getName()) {
            private Properties props;

            @Override
            public Object construct() {

                try {

                    props = databaseConnection.getJdbcProperties();
                    if (!props.containsKey("connectTimeout"))
                        props.setProperty("connectTimeout", SystemProperties.getProperty("user", "connection.connect.timeout"));

                    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
                    if (!MiscUtils.isNull(ApplicationContext.getInstance().getExternalPID()))
                        pid = ApplicationContext.getInstance().getExternalPID();
                    props.setProperty("process_id", pid);

                    String path = null;
                    try {
                        if (!MiscUtils.isNull(ApplicationContext.getInstance().getExternalProcessName()))
                            path = ApplicationContext.getInstance().getExternalProcessName();

                        if (path == null)
                            path = ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

                    } catch (URISyntaxException e) {
                        Log.error(e.getMessage(), e);
                    }
                    props.setProperty("process_name", path);

                    databaseConnection.setJdbcProperties(props);
                    ConnectionManager.createDataSource(databaseConnection, connectionBuilder, false);

                } catch (DataSourceException e) {

                    if (e.getMessage().contains("java.sql.SQLTimeoutException") && progressDialog.isActive()) {
                        cancel();
                        GUIUtilities.displayWarningMessage(Bundles.get(DefaultConnectionBuilder.class, "TimeoutException", props.getProperty("connectTimeout")));

                    } else if (e.getMessage().contentEquals("Connection cancelled"))
                        cancel();

                    exception = e;
                }

                return null;
            }

            @Override
            public void finished() {
                progressDialog.dispose();
            }
        };

        worker.start();
        progressDialog.run();
    }

    @Override
    public void cancel() {
        ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).close();
        databaseConnection.setConnected(false);
        worker.interrupt();
        cancelled = true;
    }

    @Override
    public String getConnectionName() {
        return databaseConnection.getName();
    }

    public DataSourceException getException() {
        return exception;
    }

    @Override
    public String getErrorMessage() {
        return exception != null ? exception.getMessage() : "";
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isConnected() {
        return databaseConnection.isConnected();
    }

}
