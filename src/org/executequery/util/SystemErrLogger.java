/*
 * SystemErrLogger.java
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

package org.executequery.util;

import java.io.IOException;
import java.io.PrintStream;

import org.executequery.log.Log;

/**
 * Output stream for System.err or System.out that logs to registered Log4J
 * logger instance as well as standard System.err output.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class SystemErrLogger extends SystemLogger {

    /** System.out stream identifier */
    public static final int SYSTEM_OUT = 0;

    /** System.err stream identifier */
    public static final int SYSTEM_ERR = 1;

    /** the output type */
    private int outputType;
    
    /** indicates whether to log to standard System.out/err */
    private boolean logToConsole;
    
    /** the system print stream */
    private PrintStream stream;

    /** 
     * Creates a new instance of SystemErrLogger and specifies
     * whether to write to the 'original' specified output stream 
     * also - out of System.out or System.err.
     *
     * @param useConsole - true | false
     */
    public SystemErrLogger(boolean logToConsole, int outputType) {
        init();
        this.outputType = outputType;
        initConsoleOptions(logToConsole);
    }

    public void setUseConsole(boolean logToConsole) {
        initConsoleOptions(logToConsole);
    }

    private void initConsoleOptions(boolean logToConsole) {
        this.logToConsole = logToConsole;
        if (stream == null) {
            if (outputType == SYSTEM_ERR) {
                stream = System.err;
            }
            else {
                stream = System.out;
            }
        }
    }
    
    public synchronized void logText(String text) {
        if (outputType == SYSTEM_OUT) {
            Log.info(text);
        }
        if (outputType == SYSTEM_ERR) {
            Log.error(text);
        }
    }
    
    public synchronized void write(int b) {
        if (logToConsole) {
            stream.write(b);
        }
        super.write(b);
    }

    public synchronized void write(byte[] b, int offset, int length) {
        if (logToConsole) {
            stream.write(b, offset, length);
        }
        super.write(b, offset, length);
    }

    public synchronized void write(byte[] b) {
        if (logToConsole) {
            try {
                stream.write(b);
            } catch (IOException e) {}
        }
        super.write(b);
    }

    public synchronized void flush() {
        if (stream != null) {
            stream.flush();
        }
        super.flush();
    } 

}





