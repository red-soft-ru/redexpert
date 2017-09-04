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

import java.io.IOException;

import org.executequery.ApplicationException;
import org.executequery.repository.LogRepository;
import org.executequery.util.ApplicationProperties;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.FileUtils;

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
    
    public String getLogFileDirectory() {

        UserSettingsProperties settings = new UserSettingsProperties();

        return settings.getUserSettingsBaseHome() + LOG_FILE_DIR_NAME +
            System.getProperty("file.separator");
    }

    public String getId() {

        return REPOSITORY_ID;
    }

    private void reset(String path) {

        try {

            FileUtils.writeFile(path, "");

        } catch (IOException e) {}

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











