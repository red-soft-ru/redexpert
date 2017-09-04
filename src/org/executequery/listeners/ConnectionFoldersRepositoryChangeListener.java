/*
 * ConnectionFoldersRepositoryChangeListener.java
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
import org.executequery.event.ConnectionsFolderRepositoryEvent;
import org.executequery.event.ConnectionFoldersRepositoryListener;
import org.executequery.repository.ConnectionFoldersRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.ThreadUtils;

public final class ConnectionFoldersRepositoryChangeListener implements ConnectionFoldersRepositoryListener {

    public void folderAdded(ConnectionsFolderRepositoryEvent connectionsFolderRepositoryEvent) {
        
        saveFolders();
    }

    public void folderModified(ConnectionsFolderRepositoryEvent connectionsFolderRepositoryEvent) {
        
        saveFolders();
    }

    public void folderRemoved(ConnectionsFolderRepositoryEvent connectionsFolderRepositoryEvent) {

        saveFolders();
    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof ConnectionsFolderRepositoryEvent);
    }

    private void saveFolders() {

        ThreadUtils.startWorker(new Runnable() {

            public void run() {

                connectionFoldersRepository().save();
            }

        });

    }

    private ConnectionFoldersRepository connectionFoldersRepository() {

        return (ConnectionFoldersRepository)RepositoryCache.load(ConnectionFoldersRepository.REPOSITORY_ID);
    }

}





