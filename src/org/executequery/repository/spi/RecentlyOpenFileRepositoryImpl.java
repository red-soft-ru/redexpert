/*
 * RecentlyOpenFileRepositoryImpl.java
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

package org.executequery.repository.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.event.DefaultRecentOpenFileEvent;
import org.executequery.event.RecentOpenFileEvent;
import org.executequery.repository.RecentlyOpenFileRepository;
import org.executequery.repository.RepositoryException;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

public class RecentlyOpenFileRepositoryImpl extends AbstractUserSettingsRepository
                                            implements RecentlyOpenFileRepository {

    private static final String RECENT_FILE_LIST_FILE = "recent.files";

    private List<String> files;

    public void addFile(String file) throws RepositoryException {

        if (containsFile(file)) {

            moveToTop(file);

        } else {

            appendToFront(file);
        }

        write();
    }

    public void clear() throws RepositoryException {

        ensureFilesLoaded();
        files.clear();

        write();
    }

    public String[] getFiles() {

        ensureFilesLoaded();

        return (String[])files.toArray(new String[files.size()]);
    }

    public String getId() {

        return REPOSITORY_ID;
    }

    private synchronized void write() throws RepositoryException {

        StringBuilder sb = new StringBuilder();

        for (String file : filesAsList()) {

            sb.append(file);
            sb.append(Constants.NEW_LINE_STRING);
        }

        try {

            FileUtils.writeFile(getRecentFileListFilePath(), sb.toString());

            EventMediator.fireEvent(new DefaultRecentOpenFileEvent(
                            this, RecentOpenFileEvent.RECENT_FILES_UPDATED));

        } catch (IOException e) {

            throw new RepositoryException(e);
        }

    }

    private void appendToFront(String file) {

        filesAsList().add(0, file);

        if (filesAsList().size() > getMaxRecentFileCount()) {

            trimToCapacity();
        }

    }

    private int getMaxRecentFileCount() {

        return SystemProperties.getIntProperty("user", "recent.files.count");
    }

    private void moveToTop(String file) {

        int index = indexOfFile(file);

        if (index != -1) {

            filesAsList().remove(index);
        }

        appendToFront(file);
    }



    private int indexOfFile(String file) {

        for (int i = 0, n = filesAsList().size(); i < n; i++) {

            if (filesAsList().get(i).equals(file)) {

                return i;
            }

        }

        return -1;
    }

    private boolean containsFile(String file) {

        return MiscUtils.containsValue(getFiles(), file);
    }

    private List<String> filesAsList() {

        ensureFilesLoaded();

        return files;
    }

    private void ensureFilesLoaded() {

        if (files != null) {

            return;
        }

        try {

            String recentFiles = loadFile();

            if (MiscUtils.isNull(recentFiles)) {

                files = new ArrayList<String>(0);

            } else {

                String[] recentFilesArray =
                    MiscUtils.splitSeparatedValues(recentFiles, "\n");

                files = new ArrayList<String>(recentFilesArray.length);

                for (String file : recentFilesArray) {

                    files.add(file);
                }

            }

        } catch (IOException e) {

            files = new ArrayList<String>(0);
        }

    }

    private String loadFile() throws IOException {

        String filesString = FileUtils.loadFile(getRecentFileListFilePath());

        if (filesString != null) {

            filesString = filesString.trim();

            if (filesString.endsWith("\n")) {

                filesString = filesString.substring(0, filesString.length() - 1);
            }

        }

        return filesString;
    }

    private void trimToCapacity() {

        files = filesAsList().subList(0, getMaxRecentFileCount() - 1);
    }

    private String getRecentFileListFilePath() {

        return getUserSettingsHomePath().concat(RECENT_FILE_LIST_FILE);
    }

}










