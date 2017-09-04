/*
 * HttpProxyUserPreferenceListener.java
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
import org.executequery.util.HttpProxyConfigurator;

public class HttpProxyUserPreferenceListener extends AbstractUserPreferenceListener
                                         implements UserPreferenceListener {

    private HttpProxyConfigurator httpProxyConfigurator;
    
    public void preferencesChanged(UserPreferenceEvent event) {

        if (event.getEventType() == UserPreferenceEvent.PROXY
                || event.getEventType() == UserPreferenceEvent.ALL) {
         
            httpProxyConfigurator().configureHttpProxy();
        }

    }

    private HttpProxyConfigurator httpProxyConfigurator() {
        
        if (httpProxyConfigurator == null) {
            httpProxyConfigurator = new HttpProxyConfigurator();
        }

        return httpProxyConfigurator;
    }
    
}







