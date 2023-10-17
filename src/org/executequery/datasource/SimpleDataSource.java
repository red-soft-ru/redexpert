/*
 * SimpleDataSource.java
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

import biz.redsoft.IFBCryptoPluginInit;
import biz.redsoft.IFBDataSource;
import biz.redsoft.ITPB;
import org.apache.commons.lang.StringUtils;
import org.executequery.ApplicationContext;
import org.executequery.EventMediator;
import org.executequery.ExecuteQuery;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.DefaultConnectionRepositoryEvent;
import org.executequery.gui.LoginPasswordDialog;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"rawtypes"})
public class SimpleDataSource implements DataSource, DatabaseDataSource {

    public static final DriverLoader DRIVER_LOADER = new DefaultDriverLoader();

    static final String PORT = "[port]";
    static final String SOURCE = "[source]";
    static final String HOST = "[host]";

    private Properties properties = new Properties();

    private final Driver driver;
    private IFBDataSource dataSource;
    private final String url;
    private final DatabaseConnection databaseConnection;

    private ClassLoader classLoaderFromPlugin;

    IFBCryptoPluginInit cryptoPlugin = null;

    public SimpleDataSource(DatabaseConnection databaseConnection) {

        this.databaseConnection = databaseConnection;
        if (databaseConnection.hasAdvancedProperties()) {

            populateAdvancedProperties();
        }

        driver = loadDriver(databaseConnection.getJDBCDriver());
        if (driver == null) {

            throw new DataSourceException("Error loading specified JDBC driver");
        }

        url = generateUrl(databaseConnection, properties);
        Log.info("JDBC Driver class: " + databaseConnection.getJDBCDriver().getClassName());
    }

    public Connection getConnection() throws SQLException {
        return getConnection(null);
    }

    public Connection getConnection(ITPB tpb) throws SQLException {
        while (MiscUtils.isNull(databaseConnection.getUnencryptedPassword())
                && databaseConnection.getAuthMethod().contentEquals(Bundles.get("ConnectionPanel.BasicAu"))) {
            LoginPasswordDialog lpd = new LoginPasswordDialog(Bundles.getCommon("title-enter-password"), Bundles.getCommon("message-enter-password"),
                    null, databaseConnection.getUserName());
            lpd.display();
            if (lpd.isClosedDialog())
                throw new DataSourceException("Connection cancelled");
            else {
                databaseConnection.setUserName(lpd.getUsername());
                databaseConnection.setPassword(lpd.getPassword());
                databaseConnection.setPasswordStored(lpd.isStorePassword());
                EventMediator.fireEvent(
                        new DefaultConnectionRepositoryEvent(
                                this, ConnectionRepositoryEvent.CONNECTION_MODIFIED, (DatabaseConnection) null));
            }
        }
        try {
            return getConnection(databaseConnection.getUserName(), databaseConnection.getUnencryptedPassword(), tpb);
        } catch (SQLException e) {
            if (e.getSQLState().contentEquals("28000")&&e.getErrorCode()==335544472
                    && databaseConnection.getAuthMethod().contentEquals(Bundles.get("ConnectionPanel.BasicAu"))) {
                databaseConnection.setPassword("");
                dataSource = null;
                return getConnection(tpb);
            }
            throw e;
        }
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection(username, password, null);
    }

    public Connection getConnection(String username, String password, ITPB tpb) throws SQLException {

        Properties advancedProperties = buildAdvancedProperties();

        // in the case of multifactor authentication, the user name and
        // password may not be specify, if a certificate is specify
        if (!advancedProperties.containsKey("useGSSAuth")) {
            if (StringUtils.isNotBlank(username)) {

                advancedProperties.put("user", username);
            }

            if (StringUtils.isNotBlank(password)) {

                advancedProperties.put("password", password);
            }
        }

        if (driver != null) {
            if (dataSource != null)
                return dataSource.getConnection(tpb);

            // If used jaybird
            if (databaseConnection.getJDBCDriver().getClassName().contains("FBDriver") &&
                    !StringUtils.equalsIgnoreCase(databaseConnection.getConnectionMethod(), "jdbc")) {

                try {

                    // Checking for original jaybird or rdb jaybird...
                    try {
                        Class<?> aClass = driver.getClass().getClassLoader().loadClass("org.firebirdsql.jca.FBSADataSource");
                    } catch (ClassNotFoundException e) {
                        Class<?> aClass = driver.getClass().getClassLoader().loadClass("org.firebirdsql.jaybird.xca.FBSADataSource");
                    }


                    // ...rdb jaybird
                    // in multifactor authentication case, need to initialize crypto plugin,
                    // otherwise get a message, that multifactor authentication will be unavailable
                    if (cryptoPlugin == null) {
                        try {
                            String path = databaseConnection.getJDBCDriver().getPath();
                            path = path.replace("../", "./") + ";" + path.replace("./", "../");
                            path = path.replace(".../", "../");
                            path += ";" + DynamicLibraryLoader.getFbPluginImplPath(driver.getMajorVersion());
                            Object odb = DynamicLibraryLoader.loadingObjectFromClassLoader(driver,
                                    "biz.redsoft.FBCryptoPluginInitImpl",
                                    path);
                            cryptoPlugin = (IFBCryptoPluginInit) odb;
                            // try to initialize crypto plugin
                            cryptoPlugin.init();


                        } catch (Throwable e) {
                            Log.warning("Unable to initialize cryptographic plugin. " +
                                    "Authentication using cryptographic mechanisms will not be available. " +
                                    "Please install the crypto pro library to enable cryptographic modules.");
                            advancedProperties.put("excludeCryptoPlugins", "Multifactor,GostPassword,Certificate");
                        }
                    }

                    if (databaseConnection.useNewAPI()) {
                        try {
                            dataSource = (IFBDataSource) DynamicLibraryLoader.loadingObjectFromClassLoaderWithParams(driver.getMajorVersion(), driver,
                                    "FBDataSourceImpl",
                                    new DynamicLibraryLoader.Parameter(String.class, "FBOONATIVE"));
                        } catch (ClassNotFoundException e) {
                            dataSource = (IFBDataSource) DynamicLibraryLoader.loadingObjectFromClassLoader(driver.getMajorVersion(), driver,
                                    "FBDataSourceImpl");
                        }

                    } else {
                        dataSource = (IFBDataSource) DynamicLibraryLoader.loadingObjectFromClassLoader(driver.getMajorVersion(), driver,
                                "FBDataSourceImpl");
                    }

                    for (Map.Entry<Object, Object> entry : advancedProperties.entrySet()) {
                        dataSource.setNonStandardProperty(entry.getKey().toString(), entry.getValue().toString());
                    }
                    dataSource.setURL(url);
                    classLoaderFromPlugin = dataSource.getClass().getClassLoader();

                    return dataSource.getConnection(tpb);
                } catch (ClassNotFoundException e) {
                    // ...original jaybird
                    return driver.connect(url, advancedProperties);
                }
            } else { // another databases...
                return driver.connect(url, advancedProperties);
            }
        }

        throw new DataSourceException("Error loading specified JDBC driver");
    }

    public ClassLoader getClassLoaderFromPlugin() {
        return classLoaderFromPlugin;
    }

    public void setTPBtoConnection(Connection connection, ITPB tpb) throws SQLException {
        if (dataSource != null) {
            if (connection instanceof PooledConnection)
                connection = ((PooledConnection) connection).getRealConnection();
            dataSource.setTransactionParameters(connection, tpb);
        }
    }

    public long getIDTransaction(Connection connection) throws SQLException {
        if (dataSource != null) {
            if (connection instanceof PooledConnection)
                connection = ((PooledConnection) connection).getRealConnection();
            return dataSource.getIDTransaction(connection);
        }
        return -1;
    }

    private Properties buildAdvancedProperties() {

        Properties advancedProperties = new Properties();
        for (Iterator<?> i = properties.keySet().iterator(); i.hasNext(); ) {

            String key = i.next().toString();
            advancedProperties.put(key, properties.get(key));
        }

        if (!advancedProperties.isEmpty()) {

            Log.debug("Using advanced properties :: " + advancedProperties);
        }

        return advancedProperties;
    }

    protected final Driver loadDriver(DatabaseDriver databaseDriver) {

        return DRIVER_LOADER.load(databaseDriver);
    }

    public static final String generateUrl(DatabaseConnection databaseConnection, Properties properties) {

        String url = databaseConnection.getURL();

        String connectionMethod = databaseConnection.getConnectionMethod();

        if (connectionMethod.equalsIgnoreCase("jdbc")) {
            Log.info("Using user specified JDBC URL: " + url);

        } else {

            url = databaseConnection.getJDBCDriver().getURL();
            Log.info("JDBC URL pattern: " + url);

            url = replacePart(url, databaseConnection.getHost(), HOST);
            url = replacePart(url, databaseConnection.getPort(), PORT);
            url = replacePart(url, databaseConnection.getSourceName(), SOURCE);
            Log.info("JDBC URL generated: " + url);
            Properties clone = (Properties)properties.clone();
            if (clone.getProperty("isc_dpb_repository_pin") != null)
                clone.setProperty("isc_dpb_repository_pin", "********");
            Log.info("JDBC properties: " + clone);

        }

        return url;
    }

    private static String replacePart(String url, String value, String propertyName) {

        if (url.contains(propertyName)) {

            if (MiscUtils.isNull(value)) {

                handleMissingInformationException();
            }

            String regex = propertyName.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
            url = url.replaceAll(regex, value);
        }

        return url;
    }

    private static void handleMissingInformationException() {

        throw new DataSourceException(
                "Insufficient information was provided to establish the connection.\n" +
                        "Please ensure all required details have been entered.");
    }

    protected final void rethrowAsDataSourceException(Throwable e) {

        throw new DataSourceException(e);
    }

    public static Properties buildAdvancedProperties(DatabaseConnection databaseConnection) {
        Properties properties = new Properties();
        Properties advancedProperties = databaseConnection.getJdbcProperties();

        for (Iterator i = advancedProperties.keySet().iterator(); i.hasNext(); ) {

            String key = (String) i.next();
            if (key.equalsIgnoreCase("process_id") || key.equalsIgnoreCase("process_name"))
                continue;
            properties.put(key, advancedProperties.getProperty(key));
        }

        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        String path = null;
        if (ApplicationContext.getInstance().getExternalProcessName() != null &&
                !ApplicationContext.getInstance().getExternalProcessName().isEmpty()) {
            path = ApplicationContext.getInstance().getExternalProcessName();
        }
        if (ApplicationContext.getInstance().getExternalPID() != null &&
                !ApplicationContext.getInstance().getExternalPID().isEmpty()) {
            pid = ApplicationContext.getInstance().getExternalPID();
        }
        properties.setProperty("process_id", pid);
        try {
            if (path == null)
                path = ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            properties.setProperty("process_name", path);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private void populateAdvancedProperties() {
        properties = buildAdvancedProperties(databaseConnection);
    }

    public int getLoginTimeout() {

        return DriverManager.getLoginTimeout();
    }

    public void setLoginTimeout(int timeout) {

        DriverManager.setLoginTimeout(timeout);
    }

    public PrintWriter getLogWriter() {

        return DriverManager.getLogWriter();
    }

    public void setLogWriter(PrintWriter writer) {

        DriverManager.setLogWriter(writer);
    }

    public boolean isWrapperFor(Class<?> iface) {

        return false;
    }

    public <T> T unwrap(Class<T> iface) {

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

    public void close() throws ResourceException {

        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

}




