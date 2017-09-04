/*
 * Log.java
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

package org.executequery.log;

import org.apache.log4j.Appender;
import org.executequery.repository.LogRepository;
import org.executequery.repository.RepositoryCache;

/**
 * Primary application logger.
 * 
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public final class Log {

    public static final String LOGGER_NAME = "system-logger";
    
    public static final String PATTERN = "[%d{HH:mm:ss}] %m%n";
    
    public static final int MAX_BACKUP_INDEX = 5;

    private static final String MAX_FILE_SIZE = "1MB";
    
    private static final String LEVEL = "INFO";

    private static final String LOG_FILE_PATH = 
        ((LogRepository)RepositoryCache.load(
                LogRepository.REPOSITORY_ID)).getLogFilePath(LogRepository.ACTIVITY);
    
    private static final ApplicationLog log = 
        new ApplicationLog(LOG_FILE_PATH, LOGGER_NAME, PATTERN, LEVEL, 
                MAX_BACKUP_INDEX, MAX_FILE_SIZE);

    private Log() {}

    /**
     * Adds the specified appender to the logger.
     *
     * @param appender - the appender to be added
     */
    public static void addAppender(Appender appender) {

        log.addAppender(appender);
    }
    
    /**
     * Returns whether the log level is set to DEBUG.
     */
    public static boolean isDebugEnabled() {

        return log.isDebugEnabled();
    }
    
    /**
     * Returns whether the log level is set to TRACE.
     */
    public static boolean isTraceEnabled() {
    	
    	return log.isTraceEnabled();
    }
    
    /**
     * Sets the logger level to that specified.
     *
     * @param level - the logger level to be set.<br>
     *        Valid values are: ERROR, DEBUG, INFO, WARN, ALL, FATAL, TRACE
     */
    public static void setLevel(String level) {

        log.setLevel(level);
    }

    /**
     * Logs a message at log level INFO.
     *
     * @param message  the log message.
     * @param throwable the throwable.
     */
    public static void info(Object message, Throwable throwable) {

        log.info(message, throwable);
    }

    /**
     * Logs a message at log level WARN.
     *
     * @param message  the log message.
     * @param throwable the throwable.
     */
    public static void warning(Object message, Throwable throwable) {

        log.warning(message, throwable);
    }

    /**
     * Logs a message at log level DEBUG.
     *
     * @param message  the log message.
     */
    public static void debug(Object message) {

        log.debug("DEBUG: " + message);
    }

    /**
     * Logs a message at log level DEBUG.
     *
     * @param message  the log message.
     * @param throwable the throwable.
     */
    public static void debug(Object message, Throwable throwable) {
        
        log.debug("DEBUG: " + message, throwable);
    }

    /**
     * Logs a message at log level TRACE.
     *
     * @param message  the log message.
     */
    public static void trace(Object message) {
        
        log.trace("TRACE: " + message);
    }
    
    /**
     * Logs a message at log level TRACE.
     *
     * @param message  the log message.
     * @param throwable the throwable.
     */
    public static void trace(Object message, Throwable throwable) {
        
        log.trace("TRACE: " + message, throwable);
    }
    
    /**
     * Logs a message at log level ERROR.
     *
     * @param message  the log message.
     * @param e the throwable.
     */
    public static void error(Object message, Throwable e) {
        
        log.error(message, e);
    }

    /**
     * Logs a message at log level INFO.
     *
     * @param message  the log message.
     */
    public static void info(Object message) {
        
        log.info(message);
    }

    /**
     * Logs a message at log level WARN.
     *
     * @param message  the log message.
     */
    public static void warning(Object message) {

        log.warning(message);
    }

    /**
     * Logs a message at log level ERROR.
     *
     * @param message  the log message.
     */
    public static void error(Object message) {

        log.error(message);
    }

}





