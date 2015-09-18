/*
 * DefaultConnectionBuilder.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.util.SwingWorker;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DefaultConnectionBuilder implements ConnectionBuilder {

    /** The worker thread to establish the connection */
    private SwingWorker worker;

    /** The connection progress dialog */
    private ConnectionProgressDialog progressDialog;

    /** Indicates whether the process was cancelled */
    private boolean cancelled;

    /** The database connection object */
    private DatabaseConnection databaseConnection;

    /** The exception on error */
    private DataSourceException dataSourceException;

    public DefaultConnectionBuilder(DatabaseConnection databaseConnection) {

        this.databaseConnection = databaseConnection;
    }

    public void cancel() {

        cancelled = true;
        worker.interrupt();
    }

    public String getConnectionName() {

        return databaseConnection.getName();
    }

    public DataSourceException getException() {

        return dataSourceException;
    }

    public String getErrorMessage() {

        if (dataSourceException != null) {

            return dataSourceException.getMessage();
        }

        return "";
    }

    public boolean isCancelled() {

        return cancelled;
    }

    public boolean isConnected() {

        return databaseConnection.isConnected();
    }

    public void connect() {

        progressDialog = new ConnectionProgressDialog(this);

        worker = new SwingWorker() {
            public Object construct() {

                createDataSource();
                return null;
            }
            public void finished() {

                if (!cancelled && progressDialog != null) {

                    progressDialog.dispose();
                }

            }
        };

        worker.start();
        progressDialog.run();

    }

    private void createDataSource() {

        try {

            ConnectionManager.createDataSource(databaseConnection);

        } catch (DataSourceException e) {

            dataSourceException = e;
        }

    }

}




