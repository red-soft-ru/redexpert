/*
 * SystemLogger.java
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

import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

/**
 * Base output stream for that logs to registered 
 * Log4J logger instance.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public abstract class SystemLogger extends OutputStream {
    
    /** string buffer for text concat */
    private StringBuffer buf;

    /** matcher to remove new lines from log messages */
    private Matcher newLineMatcher;

    /** Creates a new instance of SystemLogger */
    public SystemLogger() {
        init();
    }

    protected void init() {
        if (buf == null) {
            buf = new StringBuffer();
        }
        if (newLineMatcher == null) {
            newLineMatcher = Pattern.compile("[\n\r]+", 
                                    Pattern.MULTILINE).matcher("");            
        }
    }

    private void log(final String s) {
        Runnable update = new Runnable() {
            public void run() {
                if (s.trim().length() == 0) {
                    return;
                }

                newLineMatcher.reset(s);
                if (s.length() == 1 && newLineMatcher.find()) {
                    return;
                }
                String text = newLineMatcher.replaceAll(" ");
                logText(text);
            }
        };
        SwingUtilities.invokeLater(update);
    }

    public abstract void logText(String text);
    
    public synchronized void write(int b) {
        b &= 0x000000FF;
        char c = (char)b;
        buf.append(String.valueOf(c));
    }

    public synchronized void write(byte[] b, int offset, int length) {
        buf.append(new String(b, offset, length));
    }

    public synchronized void write(byte[] b) {
        buf.append(new String(b));
    }

    public synchronized void flush() {
        synchronized (buf) {
            if (buf.length() > 0) {
                log(buf.toString());
                buf.setLength(0);
            }                
        }

    } 

}











