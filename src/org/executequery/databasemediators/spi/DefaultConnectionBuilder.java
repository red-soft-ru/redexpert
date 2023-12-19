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

import org.executequery.databasemediators.ConnectionBuilder;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.util.SwingWorker;

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

            @Override
            public Object construct() {

                try {
                    ConnectionManager.createDataSource(databaseConnection, connectionBuilder);

                } catch (IllegalArgumentException e) {
                    exception = new DataSourceException(e);
                    if (e.getMessage().contentEquals("Connection cancelled"))
                        cancel();
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
