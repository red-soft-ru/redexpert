/*
 * DefaultDriverLoader.java
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

package org.executequery.datasource;

import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Takis Diakoumis
 */
public class DefaultDriverLoader implements DriverLoader {

    public static Map<String, Driver> getLoadedDrivers() {
        return LOADED_DRIVERS;
    }

    private static final Map<String, Driver> LOADED_DRIVERS = new HashMap<String, Driver>();

    private static final Class[] parameters = new Class[]{URL.class};

    public Driver load(DatabaseDriver databaseDriver) {

        Driver driver = null;
        String key = key(databaseDriver);
        if (LOADED_DRIVERS.containsKey(key)) {
            return LOADED_DRIVERS.get(key);
        }

        try {

            Class<?> clazz = null;
            String driverName = databaseDriver.getClassName();

            Log.info("Loading JDBC driver class: " + driverName);

            if (!databaseDriver.isDefaultSunOdbc()) {

                String path = databaseDriver.getPath();
                Log.trace("Loading driver from: " + path);

                if (!MiscUtils.isNull(path)) {

                    URL[] urls = MiscUtils.loadURLs(path);
                    if (urls != null) {
                        Log.debug("paths for searching of driver:");
                        for (int i = 0; i < urls.length; i++) {
                            Log.debug(urls[i].getPath());
                        }
                    }
                    DynamicLibraryLoader loader = new DynamicLibraryLoader(urls);
                    clazz = loader.loadLibrary(driverName);

                } else {

                    clazz = loadUsingSystemLoader(driverName);
                }

            } else {

                clazz = loadUsingSystemLoader(driverName);
            }

            Object object = clazz.newInstance();
            driver = (Driver) object;

            Log.info("JDBC driver " + driverName + " loaded - v"
                    + driver.getMajorVersion() + "." + driver.getMinorVersion());

            LOADED_DRIVERS.put(key(databaseDriver), driver);

        } catch (ClassNotFoundException e) {

            handleException("The specified JDBC driver class was not found", databaseDriver, e);

        } catch (MalformedURLException e) {

            handleException("Error loading the driver from the specified path.", databaseDriver, e);

        } catch (InstantiationException e) {

            handleException(e.getMessage(), databaseDriver, e);

        } catch (IllegalAccessException e) {

            handleException("The specified JDBC driver class was not accessible", databaseDriver, e);
        }
        return driver;
    }

    private String key(DatabaseDriver databaseDriver) {

        return databaseDriver.getId() + "-" + databaseDriver.getClassName();
    }

    private Class<?> loadUsingSystemLoader(String driverName) throws ClassNotFoundException {

        return Class.forName(driverName, true, ClassLoader.getSystemClassLoader());
    }

    public void unload(DatabaseDriver databaseDriver) {

        String key = key(databaseDriver);
        if (LOADED_DRIVERS.containsKey(key)) {

            Driver driver = LOADED_DRIVERS.get(key);
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            LOADED_DRIVERS.remove(key);
            driver = null;
        }

    }

    private void handleException(String message, DatabaseDriver databaseDriver, Throwable e) {

        if (Log.isDebugEnabled()) {

            Log.error("Error loading JDBC driver " + databaseDriver.getClassName(), e);
        }

        throw new DataSourceException(message);
    }

}


