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
import org.executequery.util.UserProperties;

import java.util.Objects;

/**
 * Primary application logger.
 *
 * @author Takis Diakoumis
 */
public final class Log {

    public static final String LOGGER_NAME = "system-logger";
    public static final String PATTERN = "[%d{dd.MM.yyyy HH:mm:ss.SSS}] %m%n";
    private static final String LEVEL = "INFO";
    private static final String MAX_FILE_SIZE = "1MB";
    public static final String DEFAULT_STACKTRACE_MESSAGE = "stack trace:";

    public static final int MAX_BACKUP_INDEX =
            UserProperties.getInstance().getIntProperty("editor.logging.backups");

    private static final boolean IS_LOG_ENABLED =
            UserProperties.getInstance().getBooleanProperty("system.log.enabled");

    private static final String LOG_FILE_PATH = ((LogRepository) RepositoryCache.load(LogRepository.REPOSITORY_ID))
            .getLogFilePath(LogRepository.ACTIVITY);

    private static final ApplicationLog log = new ApplicationLog(
            LOG_FILE_PATH, LOGGER_NAME, PATTERN, LEVEL, MAX_BACKUP_INDEX, MAX_FILE_SIZE
    );

    private Log() {
    }

    /**
     * Adds the specified appender to the logger.
     *
     * @param appender the appender to be added
     */
    public static void addAppender(Appender appender) {
        log.addAppender(appender);
    }

    /**
     * Returns whether the logging is enabled.
     */
    public static boolean isLogEnabled() {
        return IS_LOG_ENABLED && log.isLogEnabled();
    }

    /**
     * Returns whether the logging is enabled and the log level is set to DEBUG.
     */
    public static boolean isDebugEnabled() {
        return isLogEnabled() && log.isDebugEnabled();
    }

    /**
     * Returns whether the logging is enabled and  the log level is set to TRACE.
     */
    public static boolean isTraceEnabled() {
        return isLogEnabled() && log.isTraceEnabled();
    }

    /**
     * Sets the logger level to that specified.
     *
     * @param level the logger level to be set valid values are:<br>
     *              <code>ERROR, DEBUG, INFO, WARN, ALL, FATAL, TRACE</code>
     */
    public static void setLevel(String level) {
        log.setLevel(level);
    }

    /**
     * Logs a message at log level INFO.
     *
     * @param message   the log message.
     * @param throwable the throwable.
     */
    public static void info(Object message, Throwable throwable) {
        if (isLogEnabled())
            log.info(message, throwable);
    }

    /**
     * Logs a message at log level WARN.
     *
     * @param message   the log message.
     * @param throwable the throwable.
     */
    public static void warning(Object message, Throwable throwable) {
        if (isLogEnabled())
            log.warning(message, throwable);
    }

    /**
     * Logs a message at log level DEBUG.
     *
     * @param message the log message.
     */
    public static void debug(Object message) {
        if (isLogEnabled())
            log.debug("DEBUG: " + message);
    }

    /**
     * Logs a message at log level DEBUG.
     *
     * @param message   the log message.
     * @param throwable the throwable.
     */
    public static void debug(Object message, Throwable throwable) {
        if (isLogEnabled())
            log.debug("DEBUG: " + message, throwable);
    }

    /**
     * Logs a message at log level TRACE.
     *
     * @param message the log message.
     */
    public static void trace(Object message) {
        if (isLogEnabled())
            log.trace("TRACE: " + message);
    }

    /**
     * Logs a message at log level TRACE.
     *
     * @param message   the log message.
     * @param throwable the throwable.
     */
    public static void trace(Object message, Throwable throwable) {
        if (isLogEnabled())
            log.trace("TRACE: " + message, throwable);
    }

    /**
     * Logs a message at log level ERROR.
     *
     * @param message the log message.
     * @param e       the throwable.
     */
    public static void error(Object message, Throwable e) {
        if (isLogEnabled())
            log.error(message, e);
    }

    /**
     * Logs a message at log level INFO.
     *
     * @param message the log message.
     */
    public static void info(Object message) {
        if (isLogEnabled())
            log.info(message);
    }

    /**
     * Logs a message at log level WARN.
     *
     * @param message the log message.
     */
    public static void warning(Object message) {
        if (isLogEnabled())
            log.warning(message);
    }

    /**
     * Logs a message at log level ERROR.
     *
     * @param message the log message.
     */
    public static void error(Object message) {
        if (isLogEnabled())
            log.error(message);
    }

    public static void printStackTrace(String message) {

        if (!isLogEnabled())
            return;

        info(message);
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        int i = Objects.equals(message, DEFAULT_STACKTRACE_MESSAGE) ? 3 : 2;
        for (; i < stackTraceElements.length; i++)
            info("\tat " + stackTraceElements[i]);
    }

    public static void printStackTrace() {
        printStackTrace(DEFAULT_STACKTRACE_MESSAGE);
    }

}
