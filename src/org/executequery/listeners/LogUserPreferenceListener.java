/*
 * LogUserPreferenceListener.java
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

package org.executequery.listeners;

import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;
import org.executequery.log.Log;
import org.executequery.util.SystemErrLogger;

/**
 * Logging preference change listener.
 *
 * @author   Takis Diakoumis
 */
public class LogUserPreferenceListener extends AbstractUserPreferenceListener
                                       implements UserPreferenceListener {

    private static final String SYSTEM_LOG_OUT_KEY = "system.log.out";

    private static final String SYSTEM_LOG_ERR_KEY = "system.log.err";

    private static final String SYSTEM_LOG_LEVEL_KEY = "system.log.level";

    private final SystemErrLogger errLogger;
    
    private final SystemErrLogger outLogger;
    
    public LogUserPreferenceListener(SystemErrLogger errLogger, 
            SystemErrLogger outLogger) {
        
        this.errLogger = errLogger;
        this.outLogger = outLogger;
    }
    
    public void preferencesChanged(UserPreferenceEvent event) {

        if (event.getEventType() == UserPreferenceEvent.ALL
                || event.getEventType() == UserPreferenceEvent.LOG) {
            
            Log.setLevel(systemUserProperty(SYSTEM_LOG_LEVEL_KEY));

            if (errLogger != null) {

                errLogger.setUseConsole(systemUserBooleanProperty(SYSTEM_LOG_ERR_KEY));
            }

            if (outLogger != null) {

                outLogger.setUseConsole(systemUserBooleanProperty(SYSTEM_LOG_OUT_KEY));
            }

        }
        
    }
    
}











