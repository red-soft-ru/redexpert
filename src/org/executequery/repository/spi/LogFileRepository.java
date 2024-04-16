/*
 * LogFileRepository.java
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

import org.executequery.ApplicationException;
import org.executequery.ExecuteQuery;
import org.executequery.repository.LogRepository;
import org.executequery.util.ApplicationProperties;
import org.executequery.util.UserProperties;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;

public class LogFileRepository implements LogRepository {

    public String load(int type) {

        String path = pathForType(type);

        return loadFromPath(path);
    }

    public void resetAll() {

        int[] types = {ACTIVITY, EXPORT, IMPORT};

        for (int type : types) {

            reset(type);
        }

    }

    public void reset(int type) {

        String path = pathForType(type);

        reset(path);
    }

    public String getLogFilePath(int type) {

        return pathForType(type);
    }

    @Override
    public String getLogFileDirectory() {

        String logFolderBasePath = new UserSettingsProperties().getUserSettingsBaseHome()
                + LOG_FILE_DIR_NAME
                + FileSystems.getDefault().getSeparator();

        String logFolderPath = UserProperties.getInstance().getStringProperty("editor.logging.path");
        if (logFolderPath == null || logFolderPath.isEmpty())
            return logFolderBasePath;

        // TODO remove in the next release
        // needs to get rid of the old relative path syntax
        if (logFolderPath.startsWith("%re%")) {
            logFolderPath = logFolderPath.replace("%re%" + FileSystems.getDefault().getSeparator(), "");
            logFolderPath = logFolderPath.replace("%re%", "");

            if (logFolderPath.isEmpty())
                logFolderPath = LOG_FILE_DIR_NAME + FileSystems.getDefault().getSeparator();

            UserProperties.getInstance().setProperty("editor.logging.path", logFolderPath);
        }

        try {
            boolean useRelativePath = false;

            if (System.getProperty("os.name").toLowerCase().contains("lin")) {
                if (!logFolderPath.startsWith("/"))
                    useRelativePath = true;

            } else if (System.getProperty("os.name").toLowerCase().contains("win")) {
                if (!logFolderPath.substring(1).startsWith(":\\"))
                    useRelativePath = true;
            }

            if (useRelativePath) {
                String appPath = new File(ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
                logFolderPath = appPath + FileSystems.getDefault().getSeparator() + logFolderPath;
            }

        } catch (URISyntaxException e) {
            logFolderPath = logFolderBasePath;
            UserProperties.getInstance().setProperty("editor.logging.path", logFolderPath);
        }

        if (!logFolderPath.endsWith(FileSystems.getDefault().getSeparator()))
            logFolderPath += FileSystems.getDefault().getSeparator();

        return logFolderPath;
    }

    public String getId() {

        return REPOSITORY_ID;
    }

    private void reset(String path) {

        try {

            FileUtils.writeFile(path, "");

        } catch (IOException e) {
        }

    }

    private String loadFromPath(String path) {

        try {

            return FileUtils.loadFile(path);

        } catch (IOException e) {

            throw new ApplicationException(e);
        }

    }

    private String pathForType(int type) {

        String fileName = "";

        switch (type) {

            case ACTIVITY:
                fileName = getProperty(EQ_OUTPUT_LOG_KEY);
                break;

            case EXPORT:
                fileName = getProperty(EQ_EXPORT_LOG_KEY);
                break;

            case IMPORT:
                fileName = getProperty(EQ_IMPORT_LOG_KEY);
                break;

        }

        return getLogFileDirectory() + fileName;
    }

    private String getProperty(String key) {

        return ApplicationProperties.getInstance().getProperty(key);
    }

}











