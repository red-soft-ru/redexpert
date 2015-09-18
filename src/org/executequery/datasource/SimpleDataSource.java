/*
 * SimpleDataSource.java
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
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
@SuppressWarnings({ "rawtypes" })
public class SimpleDataSource implements DataSource, DatabaseDataSource {

    private static final DriverLoader DRIVER_LOADER = new DefaultDriverLoader();
    
    static final String PORT = "[port]";
    static final String SOURCE = "[source]";
    static final String HOST = "[host]";

    private Properties properties = new Properties();

    private Driver driver;
    private final String url;
    private final DatabaseConnection databaseConnection;

    public SimpleDataSource(DatabaseConnection databaseConnection) {

        this.databaseConnection = databaseConnection;
        if (databaseConnection.hasAdvancedProperties()) {
            
            populateAdvancedProperties();
        }

        driver = loadDriver(databaseConnection.getJDBCDriver());
        if (driver == null) {
            
            throw new DataSourceException("Error loading specified JDBC driver");
        }
        
        url = generateUrl(databaseConnection);
        Log.info("JDBC Driver class: " + driver.getClass().getName());
    }
    
    public Connection getConnection() throws SQLException {

        return getConnection(databaseConnection.getUserName(), databaseConnection.getUnencryptedPassword());
    }

    public Connection getConnection(String username, String password) throws SQLException {

        Properties advancedProperties = buildAdvancedProperties();
        
        if (StringUtils.isNotBlank(username)) {
            
            advancedProperties.put("user", username);
        }

        if (StringUtils.isNotBlank(password)) {

            advancedProperties.put("password", password);
        }

        if (driver != null) {

            return driver.connect(url, advancedProperties);
        }

        throw new DataSourceException("Error loading specified JDBC driver");
    }

    private Properties buildAdvancedProperties() {

        Properties advancedProperties = new Properties();
        
        for (Iterator<?> i = properties.keySet().iterator(); i.hasNext();) {
            
            String key = i.next().toString();
            advancedProperties.put(key, properties.get(key));
        }
        
        return advancedProperties;
    }

    protected final Driver loadDriver(DatabaseDriver databaseDriver) {

        return DRIVER_LOADER.load(databaseDriver);
    }
    
    protected final String generateUrl(DatabaseConnection databaseConnection) {

        String url = databaseConnection.getURL();
        
        if (StringUtils.isBlank(url)) {
        
            url = databaseConnection.getJDBCDriver().getURL();            
            Log.info("JDBC URL pattern: " + url);

            url = replacePart(url, databaseConnection.getHost(), HOST);
            url = replacePart(url, databaseConnection.getPort(), PORT);
            url = replacePart(url, databaseConnection.getSourceName(), SOURCE);
            Log.info("JDBC URL generated: "+url);
    
        } else {
          
            Log.info("Using user specified JDBC URL: " + url);
        }

        return url;
    }

    private String replacePart(String url, String value, String propertyName) {
     
        if (url.contains(propertyName)) {

            if (MiscUtils.isNull(value)) {
            
                handleMissingInformationException();
            }

            String regex = propertyName.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
            url = url.replaceAll(regex, value);
        }

        return url;
    }

    private void handleMissingInformationException() {

        throw new DataSourceException(
                "Insufficient information was provided to establish the connection.\n" +
                "Please ensure all required details have been entered.");
    }

    protected final void rethrowAsDataSourceException(Throwable e) {
        
        throw new DataSourceException(e);
    }

    private void populateAdvancedProperties() {

        Properties advancedProperties = databaseConnection.getJdbcProperties();
        
        for (Iterator i = advancedProperties.keySet().iterator(); i.hasNext();) {

            String key = (String) i.next();
            properties.put(key, advancedProperties.getProperty(key));
        }
        
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

    public String getJdbcUrl() {

        return url;
    }

    public String getDriverName() {
        
        return driver.getClass().getName();
    }

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {

	    return driver.getParentLogger();
	}


}



