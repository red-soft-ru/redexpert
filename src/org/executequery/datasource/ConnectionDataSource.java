/*
 * ConnectionDataSource.java
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

package org.executequery.datasource;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.log.Log;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;

/** 
 * Acts as a wrapper to the actual data source and JDBC driver.
 *
 * @author   Takis Diakoumis
 * @version  $Revision$
 * @date     $Date$
 */
@SuppressWarnings({ "rawtypes" })
public class ConnectionDataSource implements DataSource, DatabaseDataSource {
    
    public static final int ODBC = 8;
    
    public static final String PORT = "[port]";
    public static final String SOURCE = "[source]";
    public static final String HOST = "[host]";
    
    private static final String PORT_REGEX = "\\[port\\]";
    private static final String SOURCE_REGEX = "\\[source\\]";
    private static final String HOST_REGEX = "\\[host\\]";
    
    private static final Map<DatabaseDriver,Driver> loadedDrivers = new HashMap<DatabaseDriver,Driver>();

    /** flag indicating whether we are using the ODBC bridge driver */
    protected boolean usingODBC;
    
    /** the generated JDBC URL */
    private String jdbcUrl;
    
    /** driver properties object for this source */
    private DatabaseDriver databaseDriver;
    
    /** the loaded java.sql.Driver */
    private Driver driver;
    
    /** Whether the driver has been loaded */
    private boolean driverLoaded;

    /** the genrated driver connection properties */
    private final Properties properties = new Properties();
        
    /** The database connection object of this data source */
    private final DatabaseConnection databaseConnection;

    public ConnectionDataSource(DatabaseConnection databaseConnection) {

        this.databaseConnection = databaseConnection;
        this.databaseDriver = databaseConnection.getJDBCDriver();

        if (databaseConnection.hasAdvancedProperties()) {
        
            populateAdvancedProperties();
        }

    }
    
    private void populateAdvancedProperties() {

        Properties advancedProperties = databaseConnection.getJdbcProperties();
        
        for (Iterator i = advancedProperties.keySet().iterator(); i.hasNext();) {

            String key = (String) i.next();
            properties.put(key, advancedProperties.getProperty(key));
        }
        
    }

    /* (non-Javadoc)
     * @see org.executequery.datasource.DatabaseDataSource#getJdbcUrl()
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }
    
    private void loadDriver() throws SQLException {
        
        if (databaseDriver == null) {
            
            throw new SQLException("No JDBC driver specified");
        }
        
        try {
            driverLoaded = false;
            int driverType = databaseDriver.getType();
            
            if (driverType == ODBC) {

                usingODBC = true;
            }

            jdbcUrl = databaseConnection.getURL();
            
            // if the url is null - generate it
            if (MiscUtils.isNull(jdbcUrl)) {
                
                /* Generate the JDBC URL as specfied in jdbcdrivers.xml
                 * using the server, port and source values for the connection. */
                String value = null;
                jdbcUrl = databaseDriver.getURL();
                
                Log.info("JDBC URL pattern: "+jdbcUrl);
                
                // check if this url needs the server/host name
                if (jdbcUrl.contains(HOST)) {
                    value = databaseConnection.getHost();
                    if (MiscUtils.isNull(value)) {
                        handleInformationException();
                    }
                    jdbcUrl = jdbcUrl.replaceAll(HOST_REGEX, value);
                }

                // check if this url needs the port number
                if (jdbcUrl.contains(PORT)) {
                    value = databaseConnection.getPort();
                    if (MiscUtils.isNull(value)) {
                        handleInformationException();
                    }
                    jdbcUrl = jdbcUrl.replaceAll(PORT_REGEX, value);
                }

                // check if this url needs the source name
                if (jdbcUrl.contains(SOURCE)) {
                    value = databaseConnection.getSourceName();
                    if (MiscUtils.isNull(value)) {
                        handleInformationException();
                    }
                    jdbcUrl = jdbcUrl.replaceAll(SOURCE_REGEX, value);
                }

                Log.info("JDBC URL generated: "+jdbcUrl);

            } else {
              
                Log.info("Using user specified JDBC URL: "+jdbcUrl);
            }
            
            // check if this driver has already been loaded
            if (loadedDrivers.containsKey(databaseDriver)) {
                
                driver = loadedDrivers.get(databaseDriver);
                driverLoaded = true;
                return;
            }

            Class<?> clazz = null;
            String driverName = databaseDriver.getClassName();

            Log.info("Loading JDBC driver class: " + driverName);
            
            if (!usingODBC) {
                
                String path = databaseDriver.getPath();
                
                if (!MiscUtils.isNull(path)) {
                    URL[] urls = MiscUtils.loadURLs(path);

                    /* Load the JDBC libraries and initialise the driver. */
                    DynamicLibraryLoader loader = new DynamicLibraryLoader(urls);
                    clazz = loader.loadLibrary(driverName);
                
                } else {

                    clazz = Class.forName(driverName, true,
                                          ClassLoader.getSystemClassLoader());
                }

            } else {
                
                clazz = Class.forName(driverName, true,
                                      ClassLoader.getSystemClassLoader());
            } 

            Object object = clazz.newInstance();
            driver = (Driver)object;
            loadedDrivers.put(databaseDriver, driver);
            driverLoaded = true;
            //DriverManager.setLogStream(System.out);
            
        } catch (ClassNotFoundException e) {
          
            if (Log.isDebugEnabled()) {
            
                Log.error("Error loading JDBC driver " + 
                        databaseDriver.getClassName(), e);
            }
            driverLoaded = false;

            throw new SQLException("The specified JDBC driver class was not found");

        } catch (IllegalAccessException e) {
          
            driverLoaded = false;
            throw new SQLException("The specified JDBC driver class was not accessible");

        } catch (MalformedURLException e) {

            driverLoaded = false;
            throw new SQLException(e.getMessage());

        } catch (InstantiationException e) {
        
            driverLoaded = false;
            throw new SQLException(e.getMessage());            
        }

    }

    private void handleInformationException() throws SQLException {
        driverLoaded = false;
        throw new SQLException(
                "Insufficient information was provided to establish the connection.\n" +
                "Please ensure all required details have been entered.");
    }
    
    public Connection getConnection() throws SQLException{
        return getConnection(databaseConnection.getUserName(),
                             databaseConnection.getUnencryptedPassword());
    }
    
    public Connection getConnection(String user, String password)
        throws SQLException {
        
        if (!driverLoaded) {

            loadDriver();
        }

        if (!MiscUtils.isNull(user)) {
        
            properties.put("user", user);
        }

        if (!MiscUtils.isNull(password)) {

            properties.put("password", password);
        }

        //Log.info("Retrieving connection from URL: " + jdbcUrl);
        if (driver != null) {

            return driver.connect(jdbcUrl, properties);
        }

        return null;
    }
    
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }
    
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }
    
    public void setLoginTimeout(int timeout) throws SQLException {
        DriverManager.setLoginTimeout(timeout);
    }
    
    public void setLogWriter(PrintWriter writer) throws SQLException {
        DriverManager.setLogWriter(writer);
    }

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return driver.getParentLogger();
	}


}

