/*
 * LogRepository.java
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

package org.executequery.repository;

public interface LogRepository extends Repository {

    String REPOSITORY_ID = "log-repository";
    
    int ACTIVITY = 0;
    int EXPORT = 1;
    int IMPORT = 2;

    String EQ_IMPORT_LOG_KEY = "eq.import.log";

    String EQ_EXPORT_LOG_KEY = "eq.export.log";
    
    String EQ_OUTPUT_LOG_KEY = "eq.output.log";
    
    String LOG_FILE_DIR_NAME = "logs";

    void resetAll();

    void reset(int type);
    
    String load(int type);
    
    String getLogFilePath(int type);
    
    String getLogFileDirectory();

}










