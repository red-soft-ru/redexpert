/*
 * SqlCommandHistoryRepositoryImpl.java
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

package org.executequery.repository.spi;

import org.executequery.log.Log;
import org.executequery.repository.SqlCommandHistoryRepository;
import org.executequery.util.UserProperties;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class SqlCommandHistoryRepositoryImpl implements SqlCommandHistoryRepository {

    private static final String FILE_PATH = "sql-command.history";

    private UserSettingsProperties settings;

    public void addSqlCommand(String query, String connectionID) {

        if (!hasQueryAtZero(query, connectionID)) {

            final Vector<String> history = getSqlCommandHistory(connectionID);

            int size = history.size();
            if (size == maxHistoryCount()) {

                history.remove(size - 1);
            }

            history.add(0, query);

            writeHistory(history, connectionID);
        }
    }

    private boolean hasQueryAtZero(String query, String connectionID) {

        final Vector<String> history = getSqlCommandHistory(connectionID);

        if (history.isEmpty()) {

            return false;
        }

        String queryAtZero = history.get(0);

        return (queryAtZero.compareTo(query) == 0);
    }

    private int maxHistoryCount() {

        return UserProperties.getInstance().getIntProperty("editor.history.count");
    }

    public void clearSqlCommandHistory(String connectionID) {

        writeHistory(new Vector<String>(0), connectionID);
    }

    @SuppressWarnings("unchecked")
    public Vector<String> getSqlCommandHistory(String connectionID) {

        try {

            File file = new File(filePath(connectionID));

            if (!file.exists()) {

                return emptyHistory();

            } else {

                Object object = FileUtils.readObject(file);

                if (object == null || !(object instanceof Vector)) {

                    return emptyHistory();

                } else {

                    return (Vector<String>) object;
                }
            }

        } catch (IOException e) {

            if (Log.isDebugEnabled()) {

                Log.debug("IO error opening SQL command history.", e);
            }

            return emptyHistory();
        }

    }

    private void writeHistory(Vector<String> history, String connectionID) {

        try {

            FileUtils.writeObject(history, filePath(connectionID));

        } catch (IOException e) {

            if (Log.isDebugEnabled()) {

                Log.debug("IO error storing SQL command history.", e);
            }

        }

    }

    private Vector<String> emptyHistory() {

        return new Vector<String>();
    }

    private String filePath(String connectionID) {

        if (settings == null) {

            settings = new UserSettingsProperties();
        }
        String dir = settings.getUserSettingsBaseHome() + "sqlHistory" + settings.fileSeparator();
        File f_dir = new File(dir);
        if (!f_dir.exists()) {
            f_dir.mkdirs();
        }
        return dir + connectionID + "." + FILE_PATH;
    }

    public String getId() {

        return REPOSITORY_ID;
    }

}











