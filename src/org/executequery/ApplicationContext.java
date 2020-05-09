/*
 * ApplicationContext.java
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

package org.executequery;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Takis Diakoumis
 */
public final class ApplicationContext {

    private static final String EXECUTEQUERY_BUILD = "executequery.build";

    private static final String SETTINGS_DIR = "executequery.user.settings.dir";

    private static final String USER_HOME_DIR = "executequery.user.home.dir";

    private static final String USER_HOME = "user.home";

    private static final String REPO = "-repo";

    private static final String EXT_EXE_PATH = "-exe_path";

    private static final String EXT_EXE_PID = "-exe_pid";

    private static final String[] PROPERTY_OVERRIDES = {SETTINGS_DIR, USER_HOME_DIR, REPO, EXT_EXE_PATH, EXT_EXE_PID};

    private static ApplicationContext applicationContext;

    private Map<String, String> settings = new HashMap<String, String>();

    private ApplicationContext() {
    }

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


                return getUserSettingsDirectoryName()+fileSeparator();

    }

    public String getRepo() {
        String repo = settings.get(REPO);
        if (repo == null)
            return "";
        return repo;
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

    public String getExternalProcessName() {
        return settings.get(EXT_EXE_PATH);
    }

    public String getExternalPID() {
        return settings.get(EXT_EXE_PID);
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







