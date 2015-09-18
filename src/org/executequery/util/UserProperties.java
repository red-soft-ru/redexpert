/*
 * UserProperties.java
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

import org.underworldlabs.util.SystemProperties;

public final class UserProperties extends AbstractPropertiesBase {

    private static final String PROPERTY_BUNDLE_NAME = "user";

    private static final String USER_SETTINGS_FILE_KEY = "eq.user.properties";
    
    private static final String DEFAULT_PROPERTIES_BUNDLE_NAME = "defaults";
    
    private static final String DEFAULT_PROPERTIES_BUNDLE_PATH = "org/executequery/eq.default.properties";
    
    private static UserProperties instance;

    private UserProperties() {
        
        if (!SystemProperties.hasProperties(DEFAULT_PROPERTIES_BUNDLE_NAME)) {
            
            loadPropertiesResource(
                    DEFAULT_PROPERTIES_BUNDLE_NAME, DEFAULT_PROPERTIES_BUNDLE_PATH);            
        }

        if (!SystemProperties.hasProperties(PROPERTY_BUNDLE_NAME)) {
            
            Properties defaults = getProperties(DEFAULT_PROPERTIES_BUNDLE_NAME);

            loadProperties(PROPERTY_BUNDLE_NAME, userPropertiesPath(), defaults);            
        }

    }

    public static synchronized UserProperties getInstance() {

        if (instance == null) {
            
            instance = new UserProperties();
        }
        
        return instance;
    }

    protected String propertyBundle() {

        return PROPERTY_BUNDLE_NAME;
    }

    private String userPropertiesPath() {
        
        UserSettingsProperties settings = new UserSettingsProperties();
        
        return settings.getUserSettingsDirectory() + USER_SETTINGS_FILE_KEY; 
    }

}










