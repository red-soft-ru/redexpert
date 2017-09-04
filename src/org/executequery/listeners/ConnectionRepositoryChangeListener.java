/*
 * ConnectionRepositoryChangeListener.java
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

package org.executequery.listeners;

import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.ConnectionRepositoryListener;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.ThreadUtils;

public final class ConnectionRepositoryChangeListener implements ConnectionRepositoryListener {

    public void connectionAdded(ConnectionRepositoryEvent connectionRepositoryEvent) {

        saveConnections();
    }

    public void connectionRemoved(ConnectionRepositoryEvent connectionRepositoryEvent) {

        saveConnections();
    }

    public void connectionModified(ConnectionRepositoryEvent connectionRepositoryEvent) {

        saveConnections();
    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof ConnectionRepositoryEvent);
    }

    private void saveConnections() {

        ThreadUtils.startWorker(new Runnable() {

            public void run() {

                databaseConnectionRepository().save();
            }

        });

    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository)RepositoryCache.load(
                    DatabaseConnectionRepository.REPOSITORY_ID);
    }

}





