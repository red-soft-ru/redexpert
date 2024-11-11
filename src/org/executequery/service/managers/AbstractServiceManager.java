package org.executequery.service.managers;

import biz.redsoft.IFBServiceManager;
import org.executequery.databasemediators.ConnectionType;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.log.Log;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/// @author Aleksey Kozlov
public abstract class AbstractServiceManager implements ServiceManager {
    protected final IFBServiceManager serviceManager;

    protected AbstractServiceManager(DatabaseConnection dc, String className) {
        serviceManager = supports(dc) ? init(dc, className) : null;
        setup(dc);
    }

    // --- initialization methods ---

    private IFBServiceManager init(DatabaseConnection dc, String className) {
        return init(loadDriver(dc), className);
    }

    private IFBServiceManager init(Driver driver, String className) {

        if (driver == null) {
            Log.error("Couldn't load [" + className + "] class - driver is null");
            return null;
        }

        try {
            return (IFBServiceManager) DynamicLibraryLoader.loadingObjectFromClassLoader(
                    driver.getMajorVersion(),
                    driver,
                    className
            );

        } catch (ClassNotFoundException e) {
            Log.error("Class [" + className + "] no found", e);
            return null;
        }
    }

    public final void setup(DatabaseConnection dc) {

        if (dc == null)
            return;

        //todo add check for SSH tunnel

        serviceManager.setPort(dc.isSshTunnel() ? dc.getTunnelPort() : dc.getPortInt());
        serviceManager.setHost(dc.isSshTunnel() ? "localhost" : dc.getHost());
        serviceManager.setPassword(dc.getUnencryptedPassword());
        serviceManager.setDatabase(dc.getSourceName());
        serviceManager.setUser(dc.getUserName());

        String charset = dc.getCharset();
        if (!MiscUtils.isNull(charset) && !Objects.equals(charset, "NONE"))
            serviceManager.setCharSet(MiscUtils.getJavaCharsetFromSqlCharset(charset));
    }

    // --- helper methods ---

    private Driver loadDriver(DatabaseConnection dc) {
        Driver driver = null;

        try {

            if (dc == null)
                return DefaultDriverLoader.getDefaultDriver();

            for (Map.Entry<String, Driver> entry : DefaultDriverLoader.getLoadedDrivers().entrySet()) {
                String driverName = entry.getKey();
                if (driverName.startsWith(String.valueOf(dc.getDriverId()))) {
                    driver = entry.getValue();
                    break;
                }
            }

            if (driver == null)
                driver = DefaultDriverLoader.getDefaultDriver();

        } catch (SQLException e) {
            Log.error("Error occurred loading driver", e);
        }

        return driver;
    }

    private boolean supports(DatabaseConnection dc) {

        if (ConnectionType.isEmbedded(dc)) {
            Log.warning("Embedded connecting doesn't supports by the service-manager");
            return false;
        }

        return true;
    }

}
