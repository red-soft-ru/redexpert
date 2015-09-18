/*
 * ApplicationContext.java
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

package org.executequery;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public final class ApplicationContext {

    private static final String EXECUTEQUERY_BUILD = "executequery.build";

    private static final String SETTINGS_DIR = "executequery.user.settings.dir";

    private static final String USER_HOME_DIR = "executequery.user.home.dir";

    private static final String USER_HOME = "user.home";

    private static final String[] PROPERTY_OVERRIDES = {SETTINGS_DIR, USER_HOME_DIR};
    
    private static ApplicationContext applicationContext;
    
    private Map<String, String> settings = new HashMap<String, String>();

    private ApplicationContext() {}

    public static synchronized ApplicationContext getInstance() {
        
        if (applicationContext == null) {
            
            applicationContext = new ApplicationContext();
        }
        
        return applicationContext;
    }

    private String getUserHome() {
        
        return System.getProperty(USER_HOME);
    }
    
    private String getUserSettingsDirectoryName() {

        // .executequery
        
        return settings.get(SETTINGS_DIR);
    }

    public void setUserSettingsDirectoryName(String settingsDirectoryName) {

        settings.put(SETTINGS_DIR, settingsDirectoryName);
    }

    public String getUserSettingsHome() {

        // ie. /home/user_name/.executequery/
        
        if (!settings.containsKey(USER_HOME_DIR)) { // ie. /home/user_name
        
            settings.put(USER_HOME_DIR, getUserHome());
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(settings.get(USER_HOME_DIR)).
            append(fileSeparator()).
            append(getUserSettingsDirectoryName()).
            append(fileSeparator());

        return sb.toString();
    }

    private String fileSeparator() {
        
        return System.getProperty("file.separator");
    }

    public String getBuild() {
        
        return settings.get(EXECUTEQUERY_BUILD);
    }
    
    public void setBuild(String build) {

        settings.put(EXECUTEQUERY_BUILD, build);
    }

    public void startup(String[] args) {

        if (args != null && args.length > 0) {
            
            for (String arg : args) {
                
                if (isValidStartupArg(arg)) {
                    
                    int index = arg.indexOf("="); 

                    String key = arg.substring(0, index);
                    String value = arg.substring(index + 1);
                    settings.put(key, value);
                }

            }
            
        }
        
    }

    private boolean isValidStartupArg(String arg) {

        for (String key : PROPERTY_OVERRIDES) {
            
            if (arg.contains(key)) {
                
                return true;
            }
        }
            
        return false;
    }
    
}






