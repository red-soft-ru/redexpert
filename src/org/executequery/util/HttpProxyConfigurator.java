/*
 * HttpProxyConfigurator.java
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

package org.executequery.util;

import java.util.Properties;

import org.executequery.Constants;
import org.underworldlabs.util.SystemProperties;

public class HttpProxyConfigurator {

    private static final String INTERNET_PROXY_PORT_KEY = "internet.proxy.port";

    private static final String INTERNET_PROXY_HOST_KEY = "internet.proxy.host";

    private static final String HTTP_PROXY_PORT = "http.proxyPort";

    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    
    private static final String INTERNET_PROXY_SET_KEY = "internet.proxy.set";

    public void configureHttpProxy() {
        
        if (SystemProperties.getBooleanProperty(
                Constants.USER_PROPERTIES_KEY, INTERNET_PROXY_SET_KEY)) {
            
            systemProperties().put(HTTP_PROXY_HOST, 
                    systemUserProperty(INTERNET_PROXY_HOST_KEY));
            
            systemProperties().put(HTTP_PROXY_PORT, 
                    systemUserProperty(INTERNET_PROXY_PORT_KEY));

        } else {

            System.getProperties().remove(HTTP_PROXY_HOST);
            System.getProperties().remove(HTTP_PROXY_PORT);
        }

    }

    private String systemUserProperty(String key) {
        return SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, key);
    }

    private Properties systemProperties() {
        return System.getProperties();
    }

}






