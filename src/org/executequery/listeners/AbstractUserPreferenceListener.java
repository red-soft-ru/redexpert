/*
 * AbstractUserPreferenceListener.java
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

package org.executequery.listeners;

import org.executequery.Constants;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.UserPreferenceEvent;
import org.underworldlabs.util.SystemProperties;

public abstract class AbstractUserPreferenceListener {

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof UserPreferenceEvent);
    }

    protected final boolean systemUserBooleanProperty(String key) {
        
        return SystemProperties.getBooleanProperty(Constants.USER_PROPERTIES_KEY, key);
    }
    
    protected final String systemUserProperty(String key) {
        
        return SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, key);
    }
    
}










