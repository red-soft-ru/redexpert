/*
 * RecentFileIOListener.java
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

package org.executequery.io;

import org.executequery.event.ApplicationEvent;
import org.executequery.event.FileIOEvent;
import org.executequery.event.FileIOListener;
import org.executequery.log.Log;
import org.executequery.repository.RecentlyOpenFileRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.repository.RepositoryException;

public class RecentFileIOListener implements FileIOListener {

    public void inputComplete(FileIOEvent fileIoEvent) {
        
        addRecentlyOpenedFile(fileIoEvent);
    }

    public void outputComplete(FileIOEvent fileIoEvent) {

        addRecentlyOpenedFile(fileIoEvent);
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        
        return (event instanceof FileIOEvent);
    }

    private void addRecentlyOpenedFile(FileIOEvent fileIoEvent) {

        try {

            recentlyOpenFileRepository().addFile(fileIoEvent.getAbsoluteFilePath());

        } catch (RepositoryException e) {

            Log.error("An IO error occurred writing to the recent open files list: " 
                    + e.getMessage());
        }

    }

    private RecentlyOpenFileRepository recentlyOpenFileRepository() {

        return (RecentlyOpenFileRepository)RepositoryCache.load(
                    RecentlyOpenFileRepository.REPOSITORY_ID);        
    }

}






