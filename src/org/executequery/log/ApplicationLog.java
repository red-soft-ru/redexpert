/*
 * ApplicationLog.java
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

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 * Provides methods to the Log4J logging methods.
 *
 * @author   Takis Diakoumis
 */
public class ApplicationLog {

    /** The Log4J Logger object */
    private Logger logger;

    private Appender appender;

    private final String loggerName;
    private final int maxBackupIndex;
    private final String pattern;
    private final String level;
    private final String maxFileSize;
    private final String logFilePath;

    public ApplicationLog(String logFilePath, String loggerName,
            String pattern, String level,
            int maxBackupIndex, String maxFileSize) {

        this.logFilePath = logFilePath;
        this.level = level;
        this.loggerName = loggerName;
        this.pattern = pattern;
        this.maxBackupIndex = maxBackupIndex;
        this.maxFileSize = maxFileSize;
    }

    /**
     * Adds the specified appender to the logger.
     *
     * @param appender - the appender to be added
     */
    public void addAppender(Appender appender) {

        logger().addAppender(appender);
    }

    /**
     * Returns whether the log level is set to DEBUG.
     */
    public boolean isDebugEnabled() {

        return logger().isDebugEnabled();
    }

    /**
     * Returns whether the log level is set to TRACE.
     */
    public boolean isTraceEnabled() {
    	
    	return logger().isTraceEnabled();
    }
    
    /**
     * Sets the logger level to that specified.
     *
     * @param level - the logger level to be set.<br>
     *        Valid values are: ERROR, DEBUG, INFO, WARN, ALL, FATAL, TRACE
     */
    public void setLevel(String level) {

        if (level == null) {

            return;
        }

        level = level.toUpperCase();
        logger().setLevel(levelFromString(level.toUpperCase()));
    }

    /**
     * Logs a message at log level INFO.
     *
     * @param message  the log message.
     * @param throwable the throwable.
     */
    public void info(Object message, Throwable throwable) {

        logger().info(message, throwable);
    }

    /**
     * Logs a message at log level WARN.
     *
     * @param message  the log message.
     * @param throwable the throwable.
     */
    public void warning(Object message, Throwable throwable) {

        logger().warn(message, throwable);
    }

    /**
     * Logs a message at log level DEBUG.
     *
     * @param message  the log message.
     */
    public void debug(Object message) {

        if (logger().isDebugEnabled()) {

            logger().debug(message);
        }

    }

    /**
     * Logs a message at log level DEBUG.
     *
     * @param message  the log message.
     * @param throwable the throwable.
     */
    public void debug(Object message, Throwable throwable) {

        if (logger().isDebugEnabled()) {

            logger().debug(message, throwable);
        }

    }

    /**
     * Logs a message at log level TRACE.
     *
     * @param message  the log message.
     */
    public void trace(Object message) {
        
        if (logger().isTraceEnabled()) {
            
            logger().trace(message);
        }
        
    }
    
    /**
     * Logs a message at log level TRACE.
     *
     * @param message  the log message.
     * @param throwable the throwable.
     */
    public void trace(Object message, Throwable throwable) {
        
        if (logger().isTraceEnabled()) {
            
            logger().trace(message, throwable);
        }
        
    }
    
    /**
     * Logs a message at log level ERROR.
     *
     * @param message  the log message.
     * @param e the throwable.
     */
    public void error(Object message, Throwable e) {

        logger().error(message, e);
    }

    /**
     * Logs a message at log level INFO.
     *
     * @param message  the log message.
     */
    public void info(Object message) {

        logger().info(message);
    }

    /**
     * Logs a message at log level WARN.
     *
     * @param message  the log message.
     */
    public void warning(Object message) {

        logger().warn(message);
    }

    /**
     * Logs a message at log level ERROR.
     *
     * @param message  the log message.
     */
    public void error(Object message) {

        logger().error(message);
    }

    /**
     * Returns whether a logger exists and has been initialised.
     *
     * @return <code>true</code> if initialised |
     *         <code>false</code> otherwise
     */
    public boolean isLogEnabled() {

        return logger != null;
    }

    private void init() {

        init(level);
    }

    private void init(String level) {

        logger = Logger.getLogger(loggerName);

        if (level != null) {

            setLevel(level);

        } else { // default to INFO

            setLevel("INFO");
        }

        Logger.getRootLogger().addAppender(appender());
        initOthers();
    }

    private void initOthers() {

        String[] others = {"org.apache.commons.httpclient", "httpclient.wire"};

        for (String name : others) {

            Logger logger = Logger.getLogger(name);
            logger.addAppender(appender());
            logger.setLevel(Level.ERROR);
        }

    }

    private Appender appender() {

        if (appender == null) {

            try {

                RollingFileAppender fileAppender = new RollingFileAppender(
                        new PatternLayout(pattern), logFilePath, true);

                fileAppender.setMaxBackupIndex(maxBackupIndex);
                fileAppender.setMaxFileSize(maxFileSize);

                appender = fileAppender;

            } catch (IOException e) {

                e.printStackTrace();
            }

        }

        return appender;
    }

    private Logger logger() {

        if (!isLogEnabled()) {

            init();
        }

        return logger;
    }

    private Level levelFromString(String level) {

        if (level.equals("INFO")) {

            return Level.INFO;

        } else if (level.equals("WARN")) {

            return Level.WARN;

        } else if (level.equals("DEBUG")) {

            return Level.DEBUG;

        } else if (level.equals("ERROR")) {

            return Level.ERROR;

        } else if (level.equals("FATAL")) {

            return Level.FATAL;

        } else if (level.equals("TRACE")) {

            return Level.TRACE;

        } else if (level.equals("ALL")) {

            return Level.ALL;
        }

        return Level.INFO; // default
    }

}











